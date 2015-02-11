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
import com.globalsight.everest.util.comparator.DefinedAttributeComparator;
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
public class AttributeMainHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(AttributeMainHandler.class);

    @ActionHandler(action = AttributeConstant.SAVE, formClass = "com.globalsight.cxe.entity.customAttribute.Attribute", loadFromDb = true, formToken=FormUtil.Forms.NEW_ATTRIBUTE)
    public void save(HttpServletRequest request, HttpServletResponse response,
            Object form) throws Exception
    {
        logger.debug("Saving attribute...");

        clearSessionExceptTableInfo(request.getSession(false),
                AttributeConstant.ATTRIBUTE_DEFINE_KEY);

        Attribute attribute = (Attribute) form;
        if (!AttributeManager.isExistAttributeName(attribute.getName(), Long
                .toString(attribute.getId())))
        {
            attribute.setEditable(getCheckBoxParameter(request, "editable"));
            attribute.setRequired(getCheckBoxParameter(request, "required"));
            attribute.setVisible(getCheckBoxParameter(request, "visible"));
            ConditionManager.updateCondition(request, attribute);

            HibernateUtil.saveOrUpdate(attribute);
        }

        logger.debug("Saving attribute finished.");
    }

    @ActionHandler(action = AttributeConstant.REMOVE, formClass = "")
    public void remove(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        String[] ids = request.getParameterValues("selectAttributeIds");
        for (String id : ids)
        {
            deleteAttribute(id);
        }
    }

    private void deleteAttribute(String id)
    {
        Assert.assertNotEmpty(id, "id");

        long attId = Long.parseLong(id);
        Attribute attribute = HibernateUtil.get(Attribute.class, attId);
        if (attribute != null)
        {
            try
            {
                HibernateUtil.delete(attribute);
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
                .getAllAttributes(), new DefinedAttributeComparator(uiLocale),
                10, AttributeConstant.ATTRIBUTE_DEFINE_LIST,
                AttributeConstant.ATTRIBUTE_DEFINE_KEY);
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
                AttributeConstant.ATTRIBUTE_DEFINE_KEY);

    }
}
