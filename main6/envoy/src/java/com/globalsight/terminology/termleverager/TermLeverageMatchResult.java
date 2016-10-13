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

package com.globalsight.terminology.termleverager;

import com.globalsight.terminology.Hitlist;

import java.util.Iterator;
import java.io.Serializable;

/**
 * TermLeverageMatchResult contains results of term leveraging for the online
 * editor.
 * 
 * <p>
 * The result of the leveraging can produce multiple target hits per matched
 * source term. The target term list in this class is sorted based on the
 * priority, highest priority first.
 * 
 * <p>
 * This class does not store locales, it is assumed the caller knows for which
 * source and target locale this class has been constructed.
 */
public class TermLeverageMatchResult implements Serializable
{
    private static final long serialVersionUID = -6811747252793927661L;
    private Hitlist.Hit m_matchedSource = null;
    private Hitlist m_matchedTarget = new Hitlist();
    private Iterator m_targetIterator = null;

    /**
     * Set the source term and IDs.
     * 
     * @param p_term
     *            Source term string.
     * @param p_conceptId
     *            Source term's concept id.
     * @param p_termId
     *            Source term id.
     * @param p_score
     *            Source term leverage score.
     */
    public void setSource(String p_term, long p_conceptId, long p_termId,
            int p_score, String p_xml)
    {
        m_matchedSource = new Hitlist.Hit(p_term, p_conceptId, p_termId,
                p_score, p_xml);
    }

    /**
     * Add the target term to the target term list.
     * 
     * @param p_term
     *            Target term string.
     * @param p_conceptId
     *            Target term concept id.
     * @param p_termId
     *            Target term id.
     * @param p_score
     *            Target term leverage score.
     */
    public void addTarget(String p_term, long p_conceptId, long p_termId,
            int p_score, String p_xml)
    {
        m_matchedTarget.add(p_term, p_conceptId, p_termId, p_score, p_xml);
    }

    /**
     * Get the source term string.
     * 
     * @return Source term string.
     */
    public String getSourceTerm()
    {
        return m_matchedSource.m_term;
    }

    /**
     * Get the first target term string.
     * 
     * @return the first target term string in the target term list.
     */
    public String getFirstTargetTerm()
    {
        m_targetIterator = m_matchedTarget.iterator();
        return ((Hitlist.Hit) m_targetIterator.next()).m_term;
    }

    /**
     * Get the next target term string. Prior to call this method,
     * getFirstTargetTerm() should be called. Failure to do so may result in
     * NullPointerException.
     * 
     * @return the next target term string in the target term list. null is
     *         returned if no more target term is found.
     */
    public String getNextTargetTerm()
    {
        return m_targetIterator.hasNext() ? ((Hitlist.Hit) m_targetIterator
                .next()).m_term : null;
    }

    /**
     * Get the source Hitlist.Hit object.
     * 
     * @return Source Hitlist.Hit object.
     */
    public Hitlist.Hit getSourceHit()
    {
        return m_matchedSource;
    }

    /**
     * Get the iterator of target Hitlist.Hit list.
     * 
     * @return Iterator of Hitlist.Hit list.
     */
    public Iterator getTargetHitIterator()
    {
        return m_matchedTarget.iterator();
    }
}
