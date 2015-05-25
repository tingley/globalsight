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

package com.globalsight.terminology.termleverager.replacer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.everest.tuv.Tuv;
import com.globalsight.terminology.termleverager.TermLeverageResult;
import com.globalsight.terminology.termleverager.TermLeverageResult.MatchRecord;
import com.globalsight.terminology.termleverager.TermLeverageResult.MatchRecordList;
import com.globalsight.terminology.termleverager.TermLeverageResult.TargetTerm;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlNames;
import com.globalsight.util.gxml.TextNode;
import com.sun.org.apache.regexp.internal.RE;

/**
 * <p>
 * A base class for performing automatic term replacement. This class implements
 * the language-independent no-brainer algorithm for term replacement. Specific
 * classes can overwrite the methods findTermPositions(), sortTermPositions()
 * and doReplaceTerms() to provide specific behavior for certain locales.
 * </p>
 * 
 * <p>
 * This class is single-threaded but instances may be called multiple times.
 * </p>
 */
public/* abstract */class TermReplacer
{
    private static final Logger c_logger = Logger.getLogger(TermReplacer.class);

    //
    // Helper Classes
    //

    private class Position implements Comparable
    {
        public MatchRecord m_match;

        // TextNode in which the term starts.
        public TextNode m_node;

        // For terms interrupted by TMX formatting we need a pointer
        // to the TextNode in which the term ends:
        // public TextNode m_endNode;

        public int m_start;
        public int m_length;

        public Position(TextNode p_node, int p_start, int p_length,
                MatchRecord p_match)
        {
            m_node = p_node;
            m_start = p_start;
            m_length = p_length;
            m_match = p_match;
        }

        public boolean overlaps(Position p_other)
        {
            if (this.m_node == p_other.m_node
                    && !(this.m_start > p_other.m_start + p_other.m_length || this.m_start
                            + this.m_length < p_other.m_start))
            {
                return true;
            }

            return false;
        }

        /**
         * Two positions are equal if they are the same. Otherwise, the position
         * that starts earlier or represents a longer match comes first.
         */
        public int compareTo(Object p_other)
        {
            Position other = (Position) p_other;

            if (this.m_start < other.m_start)
            {
                return -1;
            }

            if (this.m_start == other.m_start)
            {
                if (this.m_length > other.m_length)
                {
                    return -1;
                }
                else if (this.m_length < other.m_length)
                {
                    return 1;
                }

                return 0;
            }

            return 1;
        }

        public TextNode getNode()
        {
            return m_node;
        }

        public int getStart()
        {
            return m_start;
        }

        public int getLength()
        {
            return m_length;
        }

        public MatchRecord getMatch()
        {
            return m_match;
        }

        public String toString()
        {
            return "(pos start=" + m_start + " length=" + m_length + " match="
                    + m_match.getMatchedSourceTerm() + " text="
                    + m_node.getTextNodeValue() + ")";
        }
    }

    /**
     * Data structure: Map from TextNode to a List of Position objects that
     * represent term matches occuring in that TextNode.
     */
    private class Positions extends HashMap
    {
        public void addPosition(TextNode p_node, int p_start, int p_length,
                MatchRecord p_match)
        {
            Position pos = new Position(p_node, p_start, p_length, p_match);

            ArrayList positions = (ArrayList) this.get(p_node);

            if (positions == null)
            {
                positions = new ArrayList();

                this.put(p_node, positions);
            }

            positions.add(pos);
        }

        public Iterator getNodeIterator()
        {
            return this.keySet().iterator();
        }

        public Collection getPositionLists()
        {
            return this.values();
        }

        public ArrayList getPositions(TextNode p_node)
        {
            return (ArrayList) this.get(p_node);
        }
    }

    //
    // Private Members
    //
    Locale m_targetLocale = null;
    String m_targetLocaleString = null;
    GxmlElement m_element = null;
    MatchRecordList m_termMatches = null;

    Positions m_positions = new Positions();
    boolean m_dirty = false;

    //
    // Constructor
    //

    /**
     * This class cannot be instantiated directly. Use the factory method
     * getInstance().
     */
    private TermReplacer(Locale p_source, Locale p_target)
    {
        m_targetLocale = p_target;
        m_targetLocaleString = p_target.toString();
    }

    //
    // Public Methods
    //

    /**
     * Factory method.
     */
    public static TermReplacer getInstance(Locale p_source, Locale p_target)
    {
        // Return a new instance depending on the locale.
        // For now we implement only the base class and do not cache.
        return new TermReplacer(p_source, p_target);
    }

    /**
     * Factory method.
     */
    public static TermReplacer getInstance(GlobalSightLocale p_source,
            GlobalSightLocale p_target)
    {
        return getInstance(p_source.getLocale(), p_target.getLocale());
    }

    //
    // Accessors
    //

    public GxmlElement getElement()
    {
        return m_element;
    }

    public MatchRecordList getTermMatches()
    {
        return m_termMatches;
    }

    public Positions getPositions()
    {
        return m_positions;
    }

    //
    // Abstract (or Interface) Methods
    //

    /**
     * Replaces terms in a given TUV using the list of terminology matches.
     * 
     * <P>
     * Algorithm:
     * <OL>
     * <LI>Find positions of terms, ignoring embedded formatting in the segment.
     * <LI>Identify overlapping positions and discard shorter matches.
     * <LI>Actually replace the terms.
     * </OL>
     * 
     * <P>
     * The algorith works destructively on the original TUV and if any terms
     * were replaced, marks the TUV as dirty by setting its GxmlElement to the
     * modified GxmlElement.
     * 
     * @param p_termMatches
     *            a MatchRecordList of MatchRecord objects, as obtained from
     *            TermLeverageResult.getMatchesForTuv().
     * 
     * @see TermLeverageResult
     */
    public/* final */Tuv replaceTerms(Tuv p_tuv, MatchRecordList p_termMatches)
    {
        if (p_termMatches == null || p_termMatches.isEmpty())
        {
            return p_tuv;
        }

        if (c_logger.isDebugEnabled())
        {
            c_logger.debug("\nreplaceTerms (" + p_termMatches.size()
                    + " terms) `" + p_tuv.getGxml() + "'");

            System.out.println(p_termMatches.toString());
        }

        m_positions.clear();
        m_element = p_tuv.getGxmlElement();
        m_termMatches = p_termMatches;
        m_dirty = false;

        findTermPositions();
        sortTermPositions();
        doReplaceTerms();

        if (m_dirty)
        {
            // Setting the TUVs GxmlElement forces its gxml string to
            // be marked as dirty, even if it is the same object.
            p_tuv.setGxmlElement(m_element);

            if (c_logger.isDebugEnabled())
            {
                c_logger.debug("\nreplaceTerms result = " + p_tuv.getGxml());
            }
        }

        return p_tuv;
    }

    /**
     * Identifies the TextNode elements that contain terms and the positions
     * where the terms start and end. The result is stored in the m_positions
     * member variable.
     * 
     * Text nodes that contain real text are those inside SEGMENT and
     * translatable SUBs. Text nodes inside LOCALIZABLE, BPT, EPT, PH are
     * ignored.
     */
    protected/* final */void findTermPositions()
    {
        List nodes = m_element
                .getDescendantElements(GxmlElement.TEXT_NODE_TYPE);

        for (int i = 0, max = nodes.size(); i < max; i++)
        {
            TextNode node = (TextNode) nodes.get(i);

            GxmlElement parent = node.getParent();

            switch (parent.getType())
            {
                case GxmlElement.SEGMENT:
                    findTermPositions(node);
                    break;

                case GxmlElement.SUB:
                    String locType = parent.getAttribute(GxmlNames.SUB_LOCTYPE);

                    if (locType != null
                            && locType.equals(GxmlNames.TRANSLATABLE))
                    {
                        findTermPositions(node);
                    }

                    break;

                default:
                    break;
            }
        }
    }

    protected/* abstract */void findTermPositions(TextNode p_node)
    {
        String text = p_node.getTextNodeValue();

        // For all terms, find all positions in the string where it
        // occurs as a case-sensitive exact match.
        for (int i = 0; i < m_termMatches.size(); ++i)
        {
            MatchRecord match = (MatchRecord) m_termMatches.get(i);

            // If the source term does not have any applicable target
            // terms, ignore.
            if (!isRelevantMatch(match))
            {
                // if (c_logger.isDebugEnabled())
                // {
                // c_logger.debug("\nIrrelevant match " + match.toString());
                // }

                continue;
            }

            String term = match.getMatchedSourceTerm();
            int termLen = term.length();
            int start = 0;
            int pos = 0;

            /*
             * This replaces substrings, need to test for word boundaries. while
             * ((pos = text.indexOf(term, start)) != -1) {
             * m_positions.addPosition(p_node, pos, termLen, match);
             * 
             * start = pos + termLength; }
             */

            // Need to use JDK 1.4's regex classes because jakarta
            // regexp is unable to match the second word boundary
            // correctly.
            Pattern pattern = Pattern.compile("\\b"
                    + RE.simplePatternToFullRegularExpression(term) + "\\b");
            Matcher matcher = pattern.matcher(text);

            while (matcher.find(start))
            {
                pos = matcher.start();

                // if (c_logger.isDebugEnabled())
                // {
                // c_logger.debug("\nFound " + term + " in text " + text +
                // " at pos " + pos);
                // }

                m_positions.addPosition(p_node, pos, termLen, match);

                start = matcher.end() /* - 1 */;
            }
        }
    }

    /**
     * Sorts the positions stored in m_positions member variable to identify
     * longest matches and to remove overlapping matches.
     * 
     * The result is a set of positions in TextNode elements whose text can be
     * unambiguously replaced by the corresponding target terms.
     */
    protected/* abstract */void sortTermPositions()
    {
        // For all positions in all TextNodes, remove overlaps
        for (Iterator it1 = m_positions.getPositionLists().iterator(); it1
                .hasNext();)
        {
            ArrayList positions = (ArrayList) it1.next();
            int len = positions.size();

            // sort the list by start index and match length.
            SortUtil.sort(positions);

            // remove overlaps
            Position oldPos = null;

            for (Iterator it2 = positions.iterator(); it2.hasNext();)
            {
                Position pos = (Position) it2.next();

                if (oldPos != null && oldPos.overlaps(pos))
                {
                    // if (c_logger.isDebugEnabled())
                    // {
                    // c_logger.debug("\nOverlap pos1 = " + oldPos +
                    // "\n\tpos2 = " + pos + "\nRemoving pos2.");
                    // }

                    it2.remove();
                }
                else
                {
                    oldPos = pos;
                }
            }
        }
    }

    /**
     * Performs the actual term replacement by iterating over all saved
     * positions and destructively replacing the TextNode's text content with a
     * new version in which source terms have been replaced by target terms.
     * 
     * If any TextNode is modified, the dirty flag (m_dirty) is set.
     */
    protected/* abstract */void doReplaceTerms()
    {
        StringBuffer tmp = new StringBuffer();

        for (Iterator it1 = m_positions.getNodeIterator(); it1.hasNext();)
        {
            TextNode node = (TextNode) it1.next();

            String text = node.getTextNodeValue();
            int start = 0;
            tmp.setLength(0);

            ArrayList positions = m_positions.getPositions(node);
            for (int i = 0, max = positions.size(); i < max; i++)
            {
                Position pos = (Position) positions.get(i);
                String targetTerm = getTargetTerm(pos.getMatch());

                // if (c_logger.isDebugEnabled())
                // {
                // c_logger.debug("\nReplacing `" +
                // pos.getMatch().getMatchedSourceTerm() + "' with `" +
                // targetTerm);
                // }

                tmp.append(text.substring(start, pos.getStart()));
                tmp.append(targetTerm);

                start = pos.getStart() + pos.getLength();
            }

            tmp.append(text.substring(start));

            node.setTextBuffer(tmp);

            m_dirty = true;
        }
    }

    /**
     * A match is relevant if the source term has translations in the target
     * locale.
     */
    private boolean isRelevantMatch(MatchRecord p_match)
    {
        ArrayList targets = p_match.getSourceTerm().getTargetTerms();

        if (targets == null || targets.size() == 0)
        {
            return false;
        }

        // Need to move locale logic into utility class.
        // See getTargetTerm().
        for (int i = 0, max = targets.size(); i < max; i++)
        {
            TargetTerm target = (TargetTerm) targets.get(i);

            String localeString = target.m_locale;

            if (localeString.equalsIgnoreCase(m_targetLocaleString))
            {
                return true;
            }

            if (localeString.substring(0, 2).equalsIgnoreCase(
                    m_targetLocaleString.substring(0, 2)))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the best target term for a source term. It is assumed that target
     * terms are sorted best first (by usage=preferred) so that we can return
     * the first one.
     */
    public String getTargetTerm(MatchRecord p_match)
    {
        ArrayList targets = p_match.getSourceTerm().getTargetTerms();

        // Need to move locale logic into utility class.
        // See isRelevantMatch().
        for (int i = 0, max = targets.size(); i < max; i++)
        {
            TargetTerm target = (TargetTerm) targets.get(i);

            String localeString = target.m_locale;

            if (localeString.equalsIgnoreCase(m_targetLocaleString))
            {
                return target.getMatchedTargetTerm();
            }

            if (localeString.substring(0, 2).equalsIgnoreCase(
                    m_targetLocaleString.substring(0, 2)))
            {
                return target.getMatchedTargetTerm();
            }
        }

        return "NoMaTcH";
    }
}
