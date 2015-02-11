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
package galign.helpers.ui;

import javax.swing.Action;
import javax.swing.JMenuItem;

/**
 * Helper class to manage Swing's Action elements.
 *
 * Action objects are meant to be shared between menu items and
 * toolbar buttons so that both initialize their state (label, icon,
 * disabled status etc) from one single object, making it easier for
 * the application to control the UI.
 *
 * Actions are, however, not well integrated into UI designers like
 * NetBeans which put Actions into the UI classes. A good design
 * encourages managing Actions (as well as the entire *behavior* of
 * the UI) in separate controller classes. For this reason it was
 * decided to move Actions to the controller, and initialize menu
 * items using this class.
 *
 * @see http://www.onjava.com/pub/a/onjava/2004/03/10/blackmamba.html
 */
public class ActionPropsSetter
{
    static public void setActionProps(Action action, JMenuItem menuItem)
    {
        action.putValue(Action.NAME, menuItem.getText());
        action.putValue(Action.MNEMONIC_KEY, new Integer(menuItem.getMnemonic()));
        action.putValue(Action.ACCELERATOR_KEY, menuItem.getAccelerator());
        action.setEnabled(menuItem.isEnabled());
    }
}
