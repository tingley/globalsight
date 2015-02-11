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
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.FocusEvent;
import com.globalsight.everest.workflow.WorkflowTask;
//import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.util.*;

class UIExit extends BaseTerminal
{
    //private JTextField nameField = new JTextField ();
    private JLabel nameField = new JLabel ();
    private int nameFieldWidth = -1;
    private boolean nameFieldWidthComputed = false; 
    private static int MAXWIDTH = 50;

    public void setLocation()
    {
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;      

        float zoomRatio = 1f; // get from panel later.
        shape.paint(g2, pos, color, zoomRatio, selected);

        if (gPane instanceof GVPane)
        {
            setLocation (g2);
            //nameField.setSize (getNameFieldPreferredWidth(g2),
            //DrawInfo.getPreferredTextSize().height);
            if (getNameFieldPreferredWidth(g2) > MAXWIDTH)
            {
                nameField.setSize (MAXWIDTH,
                                   DrawInfo.getPreferredTextSize().height);
            }
            else
            {
                nameField.setSize (getNameFieldPreferredWidth(g2),
                                   DrawInfo.getPreferredTextSize().height);
            }

        }
    }

    JLabel getNameField()
    {
        return nameField;
    }

    public void focusLost(FocusEvent event)
    {
        if (event.getSource() == nameField)
        {
            objectName = nameField.getText();
            try
            {
                //((Node)modelObject).setName(objectName);
                ((WorkflowTask)modelObject).setName(objectName);
                recomputeNameFieldWidth(null);
            }
            catch (Exception e)
            {

                /*WindowUtil.showMsgDlg(WindowUtil.getFrame(this),
                                     // msgCat.getMsg( (e.getErrorCode()) ),
                                      msgCat.getMsg("Internal Error!"),
                                      WindowUtil.TYPE_ERROR);   */
            }
        }
    }

    public UIExit(Point p, GraphicalPane inGPane)
    {
        super (p, inGPane);    
        shape = new GExit ();  
        //nameField.addFocusListener (this);
        objectName = msgCat.getMsg ("exit");
        /*nameField.setForeground (Color.white);
        nameField.setText (objectName);
        //nameField.addMouseListener (this);
        //nameField.addKeyListener (this);
        add (nameField);*/
    }

    public void setLocation(Graphics2D g2)
    {
        if (g2 == null) return;

        if (getNameFieldPreferredWidth(g2) > MAXWIDTH)
        {
            nameField.setLocation (pos.x-MAXWIDTH/2,
                                   pos.y-DrawInfo.getPreferredTextSize().height/2);   

        }
        else
        {
            nameField.setLocation (pos.x-getNameFieldPreferredWidth(g2)/2,
                                   pos.y-DrawInfo.getPreferredTextSize().height/2);   
        }
    }

    /*public void keyTyped(KeyEvent event)
    {
        if (event.getSource() == nameField)
        {
            limitText (nameField);           
        }
    } */

    public void setObjColor(int _state)
    {
        super.setObjColor(_state);
        nameField.setBackground(color);   
    }

    public void mousePressed(MouseEvent event)
    {
        if (event.getSource() == nameField)
        {
            gPane.setSelObj(this);
            (gPane.getBaseApplet()).fireSelectionEvent();
            //handleDoubleClicks (nameField, event);
        }
    }

    void setFieldsEnabled(boolean new_state)
    {
        //enableTextfield(nameField, new_state);
    }

    UIObject getDummy()
    {
        UIObject _obj = new UIExit(new Point(100, 100), gPane);        
        _obj.setPosition(new Point(pos.x, pos.y));
        return _obj;
    }

    private int getNameFieldPreferredWidth(Graphics2D g2)
    {
        if (!nameFieldWidthComputed)
        {
            recomputeNameFieldWidth(g2);
        }
        return nameFieldWidth;
    }

    private void recomputeNameFieldWidth(Graphics2D g2)
    {

        Font fon = nameField.getFont();
        if (fon == null)
        {
            fon = getFont();
            if (fon == null)
            {
                nameFieldWidth = nameField.getPreferredSize().width;
                return;
            }
        }
        FontMetrics fm  = Toolkit.getDefaultToolkit().getFontMetrics(fon);
        nameFieldWidth = fm.stringWidth(objectName) + 5;

        int prefWidth = nameField.getPreferredSize().width + 1;

        nameFieldWidth = Math.min(nameFieldWidth, prefWidth);
        nameFieldWidthComputed = true;
    }
}
