/**
 *  Copyright 2009, 2011 Welocalize, Inc. 
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
package com.globalsight.smartbox.bussiness.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.stream.XMLStreamException;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;

import com.globalsight.smartbox.bo.CompanyConfiguration;
import com.globalsight.smartbox.bo.FileProfile;
import com.globalsight.smartbox.bo.JobInfo;
import com.globalsight.smartbox.util.FileUtil;
import com.globalsight.smartbox.util.LogUtil;
import com.globalsight.smartbox.util.WebClientHelper;

/**
 * Use case 02 pre process
 * 
 * One file<->one job, One file<-> one source locale
 * 
 * @author leon
 * 
 */
public class Usecase02PreProcess implements PreProcess {
	private JobInfo jobInfo = new JobInfo();
	private String sourceLocale;

	@Override
	public JobInfo process(String originFilePath, CompanyConfiguration cpConfig) {

		jobInfo.setOriginFile(originFilePath);
		File originFile = new File(originFilePath);
		String fileName = originFile.getName();

		// validate file format
		String format = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
		format = format.toLowerCase();
		if (!("csv".equals(format) || "txt".equals(format))) {
			LogUtil.FAILEDLOG.error("File format error: should be csv or txt file");
			jobInfo.setFailedFlag(true);
			return jobInfo;
		}

		// temp directory used for saving converted file
		String tempDirPath = originFilePath.substring(0, originFilePath.lastIndexOf("."));
		File tempDir = new File(tempDirPath);
		tempDir.mkdir();
		jobInfo.setTempFile(tempDirPath);

		// Validate original file
		String sourceFile = fileHanding(originFile);
		if (sourceFile == null) {
			jobInfo.setFailedFlag(true);
			return jobInfo;
		}

		String originFileName = originFile.getName();
		String jobName = determineJobName(originFileName);
		if (jobName == null) {
			jobInfo.setFailedFlag(true);
			return jobInfo;
		}

		sourceLocale = getLocale(sourceLocale);
		// Target locale is en_US for all the files
		String targetLocale = "en_US";
		String fileProfileId = determineFileProfileId(sourceFile, sourceLocale, cpConfig.getExtension2fp());
		if (fileProfileId == null) {
			jobInfo.setFailedFlag(true);
			return jobInfo;
		}

		Vector<String> sfs = new Vector<String>();
		Vector<String> tls = new Vector<String>();
		Vector<String> fps = new Vector<String>();
		sfs.add(sourceFile);
		tls.add(targetLocale);
		fps.add(fileProfileId);

		jobInfo.setJobName(jobName);
		jobInfo.setSourceFiles(sfs);
		jobInfo.setTargetLocales(tls);
		jobInfo.setFileProfileIds(fps);
		jobInfo.setOtherInfo("infomation");

		return jobInfo;
	}

	/**
	 * Use fileName as jobName
	 * 
	 * @param originFileName
	 * @return
	 */
	private String determineJobName(String originFileName) {
		String jobName = originFileName.substring(0, originFileName.lastIndexOf("."));
		String uniqueJobName = null;
		try {
			uniqueJobName = WebClientHelper.getUniqueJobName(jobName);
		} catch (Exception e) {
			String message = "Get unique job name failed, Web Service Exception.";
			LogUtil.fail(message, e);
		}
		return uniqueJobName;
	}

	/**
	 * Determine file profile ids
	 * 
	 * @param sourceFiles
	 * @param sourceLocale
	 * @return
	 */
	private String determineFileProfileId(String sourceFile, String sourceLocale, Map<String, String> extension2fp) {
		String fpId = null;
		// Get file profile info from GS
		List<FileProfile> fileProfileInfo;
		try {
			fileProfileInfo = WebClientHelper.getFileProfileInfoFromGS();
		} catch (Exception e) {
			String message = "Get file profile info failed, Web Service Exception.";
			LogUtil.fail(message, e);
			return null;
		}

		String extension = sourceFile.substring(sourceFile.lastIndexOf(".") + 1);
		extension = extension.toLowerCase();
		String mapFPName = extension2fp.get(extension);
		if (mapFPName == null) {
			String message = "No file profile config for this extension(" + extension + ") in GSSmartBox.cfg.xml : "
					+ sourceFile;
			LogUtil.FAILEDLOG.error(message);
			return null;
		}
		boolean findNoFileProfile = true;
		for (FileProfile fp : fileProfileInfo) {
			if (fp.getName().equals(mapFPName)) {
				String sl = fp.getSourceLocale();
				Set<String> extensions = fp.getFileExtensions();
				if (!sourceLocale.equals(sl)) {
					String message = "The file profile(" + mapFPName + ") does not have the source locale("
							+ sourceLocale + ") in GlobalSight Server: " + sourceFile;
					LogUtil.FAILEDLOG.error(message);
					return null;
				}
				if (!extensions.contains(extension)) {
					String message = "The file profile(" + mapFPName + ") does not have the file extension("
							+ extension + ") in GlobalSight Server: " + sourceFile;
					LogUtil.FAILEDLOG.error(message);
					return null;
				}
				fpId = fp.getId();
				findNoFileProfile = false;
				break;
			}
		}
		if (findNoFileProfile) {
			String message = "No file profile found in GlobalSight Server for " + extension + " format: " + sourceFile;
			LogUtil.FAILEDLOG.error(message);
			return null;
		}
		return fpId;
	}

	/**
	 * Get locale of GS, en-us -> en_US
	 * 
	 * @param originLocale
	 * @return
	 */
	private String getLocale(String originLocale) {
		String[] str = originLocale.split("_");
		String localeCode = str[0];
		String country = str[1];
		String gsLocale = localeCode + "_" + country.toUpperCase();
		return gsLocale;
	}

	/**
	 * File Handing and Validate
	 * 
	 * @param originFilePath
	 */
	private String fileHanding(File originFile) {
		String fileName = originFile.getName();

		String format = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
		format = format.toLowerCase();

		// Convert to xml file
		LogUtil.info("Converting to XML: " + fileName);
		String xmlFile = convertToXML(format, originFile);
		return xmlFile;
	}

	/**
	 * Convert csv/txt file to xml file
	 * 
	 * @param format
	 * @param originFile
	 * @return
	 */
	private String convertToXML(String format, File originFile) {
		String fileName = originFile.getName();
		// Save the converted file to temp directory
		String xmlFilePath = jobInfo.getTempFile() + File.separator + fileName.substring(0, fileName.lastIndexOf("."))
				+ ".xml";
		File xmlFile = new File(xmlFilePath);
		FileReader fr = null;
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		XMLWriter output = null;
		try {
			String encoding = FileUtil.guessEncoding(originFile);

			Document document = DocumentHelper.createDocument();
			Element aElement = document.addElement("root");
			aElement.addAttribute("BomInfo", encoding == null ? "" : encoding);

			if (encoding == null) {
				fr = new FileReader(originFile);
				br = new BufferedReader(fr);
			} else {
				fis = new FileInputStream(originFile);
				isr = new InputStreamReader(fis, encoding);
				br = new BufferedReader(isr);
			}

			String str;
			if ("csv".equals(format)) {
				while ((str = br.readLine()) != null) {
					String[] values = str.split("\",\"");
					values[0] = values[0].substring(1);
					values[10] = values[10].substring(0, values[10].length() - 1);
					writeRow(aElement, values);
				}
			} else {
				while ((str = br.readLine()) != null) {
					str = str + "*";
					String[] values = str.split("\\|");
					values[10] = values[10].substring(0, values[10].lastIndexOf("*"));
					writeRow(aElement, values);
				}
			}

			output = new XMLWriter(new FileOutputStream(xmlFile));
			output.write(document);
		} catch (Exception e) {
			String message = "Failed to convert to XML, File Name: " + originFile.getName();
			LogUtil.fail(message, e);
			return null;
		} finally {
			try {
				if (output != null) {
					output.close();
				}
				if (br != null) {
					br.close();
				}
				if (isr != null) {
					isr.close();
				}
				if (fis != null) {
					fis.close();
				}
				if (fr != null) {
					fr.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return xmlFile.getPath();
	}

	private void writeRow(Element w, String[] values) throws XMLStreamException {
		/*
		 * Current colum definitions SID, Source Locale Name, Source Locale
		 * Code, (Unknown), Translation Source(eg, Machine Translation), Target
		 * Locale, Creation date, (Source Segment), (Source Segment), (Source
		 * Segment), (Source Segment)
		 */
		sourceLocale = values[2];
		Element row = w.addElement("row");
		Element sid = row.addElement("sid");
		sid.setText(values[0]);

		Element sourceLocaleName = row.addElement("sourceLocaleName");
		sourceLocaleName.setText(values[1]);
		Element sourceLocaleCode = row.addElement("sourceLocaleCode");
		sourceLocaleCode.setText(values[2]);
		Element unknown = row.addElement("unknown");
		unknown.setText(values[3]);
		Element translationSource = row.addElement("translationSource");
		translationSource.setText(values[4]);
		Element targetLocale = row.addElement("targetLocale");
		targetLocale.setText(values[5]);
		Element creationDate = row.addElement("creationDate");
		creationDate.setText(values[6]);
		Element segment1 = row.addElement("segment");
		segment1.setText(values[7]);
		Element segment2 = row.addElement("segment");
		segment2.setText(values[8]);
		Element segment3 = row.addElement("segment");
		segment3.setText(values[9]);
		Element segment4 = row.addElement("segment");
		segment4.setText(values[10]);
	}
}
