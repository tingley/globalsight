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
package com.globalsight.ling.docproc.extractor.xptag;

import com.globalsight.ling.docproc.extractor.xptag.XPTagObjects;

/**
 * <P>Handler functions for events generated by the XPTag Parser.</P>
 */
public interface IHandler
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
     * Handles version number <v3.00>
     */
    void handleVersion(XPTagObjects.Version t);

    /**
     * Handles encoding/charset <e0>
     */
    void handleEncoding(XPTagObjects.Encoding t);

    /**
     * Handles stylesheet definitions @Normal=<...>RET.
     */
    void handleStyleDefinition(XPTagObjects.StyleDefinition t);

    /**
     * Handles stylesheet selections @:  @$:  @Normal:
     */
    void handleStyleSelection(XPTagObjects.StyleSelection t);

    /**
     * Handles paragraph styles.
     */
    void handleParagraphTag(XPTagObjects.ParaTag t);

    /**
     * Handles inline character styles.
     */
    void handleCharacterTag(XPTagObjects.CharTag t);

    /**
     * Handles special characters.
     */
    void handleSpecialTag(XPTagObjects.SpecialTag t);

    /**
     * Handles end of line characters.
     */
    void handleNewline(XPTagObjects.Newline n);

    /**
     * Handles text (#PCDATA).
     */
    void handleText(XPTagObjects.Text t);
}
