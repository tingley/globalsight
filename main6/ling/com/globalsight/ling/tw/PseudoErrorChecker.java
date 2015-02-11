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

import com.globalsight.ling.common.DiplomatBasicParserException;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.List;
import java.util.Set;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.UnsupportedEncodingException;
import com.globalsight.util.edit.SegmentUtil;
import com.globalsight.util.StringUtil;

/**
 * PTag error checker.
 */
public class PseudoErrorChecker
    implements PseudoBaseHandler
{

    private boolean m_bHasError = false;
    private PseudoData m_PseudoData;
    private Vector m_TrgTagList = new Vector();
    private String m_strErrMsg = "";
    private ResourceBundle m_resources = null;
    private Locale m_locale = null;
    private static final String RESPATH = "com.globalsight.ling.tw.ErrorChecker";

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

        /*
        * Do not reset the following
        *  m_bVerifyLeadingWhiteSpace
        *  m_bVerifyTrailingWhiteSpace
        *  m_PseudoData
        */
    }

    /**
     * Checks to see if there was an error during the last
     * verification attempt.
     * @return true if there was an error, otherwise false.
     */
    public boolean hasError()
    {
        return m_bHasError;
    }

    /**
     * Pseudo Parser event handler.
     * @param originalString - the full token
     */
    public void processTag(String p_strTagName, String p_originalString)
    {
        m_TrgTagList.addElement(p_strTagName);
    }

    public void processText(String p_strText)
    {
    }

    /**
     * Performs three levels of error checking on the PseudoData provided.<p>
     * 1) Detects illegal or missing tag names.<p>
     * 2) Detects a malformed tag sequence.<p>
     * 3) Detects illegal tag positions (NOT IMPLIMENTED FOR THIS RELEASE).<p>
     * 4) Detects invalid XML characters
     * NOTE: No max length check is performed.
     * @param p_PData - an up to date PsuedoData object.
     */
    public String check(PseudoData p_PData)
        throws PseudoParserException
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

        try
        {
            // level #1 - Check madatory and invalid tags
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
            /*if ( !isMoveValid() )
            {
                return m_strErrMsg;
            }*/

            // level #4 - Check for invalid XML characters
            if (!isCharactersValid())
            {
                return m_strErrMsg;
            }

        }
        catch (TagNodeException e) 
        {
            throw new PseudoParserException(e.toString());
        }

        return null;
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
        while (n > -1) {
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
            Hashtable map= m_PseudoData.getPseudo2TmxMap();
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

        //Judge.
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
                targetString = targetString.substring(0, index) + targetString.substring(index + words.length());
            }
        }

        // Some word changed.
        if (changedWords.size() > 0)
        {
            String words = changedWords.toString();
            words = words.substring(1, words.length()-1);
            m_strErrMsg = MessageFormat.format(
                    m_resources.getString("ErrorConstantChanged"), new String[]{words});
        }
        
        return changed;
    }
    
    /**
     * <p>Validates all the target tag names against the source tag
     * names.</p>
     *
     * <p>This is the lowest level test that is performed first.
     * Basically, this test will:</p>
     *
     *  1. Validate that all the mandatory (non-erasable) tags in the
     *     source also appear in the target exactly one time each.<p>
     *  2. Any additional tags that do not appear in the source must be
     *     of the addable type.
     *     Right now, this is bold, underline and italic. <p>
     *  3. Builds one combined error, which reports all missing and
     *     illegal tags.<p>
     *
     * NOTE: This first level of error checking replaces each object
     * in the target list with a TagNode object - mapped over from the
     * source list.
     *
     * @return False if there are missing or illegal names in the
     * target. True if all tags are valid.
     */
    private boolean isTrgTagListValid()
        throws TagNodeException
    {
        String strTrgTagName;
        String invalidNames = "";
        String missingNames = "";
        TagNode firstUnusedErasable = null;
        TagNode firstUnusedNonErasable = null;
        boolean bHasMissing = false;
        boolean bHasInvalid = false;
        Vector srcTagList = m_PseudoData.getSrcCompleteTagList();
        Enumeration trgEnumerator = m_TrgTagList.elements();
        int nInvalidCnt = 0;
        int nMissingCnt = 0;

        // search for illegal names
        while (trgEnumerator.hasMoreElements())
        {
            strTrgTagName = (String)trgEnumerator.nextElement();
            firstUnusedErasable = firstUnusedNonErasable = null;

            // search the source tags for the first two occurances of
            // the item, once as erasable and once as non-erasable
            Enumeration srcEnumerator = srcTagList.elements();
            while (srcEnumerator.hasMoreElements())
            {
                if ((firstUnusedErasable != null) &&
                    (firstUnusedNonErasable != null))
                {
                    break;
                }

                TagNode srcItem = (TagNode)srcEnumerator.nextElement();
                if (srcItem.getPTagName().toLowerCase().equals(
                    strTrgTagName.toLowerCase()) && !srcItem.isMapped())
                {
                    String erasable =
                        (String)srcItem.getAttributes().get("erasable");

                    if ((erasable == null) ||
                        erasable.toLowerCase().equals("no"))
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
            // Otherwise, mark the first unsed earasable one.  If
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

                    invalidNames += PseudoConstants.PSEUDO_OPEN_TAG +
                        strTrgTagName + PseudoConstants.PSEUDO_CLOSE_TAG;
                }
                else
                {
                    // build a virtual source element to map to, then
                    // replace target-list element.
                    PseudoOverrideMapItem POMI =
                        m_PseudoData.getOverrideMapItem(mapKey);

                    String tmxName;
                    if (strTrgTagName.startsWith("/"))
                    {
                        tmxName = (String)POMI.m_hAttributes.get(
                            PseudoConstants.ADDABLE_TMX_ENDPAIRTAG);
                    }
                    else
                    {
                        tmxName = (String)POMI.m_hAttributes.get(
                            PseudoConstants.ADDABLE_TMX_TAG);
                    }

                    TagNode dynamicMapItem = new TagNode(tmxName,
                        strTrgTagName, POMI.m_hAttributes);

                    m_TrgTagList.setElementAt(dynamicMapItem,
                        m_TrgTagList.indexOf(strTrgTagName));
                }
            }
            else // when addables are not allowed
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
                invalidNames += PseudoConstants.PSEUDO_OPEN_TAG +
                    strTrgTagName + PseudoConstants.PSEUDO_CLOSE_TAG;
            }
        }

        // search for missing tags that are non-erasable
        Enumeration srcEnumerator = srcTagList.elements();
        while (srcEnumerator.hasMoreElements())
        {
            TagNode srcItem = (TagNode) srcEnumerator.nextElement();
            String erasable = (String)srcItem.getAttributes().get("erasable");

            if (!srcItem.isMapped() &&
                (erasable == null || erasable.toLowerCase().equals("no")))
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

                missingNames += PseudoConstants.PSEUDO_OPEN_TAG +
                    srcItem.getPTagName() + PseudoConstants.PSEUDO_CLOSE_TAG;
            }
        }

        // Build error message
        if (bHasMissing || bHasInvalid)
        {
            if (bHasMissing && !bHasInvalid)
            {
                // ErrorMissingTags
                String[] args = { missingNames };
                m_strErrMsg = MessageFormat.format(
                    m_resources.getString("ErrorMissingTags"), args);
            }
            else if (!bHasMissing && bHasInvalid)
            {
                // ErrorInvalidAdd
                String[] args = { invalidNames };
                m_strErrMsg = MessageFormat.format(
                    m_resources.getString("ErrorInvalidAdd"), args);
            }
            else
            {
                // combined error
                String[] args = { missingNames };
                String s1 = MessageFormat.format(
                    m_resources.getString("ErrorMissingTags"), args);
                args = new String[] { invalidNames };
                String s2 = MessageFormat.format(
                    m_resources.getString("ErrorInvalidAdd"), args);

                m_strErrMsg = s1 + "\n" + s2;
            }

            return false;
        }

        m_PseudoData.resetAllSourceListNodes();
        return true;
    }

    /**
     * <p>Builds the Pseudo tag list for the target string.</p>
     *
     * <p>NOTE: Later, during the first level of error checking, each
     * object in the list gets replaced with a TagNode object as it is
     * mapped to the source.</p>
     */
    private void buildTrgTagList() throws PseudoParserException
    {
        PseudoParser parser = new PseudoParser(this);
        parser.tokenize(m_PseudoData.getPTagTargetString());
        return;
    }

    /**
    * Verifies the position of each tag against the source tree.
    *
    * !!! Not implemented in this release.
    *
    * @return  - true if succeeds, otherwise false.
    */
    private boolean isTrgPositionValid()
    {
        m_PseudoData.resetAllSourceListNodes();
        return true;
    }

    /**
    * Verifies that a Pseudo tagged string is well formed.
    * @return true if well formed, otherwise false.
    */
    private boolean isTrgTagListWellFormed()
    {
        boolean hasError = false;
        String unbalancedTags = "";
        int size = m_TrgTagList.size();

        m_PseudoData.resetAllSourceListNodes();

        // Match open-close pairs
        for (int i = 0; i < size; i++)
        {
            TagNode curNode = (TagNode)m_TrgTagList.elementAt(i);
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
                    TagNode searchNode = (TagNode)m_TrgTagList.elementAt(j);
                    String searchTag = searchNode.getPTagName();
                    if (searchTag.startsWith(
                        String.valueOf(PseudoConstants.PSEUDO_END_TAG_MARKER)))
                    {
                        if (!searchNode.isMapped() && searchNode.isPaired() &&
                            curPTag.toLowerCase().equals(
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
            TagNode searchNode = (TagNode)m_TrgTagList.elementAt(i);
            if (!searchNode.isMapped())
            {
                hasError = true;

                if (unbalancedTags.length() > 0 )
                {
                    unbalancedTags += ", ";
                }

                unbalancedTags += PseudoConstants.PSEUDO_OPEN_TAG +
                    searchNode.getPTagName() +
                    PseudoConstants.PSEUDO_CLOSE_TAG;
            }
        }

        // report errors
        if (hasError)
        {
            //ErrorUnbalancedTags
            String[] args = { unbalancedTags };
            m_strErrMsg = MessageFormat.format(
                m_resources.getString("ErrorUnbalancedTags"), args );

            return false;
        }

        return true;
    }

    /**
     * Performs four levels of error checking on the PseudoData provided.<p>
     * 1) Detects illegal or missing tag names.<p>
     * 2) Detects a malformed tag sequence.<p>
     * 3) Detects illegal tag positions (NOT IMPLIMENTED FOR THIS RELEASE).<p>
     * 4) Detects invalid XML characters
     * 5) Detects segments exceeding the maximum lengths.
     * @param p_PData - an up to date PsuedoData object.
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
            if(errMsg!=null)
            { 
                return errMsg;
            }
            
            // ***  NOTE: LENGTH CHECK MUST COME LAST. AFTER NORMAL CHECK ***
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
     * @return true if valid, otherwise false.
     */
    private boolean isTrgGxmlLengthValid( )
        throws Exception
    {
        TmxPseudo converter = new TmxPseudo();

        // zero signals us to ignore the length
        if (m_gxmlMaxLen == 0)
        {
            return true;
        }

        // NOTE: WE ASSUME THE NORMAL SOURCE SEGMENT (WITH SUB
        // PLACEHOLDERS) WAS SET LAST.  If normal source-segment was
        // set last it will have sub place holders and the calculation
        // for gxml storage will be accurate as can be.  If a
        // map-segment was set last it will have full subs - with the
        // sub length longer than reality for our DB storage.

        // Get the resulting diplomat string that goes into our db.
        // This should have sub placeholders.
        String diplomatGxml = converter.pseudo2Tmx(m_PseudoData);

        try
        {
            int diplomatLen =
                diplomatGxml.getBytes(m_encodingGxmlMaxLen).length;

            if (diplomatLen > m_gxmlMaxLen)
            {
                m_strErrMsg = m_resources.getString("MaxLengthMsg");
                return false; //invalid
            }
            else
            {
                return true; //valid
            }
        }
        catch (UnsupportedEncodingException e)
        {
            throw new Exception("Bad encoding for gxml length validation.");
        }
    }


    /**
     * Verifies that the resulting native content length is valid.
     * @return True if valid, otherwise false.
     */
    private boolean isTrgNativeLengthValid()
        throws Exception
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

            if (m_PseudoData.getAddableMode() ==
                PseudoConstants.ADDABLES_AS_HTML)
            {
                pdataTmp.setAddables("HTML");
            }

            converter.tmx2Pseudo(m_sourceWithSubContent, pdataTmp);

            // copy over and set the target
            pdataTmp.setPTagTargetString(m_PseudoData.getPTagTargetString());

            // get resulting native content with full subs in place.

            // NOTE: The pseudo2Tmx conversion now builds native
            // string as well.  It was better to do this than
            // duplicate code in another handler.
            converter.pseudo2Tmx(pdataTmp);
            String nativeContent = converter.getLastPseudo2TmxNativeResult();

            int nativeLen =
                nativeContent.getBytes(m_encodingNativeContentMaxLen).length;

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
    * Verifies that the segment contains no invalid XML characters 
    * in the unicode range 0000 - 001f.
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
            
            if(ch <= '\u001f' && // detect full control char range and...
               ch != '\u0009' && // allow horizontal tab
               ch != '\r' &&     // allow linefeed
               ch != '\n')       // allow carriage return
            {
                String tmp = "\\0x" + Integer.toHexString(ch);
                int leftBegin = i>25 ? i-25 : 0; // show up to 25 chars before
                int leftEnd = i;                 // skip invalid char position
                int rightBegin = (i+1)<len-1 ? i+1 : i;     // skip invalid char position
                int rightEnd = (i+25)<len-1 ? i+25 : len-1; // show up to 25 chars after
                String head = leftBegin>0 ? ".... " : "";
                String tail = rightEnd<len-1 ? " ...." : "";
                
                String[] args = { tmp, 
                                  head + str.substring(leftBegin, leftEnd) + "**" + tmp + "**" + str.substring(rightBegin, rightEnd) + tail };
                m_strErrMsg = MessageFormat.format(
                    m_resources.getString("invalidXMLCharacter"), args );
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
}
