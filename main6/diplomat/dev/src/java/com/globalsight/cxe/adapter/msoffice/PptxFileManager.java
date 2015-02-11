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
package com.globalsight.cxe.adapter.msoffice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.openoffice.StringIndex;
import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.filterconfiguration.MSOffice2010Filter;
import com.globalsight.util.FileUtil;

public class PptxFileManager
{
    static private final Logger logger = Logger
            .getLogger(PptxFileManager.class);

    private static Pattern ROOT_PATTERN = Pattern
            .compile("<([^:>]*:\\S*) ([^>]*)>");

    private MSOffice2010Filter filter = null;
    private List<String> slideHiddenFiles = new ArrayList<String>();

    private static List<PptxFileType> TYPES = new ArrayList<PptxFileType>();
    static
    {
        // add slide first to check if it is hidden
        PptxFileType slide = new PptxFileType();
        slide.setDir("ppt/slides");
        slide.setMergeFile("ppt/slide.xml");
        slide.setPrefix("slide");
        TYPES.add(slide);

        // add slides' node
        PptxFileType note = new PptxFileType();
        note.setDir("ppt/notesSlides");
        note.setMergeFile("ppt/notesSlide.xml");
        note.setPrefix("notesSlide");
        TYPES.add(note);

        PptxFileType notesMaster = new PptxFileType();
        notesMaster.setDir("ppt/notesMasters");
        notesMaster.setMergeFile("ppt/notesMaster.xml");
        notesMaster.setPrefix("notesMaster");
        TYPES.add(notesMaster);

        PptxFileType slideMaster = new PptxFileType();
        slideMaster.setDir("ppt/slideMasters");
        slideMaster.setMergeFile("ppt/slideMaster.xml");
        slideMaster.setPrefix("slideMaster");
        TYPES.add(slideMaster);

        PptxFileType slideLayout = new PptxFileType();
        slideLayout.setDir("ppt/slideLayouts");
        slideLayout.setMergeFile("ppt/slideLayout.xml");
        slideLayout.setPrefix("slideLayout");
        TYPES.add(slideLayout);

        PptxFileType handoutMaster = new PptxFileType();
        handoutMaster.setDir("ppt/handoutMasters");
        handoutMaster.setMergeFile("ppt/handoutMaster.xml");
        handoutMaster.setPrefix("handoutMaster");
        TYPES.add(handoutMaster);

        PptxFileType diagramData = new PptxFileType();
        diagramData.setDir("ppt/diagrams");
        diagramData.setMergeFile("ppt/diagramData.xml");
        diagramData.setPrefix("data");
        TYPES.add(diagramData);

        PptxFileType chart = new PptxFileType();
        chart.setDir("ppt/charts");
        chart.setMergeFile("ppt/chart.xml");
        chart.setPrefix("chart");
        TYPES.add(chart);

        PptxFileType drawing = new PptxFileType();
        drawing.setDir("ppt/drawings");
        drawing.setMergeFile("ppt/drawing.xml");
        drawing.setPrefix("drawing");
        TYPES.add(drawing);

        PptxFileType comment = new PptxFileType();
        comment.setDir("ppt/comments");
        comment.setMergeFile("ppt/comment.xml");
        comment.setPrefix("comment");
        TYPES.add(comment);
    }

    public MSOffice2010Filter getFilter()
    {
        return filter;
    }

    public void setFilter(MSOffice2010Filter filter)
    {
        this.filter = filter;
    }

    public void mergeFile(String dir)
    {
        for (PptxFileType type : TYPES)
        {
            try
            {
                mergeFiles(dir, type);
            }
            catch (Exception e)
            {
                logger.error(e);
            }
        }
    }

    public void splitFile(String dir)
    {
        for (PptxFileType type : TYPES)
        {
            splitFiles(dir, type);
        }
    }

    private void splitFiles(String dirName, PptxFileType type)
    {
        File f = new File(dirName, type.getMergeFile());
        if (f.exists())
        {
            BufferedReader br = null;
            try
            {
                InputStreamReader r = new InputStreamReader(
                        new FileInputStream(f), "UTF-8");
                br = new BufferedReader(r);

                String line = null;
                Pattern p = Pattern.compile("<file name=\"([^\"]*)\">");

                StringBuffer sb = new StringBuffer();
                String name = null;

                while ((line = br.readLine()) != null)
                {
                    if (line.startsWith("<file "))
                    {
                        Matcher m = p.matcher(line);
                        if (m.find())
                        {
                            name = m.group(1);
                            sb = new StringBuffer(
                                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
                            continue;
                        }
                    }

                    if (line.equals("</file>"))
                    {
                        File f1 = new File(dirName, type.getDir() + "/" + name);
                        FileUtil.writeFile(f1, sb.toString(), "UTF-8");
                        continue;
                    }

                    sb.append(line).append(FileUtil.lineSeparator);
                }

                while ((line = br.readLine()) != null)
                {
                    sb.append(line).append(FileUtil.lineSeparator);
                }
                sb.append("</file>");
            }
            catch (Exception e)
            {
                logger.error(e);
            }
            finally
            {
                if (br != null)
                {
                    try
                    {
                        br.close();
                    }
                    catch (IOException e)
                    {
                        logger.error(e);
                    }
                }
            }

            f.delete();
        }
    }

    private void mergeFiles(String dir, PptxFileType type) throws IOException
    {
        List<File> fs = new ArrayList<File>();
        File root = new File(dir, type.getDir());
        File[] ffs = root.listFiles(type.getFileFilter());

        if (ffs == null)
            return;

        for (File f : ffs)
        {
            fs.add(f);
        }

        if (fs.size() > 0)
        {
            Collections.sort(fs, type.getComparator());
        }

        StringBuffer sb = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append(FileUtil.lineSeparator);

        String name = null;
        List<String> atts = new ArrayList<String>();
        List<String> names = new ArrayList<String>();

        for (File f : fs)
        {
            String content = FileUtil.readFile(f, "utf-8");
            if (content.length() > 600)
                content = content.substring(0, 600);

            Matcher m = ROOT_PATTERN.matcher(content);
            if (m.find())
            {
                if (name == null)
                    name = m.group(1);

                String att = m.group(2);
                String[] as = att.split(" ");
                for (String a : as)
                {
                    int i = a.lastIndexOf("=");
                    if (i > 0)
                    {
                        String n = a.substring(0, i);
                        if (!names.contains(n))
                        {
                            names.add(n);
                            atts.add(a);
                        }
                    }

                }
            }
        }

        sb.append("<").append(name);
        for (String a : atts)
        {
            sb.append(" ").append(a);
        }
        sb.append(">").append(FileUtil.lineSeparator);

        for (File f : fs)
        {
            addFile(sb, f, type);
        }
        sb.append("</").append(name).append(">").append(FileUtil.lineSeparator);

        FileUtil.writeFile(new File(dir, type.getMergeFile()), sb.toString(),
                "UTF-8");
    }

    private void addFile(StringBuffer sb, File f, PptxFileType type)
    {
        boolean isSlide = "slide".equals(type.getPrefix());
        boolean isTranslateHidden = (filter == null ? true : filter
                .isHiddenTextTranslate());
        boolean addThisFile = true;
        boolean isSlideNode = "notesSlide".equals(type.getPrefix());
        boolean isDataXml = "data".equals(type.getPrefix());
        boolean isChartXml = "chart".equals(type.getPrefix());
        boolean isDrawingXml = "drawing".equals(type.getPrefix());
        boolean isCommentXml = "comment".equals(type.getPrefix());

        BufferedReader br = null;
        try
        {
            String fname = f.getName();
            InputStreamReader r = new InputStreamReader(new FileInputStream(f),
                    "UTF-8");
            br = new BufferedReader(r);

            String line1 = br.readLine();
            String line2 = br.readLine();

            // ignore hidden slides and its node : GBS-3576
            if (!isTranslateHidden)
            {
                if (isSlide)
                {
                    String s = (line1 != null && line1.startsWith("<p:sld ")) ? line1
                            : line2;

                    if (s != null)
                    {
                        StringIndex si = StringIndex.getValueBetween(s, 0,
                                "<p:sld ", ">");

                        // check if this slide is hidden
                        if (si != null && si.allValue.contains("show=\"0\""))
                        {
                            addThisFile = false;

                            findHiddenFiles(slideHiddenFiles, f);
                        }
                    }
                }
                else
                {
                    if ((isSlideNode || isDataXml || isChartXml || isDrawingXml || isCommentXml)
                            && isSlideHiddenFile(slideHiddenFiles, f))
                    {
                        addThisFile = false;
                    }
                }
            }

            if (addThisFile)
            {
                sb.append("<file name=\"").append(fname).append("\">")
                        .append(FileUtil.lineSeparator);

                // ignore first line
                if (line1 != null && !line1.startsWith("<?xml"))
                {
                    sb.append(line1).append(FileUtil.lineSeparator);
                }

                sb.append(line2).append(FileUtil.lineSeparator);

                String line = null;
                while ((line = br.readLine()) != null)
                {
                    sb.append(line).append(FileUtil.lineSeparator);
                }
                sb.append("</file>").append(FileUtil.lineSeparator);
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    logger.error(e);
                }
            }
        }
    }

    public List<String> getSlideHiddenFiles()
    {
        return slideHiddenFiles;
    }

    /**
     * Check if this file is hidden reference, ignore hidden slide's files :
     * GBS-3576
     * 
     * @param slideHiddenFiles2
     * @param f
     * @return
     */
    public static boolean isSlideHiddenFile(List<String> p_slideHiddenFiles,
            File p_file)
    {
        if (p_slideHiddenFiles == null || p_slideHiddenFiles.size() == 0
                || p_file == null)
        {
            return false;
        }

        String key = p_file.getParentFile().getName() + "/" + p_file.getName();
        return p_slideHiddenFiles.contains(key);
    }

    /**
     * Find the reference files for one hidden slide
     * 
     * @param p_slideFile
     * @param p_hiddenSlideFiles
     * @throws IOException
     */
    public static void findHiddenFiles(List<String> p_hiddenSlideFiles,
            File p_slideFile) throws IOException
    {
        String relsFileName = "_rels/" + p_slideFile.getName() + ".rels";
        File relsFile = new File(p_slideFile.getParentFile(), relsFileName);
        if (relsFile.exists())
        {
            p_hiddenSlideFiles.add(relsFileName);

            String relsText = FileUtils.read(relsFile, "UTF-8");
            int start = 0;
            StringIndex relsSI = StringIndex.getValueBetween(relsText, start,
                    "Target=\"../", "\"");
            while (relsSI != null)
            {
                p_hiddenSlideFiles.add(relsSI.value);
                start = relsSI.allEnd;

                String[] names = relsSI.value.split("/");
                if (names != null && names.length == 2)
                {
                    if (names[1].startsWith("data"))
                    {
                        String dataRelsFileName = "_rels/" + names[1] + ".rels";
                        p_hiddenSlideFiles.add(dataRelsFileName);
                    }
                    else if (names[1].startsWith("chart"))
                    {
                        String chartRelsFileName = "_rels/" + names[1]
                                + ".rels";
                        File chartRelsFile = new File(p_slideFile
                                .getParentFile().getParentFile(), "charts/"
                                + chartRelsFileName);
                        if (chartRelsFile.exists())
                        {
                            String chartRelsText = FileUtils.read(
                                    chartRelsFile, "UTF-8");
                            int chartStart = 0;
                            StringIndex chartRelsSI = StringIndex
                                    .getValueBetween(chartRelsText, chartStart,
                                            "Target=\"../", "\"");
                            while (chartRelsSI != null)
                            {
                                p_hiddenSlideFiles.add(chartRelsSI.value);
                                chartStart = chartRelsSI.allEnd;

                                chartRelsSI = StringIndex.getValueBetween(
                                        chartRelsText, chartStart,
                                        "Target=\"../", "\"");
                            }
                        }
                    }
                }

                relsSI = StringIndex.getValueBetween(relsText, start,
                        "Target=\"../", "\"");
            }
        }
    }
}
