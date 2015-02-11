package com.globalsight.everest.webapp.pagehandler.administration.imp;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.zip.ZipIt;

public class XLZFileHelper
{
    private static final Logger logger = Logger
            .getLogger(XLZFileHelper.class.getName());
    private static String baseDir = AmbFileStoragePathUtils.getCxeDocDirPath()
            .concat(File.separator);

    public static Hashtable<String, FileProfile> convertXLZFiles(
            Hashtable<String, FileProfile> p_fileList)
    {
        Hashtable<String, FileProfile> files = new Hashtable<String, FileProfile>();
        if (p_fileList == null || p_fileList.size() == 0)
            return files;

        String filename = "", realFilename = "";
        String zipDir = "", zipFile = "", convertedFilePath = "";
        String tmp = "";
        FileProfile fp = null;
        ArrayList<String> zipFiles = null;
        long referenceFPId = 0l;
        try
        {
            Iterator<String> fileKeys = p_fileList.keySet().iterator();

            while (fileKeys.hasNext())
            {
                filename = (String) fileKeys.next();
                fp = (FileProfile) p_fileList.get(filename);

                if (filename.toLowerCase().endsWith(".xlz"))
                {
                    // XLZ files, need to zip files and reset its file profile
                    realFilename = baseDir + filename;
                    zipDir = realFilename.substring(0,
                            realFilename.lastIndexOf("."));
                    zipFiles = ZipIt.unpackZipPackage(realFilename, zipDir);
                    referenceFPId = fp.getReferenceFP();
                    fp = ServerProxy.getFileProfilePersistenceManager()
                            .getFileProfileById(referenceFPId, false);
                    convertedFilePath = filename.substring(0,
                            filename.lastIndexOf("."));
                    for (int i = 0; i < zipFiles.size(); i++)
                    {
                        zipFile = (String) zipFiles.get(i);
                        if (zipFile.toLowerCase().endsWith(".xlf")
                                || zipFile.toLowerCase().endsWith(".xliff"))
                        {
                            tmp = convertedFilePath.concat(File.separator)
                                    .concat(zipFile);
                            files.put(tmp, fp);
                        }
                    }
                }
                else
                {
                    files.put(filename, fp);
                }
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        return files;
    }
}
