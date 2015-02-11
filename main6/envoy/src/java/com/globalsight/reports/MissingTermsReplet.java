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
package com.globalsight.reports;

import inetsoft.report.ReportSheet;
import inetsoft.report.StyleConstants;
import inetsoft.report.StyleSheet;
import inetsoft.report.TextElement;
import inetsoft.sree.RepletException;
import inetsoft.sree.RepletRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.dom4j.Document;
import org.dom4j.Element;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.reports.handler.BasicReportHandler;
import com.globalsight.reports.util.LabeledValueHolder;
import com.globalsight.reports.util.ReportsPackage;
import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.util.XmlParser;

/**
 * Presents a report on missing terms from Terminology.
 */
public class MissingTermsReplet extends GlobalSightReplet
{
    private static final String MY_TEMPLATE = "/templates/basicFlowReport.srt";
    private static final String MY_MESSAGES = BasicReportHandler.BUNDLE_LOCATION
            + "missingTerms";

    /** Query to find all the terms and their languages in a termbase * */
    private static final String TERM_ENTRY_LANG_QUERY = "select cid,name from tb_language where tbid=? order by cid, name";

    private static final String TERM_BASE_QUERY = "select tbid, tb_name from tb_termbase order by tb_name";

    private static final String TERM_LANG_QUERY = "select distinct name from tb_language order by name";

    private static final String LABEL_SUFFIX = ": ";
    protected ResourceBundle m_bundle = null;
    private StyleSheet ss = null;
    private ArrayList m_termbaseLangs = null;
    private String m_selectedLang = null;
    private TreeMap m_map = null;

    private static final String ALL = "ALL";

    public MissingTermsReplet()
    {
        readTemplate();
    }

    /**
     * Initializes the report and sets all the required parameters <br>
     * 
     * @param RepletRequest
     * @throws RepletException
     */
    public void init(RepletRequest req) throws RepletException
    {
        try
        {
            super.init(req); // get the common parameters
            m_bundle = ResourceBundle.getBundle(MY_MESSAGES, theUiLocale);
            addMoreRepletParameters();
        }
        catch (RepletException re)
        {
            throw re;
        }
        catch (Exception e)
        {
            throw new RepletException(e.getMessage());
        }
    }

    /**
     * Returns the Style Report Template name
     */
    public String getTemplateName()
    {
        return MY_TEMPLATE;
    }

    /**
     * Adds additional report creation parameters to the replet parameters
     */
    private void addMoreRepletParameters() throws Exception
    {
        addTermbaseParameter();
        addLanguageParameter();
        addRepletParameters(theParameters);
    }

    /**
     * Adds the termbase ids
     */
    protected void addTermbaseParameter() throws Exception
    {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            ArrayList termbaseIds = new ArrayList();
            c = ConnectionPool.getConnection();
            ps = c.prepareStatement(TERM_BASE_QUERY);
            rs = ps.executeQuery();
            while (rs.next())
            {
                LabeledValueHolder lvh = new LabeledValueHolder(
                        rs.getString(1), rs.getString(2));
                termbaseIds.add(lvh);
            }

            theParameters.addChoice("termbaseId", termbaseIds.get(0),
                    termbaseIds.toArray());
            theParameters.setAlias("termbaseId",
                    ReportsPackage.getMessage(m_bundle, "termbase"));
        }
        finally
        {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(ps);
            ConnectionPool.silentReturnConnection(c);
        }
    }

    /**
     * Adds the termbase languages
     */
    protected void addLanguageParameter() throws Exception
    {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            ArrayList termbaseLangs = new ArrayList();
            termbaseLangs.add(ALL);
            c = ConnectionPool.getConnection();
            ps = c.prepareStatement(TERM_LANG_QUERY);
            rs = ps.executeQuery();
            while (rs.next())
            {
                termbaseLangs.add(rs.getString(1));
            }

            // We should use .addList() and use a multi-select list instead
            // to let the user select multiple languages...BUT
            // this widget just does not work in the current inetsoft version
            // with the DHTML viewer (ok for java viewer)
            // So, we're stuck with giving the user one value to choose.
            theParameters.addChoice("selectedLang", ALL,
                    termbaseLangs.toArray());
            theParameters.setAlias("selectedLang", "Language");
        }
        finally
        {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(ps);
            ConnectionPool.silentReturnConnection(c);
        }
    }

    /**
     * Creates the actual report and fills it with data and messages. Also
     * determines the grouping and chart styles. <br>
     * 
     * @param RepletRequest
     * @return ReportSheet
     * @throws RepletException
     */
    public ReportSheet createReport(RepletRequest req) throws RepletException
    {
        ss = (StyleSheet) theReport;
        try
        {
            bindMessages(req);
            bindData(req);
        }
        catch (Exception e)
        {
            ReportsPackage.logError("Problem creating report", e);
            throw new RepletException(e.getMessage());
        }
        return theReport;
    }

    /**
     * Fills out the messages on the report <br>
     * 
     * @param RepletRequest
     */
    private void bindMessages(RepletRequest req)
    {
        setCommonMessages(req);
        TextElement txtReportTitle = (TextElement) theReport
                .getElement("txtReportTitle");
        txtReportTitle.setText(ReportsPackage.getMessage(m_bundle,
                "txtReportTitle"));
    }

    /**
     * Gets the data from the DB and binds it to charts and tables <br>
     * 
     * @param RepletRequest
     * @throws Exception
     */
    private void bindData(RepletRequest req) throws Exception
    {
        LabeledValueHolder param = (LabeledValueHolder) req
                .getParameter("termbaseId");
        String termbaseName = param.getLabel();
        String termbaseId = (String) param.getValue();

        m_selectedLang = req.getString("selectedLang");
        addCriteriaFormAtTop(termbaseName);

        // create a HashMap holding the termbase languages
        ITermbaseManager s_manager = ServerProxy.getTermbaseManager();
        ITermbase tb = s_manager
                .connect(termbaseName, "MissingTermsReport", "");
        String definition = tb.getDefinition();
        setTermbaseLangs(definition);

        // query the entry langs to see what entries we do have
        fillEntryLangMap(termbaseId);

        // now go through the termbase langs and print out the missing entries
        Iterator langIter = m_termbaseLangs.iterator();
        while (langIter.hasNext())
        {
            String currentLang = (String) langIter.next();
            if (!(ALL.equals(m_selectedLang) || currentLang
                    .equals(m_selectedLang)))
                continue;

            ss.setCurrentFont(GlobalSightReplet.FONT_LABEL);
            ss.addText(ReportsPackage.getMessage(m_bundle, "missing") + " "
                    + currentLang);
            ss.setCurrentFont(GlobalSightReplet.FONT_NORMAL);
            ss.addNewline(2);
            Iterator keyIter = m_map.keySet().iterator();
            int numMissing = 0;
            while (keyIter.hasNext())
            {
                Long key = (Long) keyIter.next();
                HashSet set = (HashSet) m_map.get(key);

                if (set.contains(currentLang) == false)
                {
                    ss.addText(ReportsPackage.getMessage(m_bundle, "entry")
                            + " " + key + " -- ");
                    String term = getTerm(tb, key.longValue());
                    ss.addText(term);
                    ss.addNewline(1);
                    numMissing++;
                }
            }
            ss.setCurrentFont(GlobalSightReplet.FONT_LABEL);
            if (numMissing == 0)
            {
                ss.addText(ReportsPackage.getMessage(m_bundle, "noMissing"));

            }
            else
            {
                ss.addText(ReportsPackage.getMessage(m_bundle, "totalMissing")
                        + " " + numMissing);
            }
            ss.setCurrentFont(GlobalSightReplet.FONT_NORMAL);
            ss.addPageBreak();
        }
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
        List langGrps = root.selectNodes("/definition/languages/language/name");
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
     * Returns the term's text in some language (most likely English). This
     * really just grabs the first language out of the entryXml
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
        List langGrps = root
                .selectNodes("/conceptGrp/languageGrp/termGrp/term");
        Element firstTerm = (Element) langGrps.get(0);
        String termText = firstTerm.getText();
        XmlParser.fire(parser);
        return termText;
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
     * Adds a form at the top with the selected termbase <br>
     * 
     * @param termbaseName
     */
    private void addCriteriaFormAtTop(String p_termbaseName)
    {
        String termbaseLabel = ReportsPackage.getMessage(m_bundle, "termbase");
        ss.setCurrentFont(GlobalSightReplet.FONT_LABEL);
        ss.addText(termbaseLabel + LABEL_SUFFIX + p_termbaseName);
        ss.setCurrentFont(GlobalSightReplet.FONT_NORMAL);
        ss.addSeparator(StyleConstants.THIN_LINE);
    }
}
