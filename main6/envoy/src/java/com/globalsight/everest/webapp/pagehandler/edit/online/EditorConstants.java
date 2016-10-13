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

package com.globalsight.everest.webapp.pagehandler.edit.online;

import com.globalsight.everest.edit.online.UIConstants;

public interface EditorConstants
{
    // View modes for source & target page
    public static final int VIEWMODE_PREVIEW = UIConstants.VIEWMODE_PREVIEW;
    public static final int VIEWMODE_TEXT = UIConstants.VIEWMODE_TEXT;
    public static final int VIEWMODE_DETAIL = UIConstants.VIEWMODE_LIST;
    public static final int VIEWMODE_DYNAMIC_PREVIEW = 4;

    // Number of view modes - DYNAMIC_PREVIEW isn't really a mode.
    public static final int VIEWMODE_MAX = 3;

    // how many pages to show
    public static final int SINGLE_PAGE = 1;
    public static final int DUAL_PAGE = 2;

    // when showing one page, is it source or target
    public static final int SINGLE_PAGE_IS_SOURCE = 1;
    public static final int SINGLE_PAGE_IS_TARGET = 2;

    // split state when showing multiple pages
    public static final int SPLIT_HORIZONTALLY = 1;
    public static final int SPLIT_VERTICALLY = 2;

    // read-only flag for target page
    public static final int READ_WRITE = 1;
    public static final int READ_ONLY = 2;

    /** Flag to switch on "edit all" (cannot be undone) */
    public static final int EDIT_DEFAULT = 1;
    public static final int EDIT_ALL = 2;

    // The type of editor to show (standard segment editor, image
    // editor, etc)
    public static final int SE_SEGMENTEDITOR = 0;
    public static final int SE_IMAGEEDITOR = 1;
    public static final int SE_BIDIEDITOR = 2;
    public static final int SE_TEXTEDITOR = 3;

    // Flag that specifies editor mode
    public static final int EDITORMODE_VIEWER = 0;
    public static final int EDITORMODE_EDITOR = 1;
    public static final int EDITORMODE_REVIEW = 2;

    // Flag for ptag display
    public static final String PTAGS_COMPACT = "compact";
    public static final String PTAGS_VERBOSE = "verbose";
}
