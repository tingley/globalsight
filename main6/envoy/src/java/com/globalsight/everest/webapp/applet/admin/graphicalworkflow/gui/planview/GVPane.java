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

import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.RepaintManager;

import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.util.WindowUtil;
import com.globalsight.everest.webapp.applet.common.AppletHelper;
import com.globalsight.everest.webapp.applet.common.EnvoyAppletConstants;
import com.globalsight.everest.webapp.applet.common.GenericJTextField;
import com.globalsight.everest.webapp.applet.common.GlobalEnvoy;
import com.globalsight.everest.workflow.WorkflowArrow;
import com.globalsight.everest.workflow.WorkflowArrowInstance;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowTask;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflow.WorkflowTemplate;

public class GVPane extends GraphicalPane implements KeyListener, Printable
{
    final static int ACTIVITY_TYPE = 1;

    final static int AND_TYPE = 2;

    final static int ARROW_TYPE = 3;

    final static int CONDITION_TYPE = 4;

    final static int EXIT_TYPE = 5;

    final static int OR_TYPE = 6;

    final static int SUB_TYPE = 7;

    final static int POINTER_TYPE = 8;

    final static int NO_OP_TYPE = 8;

    final static int CHAINED_TYPE = 9;

    final static int DELAY_TYPE = 10;

    final static int UIACTIVITY_TYPE = 11;

    final static int UIAND_TYPE = 12;

    final static int UIARROW_TYPE = 13;

    final static int UICONDITION_TYPE = 14;

    final static int UIEXIT_TYPE = 15;

    final static int UIOR_TYPE = 16;

    final static int UISUB_TYPE = 17;

    final static int UINO_OP_TYPE = 18;

    final static int UICHAINED_TYPE = 19;

    final static int UIDELAY_TYPE = 20;

    final static int UISTART_TYPE = 21;

    private static final int ADD_NODE_CURSOR_TYPE = Cursor.CROSSHAIR_CURSOR;

    private static final int DEFAULT_CURSOR_TYPE = Cursor.DEFAULT_CURSOR;

    static final Cursor addNodeCursor = Cursor
            .getPredefinedCursor(ADD_NODE_CURSOR_TYPE);

    static final Cursor defaultCursor = Cursor
            .getPredefinedCursor(DEFAULT_CURSOR_TYPE);

    private int nodeType = NO_OP_TYPE;

    private int onNodeType = UINO_OP_TYPE;

    private int ox;

    private int oy;

    // comment later
    // Resources resources;
    static Image formImage;

    static Image propImage;

    static Image propChainedImage;

    static Image timerImage;

    static Image formBwImage;

    static Image attachImage;

    static Image propBwImage;

    static Image personImage;

    static Image timerBwImage;

    static Image attachBwImage;

    static Image personBwImage;

    private UIObject ghostObj;

    private Object sourceObj = null;

    private Vector planNodes;

    private int counter = 1;

    private UIObject onNodeObj;

    private boolean addingNode = false;

    private boolean addingArrow = false;

    private boolean selArrowHead = false;

    private boolean hasImages = false;

    private boolean drawMultiLine = false;

    private boolean firstClick = false;

    public boolean arrowStarted = false;

    private boolean uiActivityOffset = false;

    private boolean arrowCompleted = false;

    private double width_original = 0;

    private double height_original = 0;

    private double zoomFactorX = 1;

    private double zoomFactorY = 1;

    private JFrame frame;

    private JApplet applet;

    // Toolbar images
    Image gpactImage;

    Image gpexitImage;

    Image gpcondImage;

    Image gporImage;

    Image gpandImage;

    Image gpsubImage;

    Image gparrowImage;

    Image gppointerImage;

    Image gpsaveImage;

    Image m_printImage;

    Image gpcancelImage;

    // Hash table for Images
    Hashtable imageHash;

    // flag for setting the AND/OR nodes visible
    boolean m_areAndOrNodesVisible = false;

    // flag for setting the page in edit mode (allow editing)
    boolean m_isEditMode = true;

    // tells wheather workflow contains Exit node
    boolean hasExitNode = false;

    // info for the activity node dialog box
    private Hashtable m_dialogData;

    // task info bean map used for the rate info
    private Hashtable m_taskInfoBeanMap;

    // modified task info map
    private Hashtable m_modifiedTaskInfoMap;

    // determines whether the "Ready" tab of My Jobs UI should be
    // displayed after a successful save (only for workflow instance).
    private boolean m_isReady = false;

    /*
     * This boolean flag is added so that we can disable the popup menu of the
     * nodes when viewing an archived template.
     */
    boolean popupEnabled = true;

    /*
     * Because the Activity UI has two part:name field and role field. The two
     * part was put label on them, when click on the activity UI, actually click
     * on the one of the two labels. So the ev.getY() is not the distance value
     * from the activity UI top to the click node, it's actually the distance
     * value from the label top to the click node. When click the role field,
     * because it's on the top of activity UI, so the ev.getY() can get the
     * correct value ,but if click the bottom label(name field), the correct
     * value is (ev.getY() + activityYOffset).
     */
    private int activityYOffset = 0;

    public boolean isModify = false;
    public String aaa = "justin1";

    public void init(TWFAGP anAgp)
    {
        super.init(anAgp);
        addKeyListener(this);
    }

    Hashtable getDialogData()
    {
        return m_dialogData;
    }

    // get the hashtable that contains a list of TaskInfoBean objects
    Hashtable getTaskInfoBeanMap()
    {
        return m_taskInfoBeanMap;
    }

    // get a hashtable that will contain the modified rate related info
    Hashtable getModifiedTaskInfoMap()
    {
        if (m_modifiedTaskInfoMap == null)
        {
            m_modifiedTaskInfoMap = new Hashtable();
        }
        return m_modifiedTaskInfoMap;
    }

    public List<Object[]> getRoleInfo(String p_activityName, boolean p_isUser)
    {
        return getRoleInfo(p_activityName, p_isUser, -1);
    }

    public List<Object[]> getRoleInfo(String p_activityName, boolean p_isUser,
            long taskId)
    {
        // go to the servlet to filter the grid info.
        Vector request = new Vector();
        request.addElement(p_isUser ? "user" : "role");
        request.addElement(p_activityName);
        request.addElement(new Long(taskId));
        Vector inputFromServlet;
        if (request.size() > 1)
        {
            inputFromServlet = getEnvoyJApplet().appendDataToPostConnection(
                    request, "null");

            return (List<Object[]>) inputFromServlet.elementAt(0);
        }
        else
            return null;
    }

    // get the parent frame.
    public Frame getParentFrame()
    {
        return getEnvoyJApplet().getParentFrame();
    }

    // override super class (EnvoyJpanel) method
    public void populate(Vector p_postData)
    {
        imageHash = (Hashtable) p_postData.elementAt(0);
        gpactImage = getImage((String) imageHash.get("gpact"));
        gpexitImage = getImage((String) imageHash.get("gpexit"));
        gpcondImage = getImage((String) imageHash.get("gpcond"));
        gporImage = getImage((String) imageHash.get("gpor"));
        gpandImage = getImage((String) imageHash.get("gpand"));
        gpsubImage = getImage((String) imageHash.get("gpsub"));
        gparrowImage = getImage((String) imageHash.get("gparrow"));
        gppointerImage = getImage((String) imageHash.get("pointer"));
        gpsaveImage = getImage((String) imageHash.get("gpsave"));
        m_printImage = getImage((String) imageHash.get("gpprint"));
        gpcancelImage = getImage((String) imageHash.get("gpcancel"));
        Boolean visibleFlag = (Boolean) imageHash.get("visible");
        Boolean editFlag = (Boolean) imageHash.get("editMode");

        if (visibleFlag != null)
        {
            m_areAndOrNodesVisible = visibleFlag.booleanValue();
        }
        if (editFlag != null)
        {
            m_isEditMode = editFlag.booleanValue();
        }

        // labels
        condUdaDisplayValue = (String) p_postData.elementAt(1);

        // diaglog data
        m_dialogData = (Hashtable) p_postData.elementAt(2);

        AppletHelper.setI18nContents((Hashtable) m_dialogData
                .get(EnvoyAppletConstants.I18N_CONTENT));

        Object obj = (Object) p_postData.elementAt(3);

        GlobalEnvoy.setLocale((Locale) p_postData.elementAt(4));
        // this only is true for modifying a workflow instance
        setIsReadyFlag(p_postData);

        if (obj instanceof WorkflowTemplate)
        {
            model = (WorkflowTemplate) obj;
            maxSequenceNo = model.getMaxSequence();
        }
        else if (obj instanceof WorkflowInstance)
        {
            modelPI = (WorkflowInstance) obj;
            maxSequenceNo = modelPI.getMaxSequence();
            // only for a workflow instance we'll have the task info hashtable
            if (p_postData.size() > 6)
            {
                m_taskInfoBeanMap = (Hashtable) p_postData.elementAt(6);
            }
        }
    }

    public String getTitle()
    {
        // retrun dummy variable
        return "GVPane";
    }

    private void loadImages()
    {

    }

    public GVPane()
    {
        super();
        zoomRatio = 1f;
        at = new AffineTransform();
    }

    protected void setThisView() throws Exception
    {

        if (planNodes.size() == 0)
        {
            return;
        }
        // set Arrow lable sequence
        BaseArrow.initLabelCount(planNodes.size() * 3);

        UIObject ui_node = null;

        for (int i = 0; i < planNodes.size(); i++)
        {
            switch (((WorkflowTask) planNodes.elementAt(i)).getType())
            {
                case WorkflowConstants.START:
                {

                    ui_node = new UIStart(new Point(100, 100), this);
                    break;
                }
                case WorkflowConstants.STOP:
                {

                    ui_node = new UIExit(new Point(100, 100), this);
                    break;
                }
                case WorkflowConstants.ACTIVITY:
                {
                    ui_node = new UIActivity(new Point(100, 100), this);
                    break;
                }
                case WorkflowConstants.AND:
                {

                    ui_node = new UIAND(new Point(100, 100), this);
                    break;
                }
                case WorkflowConstants.CONDITION:
                {

                    ui_node = new UICondition(new Point(100, 100), this);
                    break;
                }
                case WorkflowConstants.SUB_PROCESS:
                {
                    ui_node = new UISubProcess(new Point(100, 100), this);
                    break;
                }
                    /*
                     * case WorkflowTask.TYPE_CHAINED_PROCESS: {
                     * Log.println(Log.LEVEL3, "GVPane.setThisView():
                     * SubProcessNode found"); ui_node = new UIChainedProcess
                     * (new Point(100,100), this); break; } case
                     * WorkflowTask.TYPE_DELAY: { Log.println(Log.LEVEL3,
                     * "GVPane.setThisView(): SubProcessNode found"); ui_node =
                     * new UIDelay(new Point(100,100), this); break; }
                     */
                case WorkflowConstants.OR:
                {

                    ui_node = new UIOR(new Point(100, 100), this);
                    break;
                }
                default:
                {

                    return; // can not go forward
                }
            }
            ui_node.setModelObject((WorkflowTask) planNodes.elementAt(i));
            uiObjects.addElement(ui_node);
        }

        // now, for the actual job. Go through each node and its arrows

        for (int i = 0; i < planNodes.size(); i++)
        {
            UIObject _node = (UIObject) uiObjects.elementAt(i);

            buildUINode(_node);

        } // end of processing nodes

        setFieldsEnabled(editMode);
        requestFocus();
        repaint();
    }

    void setView() throws Exception
    {
        if (!hasImages)
        {
            // since loadImages() may fail at init(), it's called here
            loadImages();
            hasImages = true;
        }

        if (model != null)
        {
            planNodes = model.getWorkflowTasks();
            setThisView();
        }
        else if (modelPI != null)
        {
            planNodes = modelPI.getWorkflowInstanceTasks();
            setThisView();
        }

    }

    public void setSelectedNodeState(int new_state) throws Exception
    {
        /*
         * switch (new_state) { case ThumbnailView.NODESTATE_RUNNING: {
         * activate(); break; } case ThumbnailView.NODESTATE_COMPLETED: {
         * deactivate(); break; } default: { break; } }
         */
    }

    protected void buildUINode(UIObject ui_node) throws Exception
    {
        WorkflowTask plan_node = (WorkflowTask) ui_node.getModelObject();

        if (modelPI != null)
        {
            WorkflowTaskInstance node_instance = (WorkflowTaskInstance) plan_node;

            int intState = node_instance.getTaskState();

            switch (intState)
            {
                case WorkflowConstants.STATE_INITIAL:
                    ui_node.setState(UIObject.STATE_INACTIVE);
                    break;

                case WorkflowConstants.STATE_RUNNING:
                    ui_node.setState(UIObject.STATE_ACTIVE);
                    break;
                // comments this method as not using subprocess
                case WorkflowConstants.STATE_WAITING_ON_SUB_PROCESS:
                    ui_node.setState(UIObject.STATE_WAITING_ON_SUB_PROCESS);
                    break;

                case WorkflowConstants.STATE_COMPLETED:
                    ui_node.setState(UIObject.STATE_COMPLETE);
                    break;

                default:
                    ui_node.setState(UIObject.STATE_INACTIVE);
                    break;
            }
        }

        ui_node.setObjColor(ui_node.getState());

        ui_node.setPosition(new Point(plan_node.getPosition()));

        checkToEnlarge(ui_node.getPosition());

        if (ui_node instanceof BaseActivity)
        {
            ui_node.setRoleName(plan_node.getDisplayRoleName());
            // ui_node.setObjectName(plan_node.getActivityName());
            ui_node.setObjectName(plan_node.getActivityDisplayName());
            ((BaseActivity) ui_node).getRoleNameField().setText(
                    plan_node.getDisplayRoleName());
            // ((BaseActivity)ui_node).getNameField().setText(
            // plan_node.getActivityName());
            ((BaseActivity) ui_node).getNameField().setText(
                    plan_node.getActivityDisplayName());
            ((UIActivity) ui_node).setLocation();// type checking???
            add(ui_node);
            ui_node.setArea(new Rectangle(ui_node.getPosition().x
                    - UIActivity.getStdWidth() / 2, ui_node.getPosition().y
                    - UIActivity.getStdHeight() / 2, UIActivity.getStdWidth(),
                    UIActivity.getStdHeight()));
            ui_node.repaint();
        }
        else if (ui_node instanceof BaseTerminal)
        {
            ui_node.setArea(new Rectangle(ui_node.getPosition().x - 30, ui_node
                    .getPosition().y - 30, 60, 60));
            ui_node.setLocation();
            if (ui_node instanceof UIStart)
            {
                add(((UIStart) ui_node).getNameLabel());
            }
            if (ui_node instanceof UIExit)
            {
                // ui_node.setObjectName(plan_node.getActivityName());
                ui_node.setObjectName(plan_node.getActivityDisplayName());

                /*
                 * ((UIExit)ui_node).getNameField().setText(
                 * plan_node.getActivityName());
                 */
                add(((UIExit) ui_node).getNameField());
            }
        }
        else if (ui_node instanceof BaseConnection)
        {
            BaseConnection castedConn = (BaseConnection) ui_node;
            ui_node.setArea(new Rectangle(ui_node.getPosition().x, ui_node
                    .getPosition().y, castedConn.getNodeWidth(), castedConn
                    .getNodeHeight()));
            // ((BaseConnection)ui_node).setLocation ();
            // add (((BaseConnection)ui_node).getImageButton());

            // if its a subprocess add the label to the GVpane.
            if (ui_node instanceof UISubProcess)
            {
                String subProcNm = "";
                // Node modelNode = (Node) ui_node.getModelObject() ;
                /*
                 * if (ui_node instanceof UIChainedProcess) { subProcNm =
                 * modelNode.getChainedPlanName() ; } else { subProcNm =
                 * modelNode.getSubPlanName() ; }
                 */

                /*
                 * JTextArea lbl = ((UISubProcess) ui_node).getNameLabel();
                 * lbl.setText(subProcNm) ; add (lbl);
                 */
            }
        }

        Vector arrows = (Vector) plan_node.getOutgoingArrows();

        for (int j = 0; j < arrows.size(); j++)
        {
            WorkflowArrow modelArrow = (WorkflowArrow) arrows.elementAt(j);
            buildUIArrow(ui_node, modelArrow);

        } // end of processing arrows (j loop)
    }

    private void buildUIArrow(UIObject ui_node, WorkflowArrow model_arrow)
    {
        BaseArrow _arrow = new BaseArrow(this);

        _arrow.setDefault(model_arrow.isDefault());

        uiObjects.addElement(_arrow);

        // update fields of UIObject with data from WFObject
        _arrow.setObjectName(model_arrow.getName());
        createFieldForArrow(_arrow);
        _arrow.setObjColor(_arrow.getState());
        _arrow.setPosition(new Point(model_arrow.getStartPoint()));
        _arrow.setIntermediatePoints(model_arrow.getPoints());
        _arrow.setHeadPosition(model_arrow.getEndPoint());
        _arrow.moveArrowRect();
        _arrow.setSourceNode(ui_node);

        UIObject dest_node = null;
        for (int k = 0; k < uiObjects.size(); k++)
        {
            dest_node = (UIObject) uiObjects.elementAt(k);

            if (model_arrow.getTargetNode().equals(
                    (WorkflowTask) (dest_node.getModelObject())))
            {
                _arrow.setTargetNode(dest_node);
                break;
            }
        }
        _arrow.setArrowArea(calculateArrowArea(_arrow));
        _arrow.setModelObject(model_arrow);
    }

    private Polygon calculateArrowArea(BaseArrow arrow)
    {
        int x, y;
        Polygon arrowArea = new Polygon();
        Point[] intermediatePoints = arrow.getIntermediatePoints();
        arrowArea.addPoint(arrow.getPosition().x + 3, arrow.getPosition().y);
        if (intermediatePoints != null && intermediatePoints.length > 0)
        {
            for (int i = 0; i < intermediatePoints.length; i++)
            {
                x = (int) intermediatePoints[i].getX() + 3;
                y = (int) intermediatePoints[i].getY();
                arrowArea.addPoint(x, y);
            }
        }
        arrowArea.addPoint(arrow.getHeadPosition().x + 3,
                arrow.getHeadPosition().y);
        arrowArea.addPoint(arrow.getHeadPosition().x - 3,
                arrow.getHeadPosition().y);
        if (intermediatePoints != null && intermediatePoints.length > 0)
        {
            for (int i = intermediatePoints.length - 1; i >= 0; i--)
            {
                x = (int) intermediatePoints[i].getX() - 3;
                y = (int) intermediatePoints[i].getY();
                arrowArea.addPoint(x, y);
            }
        }
        arrowArea.addPoint(arrow.getPosition().x - 3, arrow.getPosition().y);
        return arrowArea;
    }

    private void deleteNode(UIObject sel_obj)
    {
        isModify = true;

        if (sel_obj instanceof BaseActivity)
        {
            sel_obj.remove(((BaseActivity) sel_obj).getRoleNameField());
            sel_obj.remove(((BaseActivity) sel_obj).getNameField());
            remove(sel_obj);
        }
        else if (sel_obj instanceof UIExit)
        {
            if (((UIExit) sel_obj).getNameField() != null)
            {
                remove(((UIExit) sel_obj).getNameField());
            }
        }
        else if (sel_obj instanceof UISubProcess)
        {
            if (((UISubProcess) sel_obj).getNameLabel() != null)
            {
                remove(((UISubProcess) sel_obj).getNameLabel());
            }
        }
        uiObjects.removeElement(sel_obj);
        moveFocus(); // pick another for focus
    }

    private void moveFocus()
    {
        int loc = uiObjects.size() - 1; // pick another for focus
        if ((uiObjects.elementAt(loc) instanceof UIStart) && (loc > 0))
        {
            loc--;
        }
        selObj = (UIObject) uiObjects.elementAt(loc); // setup for selection
        ghostObj = selObj; // highlight it
        setSelObj(selObj); // select it
        twfagp.fireSelectionEvent(); // redraw
    }

    private void deleteArrow(BaseArrow _arrow)
    {
        isModify = true;
        remove(_arrow.getLabelField());
        uiObjects.removeElement(selObj);
        moveFocus(); // pick another for focus
    }

    void setNodeType(int _type)
    {
        nodeType = _type;
        if (nodeType == NO_OP_TYPE)
        {
            setCursor(defaultCursor);
        }
        else
        {
            setCursor(addNodeCursor);
        }
    }

    int getNodeType()
    {
        return nodeType;
    }

    protected boolean createNode(String node_name, int node_type)
    {
        try
        {
            if (model != null)
            {
                modelNode = model.addWorkflowTask(node_name, node_type);

                checkToEnlarge(selObj.getPosition());
                return true;
            }
            else if (modelPI != null)
            {

                modelNodeInstance = modelPI.addWorkflowTaskInstance(node_name,
                        node_type);

                checkToEnlarge(selObj.getPosition());
                return true;
            }
            return false;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    // comments this method as not using May 30 2002
    /*
     * void createSubNode(int _x, int _y) { UIObject obj = new UISubProcess (new
     * Point(_x-30, _y-30), this); obj.setObjColor(obj.getState()); selObj =
     * obj;
     * 
     * if ( !createNode(selObj.getObjectName(), Node.TYPE_SUB_PROCESS) ) {
     * return; } //((BaseConnection)selObj).getImageButton().setLocation(_x-5,
     * _y-5);//shailaja }
     * 
     * 
     * void createDelayNode(int _x, int _y) { UIObject obj = new UIDelay (new
     * Point(_x-30, _y-30), this); obj.setObjColor(obj.getState()); selObj =
     * obj;
     * 
     * if ( !createNode(selObj.getObjectName(), Node.TYPE_DELAY) ) { return; }
     * //((BaseConnection)selObj).getImageButton().setLocation(_x-5,
     * _y-5);//shailaja }
     */
    void createConditionNode(int _x, int _y)
    {
        UIObject obj = new UICondition(new Point(_x - 30, _y - 30), this);
        obj.setObjColor(obj.getState());
        selObj = obj;

        if (!createNode(selObj.getObjectName(), WorkflowConstants.CONDITION))
        {
            return;
        }
        // ((BaseConnection)selObj).getImageButton().setLocation(_x-5, _y-5);
    }

    void createORNode(int _x, int _y)
    {
        UIObject obj = new UIOR(new Point(_x - 30, _y - 30), this);
        obj.setObjColor(obj.getState());
        selObj = obj;

        if (!createNode(selObj.getObjectName(), WorkflowConstants.OR))
        {
            return;
        }
        // ((BaseConnection)selObj).getImageButton().setLocation(_x-5, _y-5);
    }

    void createANDNode(int _x, int _y)
    {
        UIObject obj = new UIAND(new Point(_x - 30, _y - 30), this);
        obj.setObjColor(obj.getState());
        selObj = obj;

        if (!createNode(selObj.getObjectName(), WorkflowConstants.AND))
        {
            return;
        }
        // ((BaseConnection)selObj).getImageButton().setLocation(_x-5, _y-5);
    }

    void createSubNode(int _x, int _y)
    {
        UIObject obj = new UISubProcess(new Point(_x - 30, _y - 30), this);
        obj.setObjColor(obj.getState());
        selObj = obj;

        // be sure to add the label for the subprocess node to the GVPanel
        JTextArea lbl = ((UISubProcess) obj).getNameLabel();
        lbl.setText("");
        add(lbl);

        if (!createNode(selObj.getObjectName(), WorkflowConstants.SUB_PROCESS))
        {
            return;
        }
        // ((BaseConnection)selObj).getImageButton().setLocation(_x-5,
        // _y-5);//shailaja
    }

    void createExitNode(int _x, int _y)
    {
        UIObject obj = new UIExit(new Point(_x - 30, _y - 30), this);
        obj.setObjColor(obj.getState());
        selObj = obj;

        if (!createNode(selObj.getObjectName(), WorkflowConstants.STOP))
        {
            return;
        }
        selObj.setPosition(new Point(_x, _y));
        ((UIExit) selObj).getNameField().setLocation(_x - 28, _y - 10);
    }

    void createStartNode(int _x, int _y) throws Exception
    {
        // should only be called when newing a plan
        UIObject obj = new UIStart(new Point(_x - 30, _y - 30), this);
        selObj = obj;
        if (!createNode(selObj.getObjectName(), WorkflowConstants.START))
        {
            return;
        }
        selObj.setModelObject(modelNode);
        selObj.setPosition(new Point(_x, _y));
        ((UIStart) selObj).getNameLabel().setLocation(_x - 28, _y - 10);

        uiObjects.addElement(selObj);
        add(((UIStart) selObj).getNameLabel());
        ghostObj = selObj;
        selObj.translate(ghostObj.getPosition().x - selObj.getPosition().x,
                ghostObj.getPosition().y - selObj.getPosition().y);
        ((WorkflowTask) selObj.getModelObject()).setPosition(selObj
                .getPosition());
        ((UIStart) selObj).setLocation();
        setSelObj(selObj);
        twfagp.fireSelectionEvent();
    }

    protected void createActivityNode(int _x, int _y)
    {
        UIObject obj = new UIActivity(new Point(_x - UIActivity.getStdWidth()
                / 2, _y - UIActivity.getStdHeight() / 2), this);
        obj.setObjColor(obj.getState());

        selObj = obj;
        if (!createNode(selObj.getObjectName(), WorkflowConstants.ACTIVITY))
        {

            return;
        }
        if (modelNode != null)
        {
            // set sequence No

            modelNode.setSequence(++maxSequenceNo);
        }
        else if (modelNodeInstance != null)
        {
            modelNodeInstance.setSequence(++maxSequenceNo);

        }

        selObj.setPosition(new Point(_x, _y));
        selObj.setBounds(_x - UIActivity.getStdWidth() / 2,
                _y - UIActivity.getStdHeight() / 2, UIActivity.getStdWidth(),
                UIActivity.getStdHeight());
        requestFocus();
    }

    void createActionArrow(int _x, int _y)
    {
        isModify = true;
        BaseArrow obj = new BaseArrow(new Point(_x, _y), this);
        selObj = obj;
    }

    private void createFieldForArrow(BaseArrow obj)
    {
        GenericJTextField _field = new GenericJTextField(
                WorkflowConstants.MAX_NODE_NAME_LENGTH);
        Font fon = new Font("SansSerif", Font.PLAIN, 10);
        _field.setFont(fon);
        _field.setText(obj.getObjectName());
        _field.addKeyListener(obj);
        _field.addFocusListener(obj);
        add(_field);
        obj.setLabelField(_field);
    }

    public void keyReleased(KeyEvent event)
    {
    }

    public void keyTyped(KeyEvent event)
    {
    }

    public synchronized void keyPressed(KeyEvent event)
    {

        if (!editMode || selObj == null
                || event.getKeyCode() != event.VK_DELETE)
        {
            return;
        }

        if (selObj instanceof UIStart)
        {
            // not allowed to delete the one and only start node
            return;
        }

        UIObject obj = null;

        if (!(selObj instanceof BaseArrow))
        {
            // If it's a node, delete its connected arrows first.
            try
            {
                if (model != null)
                {
                    model.removeWorkflowTask((WorkflowTask) selObj
                            .getModelObject());
                }
                else if (modelPI != null)
                {
                    WorkflowTaskInstance wfti = (WorkflowTaskInstance) selObj
                            .getModelObject();
                    int state = wfti.getTaskState();
                    // TomyD do not allow deletion of an active task.
                    if (state == WorkflowConstants.STATE_RUNNING
                            || state == WorkflowConstants.STATE_COMPLETED)
                    {
                        return;
                    }
                    modelPI.removeWorkflowTaskInstance(wfti);
                }

                deleteArrows(selObj);
                sourceObj = (WorkflowTask) selObj.getModelObject();
                deleteNode(selObj);
            }
            catch (Exception e)
            {
                return;
            }
        }
        else
        {
            // removing an arrow
            BaseArrow _obj = (BaseArrow) selObj;
            try
            {
                if (model != null)
                {
                    ((WorkflowTask) (_obj.getSourceNode()).getModelObject())
                            .removeOutgoingArrow(((WorkflowArrow) _obj
                                    .getModelObject()));

                    ((WorkflowTask) (_obj.getTargetNode()).getModelObject())
                            .removeIncommingArrow(((WorkflowArrow) _obj
                                    .getModelObject()));
                }
                else if (modelPI != null)
                {

                    modelPI.removeWorkflowArrowInstance(((WorkflowArrowInstance) _obj
                            .getModelObject()));
                }
                sourceObj = (WorkflowArrow) _obj.getModelObject();
                deleteArrow(_obj);
            }
            catch (Exception e)
            {
                return;
            }
        }

        selObj = null;
        repaint();
    }

    private boolean deleteArrowInstance(WorkflowArrowInstance arrow_instance)
    {
        try
        {
            if (arrow_instance.isActive())
            {
                int _reply = WindowUtil
                        .showMsgDlg(
                                WindowUtil.getFrame(this),
                                AppletHelper
                                        .getI18nContent("msg_arrow_with_pending_events"),
                                AppletHelper
                                        .getI18nContent("msg_event_pending_warning"),
                                WindowUtil.OPTION_YESNO,
                                WindowUtil.TYPE_QUESTION);
                return _reply == WindowUtil.RESULT_YES;
            }
            else
            {
                modelPI.removeWorkflowArrowInstance(arrow_instance);
                return true;
            }
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private void deleteArrows(UIObject sel_obj)
    {
        isModify = true;
        UIObject obj = null;
        int i = uiObjects.size() - 1;
        while (i >= 0)
        {
            obj = (UIObject) uiObjects.elementAt(i--);
            if (obj instanceof BaseArrow)
            {
                BaseArrow _obj = (BaseArrow) obj;
                // Go through all the arrows.
                if (_obj.getSourceNode() == sel_obj
                        || _obj.getTargetNode() == sel_obj)
                {
                    remove(_obj.getLabelField());
                    uiObjects.removeElement(_obj);
                    // model
                    try
                    {
                        if (model != null)
                        {
                            ((WorkflowTask) (_obj.getSourceNode())
                                    .getModelObject())
                                    .removeOutgoingArrow(((WorkflowArrow) _obj
                                            .getModelObject()));

                            ((WorkflowTask) (_obj.getTargetNode())
                                    .getModelObject())
                                    .removeIncommingArrow(((WorkflowArrow) _obj
                                            .getModelObject()));

                        }
                        else if (modelPI != null)
                        {
                            modelPI.removeWorkflowArrowInstance(((WorkflowArrowInstance) _obj
                                    .getModelObject()));
                        }
                    }
                    catch (Exception e)
                    {
                        return;
                    }
                }// end of inner if
            }// end of outer if
        }// end of while
    }

    private boolean addArrow(BaseArrow _arrow, UIObject ui_obj,
            Point[] pointArray)
    {
        isModify = true;
        _arrow.moveArrowRect();

        // This line resolve a bug. Never comment it out.
        if (!isFirst(_arrow))
            return false;

        // first perform validation (for condition node)
        validateUniqueArrowName(_arrow);

        uiObjects.addElement(_arrow);

        if (model != null)
        {
            try
            {
                WorkflowArrow modelArrow = model
                        .addArrow(_arrow.getObjectName(),
                                WorkflowConstants.REGULAR_ARROW,
                                (WorkflowTask) (_arrow.getSourceNode()
                                        .getModelObject()),
                                (WorkflowTask) (ui_obj.getModelObject()));

                modelArrow.setStartPoint(new Point(_arrow.getPosition()));
                modelArrow.setPoints(pointArray);
                modelArrow.setEndPoint(new Point(_arrow.getHeadPosition()));
                _arrow.setDefault(modelArrow.isDefault());
                _arrow.setModelObject(modelArrow);
            }
            catch (Exception e)
            {
                return false;
            }
        }
        else if (modelPI != null)
        {
            try
            {
                WorkflowArrowInstance modelArrowInstance = modelPI
                        .addArrowInstance(
                                _arrow.getObjectName(),
                                WorkflowConstants.REGULAR_ARROW,
                                (WorkflowTaskInstance) (_arrow.getSourceNode()
                                        .getModelObject()),
                                (WorkflowTaskInstance) (ui_obj.getModelObject()));

                ((WorkflowArrow) modelArrowInstance).setStartPoint(new Point(
                        _arrow.getPosition()));
                ((WorkflowArrow) modelArrowInstance).setPoints(pointArray);
                ((WorkflowArrow) modelArrowInstance).setEndPoint(new Point(
                        _arrow.getHeadPosition()));
                _arrow.setDefault(modelArrowInstance.isDefault());
                _arrow.setModelObject(modelArrowInstance);
            }
            catch (Exception e)
            {
                return false;
            }
        }
        ghostObj = null;
        return true;
    }

    void mouseUpThis(MouseEvent ev)
    {
        addingNode = false; // reset to initial value

        if (selObj == null)
        {

            return;
        }
        if (!(ev.getSource() == this)
                && !(ev.getSource() instanceof UIActivity))
        {
            // no op; source maybe image buttons (which may cause multiple
            // arrows added to model)
            return;
        }
        int _x = ev.getX();
        int _y = ev.getY();

        if (editMode && (nodeType == ARROW_TYPE)
                && (selObj instanceof BaseArrow))
        {
            if (isOnNode(_x, _y) && isButton3(ev))
                return;
            // mouse up after dragging a to-be-added arrow.
            BaseArrow _arrow = (BaseArrow) selObj;
            int i = uiObjects.size();

            while (i > 0)
            {
                UIObject ui_obj = (UIObject) uiObjects.elementAt(--i);

                if (ui_obj instanceof BaseArrow)
                {
                    continue;
                }

                if (((Rectangle) ui_obj.getArea()).contains(_arrow
                        .getHeadPosition()))
                {
                    if ((_arrow.setArrowDest(ui_obj)) && // test for valid
                            // dest node
                            (_arrow.getSourceNode() != ui_obj) && // no
                            // self-pointing
                            // arrow
                            // (for now)
                            !(_arrow.getSourceNode() instanceof UIExit) && // no
                            // out-going
                            // arrow
                            // from
                            // UIExit
                            !(ui_obj instanceof UIStart)) // no in-coming
                    // arrow from
                    // UIStart
                    {
                        _arrow.createIntermediatePointsArray();
                        setLastArrow(_arrow);

                        if (addArrow(_arrow, ui_obj,
                                _arrow.getIntermediatePoints()))
                        {
                            createFieldForArrow(_arrow);
                            drawLines.removeAllElements();
                            arrowStarted = false;
                            drawMultiLine = false;
                            addingArrow = false;
                            // _arrow.setArrowArea(calculateArrowArea(_arrow));
                            _arrow.setArrowArea(_arrow.drawArrowArea());
                            setSelObj(_arrow);
                            twfagp.fireSelectionEvent();

                            return;
                        }
                        // this else is added by Hamid
                        else
                        {
                            drawLines.removeAllElements();
                            repaint();
                        }

                    }
                }
            }

            if (ev.getSource() == this && drawMultiLine)
            {
                int A[] = _arrow.endMultipleLine(_x, _y);
                _arrow.createIntermediatePointsArray();
                addLine(new DrawLine(A[0], A[1], A[2], A[3]));

            }

            return;
        }
        // else if ( editMode && !(selObj instanceof BaseArrow) && !(nodeType ==
        // ARROW_TYPE) && (ghostObj != null) )
        else if (editMode && !(selObj instanceof BaseArrow)
                && (ghostObj != null))
        {

            // moved a node
            if (!(nodeType == ARROW_TYPE))
            {
                moveNode();
            }
            drawLines.removeAllElements();
            repaint();

        }
        else if ((selObj instanceof BaseArrow) && (nodeType == NO_OP_TYPE)
                && (ghostObj instanceof BaseArrow))
        {

            // mouse up after an existing arrow is pressed.
            if (!selObj.isSelected())
            {
                setSelObj(selObj);
                twfagp.fireSelectionEvent();
            }
            BaseArrow _arrow = (BaseArrow) selObj;
            BaseArrow ghost_obj = (BaseArrow) ghostObj;
            int i = uiObjects.size();
            while (i > 0)
            {
                UIObject ui_obj = (UIObject) uiObjects.elementAt(--i);
                if (ui_obj instanceof BaseArrow)
                {
                    continue;
                }
                if ((ui_obj != _arrow.getSourceNode())
                        && (ui_obj != _arrow.getTargetNode())
                        && ((Rectangle) ui_obj.getArea()).contains(_x, _y))
                {
                    if (editMode && selArrowHead
                            && !(ui_obj instanceof UIStart)) // no in-coming
                    // arrow into
                    // UIStart
                    {
                        // process edit does not allow retargetting arrow
                        if (model != null)
                        {
                            _arrow.setArrowDest(ui_obj);
                            ((WorkflowArrow) selObj.getModelObject())
                                    .setTargetNode((WorkflowTask) ui_obj
                                            .getModelObject());
                        }
                    }
                    break;
                }
            }
        }
        else if (ev.getSource() == this)
        {
            if (isOnNode(_x, _y) && isButton3(ev))
            {
                if (isPopupEnabled())
                    doNodePopup(ev);
            }
        }
        // repaint();

        if (selObj == ev.getSource())
        {
            // mouse up on UIActivity
            if (!selObj.isSelected())
            {
                setSelObj(selObj);
                twfagp.fireSelectionEvent();

            }

        }
        else if (selObj.getArea() != null)
        {
            // mouse up on GVPane
            if (((Rectangle) selObj.getArea()).contains(_x, _y)
                    || ((selObj instanceof BaseArrow) && (((BaseArrow) selObj)
                            .getHeadArea()).contains(_x, _y)))
            {
                if (!selObj.isSelected())
                {

                    setSelObj(selObj);
                    twfagp.fireSelectionEvent();
                }
            }
        }
        /*
         * //draw a rectangle to zoom in a specific area, HF if (
         * (ev.getSource() == this) && zoomBox ) { try { // Scale to fit. int
         * xdelta = Math.abs(x2 - x1); int ydelta = Math.abs(y2 - y1); int xmin
         * = Math.min(x1, x2); int ymin = Math.min(y1, y2); double tx =
         * at.getTranslateX() / at.getScaleX(); double ty = at.getTranslateY() /
         * at.getScaleY(); double xrelative = xmin / at.getScaleX(); double
         * yrelative = ymin / at.getScaleY(); double sx= (double)getWidth() /
         * (double)xdelta; double sy = (double)getHeight() / (double)ydelta; //
         * Translate back to origin. at.translate(-tx, -ty); // Now scale based
         * on the rectangle. at.scale(sx, sy); // Translate to the new origin.
         * at.translate(-xrelative + tx, -yrelative + ty); zoomFactorX *= sx;
         * zoomFactorY *= sy; } catch (Exception e) {}
         * 
         * x2 = -1; repaint(); }
         */
    }

    /**
     * Utility method to move node to its selected position.
     */
    private void moveNode()
    {
        // moved a node
        Graphics g = getGraphics();
        Graphics2D g2 = (Graphics2D) g;
        selObj.translate(ghostObj.getPosition().x - selObj.getPosition().x,
                ghostObj.getPosition().y - selObj.getPosition().y);
        // after translation is done update the model with latest coordinates
        ((WorkflowTask) selObj.getModelObject()).setPosition(selObj
                .getPosition());// will notify TNPane to update position
        checkToEnlarge(selObj.getPosition());
        if (selObj instanceof BaseActivity)
        {
            ((UIActivity) selObj).setLocation();
        }
        else if (selObj instanceof UIStart)
        {
            ((UIStart) selObj).setLocation();
        }
        else if (selObj instanceof UIExit)
        {
            ((UIExit) selObj).setLocation(g2);
        }
        else if (selObj instanceof BaseConnection)
        {
            // ((BaseConnection)selObj).setLocation ();
        }
        repaint();
        g2.dispose();
    }

    void mouseDragThis(MouseEvent ev)
    {
        isModify = true;
        Graphics g = getGraphics();
        Graphics2D g2 = (Graphics2D) g;
        if (!editMode || addingNode || (selObj == null))
        {
            return;
        }

        int _x = ev.getX();
        int _y = ev.getY() + activityYOffset;

        /*
         * if (( ev.getSource() == this ) && zoomBox ) { //HF x2 = _x; y2 = _y;
         * repaint(); }
         */

        if ((selObj instanceof BaseArrow)
                && ((((BaseArrow) selObj).getSourceNode() instanceof UIActivity) || (((BaseArrow) selObj)
                        .getSourceNode() instanceof UICondition)))
        {
            BaseArrow _arrow = (BaseArrow) selObj;
            if (((_arrow.getSourceNode().getLocation().x + _x) < 5)
                    || ((_arrow.getSourceNode().getLocation().y + _y) < 5))
            {
                return;// must do so for this case!!!
            }
        }
        else if (((selObj.getLocation().x + _x) < 5)
                || ((selObj.getLocation().y + _y) < 5))
        {
            // to prevent nodes being dragged to negative area
            return;
        }

        if ((nodeType == ARROW_TYPE) && (selObj instanceof BaseArrow))
        {
            // adding/dragging a new arrow from a node to be added
            BaseArrow _arrow = (BaseArrow) selObj;
            if (firstClick || drawMultiLine)
            {
                if (uiActivityOffset)
                {
                    _arrow.drawMultipleLine(_arrow.getSourceNode()
                            .getLocation().x + _x, _arrow.getSourceNode()
                            .getLocation().y + _y, g2);
                }
                else
                {
                    _arrow.drawMultipleLine(_x, _y, g2);
                    _arrow.moveHeadPosition(_x, _y);
                }
            }
            // _arrow.draw(g2, false);

            if (selArrowHead)
            {

                if (ev.getSource() instanceof UIActivity)
                {
                    if ((ox == 0) && (oy == 0))
                    {
                        // arrow originating from an activity node
                        ox = _x;
                        oy = _y;
                    }
                    int delta_x = _x - ox;
                    int delta_y = _y - oy;

                    _arrow.translate2(delta_x, delta_y);
                }
                else
                {
                    // dragging arrow from a non-UIActivity node
                    _arrow.translate2(_x - ox, _y - oy);
                }

                ox = _x;
                oy = _y;

                if (firstClick || drawMultiLine)
                {
                    if (uiActivityOffset)
                    {
                        _arrow.drawMultipleLine(_arrow.getSourceNode()
                                .getLocation().x + _x, _arrow.getSourceNode()
                                .getLocation().y + _y, g2);
                    }
                    else
                    {
                        _arrow.drawMultipleLine(_x, _y, g2);
                    }
                }
                // _arrow.draw(g2, false);//shailaja
            }
        }
        else if ((nodeType == NO_OP_TYPE) && (ghostObj != null))
        {
            // dragging an existing node/arrow
            // ghostObj.draw(g2, false);
            if ((ghostObj instanceof BaseArrow)
                    || (selArrowHead && (ghostObj instanceof BaseArrow)))
            {
                // dragging an existing arrow
                // BaseArrow ghost_obj = (BaseArrow)ghostObj;
                // ghost_obj.translate2 (_x - ox, _y - oy);
                ghostObj = null;
                return;
            }

            ghostObj.draw(g2, false);
            // dragging an existing node
            ghostObj.translate(_x - ox, _y - oy);

            ox = _x;
            oy = _y;
            ghostObj.draw(g2, false);

            /*
             * ox = _x; oy = _y; ghostObj.draw(g2, false);
             */

        }
        g2.dispose();
    }

    private boolean isButton3(InputEvent e)
    {
        return (e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK;
    }

    private void doNodePopup(MouseEvent ev)
    {
        isModify = true;
        if (!isPopupEnabled())
            return;
        if (isPopupEnabled())

            if (onNodeType == UISTART_TYPE)
            {
                ((UIStart) onNodeObj).maybeShowPopup(ev);
                requestFocus();
            }

        if (onNodeType == UICONDITION_TYPE)
        {
            ((UICondition) onNodeObj).maybeShowPopup(ev);
            requestFocus();
        }

        if (onNodeType == UIAND_TYPE)
        {
            ((UIAND) onNodeObj).maybeShowPopup(ev);
            requestFocus();
        }

        if (onNodeType == UIOR_TYPE)
        {
            ((UIOR) onNodeObj).maybeShowPopup(ev);
            requestFocus();
        }
        // comments this as not using these three nodes
        /*
         * /if (onNodeType == UIDELAY_TYPE) {
         * ((UIDelay)onNodeObj).maybeShowPopup(ev); requestFocus(); }
         * 
         * if (onNodeType == UICHAINED_TYPE) {
         * ((UIChainedProcess)onNodeObj).maybeShowPopup(ev); requestFocus(); }
         * 
         * if (onNodeType == UISUB_TYPE) {
         * ((UISubProcess)onNodeObj).maybeShowPopup(ev); requestFocus(); }
         */

        if (onNodeType == UISUB_TYPE)
        {
            ((UISubProcess) onNodeObj).maybeShowPopup(ev);
            requestFocus();
        }
    }

    public void addLine(DrawLine t1)
    {
        // isModify = "true";
        drawLines.addElement(t1);
    }

    public void setActivityY(int offset)
    {
        activityYOffset = offset;
    }

    void mouseDownThis(MouseEvent ev)
    {
        // selObj = null; // mcz 8/13/00 good
        // ghostObj = null; // mcz 8/13/00 good
        int _x = ev.getX();
        int _y = ev.getY();

        /*
         * if ( (ev.getSource() == this) && zoomBox ) { //HF x1 = _x; y1 = _y;
         * x2 = -1; }
         */

        Object source_obj = ev.getSource();

        if (nodeType == NO_OP_TYPE)
        {
            if ((source_obj == this) && isOnNode(_x, _y) && isButton3(ev))
            {
                if (isPopupEnabled())
                    doNodePopup(ev);
            }
            else
            {
                // mouse down on a node/arrow for selecting/dragging!!!
                int i = uiObjects.size();

                while (i > 0)
                {
                    UIObject obj = (UIObject) uiObjects.elementAt(--i);
                    requestFocus(); // added by ::MS

                    if (editMode && (obj instanceof BaseArrow))
                    {
                        /*
                         * if ( !(ev.getSource() instanceof UIActivity) &&
                         * (((BaseArrow)obj).getHeadArea()).contains(_x, _y) ||
                         * (((BaseArrow) obj).getArrowArea()).contains(_x, _y) )
                         * {
                         */
                        if ((((BaseArrow) obj).getHeadArea()).contains(_x, _y)
                                || (((BaseArrow) obj).getArrowArea()).contains(
                                        _x, _y))
                        {
                            // selecting/dragging an arrow.
                            selectArrow((BaseArrow) obj, _x, _y);
                            return;// end of selecting/dragging an arrow.
                        }
                    }
                    else if (((obj instanceof UIActivity) && (obj == ev
                            .getSource()))
                            || (!(ev.getSource() instanceof UIActivity)
                                    && !(obj instanceof UIActivity)
                                    && (obj.getArea() != null) && (((Rectangle) obj
                                    .getArea()).contains(_x, _y))))
                    {
                        if (!editMode)
                        {
                            if (selObj != obj)
                            {
                                selObj = obj;
                            }

                            return; // selecting; no dragging
                        }
                        // in edit

                        selectNode(obj, _x, _y);
                        return;
                    }
                    else if (!editMode
                            && !(ev.getSource() instanceof UIActivity)
                            && (obj instanceof BaseArrow))
                    {
                        if ((((BaseArrow) obj).getHeadArea()).contains(_x, _y))
                        {
                            selObj = obj;// for arrow selection
                            return;
                        }
                    }
                }
                // mouse down on blank space
                setSelObj(null);// deselect node
                twfagp.fireSelectionEvent();
                return;
            }
        }
        else if ((source_obj == this) && (nodeType == ARROW_TYPE)) // create
        // arrow on
        // gPane
        {
            if (isOnNode(_x, _y))
            {
                if (isButton3(ev)) // right click on node to pop up the menu
                {
                    if (isPopupEnabled())
                        doNodePopup(ev);
                }
                else
                // left click on node to create an arrow
                {
                    createActionArrow(_x, _y);
                    // adding an arrow
                    BaseArrow _arrow = (BaseArrow) selObj;
                    addingArrow(_arrow, _x, _y);
                    firstClick = true;
                    arrowStarted = true;
                    _arrow.startMultipleLine(_x, _y);
                    // repaint();
                    return;
                }
            }
            else
            {
                firstClick = false;
                if (arrowStarted)
                {
                    drawMultiLine = true;
                    uiActivityOffset = false;
                }
                else
                {
                    selObj = null;
                    ghostObj = null;
                }
                return;
            }

        }
        else if ((source_obj == this) && (nodeType != NO_OP_TYPE))
        {
            if (isOnNode(_x, _y) && isButton3(ev))
            {
                if (isPopupEnabled())
                    doNodePopup(ev);
            }
            else
            {
                // mouse down to create nodes
                addNode(_x, _y);
                moveNode();
                selectNode(selObj, _x, _y);
                if (arrowStarted)
                {
                    drawLines.removeAllElements();
                }
                // repaint();
                return;// end of adding non arrow mode
            }
        }
        else if ((source_obj instanceof UIActivity) && (nodeType == ARROW_TYPE))
        {
            _y = _y + activityYOffset;
            addActivityArrow((UIActivity) source_obj, _x, _y);
            BaseArrow _arrow = (BaseArrow) selObj;

            firstClick = true;
            arrowStarted = true;
            uiActivityOffset = true;
            return; // end of adding arrow mode
        }

    }

    private void addNode(int _x, int _y)
    {
        isModify = true;
        // adding a node
        switch (nodeType)
        {
            case ACTIVITY_TYPE:
                createActivityNode(_x, _y);
                break;

            case AND_TYPE:
                createANDNode(_x, _y);
                break;

            case OR_TYPE:
                createORNode(_x, _y);
                break;

            case SUB_TYPE:
                createSubNode(_x, _y);
                break;

            case CONDITION_TYPE:
                createConditionNode(_x, _y);
                break;
            // comments this as not using these three nodes May 30 2002
            /*
             * case CHAINED_TYPE: createChainedNode(_x, _y); break; case
             * DELAY_TYPE: createDelayNode(_x, _y); break;
             */
            case EXIT_TYPE:
                createExitNode(_x, _y);
                break;

            default:
                break;
        }

        addingNode = true;
        uiObjects.addElement(selObj);

        if (selObj instanceof UIActivity)
        {
            add(selObj);
            if (model != null)
            {
                // try
                // {
                // ((ActivityNode)modelNode).setRole(selObj.getRoleName());
                // use correct method
                // }
                // catch (Exception e0)
                // {
                // }
            }
            else if (modelPI != null)
            {
                // try
                // {
                // use correct method
                // ((Node)modelNodeInstance).setRole(selObj.getRoleName());
                // }
                // catch (Exception e)
                // {
                // return;
                // }
            }
        }
        else if (selObj instanceof UIStart)
        {
            add(((UIStart) selObj).getNameLabel());
        }
        else if (selObj instanceof UIExit)
        {
            add(((UIExit) selObj).getNameField());
        }

        if (selObj instanceof BaseConnection)
        {
            BaseConnection castedConn = (BaseConnection) selObj;
            selObj.setArea(new Rectangle(selObj.getPosition().x, selObj
                    .getPosition().y, castedConn.getNodeWidth(), castedConn
                    .getNodeHeight()));
        }

        if (model != null)
        {
            selObj.setModelObject(modelNode);
        }
        else if (modelPI != null)
        {
            selObj.setModelObject(modelNodeInstance);
        }
        ghostObj = selObj;
    }

    private void addingArrow(BaseArrow _arrow, int _x, int _y)
    {
        int i = uiObjects.size();
        // to find arrow source node
        while (i > 0)
        {
            UIObject ui_obj = (UIObject) uiObjects.elementAt(--i);
            if (ui_obj instanceof BaseArrow)
            {
                continue;
            }
            if (((Rectangle) ui_obj.getArea()).contains(_x, _y))
            {
                int _xx = ui_obj.getLocation().x + _x;
                int _yy = ui_obj.getLocation().y + _y;

                // arrow source node found
                addingArrow = true;
                _arrow.setSourceNode(ui_obj);
                _arrow.moveHeadPosition(_xx, _yy);

                selArrowHead = true;
                ox = _x;
                oy = _y;
                ghostObj = _arrow;
                return;

                /*
                 * addingArrow = true; createActionArrow(ui_obj.getLocation().x
                 * + _x, ui_obj.getLocation().y +_y); int _xx =
                 * ui_obj.getLocation().x + _x; int _yy = ui_obj.getLocation().y
                 * + _y;
                 * 
                 * //selObj is now an arrow // BaseArrow _arrow1 =
                 * (BaseArrow)selObj;
                 * 
                 * _arrow.setSourceNode(ui_obj); _arrow.moveHeadPosition(_xx,
                 * _yy); _arrow.startMultipleLine(_xx , _yy); selArrowHead =
                 * true; ox = 0; oy = 0; ghostObj = selObj; return;
                 */
            }
        }
    }

    protected void selectNode(UIObject ui_node, int _x, int _y)
    {
        selObj = ui_node;
        selArrowHead = false;

        if (ui_node instanceof UIActivity)
        {
            ghostObj = ((UIActivity) ui_node).getDummy();
            // See programming note at the top of UIActivity.java.
            requestFocus();
            //
            // Netscape workaround...
            if (WindowUtil.isUsingNetscape())
            {
                if (selObj instanceof UIActivity)
                {

                    selObj.requestFocus();
                }
            }
        }
        else if (ui_node instanceof UIAND)
        {
            ghostObj = ((UIAND) ui_node).getDummy();
        }
        else if (ui_node instanceof UIOR)
        {
            ghostObj = ((UIOR) ui_node).getDummy();
        }
        else if (ui_node instanceof UISubProcess)
        {
            ghostObj = ((UISubProcess) ui_node).getDummy();
        }

        else if (ui_node instanceof UICondition)
        {
            ghostObj = ((UICondition) ui_node).getDummy();
        }
        // commentsas not using these three nodes
        /*
         * else if ( ui_node instanceof UIChainedProcess ) { ghostObj =
         * ((UIChainedProcess)ui_node).getDummy(); } else if ( ui_node
         * instanceof UIDelay ) { ghostObj = ((UIDelay)ui_node).getDummy(); }
         */
        else if (ui_node instanceof UIExit)
        {
            ghostObj = ((UIExit) ui_node).getDummy();
        }
        else if (ui_node instanceof UIStart)
        {
            ghostObj = ((UIStart) ui_node).getDummy();
        }
        else
        {

        }
        ox = _x;
        oy = _y;
    }

    private void selectArrow(BaseArrow _arrow, int _x, int _y)
    {
        selObj = _arrow;
        ghostObj = _arrow.getDummy();
        ((BaseArrow) ghostObj).setHeadPosition(new Point(_x, _y));
        ox = _x;
        oy = _y;

        /*
         * selArrowHead = _arrow.checkSelArrowHead (_x, _y); ghostObj =
         * _arrow.getDummy(); if(selArrowHead) {
         * ((BaseArrow)ghostObj).setHeadPosition(new Point(_x, _y)); ox = _x; oy
         * = _y; } else { //no op for arrow tail ghostObj = null; }
         */
    }

    void mouseMovedThis(MouseEvent ev)
    {
        int _x = ev.getX();
        int _y = ev.getY();

        if (nodeType == ARROW_TYPE)
        {
            if (isOnNode(_x, _y))
            {
                setCursor(addNodeCursor);
            }
            else
            {
                setCursor(defaultCursor);
            }
        }
    }

    private void addActivityArrow(UIActivity ui_node, int _x, int _y)
    {
        isModify = true;
        // add an arrow originating from an activity node
        int i = uiObjects.size();

        while (i > 0)
        {
            UIObject ui_obj = (UIObject) uiObjects.elementAt(--i);
            if (!(ui_obj instanceof UIActivity))
                continue;
            if (ui_obj == ui_node)
            {
                addingArrow = true;
                createActionArrow(ui_obj.getLocation().x + _x,
                        ui_obj.getLocation().y + _y);
                int _xx = ui_obj.getLocation().x + _x;
                int _yy = ui_obj.getLocation().y + _y;

                // selObj is now an arrow
                BaseArrow _arrow = (BaseArrow) selObj;

                _arrow.setSourceNode(ui_obj);
                _arrow.moveHeadPosition(_xx, _yy);
                _arrow.startMultipleLine(_xx, _yy);
                selArrowHead = true;
                ox = 0;
                oy = 0;
                ghostObj = selObj;
                return;
            }
        }
    }

    private boolean isOnNode(int _x, int _y)
    {
        int i = uiObjects.size();

        while (i > 0)
        {
            UIObject ui_obj = (UIObject) uiObjects.elementAt(--i);
            if ((ui_obj instanceof BaseArrow) || (ui_obj instanceof UIExit))
            {
                continue;
            }

            if (((Rectangle) ui_obj.getArea()).contains(_x, _y))
            {
                if (ui_obj instanceof UIStart)
                {
                    onNodeType = UISTART_TYPE;
                    onNodeObj = ui_obj;
                }
                if (ui_obj instanceof UICondition)
                {
                    onNodeType = UICONDITION_TYPE;
                    onNodeObj = ui_obj;
                }
                if (ui_obj instanceof UIAND)
                {
                    onNodeType = UIAND_TYPE;
                    onNodeObj = ui_obj;
                }
                if (ui_obj instanceof UIOR)
                {
                    onNodeType = UIOR_TYPE;
                    onNodeObj = ui_obj;
                }
                if (ui_obj instanceof UISubProcess)
                {
                    onNodeType = UISUB_TYPE;
                    onNodeObj = ui_obj;
                }
                // comments as not using these three nodes May 30 2002
                /*
                 * if(ui_obj instanceof UIChainedProcess) { onNodeType =
                 * UICHAINED_TYPE; onNodeObj = ui_obj; }
                 * 
                 * if(ui_obj instanceof UIDelay) { onNodeType = UIDELAY_TYPE;
                 * onNodeObj = ui_obj; }
                 */
                return true;
            }
        }
        return false;
    }

    private void changeNode()
    {

        int m = uiObjects.size();
        UIObject obj;
        while (m > 0)
        {
            obj = (UIObject) uiObjects.elementAt(--m);
            if (obj instanceof BaseArrow)
            {
                continue;
            }
            if (obj.getModelObject() == sourceObj)
            {
                try
                {
                    obj.setObjectName(((WorkflowTask) sourceObj)
                            .getActivityName());
                }
                catch (Exception e0)
                {
                }

                if (obj instanceof UIActivity)
                {
                    ((BaseActivity) obj).getRoleNameField().setText(
                            obj.getRoleName());

                    ((BaseActivity) obj).getNameField().setText(
                            obj.getObjectName());

                }
                else if (obj instanceof BaseTerminal)
                {
                    if (obj instanceof UIExit)
                    {
                        ((UIExit) obj).setObjectName(((WorkflowTask) sourceObj)
                                .getActivityName());
                        /*
                         * ((UIExit)obj).getNameField().setText(
                         * msgCat.getMsg(obj.getObjectName()));
                         */
                    }
                }

                break;
            }
        }
    }

    private void changeNodeInstance() throws Exception
    {

        int m = uiObjects.size();
        UIObject obj;
        while (m > 0)
        {
            obj = (UIObject) uiObjects.elementAt(--m);
            if (obj instanceof BaseArrow)
            {
                continue;
            }
            if (obj.getModelObject() == sourceObj)
            {
                obj.setObjectName(((WorkflowTaskInstance) sourceObj)
                        .getActivityName());

                if (obj instanceof UIActivity)
                {
                    ((BaseActivity) obj).getRoleNameField().setText(
                            obj.getRoleName());

                    ((BaseActivity) obj).getNameField().setText(
                            obj.getObjectName());

                }
                else if (obj instanceof BaseTerminal)
                {
                    if (obj instanceof UIExit)
                    {
                        ((UIExit) obj)
                                .setObjectName(((WorkflowTaskInstance) sourceObj)
                                        .getActivityName());

                        /*
                         * ((UIExit)obj).getNameField().setText(
                         * msgCat.getMsg(obj.getObjectName()));
                         */
                    }
                }

                break;
            }
        }
    }

    public void setZoomIn()
    {
        if (zoomFactorX == 1.0)
        {
            width_original = getWidth();
            height_original = getHeight();
        }
        zoomFactorX *= 1.5;
        zoomFactorY *= 1.5;

        at.setToScale(zoomFactorX, zoomFactorY);
        width = (double) (getWidth() * 1.5);
        height = (double) (getHeight() * 1.5);
        size = getPreferredScrollableViewportSize();

        repaint();
    }

    public void setZoomOut()
    {
        if (zoomFactorX == 1.0)
        {
            width_original = getWidth();
            height_original = getHeight();
        }
        zoomFactorX /= 1.5;
        zoomFactorY /= 1.5;

        at.setToScale(zoomFactorX, zoomFactorY);
        width = (double) (getWidth() / 1.5);
        height = (double) (getHeight() / 1.5);
        size = getPreferredScrollableViewportSize();

        repaint();
    }

    /*
     * public void setZoomBox() { //HF Log.println(Log.LEVEL3,
     * "GVPane.setZoomBox()");
     * 
     * if ( zoomFactorX == 1.0 ) { width_original = getWidth(); height_original
     * = getHeight(); Log.println(Log.LEVEL1, "setZoomBox(): original width = "
     * + width_original + ", original height = " + height_original); }
     * Log.println(Log.LEVEL3, "SetZoomBox: zoomFactorX = " + zoomFactorX + ",
     * zoomFactorY = " + zoomFactorY); width = (double)( getWidth() /
     * zoomFactorX ); height = (double)( getHeight() / zoomFactorY ); size =
     * getPreferredScrollableViewportSize(); Log.println(Log.LEVEL3,
     * "setZoomBox(): new width = " + size.width + ", new height = " +
     * size.height);
     * 
     * repaint(); }
     */

    public void setIdentity()
    {
        if (zoomFactorX == 1.0)
        {
            width = WIDTH;
            height = HEIGHT;

        }
        else
        {
            width = width_original;
            height = height_original;
        }
        at.setToIdentity();
        size = getPreferredScrollableViewportSize();
        zoomFactorX = 1;
        zoomFactorY = 1;

        repaint();
    }

    // comments this method as not using Printing May 30 2002
    /*
     * public void setPrint() {
     * 
     * 
     * //Log.println(Log.LEVEL3, "GVPane.setPrint()");
     * 
     * //get a printer job /*PrinterJob pj = PrinterJob.getPrinterJob();
     * 
     * //get a page format PageFormat pf = new PageFormat(); PageFormat newPf =
     * pj.pageDialog(pf); if (newPf == pf) { // Dialog was canceled. return; }
     * 
     * //printing pages pages = new ComponentToBook(this, newPf);
     * pj.setPageable(pages);
     * 
     * //bring up the printer dialog and start printing try { if
     * (pj.printDialog()) { //get adjustable size for zooming
     * getPreferredScrollableViewportSize();
     * 
     * ProgressDlg.displayProgressDlg(frame, msgCat.getMsg("Printing in
     * progress...please wait."), applet, true); pj.print();
     * ProgressDlg.closeProgressDlg(); } else { //Log.println(Log.LEVEL3, "print
     * Cancelled"); ProgressDlg.closeProgressDlg(); } } catch (PrinterException
     * e) { e.printStackTrace(System.err); //Log.println(Log.LEVEL0, e); }
     */
    // }*/
    /**
     * These methods are added so that we can disable the popup menu of the
     * nodes when viewing an archived template.
     */
    public boolean isPopupEnabled()
    {
        return popupEnabled;
    }

    public void setPopupEnabled(boolean flag)
    {
        popupEnabled = flag;
    }

    /**
     * This method returns true under the following conditions: 1. When the
     * argument can be added (because it is the only arrow that will be
     * connecting two nodes. Basically, you cannot have two outgoing arrows from
     * a source node connecting to the same target node. 2. When the arrow is
     * the only outgoing arrow for a Start node or an activity node. 3. If the
     * user is trying to connect a condition node to another condition node.
     * This method returns false if there exists another arrow connecting the
     * two nodes that this arrow is intended to connect.
     */
    protected boolean isFirst(BaseArrow baseArrow)
    {
        if (baseArrow == null)
            return false;

        int i = uiObjects.size();
        WorkflowTask wfTask = (WorkflowTask) baseArrow.getSourceNode()
                .getModelObject();
        boolean showMessageDialog = false;
        String message = null;
        while (i > 0)
        {
            UIObject ui = (UIObject) uiObjects.elementAt(--i);
            if (ui instanceof BaseArrow
                    && ((BaseArrow) ui).getSourceNode() == baseArrow
                            .getSourceNode()
                    && ((BaseArrow) ui).getTargetNode() == baseArrow
                            .getTargetNode() && (BaseArrow) ui != baseArrow)
            {
                message = AppletHelper.getI18nContent("msg_two_arrows");
                showMessageDialog = true;
            }
            else if ((wfTask.getType() == WorkflowConstants.START || wfTask
                    .getType() == WorkflowConstants.ACTIVITY)
                    && wfTask.hasValidOutgoingArrows())
            {
                message = AppletHelper
                        .getI18nContent("msg_two_outgoing_arrows");
                showMessageDialog = true;
            }
            else if (wfTask.getType() == WorkflowConstants.CONDITION
                    && ((WorkflowTask) baseArrow.getTargetNode()
                            .getModelObject()).getType() == WorkflowConstants.CONDITION)
            {
                message = AppletHelper
                        .getI18nContent("msg_condition_to_condition");
                showMessageDialog = true;
            }

            if (showMessageDialog)
            {
                drawLines.removeAllElements();
                repaint();
                arrowStarted = false;
                drawMultiLine = false;
                JOptionPane.showMessageDialog(null, message);
                return false;
            }
        }
        return true;
    }

    // make sure there's a valid workflow before saving. Valid
    // workflow should have a start node, at least one exit node, and
    // all other nodes should have at least on incoming arrow and
    // one outgoing arrow. Also all node info should be populated.
    private boolean doValidation(Vector workflowTasks)
    {
        Iterator workflowIterator = workflowTasks.iterator();
        boolean hasActivityNode = false;

        while (workflowIterator.hasNext())
        {
            WorkflowTask workflowTask = (WorkflowTask) workflowIterator.next();

            // check only those nodes which are not marked deleted
            if (workflowTask.getStructuralState() != WorkflowConstants.REMOVED)
            {
                if (workflowTask.getType() == WorkflowConstants.STOP)
                {

                    hasExitNode = true;
                }

                if (!workflowTask.isValid())
                {
                    JOptionPane
                            .showMessageDialog(null, AppletHelper
                                    .getI18nContent("msg_invalid_workflow"));

                    return false;
                }
                else if (workflowTask.getType() == WorkflowConstants.ACTIVITY)
                {
                    hasActivityNode = true;
                }

            }

        }

        if (hasExitNode && hasActivityNode)
        {

            return true;
            // save workflow here
        }
        else
        {
            JOptionPane.showMessageDialog(null,
                    AppletHelper.getI18nContent("msg_invalid_workflow")
                            + AppletHelper.getI18nContent("msg_exit_node"));

            return false;

        }

    }

    // save workflow
    public boolean saveWorkFlow()
    {
        if (model != null)
        {
            if (doValidation(model.getWorkflowTasks()))
            {
                Vector responseObjVector = new Vector();
                responseObjVector.add("save");
                responseObjVector.add(model);
                getEnvoyJApplet().appendDataToPostConnection(responseObjVector,
                        "modifyUrl");
                System.gc();
                return true;

            }
            else
                return false;
        }
        else if (modelPI != null)
        {
            if (doValidation(modelPI.getWorkflowInstanceTasks()))
            {
                String targetUrl = m_isReady ? "readyURL" : "modifyUrl";
                Vector responseObjVector = new Vector();
                responseObjVector.add("save");
                responseObjVector.add(modelPI);
                responseObjVector.add(getModifiedTaskInfoMap());
                getEnvoyJApplet().appendDataToPostConnection(responseObjVector,
                        targetUrl);
                return true;
            }
            else
                return false;
        }
        else
            return false;
    }

    // determines whether it should go to the "Ready" tab of
    // My Jobs UI (after save)
    private void setIsReadyFlag(Vector p_postData)
    {
        if (p_postData.size() > 5)
        {
            Boolean isReady = (Boolean) p_postData.elementAt(5);
            if (isReady != null)
            {
                m_isReady = isReady.booleanValue();
            }
        }

    }

    // For a condition node we have to validate the outgoing arrow name.
    // During a drag and drop, it generates a default name which in the
    // modification process, it could create one with the same name of
    // an existing outgoing arrow. This method will resolve this issue
    // before going to the server side and getting an exception.
    private void validateUniqueArrowName(BaseArrow p_baseArrow)
    {
        WorkflowTask wfTask = (WorkflowTask) p_baseArrow.getSourceNode()
                .getModelObject();
        if (wfTask.getType() == WorkflowConstants.CONDITION)
        {
            Vector arrows = wfTask.getOutgoingArrows();
            checkForDuplicateName(arrows, p_baseArrow);
        }
    }

    // check for duplicate name and resolve it.
    private void checkForDuplicateName(Vector p_arrows, BaseArrow p_baseArrow)
    {
        int OriginalSize = p_arrows == null ? 0 : p_arrows.size();
        int size = OriginalSize;
        while (size > 0)
        {
            WorkflowArrow arrow = (WorkflowArrow) p_arrows.get(size - 1);
            if (arrow.getName().equals(p_baseArrow.getObjectName()))
            {
                p_baseArrow.changeObjectName();
                size = OriginalSize;
            }
            else
            {
                size--;
            }
        }
    }

    // Print the graphical workflow workflow
    void printWorkflow()
    {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        // pops up the dialog for selecting the margins and orientation
        PageFormat pf = printerJob.pageDialog(printerJob.defaultPage());

        printerJob.setPrintable(this, pf);
        RepaintManager.currentManager(this).setDoubleBufferingEnabled(false);
        try
        {
            // For more printing options, we can display the printer
            // dialog (OS related) by the following if statement
            // if (printerJob.printDialog())

            // perform the print process
            printerJob.print();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        RepaintManager.currentManager(this).setDoubleBufferingEnabled(true);
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: Printable Implementation
    // ////////////////////////////////////////////////////////////////////
    // TomyD -- DO NOT CALL THIS METHOD DIRECTLY
    /**
     * Prints the page at the specified index into the specified Graphics
     * context in the specified format. A PrinterJob calls the Printable
     * interface to request that a page be rendered into the context specified
     * by graphics.
     */
    public int print(Graphics p_graphics, PageFormat p_pageFormat,
            int p_pageIndex)
    {
        int pageHeight = (int) p_pageFormat.getImageableHeight();
        int pageWidth = (int) p_pageFormat.getImageableWidth();
        int yCoordinate = (int) (p_pageIndex * pageHeight);
        int numOfPages = (getHeight() / pageHeight);

        if (p_pageIndex != 0 && p_pageIndex >= numOfPages)
        {
            return Printable.NO_SUCH_PAGE;
        }

        Graphics2D g2 = (Graphics2D) p_graphics;
        g2.translate(p_pageFormat.getImageableX(), p_pageFormat.getImageableY());
        g2.translate(0f, -(p_pageIndex * pageHeight));
        g2.setClip(0, yCoordinate, getWidth(), pageHeight);

        /* set the scale to make the content of the applet can fit in one page */
        float scale = getScale(pageWidth, pageHeight);
        g2.scale(scale, scale);

        paint(g2);

        // because the print() method may be called many times for the same
        // page,
        // it makes good sense to explicitly invoke the garbage collector in
        // this method.
        // Otherwise, we may run out of memory.
        System.gc();
        return Printable.PAGE_EXISTS;
    }

    /**
     * Gets the scale for the printer. The size of the applent is larger than
     * the printable page. To make the applet can printed in one page, we need
     * to resize the scale for the applet.
     * 
     * @param pageHeight
     * @param pageWidth
     * @return
     */
    private float getScale(int pageWidth, int pageHeight)
    {

        /*
         * In applet ,the real content only occupied the 50% percentage for the
         * whole content(left with whitespace) To trip the whitespace, we assume
         * the percentage is 75%. This value can be adjusted when there is some
         * wrong happened. The safted value is 100%.
         */
        float percentageX = 0.75f;
        float percentageY = 0.9f;

        float width = (float) pageWidth / ((float) getWidth() * percentageX);
        float heigth = (float) pageHeight / ((float) getHeight() * percentageY);

        /* get the minest value */
        if (pageHeight < pageWidth)
        {
            return heigth;
        }
        else
        {
            return width < heigth ? width : heigth;
        }

    }
    // ////////////////////////////////////////////////////////////////////
    // End: Printable Implementation
    // ////////////////////////////////////////////////////////////////////
}
