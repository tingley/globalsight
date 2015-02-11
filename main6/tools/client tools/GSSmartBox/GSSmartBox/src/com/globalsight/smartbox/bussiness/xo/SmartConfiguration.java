package com.globalsight.smartbox.bussiness.xo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.axis.utils.StringUtils;

import com.globalsight.smartbox.bo.CompanyConfiguration;
import com.globalsight.smartbox.bo.FTPConfiguration;
import com.globalsight.smartbox.bo.SMBConfiguration;
import com.globalsight.smartbox.bussiness.config.ConfigConstants;
import com.globalsight.smartbox.util.LogUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.mapper.DefaultMapper;

public class SmartConfiguration {
	private final String CONFIGFILEPATH = System.getProperty("user.dir") + File.separator + ConfigConstants.CONFIG_XML;
	private static XStream xStream;
	private static String basePath;
	private String customerPaths = "Inbox,Inbox4XLZ,Outbox,JobCreatingBox,JobCreatingBox4XLZ,JobCreateSuccessfulBox,FailedBox,TempBox";
	private final String smbPaths = "SMBInbox,SMBOutbox,SMBFailedbox";
	private SmartValidator validator = new SmartValidator();
	static {
		xStream = new XStream(new DomDriver());
		xStream.registerConverter(new Extension2FpConverter(new DefaultMapper(XStream.class.getClassLoader())));
		xStream.alias("CompanyConfiguration", CompanyConfiguration.class);
		xStream.alias("FTPConfiguration", FTPConfiguration.class);
		xStream.alias("SMBConfiguration", SMBConfiguration.class);
		xStream.alias("extension2fp", HashMap.class);
		xStream.alias("property", Entry.class);
	}

	public CompanyConfiguration init() {
		CompanyConfiguration cc = new CompanyConfiguration();
		// xml to bean
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(CONFIGFILEPATH));
			String xml = validator.validateXml(in);
			cc = (CompanyConfiguration) xStream.fromXML(xml);
			basePath = cc.getBaseDir();
			if (!validator.validatePath(basePath)) {
				String message = "Configuration error for baseDir in GSSmartBox.cfg.xml.";
				LogUtil.FAILEDLOG.error(message);
				return null;
			}
		} catch (FileNotFoundException e) {
			String message = "Can not find configuration file--GSSmartBox.cfg.xml";
			LogUtil.fail(message, e);
		} catch (IOException e) {
			String message = "Can not read configuration file--GSSmartBox.cfg.xml";
			LogUtil.fail(message, e);
		}
		// validate java bean
		try {
			return validate(cc);
			/*
			 * String tmpStr = xStream.toXML(cc);
			 * LogUtil.info(tmpStr);
			 * return cc;
			 */
		} catch (Exception e) {
			String message = "Can not maper the field in class for GSSmartBox.cfg.xml";
			LogUtil.fail(message, e);
			return null;
		}

	}

	public CompanyConfiguration validate(CompanyConfiguration cc) throws ClassNotFoundException, Exception {

		// webservice
		if ((null == cc.getJobCreatingBox4XLZ()) || StringUtils.isEmpty(cc.getJobCreatingBox4XLZ().trim())) {
			customerPaths = customerPaths.replace("Inbox4XLZ,", ",");
			customerPaths = customerPaths.replace("JobCreatingBox4XLZ,", ",");
		} else {
			// it needed to be put in creating box must at first
			cc.setJobCreatingBox4XLZ(cc.getJobCreatingBox() + File.separator + cc.getJobCreatingBox4XLZ());
			// make the config bean is in same of old
		}

		conver2SysPath(cc, customerPaths, null);
		FTPConfiguration ftpConfiguration = cc.getFtpConfig();
		SMBConfiguration smbConfiguration = cc.getSmbConfig();
		if (ftpConfiguration.getUseFTP() && smbConfiguration.getUseSMB()) {
			// Can not use FTP and SMB together
			String message = "Can not use FTP and SMB together.";
			LogUtil.FAILEDLOG.error(message);
			return null;
		}
		if (ftpConfiguration.getUseFTP()) {
			if (validator.validateFTP(ftpConfiguration)) {
				createDir4Romte(cc, "ftp");
			} else {
				String message = "Can not use FTP, please check the param of ftp.";
				LogUtil.FAILEDLOG.error(message);
				return null;
			}
		} else {
			cc.setFtpConfig(null);
		}

		if (smbConfiguration.getUseSMB()) {
			String smbHost = smbConfiguration.getSMBServerHost();
			String smbUsername = smbConfiguration.getSMBUsername();
			String smbPassword = smbConfiguration.getSMBPassword();
			String prefix = "smb://";
			if ("".equals(smbUsername.trim())) {
				// no user access
				prefix = "smb://" + smbHost;
			} else {
				// user access
				prefix = "smb://" + smbUsername + ":" + smbPassword + "@" + smbHost;
			}
			conver2SysPath(smbConfiguration, smbPaths, prefix);
			if (validator.validateSMB(smbConfiguration)) {
				createDir4Romte(cc, "smb");
			} else {
				String message = "Can not use SMB, please check the param of SMB.";
				LogUtil.FAILEDLOG.error(message);
				return null;
			}
		} else {
			cc.setSmbConfig(null);
		}

		makefailedBoxIO(cc.getFailedBox());

		if (!validator.validateWS(cc)) {
			return null;
		}
		/*
		 * String preProcessClass = "com.globalsight.smartbox.bussiness.process." + cc.getPreProcessClass() +
		 * "PreProcess";
		 * String postProcessClass = "com.globalsight.smartbox.bussiness.process." + cc.getProcessCase() +
		 * "PostProcess";
		 * cc.setPreProcessClass(preProcessClass);
		 * cc.setPostProcessClass(postProcessClass);
		 */
		cc.setFileCheckToCreateJobTime(cc.getFileCheckToCreateJobTime() * 1000);
		cc.setDownloadCheckTime(cc.getDownloadCheckTime() * 1000);

		return cc;

		// smbConfiguration.setSMBInbox(prefix + smbConfiguration.getSMBInbox());

	}

	private void makefailedBoxIO(String failedBox) {
		// Create Import and Export folder under failed box in local
		String failedBoxImport = failedBox + File.separator + "Import";
		File failedBoxImportDir = new File(failedBoxImport);
		failedBoxImportDir.mkdir();
		String failedBoxExport = failedBox + File.separator + "Export";
		File failedBoxExportDir = new File(failedBoxExport);
		failedBoxExportDir.mkdir();
	}

	private void createDir4Romte(CompanyConfiguration cc, String remote) {
		String failedBox = cc.getFailedBox(), outbox = cc.getOutbox();
		// Create FTP directory for failedBox and outBox
		failedBox = failedBox + File.separator + remote;
		File failedBoxDir = new File(failedBox);
		failedBoxDir.mkdir();
		outbox = outbox + File.separator + remote;
		File outboxDir = new File(outbox);
		outboxDir.mkdir();
		cc.setFailedBox(failedBox);
		cc.setOutbox(outbox);
	}

	private void conver2SysPath(Object cc, String paths, String prefix) throws Exception {
		String[] list = paths.split(",");
		Class ccLazz = cc.getClass();
		for (String mFile : list) {

			Method geter = ccLazz.getDeclaredMethod("get" + mFile);
			String field = (String) geter.invoke(cc);
			String realPath = prefix == null ? getRealPath(mFile, field) : prefix + field + "/";
			Method seter = ccLazz.getDeclaredMethod("set" + mFile, String.class);
			seter.invoke(cc, realPath);
		}
	}

	public String getRealPath(String paramName, String path) throws IOException {
		String newPath = path;
		if (!validator.validatePath(path)) {
			newPath = basePath + File.separator + path;
			File file = new File(newPath);
			if (!file.exists()) {
				if (!file.mkdir()) {
					String message = "Configuration error for \"" + paramName + "\" in GSSmartBox.conf.";
					LogUtil.FAILEDLOG.error(message);
					newPath = null;
					throw new IOException(message);
				}
			}
		}
		return newPath;
	}
}
