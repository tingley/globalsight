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

package com.globalsight.everest.page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.snippet.Snippet;
import com.globalsight.everest.snippet.SnippetLibrary;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * <p>
 * SnippetPageTemplate extends PageTemplate for pages containing snippets. This
 * class builds additional indexes on top of the template to record the
 * locations of GS tags for a specific target language. This means there needs
 * to be one SnippetPageTemplate per (source or target) locale, not just one per
 * source page.
 * </p>
 *
 * <p>
 * This class behaves like a data class and instead of throwing exceptions
 * assumes all tags are syntactically correct.
 * </p>
 */
public class SnippetPageTemplate extends PageTemplate
{
    private static final Logger c_logger = Logger
            .getLogger(SnippetPageTemplate.class);

    static private REProgram s_reGSTags;

    static private REProgram s_reIsAddTag;
    static private REProgram s_reIsAddedTag;
    static private REProgram s_reIsDeleteTag;
    static private REProgram s_reIsEndTag;

    static private REProgram s_reAddValue;
    static private REProgram s_reAddedValue;
    static private REProgram s_reDeletedValue;
    static private REProgram s_reNameValue;
    static private REProgram s_reVersionValue;

    static
    {
        try
        {
            RECompiler dragon = new RECompiler();

            s_reGSTags = dragon.compile("<((GS\\s[^>]+)|(/GS))>");

            s_reIsAddTag = dragon.compile("ADD=[\"']");
            s_reIsAddedTag = dragon.compile("ADDED=[\"']");
            s_reIsDeleteTag = dragon.compile("DELETE=[\"']");

            s_reAddValue = dragon.compile("ADD=[\"']([^\"']+)[\"']");
            s_reAddedValue = dragon.compile("ADDED=[\"']([^\"']+)[\"']");
            s_reDeletedValue = dragon.compile("DELETED=[\"']([^\"']+)[\"']");
            s_reNameValue = dragon.compile("NAME=[\"']([^\"']+)[\"']");
            s_reVersionValue = dragon.compile("ID=[\"']([^\"']+)[\"']");
        }
        catch (RESyntaxException e)
        {
            c_logger.error("pilot error in regex", e);
        }
    }

    protected static class Position
    {
        public static final int ADD = 0;
        public static final int ADDED = 1;
        public static final int DELETE = 2;
        public static final int DELETED = 3;
        public static final int ENDTAG = 4;

        // Pointer to part or null if the position is in a snippet
        public TemplatePart m_part;

        // Pointer to (key of) snippet if the position is not in a part.
        public String m_snippetKey;

        // ADD, ADDED, DELETE, DELETED, ENDTAG
        public int m_type;

        // Offset into the TemplatePart where the tag starts
        public int m_start;
        // Offset into the TemplatePart where the tag ends
        public int m_end;

        // Does this GS tag affect our locale and gets counted, or not?
        public boolean m_isRelevant;

        public String m_name;
        public long m_version;

        public Position(TemplatePart p_part, int p_type, int p_start,
                int p_end, boolean p_isRelevant, String p_name, long p_version)
        {
            m_part = p_part;
            m_snippetKey = null;
            m_type = p_type;
            m_start = p_start;
            m_end = p_end;
            m_isRelevant = p_isRelevant;
            m_name = p_name;
            m_version = p_version;
        }

        public Position(String p_snippetKey, int p_type, int p_start,
                int p_end, boolean p_isRelevant, String p_name, long p_version)
        {
            m_part = null;
            m_snippetKey = p_snippetKey;
            m_type = p_type;
            m_start = p_start;
            m_end = p_end;
            m_isRelevant = p_isRelevant;
            m_name = p_name;
            m_version = p_version;
        }

        public boolean isInPart()
        {
            return m_part != null;
        }

        public boolean isInSnippet()
        {
            return m_snippetKey != null;
        }

        public String typeToString(int p_type)
        {
            switch (p_type)
            {
                case ADD:
                    return "ADD";
                case ADDED:
                    return "ADDED";
                case DELETE:
                    return "DELETE";
                case DELETED:
                    return "DELETED";
                case ENDTAG:
                    return "ENDTAG";
                default:
                    return "!SNAFU!";
            }
        }

        public String toString()
        {
            return "Position " + (m_part != null ? "(TP)" : "(SN)") + " type="
                    + typeToString(m_type) + " start=" + m_start + " end="
                    + m_end + " relevant=" + m_isRelevant + " name=" + m_name
                    + " version=" + m_version;
        }
    }

    /** The locale for which this template executes GS instructions. */
    protected String m_locale;

    /** Holds the GS tag positions in parts. */
    protected ArrayList m_positions = null;

    /** Holds the GS tag positions in snippets. */
    protected ArrayList m_snippetPositions = null;

    /** Map of snippet keys (name,locale,version) to Snippets in the page. */
    protected HashMap m_snippets = null;

    private SnippetPageTemplateInterpreter m_engine = null;

    //
    // Constructors
    //
    public SnippetPageTemplate()
    {
        super();
    }

    public SnippetPageTemplate(int p_type)
    {
        super(p_type);
    }

    public SnippetPageTemplate(PageTemplate p_template, String p_locale)
    {
        super(p_template);

        m_locale = p_locale;

        computePositions();
    }

    //
    // Public Methods
    //

    public String getLocale()
    {
        return m_locale;
    }

    public int getTotalPositionCount()
    {
        return m_positions.size() + m_snippetPositions.size();
    }

    //
    // Overwritten Public Methods
    //

    /**
     * Set the template parts of this PageTemplate and recomputes the GS
     * positions.
     * 
     * @see PageTemplate.setTemplateParts(Collection)
     */
    public void setTemplateParts(ArrayList p_templateParts)
    {
        super.setTemplateParts(p_templateParts);

        computePositions();
    }

    /**
     * Get the string representation of page template with TU ID data replaced
     * by Tuv content, and GS instructions executed (for the locale given in the
     * constructor).
     *
     * @return The template of the page as a string.
     */
    public String getPageData(RenderingOptions p_options) throws PageException
    {
        // if no skeleton in the page
        if (m_templateParts == null)
        {
            throw new PageException(
                    PageException.MSG_PAGETEMPLATE_GETPAGEDATA_INVALID_PARTS,
                    null, null);
        }

        int partsSize = m_templateParts.size();

        // If no TUVs are filled in and there is more than one
        // template part raise an error.
        if (m_tuvContents == null && partsSize > 1)
        {
            throw new PageException(
                    PageException.MSG_PAGETEMPLATE_GETPAGEDATA_TUVS_NOT_FILLED,
                    null, null);
        }

        if (m_positions.size() == 0)
        {
            return super.getPageData(p_options);
        }

        // delegate the hairy execution of GS tags to a separate class
        if (m_engine == null)
        {
            m_engine = new SnippetPageTemplateInterpreter(this);
        }

        return m_engine.interpret(p_options);
    }

    /**
     * Gets the Set of valid (interpreted) Tu ids for a given page. Called by
     * upload/download and the online editor.
     */
    public HashSet getInterpretedTuIds() throws PageException
    {
        // if no skeleton in the page
        if (m_templateParts == null)
        {
            throw new PageException(
                    PageException.MSG_PAGETEMPLATE_GETPAGEDATA_INVALID_PARTS,
                    null, null);
        }

        // delegate the hairy execution of GS tags to a separate class
        if (m_engine == null)
        {
            m_engine = new SnippetPageTemplateInterpreter(this);
        }

        return m_engine.getInterpretedTuIds();
    }

    //
    // Private Methods
    //

    /**
     * <p>
     * Computes the locations of GS tags in the templates and snippets to keep
     * an index that speeds up later execution of the tags.
     * </p>
     *
     * <p>
     * A position tracks the start/end of each GS tag and start/end of content
     * if applicable; the type of the tag; and whether it is relevant in the
     * locale given in the constructor.
     * </p>
     */
    private void computePositions()
    {
        m_snippetPositions = null;
        m_positions = getPositionsInParts();

        if (m_positions.size() == 0)
        {
            // O(n) to find out whether this is a no-op class or not.
            return;
        }

        m_snippetPositions = new ArrayList();
        m_snippets = new HashMap();

        ArrayList oldPositions = new ArrayList(m_positions);
        ArrayList newPositions;

        // compute the positions in all snippets (closure)
        do
        {
            newPositions = getPositionsInSnippets(oldPositions);

            m_snippetPositions.addAll(newPositions);

            oldPositions = newPositions;
        } while (newPositions.size() > 0);
    }

    /**
     * <p>
     * Computes the locations of GS tags in template parts.
     * </p>
     */
    private ArrayList getPositionsInParts()
    {
        ArrayList result = new ArrayList();

        RE re = new RE(s_reGSTags, RE.MATCH_CASEINDEPENDENT);

        Collection parts = super.getTemplateParts();

        for (Iterator it = parts.iterator(); it.hasNext();)
        {
            TemplatePart part = (TemplatePart) it.next();
            String skel = part.getSkeleton();

            if (skel != null && skel.length() > 0)
            {
                int lastIndex = 0;

                while (re.match(skel, lastIndex))
                {
                    Position pos = makePosition(re, part, null);

                    result.add(pos);

                    lastIndex = pos.m_end;
                }
            }
        }

        return result;
    }

    /**
     * <p>
     * Loads all snippets referenced in the given positions and retrieves a list
     * of new positions in the snippets.
     * </p>
     */
    private ArrayList getPositionsInSnippets(ArrayList p_positions)
    {
        ArrayList result = new ArrayList();

        for (Iterator it = p_positions.iterator(); it.hasNext();)
        {
            Position pos = (Position) it.next();

            if (refersToSnippet(pos) && !haveSnippet(pos))
            {
                // Should batch-load snippets; L8TER!!
                Snippet snip = loadSnippet(pos);

                // All snippets should exist in the database but we're
                // not using exceptions in this data class (yet) so we
                // brace ourselves for null pointers.
                if (snip != null)
                {
                    addSnippet(snip);

                    result.addAll(getPositionsInSnippet(snip));
                }
            }
        }

        return result;
    }

    /**
     * Computes the locations of GS tags in a single snippet.
     */
    private ArrayList getPositionsInSnippet(Snippet p_snippet)
    {
        ArrayList result = new ArrayList();

        RE re = new RE(s_reGSTags, RE.MATCH_CASEINDEPENDENT);

        String skel = p_snippet.getContent();

        if (skel != null && skel.length() > 0)
        {
            int lastIndex = 0;

            while (re.match(skel, lastIndex))
            {
                Position pos = makePosition(re, null, p_snippet);

                // Tue Jan 21 22:43:20 2003 CvdL: We used to detect
                // directly recursive positions and ignore them here,
                // but we have a much better mechanism now in
                // SnippetPageTemplateInterpreter, so just add.
                result.add(pos);

                lastIndex = pos.m_end;
            }
        }

        return result;
    }

    /**
     * Turns a regular expression match of a <GS> tag into a Position.
     */
    private Position makePosition(RE p_re, TemplatePart p_part,
            Snippet p_snippet)
    {
        Position result;

        String match = p_re.getParen(0);

        int start = p_re.getParenStart(0);
        int end = p_re.getParenEnd(0);
        int type = getTagType(match, m_locale);
        boolean isRelevant = getTagRelevance(match, type, m_locale);
        String snippetName = getSnippetName(match, type);
        long snippetVersion = getSnippetVersion(match, type);

        if (p_part != null)
        {
            result = new Position(p_part, type, start, end, isRelevant,
                    snippetName, snippetVersion);
        }
        else
        {
            result = new Position(getKey(p_snippet), type, start, end,
                    isRelevant, snippetName, snippetVersion);
        }

        return result;
    }

    /**
     * Examines the matched GS tag and returns the appropriate tag type (or
     * position type): ADD, ADDED, DELETE, DELETED, ENDTAG.
     */
    private int getTagType(String p_tag, String p_locale)
    {
        RE re = new RE();
        re.setMatchFlags(RE.MATCH_CASEINDEPENDENT);

        re.setProgram(s_reIsAddTag);
        if (re.match(p_tag, 0))
        {
            return Position.ADD;
        }

        re.setProgram(s_reIsAddedTag);
        if (re.match(p_tag, 0))
        {
            return Position.ADDED;
        }

        re.setProgram(s_reIsDeleteTag);
        if (re.match(p_tag, 0))
        {
            re.setProgram(s_reDeletedValue);
            if (re.match(p_tag, 0))
            {
                String locales = re.getParen(1);
                if (locales.indexOf(p_locale) >= 0)
                {
                    return Position.DELETED;
                }
            }

            return Position.DELETE;
        }

        return Position.ENDTAG;
    }

    /**
     * Examines the matched GS tag and returns true if the tag is relevant in
     * the given locale, i.e. if it is ADDED in, or DELETED from, this locale.
     */
    private boolean getTagRelevance(String p_tag, int p_type, String p_locale)
    {
        switch (p_type)
        {
            case Position.ADD:
                return true;
            case Position.DELETE:
                return true;
            case Position.DELETED:
                return true;
            case Position.ENDTAG:
                return false;

            case Position.ADDED:
            {
                RE re = new RE(s_reAddedValue, RE.MATCH_CASEINDEPENDENT);
                if (re.match(p_tag, 0))
                {
                    String locales = re.getParen(1);
                    if (locales.indexOf(p_locale) >= 0)
                    {
                        return true;
                    }
                }

                return false;
            }

            default:
                return false;
        }
    }

    /**
     * Returns the snippet name if there is an add/added snippet, else null.
     */
    private String getSnippetName(String p_tag, int p_type)
    {
        if (p_type == Position.ADD)
        {
            RE re = new RE(s_reAddValue, RE.MATCH_CASEINDEPENDENT);
            if (re.match(p_tag, 0))
            {
                return re.getParen(1);
            }
        }
        else if (p_type == Position.ADDED)
        {
            RE re = new RE(s_reNameValue, RE.MATCH_CASEINDEPENDENT);
            if (re.match(p_tag, 0))
            {
                return re.getParen(1);
            }
        }

        return null;
    }

    /**
     * Returns the snippet version if there is an ADDED snippet, else 0.
     */
    private long getSnippetVersion(String p_tag, int p_type)
    {
        if (p_type == Position.ADDED)
        {
            RE re = new RE(s_reVersionValue, RE.MATCH_CASEINDEPENDENT);
            if (re.match(p_tag, 0))
            {
                return Long.parseLong(re.getParen(1));
            }
        }

        return 0L;
    }

    /**
     * Returns true if the position refers to an added snippet.
     */
    private boolean refersToSnippet(Position p_pos)
    {
        return p_pos.m_type == Position.ADDED && p_pos.m_isRelevant == true;
    }

    private void addSnippet(Snippet p_snippet)
    {
        m_snippets.put(getKey(p_snippet), p_snippet);
    }

    /**
     * Retrieves the snippet this position is contained in.
     */
    protected Snippet getSnippet(Position p_pos)
    {
        if (p_pos.isInSnippet())
        {
            return (Snippet) m_snippets.get(p_pos.m_snippetKey);
        }

        return null;
    }

    /**
     * Retrieves the snippet this position points to.
     */
    protected Snippet getTargetSnippet(Position p_pos)
    {
        return (Snippet) m_snippets.get(getKey(p_pos));
    }

    /**
     * Returns true if the snippet this position points to has been loaded from
     * the database.
     */
    private boolean haveSnippet(Position p_pos)
    {
        return m_snippets.containsKey(getKey(p_pos));
    }

    private String getKey(Position p_pos)
    {
        String result;

        if (p_pos.m_version == 0L)
        {
            // reference to generic snippet
            result = p_pos.m_name;
        }
        else
        {
            result = p_pos.m_name + m_locale + p_pos.m_version;
        }

        return result.toUpperCase();
    }

    protected static String getKey(Snippet p_snippet)
    {
        String result;

        if (p_snippet.isGeneric())
        {
            result = p_snippet.getName();
        }
        else
        {
            result = p_snippet.getName() + p_snippet.getLocale().toString()
                    + p_snippet.getId();
        }

        return result.toUpperCase();
    }

    /**
     * <p>
     * Returns true if the snippet contains an ADD position that points to
     * itself (generic or otherwise). This test helps to prevent endless
     * recursion in case a snippet includes itself.
     * </p>
     *
     * <p>
     * We test only direct recursions here. If it turns out that people manage
     * to write indirect recursions, we need to address this in the UI in which
     * pages and snippets get authored. Performing the test here is too late.
     * </p>
     */
    private boolean snippetAddsItself(Snippet p_snippet, Position p_pos)
    {
        if (p_pos.m_type == Position.ADD || p_pos.m_type == Position.ADDED)
        {
            if (p_pos.m_name.equalsIgnoreCase(p_snippet.getName()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Loads a snippet from the database. No error handling is performed since
     * we don't want to throw exceptions from a data class. We need to revisit
     * this code and all its callers L8TER.
     */
    private Snippet loadSnippet(Position p_pos)
    {
        Snippet result = null;

        try
        {
            SnippetLibrary mgr = ServerProxy.getSnippetLibrary();

            if (p_pos.m_version == 0L)
            {
                result = mgr.getSnippet(p_pos.m_name, "", 0L);
            }
            else
            {
                result = mgr
                        .getSnippet(p_pos.m_name, m_locale, p_pos.m_version);
            }
        }
        catch (Throwable ex)
        {
            // Not a fatal error.
            c_logger.error("can't load snippet " + p_pos.m_name + " id "
                    + p_pos.m_version, ex);
        }

        return result;
    }
}
