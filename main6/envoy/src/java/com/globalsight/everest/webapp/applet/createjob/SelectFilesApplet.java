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
import java.util.zip.ZipEntry;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import netscape.javascript.JSObject;

import com.globalsight.everest.webapp.applet.common.EnvoyJApplet;

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
                        if (os.indexOf("win") != -1)// show confirm message only on Windows
                        {
                            for (File file : files)
                            {
                                if (file.isDirectory())
                                {
                                    int selection = JOptionPane.showConfirmDialog(
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
                        CreateJobUtil.runJavaScript(win, "setBaseFolder", new Object[]
                        { baseFolder });

                        List<File> allSelectedFiles = new ArrayList<File>();
                        for (File file : files)
                        {
                            allSelectedFiles.addAll(getFilesUnderDirectory(file));
                        }
                        List<File> properFiles = performValidation(allSelectedFiles);
                        if (properFiles.size() > 0)
                        {
                            performUpload(properFiles, userName, password, companyIdWorkingFor);
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
     */
    private List<File> performValidation(List<File> files)
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
                List<ZipEntry> entries = CreateJobUtil.getFilesInZipFile(file);
                String zipFileFullPath = file.getPath();
                String zipFilePath = zipFileFullPath.substring(0,
                        zipFileFullPath.indexOf(file.getName()));
                for (ZipEntry entry : entries)
                {
                    String unzippedFileFullPath = zipFilePath
                            + file.getName().substring(0,
                                    file.getName().lastIndexOf("."))
                            + File.separator + entry.getName();
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
     */
    private void performUpload(List<File> files, String userName,
            String password, String companyIdWorkingFor)
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

            FileUploadThread tt = new FileUploadThread(hostName,
                    String.valueOf(port), userName, password, enableHttps,
                    companyIdWorkingFor, file, filePathName, win);
            pool.execute(tt);
        }

        pool.shutdown();
    }
    
    /**
     * Add progress bar for all selected files.
     * Empty files are igored.
     * 
     * @param files
     */
    private void addProgressBarForAllFiles(List<File> files)
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
     */
    private String addZipFile(File file)
    {
        String zipFileFullPath = file.getPath();
        String zipFilePath = zipFileFullPath.substring(0,
                zipFileFullPath.indexOf(file.getName()));
        
        List<ZipEntry> entriesInZip = CreateJobUtil.getFilesInZipFile(file);
        
        StringBuffer ret = new StringBuffer("");
        for (ZipEntry entry : entriesInZip)
        {
            if (ret.length() > 0)
            {
                ret.append(",");
            }
            String zipEntryName = entry.getName();
            /*
             * The unzipped files are in folders named by the zip file name
             */
            String unzippedFileFullPath = zipFilePath
                    + file.getName().substring(0,
                            file.getName().lastIndexOf(".")) + File.separator
                    + zipEntryName;
            // if zip file contains subfolders, entry name will contains "/" or "\"
            if (zipEntryName.indexOf("/") != 0)
            {
                zipEntryName = zipEntryName.substring(zipEntryName
                        .lastIndexOf("/") + 1);
            }
            else if (zipEntryName.indexOf("\\") != 0)
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
                    .append(entry.getSize()).append("'}");
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
