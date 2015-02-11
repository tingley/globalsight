package com.globalsight.util2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.InputSource;

import com.globalsight.entity.FileMapped;
import com.globalsight.entity.FileProfile;
import com.globalsight.entity.Host;
import com.globalsight.entity.Job;
import com.globalsight.entity.User;
import com.globalsight.util.Constants;
import com.globalsight.util.UsefulTools;

/**
 * A wrapper and util class for configure.xml, support multi user & multi server
 * since DesktopIcon V3.0
 * 
 * @author quincy.zou
 */
public class ConfigureHelperV2
{
    private static String e_root = "amb-desktopicon-configure";

    private static String e_runcount = "run-count";

    private static String e_host = "host";

    private static String e_userSession = "user-session";

    private static String e_userpwd = "password";

    private static String e_usercorp = "company-name";

    private static String e_downlist = "download-from";

    private static String e_minutes = "download-minutes";

    private static String e_jobs = "jobs";

    private static String e_job = "job";

    private static String e_jobname = "jobname";

    private static String e_downloader = "download-user";

    private static String e_file = "file";

    private static String e_savepath = "save-path";

    private static String e_pref = "preferences";

    private static String e_pref_jobSplitting = "hide-job-splitting";

    private static String e_pref_jobPriority = "hide-job-priority";

    private static String a_isdefault = "isdefault";

    private static String a_autodown = "autodownload";

    private static String a_hosturl = "url";

    private static String a_useSSL = "useSSL";

    private static String a_username = "username";

    private static String a_version = "version";

    private static String a_createtime = "ctime";

    private static String a_downtime = "dltime";

    private static String a_fpid = "fpid";

    private static String a_fpname = "fpname";

    private static String a_display = "display";

    private static String a_cvsjob = "cvsjob";

    private static String a_cvssourcelocale = "sourcelocale";

    private static String v_true = "true";

    private static String v_false = "false";

    public static String v_na = "N/A";

    private static String corm = "company_";

    static Logger log = Logger.getLogger(ConfigureHelperV2.class.getName());

    private static DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssZ");

    private static Document m_doc = null;

    private static long m_lasttime = 0l;

    static
    {
        try
        {
            Document oldDoc = readConfigureFile();
            Document doc = null;
            boolean write = false;
            if (oldDoc == null)
            {
                Element root = DocumentHelper.createElement(e_root);
                root.addAttribute(a_version, Constants.APP_VERSION_STRING);
                root.addElement(e_runcount).setText("0");
                root.addElement(e_pref);

                doc = DocumentHelper.createDocument();
                doc.setRootElement(root);

                write = true;
            }
            else
            {
                if ("configure".equals(oldDoc.getRootElement().getName()))
                {
                    doc = toV2(oldDoc);
                    write = true;
                    writeFormattedXML(oldDoc, new File(
                            Constants.CONFIGURE_XML_FORMER), true, false);
                }
            }

            if (write)
            {
                saveConfigureXML(doc);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            System.exit(1);
        }
    }

    // //////////////////////////////////////////////
    // public methods
    // //////////////////////////////////////////////

    public static String readDefaultSavepath() throws Exception
    {
        User user = readDefaultUser();
        if (user == null)
        {
            return null;
        }
        return user.getSavepath();
    }

    /**
     * read default user from configure file
     * 
     * @return
     * @throws Exception
     */
    public static User readDefaultUser() throws Exception
    {
        String path = "//" + e_userSession + "[@" + a_isdefault + "='" + v_true
                + "']";
        Element user = getElement(path);

        if (user == null)
        {
            return null;
        }

        User u = elementToUser(user, true);

        return u;
    }

    /**
     * read the jobSplitting in Preferences of configure file
     */
    public static boolean readPrefJobSplitting() throws Exception
    {
        boolean isShow = false;

        String path = "//" + e_pref_jobSplitting;
        Element elem = getElement(path);
        if (elem == null)
        {
            return isShow;
        }
        String isShowStr = elem.getText();
        if (v_true.equalsIgnoreCase(isShowStr))
        {
            isShow = true;
        }

        return isShow;
    }

    /**
     * read the jobPriority in Preferences of configure file
     */
    public static boolean readPrefJobPriority() throws Exception
    {
        boolean isShow = false;

        String path = "//" + e_pref_jobPriority;
        Element elem = getElement(path);
        if (elem == null)
        {
            return isShow;
        }
        String isShowStr = elem.getText();
        if (v_true.equalsIgnoreCase(isShowStr))
        {
            isShow = true;
        }

        return isShow;
    }

    public static String readRuncount() throws Exception
    {
        String path = "//" + e_runcount;
        Element runcount = getElement(path);
        return runcount.getText();
    }

    /**
     * return all user's downloadable jobs
     * 
     * @return the list of Job
     * @throws Exception
     */
    public static List readJobsToDownload() throws Exception
    {
        List l = new ArrayList();
        String path = "//" + e_job + "[@" + a_downtime + "='" + v_na + "'][@"
                + a_display + "='" + v_true + "']";
        List es = getElementsList(path);
        for (Iterator iter = es.iterator(); iter.hasNext();)
        {
            Element e = (Element) iter.next();
            Job j = elementToJob(e);
            l.add(j);
        }

        return l;
    }

    /**
     * return all user's jobs
     * 
     * @return the list of Job
     * @throws Exception
     */
    public static List readJobs() throws Exception
    {
        List l = new ArrayList();
        String path = "//" + e_job + "[@" + a_display + "='" + v_true + "']";
        List es = getElementsList(path);
        for (Iterator iter = es.iterator(); iter.hasNext();)
        {
            Element e = (Element) iter.next();
            Job j = elementToJob(e);
            l.add(j);
        }

        return l;
    }

    /**
     * return the user's jobs
     * 
     * @return the list of Job
     * @throws Exception
     */
    public static List readJobsByUser(User p_user) throws Exception
    {
        if (p_user == null)
        {
            return new ArrayList();
        }

        String hoststr = p_user.getHost().getFullName();
        List l = new ArrayList();
        String path = "//" + e_host + "[@" + a_hosturl + "='" + hoststr + "']"
                + "/" + e_userSession + "[@" + a_username + "='"
                + p_user.getName() + "']//" + e_job + "[@" + a_display + "='"
                + v_true + "']";
        List es = getElementsList(path);
        for (Iterator iter = es.iterator(); iter.hasNext();)
        {
            Element e = (Element) iter.next();
            Job j = elementToJob(e);
            l.add(j);
        }

        return l;
    }

    /**
     * return the user's downloadable jobs
     * 
     * @return the list of Job
     * @throws Exception
     */
    public static List readJobsToDownloadByUser(User p_user) throws Exception
    {
        if (p_user == null)
        {
            return new ArrayList();
        }

        String hoststr = p_user.getHost().getFullName();
        List l = new ArrayList();
        String path = "//" + e_host + "[@" + a_hosturl + "='" + hoststr + "']"
                + "/" + e_userSession + "[@" + a_username + "='"
                + p_user.getName() + "']//" + e_job + "[@" + a_downtime + "='"
                + v_na + "'][@" + a_display + "='" + v_true + "']";
        List es = getElementsList(path);
        for (Iterator iter = es.iterator(); iter.hasNext();)
        {
            Element e = (Element) iter.next();
            Job j = elementToJob(e);
            l.add(j);
        }

        return l;
    }

    /**
     * return all the jobs in user's download-list
     * 
     * @return the list of Job
     * @throws Exception
     */
    public static List readJobsByDlList(User p_user) throws Exception
    {
        if (p_user == null)
        {
            return new ArrayList();
        }

        List result = new ArrayList();
        User[] us = p_user.getDownloadUsers();
        if (us != null && us.length > 0)
        {
            for (int i = 0; i < us.length; i++)
            {
                User user = us[i];
                List l = readJobsByUser(user);
                if (l != null)
                    result.addAll(l);
            }
        }

        return result;
    }

    /**
     * return all the downloadable jobs in the user's download-list
     * 
     * @return the list of Job
     * @throws Exception
     */
    public static List readJobsToDownloadByDlList(User p_user) throws Exception
    {
        if (p_user == null)
        {
            return new ArrayList();
        }

        List result = new ArrayList();
        User[] us = p_user.getDownloadUsers();
        if (us != null && us.length > 0)
        {
            for (int i = 0; i < us.length; i++)
            {
                User user = us[i];
                List l = readJobsToDownloadByUser(user);
                if (l != null)
                    result.addAll(l);
            }
        }

        return result;
    }

    /**
     * read all users from configure file
     * 
     * @return the list of all Users
     * @throws Exception
     */
    public static List readAllUsers() throws Exception
    {
        String path = "//" + e_userSession;
        List users = getElementsList(path);

        if (users == null || users.isEmpty())
        {
            return new ArrayList();
        }

        List r = new ArrayList();
        for (Iterator iter = users.iterator(); iter.hasNext();)
        {
            Element user = (Element) iter.next();
            User u = elementToUser(user, true);
            r.add(u);
        }

        return r;
    }

    /**
     * read all users from Host
     * 
     * @return the list of all Users
     * @throws Exception
     */
    public static List readAllUsers(Host p_host) throws Exception
    {
        String path = "//" + e_host + "[@" + a_hosturl + "='"
                + p_host.getFullName() + "']/" + e_userSession;
        List users = getElementsList(path);

        if (users == null || users.isEmpty())
        {
            return new ArrayList();
        }

        List r = new ArrayList();
        for (Iterator iter = users.iterator(); iter.hasNext();)
        {
            Element user = (Element) iter.next();
            User u = elementToUser(user, true);
            r.add(u);
        }

        return r;
    }

    public static void addRuncount() throws Exception
    {
        String path = "//" + e_runcount;
        Element runcount = getElement(path);
        int c = Integer.parseInt(runcount.getText());
        writeRuncount(++c);
    }

    /**
     * create or update the jobSplitting in Preferences of configure file
     */
    public static void writePrefJobSplitting(String isShowStr) throws Exception
    {
        modifyPrefJobSplitting(isShowStr);
    }

    /**
     * create or update the jobPrority in Preferences of configure file
     */
    public static void writePrefJobPriority(String isShowStr) throws Exception
    {
        modifyPrefJobPriority(isShowStr);
    }

    /**
     * add / update p_user, make it as default user. And save it in configure
     * file
     * 
     * @param p_user
     * @return
     * @throws Exception
     */
    public static Element writeDefaultUser(User p_user) throws Exception
    {
        return writeUser(p_user, true);
    }

    /**
     * add / update p_user. And save it in configure file
     * 
     * @param p_user
     * @return
     * @throws Exception
     */
    public static Element writeUser(User p_user) throws Exception
    {
        User u = readDefaultUser();
        if (p_user.equals(u))
        {
            return writeDefaultUser(p_user);
        }
        else
        {
            return writeUser(p_user, false);
        }
    }

    /**
     * add / update p_host and save it in configure file
     * 
     * @param p_host
     * @return
     * @throws Exception
     */
    public static Element writeHost(Host p_host) throws Exception
    {
        return addHostElement(p_host, true);
    }

    /**
     * check if the jobName exists in the p_user's host
     * 
     * @param p_user
     * @param p_jobName
     * @return
     * @throws Exception
     */
    public static boolean isJobnameOK(User p_user, String p_jobName)
            throws Exception
    {
        Host h = p_user.getHost();
        List allusers = readAllUsers();
        for (Iterator iter = allusers.iterator(); iter.hasNext();)
        {
            User user = (User) iter.next();
            if (user.getHost().equals(h))
            {
                List jobs = readJobsByDlList(user);
                for (Iterator it = jobs.iterator(); it.hasNext();)
                {
                    Job job = (Job) it.next();
                    if (job.getName().equalsIgnoreCase(p_jobName))
                    {
                        return false;
                    }
                }
            }

        }
        return true;
    }

    /**
     * Write new job in configure file
     * 
     * @param user
     * @param jobName
     * @return null if there is already a job with the same name
     * @throws Exception
     */
    public static Element writeNewJob(Job p_job) throws Exception
    {
        User user = p_job.getOwner();
        String jobName = p_job.getName();
        if (!isJobnameOK(user, jobName))
        {
            throw new Exception("job " + jobName + " exists");
        }

        Element ue = readUserElement(user);
        if (ue == null)
        {
            writeUser(user);
            ue = readUserElement(user);
        }
        Element job = ue.element(e_jobs).addElement(e_job);
        // write job parameters
        job.addElement(e_jobname).setText(jobName);
        job.addElement(e_downloader).setText(p_job.getDownloadUser());
        job.addAttribute(a_createtime, formatDate(p_job.getCreateDate()));
        job.addAttribute(a_downtime, v_na);
        job.addAttribute(a_display, v_true);

        // Added by Vincent
        job.addAttribute(a_cvsjob, p_job.isCVSJob() ? v_true : v_false);
        job.addAttribute(a_cvssourcelocale, p_job.getSourceLocale());
        // End of Added

        // write files in job
        List fms = p_job.getFileMappedList();
        for (Iterator iter = fms.iterator(); iter.hasNext();)
        {
            FileMapped fm = (FileMapped) iter.next();
            String filename = fm.getFile().getAbsolutePath();
            String fpid = fm.getFileProfile().getId();
            String fpname = fm.getFileProfile().getName();
            Element file = job.addElement(e_file);
            file.setText(filename);
            file.addAttribute(a_fpid, fpid);
            file.addAttribute(a_fpname, fpname);
        }

        saveConfigureXML(job.getDocument());

        return job;
    }

    public static Element writeDownloadedJob(Job p_job) throws Exception
    {
        Element job = readJobElement(p_job);
        if (job == null)
        {
            return null;
        }

        job.addAttribute(a_downtime, formatDate(new Date()));
        job.element(e_downloader).setText(p_job.getDownloadUser());
        saveConfigureXML(job.getDocument());

        return job;
    }

    public static Element removeUser(User p_user) throws Exception
    {
        // TODO no need now
        throw new UnsupportedOperationException(
                "This method is not support now");
    }

    public static Element removeJob(Job p_job) throws Exception
    {
        Element je = readJobElement(p_job);
        je.addAttribute(a_display, v_false);
        saveConfigureXML(je.getDocument());

        return je;
    }

    // //////////////////////////////////////////////
    // private methods
    // //////////////////////////////////////////////

    private static Element writeUser(User p_user, boolean isdefault)
            throws Exception
    {
        if (isdefault)
        {
            User defaultUser = readDefaultUser();
            while (defaultUser != null)
            {
                writeUser(defaultUser, false);
                defaultUser = readDefaultUser();
            }
        }

        Element ue = readUserElement(p_user);

        if (ue == null)
        {
            String hoststr = p_user.getHost().getFullName();
            String path = "//" + e_host + "[@" + a_hosturl + "='" + hoststr
                    + "']";
            Element host = getElement(path);
            if (host == null)
            {
                host = addHostElement(p_user.getHost(), false);
            }

            ue = host.addElement(e_userSession);
            ue.addAttribute(a_username, p_user.getName());
            ue.addAttribute(a_autodown, (p_user.isAutoDownload()) ? v_true
                    : v_false);
            ue.addAttribute(a_useSSL, "" + p_user.isUseSSL());
            ue.addElement(e_usercorp).setText(encodeCompanyName(p_user));
            ue.addElement(e_downlist).setText(encodeDownloadUsers(p_user));
            ue.addElement(e_userpwd).setText(
                    StringUtil.encryptString(p_user.getPassword()));
            ue.addElement(e_savepath).setText(p_user.getSavepath());
            ue.addElement(e_minutes).setText(p_user.getMinutes());
            ue.addElement(e_jobs);
        }
        else
        {
            ue.addAttribute(a_autodown, (p_user.isAutoDownload()) ? v_true
                    : v_false);
            ue.addAttribute(a_useSSL, "" + p_user.isUseSSL());
            String oriCorm = decodeCompanyName(ue.element(e_usercorp).getText());
            if (!"".equals(p_user.getCompanyName().trim())
                    || "".equals(oriCorm.trim()))
            {
                ue.element(e_usercorp).setText(encodeCompanyName(p_user));
            }
            ue.element(e_downlist).setText(encodeDownloadUsers(p_user));
            ue.element(e_userpwd).setText(
                    StringUtil.encryptString(p_user.getPassword()));
            ue.element(e_savepath).setText(p_user.getSavepath());
            ue.element(e_minutes).setText(p_user.getMinutes());
        }
        // is default user
        if (isdefault)
        {
            ue.addAttribute(a_isdefault, v_true);
        }
        else
        {
            ue.addAttribute(a_isdefault, v_false);
        }

        Document doc = ue.getDocument();
        saveConfigureXML(doc);

        return ue;
    }

    private static Element addHostElement(Host p_host, boolean p_save)
            throws Exception
    {
        String hoststr = p_host.getFullName();
        String path = "//" + e_host + "[@" + a_hosturl + "='" + hoststr + "']";
        Element he = getElement(path);
        if (he == null)
        {
            Element root = getElement("/" + e_root);
            he = root.addElement(e_host);
            he.addAttribute(a_hosturl, hoststr);

            if (p_save)
            {
                Document doc = he.getDocument();
                saveConfigureXML(doc);
            }
        }
        return he;
    }

    /**
     * parse a element to User
     * 
     * @param p_ue
     * @param getDownloadList
     *            true to get download list
     * @return
     * @throws Exception
     */
    private static User elementToUser(Element p_ue, boolean getDownloadList)
            throws Exception
    {
        if (p_ue == null || !p_ue.getName().equals(e_userSession))
        {
            return null;
        }

        Element host = p_ue.getParent();
        Host h = elementToHost(host);

        String useSSLstr = p_ue.attributeValue(a_useSSL);
        boolean useSSL = false;
        try
        {
            useSSL = Boolean.parseBoolean(useSSLstr);
        }
        catch (Exception e)
        {
            // do nothing
        }
        boolean autodown = p_ue.attributeValue(a_autodown).equals(v_true);
        String pwd = StringUtil
                .decryptString(p_ue.element(e_userpwd).getText());
        String name = p_ue.attributeValue(a_username);
        String savepath = p_ue.elementText(e_savepath);
        String minutes = p_ue.elementText(e_minutes);

        User u = new User(name, pwd, h, savepath, minutes, autodown);
        String companyName = decodeCompanyName(p_ue.elementText(e_usercorp));
        u.setCompanyName(companyName);
        u.setUseSSL(useSSL);

        if (getDownloadList)
        {
            User[] users = decodeDownloadUsers(p_ue.elementText(e_downlist), h);
            u.setDownloadUsers(users);
        }

        return u;
    }

    private static User[] decodeDownloadUsers(String p_str, Host p_h)
            throws Exception
    {
        if (p_str != null && !"".equals(p_str.trim()))
        {
            String usernames = StringUtil.decryptString(p_str);
            String[] uns = usernames.split(",");

            User[] us = new User[uns.length];
            for (int i = 0; i < us.length; i++)
            {
                us[i] = readUser(p_h.getFullName(), uns[i]);
            }

            return us;
        }
        else
        {
            return new User[]
            {};
        }
    }

    private static String decodeCompanyName(String p_str)
    {
        if (p_str != null && !"".equals(p_str.trim()))
        {
            String cormName = StringUtil.decryptString(p_str);
            if (cormName.indexOf(corm) != 0)
            {
                return "";
            }
            else
            {
                String name = cormName.substring(corm.length());
                return name;
            }
        }
        else
        {
            return "";
        }
    }

    private static String encodeDownloadUsers(User p_u)
    {
        if (p_u != null && p_u.getDownloadUsers() != null)
        {
            User[] us = p_u.getDownloadUsers();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < us.length; i++)
            {
                User user = us[i];
                sb.append(user.getName()).append(",");
            }
            if (sb.length() > 1)
                sb.deleteCharAt(sb.length() - 1);
            String encoded = StringUtil.encryptString(sb.toString());
            return encoded;
        }
        else
        {
            return null;
        }
    }

    private static String encodeCompanyName(User p_u)
    {
        if (p_u != null)
        {
            String cormName = corm + p_u.getCompanyName();
            String encoded = StringUtil.encryptString(cormName);
            return encoded;
        }
        else
        {
            return null;
        }
    }

    private static Host elementToHost(Element p_hostElement)
    {
        if (p_hostElement == null || !p_hostElement.getName().equals(e_host))
        {
            return null;
        }

        String hoststr = p_hostElement.attributeValue(a_hosturl);
        int index = hoststr.indexOf(Host.separator);
        String name = hoststr.substring(0, index);
        String port = hoststr.substring(index + 1);

        Host h = new Host(name, port);

        return h;
    }

    private static Job elementToJob(Element p_jobElement) throws Exception
    {
        if (p_jobElement == null || !p_jobElement.getName().equals(e_job))
        {
            return null;
        }

        String username = p_jobElement.getParent().getParent()
                .attributeValue(a_username);
        String hoststr = p_jobElement.getParent().getParent().getParent()
                .attributeValue(a_hosturl);
        User owner = readUser(hoststr, username);
        List fms = new ArrayList();
        Iterator it = p_jobElement.elementIterator(e_file);
        while (it.hasNext())
        {
            Element fileE = (Element) it.next();
            File f = new File(fileE.getText());
            FileProfile fp = new FileProfile(fileE.attributeValue(a_fpid),
                    fileE.attributeValue(a_fpname));
            FileMapped fm = new FileMapped(f, fp);
            fms.add(fm);
        }

        Job j = new Job(p_jobElement.element(e_jobname).getText(), owner, fms,
                parseDate(p_jobElement.attributeValue(a_createtime)),
                parseDate(p_jobElement.attributeValue(a_downtime)));
        j.setDownloadUser(p_jobElement.element(e_downloader).getText());
        j.setCVSJob(new Boolean(p_jobElement.attribute(a_cvsjob).getText())
                .booleanValue());
        j.setSourceLocale(p_jobElement.attribute(a_cvssourcelocale) == null ? ""
                : p_jobElement.attribute(a_cvssourcelocale).getText());
        return j;
    }

    private static User readUser(String p_hoststr, String p_username)
            throws Exception
    {
        String path = "//" + e_host + "[@" + a_hosturl + "='" + p_hoststr
                + "']/" + e_userSession + "[@" + a_username + "='" + p_username
                + "']";
        Element user = getElement(path);

        if (user == null)
        {
            return null;
        }

        User u = elementToUser(user, false);

        return u;
    }

    private static void writeRuncount(int p_c) throws Exception
    {
        String path = "//" + e_runcount;
        Element runcount = getElement(path);
        runcount.setText(String.valueOf(p_c));
        Document doc = runcount.getDocument();

        saveConfigureXML(doc);
    }

    private static void modifyPrefJobSplitting(String text) throws Exception
    {
        String path = "//" + e_pref_jobSplitting;
        Element elem = getElement(path);
        if (elem == null)
        {
            String pathPref = "//" + e_pref;
            Element elemPref = getElement(pathPref);
            if (elemPref == null)
            {
                String pathRoot = "//" + e_root;
                elemPref = getElement(pathRoot).addElement(e_pref);
            }
            elem = elemPref.addElement(e_pref_jobSplitting);
        }
        elem.setText(text);
        Document doc = elem.getDocument();
        saveConfigureXML(doc);
    }

    private static void modifyPrefJobPriority(String text) throws Exception
    {
        String path = "//" + e_pref_jobPriority;
        Element elem = getElement(path);
        if (elem == null)
        {
            String pathPref = "//" + e_pref;
            Element elemPref = getElement(pathPref);
            if (elemPref == null)
            {
                String pathRoot = "//" + e_root;
                elemPref = getElement(pathRoot).addElement(e_pref);
            }
            elem = elemPref.addElement(e_pref_jobPriority);
        }
        elem.setText(text);
        Document doc = elem.getDocument();
        saveConfigureXML(doc);
    }

    private static void saveConfigureXML(Document p_doc) throws Exception
    {
        File configFile = FileUtils.createFile(Constants.CONFIGURE_XML);
        writeFormattedXML(p_doc, configFile, true, false);
    }

    private static Element readJobElement(Job p_job) throws Exception
    {
        User user = p_job.getOwner();
        String jobName = p_job.getName();

        Element ue = readUserElement(user);
        String path = ue.getUniquePath() + "//" + e_job + "/" + e_jobname
                + "[text()='" + jobName + "']";
        Element name = getElement(path);
        Element job = name.getParent();
        return job;
    }

    private static Element readUserElement(User p_user) throws Exception
    {
        String path = "//" + e_host + "[@" + a_hosturl + "='"
                + p_user.getHost().getFullName() + "']/" + e_userSession + "[@"
                + a_username + "='" + p_user.getName() + "']";
        Element u = getElement(path);
        return u;
    }

    private static void writeFormattedXML(Document p_document, File p_file,
            boolean p_pretty, boolean p_compact) throws Exception
    {
        XMLWriter writer = null;
        BufferedOutputStream out = new BufferedOutputStream(
                new FileOutputStream(p_file));
        if (p_pretty)
        {
            OutputFormat format = OutputFormat.createPrettyPrint();
            writer = new XMLWriter(out, format);
        }
        else if (p_compact)
        {
            OutputFormat format = OutputFormat.createCompactFormat();
            writer = new XMLWriter(out, format);
        }
        else
        {
            writer = new XMLWriter(out);
        }
        writer.write(p_document);
        writer.flush();
        writer.close();

        if (out != null)
        {
            out.close();
        }
    }

    private static Document readConfigureFile()
    {
        File file = new File(Constants.CONFIGURE_XML);
        if (!file.exists())
        {
            return null;
        }

        long time = file.lastModified();
        if (m_doc == null || time != m_lasttime)
        {
            SAXReader r = new SAXReader();
            try
            {
                BufferedInputStream in = new BufferedInputStream(
                        new FileInputStream(file));
                InputSource is = new InputSource(in);
                is.setEncoding("UTF-8");
                m_doc = r.read(is);
                m_lasttime = time;
            }
            catch (Exception e)
            {
                log.error(
                        "can not read configure file " + file.getAbsolutePath(),
                        e);
                m_doc = null;
            }
        }

        return m_doc;
    }

    private static Document toV2(Document p_oldDoc) throws Exception
    {
        String hostname = p_oldDoc.selectSingleNode("//hostname").getText();
        String port = p_oldDoc.selectSingleNode("//port").getText();
        String username = p_oldDoc.selectSingleNode("//username").getText();
        String password = p_oldDoc.selectSingleNode("//password").getText();
        String dir = p_oldDoc.selectSingleNode("//dir").getText();
        String minute = p_oldDoc.selectSingleNode("//minute").getText();
        String time = p_oldDoc.selectSingleNode("//time").getText();
        String jobs = p_oldDoc.selectSingleNode("//jobs").getText();
        String backupjobs = p_oldDoc.selectSingleNode("//backupjobs").getText();

        Date d = new Date();
        Element root = DocumentHelper.createElement(e_root);
        root.addAttribute(a_version, Constants.APP_VERSION_STRING);
        root.addElement(e_runcount).setText(time);

        Element host = root.addElement(e_host);
        Host h = new Host(hostname, port);
        host.addAttribute(a_hosturl, h.getFullName());

        Element userSession = host.addElement(e_userSession);
        userSession.addAttribute(a_username, username);
        userSession.addAttribute(a_isdefault, v_true);
        userSession.addAttribute(a_autodown, v_true);
        userSession.addElement(e_usercorp).setText("");
        userSession.addElement(e_downlist).setText(
                StringUtil.encryptString(username));
        userSession.addElement(e_userpwd).setText(
                StringUtil.encryptString(password));
        userSession.addElement(e_savepath).setText(dir);
        userSession.addElement(e_minutes).setText(minute);

        String date = formatDate(d);
        Element jobsElement = userSession.addElement(e_jobs);
        String[] j = jobs.split(",");
        for (int i = 0; i < j.length; i++)
        {
            addJobElement(date, v_na, jobsElement, j[i], v_na);
        }
        j = null;
        j = backupjobs.split(",");
        for (int i = 0; i < j.length; i++)
        {
            addJobElement(date, date, jobsElement, j[i], username);
        }

        Document doc = DocumentHelper.createDocument();
        doc.setRootElement(root);

        return doc;
    }

    private static void addJobElement(String p_cdate, String p_downDate,
            Element p_jobs, String p_jobName, String p_username)
    {
        if ("".equals(p_jobName.trim()))
        {
            return;
        }
        Element job;
        job = p_jobs.addElement(e_job);
        job.addElement(e_jobname).setText(p_jobName);
        job.addElement(e_downloader).setText(p_username);
        Element file = job.addElement(e_file);
        file.addAttribute(a_fpid, v_na);
        file.addAttribute(a_fpname, v_na);
        file.setText(v_na);
        job.addAttribute(a_createtime, p_cdate);
        job.addAttribute(a_downtime, p_downDate);
        job.addAttribute(a_display, v_true);
    }

    private static String formatDate(Date p_d)
    {
        return df.format(p_d);
    }

    public static Date parseDate(String p_str)
    {
        try
        {
            return df.parse(p_str);
        }
        catch (ParseException e)
        {
            return null;
        }

    }

    private static Element getElement(String path) throws Exception
    {
        Document doc = readConfigureFile();
        if (doc == null)
        {
            throw new Exception("Can not read document from "
                    + Constants.CONFIGURE_XML);
        }
        Node n = doc.selectSingleNode(path);
        if (n == null)
        {
            return null;
        }
        else if (n.getNodeType() != Node.ELEMENT_NODE)
        {
            throw new Exception("Node \"" + path + "\" is not an element");
        }
        return (Element) n;
    }

    private static List getElementsList(String path) throws Exception
    {
        Document doc = readConfigureFile();
        if (doc == null)
        {
            throw new Exception("Can not read document from "
                    + Constants.CONFIGURE_XML);
        }
        List ns = doc.selectNodes(path);
        if (ns == null || ns.isEmpty())
        {
            return new ArrayList();
        }
        else
        {
            List r = new ArrayList();
            for (Iterator iter = ns.iterator(); iter.hasNext();)
            {
                Node n = (Node) iter.next();
                if (n.getNodeType() == Node.ELEMENT_NODE)
                {
                    r.add(n);
                }
            }
            return r;
        }
    }

    // //////////////////////////////////////////////
    // test methods
    // //////////////////////////////////////////////

    public static void main(String[] args)
    {
        try
        {
            // DownloadAction d = new DownloadAction();
            // d.execute(new String[] {});

            testWriteHostUser();

            testWriteJobs();

            testRead();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private static void testWriteHostUser() throws Exception
    {
        System.out.println("-----------------test write host users");
        Host h1 = new Host("host1", 80);
        writeHost(h1);
        writeHost(h1);
        h1 = new Host("host1", 180);
        writeHost(h1);
        Host h2 = new Host("host2", 80);
        writeHost(h2);
        Document doc = readConfigureFile();
        System.out.println(doc.asXML());

        User u1 = new User("name1", "pwd", h1, "d:\\1", "101", true);
        writeUser(u1, true);
        writeUser(u1, true);
        u1 = new User("name1", "pwd11", h1, "d:\\1", "101", false);
        writeUser(u1, true);
        doc = readConfigureFile();
        System.out.println(doc.asXML());

        User u2 = new User("name2", "pwd", h2, "d:\\2", "102", true);
        writeUser(u2, true);
        doc = readConfigureFile();
        System.out.println(doc.asXML());

    }

    private static void testWriteJobs() throws Exception
    {
        System.out.println("-----------------test write jobs");
        User u = readDefaultUser();
        Job job;
        List files = new ArrayList();
        FileMapped f1 = new FileMapped(new File("d:\\1.txt"), new FileProfile(
                "1", "fp_1"));
        FileMapped f2 = new FileMapped(new File("d:\\2.txt"), new FileProfile(
                "2", "fp_2"));
        FileMapped f3 = new FileMapped(new File("d:\\3.txt"), new FileProfile(
                "3", "fp_3"));

        files.add(f1);
        job = new Job("job1", u, files, new Date(), null);
        writeNewJob(job);
        Document doc = readConfigureFile();
        System.out.println(doc.asXML());

        files.add(f2);
        job = new Job("job2", u, files, new Date(), null);
        writeNewJob(job);
        doc = readConfigureFile();
        System.out.println(doc.asXML());

        files.add(f3);
        job = new Job("job3", u, files, new Date(), null);
        writeNewJob(job);
        doc = readConfigureFile();
        System.out.println(doc.asXML());

        writeDownloadedJob(job);
        doc = readConfigureFile();
        System.out.println(doc.asXML());
    }

    private static void testRead() throws Exception
    {
        System.out.println("-----------------test read");
        Document doc = readConfigureFile();
        String xml = doc.asXML();
        System.out.println(xml);

        System.out.println(readDefaultUser());

        List l;
        l = readJobs();
        System.out.println(UsefulTools.listToString(l));
        l = readJobsToDownload();
        System.out.println(UsefulTools.listToString(l));
        System.out.println(readRuncount());
    }
}
