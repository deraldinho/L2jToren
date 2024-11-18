package net.sf.l2j.gameserver.autofarm;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import net.sf.l2j.gameserver.enums.skills.SkillTargetType;
import net.sf.l2j.gameserver.enums.skills.SkillType;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.serverpackets.ExServerPrimitive.Point;
import net.sf.l2j.gameserver.skills.L2Skill;

public class Util
{
	public static String getRemainingTokens(StringTokenizer st)
	{
		final StringBuilder remainingTokens = new StringBuilder();
		while (st.hasMoreTokens())
			remainingTokens.append(st.nextToken() + " ");
		
		return remainingTokens.toString().trim();
	}
	
	public static boolean isSkillCompatible(L2Skill skill)
	{
		if (!skill.isActive() || skill.isSiegeSummonSkill())
			return false;
		
		if (skill.getTargetType().name().contains("CORPSE"))
			return false;
		
		// Sleep individual não faz sentido, vamos atacar o target logo depois e remover esse debuff
		if (skill.getSkillType() == SkillType.SLEEP && skill.getTargetType() == SkillTargetType.ONE)
			return false;
		
		switch (skill.getSkillType())
		{
			case SUMMON:
			case SUMMON_CREATURE:
			case SUMMON_FRIEND:
			case SUMMON_PARTY:
			case COMMON_CRAFT:
			case DWARVEN_CRAFT:
			case TELEPORT:
			case RECALL:
			case RESURRECT:
			case TAKE_CASTLE:
			case SIEGE_FLAG:
			case SEED:
			case SIGNET:
			case SIGNET_CASTTIME:
			case FUSION:
			case STRIDER_SIEGE_ASSAULT:
			case UNLOCK:
				return false;
		}
		
		switch (skill.getTargetType())
		{
			case SUMMON:
				return false;
		}

		switch (skill.getName())
		{
			// Skills "seed"
			case "Aura Symphony":
			case "Inferno":
			case "Blizzard":
			case "Demon Wind":
			case "Elemental Assault":
			case "Elemental Symphony":
			case "Elemental Storm":
			case "Harmony of Noblesse":
			case "Symphony of Noblesse":
				return false;
				
			case "Clan Gate":
				return false;
				
			case "Wyvern Aegis":
				return false;
		}
		
		return true;
	}
	
	public static Comparator<String> getMonsterComparator(Set<String> targets)
	{
		return (m1, m2) ->
		{
		    boolean m1Selected = targets.contains(m1);
		    boolean m2Selected = targets.contains(m2);

		    if (m1Selected && !m2Selected)
		        return -1;
		    else if (!m1Selected && m2Selected)
		        return 1;
		    else
		        return m1.compareTo(m2);
		};
	}
	
	public static Comparator<L2Skill> getSkillComparator(Collection<Integer> skills)
	{
		return (s1, s2) ->
		{
		    boolean m1Selected = skills.contains(s1.getId());
		    boolean m2Selected = skills.contains(s2.getId());

		    if (m1Selected && !m2Selected)
		        return -1;
		    else if (!m1Selected && m2Selected)
		        return 1;
		    else
		        return s1.getName().compareTo(s2.getName());
		};
	}
	
	public static boolean isNodeListEquals(Collection<Point> list1, List<Location> list2)
	{
		if (list1.size() != list2.size())
			return false;
		
		int indice = 0;
		for (Point point : list1)
		{
			final Location loc = list2.get(indice);
			if (point.getX() != loc.getX() || point.getY() != loc.getY() || point.getZ() != loc.getZ())
				return false;
			
			indice++;
		}
		
		return true;
	}
}
