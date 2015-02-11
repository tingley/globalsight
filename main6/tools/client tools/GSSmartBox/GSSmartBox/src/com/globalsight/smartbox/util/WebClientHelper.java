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
package com.globalsight.smartbox.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import com.globalsight.smartbox.bo.FileProfile;
import com.globalsight.smartbox.bo.JobInfo;
import com.globalsight.www.webservices.Ambassador;
import com.globalsight.www.webservices.AmbassadorServiceLocator;

/**
 * 
 * WebClientHelper
 * 
 * @author leon
 * 
 */
public class WebClientHelper {
	private static String accessToken = "";
	private static Ambassador ambassador;
	private static boolean installCertsHttps = false;

	public static boolean init(String host, String port, String https, String userName, String password) {
		boolean isHttps = "on".equals(https);
		String prefix = isHttps ? "https://" : "http://";
		String wsdlUrl = prefix + host + ":" + port + "/globalsight/services/AmbassadorWebService?wsdl";
		if (isHttps) {
			if (!installCertsHttps) {
				try {
					installCert(host, Integer.parseInt(port));
				} catch (Exception e) {
					String message = "Failed to install certs for https.";
					LogUtil.fail(message, e);
					return false;
				}
				installCertsHttps = true;
			}
		}
		AmbassadorServiceLocator loc = new AmbassadorServiceLocator();
		try {
			ambassador = loc.getAmbassadorWebService(new URL(wsdlUrl));
			accessToken = ambassador.login(userName, password);
		} catch (Exception e) {
			String message = "Web Service Exception when init, please make sure the url, username and password is right, and check if the GlobalSight server is open.";
			LogUtil.fail(message, e);
			return false;
		}
		return true;
	}

	/**
	 * Get unique job name
	 * 
	 * @param jobName
	 * @return
	 * @throws Exception
	 */
	public static String getUniqueJobName(String jobName) throws Exception {
		String uniqueJobName = ambassador.getUniqueJobName(accessToken, jobName);
		return uniqueJobName;
	}

	/**
	 * Create jobs
	 * 
	 * @param jobInfo
	 * @throws Exception
	 */
	public static boolean createJob(JobInfo jobInfo) {
		String jobName = jobInfo.getJobName();
		Vector<String> sourceFiles = jobInfo.getSourceFiles();
		Vector<String> fileProfileIds = jobInfo.getFileProfileIds();
		Vector<String> targetLocales = jobInfo.getTargetLocales();

		// Upload file to GS Server
		String jobId = uploadFileToServer(jobName, sourceFiles, fileProfileIds);
		if (jobId == null) {
			return false;
		}

		Vector<String> sfs = new Vector<String>();
		for (String path : sourceFiles) {
			sfs.add(path.substring(path.indexOf(File.separator) + 1));
		}
		// Create Job
		try {
			HashMap map = new HashMap();
			map.put("jobId", jobId);
			map.put("accessToken", accessToken);
			map.put("comment", null);
			map.put("filePaths", sfs);
			map.put("fileProfileIds", fileProfileIds);
			map.put("targetLocales", targetLocales);
			map.put("cvsModules", null);
			map.put("priority", null);
			map.put("attributes", null);
			ambassador.createJobOnInitial(map);
		} catch (Exception e) {
			String message = "Create job failed, Exception from Web Service, FileName:" + jobInfo.getOriginFile();
			LogUtil.fail(message, e);
			return false;
		}
		return true;
	}

	/**
	 * Get jobStatus from GS Server
	 * 
	 * @param jobInfo
	 * @return
	 * @throws Exception
	 */
	public static boolean getJobStatus(JobInfo jobInfo) {
		try {
			String jobStatus = ambassador.getJobStatus(accessToken, jobInfo.getJobName());
			Document profileDoc = DocumentHelper.parseText(jobStatus);
			Node node = profileDoc.selectSingleNode("/job");
			String id = node.selectSingleNode("id").getText();
			String status = node.selectSingleNode("status").getText();
			jobInfo.setId(id);
			jobInfo.setStatus(status);
		} catch (Exception e) {
			String message = "Failed to get job status, Job Name:" + jobInfo.getJobName();
			LogUtil.fail(message, e);
			return false;
		}
		return true;
	}

	/**
	 * Download job
	 * 
	 * @param jobInfo
	 * @return
	 * @throws Exception
	 */
	public static boolean jobDownload(JobInfo jobInfo, String baseDir, String server) {
		try {
			String fileXml = ambassador.getJobExportFiles(accessToken, jobInfo.getJobName());
			Document profileDoc = DocumentHelper.parseText(fileXml);
			Node node = profileDoc.selectSingleNode("/jobFiles");
			String root = node.selectSingleNode("root").getText();
			ArrayList<String> filePaths = new ArrayList<String>();
			List<Node> paths = node.selectNodes("paths");
			for (Node n : paths) {
				filePaths.add(n.getText());
			}

			root = replaceHostUrl(root, server);

			String rootNoCompany = root.substring(0, root.lastIndexOf("/"));
			boolean useHttps = root.startsWith("https:");
			boolean useHttp = root.startsWith("http:");

			String commonPath = ZipUtil.getCommonPath(getReplacedPath(jobInfo.getJobName(), filePaths), "");
			File targetFile = null;
			StringBuffer targetFiles = new StringBuffer();
			for (String path : filePaths) {
				int index = path.indexOf(commonPath);
				String savePath = path.substring(index + commonPath.length());
				String[] nodes = path.split("/");
				String locale = nodes[0];
				savePath = rootNoCompany + "/" + jobInfo.getJobName() + "/" + locale + savePath;

				String downloadUrl = root + "/" + path;

				if (useHttps) {
					targetFile = DownLoadHelper.downloadHttps(downloadUrl, baseDir, savePath);
				} else if (useHttp) {
					targetFile = DownLoadHelper.downloadHttp(downloadUrl, baseDir, savePath);
				}
				targetFiles.append(targetFile.getPath()).append("|");
			}
			if (targetFiles.length() > 0) {
				targetFiles.deleteCharAt(targetFiles.length() - 1);
			}
			jobInfo.setTargetFiles(targetFiles.toString());
		} catch (Exception e) {
			String message = "Failed to download job, Job Name:" + jobInfo.getJobName() + ", Job Id:" + jobInfo.getId();
			LogUtil.fail(message, e);
			return false;
		}
		return true;
	}

	/**
	 * Get File Profile Info from GS Server
	 * 
	 * @return
	 * @throws Exception
	 */
	public static List<FileProfile> getFileProfileInfoFromGS() throws Exception {
		List<FileProfile> fileProfiles = new ArrayList<FileProfile>();
		String fileProfileInfo = ambassador.getFileProfileInfoEx(accessToken);

		Document profileDoc = DocumentHelper.parseText(fileProfileInfo);
		List<Element> profileList = profileDoc.selectNodes("/fileProfileInfo/fileProfile");
		for (Element node : profileList) {
			FileProfile fp = new FileProfile();
			fp.setId(node.selectSingleNode("id").getText());
			fp.setName(node.selectSingleNode("name").getText());

			List<Element> extensions = node.selectNodes("fileExtensionInfo/fileExtension");
			for (Element extension : extensions) {
				fp.addFileExtension(extension.getText());
			}

			String sourceLocale = node.selectSingleNode("localeInfo/sourceLocale").getText();
			fp.setSourceLocale(sourceLocale);

			List<Element> targetLocales = node.selectNodes("localeInfo/targetLocale");
			for (Element targetLocale : targetLocales) {
				fp.addTargetLocale(targetLocale.getText());
			}
			fileProfiles.add(fp);
		}
		return fileProfiles;
	}

	/**
     * Check if importing/exporting is happening on server.
     * 
     * @return boolean
     */
    public static boolean isServerImportingOrExporting()
    {
        try
        {
            String status = ambassador.getImportExportStatus(accessToken);
            Document dom = DocumentHelper.parseText(status);
            Element root = dom.getRootElement();
            String creatingNum = root.element("jobsCreating").getText();
            String exportingNum = root.element("localesExporting").getText();
            int cNum = Integer.valueOf(creatingNum);
            int eNum = Integer.valueOf(exportingNum);
            if (cNum > 0 || eNum > 0)
                return true;
        }
        catch (Exception e)
        {
            LogUtil.info("Exception when try to detect server importing/exporting status:"
                    + e.getMessage());
        }

        return false;
    }

	/**
	 * Upload file to Server
	 * 
	 * @param jobName
	 * @param jobFiles
	 * @param fileProfileIds
	 * @throws Exception
	 */
	private static String uploadFileToServer(String jobName, Vector<String> sourceFiles, Vector<String> fileProfileIds) {
		String jobId = null;
		for (int i = 0; i < sourceFiles.size(); i++) {
			String filePath = sourceFiles.get(i);
			String fileProfileId = fileProfileIds.get(i);

			File file = new File(filePath);
			byte[] content;
			try {
				content = getFileContent(file);
				HashMap map = new HashMap();
				map.put("jobId", jobId);
				map.put("accessToken", accessToken);
				map.put("filePath", filePath.substring(filePath.indexOf(File.separator) + 1));
				map.put("jobName", jobName);
				map.put("fileProfileId", fileProfileId);
				map.put("bytes", content);
				jobId = ambassador.uploadFileForInitial(map);
			} catch (Exception e) {
				String message = "Failed to upload file, Exception from Web Service, FileName:" + file.getName();
				LogUtil.fail(message, e);
				return null;
			}
		}
		return jobId;
	}

	/**
	 * Read a file content into a byte array.
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static byte[] getFileContent(File file) throws IOException {
		InputStream in = null;
		byte[] content = new byte[(int) file.length()];
		in = new FileInputStream(file);
		int i = 0;
		int tmp = 0;
		while ((tmp = in.read()) != -1) {
			content[i] = (byte) tmp;
			i++;
		}
		in.close();
		return content;
	}

	private static ArrayList<String> getReplacedPath(String jobName, ArrayList<String> paths) {
		ArrayList<String> allPath = new ArrayList<String>();
		for (String path : paths) {
			allPath.add(getReplacedPath(jobName, path));
		}
		return allPath;
	}

	private static String getReplacedPath(String jobName, String path) {
		return path.substring(path.indexOf(jobName) + jobName.length() + 1);
	}

	private static boolean installCert(String host, int port) throws Exception {
		char[] passphrase = "changeit".toCharArray();
		char SEP = File.separatorChar;
		File dir = new File(System.getProperty("java.home") + SEP + "lib" + SEP + "security");
		File file_jssecacerts = new File(dir, "jssecacerts");
		if (file_jssecacerts.isFile() == false) {
			file_jssecacerts = new File(dir, "cacerts");
		}

		LogUtil.GSSMARTBOXLOG.info("Loading KeyStore " + file_jssecacerts + "...");
		InputStream in = new FileInputStream(file_jssecacerts);
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(in, passphrase);
		in.close();

		SSLContext context = SSLContext.getInstance("TLS");
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);
		X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
		SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
		context.init(null, new TrustManager[] { tm }, null);
		SSLSocketFactory factory = context.getSocketFactory();

		LogUtil.GSSMARTBOXLOG.info("Opening ssl connection to " + host + ":" + port + "...");
		SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
		socket.setSoTimeout(10000);
		try {
			LogUtil.GSSMARTBOXLOG.info("Starting SSL handshake...");
			socket.startHandshake();
			socket.close();
			LogUtil.GSSMARTBOXLOG.info("No errors, certificate is already trusted");
			return false;
		} catch (SSLException e) {
			LogUtil.fail("Exception during install certs", e);
		}

		X509Certificate[] chain = tm.chain;
		if (chain == null) {
			LogUtil.GSSMARTBOXLOG.info("Could not obtain server certificate chain");
			return false;
		}

		for (int i = 0; i < chain.length; i++) {
			X509Certificate cert = chain[i];
			String alias = host + "-" + (i + 1);
			ks.setCertificateEntry(alias, cert);

			LogUtil.GSSMARTBOXLOG.info(cert);
			LogUtil.GSSMARTBOXLOG.info("Added certificate to keystore 'jssecacerts' using alias '" + alias + "'");
		}

		OutputStream out = new FileOutputStream(new File(dir, "jssecacerts"));
		ks.store(out, passphrase);
		out.close();
		LogUtil.GSSMARTBOXLOG.info("Save key store successfully.");
		return true;
	}

	private static String replaceHostUrl(String root, String server) {
		int indexOfGS = root.indexOf("/globalsight/");

		StringBuffer url = new StringBuffer(root);
		if (indexOfGS != -1) {
			url.replace(0, indexOfGS + 1, server);
		}

		return url.toString();
	}
}

class SavingTrustManager implements X509TrustManager {
	private final X509TrustManager tm;
	public X509Certificate[] chain;

	SavingTrustManager(X509TrustManager tm) {
		this.tm = tm;
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		this.chain = chain;
		tm.checkServerTrusted(chain, authType);
	}
}