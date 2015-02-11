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

public interface UIConstants
{
    // UI Modes (the name "UI Modes" makes sense???)
    /**
     * Used for export target files.
     */
    public static final int UIMODE_EXPORT = 0;
    /**
     * Default in online editor.
     */
    public static final int UIMODE_EDITOR = 1;
    public static final int UIMODE_SNIPPETS = 2;
    public static final int UIMODE_PREVIEW_EDITOR = 3;
    /**
     * Activity is "Review (Editable)" type.
     */
    public static final int UIMODE_REVIEW = 4;
    /**
     * Activity is "Review Only" type.
     */
    public static final int UIMODE_REVIEW_READ_ONLY = 5;


    // View Modes
    public static final int VIEWMODE_PREVIEW = 1;
    public static final int VIEWMODE_TEXT = 2;
    public static final int VIEWMODE_LIST = 3;
    public static final int VIEWMODE_GXML = 4;

 
    // Edit Modes
    /**
     * Only "ICE" and "Exact Matched" locked, the others can be edited.
     */
    public static final int EDITMODE_DEFAULT = 1;
    /**
     * All segments can be edited, "unlock" all segments.
     */
    public static final int EDITMODE_EDIT_ALL = 2;
    /**
     * Read only.
     */
    public static final int EDITMODE_READ_ONLY = 3;


    public static final String COLOR_REPEATED = "#575757";
    public static final String COLOR_REPETITION = "#FF0000";
}
