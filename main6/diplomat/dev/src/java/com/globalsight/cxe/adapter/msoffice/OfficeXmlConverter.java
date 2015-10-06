package com.globalsight.cxe.adapter.msoffice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeManager;

import com.globalsight.cxe.engine.util.FileCopier;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.FileUtil;
import com.globalsight.util.file.FileWaiter;
import com.globalsight.util.zip.ZipIt;

public class OfficeXmlConverter
{
    private static String convertDir = null;
    private static String convertDir_incontextReview = null;

    private boolean isIncontextReview = false;
    private long companyId = 1;

    public OfficeXmlConverter()
    {
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public String convertOfficeToXml(String p_odFile, String p_dir) throws Exception
    {
        ZipIt.unpackZipPackage(p_odFile, p_dir);

        return p_dir;
    }

    public String convertXmlToOffice(String p_odFileName, String p_xmlDir) throws Exception
    {
        OfficeXmlRepairer.repair(p_xmlDir);

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

    public boolean getIsInContextReivew()
    {
        return isIncontextReview;
    }

    public void setIsInContextReivew(boolean vvv)
    {
        isIncontextReview = vvv;
    }

    public void convertToPdf(String type, File file, File pdfFile, String locale) throws Exception
    {
        String name = file.getName().toLowerCase();

        String folder = "word";
        if (type.equals("pptx"))
        {
            folder = "powerpoint";
        }
        else if (type.equals("xlsx"))
        {
            folder = "excel";
        }

        File tFile = null;

        String prefixName = name.substring(0, name.lastIndexOf("."));
        String fileType = name.substring(name.lastIndexOf("."));
        String company = CompanyWrapper.getCompanyNameById(CompanyThreadLocal.getInstance()
                .getValue());
        String localeDir = getConvertDir() + "/" + folder + "/" + company + "/" + locale;
        String path = localeDir + "/" + prefixName;

        File localeDirFile = new File(localeDir);
        if (!localeDirFile.exists())
        {
            localeDirFile.mkdirs();
        }
        
        FileCopier.copy(file, localeDir);
        writeCommandFile(path + ".im_command", type, "pdf");

        // Gather up the filenames.
        String expectedPdfFileName = path + ".pdf";

        File expectedPdfFile = new File(expectedPdfFileName);
        String expectedStatus = path + ".status";

        long maxTimeToWait = 60 * 60 * 1000;
        FileWaiter waiter = new FileWaiter(2000L, maxTimeToWait, expectedStatus);
        waiter.waitForFile();

        File statusFile = new File(expectedStatus.toString());
        BufferedReader reader = new BufferedReader(new FileReader(statusFile));
        String line = reader.readLine();
        String msg = reader.readLine();
        String errorCodeString = line.substring(6); // Error:1
        reader.close();
        statusFile.delete();
        int errorCode = Integer.parseInt(errorCodeString);
        if (errorCode > 0)
        {
            throw new Exception(msg);
        }

        FileUtil.copyFile(expectedPdfFile, pdfFile);
    }

    private void writeCommandFile(String p_commandFileName, String from, String to)
            throws Exception
    {
        String convertFrom = "ConvertFrom=" + from;
        String convertTo = "ConvertTo=" + to;
        String acceptChanges = "AcceptChanges=NA";

        StringBuffer text = new StringBuffer();
        text.append(convertFrom).append("\r\n");
        text.append(convertTo).append("\r\n");
        text.append(acceptChanges).append("\r\n");

        FileUtil.writeFileAtomically(new File(p_commandFileName), text.toString(), "US-ASCII");
    }

    private String getConvertDir()
    {
        if (convertDir == null)
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            convertDir = sc.getStringParameter(SystemConfigParamNames.INCTXRV_CONV_DIR_OFFICE,
                    CompanyWrapper.SUPER_COMPANY_ID);
        }

        if (convertDir_incontextReview == null)
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            convertDir_incontextReview = sc
                    .getStringParameter(SystemConfigParamNames.INCTXRV_CONV_DIR_OFFICE,
                            CompanyWrapper.SUPER_COMPANY_ID);
        }

        return isIncontextReview ? convertDir_incontextReview : convertDir;
    }

}