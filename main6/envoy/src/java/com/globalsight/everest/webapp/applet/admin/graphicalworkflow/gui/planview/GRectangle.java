/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.planview;

import java.awt.Point;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.FontMetrics;
//import java.awt.geom.*;
//import java.awt.Graphics2D.*;


class GRectangle extends GraphicalShape
{
    private int width = 91;
    private int height = 69;
    private int halfHeight = height / 2;
    private int halfWidth = width / 2;

    public void selected()
    {
    }

    public GRectangle(Point _position)
    {
        super(_position);       
    }

    public void setSize(int width, int height)
    {
        this.width = width;
        this.height = height;
        halfWidth = width/2;
        halfHeight = height/2;
    }

    public boolean contains(Point _point)
    {
        int halfHeight = height / 2;
        int halfWidth = width / 2;
        return(_point.x > position.x-halfWidth && _point.x < position.x+halfWidth
               && _point.y > position.y-halfHeight && _point.y < position.y+halfHeight);
    }

    public void dragging(java.awt.Graphics2D g2, Point p)
    {
        int x = p.x - halfWidth;
        int y = p.y - halfHeight;

        g2.setPaint(GraphicalPane.DEFAULT_COLOR);//shailaja
        g2.setXORMode(GraphicalPane.SELECTION_COLOR);
        g2.drawRect(x, y, width, height);
        g2.setPaintMode();   
    }

    public void paint(java.awt.Graphics2D g2, Point p, Color c, float zoomRatio)
    {
        FontMetrics fm = g2.getFontMetrics();
        int x = p.x;
        int y = p.y;

        g2.setPaint(c);
        g2.fillRect(0, 0, width, height);       
        g2.setPaint(GraphicalPane.DEFAULT_COLOR);
        g2.drawRect(0, 0, width, height);    
    }

    public void paint(java.awt.Graphics2D g2, Point p, Color c, float zoomRatio, boolean selected)
    {
        FontMetrics fm = g2.getFontMetrics();
        int x = p.x - width/2;
        int y = p.y - height/2;
        int tempx = width;
        int tempy = height;
        tempx *= zoomRatio;
        tempy *= zoomRatio;
        x *= zoomRatio;
        y *= zoomRatio;

        g2.setPaint(c);
        g2.fillRect(x, y, (int)(width*zoomRatio), (int)(height*zoomRatio));

        if (selected)
        {
            g2.setPaint(GraphicalPane.SELECTION_COLOR);
        }
        else
        {
            g2.setPaint(GraphicalPane.DEFAULT_COLOR);
        }
        if ( zoomRatio == 1f )
        {
            g2.drawRect(0, 0, (int)(width*zoomRatio), (int)(height*zoomRatio));
        }
        else
        {
            g2.drawRect(x-1, y-1, (int)(width*zoomRatio) + 1, (int)(height*zoomRatio) + 1);
        }       

        //TomyD start
        /*test(g2, x, y, 0);// 0 for deactive node
        if (selected)
        {
            g2.setPaint(GraphicalPane.SELECTION_COLOR);
        }
        else
        {
            g2.setPaint(GraphicalPane.DEFAULT_COLOR);
        }
        if ( zoomRatio == 1f )
        {
            test(g2, 0, 0, 0);// 0 for deactive node
        }
        else
        {
            test(g2, x-1, y-1, 0);// 0 for deactive node
        } */       
        //TomyD end
        
    }
}