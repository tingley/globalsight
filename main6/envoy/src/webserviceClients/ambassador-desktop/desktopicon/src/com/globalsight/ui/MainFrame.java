package com.globalsight.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.apache.axis.AxisProperties;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.globalsight.action.Action;
import com.globalsight.action.DownloadAction;
import com.globalsight.action.ExecAction;
import com.globalsight.action.GetVersionAction;
import com.globalsight.action.LoginAction;
import com.globalsight.action.QueryAction;
import com.globalsight.cvsoperation.ui.CVSMainPanel;
import com.globalsight.entity.Job;
import com.globalsight.entity.User;
import com.globalsight.exception.NotSupportHttpsException;
import com.globalsight.util.ConfigureHelper;
import com.globalsight.util.Constants;
import com.globalsight.util.StringUtil;
import com.globalsight.util.SwingHelper;
import com.globalsight.util.UsefulTools;
import com.globalsight.util2.CacheUtil;
import com.globalsight.util2.ConfigureHelperV2;
import com.thoughtworks.selenium.Selenium;

public class MainFrame extends JFrame
{
    /**
     * Main Frame for this application.
     */
    private static final long serialVersionUID = -1887717848419117870L;

    static Logger log = Logger.getLogger(MainFrame.class.getName());

    private JMenuBar jMenuBar = new JMenuBar();

    // Configure, View, Jobs, About, Exit
    private JMenu jm_configure, jm_view, jm_jobs, jm_help, jm_exit;

    private JMenuItem jmi_configure_user, jmi_view_user, jmi_proxy,
            jmi_Preferences;

    private JMenuItem jmi21, jmi22, jmi23, jmi24, jmi25, jmi26, jmi27, jmi28,
            jmi29;

    private JMenuItem jmi31, jmi32;

    private JMenuItem jmi41, jmi42, jmi43, jmi44;

    private JMenuItem jmi51;

    private ClosableTabbedPane closeTabbedPanel;

    private JLabel statusBar, msgCenterBar;

    private static String status_prefix = " : ";

    private static String status_msg = "MsgCenter";

    public static String START_FIREFOX_JSSH = "start Firefox with jssh";

    private String ruby = "ruby";

    private String rubylib = "export RUBYLIB=${RUBYLIB}:"
            + Constants.RESOURCE_DIRECTORY + "ruby";

    private int width = 830;

    private int height = 600;

    private List m_files = new ArrayList();

    private List jobsNewList = new ArrayList();

    private List jobsAllList = new ArrayList();

    private NoticeThread noticeThread = null;

    private Popup popupPanel = null;

    private DownloadAction downloadAction = new DownloadAction();

    private QueryAction queryAction = new QueryAction();

    public MainFrame()
    {
        super(Constants.APP_FULL_NAME);
        setProxy();
        init();
    }

    // ///////////////////////////////////////////////////////////////
    // constructor
    // ///////////////////////////////////////////////////////////////
    public MainFrame(String p_title)
    {
        super(p_title);
        setProxy();
        init();
    }

    // ///////////////////////////////////////////////////////////////
    // public methods
    // ///////////////////////////////////////////////////////////////

    /**
     * show message in the statusbar, a wrraper of
     * <code>setStatus(String p_str, int p_style)</code>
     * 
     * @param p_str
     *            message in statusbar
     * @param p_styleS
     *            must be one of Constants.FAILURE, WELCOME, SUCCESS
     */
    public void setStatus(String p_str, String p_styleS)
    {
        int p_style = Constants.SUCCESS;
        try
        {
            p_style = Integer.parseInt(p_styleS);
        }
        catch (NumberFormatException e)
        {
        }
        setStatus(p_str, p_style);
    }

    /**
     * show message in the statusbar
     * 
     * @param p_str
     *            message in statusbar
     * @param p_style
     *            must be one of Constants.FAILURE, WELCOME, SUCCESS
     */
    public void setStatus(String p_str, int p_style)
    {
        if (p_style == Constants.FAILURE)
        {
            statusBar.setForeground(Color.RED);
        }
        else if (p_style == Constants.WELCOME)
        {
            statusBar.setForeground(Color.BLUE);
        }
        else if (p_style == Constants.SUCCESS)
        {
            statusBar.setForeground(Color.BLACK);
        }
        else
        {
            statusBar.setForeground(Color.BLACK);
        }
        statusBar.setText(status_prefix + p_str);
    }

    /**
     * Logon with default user
     * 
     * @return
     */
    public boolean logon()
    {
        boolean islogin = false;
        String status;
        try
        {
            islogin = logon(ConfigureHelperV2.readDefaultUser());
        }
        catch (Exception e)
        {
            status = Constants.ERROR_READUSER;
            log.error(status, e);
            AmbOptionPane.showMessageDialog(status, "Warning",
                    JOptionPane.ERROR_MESSAGE);
        }
        return islogin;
    }

    /**
     * Logon with <code>user</code>
     * 
     * @param p_user
     * @return
     */
    public boolean logon(User p_user)
    {
        String msg = null;
        int style = Constants.FAILURE;
        boolean islogin = false;
        if (p_user == null)
        {
            msg = "There is no user to logon";
        }
        else
        {
            try
            {
                LoginAction loginAction = new LoginAction();
                CacheUtil.getInstance().setCurrentUser(null);
                CacheUtil.getInstance().setLoginingUser(p_user);
                String result = loginAction.execute(new String[]
                {});
                boolean disConnectToCVS = false;
                try
                {
                    String allPermsOfCurrUser = queryAction
                            .execute(new String[]
                            { QueryAction.q_getAllPermissionsByUser });
                    if (allPermsOfCurrUser
                            .indexOf(Constants.CONNECT_TO_CVS_PERM) != -1)
                    {
                        disConnectToCVS = true;
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                jmi32.setVisible(disConnectToCVS);
                jmi44.setVisible(disConnectToCVS);
                if (disConnectToCVS == false)
                {
                    int index = getIndex(Constants.CONNECT_TO_CVS);
                    if (index != -1)
                    {
                        closeTabbedPanel.remove(index);
                    }
                }

                if (result.equals(LoginAction.success))
                {
                    CacheUtil.getInstance().setCurrentUser(p_user);
                    log.info(p_user + " login.");
                    int availableVersion = checkVersion();
                    if (availableVersion == 0)
                    {
                        msg = Constants.MSG_SUCCESS_LOGON + " - " + p_user;
                        style = Constants.SUCCESS;
                        islogin = true;
                    }
                    else if (availableVersion == 1)
                    {
                        msg = Constants.MSG_ERROR_CONNECTION_SERVER_VERSION_HIGH;
                        style = Constants.FAILURE;
                        islogin = false;
                    }
                    else if (availableVersion == -1)
                    {
                        msg = Constants.MSG_ERROR_CONNECTION_SERVER_VERSION_LOW;
                        style = Constants.FAILURE;
                        islogin = false;
                    }
                }
                else if (Action.restartDI.equals(result))
                {
                    msg = Constants.INSTALLCERT_RESTART;
                    AmbOptionPane.showMessageDialog(
                            Constants.INSTALLCERT_RESTART, "Warning",
                            JOptionPane.WARNING_MESSAGE);
                }
                else
                {
                    msg = Constants.MSG_ERROR_CONFIGURE_USER + " with user "
                            + p_user;
                    log.info(msg);
                    AmbOptionPane.showMessageDialog(msg, "Warning",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
            catch (NotSupportHttpsException e)
            {
                msg = Constants.MSG_ERROR_TIMEDOUT + " - " + p_user.getHost();
            }
            catch (Exception e)
            {
                String errorMsgLower = e.getMessage().toLowerCase();
                if (errorMsgLower.indexOf("timed out") != -1)
                {
                    msg = Constants.MSG_ERROR_TIMEDOUT + " - "
                            + p_user.getHost();
                }
                else if (errorMsgLower.indexOf("network") != -1
                        || errorMsgLower.indexOf("connectexception") != -1)
                {
                    msg = Constants.MSG_ERROR_CONNECTION + " - "
                            + p_user.getHost();
                }
                else if (errorMsgLower.indexOf("permission") != -1)
                {
                    msg = Constants.MSG_ERROR_PERMISSION + " - " + p_user;
                }
                else if (errorMsgLower.indexOf("username") != -1)
                {
                    msg = Constants.MSG_ERROR_USER + " - " + p_user;
                }
                else if (errorMsgLower.indexOf("encryption") != -1
                        || errorMsgLower.indexOf("webserviceexception") != -1)
                {
                    msg = Constants.MSG_ERROR_REMOTE + " - " + p_user.getHost();
                }
                else if (errorMsgLower
                        .indexOf("java.lang.numberformatexception") != -1)
                {
                    msg = Constants.MSG_ERROR_HOST_OR_PORT;
                }
                else
                {
                    msg = "Unknown exception - " + e.getMessage();
                }

                if (msg.indexOf("Unknown") == 0)
                {
                    log.error(msg, e);
                }
                else
                {
                    log.error(msg);
                }

                AmbOptionPane.showMessageDialog(msg, "Warning",
                        JOptionPane.ERROR_MESSAGE);
            }
            finally
            {
                CacheUtil.getInstance().setLoginingUser(null);
            }
        }

        // set status and start download thread
        setStatus(msg, style);
        if (islogin)
        {
            startDownloadThread();
        }
        else
        {
            stopDownloadThread();
        }
        return islogin;
    }

    /**
     * Add message to show
     * 
     * @param p_job
     */
    public void addJobDownloaded(Job p_job)
    {
        jobsNewList.add(p_job);
        jobsAllList.add(p_job);
        msgCenterBar.setText("New Msg");
        if (noticeThread == null)
        {
            noticeThread = new NoticeThread();
            noticeThread.start();
        }
    }

    /**
     * Add files into file list, the files will be saved in cache and added into
     * file list after logon successefully
     * 
     * @param p_files
     */
    public void addFiles(File[] p_files)
    {
        for (int i = 0; i < p_files.length; i++)
        {
            File file = p_files[i];
            if (!UsefulTools.isInTheList(file, m_files))
            {
                m_files.add(file);
            }
        }

        addFiles();
    }

    /**
     * check the DI version, if need, we can add auto update function
     * 
     * see <code>private static String VERSION = "(2.1,2.1)"; in
     * com/globalsight/webservices/Ambassador.java </code>
     */
    public int checkVersion()
    {
        int availableVersion = 0;
        try
        {
            GetVersionAction getV = new GetVersionAction();
            String version = getV.execute(new String[]
            {});
            int index = version.indexOf(",");
            String minimalSupportedDIVersion = version.substring(1, index);
            String currentGSVersion = version.substring(index + 1,
                    version.length() - 1);
            String currentDIVersion = Constants.APP_VERSION_STRING;

            if (StringUtil.compareStringNum(currentDIVersion,
                    minimalSupportedDIVersion) < 0)
            {
                AmbOptionPane
                        .showMessageDialog(
                                "Your desktop icon is out of date. \n Please upgrade according to the correct version.",
                                "Version Check", JOptionPane.WARNING_MESSAGE);
                UsefulTools.openFile(Constants.TRANSWARE_URL);
                availableVersion = -1;
            }
            else if (StringUtil.compareStringNum(currentDIVersion,
                    currentGSVersion) < 0)
            {
                // do nothing
                // AmbOptionPane.showMessageDialog(
                // "There ia a new version supported. Please update.",
                // "Version Check");
            }
            else if (StringUtil.compareStringNum(currentDIVersion,
                    currentGSVersion) > 0)
            {
                AmbOptionPane
                        .showMessageDialog(
                                "The server you connected has been out of date for the desktop icon you are using. \n Please logon to a newer server which is synchronized with your desktop icon version.",
                                "Version Check", JOptionPane.WARNING_MESSAGE);
                UsefulTools.openFile(Constants.TRANSWARE_URL);
                availableVersion = 1;
            }

        }
        catch (Exception e)
        {
            log.error("checkVersion exception: ", e);
            AmbOptionPane.showMessageDialog(
                    "An exception occurred when checking the version. "
                            + "\nProgram will exit. Please get help from "
                            + Constants.TRANSWARE_URL, "Message",
                    JOptionPane.WARNING_MESSAGE);
            System.exit(1);
        }
        return availableVersion;
    }

    // ///////////////////////////////////////////////////////////////
    // private methods
    // ///////////////////////////////////////////////////////////////
    private void addFiles()
    {
        if (m_files != null)
        {
            CreateJobPanel createJobPanel;
            if (isAddable(Constants.CREATE_JOB_TITLE))
            {
                createJobPanel = new CreateJobPanel();
                closeTabbedPanel.add(closeTabbedPanel.getTabCount(),
                        Constants.CREATE_JOB_TITLE, createJobPanel);
                addKeyListener(createJobPanel);
            }
            else
            {
                int i = getIndex(Constants.CREATE_JOB_TITLE);
                if (i == -1)
                {
                    i = 0;
                }
                createJobPanel = (CreateJobPanel) closeTabbedPanel
                        .getComponent(i);
                closeTabbedPanel.setSelectedIndex(i);
            }
            File[] fs = new File[m_files.size()];
            m_files.toArray(fs);
            m_files.clear();
            createJobPanel.addAllFiles(fs);
        }
    }

    private void init()
    {
        // set parameters
        // setLookAndFeel();
        setSize(width, height);
        setLocation();
        setIconImage(SwingHelper.getAmbassadorIconImage());
        setResizable(false);

        // init the tabbed pane, like Create Job, configure ServerURL and
        // Account
        initTabbedPane();
        // init status bar
        initStatusBar();
        // init mebu bar
        initMenuBar();
        setJMenuBar(jMenuBar);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(closeTabbedPanel, BorderLayout.CENTER);

        JPanel south = new JPanel();
        south.setLayout(new BorderLayout());
        south.add(statusBar, BorderLayout.WEST);
        south.add(msgCenterBar, BorderLayout.EAST);
        getContentPane().add(south, BorderLayout.SOUTH);
        // add file transfer handler
        ((JPanel) getContentPane()).setTransferHandler(FileTransferHandler
                .install());
        // add actions
        initActions();

        setStatus(Constants.MSG_WELCOME, Constants.SUCCESS);
    }

    private void setLookAndFeel()
    {
        if (UsefulTools.isWindowsOS() || UsefulTools.isLinux())
        {
            try
            {
                UIManager.setLookAndFeel(UIManager
                        .getSystemLookAndFeelClassName());
            }
            catch (Exception e)
            {
                e.printStackTrace();
                log.error("error when set look and feel in MainFrame", e);
            }
        }
    }

    public void stopDownloadThread()
    {
        try
        {
            downloadAction.stopDownload();
        }
        catch (Exception e)
        {
            log.error("Download error:" + e);
            AmbOptionPane.showMessageDialog(
                    "An exception occurred when stopping download thread \n"
                            + e.getMessage(), "Error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public void startDownloadThread()
    {
        try
        {
            User user = CacheUtil.getInstance().getCurrentUser();
            if (user != null)
            {
                if (user.isAutoDownload())
                    downloadAction.execute(new String[]
                    {});
                else
                    stopDownloadThread();
            }
        }
        catch (Exception e)
        {
            log.error("Download error:" + e);
            AmbOptionPane.showMessageDialog(
                    "An exception occurred when starting download thread \n"
                            + e.getMessage(), "Error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public void restartDownloadThread()
    {
        startDownloadThread();
    }

    private void setLocation()
    {
        if (ConfigureHelper.getLocation() != null)
        {
            setLocation(ConfigureHelper.getLocation());
        }
        else
        {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            Point location = new Point((screen.height - getHeight()) / 2,
                    (screen.width - getWidth()) / 2);
            setLocation(location);
        }
    }

    private void initStatusBar()
    {
        statusBar = new JLabel();
        statusBar.setFont(new Font(null, 0, 16));
        statusBar.setHorizontalAlignment(SwingConstants.LEFT);
        statusBar.setPreferredSize(new Dimension(width - 60, 20));
        statusBar.setText(status_prefix);

        msgCenterBar = new JLabel();
        msgCenterBar.setFont(new Font(null, 0, 10));
        msgCenterBar.setPreferredSize(new Dimension(60, 20));
        msgCenterBar.setText(status_msg);
        msgCenterBar.setToolTipText("Click to view new messages, "
                + "\ndouble-click to view all messages");
    }

    private void initTabbedPane()
    {
        closeTabbedPanel = new ClosableTabbedPane();
        closeTabbedPanel.setSize(width, height - 60);
        CreateJobPanel createJobPanel = new CreateJobPanel();
        closeTabbedPanel.add(closeTabbedPanel.getTabCount(),
                Constants.CREATE_JOB_TITLE, createJobPanel);
    }

    private void initMenuBar()
    {
        // init the menu
        jm_configure = new JMenu("Configure");
        jm_view = new JMenu(" View ");
        jm_jobs = new JMenu(" Jobs ");
        jm_help = new JMenu(" Help ");
        jm_exit = new JMenu(" Exit ");

        // add menu to menu bar
        jMenuBar.add(jm_configure);
        jMenuBar.add(jm_view);
        jMenuBar.add(jm_jobs);
        jMenuBar.add(jm_help);
        jMenuBar.add(jm_exit);

        // init the menu item
        jmi_configure_user = new JMenuItem("User Options");
        jmi_view_user = new JMenuItem("View User");
        jmi_Preferences = new JMenuItem("Preferences");
        jmi21 = new JMenuItem("Jobs In Progress (IE)");
        jmi22 = new JMenuItem("Jobs Pending  (IE)");
        jmi23 = new JMenuItem("Reports  (IE)");
        jmi24 = new JMenuItem("Jobs In Progress (Safari)");
        jmi25 = new JMenuItem("Jobs Pending  (Safari)");
        jmi26 = new JMenuItem("Reports  (Safari)");
        jmi27 = new JMenuItem("Jobs In Progress (Firefox)");
        jmi28 = new JMenuItem("Jobs Pending  (Firefox)");
        jmi29 = new JMenuItem("Reports  (Firefox)");

        jmi31 = new JMenuItem("Create Job");
        jmi31.setAccelerator(KeyStroke.getKeyStroke('c'));
        jmi32 = new JMenuItem("Connect to CVS");

        jmi41 = new JMenuItem("Help");
        jmi42 = new JMenuItem("About");
        jmi43 = new JMenuItem("View log file");
        jmi44 = new JMenuItem("CVS Help");

        jmi51 = new JMenuItem("Exit");
        jmi51.setAccelerator(KeyStroke.getKeyStroke('e'));

        // add memuItem to menu
        jm_configure.add(jmi_configure_user);
        jm_configure.add(jmi_view_user);

        jmi_proxy = new JMenuItem("Proxy");
        jmi_proxy.setAccelerator(KeyStroke.getKeyStroke('p'));
        jm_configure.add(jmi_proxy);
        jm_configure.add(jmi_Preferences);

        // ie or safari
        if (UsefulTools.isWindowsOS())
        {
            jm_view.add(jmi21);
            jm_view.add(jmi22);
            jm_view.add(jmi23);
        }
        else if (UsefulTools.isMacOS())
        {
            jm_view.add(jmi24);
            jm_view.add(jmi25);
            jm_view.add(jmi26);
        }
        // firefox
        jm_view.add(jmi27);
        jm_view.add(jmi28);
        jm_view.add(jmi29);

        jm_jobs.add(jmi31);
        jm_jobs.add(jmi32);
        // set this menu invisible before logon() api is invoked and return the
        // accesstoken
        jmi32.setVisible(false);

        jm_help.add(jmi41);
        jm_help.add(jmi44);
        // set this menu invisible before login() api is invoked and return the
        // accesstoken
        jmi44.setVisible(false);
        jm_help.add(jmi42);
        jm_help.add(jmi43);

        jm_exit.add(jmi51);
    }

    private void initActions()
    {
        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                exit();
            }
        });

        initActionsOfMenu();

        addKeyListener(this);

        initActionOfMsgCenter();
    }

    private void initActionOfMsgCenter()
    {
        // statusBar.addMouseListener(new MouseAdapter()
        // {
        // public void mouseClicked(MouseEvent e)
        // {
        // User u = new User("user1", "password1", new Host("host1", "70"));
        // addJobDownloaded(new Job("job1_111111111111111111111111111", u,
        // null, null, null));
        // addJobDownloaded(new Job("job2_111111111111111111111111111", u,
        // null, null, null));
        // addJobDownloaded(new Job("job3_111111111111111111111111111", u,
        // null, null, null));
        // addJobDownloaded(new Job("job4_111111111111111111111111111", u,
        // null, null, null));
        // addJobDownloaded(new Job("job5_111111111111111111111111111", u,
        // null, null, null));
        // addJobDownloaded(new Job("job6_111111111111111111111111111", u,
        // null, null, null));
        // addJobDownloaded(new Job("job7_111111111111111111111111111", u,
        // null, null, null));
        // addJobDownloaded(new Job("job8_111111111111111111111111111", u,
        // null, null, null));
        // }
        // });

        msgCenterBar.addMouseListener(new MouseAdapter()
        {
            private List jobList = null;

            private boolean isAll = false;

            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 1)
                {
                    jobList = jobsNewList;
                    isAll = false;
                    showMessage();
                }
                else if (e.getClickCount() == 2)
                {
                    jobList = jobsAllList;
                    isAll = true;
                    showMessage();
                }
            }

            private void showMessage()
            {
                Font fontJobname = new Font("Arial", Font.PLAIN, 10);
                Font fontTitle = new Font("Arial", Font.PLAIN, 12);
                Box contain = Box.createVerticalBox();
                JScrollPane jsp = new JScrollPane(contain);
                int w = 200, h = 150;
                jsp.setPreferredSize(new Dimension(w, h));
                contain.add(Box.createVerticalStrut(10));
                int i = 0;
                for (Iterator iter = jobList.iterator(); iter.hasNext(); i++)
                {
                    Job job = (Job) iter.next();
                    Box box = Box.createHorizontalBox();
                    JLabel l = new JLabel("Job: " + job.getName() + "  ");
                    l.setFont(fontJobname);
                    box.add(l);
                    JButton jb = new JButton("View");
                    jb.setActionCommand("" + i);
                    jb.setFont(fontJobname);
                    box.add(jb);
                    jb.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                            int index = Integer.parseInt(e.getActionCommand());
                            Job job = (Job) jobList.get(index);
                            String path = job.getOwner().getSavepath();
                            UsefulTools.openFile(path);
                        }
                    });

                    contain.add(box);
                }

                MainFrame m = SwingHelper.getMainFrame();

                String titleName = (isAll) ? "All Messages: "
                        : "New Messages: ";

                if (jobList.isEmpty())
                {
                    titleName += " no message ";
                }
                else
                {
                    titleName += " new downloaded jobs";
                }

                Border b = BorderFactory.createTitledBorder(jsp.getBorder(),
                        titleName, TitledBorder.LEFT, TitledBorder.TOP,
                        fontTitle, Color.BLUE);
                jsp.setBorder(b);
                contain.addMouseListener(new MouseAdapter()
                {
                    public void mouseClicked(MouseEvent e)
                    {
                        popupPanel.hide();
                        if (noticeThread != null)
                            noticeThread.stopMe();
                        jobsNewList.clear();
                    }
                });
                if (popupPanel != null)
                    popupPanel.hide();
                popupPanel = PopupFactory.getSharedInstance().getPopup(m, jsp,
                        m.getX() + m.getWidth() - w,
                        m.getY() + m.getHeight() - h - 20);
                popupPanel.show();
            }

        });
    }

    public void addKeyListener(Component p_c)
    {
        SwingHelper.addKeyListener(p_c, new KeyAdapter()
        {
            public void keyPressed(KeyEvent event)
            {
                if ((event.getKeyCode() == 'q' || event.getKeyCode() == 'Q')
                        && event.getModifiers() == KeyEvent.ALT_MASK)
                {
                    exit();
                }
                else if ((event.getKeyCode() == 'c' || event.getKeyCode() == 'C')
                        && event.getModifiers() == KeyEvent.ALT_MASK)
                {
                    showCreateJobPane();
                }
                else if ((event.getKeyCode() == 'v' || event.getKeyCode() == 'V')
                        && event.getModifiers() == KeyEvent.CTRL_MASK
                        && !(event.getComponent() instanceof JList))
                {
                    try
                    {
                        Clipboard c = Toolkit.getDefaultToolkit()
                                .getSystemClipboard();
                        DataFlavor fileFlavor = DataFlavor.javaFileListFlavor;
                        Transferable data = c.getContents(new Object());
                        List list = (List) data.getTransferData(fileFlavor);
                        if (list != null && list.size() > 0)
                        {
                            addFiles(list2Array(list));
                        }
                    }
                    catch (UnsupportedFlavorException e)
                    {
                        // do nothing
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void initActionsOfMenu()
    {
        // configure
        jmi_configure_user.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (isAddable(Constants.USER_CONFIGURE_TITLE))
                {
                    UserOptionsPanel userConfigurePanel = new UserOptionsPanel();
                    closeTabbedPanel.add(closeTabbedPanel.getTabCount(),
                            Constants.USER_CONFIGURE_TITLE, userConfigurePanel);
                    addKeyListener(userConfigurePanel);
                }
                else
                {
                    closeTabbedPanel
                            .setSelectedIndex(getIndex(Constants.USER_CONFIGURE_TITLE) == -1 ? 0
                                    : getIndex(Constants.USER_CONFIGURE_TITLE));
                }
            }
        });

        // configure
        jmi_view_user.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                UsersDialog ud = new UsersDialog(SwingHelper.getMainFrame(),
                        true);
                ud.setVisible(true);
            }
        });

        // view by ruby
        // ie
        jmi21.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (!hasLogon())
                    return;

                ExecAction openBrowserAction = new ExecAction();
                try
                {
                    openBrowserAction.execute(new String[]
                    { ruby, Constants.RUBY_INPROGRESS_IE }, new String[]
                    { Constants.RUBY_LIB_WATIR });
                }
                catch (Exception e1)
                {
                    log.warn("Can't access the inprogress jobs page. " + e1);
                }
            }
        });

        jmi22.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (!hasLogon())
                    return;

                ExecAction openBrowserAction = new ExecAction();
                try
                {
                    openBrowserAction.execute(new String[]
                    { ruby, Constants.RUBY_PENDING_IE }, new String[]
                    { Constants.RUBY_LIB_WATIR });
                }
                catch (Exception e1)
                {
                    log.warn("Can't access the ready jobs page. " + e1);
                }
            }
        });

        jmi23.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (!hasLogon())
                    return;

                ExecAction openBrowserAction = new ExecAction();
                try
                {
                    openBrowserAction.execute(new String[]
                    { ruby, Constants.RUBY_REPORT_IE }, new String[]
                    { Constants.RUBY_LIB_WATIR });
                }
                catch (Exception e1)
                {

                    log.warn("Can't access the reports page. " + e1);
                }
            }
        });

        // safari
        jmi24.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (!hasLogon())
                    return;

                ExecAction openBrowserAction = new ExecAction();
                try
                {
                    openBrowserAction.execute(new String[]
                    { ruby, Constants.RUBY_GOTOURL_SF });
                    Thread.sleep(1000 * 3);
                    openBrowserAction.execute(new String[]
                    { ruby, Constants.RUBY_INPROGRESS_SF });
                }
                catch (Exception e1)
                {
                    log.warn("Can't access the inprogress jobs page. " + e1);
                }
            }
        });

        jmi25.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (!hasLogon())
                    return;

                ExecAction openBrowserAction = new ExecAction();
                try
                {
                    openBrowserAction.execute(new String[]
                    { ruby, Constants.RUBY_GOTOURL_SF });
                    Thread.sleep(1000 * 3);
                    openBrowserAction.execute(new String[]
                    { ruby, Constants.RUBY_PENDING_SF });
                }
                catch (Exception e1)
                {
                    log.warn("Can't access the ready jobs page. " + e1);
                }
            }
        });

        jmi26.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (!hasLogon())
                    return;

                ExecAction openBrowserAction = new ExecAction();
                try
                {
                    openBrowserAction.execute(new String[]
                    { ruby, Constants.RUBY_GOTOURL_SF });
                    Thread.sleep(1000 * 3);
                    openBrowserAction.execute(new String[]
                    { ruby, Constants.RUBY_REPORT_SF });
                }
                catch (Exception e1)
                {
                    log.warn("Can't access the reports page. " + e1);
                }
            }
        });

        // Firefox
        jmi27.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (!hasLogon())
                    return;

                try
                {
                    openJobFireFox("jobsInProgress");
                }
                catch (Exception e1)
                {
                    log.warn("Can't access the inprogress jobs page. " + e1);
                }
            }
        });

        jmi28.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (!hasLogon())
                    return;

                try
                {
                    openJobFireFox("jobsPending");
                }
                catch (Exception e1)
                {
                    log.warn("Can't access the ready jobs page. " + e1);
                }
            }
        });

        jmi29.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (!hasLogon())
                    return;

                try
                {
                    openPageFireFox("reports", false);
                }
                catch (Exception e1)
                {

                    log.warn("Can't access the reports page. " + e1);
                }
            }
        });

        // Create job
        jmi31.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                showCreateJobPane();
            }
        });

        // Connect to CVS
        jmi32.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (isAddable(Constants.CONNECT_TO_CVS))
                {
                    CVSMainPanel cvsMainPanel = new CVSMainPanel();
                    closeTabbedPanel.add(closeTabbedPanel.getTabCount(),
                            Constants.CONNECT_TO_CVS, cvsMainPanel);
                    addKeyListener(cvsMainPanel);
                }
                else
                {
                    closeTabbedPanel
                            .setSelectedIndex(getIndex(Constants.CONNECT_TO_CVS) == -1 ? 0
                                    : getIndex(Constants.CONNECT_TO_CVS));
                }
            }
        });

        // help
        jmi41.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                UsefulTools.openBrowser(Constants.SITE_DI_WIKI);
            }
        });

        // cvs help
        jmi44.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    UsefulTools.openFile(Constants.CVS_HELP_FILE);
                }
                catch (Exception e1)
                {
                    log.warn("Can't open the help file with " + e1);
                }
            }
        });

        // about
        jmi42.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                AboutDialog about = new AboutDialog(MainFrame.this);
                about.setLocationRelativeTo(jmi42);
                about.setFocusable(true);
                about.setVisible(true);
            }
        });

        // view log
        jmi43.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                UsefulTools.openFile(Constants.LOG_FILE);
            }
        });

        // exit
        jmi51.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // checkFirefoxWithJSSHRunningOnMac();
                exit();
            }
        });

        jmi_proxy.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                ProxyDialog proxy = new ProxyDialog(MainFrame.this);
                proxy.setFocusable(true);
                proxy.setLocationRelativeTo(jmi_proxy);
                proxy.setVisible(true);
            }
        });

        // configure
        jmi_Preferences.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                PreferencesDialog pref = new PreferencesDialog(MainFrame.this);
                pref.setVisible(true);
            }
        });
    }

    private void exit()
    {
        if (AmbOptionPane.showConfirmDialog("Exit Desktop Icon? ", "Exit",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
        {
            writeLocation();
            System.exit(0);
        }
        else
        {
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        }
    }

    private void showCreateJobPane()
    {
        if (isAddable(Constants.CREATE_JOB_TITLE))
        {
            CreateJobPanel createJobPanel = new CreateJobPanel();
            closeTabbedPanel.add(closeTabbedPanel.getTabCount(),
                    Constants.CREATE_JOB_TITLE, createJobPanel);
            addKeyListener(createJobPanel);
        }
        else
        {
            closeTabbedPanel
                    .setSelectedIndex(getIndex(Constants.CREATE_JOB_TITLE) == -1 ? 0
                            : getIndex(Constants.CREATE_JOB_TITLE));
        }
    }

    private boolean hasLogon()
    {
        User user = LoginAction.USER;
        if (user == null)
        {
            AmbOptionPane.showMessageDialog("Please login first", "Info");
            return false;
        }

        return true;
    }

    private void openJobFireFox(String state)
    {
        openPageFireFox(state, true);
    }

    private void openPageFireFox(String activity, boolean isJob)
    {
        // A "base url", used by selenium to resolve relative URLs
        User user = LoginAction.USER;

        WebDriver driver = new FirefoxDriver();
        String baseUrl = user.isUseSSL() ? "https://" : "http://"
                + user.getHost().getName().trim() + ":"
                + user.getHost().getPortString();

        // Create the Selenium implementation
        Selenium selenium = new WebDriverBackedSelenium(driver, baseUrl);

        // Perform actions with selenium
        selenium.open("/globalsight");
        selenium.type("name=nameField", user.getName());
        selenium.type("name=passwordField", user.getPassword());
        selenium.click("name=login0");

        String url = "/globalsight/ControlServlet?activityName=" + activity;
        if (isJob)
        {
            url += "&searchType=stateOnly";
        }
        selenium.open(url);
    }

    private void writeLocation()
    {
        Point p = getLocation();
        ConfigureHelper.setLocation(p);
    }

    public boolean isAddable(String title)
    {
        for (int i = 0; i < closeTabbedPanel.getTabCount(); i++)
        {
            if (closeTabbedPanel.getTitleAt(i).equals(title))
            {
                return false;
            }
        }
        return true;
    }

    public int getIndex(String title)
    {
        for (int i = 0; i < closeTabbedPanel.getTabCount(); i++)
        {
            if (closeTabbedPanel.getTitleAt(i).equals(title))
            {
                return i;
            }
        }

        return -1;
    }

    public ClosableTabbedPane getCloseTabbedPanel()
    {
        return closeTabbedPanel;
    }

    private void checkFirefoxWithJSSHRunningOnMac()
    {
        String result = UsefulTools.checkFirefoxWithJSSHRunningOnMac();
        if (!result.equals(UsefulTools.EMPTY_MSG))
        {
            MsgDialog msgDialog = new MsgDialog(MainFrame.this);
            msgDialog.setLocationRelativeTo(jmi27);

            if (result.equals(UsefulTools.ERROR_SOCKET))
            {
                msgDialog.setMsg(result + "\n\nClick OK button to "
                        + START_FIREFOX_JSSH);
            }
            else
            {
                msgDialog.setMsg(result);
            }

            msgDialog.setFocusable(true);

            msgDialog.show();
        }
    }

    private File[] list2Array(List list)
    {
        if (list == null)
        {
            return new File[0];
        }

        File[] files = new File[list.size()];
        for (int i = 0; i < files.length; i++)
        {
            files[i] = (File) list.get(i);
        }

        return files;
    }

    class NoticeThread extends Thread
    {
        private boolean m_run = true;

        public void run()
        {
            Border blue = BorderFactory.createLineBorder(Color.BLUE);
            Border green = BorderFactory.createLineBorder(Color.GREEN);
            while (m_run)
            {
                try
                {
                    if (m_run)
                        msgCenterBar.setBorder(blue);
                    Thread.sleep(1000);
                    if (m_run)
                        msgCenterBar.setBorder(green);
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    log.error("Error when show msg notice", e);
                }
            }
        }

        public void stopMe()
        {
            m_run = false;
            msgCenterBar.setBorder(BorderFactory.createEmptyBorder());
            msgCenterBar.setText(status_msg);
            noticeThread = null;
        }
    }

    /**
     * Sets proxy according to proxy.properties.
     */
    private void setProxy()
    {
        File file = new File(Constants.CONFIGURE_XML_PROXY);

        // the file not exists means havn't set proxy before, so do nothing.
        if (!file.exists())
        {
            return;
        }

        try
        {
            FileInputStream fis = new FileInputStream(file);
            Properties properties = new Properties();
            properties.load(fis);
            fis.close();
            boolean useProxy = "true".equals(properties
                    .getProperty("USE_PROXY").trim());
            if (useProxy)
            {
                String proxyHost = properties.getProperty("HOST");
                String proxyPort = properties.getProperty("PORT");
                AxisProperties.setProperty("http.proxyHost", proxyHost);
                AxisProperties.setProperty("http.proxyPort", proxyPort);
            }
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
            e.printStackTrace();
        }
    }

}
