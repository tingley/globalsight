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
package com.globalsight.ling.tm.fuzzy;

import com.globalsight.util.MruCache;

/**
 * Pool of atoms
*/
public class AtomPool
{
    private static final int MAX_POOL_SIZE = 2000;

    // Pool owned by class.
    private static MruCache m_cache = new MruCache(MAX_POOL_SIZE);

    /**
     * Getter of atom instance. Looks for an existing atom in the
     * cache. If none found, create a new one and put it in the
     * cache. There is no need to syncronize this method because
     * MruCache is syncronized.
     *
     * @param p_atomString String from which atom is created.
     */
    public static Atom getInstance(String p_atomString)
    {
        Atom atom = (Atom)m_cache.get(p_atomString);
        if(atom == null)
        {
            atom = new Atom(p_atomString);
            m_cache.put(p_atomString, atom);
        }
        return atom;
    }
}
