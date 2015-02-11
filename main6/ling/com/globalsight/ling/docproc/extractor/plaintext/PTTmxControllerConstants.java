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
package com.globalsight.ling.docproc.extractor.plaintext;

/**
 * <p>Constants used by the Tmx controller and extractor.</p>
 */
public interface PTTmxControllerConstants
{

    /**
     * Indicates a token that leads on a line.
     */
    static int LEADING      =   0;

    /**
     * Indicates a token that is embeded in a line.
     */
    static int EMBEDDED     =   1;

    /**
     * Indicates a token that trails on a line.
     */
    static int TRAILING     =   3;

}
