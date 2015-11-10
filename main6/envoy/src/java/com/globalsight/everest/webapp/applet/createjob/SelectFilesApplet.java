package com.globalsight.everest.webapp.applet.createjob;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import netscape.javascript.JSObject;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;

import com.globalsight.everest.webapp.applet.common.EnvoyJApplet;
import com.globalsight.webservices.client2.Ambassador2;
import com.globalsight.webservices.client2.WebService2ClientHelper;

import de.innosystec.unrar.rarfile.FileHeader;

public class SelectFilesApplet extends EnvoyJApplet
{
    private static final long serialVersionUID = 1L;

    private static final int MAX_THREAD = 5;

    private JLabel label;
    private JSObject win;
    private List<String> fileList;
    private String baseFolder = "";

    static 
    { 
        Locale.setDefault(Locale.ENGLISH);
    }

    public void init()
    {
        win = JSObject.getWindow(this);
        fileList = new ArrayList<String>();
        label = new JLabel();
        add(label);
        baseFolder = getParameter("lastSelectedFolder");
    }
    
    public void openChooser(final String userName, final String password,
            final String companyIdWorkingFor)
    {
        try
        {
            AccessController.doPrivileged(new PrivilegedAction<Object>()
            {

                @Override
                public Object run()
                {
                    MyJFileChooser chooser = new MyJFileChooser(baseFolder);
                    chooser.setMultiSelectionEnabled(true);
                    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    chooser.setApproveButtonText("Add Files");
                    chooser.setApproveButtonToolTipText("Add files or directories to create job");
                    int state = chooser.showOpenDialog(label);
                    if (state == JFileChooser.APPROVE_OPTION)
                    {
                        File[] files = chooser.getSelectedFiles();
                        baseFolder = files[0].getParent();

                        String os = System.getProperty("os.name").toLowerCase();
                        // show confirm message only on Windows
                        if (os.indexOf("win") != -1)
                        {
                            for (File file : files)
                            {
                                if (file.isDirectory())
                                {
                                    int selection = JOptionPane
                                            .showConfirmDialog(
                                                    null,
                                                    "You've selected one or more folders.\nDo you want to upload all files under the folder(s)?",
                                                    "Add Files",
                                                    JOptionPane.OK_CANCEL_OPTION);
                                    if (selection == JOptionPane.CANCEL_OPTION)
                                    {
                                        return null;
                                    }
                                    break;
                                }
                            }
                        }
                        CreateJobUtil.runJavaScript(win, "setBaseFolder",
                                new Object[]
                                { baseFolder });

                        List<File> allSelectedFiles = new ArrayList<File>();
                        for (File file : files)
                        {
                            allSelectedFiles
                                    .addAll(getFilesUnderDirectory(file));
                        }
                        try
                        {
                            List<File> properFiles = performValidation(allSelectedFiles);
                            if (properFiles.size() > 0)
                            {
                                performUpload(properFiles, userName, password,
                                        companyIdWorkingFor);
                            }
                        }
                        catch (Exception e)
                        {
                        }
                    }
                    return null;
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void destroy() 
    {
        super.destroy();
    }
    
    /**
     * Validate files, remove empty files, large files and existing files.
     * @param files
     * @throws Exception 
     */
    private List<File> performValidation(List<File> files) throws Exception
    {
        List<File> left = new ArrayList<File>();
        List<String> empty = new ArrayList<String>();
        List<String> large = new ArrayList<String>();
        List<String> exist = new ArrayList<String>();

        for (File file : files)
        {
            if (file.length() == 0)
            {
                empty.add(file.getPath());
            }
            else if (CreateJobUtil.isZipFile(file))
            {
                List<net.lingala.zip4j.model.FileHeader> entries = CreateJobUtil.getFilesInZipFile(file);
                String zipFileFullPath = file.getPath();
                String zipFilePath = zipFileFullPath.substring(0,
                        zipFileFullPath.indexOf(file.getName()));
                for (net.lingala.zip4j.model.FileHeader entry : entries)
                {
                    String unzippedFileFullPath = zipFilePath
                            + file.getName().substring(0,
                                    file.getName().lastIndexOf("."))
                            + File.separator + entry.getFileName();
                    String id = CreateJobUtil.getFileId(unzippedFileFullPath);
                    if (fileList.contains(id))
                    {
                        exist.add(unzippedFileFullPath);
                    }
                    else
                    {
                        fileList.add(id);
                        if (!left.contains(file))
                        {
                            left.add(file);
                        }
                    }
                }
            }
            else if (CreateJobUtil.isRarFile(file))
            {
                try
                {
                    List<FileHeader> entriesInRar = CreateJobUtil
                            .getFilesInRarFile(file);
                    String rarFileFullPath = file.getPath();
                    String rarFilePath = rarFileFullPath.substring(0,
                            rarFileFullPath.indexOf(file.getName()));
                    for (FileHeader header : entriesInRar)
                    {
                        String unzippedFileFullPath = rarFilePath
                                + file.getName().substring(0,
                                        file.getName().lastIndexOf("."))
                                + File.separator + header.getFileNameString();
                        String id = CreateJobUtil
                                .getFileId(unzippedFileFullPath);
                        if (fileList.contains(id))
                        {
                            exist.add(unzippedFileFullPath);
                        }
                        else
                        {
                            fileList.add(id);
                            if (!left.contains(file))
                            {
                                left.add(file);
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                }
            }
            else if (CreateJobUtil.is7zFile(file))
            {
                boolean result = CreateJobUtil
                        .canBeDecompressedSuccessfully(file);
                if (result)
                {
                    try
                    {
                        List<SevenZArchiveEntry> entriesInZip7z = CreateJobUtil
                                .getFilesIn7zFile(file);
                        String zip7zFileFullPath = file.getPath();
                        String zip7zFilePath = zip7zFileFullPath.substring(0,
                                zip7zFileFullPath.indexOf(file.getName()));
                        for (SevenZArchiveEntry item : entriesInZip7z)
                        {
                            String unzippedFileFullPath = zip7zFilePath
                                    + file.getName().substring(0,
                                            file.getName().lastIndexOf("."))
                                    + File.separator + item.getName();
                            String id = CreateJobUtil
                                    .getFileId(unzippedFileFullPath);
                            if (fileList.contains(id))
                            {
                                exist.add(unzippedFileFullPath);
                            }
                            else
                            {
                                fileList.add(id);
                                if (!left.contains(file))
                                {
                                    left.add(file);
                                }
                            }
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }
                else
                {
                    String id = CreateJobUtil.getFileId(file.getPath());
                    if (fileList.contains(id))
                    {
                        exist.add(file.getPath());
                    }
                    else
                    {
                        fileList.add(id);
                        left.add(file);
                    }
                }
            }
            else
            {
                String id = CreateJobUtil.getFileId(file.getPath());
                if (fileList.contains(id))
                {
                    exist.add(file.getPath());
                }
                else
                {
                    fileList.add(id);
                    left.add(file);
                }
            }
        }

        if (empty.size() != 0 || large.size() != 0 || exist.size() != 0)
        {
            StringBuilder sb = new StringBuilder();
            for (String emp : empty)
            {
                sb.append(emp).append(" is empty, ignored.\n");
            }
            for (String larg : large)
            {
                sb.append(larg).append(" is larger than 35M, ignored.\n");
            }
            for (String exi : exist)
            {
                sb.append(exi).append(" is already in the uploaded list, ignored.\n");
            }
            if (sb.length() > 0)
            {
                JOptionPane.showMessageDialog(null, sb.toString(), "Add Files",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
        return left;
    }
    
    /**
     * Get all files under a directory
     * 
     * @param dir
     * @return 
     */
    private List<File> getFilesUnderDirectory(File dir)
    {
        List<File> res = new ArrayList<File>();
        if (dir.isFile())
        {
            res.add(dir);
            return res;
        }
        else
        {
            String[] filesAndDirs = dir.list();
            for (String name : filesAndDirs)
            {
                String path = dir.getPath();
                File subFile = new File(path + File.separator + name);
                if (subFile.isFile() && !subFile.isHidden() && subFile.canRead())
                {
                    res.add(subFile);
                }
                else if (subFile.isDirectory())
                {
                    res.addAll(getFilesUnderDirectory(subFile));
                }
            }
            return res;
        }
    }

    /**
     * Perform the upload process by sending the files to the server, empty
     * files will be ignored. If there are no errors, go to the next page to
     * display the upload result.
     * 
     * @param files
     * @throws Exception 
     */
    private void performUpload(List<File> files, String userName,
            String password, String companyIdWorkingFor) throws Exception
    {
        this.addProgressBarForAllFiles(files);

        URL url = this.getCodeBase();
        String hostName = url.getHost();
        int port = url.getPort();
        String protocol = url.getProtocol();
        boolean enableHttps = protocol.contains("s") ? true : false;

        String tmpJobFileFolder = getParameter("folderName");
        String savingPath = "createJob_tmp" + File.separator + tmpJobFileFolder;

        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREAD);
        for (File file : files)
        {
            String filePath = file.getAbsolutePath();
            if (filePath.contains(":"))
            {
                filePath = filePath.substring(filePath.indexOf(":") + 1);
            }
            String originalFilePath = filePath.replace("\\", File.separator)
                    .replace("/", File.separator);
            String filePathName = savingPath + File.separator + originalFilePath;

			Ambassador2 ambassador = WebService2ClientHelper
					.getClientAmbassador2(hostName, String.valueOf(port),
							userName, password, enableHttps);
            String fullAccessToken = ambassador.dummyLogin(userName, password);
			FileUploadThread tt = new FileUploadThread(companyIdWorkingFor,
					file, filePathName, win, ambassador, fullAccessToken);
            pool.execute(tt);
        }

        pool.shutdown();
    }
    
    /**
     * Add progress bar for all selected files.
     * Empty files are igored.
     * 
     * @param files
     * @throws Exception 
     */
    private void addProgressBarForAllFiles(List<File> files) throws Exception
    {
        StringBuffer ret = new StringBuffer("[");
        
        for (File file : files)
        {
            if (ret.length() > 1)
            {
                ret.append(",");
            }
            if (CreateJobUtil.isZipFile(file))
            {
                ret.append(addZipFile(file));
            }
            else if (CreateJobUtil.isRarFile(file))
            {
                ret.append(addRarFile(file));
            }
            else if (CreateJobUtil.is7zFile(file))
            {
                boolean result = CreateJobUtil
                        .canBeDecompressedSuccessfully(file);
                if (result)
                {
                    ret.append(add7zFile(file));
                }
                else
                {
                    ret.append(addCommonFile(file));
                }
            }
            else
            {
                ret.append(addCommonFile(file));
            }
        }

        ret.append("]");
        CreateJobUtil.runJavaScript(win, "addDivForNewFile", new Object[] {ret.toString()} );
    }

    /**
     * Add a progress bar for each files within a zip file.
     * @param file
     * @throws Exception 
     */
    private String addZipFile(File file) throws Exception
    {
        String zipFileFullPath = file.getPath();
        String zipFilePath = zipFileFullPath.substring(0,
                zipFileFullPath.indexOf(file.getName()));
        
        List<net.lingala.zip4j.model.FileHeader> entriesInZip = CreateJobUtil.getFilesInZipFile(file);
        
        StringBuffer ret = new StringBuffer("");
        for (net.lingala.zip4j.model.FileHeader entry : entriesInZip)
        {
            if (ret.length() > 0)
            {
                ret.append(",");
            }
            String zipEntryName = entry.getFileName();
            
            /* The unzipped files are in folders named by the zip file name*/
             
            String unzippedFileFullPath = zipFilePath
                    + file.getName().substring(0,
                            file.getName().lastIndexOf(".")) + File.separator
                    + zipEntryName;
            // if zip file contains subfolders, entry name will contains "/" or "\"
            if (zipEntryName.indexOf("/") != -1)
            {
                zipEntryName = zipEntryName.substring(zipEntryName
                        .lastIndexOf("/") + 1);
            }
            else if (zipEntryName.indexOf("\\") != -1)
            {
                zipEntryName = zipEntryName.substring(zipEntryName
                        .lastIndexOf("\\") + 1);
            }
            String id = CreateJobUtil.getFileId(unzippedFileFullPath);
            ret.append("{id:'")
                    .append(id)
                    .append("',path:'")
                    .append(unzippedFileFullPath.replace("\\", File.separator)
                            .replace("/", File.separator).replace("\\", "\\\\").replace("'", "\\'"))
                    .append("',name:'").append(zipEntryName.replace("'", "\\'")).append("',size:'")
                    .append(entry.getUncompressedSize()).append("'}");
        }
        return ret.toString();
    }
    
    private String addRarFile(File file) throws Exception
    {
        String zipEntryName = null;
        String rarFileFullPath = file.getPath();
        String rarFilePath = rarFileFullPath.substring(0,
                rarFileFullPath.indexOf(file.getName()));
        
        List<FileHeader> entriesInRar = CreateJobUtil.getFilesInRarFile(file);
        
        StringBuffer ret = new StringBuffer("");
        for (FileHeader header : entriesInRar)
        {
            if (ret.length() > 0)
            {
                ret.append(",");
            }
            if (header.isUnicode())
            {
                zipEntryName = header.getFileNameW();
            }
            else
            {
                zipEntryName = header.getFileNameString();
            }
            /*
             * The unzipped files are in folders named by the zip file name
             */
            String unzippedFileFullPath = rarFilePath
                    + file.getName().substring(0,
                            file.getName().lastIndexOf(".")) + File.separator
                    + zipEntryName;
            // if zip file contains subfolders, entry name will contains "/" or "\"
            if (zipEntryName.indexOf("/") != -1)
            {
                zipEntryName = zipEntryName.substring(zipEntryName
                        .lastIndexOf("/") + 1);
            }
            else if (zipEntryName.indexOf("\\") != -1)
            {
                zipEntryName = zipEntryName.substring(zipEntryName
                        .lastIndexOf("\\") + 1);
            }
            String id = CreateJobUtil.getFileId(unzippedFileFullPath);
            ret.append("{id:'")
                    .append(id)
                    .append("',path:'")
                    .append(unzippedFileFullPath.replace("\\", File.separator)
                            .replace("/", File.separator).replace("\\", "\\\\").replace("'", "\\'"))
                    .append("',name:'").append(zipEntryName.replace("'", "\\'")).append("',size:'")
                    .append(header.getDataSize()).append("'}");
        }
        return ret.toString();
    } 
    
    private String add7zFile(File file) throws Exception
    {
        String zip7zFileFullPath = file.getPath();
        String zip7zFilePath = zip7zFileFullPath.substring(0,
                zip7zFileFullPath.indexOf(file.getName()));
        
        List<SevenZArchiveEntry> entriesInZip7z = CreateJobUtil
                .getFilesIn7zFile(file);

        StringBuffer ret = new StringBuffer("");
        for (SevenZArchiveEntry item : entriesInZip7z)
        {
            if (ret.length() > 0)
            {
                ret.append(",");
            }
            String zip7zEntryName = item.getName();
            
             /** The unzipped files are in folders named by the zip file name*/
             
            String unzippedFileFullPath = zip7zFilePath
                    + file.getName().substring(0,
                            file.getName().lastIndexOf(".")) + File.separator
                    + zip7zEntryName;
            // if zip file contains subf,olders, entry name will contains "/" or
            // "\"
            if (zip7zEntryName.indexOf("/") != -1)
            {
                zip7zEntryName = zip7zEntryName.substring(zip7zEntryName
                        .lastIndexOf("/") + 1);
            }
            else if (zip7zEntryName.indexOf("\\") != -1)
            {
                zip7zEntryName = zip7zEntryName.substring(zip7zEntryName
                        .lastIndexOf("\\") + 1);
            }
            String id = CreateJobUtil.getFileId(unzippedFileFullPath);
            ret.append("{id:'")
                    .append(id)
                    .append("',zipName:'")
                    .append(file.getName().replace("'", "\\'"))
                    .append("',path:'")
                    .append(unzippedFileFullPath.replace("\\", File.separator)
                            .replace("/", File.separator).replace("\\", "\\\\")
                            .replace("'", "\\'")).append("',name:'")
                    .append(zip7zEntryName.replace("'", "\\'"))
                    .append("',size:'").append(item.getSize()).append("'}");
        }
        return ret.toString();
    }
    
    
    /**
     * Add a progress bar for a common file.
     * @param file
     */
    private String addCommonFile(File file)
    {
        String id = CreateJobUtil.getFileId(file.getPath());
        StringBuffer ret = new StringBuffer("");
        ret.append("{id:'").append(id).append("',path:'")
                .append(file.getPath().replace("\\", "\\\\").replace("'", "\\'"))
                .append("',name:'").append(file.getName().replace("'", "\\'")).append("',size:'")
                .append(file.length()).append("'}");
        return ret.toString();
    }
    
    /**
     * Remove file from file list
     * @param id
     */
    public void removeFileFromList(String id)
    {
        if (fileList.contains(id))
        {
            fileList.remove(id);
        }
    }
    
}

// Override the position of JFileChooser.
class MyJFileChooser extends JFileChooser
{
    private static final long serialVersionUID = 1L;

    public MyJFileChooser(String baseFolder)
    {
        super(baseFolder);
    }

    protected JDialog createDialog(Component parent) throws HeadlessException
    {
        JDialog dlg = super.createDialog(parent);
        dlg.setLocationRelativeTo(null);
        return dlg;
    }
}
