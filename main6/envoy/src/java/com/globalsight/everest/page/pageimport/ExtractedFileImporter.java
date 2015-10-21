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
package com.globalsight.everest.page.pageimport;

// globalsight - general
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.adobe.InddTuMappingHelper;
import com.globalsight.cxe.adapter.passolo.PassoloUtil;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.ExtractedFile;
import com.globalsight.everest.page.ExtractedSourceFile;
import com.globalsight.everest.page.PageEventObserver;
import com.globalsight.everest.page.PageException;
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.PageTemplate;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.pageimport.optimize.Office2Optimizer;
import com.globalsight.everest.page.pageimport.optimize.OptimizeUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuTuvIndexUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuTuvPersistence;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.util.Tmx;
import com.globalsight.everest.tuv.CustomTuType;
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.tuv.RemovedTag;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuType;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvException;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.common.DiplomatNames;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.extractor.msoffice2010.WordExtractor;
import com.globalsight.ling.docproc.extractor.xliff.Extractor;
import com.globalsight.ling.docproc.extractor.xliff.XliffAlt;
import com.globalsight.ling.docproc.extractor.xliff20.XliffHelper;
import com.globalsight.ling.tm.ExactMatchedSegments;
import com.globalsight.ling.tm.Leverager;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.ling.tm.TargetLocaleLgIdsMapper;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.termleverager.TermLeverageOptions;
import com.globalsight.terminology.termleverager.TermLeverageResult;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlException;
import com.globalsight.util.gxml.GxmlFragmentReader;
import com.globalsight.util.gxml.GxmlFragmentReaderPool;
import com.globalsight.util.gxml.GxmlNames;
import com.globalsight.util.gxml.GxmlRootElement;
import com.globalsight.util.system.ConfigException;

/**
 * <p>
 * Performs the importing of a file that was extracted. Parses through the gxml
 * and creates a source page with an extracted file and all its segments from
 * the request.
 * </p>
 */
public class ExtractedFileImporter extends FileImporter
{
    static private Boolean s_autoReplaceTerms = setTermLeverageOptions();
    private static Logger c_logger = Logger
            .getLogger(ExtractedFileImporter.class.getName());
    private static XmlEntities m_xmlDecoder = new XmlEntities();
    private static Pattern p_fileProfileId = Pattern
            .compile("<fileProfileId>([\\d\\D]*)</fileProfileId>");

    public static String EMPTYTARGET = "empty target";
    boolean haveInddPageNum = false;

    //
    // Constructor
    //
    ExtractedFileImporter()
    {
        super();
    }

    //
    // Package Private Methods
    //

    /**
     * Imports the request and create Source and Target Pages from the request
     * information.
     * 
     * @return A hash map of all the pages (Source and Target) that were
     *         created. The key for the hash map is the id of the page's
     *         GlobalSightLocale.
     */
    @Override
    HashMap importFile(Request p_request) throws FileImportException
    {
        HashMap pages = new HashMap();
        SourcePage sourcePage = null;
        Collection targetPages = null;
        FileImportException exception = null;

        c_logger.info("Importing page: " + p_request.getExternalPageId());
        long start_PERFORMANCE = System.currentTimeMillis();
        long jobId = getJobIdFromEventFlowXml(p_request);
        try
        {
            long time_PERFORMANCE = System.currentTimeMillis();
            sourcePage = createSourcePage(p_request, jobId);
            pages.put(sourcePage.getGlobalSightLocale().getIdAsLong(),
                    sourcePage);

            if (c_logger.isDebugEnabled())
            {
                c_logger.debug("Performance:: Creating source page time = "
                        + +(System.currentTimeMillis() - time_PERFORMANCE)
                        + " " + p_request.getExternalPageId());
            }

            if (p_request.getType() == Request.EXTRACTED_LOCALIZATION_REQUEST)
            {
                ExactMatchedSegments exactMatchedSegments = null;

                if (p_request.getL10nProfile().getTmChoice() != L10nProfile.NO_TM)
                {
                    c_logger.info("TM leveraging for page: "
                            + p_request.getExternalPageId());
                    time_PERFORMANCE = System.currentTimeMillis();

                    exactMatchedSegments = leveragePage(p_request, sourcePage,
                            jobId);

                    if (c_logger.isDebugEnabled())
                    {
                        c_logger.debug("Performance:: TM leveraging time = "
                                + +(System.currentTimeMillis() - time_PERFORMANCE)
                                + " " + p_request.getExternalPageId());
                    }
                }

                c_logger.info("Term leveraging for page: "
                        + p_request.getExternalPageId());
                time_PERFORMANCE = System.currentTimeMillis();

                TermLeverageResult termMatches = leverageTermsForPage(
                        p_request, sourcePage, jobId);

                if (c_logger.isDebugEnabled())
                {
                    c_logger.debug("Performance:: Term leveraging time = "
                            + +(System.currentTimeMillis() - time_PERFORMANCE)
                            + " " + p_request.getExternalPageId());
                }

                c_logger.info("Creating target pages for page: "
                        + p_request.getExternalPageId());
                time_PERFORMANCE = System.currentTimeMillis();

                targetPages = importTargetPages(p_request, sourcePage,
                        termMatches, exactMatchedSegments);

                for (Iterator it = targetPages.iterator(); it.hasNext();)
                {
                    TargetPage tp = (TargetPage) it.next();
                    pages.put(tp.getGlobalSightLocale().getIdAsLong(), tp);
                }
                pages = removeUnActiveWorkflowTargetPages(pages,
                        p_request.getL10nProfile());
                if (c_logger.isDebugEnabled())
                {
                    c_logger.debug("Performance:: Creating target pages time = "
                            + +(System.currentTimeMillis() - time_PERFORMANCE)
                            + " " + p_request.getExternalPageId());
                }
            }

            // go through target pages and only send the ones that
            // aren't already set to IMPORT_FAIL
            ArrayList successfulTargetPages = new ArrayList();
            for (Iterator tpi = targetPages.iterator(); tpi.hasNext();)
            {
                TargetPage tp = (TargetPage) tpi.next();
                if (!tp.getPageState().equals(PageState.IMPORT_FAIL))
                {
                    successfulTargetPages.add(tp);
                }
            }

            PageEventObserver peo = getPageEventObserver();

            if (p_request.getType() == Request.EXTRACTED_LOCALIZATION_REQUEST
                    && successfulTargetPages.size() > 0)
            {
                peo.notifyImportSuccessEvent(sourcePage, successfulTargetPages);
            }
            else if (p_request.getType() == Request.EXTRACTED_LOCALIZATION_REQUEST)
            {
                // no successful target pages but the request isn't marked
                // as a failure yet so set the exception in it

                c_logger.info("Import failed - updating the state.");

                // set the entire request to a failure since all target pages
                // failed
                setExceptionInRequest(
                        p_request,
                        new FileImportException(
                                FileImportException.MSG_FAILED_IMPORT_ALL_TARGETS_ACTIVE,
                                null, null));

                peo.notifyImportFailEvent(sourcePage, targetPages);
            }
            else
            {
                // the request has been marked so just fail the import

                c_logger.info("Import failed - updating the state.");

                peo.notifyImportFailEvent(sourcePage, targetPages);
            }
        }

        catch (FileImportException pe)
        {
            exception = pe;
        }
        catch (GeneralException ge)
        {
            String[] args =
            { Long.toString(p_request.getId()), p_request.getExternalPageId() };

            exception = new FileImportException(
                    FileImportException.MSG_FAILED_TO_IMPORT_PAGE, args, ge);
        }
        catch (RemoteException re)
        {
            String[] args =
            { Long.toString(p_request.getId()), p_request.getExternalPageId() };

            exception = new FileImportException(
                    FileImportException.MSG_FAILED_TO_IMPORT_PAGE, args, re);
        }
        catch (Throwable ex)
        {
            c_logger.error(ex);
            String[] args =
            { Long.toString(p_request.getId()), p_request.getExternalPageId() };

            exception = new FileImportException(
                    FileImportException.MSG_FAILED_TO_IMPORT_PAGE, args,
                    new Exception(ex.toString()));
        }

        if (exception != null)
        {
            try
            {
                c_logger.error(
                        "Import failed for page "
                                + p_request.getExternalPageId() + "\n",
                        exception);

                setExceptionInRequest(p_request, exception);

                PageEventObserver peo = getPageEventObserver();
                peo.notifyImportFailEvent(sourcePage, targetPages);
            }
            catch (Throwable ex)
            {
                c_logger.error("Cannot mark pages as IMPORT_FAILED.", ex);
                throw exception;
            }
        }
        else
        {
            c_logger.info("Done importing page: "
                    + p_request.getExternalPageId());

            if (c_logger.isDebugEnabled())
            {
                c_logger.debug("Performance:: import time = "
                        + (System.currentTimeMillis() - start_PERFORMANCE)
                        + " " + p_request.getExternalPageId());
            }
        }

        return pages;
    }

    //
    // Private Methods
    //

    private HashMap removeUnActiveWorkflowTargetPages(HashMap pages,
            L10nProfile profile)
    {
        List<Long> ids = profile.getUnActivelocaleIds();
        HashMap newPages = pages;
        for (int i = 0; i < ids.size(); i++)
        {
            newPages.remove(ids.get(i));
        }
        return newPages;
    }

    /**
     * Creates the source page.
     */
    private SourcePage createSourcePage(Request p_request, long jobId)
            throws FileImportException
    {
        SourcePage page = null;
        String dataType = p_request.getDataSourceType();

        // There is no "db" dataSourceType in running at all(legacy code).
        if (dataType.equals("db"))
        {
            throw new FileImportException(new Exception(
                    "Request type 'db' is not supported any longer."));
        }
        else
        {
            page = createPageFromGxml(p_request, jobId);
        }

        return page;
    }

    /**
     * Creates Page, Leverage Groups, Tus and Tuvs
     */
    private SourcePage createPageFromGxml(Request p_request, long jobId)
            throws FileImportException
    {
        long companyId = p_request.getCompanyId();

        SourcePage srcPage = null;
        GxmlRootElement gxmlRootElement = null;

        try
        {
            gxmlRootElement = parseGxml(p_request);
        }
        catch (FileImportException ge)
        {
            srcPage = createPage(p_request, gxmlRootElement, jobId);
            return srcPage;
        }

        srcPage = createPage(p_request, gxmlRootElement, jobId);
        ExtractedSourceFile esf = null;

        try
        {
            ArrayList<Tu> tus;
            ArrayList<Tuv> srcTuvs;

            esf = getExtractedSourceFile(srcPage);
            setAttributesOfExtractedFile(p_request, esf, gxmlRootElement);
            tus = createTUs(p_request, srcPage, gxmlRootElement, jobId);
            srcTuvs = createTUVs(p_request, tus, jobId);
            setExactMatchKeysForSrcTUVs(srcTuvs);
            srcPage.setCompanyId(companyId);

            // Re-calculate word-count from excluded items.
            if (srcTuvs != null && srcTuvs.size() > 0)
            {
                Tuv tuv = srcTuvs.get(0);
                String generateFrom = tuv.getTu(jobId).getGenerateFrom();
                boolean isWSXlf = TuImpl.FROM_WORLDSERVER
                        .equalsIgnoreCase(generateFrom);
                // World Server XLF segments DO NOT need this as WS XLF need
                // keep WC original info.
                if (!isWSXlf)
                {
                    updateWordCountForPage(p_request, srcPage, srcTuvs);
                }
            }

            // Set tuId and tuvId for all TUs and TUVs in this source page.
            setTuTuvIds(srcPage, jobId);

            // Generate templates
            List<PageTemplate> templates = generateTemplates(srcPage,
                    gxmlRootElement, tus);

            // Save source page, leverage group and page template.
            HibernateUtil.save(srcPage);

            // Save TU, TUV and related data.
            srcPage = SegmentTuTuvPersistence.saveTuTuvAndRelatedData(srcPage,
                    jobId);

            // Save templatePart data.
            for (PageTemplate pTemplate : templates)
            {
                HibernateUtil.save(pTemplate.getTemplateParts());
            }

            if (haveInddPageNum)
            {
                long srcPageId = srcPage.getId();

                for (Tu tu : tus)
                {
                    if (tu.getInddPageNum() != 0)
                    {
                        InddTuMappingHelper.saveMapping(jobId, srcPageId,
                                tu.getId(), companyId, tu.getInddPageNum());
                    }
                }
            }
        }
        catch (Exception e)
        {
            c_logger.error("Exception when creating TUVs in page import.", e);
            String[] args = new String[1];
            args[0] = Long.toString(p_request.getId());
            setExceptionInRequest(p_request,
                    new FileImportException(
                            FileImportException.MSG_FAILED_TO_ITERATE_GXML_DOC,
                            args, e));
        }
        finally
        {
            // assume this is an extracted file since in this importer
            esf.clearTemplateMap();
        }

        c_logger.info("Source page is created successfully for : "
                + p_request.getExternalPageId());

        return srcPage;
    }

    /**
     * This method creates a source page from the request and the xml root
     * element. It adds the page to the request and persists the relationship.
     * 
     * @param p_request
     *            The request to import a page for.
     * @return SourcePage The page for importing.
     * @throws FileImportException
     */
    private SourcePage createPage(Request p_request, GxmlRootElement p_element,
            long jobId) throws FileImportException
    {
        SourcePage srcPage = null;
        PageManager pm = getPageManager();
        // set defaults in case the page was not parsed succesfully
        // and an error page is being created.
        String pageDataType = "unknown";
        String gxmlVersion = "1.0";
        boolean containsGsTags = false;
        int wordCount = 0;

        // if the GXML was parsed successfully
        if (p_element != null)
        {
            pageDataType = p_element.getAttribute(GxmlNames.GXMLROOT_DATATYPE);
            containsGsTags = containGsTags(p_element);
            gxmlVersion = getGxmlVersion(p_element);
            wordCount = getPageWordCount(p_element);
        }

        try
        {
            srcPage = pm.getPageWithExtractedFileForImport(p_request, p_request
                    .getL10nProfile().getSourceLocale(), pageDataType,
                    wordCount, containsGsTags, gxmlVersion);
        }
        catch (PageException pe)
        {
            if (srcPage == null)
            {
                c_logger.error("Couldn't get/create the page for importing.",
                        pe);
                String[] args = new String[2];
                args[0] = Long.toString(p_request.getId());
                args[1] = p_request.getExternalPageId();
                throw new FileImportException(
                        FileImportException.MSG_FAILED_TO_IMPORT_PAGE, args, pe);
            }
            else
            {
                c_logger.error("Exception when adding the page to "
                        + "the request.", pe);
                String[] args = new String[2];
                args[0] = Long.toString(p_request.getId());
                args[1] = srcPage.getExternalPageId();
                throw new FileImportException(
                        FileImportException.MSG_FAILED_TO_ADD_PAGE_TO_REQUEST,
                        args, pe);
            }
        }
        srcPage.setJobId(jobId);

        return srcPage;
    }

    /**
     * This method parses the GXML obtained from CXE and creates a DOM tree
     * 
     * @return GxmlRootElement - the root of the DOM tree.
     * @throws FileImportException
     */
    private GxmlRootElement parseGxml(Request p_request)
            throws FileImportException
    {
        GxmlRootElement gxmlRootElement = null;
        GxmlFragmentReader reader = null;

        try
        {
            reader = GxmlFragmentReaderPool.instance().getGxmlFragmentReader();
            String gxml = readXmlFromFile(p_request.getGxml());
            // now set the GXML string value back into the request, it will not
            // be persisted, but will be used in a second
            p_request.setGxml(gxml);
            gxmlRootElement = reader.parse(gxml);
        }
        catch (GxmlException ge)
        {
            c_logger.error("Failed to parse the GXML.", ge);
            setExceptionInRequest(p_request, ge);
        }
        catch (IOException ioe)
        {
            c_logger.error("Failed to read the GXML from the temp file", ioe);
            GeneralException ge = new GeneralException(ioe);
            setExceptionInRequest(p_request, ge);
        }
        finally
        {
            if (reader != null)
            {
                GxmlFragmentReaderPool.instance()
                        .freeGxmlFragmentReader(reader);
            }
        }

        return gxmlRootElement;
    }

    /**
     * This method adds several attributes of the source page's extracted file
     * such as base href. This method also creates a LeverageGroup.
     */
    private void setAttributesOfExtractedFile(Request p_request,
            ExtractedSourceFile p_file, GxmlRootElement p_rootElement)
            throws FileImportException
    {
        try
        {
            if (p_request.getBaseHref() != null
                    && p_request.getBaseHref().length() > 0)
            {
                p_file.setExternalBaseHref(p_request.getBaseHref());
            }

            setInternalBaseHref(p_file, p_rootElement);
            p_file.addLeverageGroup(getPageManager().createLeverageGroup());
        }
        catch (Exception e)
        {
            throw new FileImportException(e);
        }
    }

    /**
     * This method creates TUs for localizable and translatable segments of the
     * XML tree.
     * 
     * @param p_request
     *            - The request for localization.
     * @param p_page
     *            - The page that the TUs are being created for/from.
     * @return java.util.List
     */
    private ArrayList<Tu> createTUs(Request p_request, SourcePage p_page,
            GxmlRootElement p_GxmlRootElement, long jobId)
            throws FileImportException
    {
        LeverageGroup lg = getLeverageGroupForPage(p_page);
        long tmId = getTMId(p_request);
        GlobalSightLocale sourceLocale = getSourceLocale(p_request);

        return createTUs_1(p_request, p_page, lg, tmId, sourceLocale,
                p_GxmlRootElement, new ArrayList<Tu>(), jobId);
    }

    private LeverageGroup getLeverageGroupForPage(SourcePage p_page)
    {
        // assume this is an extracted page
        return getExtractedSourceFile(p_page).getLeverageGroups().iterator()
                .next();
    }

    private long getTMId(Request p_request)
    {
        return p_request.getL10nProfile().getMainTmId();
    }

    private ArrayList<Tu> createTUs_1(Request p_request, SourcePage p_page,
            LeverageGroup p_lg, long p_tmId, GlobalSightLocale p_sourceLocale,
            GxmlElement p_GxmlElement, ArrayList<Tu> p_tuList, long jobId)
            throws FileImportException
    {
        if (p_GxmlElement == null)
        {
            return p_tuList;
        }

        Matcher m = p_fileProfileId.matcher(p_request.getEventFlowXml());
        String pageDataType = null;
        boolean isJavaProperties = false;
        boolean supportSid = false;
        String sid = null;

        try
        {
            pageDataType = p_GxmlElement
                    .getAttribute(GxmlNames.GXMLROOT_DATATYPE);

            if (IFormatNames.FORMAT_PASSOLO.equals(pageDataType))
                supportSid = true;

            if (m.find())
            {
                String fId = m.group(1);
                FileProfileImpl fileProfile = HibernateUtil.get(
                        FileProfileImpl.class, Long.parseLong(fId), false);
                supportSid = fileProfile.supportsSid();
                isJavaProperties = "javaprop".equals(pageDataType);
            }
        }
        catch (Exception e)
        {
            c_logger.error(e.getMessage(), e);
        }

        List elements = p_GxmlElement.getChildElements();
        // for xliff file
        String generatFrom = new String();
        HashMap<String, String> attributeMap = new HashMap<String, String>();
        IXliffTuCreation tuc = new XliffTuCreation();

        // for PO File
        if (IFormatNames.FORMAT_PO.equals(pageDataType))
        {
            String xliffTargetLang = p_GxmlElement
                    .getAttribute(DiplomatNames.Attribute.TARGETLANGUAGE);
            attributeMap.put(XliffHelper.MARK_XLIFF_TARGET_LANG,
                    xliffTargetLang);
            tuc.setAttribute(attributeMap);
        }

        for (int i = 0, max = elements.size(); i < max; i++)
        {
            GxmlElement elem = (GxmlElement) elements.get(i);
            Tu tu;

            switch (elem.getType())
            {
                case GxmlElement.LOCALIZABLE: // 3
                    tu = createLocalizableSegment(elem, p_request, p_page,
                            p_sourceLocale, p_tmId);
                    p_lg.addTu(tu);
                    p_tuList.add(tu);
                    break;

                case GxmlElement.TRANSLATABLE: // 2
                    // for xliff
                    String xliffpart = elem.getAttribute("xliffPart");
                    boolean isCreateTu = true;

                    if (xliffpart != null)
                    {
                        isCreateTu = tuc.transProcess(p_request, xliffpart,
                                elem, p_lg, p_tuList, p_sourceLocale, jobId);
                    }

                    if (isCreateTu)
                    {
                        int inddPageNumInt = 0;
                        String inddPageNum = elem
                                .getAttribute(DiplomatNames.Attribute.INDDPAGENUM);
                        if (inddPageNum != null)
                        {
                            inddPageNumInt = Integer.parseInt(inddPageNum);
                        }

                        ArrayList<Tu> tus = createTranslatableSegments(elem,
                                p_request, p_page, p_sourceLocale, p_tmId,
                                pageDataType);

                        for (int j = 0, maxj = tus.size(); j < maxj; j++)
                        {
                            tu = tus.get(j);

                            if (inddPageNumInt != 0)
                            {
                                tu.setInddPageNum(inddPageNumInt);
                                haveInddPageNum = true;
                            }

                            Tuv tuv = tu.getTuv(p_sourceLocale.getId(), jobId);
                            if (tuv.getSid() == null)
                            {
                                if (sid != null)
                                {
                                    tuv.setSid(sid);
                                }
                                else
                                {
                                    // passolo
                                    String resName = elem
                                            .getAttribute("resname");
                                    if (resName != null)
                                        tuv.setSid(resName);
                                }
                            }

                            p_lg.addTu(tu);
                            p_tuList.add(tu);
                        }
                    }
                    break;

                case GxmlElement.GS: // 23
                    p_tuList = createTUs_1(p_request, p_page, p_lg, p_tmId,
                            p_sourceLocale, elem, p_tuList, jobId);
                    break;

                default: // other
                    String skeleton = elem.getTextValue();
                    if (supportSid)
                    {
                        if (isJavaProperties)
                        {
                            if (skeleton != null)
                            {
                                skeleton = skeleton.trim();
                                if (skeleton.endsWith("="))
                                {
                                    int index = skeleton.lastIndexOf("\n");
                                    if (index > -1)
                                    {
                                        skeleton = skeleton
                                                .substring(index + 1);
                                    }

                                    // remove "="
                                    skeleton = skeleton.substring(0,
                                            skeleton.length() - 1);
                                    sid = skeleton.trim();
                                }
                            }
                        }
                    }

                    skeleton = skeleton.toLowerCase();

                    String xliffVersion = XliffHelper.getXliffVersion(skeleton);
                    boolean isXliff = xliffVersion != null;
                    if (isXliff)
                    {
                        String trgLang = null;
                        if ("1.2".equals(xliffVersion))
                        {
                            trgLang = XliffHelper
                                    .getXliff12TargetLanguage(skeleton);

                            if (skeleton.indexOf("tool") > -1
                                    && skeleton.indexOf("worldserver") > -1)
                            {
                                generatFrom = TuImpl.FROM_WORLDSERVER;
                                attributeMap.put("generatFrom", generatFrom);
                                tuc = new WsTuCreation();
                            }

                            if (skeleton.indexOf("tool") > -1
                                    && skeleton.indexOf("madcap lingo v") > -1)
                            {
                                attributeMap.put("isMadCapLingo", "true");
                            }
                        }
                        else if ("2.0".equals(xliffVersion))
                        {
                            trgLang = XliffHelper
                                    .getXliff20TargetLanguage(skeleton);
                        }

                        if (trgLang != null)
                        {
                            attributeMap
                                    .put(XliffHelper.MARK_XLIFF_TARGET_LANG,
                                            trgLang);
                        }

                        tuc.setAttribute(attributeMap);
                    }

                    break;
            }
        }

        return p_tuList;
    }

    /**
     * This method creates TUVs from a list of TUs and a Request
     * 
     * @return java.util.List
     */
    private ArrayList<Tuv> createTUVs(Request p_request, ArrayList<Tu> p_tus,
            long jobId) throws Exception
    {
        ArrayList<Tuv> srcTuvs = new ArrayList<Tuv>(p_tus.size());
        long sourceLocaleId = getSourceLocale(p_request).getId();
        int order = 1;

        long sourceTmId = p_request.getL10nProfile()
                .getTranslationMemoryProfile().getProjectTmIdForSave();
        ProjectHandler projectHandler = ServerProxy.getProjectHandler();
        Tm tm = projectHandler.getProjectTMById(sourceTmId, false);
        String sourceTmName = null;
        if (tm != null)
        {
            sourceTmName = tm.getName();
        }

        String sourceProjectName = p_request.getL10nProfile().getProject()
                .getName();
        String projectManagerId = p_request.getL10nProfile().getProject()
                .getProjectManagerId();
        String creationId = projectManagerId == null ? Tmx.DEFAULT_USER
                : projectManagerId;
        for (int i = 0, max = p_tus.size(); i < max; i++)
        {
            Tu tu = p_tus.get(i);

            tu.setSourceTmName(sourceTmName);

            Tuv tuv = tu.getTuv(sourceLocaleId, jobId);

            tuv.setCreatedDate(new Date());
            tuv.setCreatedUser(creationId);
            tuv.setUpdatedProject(sourceProjectName);

            tu.setOrder(order);
            tuv.setOrder(order);
            order++;

            srcTuvs.add(tuv);
        }

        return srcTuvs;
    }

    /**
     * This method calculates the exact match keys for Source Tuvs by delegating
     * to the IndexerLocal component.
     */
    private void setExactMatchKeysForSrcTUVs(List<Tuv> p_srcTuvs)
            throws FileImportException
    {
        try
        {
            getTuvManager().setExactMatchKeys(p_srcTuvs);
        }
        catch (Exception e)
        {
            throw new FileImportException(e);
        }
    }

    /**
     * Creates TUs and TUVs for the segments that are localizable in the element
     * passed in.
     */
    private Tu createLocalizableSegment(GxmlElement p_elem, Request p_request,
            SourcePage p_page, GlobalSightLocale p_sourceLocale, long p_tmId)
            throws FileImportException
    {
        TuvManager tm = getTuvManager();

        Integer tuvWordCount = p_elem
                .getAttributeAsInteger(GxmlNames.LOCALIZABLE_WORDCOUNT);
        long pid = Long.parseLong(p_elem
                .getAttribute(GxmlNames.LOCALIZABLE_BLOCKID));

        String tuDataType = p_elem.getAttribute(GxmlNames.LOCALIZABLE_DATATYPE);

        // dataType is optional on LOCALIZABLE
        if (tuDataType == null || tuDataType.length() == 0)
        {
            GxmlElement diplomat = GxmlElement.getGxmlRootElement(p_elem);

            tuDataType = diplomat.getAttribute(GxmlNames.GXMLROOT_DATATYPE);
            if (tuDataType == null)
            {
                throw new FileImportException(new NullPointerException());
            }
        }

        Vector excludedTypes = p_request.getL10nProfile()
                .getTranslationMemoryProfile().getJobExcludeTuTypes();

        Tu tu = null;

        try
        {
            TuType tuType = null;
            String str_tuType = p_elem.getAttribute(GxmlNames.LOCALIZABLE_TYPE);

            try
            {
                tuType = TuType.valueOf(str_tuType);
            }
            catch (TuvException te)
            {
                // doesn't exist, create a custom one
                tuType = new CustomTuType(str_tuType);
            }

            tu = tm.createTu(p_tmId, tuDataType, tuType, 'L', pid);

            // Should this segment be excluded?
            if (excludedTypes.contains(tuType.getName()))
            {
                tuvWordCount = null;
            }

            Tuv tuv = tm.createTuv(
                    tuvWordCount == null ? 0 : tuvWordCount.intValue(),
                    p_sourceLocale, p_page);

            // Can't use tuv.setGxmlElement(p_elem) unless TuvImpl is fixed
            tuv.setGxml(p_elem.toGxml());
            tu.addTuv(tuv);
        }
        catch (Exception te)
        {
            c_logger.error(
                    "TuvException when creating TU and TUV."
                            + Long.toString(p_tmId), te);
            String[] args = new String[1];
            args[0] = Long.toString(p_request.getId());
            throw new FileImportException(
                    FileImportException.MSG_FAILED_TO_CREATE_TU_AND_TUV, args,
                    te);
        }

        return tu;
    }

    /**
     * Creates TUs and TUVs for the segments that are translatable in the
     * segment passed in.
     */
    private ArrayList<Tu> createTranslatableSegments(GxmlElement p_elem,
            Request p_request, SourcePage p_page,
            GlobalSightLocale p_sourceLocale, long p_tmId, String p_pageDataType)
            throws FileImportException
    {
        long jobId = p_page.getJobId();
        TuvManager tm = getTuvManager();
        ArrayList<Tu> tuList = new ArrayList<Tu>();

        Vector excludedTypes = p_request.getL10nProfile()
                .getTranslationMemoryProfile().getJobExcludeTuTypes();
        String translate = p_elem.getAttribute("translate");
        String xliffMrkId = p_elem.getAttribute("xliffSegSourceMrkId");
        String xliffMrkIndex = p_elem.getAttribute("xliffSegSourceMrkIndex");
        long pid = Long.parseLong(p_elem
                .getAttribute(GxmlNames.TRANSLATABLE_BLOCKID));

        TuType tuType = getTuType(p_elem, p_request);

        String tuDataType = p_elem
                .getAttribute(GxmlNames.TRANSLATABLE_DATATYPE);
        if (tuDataType == null || tuDataType.length() == 0)
        {
            GxmlElement diplomat = GxmlElement.getGxmlRootElement(p_elem);
            tuDataType = diplomat.getAttribute(GxmlNames.GXMLROOT_DATATYPE);
            if (tuDataType == null)
            {
                throw new FileImportException(new NullPointerException());
            }
        }

        List segments = p_elem.getChildElements();
        for (int i = 0, max = segments.size(); i < max; i++)
        {
            GxmlElement seg = (GxmlElement) segments.get(i);
            try
            {
                Tu tu = tm.createTu(p_tmId, tuDataType, tuType, 'T', pid);
                if (translate != null && !"".equals(translate.trim()))
                {
                    tu.setTranslate(translate);
                }
                Integer segWordCount = seg
                        .getAttributeAsInteger(GxmlNames.SEGMENT_WORDCOUNT);
                if (excludedTypes.contains(tuType.getName()))
                {
                    segWordCount = null;
                }
                // For Idiom World Server XLF, should keep its word-count info.
                String wordCountFromWs = p_elem
                        .getAttribute(Extractor.IWS_WORDCOUNT);
                if (wordCountFromWs != null)
                {
                    try
                    {
                        // Confirm it is a valid integer
                        Integer.parseInt(wordCountFromWs);
                        segWordCount = new Integer(wordCountFromWs);
                    }
                    catch (NumberFormatException e)
                    {
                    }
                }

                Tuv tuv = tm.createTuv(
                        segWordCount == null ? 0 : segWordCount.intValue(),
                        p_sourceLocale, p_page);
                tuv.setTu(tu);

                String fileName = p_page.getExternalPageId();
                String oriGxml = seg.toGxml(p_pageDataType);
                if (WordExtractor.useNewExtractor(""
                        + p_request.getDataSourceId()))
                {
                    Office2Optimizer op = new Office2Optimizer();
                    op.setGxml((TuvImpl) tuv, oriGxml, tuDataType, fileName,
                            p_pageDataType, jobId);
                }
                else
                {
                    OptimizeUtil op = new OptimizeUtil();
                    op.setGxml((TuvImpl) tuv, oriGxml, tuDataType, fileName,
                            p_pageDataType, jobId);
                }

                tuv.setSid(p_elem.getAttribute("sid"));
                tuv.setSrcComment(getSrcComment(seg));
                tu.setXliffMrkId(xliffMrkId);
                tu.setXliffMrkIndex(xliffMrkIndex);

                tu.addTuv(tuv);
                tuList.add(tu);
            }
            catch (Exception te)
            {
                if (c_logger.isDebugEnabled())
                {
                    c_logger.debug("TuvException when creating TU and TUV.", te);
                }
                String[] args = new String[1];
                args[0] = Long.toString(p_request.getId());
                throw new FileImportException(
                        FileImportException.MSG_FAILED_TO_CREATE_TU_AND_TUV,
                        args, te);
            }
        }

        return tuList;
    }

    private TuType getTuType(GxmlElement p_element, Request p_request)
    {
        String str_tuType = p_element.getAttribute(GxmlNames.TRANSLATABLE_TYPE);
        // set optional Gxml attribute "type" if not set
        if (str_tuType == null || str_tuType.length() == 0)
        {
            str_tuType = TuType.TEXT.getName();
        }
        // Provided by TuType. Should be able to use
        // tuType = new TuType(tuTypeString);
        // Needs public constructor though.
        TuType tuType;
        try
        {
            tuType = TuType.valueOf(str_tuType);
        }
        catch (TuvException te)
        {
            try
            {
                // doesn't exist, create a custom one
                tuType = new CustomTuType(str_tuType);
            }
            catch (Exception te2)
            {
                String[] args = new String[1];
                args[0] = Long.toString(p_request.getId());
                throw new FileImportException(
                        FileImportException.MSG_FAILED_TO_CREATE_TU_AND_TUV,
                        args, te2);
            }
        }
        return tuType;
    }

    private String getSrcComment(GxmlElement p_element)
    {
        String srcComment = p_element.getAttribute("srcComment");
        if (srcComment == null || srcComment.trim().length() == 0)
            return null;

        return m_xmlDecoder.decodeStringBasic(srcComment.trim());
    }

    /**
     * Creates the templates for the source page/extracted file that has been
     * created from Gxml.
     */
    private List<PageTemplate> generateTemplates(SourcePage p_page,
            GxmlRootElement p_doc, List<Tu> p_tuList)
            throws FileImportException
    {
        List<PageTemplate> templates = new ArrayList<PageTemplate>();

        TemplateGenerator tg = new TemplateGenerator();
        PageTemplate template;

        template = tg.generateDetail(p_doc, p_tuList);
        ExtractedSourceFile esf = getExtractedSourceFile(p_page);
        template.setSourcePage(p_page);
        esf.addPageTemplate(template, PageTemplate.TYPE_DETAIL);
        templates.add(template);

        template = tg.generateStandard(p_doc, p_tuList);
        template.setSourcePage(p_page);
        esf.addPageTemplate(template, PageTemplate.TYPE_STANDARD);
        templates.add(template);

        template = tg.generateExport(p_doc, p_tuList);
        template.setSourcePage(p_page);
        esf.addPageTemplate(template, PageTemplate.TYPE_EXPORT);
        templates.add(template);

        if (EditUtil.hasPreviewMode(esf.getDataType()))
        {
            template = tg.generatePreview(p_doc, p_tuList);
            template.setSourcePage(p_page);
            esf.addPageTemplate(template, PageTemplate.TYPE_PREVIEW);
            templates.add(template);
        }

        return templates;
    }

    /**
     * Creates the target pages associated with the source page and the target
     * locales specified in the L10nProfile as part of the request.
     */
    private Collection importTargetPages(Request p_request,
            SourcePage p_sourcePage, TermLeverageResult p_termMatches,
            ExactMatchedSegments p_exactMatchedSegments)
            throws FileImportException
    {
        Collection targetPages = new ArrayList();
        Collection failedTargetPages = null;
        boolean useLeveragedSegments = false;
        boolean useLeveragedTerms = false;
        L10nProfile profile = p_request.getL10nProfile();
        if (profile.getTmChoice() != L10nProfile.NO_TM)
        {
            useLeveragedSegments = true;
        }

        // hook to set the auto replace term behavior
        if (/* choose the condition here */true)
        {
            useLeveragedTerms = s_autoReplaceTerms.booleanValue();
        }

        // get the target locales
        List targetLocales = p_request.getInactiveTargetLocales();
        List unimport = p_request.getUnimportTargetLocales();
        // List unActive = profile.getUnActiveLocales();
        targetLocales.removeAll(unimport);

        if (PassoloUtil.isPassoloFile(p_sourcePage))
        {
            String path = p_sourcePage.getExternalPageId();
            String locale = PassoloUtil.getLocale(path);
            locale = PassoloUtil.getMappingLocales(locale);

            for (int i = targetLocales.size() - 1; i >= 0; i--)
            {
                GlobalSightLocale targetLocale = (GlobalSightLocale) targetLocales
                        .get(i);
                if (!targetLocale.toString().equals(locale))
                {
                    targetLocales.remove(i);
                }
            }
        }

        // targetLocales.removeAll(unActive);
        Hashtable activeLocaleInfo = p_request.getActiveTargets();

        try
        {
            if (targetLocales.size() > 0)
            {
                targetPages = getPageManager()
                        .createTargetPagesWithExtractedFile(p_sourcePage,
                                targetLocales, p_termMatches,
                                useLeveragedSegments, useLeveragedTerms,
                                p_exactMatchedSegments);
            }
            if (activeLocaleInfo.size() > 0)
            {
                Hashtable targetLocaleErrors = createFailedTargetErrorMessages(activeLocaleInfo);
                failedTargetPages = getPageManager()
                        .createFailedTargetPagesWithExtractedFile(p_sourcePage,
                                targetLocaleErrors);
            }
        }
        catch (Exception e)
        {
            c_logger.error("Exception occurred when trying to "
                    + "create target pages.", e);
            String[] args = new String[2];
            args[0] = Long.toString(p_sourcePage.getId());
            args[1] = Long.toString(p_request.getId());
            throw new FileImportException(
                    FileImportException.MSG_FAIL_TO_CREATE_TARGET_PAGES, args,
                    e);
        }
        catch (Throwable e)
        {
            c_logger.error("Exception occurred when trying to "
                    + "create target pages.", e);
            String[] args = new String[2];
            args[0] = Long.toString(p_sourcePage.getId());
            args[1] = Long.toString(p_request.getId());
            throw new FileImportException(
                    FileImportException.MSG_FAIL_TO_CREATE_TARGET_PAGES, args,
                    new Exception(e.toString()));
        }

        // return all the target pages
        if (failedTargetPages != null && failedTargetPages.size() > 0)
        {
            targetPages.addAll(failedTargetPages);
        }

        return targetPages;
    }

    private LeverageDataCenter createLeverageDataCenter(Request p_request,
            SourcePage p_sourcePage, long jobId) throws Exception
    {
        L10nProfile l10nProfile = p_request.getL10nProfile();
        LeveragingLocales leveragingLocales = l10nProfile
                .getLeveragingLocales();

        // remove the ACTIVE targets from the leveraging locales
        // since they shouldn't be leveraged against
        Set targets = p_request.getActiveTargets().keySet();
        for (Iterator it = targets.iterator(); it.hasNext();)
        {
            leveragingLocales.removeLeveragingLocales((GlobalSightLocale) it
                    .next());
        }

        GlobalSightLocale[] jobLocales = p_request.getTargetLocalesToImport();
        LeveragingLocales newLeveragingLocales = new LeveragingLocales();
        for (int i = 0; i < jobLocales.length; i++)
        {
            Set<GlobalSightLocale> levLocales = leveragingLocales
                    .getLeveragingLocales(jobLocales[i]);
            Set<GlobalSightLocale> newLevLocales = new HashSet<GlobalSightLocale>();
            newLevLocales.addAll(levLocales);
            newLeveragingLocales.setLeveragingLocale(jobLocales[i],
                    newLevLocales);
        }

        TranslationMemoryProfile tmProfile = l10nProfile
                .getTranslationMemoryProfile();

        LeverageOptions leverageOptions = new LeverageOptions(tmProfile,
                newLeveragingLocales);

        TmCoreManager tmCoreManager = LingServerProxy.getTmCoreManager();
        return tmCoreManager.createLeverageDataCenterForPage(p_sourcePage,
                leverageOptions, jobId);
    }

    private boolean isReimport(SourcePage p_sourcePage) throws Exception
    {
        boolean reimport = false;

        if (p_sourcePage.getPreviousPageId() > 0)
        {
            SourcePage prevPage = ServerProxy.getPageManager().getSourcePage(
                    p_sourcePage.getPreviousPageId());

            if (prevPage != null)
            {
                String currentState = p_sourcePage.getPageState();
                String prevState = prevPage.getPageState();

                // Not sure what is the exact criteria for
                // reimport. Modify the if condition if
                // proper criteria for reimport is found.
                // Currently assuming that the following works...
                if (currentState.equals(PageState.IMPORTING)
                        && (prevState.equals(PageState.NOT_LOCALIZED) || prevState
                                .equals(PageState.LOCALIZED)))
                {
                    reimport = true;
                }
            }
        }

        return reimport;
    }

    /**
     * Leverages the source page with all the targets specified in the
     * L10nProfile.
     */
    private ExactMatchedSegments leveragePage(Request p_request,
            SourcePage p_sourcePage, long jobId) throws FileImportException
    {
        ExactMatchedSegments exactMatchedSegments = null;

        try
        {
            LeverageDataCenter leverageDataCenter = createLeverageDataCenter(
                    p_request, p_sourcePage, jobId);

            if (isReimport(p_sourcePage))
            {
                // leverage segments from previously cancelled page
                leveragePageFromTuv(p_request, p_sourcePage, leverageDataCenter);
            }

            // leverage from page and segment TM
            c_logger.info("Starting leverage from page and segment TM");
            TmCoreManager tmCoreManager = LingServerProxy.getTmCoreManager();
            tmCoreManager.leveragePage(p_sourcePage, leverageDataCenter);
            c_logger.info("Finished leveraging successfuly from page and segment TM");

            // save the match results to leverage_match table
            Connection conn = null;
            try
            {
                conn = DbUtil.getConnection();
                LingServerProxy.getLeverageMatchLingManager()
                        .saveLeverageResults(conn, p_sourcePage,
                                leverageDataCenter);
            }
            finally
            {
                DbUtil.silentReturnConnection(conn);
            }
            // retrieve exact matches
            exactMatchedSegments = leverageDataCenter
                    .getExactMatchedSegments(p_sourcePage.getJobId());
        }
        catch (Exception e)
        {
            c_logger.error("Exception when leveraging.", e);

            String[] args = new String[2];
            args[0] = Long.toString(p_sourcePage.getId());
            args[1] = Long.toString(p_request.getId());
            throw new FileImportException(
                    FileImportException.MSG_FAILED_TO_LEVERAGE_SOURCE_PAGE,
                    args, e);
        }
        catch (Throwable e)
        {
            c_logger.error("Unexpected exception when leveraging.", e);

            String[] args = new String[2];
            args[0] = Long.toString(p_sourcePage.getId());
            args[1] = Long.toString(p_request.getId());
            throw new FileImportException(
                    FileImportException.MSG_FAILED_TO_LEVERAGE_SOURCE_PAGE,
                    args, new Exception(e.toString()));
        }

        return exactMatchedSegments;
    }

    /**
     * Leverages the source page from Translation_unit_variant table
     */
    private void leveragePageFromTuv(Request p_request,
            SourcePage p_sourcePage, LeverageDataCenter p_leverageDataCenter)
            throws Exception
    {
        c_logger.info("Starting leverage from tuv table");

        L10nProfile profile = p_request.getL10nProfile();
        LeveragingLocales leveragingLocales = profile.getLeveragingLocales();
        TargetLocaleLgIdsMapper localeLgIdMap = getPageManager()
                .getLocaleLgIdMapForReimportPage(p_sourcePage,
                        leveragingLocales.getAllTargetLocales());

        Leverager lev = LingServerProxy.getLeverager();
        lev.leverageForReimport(p_sourcePage, localeLgIdMap,
                profile.getSourceLocale(), p_leverageDataCenter);

        c_logger.info("Finished leveraging successfuly from tuv table");
    }

    /**
     * Leverages terms for all translatable segments in the specified source
     * page and persists the result.
     * 
     * @return collection of TermLeverageMatch objects, grouped by source tuv
     *         id, or null when the database does not exist or an error happens.
     */
    private TermLeverageResult leverageTermsForPage(Request p_request,
            SourcePage p_sourcePage, long jobId)
    {
        TermLeverageResult result = null;
        String project = p_request.getL10nProfile().getProject().getName();
        String termbaseName = p_request.getL10nProfile().getProject()
                .getTermbaseName();

        ArrayList sourceTuvs = getSourceTranslatableTuvs(p_sourcePage, jobId);

        if (sourceTuvs.size() > 0 && termbaseName != null
                && termbaseName.length() > 0)
        {
            try
            {
                TermLeverageOptions options = getTermLeverageOptions(p_request,
                        termbaseName);

                if (c_logger.isDebugEnabled())
                {
                    c_logger.debug("Termbase leveraging options = " + options);
                }

                if (options == null)
                {
                    c_logger.warn("Project " + project
                            + " refers to unknown termbase `" + termbaseName
                            + "'. Term leverage skipped.");
                }
                else if (options.getAllTargetPageLocales().size() == 0
                        || options.getSourcePageLangNames().size() == 0)
                {
                    c_logger.warn("No specified locale found in termbase. "
                            + "Term leverage skipped.");
                }
                else
                {
                    result = ServerProxy
                            .getTermLeverageManager()
                            .leverageTerms(sourceTuvs, options,
                                    String.valueOf(p_sourcePage.getCompanyId()));
                }
            }
            catch (Exception e)
            {
                c_logger.warn("Exception when leveraging terms, ignoring.", e);

                // Tue Apr 16 16:15:20 2002 CvdL: I don't think
                // failure to leverage terms should fail the entire
                // import.

                // throw new FileImportException(e);
            }
        }

        return result;
    }

    /**
     * Retrieves all translatable source TUVs in the source page by iterating
     * through the page's leverage groups and TUs.
     */
    private ArrayList<Tuv> getSourceTranslatableTuvs(SourcePage p_sourcePage,
            long p_jobId)
    {
        ArrayList<Tuv> result = new ArrayList<Tuv>();

        List<LeverageGroup> leverageGroups = getExtractedSourceFile(
                p_sourcePage).getLeverageGroups();
        long sourceLocaleId = p_sourcePage.getLocaleId();
        for (Iterator<LeverageGroup> it = leverageGroups.iterator(); it
                .hasNext();)
        {
            Collection<Tu> tus = it.next().getTus();
            for (Tu tu : tus)
            {
                if (!tu.isLocalizable())
                {
                    result.add(tu.getTuv(sourceLocaleId, p_jobId));
                }
            }
        }

        return result;
    }

    /**
     * Populates a term leverage options object.
     */
    private TermLeverageOptions getTermLeverageOptions(Request p_request,
            String p_termbaseName) throws GeneralException
    {
        TermLeverageOptions options = null;

        Locale sourceLocale = p_request.getL10nProfile().getSourceLocale()
                .getLocale();

        // get only the inactive target locales - all the active targets
        // should not be leveraged against
        List targetGsLocales = p_request.getInactiveTargetLocales();
        List unimport = p_request.getUnimportTargetLocales();
        targetGsLocales.removeAll(unimport);

        try
        {
            ITermbaseManager manager = ServerProxy.getTermbaseManager();

            long termbaseId = -1;
            if (manager != null)
            {
                termbaseId = manager.getTermbaseId(p_termbaseName,
                        String.valueOf(p_request.getCompanyId()));
            }

            // If termbase does not exist, return null options.
            if (termbaseId == -1)
            {
                return null;
            }

            options = new TermLeverageOptions();
            options.addTermBase(p_termbaseName);
            options.setSaveToDatabase(true);

            // fuzzy threshold set by object constructor - use defaults.
            // options.setFuzzyThreshold(50);

            ITermbase termbase = manager.connect(p_termbaseName,
                    ITermbase.SYSTEM_USER, "",
                    String.valueOf(p_request.getCompanyId()));

            // add source locale and lang names
            options.setSourcePageLocale(sourceLocale);
            List sourceLangNames = termbase.getLanguagesByLocale(sourceLocale
                    .toString());

            for (int i = 0, max = sourceLangNames.size(); i < max; i++)
            {
                String sourceLangName = (String) sourceLangNames.get(i);

                options.addSourcePageLocale2LangName(sourceLangName);
            }

            // add target locales and lang names
            for (Iterator tli = targetGsLocales.iterator(); tli.hasNext();)
            {
                Locale targetLocale = ((GlobalSightLocale) tli.next())
                        .getLocale();
                List targetLangNames = termbase
                        .getLanguagesByLocale(targetLocale.toString());

                Iterator it = targetLangNames.iterator();
                while (it.hasNext())
                {
                    String langName = (String) it.next();
                    options.addTargetPageLocale2LangName(targetLocale, langName);

                    options.addLangName2Locale(langName, targetLocale);
                }
            }
        }
        catch (Exception e)
        {
            throw new GeneralException(e);
        }

        return options;
    }

    private static Boolean setTermLeverageOptions()
    {
        Boolean result = Boolean.FALSE;

        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            boolean autoReplaceTerms = sc
                    .getBooleanParameter(SystemConfigParamNames.AUTO_REPLACE_TERMS);

            if (autoReplaceTerms == true)
            {
                result = Boolean.TRUE;
            }
        }
        catch (ConfigException ce)
        {
            // not specified - default is false
        }
        catch (GeneralException ge)
        {
            c_logger.error("A general exception was thrown when trying "
                    + "to read the system configuration file "
                    + "for system-wide leverage options.");
        }

        return result;
    }

    /**
     * Wraps the code for getting the tuv manager and handling any exceptions.
     */
    private TuvManager getTuvManager() throws FileImportException
    {
        TuvManager result = null;

        try
        {
            result = ServerProxy.getTuvManager();
        }
        catch (GeneralException ge)
        {
            c_logger.error("Couldn't find the TuvManager", ge);
            throw new FileImportException(
                    FileImportException.MSG_FAILED_TO_FIND_TUV_MANAGER, null,
                    ge);
        }

        return result;
    }

    /**
     * <p>
     * Searches the list of GXML elements for a LOCALIZABLE type="url-base" and,
     * if present, sets the page's internal base href to its value.
     * </p>
     * 
     * <p>
     * This routine assumes that a HTML page contains only one base href, if at
     * all. Multiple <base> tags might be extracted if they are, e.g., part of a
     * script that outputs different bases for IE and NN. In that case, the
     * first base tag is used.
     * </p>
     */
    private void setInternalBaseHref(ExtractedFile p_file,
            GxmlRootElement p_rootElement)
    {
        String base = null;
        List elements = p_rootElement.getChildElements();

        for (Iterator it = elements.iterator(); it.hasNext();)
        {
            GxmlElement e = (GxmlElement) it.next();

            if (e.getType() == GxmlElement.LOCALIZABLE)
            {
                String type = e.getAttribute(GxmlNames.LOCALIZABLE_TYPE);

                if (type != null && type.equals("url-base"))
                {
                    base = e.getTextValue();
                    break;
                }
            }
        }

        p_file.setInternalBaseHref(base);
    }

    /**
     * Return true if the page (Gxml) contains at least one GS tag.
     */
    private boolean containGsTags(GxmlRootElement p_root)
    {
        boolean hasGsTags = false;

        int gsTag[] =
        { GxmlElement.GS };
        List tagElements = p_root.getChildElements(gsTag);

        if (tagElements.size() > 0)
        {
            hasGsTags = true;
        }

        return hasGsTags;
    }

    /**
     * Returns the extracted source file or NULL if there isn't one.
     */
    private ExtractedSourceFile getExtractedSourceFile(SourcePage p_page)
    {
        ExtractedSourceFile result = null;

        if (p_page.getPrimaryFileType() == ExtractedSourceFile.EXTRACTED_FILE)
        {
            result = (ExtractedSourceFile) p_page.getPrimaryFile();
        }

        return result;
    }

    /**
     * Reads the content of the gxml or prsxml file and returns a String of XML
     * either GXML or PRSXML. This also deletes the file after reading its
     * contents
     * 
     * @param p_gxmlFileName
     *            filename containing the GXML (or PRSXML)
     * @exception IOException
     * @return String
     */
    public static String readXmlFromFile(String p_gxmlFileName)
            throws IOException
    {
        File f = new File(p_gxmlFileName);
        int len = (int) f.length();
        StringBuffer gxml = new StringBuffer(len);
        char buf[] = new char[32768];
        InputStreamReader reader = new InputStreamReader(
                new FileInputStream(f), "UTF8");
        int n = -1;

        do
        {
            n = reader.read(buf, 0, buf.length);
            if (n > 0)
                gxml.append(buf, 0, n);
        } while (n > 0);

        reader.close();
        f.delete();

        return gxml.toString();
    }

    /**
     * This method calculates the word count for the page based on the included
     * TUVs (ignoring the excluded TUVs).
     */
    private void updateWordCountForPage(Request p_request, SourcePage p_page,
            ArrayList p_srcTuvs)
    {
        int totalWordCount = 0;

        Vector excludedTypes = p_request.getL10nProfile()
                .getTranslationMemoryProfile().getJobExcludeTuTypes();

        for (int i = 0, imax = p_srcTuvs.size(); i < imax; i++)
        {
            Tuv tuv = (Tuv) p_srcTuvs.get(i);
            int wordCount = tuv.getWordCount();

            // read subs, exclude and count
            List subs = tuv.getSubflowsAsGxmlElements();
            for (int j = 0, jmax = subs.size(); j < jmax; j++)
            {
                GxmlElement sub = (GxmlElement) subs.get(j);

                String type = sub.getAttribute(GxmlNames.SUB_TYPE);
                if (type == null)
                {
                    type = "text";
                }

                if (!excludedTypes.contains(type))
                {
                    int subCount = Integer.parseInt(sub
                            .getAttribute(GxmlNames.SUB_WORDCOUNT));
                    wordCount += subCount;
                }
            }

            totalWordCount += wordCount;
        }

        p_page.setWordCount(totalWordCount);
    }

    /**
     * Gets the original page word count as calculated by the extractor.
     */
    private int getPageWordCount(GxmlRootElement p_element)
            throws FileImportException
    {
        int wordCount = 0;
        String wordCountAttr = p_element
                .getAttribute(GxmlNames.GXMLROOT_WORDCOUNT);

        if (wordCountAttr != null)
        {
            try
            {
                wordCount = Integer.parseInt(wordCountAttr);
            }
            catch (NumberFormatException e)
            {
                throw new FileImportException(e);
            }
        }

        return wordCount;
    }

    private String getGxmlVersion(GxmlRootElement p_element)
    {
        return p_element.getAttribute(GxmlNames.GXMLROOT_VERSION);
    }

    private Hashtable createFailedTargetErrorMessages(
            Hashtable p_targetLocaleInfo)
    {
        Hashtable targetLocaleErrors = new Hashtable(p_targetLocaleInfo.size());

        Set targetLocales = p_targetLocaleInfo.keySet();
        for (Iterator itl = targetLocales.iterator(); itl.hasNext();)
        {
            GlobalSightLocale target = (GlobalSightLocale) itl.next();
            Job j = (Job) p_targetLocaleInfo.get(target);
            String[] args = new String[2];
            args[0] = Long.toString(j.getId());
            args[1] = j.getJobName();
            GeneralException ge = new FileImportException(
                    FileImportException.MSG_FAILED_TO_IMPORT_ACTIVE_TARGET_PAGE,
                    args, null);

            targetLocaleErrors.put(target, ge);
        }

        return targetLocaleErrors;
    }

    /**
     * Set tuId and tuvId for all TUs and TUVs in current source page. And need
     * set these TuIds and TuvIds into its related business objects such as
     * xliffAlt, RemovedTag etc.
     * 
     * @param p_sourcePage
     */
    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    private void setTuTuvIds(SourcePage p_sourcePage, long p_jobId)
    {
        if (p_sourcePage == null)
        {
            return;
        }

        List<LeverageGroup> lgList = p_sourcePage.getExtractedFile()
                .getLeverageGroups();
        for (LeverageGroup lg : lgList)
        {
            Collection<Tu> tus = lg.getTus(false);
            // Set tuIds for all TUs
            SegmentTuTuvIndexUtil.setTuIds(tus);

            // Set tuIds into its own
            // TUVs,RemovedTag,RemovedPrefixTag,RemovedSuffixTag.
            List<Tuv> allTuvs = new ArrayList<Tuv>();
            for (Iterator<Tu> tuIter = tus.iterator(); tuIter.hasNext();)
            {
                TuImpl tu = (TuImpl) tuIter.next();
                allTuvs.addAll(tu.getTuvs(false, p_jobId));

                // Set tuId into its own Tuvs
                for (Iterator tuvIter = tu.getTuvs(false, p_jobId).iterator(); tuvIter
                        .hasNext();)
                {
                    TuvImpl tuv = (TuvImpl) tuvIter.next();
                    tuv.setTuId(tu.getId());
                }
                // Set tuId into its RemovedTags
                if (tu.hasRemovedTags())
                {
                    for (Iterator tagIt1 = tu.getRemovedTags().iterator(); tagIt1
                            .hasNext();)
                    {
                        RemovedTag rt = (RemovedTag) tagIt1.next();
                        rt.setTuId(tu.getId());
                    }
                }
                // Set tuId into its RemovedPrefixTag
                if (tu.getPrefixTag() != null)
                {
                    tu.getPrefixTag().setTuId(tu.getId());
                }
                // Set tuId into its RemovedSuffixTag
                if (tu.getSuffixTag() != null)
                {
                    tu.getSuffixTag().setTuId(tu.getId());
                }
            }

            // Set tuvIds for all TUVs
            SegmentTuTuvIndexUtil.setTuvIds(allTuvs);

            // Set tuvIds into its own XliffAlt objects (for XLF/XLZ/PASSOLO
            // formats)
            for (Iterator<Tuv> tuvIter = allTuvs.iterator(); tuvIter.hasNext();)
            {
                TuvImpl tuv = (TuvImpl) tuvIter.next();
                Set<XliffAlt> xlfAlts = tuv.getXliffAlt(false);
                if (xlfAlts != null && xlfAlts.size() > 0)
                {
                    for (Iterator<XliffAlt> xlfAltIter = xlfAlts.iterator(); xlfAltIter
                            .hasNext();)
                    {
                        XliffAlt alt = (XliffAlt) xlfAltIter.next();
                        alt.setTuvId(tuv.getId());
                    }
                }
            }
        }
    }

    public static boolean getLeveragematch()
    {
        return s_autoReplaceTerms.booleanValue();
    }
}
