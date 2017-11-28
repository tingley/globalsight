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
import java.util.ResourceBundle;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

public class DQFInfoReport {

	/**
	 * To Generate DQF information sheet with static data. This sheet will use
	 * in RCR and TER Reports.
	 * 
	 * @param workbook
	 * @param sheet
	 * @param r_bundle
	 *  To read the message from properties file.
	 */
	public static void generateDQFInfoSheet(Workbook workbook, Sheet sheet,
			ResourceBundle r_bundle) 
	{
		if (workbook == null || sheet == null || r_bundle == null)
		{
			return;
		}
		int rowLine = 0;
		Row row = null;
		Cell cell = null;
		ReportStyle reportStyle = new ReportStyle(workbook);
		sheet.setZoom(4, 5);
		sheet.setColumnWidth(0, 9500);
		sheet.setColumnWidth(1, 10000);
		sheet.setColumnWidth(2, 31000);
		sheet.setVerticallyCenter(true);

		// font type and height for text
		Font font = workbook.createFont();
		font.setFontName("Calibri");
		font.setFontHeightInPoints((short) 10);

		// font type and height for headers
		Font headerFont = workbook.createFont();
		headerFont.setFontName("Calibri");
		headerFont.setFontHeightInPoints((short) 12);
		headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

		// font type and height for bold content
		Font boldContentFont = workbook.createFont();
		boldContentFont.setFontName("Calibri");
		boldContentFont.setFontHeightInPoints((short) 10);
		boldContentFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

		// Scoring font style
		XSSFFont scoringFont = (XSSFFont) workbook.createFont();
		scoringFont.setFontHeightInPoints((short) 12);
		scoringFont.setColor(new XSSFColor(new Color(117, 113, 113)));
		scoringFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

		// font type of scroing values
		Font scoringContentfont = workbook.createFont();
		scoringContentfont.setFontName("Calibri");
		scoringContentfont.setFontHeightInPoints((short) 11);

		// font type of scroing values
		Font scoringBoldContentfont = workbook.createFont();
		scoringBoldContentfont.setFontName("Calibri");
		scoringBoldContentfont.setFontHeightInPoints((short) 11);
		scoringBoldContentfont.setBoldweight(Font.BOLDWEIGHT_BOLD);

		// merge the rows and columns
		sheet.addMergedRegion(CellRangeAddress.valueOf("A2:A7"));
		sheet.addMergedRegion(CellRangeAddress.valueOf("A9:A10"));
		sheet.addMergedRegion(CellRangeAddress.valueOf("A12:A13"));
		sheet.addMergedRegion(CellRangeAddress.valueOf("A25:B25"));
		sheet.addMergedRegion(CellRangeAddress.valueOf("A26:B26"));
		sheet.addMergedRegion(CellRangeAddress.valueOf("A31:B31"));
		sheet.addMergedRegion(CellRangeAddress.valueOf("A32:B32"));

		// merging B and C columns until 23 rows
		for (int mergeColIdx = 1; mergeColIdx <= 23; mergeColIdx++)
		{
			sheet.addMergedRegion(CellRangeAddress.valueOf("B" + mergeColIdx
					+ ":C" + mergeColIdx));
		}

		// Error Categories
		row = ExcelUtil.getRow(sheet, rowLine);
		CellStyle cellHeaderStyle = reportStyle.getCellHeaderStyle(headerFont,
				new XSSFColor(new Color(229, 79, 5)),
				CellStyle.VERTICAL_CENTER, (short) 0, true, true);

		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue(r_bundle.getString("dqf_ec_title"));
		cell.setCellStyle(cellHeaderStyle);

		cell = ExcelUtil.getCell(row, 1);
		cell.setCellValue(r_bundle.getString("dqf_info_descripton"));
		cell.setCellStyle(cellHeaderStyle);
		cell = row.createCell(2);
		cell.setCellStyle(cellHeaderStyle);
		rowLine++;

		row = ExcelUtil.getRow(sheet, rowLine);
		CellStyle cellContentStyle = reportStyle.getCellContentStyle(font,
				(short) 0, CellStyle.VERTICAL_CENTER, (short) 0, true, true);
		CellStyle cellBoldContentStyle = reportStyle.getCellContentStyle(
				boldContentFont, (short) 0, CellStyle.VERTICAL_CENTER,
				(short) 0, true, true);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue(r_bundle.getString("dqf_ec_accuracy_type"));
		cell.setCellStyle(cellBoldContentStyle);

		String[] descriptions = r_bundle.getString(
				"dqf_ec_accuracy_descriptions").split("(?<=;)");
		for (String description : descriptions)
		{
			cell = ExcelUtil.getCell(row, 1);
			cell.setCellValue(description);
			cell.setCellStyle(cellContentStyle);
			cell = row.createCell(2);
			cell.setCellStyle(cellContentStyle);
			rowLine++;
			row = ExcelUtil.getRow(sheet, rowLine);
		}
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue(r_bundle.getString("dqf_ec_language_type"));
		cell.setCellStyle(cellBoldContentStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellValue(r_bundle.getString("dqf_ec_language_description"));
		cell.setCellStyle(cellContentStyle);
		cell = row.createCell(2);
		cell.setCellStyle(cellContentStyle);
		rowLine++;

		row = ExcelUtil.getRow(sheet, rowLine);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue(r_bundle.getString("dqf_ec_terminology_type"));
		cell.setCellStyle(cellBoldContentStyle);
		descriptions = r_bundle.getString("dqf_ec_terminology_descriptions")
				.split("(?<=;)");
		for (String description : descriptions)
		{
			cell = ExcelUtil.getCell(row, 1);
			cell.setCellValue(description);
			cell.setCellStyle(cellContentStyle);
			cell = row.createCell(2);
			cell.setCellStyle(cellContentStyle);
			rowLine++;
			row = ExcelUtil.getRow(sheet, rowLine);
		}

		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue(r_bundle.getString("dqf_ec_cs_type"));
		cell.setCellStyle(cellBoldContentStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellValue(r_bundle.getString("dqf_ec_cs_description"));
		cell.setCellStyle(cellContentStyle);
		cell = row.createCell(2);
		cell.setCellStyle(cellContentStyle);
		rowLine++;

		row = ExcelUtil.getRow(sheet, rowLine);
		row.setHeight((short) 300);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue(r_bundle.getString("dqf_ec_atog_type"));
		cell.setCellStyle(cellBoldContentStyle);
		descriptions = r_bundle.getString("dqf_ec_atog_descriptions")
				.split(";");
		for (String description : descriptions)
		{
			cell = ExcelUtil.getCell(row, 1);
			cell.setCellValue(description);
			cell.setCellStyle(cellContentStyle);
			cell = row.createCell(2);
			cell.setCellStyle(cellContentStyle);
			rowLine++;
			if (rowLine == 13)
			{
				row.setHeight((short) 300);
			}
			row = ExcelUtil.getRow(sheet, rowLine);
		}

		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue(r_bundle.getString("dqf_ec_style_type"));
		cell.setCellStyle(cellBoldContentStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellValue(r_bundle.getString("dqf_ec_style_description"));
		cell.setCellStyle(cellContentStyle);
		cell = row.createCell(2);
		cell.setCellStyle(cellContentStyle);
		rowLine++;

		row = ExcelUtil.getRow(sheet, rowLine);
		row.setHeight((short) 500);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue(r_bundle.getString("dqf_ec_ca_type"));
		cell.setCellStyle(cellBoldContentStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellValue(r_bundle.getString("dqf_ec_ca_description"));
		cell.setCellStyle(cellContentStyle);
		cell = row.createCell(2);
		cell.setCellStyle(cellContentStyle);
		rowLine++;

		row = ExcelUtil.getRow(sheet, rowLine);

		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue(r_bundle.getString("dqf_ec_ms_type"));
		cell.setCellStyle(cellBoldContentStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellValue(r_bundle.getString("dqf_ec_ms_description"));
		cell.setCellStyle(cellContentStyle);
		cell = row.createCell(2);
		cell.setCellStyle(cellContentStyle);
		rowLine++;

		// Error Severities Heading

		row = ExcelUtil.getRow(sheet, rowLine);
		cellHeaderStyle = reportStyle.getCellHeaderStyle(headerFont,
				new XSSFColor(new Color(79, 129, 189)),
				CellStyle.VERTICAL_CENTER, (short) 0, true, true);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue(r_bundle.getString("dqf_es_title"));
		cell.setCellStyle(cellHeaderStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellValue(r_bundle.getString("dqf_info_descripton"));
		cell.setCellStyle(cellHeaderStyle);
		cell = row.createCell(2);
		cell.setCellStyle(cellHeaderStyle);
		rowLine++;

		row = ExcelUtil.getRow(sheet, rowLine);
		row.setHeight((short) 600);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue(r_bundle.getString("dqf_es_critical_type"));
		cell.setCellStyle(cellBoldContentStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellValue(r_bundle.getString("dqf_es_critical_description"));
		cell.setCellStyle(cellContentStyle);
		cell = row.createCell(2);
		cell.setCellStyle(cellContentStyle);
		rowLine++;

		row = ExcelUtil.getRow(sheet, rowLine);
		row.setHeight((short) 600);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue(r_bundle.getString("dqf_es_major_type"));
		cell.setCellStyle(cellBoldContentStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellValue(r_bundle.getString("dqf_es_major_description"));
		cell.setCellStyle(cellContentStyle);
		cell = row.createCell(2);
		cell.setCellStyle(cellContentStyle);
		rowLine++;

		row = ExcelUtil.getRow(sheet, rowLine);
		row.setHeight((short) 600);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue(r_bundle.getString("dqf_es_minor_type"));
		cell.setCellStyle(cellBoldContentStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellStyle(cellContentStyle);
		cell.setCellValue(r_bundle.getString("dqf_es_minor_description"));
		cell = row.createCell(2);
		cell.setCellStyle(cellContentStyle);
		rowLine++;

		row = ExcelUtil.getRow(sheet, rowLine);
		row.setHeight((short) 600);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue(r_bundle.getString("dqf_es_neutral_type"));
		cell.setCellStyle(cellBoldContentStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellValue(r_bundle.getString("dqf_es_neutral_description"));
		cell.setCellStyle(cellContentStyle);
		cell = row.createCell(2);
		cell.setCellStyle(cellContentStyle);
		rowLine++;

		row = ExcelUtil.getRow(sheet, rowLine);
		row.setHeight((short) 700);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue(r_bundle.getString("dqf_es_positive_type"));
		cell.setCellStyle(cellBoldContentStyle);
		cell = ExcelUtil.getCell(row, 1);

		Font boldText = workbook.createFont();
		boldText.setFontName("Calibri");
		boldText.setFontHeightInPoints((short) 10);
		boldText.setBoldweight(Font.BOLDWEIGHT_BOLD);

		RichTextString positiveDescription = new XSSFRichTextString(
				r_bundle.getString("dqf_es_positive_description"));
		positiveDescription.applyFont(0, 21, font);
		positiveDescription.applyFont(22, 34, boldText);
		positiveDescription.applyFont(35, 166, font);
		positiveDescription.applyFont(167, 367, boldText);
		cell.setCellValue(positiveDescription);
		cell.setCellStyle(cellContentStyle);
		cell = row.createCell(2);
		cell.setCellStyle(cellContentStyle);
		rowLine++;

		row = ExcelUtil.getRow(sheet, rowLine);
		row.setHeight((short) 600);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue(r_bundle.getString("dqf_es_invalid_type"));
		cell.setCellStyle(cellBoldContentStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellValue(r_bundle.getString("dqf_es_invalid_description"));
		cell.setCellStyle(cellContentStyle);
		cell = row.createCell(2);
		cell.setCellStyle(cellContentStyle);
		rowLine++;
		rowLine++;

		// Fluency Heading
		row = ExcelUtil.getRow(sheet, rowLine);
		row.setHeight((short) 650);

		cellHeaderStyle = reportStyle.getCellHeaderStyle(headerFont,
				new XSSFColor(new Color(255, 153, 51)),
				CellStyle.VERTICAL_CENTER, CellStyle.ALIGN_CENTER, true, true);

		CellStyle cellDescriptionStyle = reportStyle.getCellHeaderStyle(
				headerFont, new XSSFColor(new Color(255, 153, 51)),
				CellStyle.VERTICAL_CENTER, CellStyle.ALIGN_LEFT, true, true);

		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue(r_bundle.getString("dqf_fluency_title"));
		cell.setCellStyle(cellHeaderStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellStyle(cellHeaderStyle);
		cell = ExcelUtil.getCell(row, 2);
		cell.setCellValue(r_bundle.getString("dqf_fluency_question"));
		cell.setCellStyle(cellDescriptionStyle);
		rowLine++;

		CellStyle cellNumStyle = reportStyle.getCellContentStyle(
				scoringBoldContentfont, (short) 0, CellStyle.VERTICAL_CENTER,
				CellStyle.ALIGN_CENTER, true, true);

		CellStyle cellScoringStyle = reportStyle.getCellHeaderStyle(
				scoringFont, new XSSFColor(new Color(255, 153, 51)),
				CellStyle.VERTICAL_CENTER, CellStyle.ALIGN_CENTER, true, true);

		CellStyle cellScoringDescStyle = reportStyle.getCellHeaderStyle(
				scoringFont, new XSSFColor(new Color(255, 153, 51)),
				CellStyle.VERTICAL_CENTER, CellStyle.ALIGN_LEFT, true, true);

		CellStyle cellScoringContentcStyle = reportStyle.getCellContentStyle(
				scoringContentfont, (short) 0, CellStyle.VERTICAL_CENTER,
				CellStyle.ALIGN_LEFT, true, true);

		CellStyle cellScoringBoldContentcStyle = reportStyle
				.getCellContentStyle(scoringBoldContentfont, (short) 0,
						CellStyle.VERTICAL_CENTER, CellStyle.ALIGN_LEFT, true,
						true);

		row = ExcelUtil.getRow(sheet, rowLine);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue(r_bundle.getString("dqf_scoring_title"));
		cell.setCellStyle(cellScoringStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellStyle(cellScoringStyle);
		cell = ExcelUtil.getCell(row, 2);
		cell.setCellValue(r_bundle.getString("dqf_info_descripton"));
		cell.setCellStyle(cellScoringDescStyle);
		rowLine++;

		row = ExcelUtil.getRow(sheet, rowLine);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue("1");
		cell.setCellStyle(cellNumStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellValue(r_bundle
				.getString("dqf_scoring_incomprehensible_title"));
		cell.setCellStyle(cellScoringBoldContentcStyle);
		cell = ExcelUtil.getCell(row, 2);
		cell.setCellValue(r_bundle
				.getString("dqf_scoring_incomprehensible_descripton"));
		cell.setCellStyle(cellScoringContentcStyle);
		rowLine++;

		row = ExcelUtil.getRow(sheet, rowLine);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue("2");
		cell.setCellStyle(cellNumStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellValue(r_bundle.getString("dqf_scoring_disfluent_title"));
		cell.setCellStyle(cellScoringBoldContentcStyle);
		cell = ExcelUtil.getCell(row, 2);
		cell.setCellValue(r_bundle
				.getString("dqf_scoring_disfluent_descripton"));
		cell.setCellStyle(cellScoringContentcStyle);
		rowLine++;

		row = ExcelUtil.getRow(sheet, rowLine);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue("3");
		cell.setCellStyle(cellNumStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellValue(r_bundle.getString("dqf_scoring_good_title"));
		cell.setCellStyle(cellScoringBoldContentcStyle);
		cell = ExcelUtil.getCell(row, 2);
		cell.setCellValue(r_bundle.getString("dqf_scoring_good_descripton"));
		cell.setCellStyle(cellScoringContentcStyle);
		rowLine++;

		row = ExcelUtil.getRow(sheet, rowLine);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue("4");
		cell.setCellStyle(cellNumStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellValue(r_bundle.getString("dqf_scoring_flawless_title"));
		cell.setCellStyle(cellScoringBoldContentcStyle);
		cell = ExcelUtil.getCell(row, 2);
		cell.setCellValue(r_bundle.getString("dqf_scoring_flawless_descripton"));
		cell.setCellStyle(cellScoringContentcStyle);
		rowLine++;

		// Adequacy Heading

		row = ExcelUtil.getRow(sheet, rowLine);
		row.setHeight((short) 500);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue(r_bundle.getString("dqf_adequacy_title"));
		cell.setCellStyle(cellHeaderStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellStyle(cellHeaderStyle);
		cell = ExcelUtil.getCell(row, 2);
		cell.setCellValue(r_bundle.getString("dqf_adequacy_question"));
		cell.setCellStyle(cellDescriptionStyle);
		rowLine++;

		row = ExcelUtil.getRow(sheet, rowLine);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue(r_bundle.getString("dqf_scoring_title"));
		cell.setCellStyle(cellScoringStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellStyle(cellScoringStyle);
		cell = ExcelUtil.getCell(row, 2);
		cell.setCellValue(r_bundle.getString("dqf_info_descripton"));
		cell.setCellStyle(cellScoringDescStyle);
		rowLine++;

		row = ExcelUtil.getRow(sheet, rowLine);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue("1");
		cell.setCellStyle(cellNumStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellValue(r_bundle.getString("dqf_adequacy_none_title"));
		cell.setCellStyle(cellScoringBoldContentcStyle);
		cell = ExcelUtil.getCell(row, 2);
		cell.setCellValue(StringUtil.isNotEmpty(r_bundle
				.getString("dqf_adequacy_none_descripton")) ? r_bundle
				.getString("dqf_adequacy_none_descripton") : "");
		cell.setCellStyle(cellScoringContentcStyle);
		rowLine++;

		row = ExcelUtil.getRow(sheet, rowLine);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue("2");
		cell.setCellStyle(cellNumStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellValue(r_bundle.getString("dqf_adequacy_litle_title"));
		cell.setCellStyle(cellScoringBoldContentcStyle);
		cell = ExcelUtil.getCell(row, 2);
		cell.setCellValue(StringUtil.isNotEmpty(r_bundle
				.getString("dqf_adequacy_litle_descripton")) ? r_bundle
				.getString("dqf_adequacy_litle_descripton") : "");
		cell.setCellStyle(cellScoringContentcStyle);
		rowLine++;

		row = ExcelUtil.getRow(sheet, rowLine);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue("3");
		cell.setCellStyle(cellNumStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellValue(r_bundle.getString("dqf_adequacy_most_title"));
		cell.setCellStyle(cellScoringBoldContentcStyle);
		cell = ExcelUtil.getCell(row, 2);
		cell.setCellValue(StringUtil.isNotEmpty(r_bundle
				.getString("dqf_adequacy_most_descripton")) ? r_bundle
				.getString("dqf_adequacy_most_descripton") : "");
		cell.setCellStyle(cellScoringContentcStyle);
		rowLine++;

		row = ExcelUtil.getRow(sheet, rowLine);
		cell = ExcelUtil.getCell(row, 0);
		cell.setCellValue("4");
		cell.setCellStyle(cellNumStyle);
		cell = ExcelUtil.getCell(row, 1);
		cell.setCellValue(r_bundle.getString("dqf_adequacy_everything_title"));
		cell.setCellStyle(cellScoringBoldContentcStyle);
		cell = ExcelUtil.getCell(row, 2);
		cell.setCellValue(StringUtil.isNotEmpty(r_bundle
				.getString("dqf_adequacy_everything_descripton")) ? r_bundle
				.getString("dqf_adequacy_everything_descripton") : "");
		cell.setCellStyle(cellScoringContentcStyle);
	}

}
