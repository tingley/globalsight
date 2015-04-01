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

package com.globalsight.everest.tuv;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.page.PageManagerLocal;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.ling.docproc.extractor.xliff.Extractor;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlFragmentReader;
import com.globalsight.util.gxml.GxmlFragmentReaderPool;

public final class TuImpl extends PersistentObject implements Tu, Serializable
{
    static private final Logger logger = Logger.getLogger(TuImpl.class);

    private static final long serialVersionUID = 1L;
    public static final String M_TU_ID = PersistentObject.M_ID;
    public static final String M_TM_ID = "m_tmId";
    public static final String M_DATA_TYPE = "m_dataType";
    public static final String M_TU_TYPE = "m_tuType";
    public static final String M_LOCALIZABLE_TYPE = "m_localizableType";
    public static final String M_LEVERAGE_GROUP = "m_leverageGroup";
    public static final String M_TUVS = "m_tuvs";

    public static String TRANSLATION_MT = "mt";
    public static final String FROM_WORLDSERVER = "worldserver";

    private static Logger c_category = Logger.getLogger(TuvImpl.class);

    private long m_tmId;
    private String m_dataType = "unknown";
    private String m_tuType = TuType.UNSPECIFIED.getName();
    // Must be 'T' or 'L'. The 'U' stands for unknown.
    private char m_localizableType = 'U';
    private String m_sourceTmName = null;
    private String xliff_target = null;
    private String xliff_mrkid = null;
    private String xliff_mrkindex = null;
    private String xliff_target_language = null;
    private String passoloState = null;
    private String xliffTranslationType = null;
    private boolean xliffLocked = false;
    private String iwsScore = null;

    private LeverageGroup m_leverageGroup;
    private long leverageGroupId = 0;

    // LocaleId : Tuv
    private Map<Long, Tuv> m_tuvs = new HashMap<Long, Tuv>();
    private Set<RemovedTag> removedTags = new HashSet<RemovedTag>();
    private RemovedPrefixTag prefixTag = null;
    private RemovedSuffixTag suffixTag = null;

    private String generateFrom = null;
    private String sourceContent = null;

    /**
     * TU orders range from 1 - n. The default value is 0.
     */
    private long m_order = 0;

    /**
     * Paragraph ID of the current segment. After sentence segmentation,
     * multiple segments can belong to the same paragraph.
     */
    private long m_pid = 0;
    
    private int inddPageNum = 0;

    /**
     * Xliff attribute of "translation-unit" tag.
     */
    private String translate = null;

    public TuImpl()
    {
    }

    public TuImpl(TuImplVo tu)
    {
        m_dataType = tu.getDataType();
        m_tuType = tu.getTuType();
        m_localizableType = tu.getLocalizableType();
        leverageGroupId = tu.getLeverageGroupId();
        m_leverageGroup = tu.getLeverageGroup();
        if (m_leverageGroup != null && leverageGroupId <= 0)
        {
            leverageGroupId = m_leverageGroup.getLeverageGroupId();
        }
        m_tuvs = tu.getTuvMap();
        m_sourceTmName = tu.getSourceTmName();
        m_order = tu.getOrder();
        m_pid = tu.getPid();
    }

    public TuImpl(TuImpl tu)
    {
        m_order = tu.getOrder();
        m_tmId = tu.getTmId();
        m_dataType = tu.getDataType();
        m_tuType = tu.getTuType();

        m_localizableType = tu.getLocalizableType();
        leverageGroupId = tu.getLeverageGroupId();
        m_leverageGroup = tu.getLeverageGroup();
        if (m_leverageGroup != null && leverageGroupId <= 0)
        {
            leverageGroupId = m_leverageGroup.getLeverageGroupId();
        }
        m_pid = tu.getPid();
        m_sourceTmName = tu.getSourceTmName();
        xliffTranslationType = tu.getXliffTranslationType();

        this.xliffLocked = tu.isXliffLocked();
        this.iwsScore = tu.getIwsScore();
        this.xliff_target = tu.getXliffTarget();
        this.xliff_target_language = tu.getXliffTargetLanguage();
        this.generateFrom = tu.getGenerateFrom();

        this.sourceContent = tu.getSourceContent();
        this.passoloState = tu.getPassoloState();
        this.translate = tu.getTranslate();
    }

    //
    // Tu interface methods
    //

    /**
     * Get Tu unique identifier.
     */
    public long getTuId()
    {
        return getId();
    }

    /**
     * Set the order of this Tuv - with regards to the page.
     */
    public void setOrder(long p_order)
    {
        m_order = p_order;
    }

    public long getOrder()
    {
        return m_order;
    }

    /**
     * Sets the paragraph ID.
     */
    public void setPid(long p_pid)
    {
        m_pid = p_pid;
    }

    /**
     * Retrieves the paragraph ID.
     */
    public long getPid()
    {
        return m_pid;
    }

    /**
     * Add a Tuv to a Tu. Also add this Tu to the Tuv as a back pointer (TopLink
     * Requirement for persistence).
     */
    public void addTuv(Tuv p_tuv)
    {
        m_tuvs.put(new Long(p_tuv.getLocaleId()), p_tuv);
        p_tuv.setTu(this);
    }

    /**
     * Get a tuv based on the specified locale id.
     */
    public Tuv getTuv(long p_localeId, long p_jobId)
    {
        return getTuv(p_localeId, true, p_jobId);
    }

    /**
     * Get a tuv based on the specified locale id.
     * 
     * While creating job, need not loadTuvs(), parameter
     * "p_needLoadTuvsManually" should be false.
     */
    public Tuv getTuv(long p_localeId, boolean p_needLoadTuvsManually,
            long p_jobId)
    {
        Tuv tuv = (Tuv) m_tuvs.get(new Long(p_localeId));
        if (tuv != null && "OUT_OF_DATE".equals(tuv.getState().getName()))
        {
            tuv = null;
            m_tuvs.remove(new Long(p_localeId));
        }
        if (tuv == null && p_needLoadTuvsManually)
        {
            loadTuvs(p_jobId);
        }

        return (Tuv) m_tuvs.get(new Long(p_localeId));
    }

    /**
     * Get a collection of all Tuvs. This is a combination of tuvs that belong
     * to a source page and target page(s).
     * 
     * @return A collection of Tuvs of this Tu.
     */
    public Collection getTuvs(boolean p_needLoadTuvsManually, long jobId)
    {
        if (p_needLoadTuvsManually)
        {
            // Load TUVs to avoid TUVs loaded partially.
            loadTuvs(jobId);
        }

        return m_tuvs.values();
    }

    public Map getTuvAsSet(boolean p_needLoadTuvsManually, long p_jobId)
    {
        if (p_needLoadTuvsManually)
        {
            // Load TUVs to avoid TUVs loaded partially.
            loadTuvs(p_jobId);
        }

        return m_tuvs;
    }

    /**
     * Get the Tu type.
     */
    public String getTuType()
    {
        return m_tuType;
    }

    /**
     * Get the data type.
     */
    public String getDataType()
    {
        return m_dataType;
    }

    /**
     * Get the Tm id.
     */
    public long getTmId()
    {
        return m_tmId;
    }

    /**
     * Get the LeverageGroup associated with this Tu.
     */
    public LeverageGroup getLeverageGroup()
    {
        if (m_leverageGroup == null && leverageGroupId > 0)
        {
            loadLeverageGroup();
        }

        return m_leverageGroup;
    }

    private void loadLeverageGroup()
    {
        try
        {
            SourcePage sp = ServerProxy.getPageManager()
                    .getSourcePageByLeverageGroupId(leverageGroupId);
            Iterator lgIter = sp.getExtractedFile().getLeverageGroups()
                    .iterator();
            while (lgIter.hasNext())
            {
                LeverageGroup lg = (LeverageGroup) lgIter.next();
                if (lg.getLeverageGroupId() == this.leverageGroupId)
                {
                    this.m_leverageGroup = lg;
                    break;
                }
            }
        }
        catch (Exception e)
        {
        }

    }

    /**
     * Set the leverage group that this TU belongs to.
     */
    public void setLeverageGroup(LeverageGroup p_leverageGroup)
    {
        m_leverageGroup = p_leverageGroup;
        if (m_leverageGroup != null)
        {
            leverageGroupId = m_leverageGroup.getLeverageGroupId();
        }
    }

    public void setLeverageGroupId(long p_leverageGroupId)
    {
        this.leverageGroupId = p_leverageGroupId;
    }

    public long getLeverageGroupId()
    {
        return this.leverageGroupId;
    }

    /**
     * Get the LocalizableType.
     */
    public char getLocalizableType()
    {
        return m_localizableType;
    }

    public String getSourceTmName()
    {
        return m_sourceTmName;
    }

    public void setSourceTmName(String p_sourceTmName)
    {
        m_sourceTmName = p_sourceTmName;
    }

    public boolean isLocalizable()
    {
        return m_localizableType == 'L' ? true : false;
    }

    /**
     * Return a string representation of the object.
     * 
     * @return a string representation of the object.
     */
    public String toString()
    {
        return super.toString() + " " + "m_dataType=" + m_dataType + " "
                + "m_tuType=" + m_tuType;
    }

    //
    // package methods
    //

    /**
     * Set the TmId.
     */
    public void setTmId(long p_tmId)
    {
        m_tmId = p_tmId;
    }

    /**
     * Set the DataType.
     */
    public void setDataType(String p_dataType)
    {
        m_dataType = p_dataType;
    }

    /**
     * Set the LocalizableType. Must be 'L' or 'T'.
     */
    public void setLocalizableType(char p_localizableType)
    {
        m_localizableType = p_localizableType;
    }

    /**
     * Set the TuType. Don't expose this in Tu.
     * 
     * @param p_tuType
     *            the TuType.
     */
    public void setTuType(TuType p_tuType)
    {
        m_tuType = p_tuType.getName();
    }

    public void setTuType(String type)
    {
        m_tuType = type;
    }

    private GxmlElement getGxmlElement(String p_string)
    {
        GxmlFragmentReader reader = null;
        GxmlElement m_gxmlElement = null;

        if (p_string != null)
        {
            try
            {
                reader = GxmlFragmentReaderPool.instance()
                        .getGxmlFragmentReader();

                m_gxmlElement = reader.parseFragment(p_string);
            }
            catch (Exception e)
            {
                c_category.error("Error in TuImpl: " + toString(), e);
                throw new RuntimeException("Error in TuImpl: ", e);
            }
            finally
            {
                GxmlFragmentReaderPool.instance()
                        .freeGxmlFragmentReader(reader);
            }
        }

        return m_gxmlElement;
    }

    public GxmlElement getXliffTargetGxml()
    {
        return getGxmlElement(xliff_target);
    }

    public String getXliffTarget()
    {
        return this.xliff_target;
    }

    public void setXliffTarget(String p_target)
    {
        this.xliff_target = p_target;
    }
    
    public String getXliffMrkId()
    {
        return this.xliff_mrkid;
    }

    public void setXliffMrkId(String mid)
    {
        this.xliff_mrkid = mid;
    }
    
    public String getXliffMrkIndex()
    {
        return this.xliff_mrkindex;
    }

    public void setXliffMrkIndex(String index)
    {
        this.xliff_mrkindex = index;
    }

    public String getXliffTargetLanguage()
    {
        return this.xliff_target_language;
    }

    public void setXliffTargetLanguage(String p_lan)
    {
        this.xliff_target_language = p_lan;
    }

    public String getXliffTMAttributes()
    {
        String result = "", temp;

        temp = getXliffTranslationType();
        if (temp != null && temp.length() > 0)
        {
            result = Extractor.IWS_TRANSLATION_TYPE + " : " + temp + ", ";
        }

        result = result.trim();
        if (result.endsWith(","))
        {
            result = result.substring(0, result.length() - 1);
        }
        if (result.length() > 0)
        {
            result = " (" + result + ")";
        }

        return result;
    }

    public String getXliffTranslationType()
    {
        return xliffTranslationType;
    }

    public void setXliffTranslationType(String xliffTranslationType)
    {
        this.xliffTranslationType = xliffTranslationType;
    }

    public boolean isXliffTranslationMT()
    {
        // "mt" is the original data.
        if (Extractor.IWS_TRANSLATION_MT.equals(xliffTranslationType)
                || "mt".equals(xliffTranslationType))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean isXliffLocked()
    {
        return xliffLocked;
    }

    public void setXliffLocked(boolean xliffLocked)
    {
        this.xliffLocked = xliffLocked;
    }

    public String getIwsScore()
    {
        return iwsScore;
    }

    public void setIwsScore(String p_str)
    {
        this.iwsScore = p_str;
    }

    /**
     * Adds one <code>RemovedTag</code> to the tu.
     * 
     * @param tag
     *            the <code>RemovedTag</code> to add
     */
    public void addRemoveTag(RemovedTag tag)
    {
        if (removedTags == null)
        {
            removedTags = new HashSet<RemovedTag>();
        }

        removedTags.add(tag);
    }

    /**
     * The set method of removedTags
     * 
     * @param removedTags
     *            the removedTags to set
     */
    public void setRemovedTags(Set<RemovedTag> removedTags)
    {
        this.removedTags = removedTags;
    }

    /**
     * The get method of removedTags
     * 
     * @return the removedTags, maybe null
     */
    public Set getRemovedTags()
    {
        return removedTags;
    }

    /**
     * Checks if the tu has <code>removedTag</code>
     * 
     * @return true if has or false if not has
     */
    public boolean hasRemovedTags()
    {
        return removedTags != null && removedTags.size() > 0;
    }

    /**
     * Gets the first <code>RemovedTag</code> if has.
     * 
     * @return the first <code>RemovedTag</code> if has or null
     */
    public RemovedTag getRemovedTag()
    {
        if (hasRemovedTags())
        {
            for (RemovedTag tag : (Set<RemovedTag>) removedTags)
            {
                return tag;
            }
        }

        return null;
    }

    public boolean hasXliffTMScoreStr()
    {
        boolean result = false;
        if (iwsScore != null && iwsScore.trim().length() > 0)
        {
            result = true;
        }
        return result;
    }

    public RemovedPrefixTag getPrefixTag()
    {
        return prefixTag;
    }

    public void setPrefixTag(RemovedPrefixTag prefixTag)
    {
        this.prefixTag = prefixTag;
    }

    public RemovedSuffixTag getSuffixTag()
    {
        return suffixTag;
    }

    public void setSuffixTag(RemovedSuffixTag suffixTag)
    {
        this.suffixTag = suffixTag;
    }

    public String getGenerateFrom()
    {
        return generateFrom;
    }

    public void setGenerateFrom(String p_generator)
    {
        generateFrom = p_generator;
    }

    public String getSourceContent()
    {
        return sourceContent;
    }

    public void setSourceContent(String p_sourceContent)
    {
        sourceContent = p_sourceContent;
    }

    public String getPassoloState()
    {
        return passoloState;
    }

    public void setPassoloState(String passoloState)
    {
        this.passoloState = passoloState;
    }

    public String getTranslate()
    {
        return translate;
    }

    public void setTranslate(String translate)
    {
        this.translate = translate;
    }

    private void loadTuvs(long p_jobId)
    {
        try
        {
            if (p_jobId > 0)
            {
                List<TuvImpl> tuvList = SegmentTuvUtil.getTuvsByTuId(getId(),
                        p_jobId);
                for (TuvImpl tuv : tuvList)
                {
                    m_tuvs.put(tuv.getLocaleId(), tuv);
                }                
            }
            else
            {
                logger.warn("Invalid job Id for loadTuvs(p_jobId) method : "
                        + p_jobId);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }

    public TuvImpl getSourceTuv()
    {
    	if (this.getLeverageGroupId() < 1)
    		return null;
    	
    	PageManagerLocal local = new PageManagerLocal();
		SourcePage sp = local.getSourcePageByLeverageGroupId(this
				.getLeverageGroupId());
    	if (sp == null)
    		return null;
    	
    	try 
    	{
    	    long jobId = sp.getJobId();
			return SegmentTuvUtil.getTuvByTuIdLocaleId(this.getId(),
					sp.getLocaleId(), jobId);
		}
    	catch (Exception e) 
    	{
			logger.error(e.getMessage(), e);
		}
    	
    	return null;
    }

    public int getInddPageNum()
    {
        return inddPageNum;
    }

    public void setInddPageNum(int inddPageNum)
    {
        this.inddPageNum = inddPageNum;
    }
}
