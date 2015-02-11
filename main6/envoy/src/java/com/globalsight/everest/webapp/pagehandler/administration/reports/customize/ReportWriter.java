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
import java.util.List;

import javax.servlet.ServletOutputStream;

public interface ReportWriter 
{
    public int createSheet(String sheetName);
    
    public void setSheet(int sheetId);
    
    public void setColumnWidth(int beginIndex, int endIndex);
    
    public void addTitleCell(int column, int row, String label) 
    throws IOException;
    
    public void addHeaderCell(int column, int row, String label) 
    throws IOException;
    
    public void addContentCell(int column, int row, Object contentObj) 
    throws IOException;
    
    public void addTotal(final int totalRow, List total) 
    throws IOException;
    
    public void mergeCells(int beginColumn, 
                           int beginRow, 
                           int endColumn, 
                           int endRow) 
    throws IOException;
    
    public void commit(ServletOutputStream outputStream) throws IOException;
}
