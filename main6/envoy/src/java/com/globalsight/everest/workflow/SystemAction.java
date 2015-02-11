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
package com.globalsight.everest.workflow;

import java.io.Serializable;

/**
 * This class only represent a system action that represents an action type and
 * an action display name. The action type is the value stored in iFlow database
 * and the display name is used for displaying the action based on the user
 * locale. Note that the action display name should be set based on on a
 * resource bundle.
 */
public class SystemAction implements Serializable
{

    private static final long serialVersionUID = 1063109214967689765L;
    // ////////////////////////////////////////
    // System Actions (resrouce bundle keys)
    // ////////////////////////////////////////
    /**
     * No action taken when a task is activated.
     */
    public static String NO_ACTION = "lb_no_action";
    /**
     * Create a secondary target file when a task is activated.
     */
    public static String CSTF = "lb_cstf";
    /**
     * Create or replace a secondary target file when a task is activated.
     */
    public static String RSTF = "lb_rstf";

    public static String ETF = "lb_etf";

    private String m_actionType = null;
    private String m_actionDisplayName = null;

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////
    /**
     * SystemAction constructor used for setting the initial values.
     * 
     * @param p_actionType
     *            - The system action type (key of l10n property file.
     * @param p_actionDisplayName
     *            - The displayable action name (value of l10n property file.
     */
    public SystemAction(String p_actionType, String p_actionDisplayName)
    {
        m_actionType = p_actionType;
        m_actionDisplayName = p_actionDisplayName;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Helper Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Get the type of this particular system action. Note that the action type
     * is basically the key to the localization property files.
     * 
     * @return The type of this system action.
     */
    public String getType()
    {
        return m_actionType;
    }

    /**
     * Get the display name of this particular system action. The action name is
     * basically the value read from a localization property files based on the
     * type of this system action.
     * 
     * @return The display name of this system action.
     */
    public String getDisplayName()
    {
        return m_actionDisplayName;
    }
    // ////////////////////////////////////////////////////////////////////
    // End: Helper Methods
    // ////////////////////////////////////////////////////////////////////
}
