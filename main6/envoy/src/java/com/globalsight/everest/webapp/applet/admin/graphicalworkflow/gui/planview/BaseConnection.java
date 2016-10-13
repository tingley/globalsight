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

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

abstract class BaseConnection extends UIObject
{
    protected static final int  DEFAULT_WIDTH = 34 ;
    protected static final int  DEFAULT_HEIGHT = 34 ;

    int p0;
    int p1;
    int p2;
    int p3;
    int p4;
    int p5;
    int p6;
    int side = 34;
    int inset1;
    int inset2;
    int xPoints[];
    int yPoints[];
    int midPoint = side / 2;
    ImageButton imageButton = null;  


    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;        
        //comments TNPane as not using TUmbnailApplet
        /*if (gPane instanceof TNPane) {            
            Point hackPos = new Point (pos.x+20, pos.y+20);
            shape.paint(g2, hackPos, color, gPane.zoomRatio, selected);
        }
        else */
        shape.paint(g2, pos, color, gPane.zoomRatio, selected);    
    }

    public BaseConnection(Point p, GraphicalPane inGPane)
    {
        super(p, inGPane);   
        objectName = "Connection Name";
        area = new Rectangle(pos.x, pos.y, 34, 34);  
        pos.translate(15, 15);

        if (gPane instanceof GVPane)
        {
            imageButton = new ImageButton (((GVPane)gPane).propImage);
            imageButton.addMouseListener(this);
            imageButton.setSize (14, 14);
            imageButton.addActionListener (this);            
        }
    }

    ImageButton getImageButton()
    {
        return imageButton;
    } 

    public void mouseEntered(MouseEvent event)
    {
        oldCursor = gPane.getCursor();
        gPane.setCursor(GVPane.defaultCursor);
    }

    public void mouseExited(MouseEvent event)
    {
        gPane.setCursor(oldCursor);
    }


    public void setLocation()
    {
        imageButton.setLocation (pos.x + 10, pos.y + 10);
    }  

    public void setObjColor(int _state)
    {
        super.setObjColor(_state);
        if (gPane instanceof GVPane)
        {
            imageButton.setBackground (color);
        }
    }

    public void mouseDragged(MouseEvent event)
    {
    }

    public void mousePressed(MouseEvent event)
    {
    }

    public void mouseReleased(MouseEvent event)
    {
    }

    public void actionPerformed(ActionEvent event)
    {
        if ( !selected )
        {
            gPane.setSelObj(this);
            (gPane.getBaseApplet()).fireSelectionEvent();
        }
    }

    public int getNodeWidth()
    {
        return DEFAULT_WIDTH;
    }

    public int getNodeHeight()
    {
        return DEFAULT_HEIGHT ;
    }
}
