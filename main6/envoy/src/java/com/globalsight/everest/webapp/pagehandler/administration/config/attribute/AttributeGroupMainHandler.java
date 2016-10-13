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
package com.globalsight.everest.webapp.pagehandler.administration.config.attribute;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.AttributeSet;
import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.util.comparator.AttributeSetComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.Assert;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GeneralException;

/**
 * XmldtdFilePageHandler, A page handler to produce the entry page (index.jsp)
 * for XmldtdFile management.
 */
public class AttributeGroupMainHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(AttributeGroupMainHandler.class);

    @ActionHandler(action = AttributeConstant.CANCEL, formClass = "")
    public void cancel(HttpServletRequest request,
            HttpServletResponse response, Object form)
    {

    }

    @ActionHandler(action = AttributeConstant.REMOVE, formClass = "")
    public void remove(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        String[] ids = request.getParameterValues("selectAttributeSetIds");
        for (String id : ids)
        {
            deleteAttributeGroup(id);
        }
    }

    @ActionHandler(action = AttributeConstant.SAVE, formClass = "com.globalsight.cxe.entity.customAttribute.AttributeSet", loadFromDb = true, formToken = FormUtil.Forms.NEW_ATTRIBUTE_GROUP)
    public void save(HttpServletRequest request, HttpServletResponse response,
            Object form) throws Exception
    {
        logger.debug("Saving attribute group...");

        clearSessionExceptTableInfo(request.getSession(false),
                AttributeConstant.ATTRIBUTE_GROUP_KEY);

        AttributeSet attributeSet = (AttributeSet) form;
        String id = Long.toString(attributeSet.getId());
        if (!AttributeManager.isExistAttributeGroupName(attributeSet.getName(),
                id))
        {
            String[] items = request.getParameterValues("allItems");
            attributeSet.clearAttributes();

            if (items != null)
            {
                for (String item : items)
                {
                    Attribute att = HibernateUtil.get(Attribute.class, Long
                            .valueOf(item));
                    if (att != null)
                    {
                        attributeSet.addAttribute(att);
                    }
                }
            }

            HibernateUtil.saveOrUpdate(attributeSet);
        }

        logger.debug("Saving attribute group finished.");
    }

    private void deleteAttributeGroup(String id)
    {
        Assert.assertNotEmpty(id, "id");

        long attId = Long.parseLong(id);
        AttributeSet attributeSet = HibernateUtil
                .get(AttributeSet.class, attId);
        if (attributeSet != null)
        {
            try
            {
                for (ProjectImpl project : attributeSet.getProjects())
                {
                    project.setAttributeSet(null);
                    HibernateUtil.saveOrUpdate(project);
                }
                HibernateUtil.delete(attributeSet);
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
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

        setTableNavigation(request, session, AttributeManager
                .getAllAttributeSets(), new AttributeSetComparator(uiLocale),
                10, AttributeConstant.ATTRIBUTE_GROUP_LIST,
                AttributeConstant.ATTRIBUTE_GROUP_KEY);
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
                AttributeConstant.ATTRIBUTE_GROUP_KEY);

    }
}
