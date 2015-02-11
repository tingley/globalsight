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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.UnderlineStyle;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.AttributeClone;
import com.globalsight.cxe.entity.customAttribute.DateCondition;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class JobAttributeReportHelper
{
    private static GlobalSightCategory s_logger = (GlobalSightCategory) GlobalSightCategory
            .getLogger("Reports");
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
            DateCondition.FORMAT);

    private HttpServletRequest request = null;
    private HttpServletResponse response = null;
    private WritableWorkbook workbook = null;
    private WritableSheet sheet;
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
                    startDate = simpleDateFormat.parse(start);
                }
                catch (ParseException e)
                {
                    s_logger.error(e);
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
                    endDate = simpleDateFormat.parse(end);
                }
                catch (ParseException e)
                {
                    s_logger.error(e);
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
            AttributeClone clone = HibernateUtil.get(AttributeClone.class, Long
                    .parseLong(orderItem));
            
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
                String targetLang = w.getTargetLocale().toString();
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

        Collections.sort(jobAttributes, getComparator());
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
                    JobAttribute jobAtt1 = getJobAttributeValue(orderAttribute
                            .getName(), o1, orderAttribute.isFromSuper());
                    JobAttribute jobAtt2 = getJobAttributeValue(orderAttribute
                            .getName(), o2, orderAttribute.isFromSuper());

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

        WorkbookSettings settings = new WorkbookSettings();
        settings.setSuppressWarnings(true);
        workbook = Workbook
                .createWorkbook(response.getOutputStream(), settings);
        sheet = workbook.createSheet(bundle.getString("lb_job_attributes"), 0);
    }

    /**
     * Adds the table header for the Dell Matches sheet
     * 
     */
    private void addHeader() throws Exception
    {
        // title font is black bold on white
        WritableFont titleFont = new WritableFont(WritableFont.ARIAL, 14,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);
        WritableCellFormat titleFormat = new WritableCellFormat(titleFont);
        titleFormat.setWrap(false);
        titleFormat.setShrinkToFit(false);

        sheet.addCell(new Label(0, 0, bundle.getString("lb_job_attributes"),
                titleFormat));
        sheet.setColumnView(0, 20);

        // headerFont is black bold on light grey
        WritableFont headerFont = new WritableFont(WritableFont.ARIAL, 9,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);
        WritableFont headerFontSuper = new WritableFont(WritableFont.ARIAL, 9,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.ORANGE);
        WritableCellFormat headerFormat = new WritableCellFormat(headerFont);
        WritableCellFormat headerFormatSuper = new WritableCellFormat(headerFontSuper);
        
        headerFormat.setWrap(true);
        headerFormat.setBackground(jxl.format.Colour.GRAY_25);
        headerFormat.setShrinkToFit(false);
        headerFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        headerFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THIN);
        headerFormat.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        headerFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THIN);
        
        headerFormatSuper.setWrap(true);
        headerFormatSuper.setBackground(jxl.format.Colour.GRAY_25);
        headerFormatSuper.setShrinkToFit(false);
        headerFormatSuper.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        headerFormatSuper.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THIN);
        headerFormatSuper.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        headerFormatSuper.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THIN);

        int c = 0;
        for (String header : getHeaders())
        {
            sheet.addCell(new Label(c, 2, header, headerFormat));
            sheet.mergeCells(c, 2, c, 3);
            sheet.setColumnView(c, 17);
            c++;
        }

        for (AttributeItem item : attriutes)
        {
            WritableCellFormat format = item.isFromSuper() ? headerFormatSuper
                    : headerFormat;
            sheet.addCell(new Label(c, 2, item.getName(), format));
            sheet.mergeCells(c, 2, c, 3);
            sheet.setColumnView(c, 25);
            c++;
        }
    }

    private void writeData() throws Exception
    {
        WritableCellFormat format = new WritableCellFormat();
        format.setWrap(true);

        for (JobImpl job : jobAttributes)
        {
            int col = 0;

            // Job Id
            sheet.addCell(new Number(col++, row, job.getId()));

            // Job Name
            sheet.addCell(new Label(col++, row, job.getName(), format));

            // Number of Languages
            sheet.addCell(new Number(col++, row, job.getWorkflows().size()));

            // Submitter
            sheet.addCell(new Label(col++, row, job.getCreateUserId(), format));

            // Project
            sheet.addCell(new Label(col++, row, job.getL10nProfile()
                    .getProject().getName(), format));

            for (AttributeItem item : attriutes)
            {
                JobAttribute jobAtt = getJobAttributeValue(item.getName(), job,
                        item.isFromSuper());

                if (jobAtt == null)
                {
                    sheet.addCell(new Label(col++, row, bundle
                            .getString("lb_na")));
                }
                else
                {
                    Object ob = jobAtt.getValue();
                    if (ob == null)
                    {
                        sheet.addCell(new Label(col++, row, ""));
                    }
                    else
                    {
                        String type = jobAtt.getType();
                        if (Attribute.TYPE_FLOAT.equals(type))
                        {
                            double value = Double.parseDouble(jobAtt
                                    .getFloatValue().toString());
                            sheet.addCell(new Number(col++, row, value));
                            if (item.isTotal())
                            {
                                addTotalCell(col, row);
                            }
                        }
                        else if (Attribute.TYPE_INTEGER.equals(type))
                        {
                            sheet.addCell(new Number(col++, row, jobAtt
                                    .getIntegerValue()));
                            if (item.isTotal())
                            {
                                addTotalCell(col, row);
                            }
                        }
                        else
                        {
                            sheet.addCell(new Label(col++, row,
                                    getAttributeValueString(jobAtt), format));
                        }
                    }
                }
            }

            row++;
        }
    }

    private void writeTotal() throws Exception
    {
        WritableFont headerFont = new WritableFont(WritableFont.ARIAL, 9,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);
        WritableCellFormat headerFormat = new WritableCellFormat(headerFont);

        Set<Integer> keys = totalCells.keySet();

        if (keys.size() > 0)
        {
            row++;
            sheet.addCell(new Label(1, row, bundle.getString("lb_total"),
                    headerFormat));

            for (Integer key : keys)
            {
                Formula f = getTotalFormula(key, totalCells.get(key));
                f.setCellFormat(headerFormat);
                sheet.addCell(f);
            }
        }
    }

    private Formula getTotalFormula(int x, List<Integer> ys)
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
        Formula formula = new Formula(x - 1, row, result.toString());
        return formula;
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
        workbook.write();
        workbook.close();
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
        init();
        addHeader();
        writeData();
        writeTotal();
        end();
    }
}