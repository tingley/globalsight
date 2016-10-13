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


class UICondition extends BaseConnection
{

    JPopupMenu popupMenu;

    JMenuItem decisionMenu;
    JMenuItem scriptMenu;

    MessageCatalog msgCat;

    public UICondition(Point p, GraphicalPane inGPane)
    {
        super(p, inGPane);

        msgCat = new MessageCatalog (inGPane.localeName);
        objectName = msgCat.getMsg("conditionNode");
        int outset = 9;
        int x0 = - outset;
        int x1 = midPoint;
        int x2 = side + outset;

        int y0 = 0;
        int y1 = midPoint;
        int y2 = side;

        xPoints  = new int[4];
        yPoints  = new int[4];

        xPoints[0] = x0;    yPoints[0] = y1;
        xPoints[1] = x1;    yPoints[1] = y0;
        xPoints[2] = x2;    yPoints[2] = y1;
        xPoints[3] = x1;    yPoints[3] = y2;
        shape = new GPolygon(xPoints, yPoints, p);

        add(makePopup()); 
    }


    private JPopupMenu makePopup()
    {
        popupMenu = new JPopupMenu();
        // get label from page handler
        // decisionMenu = new JMenuItem(msgCat.getMsg("Decisions"));
        decisionMenu = new JMenuItem(AppletHelper.getI18nContent("lb_properties"));

        decisionMenu.addActionListener(this);
        popupMenu.add(decisionMenu);

        /*scriptMenu = new JMenuItem(msgCat.getMsg("scripting"));
        scriptMenu.addActionListener(this);
        popupMenu.add(scriptMenu); */

        return popupMenu;
    }

    public void maybeShowPopup(MouseEvent e) 
    {
        //TomyD -- For this release (4.4) we're not going to display the dialog.
        // The dialog box will be modified and then displayed in later release.
        /*if ( ((GVPane)gPane).isPopupEnabled() )
            popupMenu.show(e.getComponent(), e.getX(), e.getY());*/
    }    

    UIObject getDummy()
    {
        UIObject _obj = new UICondition (new Point(100, 100), gPane);        
        _obj.setPosition(new Point(pos.x,pos.y));
        return _obj;
    }

    public void actionPerformed(ActionEvent event)
    {

        Object obj = event.getSource();
        if (obj == decisionMenu)
        {
            invokeDlg(Constants.DECISIONTAB);
        }
        /* else if (obj == scriptMenu)
         {
             System.out.println("Script Menu");
             invokeDlg(Constants.SCRIPTINGTAB);
         } */
    }
    public void invokeDlg(int tabNumber)
    {
        JFrame _parentFrame = WindowUtil.getFrame (this); 
        CondNodeDlg cnd = null;
        if ( modelObject instanceof WorkflowTaskInstance )
        {
            cnd = new CondNodeDlg(_parentFrame, (WorkflowTaskInstance)modelObject, gPane.getParentApplet(),tabNumber);
        }
        else if ( modelObject instanceof WorkflowTask )
        {
            cnd = new CondNodeDlg(_parentFrame, (WorkflowTask)modelObject, gPane.getParentApplet(),tabNumber);
        }
        WindowUtil.center(cnd);
        gPane.getParentApplet().displayModal(cnd);        
    }
}
