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
package com.globalsight.everest.corpus;

import com.globalsight.everest.persistence.PersistentObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * A CorpusDocGroup is a persistent object that corresponds to a row in the
 * corpus_unit table. It contains the name of the CorpusDocGroup (corpus_unit)
 * and an array of the CorpusDocs (corpus_unit_variant) in the corpus doc group.
 */
public class CorpusDocGroup extends PersistentObject implements Serializable
{
    // ////////////////////////////////////
    // Static Members //
    // ///////////////////////////////////

    private static final long serialVersionUID = 8725464490879455573L;

    public static final String CORPUS_NAME = "m_corpusName";
    public static final String CORPUS_DOCS = "m_corpusDocs";

    // ////////////////////////////////////
    // Private Members //
    // ////////////////////////////////////
    private String m_corpusName = null;
    private ArrayList m_corpusDocs = null;

    // ////////////////////////////////////
    // Constructor //
    // ////////////////////////////////////

    /**
     * Creates a CorpusDocGroup object
     */
    public CorpusDocGroup()
    {
    }

    // ////////////////////////////////////
    // Public Methods //
    // ////////////////////////////////////

    /**
     * Returns the array of CorpusDocs for this doc group. (corpus_unit)
     * 
     * @return List
     */
    public ArrayList getCorpusDocs()
    {
        return m_corpusDocs;
    }
    
    public Set getDocs()
    {
        Set docs = null;
        if (m_corpusDocs != null)
        {
            docs = new HashSet(m_corpusDocs);;
        }
        return docs;
    }

    /**
     * Sets the array of corpus docs
     * 
     * @param p_corpusDocs
     *            array of corpus docs in this group
     */
    public void setCorpusDocs(ArrayList p_corpusDocs)
    {
        m_corpusDocs = p_corpusDocs;
    }
    
    public void setDocs(Set p_corpusDocs)
    {
        m_corpusDocs = null;        
        if (p_corpusDocs != null)
        {
            m_corpusDocs = new ArrayList(p_corpusDocs);
        }
    }

    /**
     * Returns the name of the documents in this corpus group. They all share
     * the same name -- which is the external page id of the source page.
     * 
     * @return String
     */
    public String getCorpusName()
    {
        return m_corpusName;
    }

    /**
     * Sets the corpus doc group name
     * 
     * @param p_corpusName
     *            new name
     */
    public void setCorpusName(String p_corpusName)
    {
        m_corpusName = p_corpusName;
    }
    
    public void deleteCorpusDoc(CorpusDoc p_corpusDoc)
    {
        long id =  p_corpusDoc.getId();
        if (m_corpusDocs != null && id > 0)
        {
            for (int i = 0; i < m_corpusDocs.size(); i++)
            {
                CorpusDoc doc = (CorpusDoc)m_corpusDocs.get(i);
                if (id == doc.getId())
                {
                    m_corpusDocs.remove(doc);
                    break;
                }
            }
        }       
    }
    
    public void addCorpusDoc(CorpusDoc p_corpusDoc)
    {
        if (m_corpusDocs == null)
        {
            m_corpusDocs = new ArrayList();
        }
        m_corpusDocs.add(p_corpusDoc);
    }
}
