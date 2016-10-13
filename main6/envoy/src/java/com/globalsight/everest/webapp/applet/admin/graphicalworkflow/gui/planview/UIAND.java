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
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JFrame;
import com.globalsight.everest.workflow.WorkflowTask;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
//uncomments later
//import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.dmsview.*;
import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.util.WindowUtil;
import com.globalsight.everest.webapp.applet.common.AppletHelper;
import com.globalsight.everest.webapp.applet.common.MessageCatalog;


class UIAND extends BaseConnection
{

    JPopupMenu popupMenu;

    JMenuItem generalMenu;
    JMenuItem scriptMenu;

    MessageCatalog msgCat;  

    public UIAND(Point p, GraphicalPane inGPane)
    {
        super(p, inGPane);

        //objectName = "And Name";
        objectName = "";
        
        msgCat = new MessageCatalog (inGPane.localeName);

        inset1 = 5;
        inset2 = 8;
        p0 = 0;
        p1 = inset1;
        p2 = inset2;
        p3 = midPoint;
        p4 = side - inset2;
        p5 = side - inset1;
        p6 = side;
        xPoints  = new int[16];
        yPoints  = new int[16];

        xPoints[0] = p0;    yPoints[0] = p0;
        xPoints[1] = p2;    yPoints[1] = p0;
        xPoints[2] = p3;    yPoints[2] = p1;
        xPoints[3] = p4;    yPoints[3] = p0;
        xPoints[4] = p6;    yPoints[4] = p0;
        xPoints[5] = p6;    yPoints[5] = p2;
        xPoints[6] = p5;    yPoints[6] = p3;
        xPoints[7] = p6;    yPoints[7] = p4;
        xPoints[8] = p6;    yPoints[8] = p6;
        xPoints[9] = p4;    yPoints[9] = p6;
        xPoints[10] = p3;   yPoints[10] = p5;
        xPoints[11] = p2;   yPoints[11] = p6;
        xPoints[12] = p0;   yPoints[12] = p6;
        xPoints[13] = p0;   yPoints[13] = p4;
        xPoints[14] = p1;   yPoints[14] = p3;
        xPoints[15] = p0;   yPoints[15] = p2;
        shape = new GPolygon(xPoints, yPoints, p);

        add(makePopup());      
    }

    private JPopupMenu makePopup()
    {
        popupMenu = new JPopupMenu();
        //GET LABEL FORM PAGEHANDLER
        // generalMenu = new JMenuItem(msgCat.getMsg("general"));
        generalMenu = new JMenuItem(AppletHelper.getI18nContent("lb_properties"));

        generalMenu.addActionListener(this);
        popupMenu.add(generalMenu);
        //GET LABEL FORM PAGEHANDLER

        // scriptMenu = new JMenuItem(msgCat.getMsg("scripting"));
        // scriptMenu.addActionListener(this);
        //popupMenu.add(scriptMenu);

        return popupMenu;
    }

    public void maybeShowPopup(MouseEvent e) 
    {
        //Hamid: if viewing an archived template, the popup should not
        // be displayed.
        if ( ((GVPane)gPane).isPopupEnabled() )
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }   

    UIObject getDummy()
    {
        UIObject _obj = new UIAND (new Point(100, 100), gPane);     
        _obj.setPosition(new Point(pos.x, pos.y));
        return _obj;
    }

    public void actionPerformed(ActionEvent event)
    {
        Object obj = event.getSource();     
        if (obj == generalMenu)
        {
            invokeDlg(Constants.GENERALTAB);
        }
        /* else if(obj == scriptMenu){
             invokeDlg(Constants.SCRIPTINGTAB);
          }  */
    }
    public void invokeDlg(int tabNumber)
    {
        JFrame _parentFrame = WindowUtil.getFrame (this); 
        AndNodeDlg and = null;
        if ( modelObject instanceof WorkflowTaskInstance )
        {
            and = new AndNodeDlg(_parentFrame, (WorkflowTaskInstance)modelObject, tabNumber, gPane.getParentApplet());
        }
        else if ( modelObject instanceof WorkflowTask )
        {
            and = new AndNodeDlg(_parentFrame, (WorkflowTask)modelObject, tabNumber, gPane.getParentApplet());
        }
        WindowUtil.center(and);
        gPane.getParentApplet().displayModal(and);             
    }
}