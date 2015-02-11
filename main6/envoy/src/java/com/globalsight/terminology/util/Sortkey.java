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

package com.globalsight.terminology.util;

import com.globalsight.ling.common.LocaleCreater;

import java.text.CollationKey;
import java.text.Collator;
import java.text.RuleBasedCollator;

import java.util.*;

/**
 * Helper class to generate binary sortkeys that can be stored in the
 * database.
 */
public class Sortkey
{
    /* Static class, private constructor */
    private Sortkey() { }

    /**
     * Creates a binary sortkey for a term in a given locale.
     */
    static public byte[] getSortkey(String p_term, String p_locale)
    {
        Locale locale = LocaleCreater.makeLocale(p_locale);

        // Collator manages a cache of Collator objects per locale and
        // returns each instance as clone.
        Collator coll = Collator.getInstance(locale);

        // Primary strength ignores accents (secondary strength) and
        // is case-insensitive (tertiary strength).
        coll.setStrength(Collator.PRIMARY);

        // With CANONICAL_DECOMPOSITION set, characters that are
        // canonical variants according to Unicode 2.0 will be
        // decomposed for collation.  This is the default setting and
        // should be used to get correct collation of accented
        // characters.

        // coll.setDecomposition(CANONICAL_DECOMPOSITION);

        // With FULL_DECOMPOSITION set, both Unicode canonical
        // variants and Unicode compatibility variants will be
        // decomposed for collation.  This causes not only accented
        // characters to be collated, but also characters that have
        // special formats to be collated with their norminal form.
        // For example, the half-width and full-width ASCII and
        // Katakana characters are then collated together.
        // FULL_DECOMPOSITION is the most complete and therefore the
        // slowest decomposition mode.

        // coll.setDecomposition(FULL_DECOMPOSITION);

        CollationKey key = coll.getCollationKey(p_term);

        return key.toByteArray();
    }
}
