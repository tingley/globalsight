package com.globalsight.ling.aligner;

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

class Segment
{
    private boolean m_TagsAligned = true;
    private String m_Segment = null;
    private String m_tuType = null;
    private boolean m_isLocalized = false;
    private String m_dataType = null;
    private String m_locale;

    /**
     * Segment constructor comment.
     */
    public Segment()
    {
        super();
    }

    public Segment(String p_segment)
    {
        super();
        m_Segment = p_segment;
    }
    
    /**
     *
     * @return java.lang.String
     */
    public String getSegment()
    {
        return m_Segment;
    }

    /**
     *
     * @param p_Segment java.lang.String
     */
    public void setSegment(String p_Segment)
    {
        m_Segment = p_Segment;
    }

    /**
     *
     * @param p_TagsAligned boolean
     */
    public void setTagsAligned(boolean p_TagsAligned)
    {
        m_TagsAligned = p_TagsAligned;
    }

    /**
     *
     * @return boolean
     */
    public boolean areTagsAligned()
    {
        return m_TagsAligned;
    }

    /** Getter for property m_tuType.
     * @return Value of property m_tuType.
     */
    public String getTuType()
    {
        return m_tuType;
    }    

    /** Setter for property m_tuType.
     * @param m_tuType New value of property m_tuType.
     */
    public void setTuType(String p_tuType)
    {
        this.m_tuType = p_tuType;
    }
    
    /** Getter for property m_isLocalized.
     * @return Value of property m_isLocalized.
     */
    public boolean isLocalized()
    {
        return m_isLocalized;
    }
    
    /** Setter for property m_isLocalized.
     * @param m_isLocalized New value of property m_isLocalized.
     */
    public void setIsLocalized(boolean p_isLocalized)
    {
        this.m_isLocalized = p_isLocalized;
    }
    
    /** Getter for property m_dataType.
     * @return Value of property m_dataType.
     */
    public String getDataType()
    {
        return m_dataType;
    }
    
    /** Setter for property m_dataType.
     * @param m_dataType New value of property m_dataType.
     */
    public void setDataType(String p_dataType)
    {
        this.m_dataType = p_dataType;
    }
    
    /** Getter for property m_locale.
     * @return Value of property m_locale.
     */
    public String getLocale()
    {
        return m_locale;
    }
    
    /** Setter for property m_locale.
     * @param m_locale New value of property m_locale.
     */
    public void setLocale(String p_locale)
    {
        this.m_locale = p_locale;
    }
    
}