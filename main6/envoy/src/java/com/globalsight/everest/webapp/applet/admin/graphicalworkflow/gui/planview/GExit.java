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

import java.awt.geom.Ellipse2D;  //HF
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
//import java.awt.Graphics2D.*;



class GExit extends GCircle
{
    private int inset = 10;
    private int innerDiameter = diameter - inset * 2;
    private Shape oval1, oval2;                //HF	

    public GExit()
    {
        super(60, GCircle.EXIT_TYPE);               
    }

    public void dragging(java.awt.Graphics2D g2, Point p)
    {
        int d = getDiameter();
        int x = p.x - d / 2;
        int y = p.y - d / 2;
        oval1 = new Ellipse2D.Float(x, y, d, d);   //HF
        oval2 = new Ellipse2D.Float(x+inset, y+inset, innerDiameter, innerDiameter); //HF

        g2.setPaint(GraphicalPane.DEFAULT_COLOR);
        g2.setXORMode(GraphicalPane.SELECTION_COLOR);
        //g2.drawOval(x, y, d, d);
        g2.draw(oval1);                           //HF
        //g2.drawOval(x+inset, y+inset, innerDiameter, innerDiameter);
        g2.draw(oval2);                           //HF
        g2.setPaintMode();
    }

    public void paint(java.awt.Graphics2D g2, Point p, Color c, float zoomRatio)
    {
        // g2.transform(at);                       //HF
        int d = getDiameter();
        int x = p.x - d / 2;
        int y = p.y - d / 2;
        int temp = d;
        temp *= zoomRatio;
        x *= zoomRatio;
        y *= zoomRatio;
        oval1 = new Ellipse2D.Float(x, y, d, d);   //HF
        oval2 = new Ellipse2D.Float(x+inset, y+inset, innerDiameter, innerDiameter); //HF

        g2.setPaint(c);
        g2.fill(oval1);

        g2.setPaint(GraphicalPane.DEFAULT_COLOR);
        g2.draw(oval1);
        g2.fill(oval2);        
    }    
}
