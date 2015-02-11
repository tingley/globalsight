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

package com.globalsight.everest.edit.online;

import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;

/**
 * Extends RenderingOptions that places it within the Editing package.
 */
public class RenderingOptions
    extends com.globalsight.everest.page.RenderingOptions
{
    private TranslationMemoryProfile tmProfile = null;
    private String userName;
    private boolean needShowPTags = false;

    /**
     * Default constructor
     */
    public RenderingOptions()
    {
        super();
    }

    /**
     * Constructor where the user's perms don't matter.
     */
    public RenderingOptions(int p_uiMode, int p_viewMode, int p_editMode)
    {
        super(p_uiMode, p_viewMode, p_editMode, null);
    }

    /**
     * Constructor that sets up the correct ui mode, view mode, edit mode
     * and sets up the other attributes according to the permissions.
     */
    public RenderingOptions(int p_uiMode, int p_viewMode, int p_editMode,
                            PermissionSet p_permSet)
    {
        super(p_uiMode, p_viewMode, p_editMode, p_permSet);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setTmProfile(TranslationMemoryProfile tmProfile) {
        this.tmProfile = tmProfile;
    }

    public TranslationMemoryProfile getTmProfile(){
        return this.tmProfile;
    }
    
    public boolean getNeedShowPTags()
    {
        return needShowPTags;
    }

    public void setNeedShowPTags(boolean needShowPTags)
    {
        this.needShowPTags = needShowPTags;
    }
}





