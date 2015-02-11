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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.Element;

import com.globalsight.smartbox.bo.CompanyConfiguration;
import com.globalsight.smartbox.bo.FileProfile;
import com.globalsight.smartbox.bo.JobInfo;
import com.globalsight.smartbox.util.LogUtil;
import com.globalsight.smartbox.util.WebClientHelper;
import com.globalsight.smartbox.util.XlfParser;
import com.globalsight.smartbox.util.ZipUtil;

/**
 * General use case
 * 
 * @author leon
 * 
 */
public class GeneralPreProcess implements PreProcess {
	private JobInfo jobInfo = new JobInfo();
	HashMap<String, String> sourceMap = new HashMap<String, String>();

	@Override
	public JobInfo process(String originFilePath, CompanyConfiguration cpConfig) {
		jobInfo.setOriginFile(originFilePath);
		File originFile = new File(originFilePath);
		String originFileName = originFile.getName();
		String sourceLocale = cpConfig.getSourceLocale();
		String targetLocale = cpConfig.getTargetLocale();
		File xlzDir = new File(cpConfig.getJobCreatingBox4XLZ());
		if (originFilePath.contains(xlzDir.getPath())) {
			String xlfFile = originFilePath;
			try {
				XlfParser p = new XlfParser();
				if (originFilePath.endsWith(".xlz")) {
					List<String> fileList = ZipUtil.unpackZipPackage(originFilePath, cpConfig.getTempBox());
					for (String str : fileList) {
						if (str.endsWith(".xlf")) {
							xlfFile = cpConfig.getTempBox() + File.separator + str;
							break;
						}
					}
				}
				if (xlfFile.endsWith(".xlf")) {
					File xfile = new File(xlfFile);
					Document doc = p.getDocument(xfile);
					Element xFile = p.getXFile(doc);
					sourceLocale = p.getSourceLanguage(xFile).replace("-", "_");
					targetLocale = p.getTargetLanguage(xFile).replace("-", "_");
				}
			} catch (Exception e) {
				String message = "File parser error: " + originFileName;
				LogUtil.fail(message, e);
				return null;
			}
		}

		String jobName = determineJobName(originFileName);
		if (jobName == null) {
			jobInfo.setFailedFlag(true);
			return jobInfo;
		}

		if (originFilePath.endsWith(".zip")) {
			// temp directory used for saving converted file
			String tempDirPath = originFilePath.substring(0, originFilePath.indexOf(".zip"));
			File tempDir = new File(tempDirPath);
			tempDir.mkdir();
			jobInfo.setTempFile(tempDirPath);
			jobInfo.setOriginFile(tempDirPath);
		}

		Vector<String> tls = new Vector<String>();
		Vector<String> sfs = fileHanding(originFile);
		if ((sfs == null) || (sfs.size() == 0)) {
			jobInfo.setFailedFlag(true);
			return jobInfo;
		}

		Vector<String> fps = determineFileProfileIds(sfs, sourceLocale, cpConfig.getExtension2fp());
		if ((fps == null) || (fps.size() == 0)) {
			jobInfo.setFailedFlag(true);
			return jobInfo;
		}
		for (int i = 0; i < sourceMap.entrySet().size(); i++) {
			tls.add(targetLocale);
		}

		jobInfo.setSourceMap(sourceMap);
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
		String uniqueJobName = null;
		try {
			uniqueJobName = WebClientHelper.getUniqueJobName(originFileName);
		} catch (Exception e) {
			String message = "Get unique job name failed. Web Service Exception.";
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
	private Vector<String> determineFileProfileIds(List<String> sourceFiles, String sourceLocale,
			Map<String, String> extension2fp) {
		Vector<String> fpIds = new Vector<String>();
		// Get file profile info from GS
		List<FileProfile> fileProfileInfo;
		try {
			fileProfileInfo = WebClientHelper.getFileProfileInfoFromGS();
		} catch (Exception e) {
			String message = "Get file profile info failed, Web Service Exception.";
			LogUtil.fail(message, e);
			return null;
		}

		for (String sf : sourceFiles) {
			String extension = sf.substring(sf.lastIndexOf(".") + 1);
			extension = extension.toLowerCase();
			String mapFPName = extension2fp.get(extension);
			if (mapFPName == null) {
				String message = "No file profile config for this extension(." + extension
						+ ") in GSSmartBox.cfg.xml : " + sf;
				LogUtil.FAILEDLOG.error(message);
				continue;
			}
			boolean findNoFileProfile = true;
			for (FileProfile fp : fileProfileInfo) {
				if (fp.getName().equals(mapFPName)) {
					String sl = fp.getSourceLocale();
					Set<String> extensions = fp.getFileExtensions();
					if (!sourceLocale.equals(sl)) {
						String message = "The file profile(" + mapFPName + ") does not have the source locale("
								+ sourceLocale + ") in GlobalSight Server: " + sf;
						LogUtil.FAILEDLOG.error(message);
						break;
					}
					if (!extensions.contains(extension)) {
						String message = "The file profile(" + mapFPName + ") does not have the file extension(."
								+ extension + ") in GlobalSight Server: " + sf;
						LogUtil.FAILEDLOG.error(message);
						break;
					}
					sourceMap.put(sf, fp.getId());
					fpIds.add(fp.getId());
					findNoFileProfile = false;
					break;
				}
			}
			if (findNoFileProfile) {
				String message = "No file profile found in GlobalSight Server for ." + extension + " format: " + sf;
				LogUtil.FAILEDLOG.error(message);
				continue;
			}
		}
		return fpIds;
	}

	/**
	 * File Handing
	 * 
	 * @param originFilePath
	 */
	private Vector<String> fileHanding(File originFile) {
		String fileName = originFile.getName();
		String tempDir = jobInfo.getTempFile();
		Vector<String> sourceFiles = new Vector<String>();
		if (fileName.endsWith(".zip")) {
			// Unpack zip file
			LogUtil.info("Unpack zip file: " + fileName);
			List<String> fileList = new ArrayList<String>();
			try {
				fileList = ZipUtil.unpackZipPackage(originFile.getPath(), tempDir);
			} catch (Exception e) {
				String message = "File unpack error: " + fileName;
				LogUtil.fail(message, e);
				return null;
			}

			for (String str : fileList) {
				sourceFiles.add(tempDir + File.separator + str);
			}

			if (sourceFiles.size() == 0) {
				String message = "File unpack error, no files in zip file: " + fileName;
				LogUtil.FAILEDLOG.error(message);
				return null;
			}
			originFile.delete();
		} else {
			sourceFiles.add(originFile.getPath());
		}
		return sourceFiles;
	}

}
