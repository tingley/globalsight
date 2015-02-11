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

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Rectangle;

class BaseTerminal extends UIObject
{
    static int halfWidth = 30;
    static int halfHeight = 30;
    private int TNTermDiameter = 80;  

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;     

        shape.paint(g2, pos, color, gPane.zoomRatio, selected);      
    }

    public BaseTerminal(Point p, GraphicalPane inGPane)
    {
        super(p, inGPane);   
        shape = new GCircle(TNTermDiameter);   
        area = new Rectangle(pos.x, pos.y, 60, 60);     
        pos.translate(30, 30);   
    }

    //////////////////////////////////////////////////////////////////////
    //  Begin: Implementation of UIObject's Abstract Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Get the node's default width.
     */
    public int getNodeWidth() 
    {
        return halfWidth * 2;
    }

    /**
     * Get the node's default hight.
     */
    public int getNodeHeight() 
    {
        return halfHeight * 2;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Implementation of UIObject's Abstract Methods
    //////////////////////////////////////////////////////////////////////
}
