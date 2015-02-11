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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.globalsight.everest.webapp.applet.common.AppletHelper;
import com.globalsight.everest.workflow.WorkflowArrow;
import com.globalsight.everest.workflow.WorkflowTask;

class BaseArrow extends UIObject
{
    private static final int INITIAL_VALUE = 1;
    private static int labelCount = INITIAL_VALUE;
    private int x1 = 0;
    private int y1 = 0;
    private int x2 = 0;
    private int y2 = 0;
    private boolean eventPending = false;
    private boolean isDefault = true;
    private double aHeadC;
    private double aHeadS;
    private int aHeadH;

    private String oldText;
    private Point headPos = new Point(0, 0);
    private Polygon headArea = null;
    private Polygon shape = new Polygon();
    private UIObject sourceNode = null;
    private UIObject targetNode = null;
    private JTextField labelField;
    private int labelFieldWidth = -1;
    private boolean labelFieldWidthComputed = false;
    private int startX, startY, endX, endY;
    private int startLineX, startLineY, endLineX, endLineY;
    private Vector drawLines = new Vector();
    private Vector drawBufferedLines = new Vector();
    private Point intermediatePoints[] = null;
    private Point originalIPoints[] = null;
    private Line2D line = new Line2D.Double();
    public boolean labelChanged = false;

    public BaseArrow(GraphicalPane _pane)
    {
        super(_pane);
        setParameters(_pane);
    }

    public BaseArrow(Point _p1, GraphicalPane _pane)
    {
        super(_p1, _pane);
        objectName = msgCat.getMsg("action") + labelCount++;
        area = new Rectangle(pos.x - 4, pos.y - 4, 8, 8);
        setParameters(_pane);
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: Implementation of UIObject's Abstract Methods
    // ////////////////////////////////////////////////////////////////////
    public int getNodeWidth()
    {
        return -1;
    }

    public int getNodeHeight()
    {
        return -1;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Implementation of UIObject's Abstract Methods
    // ////////////////////////////////////////////////////////////////////
    JTextField getLabelField()
    {
        return labelField;
    }

    void setLabelField(JTextField _field)
    {
        labelField = _field;

        // Added by Hamid:
        labelField.addKeyListener(new KeyAdapter()
        {
            public void keyTyped(KeyEvent e)
            {
                // validateArrowName();
                sizeArrowTextField();
            }
        });
    }

    UIObject getSourceNode()
    {
        return sourceNode;
    }

    void setSourceNode(UIObject source_node)
    {
        sourceNode = source_node;
    }

    UIObject getTargetNode()
    {
        return targetNode;
    }

    void setTargetNode(UIObject target_node)
    {
        targetNode = target_node;
    }

    Polygon getHeadArea()
    {
        return headArea;
    }

    public Point[] getIntermediatePoints()
    {
        return intermediatePoints;
    }

    public void setIntermediatePoints(Point[] corner)
    {
        intermediatePoints = corner;
    }

    public void setOriginalIPoints(Point[] corner)
    {
        originalIPoints = corner;
    }

    Point getHeadPosition()
    {
        return headPos;
    }

    void setHeadPosition(Point new_pos)
    {
        headPos = new_pos;
    }

    void moveHeadPosition(int _x, int _y)
    {
        headPos.move(_x, _y);
    }

    static void initLabelCount(int init_value)
    {
        labelCount = init_value;
    }

    // this will change the object name by incrementing the label count.
    // Method is used when validating outgoing arrow names of a condition node.
    void changeObjectName()
    {
        objectName = msgCat.getMsg("action") + labelCount++;
    }

    private void setParameters(GraphicalPane _pane)
    {
        if (_pane instanceof GVPane)
        {
            aHeadH = 15;
            aHeadC = java.lang.Math.cos(3.1415 * 20 / 180);
            aHeadS = java.lang.Math.sin(3.1415 * 20 / 180);
            gPane = _pane;// HF
        }
        /*
         * else if (_pane instanceof TNPane) { aHeadH = 6; aHeadC =
         * java.lang.Math.cos (3.1415 * 30 / 180);//overwrite parent class
         * aHeadS = java.lang.Math.sin (3.1415 * 30 / 180);//overwrite parent
         * class }
         */
    }

    private int aHead0x(int sx, int sy, int ex, int ey)
    {
        int vx = ex - sx;
        int vy = ey - sy;

        if (vx != 0 || vy != 0)
            return (int) (ex - aHeadH * (aHeadC * vx + aHeadS * vy)
                    / arrowLength((double) vx, (double) vy));
        else
            return ex;
    }

    private int aHead1x(int sx, int sy, int ex, int ey)
    {
        int vx = ex - sx;
        int vy = ey - sy;

        if (vx != 0 || vy != 0)
            return (int) (ex - aHeadH * (aHeadC * vx - aHeadS * vy)
                    / arrowLength((double) vx, (double) vy));
        else
            return ex;
    }

    private int aHead0y(int sx, int sy, int ex, int ey)
    {
        int vx = ex - sx;
        int vy = ey - sy;

        if (vx != 0 || vy != 0)
            return (int) (ey - aHeadH * (-aHeadS * vx + aHeadC * vy)
                    / arrowLength((double) vx, (double) vy));
        else
            return ey;
    }

    private int aHead1y(int sx, int sy, int ex, int ey)
    {
        int vx = ex - sx;
        int vy = ey - sy;

        if (vx != 0 || vy != 0)
            return (int) (ey - aHeadH * (aHeadS * vx + aHeadC * vy)
                    / arrowLength((double) vx, (double) vy));
        else
            return ey;
    }

    private static double arrowLength(double x, double y)
    {
        return java.lang.Math.sqrt(x * x + y * y);
    }

    public boolean contains(Point _pos)
    {
        if (shape.contains(_pos))
            return true;

        int _x = 0;
        int _y = 0;
        /*
         * int _w = 0; int _h = 0; if ( (x1 < x2) && (y1 < y2) ) { _x = x1 - 1;
         * _y = y1 - 1; _w = x2 - x1 + 2; _h = y2 - y1 + 2; } else if ( x1 < x2
         * ) { _x = x1 - 1; _y = y2 - 1; _w = x2 - x1 + 2; _h = y1 - y2 + 2; }
         * else if ( !(x1 < x2) && (y1 > y2) ) { _x = x2 - 1; _y = y2 - 1; _w =
         * x1 - x2 + 2; _h = y1 - y2 + 2; } else if ( !(x1 < x2) ) { _x = x2 -
         * 1; _y = y1 - 1; _w = x1 - x2 + 2; _h = y2 - y1 + 2; }
         * 
         * Rectangle line_area = new Rectangle(_x, _y, _w, _h); if (
         * line_area.contains(_pos) )
         */

        // use 2D Graphics, one line replaced bunch of above commented code, HF
        if (line.ptSegDist(_x, _y) < 5)
        {
            return true;
        }
        return false;
    }

    public void setEventPending(boolean _pending)
    {
        eventPending = _pending;
    }

    public void setDefault(boolean isDefault)
    {
        this.isDefault = isDefault;
    }

    private static int callCount = 0;

    public void startMultipleLine(int x, int y)
    {
        startLineX = endLineX = x;
        startLineY = endLineY = y;
    }

    public void drawMultipleLine(int x, int y, Graphics2D g)
    {
        Graphics2D g2 = g;
        g2.setPaint((Paint) GraphicalPane.DEFAULT_COLOR);

        // erase the previous line...
        g2.setXORMode(GraphicalPane.SELECTION_COLOR);
        g2.drawLine(startLineX, startLineY, endLineX, endLineY);

        // ...draw a new line...
        g2.setXORMode(GraphicalPane.SELECTION_COLOR);
        g2.drawLine(startLineX, startLineY, x, y);
        endLineX = x;
        endLineY = y;
        g2.setPaintMode();
        g2.dispose();
    }

    public int[] endMultipleLine(int x, int y)
    {
        int x1 = startLineX;
        int y1 = startLineY;
        int x2 = x;
        int y2 = y;
        startLineX = x;
        startLineY = y;
        drawBufferedLines.addElement(new Point(x2, y2));
        int a[] =
        { x1, y1, x2, y2 };
        return a;
    }

    public void createIntermediatePointsArray()
    {
        intermediatePoints = new Point[drawBufferedLines.size()];
        originalIPoints = new Point[drawBufferedLines.size()];
        drawBufferedLines.copyInto(intermediatePoints);
    }

    public Polygon drawArrowArea()
    {
        int x, y;
        arrowArea = new Polygon();
        arrowArea.addPoint(getPosition().x + 3, getPosition().y);
        for (int i = 0; i < intermediatePoints.length; i++)
        {
            x = (int) intermediatePoints[i].x + 3;
            y = (int) intermediatePoints[i].y;
            arrowArea.addPoint(x, y);
        }
        arrowArea.addPoint(headPos.x + 3, headPos.y);
        arrowArea.addPoint(headPos.x - 3, headPos.y);
        for (int i = intermediatePoints.length - 1; i >= 0; i--)
        {
            x = (int) intermediatePoints[i].x - 3;
            y = (int) intermediatePoints[i].y;
            arrowArea.addPoint(x, y);
        }
        arrowArea.addPoint(getPosition().x - 3, getPosition().y);

        return arrowArea;
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        draw(g2, true);
    }

    void draw(Graphics2D g2, boolean mode)
    {

        if (g2 == null)
        {
            return;
        }

        int sx, sy, ex, ey;
        FontMetrics fm = g2.getFontMetrics();
        int aHxPoints[] = new int[4];
        int aHyPoints[] = new int[4];

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (mode)
        {
            Point epos = getArrowHeadPosition();
            Point spos = getArrowTailPosition();

            sx = spos.x;
            sy = spos.y;
            ex = epos.x;
            ey = epos.y;

            // comments this block of code as not using delaynode
            /*
             * if(sourceNode instanceof UIDelay ){ if(ey < sy) { if(ey > sy-30
             * && sx > ex) { sx= sx + 5; }else if(ey > sy-20 && ex > sx) { sx=
             * sx - 5; }else if ( (ex - sx) > 0 || (ex - sx) < 0){ int dx = (ey
             * -sy) / (ex - sx); if(dx < -1) { sy = sy + 5; } else if(dx > 1) {
             * sy = sy +5; } }
             * 
             * } else if(sy < ey){ if ( (ex - sx) > 0 || (ex - sx) < 0){ int dx
             * = (ey -sy) / (ex - sx); if(dx <= -1) { sy = sy -5; } else if(dx
             * >= 1) { sy = sy -5; } else sx = getTailExForDelay(sx,ex,sy,ey); }
             * } }
             * 
             * //Target node as Delay if(targetNode instanceof UIDelay ){ if(ey
             * > sy) { if(sy > ey-30 && sx > ex) { ex= ex - 5; }else if(sy >
             * ey-30 && ex > sx) { ex= ex + 5; }else if ( (ex - sx) > 0 || (ex -
             * sx) < 0){ int dx = (ey -sy) / (ex - sx); if(dx < -1) { ey = ey +
             * 5; } else if(dx > 1) { ey = ey +5; } }
             * 
             * } else if(sy > ey){ if ( (ex - sx) > 0 || (ex - sx) < 0){ int dx
             * = (ey -sy) / (ex - sx); if(dx <= -1) { ey = ey -5; } else if(dx
             * >= 1) { ey = ey -5; } else{ ex = getHeadExForDelay(sx,ex,sy,ey);
             * } }
             * 
             * } }
             */
            // to get end point of target node
            if (targetNode instanceof UICondition)
            {
                ex = getHeadEx(sx, ex, sy, ey);
            }
            // to get end point of source node
            if (sourceNode instanceof UICondition)
            {
                sx = getTailEx(sx, ex, sy, ey);
            }
        }
        else
        {
            sx = pos.x;
            sy = pos.y;
            ex = headPos.x;
            ey = headPos.y;
        }

        int sxz = gPane.zoom(sx);
        int syz = gPane.zoom(sy);
        int exz = gPane.zoom(ex);
        int eyz = gPane.zoom(ey);

        if (mode)
        {
            // non-ghost image
            if (selected)
            {
                g2.setPaint((Paint) GraphicalPane.SELECTION_COLOR);
            }
            else if (eventPending)
            {
                g2.setPaint((Paint) GraphicalPane.EVENT_PENDING_COLOR);
            }
            else if (isDefault)
            {
                g2.setPaint((Paint) GraphicalPane.DEFAULT_PATH_COLOR);
            }
            else
            {
                g2.setPaint((Paint) GraphicalPane.DEFAULT_COLOR);
            }
            // g2.drawLine(sxz, syz, exz, eyz);
            if (intermediatePoints != null && intermediatePoints.length > 0)
            {
                for (int i = 0; i < intermediatePoints.length; i++)
                {
                    int x2 = (int) intermediatePoints[i].x;
                    int y2 = (int) intermediatePoints[i].y;

                    g2.drawLine(sxz, syz, x2, y2);
                    sxz = x2;
                    syz = y2;
                }
                drawArrowArea();
            }
            g2.drawLine(sxz, syz, exz, eyz);

            aHxPoints[0] = exz;
            aHyPoints[0] = eyz;
            aHxPoints[1] = aHead0x(sxz, syz, exz, eyz);
            aHyPoints[1] = aHead0y(sxz, syz, exz, eyz);
            aHxPoints[2] = aHead1x(sxz, syz, exz, eyz);
            aHyPoints[2] = aHead1y(sxz, syz, exz, eyz);
            aHxPoints[3] = exz;
            aHyPoints[3] = eyz;

            x1 = sxz;
            y1 = syz;
            x2 = exz;
            y2 = eyz;

            if (gPane instanceof GVPane)
            {
                g2.fillPolygon(aHxPoints, aHyPoints, 4);
                labelField
                        .setBounds(
                                ((sxz + exz) - getLabelFieldPreferredWidth(g2)) / 2 - 2,
                                (syz + eyz) / 2 - 10,
                                getLabelFieldPreferredWidth(g2) + 4,
                                DrawInfo.getPreferredTextSize().height);
                // labelField.setText (msgCat.getMsg (objectName)); - bug for
                // zooming
                // labelField.repaint();

                // Following try-catch is commented out by Hamid
                /*
                 * try { labelField.setCaretPosition(0); } catch (Exception e) {
                 * //ignore this exception at least until graphical planner uses
                 * swing }
                 */
            }

            // coments this block of code as not using Tumbnailview
            /*
             * else if (gPane instanceof TNPane) { g2.fillPolygon(aHxPoints,
             * aHyPoints, 4); shape = new Polygon(aHxPoints, aHyPoints, 4); }
             */
        }
        else
        {
            // ghost image
            g2.setPaint((Paint) GraphicalPane.DEFAULT_COLOR);
            g2.setXORMode(GraphicalPane.SELECTION_COLOR);
            g2.drawLine(sxz, syz, exz, eyz);
            if (gPane instanceof GVPane)
            {
                g2.fillPolygon(aHxPoints, aHyPoints, 4);
            }
            g2.setPaintMode();
        }

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    private int getLabelFieldPreferredWidth(Graphics2D g2)
    {
        if (!labelFieldWidthComputed)
        {
            recomputeLabelFieldWidth(g2);
        }
        return labelFieldWidth;
    }

    private void recomputeLabelFieldWidth(Graphics2D g2)
    {
        Font fon = labelField.getFont();
        if (fon == null)
        {

            fon = getFont();
            if (fon == null)
            {
                labelFieldWidth = labelField.getPreferredSize().width;
                return;
            }
        }
        FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(fon);
        labelFieldWidth = fm.stringWidth(objectName) + 15;
        int prefWidth = labelField.getPreferredSize().width;
        labelFieldWidth = Math.min(labelFieldWidth, prefWidth);
        labelFieldWidthComputed = true;
    }

    public void translate2(int x, int y)
    {
        if (headPos != null)
            headPos.translate(x, y);
        if (headArea != null)
            moveArrowRect();
    }

    public void moveArrowRect()
    {
        int x, y;
        double xlen = (double) (pos.x - headPos.x);
        double ylen = (double) (pos.y - headPos.y);
        double len = Math.sqrt(xlen * xlen + ylen * ylen);
        double Sin = ylen / len;
        double Cos = xlen / len;

        headArea = new Polygon();
        headArea.addPoint((int) (5 * Sin + pos.x), (int) (-5 * Cos + pos.y));
        headArea.addPoint((int) (-5 * Sin + pos.x), (int) (5 * Cos + pos.y));
        headArea.addPoint((int) (-5 * Sin + headPos.x),
                (int) (5 * Cos + headPos.y));
        headArea.addPoint((int) (5 * Sin + headPos.x),
                (int) (-5 * Cos + headPos.y));
    }

    public void keyTyped(KeyEvent event)
    {
        if (event.getSource() == labelField)
        {
            // The following line is commented out by Hamid. Please leave it
            // that
            // way:
            // limitText (labelField);
        }
    }

    public void focusGained(FocusEvent event)
    {
        if (event.getSource() == labelField)
        {
            labelChanged = true;

            if (event.isTemporary() == true)
                return;
            gPane.drawLines.removeAllElements();
            gPane.setSelObj(this);
            (gPane.getBaseApplet()).fireSelectionEvent();
            ((GVPane) gPane).arrowStarted = false;

            oldText = labelField.getText();
            labelField.putClientProperty("OldText", oldText);
        }
    }

    public void focusLost(FocusEvent event)
    {
        boolean dupDetected = false;

        if (event.getSource() == labelField)
        {
            // This line is commented out by Hamid. Please leave it that way.
            // if (event.isTemporary() == true) return;

            objectName = labelField.getText();
            if (!(modelObject instanceof WorkflowArrow))
                return; // added by Hamid
            WorkflowTask plan_node = ((WorkflowArrow) modelObject)
                    .getSourceNode();
            // Arrow[] _arrows = plan_node.getOutgoingArrows();
            java.util.List _arrows = plan_node.getOutgoingArrows();

            // comments this ... do it later
            // if ( (plan_node instanceof ActivityNode) ||
            //
            // if ( (plan_node.getType()==WorkflowConstants.ACTIVITY) ||
            // (plan_node.getType()==WorkflowConstants.CONDITION) )
            {
                for (int j = 0; j < _arrows.size(); j++)
                {
                    WorkflowArrow _arrow = (WorkflowArrow) _arrows.get(j);
                    String strLabel = _arrow.getName();

                    if ((_arrow != (WorkflowArrow) modelObject)
                            && objectName.equals(strLabel))
                    {
                        dupDetected = true;
                        break;
                    }
                }

                if (dupDetected == true)
                {
                    dupDetected = false;

                    oldText = (String) labelField.getClientProperty("OldText");
                    labelField.setText(oldText);
                    labelField.requestFocus();
                    labelField.selectAll();

                    // if ( plan_node instanceof ConditionNode )
                    {
                        JOptionPane.showMessageDialog(null, AppletHelper
                                .getI18nContent("msg_no_dup_arrow_error"));
                    }
                    /*
                     * else if ( plan_node instanceof ActivityNode ) {
                     * JOptionPane.showMessageDialog(null,
                     * "The activity node cannot have duplicate " +
                     * "arrow names\n" +
                     * "The arrow's old name has been restored\n" +
                     * "Feel free to change it as long as no duplicates occur");
                     * }
                     */

                    return;
                }
            }

            if (gPane instanceof GVPane)
            {
                if (!oldText.equals(labelField.getText()))
                {
                    ((GVPane) gPane).isModify = true;
                }
            }

            try
            {
                // author: Hamid
                // This added condition check ensures the sever is not called
                // until everything is legal
                if (!arrowNameEmpty())
                    ((WorkflowArrow) modelObject).setName(objectName);

                recomputeLabelFieldWidth(null);
            }
            catch (Exception e)
            {

                /*
                 * WindowUtil.showMsgDlg(WindowUtil.getFrame(this),
                 * //msgCat.getMsg( (e.getErrorCode()) ),
                 * msgCat.getMsg("Arrow Name Error!"), WindowUtil.TYPE_ERROR);
                 */
            }
        }
    }

    public void mouseClicked(MouseEvent event)
    {
        if (event.getSource() == labelField)
        {
            handleDoubleClicks(labelField, event);
        }
    }

    private Point getArrowHeadPosition()
    {
        // Log.println(Log.LEVEL3, "BaseArrow::: getArrowHeadPosition()");
        int x = 0, y = 0;
        int sx, sy;

        int dx = targetNode.getPosition().x;
        int dy = targetNode.getPosition().y;

        int sx1 = 0, sy1 = 0;

        boolean flag = false;
        if (intermediatePoints != null && intermediatePoints.length > 0)
        {
            sx = (int) intermediatePoints[intermediatePoints.length - 1].x;
            sy = (int) intermediatePoints[intermediatePoints.length - 1].y;
            /*
             * if(gPane instanceof TNPane){ sx
             * =(int)originalIPoints[originalIPoints.length - 1].x;
             * sy=(int)originalIPoints[originalIPoints.length - 1].y;
             * //Log.println(Log.LEVEL3,
             * "BaseArrow:::if(gPane instanceof TNPane) sx:" + sx + "sy:" + sy);
             * }
             */

            flag = true;
        }
        else
        {
            sx = sourceNode.getPosition().x;
            sy = sourceNode.getPosition().y;
        }
        double xlen = 0.0;
        double ylen = 0.0;
        xlen = (double) (sx - dx);
        ylen = (double) (sy - dy);

        double len = Math.sqrt(xlen * xlen + ylen * ylen);
        double Sin = ylen / len;
        double Cos = xlen / len;

        if (targetNode instanceof BaseActivity)
        {

            double sine1 = java.lang.Math.abs(ylen) / len;
            double cosine1 = java.lang.Math.abs(xlen) / len;
            double cosine30 = 0.87;
            double cosine60 = 0.5;
            double sine30 = 0.5;
            double sine45 = 0.71;
            double sine60 = 0.87;
            int width = UIActivity.getStdWidth();
            int height = UIActivity.getStdHeight();
            int yMultiplier = 1;
            boolean below = false;
            boolean left = false;
            Rectangle r = new Rectangle(width, height);
            Rectangle bounds = r.getBounds();
            if (sy < dy)
            { // target is below the source
                yMultiplier = -1;
                below = true;
            }

            int xMultiplier = 1;
            if (sx < dx)
            { // target is to the left of the source
                xMultiplier = -1;
                left = true;
            }
            // target node, use cosine angle
            if (sine1 < sine30)
            {
                x = dx + (width / 2) * xMultiplier;
                y = dy;

            }
            else if (sine1 < sine60)
            {

                x = dx + (width / 2) * xMultiplier;
                y = dy + (height / 2) * yMultiplier;
            }
            else
            {

                x = dx;
                y = dy + (height / 2) * yMultiplier;
            }
        }
        else if (targetNode instanceof BaseTerminal)
        {

            x = (int) (30 * Cos + dx);
            y = (int) (30 * Sin + dy);
        }
        else if (targetNode instanceof UISubProcess)
        {
            Point p = calculateUISubProcessArrowIntersection(targetNode, sx,
                    sy, dx, dy, Sin, Cos);
            x = p.x;
            y = p.y;
        }
        else if (targetNode instanceof BaseConnection)
        {

            /*
             * x = (int)(17 * Cos + dx); y = (int)(17 * Sin + dy); x += 17; //
             * Rid later y += 17;
             */
            // removed the dependency where an Arrow knows the dimensions
            // of all nodes
            // Instead created abstract methods in UIObject for getNodeWidth() &
            // height
            int width = targetNode.getNodeWidth();
            int height = targetNode.getNodeHeight();
            x = (int) ((width / 2) * Cos + dx);
            y = (int) ((height / 2) * Sin + dy);
            x += (width / 2);
            y += (height / 2);
        }
        else
        {
            // arrow targetNode is not a node

            return null;
        }

        if (headPos != null)
        {
            headPos.move(x, y);
        }
        if (headArea != null)
        {
            moveArrowRect();
        }
        return new Point(x, y);
    }

    private Point getArrowTailPosition()
    {
        int x, y;
        int dx, dy;
        int sx = sourceNode.getPosition().x;
        int sy = sourceNode.getPosition().y;

        if (intermediatePoints != null && intermediatePoints.length > 0)
        {
            dx = (int) intermediatePoints[0].x;
            dy = (int) intermediatePoints[0].y;
        }
        else
        {
            dx = targetNode.getPosition().x;
            dy = targetNode.getPosition().y;
        }

        // int dx = targetNode.getPosition().x;
        // int dy = targetNode.getPosition().y;
        double xlen = (double) (dx - sx);
        double ylen = (double) (dy - sy);
        double len = Math.sqrt(xlen * xlen + ylen * ylen);
        double Sin = ylen / len;
        double Cos = xlen / len;

        if (sourceNode instanceof BaseActivity)
        {
            double sine1 = java.lang.Math.abs(ylen) / len;
            double cosine1 = java.lang.Math.abs(xlen) / len;
            double sine30 = 0.5;
            double sine45 = 0.71;
            double sine60 = 0.87;
            int width = UIActivity.getStdWidth();
            int height = UIActivity.getStdHeight();
            int yMultiplier = 1;
            if (dy < sy)
            { // target is above the source
                yMultiplier = -1;
            }
            int xMultiplier = 1;
            if (dx < sx)
            { // target is to the left of the source
                xMultiplier = -1;
            }
            // source node, use sine angle
            if (sine1 < sine30)
            {
                // x = sx;
                // y = sy + (height/2) * yMultiplier;
                x = sx + (width / 2) * xMultiplier;
                y = sy;
            }
            else if (sine1 < sine60)
            {
                x = sx + (width / 2) * xMultiplier;
                y = sy + (height / 2) * yMultiplier;
            }
            else
            {
                x = sx;
                y = sy + (height / 2) * yMultiplier;
            }
        }
        else if (sourceNode instanceof BaseTerminal)
        {
            x = (int) (30 * Cos + sx);
            y = (int) (30 * Sin + sy);
        }
        else if (sourceNode instanceof UISubProcess)
        {
            Point p = calculateUISubProcessArrowIntersection(sourceNode, dx,
                    dy, sx, sy, Sin, Cos);
            x = p.x;
            y = p.y;
        }
        else if (sourceNode instanceof BaseConnection)
        {
            /*
             * x = (int)(17 * Cos + sx); y = (int)(17 * Sin + sy); x += 17; //
             * Rid later y += 17;
             */
            int width = sourceNode.getNodeWidth();
            int height = sourceNode.getNodeHeight();
            x = (int) ((width / 2) * Cos + sx);
            y = (int) ((height / 2) * Sin + sy);
            x += (width / 2);
            y += (height / 2);
        }
        else
        {
            // arrow sourceNode is not a node
            return null;
        }

        if (pos != null)
        {
            pos.move(x, y);
        }
        if (area != null)
        {
            area.move(x - 4, y - 4);
        }
        if (headArea != null)
        {
            moveArrowRect();
        }
        return new Point(x, y);
    }

    void setFieldsEnabled(boolean new_state)
    {
        if (labelField != null)
        {
            enableTextfield(labelField, new_state);
        }
    }

    public boolean setArrowSrc(UIObject obj)
    {
        if (obj instanceof BaseActivity || obj instanceof BaseConnection
                || (obj instanceof BaseTerminal && !(obj instanceof UIExit))) // UIStart
        {
            sourceNode = obj;
            pos = getArrowTailPosition();
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean setArrowDest(UIObject obj)
    {
        if ((obj instanceof BaseActivity || obj instanceof BaseConnection || obj instanceof BaseTerminal)
                && !(obj instanceof UIStart))
        {
            targetNode = obj;
            pos = getArrowHeadPosition();
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean checkSelArrowHead(int x, int y)
    {
        if ((Math.abs((double) (pos.x - x)) + Math.abs((double) (pos.y - y))) <= (Math
                .abs((double) (headPos.x - x)) + Math
                .abs((double) (headPos.y - y))))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    BaseArrow getDummy()
    {
        BaseArrow obj;
        obj = new BaseArrow(gPane);

        obj.setState(state);
        obj.setObjectName(objectName);
        obj.setRoleName(roleName);
        /*
         * if (pos == null) { //Log.println(Log.LEVEL1, "null pos in getdummy");
         * } else
         */
        if (pos != null)
        {
            obj.setPosition(new Point(pos));
        }

        if (headPos != null)
        {
            obj.headPos = new Point(headPos);
        }

        if (area != null)
        {
            obj.setArea(new Rectangle(area));
        }

        obj.setModelObject(modelObject);

        if (color != null)
        {
            obj.setColor(new Color(color.getRed(), color.getGreen(), color
                    .getBlue()));
        }
        return obj;
    }

    /**
     * @author:Hamid This new utility method is used in the key listener for the
     *               arrow's name text field.
     */
    /*
     * private void validateArrowName() { String text = labelField.getText(); if
     * ( text == null || text.trim().length() == 0 ) return; if ( text.length()
     * >= WorkflowConstants.MAX_NODE_NAME_LENGTH ) { //get message from
     * pagehandler JOptionPane.showMessageDialog(null,
     * msgCat.getMsg("msg_arrowMaxLength")); labelField.requestFocus(); text =
     * text.substring(0, text.length() - 2); labelField.setText(text); } }
     */

    /**
     * @author:Hamid This new utility method is to check whether the arrow's
     *               name is empty or not. It is used in the method
     *               <code>focusLost()</code> so that the arrow is not saved
     *               until the user provides a valid name for it.
     */
    private boolean arrowNameEmpty()
    {
        String text = labelField.getText();
        if (text == null || text.trim().length() == 0)
        {
            // get message from pagehandler
            JOptionPane.showMessageDialog(null,
                    AppletHelper.getI18nContent("msg_arrow_empty_name"));
            sizeArrowTextField();
            labelField.requestFocus();
            labelField.setText(msgCat.getMsg("name"));
            return true;
        }
        else
            return false;
    }

    /**
     * @author:Hamid This new utility method is used dynamically increase the
     *               size of the text field for the arrwo's name, while the user
     *               is typing it. This method is called in the key listener of
     *               the arrow's name text field.
     */
    private void sizeArrowTextField()
    {
        Font font = labelField.getFont();
        if (font == null)
            font = new Font("Helvetica", Font.PLAIN, 12);
        Graphics2D g = (Graphics2D) labelField.getGraphics();
        if (g == null)
            g = (Graphics2D) gPane.getGraphics();
        else if (g == null)
            return;
        FontRenderContext frc = g.getFontRenderContext();
        Rectangle fieldBounds = labelField.getBounds();
        if (labelField.getText() == null || labelField.getText().length() == 0)
        {
            labelFieldWidth = 45;
            labelField.setBounds(fieldBounds.x, fieldBounds.y, labelFieldWidth,
                    fieldBounds.height);
        }
        else
        {
            TextLayout layout = new TextLayout(labelField.getText(), font, frc);
            Rectangle2D bounds = layout.getBounds();
            int tx = 0;
            if ((int) bounds.getWidth() - 45 > 0)
                tx = (int) ((int) bounds.getWidth() - 45) / 2 + 5;
            labelFieldWidth = Math.max((int) bounds.getWidth(), 45) + 10;
            labelField.setBounds(fieldBounds.x - tx, fieldBounds.y,
                    Math.max((int) bounds.getWidth(), 45) + 10,
                    fieldBounds.height);
        }

        labelField.validate();
        gPane.validate();
    }

    private int getHeadEx(int sx, int ex, int sy, int ey)
    {
        if (ex == sx)
        {

        }
        else if (ey == sy && ex > sx)
        {
            ex = ex - 5;
        }
        else if (ey > sy - 30 && sy > ey && ex > sx)
        {
            ex = ex - 5;
        }
        else if (ey < sy + 30 && sy < ey && ex > sx)
        {
            ex = ex - 5;
        }
        else if (ey == sy && ex < sx)
        {
            ex = ex + 5;
        }
        else if (ey > sy - 30 && sy > ey && ex < sx)
        {
            ex = ex + 5;
        }
        else if (ey < sy + 30 && sy < ey && ex < sx)
        {
            ex = ex + 5;
        }

        return ex;
    }

    private int getTailEx(int sx, int ex, int sy, int ey)
    {
        if (ex == sx)
        {

        }
        if (ey == sy && ex > sx)
        {
            sx = sx + 11;
        }
        else if (ey > sy - 50 && sy > ey && ex > sx)
        {
            sx = sx + 7;
        }
        else if (ey < sy + 50 && sy < ey && ex > sx)
        {
            sx = sx + 7;
        }
        else if (ey == sy && ex < sx)
        {
            sx = sx - 11;
        }
        else if (ey > sy - 50 && sy > ey && ex < sx)
        {
            sx = sx - 7;
        }
        else if (ey < sy + 50 && sy < ey && ex < sx)
        {
            sx = sx - 7;
        }
        return sx;
    }

    private int getTailExForDelay(int sx, int ex, int sy, int ey)
    {

        if (sy < ey && ex > sx)
        {
            sx = sx - 5;
        }
        else if (sy < ey && sx > ex)
        {
            sx = sx + 5;
        }
        else if (ey > sy && sx == ex)
        {
            sx = sx - 5;
        }
        else if (ey < sy && sx == ex)
        {
            sx = sx - 5;
        }
        else if (ey == sy && sx > ex)
        {
            sx = sx + 5;
        }
        else if (sy < ey && ex > sx)
        {
            sx = sx - 5;
        }

        return sx;
    }

    private int getHeadExForDelay(int sx, int ex, int sy, int ey)
    {

        if (ey > sy - 20 && sy > ey && sx > ex)
        {
            ex = ex - 5;
        }
        else if (sy > ey && ex > sx)
        {
            ex = ex + 5;
        }
        else if (sy > ey && sx > ex)
        {
            ex = ex - 5;
        }
        else if (ey > sy && sx == ex)
        {
            ex = ex - 5;
        }
        else if (ey < sy && sx == ex)
        {
            ex = ex - 5;
        }
        else if (ey == sy && sx > ex)
        {
            ex = ex - 5;
        }
        else if (sy == ey && ex > sx)
        {
            ex = ex + 5;
        }

        return ex;
    }

    private Point calculateUISubProcessArrowIntersection(UIObject node, int sx,
            int sy, int dx, int dy, double Sin, double Cos)
    {
        double sine30 = 0.5;
        double sine60 = 0.87;
        int width = node.getNodeWidth();
        int height = node.getNodeHeight();

        int x, y;

        if (Math.abs(Sin) > sine60) // always land in the center of the top or
                                    // bottom
        {
            x = dx + (width / 2);
            y = dy;
            if (sy > dy)
                y += height;
        }
        else if (Math.abs(Sin) >= sine30)
        {
            // always land at a point where a virtual semicircle makes
            // tangential
            // contact with the "cut". This is at a 50 degree angle to side
            // of the polygon. Note, it the UISubrocess - xcut changes
            // this angle MUST change
            int specialAngle = 50;
            double sineSpecial = Math.sin(specialAngle * (Math.PI / 180));
            x = dx + (height / 2);
            if (sx > dx)
            {
                x += width - height;
            }
            int signMultplr = 1;
            if (Cos < 0)
            {
                signMultplr = -1;
            }
            x += (int) ((height / 2) * sineSpecial * signMultplr);
            y = dy + height / 2;
            signMultplr = 1;
            if (Sin < 0)
            {
                signMultplr = -1;
            }
            y += (int) ((height / 2) * sineSpecial * signMultplr);
        }
        else
        // always land in the center of the side nearest the source node
        {
            x = dx;
            if (sx > dx)
            {
                x += width;
            }
            y = dy + (height / 2);
        }
        return new Point(x, y);
    }
}
