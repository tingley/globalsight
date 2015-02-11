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
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.online.UIConstants;
import com.globalsight.everest.page.SnippetPageTemplate.Position;
import com.globalsight.everest.snippet.Snippet;
import com.globalsight.ling.common.DiplomatNames;
import com.globalsight.util.edit.EditUtil;


/**
 * <p>This class contains code factored out from SnippetPageTemplate
 * to interpret GS instructions in a GS-tagged source page. It fleshes
 * out the getPageData() method that here returns the combination of
 * TemplateParts + snippets + tuvs.</p>
 *
 * <p>Tue Jan 21 21:05:09 2003 CvdL: added checks for directly and
 * indirectly recursive snippets. Recursive snippets will get printed
 * with a special icon (skull or stop sign) and hovering over them
 * will reveal the recursion chain.</p>
 *
 * @see PageTemplate.getPageData()
 * @see SnippetPageTemplate.getPageData()
 */
public class SnippetPageTemplateInterpreter
    implements UIConstants
{
    private static final Logger c_logger =
        Logger.getLogger(
            SnippetPageTemplateInterpreter.class);

    /**
     * Pointer to the data object: TemplateParts + Positions + Snippets.
     */
    private SnippetPageTemplate m_data;

    private StringBuffer m_result;

    private List m_parts;
    private ArrayList m_positions;

    private int m_curPart;
    private int m_curPos;

    private TemplatePart m_part;
    private Snippet m_snippet;

    private Position m_prevPos;
    private Position m_pos;

    /** Stack for recursive snippet evaluation. */
    private Stack m_stack;

    /** Another stack to detect indirectly recursive snippets. */
    private ArrayList m_snippetStack;

    /** Counter for GS tags in the file. */
    private int m_counter;

    /** The index of the position to search for in findPosition(). */
    private int m_searchCounter;

    /** The counter of each snippet - and any embedded snippets */
    private int m_snippetCount = 0;

    /*
     * Holds UI settings: snippet UI vs editor UI, text view vs
     * preview view, read-only vs read-write.
     *
     * The template type is in m_data.getType().
     */
    private RenderingOptions m_options;

    /**
     * A set holding the TU ids that are shown in this page after GS
     * tags were executed.
     */
    private HashSet m_interpretedTuIds = null;


    //
    // Constructors
    //
    public SnippetPageTemplateInterpreter(SnippetPageTemplate p_data)
    {
        m_data = p_data;
    }


    //
    // Public Methods
    //

    /**
     * Get the string representation of page template with TU ID data
     * replaced by Tuv content, and GS instructions executed (for the
     * locale given in the SnippettemplatePart's constructor).
     *
     * @return The template of the page as a string.
     */
    protected String interpret(RenderingOptions p_options)
    {
        init(p_options);

        m_result = new StringBuffer(m_parts.size() * 100);

        initPart();
        initPosition();

        interpretParts();

        String result = m_result.toString();

        exit();

        return result;
    }

    /**
     * Find a position by its numeric index in the template.
     */
    public Position findPosition(int p_index)
    {
        if (p_index == 0)
        {
            return null;
        }

        init();

        m_searchCounter = p_index;
        pushPositions(m_positions);

        countPositions();

        Position result = m_pos;

        exit();

        return result;
    }

    /**
     * Returns the set of interpreted Tu ids, that is the TUs that are
     * needed when printing the template in the current language. The
     * resulting Set is read-only because it may be shared with other
     * threads.
     */
    public HashSet getInterpretedTuIds()
    {
        if (m_interpretedTuIds == null)
        {
            init();

            m_interpretedTuIds = new HashSet();

            initPart();
            initPosition();

            findInterpretedTuIds(m_interpretedTuIds);

            exit();
        }

        return m_interpretedTuIds;
    }


    //
    // Private Methods
    //

    /** Life cycle management: initialize object to be used. */
    private void init(RenderingOptions p_options)
    {
        m_options = p_options;

        m_snippet = null;
        m_part = null;
        m_parts = m_data.m_templateParts;
        m_positions = m_data.m_positions;
        m_stack = new Stack();
        m_snippetStack = new ArrayList();
        m_counter = 0;
    }

    private void init()
    {
        // the options aren't important just create a default one
        RenderingOptions options = new RenderingOptions(0,0,0,null);
        this.init(options);
    }

    /** Life cycle management: free garbage-collectable objects. */
    private void exit()
    {
        m_result = null;
        m_parts = null;
        m_positions = null;
        m_part = null;
        m_snippet = null;
        m_stack = null;
        m_snippetStack = null;
        m_prevPos = null;
        m_pos = null;
    }


    /**
     * Executes GS instructions in template parts and snippets.
     */
    private void interpretParts()
    {
        while (true)
        {
            String skel = getSkeleton();

            if (skel == null)
            {
                break;
            }

            if (partHasPosition())
            {
                int index = 0;

                while (partHasPosition())
                {
                    outputSkeleton(skel.substring(index, m_pos.m_start));

                    interpretPosition();

                    // DELETED may have advanced the part, refresh
                    skel = getSkeleton();
                    index = m_prevPos.m_end;
                }

                outputSkeleton(skel.substring(index));
            }
            else
            {
                outputSkeleton(skel);
            }

            // If there is a TU in this template part, append it too.
            Long tuId = getTuId();
            if (tuId.longValue() > 0L)
            {
                outputTuv(getTuv(tuId));
            }

            advancePart();
        }
    }

    /**
     * Executes the current Position in the current part (or snippet)
     * and advances the Position (skipping parts if DELETED).
     */
    private void interpretPosition()
    {
        switch (m_pos.m_type)
        {
        case Position.ADD:     interpretAdd();     break;
        case Position.ADDED:   interpretAdded();   break;
        case Position.DELETE:  interpretDelete();  break;
        case Position.DELETED: interpretDeleted(); break;
        case Position.ENDTAG:  interpretEndTag();  break;
        }

        advancePosition();
    }

    private void interpretAdd()
    {
        outputAdd();
    }

    private void interpretAdded()
    {
        if (isRelevant())
        {
            Snippet snip = getTargetSnippet(m_pos);

            if (snip != null)
            {
                if (isIndirectlyRecursiveSnippet(snip))
                {
                    outputAddedSnippetIsRecursive(m_pos, snip);
                }
                else
                {
                    outputAddedBegin(snip);

                    pushSnippet(snip);

                    interpretParts();

                    popSnippet();

                    outputAddedEnd(snip);
                }
            }
            else
            {
                // mark the error in the file (and count)
                outputAddedNotFound(m_pos);
            }
        }
        else
        {
            outputAddedOtherLocale(m_pos);
        }
    }

    private void interpretDelete()
    {
        outputDeleteStart();
    }

    private void interpretDeleted()
    {
        // DELETED is by definition relevant for this locale (it would
        // be a DELETE otherwise
        int depth = 1;

        outputDeletedStart();

        advancePosition();

  loop:
        while (depth > 0)
        {
            if (m_pos == null)
            {
                c_logger.error("\n@@@ END TAG OF DELETED CONTENT NOT FOUND");
                break;
            }

            while (!partHasPosition())
            {
                advancePart();
            }

            if (m_snippet == null && m_part == null)
            {
                c_logger.error("\n@@@ NO MORE DELETED CONTENT PARTS");
                break;
            }

            switch (m_pos.m_type)
            {
            case Position.ENDTAG:
                if (--depth == 0)
                {
                    break loop;
                }

                advancePosition();
                break;

            case Position.DELETE:
            case Position.DELETED:
                ++depth;
                advancePosition();
                break;

            default:
                advancePosition();
                break;
            }
        }

        outputDeletedEnd();
    }

    private void interpretEndTag()
    {
        outputEndTag();
    }

    //
    // Find Position Helpers
    //

    public void countPositions()
    {
        while (nextPosition())
        {
            switch (m_pos.m_type)
            {
            case Position.ADDED:   countAdded();   break;
            case Position.DELETED: countDeleted(); break;
            case Position.ADD:     m_counter++;    break;
            case Position.DELETE:  m_counter++;    break;
            case Position.ENDTAG:  break;
            }

            if (m_counter == m_searchCounter || m_pos == null)
            {
                return;
            }
        }
    }

    private void countAdded()
    {
        if (isRelevant())
        {
            m_counter++;

            if (m_counter == m_searchCounter || m_pos == null)
            {
                return;
            }

            Snippet snip = getTargetSnippet(m_pos);

            if (snip != null)
            {
                pushSnippetPositions(snip);

                countPositions();

                if (m_counter == m_searchCounter || m_pos == null)
                {
                    return;
                }
            }
        }
    }

    private void countDeleted()
    {
        int depth = 1;

        m_counter++;

        if (m_counter == m_searchCounter || m_pos == null)
        {
            return;
        }

  loop:
        while (depth > 0 && nextPosition())
        {
            switch (m_pos.m_type)
            {
            case Position.ENDTAG:
                if (--depth == 0)
                {
                    break loop;
                }
                break;

            case Position.DELETE:
            case Position.DELETED:
                ++depth;
                break;

            default:
                break;
            }
        }
    }


    /**
     * Iterator for findPosition().
     */
    private boolean nextPosition()
    {
        if (m_stack.isEmpty())
        {
            m_pos = null;

            return false;
        }

        m_pos = (Position)m_stack.pop();

        return true;
    }

    private void pushPositions(ArrayList p_positions)
    {
        for (int i = p_positions.size(); i > 0; --i)
        {
            m_stack.push(p_positions.get(i - 1));
        }
    }

    private void pushSnippetPositions(Snippet p_snippet)
    {
        pushPositions(getSnippetPositions(p_snippet));
    }


    //
    // getInterpretedTuIds Helper
    //
    private void findInterpretedTuIds(HashSet p_result)
    {
        while (true)
        {
            String skel = getSkeleton();

            if (skel == null)
            {
                break;
            }

            while (partHasPosition())
            {
                findInterpretedTuIds1(p_result);
            }

            // If there is a TU in this template part, remember it.
            Long tuId = getTuId();
            if (tuId.longValue() > 0L)
            {
                p_result.add(tuId);
            }

            advancePart();
        }
    }

    private void findInterpretedTuIds1(HashSet p_result)
    {
        if (m_pos.m_type == Position.DELETED)
        {
            int depth = 1;

            advancePosition();

      loop:
            while (depth > 0)
            {
                while (!partHasPosition())
                {
                    advancePart();
                }

                switch (m_pos.m_type)
                {
                case Position.ENDTAG:
                    if (--depth == 0)
                    {
                        break loop;
                    }

                    advancePosition();
                    break;

                case Position.DELETE:
                case Position.DELETED:
                    ++depth;
                    advancePosition();
                    break;

                default:
                    advancePosition();
                    break;
                }
            }
        }
        else
        {
            advancePosition();
        }
    }


    //
    // Data and iteration helpers
    //

    private void initPart()
    {
        m_curPart = 0;

        if (m_curPart < m_parts.size())
        {
            m_part = (TemplatePart)m_parts.get(m_curPart);
        }
        else
        {
            m_part = null;
        }
    }

    private void advancePart()
    {
        if (m_snippet != null)
        {
            m_snippet = null;
            m_part = null;
        }
        else
        {
            ++m_curPart;

            if (m_curPart < m_parts.size())
            {
                m_part = (TemplatePart)m_parts.get(m_curPart);
            }
            else
            {
                m_part = null;
            }
        }
    }

    private void initPosition()
    {
        m_curPos = 0;

        if (m_curPos < m_positions.size())
        {
            m_pos = (Position)m_positions.get(m_curPos);
        }
        else
        {
            m_pos = null;
        }

        m_prevPos = null;
    }

    private void advancePosition()
    {
        m_prevPos = m_pos;

        ++m_curPos;

        if (m_curPos < m_positions.size())
        {
            m_pos = (Position)m_positions.get(m_curPos);
        }
        else
        {
            m_pos = null;
        }
    }

    /**
     * True if the current part contains a GS tag, i.e. has the
     * current Position object pointing to it.
     */
    private boolean partHasPosition()
    {
        if (m_pos != null)
        {
            if (m_snippet != null)
            {
                // By definition when looking at a snippet,
                // m_positions contains all positions for the snippet.
                return true /*positionIsInSnippet(m_pos, m_snippet)*/;
            }
            else
            {
                return m_part == m_pos.m_part;
            }
        }

        return false;
    }

    /**
     * True if the snippet contains the position.
     */
    private boolean positionIsInSnippet(Position p_pos, Snippet p_snippet)
    {
        String snippetKey = p_pos.m_snippetKey;

        if (snippetKey != null && snippetKey.equalsIgnoreCase(
            SnippetPageTemplate.getKey(p_snippet)))
        {
            return true;
        }

        return false;
    }


    /*
    // True if the ADDED position points to the given snippet.
    private boolean positionRefersToSnippet(
        Position p_pos, Snippet p_snippet)
    {
        // snippet and position must have same name and...
        if (p_snippet.getName().equalsIgnoreCase(p_pos.m_name))
        {
            // ... be generic or have the same id
            if ((p_snippet.isGeneric() && p_pos.m_version == 0L) ||
                (p_snippet.getId() == p_pos.m_version))
            {
                return true;
            }
        }

        return false;
    }
    */

    private void pushSnippet(Snippet p_snippet)
    {
        // Remember snippet for recursion check.
        m_snippetStack.add(p_snippet);

        // A hair in the soup. Po' man's stack.
        ArrayList context = new ArrayList(8);
        context.add(m_parts);
        context.add(m_positions);
        context.add(new Integer(m_curPart));
        context.add(new Integer(m_curPos));
        context.add(m_part);
        context.add(m_snippet);
        context.add(m_prevPos);
        context.add(m_pos);

        m_stack.push(context);

        m_parts = new ArrayList(0);
        m_positions = getSnippetPositions(p_snippet);
        m_curPart = 0;
        m_curPos = 0;
        m_part = null;
        m_snippet = p_snippet;

        initPosition();
    }

    private void popSnippet()
    {
        // Pop snippet from recursion check stack.
        m_snippetStack.remove(m_snippetStack.size() - 1);

        ArrayList context = (ArrayList)m_stack.pop();

        m_parts     = (ArrayList)context.get(0);
        m_positions = (ArrayList)context.get(1);
        m_curPart   = ((Integer)context.get(2)).intValue();
        m_curPos    = ((Integer)context.get(3)).intValue();
        m_part      = (TemplatePart)context.get(4);
        m_snippet   = (Snippet)context.get(5);
        m_prevPos   = (Position)context.get(6);
        m_pos       = (Position)context.get(7);
    }

    private boolean isIndirectlyRecursiveSnippet(Snippet p_snippet)
    {
        return m_snippetStack.contains(p_snippet);
    }

    private String getRecursionChain(Snippet p_snippet)
    {
        StringBuffer result = new StringBuffer();

        int index = m_snippetStack.indexOf(p_snippet);

        for (int i = index; i < m_snippetStack.size(); i++)
        {
            Snippet s = (Snippet)m_snippetStack.get(i);

            result.append(s.getName());
            result.append(" (");
            result.append(s.getId());
            result.append(") ");
        }

        result.append(p_snippet.getName());

        return result.toString();
    }

    /**
     * Finds all positions that refer to the given snippet.
     */
    private ArrayList getSnippetPositions(Snippet p_snippet)
    {
        ArrayList result = new ArrayList();
        ArrayList positions = m_data.m_snippetPositions;

        if (positions.size() == 0)
        {
            return result;
        }

        for (int i = 0; i < positions.size(); ++i)
        {
            Position pos = (Position)positions.get(i);

            if (positionIsInSnippet(pos, p_snippet))
            {
                result.add(pos);
            }
        }

        return result;
    }

    /**
     * Returns the text string of the currently selected part of snippet.
     */
    private String getSkeleton()
    {
        if (m_snippet != null)
        {
            return m_snippet.getContent();
        }
        else if (m_part != null)
        {
            return m_part.getSkeleton();
        }

        return null;
    }

    private boolean isRelevant()
    {
        return m_pos.m_isRelevant;
    }

    /**
     * Returns the tu id of a template part or 0 if a snippet.
     */
    private Long getTuId()
    {
        if (m_part != null)
        {
            return m_part.getTuId();
        }

        return new Long(0L);
    }

    private String getTuv(Long p_id)
    {
        return (String)m_data.m_tuvContents.get(p_id);
    }

    private Snippet getTargetSnippet(Position p_pos)
    {
        return m_data.getTargetSnippet(p_pos);
    }

    //
    // Abstract Output Methods
    //

    /**
     * Returns the value of the tag counter and increments the counter.
     */
    protected final int getCounter()
    {
        return ++m_counter;
    }

    protected void outputSkeleton(String p_string)
    {
        if (m_snippet != null)
        {
            // snippets are not encoded and don't get shown in list view
            if (m_options.getViewMode() == VIEWMODE_TEXT ||
                m_options.getUiMode() == UIMODE_EXPORT)
            {
                m_result.append(EditUtil.encodeHtmlEntities(p_string));
            }
            else if (m_options.getViewMode() == VIEWMODE_PREVIEW)
            {
                m_result.append(p_string);
            }
        }
        else
        {
            // parts are already encoded (see TemplateGenerator)
            m_result.append(p_string);
        }
    }

    protected void outputTuv(String p_string)
    {
        m_result.append(p_string);
    }

    protected void outputAdd()
    {
        int count = getCounter();

        if (m_options.getUiMode() == UIMODE_EDITOR)
        {
            // nothing, but for debugging leave a trace
            if (c_logger.isDebugEnabled())
            {
                m_result.append("<!-- " + count +
                    " SNIPPET POSITION " + m_pos.m_name + " -->\n");
            }
        }
        else if (m_options.getUiMode() == UIMODE_SNIPPETS &&
                 m_options.canAddSnippets())
        {
            m_result.append(
                "<img GSpos='" + count + "'" +
                " src='/globalsight/envoy/edit/snippets/Plus_19x19.gif'" +
                " class='GSposition'" +
                " GSname=\"" +
                EditUtil.encodeHtmlEntities(m_pos.m_name) + "\"" +
                " GSonclick='selectPosition(this);" +
                " event.cancelBubble = true; return false;'" +
                " GSondblclick='addSnippet(this);" +
                " event.cancelBubble = true; return false;'" +
                " TITLE='Click to select this position' />\n");
        }
        else if (m_options.getUiMode() == UIMODE_EXPORT)
        {
            String skel = getSkeleton();
            m_result.append(skel.substring(m_pos.m_start, m_pos.m_end));
        }
        else    // !data.withAddSnippets()
        {
            m_result.append("<!-- " + count +
                " SNIPPET POSITION " + m_pos.m_name + " -->\n");
        }
    }

    protected void outputAddedBegin(Snippet p_snippet)
    {
        int count = getCounter();

        m_snippetCount++;

        if (m_options.getUiMode() == UIMODE_EDITOR)
        {
            if (m_options.getViewMode() == VIEWMODE_LIST)
            {
                m_result.append("<TR><TD>&nbsp;</TD><TD>");
            }

            if (p_snippet.isGeneric())
            {
                m_result.append(
                    "<img GSpos='" + count + "'" +
                    " GSname=\"" +
                    EditUtil.encodeHtmlEntities(m_pos.m_name) + "\"" +
                    " GSversion=\"" + m_pos.m_version + "\"" +
                    " src='/globalsight/envoy/edit/snippets/Plus_19x19.gif'" +
                    " class='GSaddedGeneric'" +
                    " TITLE='Generic snippet (cannot be edited)' />\n");
            }
            else
            {
                m_result.append(
                    "<img GSpos='" + count + "'" +
                    " GSname=\"" +
                    EditUtil.encodeHtmlEntities(m_pos.m_name) + "\"" +
                    " GSversion=\"" + m_pos.m_version + "\"" +
                    " src='/globalsight/envoy/edit/snippets/Plus_19x19.gif'" +
                    " class='GSadded'" +
                    " GSonclick='editSnippet(this);" +
                    " event.cancelBubble = true; return false;'" +
                    " TITLE='Click to edit this snippet' />\n");
            }

            if (m_options.getViewMode() == VIEWMODE_LIST)
            {
                m_result.append("</TD></TR>");
            }
        }
        else if (m_options.getUiMode() == UIMODE_SNIPPETS)
        {
            m_result.append(
                "<div GSpos='" + count + "'" +
                " class='GSadded'" +
                " GSname=\"" +
                EditUtil.encodeHtmlEntities(m_pos.m_name) + "\"" +
                " GSversion=\"" + m_pos.m_version + "\"" +
                " GSonclick='selectAddedSnippet(this);" +
                " event.cancelBubble = true; return false;'" +
                " GSondblclick='editSnippet();" +
                " event.cancelBubble = true; return false;'" +
                " TITLE='Click to select this snippet'>\n");
        }
        else if (m_options.getUiMode() == UIMODE_EXPORT)
        {
            // write a skeleton around the outer snippet only
            if (m_snippetCount == 1)
            {
                m_result.append("<" + DiplomatNames.Element.SKELETON + ">");
            }
            // else nothing - it is an embedded snippet and will be enclosed
        }
        else
        {
            m_result.append("<!-- " + count +
                " ADDED SNIPPET START " + p_snippet.getName() +
                " (" + p_snippet.getId() + ")" + " -->\n");
        }
    }

    protected void outputAddedEnd(Snippet p_snippet)
    {
        if (m_options.getUiMode() == UIMODE_EDITOR)
        {
            // nothing, but for debugging leave a trace
            if (c_logger.isDebugEnabled())
            {
                m_result.append("<!-- ADDED SNIPPET END " +
                    p_snippet.getName() + " " + p_snippet.getId() + " -->\n");
            }
        }
        else if (m_options.getUiMode() == UIMODE_SNIPPETS)
        {
            m_result.append("</div>\n");
        }
        else if (m_options.getUiMode() == UIMODE_EXPORT)
        {
            // only add to the end of the outer snippet
            if (m_snippetCount == 1)
            {
                m_result.append("</" + DiplomatNames.Element.SKELETON + ">");
            }
        }
        else
        {
            m_result.append("<!-- ADDED SNIPPET END " +
                p_snippet.getName() + " " + p_snippet.getId() + " -->\n");
        }

        m_snippetCount--;
    }

    protected void outputAddedNotFound(Position p_pos)
    {
        int count = getCounter();

        if (m_options.getUiMode() == UIMODE_EDITOR)
        {
            if (m_options.getViewMode() == VIEWMODE_LIST)
            {
                m_result.append("<TR><TD>&nbsp;</TD><TD>");
            }

            m_result.append(
                "<img GSpos='" + count + "'" +
                " src='/globalsight/envoy/edit/snippets/unknown_19x19.gif'" +
                " TITLE='SNIPPET " + p_pos.m_name +
                " LOCALE " + m_data.getLocale() +
                " ID " + p_pos.m_version + " NOT FOUND'>\n");

            if (m_options.getViewMode() == VIEWMODE_LIST)
            {
                m_result.append("</TD></TR>");
            }
        }
        else if (m_options.getUiMode() == UIMODE_SNIPPETS)
        {
            m_result.append(
                "<img GSpos='" + count + "'" +
                " src='/globalsight/envoy/edit/snippets/unknown_19x19.gif'" +
                " class='GSposition'" +
                " GSname='' GSversion='0'" +
                " GSonclick='selectAddedSnippet(this);" +
                " event.cancelBubble = true; return false;'" +
                " GSondblclick='removeAddedSnippet();" +
                " event.cancelBubble = true; return false;'" +
                " TITLE='SNIPPET " + p_pos.m_name +
                " LOCALE " + m_data.getLocale() +
                " ID " + p_pos.m_version + " NOT FOUND'>\n");
        }
        else if (m_options.getUiMode() == UIMODE_EXPORT)
        {
            m_result.append("\n<!-- " + count +
                " SNIPPET " + p_pos.m_name +
                " LOCALE " + m_data.getLocale() +
                " ID " + p_pos.m_version + " NOT FOUND -->\n");
        }
        else
        {
            m_result.append("<!-- " + count +
                " SNIPPET " + p_pos.m_name +
                " LOCALE " + m_data.getLocale() +
                " ID " + p_pos.m_version + " NOT FOUND -->\n");
        }
    }

    protected void outputAddedSnippetIsRecursive(Position p_pos,
        Snippet p_snippet)
    {
        int count = getCounter();

        if (m_options.getUiMode() == UIMODE_EDITOR)
        {
            if (m_options.getViewMode() == VIEWMODE_LIST)
            {
                m_result.append("<TR><TD>&nbsp;</TD><TD>");
            }

            m_result.append(
                "<img GSpos='" + count + "'" +
                " src='/globalsight/envoy/edit/snippets/skull_19x19.gif'" +
                " TITLE='SNIPPET " + p_pos.m_name +
                " LOCALE " + m_data.getLocale() +
                " ID " + p_pos.m_version + " IS RECURSIVE: " +
                getRecursionChain(p_snippet) + "'>\n");

            if (m_options.getViewMode() == VIEWMODE_LIST)
            {
                m_result.append("</TD></TR>");
            }
        }
        else if (m_options.getUiMode() == UIMODE_SNIPPETS)
        {
            m_result.append(
                "<img GSpos='" + count + "'" +
                " src='/globalsight/envoy/edit/snippets/skull_19x19.gif'" +
                " class='GSposition'" +
                " GSname='' GSversion='0'" +
                " GSonclick='selectAddedSnippet(this);" +
                " event.cancelBubble = true; return false;'" +
                " GSondblclick='removeAddedSnippet();" +
                " event.cancelBubble = true; return false;'" +
                " TITLE='SNIPPET " + p_pos.m_name +
                " LOCALE " + m_data.getLocale() +
                " ID " + p_pos.m_version + " IS RECURSIVE: " +
                getRecursionChain(p_snippet) + "'>\n");
        }
        else if (m_options.getUiMode() == UIMODE_EXPORT)
        {
            m_result.append("\n<!-- " + count +
                " SNIPPET " + p_pos.m_name +
                " LOCALE " + m_data.getLocale() +
                " ID " + p_pos.m_version + " IS RECURSIVE: " +
                getRecursionChain(p_snippet) + " -->\n");
        }
        else
        {
            m_result.append("<!-- " + count +
                " SNIPPET " + p_pos.m_name +
                " LOCALE " + m_data.getLocale() +
                " ID " + p_pos.m_version + " IS RECURSIVE: " +
                getRecursionChain(p_snippet) + " -->\n");
        }
    }

    protected void outputAddedOtherLocale(Position p_pos)
    {
        // nothing, but for debugging leave a trace
        if (c_logger.isDebugEnabled())
        {
            m_result.append("<!-- ADDED SNIPPET " + p_pos.m_name + " -->\n");
        }
    }

    protected void outputDeleteStart()
    {
        int count = getCounter();

        if (m_options.getUiMode() == UIMODE_EDITOR)
        {
            // nothing, but for debugging leave a trace
            if (c_logger.isDebugEnabled())
            {
                m_result.append("<!-- " + count + " DELETE CONTENT START -->\n");
            }
        }
        else if (m_options.getUiMode() == UIMODE_SNIPPETS)
        {
            m_result.append(
                "<div GSpos='" + count + "'" +
                " class='GSdelete'" +
                " GSonclick='selectDeletableContent(this);" +
                " event.cancelBubble = true; return false;'" +
                " GSondblclick='deleteContent(this);" +
                " event.cancelBubble = true; return false;'" +
                " TITLE='Click to delete this content'>");
        }
        else if (m_options.getUiMode() == UIMODE_EXPORT)
        {
            String skel = getSkeleton();
            m_result.append(skel.substring(m_pos.m_start, m_pos.m_end));
        }
        else
        {
            m_result.append("<!-- " + count + " DELETE CONTENT START -->\n");
        }
    }

    protected void outputDeletedStart()
    {
        int count = getCounter();

        if (m_options.getUiMode() == UIMODE_EDITOR)
        {
            // nothing, but for debugging leave a trace
            if (c_logger.isDebugEnabled())
            {
                m_result.append("<!-- " + count +
                    " DELETED CONTENT START -->\n");
            }
        }
        else if (m_options.getUiMode() == UIMODE_SNIPPETS)
        {
            m_result.append(
                "<img GSpos='" + count + "'" +
                " src='/globalsight/envoy/edit/snippets/Minus_19x19.gif'" +
                " class='GSdeleted'" +
                " GSonclick='selectDeletedContent(this);" +
                " event.cancelBubble = true; return false;'" +
                " GSondblclick='undeleteContent(this);" +
                " event.cancelBubble = true; return false;'" +
                " TITLE='Click to un-delete deleted content' />\n");
        }
        else if (m_options.getUiMode() == UIMODE_EXPORT)
        {
            String skel = getSkeleton();
            m_result.append(skel.substring(m_pos.m_start, m_pos.m_end));
        }
        else
        {
            m_result.append("<!-- " + count + " DELETED CONTENT START -->\n");
        }
    }

    protected void outputDeletedEnd()
    {
        if (m_options.getUiMode() == UIMODE_EXPORT)
        {
            String skel = getSkeleton();
            m_result.append(skel.substring(m_pos.m_start, m_pos.m_end));
        }
        else
        {
            // nothing, but for debugging leave a trace
            if (c_logger.isDebugEnabled())
            {
                m_result.append("<!-- DELETED CONTENT END -->\n");
            }
        }
    }

    protected void outputEndTag()
    {
        if (m_options.getUiMode() == UIMODE_EDITOR)
        {
            // nothing, but for debugging leave a trace
            if (c_logger.isDebugEnabled())
            {
                m_result.append("<!-- GS END TAG -->\n");
            }
        }
        else if (m_options.getUiMode() == UIMODE_SNIPPETS)
        {
            m_result.append("</div>");
        }
        else if (m_options.getUiMode() == UIMODE_EXPORT)
        {
            m_result.append("</" + DiplomatNames.Element.GSA + ">");
        }
        else
        {
            m_result.append("<!-- GS END TAG -->\n");
        }
    }
}
