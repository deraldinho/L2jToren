package net.sf.l2j.loginserver.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;

import net.sf.l2j.loginserver.model.Account;

/**
 * Esta classe gerencia todas as operações de banco de dados relacionadas às {@link Account}s.
 */
public class AccountTable
{
	private static final CLogger LOGGER = new CLogger(AccountTable.class.getName());
	
	private static final String SELECT_ACCOUNT = "SELECT password, access_level, last_server FROM accounts WHERE login = ?";
	private static final String INSERT_ACCOUNT = "INSERT INTO accounts (login, password, last_active) VALUES (?, ?, ?)";
	private static final String UPDATE_ACCOUNT_LAST_TIME = "UPDATE accounts SET last_active = ? WHERE login = ?";
	private static final String UPDATE_ACCOUNT_LAST_SERVER = "UPDATE accounts SET last_server = ? WHERE login = ?";
	private static final String UPDATE_ACCOUNT_ACCESS_LEVEL = "UPDATE accounts SET access_level = ? WHERE login = ?";
	
	protected AccountTable()
	{
	}
	
	/**
	 * Recupera uma conta do banco de dados.
	 * @param login O login da conta a ser recuperada.
	 * @return Uma nova instância de {@link Account} com as informações da conta, ou null se não existir.
	 */
	public Account getAccount(String login)
	{
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement ps = con.prepareStatement(SELECT_ACCOUNT))
		{
			ps.setString(1, login);
			
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
					return new Account(login, rs.getString("password"), rs.getInt("access_level"), rs.getInt("last_server"));
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Exceção ao recuperar informações da conta.", e);
		}
		return null;
	}
	
	/**
	 * Cria uma nova conta no banco de dados.
	 * @param login O login da nova conta.
	 * @param hashed A senha já criptografada (hashed).
	 * @param currentTime O timestamp da criação da conta.
	 * @return Uma nova instância de {@link Account} representando a conta criada, ou null se ocorrer um problema.
	 */
	public Account createAccount(String login, String hashed, long currentTime)
	{
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT_ACCOUNT))
		{
			ps.setString(1, login);
			ps.setString(2, hashed);
			ps.setLong(3, currentTime);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Exceção ao criar a conta para {}.", e, login);
			return null;
		}
		
		// Gera uma nova instância de Account.
		return new Account(login, hashed, 0, 1);
	}
	
	/**
	 * Atualiza o timestamp do último acesso de uma conta.
	 * @param login O login da conta a ser atualizada.
	 * @param currentTime O novo timestamp de último acesso.
	 * @return True se a atualização for bem-sucedida, false caso contrário.
	 */
	public boolean setAccountLastTime(String login, long currentTime)
	{
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_ACCOUNT_LAST_TIME))
		{
			ps.setLong(1, currentTime);
			ps.setString(2, login);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Exceção ao atualizar o último acesso da conta {}.", e, login);
			return false;
		}
		return true;
	}
	
	/**
	 * Atualiza o nível de acesso de uma conta no banco de dados.
	 * @param login O login da conta a ser atualizada.
	 * @param level O novo nível de acesso a ser definido.
	 */
	public void setAccountAccessLevel(String login, int level)
	{
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_ACCOUNT_ACCESS_LEVEL))
		{
			ps.setInt(1, level);
			ps.setString(2, login);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Não foi possível definir o nível de acesso {} para {}.", e, level, login);
		}
	}
	
	/**
	 * Atualiza o último servidor acessado por uma conta no banco de dados.
	 * @param login O login da conta a ser atualizada.
	 * @param serverId O ID do último servidor acessado.
	 */
	public void setAccountLastServer(String login, int serverId)
	{
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_ACCOUNT_LAST_SERVER))
		{
			ps.setInt(1, serverId);
			ps.setString(2, login);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Não foi possível definir o último servidor.", e);
		}
	}
	
	public static AccountTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AccountTable INSTANCE = new AccountTable();
	}
}
