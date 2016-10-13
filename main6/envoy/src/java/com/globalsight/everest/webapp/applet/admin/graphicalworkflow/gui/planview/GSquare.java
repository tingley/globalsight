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

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Graphics;


class GSquare extends GraphicalShape
{
    private int width = 151;
    private int height = 91;
    private Rectangle rectangle = new Rectangle ();


    public GSquare(Point _position)
    {
        super(_position);
        //at = new AffineTransform();
    }   

    public boolean contains(Point _point)
    {
        return rectangle.contains(_point);  
    }   

    public void dragging(java.awt.Graphics2D g2, Point p)
    {
    }    

    public void paint(java.awt.Graphics2D g2, Point p, Color c, float zoomRatio)
    {
        // g2.transform(at);
    }   

    public void paint(java.awt.Graphics2D g2, Point p, Color c, float zoomRatio, boolean selected)
    {
        //g2.transform(at);
        int x = p.x - width/2;
        int y = p.y - height/2;
        int tempx = width;
        int tempy = height;
        tempx *= zoomRatio;
        tempy *= zoomRatio;
        x *= zoomRatio;
        y *= zoomRatio;

        g2.setPaint(c);
        g2.fillRect(x, y, tempx, tempy);
        g2.drawRect(x, y, tempx, tempy);
        rectangle = new Rectangle(x, y, tempx, tempy);

        if (selected)
        {
            g2.setPaint(GraphicalPane.SELECTION_COLOR);
        }
        else
        {
            g2.setPaint(GraphicalPane.DEFAULT_COLOR);
        }
        g2.drawRect (x-2, y-2, tempx+4, tempy+4);
    }
}
