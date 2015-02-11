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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.cxe.adapter.cap.CapImporter;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.everest.comment.IssueEditionRelation;
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
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.util.Tmx;
import com.globalsight.everest.tuv.CustomTuType;
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.tuv.RemovedPrefixTag;
import com.globalsight.everest.tuv.RemovedSuffixTag;
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
import com.globalsight.ling.common.Text;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.extractor.xliff.Extractor;
import com.globalsight.ling.docproc.extractor.xliff.XliffAlt;
import com.globalsight.ling.tm.ExactMatchedSegments;
import com.globalsight.ling.tm.Leverager;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.ling.tm.TargetLocaleLgIdsMapper;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.log.GlobalSightCategory;
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
import com.globalsight.util.gxml.PrsRootElement;
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
    private static GlobalSightCategory c_logger = (GlobalSightCategory) GlobalSightCategory
            .getLogger(ExtractedFileImporter.class.getName());

    private static String TRANSLATION_MT = "<iws:status translation_type=\"machine_translation";
    private static String SEGMENT_LOCKED = "lock_status=\"locked\"";
    private static String SCORE_100 = "tm_score=\"100.00\"";
    private static String SPELL_START = "<ph[^>]*>&lt;w:proofErr w:type=&quot;spellStart&quot;/&gt;</ph>";
    private static String SPELL_END = "<ph[^>]*>&lt;w:proofErr w:type=&quot;spellEnd&quot;/&gt;</ph>";
    private static String SPELL_START_2 = "<ph[^>]*>&lt;w:proofErr w:type=&quot;gramStart&quot;/&gt;</ph>";
    private static String SPELL_END_2 = "<ph[^>]*>&lt;w:proofErr w:type=&quot;gramEnd&quot;/&gt;</ph>";
    private static String LAST_RENDER = "<ph[^>]*>&lt;w:lastRenderedPageBreak/&gt;</ph>";

    private static String REGEX_BPT = "<bpt[^>]*i=\"([^\"]*)\"[^>]*>";
    private static String REGEX_BPT_ALL = "(<bpt[^>]*i=\"{0}\"[^>]*>)[^>]*</bpt>.*(<ept[^>]*i=\"{0}\"[^>]*>)[^>]*</ept>";
    private static String REGEX_BPT_ALL2 = "<bpt[^>]*i=\"{0}\"[^>]*>[^>]*</bpt>(.*)<ept[^>]*i=\"{0}\"[^>]*>[^>]*</ept>";
    private static String REGEX_BPT_ALL3 = "(<bpt[^>]*i=\"{0}\"[^>]*>[^>]*</bpt>)(.*)(<ept[^>]*i=\"{0}\"[^>]*>[^>]*</ept>)";
    private static String REGEX_BPT_ALL_SPACE = "<bpt[^>]*i=\"{0}\"[^>]*>[^>]*</bpt>([ ]*)<ept[^>]*i=\"{0}\"[^>]*>[^>]*</ept>";
    private static String REGEX_SAME_TAG = "(<bpt[^>]*>)([^<]*)(</bpt>)([^<]*)(<ept[^>]*>)([^<]*)(</ept>)(<bpt[^>]*>)([^<]*)(</bpt>)([^<]*)(<ept[^>]*>)([^<]*)(</ept>)";
    private static String REGEX_IT = "<[pi][^>]*>[^<]*</[pi][^>]*>";
    private static String REGEX_IT2 = "(<it[^>]*>)([^<]*)(</it>)";
    private static String REGEX_TAG = "(<[^be][^>]*>)([^<]*)(</[^>]*>)";
    private static String REGEX_SEGMENT = "(<segment[^>]*>)(.*?)</segment>";

    private static String PRESERVE = "&lt;w:t xml:space=&quot;preserve&quot;&gt;";
    private static String NO_PRESERVE = "&lt;w:t&gt;";
    private static String RSIDRPR_REGEX = " w:rsidRPr=&quot;[^&]*&quot;";
    private static String RSIDR_REGEX = " w:rsidR=&quot;[^&]*&quot;";

    public static String EMPTYTARGET = "empty target";

    // for xliff
    // record the <source> tu_id array of a tanstable, so the next <target> can
    // save the tu_id
    TuImpl tuSource = new TuImpl();
    // record the <target> id for save the alt_segment.
    long targetUnitID = 0;

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
    HashMap importFile(Request p_request) throws FileImportException
    {
        HashMap pages = new HashMap();
        SourcePage sourcePage = null;
        Collection targetPages = null;
        FileImportException exception = null;

        c_logger.info("Importing page: " + p_request.getExternalPageId());
        long start_PERFORMANCE = System.currentTimeMillis();
        long lnProfileId = p_request.getL10nProfile().getId();
        try
        {
            long time_PERFORMANCE = System.currentTimeMillis();
            sourcePage = createSourcePage(p_request);
            pages.put(sourcePage.getGlobalSightLocale().getIdAsLong(),
                    sourcePage);

            c_logger.debug("Performance:: Creating source page time = "
                    + +(System.currentTimeMillis() - time_PERFORMANCE) + " "
                    + p_request.getExternalPageId());

            if (p_request.getType() == Request.EXTRACTED_LOCALIZATION_REQUEST)
            {
                ExactMatchedSegments exactMatchedSegments = null;

                if (p_request.getL10nProfile().getTMChoice() != L10nProfile.NO_TM)
                {
                    c_logger.info("TM leveraging for page: "
                            + p_request.getExternalPageId());
                    time_PERFORMANCE = System.currentTimeMillis();

                    exactMatchedSegments = leveragePage(p_request, sourcePage);

                    c_logger.debug("Performance:: TM leveraging time = "
                            + +(System.currentTimeMillis() - time_PERFORMANCE)
                            + " " + p_request.getExternalPageId());
                }

                c_logger.info("Term leveraging for page: "
                        + p_request.getExternalPageId());
                time_PERFORMANCE = System.currentTimeMillis();

                TermLeverageResult termMatches = leverageTermsForPage(
                        p_request, sourcePage);

                c_logger.debug("Performance:: Term leveraging time = "
                        + +(System.currentTimeMillis() - time_PERFORMANCE)
                        + " " + p_request.getExternalPageId());

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
                pages = removeUnActiveWorkflowTargetPages(pages, p_request
                        .getL10nProfile());
                c_logger.debug("Performance:: Creating target pages time = "
                        + +(System.currentTimeMillis() - time_PERFORMANCE)
                        + " " + p_request.getExternalPageId());
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
                c_logger.error("Import failed for page "
                        + p_request.getExternalPageId() + "\n", exception);

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

            try
            {
                long queueLength = CapImporter.getNumberOfWaitingImports();
                c_logger.info("Number of Files left in Import Queue: "
                        + queueLength);
            }
            catch (Exception e)
            {
                // don't allow failure to get the queue length to affect import
                c_logger.error("Failed to query import queue length: ", e);
            }

            c_logger.debug("Performance:: import time = "
                    + (System.currentTimeMillis() - start_PERFORMANCE) + " "
                    + p_request.getExternalPageId());
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
    private SourcePage createSourcePage(Request p_request)
            throws FileImportException
    {
        SourcePage page = null;
        String dataType = p_request.getDataSourceType();

        if (dataType.equals("db"))
        {
            page = createPageFromPrsXml(p_request);
        }
        else
        {
            page = createPageFromGxml(p_request);
        }

        return page;
    }

    /**
     * Creates Page, Leverage Groups, Tus and Tuvs
     */
    private SourcePage createPageFromGxml(Request p_request)
            throws FileImportException
    {
        SourcePage page = null;
        GxmlRootElement gxmlRootElement = null;

        try
        {
            gxmlRootElement = parseGxml(p_request);
        }
        catch (FileImportException ge)
        {
            page = createPage(p_request, gxmlRootElement);
            return page;
        }

        page = createPage(p_request, gxmlRootElement);
        ExtractedSourceFile esf = null;

        try
        {
            ArrayList tus;
            ArrayList srcTuvs;

            esf = getExtractedSourceFile(page);
            setAttributesOfExtractedFile(p_request, esf, gxmlRootElement);
            tus = createTUs(p_request, page, gxmlRootElement);
            srcTuvs = createTUVs(p_request, tus);
            setExactMatchKeysForSrcTUVs(srcTuvs);
            List templates = generateTemplates(page, gxmlRootElement, tus);
            page.setCompanyId(p_request.getCompanyId());

            // Re-calculate wordcount from excluded items.
            updateWordCountForPage(p_request, page, srcTuvs);
            long profileId = p_request.getL10nProfile().getId();

            HibernateUtil.save(page);
            Iterator iterator = templates.iterator();
            while (iterator.hasNext())
            {
                PageTemplate pTemplate = (PageTemplate) iterator.next();
                HibernateUtil.save(pTemplate.getTemplateParts());
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

        c_logger.info("Finished creating a page successfully");

        return page;
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
    private SourcePage createPage(Request p_request, GxmlRootElement p_element)
            throws FileImportException
    {
        SourcePage page = null;
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
            page = pm.getPageWithExtractedFileForImport(p_request, p_request
                    .getL10nProfile().getSourceLocale(), pageDataType,
                    wordCount, containsGsTags, gxmlVersion);
        }
        catch (PageException pe)
        {
            if (page == null)
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
                args[1] = page.getExternalPageId();
                throw new FileImportException(
                        FileImportException.MSG_FAILED_TO_ADD_PAGE_TO_REQUEST,
                        args, pe);
            }
        }

        return page;
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
    private ArrayList createTUs(Request p_request, SourcePage p_page,
            GxmlRootElement p_GxmlRootElement) throws FileImportException
    {
        LeverageGroup lg = getLeverageGroupForPage(p_page);
        long tmId = getTMId(p_request);
        GlobalSightLocale sourceLocale = getSourceLocale(p_request);

        return createTUs_1(p_request, p_page, lg, tmId, sourceLocale,
                p_GxmlRootElement, new ArrayList());
    }

    private LeverageGroup getLeverageGroupForPage(SourcePage p_page)
    {
        // assume this is an extracted page
        return (LeverageGroup) getExtractedSourceFile(p_page)
                .getLeverageGroups().iterator().next();
    }

    private long getTMId(Request p_request)
    {
        return p_request.getL10nProfile().getMainTmId();
    }

    private ArrayList createTUs_1(Request p_request, SourcePage p_page,
            LeverageGroup p_lg, long p_tmId, GlobalSightLocale p_sourceLocale,
            GxmlElement p_GxmlElement, ArrayList p_tuList)
            throws FileImportException
    {
        if (p_GxmlElement == null)
        {
            return p_tuList;
        }

        Pattern p = Pattern.compile("<fileProfileId>(.*)</fileProfileId>");
        Matcher m = p.matcher(p_request.getEventFlowXml());
        String pageDataType = null;
        boolean isJavaProperties = false;
        boolean supportSid = false;
        String sid = null;

        IssueEditionRelation ier = new IssueEditionRelation();
        try
        {
            if (m.find())
            {
                String fId = m.group(1);
                FileProfileImpl fileProfile = HibernateUtil.get(
                        FileProfileImpl.class, Long.parseLong(fId), false);
                supportSid = fileProfile.getSupportSid();
                pageDataType = p_GxmlElement
                        .getAttribute(GxmlNames.GXMLROOT_DATATYPE);
                isJavaProperties = "javaprop".equals(pageDataType);
            }
        }
        catch (Exception e)
        {
            c_logger.error(e);
        }

        List elements = p_GxmlElement.getChildElements();
        // for xliff file
        String xliffTargetLan = new String();
        XliffAlt alt = new XliffAlt();
        String generatFrom = new String();

        // for PO File
        if (IFormatNames.FORMAT_PO.equals(pageDataType))
        {
            xliffTargetLan = p_GxmlElement
                    .getAttribute(DiplomatNames.Attribute.TARGETLANGUAGE);
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
                    String xliffTranslationType = elem
                            .getAttribute(Extractor.IWS_TRANSLATION_TYPE);
                    String xliffTMScore = elem
                            .getAttribute(Extractor.IWS_TM_SCORE);
                    String xliffSourceContent = 
                        elem.getAttribute(Extractor.IWS_SOURCE_CONTENT);

                    if (xliffpart != null && xliffpart.equals("target"))
                    {
                        GxmlElement seg = (GxmlElement) elem.getChildElements()
                                .get(0);
                        ArrayList array = (ArrayList) p_lg.getTus();

                        TuImpl tuPre = (TuImpl) array.get(array.size() - 1);
                        TuvImpl tuvPre = (TuvImpl) tuPre.getTuv(p_sourceLocale
                                .getId());

                        // for transmitting GS Edition segment comments.
                        if (p_request.getEditionJobParams() != null)
                        {
                            try
                            {
                                if (elem.getAttribute("tuID") != null)
                                {
                                    long oldTuID = Long.parseLong(elem
                                            .getAttribute("tuID"));

                                    HashMap editionParaMap = (HashMap) p_request
                                            .getEditionJobParams().get(
                                                    "segComments");
                                    HashMap issueMap = (HashMap) editionParaMap
                                            .get(oldTuID);

                                    IssueEditionRelation ie = new IssueEditionRelation();
                                    ie.setTuv(tuvPre);
                                    ie.setOriginalTuId(oldTuID);

                                    if (issueMap != null)
                                    {
                                        ie.setOriginalTuvId((Long) issueMap
                                                .get("LevelObjectId"));

                                        Vector historyVec = (Vector) issueMap
                                                .get("HistoryVec");
                                        String originalIssueHistoryId = "";

                                        for (int x = 0; x < historyVec.size(); x++)
                                        {
                                            HashMap history = (HashMap) historyVec
                                                    .get(x);

                                            if (x == historyVec.size() - 1)
                                            {
                                                originalIssueHistoryId = originalIssueHistoryId
                                                        + ""
                                                        + history
                                                                .get("HistoryID");
                                            }
                                            else
                                            {
                                                originalIssueHistoryId = originalIssueHistoryId
                                                        + history
                                                                .get("HistoryID")
                                                        + ",";
                                            }
                                        }

                                        ie
                                                .setOriginalIssueHistoryId(originalIssueHistoryId);
                                    }

                                    Set ieSet = tuvPre
                                            .getIssueEditionRelation();

                                    if (ieSet != null)
                                    {
                                        ieSet.add(ie);
                                        tuvPre.setIssueEditionRelation(ieSet);
                                    }
                                    else
                                    {
                                        HashSet hs = new HashSet();
                                        hs.add(ie);
                                        tuvPre.setIssueEditionRelation(hs);
                                    }
                                }

                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }

                        if (Text.isBlank(seg.getTextValue()))
                        {
                            // Tuv srcTuv =
                            // tuPre.getTuv(p_sourceLocale.getId());
                            // tuPre.setXliffTarget(srcTuv.getGxml());
                            tuPre.setXliffTarget(seg.toGxml());
                            ((TuImpl) p_tuList.get(p_tuList.size() - 1))
                                    .setXliffTarget(seg.toGxml());
                        }
                        else
                        {
                            // String str =
                            // tuvPre.encodeGxmlAttributeEntities(seg.toGxml(pageDataType));
                            tuPre.setXliffTarget(seg.toGxml(pageDataType));
                            ((TuImpl) p_tuList.get(p_tuList.size() - 1))
                                    .setXliffTarget(seg.toGxml(pageDataType));
                        }

                        if (xliffTargetLan != null
                                && !xliffTargetLan.equals(""))
                        {
                            tuPre.setXliffTargetLanguage(xliffTargetLan);
                        }

                        if (xliffTranslationType != null
                                && xliffTranslationType.length() > 0)
                        {
                            tuPre.setXliffTranslationType(xliffTranslationType);
                        }

                        if (xliffTMScore != null && xliffTMScore.length() > 0)
                        {
                            tuPre.setIwsScore(xliffTMScore);
                        }
                        
                        if (!generatFrom.isEmpty())
                        {
                            tuPre.setGenerateFrom(generatFrom);
                        }

                        if (xliffSourceContent != null && xliffSourceContent.length() > 0)
                        {
                            tuPre.setSourceContent(xliffSourceContent);
                        }
                    }
                    else if (xliffpart != null && xliffpart.equals("altTarget"))
                    {
                        // if(altLanguage != null) {
                        String altLanguage = elem.getAttribute("altLanguage");
                        String altQuality = elem.getAttribute("altQuality");
                        GxmlElement seg = (GxmlElement) elem.getChildElements()
                                .get(0);
                        ArrayList array = (ArrayList) p_lg.getTus();
                        TuImpl tuPre = (TuImpl) array.get(array.size() - 1);
                        TuvImpl tuvPre = (TuvImpl) tuPre.getTuv(p_sourceLocale
                                .getId());
                        alt.setSegment(seg.toGxml(pageDataType));
                        alt.setLanguage(altLanguage);
                        alt.setQuality(altQuality);
                        alt.setTuv(tuvPre);
                        tuvPre.addXliffAlt(alt);

                        array.set(array.size() - 1, tuPre);
                        // }
                    }
                    else if (xliffpart != null && xliffpart.equals("altSource"))
                    {
                        GxmlElement seg = (GxmlElement) elem.getChildElements()
                                .get(0);
                        ArrayList array = (ArrayList) p_lg.getTus();

                        alt = new XliffAlt();
                        alt.setSourceSegment(seg.toGxml(pageDataType));
                    }
                    else
                    {
                        ArrayList tus = createTranslatableSegments(elem,
                                p_request, p_page, p_sourceLocale, p_tmId,
                                pageDataType);

                        for (int j = 0, maxj = tus.size(); j < maxj; j++)
                        {
                            tu = (Tu) tus.get(j);
                            Tuv tuv = tu.getTuv(p_sourceLocale.getId());
                            if (tuv.getSid() == null)
                            {
                                tuv.setSid(sid);
                            }
                            p_lg.addTu(tu);
                            p_tuList.add(tu);
                        }
                    }
                    break;

                case GxmlElement.GS: // 23
                    p_tuList = createTUs_1(p_request, p_page, p_lg, p_tmId,
                            p_sourceLocale, elem, p_tuList);
                    break;

                default: // other
                    String nodeValue1 = elem.getTextValue();
                    if (supportSid)
                    {
                        if (isJavaProperties)
                        {
                            String nodeValue = elem.getTextValue();
                            if (nodeValue != null)
                            {
                                nodeValue = nodeValue.trim();
                                if (nodeValue.endsWith("="))
                                {
                                    int index = nodeValue.lastIndexOf("\n");
                                    if (index > -1)
                                    {
                                        nodeValue = nodeValue
                                                .substring(index + 1);
                                    }

                                    // remove "="
                                    nodeValue = nodeValue.substring(0,
                                            nodeValue.length() - 1);
                                    sid = nodeValue.trim();
                                }
                            }
                        }

                    }

                    String lowcase = nodeValue1.toLowerCase();

                    if (lowcase.indexOf("xliff") > -1
                            && lowcase.indexOf("file") > -1)
                    {
                        if (lowcase.indexOf("target-language") > -1)
                        {
                            String tempStr = lowcase.substring(lowcase
                                    .indexOf("target-language"));
                            String tempStr2 = tempStr.substring(tempStr
                                    .indexOf("\"") + 1);
                            String tempStr3 = tempStr2.substring(0, tempStr2
                                    .indexOf("\""));
                            xliffTargetLan = tempStr3;
                        }
                        
                        if (lowcase.indexOf("tool") > -1
                                && lowcase.indexOf("worldserver") > -1)
                        {
                            generatFrom = TuImpl.FROM_WORLDSERVER;

                        }
                    }

                    ArrayList array = (ArrayList) p_lg.getTus();

                    // Sets XliffTranslationType in above code.
                    /*
                     * if (nodeValue1.indexOf(TRANSLATION_MT) > 0) { TuImpl
                     * tuPre = (TuImpl)array.get(array.size()-1);
                     * tuPre.setXliffTranslationType(TuImpl.TRANSLATION_MT); }
                     */

                    if (nodeValue1.indexOf(SEGMENT_LOCKED) > 0)
                    {
                        TuImpl tuPre = (TuImpl) array.get(array.size() - 1);
                        tuPre.setXliffLocked(true);
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
    private ArrayList createTUVs(Request p_request, ArrayList p_tus)
            throws Exception
    {
        ArrayList srcTuvs = new ArrayList(p_tus.size());
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
        String creationId = projectManagerId == null ? Tmx.DEFAULT_USER : projectManagerId;
        for (int i = 0, max = p_tus.size(); i < max; i++)
        {
            Tu tu = (Tu) p_tus.get(i);

            tu.setSourceTmName(sourceTmName);

            Tuv tuv = tu.getTuv(sourceLocaleId);

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
    private void setExactMatchKeysForSrcTUVs(List p_srcTuvs)
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
     * Parses through the PrsXml with PrsReader and creates a source page.
     */
    private SourcePage createPageFromPrsXml(Request p_request)
            throws FileImportException
    {
        SourcePage page = null;

        String pageDataType = GxmlNames.PRS_DATATYPE;

        // the word count must be totaled up from each record
        int pageWordCount = 0;

        // create list to keep TUs in the order they are read in the GXML
        ArrayList tuList = new ArrayList();
        GlobalSightLocale sourceLocale = p_request.getL10nProfile()
                .getSourceLocale();
        long sourceLocaleId = sourceLocale.getId();

        GxmlFragmentReaderPool pool = GxmlFragmentReaderPool.instance();
        GxmlFragmentReader reader = pool.getGxmlFragmentReader();
        PrsRootElement root;

        try
        {
            String prsxml = readXmlFromFile(p_request.getGxml());
            // now set the GXML string value back into the request, it will not
            // be persisted, but will be used in a second
            p_request.setGxml(prsxml);
            root = reader.parsePRS(prsxml);
        }
        catch (GxmlException ge)
        {
            c_logger.error("Failed to parse the PrsXml.", ge);
            page = createPage(p_request, "", 0);
            setExceptionInRequest(p_request, ge);
            return page;
        }
        catch (IOException ioe)
        {
            c_logger.error("Failed to read the PrsXml from the temp file", ioe);
            GeneralException ge = new GeneralException(ioe);
            page = createPage(p_request, "", 0);
            setExceptionInRequest(p_request, ge);
            return page;
        }
        finally
        {
            pool.freeGxmlFragmentReader(reader);
        }

        page = createPage(p_request, pageDataType, pageWordCount);

        PageManager pm = getPageManager();
        LeverageGroup lg = null;

        try
        {
            List recordElements = root
                    .getTranslatableAndLocalizableGroupedByRecord();
            int numOfRecords = recordElements.size();
            long tmId = p_request.getL10nProfile().getMainTmId();

            ExtractedSourceFile esf = getExtractedSourceFile(page);
            if (p_request.getBaseHref() != null
                    && p_request.getBaseHref().length() > 0)
            {
                esf.setExternalBaseHref(p_request.getBaseHref());
            }

            for (int nr = 0; nr < numOfRecords; nr++)
            {
                lg = pm.createLeverageGroup();
                List elements = (List) recordElements.get(nr);

                for (int i = 0; i < elements.size(); i++)
                {
                    GxmlElement elem = (GxmlElement) elements.get(i);

                    switch (elem.getType())
                    {
                        case GxmlElement.LOCALIZABLE:
                            Tu tTu = createLocalizableSegment(elem, p_request,
                                    page, sourceLocale, tmId);

                            pageWordCount += tTu.getTuv(sourceLocale.getId())
                                    .getWordCount();

                            lg.addTu(tTu);
                            tuList.add(tTu);
                            break;

                        case GxmlElement.TRANSLATABLE:
                            ArrayList tus = createTranslatableSegments(elem,
                                    p_request, page, sourceLocale, tmId,
                                    pageDataType);

                            // go through list and add to leverage group
                            // and to list of TUs to pass in to template
                            // generation
                            for (int j = 0, maxj = tus.size(); j < maxj; j++)
                            {
                                Tu lTu = (Tu) tus.get(j);

                                pageWordCount += lTu.getTuv(
                                        sourceLocale.getId()).getWordCount();

                                lg.addTu(lTu);
                                tuList.add(lTu);
                            }
                            break;

                        default:
                            break;
                    }
                }

                esf.addLeverageGroup(lg);
            }

            ArrayList srcTuvs = new ArrayList(tuList.size());
            int order = 1;

            long sourceTmId = p_request.getL10nProfile()
                    .getTranslationMemoryProfile().getProjectTmIdForSave();
            ProjectHandler projectHandler = ServerProxy.getProjectHandler();
            Tm projectTm = projectHandler.getProjectTMById(sourceTmId, false);
            String sourceTmName = null;
            if (projectTm != null)
            {
                sourceTmName = projectTm.getName();
            }

            String sourceProjectName = p_request.getL10nProfile().getProject()
                    .getName();
            String projectManagerId = p_request.getL10nProfile().getProject()
                    .getProjectManagerId();
            String creationUser = projectManagerId == null ? Tmx.DEFAULT_USER : projectManagerId;
            for (int i = 0, max = tuList.size(); i < max; i++)
            {
                Tu tu = (Tu) tuList.get(i);
                Tuv tuv = tu.getTuv(sourceLocaleId);

                tu.setOrder(order);
                tu.setSourceTmName(sourceTmName);

                tuv.setCreatedDate(new Date());
                tuv.setCreatedUser(creationUser);
                tuv.setUpdatedProject(sourceProjectName);
                tuv.setOrder(order);
                order++;

                srcTuvs.add(tuv);
            }

            // assign exact match crcs to all source tuvs
            getTuvManager().setExactMatchKeys(srcTuvs);

            page.setWordCount(pageWordCount);

            generateTemplates(page, root, tuList);

            ExtractedFileImportPersistenceHandler piph = new ExtractedFileImportPersistenceHandler();

            piph.persistObjects(page);
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

        return page;
    }

    /**
     * Called when creating a page with NO gs tags (like all Prs pages).
     */
    private SourcePage createPage(Request p_request, String p_dataType,
            int p_wordCount) throws FileImportException
    {
        return createPage(p_request, p_dataType, p_wordCount, false, "");
    }

    /**
     * Gets the SourcePage to import and add segments and TUVs too. Also adds
     * the page to the request and persists the relationship.
     * 
     * @param p_request
     *            The request to import a page for.
     * @param p_dataType
     *            The type of data (HTML, XML, etc..)
     * @param p_wordCount
     *            The number of translatable words
     * @param p_containGsTags
     *            'True' of 'False" whether this page contains GS tags.
     * @return SourcePage The page for importing.
     */
    private SourcePage createPage(Request p_request, String p_dataType,
            int p_wordCount, boolean p_containGsTags, String p_gxmlVersion)
            throws FileImportException
    {
        SourcePage page = null;
        PageManager pm = getPageManager();
        L10nProfile profile = p_request.getL10nProfile();

        try
        {
            page = pm.getPageWithExtractedFileForImport(p_request, profile
                    .getSourceLocale(), p_dataType, p_wordCount,
                    p_containGsTags, p_gxmlVersion);
        }
        catch (Exception pe)
        {
            if (page == null)
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
                args[1] = page.getExternalPageId();
                throw new FileImportException(
                        FileImportException.MSG_FAILED_TO_ADD_PAGE_TO_REQUEST,
                        args, pe);
            }
        }

        return page;
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

            Tuv tuv = tm.createTuv(tuvWordCount == null ? 0 : tuvWordCount
                    .intValue(), p_sourceLocale, p_page);

            // Can't use tuv.setGxmlElement(p_elem) unless TuvImpl is fixed
            tuv.setGxml(p_elem.toGxml());
            tu.addTuv(tuv);
        }
        catch (Exception te)
        {
            c_logger.error("TuvException when creating TU and TUV."
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
    private ArrayList createTranslatableSegments(GxmlElement p_elem,
            Request p_request, SourcePage p_page,
            GlobalSightLocale p_sourceLocale, long p_tmId, String p_pageDataType)
            throws FileImportException
    {
        TuvManager tm = getTuvManager();

        ArrayList tuList = new ArrayList();
        String str_tuType = p_elem.getAttribute(GxmlNames.TRANSLATABLE_TYPE);

        long pid = Long.parseLong(p_elem
                .getAttribute(GxmlNames.TRANSLATABLE_BLOCKID));

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
                c_logger.debug("TuvException when creating TU and TUV.", te2);
                String[] args = new String[1];
                args[0] = Long.toString(p_request.getId());
                throw new FileImportException(
                        FileImportException.MSG_FAILED_TO_CREATE_TU_AND_TUV,
                        args, te2);
            }
        }

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

        Vector excludedTypes = p_request.getL10nProfile()
                .getTranslationMemoryProfile().getJobExcludeTuTypes();

        List segments = p_elem.getChildElements();

        for (int i = 0, max = segments.size(); i < max; i++)
        {
            GxmlElement seg = (GxmlElement) segments.get(i);

            try
            {
                Tu tu = tm.createTu(p_tmId, tuDataType, tuType, 'T', pid);

                Integer segWordCount = seg
                        .getAttributeAsInteger(GxmlNames.SEGMENT_WORDCOUNT);
                if (excludedTypes.contains(tuType.getName()))
                {
                    segWordCount = null;
                }

                Tuv tuv = tm.createTuv(segWordCount == null ? 0 : segWordCount
                        .intValue(), p_sourceLocale, p_page);
                tuv.setTu(tu);

                String fileName = p_page.getExternalPageId();

                if (isOptimizeFile(tuDataType, fileName))
                {
                    tuv = setGxmlForOffice((TuvImpl) tuv, seg
                            .toGxml(p_pageDataType));
                }
                else
                {
                    tuv.setGxml(seg.toGxml(p_pageDataType));
                }

                tuv.setSid(p_elem.getAttribute("sid"));

                tu.addTuv(tuv);

                tuList.add(tu);
            }
            catch (Exception te)
            {
                c_logger.debug("TuvException when creating TU and TUV.", te);
                String[] args = new String[1];
                args[0] = Long.toString(p_request.getId());
                throw new FileImportException(
                        FileImportException.MSG_FAILED_TO_CREATE_TU_AND_TUV,
                        args, te);
            }
        }

        return tuList;
    }
    
    private boolean isOptimizeFile(String tuDataType, String fileName)
    {
        if (tuDataType == null || fileName == null)
            return false;
        
        if (!IFormatNames.FORMAT_OFFICE_XML.equals(tuDataType))
            return false;
        
        int index = fileName.lastIndexOf('.');
        if (index < 0)
            return false;
        
        String type = fileName.substring(index);
        return ".docx".equalsIgnoreCase(type);
    }


    /**
     * Extracts a removed tag from a bpt fragment.
     * 
     * @param s
     *            a bpt fragment
     * @return the extracted removed tag.
     */
    private RemovedTag extract(String s)
    {
        RemovedTag tag = null;
        
        if (s.indexOf("&lt;w:br/&gt;") > 0)
        {
            return tag;
        }

        String regex = "</[^>]*>([^<]+)<";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);
        List<String> contents = new ArrayList<String>();
        while (m.find())
        {
            contents.add(m.group(1));
        }

        if (contents.size() == 1)
        {
            tag = new RemovedTag();
            String content = contents.get(0);
            int index = s.indexOf('>' + content + '<') + 1;
            String start = s.substring(0, index);
            String end = s.substring(index + content.length());

            tag.addOrgString(s);
            tag.addNewString(content);
            tag.setPrefixString(getContent(start, tag));
            tag.setSuffixString(getContent(end, tag));
        }

        return tag;
    }

    private String mergeMultiTags(String s)
    {
        String result = s;
        Pattern p = Pattern.compile("<[^>]*>[^<]*</[^>]*>");
        Matcher m = p.matcher(s);
        while (m.find())
        {
            String all = m.group();
            int index = result.indexOf(all);
            String temp = result.substring(index);

            String allTags = getAllLinkedTags(temp);
            String mergedTag = mergeLinkedTags(allTags);
            if (!mergedTag.equals(allTags))
            {
                result = result.replace(allTags, mergedTag);
                m = p.matcher(result);
            }
        }

        return result;
    }

    private String getContent(String s)
    {
        String regex = "<[^>]*>([^<]*)</[^>]*>";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);

        StringBuffer result = new StringBuffer();
        while (m.find())
        {
            result.append(m.group(1));
        }

        return result.toString();
    }

    private String mergeLinkedTags(String s)
    {
        Pattern p = Pattern.compile(REGEX_IT2);
        Matcher m = p.matcher(s);
        if (m.find())
        {
            return m.group(1) + getContent(s) + m.group(3);
        }

        Pattern p2 = Pattern.compile(REGEX_TAG);
        Matcher m2 = p2.matcher(s);
        if (m2.find())
        {
            return m2.group(1) + getContent(s) + m2.group(3);
        }

        return s;
    }

    private boolean hasContent(String s)
    {
        String regex = "</[^>]*>([^<]+)<";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);
        return m.find();
    }

    private boolean isAllTags(String s)
    {
        if (hasContent(s))
        {
            return false;
        }

        String temp = s;
        Pattern p = Pattern.compile(REGEX_BPT);
        Matcher m = p.matcher(s);
        while (m.find())
        {
            String i = m.group(1);
            String regex = MessageFormat.format(REGEX_BPT_ALL2, i);
            Pattern p2 = Pattern.compile(regex);
            Matcher m2 = p2.matcher(s);

            if (m2.find())
            {
                String all = m2.group();
                String s2 = m2.group(1);
                if (hasContent(all) || !isAllTags(s2))
                {
                    return false;
                }

                temp = temp.replace(all, "");
                m = p.matcher(temp);
            }
        }

        return true;
    }

    private String getAllLinkedTags(String s)
    {
        Pattern p = Pattern.compile("^" + REGEX_BPT);
        Matcher m = p.matcher(s);
        if (m.find())
        {
            String i = m.group(1);
            String regex = MessageFormat.format(REGEX_BPT_ALL, i);
            Pattern p2 = Pattern.compile(regex);
            Matcher m2 = p2.matcher(s);

            if (m2.find())
            {
                String all = m2.group();
                if (isAllTags(all))
                {
                    String rest = s.substring(all.length());
                    return all + getAllLinkedTags(rest);
                }
            }

            return "";
        }

        if (s.startsWith("<ept"))
        {
            return "";
        }

        Pattern p2 = Pattern.compile("^<[^>]*>[^<]*</[^>]*>");
        Matcher m2 = p2.matcher(s);

        if (m2.find())
        {
            String all = m2.group();
            String rest = s.substring(all.length());
            return all + getAllLinkedTags(rest);
        }

        return "";
    }

    /**
     * Gets a string that contains all content. The tagNum of the
     * <code>tag</code> will be updated.
     * 
     * @param s
     *            the gxml
     * @param tag
     *            the tagNum will be updated
     * @return the string string that contains all content.
     */
    private String getContent(String s, RemovedTag tag)
    {
        String regex = "<[^>]*>([^<]*)</[^>]*>";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);

        StringBuffer result = new StringBuffer();
        int i = tag.getTagNum();
        while (m.find())
        {
            result.append(m.group(1));
            i++;
        }

        tag.setTagNum(i);
        return result.toString();
    }

    /**
     * Gets all tags that will be removed from the gxml.
     * 
     * @param s
     *            the gxml of a tuv.
     * @return ArrayList<RemovedTag>. All tags that will be removed.
     */
    private List<RemovedTag> getTags(String s)
    {
        Map<RemovedTag, Integer> map = new HashMap<RemovedTag, Integer>();

        Pattern p = Pattern.compile(REGEX_BPT);
        Matcher m = p.matcher(s);
        while (m.find())
        {
            Pattern p2 = Pattern.compile(MessageFormat.format(REGEX_BPT_ALL, m
                    .group(1)));
            Matcher m2 = p2.matcher(s);

            if (m2.find())
            {
                String all = m2.group();
                RemovedTag tag = extract(all);

                if (tag != null)
                {
                    Integer size = map.get(tag);
                    if (size == null)
                    {
                        size = 0;
                    }
                    else
                    {
                        for (RemovedTag key : map.keySet())
                        {
                            if (key.equals(tag))
                            {
                                key.mergeString(tag);
                                break;
                            }
                        }
                    }

                    map.put(tag, size + 1);
                }
                s = s.replace(all, "");
                m = p.matcher(s);
            }
            else
            {
                break;
            }
        }

        List<RemovedTag> tags = new ArrayList<RemovedTag>(map.keySet());
        for (int i = tags.size() - 1; i >= 0; i--)
        {
            RemovedTag tag = tags.get(i);

            for (int j = i - 1; j >= 0; j--)
            {
                RemovedTag tag2 = tags.get(j);

                if (tag2.sameAs(tag))
                {
                    int n = map.get(tag);
                    map.put(tag2, map.get(tag2) + n);
                    map.put(tag, 0);

                    break;
                }
            }
        }

        List<RemovedTag> removedTags = new ArrayList<RemovedTag>();

        int n = 0;
        RemovedTag tagF = null;
        for (RemovedTag tag : map.keySet())
        {
            int num = map.get(tag);

            if (num > n)
            {
                tagF = tag;
                n = map.get(tag);
            }
            else if (num == n && tagF != null)
            {
                if (tag.getTagNum() > tagF.getTagNum())
                {
                    tagF = tag;
                }
                else if (tag.getPrefixString().length() > tagF
                        .getPrefixString().length())
                {
                    tagF = tag;
                }
            }
        }

        if (tagF != null)
        {
            boolean isPreserve = false;

            removedTags.add(tagF);
            for (RemovedTag tag : map.keySet())
            {
                if (tag.sameAs(tagF))
                {
                    removedTags.add(tag);

                    if (tag.getPrefixString().indexOf(PRESERVE) > 0)
                    {
                        isPreserve = true;
                    }
                }
            }

            if (isPreserve)
            {
                String ps = tagF.getPrefixString();
                ps = ps.replace(NO_PRESERVE, PRESERVE);
                tagF.setPrefixString(ps);
            }
        }

        return removedTags;
    }

    private String removeUnusedTag(String gxml)
    {
        String s = gxml.replaceAll(SPELL_START, "");
        s = s.replaceAll(SPELL_END, "");
        s = s.replaceAll(SPELL_START_2, "");
        s = s.replaceAll(SPELL_END_2, "");
        s = s.replaceAll(LAST_RENDER, "");

        return s;
    }

    /**
     * Updates gxml for office 2010. Some tags will be removed and some tags
     * will be merged to one.
     * 
     * @param tuv
     *            the tuv to update
     * @param gxml
     *            the gxml of the tuv
     * @return updated tuv
     */
    private TuvImpl setGxmlForOffice(TuvImpl tuv, String gxml)
    {
        if (gxml != null)
        {
//            gxml = removeUnusedTag(gxml);
//            gxml = mergeSameTags(gxml);
            gxml = removeTagForSpace(gxml);
            gxml = mergeOneBpt(gxml);
            gxml = removeTags(tuv, gxml);
            gxml = mergeMultiTags(gxml);
            gxml = removeAllPrefixAndSuffixTags(tuv, gxml);
        }

        tuv.setGxml(gxml);

        return tuv;
    }
    
    private String removeAllPrefixAndSuffixTags(TuvImpl tuv, String g)
    {
        String gxml = removePrefixTag(tuv, g);
        gxml = removeSuffixTag(tuv, gxml);
        gxml = removePrefixAndSuffixTags(tuv, gxml);
        
        if (!gxml.equals(g))
            return removeAllPrefixAndSuffixTags(tuv, gxml);
        
        return gxml;
    }

    private String removePrefixAndSuffixTags(TuvImpl tuv, String gxml)
    {
        if (gxml == null || gxml.length() == 0)
        {
            return gxml;
        }
        
        TuImpl tu = (TuImpl) tuv.getTu();
        if (tu.getRemovedTag() != null)
            return gxml;
        
        Pattern p = Pattern.compile(REGEX_SEGMENT);
        Matcher m = p.matcher(gxml);
        if (m.find())
        {
            String segment = m.group(1);
            String content = m.group(2);

            String temp = content.trim();
            if (temp.startsWith("<"))
            {
                Pattern p2 = Pattern.compile("^" + REGEX_BPT);
                Matcher m2 = p2.matcher(temp);
                if (m2.find())
                {
                    String i = m2.group(1);
                    String regex = MessageFormat.format("^" + REGEX_BPT_ALL3 + "$", i);
                    Pattern p3 = Pattern.compile(regex);
                    Matcher m3 = p3.matcher(temp);
                    
                    if (m3.find())
                    {
                        String prefixString = m3.group(1);
                        String suffixString = m3.group(3);
                        String newContent = m3.group(2);
                        
                        gxml = segment + newContent + "</segment>";
                        
                        RemovedPrefixTag tag = tu.getPrefixTag();
                        if (tag == null)
                        {
                            tag = new RemovedPrefixTag();
                            tag.setTu(tu);
                            tu.setPrefixTag(tag);
                        }
                        
                        String s = tag.getString();
                        if (s == null)
                            s = "";
                        
                        tag.setString(s + prefixString);
                        
                        RemovedSuffixTag tag2 = tu.getSuffixTag();
                        if (tag2 == null)
                        {
                            tag2 = new RemovedSuffixTag();
                            tag2.setTu(tu);
                            tu.setSuffixTag(tag2);
                        }
                        
                        String s2 = tag2.getString();
                        if (s2 == null)
                            s2 = "";
                        
                        tag2.setString(suffixString + s2);
                    }
                }
            }
        }
        
        return gxml;
    }
    
    /**
     * Removes some tags and save the removed tags to database.
     * 
     * @param tuv
     *            the tuv to update
     * @param gxml
     *            the gxml of the tuv
     * @return new gxml of the tuv
     */
    private String removeTags(TuvImpl tuv, String gxml)
    {
        List<RemovedTag> removedTags = getTags(gxml);
        if (removedTags.size() > 0)
        {
            boolean flag = false;
            for (RemovedTag tag : removedTags)
            {
                if (!flag)
                {
                    flag = true;
                    TuImpl tu = (TuImpl) tuv.getTu();
                    tu.addRemoveTag(tag);
                    tag.setTu(tu);
                }

                for (int i = 0; i < tag.getOrgStrings().size(); i++)
                {
                    gxml = gxml.replace(tag.getOrgStrings().get(i), tag
                            .getNewStrings().get(i));
                }
            }
        }

        return gxml;
    }

    private String removePrefixTag(TuvImpl tuv, String gxml)
    {
        if (gxml == null || gxml.length() == 0)
        {
            return gxml;
        }

        Pattern p = Pattern.compile(REGEX_SEGMENT);
        Matcher m = p.matcher(gxml);
        if (m.find())
        {
            String segment = m.group(1);
            String content = m.group(2);

            String temp = content.trim();

            if (temp.startsWith("<") && !temp.startsWith("<bpt"))
            {
                Pattern p2 = Pattern.compile(REGEX_IT);
                Matcher m2 = p2.matcher(temp);
                if (m2.find())
                {
                    String prefixTag = m2.group();
                    
                    TuImpl tu = (TuImpl) tuv.getTu();
                    RemovedPrefixTag tag = tu.getPrefixTag();
                    if (tag == null)
                    {
                        tag = new RemovedPrefixTag();
                        tag.setTu(tu);
                        tu.setPrefixTag(tag);
                    }
                    
                    String s = tag.getString();
                    if (s == null)
                        s = "";
                    
                    tag.setString(s + prefixTag);
                    content = content.replaceFirst(REGEX_IT, "");
                    gxml = segment + content + "</segment>";
                }
            }
        }

        return gxml;
    }

    private String removeSuffixTag(TuvImpl tuv, String gxml)
    {
        if (gxml == null || gxml.length() == 0)
        {
            return gxml;
        }

        Pattern p = Pattern.compile(REGEX_SEGMENT);
        Matcher m = p.matcher(gxml);
        if (m.find())
        {
            String segment = m.group(1);
            String content = m.group(2);

            String temp = content.trim();

            if (temp.endsWith(">"))
            {
                Pattern p2 = Pattern.compile("(" + REGEX_IT + "\\s*)*$");
                Matcher m2 = p2.matcher(temp);
                if (m2.find())
                {
                    String suffixTag = m2.group();
                    TuImpl tu = (TuImpl) tuv.getTu();
                    
                    RemovedSuffixTag tag2 = tu.getSuffixTag();
                    if (tag2 == null)
                    {
                        tag2 = new RemovedSuffixTag();
                        tag2.setTu(tu);
                        tu.setSuffixTag(tag2);
                    }
                    
                    String s2 = tag2.getString();
                    if (s2 == null)
                        s2 = "";
                    
                    tag2.setString(suffixTag + s2);

                    content = content.replace(suffixTag, "");
                    gxml = segment + content + "</segment>";
                }
            }
        }

        return gxml;
    }

    /**
     * Merges some tags to one bpt.
     * 
     * @param s
     *            the gxml to update
     * @return new gxml after merging.
     */
    private String mergeOneBpt(String s)
    {
        Pattern p = Pattern.compile(REGEX_BPT);
        Matcher m = p.matcher(s);
        while (m.find())
        {
            Pattern p2 = Pattern.compile(MessageFormat.format(REGEX_BPT_ALL, m
                    .group(1)));
            Matcher m2 = p2.matcher(s);

            if (m2.find())
            {
                String all = m2.group();
                RemovedTag tag = extractForOneBpt(all);
                if (tag != null && tag.getTagNum() > 2)
                {
                    String content = tag.getNewStrings().get(0);
                    String bpt = m2.group(1) + tag.getPrefixString() + "</bpt>";
                    String ept = m2.group(2) + tag.getSuffixString() + "</ept>";
                    String newString = bpt + content + ept;
                    s = s.replace(tag.getOrgStrings().get(0), newString);
                    m = p.matcher(s);
                }
            }
            else
            {
                break;
            }
        }
        return s;
    }

    private RemovedTag extractForOneBpt(String s)
    {
        RemovedTag tag = null;

        String regex = "</[^>]*>([^<]+)<";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);
        
        List<String> contents = new ArrayList<String>();
        while (m.find())
        {
            contents.add(m.group(1));
        }
        
        String content = null;
        
        if (contents.size() == 0)
        {
            return null;
        }
        
        if (contents.size() == 1)
        {
            content = contents.get(0);
        }
            
        
        if (contents.size() > 1)
        {
            String first = contents.get(0);
            String end = contents.get(contents.size() - 1);
            
            int firstIndex = s.indexOf('>' + first + '<') + 1;
            int endIndex = s.indexOf('>' + end + '<') + 1 + end.length();
            
            content = s.substring(firstIndex, endIndex);
            if (content.indexOf("<bpt") > 0 || content.indexOf("<ept") > 0)
            {
                content = null;
            }
        }

        if (content != null)
        {
            tag = new RemovedTag();
//            String content = contents.get(0);
            int index = s.indexOf('>' + content + '<') + 1;
            String start = s.substring(0, index);
            String end = s.substring(index + content.length());

            tag.addOrgString(s);
            tag.addNewString(content);
            tag.setPrefixString(getContent(start, tag));
            tag.setSuffixString(getContent(end, tag));
        }

        return tag;
    }
    
    /**
     * Merges the same tags.
     * 
     * @param s
     *            the gxml to udpate
     * @return new gxml after merging
     */
    private String mergeSameTags(String s)
    {
        List<String> contents = new ArrayList<String>();
        String flag = "gsFlag";

        int i = 0;
        while (s.indexOf(flag) > -1)
        {
            i++;
            s += i;
        }

        Pattern p = Pattern.compile(REGEX_SAME_TAG);
        Matcher m = p.matcher(s);
        while (m.find())
        {
            String all = m.group();
            String bpt1 = m.group(2);
            String content1 = m.group(4);
            String ept1 = m.group(6);
            String bpt2 = m.group(9);
            String content2 = m.group(11);
            String ept2 = m.group(13);

            boolean match = false;
            if (ept1.equals(ept2))
            {
                bpt1 = bpt1.replace(PRESERVE, "");
                bpt2 = bpt2.replace(PRESERVE, "");

                bpt1 = bpt1.replaceAll(RSIDRPR_REGEX, "");
                bpt2 = bpt2.replaceAll(RSIDRPR_REGEX, "");

                bpt1 = bpt1.replaceAll(RSIDR_REGEX, "");
                bpt2 = bpt2.replaceAll(RSIDR_REGEX, "");

                if (bpt1.equals(bpt2))
                {
                    match = true;
                }
                else
                {
                    String tmp2 = bpt2.replaceAll(
                            "&lt;w:rFonts w:hint=&quot;[^/]*&quot;/&gt;", "");
                    if (bpt1.equals(tmp2))
                    {
                        match = true;
                    }
                }
            }

            if (match)
            {
                String b1 = m.group(2);
                String b2 = m.group(9);
                if (b1.length() > b2.length())
                {
                    StringBuffer s2 = new StringBuffer(m.group(1));
                    s2.append(b1).append(m.group(3)).append(content1);
                    s2.append(content2).append(m.group(5)).append(ept1);
                    s2.append(m.group(7));

                    s = s.replace(all, s2.toString());
                    m = p.matcher(s);
                }
                else
                {
                    StringBuffer s2 = new StringBuffer(m.group(8));
                    s2.append(b2).append(m.group(10)).append(content1);
                    s2.append(content2).append(m.group(12)).append(ept2);
                    s2.append(m.group(14));
                    s = s.replace(all, s2.toString());
                    m = p.matcher(s);
                }
            }
            else
            {
                Pattern p2 = Pattern.compile(REGEX_BPT);
                Matcher m2 = p2.matcher(s);
                if (m2.find())
                {
                    String s2 = m2.group();
                    contents.add(s2);
                    s = s.replaceFirst(REGEX_BPT, flag);
                    m = p.matcher(s);
                }
            }
        }

        for (String content : contents)
        {
            s = s.replaceFirst(flag, content);
        }

        return s;
    }

    /**
     * Removes tags that only includes a space.
     * 
     * @param s
     *            the gxml to update
     * @return new gxml after removing
     */
    private String removeTagForSpace(String s)
    {
        Pattern p = Pattern.compile(REGEX_BPT);
        Matcher m = p.matcher(s);
        while (m.find())
        {
            Pattern p2 = Pattern.compile(MessageFormat.format(
                    REGEX_BPT_ALL_SPACE, m.group(1)));
            Matcher m2 = p2.matcher(s);

            if (m2.find())
            {
                String all = m2.group();
                s = s.replace(all, m2.group(1));
            }
        }
        return s;
    }

    /**
     * Creates the templates for the source page/extracted file that has been
     * created from Gxml.
     */
    private List generateTemplates(SourcePage p_page, GxmlRootElement p_doc,
            List p_tuList) throws FileImportException
    {
        List templates = new ArrayList();

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
     * Creates the templates for the source page/extracted file that has been
     * created from PrsXml.
     */
    private void generateTemplates(SourcePage p_page, PrsRootElement p_doc,
            List p_tuList) throws FileImportException
    {
        TemplateGenerator tg = new TemplateGenerator();
        PageTemplate template;

        template = tg.generateDetail(p_doc, p_tuList);
        template.setSourcePage(p_page);
        ExtractedSourceFile esf = getExtractedSourceFile(p_page);
        esf.addPageTemplate(template, PageTemplate.TYPE_DETAIL);

        template = tg.generateStandard(p_doc, p_tuList);
        template.setSourcePage(p_page);
        esf.addPageTemplate(template, PageTemplate.TYPE_STANDARD);

        template = tg.generateExport(p_doc, p_tuList);
        template.setSourcePage(p_page);
        esf.addPageTemplate(template, PageTemplate.TYPE_EXPORT);
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
        if (profile.getTMChoice() != L10nProfile.NO_TM)
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
            SourcePage p_sourcePage) throws Exception
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

        TranslationMemoryProfile tmProfile = l10nProfile
                .getTranslationMemoryProfile();

        LeverageOptions leverageOptions = new LeverageOptions(tmProfile,
                leveragingLocales);

        TmCoreManager tmCoreManager = LingServerProxy.getTmCoreManager();

        return tmCoreManager.createLeverageDataCenterForPage(p_sourcePage,
                leverageOptions);
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
            SourcePage p_sourcePage) throws FileImportException
    {
        ExactMatchedSegments exactMatchedSegments = null;

        try
        {
            LeverageDataCenter leverageDataCenter = createLeverageDataCenter(
                    p_request, p_sourcePage);

            if (isReimport(p_sourcePage))
            {
                // leverage segments from previously cancelled page
                leveragePageFromTuv(p_request, p_sourcePage, leverageDataCenter);
            }

            // leverage from page and segment TM
            c_logger.info("Starting leverage from page and segment TM");
            TmCoreManager tmCoreManager = LingServerProxy.getTmCoreManager();
            tmCoreManager.leveragePage(p_sourcePage, leverageDataCenter);
            c_logger
                    .info("Finished leveraging successfuly from page and segment TM");

            // save the match results to leverage_match table
            tmCoreManager.saveLeverageResults(leverageDataCenter, p_sourcePage);

            // retrieve exact matches
            exactMatchedSegments = leverageDataCenter.getExactMatchedSegments();
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
        lev.leverageForReimport(p_sourcePage, localeLgIdMap, profile
                .getSourceLocale(), p_leverageDataCenter);

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
            SourcePage p_sourcePage)
    {
        TermLeverageResult result = null;

        String project = p_request.getL10nProfile().getProject().getName();
        String termbaseName = p_request.getL10nProfile().getProject()
                .getTermbaseName();

        ArrayList sourceTuvs = getSourceTranslatableTuvs(p_sourcePage);

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
                    result = ServerProxy.getTermLeverageManager()
                            .leverageTerms(sourceTuvs, options);
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
    private ArrayList getSourceTranslatableTuvs(SourcePage p_sourcePage)
    {
        ArrayList result = new ArrayList();

        List leverageGroups = getExtractedSourceFile(p_sourcePage)
                .getLeverageGroups();
        long sourceLocaleId = p_sourcePage.getLocaleId();
        Iterator itLeverageGroup = leverageGroups.iterator();

        while (itLeverageGroup.hasNext())
        {
            Collection tus = ((LeverageGroup) itLeverageGroup.next()).getTus();
            Iterator itTus = tus.iterator();

            while (itTus.hasNext())
            {
                Tu tu = (Tu) itTus.next();
                if (!tu.isLocalizable())
                {
                    result.add(tu.getTuv(sourceLocaleId));
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
                termbaseId = manager.getTermbaseId(p_termbaseName);
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
                    ITermbase.SYSTEM_USER, "");

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
                    options
                            .addTargetPageLocale2LangName(targetLocale,
                                    langName);

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
        }
        while (n > 0);

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

    public static boolean getLeveragematch()
    {
        return s_autoReplaceTerms.booleanValue();
    }
}
