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
package com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.xmldtd.XmlDtdImpl;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.comparator.XmlDtdFileComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.zip.ZipIt;

public class XmlDtdBasicHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(XmlDtdBasicHandler.class);

    static public final int BUFSIZE = 4096;

    private ThreadLocal<Long> dtdId = new ThreadLocal<Long>();

    @ActionHandler(action = XmlDtdConstant.SAVE, formClass = "com.globalsight.cxe.entity.xmldtd.XmlDtdImpl", loadFromDb = true)
    public void save(HttpServletRequest request, HttpServletResponse response,
            Object form) throws Exception
    {
        logger.debug("Saving xml dtd...");

        clearSessionExceptTableInfo(request.getSession(false),
                XmlDtdConstant.XMLDTD_KEY);

        XmlDtdImpl dtd = (XmlDtdImpl) form;
        boolean isNew = dtd.getId() < 1;
        dtd.setAddComment(getCheckBoxParameter(request, "addComment"));
        dtd.setSendEmail(getCheckBoxParameter(request, "sendEmail"));
        HibernateUtil.save(dtd);

        dtdId.set(dtd.getId());

        // Remove files for the new xml dtd.
        if (isNew)
        {
            List<File> files = getAllDtdFiles();
            for (File file : files)
            {
                file.delete();
            }
        }

        logger.debug("Saving xml dtd finished.");
    }

    @ActionHandler(action = XmlDtdConstant.UPLOAD, formClass = "")
    public void upload(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        logger.debug("Uploading dtd files...");

        FileUploader uploader = new FileUploader();
        File file = uploader.upload(request);

        String id = uploader.getFieldValue("id");
        if (id != null && id.trim().length() > 0)
        {
            dtdId.set(Long.parseLong(id));

            File targetFile = new File(DtdFileManager.getStorePath(Long
                    .parseLong(id)) + "/" + uploader.getName());
            if (!file.renameTo(targetFile))
            {
                FileUtils.copyFile(file, targetFile);
            }

            if (targetFile.getName().endsWith(".zip"))
            {
                ZipIt.unpackZipPackage(targetFile.getPath());
                targetFile.delete();
            }
        }

        logger.debug("Uploading dtd files finished");
    }

    @ActionHandler(action = XmlDtdConstant.VALIDATE_NAME, formClass = "")
    public void validateName(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        // PrintWriter out = response.getWriter();
        ServletOutputStream out = response.getOutputStream();

        try
        {
            String name = request.getParameter("name");
            String id = request.getParameter("id");
            ResourceBundle bundle = getBundle(request.getSession(false));

            if (name != null && name.length() > 0)
            {
                XmlDtdImpl xmlDtd = XmlDtdManager.getXmlDtdByName(name);
                if (xmlDtd != null
                        && (id == null || Long.parseLong(id) != xmlDtd.getId()))
                {
                    out.write(bundle.getString("lb_xml_dtd_name_exists")
                            .getBytes("UTF-8"));
                }
            }
            else
            {
                out.write(bundle.getString("lb_xml_dtd_name_null").getBytes(
                        "UTF-8"));
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw e;
        }
        finally
        {
            out.close();
            pageReturn();
        }
    }

    @ActionHandler(action = XmlDtdConstant.VIEW, formClass = "")
    public void view(HttpServletRequest request, HttpServletResponse response,
            Object form) throws Exception
    {
        String fileName = request.getParameter("fileName");
        if (fileName == null)
        {
            logger.error("file name can not be null");
            throw new IllegalStateException("file name can not be null");
        }

        logger.debug("Viewing dtd file " + fileName + " ...");

        File file = getFile(fileName);
        if (file != null)
        {
            response.setContentType("text/plain");
            String attachment = "attachment; filename=\"" + file.getName()
                    + "\";";
            response.setHeader("Content-Disposition", attachment);
            if (request.isSecure())
            {
                PageHandler.setHeaderForHTTPSDownload(response);
            }
            else
            {
                response.setHeader("Cache-Control", "no-cache");
            }
            response.setContentLength((int) file.length());

            byte[] buf = new byte[BUFSIZE];
            int readLen = 0;

            BufferedInputStream in = new BufferedInputStream(
                    new FileInputStream(file));
            OutputStream out = response.getOutputStream();
            while ((readLen = in.read(buf, 0, BUFSIZE)) != -1)
            {
                out.write(buf, 0, readLen);
            }
            in.close();
            out.flush();
        }

        logger.debug("Viewing dtd file " + fileName + " finished");

        pageReturn();
    }

    @ActionHandler(action = XmlDtdConstant.REMOVE, formClass = "")
    public void remove(HttpServletRequest request,
            HttpServletResponse response, Object form)
    {
        logger.debug("Removing dtd files...");

        String[] names = request.getParameterValues("selectFileNames");

        for (String name : names)
        {
            File file = getFile(name);
            if (file != null)
            {
                file.delete();
            }
        }

        logger.debug("Removing dtd files finished");
    }

    private File getFile(String name)
    {
        return DtdFileManager.getFile(dtdId.get(), name);
    }

    @ActionHandler(action = XmlDtdConstant.NEW, formClass = "")
    public void newDtd(HttpServletRequest request,
            HttpServletResponse response, Object form)
    {
        logger.debug("Create new xml dtd.");
        dtdId.set(null);
    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        dataForTable(request);
        if (dtdId.get() != null)
        {
            request.setAttribute(XmlDtdConstant.DTD,
                    HibernateUtil.get(XmlDtdImpl.class, dtdId.get()));
        }
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        dtdId.set(null);

        String id = request.getParameter("id");
        if (id != null)
        {
            XmlDtdImpl xmlDtd = HibernateUtil.get(XmlDtdImpl.class,
                    Long.parseLong(id));
            if (xmlDtd != null)
            {
                String currentId = CompanyThreadLocal.getInstance().getValue();
                if (CompanyWrapper.SUPER_COMPANY_ID.equals(currentId)
                        || String.valueOf(xmlDtd.getCompanyId()).equals(
                                currentId))
                {
                    dtdId.set(Long.parseLong(id));
                }
            }
        }
    }

    /**
     * Get list of all rules.
     */
    private void dataForTable(HttpServletRequest request)
            throws GeneralException
    {
        HttpSession session = request.getSession(false);
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

        setTableNavigation(request, session, getAllDtdFiles(),
                new XmlDtdFileComparator(uiLocale), 5,
                XmlDtdConstant.XMLDTD_File_LIST, XmlDtdConstant.XMLDTDFILE_KEY);
    }

    private List<File> getAllDtdFiles()
    {
        List<File> files;
        if (dtdId.get() != null)
        {
            files = DtdFileManager.getAllFiles(dtdId.get());
        }
        else
        {
            files = new ArrayList<File>();
        }

        return files;
    }
}
