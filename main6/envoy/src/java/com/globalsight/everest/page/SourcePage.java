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

// globalsight
import java.io.File;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import com.globalsight.cxe.adapter.passolo.PassoloUtil;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import com.globalsight.cxe.persistence.databaseprofile.DatabaseProfilePersistenceManager;
import com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException;
import com.globalsight.cxe.persistence.fileprofile.FileProfilePersistenceManager;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

/**
 * SourcePage is the object representation of the source page imported into the
 * system.
 */
public class SourcePage extends Page
{
    private static final long serialVersionUID = 6153911482019625264L;

    /**
     * Constant.
     */
    public static final String REQUEST = "m_request";
    public static final String COMPANY_ID = "m_companyId";

    // the word count of the page
    // is NULL if the word count hasn't been determined yet
    // or possibly if it is overriden
    private Integer m_wordCount = null;
    // a word count that has been manually overriden.
    // if NULL then it hasn't been overriden
    private Integer m_overrideWordCount = null;

    private Long m_previousPageId = null;
    private Request m_request = null;

    // id of the company which this activity belong to
    private long m_companyId;
    private Set<TargetPage> targetPages = null;

    private int BOMType = 0;
    private long jobId = -1;

    /**
     * Get name of the company this activity belong to.
     * 
     * @return The company name.
     */
    public long getCompanyId()
    {
        return this.m_companyId;
    }

    /**
     * Get name of the company this activity belong to.
     * 
     * @return The company name.
     */
    public void setCompanyId(long p_companyId)
    {
        this.m_companyId = p_companyId;
    }

    /**
     * Looks up the known format type for the associated file profile and
     * returns true if it's an MS Office document.
     * 
     * @exception Exception
     */
    public static boolean isMicrosoftOffice(SourcePage p_sourcePage)
            throws Exception
    {
        long fileProfileId = p_sourcePage.getRequest().getDataSourceId();
        KnownFormatType format = getFormatTypeByFpId(fileProfileId);
        String name = format.getName();

        if (name.equals(KnownFormatType.WORD)
                || name.equals(KnownFormatType.POWERPOINT)
                || name.equals(KnownFormatType.PDF)
                || name.equals(KnownFormatType.EXCEL))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static KnownFormatType getFormatTypeByFpId(long fpId)
            throws FileProfileEntityException, RemoteException,
            GeneralException, NamingException
    {
        FileProfile fp = (FileProfile) HibernateUtil.get(FileProfileImpl.class,
                fpId, false);
        KnownFormatType format = ServerProxy.getFileProfilePersistenceManager()
                .queryKnownFormatType(fp.getKnownFormatTypeId());
        return format;
    }

    /**
     * Looks up the known format type for the associated file profile and
     * returns true if it's a FrameMaker document.
     * 
     * @exception Exception
     */
    public static boolean isFrameMaker(SourcePage p_sourcePage)
            throws Exception
    {
        long fileProfileId = p_sourcePage.getRequest().getDataSourceId();
        KnownFormatType format = getFormatTypeByFpId(fileProfileId);

        String name = format.getName();

        if (name.equals(KnownFormatType.FRAME5)
                || name.equals(KnownFormatType.FRAME6)
                || name.equals(KnownFormatType.FRAME7))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Looks up the known format type for the associated file profile and
     * returns true if it's a native RTF document.
     * 
     * @exception Exception
     */
    public static boolean isNativeRtf(SourcePage p_sourcePage) throws Exception
    {
        long fileProfileId = p_sourcePage.getRequest().getDataSourceId();
        KnownFormatType format = getFormatTypeByFpId(fileProfileId);

        String name = format.getName();

        if (name.equals(KnownFormatType.RTF))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Constructor.
     */
    public SourcePage()
    {
        super();
    }

    /**
     * Constructor to create the source page with the correct primary file.
     */
    public SourcePage(int p_primaryFileType)
    {
        super(p_primaryFileType);
    }

    /**
     * Constructed through PageManager.
     * 
     * @param p_externalPageId
     *            - The external id of the page.
     * @param p_globalSightLocale
     *            - The locale of the page.
     * @param p_originalEncoding
     *            - The original encoding of the page.
     * @param p_dataSourceType
     *            - The datasource type of the page (i.e. file system, db, cms).
     * @param p_dataType
     *            - The data type of the page (i.e. HTML, CSS, and etc.).
     * @param p_wordCount
     *            - The word count of the page.
     * @param p_containGsTags
     *            - A boolean specifying if the page contains any GS tags
     *            (add/delete)
     */
    SourcePage(String p_externalPageId, GlobalSightLocale p_globalSightLocale,
            String p_dataSourceType, int p_wordCount, int p_pageType)
    {
        super(p_externalPageId, p_globalSightLocale, p_dataSourceType,
                p_pageType);

        m_wordCount = new Integer(p_wordCount);
    }

    SourcePage(String p_externalPageId, GlobalSightLocale p_globalSightLocale,
            String p_dataSourceType, int p_wordCount, int p_BOMType,
            int p_pageType)
    {
        super(p_externalPageId, p_globalSightLocale, p_dataSourceType,
                p_pageType);

        m_wordCount = new Integer(p_wordCount);
        BOMType = p_BOMType;
    }

    //
    // Abstract Methods Implementation
    //

    /**
     * Get the locale id of the source page.
     * 
     * @return the Locale id of the page.
     */
    public long getLocaleId()
    {
        if (getGlobalSightLocale() == null)
        {
            return -1;
        }

        return getGlobalSightLocale().getId();
    }

    //
    // Helper Methods
    //

    /**
     * Clone the current instance to create a new instance of source page.
     * 
     * @return a new instance of current page.
     */
    public SourcePage cloneSourcePage()
    {
        SourcePage newPage = new SourcePage(getExternalPageId(),
                getGlobalSightLocale(), getDataSourceType(), getWordCount(),
                getPrimaryFileType());
        PrimaryFile thisPf = getPrimaryFile();
        PrimaryFile newPf = thisPf.clonePrimaryFile();
        newPage.setPrimaryFile(newPf);
        return newPage;
    }

    /**
     * Get the locale of this page.
     * 
     * @return The locale of this page.
     */
    public GlobalSightLocale getGlobalSightLocale()
    {
        if (m_globalSightLocale == null)
        {
            if (getRequest() == null || getRequest().getL10nProfile() == null)
            {
                return null;
            }

            m_globalSightLocale = getRequest().getL10nProfile()
                    .getSourceLocale();
        }

        return m_globalSightLocale;
    }

    /**
     * Set the word count of the page. Does not clear out the overriden one if
     * there is one. This needs to be cleared out by a separate call.
     * 
     * @param p_word
     *            the word count of the page.
     */
    public void setWordCount(int p_wordCount)
    {
        m_wordCount = new Integer(p_wordCount);
    }

    /**
     * Override the calculated word count with a manually- user specified one.
     */
    public void overrideWordCount(int p_wordCount)
    {
        m_overrideWordCount = new Integer(p_wordCount);
    }

    /**
     * Clear out the override word count, so it'll use the original calculated
     * value.
     */
    public void clearOverridenWordCount()
    {
        m_overrideWordCount = null;
    }

    /**
     * Return 'true' if the word count has been overriden. 'false' if it isn't
     * overriden.
     */
    public boolean isWordCountOverriden()
    {
        if (m_overrideWordCount != null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Get the word count of the page.
     * 
     * @return The page's word count.
     */
    public int getWordCount()
    {
        int wordCount = 0;

        // if the count is overriden then return
        if (isWordCountOverriden())
        {
            wordCount = m_overrideWordCount.intValue();
        }
        else if (m_wordCount != null)
        {
            wordCount = m_wordCount.intValue();
        }

        return wordCount;
    }

    /**
     * Get the request object that this page belongs to.
     * 
     * @return The request for this source page.
     */
    public Request getRequest()
    {
        if (m_request == null && getId() > 0)
        {
            String hql = "from RequestImpl r where r.pageId = :id";
            Map map = new HashMap();
            map.put("id", getIdAsLong());
            List requests = HibernateUtil.search(hql, map);
            if (requests.size() > 0)
            {
                m_request = (RequestImpl) requests.get(0);
            }
        }
        return m_request;
    }

    /**
     * Set the request that initiated this Page creation.
     */
    public void setRequest(Request p_request)
    {
        m_request = p_request;
    }

    /**
     * Return a string representation of the object.
     * 
     * @return a string representation of the object.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append(super.toString());
        sb.append(" sourceLocale=");
        sb.append((getGlobalSightLocale() != null ? getGlobalSightLocale()
                .toDebugString() : "null"));
        sb.append(" m_wordCount=");
        sb.append((m_wordCount != null ? m_wordCount.toString() : "null"));
        sb.append(" m_overrideWordCount=");
        sb.append((m_overrideWordCount != null ? m_overrideWordCount.toString()
                : "null"));
        sb.append(" m_previousPageId=");
        sb.append((m_previousPageId != null ? m_previousPageId.toString()
                : "null"));
        sb.append("\nm_request=");
        sb.append((getRequest() != null ? getRequest().toString() : "null"));
        sb.append("\n");

        return sb.toString();
    }

    /**
     * set the previous page id for this page.
     */
    public void setPreviousPageId(long p_previousPageId)
    {
        m_previousPageId = new Long(p_previousPageId);
    }

    /**
     * get the previous page id.
     */
    public long getPreviousPageId()
    {
        return m_previousPageId == null ? 0 : m_previousPageId.longValue();
    }

    /**
     * set the previous page id for this page.
     */
    public void setPreviouPageId(Long p_previousPageId)
    {
        m_previousPageId = p_previousPageId;
    }

    /**
     * get the previous page id.
     */
    public Long getPreviouPageId()
    {
        return m_previousPageId;
    }

    //
    // Protected Methods
    //
    protected void createPrimaryFile(int p_type)
    {
        switch (p_type)
        {
            case PrimaryFile.UNEXTRACTED_FILE:
                m_unextractedFile = new UnextractedFile();
                m_unextractedFile.setLastModifiedDate(new Date());
                break;
            case PrimaryFile.EXTRACTED_FILE:
                // fall through to default
            default: // assume extracted file
                m_extractedFile = new ExtractedSourceFile();
                break;
        }
    }

    public Integer getOverrideWordCount()
    {
        return m_overrideWordCount;
    }

    public void setOverrideWordCount(Integer wordCount)
    {
        m_overrideWordCount = wordCount;
    }

    public File getFile()
    {
        String filePath = AmbFileStoragePathUtils.getCxeDocDirPath(String
                .valueOf(this.getCompanyId()))
                + File.separator
                + filtSpecialFile(getExternalPageId());
        File file = new File(filePath);

        if (!file.exists())
        {
            XmlEntities entity = new XmlEntities();
            File dir = file.getParentFile();
            if (dir.isDirectory())
            {
                File[] files = dir.listFiles();
                for (File f : files)
                {
                    if (file.getName().equals(
                            entity.decodeStringBasic(f.getName())))
                    {
                        file = f;
                        break;
                    }
                }
            }
        }

        if (!file.exists())
        {
            if (PassoloUtil.isPassoloFile(this))
            {
                String name = getPassoloFileName();
                String href = getExtractedFile().getExternalBaseHref();
                filePath = AmbFileStoragePathUtils.getCxeDocDirPath(String
                        .valueOf(this.getCompanyId()))
                        + File.separator
                        + href
                        + File.separator + name;
                file = new File(filePath);

                if (!file.exists())
                {
                    file = null;
                }
            }
            else
            {
                file = null;
            }
        }

        return file;
    }

    public File getFileByPageCompanyId()
    {
        String filePath = AmbFileStoragePathUtils.getCxeDocDirPath(String
                .valueOf(m_companyId))
                + File.separator
                + filtSpecialFile(getExternalPageId());
        File file = new File(filePath);
        if (!file.exists())
        {
            file = null;
        }

        return file;
    }

    /**
     * Convert "(Adobe file information) indd\myInddFile.indd" into
     * "indd\myInddFile.indd" or "(header) word\myWordFile.doc" into
     * "word\myWordFile.doc"
     * 
     * @param p_fileName
     *            sourcePage.getExternalPageId() or eventFlow.getDisplayName()
     * @return the real file name
     */
    public static String filtSpecialFile(String p_fileName)
    {
        String result = null;

        String externalPageIdSuffix = null;
        if (p_fileName != null && p_fileName.trim().length() > 0)
        {
            int dotIndex = p_fileName.lastIndexOf('.');
            if (dotIndex > -1)
            {
                externalPageIdSuffix = p_fileName.substring(dotIndex);
            }
        }

        if (".ppt".equalsIgnoreCase(externalPageIdSuffix)
                || ".doc".equalsIgnoreCase(externalPageIdSuffix)
                || ".xls".equalsIgnoreCase(externalPageIdSuffix)
                || ".rtf".equalsIgnoreCase(externalPageIdSuffix)
                || ".pptx".equalsIgnoreCase(externalPageIdSuffix)
                || ".docx".equalsIgnoreCase(externalPageIdSuffix)
                || ".xlsx".equalsIgnoreCase(externalPageIdSuffix)
                || ".indd".equalsIgnoreCase(externalPageIdSuffix)
                || ".odt".equalsIgnoreCase(externalPageIdSuffix)
                || ".odp".equalsIgnoreCase(externalPageIdSuffix)
                || ".ods".equalsIgnoreCase(externalPageIdSuffix))
        {
            int index_ = p_fileName.indexOf(')');
            if ((p_fileName.trim().indexOf('(') == 0) && index_ > 0)
            {
                result = p_fileName.substring(index_ + 1).trim();
            }
        }

        return (result == null) ? p_fileName : result;
    }

    public Set<TargetPage> getTargetPages()
    {
        return targetPages;
    }

    public TargetPage getTargetPageByLocaleId(long p_targetLocaleId)
    {
        TargetPage targetPage = null;
        if (targetPages != null && targetPages.size() > 0)
        {
            for (TargetPage tp : targetPages)
            {
                if (tp.getLocaleId() == p_targetLocaleId)
                {
                    targetPage = tp;
                    break;
                }
            }            
        }

        return targetPage;
    }

    public void setTargetPages(Set<TargetPage> targetPages)
    {
        this.targetPages = targetPages;
    }

    private DatabaseProfilePersistenceManager getDBProfilePersistenceManager()
            throws Exception
    {
        return ServerProxy.getDatabaseProfilePersistenceManager();
    }

    private FileProfilePersistenceManager getFileProfilePersistenceManager()
            throws Exception
    {
        return ServerProxy.getFileProfilePersistenceManager();
    }

    public String getDataSource()
    {
        String dataSourceType = getDataSourceType();
        long dataSourceId = getRequest().getDataSourceId();

        String currentRetString;
        try
        {
            if (dataSourceType.equals("db"))
            {
                currentRetString = getDBProfilePersistenceManager()
                        .getDatabaseProfile(dataSourceId).getName();
            }
            else
            {
                currentRetString = getFileProfilePersistenceManager()
                        .readFileProfile(dataSourceId).getName();
            }
        }
        catch (Exception e)
        {
            currentRetString = "Unknown";
        }
        return currentRetString;
    }

    public String getPassoloFileName()
    {
        String href = getExtractedFile().getExternalBaseHref();
        String allPath = getExternalPageId().replace("\\", "/");
        String temp = allPath.substring(href.length());
        int index = temp.indexOf("/");
        return temp.substring(0, index);
    }

    public String getPassoloFilePath()
    {
        String name = getPassoloFileName();
        String href = getExtractedFile().getExternalBaseHref();
        return href + File.separator + name;
    }

    public boolean hasRemoved()
    {
        if (PassoloUtil.isPassoloFile(this))
        {
            Set<TargetPage> tps = getTargetPages();
            for (TargetPage tp : tps)
            {
                Workflow w = tp.getWorkflowInstance();
                if (w != null)
                {
                    String state = w.getState();
                    if (!Workflow.CANCELLED.equals(state))
                    {
                        return false;
                    }
                }

            }

            return true;
        }

        return false;
    }

    /**
     * @param bOMType
     *            the bOMType to set
     */
    public void setBOMType(int bOMType)
    {
        BOMType = bOMType;
    }

    /**
     * @return the bOMType
     */
    public int getBOMType()
    {
        return BOMType;
    }

    public boolean isPassoloPage()
    {
        return PassoloUtil.isPassoloFile(this);
    }

    public void setJobId(long p_jobId)
    {
        this.jobId = p_jobId;
    }

    public long getJobId()
    {
        return this.jobId;
    }
}
