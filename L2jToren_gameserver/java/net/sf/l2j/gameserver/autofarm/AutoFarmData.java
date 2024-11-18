package net.sf.l2j.gameserver.autofarm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.stream.Collectors;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;

import net.sf.l2j.gameserver.autofarm.AutoFarmManager.AutoFarmType;
import net.sf.l2j.gameserver.autofarm.zone.AutoFarmArea;
import net.sf.l2j.gameserver.autofarm.zone.AutoFarmRoute;
import net.sf.l2j.gameserver.autofarm.zone.AutoFarmZone;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;

public class AutoFarmData
{
	private static final CLogger LOGGER = new CLogger(AutoFarmManager.class.getName());
	
	private static final String LOAD_AREAS = "SELECT * FROM autofarm_areas WHERE player_id = ?";
	private static final String LOAD_NODES = "SELECT * FROM autofarm_nodes WHERE area_id IN (%s) ORDER BY node_id";
	private static final String INSERT_AREA = "INSERT INTO autofarm_areas (player_id, area_id, name,type) VALUES (?,?,?,?)";
	private static final String INSERT_NODES = "INSERT INTO autofarm_nodes (node_id, area_id, loc_x, loc_y, loc_z) VALUES (?,?,?,?,?)";
	private static final String DELETE_AREA = "DELETE FROM autofarm_areas WHERE player_id = ? AND area_id = ?";
	private static final String DELETE_NODES = "DELETE FROM autofarm_nodes WHERE area_id = ?";
	
	/*
	 * Restauramos somente o essencial. O resto é criado conforme a demanda
	 */
	public void restorePlayer(Player player)
	{
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement ps1 = con.prepareStatement(LOAD_AREAS))
		{
			ps1.setInt(1, player.getObjectId());
			
			try (ResultSet rset = ps1.executeQuery())
			{
				while (rset.next())
				{
					final AutoFarmProfile profile = AutoFarmManager.getInstance().getProfile(player);
					final AutoFarmType type = AutoFarmType.valueOf(rset.getString("type"));
					final int areaId = rset.getInt("area_id");
					
					if (type == AutoFarmType.ZONA)
					{
						profile.getAreas().put(areaId, new AutoFarmZone(areaId, rset.getString("name"), rset.getInt("player_id")));
					}
					else if (type == AutoFarmType.ROTA)
					{
						profile.getAreas().put(areaId, new AutoFarmRoute(areaId, rset.getString("name"), rset.getInt("player_id")));
					}
				}
			}

			// Nenhuma zona foi encontrada no banco de dados
			if (AutoFarmManager.getInstance().getPlayer(player.getObjectId()) == null)
				return;
			
			// Lista de areas do player, vamos restaurar somente os nodes delas
			final List<Integer> areaIds = AutoFarmManager.getInstance().getProfile(player).getAreas().values().stream().map(AutoFarmArea::getId).toList();
			if (areaIds.isEmpty())
				return;
			
			final AutoFarmProfile profile = AutoFarmManager.getInstance().getProfile(player);
			final String placeholders = areaIds.stream().map(id -> "?").collect(Collectors.joining(","));
			
			try (PreparedStatement ps2 = con.prepareStatement(String.format(LOAD_NODES, placeholders)))
			{
				for (int i = 0; i < areaIds.size(); i++)
					ps2.setInt(i + 1, areaIds.get(i));
				
				try (ResultSet rset = ps2.executeQuery())
				{
					while (rset.next())
					{
						profile.getAreaById(rset.getInt("area_id")).getNodes().add(new Location(rset.getInt("loc_x"), rset.getInt("loc_y"), rset.getInt("loc_z")));
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Não foi possível restaurar as AutoFarmArea do player {}.", e, player.getName());
		}
	}
	
	public void deleteArea(int playerId, int areaId)
	{
		try (Connection con = ConnectionPool.getConnection())
		{
			try (PreparedStatement ps1 = con.prepareStatement(DELETE_AREA))
			{
				ps1.setInt(1, playerId);
				ps1.setInt(2, areaId);
				ps1.execute();
			}
			
			try (PreparedStatement ps2 = con.prepareStatement(DELETE_NODES))
			{
				ps2.setInt(1, areaId);
				ps2.execute();
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Não foi possível deletar a AutoFarmArea id #{}.", e, areaId);
		}	
	}
	
	public void insertNodes(AutoFarmArea area)
	{
		try (Connection con = ConnectionPool.getConnection())
		{
			try (PreparedStatement ps1 = con.prepareStatement(DELETE_NODES))
			{
				ps1.setInt(1, area.getId());
				ps1.execute();
			}
			
			try (PreparedStatement ps2 = con.prepareStatement(INSERT_NODES))
			{
				int indice = 0;
				for (Location loc : area.getNodes())
				{
					ps2.setInt(1, indice);
					ps2.setInt(2, area.getId());
					ps2.setInt(3, loc.getX());
					ps2.setInt(4, loc.getY());
					ps2.setInt(5, loc.getZ());
					ps2.addBatch();
					indice++;
				}
				ps2.executeBatch();
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Não foi possível salvar os nodes da AutoFarmArea id #{}.", e, area.getId());
		}
	}
	
	public void insertArea(int playerId, AutoFarmArea area)
	{
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT_AREA))
		{
			ps.setInt(1, playerId);
			ps.setInt(2, area.getId());
			ps.setString(3, area.getName());
			ps.setString(4, area.getType().name());
			ps.execute();
			
			area.setIsFromDb();
		}
		catch (Exception e)
		{
			LOGGER.error("Não foi possível salvar a AutoFarmArea id #{}.", e, area.getId());
		}
	}
	
	public static final AutoFarmData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final AutoFarmData INSTANCE = new AutoFarmData();
	}
}
