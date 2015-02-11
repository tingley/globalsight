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
package galign.helpers.project;

import galign.Setup;
import galign.data.Project;
import galign.helpers.AlignmentPackage;
import galign.helpers.filefilter.ProjectFileFilter;
import galign.helpers.util.GAlignException;

import org.dom4j.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

/**
 * This class builds a Project model (a UI data object) from a GAP or
 * ZIP file. If the filename denotes a zip file, it is unpacked
 * first. The GAP file is opened and read into an AlignmentPackage
 * object, from which the Project object gets initialized.
 *
 * The project holds pointers to the original location of the GAP file
 * and also of the parsed AlignmentPackage.
 */
public class ProjectHelper
{
    static public final int BUFSIZE = 4096;

    private String m_gap = null;
    private String m_directory;
    private String m_name;
    private String m_alp;

    public Project openProject(String p_filename)
        throws GAlignException
    {
        Project result = null;

        try
        {
            if (isZipPackage(p_filename))
            {
                unpackZipPackage(p_filename);
            }

            if (m_gap != null)
            {
                result = readProjectFile();
            }
        }
        catch (GAlignException e)
        {
            throw e;
        }
        catch (Exception ex)
        {
            if (Setup.DEBUG)
            {
                ex.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Zip up files in the package.
     */
    public void prepareForUpload(Project p_project)
        throws IOException
    {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(m_alp));
        File dir = new File(m_directory);
        String[] list = dir.list();
        for (int i = 0; i < list.length; i++)
        {
            addZipEntry(zos, list[i]);
        }
        zos.close();
    }
    
    private void addZipEntry(ZipOutputStream p_zos, String p_entryName)
        throws IOException
    {
        int bytesIn = 0;
        byte[] readBuffer = new byte[2156]; 
        FileInputStream fis = new FileInputStream(
                        m_directory + File.separator + p_entryName); 

        ZipEntry entry = new ZipEntry(p_entryName);
        p_zos.putNextEntry(entry);
        while((bytesIn = fis.read(readBuffer)) != -1) 
        { 
            p_zos.write(readBuffer, 0, bytesIn); 
        } 
        //close the Stream 
        fis.close(); 
        p_zos.closeEntry();
    }


    private void unpackZipPackage(String p_filename)
        throws GAlignException, IOException
    {
        m_alp = p_filename;
        String zipdir = getZipDir(p_filename);
        File zipDirFile = new File(zipdir);
        if (shouldUnzip(zipDirFile, m_alp))
        {
            ZipFile zipfile;
            try {
                zipfile = new ZipFile(p_filename);
            }
            catch (java.util.zip.ZipException ze)
            {
                throw new GAlignException("error.cannotOpenZip", ze);
            }
            zipDirFile.mkdir();
            Enumeration entries = zipfile.getEntries();
            while (entries.hasMoreElements())
            {
                // read zip entry
                ZipEntry entry = (ZipEntry)entries.nextElement();
                File outfile = new File(zipdir + File.separator+ entry.getName());

                BufferedOutputStream out = new BufferedOutputStream(
                                                new FileOutputStream(outfile));

                byte[] buf = new byte[BUFSIZE];
                BufferedInputStream in = new BufferedInputStream(
                                                zipfile.getInputStream(entry));
                int readLen = 0;
                while((readLen = in.read(buf, 0, BUFSIZE)) != -1)
                {
                    // write file
                    try {
                        out.write(buf, 0, readLen);
                    }
                    catch (FileNotFoundException e)
                    {
                        out.close();
                        cleanUp(zipfile, zipDirFile, zipdir);
                        throw new GAlignException("error.cannotUnzipFile", e);
                    }
                    catch (IOException ioe)
                    {
                        out.close();
                        cleanUp(zipfile, zipDirFile, zipdir);
                        throw new GAlignException("error.cannotUnzipFile", ioe);
                    }
                }
                out.close();
            }
        }
        m_directory = zipDirFile.getCanonicalPath();
        String[] gaps = zipDirFile.list(new ProjectFileFilter());
        m_name = gaps[0];
        m_gap = m_directory + File.separator + m_name;
    }

    /**
     * If a subdirectory by the name of the alp file (without the .alp) does
     * not exist, then return true;
     * If it does exists, check the timestamp of the .alp file with the timestamp
     * of the gap file.  If the alp is newer, return true;
     */
    private boolean shouldUnzip(File zipDirFile, String alpFilename)
    {
        if (!zipDirFile.exists())
            return true;
        try 
        {
            File alpFile = new File(alpFilename);
            String[] gaps = zipDirFile.list(new ProjectFileFilter());
            m_name = gaps[0];
            File gapFile =
                new File(zipDirFile.getCanonicalPath() + File.separator + gaps[0]);
            if (alpFile.lastModified() > gapFile.lastModified())
                return true;
        }
        catch (IOException e)
        {
            return true;
        }
        return false;
    }

    private Project readProjectFile()
        throws Exception
    {
        AlignmentPackage pkg = new AlignmentPackage(m_gap);

        return new Project(m_directory, m_name, pkg);
    }

    /*
     * Remove files that were unzipped.
     */
    private void cleanUp(ZipFile zipfile, File zipDirFile, String zipdir)
    {
        Enumeration entries = zipfile.getEntries();
        while (entries.hasMoreElements())
        {
            // read zip entry
            ZipEntry entry = (ZipEntry)entries.nextElement();
            File outfile = new File(zipdir + File.separator+ entry.getName());
            outfile.delete();
        }
        zipDirFile.delete();
    }

    private boolean isZipPackage(String p_filename)
    {
        String ext = p_filename.substring(p_filename.lastIndexOf(".") + 1);

        if (ext.equalsIgnoreCase("alp"))
        {
            return true;
        }

        return false;
    }

    /*
     * The zip directory is the directory of the alp file plus the basename of
     * the alp file.  ie.  /foo/bar/baz.alp would have a zip directory of /foo/bar/baz
     */
    private String getZipDir(String p_filename)
        throws IOException
    {
        File file = new File(p_filename);
        String dir = file.getCanonicalPath();
        dir = dir.substring(0, dir.length()-4);
        return dir;
    }
}
