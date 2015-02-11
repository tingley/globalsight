package com.globalsight.action;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import com.globalsight.util.WebClientHelper;
import com.globalsight.www.webservices.Ambassador;

public class CreateJobAction extends Action
{
    private static int MAX_SEND_SIZE = 5 * 1000 * 1024; // 5M

    public String execute(String args[]) throws Exception
    {
        // Please don't use the methods.
        return null;
    }

    /**
     * Creates a job.
     * 
     * <p>
     * Make sure that all files has been uploaded to service.
     * <code>uploadFile(File file, String jobName, String fileProfileId)</code>
     * can used to upload files.
     * 
     * @param args
     * @throws Exception
     */
    public void createJob(HashMap args) throws Exception
    {
        Ambassador abmassador = WebClientHelper.getAmbassador();
        args.put("accessToken", accessToken);
        abmassador.createJobOnInitial(args);
    }

    /**
     * Upload a file to service.
     * 
     * <p>
     * Make sure that the job name is unique, is not sure, you can call
     * <code>getUniqueJobName(String jobName)</code> to get one. The jobName and
     * file profile id is used to compose the save path.
     * 
     * <p>
     * If the file is too large, it will separate to several parts. So you don't
     * need to care about the file size.
     * 
     * @param file
     * @param jobName
     * @param fileProfileId
     * @throws Exception
     */
    public String uploadFile(File file, String jobName, String fileProfileId,
            String jobId, String priority) throws Exception
    {
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
            Ambassador abmassador = WebClientHelper.getAmbassador();
            for (int i = 0; i < fileByteList.size(); i++)
            {
                HashMap map = new HashMap();
                map.put("jobId", jobId);
                map.put("priority", priority);
                map.put("accessToken", accessToken);
                map.put("filePath", filePath);
                map.put("jobName", jobName);
                map.put("fileProfileId", fileProfileId);
                map.put("bytes", fileByteList.get(i));
                jobId = abmassador.uploadFileForInitial(map);
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
        return jobId;
    }

    public void uploadAttributeFiles(File file, String jobName, String attName)
            throws Exception
    {
        if (!file.exists())
        {
            throw new Exception("File(" + file.getPath() + ") is not exist");
        }
        // Init some parameters.
        String path = file.getAbsolutePath();
        String filePath = path.substring(path.indexOf(File.separator) + 1);
        int len = (int) file.length();
        BufferedInputStream inputStream = null;
        ArrayList<byte[]> fileByteList = new ArrayList<byte[]>();
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
                HashMap map = new HashMap();
                map.put("accessToken", accessToken);
                abmassador.uploadAttributeFiles(accessToken, jobName, attName,
                        file.getName(), fileByteList.get(i));
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

    /**
     * Gets a unique job name from service.
     * 
     * @param jobName
     * @return
     * @throws Exception
     */
    public String getUniqueJobName(String jobName) throws Exception
    {
        Ambassador abmassador = WebClientHelper.getAmbassador();
        HashMap map = new HashMap();
        map.put("accessToken", accessToken);
        map.put("jobName", jobName);
        return abmassador.getUniqueJobName(map);
    }
}
