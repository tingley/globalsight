package com.globalsight.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.KeyListener;

import javax.swing.ImageIcon;

import com.globalsight.ui.MainFrame;

public class SwingHelper
{
	public static Component getRootPane(Component p_component)
	{
		Component rootPane = p_component.getParent();
		for(;rootPane != null;)
		{
			if(rootPane.getParent() == null)
			{
				break;
			}
			rootPane = rootPane.getParent();
		}
		return rootPane;
	}
	
	public static MainFrame getMainFrame()
	{
		Frame[] fs = MainFrame.getFrames();
		for (int i = 0; i < fs.length; i++)
		{
			if(fs[i] instanceof MainFrame)
			{
				return (MainFrame) fs[i];
			}
		}
		return null;
	}
	
	public static ImageIcon getAmbassadorIcon()
	{
		ImageIcon icon = new ImageIcon(Constants.AMB_ICON);
		return icon;
	}
	
	public static Image getAmbassadorIconImage()
	{
		Image iconImage = getAmbassadorIcon().getImage();
		return iconImage;
	}
	
	public static void addKeyListener(Component p_c, KeyListener p_listener)
	{
		if (p_c != null)
		{
			p_c.addKeyListener(p_listener);
			
			Component[] cs = null;
			
			if (p_c instanceof Container)
			{
				cs = ((Container) p_c).getComponents();
			}
			
			for (int i = 0; i < cs.length; i++)
			{
				addKeyListener(cs[i], p_listener);
			}
		}		
	}
}
