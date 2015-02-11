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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.api.WFApp;
import com.globalsight.everest.webapp.applet.common.EnvoyJPanel;
import com.globalsight.everest.webapp.applet.common.MessageCatalog;
import com.globalsight.everest.workflow.WorkflowArrow;
import com.globalsight.everest.workflow.WorkflowArrowInstance;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowTask;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflow.WorkflowTemplate;

// Graphicalpane will extends EnvoyJPanel instead of JPanel
abstract class GraphicalPane extends EnvoyJPanel implements MouseListener,
        MouseMotionListener
{
    protected static double WIDTH = 680;
    protected static double HEIGHT = 480;
    public double width = WIDTH;
    public double height = HEIGHT;
    protected AffineTransform at;
    protected int x1, y1, x2, y2;
    protected float zoomRatio;
    protected boolean editMode = false;
    public static final Color DEFAULT_COLOR = Color.black;
    public static final Color EVENT_PENDING_COLOR = Color.yellow;
    public static final Color SELECTION_COLOR = Color.red;
    // for GBS-2162
    public static final Color DEFAULT_PATH_COLOR = Color.blue;
    protected WorkflowTemplate model = null;
    TWFAGP twfagp;
    UIObject selObj;
    Vector uiObjects = new Vector();
    public Vector drawLines = new Vector();
    MessageCatalog msgCat;
    WorkflowInstance modelPI = null;
    WorkflowTask modelNode = null;
    WorkflowTaskInstance modelNodeInstance = null;
    WFApp parentApplet;
    protected Dimension size;

    static BaseArrow arrow;
    // Point[] cornerPoints = null;

    // all labels
    static String localeName = "com.globalsight.everest.webapp.applet.common.AppletResourceBundle";
    static String condUdaDisplayValue = "";
    int maxSequenceNo = 0;

    public GraphicalPane()
    {
        at = new AffineTransform();
    }

    public BaseArrow getLastArrow()
    {
        return arrow;
    }

    public void setLastArrow(BaseArrow _arrow)
    {
        arrow = _arrow;
    }

    void setParentApplet(WFApp _applet)
    {
        parentApplet = _applet;
    }

    WFApp getParentApplet()
    {
        return parentApplet;
    }

    TWFAGP getBaseApplet()
    {
        return twfagp;
    }

    public void init(TWFAGP anAgp)
    {
        msgCat = new MessageCatalog(localeName);

        twfagp = anAgp;
        setBackground(Color.white);
        setLayout(null);
        setSize((int) (zoomRatio * width), (int) (zoomRatio * height));

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    int zoom(int aNum)
    {
        return (int) (zoomRatio * aNum);
    }

    int getNodeType()
    {
        // to be override by GVPane
        return GVPane.NO_OP_TYPE;
    }

    abstract void setView() throws Exception;

    // abstract void updateView(PlanEvent _event) throws Exception;
    // abstract void updateView(AWTEvent _event) throws Exception;

    public void drawEventPendingArrows(boolean _visible)
    {
        int i;
        UIObject obj;
        Object model_obj;
        i = uiObjects.size() - 1;

        while (i >= 0)
        {
            obj = (UIObject) uiObjects.elementAt(i--);
            model_obj = obj.getModelObject();

            if (model_obj instanceof WorkflowArrowInstance)
            {
                try
                {
                    if (((WorkflowArrowInstance) model_obj).isActive())
                    {
                        // this arrow has pending event(s)
                        ((BaseArrow) obj).setEventPending(_visible);
                        obj.repaint();
                    }
                }
                catch (Exception e)
                {
                    /*
                     * WindowUtil.showMsgDlg(WindowUtil.getFrame(this),
                     * //msgCat.getMsg( (e.getErrorCode()) ),
                     * msgCat.getMsg("Internal Error!"), WindowUtil.TYPE_ERROR);
                     */
                    return;
                }
            }
        }
    }

    void setSelObj(UIObject sel_obj)
    {
        int i;
        UIObject obj;
        i = uiObjects.size() - 1;
        selObj = null;

        while (i >= 0)
        {
            obj = (UIObject) uiObjects.elementAt(i--);

            if (obj == sel_obj)
            {
                selObj = obj;
                obj.setSelected(true);
            }
            else
            {
                // to support single selection
                obj.setSelected(false);
            }
        }
        repaint();
    }

    public Object[] getSelection()
    {
        Object objList[] = new Object[1];
        if (selObj != null)
        {
            objList[0] = selObj.getModelObject();
            return objList;
        }
        return new Object[0];
    }

    public synchronized void setSelection(Object[] _objList)
    {
        if (_objList == null)
        {

            return;
        }

        int i = 0;
        UIObject obj;
        i = uiObjects.size() - 1;

        if (_objList.length == 0)
        {
            // has no selection
            while (i >= 0)
            {
                obj = (UIObject) uiObjects.elementAt(i--);
                obj.setSelected(false);
                // obj.repaint();
            }
            repaint();
            return;
        }

        // has selection
        // CURRENTLY ONLY SUPPORT NODE SELECTION
        Object _obj = _objList[0]; // selected model obj
        if (!(_obj instanceof WorkflowTask) && !(_obj instanceof WorkflowArrow))
        {

            return;
        }

        Point target_pos = null;

        while (i >= 0)
        {
            obj = (UIObject) uiObjects.elementAt(i--);

            if (((obj.getModelObject() instanceof WorkflowTask)
                    && (_obj instanceof WorkflowTask) && ((WorkflowTask) (obj
                    .getModelObject())).equals((WorkflowTask) _obj))
                    || ((obj.getModelObject() instanceof WorkflowArrow)
                            && (_obj instanceof WorkflowArrow) && ((WorkflowArrow) (obj
                            .getModelObject())).equals((WorkflowArrow) _obj)))
            {
                {
                    selObj = obj;
                    obj.setSelected(true);
                    target_pos = new Point(obj.getPosition());
                }
            }
            else
            {
                // to support single selection
                obj.setSelected(false);
            }
        }

        twfagp.setScrollPosition(target_pos);
        repaint();
    }

    public void paint(Graphics g)
    {
        try
        {
            super.paint(g);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("GraphicalPane :: " + e);
        }
        /*
         * Graphics2D g2 =(Graphics2D)g; // to compose with the current
         * translation, so don't use setTransform g2.transform(at);
         * 
         * Enumeration e; e = uiObjects.elements();
         * 
         * while(e.hasMoreElements()) { UIObject obj =
         * (UIObject)e.nextElement(); try { obj.draw(g2, true); } catch
         * (NullPointerException _ex) { _ex.printStackTrace();
         * 
         * //whenever GraphicalPane paints itself, this exception should not
         * happen //if does, paint must continue and finish. so no op here } }
         * if(drawLines != null) { for ( Enumeration e1 = drawLines.elements();
         * e1.hasMoreElements();) { DrawLine tl = (DrawLine)e1.nextElement();
         * tl.draw(g2); } }
         */

    }

    public void paintComponent(Graphics g)
    {

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // draw a rectangle to zoom in //HF
        // drawZoomRect(g2);

        // to compose with the current translation, so don't use setTransform
        g2.transform(at);

        Enumeration e;
        e = uiObjects.elements();

        while (e.hasMoreElements())
        {
            UIObject obj = (UIObject) e.nextElement();
            try
            {
                obj.draw(g2, true);
            }
            catch (NullPointerException _ex)
            {

                // whenever GraphicalPane paints itself, this exception should
                // not happen
                // if does, paint must continue and finish. so no op here
            }
        }
        if (drawLines != null)
        {
            for (Enumeration e1 = drawLines.elements(); e1.hasMoreElements();)
            {
                DrawLine tl = (DrawLine) e1.nextElement();
                tl.draw(g2);
            }
        }
    }

    /*
     * void drawZoomRect(Graphics2D g2) //for zoomBox, HF { g2.setPaintMode();
     * if (x2 != -1) { Stroke oldStroke = g2.getStroke(); float[] dashPattern =
     * {2.0f, 2.0f}; g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND,
     * BasicStroke.JOIN_MITER, 2.0f, dashPattern, 0.0f)); int xmin =
     * Math.min(x1, x2), ymin = Math.min(y1, y2); int xmax = Math.max(x1, x2),
     * ymax = Math.max(y1, y2); g2.drawRect(xmin, ymin, xmax-xmin, ymax-ymin);
     * g2.setStroke(oldStroke); } }
     */

    void clearPlandetail()
    {
        int m = uiObjects.size();
        UIObject obj = null;
        BaseArrow _arrow = null;
        while (m > 0)
        {
            obj = (UIObject) uiObjects.elementAt(--m);
            if (obj == null)
            {
                continue;
            }
            else if (obj instanceof BaseArrow)
            {
                _arrow = (BaseArrow) obj;
                if (_arrow.getLabelField() != null)
                {
                    remove(_arrow.getLabelField());
                }
            }
            else if (obj instanceof UIActivity)
            {
                ((UIActivity) obj).destruct();
                remove(obj);
            }
            else if (obj instanceof UIStart)
            {
                if (((UIStart) obj).getNameLabel() != null)
                {
                    remove(((UIStart) obj).getNameLabel());
                }
            }
            else if (obj instanceof UIExit)
            {
                if (((UIExit) obj).getNameField() != null)
                {
                    remove(((UIExit) obj).getNameField());
                }
            }
            /*
             * else if (obj instanceof BaseConnection) { if
             * (((BaseConnection)obj).getImageButton() != null) {
             * remove(((BaseConnection)obj).getImageButton()); } }
             */
        }

        // remove all components on the panel
        Component[] components = getComponents();
        for (int i = 0; i < components.length; i++)
        {
            Component c = components[i];
            remove(c);
        }

        uiObjects.removeAllElements();
    }

    void checkToEnlarge(Point p)
    {

        boolean changed = false;
        /*
         * if (p.x + zoomRatio * WIDTH > width) { if (p.x + zoomRatio * WIDTH >
         * 1000) width = p.x + zoomRatio * WIDTH - 300; else width = p.x +
         * zoomRatio * WIDTH; changed = true; }
         */

        if (p.x + zoomRatio * WIDTH > width)
        {
            width = p.x + zoomRatio * WIDTH;
            changed = true;
        }
        if (p.y + zoomRatio * HEIGHT > height)
        {
            height = p.y + zoomRatio * HEIGHT;
            changed = true;
        }
        if (changed)
        {
            setSize((int) (zoomRatio * width), (int) (zoomRatio * height));

            validate();
            repaint();
        }
    }

    void mouseUpThis(MouseEvent ev)
    {
    }

    void mouseDragThis(MouseEvent ev)
    {
    }

    void mouseDownThis(MouseEvent ev)
    {
    }

    void mouseMovedThis(MouseEvent ev)
    {
    }

    public void mouseMoved(MouseEvent event)
    {
        mouseMovedThis(event);
    }

    public void mouseExited(MouseEvent event)
    {
    }

    public void mouseDragged(MouseEvent event)
    {
        mouseDragThis(event);
    }

    public void mouseClicked(MouseEvent event)
    {
    }

    public void mouseEntered(MouseEvent event)
    {
    }

    public void mousePressed(MouseEvent event)
    {
        mouseDownThis(event);
    }

    public void mouseReleased(MouseEvent event)
    {
        mouseUpThis(event);
    }

    public Dimension getPreferredSize()
    {
        size = new Dimension((int) (zoomRatio * width),
                (int) (zoomRatio * height));
        return size;
    }

    public Dimension getPreferredScrollableViewportSize()
    {
        JScrollPane sPane = new JScrollPane();
        JViewport viewport = sPane.getViewport();
        size = new Dimension((int) (zoomRatio * width),
                (int) (zoomRatio * height));
        viewport.setViewSize(size);
        revalidate();

        return size;
    }

    void setFieldsEnabled(boolean new_state)
    {
        UIObject obj;
        for (int i = 0; i < uiObjects.size(); i++)
        {
            obj = (UIObject) uiObjects.elementAt(i);
            if ((obj instanceof UIExit) || (obj instanceof UIActivity)
                    || (obj instanceof BaseArrow))
            {
                obj.setFieldsEnabled(new_state);
            }
        }
    }

    /*
     * public void invalidate() { super.invalidate();
     * 
     * if (uiObjects != null) { int m = uiObjects.size(); UIObject obj = null;
     * while( m > 0 ) { obj = (UIObject)uiObjects.elementAt(--m); if (obj ==
     * null) continue; else ((Component)obj).invalidate(); } } }
     */
}