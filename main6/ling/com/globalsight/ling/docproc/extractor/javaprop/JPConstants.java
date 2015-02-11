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

public interface JPConstants
{
    /** Encoded TMX formatting cannot be erased. */
    static final int ALL_TMX_NON_ERASABLE     =   0;
    /** Encoded TMX formatting can be erased. */
    static final int ALL_TMX_ERASABLE         =   1;
    /** Only leading TMX formatting can be erased. */
    static final int LEADING_TMX_NON_ERASABLE =   3;
    
    /**  Indicates standard property processing */
    static final int JAVAPROP_STANDARD  =   4;
    /**  Indicates HTML property processing */
    static final int JAVAPROP_HTML  =   5;
    /**  Indicates standard property and message format processing*/
    static final int JAVAPROP_MSGFORMAT  =   6;
}
