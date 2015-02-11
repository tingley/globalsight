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

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Vector;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.GeneralException;
import com.globalsight.ling.docproc.GlobalsightBreakIterator;

/**
 * Responsible for creating fuzzy index entries in the FUZZY_INDEX
 * table.
 */
public class FuzzyIndexer
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            FuzzyIndexer.class.getName());

    private Tokenizer m_tokenizer = null;

    /**
     * FuzzyIndexer constructor comment.
     */
    public FuzzyIndexer()
    {
        m_tokenizer = new Tokenizer();
    }

    /**
     * Tokenizes a given tuv and returns a list of tokens for the the
     * fuzzy index.
     */
    public Vector index(String p_tuv, long p_tuvId,
        GlobalSightLocale p_locale, long p_tmId,
        GlobalsightBreakIterator p_breakIterator)
    {
        HashMap tokens = m_tokenizer.tokenize(p_tuv, p_locale.getLocale(),
                                                p_breakIterator);

        // update the tokens with the extra data needed to create a
        // TOPLink "mappable" object.
        Collection theTokens = tokens.values();
        long localeId = p_locale.getId();
        int size = theTokens.size();
        Iterator it = theTokens.iterator();

        while (it.hasNext())
        {
            Token token = (Token)it.next();

            token.setLocale(localeId);
            token.setTmId(p_tmId);
            token.setTuvId(p_tuvId);
            token.setTokenCount(size);
        }

        return new Vector(tokens.values());
    }


    /**
     * Make a list of parameters that is needed to execute indexing
     * stored procedure.
     */
    public List makeParameterList(String p_tuv, long p_tuvId,
        GlobalSightLocale p_locale, long p_tmId,
        GlobalsightBreakIterator p_breakIterator)
    {
        if(p_tuv == null || p_tuv.length() == 0)
        {
            return new ArrayList();
        }

        // get atoms of the tuv string
        Map atoms = m_tokenizer.atomize(p_tuv, p_locale.getLocale(),
                                        p_breakIterator);
        Collection theAtoms = atoms.values();
        int size = theAtoms.size();

        // make the list
        List list = new ArrayList(size + 10);
        list.add(new Long(p_tuvId));
        list.add(p_locale.getIdAsLong());
        list.add(new Long(p_tmId));
        list.add(new Long(size));

        Iterator it = theAtoms.iterator();
        while (it.hasNext())
        {
            Atom atom = (Atom)it.next();
            list.add(new Long(atom.getAtomCrc()));
        }
        list.add(new Long(-1)); // end of a tuv

        return list;

    }

}
