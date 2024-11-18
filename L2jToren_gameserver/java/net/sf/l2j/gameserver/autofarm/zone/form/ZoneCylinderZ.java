package net.sf.l2j.gameserver.autofarm.zone.form;

import java.awt.Color;

import net.sf.l2j.gameserver.model.zone.form.ZoneCylinder;
import net.sf.l2j.gameserver.network.serverpackets.ExServerPrimitive;

public class ZoneCylinderZ extends ZoneCylinder
{
	public ZoneCylinderZ(int x, int y, int z1, int z2, int rad)
	{
		super(x, y, z1, z2, rad);
	}
	
	@Override
	public void visualizeZone(String info, ExServerPrimitive debug)
	{
		final int count = (int) (2 * Math.PI * _rad / STEP);
	    final double angle = 2 * Math.PI / count;

	    int prevX = (int) (Math.cos(0) * _rad) + _x;
	    int prevY = (int) (Math.sin(0) * _rad) + _y;

	    for (int i = 1; i <= count; i++)
	    {
	        final int x = (int) (Math.cos(angle * i) * _rad) + _x;
	        final int y = (int) (Math.sin(angle * i) * _rad) + _y;

	        // Horizontal line connecting points on the circumference
	        debug.addLine("", Color.YELLOW, true, prevX, prevY, _z1, x, y, _z1);

	        // Atualiza o ponto anterior para a próxima linha
	        prevX = x;
	        prevY = y;
	    }
	}
}
