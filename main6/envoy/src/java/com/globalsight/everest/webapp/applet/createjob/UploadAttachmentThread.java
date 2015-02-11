package com.globalsight.everest.webapp.applet.createjob;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import netscape.javascript.JSObject;

import com.globalsight.everest.webapp.applet.common.AppletHelper;
import com.globalsight.webservices.client2.Ambassador2;
import com.globalsight.webservices.client2.WebService2ClientHelper;

public class UploadAttachmentThread extends Thread
{
    private static int MAX_SEND_SIZE = 5 * 1000 * 1024; // 5M

    private String hostName;
    private String port;
    private String userName;
    private String password;
    private boolean enableHttps;
    private String companyIdWorkingFor;
    private File file;
    private String savingPath;
    private JSObject win;

    public UploadAttachmentThread(String p_hostName, String p_port,
            String p_userName, String p_password, boolean p_enableHttps,
            String companyIdWorkingFor, File file, String p_savingPath,
            JSObject win)
    {
        this.hostName = p_hostName;
        this.port = p_port;
        this.userName = p_userName;
        this.password = p_password;
        this.enableHttps = p_enableHttps;
        this.companyIdWorkingFor = companyIdWorkingFor;
        this.file = file;
        this.savingPath = p_savingPath;
        this.win = win;
    }

    public void run()
    {
        try
        {
            CreateJobUtil.runJavaScript(win, "runAttachProgress", new Object[] { 10 });
            CreateJobUtil.runJavaScript(win, "runAttachProgress", new Object[] { 20 });
            CreateJobUtil.runJavaScript(win, "runAttachProgress", new Object[] { 30 });
            CreateJobUtil.runJavaScript(win, "runAttachProgress", new Object[] { 50 });
            try
            {
                this.uploadFile();

                CreateJobUtil.runJavaScript(win, "runAttachProgress", new Object[] { 60 });
                // 100%
                CreateJobUtil.runJavaScript(win, "runAttachProgress", new Object[] { 100 });
            }
            catch (Exception ex)
            {
                CreateJobUtil.runJavaScript(win, "runAttachProgress", new Object[] { -1 });
                AppletHelper.getErrorDlg(ex.getMessage(), null);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void uploadFile() throws Exception
    {
        if (!file.exists())
        {
            throw new Exception("File(" + file.getPath() + ") does not exist.");
        }

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
            Ambassador2 ambassador = WebService2ClientHelper.getClientAmbassador2(
                    hostName, port, userName, password, enableHttps);
            String fullAccessToken = ambassador.dummyLogin(userName, password);
            for (int i = 0; i < fileByteList.size(); i++)
            {
                ambassador.uploadFiles(fullAccessToken, companyIdWorkingFor, 2,
                        savingPath, (byte[]) fileByteList.get(i));
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
