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
package com.globalsight.cxe.entity.fileprofile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.filterconfiguration.Filter;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.JSPFilter;
import com.globalsight.cxe.entity.filterconfiguration.JavaPropertiesFilter;
import com.globalsight.cxe.entity.filterconfiguration.JavaScriptFilter;
import com.globalsight.cxe.entity.filterconfiguration.MSOfficeDocFilter;
import com.globalsight.cxe.entity.filterconfiguration.MSOfficeExcelFilter;
import com.globalsight.cxe.entity.filterconfiguration.MSOfficePPTFilter;
import com.globalsight.cxe.entity.filterconfiguration.POFilter;
import com.globalsight.cxe.entity.filterconfiguration.XMLRuleFilter;
import com.globalsight.cxe.entity.xmldtd.XmlDtdImpl;
import com.globalsight.everest.persistence.PersistentObject;

/**
 * Represents a CXE File Profile entity object.
 */
public class FileProfileImpl extends PersistentObject implements FileProfile
{
    private static final long serialVersionUID = -6739135285284626287L;
    static private final Logger s_logger = Logger
            .getLogger(FileProfileImpl.class);

    /** used for TOPLink queries against the profile id attribute */
    static final public String L10NPROFILE_ID = "m_l10nProfileId";

    /** used for TOPLink queries against the known format type id attribute */
    static final public String KNOWN_FORMAT_TYPE_ID = "m_knownFormatTypeId";

    /**
     * Used for TOPLink queries based on a file profile name.
     */
    public static final String NAME = "m_name";

    public boolean useActive = true;
    private Long newId;

    // PRIVATE MEMBERS
    private long m_knownFormatTypeId;
    private long m_l10nProfileId;
    private Long m_xmlRuleFileId;
    private String m_name;
    private String m_description;
    private long m_companyId;
    private String m_code_set;
    private Set m_extensionIds;
    private boolean m_byDefaultExportStf = false;
    private String m_scriptOnImport;
    private String m_scriptOnExport;
    private boolean supportSid = false;
    private boolean unicodeEscape = false;
    private boolean entityEscape = false;
    private boolean preserveSpaces = false;

    private boolean headerTranslate = false;

    private String jsFilterRegex;

    private XmlDtdImpl xmlDtd = null;

    private long filterId = 0;

    private String filterTableName = null;

    private long secondFilterId = 0;

    private String secondFilterTableName = null;

    private int terminology_approval = 0;

    private int xlfSourceAsUnTranslatedTarget = 0;

    private long reference_fp = 0;

    public static final int UTF_BOM_PRESERVE = 1;
    public static final int UTF_BOM_ADD = 2;
    public static final int UTF_BOM_REMOVE = 3;

    public static final int UTF_8_WITH_BOM = 1;
    public static final int UTF_16_LE = 2;
    public static final int UTF_16_BE = 3;
    private int BOMType = 0;

    // CONSTRUCTORS
    /** Default constructor for TOPLink */
    public FileProfileImpl()
    {
        m_knownFormatTypeId = 0;
        m_l10nProfileId = 0;
        m_xmlRuleFileId = null;
        m_name = null;
        m_description = null;
        m_companyId = -1;
        m_code_set = null;
        m_scriptOnImport = null;
        m_scriptOnExport = null;
        m_extensionIds = new HashSet();
        filterId = 0;
    }

    /***************************************************************************
     * Constructs an FileProfileImpl from a FileProfile (no deep copy)
     * 
     * @param o
     *            Another FileProfile object *
     **************************************************************************/
    public FileProfileImpl(FileProfile o)
    {
        m_knownFormatTypeId = o.getKnownFormatTypeId();
        m_l10nProfileId = o.getL10nProfileId();
        m_xmlRuleFileId = ((FileProfileImpl) o).getXmlRuleFileId();
        m_name = o.getName();
        m_description = o.getDescription();
        m_companyId = o.getCompanyId();
        m_code_set = o.getCodeSet();
        m_scriptOnImport = o.getScriptOnImport();
        m_scriptOnExport = o.getScriptOnExport();

        m_extensionIds = new HashSet();
        if (o.getFileExtensionIds() != null)
        {
            m_extensionIds.addAll(o.getFileExtensionIds());
        }

        m_byDefaultExportStf = o.byDefaultExportStf();
        filterId = o.getFilterId();
        BOMType = o.getBOMType();
    }

    // PUBLIC METHODS

    public int getBOMType()
    {
        return BOMType;
    }

    public void setBOMType(int bOMType)
    {
        BOMType = bOMType;
    }

    /**
     * @see FileProfile.byDefaultExportStf()
     */
    public boolean byDefaultExportStf()
    {
        return m_byDefaultExportStf;
    }

    public boolean isByDefaultExportStf()
    {
        return m_byDefaultExportStf;
    }

    /**
     * Deactivate this FileProfile object. i.e. Logically delete it.
     */
    public void deactivate()
    {
        isActive(false);
    }

    /**
     * Return the list of file extension Ids for this file profile
     * 
     * @return Vector of file extension Ids (as Long)
     */
    public Vector getFileExtensionIds()
    {
        Vector ids = new Vector();
        ids.addAll(m_extensionIds);
        return ids;
    }

    public Set getExtensionIds()
    {
        return m_extensionIds;
    }

    /**
     * Return the id of the known format type (HTML,XML,CSS,etc.)
     * 
     * @return known format type id
     */
    public long getKnownFormatTypeId()
    {
        return m_knownFormatTypeId;
    }

    /**
     * Return the id of the L10nProfile that is associated with this File
     * Profile
     * 
     * @return L10NProfileId
     */
    public long getL10nProfileId()
    {
        return m_l10nProfileId;
    }

    /**
     * Return the name of the file profile
     * 
     * @return file profile name
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Return the description of the file profile
     * 
     * @return file profile description
     */
    public String getDescription()
    {
        return m_description;
    }

    /**
     * Return the company id of the file profile
     * 
     * @return file profile company id
     */
    public long getCompanyId()
    {
        return m_companyId;
    }

    /**
     * Return the character set of the file profile
     * 
     * @return file profile code_set
     */
    public String getCodeSet()
    {
        return m_code_set;
    }

    /**
     * @deprecated Only used by Hibernate.
     * 
     *             Call {@link #getXmlRuleId()} instead.
     */
    long getXmlRuleFileId()
    {

        // The former logic.
        if (m_xmlRuleFileId == null)
        {
            return 0;
        }
        else
        {
            return m_xmlRuleFileId.longValue();
        }

    }

    /**
     * Return the script on export
     * 
     * @return
     */
    public String getScriptOnExport()
    {
        return m_scriptOnExport;
    }

    /**
     * Return the script on import
     * 
     * @return
     */
    public String getScriptOnImport()
    {
        return m_scriptOnImport;
    }

    /**
     * @see FileProfile.byDefaultExportStf(boolean)
     */
    public void byDefaultExportStf(boolean p_byDefaultExportStf)
    {
        m_byDefaultExportStf = p_byDefaultExportStf;
    }

    public void setByDefaultExportStf(boolean p_byDefaultExportStf)
    {
        m_byDefaultExportStf = p_byDefaultExportStf;
    }

    /**
     * Sets the File Profile's associated list of Extension Ids
     * 
     * @param p_extensionIds
     *            An Vector of extension Ids for this file The Vector must
     *            contain objects of type Number (probably BigDecimal since
     *            TOPLink uses this method to populate from the database). This
     *            method will convert them to Longs.
     */
    public void setFileExtensionIds(Vector p_extensionIds)
    {
        m_extensionIds = new HashSet();
        if (p_extensionIds != null)
        {
            int size = p_extensionIds.size();
            for (int i = 0; i < size; i++)
            {
                Long extensionId = new Long(
                        ((Number) p_extensionIds.elementAt(i)).longValue());
                m_extensionIds.add(extensionId);
            }
        }
    }

    public void setExtensionIds(Set p_extensionIds)
    {
        m_extensionIds = p_extensionIds;
    }

    /**
     * Set the id of the known format type (HTML,XML,CSS,etc.)
     * 
     * @param p_knownFormatType
     *            The Known Format Type ID for this file
     */
    public void setKnownFormatTypeId(long p_knownFormatType)
    {
        m_knownFormatTypeId = p_knownFormatType;
    }

    /**
     * Set the id of the localization profile
     * 
     * @param p_l10nProfileId
     *            L10nProfile ID
     */
    public void setL10nProfileId(long p_l10nProfileId)
    {
        m_l10nProfileId = p_l10nProfileId;
    }

    /**
     * Set the name of the file profile
     * 
     * @param p_name
     *            The name of the file profile
     */
    public void setName(String p_name)
    {
        m_name = p_name;
    }

    /**
     * Set the description of the file profile
     * 
     * @param p_description
     *            The description of the file profile
     */
    public void setDescription(String p_description)
    {
        m_description = p_description;
    }

    /**
     * Set the company id of the file profile
     * 
     * @param p_companyId
     *            The company id of the file profile
     */
    public void setCompanyId(long p_companyId)
    {
        m_companyId = p_companyId;
    }

    /**
     * Set the character set of the file profile
     * 
     * @param p_code_set
     *            The character set of the file profile
     */
    public void setCodeSet(String p_code_set)
    {
        m_code_set = p_code_set;
    }

    /**
     * Set the id of the XML Rule File
     * 
     * @param p_xmlRuleFileId
     *            The ID of the XML Rule File
     */
    public void setXmlRuleFileId(long p_xmlRuleFileId)
    {
        if (p_xmlRuleFileId == 0)
        {
            m_xmlRuleFileId = null;
        }
        else
        {
            m_xmlRuleFileId = new Long(p_xmlRuleFileId);
        }
    }

    /**
     * Set the script on export
     * 
     * @param p_scriptOnExport
     */
    public void setScriptOnExport(String p_scriptOnExport)
    {
        m_scriptOnExport = p_scriptOnExport;
    }

    /**
     * Set the script on import
     * 
     * @param p_scriptOnImport
     */
    public void setScriptOnImport(String p_scriptOnImport)
    {
        m_scriptOnImport = p_scriptOnImport;
    }

    /** Returns a string representation of the object */
    public String toString()
    {
        return m_name;
    }

    /**
     * Return a string representation of the object for debugging purposes.
     * 
     * @return a string representation of the object for debugging purposes.
     */
    public String toDebugString()
    {
        return super.toString()
                + " m_knownFormatTypeId="
                + Long.toString(m_knownFormatTypeId)
                + " m_l10nProfileId="
                + Long.toString(m_l10nProfileId)
                + " m_xmlRuleFileId="
                + (m_xmlRuleFileId == null ? "null" : m_xmlRuleFileId
                        .toString()) + " m_name="
                + (m_name == null ? "null" : m_name) + " m_description="
                + (m_description == null ? "null" : m_description)
                + " m_scriptOnImport="
                + (m_scriptOnImport == null ? "null" : m_scriptOnImport)
                + " m_scriptOnExport="
                + (m_scriptOnExport == null ? "null" : m_scriptOnExport)
                + " m_extensionIds="
                + (m_extensionIds == null ? "null" : m_extensionIds.toString());
    }

    public String getCode_set()
    {
        return m_code_set;
    }

    public void setCode_set(String m_code_set)
    {
        this.m_code_set = m_code_set;
    }

    public long getXmlRuleId()
    {
        if (filterId > -2)
        {
            ArrayList<Filter> filters = FilterHelper.getFiltersByKnownFormatId(
                    m_knownFormatTypeId, m_companyId);
            Filter filter = FilterHelper.getFilterById(filters, filterId);
            if (filter == null)
            {
                return 0;
            }

            if (filter instanceof XMLRuleFilter)
            {
                long xmlRuleId = ((XMLRuleFilter) filter).getXmlRuleId();
                if (xmlRuleId > 0)
                    return xmlRuleId;
            }

            return 0;
        }

        return getXmlRuleFileId();
    }

    public void setXmlRuleId(Long xmlRuleId)
    {
        this.m_xmlRuleFileId = xmlRuleId;
    }

    public boolean translateHeader()
    {

        if (filterId > -2)
        {
            // The filter have been take effect.
            ArrayList<Filter> filters = FilterHelper.getFiltersByKnownFormatId(
                    m_knownFormatTypeId, m_companyId);
            Filter filter = FilterHelper.getFilterById(filters, filterId);
            if (filter == null)
            {
                return false;
            }
            else
            {
                return (filter instanceof MSOfficeDocFilter) ? ((MSOfficeDocFilter) filter)
                        .isHeaderTranslate() : false;
            }
        }
        else
        {
            // The former logic
            return getHeaderTranslate();
        }
    }

    /**
     * @deprecated Only used by hibernate. Use
     *             {@link FileProfileImpl#translateHeader()} instead.
     */
    boolean getHeaderTranslate()
    {
        return headerTranslate;
    }

    public void setHeaderTranslate(Boolean headerTranslate)
    {
        if (headerTranslate == null)
        {
            this.headerTranslate = false;
        }
        else
        {
            this.headerTranslate = headerTranslate;
        }
    }

    public boolean supportsSid()
    {
        if (filterId > -2)
        {
            // The filter have been take effect.
            ArrayList<Filter> filters = FilterHelper.getFiltersByKnownFormatId(
                    m_knownFormatTypeId, m_companyId);
            Filter filter = FilterHelper.getFilterById(filters, filterId);
            if (filter == null)
            {
                return false;
            }
            else
            {
                return (filter instanceof JavaPropertiesFilter) ? ((JavaPropertiesFilter) filter)
                        .getEnableSidSupport() : false;
            }
        }
        else
        {
            // The former logic
            return getSupportSid();
        }
    }

    /**
     * @deprecated Only used for Hibernate.
     * 
     *             Use {{@link #supportsSid()} instead.
     */
    boolean getSupportSid()
    {
        return supportSid;
    }

    public void setSupportSid(Boolean supportSid)
    {
        if (supportSid == null)
        {
            this.supportSid = false;
        }
        else
        {
            this.supportSid = supportSid;
        }
    }

    public boolean supportsUnicodeEscape()
    {
        if (filterId > -2)
        {
            // The filter have been take effect.
            ArrayList<Filter> filters = FilterHelper.getFiltersByKnownFormatId(
                    m_knownFormatTypeId, m_companyId);
            Filter filter = FilterHelper.getFilterById(filters, filterId);
            if (filter == null)
            {
                return false;
            }
            else if (filter instanceof JavaPropertiesFilter)
            {
                return ((JavaPropertiesFilter) filter).getEnableUnicodeEscape();
            }
            else if (filter instanceof JavaScriptFilter)
            {
                return ((JavaScriptFilter) filter).getEnableUnicodeEscape();
            }
            else
            {
                return false;
            }
        }
        else
        {
            // The former logic
            return getUnicodeEscape();
        }
    }

    /**
     * @deprecated Only used for Hibernate
     * 
     *             Use {@link FileProfileImpl#supportsUnicodeEscape()} instead.
     */
    boolean getUnicodeEscape()
    {
        return unicodeEscape;
    }

    public void setUnicodeEscape(Boolean unicodeEscape)
    {
        if (unicodeEscape == null)
        {
            this.unicodeEscape = false;
        }
        else
        {
            this.unicodeEscape = unicodeEscape;
        }
    }

    // This one is not mapped by Hibernate, so it doesn't matter
    // that it calls the database.
    public boolean getEntityEscape()
    {
        if (filterId > -2)
        {
            // The filter have been take effect.
            ArrayList<Filter> filters = FilterHelper.getFiltersByKnownFormatId(
                    m_knownFormatTypeId, m_companyId);
            Filter filter = FilterHelper.getFilterById(filters, filterId);
            if (filter == null)
            {
                return false;
            }
            else
            {
                return (filter instanceof JSPFilter) ? ((JSPFilter) filter)
                        .getEnableEscapeEntity() : false;
            }
        }
        else
        {
            // The former logic
            return entityEscape;
        }

    }

    public void setEntityEscape(Boolean entityEscape)
    {
        if (entityEscape == null)
        {
            this.entityEscape = false;
        }
        else
        {
            this.entityEscape = entityEscape;
        }
    }

    /**
     * @deprecated Only used for Hibernate Use
     *             {@link #getJavascriptFilterRegex()} instead.
     */
    String getJsFilterRegex()
    {
        return jsFilterRegex;
    }

    public String getJavascriptFilterRegex()
    {

        if (filterId > -2)
        {
            // The filter have been take effect.
            ArrayList<Filter> filters = FilterHelper.getFiltersByKnownFormatId(
                    m_knownFormatTypeId, m_companyId);
            Filter filter = FilterHelper.getFilterById(filters, filterId);
            if (filter == null)
            {
                return null;
            }
            else
            {
                return (filter instanceof JavaScriptFilter) ? ((JavaScriptFilter) filter)
                        .getJsFunctionText() : null;
            }
        }
        else
        {
            // The former logic
            return getJsFilterRegex();
        }
    }

    public void setJsFilterRegex(String jsFilterRegex)
    {
        this.jsFilterRegex = jsFilterRegex;
    }

    @Override
    public long getXmlDtdId()
    {
        long id = -1;
        if (xmlDtd != null)
        {
            id = xmlDtd.getId();
        }

        return id;
    }

    public XmlDtdImpl getXmlDtd()
    {
        return xmlDtd;
    }

    public void setXmlDtd(XmlDtdImpl xmlDtd)
    {
        this.xmlDtd = xmlDtd;
    }

    public long getFilterId()
    {
        return filterId;
    }

    public void setFilterId(long filterId)
    {
        this.filterId = filterId;
    }

    public String getFilterTableName()
    {
        return filterTableName;
    }

    public void setFilterTableName(String filterTableName)
    {
        this.filterTableName = filterTableName;
    }

    /**
     * Get secondary filter Id for current filter. Default -2.
     * 
     * @return long
     */
    public long getSecondFilterId()
    {
        long secondFilterId = -2;
        if (filterId > 0)
        {
            try
            {
                Filter filter = FilterHelper.getFilter(filterTableName,
                        filterId);
                if (filter != null)
                {
                    if (filter instanceof JavaPropertiesFilter)
                    {
                        secondFilterId = ((JavaPropertiesFilter) filter)
                                .getSecondFilterId();
                    }
                    else if (filter instanceof XMLRuleFilter)
                    {
                        secondFilterId = ((XMLRuleFilter) filter)
                                .getSecondFilterId();
                    }
                    else if (filter instanceof POFilter)
                    {
                        secondFilterId = ((POFilter) filter)
                                .getSecondFilterId();
                    }
                }
            }
            catch (Exception e)
            {
                s_logger.error(e.getMessage(), e);
            }
        }

        return secondFilterId;
    }

    /**
     * There are some problems with second filter. Because time is limited, only
     * some file types will be deal with.
     * 
     * @return true if the file type is not deal with or false if the file type
     *         has been deal with.
     */
    public boolean isExtractWithSecondFilter()
    {
        if (filterId < 1)
        {
            return true;
        }

        try
        {
            Filter filter = FilterHelper.getFilter(filterTableName, filterId);

            if (filter instanceof MSOfficeExcelFilter)
            {
                return false;
            }

            if (filter instanceof MSOfficeDocFilter)
            {
                return false;
            }

            if (filter instanceof MSOfficePPTFilter)
            {
                return false;
            }
        }
        catch (Exception e)
        {
            s_logger.error(e.getMessage(), e);
        }

        return true;
    }

    /**
     * Get secondary filter table name for current filter.
     * 
     * @return
     */
    public String getSecondFilterTableName()
    {
        String secondFilterTableName = null;
        if (filterId > 0)
        {
            try
            {
                Filter filter = FilterHelper.getFilter(filterTableName,
                        filterId);
                if (filter != null)
                {
                    if (filter instanceof JavaPropertiesFilter)
                    {
                        secondFilterTableName = ((JavaPropertiesFilter) filter)
                                .getSecondFilterTableName();
                    }
                    else if (filter instanceof XMLRuleFilter)
                    {
                        secondFilterTableName = ((XMLRuleFilter) filter)
                                .getSecondFilterTableName();
                    }
                    else if (filter instanceof POFilter)
                    {
                        secondFilterTableName = ((POFilter) filter)
                                .getSecondFilterTableName();
                    }
                }
            }
            catch (Exception e)
            {
                s_logger.error(e.getMessage(), e);
            }
        }

        return secondFilterTableName;
    }

    public String getFilterName()
    {
        String name = "";
        if (filterId > 0)
        {
            try
            {
                Filter filter = FilterHelper.getFilter(filterTableName,
                        filterId);
                if (filter != null)
                {
                    name = filter.getFilterName();
                }
            }
            catch (Exception e)
            {
                s_logger.error(e.getMessage(), e);
            }
        }

        return name;
    }

    public void setPreserveSpaces(Boolean p_preserveSpaces)
    {
        if (p_preserveSpaces == null)
        {
            this.preserveSpaces = false;
        }
        else
        {
            this.preserveSpaces = p_preserveSpaces;
        }
    }

    public boolean getPreserveSpaces()
    {
        if (filterId > 0)
        {
            // The filter have been take effect.
            ArrayList<Filter> filters = FilterHelper.getFiltersByKnownFormatId(
                    m_knownFormatTypeId, m_companyId);
            Filter filter = FilterHelper.getFilterById(filters, filterId);
            if (filter == null)
            {
                return false;
            }
            else
            {
                return (filter instanceof JavaPropertiesFilter) ? ((JavaPropertiesFilter) filter)
                        .getEnablePreserveSpaces() : false;
            }
        }
        else
        {
            // The former logic
            return this.preserveSpaces;
        }
    }

    public int getTerminologyApproval()
    {
        return this.terminology_approval;
    }

    public void setTerminologyApproval(int flag)
    {
        this.terminology_approval = flag;
    }

    public int getXlfSourceAsUnTranslatedTarget()
    {
        return this.xlfSourceAsUnTranslatedTarget;
    }

    public void setXlfSourceAsUnTranslatedTarget(int flag)
    {
        this.xlfSourceAsUnTranslatedTarget = flag;
    }

    public long getReferenceFP()
    {
        return reference_fp;
    }

    public void setReferenceFP(long reference_fp)
    {
        this.reference_fp = reference_fp;
    }

    public Long getNewId()
    {
        return newId;
    }

    public void setNewId(Long oldId)
    {
        this.newId = oldId;
    }

}
