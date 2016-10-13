/**
 *  Copyright 2013 Welocalize, Inc. 
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
package com.plug.Version_8_5_2.gs.everest.util.comparator;

import java.text.CollationKey;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * StringComparator that caches collation keys for performance.
 * 
 * <b>WARNING</b>: The cache is not threadsafe, so this comparator should not be
 * shared between threads.
 * 
 * It is also important to call {@link #clearCollationKeyCache()} after a sort
 * if you are intending to keep this instance in memory. Otherwise, the cache
 * will continue to grow.
 */
public class CachingStringComparator extends StringComparator
{
    private static final long serialVersionUID = 1L;

    private transient Map<String, CollationKey> m_collationKeyCache = new HashMap<String, CollationKey>();

    /**
     * Creates a StringComparator with the given locale.
     */
    public CachingStringComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Creates a StringComparator with the given locale and column to sort on.
     */
    public CachingStringComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    @Override
    protected CollationKey getCollationKey(String s)
    {
        CollationKey key = m_collationKeyCache.get(s);
        if (key == null)
        {
            key = super.getCollationKey(s);
            m_collationKeyCache.put(s, key);
        }
        return key;
    }

    public void clearCollationKeyCache()
    {
        m_collationKeyCache.clear();
    }

}