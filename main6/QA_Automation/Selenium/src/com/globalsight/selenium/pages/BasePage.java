/**
 *  Copyright 2009, 2011 Welocalize, Inc. 
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

package com.globalsight.selenium.pages;

/**
 * @author Vincent
 *
 */
public interface BasePage
{
    public static final String FIRST_PAGE_LINK = "link=1";
    public static final String NEXT_PAGE_LINK = "link=Next";

    public static final String NEW_BUTTON = "newBtn";
    public static final String EDIT_BUTTON = "editBtn";
    public static final String DONE_BUTTON = "doneBtn";
    public static final String SAVE_BUTTON = "Save";
    public static final String CANCEL_BUTTON = "Cancel";
    public static final String NEXT_BUTTON = "Next";
    public static final String OK_BUTTON = "OK";
    public static final String SEARCH_BUTTON = "Search";
    public static final String CLEAR_BUTTON = "Clear";
    public static final String NEW_VALUE_BUTTON = "//input[@value='New...']";
    public static final String EDIT_VALUE_BUTTON = "//input[@value='Edit...']";
    public static final String REMOVE_VALUE_BUTTON = "//input[@value='Remove']";
    public static final String SEARCH_VALUE_BUTTON = "//input[@value='Search...']";
    public static final String DONE_VALUE_BUTTON = "//input[@value='Done']";
    public static final String SAVE_VALUE_BUTTON = "//input[@value='Save']";
    public static final String CANCEL_VALUE_BUTTON = "//input[@value='Cancel']";
    public static final String DELETE_VALUE_BUTTON = "//input[@value='Delete']";
    public static final String SEARCH_SINGLE_VALUE_BUTTON = "//input[@value='Search']";

    public static final String CHECK_ALL = "checkAll";
}
