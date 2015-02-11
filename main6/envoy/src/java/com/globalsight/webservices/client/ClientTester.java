package com.globalsight.webservices.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ClientTester
{
	private static int MAX_SEND_SIZE = 5 * 1000 * 1024;//5M
	private static String HOST_NAME = "10.10.215.101";
	private static String HOST_PORT = "8080";
	private static String userName = "qaadmin";
	private static String password = "password";

	public static Ambassador getAmbassador() throws Exception
	{
		Ambassador ambassador = WebServiceClientHelper.getClientAmbassador(HOST_NAME, HOST_PORT, userName, password, false);
		return ambassador;
	}
	
	public static Ambassador getAmbassador(String userName, String password) throws Exception
	{
		Ambassador ambassador = WebServiceClientHelper.getClientAmbassador(HOST_NAME, HOST_PORT, userName, password, false);
		return ambassador;
	}
	
    public static void main(String[] args)
    {
    	try 
    	{
        	Ambassador ambassador = getAmbassador(userName, password);
        	String fullAccessToken = ambassador.login(userName, password);
        	System.out.println("fullAccessToken : " + fullAccessToken);
        	
        	String result = "";
//        	result = ambassador.downloadXliffOfflineFile(fullAccessToken, "247");
//        	System.out.println(result);
        	
        	System.out.println("==== Begin to upload offline file ====");
        	byte[] content = null;
        	File file = new File("e:/Downloads/test2_589424576_en_US_de_DE(1)/test2_589424576_de_DE/inbox/demo_company.html.xlf");
        	content = new byte[(int) file.length()];
        	FileInputStream fin = new FileInputStream(file);
        	fin.read(content, 0, (int) file.length());
        	
        	ambassador.uploadEditionFileBack(fullAccessToken, "247", "demo_company.html.xlf", content);
        	System.out.println("==== Upload is done ====");
        	System.out.println("==== Begin to update content ====");
        	ambassador.importOfflineTargetFiles(fullAccessToken, "247");
        	System.out.println("==== Update is done ====");
        	
//        	result = ambassador.getWorkflowPath(fullAccessToken, 12);
//        	System.out.println(result);
//        	
//        	result = ambassador.getJobsByTimeRange(fullAccessToken, "1h2d");
//        	System.out.println(result);
//        	
//        	System.out.println("**** fetchWorkflowRelevantInfoByJobs ****");
//        	result = ambassador.fetchWorkflowRelevantInfoByJobs(fullAccessToken, "1,2");
//        	System.out.println(result);
//
//        	System.out.println("**** getAllL10NProfiles ****");
//            result = ambassador.getAllL10NProfiles(fullAccessToken);
//            System.out.println(result);
//
//            System.out.println("**** getTasksInJobs ****");
//            result = ambassador.getTasksInJobs(fullAccessToken, "1,2", "Translation1_2");
//            System.out.println(result);

//        	String realAccessToken = WebServiceClientHelper.getRealAccessToken(fullAccessToken);
//        	System.out.println("realAccessToken : " + realAccessToken);
        	
//        	String allUsers = ambassador.getAllUsers(fullAccessToken);
//        	System.out.println("getAllUsers:");
//        	System.out.println(allUsers);
        	
//        	String localizedDocuments = ambassador.getLocalizedDocuments(fullAccessToken, "1001_004_2_455426079");
//        	System.out.println("getLocalizedDocuments:");
//        	System.out.println(localizedDocuments);
        	
//        	String userInfo = ambassador.getUserInfo(fullAccessToken, userName);
//        	System.out.println("getUserInfo:");
//        	System.out.println(userInfo);
        	
//        	try {
//        		String aa = ambassador.getServerVersion(realAccessToken);
//        		System.out.println(aa);
//        	} catch (Exception e) {
//        		System.out.println(e);
//        	}
        	
        	//test getXliffFileProfile()
//        	HashMap map = (HashMap) ambassador.getXliffFileProfile(realAccessToken);
//        	Iterator its = map.entrySet().iterator();
//        	while (its.hasNext()) {
//        		Map.Entry entry = (Map.Entry) its.next();
//        		long fpID = ((Long)entry.getKey()).longValue();
//        		String fpName = (String) entry.getValue();
//        		System.out.println("ID-Name :: " + fpID + "<-->" + fpName);
        		
//        		test isSupportCurrentLocalePair()
//        		String srcLangCountry = "en_US";
//        		String trgLangCountry = "de_DE";
//        		String isSupport = ambassador.isSupportCurrentLocalePair(realAccessToken, String.valueOf(fpID), srcLangCountry, trgLangCountry);
//        		System.out.println("Is supported :: " + isSupport);
//        	}
        	
        	//test getAllPermissionsByUser()
//        	String allPermissions = ambassador.getAllPermissionsByUser(realAccessToken);
//        	System.out.println(allPermissions);
        	
        	//test getFileProfileInformation()
//        	String fpInfos = ambassador.getFileProfileInformation(realAccessToken);
//        	System.out.println(fpInfos);
        	
        	//Test uploadOriginalSourceFile()
//        	testUploadOriginalSourceFile(realAccessToken);
        	
        	//Test 
//        	String taskId = String.valueOf(622);
//        	String state = String.valueOf(81);
//        	
//        	ambassador.updateTaskState(taskId, state);
        	
        	//test createEditionJob()
//        	testCreateEditionJob(realAccessToken);
        	
        	//test String.split()
//        	testStringSplit();
        	
        	//
//        	testUploadCommentReferenceFiles(realAccessToken);
        	
        	//
//        	ambassador.importOfflineTargetFiles(realAccessToken, "249");
        	
        	//Parse getAllTMProfiles() result
//        	String strAllTmProfiles = ambassador.getAllTMProfiles(realAccessToken);
//        	System.out.println("allTmProfiles :: " + strAllTmProfiles);
//        	try
//        	{
//        		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//            	DocumentBuilder db = dbf.newDocumentBuilder();
//            	InputStream stream = new ByteArrayInputStream(strAllTmProfiles.getBytes("UTF-8"));
//            	org.w3c.dom.Document doc = db.parse(stream);
//            	
//            	Element root = doc.getDocumentElement();
//            	NodeList TMProfileNL = root.getElementsByTagName("TMProfile");
//            	for (int i=0; i<TMProfileNL.getLength(); i++)
//            	{
//            		System.out.println("i :: " + i);
//            		Node subNode = TMProfileNL.item(i);
//            		if (subNode instanceof Element)
//            		{
//            			NodeList childNodeList = subNode.getChildNodes();
//                		for (int j=0; j<childNodeList.getLength(); j++)
//                		{
//                			if (childNodeList.item(j) instanceof Element)
//                			{
//                				String nodeName = childNodeList.item(j).getNodeName();
//                        		NodeList subNodeList = childNodeList.item(j).getChildNodes();
//                        		String nodeValue = null;
//                        		if (subNodeList != null && subNodeList.getLength() > 0)
//                        		{
//                            		nodeValue = subNodeList.item(0).getNodeValue();                        			
//                        		}
//                        		System.out.println("nodeName :: " + nodeName + "; nodeValue :: " + nodeValue);	
//                			}
//                		}
//            		}
//            	}
//        	}
//        	catch (Exception e)
//        	{
//        		System.out.println(e.getMessage());
//        		e.printStackTrace();
//        	}
        	
        	//
//        	String userInfoXml = ambassador.getUserInfo(realAccessToken, username);
//        	System.out.println(userInfoXml);
//        	try
//        	{
//        		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//            	DocumentBuilder db = dbf.newDocumentBuilder();
//            	InputStream stream = new ByteArrayInputStream(userInfoXml.getBytes());
//            	org.w3c.dom.Document doc = db.parse(stream);
//            	
//            	NodeList useridNL = doc.getElementsByTagName("userid");
//            	String userid = useridNL.item(0).getChildNodes().item(0).getNodeValue();
//            	
//            	NodeList firstNameNL = doc.getElementsByTagName("firstName");
//            	String firstName = firstNameNL.item(0).getChildNodes().item(0).getNodeValue();
//            	
//            	NodeList lastNameNL = doc.getElementsByTagName("lastName");
//            	String lastName = lastNameNL.item(0).getChildNodes().item(0).getNodeValue();
//            	
//            	NodeList emailNL = doc.getElementsByTagName("email");
//            	String mail = emailNL.item(0).getChildNodes().item(0).getNodeValue();
//            	
//            	NodeList statusNL = doc.getElementsByTagName("status");
//            	String status = statusNL.item(0).getChildNodes().item(0).getNodeValue();
//            	
//            	System.out.println("userid : " + userid);
//            	System.out.println("firstName : " + firstName);
//            	System.out.println("lastName : " + lastName);
//            	System.out.println("mail : " + mail);
//            	System.out.println("status : " + status);
//        	}
//        	catch (Exception e)
//        	{
//        		System.out.println(e.getMessage());
//        		e.printStackTrace();
//        	}

        	
    	}
    	catch (Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	
    }
    
    /**
    private static void testUploadOriginalSourceFile(String p_accessToken) throws Exception
    {
    	File file = new File("C:\\Documents and Settings\\york\\Desktop\\974.txt");
		if (!file.exists())
		{
			throw new Exception("File(" + file.getPath() + ") is not exist");
		}

		// Init some parameters.
		String path = file.getAbsolutePath();
		String filePath = path.substring(path.indexOf(File.separator) + 1);
		int len = (int) file.length();
		BufferedInputStream inputStream = null;
		ArrayList fileByteList = new ArrayList();

		try
		{
			inputStream = new BufferedInputStream(new FileInputStream(file));
			int size = len / MAX_SEND_SIZE;

			// Separates the file to several parts according to the size.
			for (int i = 0; i < size; i++)
			{
				byte[] fileBytes = new byte[MAX_SEND_SIZE];
				inputStream.read(fileBytes);
				fileByteList.add(fileBytes);
			}

			if (len % MAX_SEND_SIZE > 0)
			{
				byte[] fileBytes = new byte[len % MAX_SEND_SIZE];
				inputStream.read(fileBytes);
				fileByteList.add(fileBytes);
			}
			
			// Uploads all parts of files.
			Ambassador abmassador = getAmbassador();
			for (int i = 0; i < fileByteList.size(); i++)
			{				
				HashMap map = new HashMap();
				map.put("accessToken", p_accessToken);
				String jobName = "job_name_parameter";
				map.put("jobName", jobName);
				String targetLocale = "target_locale";
				map.put("targetLocale", targetLocale);
				map.put("fileName", "974.txt");
				map.put("bytes", fileByteList.get(i));
				abmassador.uploadOriginalSourceFile(map);
			}
		}
		catch (Exception e)
		{
            throw e;
		}
		finally
		{
			if (inputStream != null)
			{
				inputStream.close();
			}
		}
    }
    */
    
    /**
    private static void testCreateEditionJob(String p_accessToken) throws Exception
    {
    	Ambassador abmassador = getAmbassador();
    	
    	HashMap paramMap = new HashMap();
    	paramMap.put("accessToken", p_accessToken);
        paramMap.put("jobName", "my_job_name_001");
        
        Vector filePaths = new Vector();
        filePaths.add("log\\index.html");
        paramMap.put("filePaths", filePaths);
        
        Vector targetLocales = new Vector();
        targetLocales.add("zh_CN");
        paramMap.put("targetLocales", targetLocales);
        
        Vector fileProfileIds = new Vector();
        fileProfileIds.add("1003");
        paramMap.put("fileProfileIds", fileProfileIds);
        
        paramMap.put("taskId", "123");
        paramMap.put("priority", "3");
        paramMap.put("wsdlUrl", "http://10.10.11.206:80/globalsight/services/AmbassadorWebService?wsdl");
        paramMap.put("userName", "qaadmin1");
        paramMap.put("password", "password1");
        
        Vector jobCommentVector = new Vector();
        String comment = "6666+_+create_date+_+qaadmin+_+comment text aaa+_+123+_+T+_+7777+_+null";
        jobCommentVector.add(comment);
        paramMap.put("jobComments", jobCommentVector);
        abmassador.createEditionJob(paramMap);
    }
    */
    
    /**
    private static void testStringSplit()
    {
    	String comment = "1000+_+create_date+_+qaadmin+_+comment text aaa+_+111+_+T+_+ ";
    	String[] strs = comment.split("\\+\\_\\+");
    	for (int i=0; i<strs.length; i++)
    	{
    		System.out.println(i + " :: " + strs[i]);
    	}    	
    }
    */
    
    private static void testUploadCommentReferenceFiles(String p_accessToken) throws Exception
    {
    	File file = new File("C:\\Documents and Settings\\york\\Desktop\\974.txt");
		if (!file.exists())
		{
			throw new Exception("File(" + file.getPath() + ") is not exist");
		}

		// Init some parameters.
		String path = file.getAbsolutePath();
		String filePath = path.substring(path.indexOf(File.separator) + 1);
		int len = (int) file.length();
		BufferedInputStream inputStream = null;
		ArrayList fileByteList = new ArrayList();

		try
		{
			inputStream = new BufferedInputStream(new FileInputStream(file));
			int size = len / MAX_SEND_SIZE;

			// Separates the file to several parts according to the size.
			for (int i = 0; i < size; i++)
			{
				byte[] fileBytes = new byte[MAX_SEND_SIZE];
				inputStream.read(fileBytes);
				fileByteList.add(fileBytes);
			}

			if (len % MAX_SEND_SIZE > 0)
			{
				byte[] fileBytes = new byte[len % MAX_SEND_SIZE];
				inputStream.read(fileBytes);
				fileByteList.add(fileBytes);
			}
			
			// Uploads all parts of files.
			Ambassador abmassador = getAmbassador();
			for (int i = 0; i < fileByteList.size(); i++)
			{
				HashMap map = new HashMap();
				map.put("accessToken", p_accessToken);
				map.put("fileName", "974.txt");
				map.put("originalTaskId", "6666");
				map.put("wsdlUrl", "http://10.10.11.206:80/globalsight/services/AmbassadorWebService?wsdl");
				map.put("bytes", fileByteList.get(i));
				map.put("access", "Restricted");
				abmassador.uploadCommentReferenceFiles(map);
			}
		}
		catch (Exception e)
		{
            throw e;
		}
		finally
		{
			if (inputStream != null)
			{
				inputStream.close();
			}
		}
    }

}
