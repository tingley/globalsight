package com.adobe.cq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.globalsight.www.webservices.WebService4AEM;
import com.globalsight.www.webservices.WebService4AEM_Service;
import com.globalsight.www.webservices.WrapHashMap;
import com.globalsight.www.webservices.WrapHashMap.InputData;

@SlingServlet(paths = "/bin/createJob", methods = "POST", metatype = true)
public class CreateJobServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = -2420926640749680723L;

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	public static final Set<JobBean> jobNames = new HashSet<JobBean>();
	
//	private final static String sourceFolderPath = "sourceFile";

	@Override
	protected void doPost(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServerException,
			IOException {
		synchronized (this) {
			String priority = request.getParameter("priority");
			String comment = request.getParameter("comment");
			String jobName = request.getParameter("jobName");
			String[] targetLocales = request.getParameterValues("targetLocale");
			String tmpFolderName = request.getParameter("tmpFolderName");
			String pageKeys = request.getParameter("pageKeys");// array
			String fileMapProfileIds = request
					.getParameter("fileMapFileProfile");
			String[] pKeys = pageKeys.split(",");
			StringBuffer targetLocaleStr = new StringBuffer();

			if (targetLocales != null) {
				for (String localName : targetLocales) {
					if (targetLocaleStr.length() != 0) {
						targetLocaleStr.append(",");
					}
					targetLocaleStr.append(localName);
				}
			} else {
				return;
			}

			log.info("***********Create Job Information***********");
			log.info("Priority:		" + priority);
			log.info("Comment:		" + comment);
			log.info("JobName:		" + jobName);
			log.info("FileMapFP:		" + fileMapProfileIds);
			log.info("TargetLocale:		" + targetLocaleStr.toString());
			log.info("TempleFolder:		" + tmpFolderName);
			log.info("Page keys:		" + pageKeys);
			log.info("********************************************");

			File tempFolder = createTempFolder(tmpFolderName);

			if (tempFolder == null) {
				return;
			}

			CustomerService cs = this.getCustomerService();
			String accessFullToken = cs.getToken();
			String wsdlUrl = cs.getGlobalSightUrl();
			
			if (wsdlUrl == null) {
				log.error("wsdlUrl is null, please go to configure GlobalSight Connector!");
				return;
			}
			
			WebService4AEM_Service service = new WebService4AEM_Service(
					new URL(wsdlUrl));
			WebService4AEM port = service.getWebService4AEMPort();

			if (pKeys != null && pKeys.length != 0) {
				//Each target for each job
				for (String target : targetLocales) {
					List<File> tempFiles = createPageTempFile(pKeys,
							tempFolder, target);

					if (tempFiles != null && tempFiles.size() != 0) {
						uploadAndCreateJob(tempFiles, accessFullToken, port,
								jobName, fileMapProfileIds, comment, target,
								priority);
					} else {
						log.error("Sorry, fail to create temple file and tempFile object is null or size is zero!");
						return;
					}
				}

			} else {
				log.error("Sorry, parameter array pKeys no value!");
				return;
			}

			removeCartItems(pKeys);
		}
	}

	private void removeCartItems(String[] pKeys) {
		this.getCustomerService().removeCartItems(pKeys);

	}

	private List<File> createPageTempFile(String pkeys[], File folder,
			String target) {
		return this.getCustomerService().createPageTempFile(folder, pkeys,
				target);
	}

	private void uploadAndCreateJob(List<File> tempFiles,
			String accessFullToken, WebService4AEM service, String jobName,
			String fileMapProfileIds, String comment, String target,
			String priority) {

		if (tempFiles == null || tempFiles.size() == 0) {
			log.error("Paremater tempFiles is null!");
			return;
		}

		log.info("Begin to create new job:" + jobName);
		// File from, to = null;
		FileInputStream fin = null;
		BufferedReader myInput = null;
		jobName = this.getUniqueJobName(jobName, target);
		log.info("For the target \'" + target
				+ "\' system specify unique job name \'" + jobName + "\'");

		List<String> paths = new ArrayList<String>();
		String[] fileMapFPs = fileMapProfileIds.split("#");

		try {
			for (File tempFile : tempFiles) {
				String fileName = tempFile.getName();
				paths.add(tempFile.getPath());

				if (!tempFile.canWrite()) {
					log.error("Sorry, the system don't permit application user to access the file. Please own the authority firstly!");
					return;
				}

				XmlHelper helper = new XmlHelper(tempFile);
				helper.modifySourceFileTargetLanguage(target);

				fin = new FileInputStream(tempFile);
				myInput = new BufferedReader(new InputStreamReader(fin));

				String thisLine = "";
				StringBuffer sourceFileContent = new StringBuffer();

				while ((thisLine = myInput.readLine()) != null) {
					sourceFileContent.append(thisLine);
				}

				byte[] content = sourceFileContent.toString().getBytes("utf-8");

				WrapHashMap wrapMap = new WrapHashMap();
				InputData.Entry entry0 = new InputData.Entry();
				entry0.setKey("accessToken");
				entry0.setValue(accessFullToken);

				InputData.Entry entry1 = new InputData.Entry();
				entry1.setKey("jobName");
				entry1.setValue(jobName);

				InputData.Entry entry2 = new InputData.Entry();
				entry2.setKey("filePath");
				entry2.setValue(tempFile.getPath());// 3.10

				// get file profile associate with file
				String fileProfileId = null;

				int size = fileMapFPs.length;

				for (int i = 0; i < size; i++) {
					String fileMapFP = fileMapFPs[i];
					String fpId = fileMapFP
							.substring(fileMapFP.indexOf(",") + 1);

					if (!"".equals(fileMapFP.trim())
							&& fileName.equals(fileMapFP.substring(0,
									fileMapFP.lastIndexOf("-")))) {
						fileProfileId = fpId;
						log.info("File \'" + fileName
								+ " \' finds its fileProfileId: "
								+ fileProfileId);
						break;
					}
				}

				InputData.Entry entry3 = new InputData.Entry();
				entry3.setKey("fileProfileId");
				entry3.setValue(fileProfileId);

				InputData.Entry entry4 = new InputData.Entry();
				entry4.setKey("bytes");
				entry4.setValue(content);

				InputData.Entry entry5 = new InputData.Entry();
				entry5.setKey("priority");
				entry5.setValue(priority);

				InputData input = new InputData();
				input.getEntry().add(entry0);
				input.getEntry().add(entry1);
				input.getEntry().add(entry2);
				input.getEntry().add(entry3);
				input.getEntry().add(entry4);
				input.getEntry().add(entry5);
				wrapMap.setInputData(input);
				service.uploadFileForInitial(wrapMap);
				// end
			}

			StringBuffer fileProfiles = new StringBuffer();
			int size = fileMapFPs.length;

			for (int i = 0; i < size; i++) {
				String fileMapFP = fileMapFPs[i];
				String fpId = fileMapFP.substring(fileMapFP.indexOf(",") + 1);
				fileProfiles.append(fpId);

				if (i != (size - 1)) {
					fileProfiles.append("|");
				}
			}

			int pathSize = paths.size();
			StringBuffer sbPath = new StringBuffer();
			StringBuffer sbTarget = new StringBuffer();

			for (int i = 0; i < pathSize; i++) {
				String path = paths.get(i);
				sbPath.append(path);
				sbTarget.append(target);

				if ((i + 1) != pathSize) {
					sbPath.append("|");
					sbTarget.append("|");
				}
			}

			log.info("Job [" + jobName + "] fileProfiles: " + fileProfiles.toString());
			log.info("Job [" + jobName + "] targets: " + sbTarget.toString());
			log.info("Job [" + jobName + "] file paths: " + sbPath.toString());

			service.createJob(accessFullToken, jobName, comment,
					sbPath.toString(), fileProfiles.toString(),
					sbTarget.toString()); // 3.10
			service.addJobComment(accessFullToken, jobName, "", comment, null,
					null, "General");
			
			JobBean job = new JobBean();
			job.setJobName(jobName);
			job.setTarget(target);
			job.setStatus(AEMConstants.InProgress);
			jobNames.add(job);
			
			this.getCustomerService().persisJob(job);

			log.info("Job [" + jobName + "] creating work done.");
		} catch (Exception ex) {
			log.error("Error: ", ex);
		} finally {
			if (myInput != null)
				try {
					myInput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (fin != null)
				try {
					fin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

	}

	// need not to generate target file so mark it.
	// private void uploadAndCreateJob(List<File> tempFiles,
	// String accessFullToken, WebService4AEM service, String jobName,
	// String fileMapProfileIds, String comment, String target, String priority)
	// {
	//
	// if (tempFiles == null || tempFiles.size() == 0) {
	// log.error("Paremater tempFiles is null!");
	// return;
	// }
	//
	// log.info("Begin to create new job:" + jobName);
	// File from, to = null;
	// FileInputStream fin = null;
	// BufferedReader myInput = null;
	// jobName = this.getUniqueJobName(jobName);
	// log.info("For the target \'" + target +
	// "\' system specify unique job name \'" + jobName + "\'");
	//
	// List<String> paths = new ArrayList<String>();
	// String[] fileMapFPs = fileMapProfileIds.split("#");
	//
	// try {
	// for (File tempFile : tempFiles) {
	// from = tempFile;
	// String fileName = from.getName();
	// String targetFileName = this.getTargetFileName(target,
	// fileName, this.getFileExtension(from));
	// to = new File(tempFile.getParentFile(), targetFileName);
	// to.createNewFile();
	// paths.add(to.getPath());
	//
	// if (!from.canWrite() && !to.canWrite()) {
	// log.error("Sorry, the system don't permit application user to access the file. Please open the authority firstly!");
	// return;
	// }
	//
	// XmlHelper helper = new XmlHelper(from, to);
	// helper.modifyTargetLanguage(target);
	//
	// fin = new FileInputStream(to);
	// myInput = new BufferedReader(new InputStreamReader(fin));
	//
	// String thisLine = "";
	// StringBuffer targetFileContent = new StringBuffer();
	//
	// while ((thisLine = myInput.readLine()) != null) {
	// targetFileContent.append(thisLine);
	// }
	//
	// byte[] content = targetFileContent.toString().getBytes("utf-8");
	//
	// WrapHashMap wrapMap = new WrapHashMap();
	// InputData.Entry entry0 = new InputData.Entry();
	// entry0.setKey("accessToken");
	// entry0.setValue(accessFullToken);
	//
	// InputData.Entry entry1 = new InputData.Entry();
	// entry1.setKey("jobName");
	// entry1.setValue(jobName);
	//
	// InputData.Entry entry2 = new InputData.Entry();
	// entry2.setKey("filePath");
	// entry2.setValue(to.getPath());// 3.10
	//
	// //get file profile associate with file
	// String fileProfileId = null;
	//
	// int size = fileMapFPs.length;
	//
	// for (int i=0; i<size; i++) {
	// String fileMapFP = fileMapFPs[i];
	// String fpId = fileMapFP.substring(fileMapFP.indexOf(",")+1);
	//
	// if (!"".equals(fileMapFP.trim())
	// && fileName.equals(fileMapFP.substring(0, fileMapFP.lastIndexOf("-")))) {
	// fileProfileId = fpId;
	// log.info("File \'" + fileName + " \' finds its fileProfileId: " +
	// fileProfileId);
	// break;
	// }
	// }
	//
	// InputData.Entry entry3 = new InputData.Entry();
	// entry3.setKey("fileProfileId");
	// entry3.setValue(fileProfileId);
	//
	// InputData.Entry entry4 = new InputData.Entry();
	// entry4.setKey("bytes");
	// entry4.setValue(content);
	//
	// InputData.Entry entry5 = new InputData.Entry();
	// entry5.setKey("priority");
	// entry5.setValue(priority);
	//
	// InputData input = new InputData();
	// input.getEntry().add(entry0);
	// input.getEntry().add(entry1);
	// input.getEntry().add(entry2);
	// input.getEntry().add(entry3);
	// input.getEntry().add(entry4);
	// input.getEntry().add(entry5);
	// wrapMap.setInputData(input);
	// service.uploadFileForInitial(wrapMap);
	// // end
	// }
	//
	// StringBuffer fileProfiles = new StringBuffer();
	// int size = fileMapFPs.length;
	//
	// for (int i=0; i<size; i++) {
	// String fileMapFP = fileMapFPs[i];
	// String fpId = fileMapFP.substring(fileMapFP.indexOf(",")+1);
	// fileProfiles.append(fpId);
	//
	// if (i!= (size-1)) {
	// fileProfiles.append("|");
	// }
	// }
	//
	// int pathSize = paths.size();
	// StringBuffer sbPath = new StringBuffer();
	// StringBuffer sbTarget = new StringBuffer();
	//
	// for (int i = 0; i < pathSize; i++) {
	// String path = paths.get(i);
	// sbPath.append(path);
	// sbTarget.append(target);
	//
	// if ((i + 1) != pathSize) {
	// sbPath.append("|");
	// sbTarget.append("|");
	// }
	// }
	//
	// JobBean job = new JobBean();
	// job.setJobName(jobName);
	// job.setTarget(target);
	// job.setStatus(AEMConstants.InProgress);
	// jobNames.add(job);
	//
	// log.info("Creating job's fileProfiles: " + fileProfiles.toString());
	//
	// service.createJob(accessFullToken, jobName, comment,
	// sbPath.toString(), fileProfiles.toString(), sbTarget.toString()); // 3.10
	// service.addJobComment(accessFullToken, jobName, "", comment, null,
	// null, "General");
	//
	// log.info("Create job " + jobName + "done.");
	// } catch (Exception ex) {
	// log.error("Error: ", ex);
	// } finally {
	// if (myInput != null)
	// try {
	// myInput.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// if (fin != null)
	// try {
	// fin.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// }

	
	private File createTempFolder(String tmpFolderName) {
		File folder = new File(AEMConstants.GlobalSightSourceFile + File.separator + tmpFolderName);
		
		if (folder.mkdirs()) {
			log.info("OK! Success in create temp folder: " + folder.getName());
			return folder;
		}
		
		log.error("Fail to create sourceFile folder due to file access control problem. Please contact administrator.");
		return null;

	}

	// private void uploadAndCreateJob(String accessFullToken,
	// WebService4AEM service, String jobName, String fileProfileId,
	// String comment, String target) {
	// File sourceFile, targetFile = null;
	// FileInputStream fin = null;
	// BufferedReader myInput = null;
	// jobName = this.getUniqueJobName(jobName);
	//
	// try {
	// String sourceFilePath = "sourceFile//source_temp.xml" ;
	// sourceFile = new File(sourceFilePath);
	// String targetFilePath = "sourceFile//" + this.getTargetFileName(target);
	// targetFile = new File(targetFilePath);
	//
	// if (!sourceFile.canWrite() &&
	// !targetFile.canWrite()) {
	// log.error("Sorry, because this application user can't write to this file. Please open the authority firstly!");
	// return;
	// }
	//
	// //updateFile: targetLanguage
	// XmlHelper helper = new XmlHelper(sourceFile, targetFile);
	// helper.modifyTargetLanguage(target);
	//
	// fin = new FileInputStream(targetFile);
	// myInput = new BufferedReader(new InputStreamReader(fin));
	//
	// String thisLine = "";
	// StringBuffer targetFileContent = new StringBuffer();
	//
	// while ((thisLine = myInput.readLine()) != null) {
	// targetFileContent.append(thisLine);
	// }
	//
	// byte[] content = targetFileContent.toString().getBytes("utf-8");
	//
	// WrapHashMap wrapMap = new WrapHashMap();
	// InputData.Entry entry0 = new InputData.Entry();
	// entry0.setKey("accessToken");
	// entry0.setValue(accessFullToken);
	//
	// InputData.Entry entry1 = new InputData.Entry();
	// entry1.setKey("jobName");
	// entry1.setValue(jobName);
	//
	// InputData.Entry entry2 = new InputData.Entry();
	// entry2.setKey("filePath");
	// entry2.setValue(targetFilePath);
	//
	// InputData.Entry entry3 = new InputData.Entry();
	// entry3.setKey("fileProfileId");
	// entry3.setValue(fileProfileId);
	//
	// InputData.Entry entry4 = new InputData.Entry();
	// entry4.setKey("bytes");
	// entry4.setValue(content);
	//
	// InputData input = new InputData();
	// input.getEntry().add(entry0);
	// input.getEntry().add(entry1);
	// input.getEntry().add(entry2);
	// input.getEntry().add(entry3);
	// input.getEntry().add(entry4);
	//
	// wrapMap.setInputData(input);
	//
	// service.uploadFileForInitial(wrapMap);
	// JobBean job = new JobBean();
	// job.setJobName(jobName);
	// job.setTarget(target);
	// jobNames.add(job);
	// service.createJob(accessFullToken, jobName, comment,
	// targetFilePath, fileProfileId, target);
	// } catch (Exception ex) {
	// log.error("Error: ", ex);
	// } finally {
	// if (myInput != null)
	// try {
	// myInput.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// if (fin != null)
	// try {
	// fin.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// }

	// private void fileChannelCopy(File s, File t) {
	// FileInputStream fi = null;
	// FileOutputStream fo = null;
	// FileChannel in = null;
	// FileChannel out = null;
	//
	// try {
	// fi = new FileInputStream(s);
	// fo = new FileOutputStream(t);
	// in = fi.getChannel();
	// out = fo.getChannel();
	// in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道
	// } catch (IOException e) {
	// e.printStackTrace();
	// } finally {
	// try {
	// fi.close();
	// in.close();
	// fo.close();
	// out.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// }

	private String getTargetFileName(String target, String tempFileName,
			String suffix) {
		return tempFileName.replaceAll("[.][^.]+$", "") + "(" + target + ")."
				+ suffix;
	}

	private String getFileExtension(File file) {
		String fileName = file.getName();
		if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
			return fileName.substring(fileName.lastIndexOf(".") + 1);
		} else {
			return "";
		}
	}

	private String getUniqueJobName(String jobName, String target) {
		String randomStr = String.valueOf((new Random()).nextInt(999999999));
		while (randomStr.length() < 9) {
			randomStr = "0" + randomStr;
		}

		jobName = jobName + "_" + target + "_"+ randomStr;
		return jobName;
	}

	private CustomerService getCustomerService() {
		BundleContext ctx = FrameworkUtil.getBundle(this.getClass())
				.getBundleContext();
		ServiceReference serviceReference = ctx
				.getServiceReference(CustomerService.class.getName());
		return CustomerService.class.cast(ctx.getService(serviceReference));
	}

	public static void main(String[] args) {
		String ss = "abc.ced.hell.xml-21,87#bcd.abe.hello.xml-45,32#";

		String[] ff = ss.split("#");

		int size = ff.length;

		StringBuffer fileProfiles = new StringBuffer();

		for (int i = 0; i < size; i++) {
			String fileMapFP = ff[i];
			String fpId = fileMapFP.substring(fileMapFP.indexOf(",") + 1);
			System.out.println("@@@@@@@@@@@ " + fileMapFP);
			fileProfiles.append(fpId);

			if (i != (size - 1)) {
				fileProfiles.append("|");
			}
		}

		System.out.println(fileProfiles.toString());

		// for (String f : ff){
		// int end = f.indexOf("-");
		// if ("abc.ced.hell".equals(f.substring(0, end))) {
		// System.out.println(f);
		// System.out.println("fpid = " + f.substring(f.indexOf(",") +1));
		// }
		//
		// }
	}
}
