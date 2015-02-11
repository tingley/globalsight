package com.globalsight.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.bo.LoginBO;
import com.globalsight.bo.QueryBO;
import com.globalsight.cvsoperation.entity.FileRename;
import com.globalsight.cvsoperation.entity.ModuleMapping;
import com.globalsight.cvsoperation.entity.TargetModuleMapping;
import com.globalsight.cvsoperation.util.ModuleMappingHelper;
import com.globalsight.entity.FileMapped;
import com.globalsight.entity.Job;
import com.globalsight.entity.User;
import com.globalsight.util.DownLoadHelper;
import com.globalsight.util.StringHelper;
import com.globalsight.util.StringXmlHelper;
import com.globalsight.util.SwingHelper;
import com.globalsight.util.WebClientHelper;
import com.globalsight.util.XmlUtil;
import com.globalsight.util.zip.ZipIt;
import com.globalsight.util2.CacheUtil;
import com.globalsight.util2.ConfigureHelperV2;
import com.globalsight.vo.JobFiles;
import com.globalsight.www.webservices.Ambassador;

public class DownloadAction extends Action {

	static Logger log = Logger.getLogger(DownloadAction.class.getName());

	private static DownloadThread m_thread = null;

	public String execute(String args[]) throws Exception {
		// only allow one download thread running
		if (m_thread != null) {
			stopDownload();
		}

		// download thread
		User u = CacheUtil.getInstance().getCurrentUser();
		DownloadThread t = new DownloadThread("DownloadThread - " + u, u);
		t.start();

		m_thread = t;
		return "success";
	}

	public void stopDownload() throws Exception {
		if (m_thread != null)
			m_thread.stopMe();
	}

	class DownloadThread extends Thread {
		private boolean m_run = true;

		private User m_user = null;

		public DownloadThread(String name, User u) {
			super(name);
			m_user = u;
		}

		/**
		 * set running tag run = false, so this thread will stop after sleep.
		 */
		public void stopMe() {
			m_run = false;
		}

		public void run() {
			log.info("Started to download all available jobs in every "
					+ m_user.getMinutes() + " minute(s) by user " + m_user);
			loop: while (m_run) {
				long sleepTime = 0;
				try {
					Job[] jobs = getJobsDownloadableFromUser(m_user);
					if (m_user == null) {
						break;
					}
					int min = Integer.parseInt(m_user.getMinutes());
					sleepTime = min * 60 * 1000;
					if (log.isDebugEnabled()) {
						StringBuffer sb = new StringBuffer();
						for (int i = 0; i < jobs.length; i++) {
							sb.append(jobs[i]).append(", ");
						}
						log.debug("jobsName=" + sb.toString());
						log.debug("minutes=" + min);
					}
					if (jobs.length != 0) {
						Job[] jobsToDownload = getDownloadableJobsFromAmb(jobs);
						for (int i = 0; i < jobsToDownload.length; i++) {
							try {
								if (m_run)
									downloadOneJob(jobsToDownload[i]);
								else
									break loop;
							} catch (Exception e) {
								log.error("Download job " + jobsToDownload[i]
										+ " exception: " + e);
								throw e;
							}
						}
					}
					Thread.sleep(sleepTime);
				} catch (Exception e) {
					log.error("Failed to download files ", e);
					LoginBO loginBO = new LoginBO();
					try {
						Thread.sleep(1);
						if (m_user != null && m_run) {
							// setAccessToken(loginBO.login(m_user.getName(),
							// m_user.getPassword()));

							SwingHelper.getMainFrame().logon(m_user);
						} else {
							break;
						}
					} catch (Exception e1) {
						log.error("Relogin error. ", e1);
					}
				}
			}
			log.info("Download thread stopped  - " + m_user);
		}
		
	    private ArrayList<String> getReplacedPath(String jobName, ArrayList<String> paths)
	    {
	        ArrayList<String> allPath = new ArrayList<String>();
	        for (String path : paths)
	        {
	            allPath.add(getReplacedPath(jobName, path));
	        }
	        
	        return allPath;
	    }
	    
	    private String getReplacedPath(String jobName, String path)
	    {
	        String webservice = "webservice";
	        String tmp = "";
	        tmp = path.substring(path.indexOf(webservice) + webservice.length() + 1);
	        tmp = tmp.substring(tmp.indexOf("/") + 1);
	        return tmp;
	    }
	    
	    private int getFileIndex(Job p_job, String path)
	    {
	        int index = path.indexOf(p_job.getName()) + p_job.getName().length();
	        path = path.substring(index);
	        try
            {
                path = URLDecoder.decode(path, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                log.error(e.getMessage(), e);
            }
	        
	        List<FileMapped> files = p_job.getFileMappedList();
	        for (int i = 0; i < files.size(); i++)
	        {
	            FileMapped file = files.get(i);
	            String fPath = file.getFile().getPath();
	            fPath = fPath.replace("\\", "/");
	            int n = fPath.indexOf("/");
	            fPath = fPath.substring(n);
	            if (fPath.equals(path))
	            {
	                return i;
	            }
	        }
	        
	        return -1;
	    }

		private void downloadOneJob(Job p_job) throws Exception {
		    log.info("Start to download " + p_job);
		    Ambassador ambassador = WebClientHelper.getAmbassador();
		    String fileXml = ambassador.getJobExportFiles(accessToken, p_job.getName());
		    JobFiles files = XmlUtil.string2Object(JobFiles.class, fileXml);
		    
		    String root = files.getRoot();
		    root = replaceHostUrl(root).toString();
		    String rootNoCompany = root.substring(0, root.lastIndexOf("/"));
		    boolean useHttps = root.startsWith("https:");
		    boolean useHttp = root.startsWith("http:");
		    
		    ArrayList<String> paths = (ArrayList<String>)files.getPaths();
		    String commonPath = ZipIt.getCommonPath(getReplacedPath(p_job.getName(), paths), "");
		    File targetFile = null;
		    for (String path : paths)
		    {
		        int index = path.indexOf(commonPath);
		        String savePath = path.substring(index + commonPath.length());
		        String[] nodes = path.split("/");
		        String locale = nodes[0];
		        savePath = rootNoCompany + "/" + p_job.getName() + "/" + locale + savePath;
		        
		        String downloadUrl = root + "/" + path;
		        
              if (useHttps)
                {
                    targetFile = DownLoadHelper.downloadHttps(downloadUrl,
                            p_job.getOwner(), savePath);
                }
                else if (useHttp)
                {
                    targetFile = DownLoadHelper.downloadHttp(downloadUrl, p_job
                            .getOwner(), savePath);
                }
                if (p_job.isCVSJob())
                {
                 // The job is created from CVS
                    int i = getFileIndex(p_job, path);
                    if (i > -1)
                    {
                        saveToCVS(p_job, downloadUrl, targetFile, i);
                    }
                }
		    }

			log.info("Download " + p_job + " successfully");
			p_job.setDownloadUser(m_user.getName());
			// add job to message center
			SwingHelper.getMainFrame().addJobDownloaded(p_job);
			// save download time
			ConfigureHelperV2.writeDownloadedJob(p_job);
		}

        private Job[] getJobsDownloadableFromUser(User u) throws Exception {
			List jobs = ConfigureHelperV2.readJobsToDownloadByDlList(u);
			if (jobs == null || jobs.isEmpty()) {
				return new Job[] {};
			} else {
				Job[] r = new Job[jobs.size()];
				jobs.toArray(r);
				return r;
			}
		}

		private Job[] getDownloadableJobsFromAmb(Job[] p_jobs) throws Exception {
			IsDownloadableAction isDownloadable = new IsDownloadableAction();
			String[] allJobsNames = new String[p_jobs.length];
			for (int i = 0; i < allJobsNames.length; i++) {
				allJobsNames[i] = p_jobs[i].getName();
			}

			String result = isDownloadable.execute(allJobsNames);
			List jobList = StringHelper.split(result, "job");
			List jobDownloadable = new ArrayList();
			for (Iterator iter = jobList.iterator(); iter.hasNext();) {
				String element = (String) iter.next();
				if (element.indexOf("downloadable") != -1) {
					jobDownloadable.add(element);
				}
			}

			if (!jobDownloadable.isEmpty()) {
				Job[] jobsD = new Job[jobDownloadable.size()];
				int i = 0;
				for (Iterator iter = jobDownloadable.iterator(); iter.hasNext(); i++) {
					String element = (String) iter.next();
					String jobName = (String) StringHelper.split(element,
							"name").get(0);
					for (int j = 0; j < allJobsNames.length; j++) {
						String name = allJobsNames[j];
						if (name.equals(jobName)) {
							jobsD[i] = p_jobs[j];
							break;
						}
					}
				}

				return jobsD;
			} else {
				return new Job[0];
			}
		}

		private List getDownloadURL(Job p_job) throws Exception
		{
			QueryBO queryBO = new QueryBO();
			String localizedDocs = queryBO.query(QueryBO.q_getLocalizedDocs,
					accessToken, p_job.getName());
			
			List urlPrefixs = StringXmlHelper.getAttribute(localizedDocs,
					"urlPrefix");
			if (urlPrefixs == null || urlPrefixs.size() < 1)
			{
				throw new Exception("UrlPrefix is not exist.");
			}

			String urlPrefix = (String) urlPrefixs.get(0);
			
			List targetLocales = StringXmlHelper.getAttribute(localizedDocs,
					"targetLocale");
			if (targetLocales == null || targetLocales.size() < 1)
			{
				throw new Exception("targetLocale is not exist.");
			}
			
			List targetPages = new Vector();
			
			for (int i = 0; i < targetLocales.size(); i++)
			{
				StringBuffer targetPage = new StringBuffer();
				targetPage.append(urlPrefix)
					.append(File.separator)
					.append(targetLocales.get(i))
					.append(File.separator)
					.append("webservice")
					.append(File.separator)
					.append(URLEncoder.encode(p_job.getName(), "UTF-8"))
					.append(File.separator);
				
				targetPage = replaceHostUrl(targetPage.toString());
				
				List files = p_job.getFileMappedList();
				for (int j = 0; j < files.size(); j++)
				{
					FileMapped file = (FileMapped) files.get(j);
					String fileName = file.getFile().getPath();
					fileName = targetPage.toString() + URLEncoder.encode(fileName.substring(fileName.indexOf(File.separator) + 1), "UTF-8");
					targetPages.add(fileName);
				}
			}
			
			return targetPages;
		}

        private StringBuffer replaceHostUrl(String targetPageUrl)
        {
            String httpPrefix = "http" + (m_user.isUseSSL()? "s" : "") + "://" + m_user.getHost() + "/";
            String tmpUrl = targetPageUrl.toLowerCase();
            int indexOfGS = tmpUrl.indexOf("globalsight");
            
            StringBuffer targetPage = new StringBuffer(targetPageUrl);
            if (indexOfGS != -1)
            {
                targetPage.replace(0, indexOfGS, httpPrefix);
            }
            
            return targetPage;
        }
	}
	
	private void saveToCVS(Job p_job, String p_url, File p_file, int i) {
		if (p_file == null)
			return;
		try {
			String urlDecode = URLDecoder.decode(p_url, "UTF-8");
			String url = urlDecode.substring(urlDecode.indexOf("cxedocs") + 8);
			
			url = url.replace("/", "\\");
			String[] tmp = url.split("\\\\");
			
			if (tmp.length < 6)
				return;
			
			if (!tmp[2].equals("webservice"))
			{
			    String[] tmp2 = new String[tmp.length + 1];
			    for (int j = tmp.length - 1; j >=2; j--)
			    {
			        tmp2[j + 1] = tmp[j];
			    }
			    
			    tmp2[0] = tmp[0];
			    tmp2[1] = tmp[1];
			    tmp2[2] = "webservice";
			    tmp = tmp2;
			}
			
			String targetLocale = tmp[1];
			String filePath = ((FileMapped)p_job.getFileMappedList().get(i)).getFile().getAbsolutePath();
			StringBuffer targetFile = new StringBuffer(filePath.substring(0, filePath.indexOf(File.separator)));
			targetFile.append(File.separator).append(tmp[4]);
			StringBuffer sourceModule = new StringBuffer("/");
			for (int k=5;k<tmp.length-1;k++)
				sourceModule.append(tmp[k]).append("/");
			String sourceLocale = p_job.getSourceLocale();
			ModuleMappingHelper helper = new ModuleMappingHelper();
			ModuleMapping mm = helper.getModuleMapping(sourceLocale.toUpperCase(), sourceModule.toString().toUpperCase(), targetLocale.toUpperCase(), null, p_job.getOwner().getName().toUpperCase(), true);
			String targetFilePath = targetFile.append(mm.getTargetModule()).toString();
			String targetFileName = tmp[tmp.length-1]; //).toString().replaceAll("//", File.separator);
			TargetModuleMapping tmm = helper.getTargetModuleMapping(mm.getId());
			ArrayList fileNames = tmm.getFileRenames();
			if (fileNames != null && fileNames.size()>0 ) {
				//Change the target filename
				FileRename fr = null;
				for (int j = 0; j < fileNames.size(); j++) {
					fr = (FileRename)fileNames.get(j);
					targetFileName = targetFileName.replaceAll(fr.getSourceFilename(), fr.getTargetFilename());
				}
			}
			targetFilePath = (targetFilePath + targetFileName).replaceAll("//", File.separator);
			saveFile(p_file, new File(targetFilePath));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void saveFile(File p_sfile, File p_tfile) throws IOException,	FileNotFoundException
	{
		p_tfile.getParentFile().mkdirs();
		p_tfile.createNewFile();
		FileOutputStream outstream = new FileOutputStream(p_tfile);
		FileInputStream instream = new FileInputStream(p_sfile);
		int c;
		while ((c = instream.read()) != -1)
		{
			outstream.write(c);
		}
		outstream.close();
		instream.close();
		if (p_tfile.length() == 0)
		{
			p_tfile.delete();
		}
	}

}
