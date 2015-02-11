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
package com.globalsight.util.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

/**
 * This ZipIt class is responsible for performing some of the zipping processes
 * such as creation of a zip file and adding files to it, and unzipping a zip
 * file and extracting the files.
 * 
 * Note that when a problem comes along, this class must zip it! Zip it good!
 * (from Austin Powers)
 */
public class ZipIt implements Serializable
{
    private static final long serialVersionUID = 6900154618478625442L;
    public static final int BUFSIZE = 4096;
    public static final String DESKTOP_FOLDER = "webservice";
    private static int SPLIT_SIZE = 2 * 1000 * 1024; // 2M

    /**
     * This process will add a number of files to a zip file. The zip file does
     * not need to exist already.
     * 
     * @param p_zipFile
     *            the zip file to be created.
     * @param entryFiles
     *            the files that will be zipped (added to p_zipFile). The files
     *            will have a path relative to the zip's path.
     * 
     * @return The zip file with all of the entries added to it.
     * 
     * @throws Exception
     *             If any IOException happens during the zipping process.
     */
    public static void addEntriesToZipFile(File p_zipFile, File[] entryFiles)
            throws Exception
    {
        boolean donotIncludePath = false;
        String comment = "";

        addEntriesToZipFile(p_zipFile, entryFiles, donotIncludePath, comment);
    }

    /**
     * This process will add a number of files to a zip file. The zip file does
     * not need to exist already.
     * 
     * @param p_zipFile
     *            the zip file to be created.
     * @param entryFiles
     *            the files that will be zipped (added to p_zipFile). The files
     *            will have a path relative to the zip's path.
     * @param donotIncludePath
     *            ZipEntry will not include the source file path if true
     * @param comment
     *            zip file comment
     * @return The zip file with all of the entries added to it.
     * 
     * @throws Exception
     *             If any IOException happens during the zipping process.
     */
    public static void addEntriesToZipFile(File p_zipFile, File[] entryFiles,
            boolean donotIncludePath, String comment)
            throws FileNotFoundException, IOException
    {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(
                p_zipFile));
        zos.setComment(comment);

        // read bytes of individual files to zip
        ZipEntry[] zipEntries = new ZipEntry[entryFiles.length];
        String fileName = null;

        for (int i = 0; i < entryFiles.length; i++)
        {
            DataInputStream dis = new DataInputStream(new FileInputStream(
                    entryFiles[i]));

            long length = entryFiles[i].length();
            // create new zip entry
            fileName = (donotIncludePath) ? entryFiles[i].getName()
                    : entryFiles[i].getPath();
            int index = fileName.indexOf(File.separator);
            if (index > 0)
            {
                fileName = fileName.substring(index + 1);
            }
            zipEntries[i] = new ZipEntry(fileName);

            // put the entry into zip file
            zos.putNextEntry(zipEntries[i]);

            int chunks = (int) length / SPLIT_SIZE;
            for (int m = 0; m < chunks; m++)
            {
                byte[] arr = new byte[SPLIT_SIZE];
                dis.readFully(arr);
                // write bytes of zip entry
                zos.write(arr);
            }
            if ((int) length % SPLIT_SIZE > 0)
            {
                byte[] arr = new byte[(int) length % SPLIT_SIZE];
                dis.readFully(arr);
                // write bytes of zip entry
                zos.write(arr);

            }
            dis.close();
        }
        // close zos
        zos.close();
    }

    /**
     * Unpack the zip file and extract the files to a path relative to the zip
     * file's path.
     * 
     * @param p_zipFileName
     *            The name of the zip file.
     * 
     * @return ArrayList of String for the entry names in the zip file
     * @throws Exception
     *             If any IOException happens during the unzipping process.
     */
    public static ArrayList unpackZipPackage(String p_zipFileName)
            throws Exception
    {
        String zipdir = getZipDir(p_zipFileName);
        File zipDirFile = new File(zipdir);
        zipDirFile.mkdir();
        ZipFile zipfile = new ZipFile(p_zipFileName);

        Enumeration entries = zipfile.getEntries();
        ArrayList files = new ArrayList();
        while (entries.hasMoreElements())
        {
            // read zip entry
            ZipEntry entry = (ZipEntry) entries.nextElement();
            File outfile = new File(zipdir + File.separator
                    + entry.getName().replace('\\', File.separatorChar));
            if (entry.isDirectory())
            {
                outfile.mkdirs();
                continue;
            }
            else
            {
                if (!outfile.getParentFile().exists())
                    outfile.getParentFile().mkdirs();
            }

            BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream(outfile));

            byte[] buf = new byte[BUFSIZE];
            BufferedInputStream in = new BufferedInputStream(zipfile
                    .getInputStream(entry));
            int readLen = 0;
            while ((readLen = in.read(buf, 0, BUFSIZE)) != -1)
            {
                // write file
                try
                {
                    out.write(buf, 0, readLen);
                }
                catch (FileNotFoundException e)
                {
                    out.close();
                    cleanUp(zipfile, zipDirFile, zipdir);
                    throw e;
                }
                catch (IOException ioe)
                {
                    out.close();
                    cleanUp(zipfile, zipDirFile, zipdir);
                    throw ioe;
                }
            }
            out.close();
            in.close();

            String file = entry.getName();
            file = file.replace('\\', File.separatorChar);
            files.add(file);
        }

        try
        {
            zipfile.close();
        }
        catch (Exception e)
        {
            // if this fails, the zip file won't get deleted.
        }

        return files;
    }

    public static ArrayList unpackZipPackageForDownloadExport(
            String p_zipFileName, Map<String, String> lastModifiedTimes,
            String jobName, String locale) throws Exception
    {
        String zipdir = getZipDir(p_zipFileName);
        File zipDirFile = new File(zipdir);
        zipDirFile.mkdir();
        ZipFile zipfile = new ZipFile(p_zipFileName);
        Map<String, String> changedEntryName = changeEntryName(zipfile, jobName);
        Enumeration entries = zipfile.getEntries();
        ArrayList files = new ArrayList();
        while (entries.hasMoreElements())
        {
            // read zip entry
            ZipEntry entry = (ZipEntry) entries.nextElement();

            String entryName = getEntryName(entry);
            String tailer = changedEntryName.get(entryName);
            File outfile = new File(zipdir + File.separator + tailer);

            if (entry.isDirectory())
            {
                outfile.mkdirs();
                continue;
            }
            else
            {
                if (!outfile.getParentFile().exists())
                    outfile.getParentFile().mkdirs();
            }

            BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream(outfile));

            byte[] buf = new byte[BUFSIZE];
            BufferedInputStream in = new BufferedInputStream(zipfile
                    .getInputStream(entry));
            int readLen = 0;
            while ((readLen = in.read(buf, 0, BUFSIZE)) != -1)
            {
                // write file
                try
                {
                    out.write(buf, 0, readLen);
                }
                catch (FileNotFoundException e)
                {
                    out.close();
                    cleanUp(zipfile, zipDirFile, zipdir);
                    throw e;
                }
                catch (IOException ioe)
                {
                    out.close();
                    cleanUp(zipfile, zipDirFile, zipdir);
                    throw ioe;
                }
            }
            out.close();
            in.close();
            String lastModifiedTime = lastModifiedTimes.get(outfile.getName());
            if (lastModifiedTime != null && !lastModifiedTime.equals(""))
            {
                outfile.setLastModified(Long.parseLong(lastModifiedTime));
            }

            files.add(tailer);
        }

        try
        {
            zipfile.close();
        }
        catch (Exception e)
        {
            // if this fails, the zip file won't get deleted.
        }

        return files;
    }

    private static ArrayList<String> getEntryNames(ZipFile zipfile)
    {
        Enumeration entries = zipfile.getEntries();
        ArrayList<String> entryNames = new ArrayList<String>();
        while (entries.hasMoreElements())
        {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            entryNames.add(getEntryName(entry));
        }

        return entryNames;
    }

    private static String getEntryName(ZipEntry entry)
    {
        String entryName = entry.getName();
        if (entryName.trim().startsWith("/"))
        {
            entryName = entryName.trim().substring(1);
        }

        entryName = entryName.replace('\\', '/');

        return entryName;
    }

    private static Map<String, String> changeEntryName(ZipFile zipfile,
            String jobName)
    {
        Map<String, String> entryNamesMap = new HashMap<String, String>();
        ArrayList<String> entryNames = getEntryNames(zipfile);
        ArrayList<String> changedNames = changeEntryNames(entryNames, jobName);

        for (int i = 0; i < changedNames.size(); i++)
        {
            entryNamesMap.put(entryNames.get(i), changedNames.get(i));
        }

        return entryNamesMap;
    }

    private static ArrayList<String> changeEntryNames(
            ArrayList<String> entryNames, String jobName)
    {
        ArrayList<String> locales = new ArrayList<String>();
        ArrayList<String> endParts = new ArrayList<String>();
        ArrayList<String> changedEntryNames = new ArrayList<String>();

        for (String entryName : entryNames)
        {
            String[] result = splitEntryName(entryName, jobName);
            locales.add(result[0]);
            endParts.add(result[1]);
        }

        String commonPath = getCommonPath(endParts);
        int index = commonPath.length();

        for (int i = 0; i < locales.size(); i++)
        {
            String str = jobName + "/" + locales.get(i) + "/" + endParts.get(i).substring(index);
            str = str.replace("//", "/");
        	changedEntryNames.add(str);
        }

        return changedEntryNames;
    }

    private static String[] splitEntryName(String entryName, String jobName)
    {
        String[] result = new String[2];

        String splitString = "/" + jobName + "/";
        int i = entryName.indexOf(splitString);
        String startPart = entryName.substring(0, i);
        String endPart = entryName.substring(i + splitString.length());

        String[] tokens = startPart.split("/");
        String locale = tokens[tokens.length - 1];
        if (DESKTOP_FOLDER.equals(locale))
        {
            locale = tokens[tokens.length - 2];
        }

        result[0] = locale;
        result[1] = endPart;

        return result;
    }

    public static Map<String, String> getEntryNamesMap(
            ArrayList<String> entryNames)
    {
        Map<String, String> entryNamesMap = new HashMap<String, String>();
        String commonPath = getCommonPath(entryNames);
        ArrayList<String> truncatedEntryNames = getTruncatedCommon(commonPath,
                entryNames);
        for (int i = 0; i < truncatedEntryNames.size(); i++)
        {
            entryNamesMap.put(entryNames.get(i), truncatedEntryNames.get(i));
        }
        return entryNamesMap;
    }

    private static String getCommonPath(ArrayList<String> entryNames)
    {
        ArrayList<String[]> tokens = new ArrayList<String[]>();
        int mixLength = Integer.MAX_VALUE;
        for (String entryName : entryNames)
        {
            String[] splitName = entryName.split("/");
            tokens.add(splitName);
            if (splitName.length < mixLength)
            {
                mixLength = splitName.length;
            }
        }

        StringBuilder commonPath = new StringBuilder();

        boolean isDifferent = false;
        for (int i = 0; i < mixLength - 1 && !isDifferent; i++)
        {
            String s = null;
            for (String[] token : tokens)
            {
                if (s == null)
                {
                    s = token[i];
                    continue;
                }

                if (!s.equals(token[i]))
                {
                    isDifferent = true;
                    break;
                }
            }
            //Added for GBS-1023 
            boolean _addCommonPath = true;  
            String temp = tokens.get(0)[i];
            for(int j=1;j<tokens.size();j++)
            {
            	if(!temp.equals(tokens.get(j)[i]))
            	{
            		_addCommonPath = false;
            		isDifferent = true;
            		break;
            	}
            }
            
            if(_addCommonPath)
            {
            	commonPath.append(tokens.get(0)[i]);
            	commonPath.append("/");
            }
        }

        return commonPath.toString();
    }

    private static ArrayList<String> getTruncatedCommon(String guessedCommon,
            ArrayList<String> entryNames)
    {
        if ("".equals(guessedCommon))
        {
            return entryNames;
        }

        ArrayList<String> result = new ArrayList<String>();
        for (String s : entryNames)
        {
            int index = s.indexOf(guessedCommon);
            if (index < 0)
            {
                index = 0;
            }
            result.add(s.substring(index + guessedCommon.length()));
        }

        return result;
    }

    /**
     * Unpack the zip file and extract the files to a path relative to the zip
     * file's path. Reset the files' last modified times.
     * 
     * @param p_zipFileName
     *            The name of the zip file.
     * 
     * @return ArrayList of String for the entry names in the zip file
     * @throws Exception
     *             If any IOException happens during the unzipping process.
     */
    public static ArrayList unpackZipPackage(String p_zipFileName,
            Map<String, String> lastModifiedTimes) throws Exception
    {
        String zipdir = getZipDir(p_zipFileName);
        File zipDirFile = new File(zipdir);
        zipDirFile.mkdir();
        ZipFile zipfile = new ZipFile(p_zipFileName);

        Enumeration entries = zipfile.getEntries();
        ArrayList files = new ArrayList();
        while (entries.hasMoreElements())
        {
            // read zip entry
            ZipEntry entry = (ZipEntry) entries.nextElement();
            File outfile = new File(zipdir + File.separator
                    + entry.getName().replace('\\', File.separatorChar));
            if (entry.isDirectory())
            {
                outfile.mkdirs();
                continue;
            }
            else
            {
                if (!outfile.getParentFile().exists())
                    outfile.getParentFile().mkdirs();
            }

            BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream(outfile));

            byte[] buf = new byte[BUFSIZE];
            BufferedInputStream in = new BufferedInputStream(zipfile
                    .getInputStream(entry));
            int readLen = 0;
            while ((readLen = in.read(buf, 0, BUFSIZE)) != -1)
            {
                // write file
                try
                {
                    out.write(buf, 0, readLen);
                }
                catch (FileNotFoundException e)
                {
                    out.close();
                    cleanUp(zipfile, zipDirFile, zipdir);
                    throw e;
                }
                catch (IOException ioe)
                {
                    out.close();
                    cleanUp(zipfile, zipDirFile, zipdir);
                    throw ioe;
                }
            }
            out.close();
            in.close();
            String lastModifiedTime = lastModifiedTimes.get(outfile.getName());
            if (lastModifiedTime != null && !lastModifiedTime.equals(""))
            {
                outfile.setLastModified(Long.parseLong(lastModifiedTime));
            }

            String file = entry.getName();
            file = file.replace('\\', File.separatorChar);
            files.add(file);
        }

        try
        {
            zipfile.close();
        }
        catch (Exception e)
        {
            // if this fails, the zip file won't get deleted.
        }

        return files;
    }

    /**
     * Unpack the zip file and extract the files to a path "String zipdir"
     * 
     */
    public static ArrayList unpackZipPackage(String p_zipFileName, String zipdir)
            throws Exception
    {
        File zipDirFile = new File(zipdir);
        zipDirFile.mkdirs();
        ZipFile zipfile = new ZipFile(p_zipFileName);

        Enumeration entries = zipfile.getEntries();
        ArrayList files = new ArrayList();
        while (entries.hasMoreElements())
        {
            // read zip entry
            ZipEntry entry = (ZipEntry) entries.nextElement();
            File outfile = new File(zipdir + File.separator
                    + entry.getName().replace('\\', File.separatorChar));
            if (entry.isDirectory())
            {
                outfile.mkdirs();
                continue;
            }
            else
            {
                if (!outfile.getParentFile().exists())
                    outfile.getParentFile().mkdirs();
            }

            BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream(outfile));

            byte[] buf = new byte[BUFSIZE];
            BufferedInputStream in = new BufferedInputStream(zipfile
                    .getInputStream(entry));
            int readLen = 0;
            while ((readLen = in.read(buf, 0, BUFSIZE)) != -1)
            {
                // write file
                try
                {
                    out.write(buf, 0, readLen);
                }
                catch (FileNotFoundException e)
                {
                    out.close();
                    cleanUp(zipfile, zipDirFile, zipdir);
                    throw e;
                }
                catch (IOException ioe)
                {
                    out.close();
                    cleanUp(zipfile, zipDirFile, zipdir);
                    throw ioe;
                }
            }
            out.close();
            in.close();
            files.add(entry.getName());
        }

        try
        {
            zipfile.close();
        }
        catch (Exception e)
        {
            // if this fails, the zip file won't get deleted.
        }

        return files;
    }

    /**
     * The zip directory is the parent directory of the zip file.
     */
    private static String getZipDir(String p_zipFileName) throws IOException
    {
        File file = new File(p_zipFileName);
        return file.getParent();
    }

    /**
     * Remove files that were unzipped.
     */
    private static void cleanUp(ZipFile zipfile, File zipDirFile, String zipdir)
    {
        Enumeration entries = zipfile.getEntries();
        while (entries.hasMoreElements())
        {
            // read zip entry
            ZipEntry entry = (ZipEntry) entries.nextElement();
            File outfile = new File(zipdir + File.separator + entry.getName());
            outfile.delete();
        }
        zipDirFile.delete();
    }
    
    static final int BUFFER = 8192;
    /**
     * Zip a directory, currently for OpenOffice converter
     * @param srcPathName
     * @param zipFile
     */
    public static void compress(String srcPathName, File zipFile)
    {
        File file = new File(srcPathName);
        if (!file.exists())
            throw new RuntimeException(srcPathName + "do not exists!");
        try
        {
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
            CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream, new CRC32());
            ZipOutputStream out = new ZipOutputStream(cos);
            String basedir = "";
            compress(file, out, basedir, false);
            out.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static void compress(File file, ZipOutputStream out, String basedir, boolean includeName)
    {
        if (file.isDirectory())
        {
            compressDirectory(file, out, basedir, includeName);
        }
        else
        {
            compressFile(file, out, basedir);
        }
    }

    private static void compressDirectory(File dir, ZipOutputStream out, String basedir, boolean includeName)
    {
        if (!dir.exists())
            return;

        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            compress(files[i], out, includeName ? basedir + dir.getName() + "/" : basedir, true);
        }
    }

    private static void compressFile(File file, ZipOutputStream out, String basedir)
    {
        if (!file.exists())
        {
            return;
        }
        try
        {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            ZipEntry entry = new ZipEntry(basedir + file.getName());
            out.putNextEntry(entry);
            int count;
            byte data[] = new byte[BUFFER];
            while ((count = bis.read(data, 0, BUFFER)) != -1)
            {
                out.write(data, 0, count);
            }
            bis.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
