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
 
// iflow classes
import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.util.WindowUtil;
import com.globalsight.everest.webapp.applet.common.AppletHelper;
import com.globalsight.everest.webapp.applet.common.MessageCatalog;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowTask;
import com.globalsight.everest.workflow.WorkflowTaskInstance;

 
// java classes
import java.awt.Point;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
 
class UISubProcess extends BaseConnection {

    protected static final int  DEFAULT_POS_TRANSLATE = 15 ;

    private static final int  SUBPROC_NODE_WIDTH = 78 ;
    private static final int  SUBPROC_NODE_HEIGHT = 50 ;
    private static final int  SUBPROC_LBL_WIDTH = 66 ;
    
      // added label to show the name of the subprocess
    private JTextArea subProcName = new JTextArea();
    
	  JPopupMenu popupMenu;
    
    JMenuItem generalMenu;
    JMenuItem userDefMenu;
    JMenuItem scriptMenu;
    JMenuItem dataMapMenu;
    JMenuItem actionSetsMenu;
    
    MessageCatalog msgCat;

    public UISubProcess(Point p, GraphicalPane inGPane)    
    {        
      super(p, inGPane);  
      //setName("UISubProcess");
      //objectName = "Subprocess Name";
      objectName = "";
      msgCat = new MessageCatalog(inGPane.localeName); 

         // Note: if you change the xcut & ycut, you must change the 
         // "specialAngle" local variable in BaseArrow.
      int xcut = 15; 
      int xedge = SUBPROC_NODE_WIDTH - (2*xcut); 
      int ycut = 12; 
      int yedge = SUBPROC_NODE_HEIGHT-(2*ycut) ;

      int x0 = 0; 
      int x1 = xcut;
      int x2 = xedge + xcut ;
      int x3 = xedge + 2* xcut;
      int y0 = 0; 
      int y1 = ycut ;
      int y2 = ycut + yedge ;
      int y3 = 2*ycut + yedge ;

	    xPoints  = new int[8];
     	yPoints  = new int[8];

		  xPoints[0] = x0;  yPoints[0] = y1;
		  xPoints[1] = x0; 	yPoints[1] = y2;
		  xPoints[2] = x1; 	yPoints[2] = y3;
		  xPoints[3] = x2;  yPoints[3] = y3;
		  xPoints[4] = x3; 	yPoints[4] = y2;
		  xPoints[5] = x3; 	yPoints[5] = y1;
		  xPoints[6] = x2; 	yPoints[6] = y0;
		  xPoints[7] = x1; 	yPoints[7] = y0;
        //  offset it by half the width. But remember that BaseConnecter offsets it
        // by the old, smaller dimension that all other connecors are.        
      p.translate(DEFAULT_POS_TRANSLATE-(SUBPROC_NODE_WIDTH/2), 
                  DEFAULT_POS_TRANSLATE-(SUBPROC_NODE_HEIGHT/2)) ;
      shape = new GPolygon(xPoints, yPoints, p);
         // need to "override" the setting of the area and the center position (pos)
         // that are set in the BaseConnection for all small connection nodes 
      area = new Rectangle(p.x, p.y, SUBPROC_NODE_WIDTH, SUBPROC_NODE_HEIGHT);
           
      subProcName.setText(objectName);
      Font ft = new Font("SansSerif", Font.PLAIN, 10);        
      subProcName.setFont(ft);      
      subProcName.setLineWrap(true);
      subProcName.setWrapStyleWord(true);
      subProcName.setEditable(false) ;
      subProcName.addMouseListener(this) ;
      subProcName.addMouseMotionListener(this) ;
      subProcName.addKeyListener(this) ;

      subProcName.setSize (SUBPROC_LBL_WIDTH, (subProcName.getFontMetrics(ft).getHeight()*2));  
   
//      add(subProcName) ;
        
      add(makePopup());        
    }

    private JPopupMenu makePopup()
    {
        popupMenu = new JPopupMenu();
        
        generalMenu = new JMenuItem(AppletHelper.getI18nContent("lb_general"));
        generalMenu.addActionListener(this);
        popupMenu.add(generalMenu);

        /*userDefMenu = new JMenuItem(msgCat.getMsg("User-defined Attributes"));
        userDefMenu.addActionListener(this);
        popupMenu.add(userDefMenu);
                        
        scriptMenu = new JMenuItem(msgCat.getMsg("Scripting"));
        scriptMenu.addActionListener(this);
        popupMenu.add(scriptMenu);
        
        dataMapMenu = new JMenuItem(msgCat.getMsg("DataMapping"));
        dataMapMenu.addActionListener(this);
        popupMenu.add(dataMapMenu);
        
        actionSetsMenu = new JMenuItem(msgCat.getMsg("Action Set"));
        actionSetsMenu.addActionListener(this);
        popupMenu.add(actionSetsMenu);*/
                
        return popupMenu;
    }
    
    public void maybeShowPopup(MouseEvent e) 
    {
        if ( ((GVPane)gPane).isPopupEnabled() ){
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
            //((GVPane)gPane).savePopupVisible(popupMenu);
        }
    }
    
    UIObject getDummy() 
    {
        UIObject _obj = new UISubProcess (new Point(pos.x+9, pos.y-3), gPane);        
        return _obj;
    }

    public void actionPerformed(ActionEvent event) {	    
	    Object obj = event.getSource();	    
	    if(obj == generalMenu)
	    {	     
    	     invokeDlg(Constants.GENERALTAB);
	    }
	    /*else if(obj == userDefMenu){
      	     invokeDlg(Constants.USERDEFTAB);
	    }
	    else if(obj == scriptMenu){
      	     invokeDlg(Constants.SCRIPTTAB);
	    }
	    else if(obj == dataMapMenu){
      	     invokeDlg(Constants.DATAMAPTAB);
	    }
	    else if(obj == actionSetsMenu){
	        invokeDlg(Constants.PROACTIONSETSTAB);
	    } */
    }
    
    public void invokeDlg(int tabNumber)
    {
        /*JFrame _parentFrame = WindowUtil.getFrame(this); 
        //SubProcessPropertiesDlg sub = null;
        if ( modelObject instanceof WorkflowTaskInstance )
        {
            and = new AndNodeDlg(_parentFrame, (WorkflowTaskInstance)modelObject, tabNumber, gPane.getParentApplet());
            //sub = new SubProcessPropertiesDlg( _parentFrame, (WorkflowTaskInstance)modelObject, gPane, tabNumber);
        }
        else if ( modelObject instanceof WorkflowTask )
        {
            and = new AndNodeDlg(_parentFrame, (WorkflowTask)modelObject, tabNumber, gPane.getParentApplet());
            //sub = new SubProcessPropertiesDlg( _parentFrame, (WorkflowTask)modelObject, gPane, tabNumber);
        }
        WindowUtil.center(and);
        gPane.getParentApplet().displayModal(and);*/
        javax.swing.JOptionPane.showMessageDialog(
            null, "Sub-Process node is not implemented yet.");
    }


  public JTextArea getNameLabel() {
    return subProcName ;
  }

  public void setSubProcessName(String name) {
    subProcName.setText(name) ;
  }

  public void mouseDragged(MouseEvent event) 
  {
      if ( /*(!((GVPane)gPane).isAddingArrows()) &&*/
               (((GVPane)gPane).selObj == this) &&
           !((event.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK))              
      {          
          Point lblPt = subProcName.getLocation() ;
          MouseEvent ev2 = new MouseEvent(
              this, event.getID(), event.getWhen(), event.getModifiers(),
              event.getX()+lblPt.x, event.getY()+lblPt.y, 
              event.getClickCount(), event.isPopupTrigger());
            // they're dragging on the Subprocess name text 
            // area - just delegate it to the  gvPane
         gPane.mouseDragThis(ev2);
      }
  }
  public void mouseReleased(MouseEvent event) {
    if ( /*(((GVPane)gPane).isUsingPointer()) && */
            event.getSource() == subProcName &&
            (((GVPane)gPane).selObj == this) )              
       {
         Point lblPt = subProcName.getLocation() ;
         MouseEvent ev2 = new MouseEvent(gPane, event.getID(),
                      event.getWhen(), event.getModifiers(),
                      event.getX()+lblPt.x, event.getY()+lblPt.y,
                      event.getClickCount(), event.isPopupTrigger());
             // they're done dragging on the Subprocess name text area - just delegate 
             // it to the  gvPane
             gPane.mouseUpThis(ev2);
       }
   }
   
     // Mouse down on the Text area
   public void mousePressed(MouseEvent evt) 
   {
       if ((evt.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
       {
           maybeShowPopup(evt) ;
       }
        Point lblPt = subProcName.getLocation() ;
        if (((GVPane)gPane).selObj != this ) 
        {
            gPane.setSelObj(this) ;
            ((GVPane)gPane).selectNode(this, evt.getX()+lblPt.x, evt.getY()+lblPt.y) ;
            gPane.twfagp.fireSelectionEvent();                    
         }
   }
   
     /**
      * Pass along hte delete key pressed on the subProcName - delegate it to the 
      * gVPane
      * 
      */
  public void keyPressed(KeyEvent e) {  
      if (e.getKeyCode() == KeyEvent.VK_DELETE)
          ((GVPane)gPane).keyPressed(e);
  }  

   /**
    * Override the paint method to ensure that the label gets repainted in the correct location
    */
  	public void paintComponent(Graphics g) { 
      super.paintComponent(g);
      int lblHeight = subProcName.getFontMetrics(subProcName.getFont()).getHeight() ;
         // zzz magic number!
      subProcName.setLocation(pos.x+7, pos.y+(27)-(lblHeight));
      subProcName.setBackground(color) ;
    }

    /**
     * Override the default width and height - so that subporcesses & chained process
     * nodes are large enough to hold their label.
     */
  public int getNodeWidth() {
    return SUBPROC_NODE_WIDTH ;
  }
  public int getNodeHeight() {
    return SUBPROC_NODE_HEIGHT ;
  }
}
