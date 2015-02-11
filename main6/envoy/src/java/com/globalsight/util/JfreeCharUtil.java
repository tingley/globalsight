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

package com.globalsight.util;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

import org.apache.log4j.Logger;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;


public class JfreeCharUtil
{
    static private final Logger s_logger = Logger
    .getLogger(JfreeCharUtil.class);
    
    private static PieDataset buildDatas(Map<String, Double> datas)
    {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (String key : datas.keySet())
        {
            dataset.setValue(key, datas.get(key));
        }
        return dataset;
    }
    
    public static void drawPieChart2D(String title, Map<String, Double> datas,
            File OutFile)
    {
        PieDataset dataset = buildDatas(datas);
        JFreeChart chart = ChartFactory.createPieChart(title, dataset, true,
                true, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}={1}({2})", NumberFormat.getNumberInstance(),
                new DecimalFormat("0.00%")));
        plot.setLegendLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}={1}({2})"));
        chart.setBackgroundPaint(Color.white);
        plot.setCircular(true);
        TextTitle textTitle = new TextTitle(title);
        Font font = new Font(textTitle.getFont().getName(),
                Font.CENTER_BASELINE, 20);
        textTitle.setFont(font);
        chart.setTitle(textTitle);
        FileOutputStream fos_jpg = null;

        try
        {
            fos_jpg = new FileOutputStream(OutFile);
            ChartUtilities.writeChartAsJPEG(fos_jpg, 1, chart, 640, 480, null);
            fos_jpg.close();
        }
        catch (Exception e)
        {
            s_logger.error(e.getMessage(), e);
        }
    }

    public static void drawPieChart3D(String title, Map<String, Double> datas,
            File OutFile)
    {
        PieDataset dataset = buildDatas(datas);
        JFreeChart chart = ChartFactory.createPieChart3D(title, dataset, true,
                true, false);
        PiePlot3D plot = (PiePlot3D) chart.getPlot();
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}={1}({2})", NumberFormat.getNumberInstance(),
                new DecimalFormat("0.00%")));
        plot.setLegendLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}={1}({2})"));
        chart.setBackgroundPaint(Color.white);
        plot.setForegroundAlpha(0.7f);
        plot.setCircular(true);
        TextTitle textTitle = new TextTitle(title);
        Font font = new Font(textTitle.getFont().getName(),
                Font.CENTER_BASELINE, 20);
        textTitle.setFont(font);
        chart.setTitle(textTitle);
        FileOutputStream fos_jpg = null;

        try
        {
            fos_jpg = new FileOutputStream(OutFile);
            ChartUtilities.writeChartAsJPEG(fos_jpg, 1, chart, 640, 480, null);
            fos_jpg.close();
        }
        catch (Exception e)
        {
            s_logger.error(e.getMessage(), e);
        }
    }
}
