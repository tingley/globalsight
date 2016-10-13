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

import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import com.globalsight.everest.webapp.applet.common.MessageCatalog;


class UIStart extends BaseTerminal
{

    JPopupMenu popupMenu;    
    JMenuItem formsMenu;   

    MessageCatalog msgCat;

    private int startDiameter = 60;
    private JLabel nameLabel = new JLabel ();

    public void setLocation()
    {
        Point framePoint = new Point (pos.x - halfWidth, pos.y - halfHeight);
        nameLabel.setLocation (framePoint.x + 2, framePoint.y + 20);  
    }   

    JLabel getNameLabel()
    {
        return nameLabel;
    }   

    /*public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;      
        float zoomRatio = 1f; // get from panel later.
        shape.paint(g2, pos, color, zoomRatio, selected);
        nameLabel.setText (msgCat.getMsg ("start"));

    } */  

    public void mouseEntered(MouseEvent event)
    {
        oldCursor = gPane.getCursor();
        gPane.setCursor(GVPane.defaultCursor);
    }   

    public void mouseExited(MouseEvent event)
    {
        gPane.setCursor(oldCursor);
    }

    public void mousePressed(MouseEvent event)
    {
        if ((event.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
            maybeShowPopup(event);
        
        if (event.getSource() == nameLabel)
        {
            gPane.setSelObj(this);
            (gPane.getBaseApplet()).fireSelectionEvent();
        }
    }   

    public UIStart(Point p, GraphicalPane inGPane)
    {
        super (p, inGPane);
        msgCat = new MessageCatalog (inGPane.localeName);
        shape = new GCircle (startDiameter, GCircle.START_TYPE); 
        //nameLabel.addMouseListener(this);
        //nameLabel.setSize (56, 20);
        //nameLabel.setForeground (Color.white);
        //nameLabel.setBackground (GraphicalPane.DEFAULT_COLOR);      
        //nameLabel.setHorizontalAlignment (SwingConstants.CENTER); 

        //add(nameLabel);
        // disable righ click
        //add(makePopup());       
    }

    /*private JPopupMenu makePopup()
    {
        popupMenu = new JPopupMenu();
        //Get Lable Name for pageHandler
        formsMenu = new JMenuItem(msgCat.getMsg("Forms"));
        formsMenu.addActionListener(this);
        popupMenu.add(formsMenu);                
        return popupMenu;
    }  */

    public void maybeShowPopup(MouseEvent e) 
    {
        //Hamid: if viewing an archived template, the popup should not
        // be displayed.

        // disable righ click

        //if ( ((GVPane)gPane).isPopupEnabled() )
        //   popupMenu.show(e.getComponent(), e.getX(), e.getY()); 
    }

    // disable righ click

    public void actionPerformed(ActionEvent event)
    {
        /*Object obj = event.getSource();	    
        if(obj == formsMenu)
        {	        
            JFrame _parentFrame = WindowUtil.getFrame (this); 
            StartNodeDlg snd = null;
            if ( modelObject instanceof NodeInstance )
            {
                snd = new StartNodeDlg(_parentFrame, (NodeInstance)modelObject, gPane.getParentApplet());
            }
            else if ( modelObject instanceof Node )
            {
                snd = new StartNodeDlg(_parentFrame, (Node)modelObject, gPane.getParentApplet());
            }
            WindowUtil.center(snd);
            gPane.getParentApplet().displayModal(snd);     
        } */
    }        

    UIObject getDummy()
    {
        UIObject _obj = new UIStart(new Point(100, 100), gPane);        
        _obj.setPosition(new Point(pos.x, pos.y));
        return _obj;
    }
}
