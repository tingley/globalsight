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
package com.globalsight.ling.docproc.extractor.cfscript;

public interface IParseEvents
{
    /** C++-style comment like "// ... &lt;eol&gt;" */
    static final int SINGLE_LINE_COMMENT = 1;
    /** C-style comment like "/* ... * /" */
    static final int MULTI_LINE_COMMENT = 2;
    /**
     * C++-style comment like "// ... --&gt" with a trailing SGML
     * closing comment token and without eol
     */
    static final int CRIPPLED_SINGLE_LINE_COMMENT = 3;

    public void handleStart();
    public void handleFinish();

    public void handleWhite(String s);
    public void handleComment(String s, int commentType);
    public void handleEndOfLine(String s);
    public void handleCDO(String s);
    public void handleCDC(String s);

    public void handleLiteral(String s);
    public void handleString(String s);
    public void handleKeyword(String s);
    public void handleOperator(String s);
}
