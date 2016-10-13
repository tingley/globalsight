package com.globalsight;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.TreePath;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.globalsight.file.FileVerifiers;
import com.globalsight.file.Verifier;
import com.globalsight.table.AttribiteEditor;
import com.globalsight.table.AttributeRender;
import com.globalsight.table.FileTableModel;
import com.globalsight.table.RowVo;
import com.globalsight.util.AuthenticationPrompter;
import com.globalsight.util.FileUtil;
import com.globalsight.util.XmlUtil;
import com.globalsight.vo.FileProfileVo;
import com.globalsight.vo.ProfileInfo;
import com.globalsight.webservices.client2.Ambassador2;
import com.globalsight.webservices.client2.WebService2ClientHelper;
import com.sun.java.browser.net.ProxyInfo;
import com.sun.java.browser.net.ProxyService;

public class EditSourceApplet extends Applet
{
    private static int MAX_SEND_SIZE = 5 * 1000 * 1024; // 5M

    private static final long serialVersionUID = 1L;

    private JPanel jPanel = null;
    private JButton bRemoveFiles = null;
    private JButton bAddFiles = null;
    private JButton bCancel = null;
    private JButton bAdd = null;
    private JScrollPane jScrollPane = null;
    private JTree jTree = null;

    // private DefaultListModel filesModel, fileProfilesModel;
    private FileTableModel fileTableModel;

    private JScrollPane jScrollPane1 = null;
    private JTable jTable = null;
    private Map<Long, List<FileProfileVo>> l10nProfiles;

    private static int NON_SSL_PORT = 80;
    private static int SSL_PORT = 443;

    private static String URL = "/globalsight/AppletService?action="; // @jve:decl-index=0:
    private String jobId = null; // @jve:decl-index=0:
    private String companyId = null; // @jve:decl-index=0:
    private String l10nProfileId = null;
    private String pageLocale = null;
    private String userName = null;
    private String password = null;
    private JPanel jPanel1 = null;
    private AuthenticationPrompter s_authPrompter = new AuthenticationPrompter();

    private Map<String, String> resource = null; // @jve:decl-index=0:
    private Verifier existVerifier = null; // @jve:decl-index=0:
    private Verifier addedVerifier = null; // @jve:decl-index=0:
//    private Verifier mappedVerifier = null;  //  @jve:decl-index=0:
    private JLabel jLabel1 = null;
    private JSlider jSlider = null;
    // @jve:decl-index=0:
    /**
     * This is the default constructor
     */
    public EditSourceApplet()
    {
        super();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    public void init()
    {
        this.setLayout(null);
        this.setSize(924, 507);

        this.add(getJPanel(), null);
    }

    public String getJobId()
    {
        if (jobId == null)
        {
            jobId = getParameter("jobId");
        }

        return jobId;
    }

    public String getUserName()
    {
        if (userName == null)
        {
            userName = getParameter("userName");
        }

        return userName;
    }
    
    public String getPassword()
    {
        if (password == null)
        {
            password = getParameter("password");
        }
        
        return password;
    }
    
    private String getPageLocale()
    {
        if (pageLocale == null)
        {
            pageLocale = getParameter("pageLocale");
        }
        
        return pageLocale;
    }
    
    private String getCompanyId()
    {
        if (companyId == null)
        {
            companyId = getParameter("companyId");
        }

        return companyId;
    }

    /**
     * This method initializes jPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel()
    {
        if (jPanel == null)
        {
            jLabel1 = new JLabel("", JLabel.RIGHT);
            jLabel1.setText(getLable("lb_number_directory", "Number of directory levels:"));
            jLabel1.setBounds(new Rectangle(404, 353, 231, 26));
            
            jPanel = new JPanel();
            jPanel.setLayout(null);
            jPanel.setBounds(new Rectangle(0, 0, 924, 505));
            jPanel.setEnabled(false);
            jPanel.add(getBRemoveFiles(), null);
            jPanel.add(getBAddFiles(), null);
            jPanel.add(getBCancel(), null);
            jPanel.add(getBAdd(), null);
            jPanel.add(getJScrollPane(), null);
            jPanel.add(getJScrollPane1(), null);
            jPanel.add(getJPanel1(), null);
            jPanel.add(jLabel1, null);
            jPanel.add(getJSlider(), null);
        }
        return jPanel;
    }

    /**
     * This method initializes bRemoveFiles
     * 
     * @return javax.swing.JButton
     */
    private JButton getBRemoveFiles()
    {
        if (bRemoveFiles == null)
        {
            bRemoveFiles = new JButton();
            bRemoveFiles.setBounds(new Rectangle(766, 401, 120, 26));
            bRemoveFiles.setText(getLable("lb_remove_files", "Remove File(s)"));
            bRemoveFiles.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    int[] rows = jTable.getSelectedRows();
                    for (int i = rows.length - 1; i >= 0; i--)
                    {
                        fileTableModel.getRows().remove(rows[i]);
                    }

                    fileTableModel.fireTableDataChanged();
                }
            });
        }
        return bRemoveFiles;
    }

    /**
     * This method initializes bAddFiles
     * 
     * @return javax.swing.JButton
     */
    private JButton getBAddFiles()
    {
        if (bAddFiles == null)
        {
            bAddFiles = new JButton();
            bAddFiles.setBounds(new Rectangle(253, 400, 120, 26));
            bAddFiles.setText(getLable("lb_add_files", "Add File(s)"));
            bAddFiles.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    TreePath[] paths = jTree.getSelectionPaths();
                    if (paths != null)
                    {
                        List<File> allFiles = new ArrayList<File>();
                        for (TreePath path : paths)
                        {
                            Node node = (Node) path.getLastPathComponent();
                            File file = node.getSelf();
                            List<File> files = FileUtil.getAllFiles(file);
                            allFiles.addAll(files);
                        }

                        addFiles(allFiles);
                    }
                }
            });
        }
        return bAddFiles;
    }

    private void addFile(File file)
    {
        List<File> files = new ArrayList<File>();
        files.add(file);
        addFiles(files);
    }

    private void addFiles(List<File> files)
    {
        FileVerifiers verifiers = new FileVerifiers();
        verifiers.addVerifier(getExistVerifier());
        verifiers.addVerifier(getAddedVerifier());
        List<File> fs = verifiers.validate(files);
        
        for (File f : fs)
        {
            RowVo rowVo = new RowVo();
            rowVo.setFile(f);
            List<FileProfileVo> fileProfiles = getFileProfiles(rowVo);
            rowVo.setFileProfiles(fileProfiles);
            if (fileProfiles.size() > 0)
            {
                rowVo.setSelectIndex(0);
            }
            fileTableModel.getRows().add(rowVo);
        }
        
        fileTableModel.fireTableDataChanged();
    }

    private void projectChanged()
    {
        List<RowVo> rows = fileTableModel.getRows();
        for (RowVo row : rows)
        {
            List<FileProfileVo> fileProfiles = getFileProfiles(row);
            row.setFileProfiles(fileProfiles);
        }

        // fileTableModel.fireTableDataChanged();
    }

    private long getL10nProfileId()
    {
        if (l10nProfileId == null)
        {
        	l10nProfileId = getParameter("l10nProfileId");
        }
        
        if (l10nProfileId == null)
        {
            List<RowVo> rows = fileTableModel.getRows();
            for (RowVo row : rows)
            {
                int index = row.getSelectIndex();
                if (index > -1)
                {
                    FileProfileVo vo = row.getFileProfiles().get(index);
                    return vo.getL10nProfileId();
                }
            }
            
            return -1;
        }
        return Long.parseLong(l10nProfileId);
    }

    private List<FileProfileVo> getFileProfiles(RowVo rowVo)
    {
    	Map<Long, List<FileProfileVo>> l10nProfiles = getLp2FpMap();
        List<FileProfileVo> fileProfiles = new ArrayList<FileProfileVo>();

        long l10nProfileId = getL10nProfileId();
        if (l10nProfileId > -1)
        {
            List<FileProfileVo> fileProfils = l10nProfiles.get(l10nProfileId);
            String extension = getExtension(rowVo);
            if (fileProfils != null)
            {
                for (FileProfileVo vo : fileProfils)
                {
                    if (vo.getFileExtensions() == null || vo.getFileExtensions().contains(extension))
                    {
                        fileProfiles.add(vo);
                    }
                }
            }
        }
        else
        {
            Collection<List<FileProfileVo>> values = l10nProfiles.values();
            for (List<FileProfileVo> fileProfils : values)
            {
                String extension = getExtension(rowVo);
                for (FileProfileVo vo : fileProfils)
                {
                    if (vo.getFileExtensions().contains(extension))
                    {
                        fileProfiles.add(vo);
                    }
                }
            }
        }
        return fileProfiles;
    }

    private String getExtension(RowVo rowVo)
    {
        File file = rowVo.getFile();
        String name = file.getName();
        int index = name.lastIndexOf(".");
        if (index < 0)
            return "";

        return name.substring(index + 1).toLowerCase();
    }

    /**
     * This method initializes bCancel
     * 
     * @return javax.swing.JButton
     */
    private JButton getBCancel()
    {
        if (bCancel == null)
        {
            bCancel = new JButton();
            bCancel.setBounds(new Rectangle(645, 460, 120, 26));
            bCancel.setText(getLable("lb_cancel", "Cancel"));
            bCancel.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    try
                    {
                        getAppletContext()
                                .showDocument(
                                        new URL("javascript:closeDialog()"));
                    }
                    catch (MalformedURLException e1)
                    {
                        e1.printStackTrace();
                    }
                }
            });
        }
        return bCancel;
    }
    
    public Frame getParentWindow()
    {
        Container c = getParent();
        while (c != null)
        {
            if (c instanceof Frame)
                return (Frame) c;
            c = c.getParent();
        }
        return null;
    }
    
    private EditSourceApplet getEditApplet()
    {
        return this;
    }

    /**
     * This method initializes bAdd
     * 
     * @return javax.swing.JButton
     */
    private JButton getBAdd()
    {
        if (bAdd == null)
        {
            bAdd = new JButton();
            bAdd.setBounds(new Rectangle(770, 460, 120, 26));
            bAdd.setText(getLable("lb_add", "Add"));

            bAdd.addMouseListener(new java.awt.event.MouseAdapter()
            {
                public void mouseClicked(java.awt.event.MouseEvent e)
                {
                    List<RowVo> rows = fileTableModel.getRows();
                    if (rows.size() < 1)
                    {
                        JOptionPane.showMessageDialog(null, getLable(
                                "msg_no_file", "Please add files first."));
                        return;
                    }
                    
                    List<File> allFiles = new ArrayList<File>();
                    for (RowVo row : rows)
                    {
                        if ("".equals(row.getSelectFileProfile()))
                        {
                            JOptionPane.showMessageDialog(null, new JLabel(getLable(
                                    "msg_no_map", "Please specify the file profile(s) marked with yellow color.")));
                            return;
                        }
                        
                        allFiles.add(row.getFile());
                    }
                    
                    FileVerifiers verifiers = new FileVerifiers();
                    verifiers.addVerifier(getExistVerifier());
                    if (verifiers.validate(allFiles).size() != allFiles.size())
                    {
                        return;
                    }
                    
                    List<RowVo> aRows = new ArrayList<RowVo>();
                    aRows.addAll(rows);
                    fileTableModel.getRows().clear();
                    fileTableModel.fireTableDataChanged();
                    
                    Map<String, String> args = new HashMap<String, String>();
                    args.put("jobId", getJobId());
                    String key = execute("canAddSourceFile", args);
                    if (key == null || key.trim().length() == 0)
                    {
                        ProcessDialog process = new ProcessDialog(getParentWindow(), getEditApplet());
                        process.setRows(aRows);
                        process.setVisible(true);
                        process.uploadFiles();
                    }
                    else
                    {
                        String msg = getResource().get(key);
                        JOptionPane.showMessageDialog(null, getLable(
                                msg, "Can not add files now."));
                        try
                        {
                            getAppletContext().showDocument(
                                    new URL("javascript:refreshJobPage()"));
                        }
                        catch (MalformedURLException e1)
                        {
                            e1.printStackTrace();
                        }
                    }
                }
            });
        }
        return bAdd;
    }

    /**
     * This method initializes jScrollPane
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane()
    {
        if (jScrollPane == null)
        {
            jScrollPane = new JScrollPane();
            jScrollPane.setBounds(new Rectangle(31, 19, 343, 362));
            jScrollPane.setViewportView(getJTree());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jTree
     * 
     * @return javax.swing.JTree
     */
    private JTree getJTree()
    {
        if (jTree == null)
        {
            FileSystemView m_fileSystemView = FileSystemView
                    .getFileSystemView();
            FileSystemTreeModel model = new FileSystemTreeModel(
                    m_fileSystemView, new Locale("en_US"));
            jTree = new JTree(model);
            jTree.setShowsRootHandles(false);
            jTree.setSize(new Dimension(343, 350));
            jTree.setCellRenderer(model);
            jTree.expandRow(1);

            jTree.addMouseListener(getMouseListener());
        }
        return jTree;
    }

    private MouseListener getMouseListener()
    {
        return new MouseListener()
        {
            public void mouseClicked(MouseEvent me)
            {

                if (me.getClickCount() > 1
                        && me.getModifiers() == InputEvent.BUTTON1_MASK)
                {
                    JTree srcTree = (JTree) me.getSource();
                    Object node = (srcTree).getLastSelectedPathComponent();
                    if (node instanceof Node)
                    {
                        Node n = (Node) node;
                        File file = n.getSelf();
                        if (file.isFile())
                        {
                            addFile(file);
                        }
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent arg0)
            {
                
            }

            @Override
            public void mouseExited(MouseEvent arg0)
            {

            }

            @Override
            public void mousePressed(MouseEvent arg0)
            {

            }

            @Override
            public void mouseReleased(MouseEvent arg0)
            {

            }
        };
    }

    /**
     * This method initializes jScrollPane1
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane1()
    {
        if (jScrollPane1 == null)
        {
            jScrollPane1 = new JScrollPane();
            jScrollPane1.setBounds(new Rectangle(405, 20, 485, 316));
            jScrollPane1.setBackground(Color.white);
            jScrollPane1.setViewportView(getJTable());
        }
        return jScrollPane1;
    }

    /**
     * This method initializes jTable
     * 
     * @return javax.swing.JTable
     */
    private JTable getJTable()
    {
        if (jTable == null)
        {
            fileTableModel = new FileTableModel();
            fileTableModel.setResource(getResource());

            fileTableModel.addTableModelListener(new TableModelListener()
            {

                @Override
                public void tableChanged(TableModelEvent arg0)
                {
                    projectChanged();
                }
            });

            jTable = new JTable(fileTableModel);
            jTable.getSelectionModel().addListSelectionListener(
                    new ListSelectionListener()
                    {

                        @Override
                        public void valueChanged(ListSelectionEvent arg0)
                        {
                            int[] rows = jTable.getSelectedRows();
                            List<FileProfileVo> fileProfiles = null;
                            for (int n : rows)
                            {
                                if (fileProfiles == null)
                                {
                                    fileProfiles = new ArrayList<FileProfileVo>();
                                    fileProfiles
                                            .addAll(fileTableModel.getRows()
                                                    .get(n).getFileProfiles());
                                }
                                else
                                {
                                    List<FileProfileVo> profiles = fileTableModel
                                            .getRows().get(n).getFileProfiles();

                                    for (int i = fileProfiles.size() - 1; i >= 0; i--)
                                    {
                                        FileProfileVo fp = fileProfiles.get(i);
                                        if (!profiles.contains(fp))
                                        {
                                            fileProfiles.remove(fp);
                                        }
                                    }

                                    if (fileProfiles.size() == 0)
                                    {
                                        break;
                                    }
                                }
                            }
                        }
                    });
            jTable.getColumnModel().getColumn(1).setCellEditor(
                    new AttribiteEditor());
            jTable.getColumnModel().getColumn(1).setCellRenderer(
                    new AttributeRender());
            // jTable.set
        }
        return jTable;
    }

    public String execute(String method, Map<String, String> args)
    {
        return execute(method, args, null);
    }

    public String execute(String method, Map<String, String> args, File file)
    {
        return execute(method, args, file, null);
    }

    public String execute(String method, Map<String, String> args, File file,
            Map<String, String> parms)
    {
        return (String) execute(method, args, file, parms, false);
    }

    public Object execute(String method, Map<String, String> args, File file,
            Map<String, String> parms, boolean returnObject)
    {
        HttpClient client = new HttpClient();
        try
        {
            setUpClientForProxy(client, method);
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
        client.getHttpConnectionManager().getParams()
                .setConnectionTimeout(5000);
        StringBuilder s = new StringBuilder(getUrlPrefix());
        s.append(URL).append(method);

        if (args != null)
        {
            for (String key : args.keySet())
            {
                s.append("&").append(key).append("=").append(args.get(key));
            }
        }
        s.append("&currentCompanyId=").append(getCompanyId());

        PostMethod filePost = new UTF8PostMethod(s.toString());

        if (file != null)
        {
            try
            {
                // upload file via web service here.
                String addFileTmpSavingPathName = getRandomSavingPath(file);
                this.uploadFileViaWebService(addFileTmpSavingPathName, file);

                int size = 3;
                if (parms != null)
                {
                    size += parms.size();
                }

                Part[] parts = new Part[size];
                parts[0] = new FilePart(file.getName(), file);
                parts[1] = new StringPart("addFileTmpSavingPathName", addFileTmpSavingPathName);
                parts[2] = new StringPart("currentCompanyId", getCompanyId());

                if (parms != null)
                {
                    int i = 3;
                    for (String key : parms.keySet())
                    {
                        parts[i] = new StringPart(key, parms.get(key), "utf-8");
                        i++;
                    }
                }

                HttpMethodParams params = filePost.getParams();
                MultipartRequestEntity entity = new MultipartRequestEntity(
                        parts, params);
                filePost.setRequestEntity(entity);
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            if (parms != null)
            {
                for (String key : parms.keySet())
                {
                    filePost.addParameter(key, parms.get(key));
                }
            }

            filePost.addParameter("currentCompanyId", getCompanyId());
        }

        int status;
        try
        {
            status = client.executeMethod(filePost);
            if (status == HttpStatus.SC_OK)
            {
                if (returnObject)
                {
                    ObjectInputStream in = new ObjectInputStream(filePost
                            .getResponseBodyAsStream());
                    try
                    {
                        return in.readObject();
                    }
                    catch (ClassNotFoundException e)
                    {
                        e.printStackTrace();
                        return null;
                    }
                }

                return filePost.getResponseBodyAsString();
            }
        }
        catch (HttpException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public Map<Long, List<FileProfileVo>> getLp2FpMap()
    {
        if (l10nProfiles == null)
        {
        	l10nProfiles = new HashMap<Long, List<FileProfileVo>>();
            String fileProfile = execute("getFileProfiles", null);
            if (fileProfile != null)
            {
                ProfileInfo info = XmlUtil.string2Object(ProfileInfo.class,
                        fileProfile);
                for (FileProfileVo vo : info.getFileProfiles())
                {
                	List<FileProfileVo> fps = l10nProfiles.get(vo.getL10nProfileId());
                    if (fps == null)
                    {
                        fps = new ArrayList<FileProfileVo>();
                        l10nProfiles.put(vo.getL10nProfileId(), fps);
                    }
                    fps.add(vo);
                }
            }
        }
        return l10nProfiles;
    }
    
    private int determineValidPort(int p_port, String p_protocol)
    {
        // Note that ssl protocols end with 's' (i.e. https, or t3s)
        return p_port == -1 ? (p_protocol.toLowerCase().endsWith("s") ? SSL_PORT
                : NON_SSL_PORT)
                : p_port;
    }

    private String getUrlPrefix()
    {
        URL url = getCodeBase();
        String protocol = url.getProtocol();
        int port = determineValidPort(url.getPort(), protocol);

        StringBuffer sb = new StringBuffer();
        sb.append(protocol);
        sb.append("://");
        sb.append(url.getHost());
        sb.append(":");
        sb.append(port);

        return sb.toString();
    }

    private ExistFiles getExistFiles()
    {
        ExistFiles existFiles = null;
        Map<String, String> args = new HashMap<String, String>();
        args.put("jobId", getJobId());
        String files = execute("getFiles", args);
        if (files != null)
        {
            existFiles = XmlUtil.string2Object(ExistFiles.class, files);
        }

        return existFiles;
    }

    private Verifier getExistVerifier()
    {
        if (existVerifier == null)
        {
            existVerifier = new Verifier()
            {
                @Override
                public String validate(File file)
                {
                    ExistFiles eFiles = getExistFiles();
                    if (eFiles.isExist(file))
                        return getLable("msg_file_exist",
                                "The following file(s) have been included in the job.");

                    return null;
                }
            };
        }

        return existVerifier;
    }

    private Verifier getAddedVerifier()
    {
        if (addedVerifier == null)
        {
            addedVerifier = new Verifier()
            {
                @Override
                public String validate(File file)
                {
                    List<RowVo> rows = fileTableModel.getRows();
                    for (RowVo row : rows)
                    {
                        if (file.equals(row.getFile()))
                            return getLable("msg_file_added",
                                    "The following file(s) can not be added repeatedly.");
                    }

                    return null;
                }
            };
        }

        return addedVerifier;
    }

    /**
     * This method initializes jPanel1
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel1()
    {
        if (jPanel1 == null)
        {
            jPanel1 = new JPanel();
            jPanel1.setLayout(null);
            jPanel1.setBounds(new Rectangle(11, 447, 875, 2));
            jPanel1.setBorder(BorderFactory
                    .createEtchedBorder(EtchedBorder.LOWERED));
        }
        return jPanel1;
    }

    /**
     * Sets up the HttpClient to use a proxy if one needs to be used. Also sets
     * up for proxy authentication (NTLM and Basic)
     * 
     * @param p_httpClient
     */
    private void setUpClientForProxy(HttpClient p_client, String method)
            throws Exception
    {
        // first see if a proxy needs to be used at all
        // detectProxyInfoFromSystem() is known to work at Dell
        // ProxyInfo proxyInfo = detectProxyInfoFromSystem();

        // detectProxyInfoFromBrowser() is probably more correct,
        String urlPrefix = getUrlPrefix();
        // String servletUrl = getParameter(AppletHelper.SERVLET_URL);
        // String randID = getParameter(AppletHelper.RANDOM);
        String servletLocation = urlPrefix + URL + method;
        ProxyInfo proxyInfo = detectProxyInfoFromBrowser(new URL(
                servletLocation));

        if (proxyInfo != null)
        {
            // set to use proxy
            p_client.getHostConfiguration().setProxy(proxyInfo.getHost(),
                    proxyInfo.getPort());

            // set to authenticate to proxy if need be
            p_client.getParams().setParameter(CredentialsProvider.PROVIDER,
                    s_authPrompter);
        }
    }

    public static ProxyInfo detectProxyInfoFromBrowser(URL p_url)
    {
        ProxyInfo proxyInfo = null;
        try
        {
            ProxyInfo infos[] = ProxyService.getProxyInfo(p_url);
            if (infos != null && infos.length > 0)
            {
                proxyInfo = infos[0];
            }
        }
        catch (Exception ex)
        {
            System.out.println("---- Could not retrieve proxy configuration: "
                    + ex.getMessage());
        }

        return proxyInfo;
    }

    public String getLable(String key, String defaultValue)
    {
        Map<String, String> resource = getResource();
        if (resource == null)
            return defaultValue;

        String value = resource.get(key);
        if (value == null)
            return defaultValue;

        return value;
    }

    @SuppressWarnings("unchecked")
	public Map<String, String> getResource()
    {
        if (resource == null)
        {
            Map<String, String> args = new HashMap<String, String>();
            args.put("name", "addSourceFiles");
            args.put("pageLocale", getPageLocale());
            try
            {
                resource = (Map<String, String>) execute("getResource", args, null,
                        null, true);
            }
            catch (Exception e)
            {
                resource = new HashMap<String, String>();
            }

        }
        return resource;
    }

    /**
     * This method initializes jSlider  
     *  
     * @return javax.swing.JSlider  
     */
    private JSlider getJSlider()
    {
        if (jSlider == null)
        {
            jSlider = new JSlider();
            Font font = jSlider.getFont();
            int size = font.getSize();
            size = size / 3 * 2;
            jSlider.setFont(new Font(font.getName(), font.getSize(), size));
            jSlider.setValue(0);
            jSlider.setMaximum(12);
            jSlider.setMinorTickSpacing(1);
            jSlider.setMajorTickSpacing(3);
            jSlider.setPaintLabels(true);
            jSlider.setSnapToTicks(true);
            jSlider.setPaintTicks(true);
            jSlider.setToolTipText(getLable("lb_tip_number_directory", "Set the number of directory levers."));
            jSlider.setBounds(new Rectangle(651, 349, 244, 38));
            jSlider.addChangeListener(new javax.swing.event.ChangeListener()
            {
                public void stateChanged(javax.swing.event.ChangeEvent e)
                {
                    int n = jSlider.getValue();
                    if (n == jSlider.getMaximum())
                    {
                        n = -1;
                    }
                    
                    fileTableModel.setDirectNumber(n);
                    fileTableModel.fireTableDataChanged();
                }
            });
        }
        return jSlider;
    }

    
    private void uploadFileViaWebService(String addFileTmpSavingPathName, File file)
            throws Exception
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
            URL url = this.getCodeBase();
            String hostName = url.getHost();
            int port = url.getPort();
            String protocol = url.getProtocol();
            boolean enableHttps = protocol.contains("s") ? true : false;

            Ambassador2 ambassador = WebService2ClientHelper
                    .getClientAmbassador2(hostName, String.valueOf(port),
                            getUserName(), getPassword(), enableHttps);
            String fullAccessToken = ambassador.dummyLogin(getUserName(),
                    getPassword());
            for (int i = 0; i < fileByteList.size(); i++)
            {
                ambassador.uploadFiles(fullAccessToken, getCompanyId(), 3,
                        addFileTmpSavingPathName, (byte[]) fileByteList.get(i));
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
    
    private String getRandomSavingPath(File file)
    {
        Random rand = new Random(System.currentTimeMillis());
        return "addFiles_tmp" + File.separator + rand.nextLong()
                + File.separator + file.getName(); 
    }

} // @jve:decl-index=0:visual-constraint="17,10"
