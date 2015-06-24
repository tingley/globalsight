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
package com.globalsight.everest.webapp.pagehandler.administration.reports;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.AttributeClone;
import com.globalsight.cxe.entity.customAttribute.DateCondition;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.SortUtil;

public class JobAttributeReportHelper
{
    private static Logger s_logger = Logger.getLogger("Reports");
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
            DateCondition.FORMAT);
    private SimpleDateFormat simpleDate = new SimpleDateFormat("MM/dd/yyyy");
    private HttpServletRequest request = null;
    private HttpServletResponse response = null;
    private Workbook workbook = null;
    private Sheet sheet;
    private CellStyle contentStyle = null;
    private CellStyle headerStyle = null;
    private CellStyle headerStyleSuper = null;
    private ResourceBundle bundle;

    private Date startDate;
    private Date endDate;
    private List<AttributeItem> attriutes;
    private TreeMap<Integer, List<Integer>> totalCells;
    private List<Long> projectIds;
    private List<String> submitIds;
    private List<String> status;
    private List<String> trgLocaleList;
    private AttributeItem orderAttribute;
    private int order = 1;
    private int row = 4;

    private List<JobImpl> jobAttributes;

    public JobAttributeReportHelper(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        this.request = p_request;
        this.response = p_response;
        bundle = PageHandler.getBundle(request.getSession());
        readParameter();
    }

    private List<String> getHeaders()
    {
        List<String> head = new ArrayList<String>();
        head.add(bundle.getString("lb_job_id"));
        head.add(bundle.getString("lb_job_name"));
        head.add(bundle.getString("lb_number_of_languages"));
        head.add(bundle.getString("lb_submitter"));
        head.add(bundle.getString("lb_project"));
        return head;
    }

    private void readParameter()
    {
        String start = request.getParameter("startDate");
        if (start != null)
        {
            start = start.trim();
            if (start.length() > 0)
            {
                try
                {
                    startDate = simpleDate.parse(start);
                }
                catch (ParseException e)
                {
                    s_logger.error(e.getMessage(), e);
                }
            }
        }

        String end = request.getParameter("endDate");
        if (end != null)
        {
            end = end.trim();
            if (end.length() > 0)
            {
                try
                {
                	 Date date = simpleDate.parse(end);
                	 long endLong = date.getTime()+(24*60*60*1000-1);
                     endDate = new Date(endLong);
                }
                catch (ParseException e)
                {
                    s_logger.error(e.getMessage(), e);
                }
            }
        }

        String[] items = request.getParameterValues("selectedProjects");
        projectIds = new ArrayList<Long>();
        for (String item : items)
        {
            projectIds.add(Long.parseLong(item));
        }

        String[] subId = request.getParameterValues("selectedSubmitters");
        submitIds = new ArrayList<String>();
        for (String id : subId)
        {
            submitIds.add(id);
        }

        String[] paramTrgLocales = request
                .getParameterValues("targetLocalesList");
        trgLocaleList = new ArrayList<String>();
        if (paramTrgLocales != null)
        {
            for (int i = 0; i < paramTrgLocales.length; i++)
            {
                trgLocaleList.add(paramTrgLocales[i]);
            }
        }

        String[] normalAtts = request.getParameterValues("normalAtts");
        String[] totalAtts = request.getParameterValues("totalAtts");

        attriutes = new ArrayList<AttributeItem>();

        if (normalAtts != null)
        {
            for (String att : normalAtts)
            {
                AttributeClone clone = HibernateUtil.get(AttributeClone.class,
                        Long.parseLong(att));
                AttributeItem item = new AttributeItem(clone);
                item.setTotal(false);
                attriutes.add(item);
            }
        }

        if (totalAtts != null)
        {
            for (String att : totalAtts)
            {
                AttributeClone clone = HibernateUtil.get(AttributeClone.class,
                        Long.parseLong(att));
                AttributeItem item = new AttributeItem(clone);
                item.setTotal(true);
                attriutes.add(item);
            }
        }

        String orderItem = request.getParameter("orderItem");
        if (orderItem != null && orderItem.length() > 0)
        {
            AttributeClone clone = HibernateUtil.get(AttributeClone.class,
                    Long.parseLong(orderItem));

            AttributeItem item = new AttributeItem(clone);
            if (attriutes.contains(item))
            {
                orderAttribute = new AttributeItem(clone);
            }
        }

        order = "asc".equalsIgnoreCase(request.getParameter("order")) ? 1 : -1;

        String[] jobStatus = request.getParameterValues("selectedStatus");
        status = new ArrayList<String>();
        for (String item : jobStatus)
        {
            status.add(item);
        }
    }

    private void initDate()
    {
        totalCells = new TreeMap<Integer, List<Integer>>();

        Session session = HibernateUtil.getSession();
        Criteria c = session.createCriteria(JobImpl.class);
        c.add(Restrictions.in("createUserId", submitIds));
        c.add(Restrictions.in("state", status));

        if (startDate != null)
        {
            c.add(Restrictions.gt("createDate", startDate));
        }

        if (endDate != null)
        {
            c.add(Restrictions.le("createDate", endDate));
        }

        jobAttributes = c.list();

        for (int i = jobAttributes.size() - 1; i >= 0; i--)
        {
            JobImpl job = jobAttributes.get(i);
            long projectId = job.getL10nProfile().getProject().getId();
            if (projectIds.indexOf(projectId) < 0)
            {
                jobAttributes.remove(i);
                continue;
            }

            boolean found = false;
            for (Workflow w : job.getWorkflows())
            {
                String targetLang = Long.toString(w.getTargetLocale().getId());
                if (trgLocaleList.contains(targetLang))
                {
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                jobAttributes.remove(i);
                continue;
            }
        }

        SortUtil.sort(jobAttributes, getComparator());
    }

    private Comparator<JobImpl> getComparator()
    {
        return new Comparator<JobImpl>()
        {

            @Override
            public int compare(JobImpl o1, JobImpl o2)
            {
                if (orderAttribute != null)
                {
                    JobAttribute jobAtt1 = getJobAttributeValue(
                            orderAttribute.getName(), o1,
                            orderAttribute.isFromSuper());
                    JobAttribute jobAtt2 = getJobAttributeValue(
                            orderAttribute.getName(), o2,
                            orderAttribute.isFromSuper());

                    if (jobAtt2 == null)
                        return -1;

                    if (jobAtt1 == null)
                        return 1;

                    Object ob2 = jobAtt2.getValue();
                    if (ob2 == null)
                    {
                        return -1;
                    }

                    Object ob1 = jobAtt1.getValue();
                    if (ob1 == null)
                    {
                        return 1;
                    }

                    if (ob1 instanceof Integer)
                    {
                        Integer int1 = (Integer) ob1;
                        if (ob2 instanceof Integer)
                        {
                            Integer int2 = (Integer) ob2;
                            return int1.compareTo(int2) * order;
                        }
                    }

                    if (ob1 instanceof Float)
                    {
                        Float f1 = (Float) ob1;
                        if (ob2 instanceof Float)
                        {
                            Float f2 = (Float) ob2;
                            return f1.compareTo(f2) * order;
                        }
                    }

                    String s1 = getAttributeValueString(jobAtt1);
                    String s2 = getAttributeValueString(jobAtt2);
                    if (s2.length() == 0)
                    {
                        return -1;
                    }
                    if (s1.length() == 0)
                    {
                        return 1;
                    }

                    return order * s1.compareTo(s2);
                }
                else
                {
                    return (int) (o1.getId() - o2.getId());
                }
            }
        };
    }

    private void init() throws Exception
    {
        initDate();

        workbook = new SXSSFWorkbook();
        sheet = workbook.createSheet(bundle.getString("lb_job_attributes"));
    }
    
    private void addTitle() throws Exception
    {
    	// title font is black bold on white
    	Font titleFont = workbook.createFont();
        titleFont.setUnderline(Font.U_NONE);
        titleFont.setFontName("Arial");
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
    	
        Row titleRow = getRow(0);
        Cell cell_A = getCell(titleRow, 0);
        cell_A.setCellValue(bundle.getString("lb_job_attributes"));
        cell_A.setCellStyle(titleStyle);
        sheet.setColumnWidth(0, 20 * 256);
    }

    /**
     * Adds the table header for the Dell Matches sheet
     * 
     */
    private void addHeader() throws Exception
    {
        int c = 0;
        Row headerRow = getRow(2);
        for (String header : getHeaders())
        {
        	Cell cell_Header = getCell(headerRow, c);
        	cell_Header.setCellValue(header);
        	cell_Header.setCellStyle(getHeaderStyle(false));
        	sheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
            setRegionStyle(new CellRangeAddress(2, 3, c, c), headerStyle);
            sheet.setColumnWidth(c, 17 * 256);
            c++;
        }

        for (AttributeItem item : attriutes)
        {
            CellStyle style = item.isFromSuper() ? getHeaderStyle(true)
                    : getHeaderStyle(false);
            Cell cell_Attriutes = getCell(headerRow, c);
            cell_Attriutes.setCellValue(item.getName());
            cell_Attriutes.setCellStyle(style);
        	sheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
            setRegionStyle(new CellRangeAddress(2, 3, c, c), style);
            sheet.setColumnWidth(c, 25 * 256);
            c++;
        }
    }

    private void writeData() throws Exception
    {
        for (JobImpl job : jobAttributes)
        {
            int col = 0;
            Row theRow = getRow(row);

            // Job Id
            Cell cell_A = getCell(theRow, col++);
            cell_A.setCellValue(job.getId());
            cell_A.setCellStyle(getContentStyle());

            // Job Name
            Cell cell_B = getCell(theRow, col++);
            cell_B.setCellValue(job.getName());
            cell_B.setCellStyle(getContentStyle());

            // Number of Languages
            Cell cell_C = getCell(theRow, col++);
            cell_C.setCellValue(job.getWorkflows().size());
            cell_C.setCellStyle(getContentStyle());

            // Submitter
            Cell cell_D = getCell(theRow, col++);
            cell_D.setCellValue(UserHandlerHelper.getUser(
                    job.getCreateUserId()).getUserName());
            cell_D.setCellStyle(getContentStyle());

            // Project
            Cell cell_E = getCell(theRow, col++);
            cell_E.setCellValue(job.getL10nProfile()
                    .getProject().getName());
            cell_E.setCellStyle(getContentStyle());

            for (AttributeItem item : attriutes)
            {
                JobAttribute jobAtt = getJobAttributeValue(item.getName(), job,
                        item.isFromSuper());

                Cell cell = getCell(theRow, col++);
                if (jobAtt == null)
                {
                	cell.setCellValue(bundle
                            .getString("lb_na"));
                	cell.setCellStyle(getContentStyle());
                }
                else
                {
                    Object ob = jobAtt.getValue();
                    if (ob == null)
                    {
                    	cell.setCellValue("");
                    	cell.setCellStyle(getContentStyle());
                    }
                    else
                    {
                        String type = jobAtt.getType();
                        if (Attribute.TYPE_FLOAT.equals(type))
                        {
                            double value = Double.parseDouble(jobAtt
                                    .getFloatValue().toString());
                            cell.setCellValue(value);
                            cell.setCellStyle(getContentStyle());
                            if (item.isTotal())
                            {
                                addTotalCell(col, row);
                            }
                        }
                        else if (Attribute.TYPE_INTEGER.equals(type))
                        {
                        	cell.setCellValue(jobAtt
                                    .getIntegerValue());
                        	cell.setCellStyle(getContentStyle());
                            if (item.isTotal())
                            {
                                addTotalCell(col, row);
                            }
                        }
                        else
                        {
                        	cell.setCellValue(getAttributeValueString(jobAtt));
                        	cell.setCellStyle(getContentStyle());
                        }
                    }
                }
            }

            row++;
        }
    }

    private void writeTotal() throws Exception
    {
    	Font totalFont = workbook.createFont();
    	totalFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
    	totalFont.setColor(IndexedColors.BLACK.getIndex());
    	totalFont.setUnderline(Font.U_NONE);
    	totalFont.setFontName("Arial");
    	totalFont.setFontHeightInPoints((short) 9);
    	
        CellStyle totalStyle = workbook.createCellStyle();
        totalStyle.setFont(totalFont);

        Set<Integer> keys = totalCells.keySet();
        
        if (keys.size() > 0)
        {
            row++;
            Cell cell_B = getCell(getRow(row), 1);
            cell_B.setCellValue(bundle.getString("lb_total"));
            cell_B.setCellStyle(totalStyle);

            for (Integer key : keys)
            {
            	Cell cell = getCell(getRow(row), key - 1);
            	cell.setCellFormula(
                		getTotalFormula(key, totalCells.get(key)));
            	cell.setCellStyle(totalStyle);
            }
        }
    }

    private String getTotalFormula(int x, List<Integer> ys)
    {
        StringBuffer result = new StringBuffer("SUM(");
        for (int i = 0; i < ys.size(); i++)
        {
            if (i > 0)
            {
                result.append("+");
            }
            result.append(toChar(x - 1)).append(ys.get(i) + 1);
        }
        result.append(")");
        return result.toString();
    }

    private String toChar(int i)
    {
        int x = i + 1;
        String s = "";

        int m = x % 26;
        int n = x / 26;

        while (n > 0)
        {
            s += (char) (n + 64);
            m = m % 26;
            n = m / 26;
        }

        if (m > 0)
        {
            s += (char) (m + 64);
        }

        return s;
    }

    private String getAttributeValueString(JobAttribute jobAtt)
    {
        Object ob = jobAtt.getValue();
        if (ob instanceof Date)
        {
            Date date = (Date) ob;
            return simpleDateFormat.format(date);
        }

        if (ob instanceof List)
        {
            List<String> list = (List) ob;
            StringBuffer s = new StringBuffer();
            for (String v : list)
            {
                if (s.length() > 0)
                {
                    s.append("\012");
                }

                s.append(v);
            }

            return s.toString();
        }

        return ob.toString();
    }

    private JobAttribute getJobAttributeValue(String name, JobImpl job,
            boolean isSuper)
    {
        List<JobAttribute> jobAtts = job.getAttributesAsList();
        for (JobAttribute jobAtt : jobAtts)
        {
            if (isSuper
                    && !CompanyWrapper.SUPER_COMPANY_ID.equals(jobAtt
                            .getAttribute().getCompanyId()))
            {
                continue;
            }

            if (name.equals(jobAtt.getAttribute().getDisplayName()))
            {
                return jobAtt;
            }
        }

        return null;
    }

    private void end() throws Exception
    {
        if (workbook != null)
        {
        	ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.close();
            ((SXSSFWorkbook)workbook).dispose();
        }
    }

    private void addTotalCell(int x, int y)
    {
        List<Integer> cells = totalCells.get(x);
        if (cells == null)
        {
            cells = new ArrayList<Integer>();
            totalCells.put(x, cells);
        }
        cells.add(y);
    }

    /**
     * Generates the Excel report as a temp file and returns the temp file.
     * 
     * @return File
     * @exception Exception
     */
    public void generateReport() throws Exception
    {
        String userId = (String) request.getSession().getAttribute(
                WebAppConstants.USER_NAME);

        init();

        List<Long> reportJobIDS = ReportHelper.getJobIDS(new ArrayList(
                jobAttributes));
        // Cancel Duplicate Request
        if (ReportHelper.checkReportsDataInProgressStatus(userId,
                reportJobIDS, getReportType()))
        {
            workbook = null;
            response.sendError(response.SC_NO_CONTENT);
            return;
        }
        // Set ReportsData.
        ReportHelper.setReportsData(userId, reportJobIDS, getReportType(), 
                0, ReportsData.STATUS_INPROGRESS);

        addTitle();
        addHeader();
        writeData();
        writeTotal();

        // Set ReportsData.
        ReportHelper.setReportsData(userId, reportJobIDS, getReportType(), 
                100, ReportsData.STATUS_FINISHED);
        end();
    }
    
    public void setRegionStyle(CellRangeAddress cellRangeAddress,
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
    
    private CellStyle getHeaderStyle(Boolean isSuper) throws Exception
    {
    	if(isSuper && headerStyleSuper != null){
    		return headerStyleSuper;
    	}
    	
    	if(!isSuper && headerStyle != null){
    		return headerStyle;
    	}
    	
	    Font font = workbook.createFont();
	    font.setBoldweight(Font.BOLDWEIGHT_BOLD);
	    if(isSuper){	    	
	    	font.setColor(IndexedColors.ORANGE.getIndex());
	    }
	    else {
	    	font.setColor(IndexedColors.BLACK.getIndex());
		}
	    font.setUnderline(Font.U_NONE);
	    font.setFontName("Arial");
	    font.setFontHeightInPoints((short) 9);
	
	    CellStyle cs = workbook.createCellStyle();
	    cs.setFont(font);
	    cs.setWrapText(true);
	    cs.setFillPattern(CellStyle.SOLID_FOREGROUND );
	    cs.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	    cs.setBorderTop(CellStyle.BORDER_THIN);
	    cs.setBorderRight(CellStyle.BORDER_THIN);
	    cs.setBorderBottom(CellStyle.BORDER_THIN);
	    cs.setBorderLeft(CellStyle.BORDER_THIN);
	    
	    if(isSuper){
	    	headerStyleSuper = cs;
	    	return headerStyleSuper;
	    }else{
	    	headerStyle = cs;
	    	return headerStyle;
	    }	    
    }
    
    private CellStyle getContentStyle() throws Exception
    {
        if (contentStyle == null)
        {
            CellStyle style = workbook.createCellStyle();
            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_LEFT);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            Font font = workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            style.setFont(font);

            contentStyle = style;
        }

        return contentStyle;
    }
    
    private Row getRow(int p_col)
    {
        Row row = sheet.getRow(p_col);
        if (row == null)
            row = sheet.createRow(p_col);
        return row;
    }

    private Cell getCell(Row p_row, int index)
    {
        Cell cell = p_row.getCell(index);
        if (cell == null)
            cell = p_row.createCell(index);
        return cell;
    }
    
    public String getReportType()
    {
        return ReportConstants.JOB_ATTRIBUTE_REPORT;
    }
}
