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
package com.globalsight.ling.docproc.extractor.vbscript;

/**
 * <p>Defines the token type for the VBScript parser.</p>
 */
public class VBToken
{
    // JavaScript token (string extraction oriented view)
    protected static final int EMPTY                    = 0;
    protected static final int NEWLINE                  = 1;
    protected static final int STRING                   = 2;
    protected static final int COMMENT                  = 3;
    protected static final int CONCAT_OP                = 4;
    protected static final int ASSIGN                   = 5;
    protected static final int OPEN_PARENTHESIS         = 6;
    protected static final int CLOSE_PARENTHESIS        = 7;
    protected static final int OPEN_SQBRAQUET           = 8;
    protected static final int CLOSE_SQBRAQUET          = 9;
    protected static final int EOF                      = 10;
    protected static final int RETURN                   = 11;
    protected static final int MISC_KEYWORD             = 12;
    protected static final int MISC_OPERATOR            = 13;
    protected static final int SPACES                   = 14;
    protected static final int UNKNOWN                  = 15;

    public int      m_nType = EMPTY;
    public String   m_strContent = "";

    public VBToken()
    {
        super();
    }
}
