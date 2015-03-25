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
package com.globalsight.ling.tw;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.cxe.adapter.openoffice.StringIndex;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.SegmentUtil;

/**
 * PTag error checker.
 */
public class PseudoErrorChecker implements PseudoBaseHandler
{
    private boolean m_bHasError = false;
    private PseudoData m_PseudoData;
    private boolean m_isSource = false;
    private String m_mockSource = "";
    private String m_mockTarget = "";
    private HashMap<String, String> m_InternalTags = null;
    private String m_oriMockTarget = "";
    private Vector m_targetTexts = new Vector();
    private String m_newTarget = "";
    private Vector m_TrgTagList = new Vector();
    private boolean m_tagsIsOk = false;
    private String m_strErrMsg = "";
    private String m_strInternalErrMsg = "";
    private boolean m_escapeResult = false;
    private ResourceBundle m_resources = null;
    private Locale m_locale = null;
    private static final String RESPATH = "com.globalsight.ling.tw.ErrorChecker";
    private static final String ESCAPABLE_CHARS = "<>&'\"";
    private static final String LT = "&lt;";
    private static final String GT = "&gt;";
    private static final String AMP = "&amp;";
    private static final String APOS = "&apos;";
    private static final String QUOT = "&quot;";
    private static final String[] ESCAPED_STRINGS =
    { LT, GT, AMP, APOS, QUOT };

    // Source with full subflows expanded.
    String m_sourceWithSubContent = null;
    // Max length for GXML string - storage in our DB
    private int m_gxmlMaxLen = 0;
    // Encoding accociated with GXML string length - storage in our DB
    private String m_encodingGxmlMaxLen = null;

    // Max length for NATIVE content - storage in client DB - for DB
    // jobs
    private int m_nativeContentMaxLen = 0;
    // Encoding accociated with NATIVE content - storage in client DB
    // - for DB jobs
    private String m_encodingNativeContentMaxLen = null;

    private String styles;
    private SegmentUtil segmentUtil;

    private static Pattern P1 = Pattern.compile("\\[([^/][^\\[]*)\\]");

    private static List<String> MOVABLE_TAG = new ArrayList<String>();
    static
    {
        MOVABLE_TAG.add("b");
        MOVABLE_TAG.add("i");
        MOVABLE_TAG.add("u");
        MOVABLE_TAG.add("sub");
        MOVABLE_TAG.add("sup");

        MOVABLE_TAG.add("bold");
        MOVABLE_TAG.add("italic");
        MOVABLE_TAG.add("underline");
        MOVABLE_TAG.add("subscript");
        MOVABLE_TAG.add("superscript");
    }

    /**
     * Constructor - uses default locale for error messages.
     */
    public PseudoErrorChecker()
    {
        super();
    }

    /**
     * Resets the this error checker.
     */
    public void reset()
    {
        m_PseudoData.resetAllSourceListNodes();
        m_TrgTagList = new Vector();
        m_bHasError = false;
        m_strErrMsg = "";
        m_mockSource = "";
        m_mockTarget = "";
        m_targetTexts = new Vector();
        m_newTarget = "";
        m_strInternalErrMsg = "";
        /*
         * Do not reset the following m_bVerifyLeadingWhiteSpace
         * m_bVerifyTrailingWhiteSpace m_PseudoData
         */
    }

    /**
     * Get a new target string after fixing moved tag for Office-XML
     * 
     * @return the new target String, or "" if do nothing
     */
    public String getNewTarget()
    {
        if (!isTagUnmovableFormat())
        {
            return "";
        }

        if (m_oriMockTarget.equals(m_mockTarget))
        {
            return "";
        }

        StringBuffer result = new StringBuffer();
        int tCount = 0;
        for (int i = 0; i < m_mockTarget.length(); i++)
        {
            char c = m_mockTarget.charAt(i);

            if (c == 'T')
            {
                String text = (String) m_targetTexts.get(tCount);
                result.append(text);
                tCount++;
            }
            else if (c == 'E')
            {
                continue;
            }
            else
            {
                result.append(c);
            }
        }

        return result.toString();
    }

    /**
     * Checks to see if there was an error during the last verification attempt.
     * 
     * @return true if there was an error, otherwise false.
     */
    public boolean hasError()
    {
        return m_bHasError;
    }

    /**
     * Pseudo Parser event handler.
     * 
     * @param originalString
     *            - the full token
     */
    public void processTag(String p_strTagName, String p_originalString)
    {
        if (m_isSource)
        {
            m_mockSource += p_originalString;
        }
        else
        {
            m_TrgTagList.addElement(p_strTagName);
            m_mockTarget += p_originalString;
        }
    }

    public void processText(String p_strText)
    {
        if (m_isSource)
        {
            m_mockSource += isEmpty(p_strText) ? "E" : "T";
        }
        else
        {
            m_mockTarget += isEmpty(p_strText) ? "E" : "T";

            if (!isEmpty(p_strText))
            {
                m_targetTexts.add(p_strText);
            }
        }
    }

    private boolean isEmpty(String str)
    {
        if (str == null || "".equals(str) || str.length() == 0)
        {
            return true;
        }

        // end of page: #8234 #8236
        if (str.length() == 2 && 8234 == (int) str.charAt(0)
                && 8236 == (int) str.charAt(1))
        {
            return true;
        }

        return false;
    }

    /**
     * Performs three levels of error checking on the PseudoData provided.
     * <p>
     * 1) Detects illegal or missing tag names.
     * <p>
     * 2) Detects a malformed tag sequence.
     * <p>
     * 3) Detects illegal tag positions (NOT IMPLIMENTED FOR THIS RELEASE).
     * <p>
     * 4) Detects invalid XML characters NOTE: No max length check is performed.
     * 
     * @param p_PData
     *            - an up to date PsuedoData object.
     */
    public String check(PseudoData p_PData) throws PseudoParserException
    {
        m_PseudoData = p_PData;

        this.reset(); // must reset after m_PseudoData is set

        // only re-load resources if there is a new PData locale.
        if ((m_locale == null) || !m_locale.equals(p_PData.getLocale()))
        {
            try
            {
                m_locale = p_PData.getLocale();
                m_resources = ResourceBundle.getBundle(RESPATH, m_locale);
            }
            catch (Exception e)
            {
                System.out.println(e.toString());
            }
        }

        if (notCountWordsValid())
        {
            return m_strErrMsg;
        }

        buildTrgTagList();
        buildSrcTagList();

        try
        {
            // level #1 - Check mandatory and invalid tags
            if (!isTrgTagListValid())
            {
                return m_strErrMsg;
            }

            // level #2 - Check well formedness - very basic
            if (!isTrgTagListWellFormed())
            {
                return m_strErrMsg;
            }

            // level #3 - Check Rule based movement
            /*
             * if ( !isMoveValid() ) { return m_strErrMsg; }
             */

            // level #4 - Check for invalid XML characters
            if (!isCharactersValid())
            {
                return m_strErrMsg;
            }

            if (!checkStyleTag(m_mockSource, m_mockTarget))
            {
                return m_strErrMsg;
            }

            // check if tags are moved for special format like office 2010
            // phase 1, just apply to office 2010
            if (isTagUnmovableFormat() && isTagMoved())
            {
                return m_strErrMsg;
            }

            String newTarget = getNewTarget();
            if (!"".equals(newTarget))
            {
                m_PseudoData.setPTagTargetString(newTarget);
            }
        }
        catch (TagNodeException e)
        {
            throw new PseudoParserException(e.toString());
        }

        return null;
    }

    private boolean checkStyleTag(String s1, String s2)
    {
        s1 = s1.replaceAll("[TE]+", "");
        s2 = s2.replaceAll("[TE]+", "");

        Map<String, List<String>> t1 = getInsideTags(s1);
        Map<String, List<String>> t2 = getInsideTags(s2);

        for (String s : t2.keySet())
        {
            List<String> ts2 = t2.get(s);

            if (ts2.size() > 0)
            {
                List<String> errorTag = new ArrayList<String>();

                List<String> ts1 = t1.get(s);

                for (String t : ts2)
                {
                    if (ts1 == null || !ts1.contains(t))
                        errorTag.add(t);
                }

                if (errorTag.size() > 0)
                {
                    String errorTagString = errorTag.toString();
                    errorTagString = errorTagString.substring(1,
                            errorTagString.length() - 1);

                    m_strErrMsg = MessageFormat.format(
                            m_resources.getString("ErrorTagInside"),
                            errorTagString, s);

                    return false;
                }
            }
        }

        return true;
    }

    private Map<String, List<String>> getInsideTags(String s)
    {
        List<String> tags = new ArrayList<String>();
        // tags.add("bold");
        tags.add("color");
        tags.add("hyperlink");
        // tags.add("italic");
        // tags.add("superscript");
        // tags.add("underline");

        Map<String, List<String>> insideTags = new HashMap<String, List<String>>();

        for (String tag : tags)
        {
            Pattern p = Pattern.compile("\\[(" + tag + "[^\\]]*)\\]");
            Matcher m = p.matcher(s);

            while (m.find())
            {
                String t = m.group(1);
                String content = getContent(s, t);
                List<String> ts = getAllTags(content);

                insideTags.put(t, ts);
            }
        }

        return insideTags;
    }

    private String getContent(String s, String tag)
    {
        Pattern p = Pattern.compile("\\[" + tag + "[^\\]]*\\](.*?)\\[/" + tag
                + "[^\\]]*\\]");
        Matcher m = p.matcher(s);

        while (m.find())
        {
            return m.group(1);
        }
        return null;
    }

    private List<String> getAllTags(String s)
    {
        List<String> tags = new ArrayList<String>();
        if (s == null || "".equals(s.trim()))
        {
            return tags;
        }

        Pattern p = Pattern.compile("\\[[^\\]]*\\]");
        Matcher m = p.matcher(s);

        while (m.find())
        {
            tags.add(m.group());
        }

        return tags;
    }

    /**
     * Moves all tags that the content included int, but [[] will not be moved.
     * <p>
     * 
     * For example, <code>[a1][[a1][b]</code> while be changed to [[a1][b].
     * 
     * @param src
     * @return
     */
    private String moveBrackets(String src)
    {
        String regex = "\\[[^\\]]*?\\d+\\]";
        String s = Double.toString(Math.random());
        int n = src.indexOf(s);
        while (n > -1)
        {
            s = Double.toString(Math.random());
            n = src.indexOf(s);
        }

        src = src.replaceAll("\\[\\[", s);

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(src);
        StringBuffer sb = new StringBuffer();
        while (m.find())
        {
            m.appendReplacement(sb, "");
        }
        m.appendTail(sb);

        src = sb.toString();
        src = src.replaceAll(s, "[[");

        return src;
    }

    /**
     * Some words are signed to not translate, validate all of them changed or
     * not.
     * 
     * @return
     */
    private boolean notCountWordsValid()
    {
        boolean changed = false;
        String target = m_PseudoData.getPTagTargetString();
        String src = m_sourceWithSubContent;
        if (src.length() < 1)
        {
            src = m_PseudoData.getPTagSourceString();

            // for offline upload
            Hashtable map = m_PseudoData.getPseudo2TmxMap();
            if (map != null && !map.isEmpty())
            {
                Set keys = map.keySet();
                Iterator iterator = keys.iterator();
                while (iterator.hasNext())
                {
                    String key = (String) iterator.next();
                    String value = (String) map.get(key);
                    src = StringUtil.replace(src, "[" + key + "]", value);
                }
            }
        }

        List notCountWords = getSegmentUtil().getNotTranslateWords(src);

        // Judge.
        target = moveBrackets(target);
        List changedWords = new ArrayList();
        String targetString = new String(target);
        for (int i = 0; i < notCountWords.size(); i++)
        {
            String words = (String) notCountWords.get(i);
            int index = targetString.indexOf(words);
            if (index < 0)
            {
                changedWords.add(words);
                changed = true;
            }
            else
            {
                targetString = targetString.substring(0, index)
                        + targetString.substring(index + words.length());
            }
        }

        // Some word changed.
        if (changedWords.size() > 0)
        {
            String words = changedWords.toString();
            words = words.substring(1, words.length() - 1);
            m_strErrMsg = MessageFormat.format(
                    m_resources.getString("ErrorConstantChanged"), new String[]
                    { words });
        }

        return changed;
    }

    /**
     * Convert all the escapable characters in the given string into their
     * string (escaped) equivalents. Return the converted string.
     * 
     * @param p_str
     *            the string to be modified.
     * 
     * @return the escaped result.
     */
    private String escapeString(String p_str)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < p_str.length(); i++)
        {
            char c = p_str.charAt(i);
            int index = ESCAPABLE_CHARS.indexOf((int) c);
            if (index > -1)
            {
                sb.append(ESCAPED_STRINGS[index]);
            }
            else
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Convert all the escaped strings in the given string into their character
     * (unescaped) equivalents. Return the converted string.
     * 
     * @param p_str
     *            the string to be modified.
     * 
     * @return the escaped result.
     */
    private String unescapeString(String p_str)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < p_str.length(); i++)
        {
            char c = p_str.charAt(i);
            boolean found = false;
            if (c == '&')
            {
                for (int j = 0; !found && j < ESCAPED_STRINGS.length; j++)
                {
                    String s = ESCAPED_STRINGS[j];
                    int index = p_str.indexOf(s, i);
                    if (found = (index == i))
                    {
                        sb.append(ESCAPABLE_CHARS.charAt(j));
                        i += (s.length() - 1);
                    }
                }
            }

            if (!found)
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * <p>
     * Validates all the target tag names against the source tag names.
     * </p>
     * 
     * <p>
     * This is the lowest level test that is performed first. Basically, this
     * test will:
     * </p>
     * 
     * 1. Validate that all the mandatory (non-erasable) tags in the source also
     * appear in the target exactly one time each.
     * <p>
     * 2. Any additional tags that do not appear in the source must be of the
     * addable type. Right now, this is bold, underline and italic.
     * <p>
     * 3. Builds one combined error, which reports all missing and illegal tags.
     * <p>
     * 
     * NOTE: This first level of error checking replaces each object in the
     * target list with a TagNode object - mapped over from the source list.
     * 
     * @return False if there are missing or illegal names in the target. True
     *         if all tags are valid.
     */
    private boolean isTrgTagListValid() throws TagNodeException
    {
        // for special tag []
        if ("[]".equals(m_PseudoData.getPTagSourceString())
                && m_TrgTagList != null && m_TrgTagList.size() == 1
                && "".equals(m_TrgTagList.get(0)))
        {
            m_tagsIsOk = true;
            return true;
        }

        // simple tags
        String strTrgTagName;
        String invalidNames = "";
        String missingNames = "";
        String missingInternalNames = "";
        TagNode firstUnusedErasable = null;
        TagNode firstUnusedNonErasable = null;
        boolean bHasMissing = false;
        boolean bHasInvalid = false;
        List<TagNode> sourceTagList = new ArrayList<TagNode>();
        Vector srcTagList = m_PseudoData.getSrcCompleteTagList();
        List<TagNode> mtIdentifierList = m_PseudoData.getMTIdentifierList();
        for (Object o : srcTagList)
        {
            sourceTagList.add((TagNode) o);
        }
        for (TagNode node : mtIdentifierList)
        {
            sourceTagList.add(node);
        }
        Enumeration trgEnumerator = m_TrgTagList.elements();
        int nInvalidCnt = 0;
        int nMissingCnt = 0;
        int nMissingInternalCnt = 0;

        // search for illegal names
        while (trgEnumerator.hasMoreElements())
        {
            strTrgTagName = (String) trgEnumerator.nextElement();
            firstUnusedErasable = firstUnusedNonErasable = null;

            // search the source tags for the first two occurances of
            // the item, once as erasable and once as non-erasable
            for (TagNode srcItem : sourceTagList)
            {
                if ((firstUnusedErasable != null)
                        && (firstUnusedNonErasable != null))
                {
                    break;
                }

                if (srcItem.isMapped())
                    continue;

                String srcItemTag = srcItem.getPTagName();
                boolean isEquals = srcItemTag.equalsIgnoreCase(strTrgTagName);
                String encodeTrg = strTrgTagName;
                if (!isEquals && TagNode.INTERNAL.equals(srcItem.getTmxType()))
                {
                    encodeTrg = escapeString(strTrgTagName);
                    isEquals = encodeTrg.equalsIgnoreCase(srcItemTag);
                }

                while (!isEquals && encodeTrg.startsWith("[[")
                        && encodeTrg.endsWith("]"))
                {
                    encodeTrg = encodeTrg.substring(2, encodeTrg.length() - 1);
                    isEquals = encodeTrg.equalsIgnoreCase(srcItemTag);
                }

                // make i1 == i, nbsp1 == nbsp
                if (checkTagName(srcItemTag, strTrgTagName))
                {
                    if (!isEquals && strTrgTagName.startsWith(srcItemTag))
                    {
                        String temp = strTrgTagName.substring(srcItemTag
                                .length());
                        try
                        {
                            long tt = Long.parseLong(temp);
                            isEquals = true;
                        }
                        catch (Exception e)
                        {
                            isEquals = false;
                        }
                    }
                    if (!isEquals && srcItemTag.startsWith(strTrgTagName))
                    {
                        String temp = srcItemTag.substring(strTrgTagName
                                .length());
                        try
                        {
                            long tt = Long.parseLong(temp);
                            isEquals = true;
                        }
                        catch (Exception e)
                        {
                            isEquals = false;
                        }
                    }
                }
                
                // for xliff 2.0 offline upload
                // for example [b] is changed to [g1]
                if (MOVABLE_TAG.contains(srcItemTag) && strTrgTagName.startsWith("g"))
                {
                    String temp = strTrgTagName.substring(1);
                    try
                    {
                        long tt = Long.parseLong(temp);
                        Hashtable atts = srcItem.getAttributes();
                        
                        String i = (String) atts.get("i");
                        if (i == null)
                        {
                            i = (String) atts.get("x");
                        }
                        
                        if (i != null && tt == Long.parseLong(i))
                        {
                            isEquals = true;
                        }
                    }
                    catch (Exception e)
                    {
                        isEquals = false;
                    }
                }
                
                // for xliff 2.0 offline upload
                // for example [/b] is changed to [/g1]
                if (srcItemTag.startsWith("/") && strTrgTagName.startsWith("/"))
                {
                    String src = srcItemTag.substring(1);
                    String trg = strTrgTagName.substring(1);
                    
                    if (MOVABLE_TAG.contains(src) && trg.startsWith("g"))
                    {
                        String temp = trg.substring(1);
                        try
                        {
                            long tt = Long.parseLong(temp);
                            Hashtable atts = srcItem.getAttributes();
                            
                            String i = (String) atts.get("i");
                            if (i == null)
                            {
                                i = (String) atts.get("x");
                            }
                            
                            if (i != null && tt == Long.parseLong(i))
                            {
                                isEquals = true;
                            }
                        }
                        catch (Exception e)
                        {
                            isEquals = false;
                        }
                    }
                }

                if (isEquals)
                {
                    String erasable = (String) srcItem.getAttributes().get(
                            "erasable");

                    if ((erasable == null)
                            || erasable.toLowerCase().equals("no"))
                    {
                        if (firstUnusedNonErasable == null)
                        {
                            firstUnusedNonErasable = srcItem;
                        }
                    }
                    else if (erasable.toLowerCase().equals("yes"))
                    {
                        if (firstUnusedErasable == null)
                        {
                            firstUnusedErasable = srcItem;
                        }
                    }
                }
            }

            // If an unmapped match was found in the source, mark the
            // first unused nonerasable one as being mapped.
            // Otherwise, mark the first unsed earasable one. If
            // neither openings are found, check if it is addable.
            if (firstUnusedNonErasable != null)
            {
                firstUnusedNonErasable.setMapped(true);

                // replace target element with source element that we
                // mapped to.
                m_TrgTagList.setElementAt(firstUnusedNonErasable,
                        m_TrgTagList.indexOf(strTrgTagName));
            }
            else if (firstUnusedErasable != null)
            {
                firstUnusedErasable.setMapped(true);

                // replace target element with source element that we
                // mapped to.
                m_TrgTagList.setElementAt(firstUnusedErasable,
                        m_TrgTagList.indexOf(strTrgTagName));
            }
            else if (m_PseudoData.isAddableAllowed())
            {
                // search addable tags (when allowed)
                String mapKey = m_PseudoData.isAddableTag(strTrgTagName);
                if (mapKey == null)
                {
                    bHasInvalid = true;
                    nInvalidCnt += 1;

                    if (invalidNames.length() > 0)
                    {
                        invalidNames += ", ";
                    }
                    if ((nInvalidCnt % 5) == 0)
                    {
                        invalidNames += "\n\t";
                    }

                    invalidNames += PseudoConstants.PSEUDO_OPEN_TAG
                            + strTrgTagName + PseudoConstants.PSEUDO_CLOSE_TAG;
                }
                else
                {
                    // build a virtual source element to map to, then
                    // replace target-list element.
                    PseudoOverrideMapItem POMI = m_PseudoData
                            .getOverrideMapItem(mapKey);

                    String tmxName;
                    if (strTrgTagName.startsWith("/"))
                    {
                        tmxName = (String) POMI.m_hAttributes
                                .get(PseudoConstants.ADDABLE_TMX_ENDPAIRTAG);
                    }
                    else
                    {
                        tmxName = (String) POMI.m_hAttributes
                                .get(PseudoConstants.ADDABLE_TMX_TAG);
                    }

                    if (tmxName == null)
                    {
                        bHasInvalid = true;
                        nInvalidCnt += 1;

                        if (invalidNames.length() > 0)
                        {
                            invalidNames += ", ";
                        }
                        if ((nInvalidCnt % 10) == 0)
                        {
                            invalidNames += "\n\t";
                        }
                        invalidNames += PseudoConstants.PSEUDO_OPEN_TAG
                                + strTrgTagName
                                + PseudoConstants.PSEUDO_CLOSE_TAG;
                    }
                    else
                    {
                        TagNode dynamicMapItem = new TagNode(tmxName,
                                strTrgTagName, POMI.m_hAttributes);

                        m_TrgTagList.setElementAt(dynamicMapItem,
                                m_TrgTagList.indexOf(strTrgTagName));
                    }
                }
            }
            else
            // when addables are not allowed
            {
                bHasInvalid = true;
                nInvalidCnt += 1;

                if (invalidNames.length() > 0)
                {
                    invalidNames += ", ";
                }
                if ((nInvalidCnt % 10) == 0)
                {
                    invalidNames += "\n\t";
                }
                invalidNames += PseudoConstants.PSEUDO_OPEN_TAG + strTrgTagName
                        + PseudoConstants.PSEUDO_CLOSE_TAG;
            }
        }

        // search for missing tags that are non-erasable
        for (TagNode srcItem : sourceTagList)
        {
            String erasable = (String) srcItem.getAttributes().get("erasable");

            if (!srcItem.isMapped()
                    && (erasable == null || erasable.toLowerCase().equals("no")))
            {
                if (TagNode.INTERNAL.equals(srcItem.getTmxType())
                        && !srcItem.isMTIdentifier())
                {
                    nMissingInternalCnt += 1;

                    if (missingInternalNames.length() > 0)
                    {
                        missingInternalNames += ", ";
                    }
                    if ((nMissingInternalCnt % 10) == 0)
                    {
                        missingInternalNames += "\n\t";
                    }

                    String tag = srcItem.getPTagName();

                    missingInternalNames += PseudoConstants.PSEUDO_OPEN_TAG
                            + tag + PseudoConstants.PSEUDO_CLOSE_TAG;
                    ;
                }
                else
                {
                    String tag = srcItem.getPTagName();
                    String tag2 = PseudoConstants.PSEUDO_OPEN_TAG + tag
                            + PseudoConstants.PSEUDO_CLOSE_TAG;
                    boolean isInsideSubTag = false;
                    if ("sub".equals(srcItem.getAttributes().get("type")))
                    {
                        Hashtable nativeMap = m_PseudoData
                                .getPseudo2NativeMap();
                        Iterator nativeMapvalues = nativeMap.values()
                                .iterator();

                        while (nativeMapvalues.hasNext())
                        {
                            String vvv = (String) nativeMapvalues.next();
                            if (vvv.contains(tag2))
                            {
                                isInsideSubTag = true;
                                break;
                            }
                        }
                    }

                    if (!isInsideSubTag)
                    {
                        bHasMissing = true;
                        nMissingCnt += 1;

                        if (missingNames.length() > 0)
                        {
                            missingNames += ", ";
                        }
                        if ((nMissingCnt % 10) == 0)
                        {
                            missingNames += "\n\t";
                        }

                        missingNames += tag2;
                    }
                }
            }
        }

        // Build error message
        if (bHasMissing || bHasInvalid)
        {
            if (bHasMissing && !bHasInvalid)
            {
                // ErrorMissingTags
                String[] args =
                { missingNames };
                m_strErrMsg = MessageFormat.format(
                        m_resources.getString("ErrorMissingTags"), args);
            }
            else if (!bHasMissing && bHasInvalid)
            {
                // ErrorInvalidAdd
                String[] args =
                { invalidNames };
                m_strErrMsg = MessageFormat.format(
                        m_resources.getString("ErrorInvalidAdd"), args);
            }
            else
            {
                // combined error
                String[] args =
                { missingNames };
                String s1 = MessageFormat.format(
                        m_resources.getString("ErrorMissingTags"), args);
                args = new String[]
                { invalidNames };
                String s2 = MessageFormat.format(
                        m_resources.getString("ErrorInvalidAdd"), args);

                m_strErrMsg = s1 + "\n" + s2;
            }

            return false;
        }
        else
        {
            String[] args =
            { missingInternalNames };
            m_strInternalErrMsg = missingInternalNames;
        }

        m_PseudoData.resetAllSourceListNodes();
        return true;
    }

    // check if the srcTag and trgTag are start with "i" or "nbsp".
    private boolean checkTagName(String srcItemTag, String strTrgTagName)
    {
        if (srcItemTag.startsWith("i") && strTrgTagName.startsWith("i"))
        {
            return true;
        }

        if (srcItemTag.startsWith("nbsp") && strTrgTagName.startsWith("nbsp"))
        {
            return true;
        }

        return false;
    }

    /**
     * <p>
     * Builds the Pseudo tag list for the target string.
     * </p>
     * 
     * <p>
     * NOTE: Later, during the first level of error checking, each object in the
     * list gets replaced with a TagNode object as it is mapped to the source.
     * </p>
     */
    private void buildTrgTagList() throws PseudoParserException
    {
        m_isSource = false;
        PseudoParser parser = new PseudoParser(this);
        parser.tokenize(m_PseudoData.getWrappedPTagTargetString());
        return;
    }

    /**
     * <p>
     * Builds the Pseudo tag list for the source string.
     * </p>
     * 
     */
    private void buildSrcTagList() throws PseudoParserException
    {
        m_isSource = true;
        PseudoParser parser = new PseudoParser(this);
        parser.tokenize(m_PseudoData.getWrappedPTagSourceString());
        return;
    }

    /**
     * Verifies the position of each tag against the source tree.
     * 
     * !!! Not implemented in this release.
     * 
     * @return - true if succeeds, otherwise false.
     */
    private boolean isTrgPositionValid()
    {
        m_PseudoData.resetAllSourceListNodes();
        return true;
    }

    /**
     * Verifies that a Pseudo tagged string is well formed.
     * 
     * @return true if well formed, otherwise false.
     */
    private boolean isTrgTagListWellFormed()
    {
        if (m_tagsIsOk)
        {
            return true;
        }

        boolean hasError = false;
        String unbalancedTags = "";
        int size = m_TrgTagList.size();

        m_PseudoData.resetAllSourceListNodes();

        // Match open-close pairs
        for (int i = 0; i < size; i++)
        {
            TagNode curNode = (TagNode) m_TrgTagList.elementAt(i);
            String curPTag = curNode.getPTagName();

            // skip nodes that have already been matched or are not paired.
            if (curNode.isMapped() || !curNode.isPaired())
            {
                if (!curNode.isPaired())
                {
                    curNode.setMapped(true);
                }

                continue;
            }
            else
            {
                // search forward from current index for an unused end
                // tag of the same type. Mark both matching tags as
                // mapped.
                for (int j = i + 1; j < size; j++)
                {
                    TagNode searchNode = (TagNode) m_TrgTagList.elementAt(j);
                    String searchTag = searchNode.getPTagName();
                    if (searchTag.startsWith(String
                            .valueOf(PseudoConstants.PSEUDO_END_TAG_MARKER)))
                    {
                        if (!searchNode.isMapped()
                                && searchNode.isPaired()
                                && curPTag.toLowerCase().equals(
                                        searchTag.substring(1).toLowerCase()))
                        {
                            curNode.setMapped(true);
                            searchNode.setMapped(true);
                            break;
                        }
                    }
                }
            }
        }

        // Traverse again looking for any unmapped nodes.
        for (int i = 0; i < size; i++)
        {
            TagNode searchNode = (TagNode) m_TrgTagList.elementAt(i);
            if (!searchNode.isMapped())
            {
                if (m_PseudoData.isXliffXlfFile())
                {
                    String ptname = searchNode.getPTagName();
                    String tType = searchNode.getTmxType();

                    if ("bpt".equals(tType) || "ept".equals(tType))
                    {
                        continue;
                    }
                }

                hasError = true;

                if (unbalancedTags.length() > 0)
                {
                    unbalancedTags += ", ";
                }

                unbalancedTags += PseudoConstants.PSEUDO_OPEN_TAG
                        + searchNode.getPTagName()
                        + PseudoConstants.PSEUDO_CLOSE_TAG;
            }
        }

        // report errors
        if (hasError)
        {
            // ErrorUnbalancedTags
            String[] args =
            { unbalancedTags };
            m_strErrMsg = MessageFormat.format(
                    m_resources.getString("ErrorUnbalancedTags"), args);

            return false;
        }

        return true;
    }

    /**
     * Performs four levels of error checking on the PseudoData provided.
     * <p>
     * 1) Detects illegal or missing tag names.
     * <p>
     * 2) Detects a malformed tag sequence.
     * <p>
     * 3) Detects illegal tag positions (NOT IMPLIMENTED FOR THIS RELEASE).
     * <p>
     * 4) Detects invalid XML characters 5) Detects segments exceeding the
     * maximum lengths.
     * 
     * @param p_PData
     *            - an up to date PsuedoData object.
     */
    public String check(PseudoData p_PData, String p_sourceWithSubContent,
            int p_gxmlMaxLen, String p_gxmlStorageEncoding,
            int p_nativeContentMaxLen, String p_nativeStorageEncoding)
            throws PseudoParserException
    {
        // new data needed for length validation
        m_sourceWithSubContent = p_sourceWithSubContent;
        m_gxmlMaxLen = p_gxmlMaxLen;
        m_encodingGxmlMaxLen = p_gxmlStorageEncoding;
        m_nativeContentMaxLen = p_nativeContentMaxLen;
        m_encodingNativeContentMaxLen = p_nativeStorageEncoding;

        try
        {
            String errMsg = check(p_PData); // normal syntax check
            if (errMsg != null)
            {
                return errMsg;
            }

            // *** NOTE: LENGTH CHECK MUST COME LAST. AFTER NORMAL CHECK ***
            // Because we must have a valid string to build the native version.

            // level #5 - Validate gxml Target length -storage in
            // system3 database
            if (!isTrgGxmlLengthValid())
            {
                return m_strErrMsg;
            }

            // level #5 - Validate native content Target length -
            // storage in client database
            if (!isTrgNativeLengthValid())
            {
                return m_strErrMsg;
            }
        }
        catch (TagNodeException e)
        {
            throw new PseudoParserException(e.toString());
        }
        catch (Exception e)
        {
            throw new PseudoParserException(e.toString());
        }

        return null;
    }

    /**
     * Verifies that the resulting gxml length is valid.
     * 
     * @return true if valid, otherwise false.
     */
    private boolean isTrgGxmlLengthValid() throws Exception
    {
        TmxPseudo converter = new TmxPseudo();

        // zero signals us to ignore the length
        if (m_gxmlMaxLen == 0)
        {
            return true;
        }

        // NOTE: WE ASSUME THE NORMAL SOURCE SEGMENT (WITH SUB
        // PLACEHOLDERS) WAS SET LAST. If normal source-segment was
        // set last it will have sub place holders and the calculation
        // for gxml storage will be accurate as can be. If a
        // map-segment was set last it will have full subs - with the
        // sub length longer than reality for our DB storage.

        // Get the resulting diplomat string that goes into our db.
        // This should have sub placeholders.
        String diplomatGxml = converter.pseudo2Tmx(m_PseudoData);

        try
        {
            int diplomatLen = diplomatGxml.getBytes(m_encodingGxmlMaxLen).length;

            if (diplomatLen > m_gxmlMaxLen)
            {
                m_strErrMsg = m_resources.getString("MaxLengthMsg");
                return false; // invalid
            }
            else
            {
                return true; // valid
            }
        }
        catch (UnsupportedEncodingException e)
        {
            throw new Exception("Bad encoding for gxml length validation.");
        }
    }

    /**
     * Returns true for document formats that the tags cannot be moved (like
     * Office 2010). The XML structure of open office is different with Office
     * 2010, the text can be added between different tags.
     */
    private boolean isTagUnmovableFormat()
    {
        String dataType = (m_PseudoData != null) ? m_PseudoData.getDataType()
                : null;

        if (dataType == null)
        {
            return false;
        }

        if (dataType.equals("office-xml"))
        {
            return true;
        }

        // check if it is idml
        if (dataType.equals("xml"))
        {
            // GBS-3722, MT identifiers should only be located in the beginning
            // and in the end.
            if (m_PseudoData.getMTIdentifierList().size() > 0)
            {
                return true;
            }

            if (m_PseudoData.m_hPseudo2TmxMap == null
                    || m_PseudoData.m_hPseudo2TmxMap.size() == 0)
            {
                return false;
            }

            java.util.Collection tags = m_PseudoData.m_hPseudo2TmxMap.values();
            for (Object object : tags)
            {
                String tag = object.toString();
                if (tag.contains("&lt;Content&gt;")
                        || tag.contains("&lt;CharacterStyleRange ")
                        || tag.contains("&lt;/Content&gt;")
                        || tag.contains("&lt;/CharacterStyleRange&gt;"))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isTagMoved()
    {
        // no tag
        if (m_TrgTagList.size() == 0)
        {
            return false;
        }

        // GBS-3722, MT identifier tags cannot be moved
        if (isMTIdentifierTagsMoved())
        {
            return true;
        }

        m_InternalTags = new HashMap<String, String>();

        m_mockSource = fixMockSegment(m_mockSource, true);
        m_mockTarget = fixMockSegment(m_mockTarget, false);
        m_oriMockTarget = new String(m_mockTarget);

        if (!m_mockSource.contains("T") && m_mockTarget.contains("T"))
        {
            m_strErrMsg = MessageFormat
                    .format("Text cannot be added into segment ({0}) which only contain tags",
                            m_PseudoData.getPTagSourceString());
            return true;
        }

        boolean result = fixMovedTag(false);

        return result;
    }

    /**
     * Checks if MT identifier tags are moved.
     * <p>
     * 
     * @since GBS-3722
     */
    private boolean isMTIdentifierTagsMoved()
    {
        String target = m_PseudoData.getPTagTargetString();
        Map<String, String> mtIdentifierLeading = m_PseudoData
                .getMTIdentifierLeading();
        Map<String, String> mtIdentifierTrailing = m_PseudoData
                .getMTIdentifierTrailing();
        if (mtIdentifierLeading.size() == 0 && mtIdentifierTrailing.size() == 0)
        {
            return false;
        }
        for (String tag : mtIdentifierLeading.keySet())
        {
            int index = target.indexOf(tag);
            if (index != -1)
            {
                String leadingText = target.substring(0, index);
                if (leadingText.trim().length() > 0)
                {
                    m_strErrMsg = MessageFormat.format(
                            m_resources.getString("ErrorTagMoved"), tag);
                    return true;
                }
            }
        }
        for (String tag : mtIdentifierTrailing.keySet())
        {
            int index = target.lastIndexOf(tag);
            if (index != -1)
            {
                String trailingText = target.substring(index + tag.length());
                if (trailingText.trim().length() > 0)
                {
                    m_strErrMsg = MessageFormat.format(
                            m_resources.getString("ErrorTagMoved"), tag);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checking if the tag and text position is valid
     * 
     * @return
     */
    private boolean fixMovedTag(boolean doFix)
    {
        // tag not moved
        if (m_mockSource.equals(m_mockTarget))
        {
            return false;
        }

        boolean isTagMoved = false;
        String firstMovedTag = "";

        // length is not same, this issue should be checked by other checking
        // should not true here, just output this situation if true
        if (m_mockSource.length() != m_mockTarget.length())
        {
            return false;
        }

        int length = m_mockSource.length();
        char srcChar, trgChar;
        StringBuffer newSource = new StringBuffer(m_mockSource);
        StringBuffer newTarget = new StringBuffer(m_mockTarget);

        for (int i = 0; i < length; i++)
        {
            srcChar = m_mockSource.charAt(i);
            trgChar = m_mockTarget.charAt(i);

            // continuew if source, target are same or 'T', 'E'
            if (srcChar == trgChar || trgChar == 'E')
            {
                continue;
            }
            else
            // not same, and target is 'T'
            {
                isTagMoved = true;
                String suffix = m_mockSource.substring(i + 1);
                String prefix = m_mockSource.substring(0, i);

                if ((suffix.contains("T") && suffix.contains("["))
                        || (suffix.contains("[") && !prefix.contains("[")))
                {
                    int index = suffix.indexOf("[");
                    int index_end = suffix.indexOf("]", index);
                    firstMovedTag = suffix.substring(index, index_end + 1);

                    if (doFix)
                    {
                        newSource.insert(i + 1 + index_end + 1, "T");
                        newSource.deleteCharAt(i);
                        newTarget.insert(i + 1 + index_end + 1, "T");
                        newTarget.deleteCharAt(i);
                    }
                }
                else
                {
                    int index = prefix.lastIndexOf("T") + 1;
                    int index_end = i;
                    firstMovedTag = prefix.substring(index, index_end);

                    if (doFix)
                    {
                        newSource.deleteCharAt(i);
                        newSource.insert(index, "T");
                        newTarget.deleteCharAt(i);
                        newTarget.insert(index, "T");
                    }
                }

                if (doFix)
                {
                    m_mockSource = newSource.toString();
                    m_mockTarget = newTarget.toString();
                    return fixMovedTag(doFix);
                }
                else
                {
                    break;
                }
            }
        }

        if (isTagMoved)
        {
            String tagName = firstMovedTag;
            if (m_InternalTags != null
                    && m_InternalTags.containsKey(firstMovedTag))
            {
                tagName = m_InternalTags.get(firstMovedTag);
                if (m_escapeResult)
                {
                    tagName = escapeString(tagName);
                }
            }

            m_strErrMsg = MessageFormat.format(
                    m_resources.getString("ErrorTagMoved"), tagName);
        }

        return isTagMoved;
    }

    private String getInternalText(String tag)
    {
        String key1 = "[" + tag + "]";
        if (m_PseudoData.getInternalTexts().containsKey(key1))
        {
            return m_PseudoData.getInternalTexts().get(key1);
        }

        String key2 = "[" + escapeString(tag) + "]";
        if (m_PseudoData.getInternalTexts().containsKey(key2))
        {
            return m_PseudoData.getInternalTexts().get(key2);
        }

        return null;
    }

    private int getInternalIndex(String tag)
    {
        boolean addPrefix = true;
        if (tag != null && tag.startsWith("[") && tag.endsWith("]"))
        {
            addPrefix = false;
        }

        String key1 = addPrefix ? "[" + tag + "]" : tag;
        if (m_PseudoData.getInternalTexts().containsKey(key1))
        {
            Iterator<String> keys = m_PseudoData.getInternalTexts().keySet()
                    .iterator();
            int index = 0;
            while (keys.hasNext())
            {

                if (key1.equals(keys.next()))
                {
                    return index;
                }

                index++;
            }
        }

        String key2 = addPrefix ? "[" + escapeString(tag) + "]"
                : escapeString(tag);
        if (m_PseudoData.getInternalTexts().containsKey(key2))
        {
            Iterator<String> keys = m_PseudoData.getInternalTexts().keySet()
                    .iterator();
            int index = 0;
            while (keys.hasNext())
            {
                if (key2.equals(keys.next()))
                {
                    return index;
                }

                index++;
            }
        }

        return -1;
    }

    private String fixMockSegment(String ori, boolean isSource)
    {
        Matcher m = P1.matcher(ori);
        while (m.find())
        {
            String tag = m.group(1);
            String tmx = (String) m_PseudoData.m_hPseudo2TmxMap.get(tag);
            String itext = null;
            if (MOVABLE_TAG.contains(tag) || tmx != null
                    && tmx.contains("erasable=\"yes\""))
            {
                ori = StringUtil.replace(ori, "[" + tag + "]", "");
                ori = StringUtil.replace(ori, "[/" + tag + "]", "");
            }
            else if (tmx == null && ((itext = getInternalText(tag)) != null))
            {
                // check if it is removed from target
                String key = "[" + escapeString(tag) + "]";
                if (m_strInternalErrMsg != null
                        && m_strInternalErrMsg.contains(key))
                {
                    ori = StringUtil.replace(ori, "[" + tag + "]", "");
                }
                // is internal text, check contain tag (like w:t ...) or not
                else if (containTag(itext))
                {
                    int itextIndex = getInternalIndex(tag);
                    String ttt = "[gi" + itextIndex + "]";
                    ori = StringUtil.replace(ori, "[" + tag + "]", ttt);

                    if (isSource)
                    {
                        m_InternalTags.put(ttt, "[" + tag + "]");
                    }
                }
                else
                {
                    ori = StringUtil.replace(ori, "[" + tag + "]", "");
                }
            }
        }

        String newstr = StringUtil.replaceWithRE(ori, "[TE]+", "T");

        if (newstr.startsWith("["))
        {
            newstr = "T" + newstr;
        }

        if (newstr.endsWith("]"))
        {
            newstr = newstr + "T";
        }

        return newstr;
    }

    private boolean containTag(String itext)
    {
        StringBuffer sb = new StringBuffer(itext);
        if (itext.contains("<ept ") && itext.contains("</ept>"))
        {
            StringIndex si = StringIndex.getValueBetween(sb, 0, "<ept ",
                    "</ept>");

            // it is from new extractor.
            if (si != null && si.value.contains("&lt;/style&gt;"))
            {
                return false;
            }

            if (si != null && si.value.contains("&lt;")
                    && si.value.contains("&gt;"))
            {
                return true;
            }
        }

        if (itext.contains("<bpt ") && itext.contains("</bpt>"))
        {
            StringIndex si = StringIndex.getValueBetween(sb, 0, "<bpt ",
                    "</bpt>");

            if (si != null && si.value.contains("&lt;")
                    && si.value.contains("&gt;"))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Verifies that the resulting native content length is valid.
     * 
     * @return True if valid, otherwise false.
     */
    private boolean isTrgNativeLengthValid() throws Exception
    {
        TmxPseudo converter = new TmxPseudo();

        // zero signals us to ignore the length
        if (m_nativeContentMaxLen == 0)
        {
            return true;
        }

        try
        {
            // SETUP TEMPORARY PSEUDO DATA with FULL SUBS IN THE SOURCE.

            // TODO: we do not have a copy constructor -- CRAP !
            // So we have to manually copy states.
            PseudoData pdataTmp = new PseudoData();

            pdataTmp.setMode(m_PseudoData.getMode());

            if (m_PseudoData.getAddableMode() == PseudoConstants.ADDABLES_AS_HTML)
            {
                pdataTmp.setAddables("HTML");
            }

            converter.tmx2Pseudo(m_sourceWithSubContent, pdataTmp);

            // copy over and set the target
            pdataTmp.setPTagTargetString(m_PseudoData.getPTagTargetString());

            // get resulting native content with full subs in place.

            // NOTE: The pseudo2Tmx conversion now builds native
            // string as well. It was better to do this than
            // duplicate code in another handler.
            converter.pseudo2Tmx(pdataTmp);
            String nativeContent = converter.getLastPseudo2TmxNativeResult();

            int nativeLen = nativeContent
                    .getBytes(m_encodingNativeContentMaxLen).length;

            if (nativeLen > m_nativeContentMaxLen)
            {
                m_strErrMsg = m_resources.getString("MaxLengthMsg");
                return false;
            }
            else
            {
                return true;
            }
        }

        catch (UnsupportedEncodingException e)
        {
            throw new Exception(
                    "Bad encoding for native-content length validation.");
        }
    }

    /**
     * Verifies that the segment contains no invalid XML characters in the
     * unicode range 0000 - 001f.
     * 
     * @return true if range is not present, otherwise false.
     */
    private boolean isCharactersValid()
    {
        boolean found = true;
        String str = m_PseudoData.getPTagTargetString();
        int len = str.length();

        for (int i = 0; i < len; i++)
        {
            char ch = str.charAt(i);

            if (ch <= '\u001f' && // detect full control char range and...
                    ch != '\u0009' && // allow horizontal tab
                    ch != '\r' && // allow linefeed
                    ch != '\n') // allow carriage return
            {
                String tmp = "\\0x" + Integer.toHexString(ch);
                int leftBegin = i > 25 ? i - 25 : 0; // show up to 25 chars
                                                     // before
                int leftEnd = i; // skip invalid char position
                int rightBegin = (i + 1) < len - 1 ? i + 1 : i; // skip invalid
                                                                // char position
                int rightEnd = (i + 25) < len - 1 ? i + 25 : len - 1; // show up
                                                                      // to 25
                                                                      // chars
                                                                      // after
                String head = leftBegin > 0 ? ".... " : "";
                String tail = rightEnd < len - 1 ? " ...." : "";

                String[] args =
                {
                        tmp,
                        head + str.substring(leftBegin, leftEnd) + "**" + tmp
                                + "**" + str.substring(rightBegin, rightEnd)
                                + tail };
                m_strErrMsg = MessageFormat.format(
                        m_resources.getString("invalidXMLCharacter"), args);
                found = false;
                break;
            }
        }
        return found;
    }

    public SegmentUtil getSegmentUtil()
    {
        if (segmentUtil == null)
        {
            segmentUtil = new SegmentUtil(styles);
        }

        return segmentUtil;
    }

    public void setStyles(String styles)
    {
        this.styles = styles;
    }

    public String geStrInternalErrMsg()
    {
        return m_strInternalErrMsg;
    }

    public void setStrInternalErrMsg(String m_strInternalErrMsg)
    {
        this.m_strInternalErrMsg = m_strInternalErrMsg;
    }

    public void setEscapeResult(boolean vv)
    {
        m_escapeResult = vv;
    }

    public boolean getEscapeResult()
    {
        return m_escapeResult;
    }
}
