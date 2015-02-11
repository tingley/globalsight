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
package com.globalsight.ling.docproc.extractor.sgml;

import com.globalsight.ling.docproc.extractor.sgml.SgmlObjects;

/**
 * <P>Handler functions for events that the Sgml Parser generates.</P>
 */
public interface ISgmlHandler
{
    /**
     * Called once at the beginning of a new document.
     */
    void handleStart();

    /**
     * Called once at the end of a document.
     */
    void handleFinish();

    /**
     * Handle an HTML comment <code>&lt;!-- --&gt;</code>.
     */
    void handleComment(SgmlObjects.Comment c);

    /**
     * Handle an HTML declaration <code>&lt;!DOCTYPE  &gt;</code>.
     */
    void handleDeclaration(SgmlObjects.Declaration t);

    /**
     * Handle an HTML processing instruction <code>&lt;?  ?&gt;</code>.
     */
    void handlePI(SgmlObjects.PI t);

    /**
     * Handle an HTML start tag including its attributes.
     */
    void handleStartTag(SgmlObjects.Tag t);

    /**
     * Handle an HTML end tag.
     */
    void handleEndTag(SgmlObjects.EndTag t);

    /**
     * Handle end of line characters.
     */
    void handleNewline(SgmlObjects.Newline n);

    /**
     * Handle the <code>&lt;script&gt;</code> tag; the script text is
     * included in the argument <code>t</code>.
     */
    void handleScript(SgmlObjects.Script t);

    /**
     * Handle the <code>&lt;java&gt;</code> tag; the java text is
     * included in the argument <code>t</code>.
     */
    void handleJava(SgmlObjects.Java t);

    /**
     * Handle the <code>&lt;style&gt;</code> tag; the style text is
     * included in the argument <code>t</code>.
     */
    void handleStyle(SgmlObjects.Style t);

    /**
     * Handle text (#PCDATA).
     */
    void handleText(SgmlObjects.Text t);
}
