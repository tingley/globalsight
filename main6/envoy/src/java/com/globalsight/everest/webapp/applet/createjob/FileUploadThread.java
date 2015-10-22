package com.globalsight.everest.webapp.applet.createjob;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import netscape.javascript.JSObject;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;

import com.globalsight.everest.webapp.applet.common.AppletHelper;
import com.globalsight.webservices.client2.Ambassador2;

import de.innosystec.unrar.rarfile.FileHeader;

public class FileUploadThread extends Thread
{
    private static int MAX_SEND_SIZE = 5 * 1000 * 1024; // 5M

    private String companyIdWorkingFor;
    private File file;
    private String savingPath;
    private JSObject win;
    private Ambassador2 ambassador;
    private String fullAccessToken;

	public FileUploadThread(String companyIdWorkingFor, File file,
			String p_savingPath, JSObject win, Ambassador2 ambassador,
			String fullAccessToken)
    {
        this.companyIdWorkingFor = companyIdWorkingFor;
        this.file = file;
        this.savingPath = p_savingPath;
        this.win = win;
        this.ambassador = ambassador;
        this.fullAccessToken = fullAccessToken;
    }

    public void run()
    {
        try
        {
            List<String> files = new ArrayList<String>();
            if (CreateJobUtil.isZipFile(file))
            {
                List<net.lingala.zip4j.model.FileHeader> entries = CreateJobUtil.getFilesInZipFile(file);
                String zipFileFullPath = file.getPath();
                String zipFilePath = zipFileFullPath.substring(0,
                        zipFileFullPath.indexOf(file.getName()));
                for (net.lingala.zip4j.model.FileHeader entry : entries)
                {
                    String zipEntryName = entry.getFileName();
                    files.add(zipFilePath
                            + file.getName().substring(0,
                                    file.getName().lastIndexOf("."))
                            + File.separator + zipEntryName);
                }
            }
            else if (CreateJobUtil.isRarFile(file))
            {
                String rarEntryName = null;
                List<FileHeader> entriesInRar = CreateJobUtil.getFilesInRarFile(file);
                String zipFileFullPath = file.getPath();
                String zipFilePath = zipFileFullPath.substring(0,
                        zipFileFullPath.indexOf(file.getName()));
                for (FileHeader header : entriesInRar)
                {
                    if (header.isUnicode())
                    {
                        rarEntryName = header.getFileNameW();
                    }
                    else
                    {
                        rarEntryName = header.getFileNameString();
                    }
                    files.add(zipFilePath
                            + file.getName().substring(0,
                                    file.getName().lastIndexOf("."))
                            + File.separator + rarEntryName);
                }
            }
            else if (CreateJobUtil.is7zFile(file))
            {
                boolean result = CreateJobUtil
                        .canBeDecompressedSuccessfully(file);
                if (result)
                {
                    List<SevenZArchiveEntry> entriesInZip7z = CreateJobUtil
                            .getFilesIn7zFile(file);
                    String zip7zFileFullPath = file.getPath();
                    String zipFilePath = zip7zFileFullPath.substring(0,
                            zip7zFileFullPath.indexOf(file.getName()));
                    for (SevenZArchiveEntry item : entriesInZip7z)
                    {
                        String zip7zEntryName = item.getName();
                        files.add(zipFilePath
                                + file.getName().substring(0,
                                        file.getName().lastIndexOf("."))
                                + File.separator + zip7zEntryName);
                    }
                }
                else
                {
                    files.add(file.getPath());
                }
            }
            else
            {
                files.add(file.getPath());
            }
            // 15%
            startProgressBar(files, 15);
            // 30%
            startProgressBar(files, 30);
            // 45%
            startProgressBar(files, 45);
            // 60%
            startProgressBar(files, 60);
            try
            {
                // If the file is a zip file, just upload the zip file, but
                // pretends that files in the zip are uploaded separately.
                this.uploadFile();

                // 70%
//                startProgressBar(files, 70);
                // 80%
//                startProgressBar(files, 80);
                // 90%
//                startProgressBar(files, 90);
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
