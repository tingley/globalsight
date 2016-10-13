package com.globalsight.cxe.adapter.openoffice;

import java.io.File;
import java.util.List;

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeManager;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.FileUtil;
import com.globalsight.util.zip.ZipIt;

public class OpenOfficeConverter
{

    // private static OfficeManager m_officeManager = null;
    // private static Object officeManagerLocker = new Object();
    private static String officeHome = null;
    private static String isOoInstalledInit = null;
    private static boolean isOoInstalled = false;
    private static Object convertLocker = new Object();

    static
    {
        java.util.logging.Logger jlog = java.util.logging.Logger
                .getLogger("org.artofsolving.jodconverter");
        jlog.setLevel(java.util.logging.Level.SEVERE);
    }

    public OpenOfficeConverter()
    {
    }

    public String convertOdToXml(String p_odFile, String p_dir) throws Exception
    {
        ZipIt.unpackZipPackage(p_odFile, p_dir);

        return p_dir;
    }

    public String convertXmlToOd(String p_odFileName, String p_xmlDir) throws Exception
    {
        File xmlDir = new File(p_xmlDir);
        File parent = xmlDir.getParentFile();
        File zipFile = new File(parent, p_odFileName);
        if (zipFile.exists())
        {
            zipFile.delete();
        }

        // get all files
        List<File> fs = FileUtil.getAllFiles(xmlDir);
        File[] entryFiles = new File[fs.size()];
        entryFiles = fs.toArray(entryFiles);

        // create zip file
        zipFile.createNewFile();
        ZipIt.compress(xmlDir.getPath(), zipFile);

        return zipFile.getPath();
    }

    public String convertOdToHtml(String p_odFile, String p_htmlFile) throws Exception
    {
        return convertOdToHtml(new File(p_odFile), new File(p_htmlFile));
    }

    /**
     * Convert open document to html, single thread to call local process
     * @param p_odFile
     * @param p_htmlFile
     * @return
     * @throws Exception
     */
    public String convertOdToHtml(File p_odFile, File p_htmlFile) throws Exception
    {
        OfficeManager officeManager = null;
        boolean stopped = false;
        try
        {
            synchronized (convertLocker)
            {
                DefaultOfficeManagerConfiguration conf = new DefaultOfficeManagerConfiguration();
                conf = conf.setOfficeHome(getOfficeHome());
                officeManager = conf.buildOfficeManager();

                officeManager.start();
                OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
                converter.convert(p_odFile, p_htmlFile);
                officeManager.stop();
                stopped = true;
            }
        }
        catch (OfficeException oe)
        {
            throw oe;
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            try
            {
                if (!stopped && officeManager != null)
                {
                    officeManager.stop();
                }
            }
            catch (Exception e)
            {
                // ignore
            }
        }

        return p_htmlFile.getPath();
    }

    public static String getOfficeHome()
    {
        if (officeHome == null)
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            officeHome = sc.getStringParameter(SystemConfigParamNames.OPENOFFICE_INSTALL_DIR,
                    CompanyWrapper.SUPER_COMPANY_ID);
        }

        return officeHome;
    }

    public static boolean isOpenOfficeInstalled()
    {
        if (isOoInstalledInit == null)
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            String value = sc.getStringParameter(SystemConfigParamNames.OPENOFFICE_INSTALL_KEY,
                    CompanyWrapper.SUPER_COMPANY_ID);

            isOoInstalled = "true".equalsIgnoreCase(value);
            isOoInstalledInit = "inited";
        }

        return isOoInstalled;
    }

    // public static OfficeManager getOfficeManagerInstance() throws
    // IllegalStateException
    // {
    // if (m_officeManager == null)
    // {
    // synchronized (officeManagerLocker)
    // {
    // if (m_officeManager == null)
    // {
    // DefaultOfficeManagerConfiguration conf = new
    // DefaultOfficeManagerConfiguration();
    // m_officeManager = conf.buildOfficeManager();
    // m_officeManager.start();
    // }
    // }
    // }
    //
    // return m_officeManager;
    // }
}