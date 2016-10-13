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


package com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.util;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.WindowAdapter; 
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.JDialog;


public class ComponentManager extends WindowAdapter
{
    private Hashtable _components = new Hashtable ();

    /**
       @roseuid 372F67410217
     */
    public void setComponent(String key, Component component)
    {
        _components.put(key, component);
    }

    /**
       @roseuid 372F6741021A
     */
    public void displayModal(JDialog dialog)
    {  //to swing
        dialog.addWindowListener(this);
        dialog.setModal(true);
        dialog.setVisible(true);
    }

    /**
       @roseuid 372F6741021C
     */
    public void windowOpened(WindowEvent event)
    {
        setEnabled(false);
    }

    /**
       @roseuid 372F6741021E
     */
    public void windowClosed(WindowEvent event)
    {
        setEnabled(true);
        ((Window) event.getSource()).removeWindowListener(this);
    }

    /**
       @roseuid 372F67410222
     */
    private void setEnabled(boolean b)
    {
        Enumeration enumeration = _components.elements();
        while (enumeration.hasMoreElements())
        {
            try
            {
                Component component = (Component) enumeration.nextElement();
                component.setEnabled(b);
            }
            catch (Exception e)
            {

            }
        }
    }
}
