package com.globalsight.everest.webapp.pagehandler.administration.createJobs;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.Condition;
import com.globalsight.cxe.entity.customAttribute.DateCondition;
import com.globalsight.cxe.entity.customAttribute.FloatCondition;
import com.globalsight.cxe.entity.customAttribute.IntCondition;
import com.globalsight.cxe.entity.customAttribute.TextCondition;
import com.globalsight.cxe.entity.filterconfiguration.ValidateException;
import com.globalsight.everest.permission.PermissionException;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.Assert;

public class SetAttributeHandler extends PageHandler
{

    private static final Logger logger = Logger
            .getLogger(SetAttributeHandler.class);
    
    public void invokePageHandler(WebPageDescriptor pageDescriptor,
            HttpServletRequest request, HttpServletResponse response,
            ServletContext context) throws ServletException, IOException,
            EnvoyServletException, PermissionException
    {
        String action = request.getParameter("action") == null ? "" : request
                .getParameter("action");
        long attributeId = Long.parseLong(request.getParameter("attributeId"));
        String textValue = request.getParameter("value");
        Attribute attribute = HibernateUtil.get(Attribute.class, attributeId);
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        if (action.equals("editText"))
        {
            Condition condition = attribute.getCondition();
            if (condition != null
                    && Attribute.TYPE_TEXT.equals(condition.getType()))
            {
                TextCondition textCondition = (TextCondition) condition;
                Integer maxLength = textCondition.getLength();
                if (maxLength != null && textValue.length() > maxLength)
                {
                    writer.write("The input text is larger than " + maxLength
                            + ", please correct it.");
                }
            }
        }
        else if (action.equals("editFloat")) 
        {
            try
            {
                Assert.assertIsFloat(textValue);
                Float floatValue = Float.parseFloat(textValue);
                Condition condition = attribute.getCondition();
                if (condition != null
                        && Attribute.TYPE_FLOAT.equals(condition.getType()))
                {
                    FloatCondition floatCondition = (FloatCondition) condition;
                    Float max = floatCondition.getMax();
                    Float min = floatCondition.getMin();
                    if (max != null && floatValue > max)
                    {
                        writer.write("The input number " + textValue + " is bigger than " + max
                                + ", please correct it.");
                    }
                    else if (min != null && floatValue < min)
                    {
                        writer.write("The input number " + textValue + " is less than " + min
                                + ", please correct it.");
                    }
                }
            }
            catch (ValidateException e)
            {
                writer.write(e.getMessage());
            }
        }
        else if (action.equals("editInt")) 
        {
            try
            {
                Assert.assertIsInteger(textValue);
                Integer intValue = Integer.parseInt(textValue);
                Condition condition = attribute.getCondition();
                if (condition != null
                        && Attribute.TYPE_INTEGER.equals(condition.getType()))
                {
                    IntCondition intCondition = (IntCondition) condition;
                    Integer max = intCondition.getMax();
                    Integer min = intCondition.getMin();
                    if (max != null && intValue > max)
                    {
                        writer.write("The input number " + textValue + " is bigger than " + max
                                + ", please correct it.");
                    }
                    else if (min != null && intValue < min)
                    {
                        writer.write("The input number " + textValue + " is less than " + min
                                + ", please correct it.");
                    }
                }
            }
            catch (ValidateException e)
            {
                writer.write(e.getMessage());
            }
        }
        else if (action.equals("editDate")) 
        {
            try
            {
                Assert.assertIsDate(textValue, DateCondition.FORMAT);
            }
            catch (ValidateException e)
            {
                writer.write(e.getMessage());
            }
        }
        
        writer.close();
        return;
    }
}
