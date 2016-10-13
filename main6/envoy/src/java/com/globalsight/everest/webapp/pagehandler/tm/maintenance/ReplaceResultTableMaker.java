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

package com.globalsight.everest.webapp.pagehandler.tm.maintenance;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.searchreplace.TmConcordanceResult;
import com.globalsight.everest.webapp.WebAppConstants;

import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;

import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.PseudoOverrideItemException;
import com.globalsight.ling.tw.PtagStringFormatter;
import com.globalsight.ling.tw.TmxPseudo;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;

import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlNames;

import java.io.IOException;

import java.sql.Connection;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Creates a HTML table display of TUs found during a search in the TM.
 */
public class ReplaceResultTableMaker
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            ReplaceResultTableMaker.class);

    public static final String s_CHECKMARK =
        "<IMG SRC='/globalsight/images/checkmark.gif' HEIGHT=9 WIDTH=13 " +
        "HSPACE=10 VSPACE=3>";

    private HashSet m_replaced;
    private boolean m_makingSource;
    private boolean m_wasReplaced;

    // Constructor
    public ReplaceResultTableMaker()
    {
    }

    /**
     * Converts the replace results into table rows.
     */
	public String getTableRows(TmConcordanceResult p_tmResults,
			Collection p_replaced) throws EnvoyServletException
    {
        m_replaced = new HashSet(p_replaced);

        StringBuffer sb = new StringBuffer();
        List<SegmentTmTu> tus = p_tmResults.getTus();
        GlobalSightLocale srcLocale = p_tmResults.getSourceLocale();
        ArrayList trgLocales = p_tmResults.getTargetLocales();
        // NOTE: for now we only display one target locale
        GlobalSightLocale trgLocale = (GlobalSightLocale)trgLocales.get(0);
        String rowColor, targetRowColor;

        try
        {
            for (int i = 0, max = tus.size(); i < max; i++)
            {
                SegmentTmTu tu = (SegmentTmTu)tus.get(i);

                rowColor = (i % 2 == 0) ? "#FFFFFF" : "#EEEEEE";

                BaseTmTuv srcTuv = tu.getFirstTuv(srcLocale);
                BaseTmTuv trgTuv;
                Collection targetTuvs = tu.getTuvList(trgLocale);
                boolean first = true;
                boolean wasReplaced;
                int count = 0;

                for (Iterator it = targetTuvs.iterator(); it.hasNext(); )
                {
                    trgTuv = (BaseTmTuv)it.next();

                    wasReplaced = m_replaced.contains(trgTuv);

                    targetRowColor = (count++ % 2 == 0) ? rowColor : "#DDDDDD";
                    sb.append(makeTableRow(srcTuv, trgTuv, rowColor,
                        targetRowColor, first, wasReplaced));

                    first = false;
                }
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }

        return sb.toString();
    }

    /**
     * Make a single table row for a search result.
     */
    private String makeTableRow(BaseTmTuv p_srcTuv, BaseTmTuv p_trgTuv,
        String p_rowColor, String p_targetRowColor, boolean p_first,
        boolean p_wasReplaced)
        throws Exception
    {
        StringBuffer sb = new StringBuffer();

        // start row
        sb.append("<TR VALIGN=TOP BGCOLOR=\"");
        sb.append(p_rowColor);
        sb.append("\">");

        // Make segmentID cell
        sb.append("<TD CLASS=results STYLE=\"padding-top: 2px\">");
        if (p_first)
        {
            sb.append(p_srcTuv.getTu().getId());
        }
        else
        {
            sb.append("&nbsp;");
        }
        sb.append("</TD>\n");

        // Make source segment cell (parent then subflows)
        // - Inner table is two cells wide
        // - parent seg spans both cells in first row
        // - remaining rows have subflowID and then seg text in separate cells
        sb.append("<TD CLASS=results>\n");
        if (p_first)
        {
            sb.append("<TABLE CLASS=\"standardText\">\n");
            sb.append(makeSourceParentLevel(p_srcTuv));
            // sb.append(makeSubflowLevel(p_srcTuv));
            sb.append("</TABLE>\n");
        }
        else
        {
            sb.append("&nbsp;");
        }
        sb.append("</TD>\n");

        // Make target segment cell (parent then subflows)
        // - Inner table is two cells wide
        // - parent seg spans both cells in first row
        // - remaining rows have subflowID and then seg text in separate cells
        sb.append("<TD>\n");
        sb.append("<TABLE CLASS=\"standardText\" BGCOLOR=\"");
        sb.append(p_targetRowColor);
        sb.append("\">\n");
        sb.append(makeTargetParentLevel(p_trgTuv, p_wasReplaced));
        // sb.append(makeSubflowLevel(p_trgTuv));
        sb.append("</TABLE>\n");
        sb.append("</TD>\n");

        // end row
        sb.append("</TR>\n\n");

        return sb.toString();
    }

    /** Make root segment HTML */
    private String makeSourceParentLevel(BaseTmTuv p_tuv)
        throws Exception
    {
        m_makingSource = true;
        return makeParentLevel(p_tuv);
    }

    /** Make root segment HTML */
    private String makeTargetParentLevel(BaseTmTuv p_tuv, boolean p_replaced)
        throws Exception
    {
        m_makingSource = false;
        m_wasReplaced = p_replaced;
        return makeParentLevel(p_tuv);
    }

    /** Make root segment HTML */
    private String makeParentLevel(BaseTmTuv p_tuv)
        throws Exception
    {
        StringBuffer result = new StringBuffer();
        String gxml = null;
        String rawPtagStr = null;

        gxml = GxmlUtil.stripRootTag(p_tuv.getSegment());

        rawPtagStr = makePtagString(gxml, PseudoConstants.PSEUDO_COMPACT,
            p_tuv.getTu().getFormat());

        // build html
        result.append("<TR VALIGN=TOP>\n");

        // Target TUVs get a check mark if they were replaced.
        if (m_makingSource == false)
        {
            result.append("<TD>");

            if (m_wasReplaced)
            {
                result.append(s_CHECKMARK);
            }
            else
            {
                result.append("&nbsp;");
            }

            result.append("</TD>");
        }

        result.append("<TD STYLE=\"word-wrap: break-word\" COLSPAN=2 WIDTH=300");
        if (EditUtil.isRTLLocale(p_tuv.getLocale()))
        {
            result.append(Text.containsBidiChar(rawPtagStr) ?
                " DIR=\"RTL\"" : " DIR=\"LTR\"" );
        }
        result.append(">\n");

        result.append(colorPtagString(rawPtagStr, p_tuv.getLocale()));
        result.append("</TD>\n");
        result.append("</TR>\n");

        return result.toString();
    }

    /** Make subflow HTML. */
    /* No subflows in 4.5 SegmentTM.

    private String makeSubflowLevel(BaseTmTuv p_tuv)
        throws Exception
    {
        StringBuffer sb = new StringBuffer();
        List parentsOfSubs = null;
        ListIterator parentsOfSubsIt = null;
        List subsUnderParent = null;
        ListIterator subsUnderParentIt = null;
        GxmlElement aSub = null;
        String gxml = null;
        String rawPtagStr = null;

        parentsOfSubs = p_tuv.getSubflowParentsAsGxmlElements();
        parentsOfSubsIt = parentsOfSubs.listIterator();
        while (parentsOfSubsIt.hasNext())
        {
            GxmlElement aParentOfSubs = (GxmlElement)parentsOfSubsIt.next();
            String rootId = makeSubflowRootId(p_tuv.getTu().getTuId(),
                aParentOfSubs);
            subsUnderParent = aParentOfSubs.getDescendantElements(
                GxmlElement.SUB_TYPE);
            subsUnderParentIt = subsUnderParent.listIterator();

            while (subsUnderParentIt.hasNext())
            {
                aSub = (GxmlElement) subsUnderParentIt.next();
                gxml = aSub.toGxmlExcludeTopTags();

                // *always* use the SUB's source datatype if present
                // if sub datatype is not present - we get it from the parent
                String srcDataType = aSub.getAttribute(GxmlNames.SUB_DATATYPE);
                if (srcDataType == null || srcDataType.length() <= 0)
                {
                    srcDataType = p_tuv.getDataType();
                }

                rawPtagStr = makePtagString(gxml,
                    PseudoConstants.PSEUDO_COMPACT, srcDataType);

                sb.append("<TR VALIGN=TOP>\n");
                sb.append("<TD WIDTH=80>\n");
                sb.append(rootId); // parent TuId and parent ptag identifier
                sb.append(aSub.getAttribute(GxmlNames.SUB_ID)); // subID
                sb.append("</TD>\n");
                sb.append("<TD WIDTH=220 STYLE=\"word-wrap: break-word\"");

                if (EditUtil.isRTLLocale(p_tuv.getLocale()))
                {
                    sb.append( Text.containsBidiChar(rawPtagStr) ?
                        " DIR=\"RTL\">\n" : " DIR=\"LTR\">\n" );
                }
                else
                {
                    sb.append(">\n");
                }

                sb.append(colorPtagString(rawPtagStr, p_tuv.getLocale()));

                sb.append("</TD>\n");
                sb.append("</TR>\n");
            }
        }

        return sb.toString();
    }
    */

    /** Convert a gxml string to a ptag string. */
    private String makePtagString(String p_gxml, int p_pTagDisplayMode,
        String p_dataType)
        throws Exception
    {
        PseudoData pTagData = null;
        TmxPseudo convertor = null;

        // create ptag resources
        pTagData = new PseudoData();
        convertor = new TmxPseudo();

        // convert gxml
        pTagData.setMode(p_pTagDisplayMode);
        pTagData.setAddables(p_dataType);

        convertor.tmx2Pseudo(p_gxml, pTagData);

        return pTagData.getPTagSourceString();
    }

    /** color code ptag strings*/
    private String colorPtagString(String p_rawPtagString,
        GlobalSightLocale p_locale)
        throws Exception
    {
        PtagStringFormatter format = new PtagStringFormatter();

        return format.htmlLtrPtags(
            EditUtil.encodeHtmlEntities(p_rawPtagString), null, false,
            p_locale);
    }

    /**
     * Builds a subflow-root id.  The caller must append a segment id
     * to form a full offline id.
     * @param p_tuId the TU id of the target tuv.
     * @param p_gxml_element the elment used to from the placeholder Id
     * @return the subflow root id as a string
     */
    private String makeSubflowRootId(long p_tuId, GxmlElement p_element)
    {
        String parentPtagId = "";
        String tagName = p_element.getName();
        Hashtable attributes = new Hashtable();
        PseudoData placeHolder = new PseudoData();
        StringBuffer sb = new StringBuffer();

        attributes.put("type", p_element.getAttribute("type"));
        attributes.put("x",    p_element.getAttribute("x"));

        try // try to get the full name
        {
            placeHolder.setMode(PseudoConstants.PSEUDO_COMPACT);
            parentPtagId = placeHolder.makePseudoTagName(
                tagName, attributes, "" );
        }
        catch (Exception e1)
        {
            try // to get the x attribute only
            {
                parentPtagId = p_element.getAttribute("x");
            }
            catch (Exception e2)
            {
                // drop it
                parentPtagId = "";
            }
        }

        sb.append(Long.toString(p_tuId));
        sb.append(AmbassadorDwUpConstants.SEGMENT_ID_DELIMITER);
        sb.append("[");
        sb.append(parentPtagId);
        sb.append("]");
        sb.append(AmbassadorDwUpConstants.SEGMENT_ID_DELIMITER);

        return sb.toString();
    }
}

