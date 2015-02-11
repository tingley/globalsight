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
package com.globalsight.ling.docproc.merger.fm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.config.UserParamNames;
import com.globalsight.cxe.adapter.openoffice.StringIndex;
import com.globalsight.everest.page.pageexport.ExportHelper;
import com.globalsight.ling.docproc.extractor.fm.Parser;
import com.globalsight.ling.docproc.extractor.fm.Tag;
import com.globalsight.ling.docproc.merger.html.HtmlPreviewerHelper;

/**
 * This helper class to resolve the mapping relationship for color and match
 * result
 */
public class FMPreviewerHelper
{
    private String m_mifContent = null;
    private String m_colorNon;
    private String m_colorIce;
    private String m_color100;
    private static String NewLine = "\r\n";
    private static String GS_100match = "_gs_100match";
    private static String GS_icematch = "_gs_icematch";

    private static String FColor_s = "<FColor `";
    private static String FColor_e = "'>";

    private static String String_s = "<String `";
    private static String String_e = "'>";

    private static String ParaLine_font_color = "   <Font \r\n    <FColor `(color)'>\r\n   > # end of Font";
    private static String ParaLine_font_color_p = "(color)";

    public FMPreviewerHelper(String mifContent)
    {
        m_mifContent = mifContent;
    }

    protected static Logger getLogger()
    {
        return Logger.getLogger(FMPreviewerHelper.class);
    }

    public String process() throws Exception
    {
        if (m_mifContent == null)
        {
            return m_mifContent;
        }

        if (m_mifContent.indexOf(ExportHelper.GS_COLOR_S) == -1)
        {
            return m_mifContent;
        }

        String allContent = changeColors(m_mifContent);

        // allContent = doingFixing(allContent);

        return allContent;
    }

    private String doingFixing(String mifContent)
    {
        StringReader sr = new StringReader(mifContent);
        Parser p = new Parser(sr);
        List<String> lines = p.getLineList();
        StringBuffer allContent = new StringBuffer();

        return allContent.toString();
    }

    private String changeColors(String mifContent)
    {
        String[] gsColors = new String[] { UserParamNames.PREVIEW_NONMATCH_COLOR_DEFAULT,
                UserParamNames.PREVIEW_100MATCH_COLOR_DEFAULT,
                UserParamNames.PREVIEW_ICEMATCH_COLOR_DEFAULT };
        String gsColorsDefine = HtmlPreviewerHelper.getGSColorDefineColors(mifContent);
        if (gsColorsDefine != null)
        {
            String[] temp = gsColorsDefine.split(",");
            if (temp != null && temp.length == 3)
            {
                gsColors = temp;
            }
        }
        m_colorNon = gsColors[0];
        m_color100 = gsColors[1];
        m_colorIce = gsColors[2];
        m_colorNon = m_colorNon.substring(0, 1).toUpperCase() + m_colorNon.substring(1);
        m_color100 = m_color100.substring(0, 1).toUpperCase() + m_color100.substring(1);
        m_colorIce = m_colorIce.substring(0, 1).toUpperCase() + m_colorIce.substring(1);

        String FColor_Non = FColor_s + m_colorNon + FColor_e;
        String FColor_100 = FColor_s + m_color100 + FColor_e;
        String FColor_Ice = FColor_s + m_colorIce + FColor_e;

        // remove color define string
        String gscolordef = HtmlPreviewerHelper.getGSColorDefine(mifContent);
        if (gscolordef != null)
        {
            mifContent = mifContent.replace(gscolordef, "");
        }

        // parse gs colors
        StringReader sr = new StringReader(mifContent);
        Parser p = new Parser(sr);
        List<String> lines = p.getLineList();

        // fixing some error
        preProcessLines(lines);

        List<String> addedPgfNames = new ArrayList<String>();
        StringBuffer allContent = new StringBuffer();

        String gscolor = null;
        boolean inPgfCatalog = false;
        boolean inPgf = false;
        StringBuffer pgf = new StringBuffer();
        String pgfName = null;
        String pgfNameTag = null;
        String pgfColorTag = null;

        List<GSContentColor> multiGSColors = null;
        boolean inPara = false;
        boolean inMaker = false;
        boolean inParaLine = false;
        boolean paraProcessed = false;
        StringBuffer para = new StringBuffer();
        StringBuffer paraLine = new StringBuffer();
        String paraPgfName = null;
        String paraPgfNameTag = null;
        String paraPgfColorTag = null;
        String paraLineStringTag = null;

        for (int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i);
            String lineTrimmed = line.trim();

            // <PgfCatalog - <Pgf
            if (lineTrimmed.equals(Tag.PGF_CATALOG_HEAD))
            {
                inPgfCatalog = true;
            }

            if (lineTrimmed.equals(Tag.PGF_CATALOG_END))
            {
                inPgfCatalog = false;
            }

            if (inPgfCatalog && lineTrimmed.equals(Tag.PGF_HEAD))
            {
                pgf.delete(0, pgf.length());
                inPgf = true;
                gscolor = null;
                pgf.append(line).append(NewLine);
                pgfName = null;
                pgfNameTag = null;
                pgfColorTag = null;
            }
            else if (inPgf)
            {
                if (lineTrimmed.startsWith(Tag.PGFTAG_HEAD))
                {
                    pgfName = Parser.getStringContent(lineTrimmed);
                    pgfNameTag = line;
                }

                if (lineTrimmed.startsWith(Tag.FCOLOR_HEAD))
                {
                    pgfColorTag = lineTrimmed;
                }

                if (gscolor == null)
                {
                    gscolor = getFirstGSColor(line);
                }

                if (gscolor != null)
                {
                    line = removeGSColorTag(line);
                }

                pgf.append(line).append(NewLine);

                if (lineTrimmed.equals(Tag.PGF_END))
                {
                    String oriPgf = pgf.toString();
                    inPgf = false;

                    // add ori PGF tag, change color if need
                    if (pgfColorTag != null)
                    {
                        if (gscolor == null || gscolor.equalsIgnoreCase(m_colorNon))
                        {
                            allContent.append(oriPgf.replace(pgfColorTag, FColor_Non));
                        }
                        else if (gscolor.equalsIgnoreCase(m_color100))
                        {
                            allContent.append(oriPgf.replace(pgfColorTag, FColor_100));
                        }
                        else if (gscolor.equalsIgnoreCase(m_colorIce))
                        {
                            allContent.append(oriPgf.replace(pgfColorTag, FColor_Ice));
                        }
                    }
                    else
                    {
                        allContent.append(oriPgf);
                    }

                    // add new 100 and ice match PGF tag
                    String pgfName100 = pgfName + GS_100match;
                    String pgfNameTag100 = pgfNameTag.replace(pgfName, pgfName100);
                    String newPgf100 = oriPgf.replace(pgfNameTag, pgfNameTag100);
                    newPgf100 = newPgf100.replace(pgfColorTag, FColor_100);
                    addedPgfNames.add(pgfName100);
                    allContent.append(newPgf100);

                    String pgfNameIce = pgfName + GS_icematch;
                    String pgfNameTagIce = pgfNameTag.replace(pgfName, pgfNameIce);
                    String newPgfIce = oriPgf.replace(pgfNameTag, pgfNameTagIce);
                    newPgfIce = newPgfIce.replace(pgfColorTag, FColor_Ice);
                    addedPgfNames.add(pgfNameIce);
                    allContent.append(newPgfIce);
                }
            }
            // <para
            else if (lineTrimmed.equals(Tag.PARA_HEAD))
            {
                para.delete(0, para.length());
                inPara = true;
                gscolor = null;
                para.append(line).append(NewLine);
                paraPgfName = null;
                paraPgfNameTag = null;
                inMaker = false;
                paraProcessed = false;
                paraPgfColorTag = null;
            }
            else if (inPara)
            {
                if (lineTrimmed.startsWith(Tag.MTEXT_HEAD))
                {
                    inMaker = true;
                }

                if (lineTrimmed.equals(Tag.PARALINE_HEAD))
                {
                    paraLine.delete(0, paraLine.length());
                    inParaLine = true;
                    paraLine.append(line).append(NewLine);
                    paraLineStringTag = "";
                    multiGSColors = null;
                }
                else if (inParaLine)
                {
                    if (gscolor == null)
                    {
                        gscolor = getFirstGSColor(line);
                    }

                    if (getFirstGSColor(line) != null)
                    {
                        // handle special char >
                        String lineContent = line;
                        int start = line.indexOf("`");
                        int end = line.indexOf("'", start);
                        if (start != -1 && end != -1)
                        {
                            lineContent = line.substring(start + 1, end);
                        }
                        
                        multiGSColors = getGSColor(lineContent);
                        line = removeGSColorTag(line);
                        paraLineStringTag = line;
                    }
                    
                    if (lineTrimmed.startsWith("<FTag `"))
                    {
                        paraLine.append("<FTag `'>").append(NewLine);
                    }
                    else if (lineTrimmed.startsWith("<String `"))
                    {
                        if (multiGSColors != null && multiGSColors.size() > 0)
                        {
                            StringBuffer newString = new StringBuffer();
                            int mcsize = multiGSColors.size();
                            for (int j = 0; j < mcsize; j++)
                            {
                                GSContentColor gsc = multiGSColors.get(j);
                                String color = gsc.getColor();
                                color = color.substring(0, 1).toUpperCase() + color.substring(1);
                                newString.append(ParaLine_font_color.replace(ParaLine_font_color_p,
                                        color));
                                newString.append(NewLine);
                                newString.append(String_s);
                                newString.append(gsc.getContent());
                                newString.append(String_e);
                                if (j != mcsize - 1)
                                {
                                    newString.append(NewLine);
                                }
                            }
                            
                            String newLine = newString.toString();
                            paraLine.append(newLine).append(NewLine);
                            paraProcessed = true;
                            multiGSColors.clear();
                        }
                        else
                        {
                            paraLine.append(line).append(NewLine);
                        }
                    }
                    else
                    {
                        paraLine.append(line).append(NewLine);
                    }
                    
                    if (lineTrimmed.equals(Tag.PARALINE_END))
                    {
                        String oriParaLine = paraLine.toString();
                        inParaLine = false;
                        para.append(oriParaLine);
                    } // end of lineTrimmed.equals(Tag.PARALINE_END)
                }
                else
                {
                    if (lineTrimmed.startsWith(Tag.PGFTAG_HEAD))
                    {
                        paraPgfName = Parser.getStringContent(lineTrimmed);
                        paraPgfNameTag = line;
                    }

                    if (lineTrimmed.startsWith(Tag.FCOLOR_HEAD))
                    {
                        paraPgfColorTag = lineTrimmed;
                    }

                    if (lineTrimmed.startsWith(Tag.PGF_NUMBER_FORMAT)
                            && getFirstGSColor(line) != null)
                    {
                        line = removeGSColorTag(line);
                    }

                    para.append(line).append(NewLine);

                    if (lineTrimmed.equals(Tag.PARA_END))
                    {
                        String oriPara = para.toString();
                        inPara = false;

                        if (paraProcessed)
                        {
                            allContent.append(oriPara);
                        }
                        // add ori PGF tag, change color if need
                        else if (gscolor == null || gscolor.equalsIgnoreCase(m_colorNon))
                        {
                            if (paraPgfColorTag != null)
                            {
                                allContent.append(oriPara.replace(paraPgfColorTag, FColor_Non));
                            }
                            else
                            {
                                allContent.append(oriPara);
                            }
                        }
                        else if (gscolor.equalsIgnoreCase(m_color100))
                        {
                            if (paraPgfColorTag != null)
                            {
                                allContent.append(oriPara.replace(paraPgfColorTag, FColor_100));
                            }
                            else if (paraPgfName != null)
                            {
                                String newparaPdfName = paraPgfName + GS_100match;
                                String nweparaPgfNameTag = paraPgfNameTag.replace(paraPgfName,
                                        newparaPdfName);
                                String newPara = oriPara.replace(paraPgfNameTag, nweparaPgfNameTag);
                                allContent.append(newPara);
                            }
                            else
                            {
                                allContent.append(oriPara);
                            }
                        }
                        else if (gscolor.equalsIgnoreCase(m_colorIce))
                        {
                            if (paraPgfColorTag != null)
                            {
                                allContent.append(oriPara.replace(paraPgfColorTag, FColor_Ice));
                            }
                            else if (paraPgfName != null)
                            {
                                String newparaPdfName = paraPgfName + GS_icematch;
                                String nweparaPgfNameTag = paraPgfNameTag.replace(paraPgfName,
                                        newparaPdfName);
                                String newPara = oriPara.replace(paraPgfNameTag, nweparaPgfNameTag);
                                allContent.append(newPara);
                            }
                            else
                            {
                                allContent.append(oriPara);
                            }
                        }
                    }
                }
            }
            // <VariableDef
            // <XRefDef
            else
            {
                if (getFirstGSColor(line) != null)
                {
                    line = removeGSColorTag(line);
                }

                if (lineTrimmed.startsWith(Tag.FCOLOR_HEAD))
                {
                    line = line.replace(lineTrimmed, FColor_Non);
                }

                allContent.append(line).append(NewLine);
            }
        }

        return allContent.toString();
    }

    private void preProcessLines(List<String> lines)
    {
        for (int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i).trim();

            if (line.startsWith(ExportHelper.GS_COLOR_E))
            {
                Pattern pe = Pattern.compile(ExportHelper.RE_GS_COLOR_E);
                String endTag = null;

                Matcher me = pe.matcher(line);
                if (me.find())
                {
                    endTag = me.group();
                    line = line.replace(endTag, "");
                }

                lines.set(i, line);

                if (endTag != null)
                {
                    int x = i - 1;
                    for (; x > 0; x--)
                    {
                        String xline = lines.get(x);
                        if (xline.contains(ExportHelper.GS_COLOR_S))
                        {
                            xline = xline.replace("'>", endTag + "'>");
                            lines.set(x, xline);
                            break;
                        }
                    }
                }
            }
            else if (line.startsWith(ExportHelper.GS_COLOR_S))
            {
                Pattern ps = Pattern.compile(ExportHelper.RE_GS_COLOR_S);
                String startag = null;

                Matcher ms = ps.matcher(line);
                if (ms.find())
                {
                    startag = ms.group();
                    line = line.replace(startag, "");
                }

                lines.set(i, line);

                if (startag != null)
                {
                    int x = i + 1;
                    for (; x < lines.size(); x++)
                    {
                        String xline = lines.get(x);
                        if (xline.contains(ExportHelper.GS_COLOR_E))
                        {
                            xline = xline.replace("<String `", "<String `" + startag);
                            lines.set(x, xline);
                            break;
                        }
                    }
                }
            }
        }
    }

    private String getFirstGSColor(String line)
    {
        if (line.contains(ExportHelper.GS_COLOR_S) || line.contains(ExportHelper.GS_COLOR_E))
        {
            return HtmlPreviewerHelper.getFirstGSColor(line);
        }
        else
        {
            return null;
        }
    }

    private List<GSContentColor> getGSColor(String line)
    {
        if (line.contains(ExportHelper.GS_COLOR_S) || line.contains(ExportHelper.GS_COLOR_E))
        {
            return HtmlPreviewerHelper.getGSColor(line);
        }
        else
        {
            return null;
        }
    }

    private String removeGSColorTag(String line)
    {
        return HtmlPreviewerHelper.removeGSColorTag(line);
    }
}
