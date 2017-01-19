/**
 *  Copyright 2009-2016 Welocalize, Inc. 
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
 */
package com.globalsight.util;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Report styles
 * 
 * @author Vincent Yan
 * @since 8.7.2
 * @date 10/19/2016
 */
public class ReportStyle
{
    private Workbook workbook = null;
    private CellStyle headerStyle = null;
    private CellStyle contentStyle = null;
    private CellStyle rtlContentStyle = null;
    private CellStyle lockedStyle = null;
    private CellStyle unlockedStyle = null;
    private CellStyle unlockedRightStyle = null;
    private CellStyle titleStyle = null;
    private CellStyle colorStyle = null;
    private short colorIndex;
    private Font contentFont = null;
    private Font internalFont = null;

    public ReportStyle()
    {
        init();
    }

    public ReportStyle(Workbook workbook)
    {
        this.workbook = workbook;
        init();
    }

    private void init()
    {
        if (workbook == null)
        {
            workbook = new SXSSFWorkbook();
        }
    }

    public Workbook getWorkbook()
    {
        return workbook;
    }

    public void setWorkbook(Workbook workbook)
    {
        if (workbook == null)
            init();
        else
            this.workbook = workbook;
    }

    public CellStyle getHeaderStyle()
    {
        if (headerStyle == null)
        {
            Font font = workbook.createFont();
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            font.setColor(IndexedColors.BLACK.getIndex());
            font.setUnderline(Font.U_NONE);
            font.setFontName("Times");
            font.setFontHeightInPoints((short) 11);

            headerStyle = workbook.createCellStyle();
            headerStyle.setFont(font);
            headerStyle.setWrapText(true);
            headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setBorderTop(CellStyle.BORDER_THIN);
            headerStyle.setBorderRight(CellStyle.BORDER_THIN);
            headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
            headerStyle.setBorderLeft(CellStyle.BORDER_THIN);
            headerStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        }

        return headerStyle;
    }

    public void setHeaderStyle(CellStyle headerStyle)
    {
        this.headerStyle = headerStyle;
    }

    /**
     * Gets the content font.
     * 
     * @since GBS-4663
     */
    public Font getContentFont()
    {
        if (contentFont == null)
        {
            Font font = workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);

            contentFont = font;
        }

        return contentFont;
    }

    /**
     * Gets the internal text font.
     * 
     * @since GBS-4663
     */
    public Font getInternalFont()
    {
        if (internalFont == null)
        {
            Font font = workbook.createFont();
            font.setColor(IndexedColors.BROWN.getIndex());
            font.setFontName("Internal Text");
            font.setFontHeightInPoints((short) 10);

            internalFont = font;
        }

        return internalFont;
    }

    public CellStyle getContentStyle()
    {
        if (contentStyle == null)
        {
            Font font = workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);

            contentStyle = workbook.createCellStyle();
            contentStyle.setWrapText(true);
            contentStyle.setAlignment(CellStyle.ALIGN_LEFT);
            contentStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            contentStyle.setFont(font);
        }

        return contentStyle;
    }

    public void setContentStyle(CellStyle contentStyle)
    {
        this.contentStyle = contentStyle;
    }

    public CellStyle getRtlContentStyle()
    {
        if (rtlContentStyle == null)
        {
            rtlContentStyle = workbook.createCellStyle();
            rtlContentStyle.cloneStyleFrom(getContentStyle());
            rtlContentStyle.setAlignment(CellStyle.ALIGN_RIGHT);
        }

        return rtlContentStyle;
    }

    public void setRtlContentStyle(CellStyle rtlContentStyle)
    {
        this.rtlContentStyle = rtlContentStyle;
    }

    public CellStyle getLockedStyle()
    {
        if (lockedStyle == null)
        {
            lockedStyle = workbook.createCellStyle();
            lockedStyle.cloneStyleFrom(getContentStyle());
            lockedStyle.setLocked(true);
        }

        return lockedStyle;
    }

    public void setLockedStyle(CellStyle lockedStyle)
    {
        this.lockedStyle = lockedStyle;
    }

    public CellStyle getUnlockedStyle()
    {
        if (unlockedStyle == null)
        {
            unlockedStyle = workbook.createCellStyle();
            unlockedStyle.cloneStyleFrom(getContentStyle());
            unlockedStyle.setLocked(false);
        }

        return unlockedStyle;
    }

    public void setUnlockedStyle(CellStyle unlockedStyle)
    {
        this.unlockedStyle = unlockedStyle;
    }

    public CellStyle getUnlockedRightStyle()
    {
        if (unlockedRightStyle == null)
        {
            unlockedRightStyle = workbook.createCellStyle();
            unlockedRightStyle.cloneStyleFrom(getContentStyle());
            unlockedRightStyle.setLocked(false);
            unlockedRightStyle.setAlignment(CellStyle.ALIGN_RIGHT);
        }

        return unlockedRightStyle;
    }

    public void setUnlockedRightStyle(CellStyle unlockedRightStyle)
    {
        this.unlockedRightStyle = unlockedRightStyle;
    }

    public CellStyle getTitleStyle()
    {
        if (titleStyle == null)
        {
            Font titleFont = workbook.createFont();
            titleFont.setUnderline(Font.U_NONE);
            titleFont.setFontName("Times");
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

            titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
        }

        return titleStyle;
    }

    public void setTitleStyle(CellStyle titleStyle)
    {
        this.titleStyle = titleStyle;
    }

    public CellStyle getColorStyle(short color)
    {
        if (colorStyle == null || color != colorIndex)
        {
            colorStyle = workbook.createCellStyle();
            colorStyle.cloneStyleFrom(getContentStyle());

            colorStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            colorStyle.setFillForegroundColor(color);
        }

        return colorStyle;
    }

    public void setColorStyle(CellStyle colorStyle)
    {
        this.colorStyle = colorStyle;
    }
}
