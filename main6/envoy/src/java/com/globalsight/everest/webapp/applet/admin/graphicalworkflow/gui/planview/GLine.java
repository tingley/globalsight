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


class GLine extends GraphicalShape
{
    int width = 90;
    int height = 90;    

    public GLine(Point position)
    {
        super(position);
    }   

    public boolean contains(Point point)
    {
        int halfHeight = height / 2;
        int halfWidth = width / 2;
        return(point.x > position.x-halfWidth && point.x < position.x+halfWidth
               && point.y > position.y-halfHeight && point.y < position.y+halfHeight);
    }   

    public void dragging(java.awt.Graphics2D g2, Point p)
    {
    }

    public void paint(java.awt.Graphics2D g2, Point p, Color c, float zoomRatio, boolean selected)
    {
        //g2.transform(at);
    }   

    public void paint(java.awt.Graphics2D g2, Point p, Color c, float zoomRatio)
    {
        // g2.transform(at);
    }
}
