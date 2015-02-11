package com.globalsight.everest.webapp.applet.createjob;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

import netscape.javascript.JSObject;

import com.globalsight.everest.webapp.applet.common.AppletHelper;
import com.globalsight.webservices.client2.Ambassador2;
import com.globalsight.webservices.client2.WebService2ClientHelper;

public class FileUploadThread extends Thread
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

    public FileUploadThread(String p_hostName, String p_port,
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
            List<String> files = new ArrayList<String>();
            if (CreateJobUtil.isZipFile(file))
            {
                List<ZipEntry> entries = CreateJobUtil.getFilesInZipFile(file);
                String zipFileFullPath = file.getPath();
                String zipFilePath = zipFileFullPath.substring(0,
                        zipFileFullPath.indexOf(file.getName()));
                for (ZipEntry entry : entries)
                {
                    String zipEntryName = entry.getName();
                    files.add(zipFilePath
                            + file.getName().substring(0,
                                    file.getName().lastIndexOf("."))
                            + File.separator + zipEntryName);
                }
            }
            else
            {
                files.add(file.getPath());
            }
            // 10%
            startProgressBar(files, 10);
            // 20%
            startProgressBar(files, 20);
            // 30%
            startProgressBar(files, 30);
            // 40%
            startProgressBar(files, 40);
            // 50%
            startProgressBar(files, 50);
            // 60%
            startProgressBar(files, 60);
            try
            {
                // If the file is a zip file, just upload the zip file, but
                // pretends that files in the zip are uploaded separately.
                this.uploadFile();

                // 70%
                startProgressBar(files, 70);
                // 80%
                startProgressBar(files, 80);
                // 90%
                startProgressBar(files, 90);
                // 100%
                startProgressBar(files, 100);
            }
            catch (Exception ex)
            {
                resetProgressBar(win, files);
                ex.printStackTrace();
                AppletHelper.getErrorDlg(ex.getMessage(), null);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Reset progress bars
     * @param win
     * @param files
     */
    private void resetProgressBar(JSObject win, List<String> files)
    {
        for (String filePath : files)
        {
            String fileId = CreateJobUtil.getFileId(filePath);
            CreateJobUtil.runJavaScript(win, "uploadError", new Object[]{ fileId });
        }
    }
    
    /**
     * Start rolling of progress bars
     * @param files
     * @param percentage
     */
    private void startProgressBar(List<String> files, int percentage)
    {
        for (String filePath : files)
        {
            String fileId = CreateJobUtil.getFileId(filePath);
            CreateJobUtil.runJavaScript(win, "runProgress", new Object[]
            { fileId, percentage });
        }
    }

    private void uploadFile() throws Exception
    {
        if (!file.exists())
        {
            throw new Exception("File(" + file.getPath() + ") does not exist.");
        }

        // Init some parameters.
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
                ambassador.uploadFiles(fullAccessToken, companyIdWorkingFor, 1,
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
