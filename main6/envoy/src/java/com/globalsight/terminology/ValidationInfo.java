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

package com.globalsight.terminology;

import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.TermbaseExceptionMessages;

import com.globalsight.util.edit.EditUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A data class holding entry validation information:
 * - missing fields     (for later, with entry models)
 * - per term, a set of conflicting terms and their entries.
 *
 * Entry models may be implemented in the editor, who's to say.
 * I'm just throwing it in for fun.
 *
 * <validationresult>
 *   <conceptid>1234</conceptid>
 *   <validation>
 *     <duplicates> (*)
 *       <original>
 *         <language>English</language><term>entry term</term>
 *       </original>
 *       <others>
 *         <other> (+)
 *           <language>English</language>
 *           <term tid cid>similar term</term> (*)
 *         </other>
 *       </others>
 *     </duplicates>
 *   </validation>
 *   <missingfields>
 *     <concept> (+)
 *       <field type="conceptDomain"/> (*)
 *       <language name="English"> (*)
 *         <field type="Definition"/> (*)
 *         <term term="entry term"> (*)
 *           <field type="POS"/> (+)
 *         </term>
 *       </language>
 *     </concept>
 *   </missingfields>
 * </validationresult>
 *
 * @see Validator
 */
public class ValidationInfo
{
    static private class DuplicateTerm
    {
        private String m_term;
        private long m_cid;
        private long m_tid;

        public DuplicateTerm(String p_term, long p_cid, long p_tid)
        {
            m_term = p_term;
            m_cid = p_cid;
            m_tid = p_tid;
        }

        /**
         * @return an xml string of the form
         * <term tid="..." cid="...">similar term</term>
         */
        public String asXML()
        {
            StringBuffer result = new StringBuffer();

            result.append("<term cid=\"");
            result.append(m_cid);
            result.append("\" tid=\"");
            result.append(m_tid);
            result.append("\">");
            result.append(EditUtil.encodeXmlEntities(m_term));
            result.append("</term>\n");

            return result.toString();
        }
    }

    static private class DuplicatesPerTerm
    {
        // Original language and term in entry to be validated.
        private String m_language;
        private String m_term;

        // Map of Language to ArrayList of DuplicateTerm
        private HashMap m_duplicates = new HashMap();

        public DuplicatesPerTerm(String p_language, String p_term)
        {
            m_language = p_language;
            m_term = p_term;
        }

        public String getLanguage()
        {
            return m_language;
        }

        public String getTerm()
        {
            return m_term;
        }

        public void addDuplicate(String p_language, String p_term,
            long p_cid, long p_tid)
        {
            ArrayList l = (ArrayList)m_duplicates.get(p_language);

            if (l == null)
            {
                l = new ArrayList();
                m_duplicates.put(p_language, l);
            }

            l.add(new DuplicateTerm(p_term, p_cid, p_tid));
        }

        /**
         * @return an xml string of the form
         * <duplicates>
         *   <original>
         *     <language>English</language><term>entry term</term>
         *   </original>
         *   <others>
         *     <other>
         *       <language>English</language>
         *       <term tid cid>similar term</term>
         *       <term tid cid>similar term</term>
         *       <term tid cid>similar term</term>
         *     </other>
         *   </others>
         * </duplicates>
         */
        public String asXML()
        {
            StringBuffer result = new StringBuffer();

            result.append("<duplicates>\n");
            result.append("<original>\n");
            result.append("<language>");
            result.append(EditUtil.encodeXmlEntities(m_language));
            result.append("</language>");
            result.append("<term>");
            result.append(EditUtil.encodeXmlEntities(m_term));
            result.append("</term>\n");
            result.append("</original>\n");

            result.append("<others>\n");
            for (Iterator it = m_duplicates.keySet().iterator(); it.hasNext();)
            {
                String key = (String)it.next();

                result.append("<other>\n");
                result.append("<language>");
                result.append(EditUtil.encodeXmlEntities(key));
                result.append("</language>\n");

                ArrayList dups = (ArrayList)m_duplicates.get(key);

                for (int i = 0, max = dups.size(); i < max; i++)
                {
                    DuplicateTerm info = (DuplicateTerm)dups.get(i);

                    result.append(info.asXML());
                }

                result.append("</other>\n");
            }
            result.append("</others>\n");
            result.append("</duplicates>\n");

            return result.toString();
        }
    }

    //
    // Members
    //

    /** The original concept id - for reference. */
    private long m_conceptId = 0;
    /** List of DuplicatePerTerm for terms in the validated entry. */
    private ArrayList m_duplicates = new ArrayList();

    //
    // Constructors
    //
    public ValidationInfo(long p_conceptId)
    {
        m_conceptId = p_conceptId;
    }

    public void addDuplicate(String p_language, String p_term,
        String p_dupLanguage, String p_dupTerm, long p_cid, long p_tid)
    {
        // If caller passes in the term from the concept itself,
        // ignore it.
        if (p_cid == m_conceptId)
        {
            return;
        }

        DuplicatesPerTerm list = null;

        for (int i = 0, max = m_duplicates.size(); i < max; i++)
        {
            DuplicatesPerTerm temp = (DuplicatesPerTerm)m_duplicates.get(i);

            if (temp.getLanguage().equals(p_language) &&
                temp.getTerm().equals(p_term))
            {
                list = temp;
                break;
            }
        }

        if (list == null)
        {
            list = new DuplicatesPerTerm(p_language, p_term);
            m_duplicates.add(list);
        }

        list.addDuplicate(p_dupLanguage, p_dupTerm, p_cid, p_tid);
    }

    /**
     * @return an xml string of the form
     * <validationresult>
     *   <conceptid>1234</conceptid>
     *   <validation>
     *     <duplicates>
     *       <original>
     *         <language>English</language><term>entry term</term>
     *       </original>
     *       <others>
     *         <other>
     *           <language>English</language>
     *           <term tid cid>similar term</term>
     *           <term tid cid>similar term</term>
     *           <term tid cid>similar term</term>
     *         </other>
     *       </others>
     *     </duplicates>
     *   </validation>
     * </validationresult>
     */
    public String asXML()
    {
        StringBuffer result = new StringBuffer();

        result.append("<validationresult>\n");
        result.append("<conceptid>");
        result.append(m_conceptId);
        result.append("</conceptid>\n");

        result.append("<validation>\n");
        for (int i = 0, max = m_duplicates.size(); i < max; ++i)
        {
            DuplicatesPerTerm list = (DuplicatesPerTerm)m_duplicates.get(i);

            result.append(list.asXML());
        }
        result.append("</validation>\n");

        result.append("</validationresult>");

        return result.toString();
    }

    /* TEST CODE
    static public void main(String[] argv)
    {
        long cid = 1;
        ValidationInfo i = new ValidationInfo(1);

        i.addDuplicate("English", "term", "English", "term-1", 1, 2);
        i.addDuplicate("English", "term", "English", "term-2", 2, 3);
        i.addDuplicate("French", "abc", "French", "term-1", 1, 2);
        i.addDuplicate("English", "term", "Book", "book-1", 3, 4);
        i.addDuplicate("French", "abc", "French", "term-2", 1, 2);
        i.addDuplicate("English", "term", "Book", "book-2", 4, 5);
        i.addDuplicate("English", "TERM2", "English", "TERM3", 1, 2);
        i.addDuplicate("English", "TERM2", "English", "TERM4", 1, 2);

        System.out.println(i.asXML());
    }
    */
}
