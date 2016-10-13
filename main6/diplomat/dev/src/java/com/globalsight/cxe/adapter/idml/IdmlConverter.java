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
package com.globalsight.cxe.adapter.idml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.FileUtil;
import com.globalsight.util.file.FileWaiter;
import com.globalsight.util.zip.ZipIt;

public class IdmlConverter
{
    static
    {
        java.util.logging.Logger jlog = java.util.logging.Logger
                .getLogger("org.artofsolving.jodconverter");
        jlog.setLevel(java.util.logging.Level.SEVERE);
    }

    private static String isInstalledInit = null;
    private static boolean isInstalled = false;

    private static String convertDir = null;
    private static String convertDir_incontextReview = null;
    
    private boolean isIncontextReview = false;
    private long companyId = 1;

    public String convertIdmlToXml(String p_odFile, String p_dir)
            throws Exception
    {
        ZipIt.unpackZipPackage(p_odFile, p_dir);

        return p_dir;
    }

    public String convertXmlToIdml(String p_odFileName, String p_xmlDir)
            throws Exception
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
    
    public boolean getIsInContextReivew()
    {
        return isIncontextReview;
    }
    
    public void setIsInContextReivew(boolean vvv)
    {
        isIncontextReview = vvv;
    }

    public static boolean isInstalled()
    {
        if (isInstalledInit == null)
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            String value = sc.getStringParameter(
                    SystemConfigParamNames.INDD_INSTALL_KEY,
                    CompanyWrapper.SUPER_COMPANY_ID);

            if (convertDir == null)
            {
                convertDir = sc.getStringParameter(
                        SystemConfigParamNames.ADOBE_CONV_DIR_CS5,
                        CompanyWrapper.SUPER_COMPANY_ID);
            }

            isInstalled = "true".equalsIgnoreCase(value);
            isInstalledInit = "inited";
        }

        return isInstalled;
    }

    private String getConvertDir()
    {
        if (convertDir == null)
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            convertDir = sc.getStringParameter(
                    SystemConfigParamNames.ADOBE_CONV_DIR_CS5,
                    CompanyWrapper.SUPER_COMPANY_ID);
        }
        
        if (convertDir_incontextReview == null)
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            convertDir_incontextReview = sc.getStringParameter(
                    SystemConfigParamNames.INCTXRV_CONV_DIR_INDD,
                    CompanyWrapper.SUPER_COMPANY_ID);
        }

        return isIncontextReview ? convertDir_incontextReview : convertDir;
    }

    /**
     * Actually writes out the command file. The format of the command file is:
     * ConvertFrom=idml ConvertTo=pdf AcceptChanges=true | false | NA
     */
    private void writeCommandFile(String p_commandFileName, String from,
            String to) throws Exception
    {
        String convertFrom = "ConvertFrom=" + from;
        String convertTo = "ConvertTo=" + to;
        String acceptChanges = "AcceptChanges=NA";

        StringBuffer text = new StringBuffer();
        text.append(convertFrom).append("\r\n");
        text.append(convertTo).append("\r\n");
        text.append(acceptChanges).append("\r\n");
        text.append("MasterTranslated=true\r\n");
        text.append("TranslateHiddenLayer=true\r\n");

        FileUtil.writeFileAtomically(new File(p_commandFileName),
                text.toString(), "US-ASCII");
    }

    public void convertToPdf(File p_odFile, File p_pdfFile, String locale)
            throws Exception
    {
        String name = p_odFile.getName().toLowerCase();

        String type = "idml";
        String folder = "indd";

        File tFile = null;
        if (!isIncontextReview)
        {
            String testFile = type + (int) (Math.random() * 1000000) + ".test";
            tFile = new File(getConvertDir() + "/" + folder + "/" + testFile);
            FileUtil.writeFile(tFile, "test converter is start or not");
        }
        else
        {
            String testFile = type + (int) (Math.random() * 1000000) + ".test";
            tFile = new File(getConvertDir() + "/" + folder + "/" + locale + "/" + testFile);
            FileUtil.writeFile(tFile, "test converter is start or not");
        }

        name = (int) (Math.random() * 1000000) + name;
        String prefixName = name.substring(0, name.lastIndexOf("."));
        String fileType = name.substring(name.lastIndexOf("."));
        // String company = CompanyWrapper.getCompanyNameById(CompanyThreadLocal
        // .getInstance().getValue());
        String path = getConvertDir() + "/" + folder + "/" + "/" + locale + "/"
                + prefixName;
        

        FileUtil.copyFile(p_odFile, new File(path + fileType));
        writeCommandFile(path + ".ip_command", type, "pdf");

        // Gather up the filenames.
        String expectedPdfFileName = path + ".pdf";

        File expectedPdfFile = new File(expectedPdfFileName);

        String expectedStatus = path + ".ip_status";

        int i = 0;
        File f = new File(expectedStatus);
        boolean found = false;
        while (i++ < 10)
        {
            Thread.sleep(2000);
            if (f.exists())
            {
                found = true;
                break;
            }
        }

        if (!found)
        {
            if (tFile.exists())
            {
                tFile.delete();
                if (!isIncontextReview)
                {
                    throw new Exception("idml converter is not started");
                }
                else
                {
                    throw new Exception("In Context Review converter is not started");
                }
            }
        }

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

        FileUtil.copyFile(expectedPdfFile, p_pdfFile);
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }
}