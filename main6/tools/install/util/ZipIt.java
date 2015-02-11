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
package util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
    private static final int BUFFER = 8192;
    private static final String FILE_SEPARATOR = "_fileSeparator_";

    /**
     * This process will add a number of files to a zip file.
     * 
     * @param p_zipFile
     *            the ZIP file to be created.
     * @param entryFiles
     *            the files that will be zipped (added to p_zipFile). The files
     *            will have a path relative to the zip's path.
     * @param encodeEntryName
     *            if add files with encoded file name to avoid special character
     *            problem.
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void addEntriesToZipFile(File p_zipFile, File[] entryFiles,
            boolean encodeEntryName) throws FileNotFoundException, IOException
    {
        boolean donotIncludePath = false;
        String comment = "";

        if (encodeEntryName)
        {
            // Get copy files with encoded file name to avoid special character
            // problem.
            File[] newEntryFiles = new File[entryFiles.length];
            for (int i = 0; i < entryFiles.length; i++)
            {
                File oriFile = entryFiles[i];
                String encodedName = URLEncoder.encode(oriFile.getName(), "UTF-8");
                String path = oriFile.getParentFile().getAbsolutePath();
                path = path.replace("\\", File.separator);
                path = path.replace(File.separator, FILE_SEPARATOR);
                String encodedPath = URLEncoder.encode(path, "UTF-8");
                encodedPath = encodedPath.replace(FILE_SEPARATOR, File.separator);
                File destFile = new File(encodedPath, encodedName);
                FileUtil.copyFile(oriFile, destFile);
                newEntryFiles[i] = destFile;
            }

            // Add name encoded files into p_zipFile.
            addEntriesToZipFile(p_zipFile, newEntryFiles, donotIncludePath, comment);

            // Delete the TEMP files.
            for (File destFile : newEntryFiles)
            {
                destFile.delete();
            }
        }
        else
        {
            addEntriesToZipFile(p_zipFile, entryFiles, donotIncludePath,
                    comment);
        }
    }

    /**
     * This process will add a number of files to a zip file.
     * 
     * @param p_zipFile
     *            the ZIP file to be created.
     * @param entryFiles
     *            the files that will be zipped (added to p_zipFile). The files
     *            will have a path relative to the zip's path.
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void addEntriesToZipFile(File p_zipFile, File[] entryFiles)
            throws FileNotFoundException, IOException
    {
        boolean donotIncludePath = false;
        String comment = "";

        addEntriesToZipFile(p_zipFile, entryFiles, donotIncludePath, comment);
    }

    public static void addEntriesToZipFile(File p_zipFile, File[] entryFiles,
            boolean donotIncludePath, String comment)
            throws FileNotFoundException, IOException
    {
        Map<File, String> entryFileToFileNameMap = new HashMap<File, String>();
        for (File file : entryFiles)
        {
            entryFileToFileNameMap.put(file,
                    getEntryFileName(file, donotIncludePath));
        }

        addEntriesToZipFile(p_zipFile, entryFileToFileNameMap, comment);
    }

    public static void addEntriesToZipFile(File p_zipFile, Set<File> entryFiles,
            boolean donotIncludePath, String comment)
            throws FileNotFoundException, IOException
    {
        Map<File, String> entryFileToFileNameMap = new HashMap<File, String>();
        for (File file : entryFiles)
        {
            entryFileToFileNameMap.put(file,
                    getEntryFileName(file, donotIncludePath));
        }

        addEntriesToZipFile(p_zipFile, entryFileToFileNameMap, comment);
    }

    /**
     * This process will add a number of files to a zip file. 
     * 
     * @param p_zipFile
     *            The zip file to be created.
     * @param entryFiles
     *            The files that will be zipped (added to p_zipFile). The files
     *            will have a path relative to the zip's path.
     * @param excludedPath
     *            ZipEntry will not include the excludedPath.
     * @param comment
     *            zip file comment
     *
     * @return The zip file with all of the entries added to it.
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void addEntriesToZipFile(File p_zipFile,
            Set<File> entryFiles, String excludedPath, String comment)
            throws FileNotFoundException, IOException
    {
        String fileName = null;
        if(excludedPath != null && !excludedPath.endsWith(File.separator))
        {
            excludedPath += File.separator;
        }

        Map<File, String> entryFileToFileNameMap = new HashMap<File, String>();
        for(File file : entryFiles)
        {
            fileName = file.getPath();
            if(excludedPath != null && fileName.startsWith(excludedPath))
            {
                fileName = fileName.substring(fileName.indexOf(excludedPath)
                        + excludedPath.length());
            }
            else
            {
                int index = fileName.indexOf(File.separator);
                if (index > 0)
                {
                    fileName = fileName.substring(index + 1);
                }
            }
            entryFileToFileNameMap.put(file, fileName);
        }

        addEntriesToZipFile(p_zipFile, entryFileToFileNameMap, comment);
    }

    /**
     * Add files to ZIP file with specified entry file name.
     * 
     * @param p_zipFile
     *            -- the ZIP file object that files will be added into.
     * @param entryFileToFileNameMap
     *            -- File : entry file name in string.
     * @param comment
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void addEntriesToZipFile(File p_zipFile,
            Map<File, String> entryFileToFileNameMap, String comment)
            throws FileNotFoundException, IOException
    {
        FileOutputStream fileOutputStream = new FileOutputStream(p_zipFile);
        CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream,
                new CRC32());
        ZipOutputStream zos = new ZipOutputStream(cos);
        zos.setComment(comment);

        for (Map.Entry<File, String> entry : entryFileToFileNameMap.entrySet())
        {
            File entryFile = entry.getKey();
            String entryFileName = entry.getValue();

            // create new zip entry
            zos.putNextEntry(new ZipEntry(entryFileName));

            //
            DataInputStream dis = new DataInputStream(new FileInputStream(entryFile));
            long length = entryFile.length();
            int chunks = (int) length / SPLIT_SIZE;
            for (int m = 0; m < chunks; m++)
            {
                byte[] arr = new byte[SPLIT_SIZE];
                dis.readFully(arr);
                zos.write(arr);
            }
            if ((int) length % SPLIT_SIZE > 0)
            {
                byte[] arr = new byte[(int) length % SPLIT_SIZE];
                dis.readFully(arr);
                zos.write(arr);
            }

            dis.close();
        }

        zos.close();
    }

    /**
     * Get entry file name that includes all pathName or just file name.
     */
    private static String getEntryFileName(File entryFile,
            boolean donotIncludePath)
    {
        String fileName = (donotIncludePath) ? entryFile.getName() : entryFile
                .getPath();
        int index = fileName.indexOf(File.separator);
        if (index > 0)
        {
            fileName = fileName.substring(index + 1);
        }

        return fileName;
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
    public static ArrayList<String> unpackZipPackage(String p_zipFileName)
            throws Exception
    {
        Map<String, String> lastModifiedTimes = new HashMap<String, String>();
        return unpackZipPackage(p_zipFileName, lastModifiedTimes);
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
    public static ArrayList<String> unpackZipPackage(String p_zipFileName,
            Map<String, String> lastModifiedTimes) throws Exception
    {
        String zipdir = getZipDir(p_zipFileName);
        File zipDirFile = new File(zipdir);
        zipDirFile.mkdirs();
        ZipFile zipfile = new ZipFile(p_zipFileName);

        Enumeration entries = zipfile.getEntries();
        ArrayList<String> files = new ArrayList<String>();
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

            if (lastModifiedTimes != null && lastModifiedTimes.size() > 0)
            {
                String lastModifiedTime = lastModifiedTimes.get(outfile
                        .getName());
                if (lastModifiedTime != null && !lastModifiedTime.equals(""))
                {
                    outfile.setLastModified(Long.parseLong(lastModifiedTime));
                }
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
    public static ArrayList<String> unpackZipPackage(String p_zipFileName,
            String zipdir) throws Exception
    {
        File zipDirFile = new File(zipdir);
        zipDirFile.mkdirs();
        ZipFile zipfile = new ZipFile(p_zipFileName);

        Enumeration entries = zipfile.getEntries();
        ArrayList<String> files = new ArrayList<String>();
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

    public static ArrayList<String> unpackZipPackageForDownloadExport(
            String p_zipFileName, Map<String, String> lastModifiedTimes,
            String[] jobNames, String locale) throws Exception
    {
        String zipdir = getZipDir(p_zipFileName);
        File zipDirFile = new File(zipdir);
        zipDirFile.mkdirs();
        ZipFile zipfile = new ZipFile(p_zipFileName);
        ArrayList<String> files = new ArrayList<String>();
        Map<String, String> changedEntryName = new HashMap<String, String>();
        for(String jobName:jobNames)
        {       	
        	try {				
        		changedEntryName.putAll(changeEntryName(zipfile, jobName));
			} catch (Exception e) {}
        	Enumeration temp = zipfile.getEntries();
        	int jobIdIndex = -1;
        	while (temp.hasMoreElements())
        	{
        		ZipEntry entry = (ZipEntry) temp.nextElement();  		
        		String entryName = getEntryName(entry);
        		int index = entryName.indexOf("/" + jobName + "/");
        		if(index != -1)
        		{
        			if(jobIdIndex == -1)
        			{
        				jobIdIndex = index;
        			}
    				if(index < jobIdIndex)
    				{
    					jobIdIndex = index;
    				}
        		}
        	}
        	Enumeration entries = zipfile.getEntries();
        	while (entries.hasMoreElements())
        	{
        		// read zip entry
        		ZipEntry entry = (ZipEntry) entries.nextElement();
        		
        		String entryName = getEntryName(entry);
        		if(entryName.indexOf("/" + jobName + "/") == -1 ||
        				entryName.indexOf("/" + jobName + "/") != jobIdIndex)
        			continue;
        		String tailer = changedEntryName.get(entryName);
        		files.add(tailer);
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
        		
        	}
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
        String entryName = null;
        try
        {
            entryName = URLDecoder.decode(entry.getName(), "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            entryName = entry.getName();
        }

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
        
        ArrayList<String> endParts = new ArrayList<String>();
        int jobIdIndex = -1;
    	for(String entryName:entryNames)
    	{
    		int index = entryName.indexOf("/" + jobName + "/");
    		if(index != -1)
    		{
    			if(jobIdIndex == -1)
    			{
    				jobIdIndex = index;
    			}
				if(index < jobIdIndex)
				{
					jobIdIndex = index;
				}
    		}
    	}
        
        for (String entryName : entryNames)
        {
        	if(entryName.indexOf("/" + jobName + "/") == -1 ||
        			entryName.indexOf("/" + jobName + "/") != jobIdIndex)
        		continue;
            String[] result = splitEntryName(entryName, jobName);
            endParts.add(result[1]);
        }
        String commonPath = getCommonPath(endParts);
        int index = commonPath.length();
        
        for(String entryName:entryNames)
        {
        	if(entryName.indexOf("/" + jobName + "/") == -1 ||
        			entryName.indexOf("/" + jobName + "/") != jobIdIndex)
        		continue;
        	entryNamesMap.put(entryName, changeEntryName(entryName, index, jobName));
        }

        return entryNamesMap;
    }

    private static String changeEntryName(
            String entryName, int commonPathIndex, String jobName)
    {
        String changedEntryName = new String();

		String[] result = splitEntryName(entryName, jobName);
		String locale = result[0];
		String endPart = result[1];

        String str = jobName + "/" + locale + "/" + endPart.substring(commonPathIndex);
        str = str.replace("//", "/");
    	changedEntryName = str;

        return changedEntryName;
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
