package com.globalsight.smartbox.bussiness.xo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import jcifs.smb.SmbFile;

import com.globalsight.smartbox.bo.CompanyConfiguration;
import com.globalsight.smartbox.bo.FTPConfiguration;
import com.globalsight.smartbox.bo.SMBConfiguration;
import com.globalsight.smartbox.util.FtpHelper;
import com.globalsight.smartbox.util.LogUtil;
import com.globalsight.smartbox.util.WebClientHelper;

public class SmartValidator {
	private final int ftpPort = 21;
	private String omitNodes = "fileboxes,webClient,checkTimer";

	public boolean validatePath(String path) {
		File file = new File(path);
		return file.exists();
	}

	public boolean validateFTP(FTPConfiguration ftpConfiguration) {
		String ftpHost = ftpConfiguration.getFtpHost();
		int ftpPort = ftpConfiguration.getFtpPort();
		String ftpUsername = ftpConfiguration.getFtpUsername();
		String ftpPassword = ftpConfiguration.getFtpPassword();
		String ftpInbox = ftpConfiguration.getFtpInbox();
		String ftpOutbox = ftpConfiguration.getFtpOutbox();
		String ftpFailedbox = ftpConfiguration.getFtpFailedbox();
		// Validate FTP server
		FtpHelper ftpHelper = new FtpHelper(ftpHost, ftpPort, ftpUsername, ftpPassword);
		boolean serverAvaliable = ftpHelper.testConnect();
		if (!serverAvaliable) {
			return false;
		}
		boolean dirExists = true;

		dirExists = ftpHelper.ftpDirExists(ftpInbox);
		if (!dirExists) {
			String message = "Configuration error for \"FTPInbox\" in GSSmartBox.cfg.xml.";
			LogUtil.FAILEDLOG.error(message);
			return false;
		}

		dirExists = ftpHelper.ftpDirExists(ftpOutbox);
		if (!dirExists) {
			String message = "Configuration error for \"FTPOutbox\" in GSSmartBox.cfg.xml.";
			LogUtil.FAILEDLOG.error(message);
			return false;
		}

		dirExists = ftpHelper.ftpDirExists(ftpFailedbox);
		if (!dirExists) {
			String message = "Configuration error for \"FTPFailedbox\" in GSSmartBox.cfg.xml.";
			LogUtil.FAILEDLOG.error(message);
			return false;
		}
		// Create Import and Export folder unbder failedbox in FTP
		if (!ftpHelper.ftpDirExists(ftpFailedbox + "/Import")) {
			ftpHelper.ftpCreateDir(ftpFailedbox + "/Import");
		}
		if (!ftpHelper.ftpDirExists(ftpFailedbox + "/Export")) {
			ftpHelper.ftpCreateDir(ftpFailedbox + "/Export");
		}

		return true;

	}

	public boolean validateSMB(SMBConfiguration smbConfiguration) {
		String smbInbox = smbConfiguration.getSMBInbox();
		String smbOutbox = smbConfiguration.getSMBOutbox();
		String smbFailedbox = smbConfiguration.getSMBFailedbox();
		try {
			// Validate SMB server
			SmbFile sfInbox = new SmbFile(smbInbox);
			if (!sfInbox.exists()) {
				String message = "Configuration error for \"SMBInbox\" in GSSmartBox.cfg.xml, please check the SMB Configuration(host, username, password, SMBInbox).";
				LogUtil.FAILEDLOG.error(message);
				return false;
			}

			SmbFile sfOutbox = new SmbFile(smbOutbox);
			if (!sfOutbox.exists()) {
				String message = "Configuration error for \"SMBOutbox\" in GSSmartBox.cfg.xml, please check the SMB Configuration(host, username, password, SMBOutbox).";
				LogUtil.FAILEDLOG.error(message);
				return false;
			}

			SmbFile sfFailedbox = new SmbFile(smbFailedbox);
			if (!sfFailedbox.exists()) {
				String message = "Configuration error for \"SMBFailedbox\" in GSSmartBox.cfg.xml, please check the SMB Configuration(host, username, password, Failedbox).";
				LogUtil.FAILEDLOG.error(message);
				return false;
			}

			// Create Import and Export folder unbder failedbox in SMB
			SmbFile sfFailedboxImport = new SmbFile(smbFailedbox + "/Import");
			if (!sfFailedboxImport.exists()) {
				sfFailedboxImport.mkdir();
			}
			SmbFile sfFailedboxExport = new SmbFile(smbFailedbox + "/Export");
			if (!sfFailedboxExport.exists()) {
				sfFailedboxExport.mkdir();
			}
		} catch (Exception e) {
			String message = "SMB Config error.";
			LogUtil.FAILEDLOG.error(message);
			return false;
		}

		return true;
	}

	public boolean validateWS(CompanyConfiguration cc) {
		String host = cc.getHost();
		String port = cc.getPort();
		String https = cc.getHttps();
		String username = cc.getUsername();
		String password = cc.getPassword();
		// Check the url, username and password is right
		return WebClientHelper.init(host, port, https, username, password);
	}

	public String validateXml(InputStream is) {
		String[] omitMatch = omitNodes.split(",");
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		StringBuilder sb = new StringBuilder();

		String line = null;

		try {

			while ((line = reader.readLine()) != null) {
				for (String omit : omitMatch) {

					if (line.contains(omit)) {
						line = "";
					}
				}
				sb.append(line);

			}

		} catch (IOException e) {
			System.out.print(e.getMessage());

		} finally {

			try {

				is.close();

			} catch (IOException e) {

				System.out.print(e.getMessage());

			}

		}

		return sb.toString();

	}
}
