package com.globalsight.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.UIManager;

public class CloseIcon implements Icon {

   
    private int SIZE = 8;
    private int x_coordinaat;
    private int y_coordinaat;
    private Rectangle iconRect;
    private Color COLOR_CROSS = UIManager.getColor("BLUE");

    
    public CloseIcon() {
    }

    
    public void paintIcon(Component c, Graphics g, int x, int y) {
        this.x_coordinaat = x;
        this.y_coordinaat = y;
        drawCross(g, x, y);
        iconRect = new Rectangle(x, y, SIZE, SIZE);
    }

    
    private void drawCross(Graphics g, int xo, int yo) {
        
        g.setColor(COLOR_CROSS);
        g.drawLine(xo, yo, xo + SIZE, yo + SIZE);
        g.drawLine(xo, yo + 1, xo + (SIZE - 1), yo + SIZE); 
        g.drawLine(xo, yo + SIZE, xo + SIZE, yo); 
        g.drawLine(xo, yo + (SIZE - 1), xo + (SIZE - 1), yo); 
        g.drawLine(xo + 1, yo, xo + SIZE, yo + (SIZE - 1)); 
        g.drawLine(xo + 1, yo + SIZE, xo + SIZE, yo + 1); 

        g.setColor(Color.WHITE);
        g.drawLine(xo + 2, yo, xo + 4, yo + 2); 
        g.drawLine(xo + 6, yo + 4, xo + SIZE, yo + 6); 
        g.drawLine(xo + 2, yo + SIZE, xo + 4, yo + 6); 
        g.drawLine(xo + 6, yo + 4, xo + SIZE, yo + 2); 
    }

  
    public boolean coordinatenInIcon(int x, int y) {
        boolean isInIcon = false;
        if (iconRect != null) {
            isInIcon = iconRect.contains(x, y);
        }
        return isInIcon;
    }

  
    public int getIconWidth() {
        return SIZE;
    }

    public int getIconHeight() {
        return SIZE;
    }
}
