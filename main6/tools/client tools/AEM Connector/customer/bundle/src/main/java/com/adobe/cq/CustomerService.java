package com.adobe.cq;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Session;

public interface CustomerService {

	public int insertGlobalsightData(String hostName, String hostPort, String username, String password, String desc, String enableHttps , String checkInterval);
	
	public String getCurrentUserId(Session session);
	
	public String getCurrentNodeTitle(Node node) throws Exception;

	public int hasAccessToken();
	
	public String getToken();
	
	public String getXmlFileProfileExt();
	
	public String getJobStatus(String jobName);
	
	public String getJobExportFile(String jobName);
	
	public Map<String, String> getGlobalsightData();
	
	public String getGlobalSightUrl();
	
	public File createPageTempFile(File folder, String pageKey);
	
	/**
	 * 
	 * @param folder temple folder
	 * @param pageKeys	translate pages
	 * @param target translate language
	 * @return
	 */
	public List<File> createPageTempFile(File folder, String pageKeys[], String target);
	
	public void persisJob(Set<JobBean> jobs);
	
	public void persisJob(JobBean job);
	
	public Set<JobBean> getInProgressJob();

	public void updateJobStatus(JobBean job);
	
	public void removeCartItems(String[] pKeys);
	
	public String getCheckInterval();
}
