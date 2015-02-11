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

package com.globalsight.everest.tuv;

import org.apache.log4j.Logger;

import com.globalsight.everest.tuv.TuvException;

/**
 * <p>This class represents custom user translation unit types not
 * previously known by the system.</p>
 *
 * @see TuType.
 */
public final class CustomTuType
    extends TuType
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            CustomTuType.class.getName());

    /**
     * <p>Construct a CustomTuType from a name.  Cannot be a TuType
     * that is previously known.  Cannot be a CustomTuType that has
     * been previously defined.</p>
     *
     * <p>First use TuType valueOf(String p_name) to obtain an
     * existing TuType.  If a TuvException is thrown, then construct
     * one with this constructor.</p>
     */
    public CustomTuType(String p_name)
        throws TuvException
    {
        super(p_name);
    }
}
