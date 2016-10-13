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

import java.io.File;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.xmldtd.XmlDtdImpl;
import com.globalsight.everest.util.comparator.XmlDtdComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.Assert;
import com.globalsight.util.GeneralException;

/**
 * XmldtdFilePageHandler, A page handler to produce the entry page (index.jsp)
 * for XmldtdFile management.
 */
public class XmlDtdMainHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(XmlDtdMainHandler.class);

    @ActionHandler(action = XmlDtdConstant.CANCEL, formClass = "")
    public void cancel(HttpServletRequest request,
            HttpServletResponse response, Object form)
    {

    }

    @ActionHandler(action = XmlDtdConstant.REMOVE, formClass = "")
    public void remove(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        String[] ids = request.getParameterValues("selectXmlDtdIds");
        for (String id : ids)
        {
            deleteXmlDtd(id);
        }
    }
    
    private void deleteXmlDtd(String id)
    {
        Assert.assertNotEmpty(id, "id");
        
        long dtdId = Long.parseLong(id);
        XmlDtdImpl xmlDtd = HibernateUtil.get(XmlDtdImpl.class, dtdId);
        if (xmlDtd != null)
        {
            try
            {
                HibernateUtil.delete(xmlDtd);
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
            }
        }
        
        List<File> files = DtdFileManager.getAllFiles(dtdId);
        for (File file : files)
        {
            file.delete();
        }
        
        File root = new File(DtdFileManager.getStorePath(dtdId));
        if (root.exists())
        {
            root.delete();
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

        setTableNavigation(request, session, XmlDtdManager.getAllXmlDtd(),
                new XmlDtdComparator(uiLocale), 10, XmlDtdConstant.XMLDTD_LIST,
                XmlDtdConstant.XMLDTD_KEY);
    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        dataForTable(request);
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        clearSessionExceptTableInfo(request.getSession(false),
                XmlDtdConstant.XMLDTD_KEY);

    }
}
