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
import com.globalsight.everest.page.ExtractedFile;


/**
 * This represents a target file that has been imported into they
 * system and also parsed and extracted.  
 */
public class ExtractedTargetFile
    extends ExtractedFile
{
    private static final long serialVersionUID = -6602918011118128470L;

    /**
     * Default constructor.
     */
    public ExtractedTargetFile()
    {}

    /**
     * Create a new primray file from this one's data.
     */
    public PrimaryFile clonePrimaryFile()
    {
        ExtractedTargetFile pf = new ExtractedTargetFile();
        pf.setInternalBaseHref(getInternalBaseHref());
        pf.setExternalBaseHref(getExternalBaseHref());
        return pf;
    }

    /**
     * Return a string representation of the object.
     * @return a string representation of the object.
     */
    public String toString()
    {
        return super.toString();
    }
}

