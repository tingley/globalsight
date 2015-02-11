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

package com.globalsight.everest.page;

// globalsight
import com.globalsight.everest.request.Request;

// java
import java.io.Serializable;

/**
 * This interface represents a PrimaryFile in System4.
 * A primary file is one that has a source and target and
 * was imported into the system.
 */
public interface PrimaryFile
    extends Serializable
{
    // primary file types - must match the type specified at the request level
    public static final int EXTRACTED_FILE = 
            Request.EXTRACTED_LOCALIZATION_REQUEST;
    public static final int UNEXTRACTED_FILE = 
            Request.UNEXTRACTED_LOCALIZATION_REQUEST;

    ///////////////////////////////////////////////////////////////
    // public methods that need to be implemented
    /////////////////////////////////////////////////////////////

    /**
     * Create a copy of the particular primary file. 
     */
    public PrimaryFile clonePrimaryFile();

    /**
     * Returns the type of primary file this is (from the list of statics above)
     */
    public int getType(); 
}
