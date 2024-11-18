package net.sf.l2j.gameserver.autofarm;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.autofarm.zone.AutoFarmArea;
import net.sf.l2j.gameserver.autofarm.zone.form.ZoneNPolyZ;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.serverpackets.ExServerPrimitive;
import net.sf.l2j.gameserver.network.serverpackets.ExServerPrimitive.Point;

public class ZoneBuilder
{
	private static final CLogger LOGGER = new CLogger(ZoneBuilder.class.getName());
	
	public ExServerPrimitive getDebugPacket(Player player)
	{
		return player.getDebugPacket("ZoneBuilder");
	}
	
	private static ExServerPrimitive getDebugPacket2(Player player)
	{
		return player.getDebugPacket("ZBCylinder");
	}
	
	public Map<Integer, Point> getPoints(Player player)
	{
		return getDebugPacket(player).getPoints();
	}
	
	public List<Location> getPointsLoc(Player player)
	{
		return new ArrayList<>(getPoints(player).values());
	}
	
	public void clearAllPreview(Player player)
	{
		player.clearDebugPackets();
	}
	
	public void clearCylinderPreview(Player player)
	{
		getDebugPacket2(player).reset();
		getDebugPacket2(player).sendTo(player);
	}
	
	public void restoreDebugPoints(Player player, List<Location> location)
	{
		final ExServerPrimitive debug = getDebugPacket(player);
		for (Location loc : location)
			addPoint(debug, loc);
	}
	
	public void removePoint(Player player, int nodeId)
	{
		final ExServerPrimitive debug = getDebugPacket(player);

		// Ponto removido
		debug.getPoints().remove(nodeId);
		
		if (debug.getPoints().isEmpty())
		{
			ThreadPool.execute(() -> clearAllPreview(player));
			return;
		}
		
		// Vamos criar um backup dos nodes dos pontos para reconstruir o debug depois 
		final List<Location> backup = getPointsLoc(player);
		debug.reset();
		
		// Criar novos pontos é importante para atualizar o preview, nomes e ID
		backup.forEach(l -> addPoint(debug, l));

		debug.sendTo(player);
	}
	
	public void addPoint(Player player, Location loc)
	{
		final ExServerPrimitive debug = getDebugPacket(player);
		addPoint(debug, loc);
		debug.sendTo(player);
		AutoFarmManager.getInstance().showZoneWindow(player);
	}
	
	private static void addPoint(ExServerPrimitive debug, Location loc)
	{
		// Novo ponto
		debug.addPoint(String.valueOf(debug.getPoints().size() + 1), Color.RED, true, loc.getX(), loc.getY(), loc.getZ());
		
		// Linha caso haja pontos suficientes
		if (debug.getPoints().size() > 1)
		{
			final Point previousPoint = debug.getPoints().get(debug.getPoints().size() - 1);
			debug.addLine(Color.GREEN, previousPoint.getX(), previousPoint.getY(), previousPoint.getZ(), loc.getX(), loc.getY(), loc.getZ());
		}
	}
	
	public void preview(Player player)
	{
		if (getPoints(player).isEmpty())
			return;
		
		getDebugPacket(player).sendTo(player);
	}
	
	/*
	 * Usamos um pacote diferente porque esse será enviado constatemente
	 */
	public void previewCylinder(Player player, int radius)
	{
		final ExServerPrimitive packet = getDebugPacket2(player);
		packet.reset();
		previewCylinder(player, packet, radius);
		packet.sendTo(player);
	}
	
	private static void previewCylinder(Player player, ExServerPrimitive debug, int radius)
	{
		final int playerX = player.getX();
		final int playerY = player.getY();
		final int playerZ = player.getZ() - 20;
		final int count = (int) (2 * Math.PI * radius / (radius / 5)); // (radius < 100 ? 10 : 200)
		final double angle = 2 * Math.PI / count;

		int prevX = (int) (Math.cos(0) * radius) + playerX;
		int prevY = (int) (Math.sin(0) * radius) + playerY;

		for (int i = 1; i <= count; i++)
		{
			final int x = (int) (Math.cos(angle * i) * radius) + playerX;
			final int y = (int) (Math.sin(angle * i) * radius) + playerY;

			// Horizontal line connecting points on the circumference
			debug.addLine("", Color.YELLOW, true, prevX, prevY, playerZ, x, y, playerZ);

			// Atualiza o ponto anterior para a próxima linha
			prevX = x;
			prevY = y;
		}
	}
	
	/*
	 * O zoneId é enviado porque ele pode ainda nao ter sido setado
	 */
	public void previewFinalArea(AutoFarmProfile fp, int areaId)
	{
		final ExServerPrimitive packet = getDebugPacket(fp.getPlayer());
		packet.reset();
		fp.getAreaById(areaId).visualizeZone(packet);
		packet.sendTo(fp.getPlayer());
	}
	
	public void setAutoFarmAreaZone(Player player, AutoFarmArea area)
	{
		final List<Location> nodes = area.getNodes(); // banco de dados
		if (nodes.isEmpty()) // nova ou atualizando existente
			nodes.addAll(getPoints(player).values());

		final int minZ = nodes.stream().mapToInt(Location::getZ).min().orElse(0) - 100;
		final int maxZ = nodes.stream().mapToInt(Location::getZ).max().orElse(0) + 100;

		int[] aX = new int[nodes.size()];
		int[] aY = new int[nodes.size()];
		int[] aZ = new int[nodes.size()];

		if (Config.DEVELOPER)
		{
			final StringBuilder sb = new StringBuilder();
			sb.append(String.format("<zone shape=\"%s\" minZ=\"%d\" maxZ=\"%d\">\n", "NPoly", minZ, maxZ));
			sb.append(String.format("\t<stat name=\"name\" val=\"%s\"/>\n", area.getName()));

			for (Location loc : nodes)
				sb.append(String.format("\t<node x=\"%d\" y=\"%d\" z=\"%d\"/>\n", loc.getX(), loc.getY(), loc.getZ()));

			sb.append("</zone>");
			LOGGER.info(sb.toString());
		}

		for (int i = 0; i < nodes.size(); i++)
		{
			aX[i] = nodes.get(i).getX();
			aY[i] = nodes.get(i).getY();
			aZ[i] = nodes.get(i).getZ();
		}

		area.setZone(new ZoneNPolyZ(aX, aY, aZ, minZ, maxZ));
	}
	
	/*
	 * Calculate the area using the Shoelace formula
	 */
	public double calculateArea(Player player)
	{
		final List<Location> nodes = getPointsLoc(player);
		final int n = nodes.size();
		double area = 0.0;

		for (int i = 0; i < n; i++)
		{
			int j = (i + 1) % n;
			area += nodes.get(i).getX() * nodes.get(j).getY() - nodes.get(i).getY() * nodes.get(j).getX();
		}

		area = Math.abs(area) / 2.0;

		// Calculate the height
	//	    double height = Math.abs(_z2 - _z1);

		// Calculate the volume
	//	    return area * height;
		return area;
	}

	public double calculatePerimeter(Player player)
	{
		final List<Location> nodes = getPointsLoc(player);
		final int n = nodes.size();
		double perimeter = 0.0;

		for (int i = 0; i < n; i++)
		{
			int j = (i + 1) % n;
			double dx = nodes.get(i).getX() - nodes.get(j).getX();
			double dy = nodes.get(i).getY() - nodes.get(j).getY();
			perimeter += Math.sqrt(dx * dx + dy * dy);
		}

		return perimeter;
	}
	
	public static ZoneBuilder getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ZoneBuilder INSTANCE = new ZoneBuilder();
	}
}