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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.everest.page.PageTemplate;
import com.globalsight.everest.page.TemplatePart;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.ling.common.RegEx;
import com.globalsight.ling.common.RegExException;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlNames;
import com.globalsight.util.gxml.GxmlRootElement;
import com.globalsight.util.gxml.PrsRootElement;

/**
 * <p>Generates different types of PageTemplates for different data
 * sources (GXML or PRS data source).</p>
 *
 * <p>Algorithms for generating these templates are seen in design
 * document for page import process.</p>
 *
 * @author Bethany Wang
 */
public class TemplateGenerator
    implements TemplateGeneratorConstants
{
    private static final Logger c_logger =
        Logger.getLogger(
            TemplateGenerator.class.getName());

    /**
     * <p>A DataSkeletonPair defined as a row of HTML code in the
     * Template data section.</p>
     *
     * <p>It consists of 2 parts: startSkeleton and endSkeleton.</p>
     */
    class DataSkeletonPair
    {
        private String startSkeleton;
        private String endSkeleton;

        DataSkeletonPair(String start, String end)
        {
            startSkeleton = start;
            endSkeleton = end;
        }

        DataSkeletonPair(StringBuffer start, StringBuffer end)
        {
            startSkeleton = start.toString();
            endSkeleton = end.toString();
        }

        String getStartSkeleton()
        {
            return startSkeleton;
        }

        String getEndSkeleton()
        {
            return endSkeleton;
        }
    }


    public TemplateGenerator()
    {
    }

    ////////////////////////////////////////////////////////////////
    //  public class API
    ////////////////////////////////////////////////////////////////

    /**
     * <p>Generates a detail PageTemplate for GXML data source.</p>
     *
     * @param p_gxmlRoot a GxmlRootElement used to generate the
     * template on
     * @param p_Tus a list of TUs generated based on the above
     * GxmlRootElement
     *
     * @return a PageTemplate of detail type
     */
    public PageTemplate generateDetail(GxmlRootElement p_gxmlRoot, List<Tu> p_Tus)
        throws FileImportException
    {
        if (c_logger.isDebugEnabled())
        {
            c_logger.debug("TemplateGenerator.generateDetail(Gxml)");
        }

        ArrayList<TemplatePart> templateParts = new ArrayList<TemplatePart>(
                p_Tus.size() + 1);
        int tuPointer = 0;

        String rest = getTemplateFormat(NAME_TMPL_START,
            TMPL_TYPE_DETAIL, SRC_TYPE_GXML);

        PageTemplate pageTemplate = new PageTemplate(PageTemplate.TYPE_DETAIL);
        SubProcessResult processResult =
            processOnGxmlRootForDetailAndPreview(p_gxmlRoot, p_Tus,
                tuPointer, pageTemplate, templateParts, rest, true);

        rest = processResult.getRest();
        templateParts = processResult.getTemplateParts();
        tuPointer = processResult.getTuPointer();

        rest += getTemplateFormat(NAME_TMPL_END, TMPL_TYPE_DETAIL,
            SRC_TYPE_GXML);

        TemplatePart tPart = new TemplatePart(pageTemplate, rest, null,
            tuPointer);
        templateParts.add(tPart);

        pageTemplate.setTemplatePartsForPersistence(templateParts);
        return pageTemplate;
    }

    /**
     * <p>Generates a standard PageTemplate for GXML data source.</p>
     *
     * @param p_gxmlRoot the GxmlRootElement for which to generate the
     * template
     * @param p_Tus a list of TUs generated based on the above
     * GxmlRootElement
     *
     * @return a PageTemplate of standard type
     */
    public PageTemplate generateStandard(GxmlRootElement p_gxmlRootElement,
        List<Tu> p_Tus)
        throws FileImportException
    {
        if (c_logger.isDebugEnabled())
        {
            c_logger.debug("TemplateGenerator.generateStandard(Gxml)");
        }

        ArrayList<TemplatePart> templateParts = new ArrayList<TemplatePart>(
                p_Tus.size() + 1);

        TemplatePart tPart = null;
        int size = p_Tus.size();
        int tuPointer = 0;

        String rest = getTemplateFormat(NAME_TMPL_START,
            TMPL_TYPE_STANDARD, SRC_TYPE_GXML) + "\n";

        PageTemplate pageTemplate =
            new PageTemplate(PageTemplate.TYPE_STANDARD);

        rest = processStandardMode(pageTemplate, p_gxmlRootElement,
            rest, templateParts, p_Tus, tuPointer);

        // Create the last part (currently the empty string)
        rest += getTemplateFormat(NAME_TMPL_END, TMPL_TYPE_STANDARD,
            SRC_TYPE_GXML);

        tPart = new TemplatePart(pageTemplate, rest, null, size);
        templateParts.add(tPart);

        pageTemplate.setTemplatePartsForPersistence(templateParts);

        return pageTemplate;
    }


    private String processStandardMode(PageTemplate p_pageTemplate,
            GxmlRootElement p_gxmlRootElement, String p_rest,
            ArrayList<TemplatePart> p_tmplParts, List<Tu> p_Tus, int p_tuPointer)
    {
        SubProcessResult spr =
            new SubProcessResult(p_tuPointer, p_tmplParts, p_rest);

        spr = processStandardMode_1(p_pageTemplate, spr,
            p_gxmlRootElement, p_Tus);

        return spr.getRest();
    }

    @SuppressWarnings({ "rawtypes"})
    private SubProcessResult processStandardMode_1(PageTemplate p_pageTemplate,
            SubProcessResult p_spr, GxmlElement p_gxmlElement, List<Tu> p_Tus)
    {
        if (p_gxmlElement == null)
        {
            return p_spr;
        }

        List childElements = p_gxmlElement.getChildElements();

        if (childElements == null || childElements.size() == 0)
        {
            return p_spr;
        }

        String rest = p_spr.getRest();
        int tuPointer = p_spr.getTuPointer();
        ArrayList<TemplatePart> tmplParts = p_spr.getTemplateParts();
        DataSkeletonPair dataRow;
        TemplatePart tPart = null; 
        Tu tu = null;

        for (int i = 0; i < childElements.size(); i++)
        {
            GxmlElement child = (GxmlElement)childElements.get(i);

            switch (child.getType())
            {
            case GxmlElement.GS:
                rest += child.getStartTag();

                SubProcessResult spr =
                    new SubProcessResult(tuPointer, tmplParts, rest);

                spr = processStandardMode_1(p_pageTemplate, spr, child, p_Tus);

                rest = spr.getRest();
                tuPointer = spr.getTuPointer();
                tmplParts = spr.getTemplateParts();

                rest += child.getEndTag();
                break;

            case GxmlElement.SKELETON:
                break;

            case GxmlElement.LOCALIZABLE:
                tu = (Tu)p_Tus.get(tuPointer);
                dataRow = generateDataSkeletonPairForGxmlStdTmpl(tuPointer);
                tPart = new TemplatePart(p_pageTemplate,
                    rest + dataRow.getStartSkeleton(), tu, tuPointer);

                tmplParts.add(tPart);

                tuPointer++;
                rest = dataRow.getEndSkeleton();
                break;

            case GxmlElement.TRANSLATABLE:
                String xliffpart = child.getAttribute("xliffPart");
                
                if(xliffpart == null || 
                        (xliffpart != null && (xliffpart.equals("target")))) {
                    List segments = child.getChildElements(GxmlElement.SEGMENT);
    
                    for (int k = 0; k < segments.size(); k++)
                    {
//                        GxmlElement segment = (GxmlElement)segments.get(k);
                        tu = (Tu)p_Tus.get(tuPointer);
                        dataRow = generateDataSkeletonPairForGxmlStdTmpl(tuPointer);
                        tPart = new TemplatePart(p_pageTemplate,
                            rest + dataRow.getStartSkeleton(), tu, tuPointer);
    
                        tmplParts.add(tPart);
    
                        rest = dataRow.getEndSkeleton();
                        tuPointer++;
                    }
                }
                
                break;

            default:
                break;
            }
        }

        p_spr.setRest(rest);
        p_spr.setTemplateParts(tmplParts);
        p_spr.setTuPointer(tuPointer);

        return p_spr;
    }


    /**
     * <p>Generates a preview PageTemplate for HTML data source.</p>
     *
     * @param p_gxmlRoot the GxmlRootElement for which to generate the
     * template
     * @param p_Tus a list of TU ids generated based on the above
     * GxmlRootElement
     *
     * @return a PageTemplate of type PREVIEW
     */
    public PageTemplate generatePreview(GxmlRootElement p_gxmlRoot,
            List<Tu> p_Tus) throws FileImportException
    {
        if (c_logger.isDebugEnabled())
        {
            c_logger.debug("TemplateGenerator.generatePreview(Gxml)");
        }

        ArrayList<TemplatePart> templateParts = new ArrayList<TemplatePart>(
                p_Tus.size() + 1);
        int tuPointer = 0;
        String rest = "";

        PageTemplate pageTemplate =
            new PageTemplate(PageTemplate.TYPE_PREVIEW);

        // forward the process to sub-procedure
        SubProcessResult processResult =
            processOnGxmlRootForDetailAndPreview(p_gxmlRoot, p_Tus,
                tuPointer, pageTemplate,templateParts, rest, false);

        rest = processResult.getRest();
        templateParts = processResult.getTemplateParts();
        tuPointer = processResult.getTuPointer();

        TemplatePart tPart = new TemplatePart(pageTemplate, rest, null,
            tuPointer);
        templateParts.add(tPart);

        pageTemplate.setTemplatePartsForPersistence(templateParts);

        return pageTemplate;
    }

    /**
     * <p>Generates an export PageTemplate for GXML data source.</p>
     *
     * @param p_gxmlRoot a GxmlRootElement used to generate the
     * template on
     * @param p_Tus a list of TUs generated based on the above
     * GxmlRootElement
     *
     * @return  a PageTemplate of export type
     */
    public PageTemplate generateExport(GxmlRootElement p_gxmlRoot,
            List<Tu> p_Tus) throws FileImportException
    {
        if (c_logger.isDebugEnabled())
        {
            c_logger.debug("TemplateGenerator.generateExport(Gxml)");
        }

        ArrayList<TemplatePart> templateParts = new ArrayList<TemplatePart>(
                p_Tus.size() + 1);
        int tuPointer = 0;
        String rest = "";

        PageTemplate pageTemplate =
            new PageTemplate(PageTemplate.TYPE_EXPORT);

        // forward the process to sub-procedure
        SubProcessResult processResult = processOnGxmlRootForExport(p_gxmlRoot,
                p_Tus, tuPointer, pageTemplate, templateParts, rest);

        rest = processResult.getRest();
        templateParts = processResult.getTemplateParts();
        tuPointer = processResult.getTuPointer();

        // Create the last part
        TemplatePart tPart = new TemplatePart(pageTemplate, rest, null,
            tuPointer);
        templateParts.add(tPart);

        pageTemplate.setTemplatePartsForPersistence(templateParts);

        return pageTemplate;
    }

    /**
     * Generates a standard PageTemplate for PRS data source (list mode).
     *
     * @param p_prsRoot the PrsRootElement for which to generate the
     * template
     * @param p_Tus a list of TUs generated from the above
     * PrsRootElement
     *
     * @return a PageTemplate of standard type
     * @deprecated -- legacy codes
     */
    @SuppressWarnings("rawtypes")
    public PageTemplate generateStandard(PrsRootElement p_prsRoot,
            List<Tu> p_Tus) throws FileImportException
    {
        if (c_logger.isDebugEnabled())
        {
            c_logger.debug("TemplateGenerator.generateStandard(Prs)");
        }

        ArrayList<TemplatePart> templateParts = new ArrayList<TemplatePart>(
                p_Tus.size() + 1);
        int tuPointer = 0;
        Tu tu = null;
        TemplatePart tPart = null;
        String rest = "";
        StringBuffer tuIds = new StringBuffer(64);

        PageTemplate pageTemplate =
            new PageTemplate(PageTemplate.TYPE_STANDARD);

        // Process all records
        List records = p_prsRoot.getChildElements();
        for (int i = 0; i < records.size(); i++)
        {
            // Retrieve record_start format
            String tmp = getTemplateFormat(NAME_RECORD_START,
                TMPL_TYPE_STANDARD, SRC_TYPE_PRS);

            // update the record id in the template
            try
            {
                tmp = RegEx.substituteAll(tmp, "<<ID>>", String.valueOf(i));
            }
            catch (RegExException ex)
            {
                c_logger.error("PILOT ERROR IN REGEXP", ex);
            }

            rest += tmp;

            GxmlElement record = (GxmlElement)records.get(i);

            // Process the columns under a record, only care about the
            // 'Translatable' column.
            List columns = record.getChildElements(GxmlElement.COLUMN);
            for (int j = 0; j < columns.size(); j++)
            {
                GxmlElement column = (GxmlElement)columns.get(j);

                String contentMode = column.getAttribute(
                    GxmlNames.COLUMN_CONTENTMODE);

                if (!PRS_COLUMN_CONTENTMODE_TRANSLATABLE.equals(contentMode))
                {
                    // ignore non-translatable columns
                    continue;
                }

                rest += PRS_COLUMN_ROW_START;

                // Process the label of a column.
                List labels = column.getChildElements(GxmlElement.LABEL);
                if (labels != null)
                {
                    GxmlElement label = (GxmlElement)labels.get(0);
                    rest += PRS_LABEL_COL_START +
                        label.getTotalTextValue() + PRS_LABEL_COL_END;
                }

                // Process the content of a column based on the
                // assumption that there is one and only one content
                // for each column.
                GxmlElement content = (GxmlElement)column.getChildElements(
                    GxmlElement.CONTENT).get(0);

                rest += PRS_CONTENT_LIST_START;

                // Process the GxmlRootElement under context element,
                // based on the assumption that there should be one
                // and only one GxmlRootElement for each translatable
                // column element.
                GxmlElement gxmlRoot = content.getChildElement(0);
                List elmts = gxmlRoot.getChildElements();

                for (int k = 0; k < elmts.size(); k++)
                {
                    GxmlElement elmt = (GxmlElement)elmts.get(k);

                    if (elmt.getType() == GxmlElement.LOCALIZABLE)
                    {
                        // get Tu and its order
                        tu = (Tu)p_Tus.get(tuPointer);

                        // make a TemplatePart and add it to the
                        // TemplatePart List
                        tPart = new TemplatePart(pageTemplate, rest, tu,
                            tuPointer);
                        templateParts.add(tPart);

                        tuIds.append(' ');
                        tuIds.append(tu.getOrder());

                        tuPointer++;

                        // clean up rest variable (noop, see below)
                        rest = "";
                    }
                    else if (elmt.getType() == GxmlElement.TRANSLATABLE)
                    {
                        List segments = elmt.getChildElements();

                        for (int h = 0; h < segments.size(); h++)
                        {
                            // get Tu and its order
                            tu = (Tu)p_Tus.get(tuPointer);

                            // make a TemplatePart and add it to the
                            // TemplatePart List
                            tPart = new TemplatePart(pageTemplate,
                                rest, tu, tuPointer);
                            templateParts.add(tPart);

                            tuIds.append(' ');
                            tuIds.append(tu.getOrder());

                            tuPointer++;

                            // finish row and prepare for next
                            if (h < segments.size() - 1)
                            {
                                rest = PRS_CONTENT_LIST_MID;
                            }
                            else
                            {
                                rest = "";
                            }
                        }
                    }
                }

                rest = PRS_CONTENT_LIST_END;
                rest += PRS_COLUMN_ROW_END;
            }

            // Leave the list of TU ids in the template for
            // OnlineEditorManager
            rest += "<<TUS" + i + tuIds.toString() + ">>";
            tuIds.setLength(0);

            rest += getTemplateFormat(NAME_RECORD_END, TMPL_TYPE_STANDARD,
                SRC_TYPE_PRS);
        }

        // Create the last part
        tPart = new TemplatePart(pageTemplate, rest, null, tuPointer);
        templateParts.add(tPart);

        tuPointer++;

        pageTemplate.setTemplatePartsForPersistence(templateParts);

        return pageTemplate;
    }

    /**
     * To generate a detail PageTemplate for PRS data source (text view).
     *
     * Preview: there is a single Preview() function in the target
     * page (me_target.jsp). Each record calls Preview() with a
     * numbered variable tuvids<<ID>> which contains the list of tuv
     * ids of the record.
     *
     * To create the list of tuv ids we first add a list of TU ids per
     * record to the template: <<TUS1 1,2,3>>, <<TUS2 4,5,6>> etc.
     *
     * OnlineEditorManager then extracts these placeholders and the tu
     * ids, finds the corresponding TUV ids and replaces
     * <<TUVS<<ID>>>> with the tuv ids.
     *
     * @param p_prsRoot a PrsRootElement used to generate the template
     * @param p_Tus a list of TU generated based on the above
     * PrsRootElement
     *
     * @return  a PageTemplate of detail type
     * @deprecated -- legacy codes
     */
    @SuppressWarnings("rawtypes")
    public PageTemplate generateDetail(PrsRootElement p_prsRoot, List<Tu> p_Tus)
        throws FileImportException
    {
        if (c_logger.isDebugEnabled())
        {
            c_logger.debug("TemplateGenerator.generateDetail(Prs)");
        }

        ArrayList<TemplatePart> templateParts = new ArrayList<TemplatePart>(
                p_Tus.size() + 1);

        int tuPointer = 0;
        TemplatePart tPart = null;
        String rest = "";
        StringBuffer tuIds = new StringBuffer(64);

        PageTemplate pageTemplate = new PageTemplate(PageTemplate.TYPE_DETAIL);

        // Process all records
        List records = p_prsRoot.getChildElements();
        for (int i = 0; i < records.size(); i++)
        {
            int i_tuStart = tuPointer;
            int i_tuEnd;

            String tmp = getTemplateFormat(NAME_RECORD_START,
                TMPL_TYPE_DETAIL, SRC_TYPE_PRS);

            try
            {
                tmp = RegEx.substituteAll(tmp, "<<ID>>", String.valueOf(i));
            }
            catch (RegExException ex)
            {
                c_logger.error("PILOT ERROR IN REGEXP", ex);
            }

            rest += tmp;

            GxmlElement record = (GxmlElement)records.get(i);

            // Process the columns under a record, only cares about
            // the 'Translatable' column
            List columns = record.getChildElements(GxmlElement.COLUMN);
            for (int j = 0; j < columns.size(); j++)
            {
                GxmlElement column = (GxmlElement)columns.get(j);

                String contentMode =
                    column.getAttribute(GxmlNames.COLUMN_CONTENTMODE);

                if (PRS_COLUMN_CONTENTMODE_INVISIBLE.equals(contentMode))
                {
                    // ignore invisible columns
                    continue;
                }

                rest += PRS_COLUMN_ROW_START;

                // Process the label of a column
                List labels = column.getChildElements(GxmlElement.LABEL);
                if (labels != null)
                {
                    GxmlElement label = (GxmlElement)labels.get(0);
                    rest += PRS_LABEL_COL_START +
                        label.getTotalTextValue() + PRS_LABEL_COL_END;
                }

                // Process the content of a column based on the
                // assumption that there is one and only one content
                // for each column
                GxmlElement content = (GxmlElement)column.getChildElements(
                    GxmlElement.CONTENT).get(0);

                rest += PRS_CONTENT_COL_START;

                // Process the GxmlRootElement under context element,
                // based on the assumption that there should be one
                // and only one GxmlRootElement for each translatable
                // or contextual column element
                if (PRS_COLUMN_CONTENTMODE_CONTEXUAL.equals(contentMode))
                {
                    GxmlElement gxmlRoot = content.getChildElement(0);
                    List elmts = gxmlRoot.getChildElements();

                    for (int k = 0; k < elmts.size(); k++)
                    {
                        GxmlElement elmt = (GxmlElement)elmts.get(k);

                        if (elmt.getType() == GxmlElement.SKELETON ||
                            elmt.getType() == GxmlElement.LOCALIZABLE)
                        {
                            rest += elmt.getTotalTextValue();
                        }
                        else if (elmt.getType() == GxmlElement.TRANSLATABLE)
                        {
                            List segments = elmt.getChildElements();

                            for (int h = 0; h < segments.size(); h++)
                            {
                                GxmlElement sgmt = (GxmlElement)segments.get(h);
                                rest += sgmt.getTotalTextValue();
                            }
                        }
                    }
                }
                else if (PRS_COLUMN_CONTENTMODE_TRANSLATABLE.equals(
                    contentMode))
                {
                    GxmlRootElement gxmlRoot =
                        (GxmlRootElement)content.getChildElement(0);

                    // forward the process to sub-procedure
                    SubProcessResult res =
                        processOnGxmlRootForDetailAndPreview(gxmlRoot,
                            p_Tus, tuPointer, pageTemplate, templateParts,
                            rest, /*??*/false);

                    rest = res.getRest();
                    templateParts = res.getTemplateParts();
                    tuPointer = res.getTuPointer();
                }

                rest += PRS_CONTENT_COL_END;
                rest += PRS_COLUMN_ROW_END;
            }

            // Leave the list of TU ids in the template for
            // OnlineEditorManager

            i_tuEnd = tuPointer;
            tuIds.setLength(0);

            for (int i_pos = i_tuStart; i_pos < i_tuEnd; ++i_pos)
            {
                Tu tu = (Tu)p_Tus.get(i_pos);

                tuIds.append(' ');
                tuIds.append(tu.getOrder());
            }

            rest += "<<TUS" + i + tuIds.toString() + ">>";

            tuIds.setLength(0);

            rest += getTemplateFormat(NAME_RECORD_END, TMPL_TYPE_DETAIL,
                SRC_TYPE_PRS);
        }

        // Create the last part
        tPart = new TemplatePart(pageTemplate, rest, null, tuPointer);
        templateParts.add(tPart);

        pageTemplate.setTemplatePartsForPersistence(templateParts);

        return pageTemplate;
    }

    /**
     * Generates an export PageTemplate for PRS data source.
     *
     * @param p_prsRoot a PrsRootElement for which to generate the
     * template
     *
     * @param p_Tus a list of TUs generated based on the above
     * PrsRootElement
     *
     * @return  a PageTemplate of export type
     * @deprecated -- legacy codes
     */
    @SuppressWarnings("rawtypes")
    public PageTemplate generateExport(PrsRootElement p_prsRoot, List<Tu> p_Tus)
            throws FileImportException
    {
        if (c_logger.isDebugEnabled())
        {
            c_logger.debug("TemplateGenerator.generateExport(Prs)");
        }

        ArrayList<TemplatePart> templateParts = new ArrayList<TemplatePart>(
                p_Tus.size() + 1);
        int tuPointer = 0;

        // Populate root tag and attribute
        String rest = p_prsRoot.getStartTag();

        PageTemplate pageTemplate = new PageTemplate(PageTemplate.TYPE_EXPORT);

        // Process all records
        List records = p_prsRoot.getChildElements();
        for (int i = 0; i < records.size(); i++)
        {
            GxmlElement record = (GxmlElement)records.get(i);

            // process record start tag
            rest += record.getStartTag() + "\n";

            // Process the acqSqlParm elements, there should be at
            // most one acqSqlParm element
            List acqs = record.getChildElements(GxmlElement.ACQSQLPARM);
            if (acqs != null)
            {
                GxmlElement acq = (GxmlElement)acqs.get(0);
                rest += acq.toGxml();
            }

            // Process all columns under the record
            List columns = record.getChildElements(GxmlElement.COLUMN);
            for (int j=0; j<columns.size(); j++)
            {
                GxmlElement column = (GxmlElement)columns.get(j);

                String contentMode =
                    column.getAttribute(GxmlNames.COLUMN_CONTENTMODE);

                rest += "\n" + column.getStartTag() + "\n";

                // Process the label of a column
                List labels = column.getChildElements(GxmlElement.LABEL);
                if (labels != null)
                {
                    GxmlElement label = (GxmlElement)labels.get(0);
                    rest += label.toGxml() + "\n";
                }

                // Process the content of a column based on the
                // assumption that there is one and only one content
                // for each column
                GxmlElement content = (GxmlElement)column.getChildElements(
                    GxmlElement.CONTENT).get(0);
                rest += content.getStartTag();

                // Process the content depends on the column
                // contentMode, which could be 'translatable',
                // 'contextual' or 'invisible'
                if (PRS_COLUMN_CONTENTMODE_CONTEXUAL.equals(contentMode))
                {
                    // Based on assumption that the 'contextual'
                    // column has one and only one GxmlRoot Element,
                    // and it keeps the original content as it was
                    GxmlElement gxmlRoot = content.getChildElement(0);
                    rest += gxmlRoot.toGxml();
                }
                else if (PRS_COLUMN_CONTENTMODE_INVISIBLE.equals(contentMode))
                {
                    // Based on assumption that the 'invisible' column
                    // has only text data
                    rest += content.getTotalTextValue();
                }
                else if (PRS_COLUMN_CONTENTMODE_TRANSLATABLE.equals(contentMode))
                {
                    // process the GxmlRootElement under context
                    // element, based on the assumption that there
                    // should be one and only one GxmlRootElement for
                    // each translatable column element
                    GxmlRootElement gxmlRoot =
                        (GxmlRootElement)content.getChildElement(0);

                    SubProcessResult processResult = processOnGxmlRootForExport(
                            gxmlRoot, p_Tus, tuPointer, pageTemplate,
                            templateParts, rest);

                    rest = processResult.getRest();
                    templateParts = processResult.getTemplateParts();
                    tuPointer = processResult.getTuPointer();
                }

                // populate content end-tag
                rest += content.getEndTag();
                // populate column end-tag
                rest += "\n" + column.getEndTag();
            }

            // populate record end-tag
            rest += "\n" + record.getEndTag();
        }

        // Create the last part
        rest += "\n" + p_prsRoot.getEndTag();

        TemplatePart tPart = new TemplatePart(pageTemplate, rest, null,
            tuPointer);
        templateParts.add(tPart);

        pageTemplate.setTemplatePartsForPersistence(templateParts);

        return pageTemplate;
    }


    ////////////////////////////////////////////////////////////////
    //  Begin:  Local methods
    ////////////////////////////////////////////////////////////////

    /**
     * A sub-process on a GxmlRootElement for generating export
     * template.
     *
     * @param p_gxmlRoot the GxmlRootElement to process
     *
     * @param p_Tus the Tus for this process, for PRS process, it is
     * the Tus for the whole PRS structure
     *
     * @param p_tuPointer A position pointer to get a tu from the Tu
     * list.
     *
     * @p_pageTemplate a reference to the template of the process.
     *
     * @param p_tmplParts It is the templateParts list for target
     * template. Its content is being changed inside this method.
     *
     * @param p_rest It is a state variable for generating
     * the template parts.
     *
     * @return SubProcessResult
     */
    private SubProcessResult processOnGxmlRootForExport(
            GxmlRootElement p_gxmlRoot, List<Tu> p_Tus, int p_tuPointer,
            PageTemplate p_pageTemplate, ArrayList<TemplatePart> p_tmplParts,
            String p_rest)
    {
        // special case: need to output <diplomat>

        p_rest += p_gxmlRoot.getStartTag() + "\n";

        SubProcessResult spr =
            new SubProcessResult(p_tuPointer, p_tmplParts, p_rest);

        spr = processOnGxmlRootForExport_1(spr, p_gxmlRoot,
            p_pageTemplate, p_Tus);

        String rest = spr.getRest();
        spr.setRest(rest + p_gxmlRoot.getEndTag());

        return spr;
    }

    @SuppressWarnings("rawtypes")
    private SubProcessResult processOnGxmlRootForExport_1(
        SubProcessResult p_spr, GxmlElement p_gxmlElement,
        PageTemplate p_pageTemplate, List<Tu> p_Tus)
    {
        if (p_gxmlElement == null)
        {
            return p_spr;
        }

        List childElements = p_gxmlElement.getChildElements();

        if (childElements == null || childElements.size() == 0)
        {
            return p_spr;
        }

        StringBuffer theRest = new StringBuffer(p_spr.getRest());
        int tuPointer = p_spr.getTuPointer();
        ArrayList<TemplatePart> tmplParts = p_spr.getTemplateParts();
        TemplatePart tPart = null;
        Tu tu = null;

        for (int i = 0; i < childElements.size(); i++)
        {
            GxmlElement child = (GxmlElement)childElements.get(i);

            switch (child.getType())
            {
            case GxmlElement.GS:
                theRest.append(child.getStartTag()).append("\n");
                SubProcessResult spr =
                    new SubProcessResult(tuPointer, tmplParts, theRest.toString());

                spr = processOnGxmlRootForExport_1(spr, child,
                    p_pageTemplate, p_Tus);

                theRest.setLength(0);
                theRest.append(spr.getRest());
                tuPointer = spr.getTuPointer();
                tmplParts = spr.getTemplateParts();

                theRest.append(child.getEndTag()).append("\n");
                break;

            case GxmlElement.SKELETON:
                theRest.append(child.toGxml()).append("\n");
                break;

            case GxmlElement.LOCALIZABLE:
                tu = (Tu) p_Tus.get(tuPointer);

                theRest.append(child.getStartTag());
                tPart = new TemplatePart(p_pageTemplate, theRest.toString(), tu, tuPointer);
                tmplParts.add(tPart);
                theRest.setLength(0);
                theRest.append(child.getEndTag()).append("\n");
                tuPointer++;
                break;

            case GxmlElement.TRANSLATABLE:
                String xliffpart = child.getAttribute("xliffPart");
                if(xliffpart == null || "target".equals(xliffpart))
                {
                    theRest.append(child.getStartTag()).append("\n");
                    List segments = child.getChildElements(GxmlElement.SEGMENT);
                
                    for (int k = 0; k < segments.size(); k++)
                    {
                        GxmlElement segment = (GxmlElement)segments.get(k);
                        if (c_logger.isDebugEnabled())
                        {
                            c_logger.debug("Ignoring start tag" + segment.getStartTag());                            
                        }
                        tu = (Tu)p_Tus.get(tuPointer);
    
                        tPart = new TemplatePart(p_pageTemplate, theRest.toString(), tu, tuPointer);
                        if (c_logger.isDebugEnabled())
                        {
                            c_logger.debug("TPART='" + tPart.toString() + "'");                            
                        }
    
                        tmplParts.add(tPart);
                        if (c_logger.isDebugEnabled())
                        {
                            c_logger.debug("Ignoring end tag" + segment.getEndTag());                            
                        }
                        theRest.setLength(0);
                        theRest.append("\n");
                        tuPointer++;
                    }
                
                    theRest.append(child.getEndTag()).append("\n");
                }
                break;

            default:
                break;
            }
        }

        p_spr.setRest(theRest.toString());
        p_spr.setTemplateParts(tmplParts);
        p_spr.setTuPointer(tuPointer);

        return p_spr;
    }


    /**
     * <p>A sub-process on a GxmlRootElement for generating detail and
     * preview template.</p>
     *
     * @param p_gxmlRoot the GxmlRootElement to process
     * @param p_Tus the Tus for this process, for PRS process, it is
     * the Tus for the whole PRS structure
     * @param p_tuPointer A position pointer to get a tu from the Tu
     * list.
     * @param p_pageTemplate a reference to the template of the
     * process.
     * @param p_tmplParts is the templateParts list for target
     * template. Its content is being changed inside this method.
     * @param p_rest is a state variable for generating the template
     * parts.
     * @param p_encode flag to encode HTML entities in skeleton parts
     * or not.
     * @return SubProcessResult
     */
    private SubProcessResult processOnGxmlRootForDetailAndPreview(
            GxmlRootElement p_gxmlRoot, List<Tu> p_Tus, int p_tuPointer,
            PageTemplate p_pageTemplate, ArrayList<TemplatePart> p_tmplParts,
            String p_rest, boolean p_encode)
    {
        SubProcessResult spr =
            new SubProcessResult(p_tuPointer, p_tmplParts, p_rest);

        spr = processOnGxmlRootForDetailAndPreview_1(spr, p_gxmlRoot,
            p_Tus, p_pageTemplate, p_encode);

        return spr;
    }

    @SuppressWarnings("rawtypes")
    private SubProcessResult processOnGxmlRootForDetailAndPreview_1(
        SubProcessResult p_spr, GxmlElement p_gxmlElement, List<Tu> p_Tus,
        PageTemplate p_pageTemplate, boolean p_encode)
    {
        if (p_gxmlElement == null)
        {
            return p_spr;
        }

        List childElements = p_gxmlElement.getChildElements();

        if (childElements == null || childElements.size() == 0)
        {
            return p_spr;
        }

        String rest = p_spr.getRest();
        int tuPointer =  p_spr.getTuPointer();
        ArrayList<TemplatePart> tmplParts = p_spr.getTemplateParts();
        TemplatePart tPart = null;
        Tu tu = null;
        String str_text;

        for (int i = 0; i < childElements.size(); i++)
        {
            GxmlElement child = (GxmlElement)childElements.get(i);

            switch (child.getType())
            {
            case GxmlElement.GS:
                rest += child.getStartTag();

                SubProcessResult spr =
                    new SubProcessResult(tuPointer, tmplParts, rest);

                spr = processOnGxmlRootForDetailAndPreview_1(spr, child,
                    p_Tus, p_pageTemplate, p_encode);

                rest = spr.getRest();
                tmplParts = spr.getTemplateParts();
                tuPointer = spr.getTuPointer();

                rest += child.getEndTag();
                break;

            case GxmlElement.SKELETON:
                str_text = child.getTotalTextValue();

                if (p_encode)
                {
                    str_text = EditUtil.encodeHtmlEntities(str_text);
                }

                rest += str_text;
                break;

            case GxmlElement.LOCALIZABLE:
                tu = (Tu) p_Tus.get(tuPointer);
                tPart = new TemplatePart(p_pageTemplate, rest, tu, tuPointer);
                tmplParts.add(tPart);
                tuPointer++;
                rest = "";

                break;

            case GxmlElement.TRANSLATABLE:
                List segments = child.getChildElements();
                String xliffpart = child.getAttribute("xliffPart");
                
                if(xliffpart == null || 
                    (xliffpart != null && (xliffpart.equals("target")))) {
                    for (int k = 0; k < segments.size(); k++)
                    {
                        tu = (Tu)p_Tus.get(tuPointer);
                        tPart =
                            new TemplatePart(p_pageTemplate, rest, tu, tuPointer);
    
                        tmplParts.add(tPart);
    
                        tuPointer++;
                        rest = "";
                    }
                }
                break;

            default:
                break;
            }
        }

        p_spr.setRest(rest);
        p_spr.setTemplateParts(tmplParts);
        p_spr.setTuPointer(tuPointer);

        return p_spr;
    }

    /**
     * Generates a piece of the standard template for a Gxml data row.
     */
    private DataSkeletonPair generateDataSkeletonPairForGxmlStdTmpl(
        int p_tuPointer)
    {
        StringBuffer startPiece = new StringBuffer();
        StringBuffer endPiece = new StringBuffer();

        if (isEvenNumber(p_tuPointer))
        {
            startPiece.append(GXML_DATAROW_EVEN_START);
        }
        else
        {
            startPiece.append(GXML_DATAROW_ODD_START);
        }

        endPiece.append(GXML_DATAROW_END);

        return new DataSkeletonPair(startPiece, endPiece);
    }

    /**
     * <p>Returns <code>true</code> when the given integer number is
     * an even number, otherwise <code>false</code>.</p>
     */
    private boolean isEvenNumber(int p_num)
    {
        return(p_num % 2) == 0;
    }

    /**
     * <p>Retrieves the template format with the specified name,
     * template type and source type.</p>
     * @return a String representation of the template text.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    // ----------
    private String getTemplateFormat(String p_name, String p_tmplType,
        String p_srcType) throws FileImportException
    {
        TemplateFormat tmplFormat = null;

        try
        {
            Vector args = new Vector();
            args.add(p_name);
            args.add(p_tmplType);
            args.add(p_srcType);

            String hql = "from TemplateFormat t where t.name = :name " +
            		"and t.templateType = :tType and t.sourceType = :sType";
            Map map = new HashMap();
            map.put("name", p_name);
            map.put("tType", p_tmplType);
            map.put("sType", p_srcType);
            Collection templates = HibernateUtil.search(hql, map);

            if (templates.size() > 0)
            {
                Iterator it = templates.iterator();
                tmplFormat = (TemplateFormat)it.next();
            }
            else
            {
                String[] errArgs = { p_name, p_tmplType, p_srcType};
                throw new FileImportException(
                    FileImportException.MSG_TEMPLATE_FORMAT_NOT_FOUND,
                    errArgs, null);
            }
        }
        catch (Exception ex)
        {
            throw new FileImportException(
                FileImportException.MSG_FAILED_TO_GET_TEMPLATE_FORMAT,
                null, ex);
        }

        return tmplFormat.getText();
    }
}

