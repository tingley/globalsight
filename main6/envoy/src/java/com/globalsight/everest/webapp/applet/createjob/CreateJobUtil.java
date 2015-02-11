package com.globalsight.everest.webapp.applet.createjob;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.rarfile.FileHeader;
import netscape.javascript.JSObject;

public class CreateJobUtil
{
    private static final Charset UTF8 = Charset.forName("UTF-8");
    
    /**
     * Check if a file is a zip file
     * @param file
     * @return true if is a zip file
     */
    public static boolean isZipFile(File file)
    {
        String extension = CreateJobUtil.getFileExtension(file);
        if (extension != null && extension.equalsIgnoreCase("zip"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Check if a file is a rar file
     * @param file
     * @return true if is a rar file
     */
    public static boolean isRarFile(File file)
    {
        String extension = CreateJobUtil.getFileExtension(file);
        if (extension != null && extension.equalsIgnoreCase("rar"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Check if a file is a rar file
     * @param file
     * @return true if is a rar file
     */
    public static boolean is7zFile(File file)
    {
        String extension = CreateJobUtil.getFileExtension(file);
        if (extension != null && extension.equalsIgnoreCase("7z"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Get the file extension of a file
     * @param file
     * @return file extension
     */
    public static String getFileExtension(File file)
    {
        if (file != null)
        {
            String fileName = file.getName();
            if (fileName.lastIndexOf(".") != -1)
            {
                String extension = fileName
                        .substring(fileName.lastIndexOf(".") + 1);
                return extension;
            }
            else
            {
                return "";
            }
        }
        else
        {
            return null;
        }
    }
    
    public static List<ZipEntry> getFilesInZipFile(File file)
    {
        List<ZipEntry> filesInZip = new ArrayList<ZipEntry>();
        ZipInputStream zin = null;
        try
        {
            String zipFileFullPath = file.getPath();
            zin = new ZipInputStream(new FileInputStream(zipFileFullPath));
            ZipEntry zipEntry = null;
            while ((zipEntry = zin.getNextEntry()) != null)
            {
                if (zipEntry.isDirectory())
                {
                    continue;
                }
                filesInZip.add(zipEntry);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (zin != null)
            {
                try
                {
                    zin.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return filesInZip;
    }
    
    public static List<FileHeader> getFilesInRarFile(File file) throws Exception
    {
        List<FileHeader> filesInRar = new ArrayList<FileHeader>();
        Archive archive = null;
        try
        {
            archive = new Archive(file);
            FileHeader fileHeader = null;
            while ((fileHeader = archive.nextFileHeader()) != null)
            {
                if (fileHeader.isDirectory())
                {
                    continue;
                }
                filesInRar.add(fileHeader);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (archive != null)
            {
                try
                {
                    archive.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return filesInRar;
    }
    
    public static List<SevenZArchiveEntry> getFilesIn7zFile(File zip7zfile)
            throws Exception
    {
        SevenZFile sevenZFile = null;
        List<SevenZArchiveEntry> filesInZip7z = new ArrayList<SevenZArchiveEntry>();
        try
        {
            sevenZFile = new SevenZFile(zip7zfile);
            SevenZArchiveEntry entry = sevenZFile.getNextEntry();
            while (entry != null)
            {
                if (!entry.isDirectory())
                {
                    filesInZip7z.add(entry);
                }
                try
                {
                    entry = sevenZFile.getNextEntry();
                }
                catch (Exception e)
                {
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (sevenZFile != null)
            {
                try
                {
                    sevenZFile.close();
                }
                catch (IOException e)
                {
                    System.err.println("Error closing file: " + e);
                }
            }
        }
        return filesInZip7z;
    }
    
    /**
     * Run a javascript function defined on the jsp
     * @param window
     * @param functionName
     * @param parameters
     */
    public static Object runJavaScript(JSObject window, String functionName,
            Object[] parameters)
    {
        Object ret = window.call(functionName, parameters);
        return ret;
    }
    
    /**
     * Get the id of a file
     * @param data
     * @return
     */
    public static String getFileId(String data) {
        byte[] bytes = getMD5(data);
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.put(bytes, 0, 8);
        return String.valueOf(Math.abs(buf.getLong(0)));
    }
    
    private static byte[] getMD5(String s) {
        MessageDigest digest;
        try {
            digest = java.security.MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e); // can't happen
        }
        digest.update(s.getBytes(UTF8));
        return digest.digest();
    }

    /**
     * Test if the 7z file can be de-compressed successfully.
     * 
     * @param file -- a 7z file
     */
    public static boolean canBeDecompressedSuccessfully(File file)
            throws Exception
    {
        SevenZFile sevenZFile = new SevenZFile(file);
        SevenZArchiveEntry entry = sevenZFile.getNextEntry();
        while (entry != null)
        {
            try
            {
                entry = sevenZFile.getNextEntry();
            }
            catch (Exception e)
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * decompress a zip file
     * @param file---a zip file
     * @return
     */
    public static boolean unzipFile(File file)
    {
        String zipFileFullPath = file.getPath();// path contains file name
        String zipFilePath = zipFileFullPath.substring(0,
                zipFileFullPath.indexOf(file.getName()));// path without file name
        ZipInputStream zin = null;
        try
        {
            FileInputStream fis=new FileInputStream(zipFileFullPath);
            zin = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry zipEntry = null;
            byte[] buf = new byte[1024];
            
            while ((zipEntry = zin.getNextEntry()) != null)
            {
                String zipEntryName = zipEntry.getName();
                String newPath = zipFilePath
                        + File.separator
                        + file.getName().substring(0,
                                file.getName().lastIndexOf("."))
                        + File.separator
                        + zipEntryName;// original path + zipfile Name + entry name
                File outfile = new File(newPath);
                if (zipEntry.isDirectory())
                {
                    outfile.mkdirs();
                    continue;
                }
                else 
                {
                    if (!outfile.getParentFile().exists())
                    {
                        outfile.getParentFile().mkdirs();
                    }
                }
                
                OutputStream os = new BufferedOutputStream(
                        new FileOutputStream(outfile));
                int readLen = 0;
                try
                {
                    readLen = zin.read(buf, 0, 1024);
                }
                catch (IOException ioe)
                {
                    readLen = -1;
                }
                while (readLen != -1)
                {
                    os.write(buf, 0, readLen);
                    try
                    {
                        readLen = zin.read(buf, 0, 1024);
                    }
                    catch (IOException ioe)
                    {
                        readLen = -1;
                    }
                }
                os.close();
            }
        }
        catch (IOException e)
        {
            return false;
        }
        finally
        {
            if (zin != null)
            {
                try
                {
                    zin.close();
                }
                catch (IOException e)
                {
                }
            }
        }

        return true;
    }
    
    /**
     * decompress a rar file
     * @param rarFile---a rar file
     */
    public static boolean unrarFile(File rarFile) throws Exception
    {
        Archive archive = null;
        FileOutputStream fos = null;
        String pathname = rarFile.getAbsolutePath();
        String destDir = pathname.substring(0, pathname.lastIndexOf("."));
        try
        {
            File destDirFile = new File(destDir);
            destDirFile.mkdirs();
            archive = new Archive(rarFile);
            FileHeader fileHeader = archive.nextFileHeader();
            while (fileHeader != null)
            {
                if (!fileHeader.isDirectory())
                {
                    String compressFileName = fileHeader.getFileNameString()
                            .trim();
                    File file = new File(destDirFile, compressFileName);
                    file.getParentFile().mkdirs();

                    fos = new FileOutputStream(file);
                    archive.extractFile(fileHeader, fos);
                    fos.close();
                    fos = null;
                }
                fileHeader = archive.nextFileHeader();
            }
        }
        catch (Exception e)
        {
            return false;
        }
        finally
        {
            if (fos != null)
            {
                try
                {
                    fos.close();
                    fos = null;
                }
                catch (Exception e)
                {
                }
            }
            if (archive != null)
            {
                try
                {
                    archive.close();
                    archive = null;
                }
                catch (Exception e)
                {
                }
            }
        }

        return true;
    }
    
    /**
     * decompress a 7z file
     * @param zip7zfile---a 7z file
     */
    public static boolean un7zFile(File zip7zfile) throws Exception
    {
        SevenZFile sevenZFile = null;
        FileOutputStream out = null;
        String zipFileFullPathName = zip7zfile.getAbsolutePath();
        String zipFolder = zipFileFullPathName.substring(0,
                zipFileFullPathName.lastIndexOf("."));

        try
        {
            sevenZFile = new SevenZFile(zip7zfile);
            SevenZArchiveEntry entry = sevenZFile.getNextEntry();

            while (entry != null)
            {
                if (!entry.isDirectory())
                {
                    File entryFile = new File(zipFolder, entry.getName());
                    entryFile.getParentFile().mkdirs();
                    out = new FileOutputStream(entryFile);
                    byte[] content = new byte[(int) entry.getSize()];
                    sevenZFile.read(content);
                    out.write(content);
                    out.close();
                }
                entry = sevenZFile.getNextEntry();
            }
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
        finally
        {
            if (out != null)
            {
                try
                {
                    out.close();
                    out = null;
                }
                catch (Exception e)
                {
                }
            }
            if (sevenZFile != null)
            {
                try
                {
                    sevenZFile.close();
                }
                catch (IOException e)
                {
                }
            }
        }
    }
}
