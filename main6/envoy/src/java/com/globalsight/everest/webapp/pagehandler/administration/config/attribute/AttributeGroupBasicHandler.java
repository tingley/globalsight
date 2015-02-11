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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.AttributeSet;
import com.globalsight.cxe.entity.filterconfiguration.JsonUtil;
import com.globalsight.cxe.entity.filterconfiguration.ValidateException;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.Assert;
import com.globalsight.util.FormUtil;
import com.globalsight.util.SortUtil;

public class AttributeGroupBasicHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(AttributeGroupBasicHandler.class);

    static public final int BUFSIZE = 4096;

    private void validateName(String name, String id, ResourceBundle bundle)
    {
        if (name != null)
        {
            name = name.trim();
            Assert.assertNotEmpty(name, bundle.getString("lb_name"));

            Assert.assertFalse(
                    name.startsWith(AttributeSet.PROTECT_NAME_PREFIX),
                    bundle.getString("msg_validate_attribute_name_protect"));

            boolean isExist = AttributeManager.isExistAttributeGroupName(name,
                    id);
            String msg = bundle.getString("msg_validate_name_exist");
            Assert.assertFalse(isExist, MessageFormat.format(msg, name));
        }
    }

    @ActionHandler(action = AttributeConstant.VALIDATE, formClass = "")
    public void validate(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        ServletOutputStream out = response.getOutputStream();

        try
        {
            ResourceBundle bundle = getBundle(request.getSession(false));
            validateName(request.getParameter("name"),
                    request.getParameter("id"), bundle);
        }
        catch (ValidateException ve)
        {
            ResourceBundle bundle = PageHandler.getBundle(request.getSession());
            String s = "({\"error\" : "
                    + JsonUtil.toJson(ve.getMessage(bundle)) + "})";
            out.write(s.getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            String s = "({\"error\" : " + JsonUtil.toObjectJson(e.getMessage())
                    + "})";
            out.write(s.getBytes("UTF-8"));
            logger.error(e.getMessage(), e);
        }
        finally
        {
            out.close();
            pageReturn();
        }
    }

    @ActionHandler(action = AttributeConstant.NEW, formClass = "")
    public void newAttribute(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        FormUtil.addSubmitToken(request, FormUtil.Forms.NEW_ATTRIBUTE_GROUP);
    }

    @ActionHandler(action = AttributeConstant.EDIT, formClass = "")
    public void editAttribute(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        FormUtil.addSubmitToken(request, FormUtil.Forms.NEW_ATTRIBUTE_GROUP);
    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {

    }

    public Comparator<Attribute> getComparator()
    {
        return new Comparator<Attribute>()
        {
            @Override
            public int compare(Attribute o1, Attribute o2)
            {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        };
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        List<Attribute> selectedAttributes = new ArrayList<Attribute>();

        String id = request.getParameter("id");
        if (id != null)
        {
            AttributeSet attributeSet = HibernateUtil.get(AttributeSet.class,
                    Long.parseLong(id));
            request.setAttribute(AttributeConstant.ATTRIBUTE_SET, attributeSet);
            selectedAttributes = attributeSet.getAttributeAsList();
        }

        List<Attribute> allAttributes = (List<Attribute>) AttributeManager
                .getAllAttributes();

        Comparator<Attribute> comparator = getComparator();
        SortUtil.sort(selectedAttributes, comparator);
        SortUtil.sort(allAttributes, comparator);

        request.setAttribute("selectedAttributes", selectedAttributes);
        request.setAttribute("allAttributes", allAttributes);
    }
}
