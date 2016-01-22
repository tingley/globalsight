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

package com.globalsight.everest.tm.exporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.globalsight.everest.edit.offline.page.TmxUtil;
import com.globalsight.everest.projecthandler.ProjectTmTuTProp;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.exporter.ExportOptions.FilterOptions;
import com.globalsight.everest.tm.util.Tmx;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.exporter.ExportOptions;
import com.globalsight.exporter.IWriter;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.machineTranslation.MachineTranslator;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.StringUtil;
import com.globalsight.util.UTC;
import com.globalsight.util.XmlParser;
import com.globalsight.util.edit.EditUtil;

/**
 * Writes TU entries to a TMX file as directed by the conversion settings in the
 * supplied export options.
 * 
 * We should eventually support writing TMX level 1, level 2, G-TMX (a
 * proprieatry extension of TMX to use for backups), and TTX (Trados' dialect of
 * TMX).
 */
public class TmxWriter implements IWriter
{
    private static final Logger CATEGORY = Logger.getLogger(TmxWriter.class);

    // TMX levels determine how much information gets output,
    // and in which form.
    static public final int TMX_LEVEL_TRADOS = 0;
    static public final int TMX_LEVEL_1 = 1;
    static public final int TMX_LEVEL_2 = 2;
    static public final int TMX_LEVEL_NATIVE = 10;

    //
    // Private Member Variables
    //
    private Tm m_database;
    private com.globalsight.everest.tm.exporter.ExportOptions m_options;
    private PrintWriter m_output;
    private String m_filename;

    // TMX header info
    private Tmx m_tmx;
    private int m_tmxLevel;

    // Helper for printing XML strings with empty elements expanded (for Trados)
    private OutputFormat m_outputFormat;

	private final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");
	private final SimpleDateFormat format = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
    //
    // Constructors
    //

    public TmxWriter(ExportOptions p_options, Tm p_database)
    {
        m_database = p_database;
        setExportOptions(p_options);

        m_outputFormat = new OutputFormat();
        m_outputFormat.setExpandEmptyElements(true);
    }
    
    public TmxWriter(ExportOptions p_options, Tm p_database,Tmx tmx)
    {
    	m_tmx = tmx;
        m_database = p_database;
        setExportOptions(p_options);

        m_outputFormat = new OutputFormat();
        m_outputFormat.setExpandEmptyElements(true);
    }

    //
    // Interface Implementation -- IWriter
    //

    public void setExportOptions(ExportOptions p_options)
    {
        m_options = (com.globalsight.everest.tm.exporter.ExportOptions) p_options;

        m_tmxLevel = getTmxLevel(m_options);
    }

    /**
     * Analyzes export options and returns an updated ExportOptions object with
     * a status whether the options are syntactically correct.
     */
    public ExportOptions analyze()
    {
        return m_options;
    }

    /**
     * Writes the file header (eg for TBX).
     */
    public void writeHeader(SessionInfo p_session) throws IOException
    {
		m_filename = m_options.getFileName();
		String identifyKey = m_options.getIdentifyKey();
		String directory = ExportUtil.getExportDirectory();
		if (identifyKey != null && !identifyKey.equals(""))
		{
			directory = directory + "/" + identifyKey;
		}
        new File(directory).mkdirs();

        String encoding = m_options.getJavaEncoding();
        if (encoding == null || encoding.length() == 0)
        {
            throw new IOException("invalid encoding " + m_options.getEncoding());
        }

        // We support only Unicode encodings for XML files: UTF-8 and
        // UTF-16 (little and big endian)
        String ianaEncoding = m_options.getEncoding();
        if (ianaEncoding.toUpperCase().startsWith("UTF-16"))
        {
            ianaEncoding = "UTF-16";
        }
		String filename = directory + "/" + m_filename;
		new File(filename).delete();

		m_output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(filename), encoding)));

        m_tmx = createTmxHeader(p_session);

        m_output.print("<?xml version=\"1.0\" encoding=\"");
        m_output.print(ianaEncoding);
        m_output.println("\" ?>");
        m_output.println(m_tmx.getTmxDeclaration());
        m_output.print("<tmx version=\"");
        m_output.print(m_tmx.getTmxVersion());
        m_output.println("\">");
        m_output.print(m_tmx.getHeaderXml());
        m_output.println("<body>");

        checkIOError();
    }

    /**
     * Writes the file trailer (eg for TMX).
     */
    public void writeTrailer(SessionInfo p_session) throws IOException
    {
        m_output.println("</body>");
        m_output.println("</tmx>");

        m_output.close();
    }

    /**
     * Writes the next few entries to the file.
     * 
     * @param p_entries
     *            ArrayList of TU objects.
     */
    public void write(ArrayList p_entries, SessionInfo p_session)
            throws IOException
    {
        for (int i = 0; i < p_entries.size(); ++i)
        {
            Object o = p_entries.get(i);

            write(o, p_session);
        }
    }

    /**
     * Writes a single entry to the export file.
     * 
     */
    public void write(Object p_entry, SessionInfo p_session) throws IOException
    {
        if (p_entry == null)
        {
            return;
        }

        try
        {
            SegmentTmTu tu = (SegmentTmTu) p_entry;

            // convert TU to export level
            convertTuToTmxLevel(tu, m_tmxLevel);

            // then convert to XML and print
//            String xml = convertToTmx(tu, m_tmx, m_options, m_outputFormat);
			String xml = convertToTmx(tu, m_tmx, m_options, m_outputFormat,	true);
            xml = TmxUtil.operateCDATA(xml);

            // Goes through lengths not to throw an IO exception,
            // will check below.
            m_output.print(xml);
        }
        catch (Throwable ignore)
        {
            // Couldn't convert a TU/TUV, log error and continue.

            CATEGORY.error("Can't convert TU to TMX, skipping.", ignore);
        }

        checkIOError();
    }

    public String getSegmentTmForXml(Object p_entry) throws IOException
	{
		String xml = null;
		if (p_entry == null)
		{
			return xml;
		}
		try
		{
			SegmentTmTu tu = (SegmentTmTu) p_entry;
			convertTuToTmxLevel(tu, m_tmxLevel);
			xml = convertToTmx(tu, m_tmx, m_options, m_outputFormat, true);
			xml = TmxUtil.operateCDATA(xml);
		}
		catch (Throwable ignore)
		{
			CATEGORY.error("Can't convert TU to TMX, skipping.", ignore);
		}
		return xml;
	}
    
    //
    // Private Methods
    //

    /**
     * Converts the native information in a TU to what we want to output
     * according to the TMX export level. This means we strip out internal tags
     * for level 1, and convert tags to Trados form if we need to.
     */
    private void convertTuToTmxLevel(SegmentTmTu p_tu, int p_level)
            throws Exception
    {
        List<BaseTmTuv> tuvs = p_tu.getTuvs();

        for (int i = 0; i < tuvs.size(); i++)
        {
            SegmentTmTuv tuv = (SegmentTmTuv) tuvs.get(i);
            convertTuvToTmxLevel(p_tu, tuv, p_level);
        }

        // TMX 1.4 standard don't allow the some tag such as "ph" has id
        // attribute, but the xliff standard rule the id attribute is the
        // require attribute of ph tag. When use xliff file create a job, and
        // after export the local tm database segment will have ph tag including
        // id attribute, and export the tmx and reimport other tm, will error.
        // so when export, must remove the id attribute of ph tag.
        TmxChecker tmxChecker = new TmxChecker();
        tmxChecker.fixTuvByDtd(tuvs);

        // Fix any remaining TU-wide problems.
        // (This parses TUVs again but such is life.)

        // Fix "x" and "i" numbering for TMX compliance.
        // "x" must start with 1, and "i" in target must use
        // the same value as "i" in source.
        SegmentTmTuv sourceTuv = (SegmentTmTuv) p_tu.getSourceTuv();
        tuvs.remove(sourceTuv);
        fixAttributeIX(sourceTuv, tuvs);
    }

    /**
     * Converts the native information in a TUV to what we want to output
     * according to the TMX export level. This means we strip out internal tags
     * for level 1, and convert tags to Trados form if we need to.
     */
    public static SegmentTmTuv convertTuvToTmxLevel(SegmentTmTu p_tu,
            SegmentTmTuv p_tuv, int p_level)
    {
        String format = p_tu.getFormat();

        Document dom = getDom(p_tuv.getSegment());
        Element root = dom.getRootElement();

        // For any non-native format, remove all non-TMX attributes.
        // We output the tmx-gs.dtd (in ../util) for NATIVE.
        if (p_level != TMX_LEVEL_NATIVE)
        {
            removeNonTmxAttributes(root);
        }

        // TMX Level 1 does not contain any internal tags.
        if (p_level == TMX_LEVEL_1)
        {
            replaceNbsps(root);

            removeNodes(root, "//bpt");
            removeNodes(root, "//ept");
            removeNodes(root, "//ph");
            removeNodes(root, "//it");
            removeNodes(root, "//ut");
            removeNodes(root, "//hi");
        }
        // TMX_LEVEL_2, Native G-TMX, TMX_LEVEL_TRADOS
        else
        {
			// TMX Compliance: output formatting tags like <bpt type="bold"/>
			// with an HTML code inside.
			if (format.equalsIgnoreCase("html")) {
				injectStandardFormattingCodes(root);
			}

			// Remove any SUB tags.
            removeSubElements(root);
        }

        p_tuv.setSegment(root.asXML());

        return p_tuv;
    }

    //
    // Helper Methods
    //

    /**
     * Removes attributes that were added to G-TMX but are not valid in TMX.
     */
    private static void removeNonTmxAttributes(Element p_segment)
    {
        removeNodes(p_segment, "//@erasable");
        removeNodes(p_segment, "//@movable");
        removeNodes(p_segment, "//@wordcount");
        removeNodes(p_segment, "//it/@i");
    }

    /**
     * Removes nodes identified by the XPath p_path from a DOM Element.
     */
    private static void removeNodes(Element p_segment, String p_path)
    {
        List nodes = p_segment.selectNodes(p_path);

        for (int i = 0; i < nodes.size(); i++)
        {
            Node node = (Node) nodes.get(i);

            node.detach();
        }
    }

    /**
     * Converts an XML string to a DOM document.
     */
    private static Document getDom(String p_xml)
    {
        XmlParser parser = null;

        try
        {
            parser = XmlParser.hire();
            return parser.parseXml(p_xml);
        }
        catch (Exception ex)
        {
            throw new RuntimeException("invalid GXML `" + p_xml + "': "
                    + ex.getMessage());
        }
        finally
        {
            XmlParser.fire(parser);
        }
    }

	public String convertToTmx(SegmentTmTu p_tu, Tmx tmx,
			com.globalsight.everest.tm.exporter.ExportOptions options,
			OutputFormat outputFormat, boolean singleExport) throws Exception
	{
        StringBuffer result = new StringBuffer();
        // Add all TUVs.
        Collection locales = p_tu.getAllTuvLocales();
		FilterOptions filterString = options.getFilterOptions();
		Date createdAfter = parseStartDate(filterString.m_createdAfter);
		Date createdBefore = parseEndDate(filterString.m_createdBefore);
		Date modifyAfter = parseStartDate(filterString.m_modifiedAfter);
		Date modifyBefore = parseEndDate(filterString.m_modifiedBefore);
		Date lastUsageDateAfter = parseStartDate(filterString.m_lastUsageAfter);
		Date lastUsageDateBefore = parseEndDate(filterString.m_lastUsageBefore);
		String creationUser = filterString.m_createdBy;
		String modifyUser = filterString.m_modifiedBy;
		String sourceLang = p_tu.getSourceLocale().toString();
		sourceLang = handleSpecialLocaleCode(sourceLang);
		String filterLang = filterString.m_language;
        List<String> oldfilterLangList = Arrays.asList(filterLang.split(","));
        HashSet<String> filterLangList = new HashSet<String>();
		for (String selectLang : oldfilterLangList)
		{
			if (StringUtil.isNotEmpty(selectLang))
			{
				filterLangList.add(handleSpecialLocaleCode(selectLang));
			}
		}
        filterLangList.remove(sourceLang);

        boolean isRun = false;
        Tmx.Prop prop = null;

        StringBuffer tuAndSource = new StringBuffer();
		String tuResult = getTUStr(p_tu, tmx, options, prop);
        SegmentTmTuv sourcTuv = (SegmentTmTuv) p_tu.getSourceTuv();
		if (!isRun)
		{
			if (sourcTuv.getSid() != null)
			{
				prop = new Tmx.Prop(Tmx.PROP_TM_UDA_SID, sourcTuv.getSid());
				tuAndSource.append(prop.asXML());
				isRun = true;
			}
		}
		tuAndSource.append(convertToTmx(sourcTuv, sourceLang, options,
				outputFormat));

		// Only loop target locales
        locales.remove(p_tu.getSourceLocale());
        for (Iterator it = locales.iterator(); it.hasNext();)
        {
            GlobalSightLocale locale = (GlobalSightLocale) it.next();
			String localeCode = handleSpecialLocaleCode(locale.toString());
			if (filterLangList.size() > 0
					&& !filterLangList.contains(localeCode.toLowerCase()))
			{
				continue;
			}

			Collection tuvs = p_tu.getTuvList(locale);
            for (Iterator it2 = tuvs.iterator(); it2.hasNext();)
			{
				SegmentTmTuv tuv = (SegmentTmTuv) it2.next();

				try
				{
					Date creationDate = format.parse(format.format(tuv
							.getCreationDate()));
					if (!filterByDate(creationDate, createdAfter, createdBefore))
					{
						continue;
					}

					Date modifyDate = tuv.getModifyDate();
					if (modifyDate != null)
					{
						modifyDate = format.parse(format.format(modifyDate));
						if (!filterByDate(modifyDate, modifyAfter, modifyBefore))
						{
							continue;
						}
					}

					Date lastUsageDate = tuv.getLastUsageDate();
					if (lastUsageDate != null)
					{
						lastUsageDate = format.parse(format.format(lastUsageDate));
						if (!filterByDate(lastUsageDate, lastUsageDateAfter,
								lastUsageDateBefore))
						{
							continue;
						}
					}

					if (StringUtil.isNotEmpty(creationUser)
							&& !creationUser.equalsIgnoreCase(tuv.getCreationUser()))
					{
						continue;
					}

					if (StringUtil.isNotEmpty(modifyUser) 
							&& !modifyUser.equalsIgnoreCase(tuv.getModifyUser()))
					{
						continue;
					}
				}
				catch (ParseException e)
				{
					CATEGORY.error(e);
				}

				result.append(tuResult);
				if (!isRun)
				{
					if (tuv.getSid() != null)
					{
						prop = new Tmx.Prop(Tmx.PROP_TM_UDA_SID, tuv.getSid());
						result.append(prop.asXML());
						isRun = true;
					}
				}
				result.append(tuAndSource.toString());
				result.append(convertToTmx(tuv, sourceLang, options, outputFormat));
				result.append("</tu>\r\n");
			}
        }

        return result.toString();
    }

	private boolean filterByDate(Date date, Date startDate, Date endDate)
	{
		if (startDate != null)
		{
			if (!date.after(startDate))
			{
				return false;
			}
		}

		if (endDate != null)
		{
			if (!date.before(endDate))
			{
				return false;
			}
		}

		return true;
	}

	private static String getTUStr(SegmentTmTu p_tu, Tmx tmx,
			com.globalsight.everest.tm.exporter.ExportOptions options,
			Tmx.Prop prop)
	{
		GlobalSightLocale srcLocale = p_tu.getSourceLocale();
		String srcLang = ExportUtil.getLocaleString(srcLocale);
		StringBuffer result = new StringBuffer();
		result.append("<tu");

		// Remember valid TU IDs
		if (p_tu.getId() > 0)
		{
			result.append(" ");
			result.append(Tmx.TUID);
			result.append("=\"");
			result.append(p_tu.getId());
			result.append("\"");
		}

		// Default datatype is HTML, mark different TUs.
		if (!p_tu.getFormat().equals(tmx.getDatatype()))
		{
			result.append(" ");
			result.append(Tmx.DATATYPE);
			result.append("=\"");
			result.append(p_tu.getFormat());
			result.append("\"");
		}

		// Default srclang is en_US, mark different TUs.
		if (!srcLang.equalsIgnoreCase(tmx.getSourceLang()))
		{
			result.append(" ");
			result.append(Tmx.SRCLANG);
			result.append("=\"");
			result.append(srcLang);
			result.append("\"");
		}

		result.append(">\r\n");

		// Property for TU type (text, string), default "text"
		if (!p_tu.getType().equals("text"))
		{
			prop = new Tmx.Prop(Tmx.PROP_SEGMENTTYPE, p_tu.getType());
			result.append(prop.asXML());
		}

		// Property for TU type (T, L), default "T"
		if (!p_tu.isTranslatable())
		{
			prop = new Tmx.Prop(Tmx.PROP_TUTYPE, Tmx.VAL_TU_LOCALIZABLE);
			result.append(prop.asXML());
		}

		// Property for TU's source TM name.
		String temp = p_tu.getSourceTmName();
		if (temp != null && temp.length() > 0)
		{
			prop = new Tmx.Prop(Tmx.PROP_SOURCE_TM_NAME, temp);
			result.append(prop.asXML());
		}

		// add tu attributes
		List<ProjectTmTuTProp> props = ProjectTmTuTProp
				.getTuProps(p_tu.getId());
		if (props != null)
		{
			for (ProjectTmTuTProp pp : props)
			{
				result.append(pp.convertToTmx());
			}
		}

		// add TU attributes from TM3 convert
		if (props == null || props.size() == 0)
		{
			Collection<ProjectTmTuTProp> tuProps = p_tu.getProps();
			if (tuProps != null)
			{
				for (ProjectTmTuTProp pp : tuProps)
				{
					result.append(pp.convertToTmx());
				}
			}
		}
		return result.toString();
	}

    public static String convertToTmx(SegmentTmTuv p_tuv, String p_srcLang,
            com.globalsight.everest.tm.exporter.ExportOptions options,
            OutputFormat outputFormat) throws Exception
    {
        StringBuffer result = new StringBuffer();
        String temp;
        Tmx.Prop prop;

        result.append("<tuv xml:lang=\"");
        result.append(ExportUtil.getLocaleString(p_tuv.getLocale()));
        result.append("\" ");

        if (p_tuv.getCreationDate() != null)
        {
            result.append(Tmx.CREATIONDATE);
            result.append("=\"");
            result.append(UTC.valueOfNoSeparators(p_tuv.getCreationDate()));
            result.append("\" ");
        }

        temp = p_tuv.getCreationUser();
        if (temp != null && temp.length() > 0)
        {
            try
            {
                boolean changeCreationId = options.getSelectOptions().m_selectChangeCreationId;
                String localeCode = p_tuv.getLocale().toString();
                if (localeCode.equalsIgnoreCase("iw_IL"))
                {
                    localeCode = "he_IL";
                }
                if (p_srcLang != null && !p_srcLang.equals(localeCode)
                        && changeCreationId)
                {
                    String[] supportedMTEngines = MachineTranslator.gsSupportedMTEngines;
                    for (int i = 0; i < supportedMTEngines.length; i++)
                    {
                        if (temp.toLowerCase().indexOf(
                                supportedMTEngines[i].toLowerCase()) > -1)
                        {
                            temp = "MT!";
                            break;
                        }
                    }
                }
            }
            catch (Exception ex)
            {

            }

            result.append(Tmx.CREATIONID);
            result.append("=\"");
            result.append(EditUtil.encodeXmlEntities(UserUtil
                    .getUserNameById(temp)));
            result.append("\" ");
        }

        if (p_tuv.getModifyDate() != null)
        {
            if (p_tuv.getCreationDate() != null
                    && !p_tuv.getCreationDate().equals(p_tuv.getModifyDate()))
            {
                result.append(Tmx.CHANGEDATE);
                result.append("=\"");
                result.append(UTC.valueOfNoSeparators(p_tuv.getModifyDate()));
                result.append("\" ");
            }

        }

        temp = p_tuv.getModifyUser();
        if (temp != null && temp.length() > 0)
        {
            result.append(Tmx.CHANGEID);
            result.append("=\"");
            result.append(EditUtil.encodeXmlEntities(UserUtil
                    .getUserNameById(temp)));
            result.append("\" ");
        }

        if (p_tuv.getLastUsageDate() != null)
        {
            result.append(Tmx.LASTUSAGEDATE);
            result.append("=\"");
            result.append(UTC.valueOfNoSeparators(p_tuv.getLastUsageDate()));
            result.append("\" ");
        }
        result.append(">\r\n");

        // Property for TUV's update project.
        temp = p_tuv.getUpdatedProject();
        if (temp != null && temp.length() > 0)
        {
            prop = new Tmx.Prop(Tmx.PROP_CREATION_PROJECT, temp);
            result.append(prop.asXML());
        }

        // previous hash value
        long hash = p_tuv.getPreviousHash();
        if (hash != -1)
        {
        	prop = new Tmx.Prop(Tmx.PROP_PREVIOUS_HASH, String.valueOf(hash));
            result.append(prop.asXML());
        }

        // previous hash value
        hash = p_tuv.getNextHash();
        if (hash != -1)
        {
        	prop = new Tmx.Prop(Tmx.PROP_NEXT_HASH, String.valueOf(hash));
            result.append(prop.asXML());
        }

        long jobId = p_tuv.getJobId();
        if (jobId > 0)
        {
        	prop = new Tmx.Prop(Tmx.PROP_JOB_ID, String.valueOf(jobId));
            result.append(prop.asXML());
        }

        String jobName = p_tuv.getJobName();
        if (jobName != null && jobName.length() > 0)
        {
        	prop = new Tmx.Prop(Tmx.PROP_JOB_NAME, String.valueOf(jobName));
            result.append(prop.asXML());
        }

        // TODO: preserve the sub ids and locType in <prop>.
        result.append(convertToTmx(p_tuv.getSegment(), outputFormat));
        result.append("</tuv>\r\n");

        return result.toString();
    }

    /**
     * Convert a segment string to TMX by removing <sub> elements.
     * 
     * TODO: output sub information as <prop>.
     */
    private static String convertToTmx(String p_segment,
            OutputFormat outputFormat)
    {
        StringBuffer result = new StringBuffer();

        Document dom = getDom(p_segment);

        result.append("<seg>");
        result.append(getInnerXml(dom.getRootElement(), outputFormat));
        result.append("</seg>\r\n");

        return result.toString();
    }

    /**
     * Returns the XML representation like Element.asXML() but without the
     * top-level tag.
     */
    private static String getInnerXml(Element p_node, OutputFormat outputFormat)
    {
        StringBuffer result = new StringBuffer();

        List content = p_node.content();

        for (int i = 0, max = content.size(); i < max; i++)
        {
            Node node = (Node) content.get(i);

            // Work around a specific behaviour of DOM4J text nodes:
            // The text node asXML() returns the plain Unicode string,
            // so we need to encode entities manually.
            if (node.getNodeType() == Node.TEXT_NODE)
            {
                result.append(EditUtil.encodeXmlEntities(node.getText()));
            }
            else
            {
                // Note: DOM4J's node.asXML() constructs the same 2 objects.
                StringWriter out = new StringWriter();
                XMLWriter writer = new XMLWriter(out, outputFormat);

                try
                {
                    writer.write(node);
                }
                catch (IOException ignore)
                {
                }

                result.append(out.toString());
            }
        }

        return result.toString();
    }

    /**
     * Removes all <sub> elements from the segment. <sub> is special since it
     * does not only surround embedded tags but also text, which must be pulled
     * out of the <sub> and added to the parent tag.
     */
    private static Element removeSubElements(Element p_seg)
    {
        ArrayList elems = new ArrayList();

        findSubElements(elems, p_seg);

        for (int i = 0; i < elems.size(); i++)
        {
            Element elem = (Element) elems.get(i);

            removeSubElement(elem);
        }

        return p_seg;
    }

    /**
     * Removes the given <sub> element from the segment. <sub> is special since
     * it does not only surround embedded tags but also text, which must be
     * pulled out of the <sub> and added to the parent tag.
     */
    private static void removeSubElement(Element p_element)
    {
        Element parent = p_element.getParent();
        int index = parent.indexOf(p_element);

        // We copy the current content, clear out the parent, and then
        // re-add the old content, inserting the <sub>'s textual
        // content instead of the <sub> (this clears any embedded TMX
        // tags in the subflow).

        ArrayList newContent = new ArrayList();
        List content = parent.content();

        for (int i = content.size() - 1; i >= 0; --i)
        {
            Node node = (Node) content.get(i);

            newContent.add(node.detach());
        }

        Collections.reverse(newContent);
        parent.clearContent();

        for (int i = 0, max = newContent.size(); i < max; ++i)
        {
            Node node = (Node) newContent.get(i);

            if (i == index)
            {
                parent.addText(p_element.getText());
            }
            else
            {
                parent.add(node);
            }
        }
    }

    private static void findSubElements(ArrayList p_result, Element p_element)
    {
        // Depth-first traversal: add embedded <sub> to the list first.
        for (int i = 0, max = p_element.nodeCount(); i < max; i++)
        {
            Node child = (Node) p_element.node(i);

            if (child instanceof Element)
            {
                findSubElements(p_result, (Element) child);
            }
        }

        if (p_element.getName().equals("sub"))
        {
            p_result.add(p_element);
        }
    }

    /**
     * Removes all <sub> elements from the segment. <sub> is special since it
     * does not only surround embedded tags but also text, which must be pulled
     * out of the <sub> and added to the parent tag.
     */
    private static Element replaceNbsps(Element p_seg)
    {
        ArrayList elems = new ArrayList();

        findNbspElements(elems, p_seg);

        for (int i = 0; i < elems.size(); i++)
        {
            Element elem = (Element) elems.get(i);

            replaceNbsp(elem);
        }

        return p_seg;
    }

    /**
     * Removes the given <sub> element from the segment. <sub> is special since
     * it does not only surround embedded tags but also text, which must be
     * pulled out of the <sub> and added to the parent tag.
     */
    private static void replaceNbsp(Element p_element)
    {
        Element parent = p_element.getParent();
        int index = parent.indexOf(p_element);

        // We copy the current content, clear out the parent, and then
        // re-add the old content, inserting the <sub>'s textual
        // content instead of the <sub> (this clears any embedded TMX
        // tags in the subflow).

        ArrayList newContent = new ArrayList();
        List content = parent.content();

        for (int i = content.size() - 1; i >= 0; --i)
        {
            Node node = (Node) content.get(i);

            newContent.add(node.detach());
        }

        Collections.reverse(newContent);
        parent.clearContent();

        for (int i = 0, max = newContent.size(); i < max; ++i)
        {
            Node node = (Node) newContent.get(i);

            if (i == index)
            {
                parent.addText("\u00A0");
            }
            else
            {
                parent.add(node);
            }
        }
    }

    private static void findNbspElements(ArrayList p_result, Element p_element)
    {
        // Depth-first traversal: add embedded <ph x-nbspace> to the list first.
        for (int i = 0, max = p_element.nodeCount(); i < max; i++)
        {
            Node child = (Node) p_element.node(i);

            if (child instanceof Element)
            {
                findNbspElements(p_result, (Element) child);
            }
        }

        if (p_element.getName().equals("ph"))
        {
            String attr = p_element.attributeValue("type");

            if (attr != null && attr.equals("x-nbspace"))
            {
                p_result.add(p_element);
            }
        }
    }

    /**
     * Finds elements bearing an "x" element. These are bpt (required) and it,
     * ph, hi (optional).
     */
    private void findElementsWithX(ArrayList p_result, Element p_element)
    {
        // Prefix-traversal
        if (p_element.attributeValue("x") != null)
        {
            p_result.add(p_element);
        }

        for (int i = 0, max = p_element.nodeCount(); i < max; i++)
        {
            Node child = (Node) p_element.node(i);

            if (child instanceof Element)
            {
                findElementsWithX(p_result, (Element) child);
            }
        }
    }

    /**
     * Injects HTML codes into empty TMX tags from TM2. <bpt type=bold />
     * becomes <bpt type=bold>&lt;B&gt;</bpt>
     */
    private static void injectStandardFormattingCodes(Element p_root)
    {
        injectStandardFormattingCodes(p_root, p_root);
    }

    private static void injectStandardFormattingCodes(Element p_root, Element p_element)
    {
        // Depth-first traversal: add embedded <sub> to the list first.
        for (int i = 0, max = p_element.nodeCount(); i < max; i++)
        {
            Node child = (Node) p_element.node(i);

            if (child instanceof Element)
            {
                injectStandardFormattingCodes(p_root, (Element) child);
            }
        }

        String tagName = p_element.getName();
        String typeAttr = p_element.attributeValue("type");
        String iAttr = p_element.attributeValue("i");
        String posAttr = p_element.attributeValue("pos");

        if (tagName.equals("bpt") && typeAttr != null && iAttr != null)
        {
            Element ept = (Element) p_root.selectSingleNode("//ept[@i='"
                    + iAttr + "']");

            if (typeAttr.equals("bold"))
            {
                p_element.addText("<B>");
                ept.addText("</B>");
            }
            else if (typeAttr.equals("italic"))
            {
                p_element.addText("<I>");
                ept.addText("</I>");
            }
            else if (typeAttr.equals("ulined"))
            {
                p_element.addText("<U>");
                ept.addText("</U>");
            }
        }
        else if (tagName.equals("it") && typeAttr != null && posAttr != null)
        {
            if (typeAttr.equals("bold"))
            {
                if (posAttr.equals("begin"))
                {
                    p_element.addText("<B>");
                }
                else
                {
                    p_element.addText("</B>");
                }
            }
            else if (typeAttr.equals("italic"))
            {
                if (posAttr.equals("begin"))
                {
                    p_element.addText("<I>");
                }
                else
                {
                    p_element.addText("</I>");
                }
            }
            else if (typeAttr.equals("ulined"))
            {
                if (posAttr.equals("begin"))
                {
                    p_element.addText("<U>");
                }
                else
                {
                    p_element.addText("</U>");
                }
            }
        }
    }

    /**
     * Fixes the value of the "i" attribute across TUVs. In TM2, "i" is unique
     * across all TUVs, for TMX compliance thes "i" linked by "x" must be the
     * same. Furthermore, "x" numbering must start at 1.
     */
	private void fixAttributeIX(SegmentTmTuv p_sourceTuv, List<BaseTmTuv> p_tuvs)
    {
        ArrayList<Element> roots = new ArrayList<Element>();
        for (int i = 0; i < p_tuvs.size(); i++)
        {
        	SegmentTmTuv tuv = (SegmentTmTuv) p_tuvs.get(i);
            Document dom = getDom(tuv.getSegment());
            roots.add(dom.getRootElement());
        }

        Element sroot = getDom(p_sourceTuv.getSegment()).getRootElement();

        fixAttributeIX(sroot, roots);

        // Save the modified segments back into the tuvs.
        p_sourceTuv.setSegment(sroot.asXML());

        for (int i = 0; i < p_tuvs.size(); i++)
        {
            SegmentTmTuv tuv = (SegmentTmTuv) p_tuvs.get(i);
            Element root = (Element) roots.get(i);
            tuv.setSegment(root.asXML());
        }
    }

    /**
     * Finds all "x" and "i" attributes in the source TUV that need to be fixed
     * in the other TUVs.
     */
    private void fixAttributeIX(Element p_root, ArrayList p_roots)
    {
        // First use the same "i" across source and target tuvs.
        List bpts = p_root.selectNodes("//bpt");

        for (int i = 0, max = bpts.size(); i < max; i++)
        {
            Element bpt = (Element) bpts.get(i);

            String xAttr = bpt.attributeValue("x");
            String iAttr = bpt.attributeValue("i");

            // Be prepared for data errors where "x" is missing.
            // Don't crash here because of it. Fix it elsewhere.
            if (xAttr != null && iAttr != null)
            {
                fixAttributeI(xAttr, iAttr, p_roots);
            }
        }

        // Then renumber all "x" starting at one. Gaaah.
        ArrayList elems = new ArrayList();
        findElementsWithX(elems, p_root);

        for (int num = 1, i = 0, max = elems.size(); i < max; i++, num++)
        {
            Element elem = (Element) elems.get(i);

            String name = elem.getName();
            String oldX = elem.attributeValue("x");
            String newX = String.valueOf(num);

            // Renumber in this TUV.
            elem.addAttribute("x", newX);

            // Renumber in all others TUV.
            fixAttributeX(name, oldX, newX, p_roots);
        }
    }

    /**
     * Fixes a single "i" attribute in all other TUVs based on the "x".
     */
    private void fixAttributeI(String p_x, String p_i, ArrayList p_roots)
    {
        for (int i = 0, max = p_roots.size(); i < max; i++)
        {
            Element root = (Element) p_roots.get(i);

            Element bpt = (Element) root.selectSingleNode("//bpt[@x='" + p_x
                    + "']");

            if (bpt == null)
            {
                continue;
            }

            String curI = bpt.attributeValue("i");
            Element ept = (Element) root.selectSingleNode("//ept[@i='" + curI
                    + "']");

            bpt.addAttribute("i", p_i);
            if (ept != null)
            {
                ept.addAttribute("i", p_i);
            }
        }
    }

    /**
     * Updates a single "x" attribute in all other TUVs with a new value.
     */
    private void fixAttributeX(String p_name, String p_oldX, String p_newX,
            ArrayList p_roots)
    {
        for (int i = 0, max = p_roots.size(); i < max; i++)
        {
            Element root = (Element) p_roots.get(i);

            Element elem = (Element) root.selectSingleNode("//" + p_name
                    + "[@x='" + p_oldX + "']");

            if (elem == null)
            {
                continue;
            }

            elem.addAttribute("x", p_newX);
        }
    }
    private static Date parseDate(String s) throws LingManagerException
    {
        return StringUtil.isEmpty(s) ? null : new Date(s);
    }
    
    private Date parseStartDate(String startStr)
	{
		Date start = parseDate(startStr);
		if (start != null)
		{
			String startDate = dateFormat.format(start) + " 00:00:00";
			try
			{
				if (StringUtil.isNotEmpty(startDate))
				{
					return format.parse(startDate);
				}
				else return null;
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			return null;
		}
		return null;
	}

    private Date parseEndDate(String endStr)
	{
		Date end = parseDate(endStr);
		if (end != null)
		{
			String endDate = dateFormat.format(end) + " 23:59:59";
			try
			{
				if (StringUtil.isNotEmpty(endDate))
				{
					return format.parse(endDate);
				}
				else return null;
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			return null;
		}

		return null;
	}
    
    /**
     * Creates a Tmx (header) structure holding all our header info.
     */
    private Tmx createTmxHeader(SessionInfo p_session)
    {
        Tmx result = new Tmx();

        // Mandatory attributes.
        if (m_tmxLevel == TMX_LEVEL_NATIVE)
        {
            result.setTmxVersion(Tmx.TMX_GS);
        }
        else
        {
            result.setTmxVersion(Tmx.TMX_14);
        }
        result.setCreationTool(Tmx.GLOBALSIGHT);
        result.setCreationToolVersion(Tmx.GLOBALSIGHTVERSION);
        result.setSegmentationType(Tmx.SEGMENTATION_SENTENCE);
        result.setOriginalFormat(Tmx.TMF_GXML);
        result.setAdminLang(Tmx.DEFAULT_ADMINLANG);

        // TODO: get source language from TM or ExportOptions
        // This is a default, individual TUs can overwrite this.
        result.setSourceLang(Tmx.DEFAULT_SOURCELANG);

        result.setDatatype(Tmx.DATATYPE_HTML);

        // Optional attributes.

        // original encoding: unknown.
        result.setCreationDate(m_database.getCreationDate());
        result.setCreationId(m_database.getCreationUser());

        // TODO: Don't have information about last modification.
        result.setChangeDate(new Date());
        result.setChangeId(p_session.getUserName()
        /* TODO: m_database.getModificationUser() */);

        // result.addNote("CvdL did this.");

        return result;
    }

    private int getTmxLevel(ExportOptions p_options)
    {
        com.globalsight.everest.tm.exporter.ExportOptions options = (com.globalsight.everest.tm.exporter.ExportOptions) p_options;

        String type = options.getFileType();

        if (type.equalsIgnoreCase(options.TYPE_XML))
        {
            return TMX_LEVEL_NATIVE;
        }
        else if (type.equalsIgnoreCase(options.TYPE_TMX1))
        {
            return TMX_LEVEL_1;
        }
        else if (type.equalsIgnoreCase(options.TYPE_TMX2))
        {
            return TMX_LEVEL_2;
        }
        else if (type.equalsIgnoreCase(options.TYPE_TTMX))
        {
            return TMX_LEVEL_TRADOS;
        }

        return TMX_LEVEL_2;
    }

    private void checkIOError() throws IOException
    {
        // The JDK is so incredibly inconsistent (aka, stupid).
        // PrintWriter.println() does not throw exceptions.
        if (m_output.checkError())
        {
            throw new IOException("write error");
        }
    }

	private static String handleSpecialLocaleCode(String localeCode)
	{
		if ("in_ID".equalsIgnoreCase(localeCode))
		{
			localeCode = "id_ID";
		}
		else if ("iw_IL".equalsIgnoreCase(localeCode))
		{
			localeCode = "he_IL";
		}
		return localeCode.toLowerCase();
	}
}
