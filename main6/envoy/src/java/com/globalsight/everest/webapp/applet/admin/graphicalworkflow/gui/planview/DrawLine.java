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
import java.awt.RenderingHints;


public class DrawLine 
{
    public int startX, endX;
    public int startY, endY;

    public DrawLine(int x1, int y1, int x2, int y2)
    {
        startX = x1;
        startY = y1;
        endX = x2;
        endY = y2;
    }

    public void draw(Graphics2D g2)
    {
        int x1 = startX;
        int y1 = startY;
        int x2 = endX;
        int y2 = endY;
        g2.setPaint(GraphicalPane.DEFAULT_COLOR);
        g2.setXORMode(GraphicalPane.SELECTION_COLOR);
        //make line smooth
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawLine(x1, y1, x2, y2);
        g2.setPaintMode();
    }
}