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
package com.globalsight.ling.docproc;

import java.text.CharacterIterator;
import java.text.BreakIterator;

import com.ibm.text.RuleBasedBreakIterator;

/**
 * This class needs to be documented.
 */
public class GlobalsightBreakIterator
    implements Cloneable
{
    static public final int DONE = BreakIterator.DONE;

    private BreakIterator m_bi;
    private RuleBasedBreakIterator m_gsbi;
    private boolean m_bGlobalsightBreakIterator;

    /** Creates new GlobalsightBreakIterator */
    public GlobalsightBreakIterator(BreakIterator p_bi)
    {
        m_bi = p_bi;
        m_bGlobalsightBreakIterator = false;
    }

    /** Creates new GlobalsightBreakIterator */
    public GlobalsightBreakIterator(RuleBasedBreakIterator p_gsbi)
    {
        m_gsbi = p_gsbi;
        m_bGlobalsightBreakIterator = true;
    }

    /**
     * Create a copy of this iterator
     * @return A copy of this
     */
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new InternalError();
        }
    }

    /**
     * Get the text being scanned
     * @return the text being scanned
     */
    public CharacterIterator getText()
    {
        if (m_bGlobalsightBreakIterator)
        {
            return m_gsbi.getText();
        }

        return m_bi.getText();
    }

    /**
     * Set a new text for scanning.  The current scan
     * position is reset to first().
     * @param newText new text to scan.
     */
    public void setText(CharacterIterator p_text)
    {
        if (m_bGlobalsightBreakIterator)
        {
            m_gsbi.setText(p_text);
            return;
        }

        m_bi.setText(p_text);
    }

    /**
     */
    public void setText(String p_text)
    {
        if (m_bGlobalsightBreakIterator)
        {
            m_gsbi.setText(p_text);
            return;
        }

        m_bi.setText(p_text);
    }

    /**
     * Return the boundary following the current boundary.
     * @return The character index of the next text boundary or DONE
     * if all boundaries have been returned.  Equivalent to next(1).
     */
    public int next()
    {
        if (m_bGlobalsightBreakIterator)
        {
            return m_gsbi.next();
        }

        return m_bi.next();
    }

    /**
     * Return the last boundary. The iterator's current position is
     * set to the last boundary.
     * @return The character index of the last text boundary.
     */
    public int last()
    {
        if (m_bGlobalsightBreakIterator)
        {
            return m_gsbi.last();
        }

        return m_bi.last();
    }

    /**
     * Return the first boundary. The iterator's current position is
     * set to the first boundary.
     * @return The character index of the first text boundary.
     */
    public int first()
    {
        if (m_bGlobalsightBreakIterator)
        {
            return m_gsbi.first();
        }

        return m_bi.first();
    }

    /**
     * Return the nth boundary from the current boundary
     * @param n which boundary to return.  A value of 0 does nothing.
     * Negative values move to previous boundaries and positive values
     * move to later boundaries.
     * @return The index of the nth boundary from the current position.
     */
    public int next(int p_n)
    {
        if (m_bGlobalsightBreakIterator)
        {
            return m_gsbi.next(p_n);
        }

        return m_bi.next(p_n);
    }

    /**
     * Return the first boundary following the specified offset.  The
     * value returned is always greater than the offset or the value
     * BreakIterator.DONE
     * @param offset the offset to begin scanning. Valid values are
     * determined by the CharacterIterator passed to setText().
     * Invalid values cause an IllegalArgumentException to be thrown.
     * @return The first boundary after the specified offset.
     */
    public int following(int p_offset)
    {
        if (m_bGlobalsightBreakIterator)
        {
            return m_gsbi.following(p_offset);
        }

        return m_bi.following(p_offset);
    }

    /**
     * Return character index of the text boundary that was most
     * recently returned by next(), previous(), first(), or last()
     * @return The boundary most recently returned.
     */
    public int current()
    {
        if (m_bGlobalsightBreakIterator)
        {
            return m_gsbi.current();
        }

        return m_bi.current();
    }

    /**
     * Return the boundary preceding the current boundary.
     * @return The character index of the previous text boundary or
     * DONE if all boundaries have been returned.
     */
    public int previous()
    {
        if (m_bGlobalsightBreakIterator)
        {
            return m_gsbi.previous();
        }

        return m_bi.previous();
    }
}
