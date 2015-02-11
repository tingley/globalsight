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
import java.util.ResourceBundle;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.filterconfiguration.JsonUtil;
import com.globalsight.cxe.entity.filterconfiguration.ValidateException;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.Assert;
import com.globalsight.util.FormUtil;

public class AttributeBasicHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(AttributeBasicHandler.class);

    private void validateName(String name, String id, ResourceBundle bundle)
    {
        if (name != null)
        {
            name = name.trim();
            Assert.assertNotEmpty(name, bundle.getString("lb_internal_name"));

            boolean isExist = AttributeManager.isExistAttributeName(name, id);
            String msg = bundle.getString("msg_validate_internal_name_exist");
            Assert.assertFalse(isExist, MessageFormat.format(msg, name));

            Attribute att = null;
            if (id != null)
            {
                att = HibernateUtil.get(Attribute.class, Long
                        .parseLong(id));
            }
            
            if (att == null)
            {
                String errormsg = bundle
                        .getString("msg_validate_attribute_group_name_protect");
                Assert.assertFalse(name
                        .startsWith(Attribute.PROTECT_NAME_PREFIX), errormsg);
            }
            else
            {
                String attName = att.getName();
                if (attName.startsWith(Attribute.PROTECT_NAME_PREFIX)
                        && !attName.equals(name))
                {
                    String errormsg = bundle
                            .getString("msg_validate_internal_name_changed");
                    Assert.assertTrue(attName.equals(name), errormsg);
                }
                else
                {
                    String errormsg = bundle
                            .getString("msg_validate_attribute_group_name_protect");
                    Assert.assertFalse(name
                            .startsWith(Attribute.PROTECT_NAME_PREFIX),
                            errormsg);
                }
            }
        }
    }

    private void validateDisplayName(String displayName, String id,
            ResourceBundle bundle)
    {
        if (displayName != null)
        {
            displayName = displayName.trim();
            Assert.assertNotEmpty(displayName, bundle
                    .getString("lb_display_name"));

            Attribute attribute = AttributeManager
                    .getAttributeByDisplayName(displayName);
            boolean isExist = attribute != null
                    && (id == null || Long.parseLong(id) != attribute.getId());
            String msg = bundle.getString("msg_validate_display_name_exist");
            Assert.assertFalse(isExist, MessageFormat.format(msg, displayName));
        }
    }

    private void validateInteger(String number, ResourceBundle bundle)
    {
        if (number != null)
        {
            number = number.trim();
            if (number.length() > 0)
            {
                Assert.assertIsInteger(number);
            }
        }
    }
    
    private void validateFloat(String number, ResourceBundle bundle)
    {
        if (number != null)
        {
            number = number.trim();
            if (number.length() > 0)
            {
                Assert.assertIsFloat(number);
            }
        }
    }

    @ActionHandler(action = AttributeConstant.NEW, formClass = "")
    public void newAttribute(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        FormUtil.addSubmitToken(request, FormUtil.Forms.NEW_ATTRIBUTE);
    }

    @ActionHandler(action = AttributeConstant.EDIT, formClass = "")
    public void editAttribute(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        FormUtil.addSubmitToken(request, FormUtil.Forms.NEW_ATTRIBUTE);
    }

    private void validateTextLength(String length, ResourceBundle bundle)
    {
        if (length != null)
        {
            length = length.trim();
            if (length.length() > 0)
            {
                Assert.assertIsInteger(length);
                int n = Integer.parseInt(length);
                Assert.assertTrue(n > 0, bundle.getString("msg_validate_text_length_small"));
            }
        }
    }
    
    private void compaireInt(String max, String min, ResourceBundle bundle)
    {
        if (max != null && max.length() > 0 && min != null && min.length() > 0)
        {
            String msg = bundle.getString("msg_validate_max_small");
            Assert.assertFalse(Integer.parseInt(max) < Integer.parseInt(min), msg);
        }
    }
    
    private void compaireFloat(String max, String min, ResourceBundle bundle)
    {
        if (max != null && max.length() > 0 && min != null && min.length() > 0)
        {
            String msg = bundle.getString("msg_validate_max_small");
            Assert.assertFalse(Float.parseFloat(max) < Float.parseFloat(min), msg);
        }
    }
    
    @ActionHandler(action = AttributeConstant.VALIDATE, formClass = "com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeBasicForm")
    public void validate(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        ServletOutputStream out = response.getOutputStream();
        AttributeBasicForm date = (AttributeBasicForm) form;
        try
        {
            String id = date.getId();
            ResourceBundle bundle = getBundle(request.getSession(false));
            validateName(date.getName(), id, bundle);
            validateDisplayName(date.getDisplayName(), id, bundle);
            validateTextLength(date.getTextLength(), bundle);
            validateInteger(date.getIntMin(), bundle);
            validateInteger(date.getIntMax(), bundle);
            compaireInt(date.getIntMax(), date.getIntMin(), bundle);
            validateFloat(date.getFloatMin(), bundle);
            validateFloat(date.getFloatMax(), bundle);
            compaireFloat(date.getFloatMax(), date.getFloatMin(), bundle);
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
        }
        finally
        {
            out.close();
            pageReturn();
        }
    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {

    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        String id = request.getParameter("id");
        if (id != null)
        {
            Attribute attribute = HibernateUtil.get(Attribute.class, Long
                    .parseLong(id));
            request.setAttribute(AttributeConstant.ATTRIBUTE, attribute);
        }
    }
}
