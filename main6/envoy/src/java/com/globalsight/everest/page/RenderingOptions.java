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

package com.globalsight.everest.page;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.online.UIConstants;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.permission.Permission;
import java.util.Vector;
import java.util.Iterator;

/**
 * Specifies the various options for rendering a page.
 * Specifies modes and the PermissionSet group that the user has.
 */
public class RenderingOptions
{
    // private variables

    // --- see com.globalsight.everest.edit.online.UIConstants for the various modes
    private int m_uiMode = 0;   //
    private int m_viewMode = 0; // Preview, Text, Detail,
    private int m_editMode = 0; // Read only or R/W


    private boolean m_addSnippets = false;
    private boolean m_editSnippets = false; //this is everything else but add

    private static final Logger s_logger =
        Logger.getLogger(
            RenderingOptions.class);

    /**
     * Default constructor
     */
    public RenderingOptions()
    {
        //all the private variables are set to their default
    }

    /**
     * Constructor where the user's access group doesn't matter.
     */
    public RenderingOptions(int p_uiMode, int p_viewMode, int p_editMode)
    {
        this(p_uiMode, p_viewMode, p_editMode, null);
    }

    /**
     * Constructor that sets up the correct ui mode, view mode, edit mode
     * and sets up the other attributes according to the access group.
     */
    public RenderingOptions(int p_uiMode, int p_viewMode, int p_editMode,
                            PermissionSet p_permSet)
    {
        m_uiMode = p_uiMode;
        m_viewMode = p_viewMode;
        m_editMode = p_editMode;

        if (p_permSet !=null)
        {
            if (p_permSet.getPermissionFor(Permission.SNIPPET_ADD)==true)
                m_addSnippets = true;
            if (p_permSet.getPermissionFor(Permission.SNIPPET_EDIT)==true)
                m_editSnippets = true;
        }
    }

    public boolean canEditSnippets()
    {
        return m_editSnippets;
    }

    public boolean canAddSnippets()
    {
        return m_addSnippets;
    }

    public int getViewMode()
    {
        return m_viewMode;
    }

    public void setViewMode(int p_mode)
    {
        m_viewMode = p_mode;
    }

    public int getUiMode()
    {
        return m_uiMode;
    }

    public void setUiMode(int p_mode)
    {
        m_uiMode = p_mode;
    }

    public int getEditMode()
    {
        return m_editMode;
    }

    public void setEditMode(int p_mode)
    {
        m_editMode = p_mode;
    }

    public String toString()
    {
        StringBuffer result = new StringBuffer();

        result.append("RenderingOptions - UiMode: ");
        result.append(getUiMode());
        result.append(" ViewMode: ");
        result.append(getViewMode());
        result.append(" EditMode: ");
        result.append(getEditMode());
        return result.toString();
    }
}






