package com.globalsight.cxe.adapter.msoffice2010;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.page.pageexport.ExportHelper;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.docproc.merger.html.HtmlPreviewerHelper;
import com.globalsight.util.FileUtil;
import com.globalsight.util.file.FileWaiter;
import com.globalsight.util.zip.ZipIt;

public class MsOffice2010Converter
{
    private static String officeHome = null;
    private static String isInstalledInit = null;
    private static boolean isInstalled = false;
    
    private static String convertDir = null;

    static private final Logger logger = Logger
                .getLogger(MsOffice2010Converter.class);

    public MsOffice2010Converter()
    {
    }

    public String convertOdToXml(String p_odFile, String p_dir) throws Exception
    {
        ZipIt.unpackZipPackage(p_odFile, p_dir);

        return p_dir;
    }

    public String convertXmlToOffice(String p_odFileName, String p_xmlDir) throws Exception
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
    
    /**
     * Actually writes out the command file. The format of the command file is:
     * ConvertFrom=doc | rtf | html | ppt | xls | docx | pptx | xlsx
     * ConvertTo=doc | rtf | html | ppt | xls | docx | pptx | xlsx
     * AcceptChanges=true | false | NA
     */
    private void writeCommandFile(String p_commandFileName, String from, String to) throws Exception
    {
        String convertFrom = "ConvertFrom=" + from;
        String convertTo = "ConvertTo=" + to;
        String acceptChanges = "AcceptChanges=NA";

        StringBuffer text = new StringBuffer();
        text.append(convertFrom).append("\r\n");
        text.append(convertTo).append("\r\n");
        text.append(acceptChanges).append("\r\n");

        FileUtil.writeFileAtomically(new File(p_commandFileName),
                text.toString(), "US-ASCII");
    }
    
    public void convertToHtml(File p_odFile, File p_htmlFile, File p_pdfFile, 
            String locale, boolean handleGSColorTag, boolean toPdf) throws Exception
    {
    	String name = p_odFile.getName().toLowerCase();

    	String type = null;
    	String folder = null;
        if (name.endsWith("doc") || name.endsWith("docx"))
        {
            type = "docx";
            folder = "word";
        }       
        else if (name.endsWith("ppt") || name.endsWith("pptx"))
        {
            type = "pptx";
            folder = "powerpoint";
        }
        else if (name.endsWith("xls") || name.endsWith("xlsx"))
        {
            type = "xlsx";
            folder = "excel";
        }
        
        name = (int) (Math.random() * 1000000) + name;
        String prefixName = name.substring(0, name.lastIndexOf("."));
        String fileType = name.substring(name.lastIndexOf(".") );
        
        String testFile = type + (int) (Math.random() * 1000000) + ".test";
        File tFile = new File(getConvertDir() + "/" + folder + "/" + testFile);
        FileUtil.writeFile(tFile, "test converter is start or not");
//		String company = CompanyWrapper.getCompanyNameById(CompanyThreadLocal
//				.getInstance().getValue());
//		String path = getConvertDir() + "/" + folder + "/" + company + "/"
//				+ locale + "/" + prefixName;
		
		String path = getConvertDir() + "/" + folder + "/" + locale + "/" + prefixName;
        
        FileUtil.copyFile(p_odFile, new File(path + fileType));
    	writeCommandFile(path + ".im_command", type, "html");

        // Gather up the filenames.
        String expectedHtmlFileName = path + ".html";
        File expectedHtmlFile = new File(expectedHtmlFileName);
        String expectedStatus = path + ".status";
        
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
        		throw new Exception(type + " converter is not started");
        	}
        }

        long maxTimeToWait = 60 * 60 * 1000;
        FileWaiter waiter = new FileWaiter(2000L, maxTimeToWait,
        		expectedStatus);
        waiter.waitForFile();
        
        File statusFile = new File(expectedStatus.toString());
        BufferedReader reader = new BufferedReader(new FileReader(
                statusFile));
        String line = reader.readLine();
        String msg = reader.readLine();
        logger.info(msg);
        String errorCodeString = line.substring(6); // Error:1
        reader.close();
        statusFile.delete();
        int errorCode = Integer.parseInt(errorCodeString);
        if (errorCode > 0)
        {
            throw new Exception(msg);
        }
        
        if (handleGSColorTag)
        {
            changeFileColorForPreview(expectedHtmlFile);
        }
        
        FileUtil.copyFile(expectedHtmlFile, p_htmlFile);
        File files = new File(path + ".files");
        if (files.exists())
        {
            if (handleGSColorTag)
            {
                changeDirColorForPreview(files);
            }
            
        	String parent = p_htmlFile.getParent();
        	FileUtil.copyFolder(files, new File(parent + "/" + files.getName()));
        }
        files = new File(path + "_files");
        if (files.exists())
        {
            if (handleGSColorTag)
            {
                changeDirColorForPreview(files);
            }
        	String parent = p_htmlFile.getParent();
        	FileUtil.copyFolder(files, new File(parent + "/" + files.getName()));
        }
        
        if (toPdf)
        {
            // remove PicExportError
            String content = FileUtils.read(expectedHtmlFile, "UTF-8");
            int startIndex = content.indexOf("<head>");
            int endIndex = content.indexOf("</head>");
            
            if (startIndex != -1 && endIndex != -1)
            {
                String headString = content.substring(startIndex, endIndex);

                if (headString.contains("list-style-image:url(\"PicExportError\");"))
                {
                    String before = content.substring(0, startIndex);
                    String end = content.substring(endIndex);
                    headString = headString.replace(
                            "list-style-image:url(\"PicExportError\");",
                            "list-style-image:url(\"\");");
                    content = before + headString + end;
                    
                    FileUtils.write(expectedHtmlFile, content, "UTF-8");
                }
            }
            
            // start convert
            String expectedPdfFileName = path + ".pdf";
            File expectedPdfFile = new File(expectedPdfFileName);
            String expectedPdfStatus = path + ".pdf.status";
            
            writeCommandFile(path + ".ex_command", "html", "pdf");
            
            FileWaiter pdfwaiter = new FileWaiter(2000L, maxTimeToWait,
                    expectedPdfStatus);
            pdfwaiter.waitForFile();
            
            File pdfStatusFile = new File(expectedPdfStatus.toString());
            BufferedReader pdfreader = new BufferedReader(new FileReader(
                    pdfStatusFile));
            String pdfline = pdfreader.readLine();
            String pdfmsg = pdfreader.readLine();
            logger.info(pdfmsg);
            String pdferrorCodeString = pdfline.substring(6); // Error:1
            pdfreader.close();
            pdfStatusFile.delete();
            int pdferrorCode = Integer.parseInt(pdferrorCodeString);
            if (pdferrorCode > 0)
            {
                throw new Exception(pdfmsg);
            }
            
            FileUtil.copyFile(expectedPdfFile, p_pdfFile);
        }
    }
    
    private void changeDirColorForPreview(File predir) throws IOException
    {
        File[] files = predir.listFiles(new ListFileNameFilter());

        changeFilesColorForPreview(files);

        File[] allfiles = predir.listFiles();
        for (File file : allfiles)
        {
            if (file.isDirectory())
            {
                File[] subfiles = file.listFiles(new ListFileNameFilter());
                changeFilesColorForPreview(subfiles);
            }
        }

    }
    
    private void changeFilesColorForPreview(File[] files) throws IOException
    {
        if (files == null || files.length == 0)
        {
            return;
        }

        for (File file : files)
        {
            changeFileColorForPreview(file);
        }
    }
    
    private void changeFileColorForPreview(File file) throws IOException
    {
        if (file == null || !file.exists())
        {
            return;
        }

        String content = FileUtils.read(file, "UTF-8");

        if (content.contains(ExportHelper.GS_COLOR_S))
        {
            String newcontent = HtmlPreviewerHelper.processGSColorTag(content);
            FileUtils.write(file, newcontent, "UTF-8");
        }
    }
    
    private class ListFileNameFilter implements FilenameFilter
    {
        @Override
        public boolean accept(File dir, String name)
        {
            if (name.toLowerCase().endsWith(".html") || name.toLowerCase().endsWith(".htm"))
            {
                return true;
            }

            return false;
        }

    }
    
    private String getConvertDir()
    {
    	if (convertDir == null)
        {
    		SystemConfiguration sc = SystemConfiguration.getInstance();
        	convertDir = sc.getStringParameter(SystemConfigParamNames.MSOFFICE_CONV_DIR,
                    CompanyWrapper.SUPER_COMPANY_ID);
        }
    	
    	return convertDir;
    }

    public static boolean isInstalled()
    {
        if (isInstalledInit == null)
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            String value = sc.getStringParameter(SystemConfigParamNames.WORD_INSTALL_KEY,
                    CompanyWrapper.SUPER_COMPANY_ID);
            
            if (!"true".equalsIgnoreCase(value))
            {
            	sc.getStringParameter(SystemConfigParamNames.EXCEL_INSTALL_KEY,
                        CompanyWrapper.SUPER_COMPANY_ID);
            }
            
            if (!"true".equalsIgnoreCase(value))
            {
            	sc.getStringParameter(SystemConfigParamNames.POWERPOINT_INSTALL_KEY,
                        CompanyWrapper.SUPER_COMPANY_ID);
            }
            
            if (convertDir == null)
            {
            	convertDir = sc.getStringParameter(SystemConfigParamNames.MSOFFICE_CONV_DIR,
                        CompanyWrapper.SUPER_COMPANY_ID);
            }

            isInstalled = "true".equalsIgnoreCase(value);
            isInstalledInit = "inited";
        }

        return isInstalled;
    }
}