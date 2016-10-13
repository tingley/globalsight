package com.globalsight.action;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.entity.TM;
import com.globalsight.util.WebClientHelper;
import com.globalsight.util.XmlUtil;
import com.globalsight.www.webservices.Ambassador;

public class TMImportAction extends Action {

	private static int MAX_SEND_SIZE = 5 * 1000 * 1024; // 5M
	static Logger logger = Logger.getLogger(TMImportAction.class.getName());
	
	public static String TM_IMPORT_FAIL_FILES = "failFiles";
	public static String TM_IMPORT_STATUS = "status";
	public static String TM_IMPORT_STATUS_SUCC = "success";
	public static String TM_IMPORT_STATUS_FAIL = "failure";
	
	public static String TM_XML_TAG = "ProjectTM";

	/**
	 * Upload TMX file to server.
	 */
	
	/**
     * Upload TMX file to server.
     * 
     * @param p_accessToken
     *          Access token
     * @param p_fileName
     *          File name which will be uploaded to server.
     * @param p_contentsInBytes
     *          TMX file contents in byte[].
     *          
     */
	public void uploadTmxFile(String p_fileName, String p_tmName, byte[] p_contentsInBytes) throws Exception
    {
        Ambassador abmassador = WebClientHelper.getAmbassador();
        abmassador.uploadTmxFile(accessToken, p_fileName, p_tmName, p_contentsInBytes);
    }
	
	/**
     * Import TMX files into specified project tm.
     *  
     * @param p_tmName
     *          Project TM name to import TMX files into.
     * @param p_syncMode
     *          Synchronization options : merge, overwrite, discard. Default "merge".
     *          
     */
	public void importTmxFile(String p_tmName, String p_syncMode) throws Exception
    {
        Ambassador abmassador = WebClientHelper.getAmbassador();
        abmassador.importTmxFile(accessToken, p_tmName, p_syncMode);
    }
    
	/**
     * Get All Project TM.
     * 
     * @return TM List.
     * 
     * @param xml			The whole XML String
     * @param TM_XML_TAG	The TM XML String tag 
     * @see com.globalsight.test.XmlUtilTest.testString2Objects
     */
    public List<TM> getAllProjectTMsByList() throws Exception
    {
        String xml = getAllProjectTMs();
        return (List<TM>) XmlUtil.string2Objects(TM.class, xml, TM_XML_TAG);
    }
	 
    /**
     * Get All Project TM.
     * 
     * @return XML String
     */
    public String getAllProjectTMs() throws Exception
    {
        Ambassador abmassador = WebClientHelper.getAmbassador();
        return abmassador.getAllProjectTMs(accessToken);
    }
    
	@SuppressWarnings("unchecked")
	public Map uploadFile(String[] p_files, String p_tmName,String p_syncMode) throws Exception {
		Map result = new HashMap();
		List failFiles = new ArrayList();
		boolean isSucc = true;
		
		for (int index = 0; index < p_files.length; index++) {
			String fileName = p_files[index];
			isSucc = uploadFile(fileName, p_tmName, p_syncMode);
			if(!isSucc){
				failFiles.add(fileName);
			}
		}

		if(failFiles.size()<1){
			result.put(TM_IMPORT_STATUS, TM_IMPORT_STATUS_SUCC);
		}else{
			result.put(TM_IMPORT_STATUS, TM_IMPORT_STATUS_FAIL);
			result.put(TM_IMPORT_FAIL_FILES, failFiles);
		}
		
		return result;
	}
    
    /**
	 * Get TM instance from XML string
	 */
	/*public TM getInstanceFromXML(String xml){
		return XmlUtil.string2Object(TM.class, xml);
	}
	
	
	public static List<TM> getAllTMFromXML(String xml, String tag){
		if(null==xml || xml.length()==0){
			return null;
		}
		
		List<String> xmlList = getXMLForObject(xml, tag);
		List<TM> tmList = new ArrayList<TM>();
		
		for(int i=0;i<xmlList.size();i++){
			TM tm = XmlUtil.string2Object(TM.class, xmlList.get(i));
			System.out.println(tm+"\t\t"+tm.getName());
			tmList.add(tm);
		}
		
		return tmList;
		//return string2Objects(TM.class,xml,tag);
	}
	
	public static <T> List<T> string2Objects(Class<T> clazz,String xml, String tag){
		List<T> list = new ArrayList<T>();
		if(null==xml || xml.length()==0){
			return null;
		}
		
		List<String> xmlList = getXMLForObject(xml, tag);
		for(int i=0;i<xmlList.size();i++){
			T temp = XmlUtil.string2Object(clazz, xmlList.get(i));
			list.add(temp);
		}
		
		return list;
	}
	
	public static List<String> getXMLForObject(String xml, String tag){
		List<String> list = new ArrayList<String>();
		String preTag = "<"+tag+">";
		String endTag = "</"+tag+">";
		
		int prfPos=0,endPos = 0;
		while(prfPos>=0 && endPos>=0 && prfPos<xml.length() && endPos<xml.length()){
			prfPos = xml.indexOf(preTag,endPos);
			endPos = xml.indexOf(endTag,prfPos)+endTag.length();
			if(prfPos>=0){
				String temp = xml.substring(prfPos, endPos);
				list.add(temp);
			}
		}
		return list;
	}
*/
    
    /**
     * Upload a file to service.
     * 
     * <p>
     * If the file is too large, it will separate to several parts. So you don't
     * need to care about the file size.
     * 
     * @param p_file
     * @param p_tmName
     * @param p_syncMode
     * @throws Exception
     */
    public boolean uploadFile(String p_fileName, String p_tmName, String p_syncMode) throws Exception
    {
        boolean result = false;
    	File file = new File(p_fileName);
    	if (!file.exists())
        {
            throw new Exception("File(" + p_fileName + ") is not exist");
        }

        // Init some parameters.
        //String path = p_file.getAbsolutePath();
        String uploadName = p_fileName.substring(p_fileName.lastIndexOf(File.separator) + 1);     
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
            Ambassador abmassador = WebClientHelper.getAmbassador();
            for (int i = 0; i < fileByteList.size(); i++)
            {               
                /*HashMap map = new HashMap();
                map.put("accessToken", accessToken);
                map.put("filePath", filePath);
                map.put("jobName", p_tmName);
                map.put("syncMode", p_syncMode);
                map.put("bytes", fileByteList.get(i));
                abmassador.uploadFile(map);*/
            	abmassador.uploadTmxFile(accessToken, uploadName, p_tmName, (byte[]) fileByteList.get(i));
            }
            result = true;
        }
        catch (Exception e)
        {
        	logger.error("There is some error when uploading the tmx files:", e);
        }
        finally
        {
            if (inputStream != null)
            {
                inputStream.close();
            }
        }
        
        return result;
    }
    
	@Override
	public String execute(String[] args) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
