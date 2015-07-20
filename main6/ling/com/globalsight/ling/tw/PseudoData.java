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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.docproc.TmxTagGenerator;
import com.globalsight.ling.tw.internal.InternalTexts;

public class PseudoData
{
    /** The resulting pseudo to tmx mappings */
    public Hashtable m_hPseudo2TmxMap = null;
    public Hashtable m_missedhPseudo2TmxMap = null;
    /* The result of a Tmx2Pseudo conversion. */
    private String m_strPTagSource = "";
    /* The target input string for error checking and Pseudo2Tmx conversion. */
    private String m_strPTagTarget = "";
    // Appended to Pseudo tag names to create unique numbered tags
    private int m_nNextPTagIndex = 0;
    // The pseudo tag naming mode - see PseudoConstants.
    private int m_nMode = PseudoConstants.PSEUDO_VERBOSE;
    // Controls addable tag content - see PseudoConstants.
    private int m_nAddablesMode = PseudoConstants.ADDABLES_DISABLED;
    // The resulting Pseudo to Native mappings
    private Hashtable m_hPseudo2NativeMap = null;
    // This base index should remain unchanged for a given source segment.
    private int m_nBaseUniqueIndex = 1; // must start at 1
    // A array of objects that represent all tagss in te source
    private Vector m_SrcCompleteTagList = new Vector();
    // Locale (for now its just for error messages)
    private Locale m_locale = null;
    // Ptag resources
    private ResourceBundle m_resources = null;
    private HashMap<String, String> m_internalTexts = new HashMap<String, String>();
    private InternalTexts internalTexts = new InternalTexts();
    // data type
    private String m_dataType = null;
    private boolean ignoreNativeId = false;
    private boolean m_isXliffXlfFile = false;
    private boolean isXliff20File = false;

    // Hash which holds directives to override the standard creation of PTags.
    static private Properties m_hPseudoOverrideMap;
    // Tags that may be added by the user for some formats.
    static private Hashtable m_hAddableTagMap;
    static private Hashtable<String, String> m_hAddableOffice2010TagMap;
    // default resource
    static private ResourceBundle m_defaultResource;
    // GBS-3722
    private List<TagNode> m_mtIdentifierList = new ArrayList<TagNode>();
    private Map<String, String> m_mtIdentifiers = new HashMap<String, String>();
    private Map<String, String> m_mtIdentifierLeading = new HashMap<String, String>();
    private Map<String, String> m_mtIdentifierTrailing = new HashMap<String, String>();
    private boolean m_isFromSourceTargetPanel = true;

    static
    {
        try
        {
            // load default resource
            m_defaultResource = ResourceBundle.getBundle(
                    PseudoConstants.PSEUDO_RESPATH, Locale.US);

            // must set m_hPseudoOverrideMap first
            m_hPseudoOverrideMap = mapPseudoOverrides();

            // must set m_hAddableTagMap second
            m_hAddableTagMap = mapAddableTags(m_hPseudoOverrideMap);

            m_hAddableOffice2010TagMap = mapAddable2010Tags();
        }
        catch (Exception e)
        {
            // throws unchecked exception
            throw new RuntimeException(e.toString());
        }
    }

    /**
     * PseudoData class constructor
     */
    public PseudoData()
    {
        // default locale
        m_locale = Locale.US;
        // default resource
        m_resources = m_defaultResource;
    }

    public boolean isXliffXlfFile()
    {
        return m_isXliffXlfFile;
    }

    public void setIsXliffXlfFile(boolean isXliffXlfFile)
    {
        m_isXliffXlfFile = isXliffXlfFile;
    }

    public boolean isFromSourceTargetPanel()
    {
        return m_isFromSourceTargetPanel;
    }

    public void setIsFromSourceTargetPanel(boolean p_isFromSourceTargetPanel)
    {
        m_isFromSourceTargetPanel = p_isFromSourceTargetPanel;
    }

    /**
     * Resets the object.
     */
    public void reset()
    {
        if (m_strPTagSource != null)
        {
            m_strPTagSource = "";
        }

        if (m_strPTagTarget != null)
        {
            m_strPTagTarget = "";
        }

        m_SrcCompleteTagList = new Vector();

        if (m_hPseudo2NativeMap != null)
        {
            m_hPseudo2NativeMap.clear();
        }

        if (m_hPseudo2TmxMap != null)
        {
            m_hPseudo2TmxMap.clear();
        }

        if (m_missedhPseudo2TmxMap != null)
        {
            m_missedhPseudo2TmxMap.clear();
        }

        if (m_internalTexts != null)
        {
            m_internalTexts.clear();
        }

        if (internalTexts != null)
        {
            internalTexts = new InternalTexts();
        }

        m_nBaseUniqueIndex = 1; // must start at 1
        m_nNextPTagIndex = 0; // must start at 0

        /*
         * Do not reset the following m_hAddablesTagMap m_hPseudoOverridesMap
         * m_nAddablesMode m_nMode
         */
    }

    public void resetMTIdentifierList()
    {
        m_mtIdentifierList.clear();
        m_mtIdentifiers.clear();
        m_mtIdentifierLeading.clear();
        m_mtIdentifierTrailing.clear();
    }

    /**
     * Resets the source lists "mapped" flag for each node.
     */
    public void resetAllSourceListNodes()
    {
        Enumeration SrcEnumerator = m_SrcCompleteTagList.elements();

        while (SrcEnumerator.hasMoreElements())
        {
            TagNode node = (TagNode) SrcEnumerator.nextElement();
            node.setMapped(false);
        }
        for (TagNode node : m_mtIdentifierList)
        {
            node.setMapped(false);
        }
    }

    /**
     * Returns the current PTag mode. PseudoConstants.PSEUDO_VERBOSE or
     * PseudoConstants.PSEUDO_COMPACT
     */
    public int getMode()
    {
        return m_nMode;
    }

    /**
     * Returns true if current mode is compact.
     */
    public boolean isModeCompact()
    {
        return (m_nMode == PseudoConstants.PSEUDO_COMPACT) ? true : false;
    }

    /**
     * Returns true if current mode is verbose.
     */
    public boolean isModeVerbose()
    {
        return (m_nMode == PseudoConstants.PSEUDO_VERBOSE) ? true : false;
    }

    /**
     * Returns the current addable-tag mode.
     */
    public int getAddableMode()
    {
        return m_nAddablesMode;
    }

    /**
     * Determines whether addable-tags (in general) are allowed.
     * <p>
     * Use getAddablesMode() to determine exactly what mode we are in.
     * <p>
     * 
     * @return true if addables are allowed. Otherwise false.
     */
    public boolean isAddableAllowed()
    {
        return (m_nAddablesMode != PseudoConstants.ADDABLES_DISABLED) ? true
                : false;
    }

    /**
     * Returns the tmx to native mappings in the form of a hashtable.
     * 
     * @return Hashtable if present, otherwise null.
     */
    public Hashtable getPseudo2NativeMap()
    {
        Hashtable rslt = null;

        // must copy hash in java 1.1 style
        if (this.m_hPseudo2NativeMap != null)
        {
            rslt = new Hashtable();

            String key;
            for (Enumeration e = this.m_hPseudo2NativeMap.keys(); e
                    .hasMoreElements();)
            {
                key = (String) e.nextElement();
                rslt.put(key, (String) m_hPseudo2NativeMap.get(key));
            }
        }

        return rslt;
    }

    /**
     * Returns the mapping of Tmx tags to Pseudo tags for a given diplomat
     * string.
     * 
     * @return Hashtable
     */
    public Hashtable getPseudo2TmxMap()
    {
        return m_hPseudo2TmxMap;
    }

    public Hashtable getMissedPseudo2TmxMap()
    {
        return m_missedhPseudo2TmxMap;
    }

    /**
     * Returns the current Pseudo version of the source string
     */
    public String getPTagSourceString()
    {
        return m_strPTagSource;
    }

    /**
     * Returns the latest Pseudo target string submitted for conversion.
     */
    public String getPTagTargetString()
    {
        return m_strPTagTarget;
    }

    /**
     * Returns the current Pseudo version of the source string with wrapped
     * internal text if exists.
     */
    public String getWrappedPTagSourceString()
    {
        String wrappedSourceString = m_strPTagSource;
        if (internalTexts != null)
        {
            // for GBS-2580, use wrapped string instead
            Map<String, String> map = internalTexts.getWrappedInternalTexts();
            for (String tag : map.keySet())
            {
                if (wrappedSourceString.contains(tag))
                {
                    wrappedSourceString = wrappedSourceString.replace(tag,
                            map.get(tag));
                }
            }
        }
        return wrappedSourceString;
    }

    /**
     * Returns the latest Pseudo target string submitted for conversion with
     * wrapped internal text if exists.
     */
    public String getWrappedPTagTargetString()
    {
        String wrappedTargetString = m_strPTagTarget;
        if (internalTexts != null)
        {
            // for GBS-2580, use wrapped string instead
            Map<String, String> map = internalTexts.getWrappedInternalTexts();
            for (String tag : map.keySet())
            {
                if (wrappedTargetString.contains(tag))
                {
                    wrappedTargetString = wrappedTargetString.replace(tag,
                            map.get(tag));
                }
            }
        }
        return wrappedTargetString;
    }

    /**
     * Returns the users locale (mainly for error messages).
     * 
     * @return Locale
     */
    public Locale getLocale()
    {
        return m_locale;
    }

    /**
     * Returns the ptag resource bundle.
     * 
     * @return ResourceBundle
     */
    public ResourceBundle getResources()
    {
        return m_resources;
    }

    /**
     * Returns a complete list of tags that are addable.
     * 
     * @return hashtable - where the keys are valid addable tag names.
     */
    public Vector getSrcCompleteTagList()
    {
        return m_SrcCompleteTagList;
    }

    public List<TagNode> getMTIdentifierList()
    {
        return m_mtIdentifierList;
    }

    /**
     * Returns an entry from the pseudo override map.
     * 
     * @param p_strKey
     *            - the name of a TMX-type that appears in the map.
     * @return PseudoOverrideMapItem
     */
    public PseudoOverrideMapItem getOverrideMapItem(String p_key)
    {
        return (PseudoOverrideMapItem) m_hPseudoOverrideMap.get(p_key);
    }

    /**
     * Returns a TagNode which holds all the information about a given source
     * tag.
     * 
     * @param p_idx
     *            - the index of the item in the source list.
     * @return a TagNode or null if the index is out of range.
     */
    public TagNode getSrcTagItem(int p_idx)
    {
        if (m_SrcCompleteTagList == null
                || !(p_idx < m_SrcCompleteTagList.size()))
        {
            return null;
        }

        return (TagNode) m_SrcCompleteTagList.elementAt(p_idx);
    }

    /**
     * Returns the starting point from which to create new (unique) "i"
     * attributes for newly added tags.
     */
    public int getBaseUniqueIndex()
    {
        return m_nBaseUniqueIndex;
    }

    /**
     * Records the results of the last diplomat 2 psuedo conversion. This
     * becomes the PTag source string for display.
     */
    public void setPTagSourceString(String pseudoString)
    {
        this.m_strPTagSource = pseudoString;
    }

    /**
     * Sets the translated PTag string to be converted back into diplomat.
     */
    public void setPTagTargetString(String pseudoString)
    {
        this.m_strPTagTarget = pseudoString;
    }

    /**
     * Sets the base unique index.
     */
    public void setBaseUniqueIndex(int p_nBaseIdx)
    {
        m_nBaseUniqueIndex = p_nBaseIdx;
    }

    /**
     * Sets the tag mode - compact or verbose
     * 
     * @param p_nMode
     *            - 1=verbose, 2=compact
     */
    public void setMode(int p_nMode)
    {
        m_nMode = p_nMode;
    }

    /**
     * Set the Pseudo2NativeMap.
     */
    public void setPseudo2NativeMap(Hashtable p_hMap)
    {
        m_hPseudo2NativeMap = p_hMap;
    }

    /**
     * Returns a mapping of tmx types to Override items.
     * <p.>
     * <p>
     * NOTE:
     * <p>
     * All TMX types do not necessarily have a corresponding override defined.
     * An entry in this table is made only if:
     * <p>
     * - You want a particular TMX type to appear unnumbered to the user.
     * (Everything else is numbered by default)
     * <p>
     * Also NOTE: Anything unnumbered MUST also be addable.
     * <p>
     * - You want an alternate name for verbose or compact modes.
     * <p>
     * If the TMX type is not defined in the diplomat string, we default to
     * numbered "x" or "g".
     * <p>
     * (See the makePseudoTagName method)
     * <p>
     * These mappings apply to diplomat input without respect to the parser.
     * <p>
     * 
     * @return a mapping table in the form of a properties hash.
     * @throws PseudoOverrideItemException
     */
    private static Properties mapPseudoOverrides()
            throws PseudoOverrideItemException
    {
        Properties p = new Properties();

        // ADDABLE NATIVE TAGS
        // The following hashtables are used to:
        // 1) Generate the TMX for an addable PTag
        // 2) And to populate the attributes for an addable tag
        // in the error checkers target tag list.

        // Common attributes for addable tags
        String erasableVal = "yes";
        String movableVal = "yes";

        // Unique attributes for paired addable tags

        // bold
        String strB = TmxTagGenerator.getInlineTypeName(TmxTagGenerator.BOLD);

        Hashtable B_Data = new Hashtable();
        B_Data.put(PseudoConstants.ADDABLE_TMX_TAG, "bpt");
        B_Data.put(PseudoConstants.ADDABLE_TMX_TYPE, strB);
        B_Data.put(PseudoConstants.ADDABLE_TMX_ENDPAIRTAG, "ept");
        B_Data.put(PseudoConstants.ADDABLE_ATTR_ERASABLE, erasableVal);
        // B_Data.put(PseudoConstants.ADDABLE_ATTR_MOVABLE, movableVal);
        // Note: enter raw native content - caller must encode as needed
        B_Data.put(PseudoConstants.ADDABLE_HTML_CONTENT, "<b>");
        B_Data.put(PseudoConstants.ADDABLE_ENDPAIR_HTML_CONTENT, "</b>");
        B_Data.put(PseudoConstants.ADDABLE_RTF_CONTENT, "\\b ");
        B_Data.put(PseudoConstants.ADDABLE_ENDPAIR_RTF_CONTENT, "\\b0 ");

        // subscript
        String strSub = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.OFFICE_SUB);

        Hashtable sub_Data = new Hashtable();
        sub_Data.put(PseudoConstants.ADDABLE_TMX_TAG, "bpt");
        sub_Data.put(PseudoConstants.ADDABLE_TMX_TYPE, strSub);
        sub_Data.put(PseudoConstants.ADDABLE_TMX_ENDPAIRTAG, "ept");
        sub_Data.put(PseudoConstants.ADDABLE_ATTR_ERASABLE, erasableVal);
        // B_Data.put(PseudoConstants.ADDABLE_ATTR_MOVABLE, movableVal);
        // Note: enter raw native content - caller must encode as needed
        sub_Data.put(PseudoConstants.ADDABLE_HTML_CONTENT, "<sub>");
        sub_Data.put(PseudoConstants.ADDABLE_ENDPAIR_HTML_CONTENT, "</sub>");
        sub_Data.put(PseudoConstants.ADDABLE_RTF_CONTENT, "\\b ");
        sub_Data.put(PseudoConstants.ADDABLE_ENDPAIR_RTF_CONTENT, "\\b0 ");

        // subscript
        String strSup = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.OFFICE_SUP);
        Hashtable sup_Data = new Hashtable();
        sup_Data.put(PseudoConstants.ADDABLE_TMX_TAG, "bpt");
        sup_Data.put(PseudoConstants.ADDABLE_TMX_TYPE, strSup);
        sup_Data.put(PseudoConstants.ADDABLE_TMX_ENDPAIRTAG, "ept");
        sup_Data.put(PseudoConstants.ADDABLE_ATTR_ERASABLE, erasableVal);
        // B_Data.put(PseudoConstants.ADDABLE_ATTR_MOVABLE, movableVal);
        // Note: enter raw native content - caller must encode as needed
        sup_Data.put(PseudoConstants.ADDABLE_HTML_CONTENT, "<sup>");
        sup_Data.put(PseudoConstants.ADDABLE_ENDPAIR_HTML_CONTENT, "</sup>");
        sup_Data.put(PseudoConstants.ADDABLE_RTF_CONTENT, "\\b ");
        sup_Data.put(PseudoConstants.ADDABLE_ENDPAIR_RTF_CONTENT, "\\b0 ");
        
        // highlight
        String strHighlight = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.OFFICE_HIGHLIGHT);
        Hashtable highlight_Data = new Hashtable();
        highlight_Data.put(PseudoConstants.ADDABLE_TMX_TAG, "bpt");
        highlight_Data.put(PseudoConstants.ADDABLE_TMX_TYPE, strHighlight);
        highlight_Data.put(PseudoConstants.ADDABLE_TMX_ENDPAIRTAG, "ept");
        highlight_Data.put(PseudoConstants.ADDABLE_ATTR_ERASABLE, erasableVal);
        // B_Data.put(PseudoConstants.ADDABLE_ATTR_MOVABLE, movableVal);
        // Note: enter raw native content - caller must encode as needed
        highlight_Data.put(PseudoConstants.ADDABLE_HTML_CONTENT, "<highlight>");
        highlight_Data.put(PseudoConstants.ADDABLE_ENDPAIR_HTML_CONTENT, "</highlight>");
        highlight_Data.put(PseudoConstants.ADDABLE_RTF_CONTENT, "\\b ");
        highlight_Data.put(PseudoConstants.ADDABLE_ENDPAIR_RTF_CONTENT, "\\b0 ");

        // BOLD
        String cStrB = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.C_BOLD);

        Hashtable C_B_Data = new Hashtable();
        C_B_Data.put(PseudoConstants.ADDABLE_TMX_TAG, "bpt");
        C_B_Data.put(PseudoConstants.ADDABLE_TMX_TYPE, cStrB);
        C_B_Data.put(PseudoConstants.ADDABLE_TMX_ENDPAIRTAG, "ept");
        C_B_Data.put(PseudoConstants.ADDABLE_ATTR_ERASABLE, erasableVal);
        // B_Data.put(PseudoConstants.ADDABLE_ATTR_MOVABLE, movableVal);
        // Note: enter raw native content - caller must encode as needed
        C_B_Data.put(PseudoConstants.ADDABLE_HTML_CONTENT, "<B>");
        C_B_Data.put(PseudoConstants.ADDABLE_ENDPAIR_HTML_CONTENT, "</B>");
        C_B_Data.put(PseudoConstants.ADDABLE_RTF_CONTENT, "\\b ");
        C_B_Data.put(PseudoConstants.ADDABLE_ENDPAIR_RTF_CONTENT, "\\b0 ");

        // strong
        String strStrong = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.STRONG);

        Hashtable STRONG_Data = new Hashtable();
        STRONG_Data.put(PseudoConstants.ADDABLE_TMX_TAG, "bpt");
        STRONG_Data.put(PseudoConstants.ADDABLE_TMX_TYPE, strStrong);
        STRONG_Data.put(PseudoConstants.ADDABLE_TMX_ENDPAIRTAG, "ept");
        STRONG_Data.put(PseudoConstants.ADDABLE_ATTR_ERASABLE, erasableVal);
        // B_Data.put(PseudoConstants.ADDABLE_ATTR_MOVABLE, movableVal);
        // Note: enter raw native content - caller must encode as needed
        STRONG_Data.put(PseudoConstants.ADDABLE_HTML_CONTENT, "<strong>");
        STRONG_Data.put(PseudoConstants.ADDABLE_ENDPAIR_HTML_CONTENT,
                "</strong>");
        STRONG_Data.put(PseudoConstants.ADDABLE_RTF_CONTENT, "\\b ");
        STRONG_Data.put(PseudoConstants.ADDABLE_ENDPAIR_RTF_CONTENT, "\\b0 ");

        // STRONG
        String cStrStrong = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.C_STRONG);

        Hashtable C_STRONG_Data = new Hashtable();
        C_STRONG_Data.put(PseudoConstants.ADDABLE_TMX_TAG, "bpt");
        C_STRONG_Data.put(PseudoConstants.ADDABLE_TMX_TYPE, cStrStrong);
        C_STRONG_Data.put(PseudoConstants.ADDABLE_TMX_ENDPAIRTAG, "ept");
        C_STRONG_Data.put(PseudoConstants.ADDABLE_ATTR_ERASABLE, erasableVal);
        // B_Data.put(PseudoConstants.ADDABLE_ATTR_MOVABLE, movableVal);
        // Note: enter raw native content - caller must encode as needed
        C_STRONG_Data.put(PseudoConstants.ADDABLE_HTML_CONTENT, "<STRONG>");
        C_STRONG_Data.put(PseudoConstants.ADDABLE_ENDPAIR_HTML_CONTENT,
                "</STRONG>");
        C_STRONG_Data.put(PseudoConstants.ADDABLE_RTF_CONTENT, "\\b ");
        C_STRONG_Data.put(PseudoConstants.ADDABLE_ENDPAIR_RTF_CONTENT, "\\b0 ");

        // em
        String strEm = TmxTagGenerator.getInlineTypeName(TmxTagGenerator.EM);

        Hashtable EM_Data = new Hashtable();
        EM_Data.put(PseudoConstants.ADDABLE_TMX_TAG, "bpt");
        EM_Data.put(PseudoConstants.ADDABLE_TMX_TYPE, strEm);
        EM_Data.put(PseudoConstants.ADDABLE_TMX_ENDPAIRTAG, "ept");
        EM_Data.put(PseudoConstants.ADDABLE_ATTR_ERASABLE, erasableVal);
        EM_Data.put(PseudoConstants.ADDABLE_HTML_CONTENT, "<em>");
        EM_Data.put(PseudoConstants.ADDABLE_ENDPAIR_HTML_CONTENT, "</em>");
        EM_Data.put(PseudoConstants.ADDABLE_RTF_CONTENT, "\\i ");
        EM_Data.put(PseudoConstants.ADDABLE_ENDPAIR_RTF_CONTENT, "\\i0 ");

        // EM
        String cStrEm = TmxTagGenerator.getInlineTypeName(TmxTagGenerator.C_EM);

        Hashtable C_EM_Data = new Hashtable();
        C_EM_Data.put(PseudoConstants.ADDABLE_TMX_TAG, "bpt");
        C_EM_Data.put(PseudoConstants.ADDABLE_TMX_TYPE, cStrEm);
        C_EM_Data.put(PseudoConstants.ADDABLE_TMX_ENDPAIRTAG, "ept");
        C_EM_Data.put(PseudoConstants.ADDABLE_ATTR_ERASABLE, erasableVal);
        C_EM_Data.put(PseudoConstants.ADDABLE_HTML_CONTENT, "<EM>");
        C_EM_Data.put(PseudoConstants.ADDABLE_ENDPAIR_HTML_CONTENT, "</EM>");
        C_EM_Data.put(PseudoConstants.ADDABLE_RTF_CONTENT, "\\i ");
        C_EM_Data.put(PseudoConstants.ADDABLE_ENDPAIR_RTF_CONTENT, "\\i0 ");

        // i
        String strI = TmxTagGenerator.getInlineTypeName(TmxTagGenerator.ITALIC);

        Hashtable I_Data = new Hashtable();
        I_Data.put(PseudoConstants.ADDABLE_TMX_TAG, "bpt");
        I_Data.put(PseudoConstants.ADDABLE_TMX_TYPE, strI);
        I_Data.put(PseudoConstants.ADDABLE_TMX_ENDPAIRTAG, "ept");
        I_Data.put(PseudoConstants.ADDABLE_ATTR_ERASABLE, erasableVal);
        // I_Data.put(PseudoConstants.ADDABLE_ATTR_MOVABLE, movableVal);
        // Note: enter raw native content - caller must encode as needed
        I_Data.put(PseudoConstants.ADDABLE_HTML_CONTENT, "<i>");
        I_Data.put(PseudoConstants.ADDABLE_ENDPAIR_HTML_CONTENT, "</i>");
        I_Data.put(PseudoConstants.ADDABLE_RTF_CONTENT, "\\i ");
        I_Data.put(PseudoConstants.ADDABLE_ENDPAIR_RTF_CONTENT, "\\i0 ");

        // I
        String cStrI = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.C_ITALIC);

        Hashtable C_I_Data = new Hashtable();
        C_I_Data.put(PseudoConstants.ADDABLE_TMX_TAG, "bpt");
        C_I_Data.put(PseudoConstants.ADDABLE_TMX_TYPE, cStrI);
        C_I_Data.put(PseudoConstants.ADDABLE_TMX_ENDPAIRTAG, "ept");
        C_I_Data.put(PseudoConstants.ADDABLE_ATTR_ERASABLE, erasableVal);
        // I_Data.put(PseudoConstants.ADDABLE_ATTR_MOVABLE, movableVal);
        // Note: enter raw native content - caller must encode as needed
        C_I_Data.put(PseudoConstants.ADDABLE_HTML_CONTENT, "<I>");
        C_I_Data.put(PseudoConstants.ADDABLE_ENDPAIR_HTML_CONTENT, "</I>");
        C_I_Data.put(PseudoConstants.ADDABLE_RTF_CONTENT, "\\i ");
        C_I_Data.put(PseudoConstants.ADDABLE_ENDPAIR_RTF_CONTENT, "\\i0 ");

        // u
        String strU = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.UNDERLINE);

        Hashtable U_Data = new Hashtable();
        U_Data.put(PseudoConstants.ADDABLE_TMX_TAG, "bpt");
        U_Data.put(PseudoConstants.ADDABLE_TMX_TYPE, strU);
        U_Data.put(PseudoConstants.ADDABLE_TMX_ENDPAIRTAG, "ept");
        U_Data.put(PseudoConstants.ADDABLE_ATTR_ERASABLE, erasableVal);
        // U_Data.put(PseudoConstants.ADDABLE_ATTR_MOVABLE, movableVal);
        // Note: enter raw native content - caller must encode as needed
        U_Data.put(PseudoConstants.ADDABLE_HTML_CONTENT, "<u>");
        U_Data.put(PseudoConstants.ADDABLE_ENDPAIR_HTML_CONTENT, "</u>");
        U_Data.put(PseudoConstants.ADDABLE_RTF_CONTENT, "{\\ul ");
        U_Data.put(PseudoConstants.ADDABLE_ENDPAIR_RTF_CONTENT, "}");

        // U
        String cStrU = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.C_UNDERLINE);

        Hashtable C_U_Data = new Hashtable();
        C_U_Data.put(PseudoConstants.ADDABLE_TMX_TAG, "bpt");
        C_U_Data.put(PseudoConstants.ADDABLE_TMX_TYPE, cStrU);
        C_U_Data.put(PseudoConstants.ADDABLE_TMX_ENDPAIRTAG, "ept");
        C_U_Data.put(PseudoConstants.ADDABLE_ATTR_ERASABLE, erasableVal);
        // U_Data.put(PseudoConstants.ADDABLE_ATTR_MOVABLE, movableVal);
        // Note: enter raw native content - caller must encode as needed
        C_U_Data.put(PseudoConstants.ADDABLE_HTML_CONTENT, "<U>");
        C_U_Data.put(PseudoConstants.ADDABLE_ENDPAIR_HTML_CONTENT, "</U>");
        C_U_Data.put(PseudoConstants.ADDABLE_RTF_CONTENT, "{\\ul ");
        C_U_Data.put(PseudoConstants.ADDABLE_ENDPAIR_RTF_CONTENT, "}");

        // Unique attributes for un-paired addable tags

        // Nbsp
        String strNbsp = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.NBSPACE);

        Hashtable NBSP_Data = new Hashtable();
        NBSP_Data.put(PseudoConstants.ADDABLE_TMX_TAG, "ph");
        NBSP_Data.put(PseudoConstants.ADDABLE_TMX_TYPE, strNbsp);
        NBSP_Data.put(PseudoConstants.ADDABLE_ATTR_ERASABLE, erasableVal);
        // NBSP_Data.put(PseudoConstants.ADDABLE_ATTR_MOVABLE, movableVal);
        // Note: enter raw native content - caller must encode as needed
        NBSP_Data.put(PseudoConstants.ADDABLE_HTML_CONTENT, "&nbsp;");
        NBSP_Data.put(PseudoConstants.ADDABLE_RTF_CONTENT, "\\~");

        // br
        String strBr = TmxTagGenerator.getInlineTypeName(TmxTagGenerator.X_BR);

        Hashtable BR_Data = new Hashtable();
        BR_Data.put(PseudoConstants.ADDABLE_TMX_TAG, "ph");
        BR_Data.put(PseudoConstants.ADDABLE_TMX_TYPE, strBr);
        BR_Data.put(PseudoConstants.ADDABLE_ATTR_ERASABLE, erasableVal);
        // BR_Data.put(PseudoConstants.ADDABLE_ATTR_MOVABLE, movableVal);
        // Note: enter raw native content - caller must encode as needed
        BR_Data.put(PseudoConstants.ADDABLE_HTML_CONTENT, "<br/>");

        // BR
        String cStrBr = TmxTagGenerator.getInlineTypeName(TmxTagGenerator.C_BR);

        Hashtable C_BR_Data = new Hashtable();
        C_BR_Data.put(PseudoConstants.ADDABLE_TMX_TAG, "ph");
        C_BR_Data.put(PseudoConstants.ADDABLE_TMX_TYPE, strBr);
        C_BR_Data.put(PseudoConstants.ADDABLE_ATTR_ERASABLE, erasableVal);
        // BR_Data.put(PseudoConstants.ADDABLE_ATTR_MOVABLE, movableVal);
        // Note: enter raw native content - caller must encode as needed
        C_BR_Data.put(PseudoConstants.ADDABLE_HTML_CONTENT, "<BR/>");

        // Character entity references

        // Get the TMX type-names for the remaining overrides that are
        // not addable.
        String strXB = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.X_BOLD);
        String strXSTRONG = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.X_STRONG);
        String strXI = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.X_ITALIC);
        String strXEM = TmxTagGenerator.getInlineTypeName(TmxTagGenerator.X_EM);
        String strXU = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.X_UNDERLINE);
        String strL = TmxTagGenerator.getInlineTypeName(TmxTagGenerator.LINK);
        String strFC = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.FONTCHANGE);
        String strLB = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.LINEBREAK);
        String strSP = TmxTagGenerator.getInlineTypeName(TmxTagGenerator.SPACE);
        String strTab = TmxTagGenerator.getInlineTypeName(TmxTagGenerator.TAB);
        String strFF = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.FORMFEED);

        String cStrXB = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.C_X_BOLD);
        String cStrXSTRONG = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.C_X_STRONG);
        String cStrXI = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.C_X_ITALIC);
        String cStrXEM = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.C_X_EM);
        String cStrXU = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.C_X_UNDERLINE);
        String cStrL = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.C_LINK);
        String cStrFC = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.C_FONTCHANGE);
        String cStrLB = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.C_LINEBREAK);
        String cStrTab = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.C_TAB);
        String cStrFF = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.C_FORMFEED);
        String officeSuperscript = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.OFFICE_SUPERSCRIPT);
        String officeHyperlink = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.OFFICE_HYPERLINK);
        String officeBold = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.OFFICE_BOLD);
        String officeColor = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.OFFICE_COLOR);
        String officeItalic = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.OFFICE_ITALIC);
        String officeUnderline = TmxTagGenerator
                .getInlineTypeName(TmxTagGenerator.OFFICE_UNDERLINE);

        // =============================================================
        //
        // Note: A Tmx type listed in the following hash will always explicitly
        // become a numbered or unnumbered PTag. For example, one
        // parser cannot assume "link" will become numbered while
        // another assume it will be unnumbered. These mappings
        // apply to all parsers.
        //
        // NOTE: Remember, by default everything becomes a numbered PTag
        // unless stated otherwise in this table.
        //
        // NOTE: Everything must be filled in in the table
        // (no empty strings).
        // And never use "g" or "x" as a compact or verbose name.
        //
        // NOTE: The m_nAddablesMode flag will enable/disable all
        // addables. This is curently used to restrict the use of addable
        // tags to HTML only. Other native content could be added to the
        // each hashtable which defines each addable - thus allowing us to
        // support more formats.
        //
        // NOTE: New mappings were added for x-bold, x-uline and x-italic.
        // These types occur when the html parser encounters HTML tags
        // for bold, italic or underline which have attributes. We must
        // treat these types differently to maintain the native content.
        // To do so, an extra set of overrides are defined below. These
        // tags will be named the same name as the standard ones (without
        // attributes) but **MUST** be defined as numbered and non-addable.
        //
        // NOTE: Wed Oct 02 18:06:51 2002 CvdL. New mapping added for x-br.
        // To remain backward-compatible with existing TM data, we
        // cannot use LINEBREAK (lb) since the HTML extractor assigns x-br.
        // We should use LINEBREAK at some point but we need to migrate TMs.
        //
        // NOTE: We should maybe consider moving this entire mapping
        // structure to the TmxGenerator class.
        // =============================================================

        // TmxType TmxType paired verbose comp. num. addable
        p.put(strL, new PseudoOverrideMapItem(strL, true, "link", "l", true,
                null));
        p.put(cStrL, new PseudoOverrideMapItem(cStrL, true, "LINK", "L", true,
                null));
        p.put(strFC, new PseudoOverrideMapItem(strFC, true, "font", "f", true,
                null));
        p.put(cStrFC, new PseudoOverrideMapItem(cStrFC, true, "FONT", "F",
                true, null));
        p.put(strB, new PseudoOverrideMapItem(strB, true, "bold", "b", false,
                B_Data));
        p.put(cStrB, new PseudoOverrideMapItem(cStrB, true, "BOLD", "B", false,
                C_B_Data));
        p.put(strI, new PseudoOverrideMapItem(strI, true, "italic", "i", false,
                I_Data));
        p.put(cStrI, new PseudoOverrideMapItem(cStrI, true, "ITALIC", "I",
                false, C_I_Data));
        p.put(strEm, new PseudoOverrideMapItem(strEm, true, "em", "e", false,
                EM_Data));
        p.put(cStrEm, new PseudoOverrideMapItem(cStrEm, true, "EM", "E", false,
                C_EM_Data));
        p.put(strU, new PseudoOverrideMapItem(strU, true, "underline", "u",
                false, U_Data));
        p.put(cStrU, new PseudoOverrideMapItem(cStrU, true, "UNDERLINE", "U",
                false, C_U_Data));
        p.put(strXB, new PseudoOverrideMapItem(strXB, true, "bold", "b", true,
                null));
        p.put(cStrXB, new PseudoOverrideMapItem(cStrXB, true, "BOLD", "B",
                true, null));
        p.put(strXI, new PseudoOverrideMapItem(strXI, true, "italic", "i",
                true, null));
        p.put(cStrXI, new PseudoOverrideMapItem(cStrXI, true, "ITALIC", "I",
                true, null));
        p.put(strXEM, new PseudoOverrideMapItem(strXEM, true, "em", "e", true,
                null));
        p.put(cStrXEM, new PseudoOverrideMapItem(cStrXEM, true, "EM", "E",
                true, null));
        p.put(strXU, new PseudoOverrideMapItem(strXU, true, "underline", "u",
                true, null));
        p.put(cStrXU, new PseudoOverrideMapItem(cStrXU, true, "UNDERLINE", "U",
                true, null));
        p.put(strLB, new PseudoOverrideMapItem(strLB, false, "lineBreak", "lb",
                true, null));
        p.put(cStrLB, new PseudoOverrideMapItem(cStrLB, false, "LINE_BREAK",
                "LB", true, null));
        p.put(strTab, new PseudoOverrideMapItem(strTab, false, "tab", "t",
                true, null));
        p.put(cStrTab, new PseudoOverrideMapItem(cStrTab, false, "TAB", "T",
                true, null));
        p.put(strSP, new PseudoOverrideMapItem(strSP, false, "space", "sp",
                true, null));
        p.put(strFF, new PseudoOverrideMapItem(strFF, false, "formfeed", "ff",
                true, null));
        p.put(cStrFF, new PseudoOverrideMapItem(cStrFF, false, "FORM_FEED",
                "FF", true, null));
        p.put(strNbsp, new PseudoOverrideMapItem(strNbsp, false, "nbsp",
                "nbsp", false, NBSP_Data));
        p.put(strBr, new PseudoOverrideMapItem(strBr, false, "break", "br",
                false, BR_Data));
        p.put(cStrBr, new PseudoOverrideMapItem(cStrBr, false, "BREAK", "BR",
                false, C_BR_Data));
        p.put(strStrong, new PseudoOverrideMapItem(strStrong, true, "strong",
                "strong", false, STRONG_Data));
        p.put(cStrStrong, new PseudoOverrideMapItem(cStrStrong, true, "STRONG",
                "STRONG", false, C_STRONG_Data));
        p.put(strXSTRONG, new PseudoOverrideMapItem(strXSTRONG, true, "strong",
                "strong", true, null));
        p.put(cStrXSTRONG, new PseudoOverrideMapItem(cStrXSTRONG, true,
                "strong", "strong", true, null));
        p.put(officeSuperscript, new PseudoOverrideMapItem(officeSuperscript,
                true, "superscript", "superscript", true, null));
        p.put(officeHyperlink, new PseudoOverrideMapItem(officeHyperlink, true,
                "hyperlink", "hyperlink", true, null));
        p.put(officeBold, new PseudoOverrideMapItem(officeBold, true, "bold",
                "bold", true, null));
        p.put(officeColor, new PseudoOverrideMapItem(officeColor, true,
                "color", "color", true, null));
        p.put(officeItalic, new PseudoOverrideMapItem(officeItalic, true,
                "italic", "italic", true, null));
        p.put(officeUnderline, new PseudoOverrideMapItem(officeUnderline, true,
                "underline", "underline", true, null));

        p.put(strSub, new PseudoOverrideMapItem(strSub, true, "subscript",
                "sub", false, sub_Data));
        p.put(strSup, new PseudoOverrideMapItem(strSup, true, "superscript",
                "sup", false, sup_Data));
        
        p.put(strHighlight, new PseudoOverrideMapItem(strSup, true, "highlight",
                "hl", true, null));

        return p;
    }

    private static Hashtable mapAddable2010Tags()
    {
        Hashtable map = new Hashtable();
        map.put("i", "italic");
        map.put("italic", "italic");
        map.put("b", "bold");
        map.put("bold", "bold");
        map.put("u", "ulined");
        map.put("underline", "ulined");
        map.put("sub", "office-sub");
        map.put("subscript", "office-sub");
        map.put("sup", "office-sup");
        map.put("superscript", "office-sup");

        return map;
    }

    /*
     * Builds Pseudo-to-Tmx tag mapping for valid addable pseudo tags.
     * 
     * @return Hashtable
     */
    private static Hashtable mapAddableTags(Hashtable p_hMainOverrideMap)
    {
        Hashtable h = new Hashtable();
        String tag;

        for (Enumeration e = p_hMainOverrideMap.keys(); e.hasMoreElements();)
        {
            String key = (String) e.nextElement();
            PseudoOverrideMapItem item = (PseudoOverrideMapItem) p_hMainOverrideMap
                    .get(key);

            if (item.m_hAttributes != null)
            {
                // Map both forms of the ptag (verbose/compact) for look up
                if (item.m_strVerbose != null && item.m_strVerbose.length() > 0)
                {
                    if (Character.isLowerCase(item.m_strVerbose.charAt(0)))
                    {
                        h.put(item.m_strVerbose.toLowerCase(), item.m_strTmx);
                    }
                    else
                    {
                        h.put(item.m_strVerbose.toUpperCase(), item.m_strTmx);
                    }
                }

                if (item.m_strCompact != null && item.m_strCompact.length() > 0)
                {
                    if (Character.isLowerCase(item.m_strCompact.charAt(0)))
                    {
                        h.put(item.m_strCompact.toLowerCase(), item.m_strTmx);
                    }
                    else
                    {
                        h.put(item.m_strCompact.toUpperCase(), item.m_strTmx);
                    }
                }
            }
        }

        return h;
    }

    /**
     * Checks if this tag name appears in the addable tag list (for **ANY**
     * native format!!).
     * 
     * @return the OverrideMapkey if it appears in the list, otherwise null.
     * @param p_strTagName
     *            - the tag name to lookup (with out without brackets).
     */
    public String isAddableTag(String p_strTagName)
    {
        // trim brackets (if both present)
        if (p_strTagName.startsWith("[") && p_strTagName.endsWith("]"))
        {
            p_strTagName = p_strTagName.substring(1, p_strTagName.length() - 1);
        }

        // do not distingush between a begin or an end during lookup
        if (p_strTagName.startsWith("/"))
        {
            p_strTagName = p_strTagName.substring(1);
        }

        if (p_strTagName.length() > 0)
        {
            if (Character.isLowerCase(p_strTagName.charAt(0)))
            {
                p_strTagName = p_strTagName.toLowerCase();
            }
            else
            {
                p_strTagName = p_strTagName.toUpperCase();
            }
        }

        if (m_nAddablesMode == PseudoConstants.ADDABLES_AS_OFFICE_2010
                || m_nAddablesMode == PseudoConstants.ADDABLES_AS_MIF)
        {
            p_strTagName = p_strTagName.toLowerCase();
            return m_hAddableOffice2010TagMap.get(p_strTagName);
        }

        return (String) m_hAddableTagMap.get(p_strTagName);
    }

    /**
     * Checks if this tag name appears in the addable tag list (for the
     * specified native format).
     * 
     * @return the OverrideMapkey if it appears in the list, otherwise null.
     * @param p_strTagName
     *            - the tag name to lookup (with out without brackets).
     * @param p_formatName
     *            - the format name under which to look. Example: html
     */
    public String isAddableInFormat(String p_strTagName,
            String p_nativeFormatName)
    {
        String overrideMapKey = null;
        PseudoOverrideMapItem overrideMapItem = null;
        String contentCheck = "undefined";

        // trim brackets (if both present)
        if (p_strTagName.startsWith("[") && p_strTagName.endsWith("]"))
        {
            p_strTagName = p_strTagName.substring(1, p_strTagName.length() - 1);
        }

        // do not distingush between a begin or an end during lookup
        if (p_strTagName.startsWith("/"))
        {
            p_strTagName = p_strTagName.substring(1);
        }

        // set the addable attribute we need to confirm (we could use any of the
        // addable attributes)
        // we'll use ADDABLE_???_CONTENT
        if (p_nativeFormatName.toLowerCase().equals("html"))
        {
            contentCheck = PseudoConstants.ADDABLE_HTML_CONTENT;
        }
        else if (p_nativeFormatName.toLowerCase().equals("rtf"))
        {
            contentCheck = PseudoConstants.ADDABLE_RTF_CONTENT;
        }

        // confirm adability for the specified format
        // overrideMapKey =
        // (String)m_hAddableTagMap.get(p_strTagName.toLowerCase());
        overrideMapKey = (String) isAddableTag(p_strTagName);
        if (overrideMapKey != null)
        {
            overrideMapItem = getOverrideMapItem(overrideMapKey);
            return overrideMapItem.m_hAttributes.get(contentCheck) != null ? overrideMapKey
                    : null;
        }
        else
        {
            return null;
        }
    }

    /**
     * Adds an entry to the tag listings for the source string.
     * <p>
     * 
     * This source list is a complete list of all tags found in the source (in
     * the order in which they appear).
     * <p>
     * 
     * The table can be retrieved with the getSrcCompleteTagList().
     * <p>
     * 
     * @param p_PTagItem
     *            - the item to be added.
     * @return the index at which this item was added to the list.
     */
    public int addSourceTagItem(TagNode p_PTagItem)
    {
        // Store the item in the difinative source list.
        p_PTagItem.setSourceListIndex(m_SrcCompleteTagList.size());
        m_SrcCompleteTagList.addElement(p_PTagItem);

        return p_PTagItem.getSourceListIndex();
    }

    /**
     * Returns a TagNode which holds all the information about a given source
     * tag. The item is searched for by the target PTag name to which is was
     * mapped.
     * 
     * @param p_TrgPTag
     *            - the name of the tag in the target string.
     * @return a TagNode. Null if it is not found.
     */
    public TagNode findSrcItemByTrgName(String p_TrgPTag)
    {
        if (m_SrcCompleteTagList == null || (p_TrgPTag.length() == 0))
        {
            return null;
        }

        Enumeration SrcEnumerator = m_SrcCompleteTagList.elements();
        while (SrcEnumerator.hasMoreElements())
        {
            TagNode SrcItem = (TagNode) SrcEnumerator.nextElement();
            if (SrcItem.getPTagName().equals(p_TrgPTag))
            {
                return SrcItem;
            }
        }

        return null;
    }

    /**
     * Returns a TagNode which holds all the information about a given source
     * tag. The item is searched for by the target PTag name to which is was
     * mapped and a matching set of attributes.
     * 
     * @param p_TrgPTag
     *            - the name of the tag in the target string.
     * @return a TagNode. Null if it is not found.
     */
    public TagNode findSrcItemByTrgName(String p_PTag, Hashtable p_hAttributes)
    {
        String thisTagAttrKey;
        String thisTagAttrVal;

        TagNode srcListItem = findSrcItemByTrgName(p_PTag);

        if (srcListItem != null)
        {
            // compare attributes for exact match
            Enumeration thisTagKeys = p_hAttributes.keys();
            while (thisTagKeys.hasMoreElements())
            {
                thisTagAttrKey = (String) thisTagKeys.nextElement();

                // but do not compare i attribute
                if (thisTagAttrKey.equals("i"))
                {
                    continue;
                }

                // and do not compare x attribute
                if (thisTagAttrKey.equals("x"))
                {
                    continue;
                }

                thisTagAttrVal = (String) p_hAttributes.get(thisTagAttrKey);

                if (!thisTagAttrVal.equals(srcListItem.getAttributes().get(
                        thisTagAttrKey)))
                {
                    return null;
                }
            }
        }

        return srcListItem;
    }

    /*
     * Set addables mode.<p>
     * 
     * @param p_nMode if mode equals 0, addables are disabled. If set to 1, HTML
     * addables are enabled.
     * 
     * @see com.globalsight.ling.tw.PseudoContants for more modes.
     */
    public void setAddables(String p_format)
    {
        if (p_format.equalsIgnoreCase("html"))
        {
            m_nAddablesMode = PseudoConstants.ADDABLES_AS_HTML;
        }
        else if (p_format.equalsIgnoreCase("rtf"))
        {
            m_nAddablesMode = PseudoConstants.ADDABLES_AS_RTF;
        }
        else if (p_format.equalsIgnoreCase("xlf"))
        {
            m_nAddablesMode = PseudoConstants.ADDABLES_AS_XLF;
        }
        else if (p_format.equalsIgnoreCase("office-xml"))
        {
            m_nAddablesMode = PseudoConstants.ADDABLES_AS_OFFICE_2010;
        }
        else if (p_format.equalsIgnoreCase("mif"))
        {
            m_nAddablesMode = PseudoConstants.ADDABLES_AS_MIF;
        }
        else
        {
            m_nAddablesMode = PseudoConstants.ADDABLES_DISABLED;
        }
    }

    /*
     * Sets the locale (mainly for error messages) And re-loads resources.
     * 
     * @param p_locale
     */
    public void setLocale(String p_locale)
    {
        Locale locale = null;
        ResourceBundle newBundle = null;

        if (p_locale.length() == 2) // must be language only
        {
            locale = new Locale(p_locale, "");
        }
        else if (p_locale.length() == 5) // language plus country
        {
            locale = new Locale(p_locale.substring(0, 2), p_locale.substring(3,
                    5));
        }
        else
        {
            // handle variants - use default locale for now
            locale = Locale.US;
        }

        // sets member variable m_locale and re-loads resources
        setLocale(locale);

    }

    /*
     * Sets the locale (mainly for error messages) and re-loads resources.
     * 
     * @param p_locale
     */
    public void setLocale(Locale p_locale)
    {
        ResourceBundle newBundle = null;

        m_locale = p_locale;

        // re-load resources - defaults loaded in constructor
        try
        {
            newBundle = ResourceBundle.getBundle(
                    PseudoConstants.PSEUDO_RESPATH, p_locale);
        }
        catch (MissingResourceException e)
        {
            // If cannot find defaults now, keep the ones we loaded in
            // constructor
        }

        m_resources = newBundle;
    }

    /**
     * Builds a new pseudo tag name based the Tmx type. Numbering is determined
     * automatically.
     * 
     * @param p_tmxTagName
     *            - The TMX tag name.
     * @param p_attributes
     *            - The TMX tag attributes.
     * @param p_strDefaultName
     *            - the default name to use.
     * @return the PTag name as a string.
     */
    public String makePseudoTagName(String p_tmxTagName,
            Hashtable p_attributes, String p_strDefaultName)
            throws DiplomatBasicParserException
    {
        // do not force the tag to be numbered
        return (makePseudoTagName(p_tmxTagName, p_attributes, p_strDefaultName,
                false));
    }

    /**
     * Builds a new pseudo tag name based the Tmx type. Numbering can be forced
     * ON or left on automatic.
     * 
     * @param p_tmxTagName
     *            - The TMX tag name.
     * @param p_attributes
     *            - The TMX tag attributes.
     * @param p_strDefaultName
     *            - the default name to use.
     * @param p_bMakeNumbered
     *            - true forces "mapped" types to be numbered.
     *            <p>
     *            When false, numbering is done according to the mapped override
     *            attributes..
     * @return the PTag name as a string.
     */
    public String makePseudoTagName(String p_tmxTagName,
            Hashtable p_attributes, String p_strDefaultName,
            boolean p_bMakeNumbered) throws DiplomatBasicParserException
    {
        String tag = "";
        String p_strType = (String) p_attributes.get("type");
        String nativeCodeID = (String) p_attributes.get("x");

        if (isIgnoreNativeId())
        {
            nativeCodeID = "0";
        }

        if (p_tmxTagName.equals("ph"))
        {
            boolean isValidInt = false;
            if (nativeCodeID != null)
            {
                try
                {
                    Integer.parseInt(nativeCodeID);
                    isValidInt = true;
                }
                catch (NumberFormatException nfe)
                {
                }
            }
            // For ph tag, its "x" is from "id" for iws xliff files, but GS will
            // generate "x" for ph. So this is difference between system.
            if (nativeCodeID == null || !isValidInt)
            {
                nativeCodeID = (String) p_attributes.get("id");
                if (nativeCodeID != null)
                {
                    try
                    {
                        int intNativeCodeID = (new Integer(nativeCodeID))
                                .intValue() + 1;
                        nativeCodeID = String.valueOf(intNativeCodeID);
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }

        if ((p_strType == null || p_strType.length() == 0))
        {
            if (nativeCodeID == null)
            {
                nativeCodeID = (String) p_attributes.get("i");

                if (nativeCodeID == null)
                {
                    nativeCodeID = (String) p_attributes.get("id");
                }
            }

            // NOTE: an empty type cannot be an addable type - so we
            // should always have an X attribute. However, it is
            // possble to have an addable type without an x attribute
            // after passing through the ptag module once. This is
            // becuase addables are unnumbered and the generic content
            // for the addable does not have an X.
            tag = p_strDefaultName + nativeCodeID; // default
        }
        else if (p_tmxTagName.equals("it"))
        {
            // SPECIAL OVERRIDING BEHAVIOR FOR "IT" TAGS - PER THE SPEC.

            // Force the erasable attribute to non-erasable This
            // dynamic change only affects the behavior of the error
            // checker. The original tmx is unchanged.
            p_attributes.put("erasable", "no");

            // Force the PTag name to its default value and preserve
            // the numbering
            tag = "x" + nativeCodeID;
        }
        else
        // look for an override for this type
        {
            if (m_isXliffXlfFile && nativeCodeID == null)
            {
                nativeCodeID = (String) p_attributes.get("i");

                if (nativeCodeID == null)
                {
                    nativeCodeID = (String) p_attributes.get("id");
                }
            }

            PseudoOverrideMapItem PNameItem = (PseudoOverrideMapItem) m_hPseudoOverrideMap
                    .get(p_strType);

            if (PNameItem == null || m_isXliffXlfFile)
            {
                // OVERRIDE IS NOT DEFINED

                // The x attribute (nativeCodeID) is mandatory for
                // bpt, it, ph and ut. It is needed to build a unique
                // name that links the ptag to its native code and
                // allows for movable ptags. Since sub's never get
                // passed to the ptag module we should be OK. If we
                // ever do, they will have to have a mandatory x
                // attribute as well.
                if ((nativeCodeID == null) || (nativeCodeID.length() <= 0))
                {
                    throw new DiplomatBasicParserException(
                            "MakePtagName(): The \"x\" attribute is empty for : "
                                    + p_tmxTagName);
                }

                if (p_strType.length() == 0)
                {
                    // No type - default to numbered "g" or "x"
                    tag = p_strDefaultName + nativeCodeID; // default
                }
                else
                {
                    // Else, depending on the mode, use g/x or the
                    // tmx type when defined. Both numbered.
                    if (m_nMode == PseudoConstants.PSEUDO_COMPACT)
                    {
                        tag = p_strDefaultName; // default
                    }
                    else
                    {
                        tag = p_strType;
                    }

                    tag = tag + nativeCodeID;
                }
            }
            else
            {
                // OVERRIDE IS DEFINED

                switch (m_nMode)
                {
                    case PseudoConstants.PSEUDO_VERBOSE:
                        if (PNameItem.m_strVerbose.length() == 0)
                        {
                            tag = p_strType; // default
                        }
                        else
                        {
                            tag = PNameItem.m_strVerbose;
                        }
                        break;
                    case PseudoConstants.PSEUDO_COMPACT:
                        if (PNameItem.m_strCompact.length() == 0)
                        {
                            tag = p_strDefaultName; // default
                        }
                        else
                        {
                            tag = PNameItem.m_strCompact;
                        }
                        break;
                    case PseudoConstants.PSEUDO_TMXNAME:
                        tag = p_strType;
                        break;
                    default:
                        tag = p_strDefaultName;
                        break;
                }

                // for mapped types we can force them to be numbered.
                if (nativeCodeID != null)
                {
                    if (PNameItem.m_bNumbered || (p_bMakeNumbered == true)
                            || (!isAddableAllowed()))
                    {
                        tag = tag + nativeCodeID;
                    }
                }
            }
        }

        // drop the non-native prefix - if present
        if (tag.startsWith("x-"))
        {
            tag = tag.substring(2);
        }

        return tag;
    }

    public HashMap<String, String> getInternalTexts()
    {
        return m_internalTexts;
    }

    public Map<String, String> getMTIdentifiers()
    {
        return m_mtIdentifiers;
    }

    public Map<String, String> getMTIdentifierLeading()
    {
        return m_mtIdentifierLeading;
    }

    public Map<String, String> getMTIdentifierTrailing()
    {
        return m_mtIdentifierTrailing;
    }

    public void addInternalTags(String tag, String segment)
            throws TagNodeException
    {
        m_internalTexts.put(tag, segment);
        Properties attributes = new Properties();
        attributes.put("internal", true);
        String internalTag = null;
        if (tag.startsWith("[") && tag.endsWith("]"))
        {
            internalTag = tag.substring(1, tag.length() - 1);
        }
        else
        {
            internalTag = tag;
        }
        TagNode tagNode = new TagNode(TagNode.INTERNAL, internalTag, attributes);
        m_SrcCompleteTagList.add(tagNode);
    }

    public void addMTIdentifierLeading(String tag, String segment)
            throws TagNodeException
    {
        m_mtIdentifierLeading.put(tag, segment);
        addMTIdentifiers(tag, segment);
    }

    public void addMTIdentifierTrailing(String tag, String segment)
            throws TagNodeException
    {
        m_mtIdentifierTrailing.put(tag, segment);
        addMTIdentifiers(tag, segment);
    }

    private void addMTIdentifiers(String tag, String segment)
            throws TagNodeException
    {
        m_mtIdentifiers.put(tag, segment);
        Properties attributes = new Properties();
        attributes.put("internal", true);
        String internalTag = null;
        if (tag.startsWith("[") && tag.endsWith("]"))
        {
            internalTag = tag.substring(1, tag.length() - 1);
        }
        else
        {
            internalTag = tag;
        }
        TagNode tagNode = new TagNode(TagNode.INTERNAL, internalTag, attributes);
        tagNode.setMTIdentifier(true);
        m_mtIdentifierList.add(tagNode);
    }

    public void addInternalTags(InternalTexts texts) throws TagNodeException
    {
        internalTexts = texts;
        HashMap<String, String> map = texts.getInternalTexts();
        for (String tag : map.keySet())
        {
            addInternalTags(tag, map.get(tag));
        }
    }

    public String revertInternalTags(String segment)
    {
        for (String key : m_internalTexts.keySet())
        {
            segment = segment.replace(key, m_internalTexts.get(key));
        }

        return segment;
    }

    public void setDataType(String p_dataType)
    {
        m_dataType = p_dataType;
    }

    public String getDataType()
    {
        return m_dataType;
    }

    public boolean isIgnoreNativeId()
    {
        return ignoreNativeId;
    }

    public void setIgnoreNativeId(boolean ignoreNativeId)
    {
        this.ignoreNativeId = ignoreNativeId;
    }

    /**
     * @return the isXliff20File
     */
    public boolean isXliff20File()
    {
        return isXliff20File;
    }

    /**
     * @param isXliff20File the isXliff20File to set
     */
    public void setXliff20File(boolean isXliff20File)
    {
        this.isXliff20File = isXliff20File;
    }
}