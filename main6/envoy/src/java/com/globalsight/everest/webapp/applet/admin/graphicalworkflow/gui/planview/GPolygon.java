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
import java.awt.Polygon;
import java.awt.Graphics2D;
import java.awt.Color;



class GPolygon extends GraphicalShape
{
    private Polygon polygon;
    private boolean flag = false;

    public GPolygon(int[] inX, int[] inY, Point position)
    {
        super(position);
        //at = new AffineTransform();       //HF 
        polygon = new Polygon (inX, inY, inX.length);
    }

    /**
       Constructor for UIChained Process only
     */
    public GPolygon(int[] inX, int[] inY, Point position,boolean UIChainFlag)
    {
        super(position);
        flag = UIChainFlag;
        //at = new AffineTransform();     //HF     
        polygon = new Polygon (inX, inY, inX.length);
    }

    public boolean contains(Point point)
    {
        return polygon.contains (point);
    }

    public void dragging(Graphics2D g2, Point p)
    {
        Polygon tempPoly = new Polygon (polygon.xpoints, polygon.ypoints,
                                        polygon.npoints);
        tempPoly.translate (p.x, p.y);
        g2.setPaint(GraphicalPane.DEFAULT_COLOR);
        g2.setXORMode(GraphicalPane.SELECTION_COLOR);
        g2.drawPolygon(tempPoly);   
    }

    public void paint(Graphics2D g2, Point p, Color c, float zoomRatio)
    {
        //g2.transform(at);  //HF

        Polygon tempPoly = new Polygon (polygon.xpoints, polygon.ypoints,
                                        polygon.npoints);
        tempPoly.translate (p.x, p.y);
        // Inside color.    	
        g2.setPaint(c);
        g2.fillPolygon(tempPoly);

        // Frame of the polygon.
        g2.setPaint(GraphicalPane.DEFAULT_COLOR);
        g2.drawPolygon(tempPoly);

        //for drawing an arrow inside polygon for UIChainedProcess node only. 
        if (flag)
        {
            g2.setPaint(Color.black);           
            g2.drawLine(p.x + 2, p.y + 16, p.x + 30, p.y + 16);
            //for drawing an arrow head    
            int [] x1Points  = new int[4];
            int [] y1Points  = new int[4];
            x1Points[0] = p.x + 22;
            x1Points[1] = p.x + 30;
            x1Points[2] = p.x + 22;
            x1Points[3] = p.x + 25;

            y1Points[0]  = p.y + 13;
            y1Points[1]  = p.y + 16;
            y1Points[2]  = p.y + 19;
            y1Points[3]  = p.y + 16;

            Polygon tempPoly1 = new Polygon (x1Points, y1Points,x1Points.length);        
            g2.setPaint(Color.black);
            g2.fillPolygon(tempPoly1);
            g2.setPaint(Color.black);
            g2.drawPolygon(tempPoly1);       
        }
    }

    public void paint(Graphics2D g2, Point p, Color c, float zoomRatio, boolean selected)
    {
        // g2.transform(at);  //HF

        Polygon tempPoly = new Polygon (polygon.xpoints, polygon.ypoints,
                                        polygon.npoints);
        tempPoly.translate (p.x, p.y);
        // Inside color.
        g2.setPaint(c);
        g2.fillPolygon(tempPoly);

        // Frame of the polygon.
        if (selected)
        {
            g2.setPaint(GraphicalPane.SELECTION_COLOR);
            g2.drawPolygon(tempPoly);

            //for drawing an arrow inside polygon for UIChainedProcess node only
            if (flag)
            {
                g2.setPaint(Color.black);
                g2.drawLine(p.x + 2, p.y + 16, p.x + 30, p.y + 16);

                //for drawing arrow head
                int [] x1Points  = new int[4];
                int [] y1Points  = new int[4];
                x1Points[0] = p.x + 22;
                x1Points[1] = p.x + 30;
                x1Points[2] = p.x + 22;
                x1Points[3] = p.x + 25;

                y1Points[0]  = p.y + 13;
                y1Points[1]  = p.y + 16;
                y1Points[2]  = p.y + 19;
                y1Points[3]  = p.y + 16;

                Polygon tempPoly1 = new Polygon (x1Points, y1Points,x1Points.length);        
                g2.setPaint(Color.black);
                g2.fillPolygon(tempPoly1);
                g2.setPaint(Color.black);
                g2.drawPolygon(tempPoly1);
            }
        }
        else
        {
            g2.setPaint(GraphicalPane.DEFAULT_COLOR);
            g2.drawPolygon(tempPoly);

            //for drawing an arrow inside polygon for UIChainedProcess node only
            if (flag)
            {
                g2.setPaint(Color.black);
                g2.drawLine(p.x + 2, p.y + 16, p.x + 30, p.y + 16);

                //for drawing arrow head
                int [] x1Points  = new int[4];
                int [] y1Points  = new int[4];
                x1Points[0] = p.x + 22;
                x1Points[1] = p.x + 30;
                x1Points[2] = p.x + 22;
                x1Points[3] = p.x + 25;

                y1Points[0] = p.y + 13;
                y1Points[1] = p.y + 16;
                y1Points[2] = p.y + 19;
                y1Points[3] = p.y + 16;

                Polygon tempPoly1 = new Polygon (x1Points, y1Points,x1Points.length);        
                g2.setPaint(Color.black);
                g2.fillPolygon(tempPoly1);
                g2.setPaint(Color.black);
                g2.drawPolygon(tempPoly1);          
            }
        }
    }
}