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
package com.globalsight.reports.handler;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.Element;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.reports.Constants;
import com.globalsight.reports.datawrap.MissingTermsDataWrap;
import com.globalsight.reports.util.ReportHandlerFactory;
import com.globalsight.reports.util.ReportsPackage;
import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.util.XmlParser;

/**
 * Presents a report on missing terms from Terminology.
 */
public class MissingTermsReportHandler extends BasicReportHandler
{
    private TreeMap m_map = null;
    private String m_selectedLang = null;
    protected ResourceBundle m_bundle = null;
    private ArrayList m_termbaseLangs = null;
    private MissingTermsDataWrap reportDataWrap = null;

    private static final String ALL = "ALL";
    private static final String LABEL_SUFFIX = ": ";
    private static final String BLANK = " ";
    private static final String ORIGINAL_PAGEID = "1";
    private static final String USERNAME = "MissingTermsReport";
    private static final String PASSWORD = "";
    private static final String SEPRATOR = " --     ";
    private static final String MY_MESSAGES = BUNDLE_LOCATION + "missingTerms";
    private static final String LANGUAGEDEFINITION = "/definition/languages/language/name";
    private static final String TERMBASEXML = "/conceptGrp/languageGrp/termGrp/term";
    // Query to find all the terms and their languages in a termbase
    private static final String TERM_LANG_QUERY = "select distinct(name) from tb_language order by name";
    private static final String TERM_BASE_QUERY = "select tbid, tb_name from tb_termbase tb "
            + "where tb.companyid = ? order by tb_name";
    private static final String TERM_BASE_QUERY_GS = "select tbid, tb_name from tb_termbase order by tb_name";
    private static final String TERM_ENTRY_LANG_QUERY = "select cid,name from tb_language where tbid=? order by cid, name";

    private static final int pageCount = 50;

    /**
     * Initializes the report and sets all the required parameters
     */
    public void init()
    {
        try
        {
            super.init(); // get the common parameters
            m_bundle = ResourceBundle.getBundle(MY_MESSAGES, theUiLocale);
            this.reportKey = Constants.MISSINGTERMS_REPORT_KEY;
        }
        catch (Exception e)
        {
            ReportsPackage.logError(e);
        }
    }

    /**
     * The entry of the handler. This method will dispatch the
     * HttpServletRequest to different page based on the value of
     * <code>Constants.REPORT_ACT</code>. <code>Constants.REPORT_ACT_PREP</code>
     * means this is request for prepare the parameter data, then system will
     * show the parameter page. <code>Constants.REPORT_ACT_CREATE</code> means
     * create the real report based on the user input data, then system will
     * show the report page.
     */
    public void invokeHandler(HttpServletRequest req, HttpServletResponse res,
            ServletContext p_context) throws Exception
    {
        super.invokeHandler(req, res, p_context);
        String act = (String) req.getParameter(Constants.REPORT_ACT);
        if (Constants.REPORT_ACT_PREP.equalsIgnoreCase(act))
        {
            cleanSession(theSession);
            addMoreReportParameters(req); // prepare data for parameter page
            dispatcherForward(
                    ReportHandlerFactory.getTargetUrl(reportKey
                            + Constants.REPORT_ACT_PREP), req, res, p_context);
        }
        else if (Constants.REPORT_ACT_CREATE.equalsIgnoreCase(act))
        {
            createReport(req); // fill report DataWrap with data
            dispatcherForward(
                    ReportHandlerFactory.getTargetUrl(reportKey
                            + Constants.REPORT_ACT_CREATE), req, res, p_context);
        }
        else if (Constants.REPORT_ACT_TURNPAGE.equalsIgnoreCase(act))
        {
            String pageId = req.getParameter(Constants.REPORT_SHOWPAGE_PAGEID);
            bindData(req, pageId); // bind the data to one report page
            dispatcherForward(
                    ReportHandlerFactory.getTargetUrl(reportKey
                            + Constants.REPORT_ACT_CREATE), req, res, p_context);
        }
    }

    /**
     * Prepare the data for the parameter of report page <br>
     * 
     * @param HttpServletRequest
     * @throws Exception
     */
    private void addMoreReportParameters(HttpServletRequest req)
            throws Exception
    {
        addTermbaseParameter(m_bundle, req);
        addLanguageParameter(m_bundle, req);
    }

    /**
     * Initializes a parameter for Termbase name <br>
     * 
     * @param ResourcBundle
     * @param HttpServletRequest
     * @throws Exception
     */
    private void addTermbaseParameter(ResourceBundle p_bundle,
            HttpServletRequest req) throws Exception
    {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            ArrayList termbaseNames = new ArrayList();
            c = ConnectionPool.getConnection();

            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                ps = c.prepareStatement(TERM_BASE_QUERY);
                ps.setLong(1, Long.parseLong(currentId));
            }
            else
            {
                ps = c.prepareStatement(TERM_BASE_QUERY_GS);
            }

            rs = ps.executeQuery();
            HashMap termbasesMap = new HashMap();
            while (rs.next())
            {
                // save for usage in future
                termbasesMap.put(rs.getString(2), rs.getString(1));
                termbaseNames.add(rs.getString(2));
            }
            setSessionAttribute(theSession, Constants.TERMBASE_HASHMAP,
                    termbasesMap);
            String termbaseLabel = ReportsPackage.getMessage(p_bundle,
                    Constants.TERMBASE);
            req.setAttribute(Constants.TERMBASE_NAME, termbaseNames);
            req.setAttribute(Constants.TERMBASE_LABEL, termbaseLabel);
        }
        finally
        {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(ps);
            ConnectionPool.silentReturnConnection(c);
        }
    }

    /**
     * Initializes a parameter for Language name <br>
     * 
     * @throws Exception
     */
    private void addLanguageParameter(ResourceBundle p_bundle,
            HttpServletRequest req) throws Exception
    {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            ArrayList termbaseLangs = new ArrayList();
            ArrayList termbaseLangLables = new ArrayList();
            termbaseLangLables.add(p_bundle.getString("all"));
            termbaseLangs.add(ALL);
            c = ConnectionPool.getConnection();
            ps = c.prepareStatement(TERM_LANG_QUERY);
            rs = ps.executeQuery();
            while (rs.next())
            {
                termbaseLangs.add(rs.getString(1));
                termbaseLangLables.add(rs.getString(1));
            }
            // We should use .addList() and use a multi-select list instead
            // to let the user select multiple languages...BUT
            // this widget just does not work in the current inetsoft version
            // with the DHTML viewer (ok for java viewer)
            // So, we're stuck with giving the user one value to choose.
            String langsLabel = ReportsPackage.getMessage(p_bundle,
                    Constants.LANGUAGE);
            req.setAttribute(Constants.TERMBASELANG_LABEL, langsLabel);
            req.setAttribute(Constants.TERMBASE_LANGS, termbaseLangs);
            req.setAttribute(Constants.TERMBASE_LANG_LABLES, termbaseLangLables);
        }
        finally
        {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(ps);
            ConnectionPool.silentReturnConnection(c);
        }
    }

    /**
     * Get TermbaseId by Termbase Name <br>
     * 
     * @param HttpServletRequest
     * @param String
     */
    private String getTermBaseIdByName(String termbaseName)
    {
        String termbaseId = "-1";
        try
        {
            HashMap termbasesMap = (HashMap) getSessionAttribute(theSession,
                    Constants.TERMBASE_HASHMAP);
            termbaseId = (String) termbasesMap.get(termbaseName);
        }
        catch (Exception e)
        {
            ReportsPackage.logError(e);
        }

        return termbaseId;
    }

    /**
     * Creates the actual report and fills it with data and messages. <br>
     * 
     * @param HttpServletRequest
     */
    public void createReport(HttpServletRequest req)
    {
        try
        {
            reportDataWrap = new MissingTermsDataWrap();
            bindMessages();
            fillAllData(req);
            bindData(req, ORIGINAL_PAGEID);
        }
        catch (Exception e)
        {
            ReportsPackage.logError(e);
        }
    }

    /**
     * Fills out the messages on the report
     */
    private void bindMessages()
    {
        reportDataWrap.setReportTitle(ReportsPackage.getMessage(m_bundle,
                Constants.REPORT_TITLE));
        // set common messages for report, such as header ,footer
        setCommonMessages(reportDataWrap);
    }

    /**
     * fill all data into HashMap(key: pageId, value: pageDataWrap) <br>
     * 
     * @param HttpServletRequest
     * @throws Exception
     */
    private void fillAllData(HttpServletRequest req) throws Exception
    {

        String termbaseName = (String) req
                .getParameter(Constants.TERMBASE_NAME);
        String termbaseId = (String) getTermBaseIdByName(termbaseName);

        m_selectedLang = (String) req.getParameter(Constants.TERMBASE_LANGS);
        addCriteriaFormAtTop(termbaseName);

        // create a HashMap holding the termbase languages
        ITermbaseManager s_manager = ServerProxy.getTermbaseManager();
        ITermbase tb = s_manager.connect(termbaseName, USERNAME, PASSWORD);
        String definition = tb.getDefinition();
        setTermbaseLangs(definition);

        // query the entry langs to see what entries we do have
        fillEntryLangMap(termbaseId);

        // now go through the termbase langs and print out the missing entries
        Iterator langIter = m_termbaseLangs.iterator();
        int pageId = 0;
        HashMap pageId2DataMap = new HashMap();
        while (langIter.hasNext())
        {
            String currentLang = (String) langIter.next();
            if (!(ALL.equals(m_selectedLang) || currentLang
                    .equals(m_selectedLang)))
            {
                continue;
            }

            PageDataWrap pageDataWrap = new PageDataWrap();
            pageDataWrap.setPageHeader(ReportsPackage.getMessage(m_bundle,
                    Constants.MISSING) + BLANK + currentLang);
            Iterator keyIter = m_map.keySet().iterator();
            int numMissing = 0;

            while (keyIter.hasNext())
            {
                Long key = (Long) keyIter.next();
                HashSet set = (HashSet) m_map.get(key);
                if (set.contains(currentLang) == false)
                {
                    String term = getTerm(tb, key.longValue());
                    String temp = ReportsPackage.getMessage(m_bundle,
                            Constants.ENTRY) + BLANK + key + SEPRATOR + term;
                    pageDataWrap.setMissingItems(temp);

                    if ((numMissing + 1) % pageCount == 0)
                    {
                        pageDataWrap.setPageFooter(BLANK);
                        pageId2DataMap.put(new Integer(++pageId), pageDataWrap);
                        pageDataWrap = new PageDataWrap();
                        pageDataWrap.setPageHeader(ReportsPackage.getMessage(
                                m_bundle, Constants.MISSING)
                                + BLANK
                                + currentLang);
                    }

                    numMissing++;
                }
            }

            if (numMissing == 0)
            {
                pageDataWrap.setPageFooter(ReportsPackage.getMessage(m_bundle,
                        Constants.NOMISSING));
            }
            else
            {
                pageDataWrap.setPageFooter(ReportsPackage.getMessage(m_bundle,
                        Constants.TOTALMISSING) + BLANK + numMissing);
            }
            // put the pageDataWrap into HashMap(Key: pageId, Value:
            // pageDataWrap)
            pageId2DataMap.put(new Integer(++pageId), pageDataWrap);
        }
        reportDataWrap.setCurrentPageNum(0);
        reportDataWrap.setTotalPageNum(pageId);

        if (pageId2DataMap.size() == 0)
        {
            PageDataWrap pageDataWrap = new PageDataWrap();
            pageDataWrap.setPageHeader(BLANK);
            pageDataWrap.setPageFooter(BLANK);
            pageId2DataMap.put(new Integer(1), pageDataWrap);
            reportDataWrap.setTotalPageNum(1);
        }
        setSessionAttribute(theSession, Constants.PAGEDATAWRAP_HASHMAP,
                pageId2DataMap);
        setSessionAttribute(theSession, reportKey + Constants.REPORT_DATA_WRAP,
                reportDataWrap);
    }

    /**
     * Bind one pageDataWrap to MissingTermsDataWrap <br>
     * 
     * @param HttpServletRequest
     * @param pageId
     * @throws Exception
     */
    private void bindData(HttpServletRequest req, String pageId)
            throws Exception
    {
        HashMap datawrapMap = (HashMap) getSessionAttribute(theSession,
                Constants.PAGEDATAWRAP_HASHMAP);
        MissingTermsDataWrap reportDataWrap2 = (MissingTermsDataWrap) getSessionAttribute(
                theSession, reportKey + Constants.REPORT_DATA_WRAP);
        if (reportDataWrap2 == null || datawrapMap == null)
        {
            return;
        }

        int curPageId = reportDataWrap2.getCurrentPageNum();
        int nextId = 1;
        try
        {
            nextId = Integer.parseInt(pageId);
        }
        catch (Exception exc)
        {
            nextId = curPageId;
        }
        if (nextId > reportDataWrap2.getTotalPageNum() || nextId <= 0)
        {
            nextId = curPageId;
        }
        PageDataWrap pageDataWrap = (PageDataWrap) datawrapMap.get(new Integer(
                nextId));
        reportDataWrap2.setPageHeader(pageDataWrap.getPageHeader());
        reportDataWrap2.setMissingItems(pageDataWrap.getMissingItems());
        reportDataWrap2.setPageFooter(pageDataWrap.getPageFooter());
        reportDataWrap2.setCurrentPageNum(nextId);
        req.setAttribute(Constants.MISSINGTERMS_REPORT_DATA, reportDataWrap2);
    }

    /**
     * Adds a form at the top of the report <br>
     * 
     * @param termbaseName
     */
    private void addCriteriaFormAtTop(String termbaseName)
    {
        String termbaseLabel = ReportsPackage.getMessage(m_bundle,
                Constants.TERMBASE) + LABEL_SUFFIX;
        ArrayList criteriaFormLabel = new ArrayList();
        ArrayList criteriaFormValue = new ArrayList();
        criteriaFormLabel.add(termbaseLabel);
        criteriaFormValue.add(termbaseName);
        reportDataWrap.setCriteriaFormLabel(criteriaFormLabel);
        reportDataWrap.setCriteriaFormValue(criteriaFormValue);
    }

    /**
     * Figures out what languages are in the Termbase. These are the user
     * specified language names.
     * 
     * @param p_xmlDefinition
     *            termbase def xml
     */
    private void setTermbaseLangs(String p_xmlDefinition) throws Exception
    {
        XmlParser parser = XmlParser.hire();
        Document dom = parser.parseXml(p_xmlDefinition);
        Element root = dom.getRootElement();
        List langGrps = root.selectNodes(LANGUAGEDEFINITION);
        m_termbaseLangs = new ArrayList();

        for (int i = 0; i < langGrps.size(); i++)
        {
            Element e = (Element) langGrps.get(i);
            String lang = e.getText();
            m_termbaseLangs.add(lang);
        }

        XmlParser.fire(parser);
    }

    /**
     * Queries the tb_language table to find out what entries we do have for
     * various languages
     * 
     * @param p_termbaseId
     *            termbase id
     */
    private void fillEntryLangMap(String p_termbaseId) throws Exception
    {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            c = ConnectionPool.getConnection();
            ps = c.prepareStatement(TERM_ENTRY_LANG_QUERY);
            ps.setLong(1, Long.valueOf(p_termbaseId).longValue());
            rs = ps.executeQuery();
            m_map = new TreeMap();
            while (rs.next())
            {
                Long cid = new Long(rs.getLong(1));
                String name = rs.getString(2);
                HashSet set = (HashSet) m_map.get(cid);
                if (set == null)
                {
                    set = new HashSet();
                    m_map.put(cid, set);
                }
                set.add(name);
            }
        }
        finally
        {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(ps);
            ConnectionPool.silentReturnConnection(c);
        }
    }

    /**
     * Returns the term's text in some language (most likely English). This
     * really just grabs the first language out of the entryXml <br>
     * 
     * @param p_tb
     *            termbase
     * @param p_entryId
     *            entry (concept) id
     * @return term text
     */
    private String getTerm(ITermbase p_tb, long p_entryId) throws Exception
    {
        String entryXml = p_tb.getEntry(p_entryId);
        XmlParser parser = XmlParser.hire();
        Document dom = parser.parseXml(entryXml);
        Element root = dom.getRootElement();
        List langGrps = root.selectNodes(TERMBASEXML);
        Element firstTerm = (Element) langGrps.get(0);
        String termText = firstTerm.getText();
        XmlParser.fire(parser);
        return termText;
    }

    /**
     * Internal class for usage of separation page One instance for one page
     */
    class PageDataWrap implements Serializable
    {

        private String pageHeader = null;
        private ArrayList missingItems = new ArrayList();
        private String pageFooter = null;

        public void setPageHeader(String header)
        {
            this.pageHeader = header;
        }

        public String getPageHeader()
        {
            return this.pageHeader;
        }

        public void setMissingItems(String missingItem)
        {
            if (missingItems != null)
            {
                missingItems.add(missingItem);
            }
        }

        public ArrayList getMissingItems()
        {
            return this.missingItems;
        }

        public void setPageFooter(String footer)
        {
            this.pageFooter = footer;
        }

        public String getPageFooter()
        {
            return this.pageFooter;
        }
    }
}
