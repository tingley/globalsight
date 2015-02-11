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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletOutputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.globalsight.everest.webapp.pagehandler.administration.reports.util.CurrencyThreadLocal;

public class ExcelReportWriter implements ReportWriter
{
 
	private Workbook workbook = null;
	private Sheet workSheet = null;
    private ArrayList<Sheet> sheets = new ArrayList<Sheet>();
    private ResourceBundle bundle = null;
    
    private CellStyle titleStyle = null;
    private CellStyle headerStyle = null;
    private CellStyle moneyStyle = null;
    private CellStyle stringStyle = null;
    private CellStyle intStyle = null;
    private CellStyle totalStyle = null;
    private CellStyle totalMoneyStyle = null;

    public ExcelReportWriter(ResourceBundle bundle) throws IOException
    {
        this.bundle = bundle;
        this.workbook = new SXSSFWorkbook();
        this.setFormats();
    }
    
    public int createSheet(String sheetName)
    {
        this.sheets.add(0, this.workbook.createSheet(sheetName));
        return 0;
    }
    
    public void setSheet(int sheetId)
    {
        this.workSheet = (Sheet)this.sheets.get(sheetId);
    }
    
    public void setColumnWidth(int beginIndex, int endIndex)
    {
        this.workSheet.setColumnWidth(beginIndex, endIndex);
    }
    
    public void addTitleCell(int column, int row, String label) 
    throws IOException 
    {
    	Cell cell = getCell(getRow(row), column);
    	cell.setCellValue(label);
    	cell.setCellStyle(titleStyle);
    }
    
    public void addHeaderCell(int column, int row, String label) 
    throws IOException 
    {
    	Cell cell = getCell(getRow(row), column);
    	cell.setCellValue(label);
    	cell.setCellStyle(headerStyle);
    }
    
    public void addContentCell(int column, int row, Object contentObj) 
    throws IOException 
    {
    	Cell cell = getCell(getRow(row), column);
        
        if (contentObj instanceof BigDecimal)
        {
        	cell.setCellValue(((BigDecimal)contentObj).doubleValue());
        	cell.setCellStyle(moneyStyle);
        }
        else if (contentObj instanceof Long)
        {
        	cell.setCellValue(((Long)contentObj).longValue());
        	cell.setCellStyle(intStyle);
        }
        else if (contentObj instanceof Integer)
        {
        	cell.setCellValue(((Integer)contentObj).intValue());
        	cell.setCellStyle(intStyle);
        }
        else
        {
        	cell.setCellValue(contentObj.toString());
        	cell.setCellStyle(stringStyle);
        }
    }
    
    public void addTotal(final int totalRow, List total) 
    throws IOException
    {
    	Cell totalCell = getCell(getRow(totalRow), 0);
    	totalCell.setCellValue(bundle.getString("lb_total"));
    	totalCell.setCellStyle(totalStyle);
        
        for (int i = 1; i < total.size(); i++)
        {
            Cell cell = getCell(getRow(totalRow), i);
            if (total.get(i) instanceof BigDecimal)
            {
            	cell.setCellValue(((BigDecimal)total.get(i)).doubleValue());
            	cell.setCellStyle(totalMoneyStyle);
            }
            else if (total.get(i) instanceof Long)
            {
            	cell.setCellValue(((Long)total.get(i)).longValue());
            	cell.setCellStyle(totalStyle);
            }
            else
            {
            	cell.setCellValue("");
            	cell.setCellStyle(totalStyle);
            }
        }
    }
    
    public void mergeCells(int beginColumn, 
                           int beginRow, 
                           int endColumn, 
                           int endRow)
    throws IOException
    {
    	this.workSheet.addMergedRegion(new CellRangeAddress(beginRow, endRow, beginColumn, endColumn));
        setRegionStyle(this.workSheet, new CellRangeAddress(beginRow, endRow, beginColumn, endColumn), headerStyle);   
    }
    
    public void commit(ServletOutputStream outputStream) throws IOException
    {
    	ServletOutputStream out = outputStream;
    	this.workbook.write(out);
        out.close();
        ((SXSSFWorkbook)this.workbook).dispose();
    }
    
    
    private void setFormats() throws IOException 
    {
        // Set title format
    	Font titleFont = this.workbook.createFont();
        titleFont.setUnderline(Font.U_NONE);
        titleFont.setFontName("Times");
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        titleFont.setColor(IndexedColors.BLACK.getIndex());
    
        this.titleStyle = this.workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setWrapText(false);

        // Set header format
        Font headerFont = this.workbook.createFont();
        headerFont.setUnderline(Font.U_NONE);
        headerFont.setFontName("Times");
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        headerFont.setColor(IndexedColors.BLACK.getIndex());
        
        this.headerStyle = this.workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setWrapText(true);
        headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND );
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
        headerStyle.setBorderRight(CellStyle.BORDER_THIN);
        headerStyle.setBorderTop(CellStyle.BORDER_THIN);
        headerStyle.setBorderLeft(CellStyle.BORDER_THIN);
        
        // Set content format
        Font font = this.workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        
        this.stringStyle = this.workbook.createCellStyle();
        stringStyle.setAlignment(CellStyle.ALIGN_LEFT);
        stringStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        this.intStyle = this.workbook.createCellStyle();
        intStyle.setAlignment(CellStyle.ALIGN_LEFT);
        intStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        
        // Set money format
        String moneyFormatString = CurrencyThreadLocal.getMoneyFormatString();
        DataFormat moneyFormat = this.workbook.createDataFormat();
        this.moneyStyle = this.workbook.createCellStyle();
        moneyStyle.setDataFormat(moneyFormat.getFormat(moneyFormatString));
        moneyStyle.setWrapText(false);
        
        // Set total format
        Font totalFont = this.workbook.createFont();
        totalFont.setUnderline(Font.U_NONE);
        totalFont.setFontName("Arial");
        totalFont.setFontHeightInPoints((short) 10);
        totalFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        totalFont.setColor(IndexedColors.BLACK.getIndex());
        
        this.totalStyle = this.workbook.createCellStyle();
        totalStyle.setFont(totalFont);
        totalStyle.setWrapText(true);
        totalStyle.setBorderTop(CellStyle.BORDER_THIN);
        totalStyle.setBorderBottom(CellStyle.BORDER_THIN);
        totalStyle.setFillPattern(CellStyle.SOLID_FOREGROUND );
        totalStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        
        // Set total money format
        this.totalMoneyStyle = this.workbook.createCellStyle();
        totalMoneyStyle.setDataFormat(moneyFormat.getFormat(moneyFormatString));
        totalMoneyStyle.setWrapText(false);
        totalMoneyStyle.setBorderTop(CellStyle.BORDER_THIN);
        totalMoneyStyle.setBorderBottom(CellStyle.BORDER_THIN);
        totalMoneyStyle.setFillPattern(CellStyle.SOLID_FOREGROUND );
        totalMoneyStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    }
    
    public void setRegionStyle(Sheet sheet, CellRangeAddress cellRangeAddress,
    		CellStyle cs) {
    		for (int i = cellRangeAddress.getFirstRow(); i <= cellRangeAddress.getLastRow();
    			i++) {
    			Row row = getRow(i);
    			for (int j = cellRangeAddress.getFirstColumn(); 
    				j <= cellRangeAddress.getLastColumn(); j++) {
    				Cell cell = getCell(row, j);
    				cell.setCellStyle(cs);
    			}
    	  }
    }
    
    private Row getRow(int p_col)
    {
        Row row = this.workSheet.getRow(p_col);
        if (row == null)
            row = this.workSheet.createRow(p_col);
        return row;
    }

    private Cell getCell(Row p_row, int index)
    {
        Cell cell = p_row.getCell(index);
        if (cell == null)
            cell = p_row.createCell(index);
        return cell;
    }
    
}
