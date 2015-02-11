/*
Copyright (c) 2000 GlobalSight Corporation. All rights reserved.

THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.

THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
BY LAW.
*/

package com.globalsight.ling.aligner;

import java.util.Properties;
import java.util.LinkedList;
import java.util.Iterator;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;


/**
 * Align the tags of two GXML encoded segments. Compare each tag and
 * if they are equivalent set their x attributes to the same id.
 */
public class SegmentTagsAligner
{   
    private static final REProgram c_removeTagsResult = precompileRegexp();

    private LinkedList m_SourceTagList = null;
    private LinkedList m_TargetTagList = null;     
    private LinkedList m_addableBptList = null;     
    private boolean m_differingX = false;   

    /**
     * SegmentTagsAligner constructor comment.
     */
    public SegmentTagsAligner()
    {
        super();        
        m_addableBptList = new LinkedList();
    }

    private static REProgram precompileRegexp()
    {
        REProgram reProgram = null;

        try
        {
            RECompiler compiler = new RECompiler();
            reProgram = compiler.compile("<(bpt|ept|it|ph|ut)[^>]*>(.|[:space:])*?</(\\1)[:space:]*>");
        }
        catch (RESyntaxException e)
        {
            // SNH (Should Never Happen)
            throw new RuntimeException(e.getMessage());
        }

        return reProgram;
    }

    public String alignTags(String p_source, String p_target)
        throws AlignerException
    {
        m_differingX = false; 

        m_SourceTagList = extractTmxTagPairs(p_source);
        m_TargetTagList = extractTmxTagPairs(p_target);

        // no tags to align
        if (m_SourceTagList.size() == 0 &&
            m_TargetTagList.size() == 0)
        {
            return p_target;
        }

        compareTags();

        if (!areMismatches())
        {
            return insertNewTargetTags(p_target);
        }
        else
        {
            return null;
        }
    }
    
    public String removeUnmatchedTargetTags(String p_source, String p_target)
        throws AlignerException
    {
        m_differingX = false;      
        
        m_SourceTagList = extractTmxTagPairs(p_source);
        m_TargetTagList = extractTmxTagPairs(p_target);  
        m_addableBptList.clear();
        
        // no tags in the target, just return it.
        if (m_TargetTagList.size() == 0)
        {
            return p_target;
        }
        
        compareTags();
        
        findAndMarkAmbiguousTags();
             
        return removeUnmatched(p_target);
    }
    
    private void findAndMarkAmbiguousTags()
    {        
        int startIndex = 1;
        int index;
        Iterator targetIterator = m_TargetTagList.iterator();
        while(targetIterator.hasNext())
        {            
            TmxTag currentTag = (TmxTag)targetIterator.next();                                               
            index = startIndex;
            startIndex++;        
            
            // All addables are keepers 
            if (currentTag.isAddable())
            {
                m_addableBptList.add(currentTag);   
                currentTag.setMatched();
                continue;
            }
            
            // find the ept's mate if it's a keeper then we
            // keep the ept too
            if (currentTag.isEpt())
            {
                if (!keepEpt(currentTag))
                {
                    currentTag.setUnMatched();
                }                
                continue;
            }
            
            // already found a match - skip
            if (currentTag.isDuplicate())
            {
                continue;
            }                     
            
            // add all bpt's to this list to search later.
            if (currentTag.isBpt())
            {
                m_addableBptList.add(currentTag);                            
            }
            
            // find all matching tags for currentTag
            while (index < m_TargetTagList.size())
            {
                TmxTag compareTag = (TmxTag)m_TargetTagList.get(index);
                if (currentTag.equals(compareTag))
                {
                    currentTag.setDuplicate();
                    compareTag.setDuplicate();                  
                }
                index++;                
            }
        }
    }
    
    private boolean keepEpt(TmxTag p_tag)
    {        
        Iterator bptIterator = m_addableBptList.iterator();
        while(bptIterator.hasNext())
        {            
            TmxTag bptTag = (TmxTag)bptIterator.next(); 
            if (bptTag.getIValue().equals(p_tag.getIValue()))
            {                                
                bptIterator.remove();                
                if (!bptTag.isDuplicate() && bptTag.isMatched())
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }
        
        return false;
    }
    
    private String removeUnmatched(String p_target)
        throws AlignerException
    {
        StringBuffer tempTarget = new StringBuffer(p_target);

        // go backwards through tag list
        while (!m_TargetTagList.isEmpty())
        {
            TmxTag tag = (TmxTag)m_TargetTagList.removeLast();

            tempTarget.delete(tag.getInsertionPoint(),
                tag.getInsertionPoint() + tag.getLength());

            if ((tag.isMatched() && !tag.isDuplicate()))
            {
                tempTarget.insert(tag.getInsertionPoint(), tag.getString());
            }
        }

        return tempTarget.toString();    
    }
    
    private LinkedList extractTmxTagPairs(String p_GxmlSegment)
        throws AlignerException
    {
        int start = 0;
        String tagMatch = null;
        LinkedList tagList = new LinkedList();
        TmxTag tag = null;

        // matches this:
        // "<(bpt|ept|it|ph|ut)[^>]+>(.|[:space:])*?</(\\1)[:space:]*>",
        // RE.MATCH_NORMAL
        RE matcher = new RE(c_removeTagsResult, RE.MATCH_NORMAL);
        while (matcher.match(p_GxmlSegment, start))
        {
            tagMatch = matcher.getParen(0);
            start = matcher.getParenEnd(0);
            tag = new TmxTag(tagMatch, matcher.getParenStart(0));

            // we assume all ept's have bpt mates so we ignore the
            // ept's for matching
            if (tag.isEpt())
            {
                tag.setMatched();
            }
            
            tagList.add(tag);
        }

        return tagList;
    }

    private void compareTags()
    {
        Iterator targetIterator = m_TargetTagList.iterator();
        TmxTag tag = null;

        // compare each target tag to all source tags until we get a match
        while (targetIterator.hasNext())
        {
            tag = (TmxTag)targetIterator.next();
            findMatchAndMark(tag);
        }
    }

    private void findMatchAndMark(TmxTag p_targetTag)
    {
        Iterator sourceIterator = m_SourceTagList.iterator();
        TmxTag tag = null;

        // compare each target tag to source tags until we get a match
        while (sourceIterator.hasNext())
        {
            tag = (TmxTag)sourceIterator.next();

            // skip if already matched
            if (!tag.isMatched())
            {
                if (tag.equals(p_targetTag))
                {
                    tag.setMatched();
                    p_targetTag.setMatched();

                    // we don't add x to ept elements
                    if (!tag.getName().equals("ept"))
                    {
                        if (!tag.getXValue().equals(p_targetTag.getXValue()))
                        {
                            if (!tag.isAddable())
                            {
                                m_differingX = true;
                            }
                        }

                        p_targetTag.setXValue(tag.getXValue());
                    }

                    // we've found our match
                    break;
                }
            }
        }
    }

    private boolean areMismatches()
    {
        // look for mismatches but ignore erasables (we assume these
        // are addables)

        Iterator sourceIterator = m_SourceTagList.iterator();
        while (sourceIterator.hasNext())
        {
            TmxTag tag = (TmxTag)sourceIterator.next();
            if (!tag.isMatched() && !tag.isAddable())
            {
                return true;
            }

        }

        Iterator targetIterator = m_TargetTagList.iterator();
        while(targetIterator.hasNext())
        {
            TmxTag tag = (TmxTag)targetIterator.next();
            if (!tag.isMatched() && !tag.isAddable())
            {
                return true;
            }
        }


        return false;
    }

    private String insertNewTargetTags(String p_target)
        throws AlignerException
    {
        StringBuffer tempTarget = new StringBuffer(p_target);

        // go backwards through tag list
        while (!m_TargetTagList.isEmpty())
        {
            TmxTag tag = (TmxTag)m_TargetTagList.removeLast();

            tempTarget.delete(tag.getInsertionPoint(),
                tag.getInsertionPoint() + tag.getLength());

            tempTarget.insert(tag.getInsertionPoint(), tag.getString());
        }

        return tempTarget.toString();
    }

    /**
     * Did we find a matching tag with a different X value?
     */
    public boolean isDifferingX()
    {
        return m_differingX;
    }
}
