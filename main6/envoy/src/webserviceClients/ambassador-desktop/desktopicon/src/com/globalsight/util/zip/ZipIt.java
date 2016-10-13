/*                        
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
//import org.apache.tools.zip.ZipEntry;
//import org.apache.tools.zip.ZipFile;
//import org.apache.tools.zip.ZipOutputStream;


/**
 * This ZipIt class is responsible for performing some of the zipping 
 * processes such as creation of a zip file and adding files to it, and 
 * unzipping a zip file and extracting the files.  
 *
 * Note that when a problem comes along, this class must zip it! 
 * Zip it good! (from Austin Powers)
 */
public class ZipIt implements Serializable
{

    static public final int BUFSIZE = 4096;

    
    //////////////////////////////////////////////////////////////////////
    //  Begin: static Methods
    //////////////////////////////////////////////////////////////////////

    /**
    * This process will add a number of files to a zip file. The zip
    * file does not need to exist already.
    *
    * @param p_zipFile the zip file to be created.
    * @param entryFiles the files that will be zipped (added to p_zipFile).
    * The files will have a path relative to the zip's path.
    *
    * @return The zip file with all of the entries added to it.
    *
    * @throws Exception If any IOException happens during the 
    * zipping process.
    */    
    public static void addEntriesToZipFile (File p_zipFile, 
                                            File[] entryFiles) 
        throws Exception 
    {
    	ZipOutputStream zos = new ZipOutputStream(
                new FileOutputStream(p_zipFile));
        // read bytes of individual files to zip
        ZipEntry[] zipEntries = new ZipEntry[entryFiles.length];
        String fileName = null;
        for (int i = 0; i < entryFiles.length; i++)
        {
            long length = entryFiles[i].length();
            byte[] array = new byte[(int)length];
            DataInputStream dis = new DataInputStream(
                new FileInputStream(entryFiles[i]));
            dis.readFully(array, 0, (int)length);
            dis.close();

            // create new zip entry
            fileName = entryFiles[i].getPath();
            int index = fileName.indexOf(File.separator);
            if (index > 0)
            {
                fileName = fileName.substring(index+1);
            }
            zipEntries[i] = new ZipEntry(fileName);

            // put the entry into zip file
            zos.putNextEntry(zipEntries[i]);

            // write bytes of zip entry
            zos.write(array, 0, (int)length);
        }
        // close zos
        zos.close();
    }


    /**
     * Unpack the zip file and extract the files to a path relative to
     * the zip file's path.
     *
     * @param p_zipFileName The name of the zip file.
     * 
     * @return ArrayList of String for the entry names in the zip file
     * @throws Exception If any IOException happens during the 
     * unzipping process.
     */
    public static ArrayList unpackZipPackage(String p_zipFileName)
    throws Exception
    {
        String zipdir = getZipDir(p_zipFileName);
        File zipDirFile = new File(zipdir);
        zipDirFile.mkdir();
        ZipFile zipfile = new ZipFile(p_zipFileName);

        Enumeration entries = zipfile.entries();
        ArrayList files = new ArrayList();
        while (entries.hasMoreElements())
        {
            // read zip entry
            ZipEntry entry = (ZipEntry)entries.nextElement();
            File outfile = new File(
                zipdir + File.separator+ entry.getName());
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
            BufferedInputStream in = new BufferedInputStream(
                zipfile.getInputStream(entry));
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
        catch(Exception e)
        {
            // if this fails, the zip file won't get deleted.
        }

        return files;
    }
    
    /**
     * Unpack the zip file and extract the files to a path "String zipdir"
     * 
     */
    public static ArrayList unpackZipPackage(String p_zipFileName , String zipdir)
    throws Exception
    {
        File zipDirFile = new File(zipdir);
        zipDirFile.mkdirs();
        ZipFile zipfile = new ZipFile(p_zipFileName);

        Enumeration entries = zipfile.entries();
        ArrayList files = new ArrayList();
        while (entries.hasMoreElements())
        {
            // read zip entry
            ZipEntry entry = (ZipEntry)entries.nextElement();
            File outfile = new File(
                zipdir + File.separator+ entry.getName());
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
            BufferedInputStream in = new BufferedInputStream(
                zipfile.getInputStream(entry));
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
        catch(Exception e)
        {
            // if this fails, the zip file won't get deleted.
        }

        return files;
    }

    /*
     * The zip directory is the parent directory of the zip file.
     */
    private static String getZipDir(String p_zipFileName)
    throws IOException
    {
        File file = new File(p_zipFileName);
        return file.getParent();
    }

    /*
     * Remove files that were unzipped.
     */
    private static void cleanUp(ZipFile zipfile, File zipDirFile, String zipdir)
    {
        Enumeration entries = zipfile.entries();
        while (entries.hasMoreElements())
        {
            // read zip entry
            ZipEntry entry = (ZipEntry)entries.nextElement();
            File outfile = new File(zipdir + File.separator+ entry.getName());
            outfile.delete();
        }
        zipDirFile.delete();
    }

    public static Map getEntryNamesMap(
            ArrayList entryNames)
    {
        Map entryNamesMap = new HashMap();
        String commonPath = getCommonPath(entryNames, "");
        ArrayList truncatedEntryNames = getTruncatedCommon(commonPath, entryNames);
        for(int i = 0; i < truncatedEntryNames.size(); i++)
        {
            entryNamesMap.put(entryNames.get(i), truncatedEntryNames.get(i));
        }
        return entryNamesMap;
    }

    private static int getFolderCount(String filePath)
    {
        return filePath.split("/").length;
    }
    private static ArrayList orderByFolderCount(ArrayList entryNames)
    {
        ArrayList cloneObj = (ArrayList) entryNames.clone();
        for(int i = 0; i < cloneObj.size(); i++)
        {
            String min = (String) cloneObj.get(0);
            for(int j = i + 1; j < cloneObj.size(); j++)
            {
                String entryName = (String) cloneObj.get(j);
                if(getFolderCount(entryName) < getFolderCount(min))
                {
                    String tmp = min;
                    cloneObj.set(i, entryName);
                    cloneObj.set(j, tmp);
                }
            }
        }
        return cloneObj;
    }
    public static String getCommonPath(ArrayList entryNames, String prefix)
    {
        ArrayList cloneObj = orderByFolderCount(entryNames);
        String first = (String) cloneObj.get(0);
        if(getFolderCount(first) < 2)
        {
            return prefix;
        }
        String guessedCommon = first.split("/")[0];
        if(getFolderCount(first) == 2)
        {
            if(isCommon(guessedCommon, cloneObj))
            {
                return (prefix.length() == 0) ? guessedCommon : prefix + "/" + guessedCommon;
            }
            else
            {
                return prefix;
            }
        }
        if(isCommon(guessedCommon, cloneObj))
        {
            prefix = (prefix.length() == 0) ? guessedCommon : prefix + "/" + guessedCommon;
            cloneObj = getTruncatedCommon(guessedCommon, cloneObj);
            return getCommonPath(cloneObj, prefix);
        }
        else
        {
            return prefix;
        }
        
    }

    private static ArrayList getTruncatedCommon(String guessedCommon,
            ArrayList entryNames)
    {
        if("".equals(guessedCommon))
        {
            return entryNames;
        }
        ArrayList cloneObj = (ArrayList) entryNames.clone();
        for(int i = 0; i < cloneObj.size(); i++)
        {
            String entryName = ((String) cloneObj.get(i)).substring(guessedCommon.length() + 1);
            cloneObj.set(i, entryName);
        }
        return cloneObj;
    }

    private static boolean isCommon(String guessedCommon,
            ArrayList entryNames)
    {
        for(int i = 0; i < entryNames.size(); i++)
        {
            if(! ((String) entryNames.get(i)).startsWith(guessedCommon + "/"))
            {
                return false;
            }
        }
        return true;
    }

    //////////////////////////////////////////////////////////////////////
    //  End: static Methods
    ////////////////////////////////////////////////////////////////////// 
}
