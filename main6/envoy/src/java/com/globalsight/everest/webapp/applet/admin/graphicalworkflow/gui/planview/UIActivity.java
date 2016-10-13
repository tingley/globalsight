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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.globalsight.everest.webapp.applet.common.AppletHelper;
import com.globalsight.everest.webapp.applet.common.MessageCatalog;
import com.globalsight.everest.workflow.WorkflowTask;
import com.globalsight.everest.workflow.WorkflowTaskInstance;

class UIActivity extends BaseActivity
{
    JPopupMenu popupMenu;

    JMenuItem generalMenu;
    /*
     * JMenuItem assigneeMenu; JMenuItem attributeMenu; JMenuItem formMenu;
     * JMenuItem scriptMenu; JMenuItem timerMenu; JMenuItem statusMenu;
     */

    MessageCatalog msgCat;

    private static boolean bComputePreferredSizeCalled = false;
    private static int WIDTH = 60;
    private static int HEIGHT = 70;
    private static int FIELD_WIDTH = 50;
    private static int FIELD_HEIGHT = 20;

    private static final int vGap = 3;
    private static final int hGap = 5;

    public static int getStdWidth()
    {
        computePreferredSize(); // make sure sizes have been computed based on
                                // font
        return WIDTH;
    }

    public static int getStdHeight()
    {
        computePreferredSize(); // make sure sizes have been computed based on
                                // font
        return HEIGHT;
    }

    public void destruct()
    {
        if (roleNameField != null)
            remove(roleNameField);
        if (nameField != null)
            remove(nameField);
    }

    UIObject getDummy()
    {
        UIObject _obj = new UIActivity(new Point(100, 100), gPane);
        _obj.setPosition(new Point(pos.x, pos.y));
        return _obj;
    }

    public void setLocation()
    {
        Point framePoint = new Point(pos.x - WIDTH / 2, pos.y - HEIGHT / 2);
        setLocation(framePoint.x, framePoint.y);
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        float zoomRatio = 1f; // get from panel later.
        Point framePoint = new Point(pos.x - WIDTH / 2, pos.y - HEIGHT / 2);
        shape.paint(g2, framePoint, color, zoomRatio, selected);
    }

    public void buttonPropDown()
    {
        invokeDlg(Constants.GENERALTAB);
    }

    /*
     * public void buttonRoleDown() { invokeDlg (Constants.ROLETAB); }
     * 
     * public void buttonFormDown() { invokeDlg (Constants.FORMSTAB); }
     * 
     * 
     * 
     * public void buttonAttributeDown() { invokeDlg (Constants.USERDEFINEDTAB);
     * }
     * 
     * public void buttonScriptDown() { invokeDlg(Constants.SCRIPTSTAB); }
     * 
     * public void buttonTimerDown() { invokeDlg(Constants.TIMERTAB); }
     * 
     * public void buttonStatusDown() { invokeDlg(Constants.STATUSTAB); }
     */

    public void setObjColor(int _state)
    {
        super.setObjColor(_state);

        Color color2 = color.brighter();
        roleNameField.setOpaque(true);
        nameField.setOpaque(true);
        roleNameField.setBackground(color2);
        nameField.setBackground(color2);
    }

    public void focusGained(FocusEvent event)
    {
        // Note: In netscape-solaris, we're not getting
        // mousePressed/mouseReleased events on
        // click on the textFields. So...
        if (((GVPane) gPane).selObj != this)
        {
            gPane.setSelObj(this);
        }
    }

    public void focusLost(FocusEvent event)
    {
        /*
         * try { /* if (event.getSource() == roleNameField) { roleName =
         * roleNameField.getText();
         * ((WorkflowTask)modelObject).setRole(roleName); }
         * 
         * if (event.getSource() == nameField) { objectName =
         * nameField.getText(); // if name field is blank, reset to default if
         * (objectName.length() == 0) {
         * ((WorkflowTask)modelObject).setName("ActivityName");
         * nameField.setText("ActivityName"); } else
         * ((WorkflowTask)modelObject).setName(objectName); } } catch (Exception
         * e) {
         * 
         * 
         * }
         */
    }

    public void mouseDragged(MouseEvent event)
    {
        event.setSource(this);

        if ((((GVPane) gPane).getNodeType() == GVPane.NO_OP_TYPE)
                || (((GVPane) gPane).selObj instanceof BaseArrow))
        {
            // GVPane not in adding mode, or adding arrow: dragging allowed
            gPane.mouseDragThis(event);
        }
    }

    public void mouseEntered(MouseEvent event)
    {
        event.setSource(this);

        if (!(gPane.getNodeType() == GVPane.ARROW_TYPE))
        {
            oldCursor = gPane.getCursor();
            gPane.setCursor(GVPane.defaultCursor);
        }
        else if ((gPane.getNodeType() == GVPane.ARROW_TYPE))
        {
            oldCursor = gPane.getCursor();
            gPane.setCursor(GVPane.addNodeCursor);
        }
    }

    public void mouseExited(MouseEvent event)
    {
        if ((event.getSource() == this)
                && !(gPane.getNodeType() == GVPane.ARROW_TYPE))
        {
            gPane.setCursor(oldCursor);
        }
    }

    public void mousePressed(MouseEvent event)
    {
        maybeShowPopup(event);

        if (event.getSource() == nameField)
        {
            ((GVPane) gPane).setActivityY(nameField.getHeight() + vGap * 2);
        }
        else if (event.getSource() == roleNameField)
        {
            ((GVPane) gPane).setActivityY(vGap);
        }

        event.setSource(this);
        gPane.setSelObj(this);
        requestFocus();
        gPane.mouseDownThis(event);
    }

    public UIActivity(Point p, GraphicalPane inGPane)
    {
        super(p, inGPane);

        computePreferredSize();
        GRectangle gRect = new GRectangle(p);
        gRect.setSize(WIDTH - 1, HEIGHT - 1);
        shape = gRect;

        msgCat = new MessageCatalog(inGPane.localeName);

        addMouseListener(this);
        addMouseMotionListener(this);

        addKeyListener(new KeyAdapter()
        {
            // So we grab key events here, and forward to gvPane
            public void keyPressed(KeyEvent ev)
            {
                if (gPane instanceof GVPane)
                {
                    ((GVPane) gPane).keyPressed(ev);
                }
            }
        });
        try
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    roleNameField.setText(msgCat.getMsg(roleName));
                    nameField.setText(msgCat.getMsg(objectName));
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        roleNameField.addFocusListener(this);
        roleNameField.addMouseListener(this);
        roleNameField.addKeyListener(this);
        roleNameField.addMouseMotionListener(this);

        nameField.addFocusListener(this);
        nameField.addMouseListener(this);
        nameField.addKeyListener(this);
        nameField.addMouseMotionListener(this);

        setSize(WIDTH, HEIGHT);

        rolePane.setBounds(hGap, vGap + 1, FIELD_WIDTH, FIELD_HEIGHT
                + (FIELD_HEIGHT / 2));
        namePane.setBounds(hGap, 2 * FIELD_HEIGHT - 2 * vGap, FIELD_WIDTH,
                FIELD_HEIGHT + (FIELD_HEIGHT / 2));

        add(rolePane);
        add(namePane);
        add(makePopup());
    }

    private JPopupMenu makePopup()
    {
        popupMenu = new JPopupMenu();

        generalMenu = new JMenuItem(
                AppletHelper.getI18nContent("lb_properties"));

        generalMenu.addActionListener(this);
        popupMenu.add(generalMenu);

        /*
         * assigneeMenu = new JMenuItem(msgCat.getMsg("Assignee"));
         * assigneeMenu.addActionListener(this); popupMenu.add(assigneeMenu);
         * 
         * attributeMenu = new
         * JMenuItem(msgCat.getMsg("User-DefinedAttributes"));
         * attributeMenu.addActionListener(this); popupMenu.add(attributeMenu);
         * 
         * scriptMenu = new JMenuItem(msgCat.getMsg("Scripts"));
         * scriptMenu.addActionListener(this); popupMenu.add(scriptMenu);
         * 
         * formMenu = new JMenuItem(msgCat.getMsg("Forms"));
         * formMenu.addActionListener(this); popupMenu.add(formMenu);
         * 
         * timerMenu = new JMenuItem(msgCat.getMsg("Timers"));
         * timerMenu.addActionListener(this); popupMenu.add(timerMenu);
         * 
         * if (gPane.modelPI != null ) { statusMenu = new
         * JMenuItem(msgCat.getMsg("TimerStatus"));
         * statusMenu.addActionListener(this); popupMenu.add(statusMenu); }
         */
        return popupMenu;
    }

    public void mouseReleased(MouseEvent event)
    {
        if (event.getSource() == nameField)
        {
            ((GVPane) gPane).setActivityY(nameField.getHeight() + vGap * 2);
        }
        else if (event.getSource() == roleNameField)
        {
            ((GVPane) gPane).setActivityY(vGap);
        }

        event.setSource(this);
        maybeShowPopup(event);
        gPane.mouseUpThis(event);
    }

    private void maybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger() && ((GVPane) gPane).isPopupEnabled())
        {
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    public void actionPerformed(ActionEvent event)
    {

        Object obj = event.getSource();
        if (obj == generalMenu/* && showPopup() */)
        {
            buttonPropDown();
        }

        /*
         * if (obj == generalMenu) { buttonPropDown(); } else if (obj ==
         * formMenu) { buttonFormDown(); } else if (obj == assigneeMenu) {
         * buttonRoleDown(); } else if (obj == attributeMenu) {
         * buttonAttributeDown(); } else if (obj == scriptMenu) {
         * buttonScriptDown(); } else if (obj == timerMenu) { buttonTimerDown();
         * } else if (obj == statusMenu) { buttonStatusDown(); }
         */
    }

    public void invokePropertiesDialog()
    {
        // for external/general usage
        invokeDlg(Constants.GENERALTAB);
    }

    public void invokeDlg(int tabNumber)
    {
        Hashtable ht = ((GVPane) gPane).getDialogData();
        if (gPane.modelPI == null)
        {
            ht.put("wft", modelObject);
            Vector data = WorkflowTaskDialog.getDialog((GVPane) gPane, "", ht);
        }
        else
        {
            WorkflowTaskInstance wfti = (WorkflowTaskInstance) modelObject;
            Object taskInfoBean = null;
            if (wfti.getTaskId() == WorkflowTask.ID_UNSET)
            {
                taskInfoBean = ((GVPane) gPane).getTaskInfoBeanMap().get(
                        new Long(wfti.getSequence()));
            }
            else
            {
                taskInfoBean = ((GVPane) gPane).getTaskInfoBeanMap().get(
                        new Long(wfti.getTaskId()));
            }

            ht.put("wft", wfti);
            if (taskInfoBean != null)
            {
                ht.put("taskInfoBean", taskInfoBean);
            }
            else
            {
                ht.remove("taskInfoBean");
            }
            ht.put("modifiedTaskInfoMap",
                    ((GVPane) gPane).getModifiedTaskInfoMap());
            Vector data = WorkflowTaskDialog.getDialog((GVPane) gPane, "", ht);
            ((GVPane) gPane).getTaskInfoBeanMap().putAll(
                    ((GVPane) gPane).getModifiedTaskInfoMap());
        }

        if ((roleName = ((WorkflowTask) modelObject).getDisplayRoleName()) != null)
        {
            if (!roleNameField.getText().equals(roleName))
            {
                ((GVPane) gPane).isModify = true;
            }

            roleNameField.setText(roleName);

        }
        // if ((objectName= ((WorkflowTask)modelObject).getActivityName())
        // !=null )
        if ((objectName = ((WorkflowTask) modelObject).getActivityDisplayName()) != null)
        {
            if (!nameField.getText().equals(objectName))
            {
                ((GVPane) gPane).isModify = true;
            }

            nameField.setText(objectName);
        }
    }

    public void keyReleased(KeyEvent event)
    {
        if (event.getSource() == nameField)
        {
            objectName = nameField.getText();
            try
            {
                ((WorkflowTask) modelObject).setName(objectName);
            }
            catch (Exception e)
            {

            }
        }
        if (event.getSource() == roleNameField)
        {
            String[] roleName =
            { roleNameField.getText() };
            try
            {
                ((WorkflowTask) modelObject).setRoles(roleName);
            }
            catch (Exception e)
            {

            }
        }
    }

    public void keyTyped(KeyEvent event)
    {
        if (event.getSource() == nameField
                || event.getSource() == roleNameField)
            limitText2((JTextArea) event.getSource());
    }

    void draw(Graphics g, boolean mode)
    {
        // overrides super-class' method
        // non-dragging painting is not needed since UIActivity is a panel
        // itself (unlike other nodes)
        Graphics2D g2 = (Graphics2D) g;
        if (!mode)
        {
            shape.dragging(g2, pos);
        }
    }

    // Routine to compute all size/position parameters based on textField's
    // preferred size.
    // This is needed because the UIAcitivty size should grow if larger fonts
    // are used
    private static void computePreferredSize()
    {
        if (bComputePreferredSizeCalled)
            return;
        bComputePreferredSizeCalled = true;

        Dimension textSize = DrawInfo.getPreferredTextSize();

        int wid = textSize.width + 4 * hGap;
        int hei = textSize.height * 2 + vGap + 1;

        WIDTH = Math.max(WIDTH, wid);
        HEIGHT = Math.max(HEIGHT, hei);

        FIELD_WIDTH = Math.max(FIELD_WIDTH, textSize.width);
        FIELD_WIDTH = WIDTH - 2 * hGap; // stretch out text foxes
        FIELD_HEIGHT = Math.max(FIELD_HEIGHT, textSize.height);
    }

    // should not display the popup for a completed task.
    /*
     * private boolean showPopup() { return gPane.modelPI == null ? true :
     * ((WorkflowTaskInstance)modelObject).getTaskState() !=
     * WorkflowConstants.STATE_COMPLETED; }
     */
}
