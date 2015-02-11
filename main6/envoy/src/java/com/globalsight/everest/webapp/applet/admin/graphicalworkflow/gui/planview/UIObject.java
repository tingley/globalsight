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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JPanel;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Cursor;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.FocusEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.KeyListener;
import java.awt.event.FocusListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import com.globalsight.everest.workflow.WorkflowTask;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.util.WindowUtil;
import com.globalsight.everest.webapp.applet.common.AppletHelper;
import com.globalsight.everest.webapp.applet.common.MessageCatalog;



abstract class UIObject extends JPanel implements ActionListener, MouseListener, MouseMotionListener, KeyListener, FocusListener
{
    static final int STATE_INACTIVE = 0;
    static final int STATE_ACTIVE = 1;
    static final int STATE_COMPLETE = 2;
    static final int STATE_WAITING_ON_SUB_PROCESS = 3;
    //private int TEXTLIMIT = Node.MAX_NODE_NAME_LENGTH;
    //replaced node to workflowconstants
    private int   TEXTLIMIT = WorkflowConstants.MAX_NODE_NAME_LENGTH;

    boolean selected = false;
    int state = STATE_INACTIVE;
    static final Color COLOR_INACTIVE = new Color (153, 204, 204);
    static final Color COLOR_ACTIVE = new Color (51, 153, 102);
    static final Color COLOR_COMPLETE = new Color (102, 153, 204);
    static final Color COLOR_EVENT_PENDING = new Color (255, 0, 255);
    static final Color COLOR_WAITING = new Color (255, 225, 0);
    Color color;
    Object modelObject = null;
    String objectName = "WFObject Name";
    String roleName = new String ();
    Point pos = new Point (0, 0);
    Rectangle area = null;  
    GraphicalPane gPane;
    GraphicalShape shape;
    Cursor oldCursor;
    MessageCatalog msgCat = new MessageCatalog (GraphicalPane.localeName);
    Polygon arrowArea = null;    

    public UIObject(GraphicalPane inGPane)
    {
        gPane = inGPane;      
    }

    public UIObject(Point _p, GraphicalPane inGPane)
    {
        pos = _p;
        setLayout (null);
        gPane = inGPane;       
    }

    //////////////////////////////////////////////////////////////////////
    //  Begin: Abstract Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Get the width of the graphical node.
     */
    abstract public int getNodeWidth() ;

    /**
     * Get the height of the graphical node.
     */
    abstract public int getNodeHeight() ;
    //////////////////////////////////////////////////////////////////////
    //  End: Abstract Methods
    //////////////////////////////////////////////////////////////////////
  

    Point getPosition()
    {
        return pos;
    }

    void setPosition(Point _pos)
    {
        pos = _pos;
    }

    Rectangle getArea()
    {
        return area;
    }

    void setArea(Rectangle _area)
    {
        area = _area;
    }

    Polygon getArrowArea()
    {
        return arrowArea;
    }

    void setArrowArea(Polygon _area)
    {
        arrowArea = _area;
    }

    String getRoleName()
    {
        return roleName;
    }

    void setRoleName(String role_name)
    {
        roleName = role_name;
    }

    String getObjectName()
    {
        //differentiating from Component.getName()
        return objectName;
    }

    void setObjectName(String obj_name)
    {
        //differentiating from Component.setName()
        objectName = obj_name;
    }

    int getState()
    {
        return state;
    }

    void setState(int _state)
    {
        state = _state;
    }

    Object getModelObject()
    {
        return modelObject;
    }

    void setModelObject(Object _obj)
    {
        modelObject = _obj;
    }

    void setColor(Color _color)
    {
        color = _color;
    }

    public void translate(int x, int y)
    {
        pos.translate(x, y);
        area.translate(x, y);   
    }

    public boolean isSelected()
    {
        return selected;
    }

    public void commitEdit()
    {
    }


    public void setSelected(boolean inSelected)
    {
        selected = inSelected;
    }

    public void setLocation()
    {
    }

    public void setObjColor(int _state)
    {
        if (gPane.modelPI == null)
        {    // Template view
            color = COLOR_INACTIVE;
            setBackground (color);
            return;
        }

        switch (_state)
        {
            case STATE_ACTIVE:
                color = COLOR_ACTIVE; break;
            case STATE_INACTIVE:
                color = COLOR_INACTIVE; break;
            case STATE_COMPLETE:
                color = COLOR_COMPLETE; break;
            case STATE_WAITING_ON_SUB_PROCESS:
                color = COLOR_WAITING; break;
            default:
                color = COLOR_INACTIVE; break;
        }

        setBackground (color);
    }

    public void keyTyped(KeyEvent event)
    {
    }

    public void keyPressed(KeyEvent event)
    {
    }

    public void keyReleased(KeyEvent event)
    {
    }

    public void focusLost(FocusEvent event)
    {
    }

    public void focusGained(FocusEvent event)
    {
    }

    public void mouseMoved(MouseEvent event)
    {
    }

    public void mouseDragged(MouseEvent event)
    {
    }

    public void mouseClicked(MouseEvent event)
    {
    }

    public void mouseExited(MouseEvent event)
    {
    }

    public void mouseEntered(MouseEvent event)
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
    }

    protected void limitText(JTextField textField)
    {
        String st = textField.getText();
        if (st.length() >= TEXTLIMIT)
        {
            WindowUtil.showMsgDlg(WindowUtil.getFrame(textField),
                                  AppletHelper.getI18nContent("msg_arrow_max_length"),
                                  "",
                                  WindowUtil.TYPE_ERROR);
            int carPos = textField.getCaretPosition();
            textField.setText (st.substring(0, carPos-1) + st.substring(carPos));
            textField.setCaretPosition(carPos-1);
        }
    }    

    protected void limitText2(JTextArea textArea)
    {
        String st = textArea.getText();
        if (st.length() >= TEXTLIMIT)
        {
            //JOptionPane.showMessageDialog(WindowUtil.getFrame(textArea),"You have exceeded the text limit of 31 characters.","TextLimit",JOptionPane.WARNING_MESSAGE);

            /*WindowUtil.showMsgDlg(WindowUtil.getFrame(textArea),
                                  msgCat.getMsg("The node's name can not be longer than 30 characters."),
                                  msgCat.getMsg("Internal Error!"),
                                  WindowUtil.TYPE_ERROR);*/
            int carPos = textArea.getCaretPosition();
            textArea.setText (st.substring(0, carPos-1) + st.substring(carPos));
            textArea.setCaretPosition(carPos-1);
        }
    }

    void handleDoubleClicks(JTextField textField, MouseEvent event)
    {
        if (event.getClickCount() >= 2)
            textField.selectAll();
    }

    void handleDoubleClicks2(JLabel textArea, MouseEvent event)
    {
        //if (event.getClickCount() >= 2)
        //  textArea.selectAll();
    }


    void setFieldsEnabled(boolean _state)
    {
        //overriden by relevant sub-classes
    }

    /**
        Utility method to enable/disable textFields. Needed
        because disabling in Netscape is done differently
     */
    protected void enableTextfield(JTextField tf, boolean new_state)
    {
        tf.setEditable(new_state);
        /*if (WindowUtil.isUsingNetscape())
        {
            // In netscape/solaris - disabled text not readable. So we make them readonly
            tf.setEditable(new_state);
        }
        else
        {
            tf.setEnabled(new_state);
        }*/
    }

    void draw(Graphics g, boolean mode)
    {
        //overriden by UIActivity sub-classe
        Graphics2D g2 =(Graphics2D)g;
        if (mode)
        {
            paintComponent(g);
        }
        else
        {
            shape.dragging(g2, pos);
        }
    }
}
