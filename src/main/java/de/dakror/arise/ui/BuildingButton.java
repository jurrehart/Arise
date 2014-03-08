package de.dakror.arise.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import de.dakror.arise.game.Game;
import de.dakror.arise.game.building.Building;
import de.dakror.arise.game.building.Center;
import de.dakror.arise.layer.CityHUDLayer;
import de.dakror.arise.layer.CityLayer;
import de.dakror.arise.settings.Resources.Resource;
import de.dakror.arise.util.Assistant;
import de.dakror.gamesetup.ui.button.IconButton;
import de.dakror.gamesetup.util.Helper;

/**
 * @author Dakror
 */
public class BuildingButton extends IconButton
{
	Building building;
	BufferedImage tooltipCache;
	int tooltipRows, tooltipHeight;
	
	public BuildingButton(int x, int y, int width, int height, Image img, Building building)
	{
		super(x, y, width, height, img);
		mode1 = true;
		this.building = building;
		tooltip = building.getName();
	}
	
	@Override
	public void update(int tick)
	{
		checkIfCanEffort();
	}
	
	@Override
	public void drawTooltip(int x, int y, Graphics2D g)
	{
		Color c = g.getColor();
		ArrayList<Resource> resources = building.getBuildingCosts().getFilled();
		ArrayList<Resource> products = building.getProducts().getFilled();
		
		if (tooltipCache == null)
		{
			int width = 250;
			tooltipRows = Helper.getLineCount(building.getDescription(), width - 40, g, 25);
			tooltipHeight = tooltipRows * 25 + 75 + resources.size() * 30 + (products.size() > 0 ? 55 + products.size() * 30 : 30) + (building.getMinCityLevel() > 0 ? 35 : 0);
			
			tooltipCache = new BufferedImage(width, tooltipHeight, BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D g2 = (Graphics2D) tooltipCache.getGraphics();
			Helper.setRenderingHints(g2, true);
			g2.setFont(g.getFont());
			
			Helper.drawShadow(0, 0, width, tooltipHeight, g2);
			g2.setColor(Color.white);
			Helper.drawString(tooltip, 20, 50, g2, 40);
			Helper.drawStringWrapped(building.getDescription(), 30, 80, width - 40, g2, 25);
			Helper.drawString("Baukosten", 25, 50 + tooltipRows * 25 + 35, g2, 30);
			if (products.size() > 0) Helper.drawString("Produktion", 25, 80 + tooltipRows * 25 + 35 + resources.size() * 30, g2, 30);
			for (int i = 0; i < products.size(); i++)
			{
				Resource r = products.get(i);
				int f = building.getProducts().get(r) * Game.world.getSpeed();
				int sc = building.getScale().get(r) * Game.world.getSpeed();
				
				String pr = f + "";
				if (pr.length() > 3) pr = pr.substring(0, pr.length() - 3) + "k";
				if (pr.length() > 5) pr = pr.substring(0, pr.length() - 5) + "m";
				
				String scs = sc + "";
				if (scs.length() > 3) scs = scs.substring(0, scs.length() - 3) + "k";
				if (scs.length() > 5) scs = scs.substring(0, scs.length() - 5) + "m";
				
				Assistant.drawLabelWithIcon(30, 80 + tooltipRows * 25 + 40 + resources.size() * 30 + i * 30 + (building.getMinCityLevel() > 0 ? 60 : 0), 25, new Point(r.getIconX(), r.getIconY()), (f > 0 ? "+" : "") + pr + "/h" + (sc > 0 ? " (+" + scs + "/lvl)" : ""), 30, g2);
			}
		}
		else g.drawImage(tooltipCache, x, y - tooltipCache.getHeight(), null);
		
		for (int i = 0; i < resources.size(); i++)
		{
			Resource r = resources.get(i);
			if (r.isUsable())
			{
				boolean en = CityLayer.resources.get(r) >= building.getBuildingCosts().get(r);
				g.setColor(en ? Color.white : Color.red);
			}
			else
			{
				boolean en = CityLayer.resources.get(r) < building.getBuildingCosts().get(r);
				g.setColor(en ? Color.decode("#18acf1") : Color.red);
			}
			Assistant.drawResource(building.getBuildingCosts(), r, x + 30, y - tooltipHeight + 100 + tooltipRows * 25 + i * 30, 25, 30, g);
		}
		
		if (building.getMinCityLevel() > 0)
		{
			g.setColor(CityHUDLayer.cl.city.getLevel() >= building.getMinCityLevel() ? Color.white : Color.red);
			Helper.drawString("min. Stadtlevel: " + (building.getMinCityLevel() + 1), x + 25, y - tooltipHeight + 80 + tooltipRows * 25 + 40 + resources.size() * 30 + products.size() * 30, g, 25);
		}
		
		g.setColor(c);
	}
	
	public void checkIfCanEffort()
	{
		ArrayList<Resource> resources = building.getBuildingCosts().getFilled();
		
		boolean canEffort = true;
		for (int i = 0; i < resources.size(); i++)
		{
			Resource r = resources.get(i);
			if (r.isUsable())
			{
				boolean en = CityLayer.resources.get(r) >= building.getBuildingCosts().get(r);
				if (!en) canEffort = false;
			}
			else
			{
				boolean en = CityLayer.resources.get(r) < building.getBuildingCosts().get(r);
				if (!en) canEffort = false;
			}
		}
		
		if (CityHUDLayer.cl.city.getLevel() < building.getMinCityLevel()) canEffort = false;
		
		if (CityLayer.resources.get(Resource.BUILDINGS) >= new Center(0, 0, CityHUDLayer.cl.city.getLevel()).getScalingProducts().get(Resource.BUILDINGS)) canEffort = false;
		
		enabled = canEffort;
	}
}
