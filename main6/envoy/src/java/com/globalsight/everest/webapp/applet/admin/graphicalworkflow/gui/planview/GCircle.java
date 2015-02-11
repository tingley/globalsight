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


class GCircle extends GraphicalShape
{
    protected static final int START_TYPE = -2;
    protected static final int EXIT_TYPE = -3;
    private int m_nodeType = START_TYPE;
    int diameter;
    Shape oval;     


    /**
     * Constructor
     */
    public GCircle(int inDiameter)
    {
        this(inDiameter, START_TYPE);
    }   

    /**
     * Constructor with specified circle diameter and node type (start/exit nodes).
     */
    public GCircle(int inDiameter, int p_nodeType)
    {
        super(new Point(0, 0));
        m_nodeType = p_nodeType;
        setDiameter (inDiameter);
    }   


    public void selected()
    {
    }

    public int getDiameter()
    {
        return diameter;
    }   

    public boolean contains(Point point)
    {
        return oval.contains(point);       
    }   

    public void dragging(Graphics2D g2, Point p)
    {
        int d = getDiameter();
        int x = p.x - d / 2;
        int y = p.y - d / 2;
        oval = new Ellipse2D.Float(x, y, d, d);  

        g2.setPaint(GraphicalPane.DEFAULT_COLOR);
        g2.setXORMode(GraphicalPane.SELECTION_COLOR);       
        g2.draw(oval);   
        g2.setPaintMode();
    }   

    public void paint(Graphics2D g2, Point p, Color c, float zoomRatio)
    {
        //g2.transform(at);
        int d = getDiameter();
        int x = p.x - d / 2;
        int y = p.y - d / 2;
        int temp = d;
        temp *= zoomRatio;
        x *= zoomRatio;
        y *= zoomRatio;
        d *= zoomRatio;

        oval = new Ellipse2D.Float(x, y, d, d);         
        
        g2.setPaint(GraphicalPane.DEFAULT_COLOR);
        g2.fill(oval);              
    }   

    public void paint(Graphics2D g2, Point p, Color c, float zoomRatio, boolean selected)
    {
        // g2.transform(at);
        int d = getDiameter();
        int x = p.x - d / 2;
        int y = p.y - d / 2;
        int temp = d;
        temp *= zoomRatio;
        x *= zoomRatio;
        y *= zoomRatio;
        d *= zoomRatio;

        //TomyD testing --
        g2.setPaint(GraphicalPane.DEFAULT_COLOR);
        paintImage(g2, x, y, m_nodeType);
        if (selected)
        {
            g2.setPaint(GraphicalPane.SELECTION_COLOR);
            // draw the red circle around the image
            oval = new Ellipse2D.Float(x-1, y-1, d+2, d+2);
            g2.draw(oval);  
        }
        // end of TomyD -- image instead of drawing circles

        // TomyD -- main code for drawing the start/exit nodes
        /*oval = new Ellipse2D.Float(x, y, d, d);         
        g2.setPaint(GraphicalPane.DEFAULT_COLOR);

        g2.fill(oval);  
        if (selected)
        {
            oval = new Ellipse2D.Float(x-2, y-2, d+4, d+4);
            g2.setPaint(GraphicalPane.SELECTION_COLOR);         
            g2.draw(oval);  
        } */
    }   

    public void setDiameter(int inDiameter)
    {
        diameter = inDiameter;        
    }    
}