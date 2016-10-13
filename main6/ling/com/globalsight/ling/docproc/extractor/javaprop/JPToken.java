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
package com.globalsight.ling.docproc.extractor.javaprop;

/**
 * <p>Defines a Token class (and various type IDs) which are returned
 * from the parsers getNextToken() method.</p>
 *
 * Creation date: (7/14/2000 4:48:04 PM)
 * @author: Bill Brotherton
 */
public class JPToken
{
    // JavaProp token
    public static final int EMPTY                       = 0;
    public static final int UNKNOWN                     = 1;
    public static final int KEY_VALUE                   = 2;
    public static final int PROP_COMMENT                = 3;
    public static final int EOF                         = 4;
    public static final int SPACES                      = 5;
    public static final int PROP_KEY                    = 6;
    public static final int KEY_TERMINATOR              = 7;
    public static final int KEY_TERMINATOR_EMPTY_VALUE  = 8;

    public int m_nType = EMPTY;
    public String m_strContent = "";

    public int m_nInputLineNumber = 0;

    public JPToken()
    {
        super();
    }
}
