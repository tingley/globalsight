/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.everest.webapp.pagehandler.rss;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.SortUtil;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndImage;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class RSSUtil
{
    private static final Logger s_logger = Logger.getLogger(RSSUtil.class
            .getName());

    public static Feed getFeedByURL(String rssUrl)
    {
        Feed feed = null;

        URLConnection url = null;
        XmlReader reader = null;
        SyndFeed syndFeed = null;
        SyndFeedInput input = new SyndFeedInput();
        try
        {
            // url = new URL(rssUrl);
            url = new URL(rssUrl).openConnection();

            reader = new XmlReader(url);
            syndFeed = input.build(reader);

            String encoding = reader.getEncoding();
            String channelTitle = syndFeed.getTitle();
            String channelLink = syndFeed.getLink();
            String channelDescription = syndFeed.getDescription();
            String channelLanguage = syndFeed.getLanguage();
            String channelCopyRight = syndFeed.getCopyright();

            SyndImage si = syndFeed.getImage();
            String imageTitle = "", imageLink = "", imageUrl = "";
            if (si != null)
            {
                imageTitle = si.getTitle();
                imageLink = si.getLink();
                imageUrl = si.getUrl();
            }

            boolean isDefault = true;
            String companyId = CompanyThreadLocal.getInstance().getValue();

            feed = new Feed(rssUrl, encoding, "2.0", channelTitle, channelLink,
                    channelDescription, channelLanguage, channelCopyRight,
                    imageTitle, imageLink, imageUrl, isDefault, companyId);

            List entries = syndFeed.getEntries();
            Date date = null;
            Calendar cal = null;
            if (entries != null && entries.size() > 0)
            {
                for (int i = 0; i < entries.size(); i++)
                {
                    SyndEntry entry = (SyndEntry) entries.get(i);

                    Item item = new Item();
                    item.setTitle(entry.getTitle());
                    item.setLink(entry.getLink());
                    SyndContent sc = entry.getDescription();
                    String desc = "";
                    if (sc != null)
                    {
                        desc = sc.getValue();
                    }
                    // Remove additional info for BS
                    if (desc.indexOf("Permanent link to this entry") > -1)
                    {
                        desc = desc.substring(0, desc.lastIndexOf("<p>"));
                    }
                    item.setDescription(desc);
                    date = entry.getPublishedDate();
                    if (date != null)
                    {
                        item.setPubDate(date.toString());
                        cal = Calendar.getInstance();
                        cal.clear();
                        cal.setTime(date);
                        item.setPublishedDate(cal);
                    }
                    item.setAuthor(entry.getAuthor());
                    item.setFeed(feed);

                    feed.addItem(item);
                }
            }
        }
        catch (MalformedURLException e)
        {
            s_logger.error("The RSS url is invalid!", e);
        }
        catch (IOException e)
        {
            s_logger.error("Can't create XmlReader for RSS url : " + rssUrl, e);
        }
        catch (FeedException e)
        {
            s_logger.error(
                    "Faild to create 'syndFeed' for RSS url : " + rssUrl, e);
        }

        return feed;
    }

    public static ArrayList<Item> refreshFeedByURL(Feed p_feed)
    {
        ArrayList<Item> items = new ArrayList<Item>();

        URL url = null;
        XmlReader reader = null;
        SyndFeed syndFeed = null;
        SyndFeedInput input = new SyndFeedInput();
        try
        {
            url = new URL(p_feed.getRssUrl());

            reader = new XmlReader(url);
            syndFeed = input.build(reader);

            String companyId = CompanyThreadLocal.getInstance().getValue();
            Calendar maxCalendar = getMaxPublishDate(p_feed);

            List entries = syndFeed.getEntries();
            Date date = null;
            Calendar cal = null;
            if (entries != null && entries.size() > 0)
            {
                for (int i = 0; i < entries.size(); i++)
                {
                    SyndEntry entry = (SyndEntry) entries.get(i);
                    date = entry.getPublishedDate();
                    cal = Calendar.getInstance();
                    cal.clear();
                    cal.setTime(date);
                    if (cal.after(maxCalendar))
                    {
                        Item item = new Item();
                        item.setTitle(entry.getTitle());
                        item.setLink(entry.getLink());
                        SyndContent sc = entry.getDescription();
                        item.setDescription(sc == null ? "" : sc.getValue());
                        item.setPubDate(date.toString());
                        item.setPublishedDate(cal);
                        item.setAuthor(entry.getAuthor());
                        item.setFeed(p_feed);

                        items.add(item);
                    }
                }
            }
        }
        catch (MalformedURLException e)
        {
            s_logger.error("The RSS url is invalid!", e);
        }
        catch (IOException e)
        {
            s_logger.error(
                    "Can't create XmlReader for RSS url : "
                            + p_feed.getRssUrl(), e);
        }
        catch (FeedException e)
        {
            s_logger.error(
                    "Faild to create 'syndFeed' for RSS url : "
                            + p_feed.getRssUrl(), e);
        }

        return items;
    }

    public static String saveToFile(String p_folder, Item p_item)
    {
        if (p_folder == null || p_folder.equals("") || p_item == null)
            return null;

        String folderName = p_folder;
        try
        {
            folderName += File.separator
                    + convertTo(p_item.getFeed().getChannelTitle())
                    + File.separator;
            File f = new File(folderName);
            if (!f.exists() || f.isFile())
            {
                f.mkdirs();
            }
            String filename = folderName + getDateString() + "_"
                    + convertTo(p_item.getTitle()) + ".html";

            File file = new File(filename);
            if (file.exists())
            {
                s_logger.error("Error::The file [ " + filename
                        + " ]has been exist");
                return null;
            }
            FileOutputStream fos = new FileOutputStream(file);
            Writer out = new OutputStreamWriter(fos, "utf-8");
            out.write(generateContent(p_item).toString());
            out.close();
            fos.close();

            p_item.setStatus(1);
            HibernateUtil.update(p_item);

            return filename;
        }
        catch (Exception e)
        {
            s_logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static String getStorageRoot()
    {
        SystemConfiguration sc = SystemConfiguration.getInstance();

        String root = sc.getStringParameter("fileStorage.dir");

        if (!(root.endsWith("/") || root.endsWith("\\")))
        {
            root = root + "/";
        }

        root += "RSS Docs" + File.separator;
        try
        {
            File f = new File(root);
            if (!f.exists() || f.isFile())
            {
                f.mkdirs();
            }
            return root;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static int copyFiles(String p_source, String p_target)
    {
        File sourceFile = null, targetFile = null;
        BufferedOutputStream fout = null;
        BufferedInputStream fin = null;
        try
        {
            sourceFile = new File(p_source);
            if (!sourceFile.exists())
                return -1;
            targetFile = new File(p_target);
            if (!targetFile.exists())
                targetFile.getParentFile().mkdirs();
            fout = new BufferedOutputStream(new FileOutputStream(targetFile));
            fin = new BufferedInputStream(new FileInputStream(sourceFile));
            byte[] buf = new byte[4096];
            int count = 0;
            while ((count = fin.read(buf)) >= 0)
            {
                fout.write(buf, 0, count);
            }
            return 0;
        }
        catch (Exception e)
        {
            return -1;
        }
        finally
        {
            try
            {
                fin.close();
                fout.close();
            }
            catch (IOException ioe)
            {
            }
        }
    }

    /**
     * Generate a standard HTML format file Because the content got from RSS xml
     * is part of HTML file, then need to add some tags to make it as a standard
     * HTML format
     * 
     * @param p_item
     *            Content of item
     * @return
     */
    private static StringBuilder generateContent(Item p_item)
    {
        if (p_item == null)
            return new StringBuilder();

        StringBuilder sb = new StringBuilder(1024);
        sb.append("<html>\n<head>\n<META HTTP-EQUIV=\"content-type\" CONTENT=\"text/html;charset=UTF-8\">\n<title>");
        sb.append(p_item.getTitle() + "</title>\n</head>\n<body>");
        sb.append(p_item.getDescription());
        sb.append("</body>\n</html>");
        return sb;
    }

    private static String getDateString()
    {
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_hhmmss");
            return sdf.format(Calendar.getInstance().getTime());
        }
        catch (Exception e)
        {
            return "";
        }
    }

    private static Calendar getMaxPublishDate(Feed p_feed)
    {
        Set items = p_feed.getItems();
        Object[] itemsArray = items.toArray();
        List itemsList = Arrays.asList(itemsArray);
        SortUtil.sort(itemsList, new RSSItemComparator());
        return ((Item) itemsList.get(0)).getPublishedDate();
    }

    private static String getUploadPath(String p_jobName,
            String p_sourceLocale, Date p_uploadDate)
    {
        // format the time with server's default time zone
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmm");

        StringBuffer sb = new StringBuffer();
        // sb.append(getCXEBaseDir());
        sb.append(AmbFileStoragePathUtils.getCxeDocDir());
        sb.append(File.separator);
        sb.append(p_sourceLocale);
        sb.append(File.separator);
        sb.append(sdf.format(p_uploadDate));
        sb.append("_");
        sb.append(p_jobName);
        return sb.toString();
    }

    /**
     * To save the note information of job
     * 
     * @param p_newJobName
     * @param p_uploadDateInLong
     * @param p_notes
     * @param p_user
     */
    private static void saveJobNote(String p_newJobName,
            long p_uploadDateInLong, String p_notes, User p_user)
    {
        // save job note into <docs_folder>\<company_name>\<newJobName>.txt
        // read it in
        // com.globalsight.everest.jobhandler.jobcreation.JobAdditionEngine.createNewJob()
        // the content format is <userName>,<Date long type>,<note>
        try
        {
            String jobNote = p_notes;
            if (jobNote != null && jobNote.trim().length() != 0)
            {
                File jobnotesFile = new File(
                        AmbFileStoragePathUtils.getCxeDocDir(), p_newJobName
                                + ".txt");
                if (jobnotesFile.exists())
                {
                    jobnotesFile.delete();
                }
                if (jobnotesFile.createNewFile())
                {
                    char token = ',';
                    User user = p_user;
                    // save name for security
                    StringBuffer sb = new StringBuffer(user.getUserName());
                    sb.append(token);
                    sb.append(p_uploadDateInLong);
                    sb.append(token);
                    sb.append(jobNote);
                    FileUtils.write(jobnotesFile, sb.toString(), "utf-8");
                }
            }
        }
        catch (Exception e)
        {
            // do nothing but write log, because this exception
            // is not important
            s_logger.info(
                    "Error when save "
                            + p_newJobName
                            + " job's notes (added when uploading) into GlobalSight File System",
                    e);
        }
    }

    public static String getBaseDocRoot()
    {
        SystemConfiguration sc = SystemConfiguration.getInstance();

        String root = sc.getStringParameter("cxe.docsDir");

        if (!(root.endsWith("/") || root.endsWith("\\")))
        {
            root = root + "/";
        }
        return root;
    }

    /**
     * Get all files in sub-folder
     * 
     * @param p_file
     * @return
     */
    private static ArrayList<File> getSubFiles(File p_file)
    {
        ArrayList<File> files = new ArrayList<File>();
        if (p_file == null)
            return files;
        File[] sfile = p_file.listFiles();
        for (int i = 0; i < sfile.length; i++)
        {
            if (sfile[i].isDirectory())
            {
                files.addAll(getSubFiles(sfile[i]));
            }
            else
                files.add(sfile[i]);
        }
        return files;
    }

    /**
     * Save basic job info and copy file(s) from storage to document folder.
     * 
     * @param p_jobName
     * @param p_srcLocale
     * @param p_projectId
     * @param p_projectName
     * @param p_notes
     * @param p_files
     * @param p_user
     * @return
     * @throws EnvoyServletException
     * @throws IOException
     */
    public static ArrayList<String> saveData(String p_jobName,
            String p_srcLocale, String p_projectId, String p_projectName,
            String p_notes, String p_itemId, User p_user)
            throws EnvoyServletException, IOException
    {
        ArrayList returnValue = new ArrayList();
        ArrayList<String> results = new ArrayList<String>();
        HashSet<String> results1 = new HashSet<String>();
        long uploadDateInLong = System.currentTimeMillis();
        Date uploadDate = new Date(uploadDateInLong);
        String uploadPath = getUploadPath(p_jobName, p_srcLocale, uploadDate);
        returnValue.add(uploadPath.substring(getBaseDocRoot().length()));
        try
        {
            RSSPersistenceManager rssManager = ServerProxy
                    .getRSSPersistenceManager();
            Item item = rssManager.getItem(Long.parseLong(p_itemId));
            String toPath = saveToFile(uploadPath, item);
            results.add(toPath);
            results1.add(toPath.substring(getBaseDocRoot().length()));

            // now update the job name to include the timestamp
            String newJobName = uploadPath.substring(
                    uploadPath.lastIndexOf(File.separator) + 1,
                    uploadPath.length());

            saveJobNote(newJobName, uploadDateInLong, p_notes, p_user);

            returnValue.add(results);
            returnValue.add(results1);
            return returnValue;
        }
        catch (Exception ex)
        {
            throw new EnvoyServletException(ex);
        }
    }

    private static String convertTo(String p_str)
    {
        char[] chars = p_str.toCharArray();
        StringBuilder s = new StringBuilder();

        for (char c : chars)
        {
            if ((c <= '/' && c != ' ') || (c >= ':' && c <= '@')
                    || (c >= '[' && c <= '`') || (c >= '{' && c <= '~'))
                continue;
            s.append(c);
        }
        return s.toString();
    }

    public static ArrayList<String> getTranslatedItems(Feed p_feed)
    {
        ArrayList<String> items = new ArrayList<String>();
        if (p_feed == null)
            return null;
        Set curItems = p_feed.getItems();
        String tmp = "";
        Object[] allItems = curItems.toArray();
        Item item = null;
        for (Object obj : allItems)
        {
            item = (Item) obj;
            if (item.getStatus() > 0)
            {
                tmp = item.getTitle() + "_" + item.getPubDate();
                items.add(tmp);
            }
        }
        return items;
    }

    public static void refreshFeed(Feed p_feed,
            ArrayList<String> translatedItems)
    {
        Set items = p_feed.getItems();
        Object[] allItems = (Object[]) items.toArray();
        Item item = null;
        for (Object obj : allItems)
        {
            item = (Item) obj;
            if (translatedItems.contains(item.getTitle() + "_"
                    + item.getPubDate()))
                item.setStatus(1);
        }
    }

    public static String convertToHtml(String str)
    {
        if (str == null || str.equals(""))
            return "";
        StringBuilder sb = new StringBuilder();
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++)
        {
            if (chars[i] == '\"')
                sb.append("&quot;");
            else
                sb.append(chars[i]);
        }
        return sb.toString();
    }

}
