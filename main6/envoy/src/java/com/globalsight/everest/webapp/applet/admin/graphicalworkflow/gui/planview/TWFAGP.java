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

import javax.swing.JDialog;
import javax.swing.BoundedRangeModel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;

import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.util.WindowUtil;
import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.api.WFApp;
import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.util.ComponentManager;
import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.api.SelectionSource;
import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.api.SelectionListener;
import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.api.SelectionEvent;
import com.globalsight.everest.webapp.applet.common.EnvoyJApplet;

/**
 * This abstract class provides the framework for {@link TNApplet} and {@link
 * GVApplet}. It provides the basic methods that listen for plan changes, process
 * instance changes and selection changes, as well as notifying other applets
 * of these changes. <p>
 * Whenever something is selected something in the <b>Organizer, </b>this abstract
 * class helps keep the <b>Thumbnail View </b>and <b>Graphical View </b>(as
 * displayed in the<b> Target Area</b>) coordinated.
 */


// TWFAGP will extends EnvoyJApplet instead of JApplet


public abstract class TWFAGP extends EnvoyJApplet implements  SelectionSource,WFApp

{
    private  Vector selectionListeners = new Vector();
    //private static AvailabilityNotifier _notifier = new AvailabilityNotifier();
    public static Object lock = new Object();
    private  ColorLegendDlg colorLegendDlg;
    public int m_nPrevH =0;
    public int m_nPrevV =0;
    public BoundedRangeModel m_brmH;
    public BoundedRangeModel m_brmV;

    private ComponentManager _compMgr = new ComponentManager ();
    //also used in sub-classes
    GraphicalPane gPane;

    //protected JScrollPane sPane = new JScrollPane (gPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
    protected JScrollPane sPane;

    public void scrollGVToOriginal()
    {
        if ( sPane != null )
        {
            JScrollBar sbH = sPane.getHorizontalScrollBar();
            JScrollBar sbV = sPane.getVerticalScrollBar();

            m_brmH = sbH.getModel();
            m_brmV = sbV.getModel();
        }

        if (m_brmH != null && m_brmV != null)
        {
            m_brmH.setValue(m_nPrevH);
            m_brmV.setValue(m_nPrevV);
        }
    }

    void setScrollPosition(Point new_pos)
    {
        /*  Point cur_pos = sPane.getScrollPosition();
            
            if ( (new_pos.x < cur_pos.x) ||
                 (new_pos.y < cur_pos.y) )
            {
               sPane.setScrollPosition(new_pos.x + (int)(sPane.getSize().width/2), new_pos.y + (int)(sPane.getSize().height/2));
            }
    
            if ( new_pos.x > cur_pos.x + sPane.getSize().width )
            {
                //need to scroll right
                sPane.setScrollPosition(1000, 1000);
                System.out.println("TWGAGP.setScrollPosition(): setting x to " + (new_pos.x - sPane.getSize().width/2));
                sPane.setScrollPosition(new_pos.x - sPane.getSize().width/2, new_pos.y - sPane.getSize().height/2);
                System.out.println("TWGAGP.setScrollPosition(): sPane.getScrollPosition() = " + sPane.getScrollPosition());
                if ( sPane.getScrollPosition().x < new_pos.x - sPane.getSize().width/2 )
                {
                    sPane.setScrollPosition(new_pos.x - sPane.getSize().width/2, new_pos.y - sPane.getSize().height/2);
                    System.out.println("TWGAGP.setScrollPosition(): sPane.getScrollPosition() = " + sPane.getScrollPosition());
                }
            }
    
            if ( new_pos.y > cur_pos.y + sPane.getSize().height )
            {
                //need to scroll down
                System.out.println("TWGAGP.setScrollPosition(): setting y to " + (new_pos.y - sPane.getSize().height/2));
                sPane.setScrollPosition(sPane.getScrollPosition().x, new_pos.y - sPane.getSize().height/2);
                System.out.println("TWGAGP.setScrollPosition(): sPane.getScrollPosition().y = " + sPane.getScrollPosition().y);
            }
        */
    }




    //method of WFApp
    public void displayModal(JDialog dialog)
    {
        _compMgr.displayModal(dialog);
    }




    /*public void open() {
        gPane.setParentApplet(this);
    }*/
    /**
    * Closes the parent applet that this applet listens to.
    */
    public void close()
    {
        //no op
    }

    /**
    * Initilizes <code>java.awt.locale.</code> Initializes a new <code>MessageCatalog
    * </code>object.
    */
    public void init()
    {
        super.init();        
    }

    /**
    * Notifies other components that this applet has become unavailable.
    */
    public void stop()
    {
        //TWFAGP._notifier.notifyUnavailability(this);
    }

    /**
    * Notifies other components that this applet is now available.
    */
    public void start()
    {
        //TWFAGP._notifier.notifyAvailability(this);
    }

    /**
    * Sets the <code>ColorLegendDlg </code>object to be visible.
    *
    * @param _visible
    */
    public void setColorLegendVisible(boolean _visible)
    {
        if ( _visible )
        {
            if ( (colorLegendDlg!= null) && !colorLegendDlg.isVisible() )
            {
                colorLegendDlg.setVisible(_visible);
            }
            else if ( (colorLegendDlg!= null) && colorLegendDlg.isVisible() )
            {
                //no op
                return;
            }
            else
            {
                colorLegendDlg = new ColorLegendDlg(WindowUtil.getFrame(this));
                WindowUtil.center(colorLegendDlg);
                colorLegendDlg.show();
            }
        }
        else if ( !_visible && (colorLegendDlg != null) )
        {
            colorLegendDlg.setVisible(_visible);
        }
    }


    /**
    * Returns a list of <code>WFObjects </code>that are selected. Currently only
    * single selection is supported. This implies that only the first element
    * of the array is valid and used. This method returns null if the applet's
    * <code>GraphicalPane </code>is null.
    *
    * @return an <code>Object </code>that contains <code>WFObjects </code>that have been
    * selected by this applet
    * @see #setSelection
    */
    public Object[] getSelection()
    {
        if (gPane == null)
        {
            // Log.println(Log.LEVEL1, this+".getSelection called while gPane is null.");
            return null;
        }
        return gPane.getSelection();
    }

    /**
    * Sets the selection of nodes and arrows . This method is used to synchronize
    * object selections among viewing canvases. Currently only a single selection
    * is supported. This implies that only the first element of the array is valid
    * and used.
    *
    * @param _objList an <code>Object </code>that contains <code>WFObjects
    * </code>that have been selected
    * @see #getSelection
    */
    public synchronized void setSelection(Object[] _objList)
    {
        if (gPane == null)
        {
            return;
        }
        gPane.setSelection (_objList);
    }

    /**
    * Fires a <code>SelectionEvent </code>object to let <code>ClientApplet
    * </code>know that the selection has been changed. This method iterates through
    * the list of selection listeners and fires the <code>SelectionListener.selectionChanged
    * </code>method for each.
    */
    protected void fireSelectionEvent()
    {
        for (int i = 0; i < selectionListeners.size(); i++)
        {
            ((SelectionListener)selectionListeners.elementAt(i)).
                    selectionChanged(new SelectionEvent (this, SelectionEvent.TYPE_ACTIVITY));
        }
    }

    /**
   * Adds a listener to the selection listeners' list. The <code>SelectionListener
   * </code>interface is implemented by any by any class that is interested in
   * receiving selection events from a <code>SelectionSource </code>object when
   * the selection is changed. <code>SelectionListeners </code>is a list which
   * contain a set of selection listeners.
   *
   * @param sListener the <code>SelectionListener </code>object that this
   * applet adds to the list of selection listeners.
   * @see #removeSelectionListener
   * @see #addListener
   */
    public void addSelectionListener(SelectionListener sListener)
    {
        selectionListeners.addElement (sListener);
    }

    /**
    * Removes <code>sListener </code>from the selection listeners' list.
    *
    * @param sListener the <code>SelectionListener </code>object to be removed
    * from the list of selection listeners.
    * @see #addSelectionListener
    * @see #removeListener
    */
    public void removeSelectionListener(SelectionListener sListener)
    {
        selectionListeners.removeElement (sListener);
    }


    /**
    * Used by the i-Flow StandAlone Development Manager to resize the <code>ScrollPane
    * </code>object. This method is not called by the i-Flow browser-based Development
    * Clients.
    *
    * @param parentBounds
    */
    public void setBounds(Rectangle parentBounds)
    {
        super.setBounds(parentBounds);
        if ( sPane != null )
            sPane.setPreferredSize (new Dimension(parentBounds.width, parentBounds.height));
    }
}
