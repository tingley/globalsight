package com.globalsight.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import com.globalsight.action.AddCommentAction;
import com.globalsight.action.CreateJobAction;
import com.globalsight.entity.FileMapped;
import com.globalsight.entity.FileProfile;
import com.globalsight.entity.Job;
import com.globalsight.entity.User;
import com.globalsight.ui.attribute.AttributeTableModel;
import com.globalsight.ui.attribute.vo.Attributes;
import com.globalsight.ui.attribute.vo.FileJobAttributeVo;
import com.globalsight.ui.attribute.vo.JobAttributeVo;
import com.globalsight.util.Constants;
import com.globalsight.util.UsefulTools;
import com.globalsight.util.XmlUtil;
import com.globalsight.util2.CacheUtil;
import com.globalsight.util2.ConfigureHelperV2;

public class UploadFilesDialog extends JDialog
{

    private static final long serialVersionUID = 924879905158920329L;

    static Logger log = Logger.getLogger(UploadFilesDialog.class.getName());

    public UploadFilesDialog(Frame p_owner)
    {
        super(p_owner, Constants.UPLOADING, true);
        initPanel();
        setHiddenAble(false);
    }

    public void show()
    {
        startAmimate();
        if (isInstallParameters)
            commitJob();
        super.show();
    }

    private void initPanel()
    {
        setSize(new Dimension(450, 300));

        Container contentPane = this.getContentPane();
        contentPane.setLayout(new GridBagLayout());
        GridBagConstraints c1 = new GridBagConstraints();
        c1.insets = new Insets(2, 2, 2, 2);

        jProgressBar = new JProgressBar();
        jProgressBar.setMaximum(100);
        jProgressBar.setMinimum(0);
        c1.gridx = 0;
        c1.gridy = 0;
        c1.gridwidth = 4;
        c1.gridheight = 1;
        contentPane.add(jProgressBar, c1);

        jLabelMsg = new JLabel(commitFile);
        c1.gridx = 0;
        c1.gridy = 1;
        c1.gridwidth = 4;
        c1.gridheight = 1;
        contentPane.add(jLabelMsg, c1);

        jTextArea = new JTextArea("", 10, 30);
        jTextArea.setLineWrap(true);
        jTextArea.setEditable(false);
        JScrollPane jsp = new JScrollPane(jTextArea);
        c1.gridx = 0;
        c1.gridy = 2;
        c1.gridwidth = 4;
        c1.gridheight = 4;
        contentPane.add(jsp, c1);

        okButton = new JButton(" OK ");
        okButton.setEnabled(false);
        c1.gridx = 2;
        c1.gridy = 6;
        c1.gridwidth = 2;
        c1.gridheight = 1;
        contentPane.add(okButton, c1);

        okButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
            }
        });

        // popup menu
        pMenu = new PopupMenu();
        MenuItem mi_log = new MenuItem("View log file");
        MenuItem mi_close = new MenuItem("Close me");
        pMenu.add(mi_log);
        pMenu.add(mi_close);
        mi_log.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                UsefulTools.openFile(Constants.LOG_FILE);
            }
        });
        mi_close.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                closeMe();
            }
        });
        contentPane.add(pMenu);
        contentPane.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                pMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                closeMe();
            }
        });
    }

    public void commitJob(String p_jobName, DefaultListModel p_files,
            DefaultListModel p_fps, String p_commentText, String p_attachFile,
            String p_jobCreateOption, String p_jobFileNumOrSize)
    {
        m_jobName = p_jobName;
        m_files = p_files;
        m_fps = p_fps;
        m_commentText = p_commentText;
        m_attachFile = p_attachFile;
        m_isCVSJob = false;
        isInstallParameters = true;
        m_jobCreateOption = p_jobCreateOption;
        m_jobFileNumOrSize = p_jobFileNumOrSize;
    }

    public void commitJob(String p_jobName, DefaultListModel p_files,
            DefaultListModel p_fps, String p_commentText, String p_attachFile,
            String p_jobCreateOption, String p_jobFileNumOrSize,
            String p_priority, AttributeTableModel attributes)
    {
        m_jobName = p_jobName;
        m_files = p_files;
        m_fps = p_fps;
        m_commentText = p_commentText;
        m_attachFile = p_attachFile;
        m_isCVSJob = false;
        isInstallParameters = true;
        m_jobCreateOption = p_jobCreateOption;
        m_jobFileNumOrSize = p_jobFileNumOrSize;
        m_priority = p_priority;
        this.attributes = attributes;
    }

    public void commitCVSJob(String p_jobName, DefaultListModel p_files,
            DefaultListModel p_fps, String p_commentText, String p_attachFile,
            String p_jobCreateOption, String p_jobFileNumOrSize,
            AttributeTableModel attributes)
    {
        m_jobName = p_jobName;
        m_files = p_files;
        m_fps = p_fps;
        m_commentText = p_commentText;
        m_attachFile = p_attachFile;
        m_isCVSJob = true;
        isInstallParameters = true;
        m_jobCreateOption = p_jobCreateOption;
        m_jobFileNumOrSize = p_jobFileNumOrSize;
        this.attributes = attributes;
    }

    public void commitCVSJob(String p_jobName, DefaultListModel p_files,
            DefaultListModel p_fps, String p_commentText, String p_attachFile,
            String p_jobCreateOption, String p_jobFileNumOrSize,
            String p_priority, AttributeTableModel attributes)
    {
        m_jobName = p_jobName;
        m_files = p_files;
        m_fps = p_fps;
        m_commentText = p_commentText;
        m_attachFile = p_attachFile;
        m_isCVSJob = true;
        isInstallParameters = true;
        m_jobCreateOption = p_jobCreateOption;
        m_jobFileNumOrSize = p_jobFileNumOrSize;
        m_priority = p_priority;
        this.attributes = attributes;
    }

    // use thread because Dialog.show() ...
    private void commitJob()
    {
        Thread thread = new Thread("UploadFilesDialog - commitJob")
        {
            public void run()
            {
                // copy all files to ArrayList
                List<File> fileList = new ArrayList<File>();
                Enumeration files = m_files.elements();
                double totalFileSize = 0;
                while (files != null && files.hasMoreElements())
                {
                    File file = (File) files.nextElement();
                    totalFileSize += file.length();
                    fileList.add(file);
                }

                // copy all file profiles to ArrayList
                List<FileProfile> fpList = new ArrayList<FileProfile>();
                Enumeration fps = m_fps.elements();
                while (fps != null && fps.hasMoreElements())
                {
                    FileProfile fp = (FileProfile) fps.nextElement();
                    fpList.add(fp);
                }

                // copy job name
                String tempjobName = m_jobName;

                // create job via "max file size"
                if (m_jobCreateOption != null
                        && m_jobCreateOption
                                .equals(Constants.JOB_CREATE_OPTION_MAX_FILE_SIZE))
                {
                    // get maxJobFileSize in bytes
                    double maxJobFileSizeInBytes = totalFileSize + 1024;
                    boolean ifSortJob = true;
                    try
                    {
                        maxJobFileSizeInBytes = ((new Double(m_jobFileNumOrSize))
                                .doubleValue() + 1) * 1024;
                        if (maxJobFileSizeInBytes > totalFileSize)
                        {
                            ifSortJob = false;
                        }
                    }
                    catch (Exception e)
                    {
                    }

                    // commit job in loop
                    m_files.removeAllElements();
                    m_fps.removeAllElements();
                    double totalSize = 0;
                    int jobNumber = 0;

                    for (int k = 0; k < fileList.size(); k++)
                    {
                        File file = (File) fileList.get(k);
                        m_files.addElement(file);
                        FileProfile fp = (FileProfile) fpList.get(k);
                        m_fps.addElement(fp);

                        totalSize += file.length();
                        if (totalSize >= maxJobFileSizeInBytes)
                        {
                            if (m_files.size() <= 1)
                            {
                                if (ifSortJob)
                                {
                                    jobNumber++;
                                    m_jobName = tempjobName + "_" + jobNumber;
                                }
                                commitSingleJob();
                                m_files.removeAllElements();
                                m_fps.removeAllElements();

                                totalSize = 0;
                            }
                            else
                            {
                                m_files.removeElement(file);
                                m_fps.removeElement(fp);
                                if (ifSortJob)
                                {
                                    jobNumber++;
                                    m_jobName = tempjobName + "_" + jobNumber;
                                }
                                commitSingleJob();
                                m_files.removeAllElements();
                                m_files.addElement(file);
                                m_fps.removeAllElements();
                                m_fps.addElement(fp);
                                totalSize = file.length();
                            }
                        }
                    }
                    if (m_files.size() > 0)
                    {
                        if (ifSortJob)
                        {
                            jobNumber++;
                            m_jobName = tempjobName + "_" + jobNumber;
                        }
                        commitSingleJob();
                        m_files.removeAllElements();
                        m_fps.removeAllElements();
                    }
                }
                else
                {
                    // get maxJobFileNum
                    int maxJobFileNum = m_files.getSize();
                    boolean ifSortJob = true;
                    try
                    {
                        maxJobFileNum = (new Integer(m_jobFileNumOrSize))
                                .intValue();
                        if (maxJobFileNum >= m_files.getSize())
                        {
                            maxJobFileNum = m_files.getSize();
                            ifSortJob = false;
                        }
                    }
                    catch (Exception e)
                    {
                    }

                    // commit job in loop
                    m_files.removeAllElements();
                    m_fps.removeAllElements();
                    int cnt = 0;
                    int jobNumber = 0;

                    for (int k = 0; k < fileList.size(); k++)
                    {
                        File file = (File) fileList.get(k);
                        FileProfile fp = (FileProfile) fpList.get(k);
                        m_files.addElement(file);
                        m_fps.addElement(fp);
                        cnt++;
                        if (cnt >= maxJobFileNum)
                        {
                            if (ifSortJob)
                            {
                                jobNumber++;
                                m_jobName = tempjobName + "_" + jobNumber;
                            }
                            commitSingleJob();
                            m_files.removeAllElements();
                            m_fps.removeAllElements();
                            cnt = 0;
                        }
                    }
                    if (m_files.size() > 0)
                    {
                        if (ifSortJob)
                        {
                            jobNumber++;
                            m_jobName = tempjobName + "_" + jobNumber;
                        }
                        commitSingleJob();
                        m_files.removeAllElements();
                        m_fps.removeAllElements();
                    }
                }

                okButton.setEnabled(true);

                stopAmimate();

                if (!isJobCreateOK)
                {
                    jLabelMsg.setForeground(Color.red);
                    jLabelMsg.setText("Error, please see log below.");
                }
                else if (isJobCommentAddedOK)
                {
                    jLabelMsg.setForeground(Color.blue);
                    jLabelMsg.setText("Uploaded successfully.");
                    addMsg("Total amount of files imported: " + totalFileNum);
                }
                else
                {
                    jLabelMsg.setForeground(Color.blue);
                    jLabelMsg
                            .setText("Uploaded successfully but error occurred when adding comment.");
                    addMsg("Total amount of files imported: " + totalFileNum);
                }
                setHiddenAble(true);
            }

            // Commit single job
            private void commitSingleJob()
            {
                // Reads parameters.
                String comment = m_commentText.trim();
                String jobName = m_jobName;
                Enumeration e_fs = m_files.elements();
                Enumeration e_fps = m_fps.elements();
                int stepCount = m_files.getSize();
                if (comment.length() > 0)
                {
                    stepCount++;
                }

                stepCount += getAttributeFileSize();

                // Set default values.
                boolean isOK = true;
                CreateJobAction createJobAction = new CreateJobAction();
                Vector<String> filePaths = new Vector<String>();
                Vector<String> fileProfileIds = new Vector<String>();
                Vector<String> targetLocales = new Vector<String>();
                HashMap<String, String> moduleMappingIds = new HashMap<String, String>();
                String jobId = null;

                try
                {
                    // Gets job name.
                    jobName = createJobAction.getUniqueJobName(m_jobName);

                    // Upload all files.
                    while (e_fs.hasMoreElements())
                    {
                        // Adds filePath.
                        File file = (File) e_fs.nextElement();
                        String path = file.getAbsolutePath();
                        String filePath = path.substring(path
                                .indexOf(File.separator) + 1);
                        filePaths.add(filePath);

                        // Adds FileProfile id.
                        FileProfile fp = (FileProfile) e_fps.nextElement();
                        fileProfileIds.add(fp.getId());
                        m_sourceLocale = fp.getSourceLocale();

                        // Adds target locale.
                        String targetLocale = getTargetLocales(fp);
                        targetLocales.add(targetLocale);

                        // Upload the file to service.
                        startAddFileMsg(file.getAbsolutePath());
                        jobId = createJobAction.uploadFile(file, jobName,
                                fp.getId(), jobId, m_priority);
                        endAddFileMsg(file.getAbsolutePath(), jobName,
                                "successfully");

                        // Changes progress bar.
                        int oriV = jProgressBar.getValue();
                        int max = jProgressBar.getMaximum();
                        int newV = oriV + max / stepCount;
                        if (newV > max)
                            newV = max;
                        jProgressBar.setValue(newV);
                    }

                    uploadAttributeFiles(jobName, stepCount);

                    // For GBS-1572, Vincent Yan, 2010-10-19
                    // add comment
                    // Save the comment to a temporay file named as job_name.txt
                    // in CXE folder, And upload the attached file to the folder
                    // named with the job name in the same folder
                    if (comment.length() > 0 || m_attachFile.length() > 0)
                    {
                        try
                        {
                            addMsg(m_sign + "Start adding comment to job "
                                    + jobName);
                            AddCommentAction commentAction = new AddCommentAction();
                            boolean result = commentAction
                                    .executeWithThread(new String[]
                                    { jobName, m_commentText, m_attachFile });
                            commitFile = "Adding comment";
                            if (!result)
                            {
                                throw commentAction.getException();
                            }
                            addMsg("Added comment successfully.");
                            log.info("Success to add comments for job "
                                    + jobName + ".");
                        }
                        catch (Exception e)
                        {
                            isJobCommentAddedOK = false;

                            addMsg("Added comment unsuccessfully.");
                            log.error("Can not add comment to job " + jobName
                                    + " with Exception " + e);
                        }
                    }

                    // Creates the job.
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("jobId", jobId);
                    map.put("comment", comment);
                    map.put("filePaths", filePaths);
                    map.put("fileProfileIds", fileProfileIds);
                    map.put("targetLocales", targetLocales);
                    map.put("cvsModules", moduleMappingIds);
                    map.put("priority", m_priority);
                    map.put("attributes", getAttributesXml());

                    createJobAction.createJob(map);
                    totalFileNum += m_files.size();
                }
                catch (Exception e)
                {
                    isOK = false;
                    isJobCreateOK = false;

                    addMsg("Can't create job  with Exception " + e);
                    log.error("Can't create job  with Exception " + e, e);
                }

                // back up job
                if (isOK)
                {
                    if (!m_isCVSJob)
                    {
                        backupJob(jobName);
                    }
                    else
                    {
                        backupCVSJob(jobName);
                    }

                    jProgressBar.setValue(jProgressBar.getMaximum());
                }
            }

            private String getTargetLocales(FileProfile fp)
            {
                String[] importedTagetLocales = fp.getUsedTargetLocales();
                if (importedTagetLocales == null
                        || importedTagetLocales.length == 0)
                {
                    importedTagetLocales = fp.getTargetLocales();
                }

                if ("".equals(importedTagetLocales[0].trim()))
                {
                    importedTagetLocales = fp.getTargetLocales();
                }

                StringBuffer sb = new StringBuffer();
                int len = importedTagetLocales.length;
                for (int i = 0; i < len; i++)
                {
                    String targetLocale = importedTagetLocales[i];
                    sb.append(targetLocale);
                    if (i != len - 1)
                        sb.append(",");
                }

                return sb.toString();
            }
        };
        thread.start();
    }

    private void closeMe()
    {
        if (canHidden())
        {
            setVisible(false);
        }
        else
        {
            int result = AmbOptionPane.showConfirmDialog(
                    "The job is being created. Cancel it all the same? ",
                    "Interruption operation", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION)
            {
                setVisible(false);
            }
            else
            {
                setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            }
        }
    }

    private void backupJob(String jobName)
    {
        try
        {
            User u = CacheUtil.getInstance().getCurrentUser();
            List<FileMapped> flist = new ArrayList<FileMapped>();
            for (int i = 0; i < m_files.size(); i++)
            {
                File file = (File) m_files.get(i);
                FileProfile fp = (FileProfile) m_fps.get(i);
                FileMapped fe = new FileMapped(file, fp);
                flist.add(fe);
            }
            Job job = new Job(jobName, u, flist, new Date(), null);
            ConfigureHelperV2.writeNewJob(job);
            log.info("Job " + jobName + " has been backuped.");
        }
        catch (Exception e)
        {
            log.error("Can not backup the job" + jobName + " .\n" + e);
        }
    }

    private void backupCVSJob(String jobName)
    {
        try
        {
            User u = CacheUtil.getInstance().getCurrentUser();
            List<FileMapped> flist = new ArrayList<FileMapped>();
            for (int i = 0; i < m_files.size(); i++)
            {
                File file = (File) m_files.get(i);
                FileProfile fp = (FileProfile) m_fps.get(i);
                FileMapped fe = new FileMapped(file, fp);
                flist.add(fe);
            }
            Job job = new Job(jobName, u, flist, new Date(), null);
            job.setCVSJob(true);
            job.setSourceLocale(m_sourceLocale);
            ConfigureHelperV2.writeNewJob(job);
            log.info("Job " + jobName + " has been backuped.");
        }
        catch (Exception e)
        {
            log.error("Can not backup the job" + jobName + " .\n" + e);
        }
    }

    private void addMsg(String p_msg)
    {
        jTextArea.append(p_msg + "\n");
    }

    private void startAddFileMsg(String p_fileName)
    {
        addMsg(m_sign + "Start uploading file: " + p_fileName);
    }

    private void endAddFileMsg(String p_fileName, String p_jobname,
            String p_status)
    {
        addMsg("Uploaded file " + p_fileName + " into job " + p_jobname + " "
                + p_status);
    }

    private void stopAmimate()
    {
        m_amimate.stop();
    }

    // show ....
    private void startAmimate()
    {
        Thread thread = new Thread("UploadFilesDialog - startAmimate")
        {
            public void run()
            {
                int size = 15;
                int i = 0;
                while (true)
                {
                    StringBuffer signs = new StringBuffer();
                    for (int j = 0; j < i; j++)
                    {
                        signs.append(".");
                    }
                    for (int j = 0; j < size - i; j++)
                    {
                        signs.append(" ");
                    }
                    String text = commitFile + signs;

                    jLabelMsg.setText(text);

                    if (++i > size)
                        i = 0;

                    try
                    {
                        Thread.sleep(777);
                    }
                    catch (InterruptedException e)
                    {
                        // e.printStackTrace();
                        // do nothing, only print the Exception in developing
                    }
                }
            }
        };

        thread.start();
        m_amimate = thread;
    }

    private int getAttributeFileSize()
    {
        int size = 0;

        List<JobAttributeVo> vos = attributes.getAttributes();
        for (JobAttributeVo vo : vos)
        {
            if (vo instanceof FileJobAttributeVo)
            {
                FileJobAttributeVo fileVo = (FileJobAttributeVo) vo;
                List<String> files = fileVo.getFiles();
                size += files.size();
            }
        }

        return size;
    }

    private void uploadAttributeFiles(String jobName, int stepCount)
            throws Exception
    {
        CreateJobAction createJobAction = new CreateJobAction();

        List<JobAttributeVo> vos = attributes.getAttributes();
        for (JobAttributeVo vo : vos)
        {
            if (vo instanceof FileJobAttributeVo)
            {
                FileJobAttributeVo fileVo = (FileJobAttributeVo) vo;
                List<String> files = fileVo.getFiles();
                for (String file : files)
                {
                    addMsg("Uploaded attribute file " + file
                            + " for attribute " + fileVo.getDisplayName() + " "
                            + "successfully");

                    createJobAction.uploadAttributeFiles(new File(file),
                            jobName, fileVo.getInternalName());
                    int oriV = jProgressBar.getValue();
                    int max = jProgressBar.getMaximum();
                    int newV = oriV + max / stepCount;
                    if (newV > max)
                        newV = max;
                    jProgressBar.setValue(newV);
                }
            }
        }
    }

    private String getAttributesXml()
    {
        Attributes atts = new Attributes();
        atts.setAttributes(attributes.getAttributes());
        return XmlUtil.object2String(atts, true);
    }

    private boolean canHidden()
    {
        return m_canHidden;
    }

    private void setHiddenAble(boolean newValue)
    {
        m_canHidden = newValue;
    }

    private JProgressBar jProgressBar;

    private JLabel jLabelMsg;

    private JTextArea jTextArea;

    private JButton okButton;

    private String commitFile = "Uploading file.";

    private String m_sign = Constants.NOTICE_SIGN;

    private String m_jobName;

    private String m_commentText;

    private String m_attachFile;

    private DefaultListModel m_files, m_fps;

    private AttributeTableModel attributes;

    private String m_jobCreateOption, m_jobFileNumOrSize;

    private boolean isInstallParameters = false;

    private boolean wright = true;

    private Thread m_amimate;

    private PopupMenu pMenu;

    private boolean m_canHidden;

    private boolean m_isCVSJob;

    private String m_sourceLocale;

    private String m_priority;

    private boolean isJobCreateOK = true;

    private boolean isJobCommentAddedOK = true;

    private int totalFileNum = 0;
}
