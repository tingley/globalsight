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
package com.globalsight.everest.webapp.pagehandler.administration.reports.customize;


import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormat;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import com.globalsight.everest.webapp.pagehandler.administration.reports.util.CurrencyThreadLocal;

public class ExcelReportWriter implements ReportWriter
{
    private WritableWorkbook workbook = null;
    
    private ArrayList sheets = new ArrayList();
    private WritableSheet workSheet = null;
    private ResourceBundle bundle = null;
    
    private WritableCellFormat titleFormat = null;
    private WritableCellFormat headerFormat = null;
    private WritableCellFormat moneyFormat = null;
    private WritableCellFormat stringFormat = null;
    private WritableCellFormat intFormat = null;
    private WritableCellFormat totalFormat = null;
    private WritableCellFormat totalMoneyFormat = null;
    

    public ExcelReportWriter(OutputStream outputStream, ResourceBundle bundle) throws IOException
    {
        WorkbookSettings settings = new WorkbookSettings();
        settings.setSuppressWarnings(true);
        this.bundle = bundle;
        this.workbook = Workbook.createWorkbook(outputStream, settings);
        this.setFormats();
    }

    
    public int createSheet(String sheetName)
    {
        this.sheets.add(0, this.workbook.createSheet(sheetName, 0));
        return 0;
    }
    
    public void setSheet(int sheetId)
    {
        this.workSheet = (WritableSheet)this.sheets.get(sheetId);
    }
    
    public void setColumnView(int beginIndex, int endIndex)
    {
        this.workSheet.setColumnView(beginIndex, endIndex);
    }
    
    public void addTitleCell(int column, int row, String label) 
    throws IOException 
    {
        this.addCell(new Label(column, row, label, titleFormat));
    }
    
    public void addHeaderCell(int column, int row, String label) 
    throws IOException 
    {
        this.addCell(new Label(column, row, label, headerFormat));
    }
    
    public void addContentCell(int column, int row, Object contentObj) 
    throws IOException 
    {
        WritableCell cell = null;
        
        if (contentObj instanceof BigDecimal)
        {
            cell = new Number(column, row, 
                              ((BigDecimal)contentObj).doubleValue(), 
                              moneyFormat);
        }
        else if (contentObj instanceof Long)
        {
            cell = new Number(column, row, 
                              ((Long)contentObj).longValue(), 
                              intFormat);
        }
        else if (contentObj instanceof Integer)
        {
            cell = new Number(column, row, 
                              ((Integer)contentObj).intValue(), 
                              intFormat);
        }
        else
        {
            cell = new Label(column, row, contentObj.toString(), stringFormat);
        }
        
        this.addCell(cell);
    }
    
    public void addTotal(final int totalRow, List total) 
    throws IOException
    {
        this.addCell(new Label(0, totalRow, bundle.getString("lb_total"), totalFormat));
        
        for (int i = 1; i < total.size(); i++)
        {
            WritableCell cell = null;
            if (total.get(i) instanceof BigDecimal)
            {
                cell = new Number(i, 
                                  totalRow, 
                                  ((BigDecimal)total.get(i)).doubleValue(), 
                                  totalMoneyFormat);
            }
            else if (total.get(i) instanceof Long)
            {
                cell = new Number(i, 
                                  totalRow, 
                                  ((Long)total.get(i)).longValue(), 
                                  totalFormat);
            }
            else
            {
                cell = new Label(i, totalRow, " ", totalFormat);
            }
            
            this.addCell(cell);
        }
    }
    
    public void mergeCells(int beginColumn, 
                           int beginRow, 
                           int endColumn, 
                           int endRow)
    throws IOException
    {
        try
        {
            this.workSheet.mergeCells(beginColumn, beginRow, endColumn, endRow);
        }
        catch (WriteException e) 
        {
            throw new IOException(e.toString());
        }
    }
    
    public void commit() throws IOException
    {
        this.workbook.write();
        
        try
        {
            this.workbook.close();
        }
        catch(WriteException e)
        {
            throw new IOException(e.toString());
        }
        
    }
    
    
    private void setFormats() throws IOException 
    {
        try
        {
            // Set title format
            WritableFont titleFont = new WritableFont(
                                         WritableFont.TIMES, 
                                         14,
                                         WritableFont.BOLD, 
                                         false, 
                                         UnderlineStyle.NO_UNDERLINE,
                                         Colour.BLACK);
        
            this.titleFormat = new WritableCellFormat(titleFont);
            titleFormat.setWrap(false);
            titleFormat.setShrinkToFit(false);

            // Set header format
            WritableFont headerFont = new WritableFont(
                                          WritableFont.TIMES, 
                                          11,
                                          WritableFont.BOLD, 
                                          false, 
                                          UnderlineStyle.NO_UNDERLINE,
                                          Colour.BLACK);
            
            this.headerFormat = new WritableCellFormat(headerFont);
            headerFormat.setWrap(true);
            headerFormat.setBackground(Colour.GRAY_25);
            headerFormat.setShrinkToFit(false);
            headerFormat.setBorder(Border.TOP, BorderLineStyle.THIN);
            headerFormat.setBorder(Border.BOTTOM, BorderLineStyle.THIN);
            headerFormat.setBorder(Border.LEFT, BorderLineStyle.THIN);
            headerFormat.setBorder(Border.RIGHT, BorderLineStyle.THIN);
            
            // Set content format
            this.stringFormat = new WritableCellFormat();
            this.intFormat = new WritableCellFormat();
            
            // Set money format
            String moneyFormatString = CurrencyThreadLocal.getMoneyFormatString();
            this.moneyFormat = new WritableCellFormat(new NumberFormat(moneyFormatString));
            moneyFormat.setWrap(false);
            moneyFormat.setShrinkToFit(false);
            
            // Set total format
            WritableFont totalFont = new WritableFont(WritableFont.ARIAL, 
                                                      10,
                                                      WritableFont.BOLD,
                                                      false,
                                                      UnderlineStyle.NO_UNDERLINE,
                                                      Colour.BLACK);

            totalFormat = new WritableCellFormat(totalFont);
            totalFormat.setWrap(true);
            totalFormat.setShrinkToFit(false);
            totalFormat.setBorder(Border.TOP, BorderLineStyle.THIN);
            totalFormat.setBorder(Border.BOTTOM, BorderLineStyle.THIN);
            totalFormat.setBackground(Colour.GRAY_25);
            
            // Set total money format
            this.totalMoneyFormat = new WritableCellFormat(new NumberFormat(moneyFormatString));
            moneyFormat.setWrap(false);
            totalMoneyFormat.setShrinkToFit(false);
            totalMoneyFormat.setBorder(Border.TOP, BorderLineStyle.THIN);
            totalMoneyFormat.setBorder(Border.BOTTOM, BorderLineStyle.THIN);
            totalMoneyFormat.setBackground(Colour.GRAY_25);
            
        } 
        catch (WriteException e) 
        {
            throw new IOException(e.toString());
        }

    }
    
    private void addCell(WritableCell cell) 
    throws IOException 
    {
        try
        {
            this.workSheet.addCell(cell);
        }
        catch (WriteException e) 
        {
            throw new IOException(e.toString());
        }
    } 
    
}
