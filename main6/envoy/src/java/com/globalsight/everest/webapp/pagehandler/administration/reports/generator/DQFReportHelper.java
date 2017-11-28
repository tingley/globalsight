package com.globalsight.everest.webapp.pagehandler.administration.reports.generator;

import com.globalsight.everest.workflow.DQFData;
import com.globalsight.util.ExcelUtil;
import com.globalsight.util.ReportStyle;
import com.globalsight.util.StringUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ResourceBundle;

/**
 * Helper class used to DQF function in reports.
 * DQF is shown just in RCR/TER report for now.
 * Created by Administrator on 2016/11/16.
 */
public class DQFReportHelper
{
    public static void writeDQFInfo(boolean needProtect, boolean isDQFEnabled, DQFData dqfData, Sheet sheet, int startRow, ResourceBundle bundle)
    {
        if (!isDQFEnabled || dqfData == null || sheet == null || sheet.getWorkbook() == null || startRow < 1 || bundle == null)
            return;

    //  boolean isStored = StringUtil.isNotEmpty(dqfData.getFluency());
        ReportStyle reportStyle = new ReportStyle(sheet.getWorkbook());

        // DQF enabled
        int row = startRow;
        Row rowLine = ExcelUtil.getRow(sheet, row);
        Cell cell = ExcelUtil.getCell(rowLine, 0);
        cell.setCellValue(bundle.getString("lb_dqf_fluency_only"));
        cell.setCellStyle(reportStyle.getHeaderStyle());

        cell = ExcelUtil.getCell(rowLine, 1);
        if (needProtect)
        {
            cell.setCellStyle(reportStyle.getLockedStyle());
        }
        else
        {
            cell.setCellStyle(reportStyle.getUnlockedStyle());
        }
        cell.setCellValue(dqfData.getFluency());

        row++;

        rowLine = ExcelUtil.getRow(sheet, row);
        cell = ExcelUtil.getCell(rowLine, 0);
        cell.setCellValue(bundle.getString("lb_dqf_adequacy_only"));
        cell.setCellStyle(reportStyle.getHeaderStyle());
        cell = ExcelUtil.getCell(rowLine, 1);
        if (needProtect)
        {
            cell.setCellStyle(reportStyle.getLockedStyle());
        }
        else
        {
            cell.setCellStyle(reportStyle.getUnlockedStyle());
        }
        cell.setCellValue(dqfData.getAdequacy());
        row++;

        rowLine = ExcelUtil.getRow(sheet, row);
        cell = ExcelUtil.getCell(rowLine, 0);
        cell.setCellValue(bundle.getString("lb_comment"));
        cell.setCellStyle(reportStyle.getHeaderStyle());
        cell = ExcelUtil.getCell(rowLine, 1);
        if (needProtect)
        {
            cell.setCellStyle(reportStyle.getLockedStyle());
        }
        else
        {
            cell.setCellStyle(reportStyle.getUnlockedStyle());
        }
        cell.setCellValue(dqfData.getComment());
    }
}
