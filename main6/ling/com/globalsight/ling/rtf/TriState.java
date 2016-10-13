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
//
// Copyright (c) 2003 GlobalSight Corporation. All rights reserved.
//
// THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
// GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
// IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
// OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
// AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
//
// THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
// SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
// UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
// BY LAW.
//
package com.globalsight.ling.rtf;

/**
 * <p>Base class for data objects that have 3 states: not initialized
 * (meaning they inherit the value from someplace else), initialized
 * to the default value, or initialized to a specific value.</p>
 *
 * <p>Once initialized, an instance of this class can be switched back
 * to the un-initialized state.</p>
 */
public abstract class TriState
{
    static public boolean ISSET = true;
    static public boolean ISNOTSET = false;

    private boolean m_isSet = ISNOTSET;

    public boolean isSet()
    {
        return m_isSet == ISSET;
    }

    public boolean isNotSet()
    {
        return m_isSet == ISNOTSET;
    }

    public void setValueSet()
    {
        m_isSet = ISSET;
    }

    public void setValueNotSet()
    {
        m_isSet = ISNOTSET;
    }

    // Due to Java constraints we cannot predefine the methods
    // getValue() and setValue() as abstract.
}
