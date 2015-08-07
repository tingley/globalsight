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
package com.globalsight.everest.edit.online;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.tda.TdaHelper;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants;
import com.globalsight.everest.webapp.pagehandler.edit.online.OnlineTagHelper;
import com.globalsight.ling.docproc.extractor.xliff.XliffAlt;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.gxml.GxmlElement;

/**
 * A data object that holds the viewing elements for segment editor.
 */
public class SegmentView
    implements Serializable
{
    private static final long serialVersionUID = 7558068750222384152L;
    private GxmlElement m_sourceSegment = null;
    private GxmlElement m_targetSegment = null;
    private long m_subId = -1;
    private long m_targetLocaleId = 0;
    private boolean m_isLocalizable = false;
    private String m_dataType = null;
    private String m_itemType = null;
    private int m_wordCount = 0;

    private String m_sourceImageUrl = null;
    private String m_targetImageUrl = null;
    private boolean m_imageMapExists = false;

    /** A list of {@link SegmentMatchResult} objects. */
    private ArrayList m_tmMatchResults = null;
    /** A list of {@link SegmentMatchResult} objects. */
    private Collection m_tbMatchResults = null;
    /** A list of {@link SegmentVersion} objects. */
    private ArrayList m_segmentVersions = null;
    
    private String xliffTarget = null;
    private Set xliffAlt = null;
    private Tuv targetTuv = null;
    private String pagePath = "";
    
    static private final Logger s_logger = Logger
            .getLogger(SegmentView.class);
    

    //////////////////////////////////////////////////////////////////
    // Constructors
    //////////////////////////////////////////////////////////////////

    public SegmentView()
    {
    }

    public SegmentView(GxmlElement p_sourceSegment,
        GxmlElement p_targetSegment, String p_dataType, String p_itemType,
        ArrayList p_tmMatchResults, ArrayList p_segmentVersions)
    {
        m_sourceSegment = p_sourceSegment;
        m_targetSegment = p_targetSegment;
        m_dataType = p_dataType;
        m_itemType = p_itemType;
        m_tmMatchResults = p_tmMatchResults;
        m_segmentVersions = p_segmentVersions;
    }

    public SegmentView(GxmlElement p_sourceSegment,
        GxmlElement p_targetSegment, String p_dataType, String p_itemType,
        String p_sourceImageUrl, String p_targetImageUrl, int p_wordCount,
        ArrayList p_tmMatchResults, ArrayList p_segmentVersions)
    {
        m_sourceSegment = p_sourceSegment;
        m_targetSegment = p_targetSegment;
        m_sourceImageUrl = p_sourceImageUrl;
        m_targetImageUrl = p_targetImageUrl;
        m_dataType = p_dataType;
        m_itemType = p_itemType;
        m_wordCount = p_wordCount;
        m_tmMatchResults = p_tmMatchResults;
        m_segmentVersions = p_segmentVersions;
    }

    //////////////////////////////////////////////////////////////////
    // public APIs
    //////////////////////////////////////////////////////////////////

    public void setIsLocalizable(boolean arg)
    {
        m_isLocalizable = arg;
    }

    public boolean isLocalizable()
    {
        return m_isLocalizable;
    }

    public void setSourceSegment(GxmlElement p_sourceSegment)
    {
        m_sourceSegment = p_sourceSegment;
    }

    public GxmlElement getSourceSegment()
    {
        return m_sourceSegment;
    }

    public void setTargetSegment(GxmlElement p_targetSegment)
    {
        m_targetSegment = p_targetSegment;
    }

    public GxmlElement getTargetSegment()
    {
        return m_targetSegment;
    }
    
    public void setSubId(long p_subId)
    {
    	m_subId = p_subId;
    }

    public long getSubId()
    {
        return m_subId;
    }

    public void setSourceImageUrl(String p_url)
    {
        m_sourceImageUrl = p_url;
    }

    public String getSourceImageUrl()
    {
        return m_sourceImageUrl;
    }

    public void setTargetImageUrl(String p_url)
    {
        m_targetImageUrl = p_url;
    }

    public String getTargetImageUrl()
    {
        return m_targetImageUrl;
    }

    public void setImageMapExists(boolean p_flag)
    {
        m_imageMapExists = p_flag;
    }

    public boolean getImageMapExists()
    {
        return m_imageMapExists;
    }

    public void setDataType(String p_dataType)
    {
        m_dataType = p_dataType;
    }

    public String getDataType()
    {
        return m_dataType;
    }

    public void setItemType(String p_itemType)
    {
        m_itemType = p_itemType;
    }

    public String getItemType()
    {
        return m_itemType;
    }

    /**
     * <p>Sets the best matches for the segment being edited. The
     * argument must be a list of {@see SegmentMatchResult}
     * objects.</p>
     */
    public void setTmMatchResults(ArrayList p_matchResults)
    {
        m_tmMatchResults = p_matchResults;
    }

    /**
     * <p>Returns the best TM matches for the segment being edited.</p>
     *
     * @return a List of {@see SegmentMatchResult} objects, or null.
     */
    public ArrayList getTmMatchResults()
    {
        return m_tmMatchResults;
    }

    /**
     * <p>Sets the best terminology matches for the segment being
     * edited. The argument is a collection of {@see
     * TermLeverageMatchResult} objects.</p>
     */
    public void setTbMatchResults(Collection p_matchResults)
    {
        m_tbMatchResults = p_matchResults;
    }

    /**
     * <p>Returns the best TB matches for the segment being edited.</p>
     *
     * @return a List of {@see SegmentMatchResult} objects, or null.
     */
    public Collection getTbMatchResults()
    {
        return m_tbMatchResults;
    }

    /**
     * <p>Sets the previous versions of a segment in the same workflow
     * if there are any.</p>
     */
    public void setSegmentVersions(ArrayList p_segmentVersions)
    {
        m_segmentVersions = p_segmentVersions;
    }

    /**
     * <p>Returns the previous versions of a segment in the same
     * workflow if there are any.</p>
     *
     * @return a List of {@see SegmentVersion} objects, or null.
     */
    public ArrayList getSegmentVersions()
    {
        return m_segmentVersions;
    }

    public int getWordCount()
    {
        return m_wordCount;
    }

    public void setWordCount(int p_wordCount)
    {
        m_wordCount = p_wordCount;
    }
    
    public void setXliffAlt(Set p_alt) {
        this.xliffAlt = p_alt;
    }
    
    public List getXliffAlt() {
        if(xliffAlt != null) {
            return orderAltByMatchPercent();
        }
        
        return null;
    }
    
    private List orderAltByMatchPercent() {
        Object[] al = xliffAlt.toArray();
        
        for(int i = 0; i < al.length - 1; i++) {
            XliffAlt alt1 = (XliffAlt)al[i];
            double percent1 = TdaHelper.PecentToDouble(alt1.getQuality());
            
            for(int j = i + 1 ; j < xliffAlt.size(); j++) {
                XliffAlt alt2 = (XliffAlt)al[j];
                double percent2 = TdaHelper.PecentToDouble(alt2.getQuality());
                
                if(percent1 < percent2) {
                    al[i] = alt2;
                    al[j] = alt1;
                    alt1 = alt2;
                    percent1 = percent2;
                }
            }
        }
        
        return Arrays.asList(al);
    }
    
    public void setTargetTuv(Tuv p_tuv) {
        this.targetTuv = p_tuv;
    }
    
    public Tuv getTargetTuv() {
        return this.targetTuv;
    }

	public String getPagePath() 
	{
		return pagePath;
	}

	public void setPagePath(String pagePath) 
	{
		this.pagePath = pagePath;
	}

	public void setTargetLocaleId(long targetLocaleId) {
		this.m_targetLocaleId = targetLocaleId;
	}

	public long getTargetLocaleId() {
		return m_targetLocaleId;
	}
	
	public String getSourceHtmlString(String pTagFormat) 
	{
	    OnlineTagHelper applet = new OnlineTagHelper();
	    String seg = GxmlUtil.getInnerXml(getSourceSegment());
        try
        {
            applet.setInputSegment(seg, "", getDataType());
            if (EditorConstants.PTAGS_VERBOSE.equals(pTagFormat))
            {
                applet.getVerbose();
                seg = applet.makeVerboseColoredPtags(seg);
            }
            else
            {
                applet.getCompact();
                seg = applet.makeCompactColoredPtags(seg);
            }
            return seg;
        }
        catch (Exception e)
        {
            s_logger.info("getSourceHtmlString Error.", e);
        }
        
        return seg;
	}
	
	   public String getTargetHtmlString(String pTagFormat, boolean colorPtags) 
	    {
	        OnlineTagHelper applet = new OnlineTagHelper();
	        String seg = GxmlUtil.getInnerXml(getTargetSegment());
	        String result = seg;	        	       
	        
	        try
	        {
	            applet.setInputSegment(seg, "", getDataType());
	            if (EditorConstants.PTAGS_VERBOSE.equals(pTagFormat))
	            {
	                result = applet.getVerbose();
	                if (colorPtags)
	                {
	                    result = applet.makeVerboseColoredPtags(seg);
	                }
	            }
	            else
	            {
	                result = applet.getCompact();
	                if (colorPtags)
                    {
	                    result = applet.makeCompactColoredPtags(seg);
                    }
	            }
	        }
	        catch (Exception e)
	        {
	            s_logger.info("getTargetHtmlString Error.", e);
	        }
	        
	        return result;
	    }
}
