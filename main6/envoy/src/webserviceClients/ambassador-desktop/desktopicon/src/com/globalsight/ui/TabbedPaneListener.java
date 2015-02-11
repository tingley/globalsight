package com.globalsight.ui;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

public class TabbedPaneListener extends MouseAdapter
{

	public void mouseReleased(MouseEvent e)
	{

		super.mouseReleased(e);

		ClosableTabbedPane tabPane = (ClosableTabbedPane) e.getSource();
		if (tabPane.isEnabled())
		{

			int tabIndex = getTabByCoordinate((JTabbedPane) tabPane, e.getX(),
					e.getY());

			if (tabIndex >= 0 && tabPane.isEnabledAt(tabIndex))
			{

				CloseIcon closeIcon = (CloseIcon) tabPane.getIconAt(tabIndex);
				if (closeIcon.coordinatenInIcon(e.getX(), e.getY()))
				{
					tabPane.remove(tabIndex);
				}
			}
		}
	}

	private int getTabByCoordinate(JTabbedPane pane, int x, int y)
	{

		Point p = new Point(x, y);

		int tabCount = pane.getTabCount();
		for (int i = 0; i < tabCount; i++)
		{
			if (pane.getBoundsAt(i).contains(p.x, p.y))
			{
				return i;
			}
		}
		return -1;
	}

}
