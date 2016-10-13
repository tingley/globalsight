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

import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.api.GraphicalView;
import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.util.WindowUtil;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.URL;



/**
 * The applet class that implements the <b>Graphical View.  </b>The <b>Graphical
 * View </b>is where one can create or modify plan templates and participate
 * in processes.
 */
public class GVApplet extends TWFAGP implements GraphicalView, ActionListener
{
    protected JPanel toolBar;
    protected JPanel toolbarPnl;
    private FontMetrics fontMetrics;
    //private Resources resources;
    protected ImageButton buttonOR;
    protected ImageButton buttonAND;
    protected ImageButton buttonSub;
    //protected ImageButton buttonChained;
    //protected ImageButton buttonDelay;
    protected ImageButton buttonExit;
    protected ImageButton buttonActivity;
    protected ImageButton buttonCondition;
    protected ImageButton buttonActionArrow;
    protected ImageButton buttonPointer;
    // new buttons for save and cancel
    protected ImageButton buttonSave;
    protected ImageButton m_printButton;
    protected ImageButton buttonCancel;

    private boolean hasToolbar = false;
    /*  private int m_nPrevH =0;
      private int m_nPrevV =0;
      private BoundedRangeModel m_brmH;
      private BoundedRangeModel m_brmV; */

    /**
    * Constructs an instance of this class.
    */
    public GVApplet()
    {
    }
    
    public boolean getIsModified() {
        return ((GVPane)gPane).isModify;
    }
    
    public void saveWorkflow() {
        ((GVPane)gPane).saveWorkFlow();
    }

    /**
    * Initializes the applet, and sets up the display of the <b>Graphical
    * View. </b>This method sets up certain elements of the <b>Graphical View,
    * </b>such as the <code>JScrollPane </code>object and the <code>GraphicalPane
    * </code>object, as well as such attributes as the layout, and the background.
    */
    public  void init()
    {
        super.init ();


        if ( getGraphics() != null )
        {
            fontMetrics = getGraphics().getFontMetrics();
        }
        gPane = (GVPane)getMainPanel();
        // gPane = new GVPane (this);


        gPane.init(this);
        gPane.setFieldsEnabled(gPane.editMode);
        setBackground(Color.white);

        synchronized(lock)
        {
            try
            {
                sPane = new JScrollPane(gPane,
                                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
                sPane.setViewportView(gPane);
                getContentPane().add(sPane, BorderLayout.CENTER);
                sPane.getHorizontalScrollBar().setUnitIncrement(10);
                sPane.getVerticalScrollBar().setUnitIncrement(10);
                sPane.setPreferredSize (getSize());
                //AppletList.addApplet ("GPUI", this);
            }
            catch (Exception e)
            {

            }
        } // end synchronized

        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (Exception e)
        {

        }

        open();
        startEdit();
    }
    public void open()
    {

        gPane.setParentApplet(this);
    }

    /*protected ImageButton addImageButton(String image_name, int _type)
    {
        Image _image = resources.loadImage(image_name);
        ImageButton _button = new ImageButton(_image);
        _button.setType(_type);
        _button.setBackground(toolBar.getBackground());
        _button.addActionListener(this);
        toolBar.add(_button);
        return _button;
    }*/

    protected ImageButton addImageButton(Image _image, int _type)
    {
        ImageButton _button = new ImageButton(_image);
        _button.setType(_type);
        _button.setBackground(toolBar.getBackground());
        _button.addActionListener(this);
        toolBar.add(_button);
        return _button;
    }

    protected void initToolBar()
    {
        toolbarPnl = new JPanel();
        toolbarPnl.setLayout(new FlowLayout(FlowLayout.LEFT));
        // resources = new Resources(this);
        toolBar = new JPanel();
        
        //add image name from PageHandler
        buttonActivity    = addImageButton(((GVPane)gPane).gpactImage,     ImageButton.ACTIVITY_TYPE);
        buttonExit        = addImageButton(((GVPane)gPane).gpexitImage,    ImageButton.EXIT_TYPE);
        buttonCondition   = addImageButton(((GVPane)gPane).gpcondImage,    ImageButton.CONDITION_TYPE);
        if (((GVPane)gPane).m_areAndOrNodesVisible)
        {
            buttonOR = addImageButton(((GVPane)gPane).gporImage, ImageButton.OR_TYPE);
            buttonAND = addImageButton(((GVPane)gPane).gpandImage, ImageButton.AND_TYPE);
            // TomyD -- commented out since this node is not being implemented yet.
            //buttonSub = addImageButton(((GVPane)gPane).gpsubImage, ImageButton.SUB_TYPE);
        }
        buttonActionArrow = addImageButton(((GVPane)gPane).gparrowImage,   ImageButton.ARROW_TYPE);
        buttonPointer     = addImageButton(((GVPane)gPane).gppointerImage, ImageButton.POINTER_TYPE); 
        //buttonChained     = addImageButton("gpchained.gif", ImageButton.CHAINED_TYPE);
        //buttonDelay       = addImageButton("gpdelay.gif",   ImageButton.DELAY_TYPE);
        buttonSave        = createImageButton(((GVPane)gPane).gpsaveImage, ImageButton.SAVE_TYPE);
        m_printButton     = createImageButton(((GVPane)gPane).m_printImage, ImageButton.PRINT_TYPE);

        toolBar.setBounds (0, 0, 800, 40);
        toolbarPnl.add(toolBar);
        toolbarPnl.add(buttonSave);
        toolbarPnl.add(m_printButton);
    }

    // Create Image button without adding it to the default toolbar
    protected ImageButton createImageButton(Image _image, int _type)
    {
        ImageButton _button = new ImageButton(_image);
        _button.setType(_type);
        _button.setBackground(toolBar.getBackground());
        _button.addActionListener(this);

        return _button;
    }

    
    /**
    * Calls {@link com.Fujitsu.iflow.model.Plan#startEdit} and displays the
    * template editing toolbar, placing the template in edit mode. Once in edit
    * mode, the server locks the template so no other user can make changes to
    * it while an edit session is running. Only non-published templates can be
    * edited, these are templates that have not had processes created from
    * them. If a template is already published, however, you can copy the existing
    * template into a new template. This new template can then be edited.
    *
    * @see #commitEdit
    * @see #cancelEdit
    */
    //public void startEdit() throws WFInternalException,WFServerException,ModelInternalException {
    public void startEdit()
    {
        boolean areNodes = false;
        try
        {

            if (gPane == null)
            {

                return;
            }



            if ( gPane.model != null )
            {


                Identity();


                //gPane.model.startEdit();
                if ( (gPane.model.getWorkflowTasks()).size() == 0 )
                {



                    //add a start node if it is an empty plan
                    ((GVPane)gPane).createStartNode(50, 50);
                }
                else
                {
                    // ((GVPane)gPane).setView();

                    areNodes= true;
                }

            }
            else if ( gPane.modelPI != null )
            {


                gPane.drawEventPendingArrows(true);//show these arrows in different color

                areNodes= true;

            }


            gPane.editMode = ((GVPane)gPane).m_isEditMode;//true;
            ((GVPane)gPane).setNodeType(GVPane.NO_OP_TYPE);
            setToolbarVisible(gPane.editMode);
            resetButtons(GVPane.NO_OP_TYPE);
            gPane.setFieldsEnabled(gPane.editMode);
            setPopupEnabled(gPane.editMode);

            validate();
            repaint();
            if ( areNodes )
                ((GVPane)gPane).setView();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public void cancelEdit() throws Exception
    {   //Not using now
    }

    public void checkToolbar()
    {
        if (gPane.editMode == true)
        {
            // just like template cancelEdit but without the
            // model operations cuz the model already has cleaned up
            gPane.editMode = false;
            ((GVPane)gPane).setNodeType(GVPane.NO_OP_TYPE);
            resetButtons(GVPane.NO_OP_TYPE);
            gPane.setFieldsEnabled(gPane.editMode);
            setToolbarVisible(gPane.editMode);
            scrollGVToOriginal();
            validate();
            repaint();
        }
    }

    public void commitEdit() throws Exception {
    }

    /**
    * Repaints the <b>Graphical View </b>from information on the server. This
    * method calls <code>java.awt.Container.invalidate </code>and <code>
    * java.awt.Container.validate </code>before repainting.
    *
    * @see "<code>java.awt.Container</code>"
    */
    public void updateView()
    {
        gPane.invalidate();
        validate();
        repaint();
    }

    /**
    * Activates or deactivates a node, thereby putting it into either the running
    * or completed state.
    *
    * @param new_state Either <code>NODESTATE_RUNNING </code>or <code>
    * NODESTATE_COMPLETED.</code>
    * @exception ModelException
    */
    public void setSelectedNodeState(int new_state) throws Exception
    {
        ((GVPane)gPane).setSelectedNodeState(new_state);
    }

    public void ZoomIn()
    {
        if (gPane == null)
        {
            //Log.println(Log.LEVEL1, this + ".ZoomIn called while gPane is null.");
            return;
        }
        ((GVPane)gPane).setZoomIn();
    }

    public void ZoomOut()
    {
        if (gPane == null)
        {
            //Log.println(Log.LEVEL1, this + ".ZoomOut called while gPane is null.");
            return;
        }
        ((GVPane)gPane).setZoomOut();
    }

    /*  public void ZoomBox(){  //HF
          if (gPane == null)
          {
              Log.println(Log.LEVEL1, this + ".ZoomBox called while gPane is null.");
              return;
          }
          ((GVPane)gPane).setZoomBox();
      } */

    public void Identity()
    {
        if (gPane == null)
        {

            return;
        }
        ((GVPane)gPane).setIdentity();
        scrollGVToOriginal();
    }

    /**
    * This method handles mouse click events over the <code>GraphicalView
    * </code>applet. This method identifies the clicked button, sets it in a down
    * position, and sets all other buttons in an up position. It then sets the
    * type of node for the cursor, based on the button that was clicked.
    */
    public synchronized  void actionPerformed(ActionEvent event)
    {
        // Safety check.
        if ( gPane == null )
        {
            return;
        }

        Object eventSource = event.getSource();
        String _command = event.getActionCommand();

        if ( !_command.equals(ImageButton.BUTTON_DOWN_ACTION) )
        {
            return;
        }

        if (eventSource == buttonPointer)
        {
            resetButtons(ImageButton.NO_OP_TYPE);
            ((GVPane)gPane).setNodeType(GVPane.NO_OP_TYPE);
            if ( ((GVPane)gPane).drawLines != null )
            {
                ((GVPane)gPane).drawLines.removeAllElements();
                ((GVPane)gPane).repaint();
            }
//            setColorLegendVisible(true);
        }

        if (eventSource == buttonActivity)
        {
            resetButtons(ImageButton.ACTIVITY_TYPE);
            ((GVPane)gPane).setNodeType(GVPane.ACTIVITY_TYPE);
//            setColorLegendVisible(false);
        }
        else if (eventSource == buttonExit)
        {
            resetButtons(ImageButton.EXIT_TYPE);
            ((GVPane)gPane).setNodeType(GVPane.EXIT_TYPE);
        }
        else if (eventSource == buttonAND)
        {
            resetButtons(ImageButton.AND_TYPE);
            ((GVPane)gPane).setNodeType(GVPane.AND_TYPE);
        }
        else if (eventSource == buttonSub)
        {
             resetButtons(ImageButton.SUB_TYPE);
             ((GVPane)gPane).setNodeType(GVPane.SUB_TYPE);
        }
        else if (eventSource == buttonOR)
        {
            resetButtons(ImageButton.OR_TYPE);
            ((GVPane)gPane).setNodeType(GVPane.OR_TYPE);
        }
        else if (eventSource == buttonCondition)
        {
            resetButtons(ImageButton.CONDITION_TYPE);
            ((GVPane)gPane).setNodeType(GVPane.CONDITION_TYPE);
        }
        
        /* else if (eventSource == buttonChained)
        {
            resetButtons(ImageButton.CHAINED_TYPE);
            ((GVPane)gPane).setNodeType(GVPane.CHAINED_TYPE);
        }
         
         else if (eventSource == buttonDelay)
         {
             resetButtons(ImageButton.DELAY_TYPE);
             ((GVPane)gPane).setNodeType(GVPane.DELAY_TYPE);
         } */
        else if (eventSource == buttonActionArrow)
        {
            resetButtons(ImageButton.ARROW_TYPE);
            ((GVPane)gPane).setNodeType(GVPane.ARROW_TYPE);
        }
        else if (eventSource == buttonPointer)
        {
            resetButtons(ImageButton.POINTER_TYPE);
            ((GVPane)gPane).setNodeType(GVPane.POINTER_TYPE);
        }
        // for save 
        else if (eventSource == buttonSave)
        {
            // resetButtons(ImageButton.SAVE_TYPE);
            //((GVPane)gPane).setNodeType(GVPane.POINTER_TYPE);
            ((GVPane)gPane).hasExitNode =false;

            if (((GVPane)gPane).saveWorkFlow())
            {
                buttonSave.setButtonDown(false);
                callNewPage();
            }
            else
            {
                buttonSave.setButtonDown(false);
            }
        }
        else if (eventSource == m_printButton)
        {
            m_printButton.setButtonDown(false);
            ((GVPane)gPane).printWorkflow();
        }
    }

    
    void resetButtons(int current_type)
    {
        if (!gPane.editMode)
        {
            return;
        }

        if ( (buttonActivity.getType() != current_type) &&
             (buttonActivity.isButtonDown()) )
        {
            buttonActivity.setButtonDown(false);
        }
        else if ( (buttonActionArrow.getType() != current_type) &&
                  (buttonActionArrow.isButtonDown()) )
        {
            buttonActionArrow.setButtonDown(false);
        }
        else if ( (buttonAND != null && 
                   buttonAND.getType() != current_type) &&
                  (buttonAND.isButtonDown()) )
        {
            buttonAND.setButtonDown(false);
        }
        else if ( (buttonOR != null && 
                   buttonOR.getType() != current_type) &&
                  (buttonOR.isButtonDown()) )
        {
            buttonOR.setButtonDown(false);
        }
        else if ( (buttonSub != null && 
                   buttonSub.getType() != current_type) &&
                  (buttonSub.isButtonDown()) )
        {
            buttonSub.setButtonDown(false);
        }
        else if ( (buttonCondition.getType() != current_type) &&
                  (buttonCondition.isButtonDown()) )
        {
            buttonCondition.setButtonDown(false);
        }
        /* else if ( (buttonSub.getType() != current_type) &&
                   (buttonSub.isButtonDown()) )
         {
             buttonSub.setButtonDown(false);
         }
         else if ( (buttonChained.getType() != current_type) &&
                   (buttonChained.isButtonDown()) )
         {
             buttonChained.setButtonDown(false);
         }
         else if ( (buttonDelay.getType() != current_type) &&
                   (buttonDelay.isButtonDown()) )
         {
             buttonDelay.setButtonDown(false);
         }*/
        else if ( (buttonExit.getType() != current_type) &&
                  (buttonExit.isButtonDown()) )
        {
            buttonExit.setButtonDown(false);
        }
        else if ( (buttonPointer.getType() != current_type) &&
                  (buttonPointer.isButtonDown()) )
        {
            buttonPointer.setButtonDown(false);
        }
        else if ( (buttonPointer.getType() == current_type
                   || current_type == GVPane.NO_OP_TYPE)
                  && ! (buttonPointer.isButtonDown()) )
        {
            buttonPointer.setButtonDown(true);
        }
        // FOR SAVE
        /* else if ( (buttonSave.getType() != current_type) &&
                  (buttonSave.isButtonDown()) )
        {
            buttonSave.setButtonDown(false);
        }  */


    }

    void setToolbarVisible(boolean new_state)
    {
        if (!gPane.editMode)
        {
            return;
        }

        if ( !hasToolbar )
        {
            //since loading Images may fail at init(), it's handled here
            initToolBar();
            hasToolbar = true;
        }

        if ( new_state )
        {

            getContentPane().add(toolbarPnl, BorderLayout.NORTH);
        }
        else
        {

            remove(toolbarPnl);
        }
    }

    public void setFontMetrics(FontMetrics fontMetricsParam)
    {
        fontMetrics = fontMetricsParam;
    }

    /**
     *   author: Hamid
     *   These methods are added so that we can disable the popup menu
     *   of the nodes when viewing an archived template.
     */
    public boolean isPopupEnabled()
    {
        if ( gPane == null ) return true;
        else return((GVPane)gPane).isPopupEnabled();
    }

    public void setPopupEnabled(boolean flag)
    {
        if ( gPane == null ) return;
        else ((GVPane)gPane).setPopupEnabled(flag);
    }
}
