package com.globalsight.everest.webapp.pagehandler.administration.createJobs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionException;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.jobAttribute.JobAttributeFileManager;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.SortUtil;

public class JobAttributeHandler extends PageHandler
{

    private static final Logger logger = Logger
            .getLogger(JobAttributeHandler.class);

    public void invokePageHandler(WebPageDescriptor pageDescriptor,
            HttpServletRequest request, HttpServletResponse response,
            ServletContext context) throws ServletException, IOException,
            EnvoyServletException, PermissionException
    {
        try
        {
            HttpSession session = request.getSession(false);
            PermissionSet userPerms = (PermissionSet) session
                    .getAttribute(WebAppConstants.PERMISSIONS);
            if (!userPerms.getPermissionFor(Permission.CREATE_JOB) 
            		&& !userPerms.getPermissionFor(Permission.CREATE_JOB_NO_APPLET) )
            {
                logger.error("User doesn't have the permission to see the page.");
                response.sendRedirect("/globalsight/ControlServlet?");
                return;
            }
            ResourceBundle bundle = PageHandler.getBundle(session);
            setLable(request, bundle);

            String l10Nid = request.getParameter("l10Nid");
            L10nProfile lp = ServerProxy.getProjectHandler().getL10nProfile(
                    Long.valueOf(l10Nid));
            Project p = lp.getProject();
            if (p.getAttributeSet() != null)
            {
                SessionManager sessionMgr = (SessionManager) request
                        .getSession().getAttribute(SESSION_MANAGER);
                String uuid = (String) sessionMgr.getAttribute("uuid");
                String root = "";
                if (uuid == null)
                {
                    uuid = JobImpl.createUuid();
                    sessionMgr.setAttribute("uuid", uuid);
                    root = JobAttributeFileManager.getStorePath(uuid);
                }

                List<AttributeExtension> jobAttributesList = new ArrayList<AttributeExtension>();
                List<Attribute> attsList = p.getAttributeSet()
                        .getAttributeAsList();
                for (Attribute att : attsList)
                {
                    AttributeExtension attributeExt = new AttributeExtension();
                    JobAttribute jobAttribute = new JobAttribute();
                    jobAttribute.setAttribute(att.getCloneAttribute());

                    attributeExt.setRoot(root);
                    attributeExt.setJobAttribute(jobAttribute);
                    attributeExt.setAttribute(att);
                    jobAttributesList.add(attributeExt);
                }
                SortUtil.sort(jobAttributesList, new Comparator<Object>()
                {

                    public int compare(Object o1, Object o2)
                    {
                        AttributeExtension tmp1 = (AttributeExtension) o1;
                        AttributeExtension tmp2 = (AttributeExtension) o2;
                        return tmp1
                                .getAttribute()
                                .getDisplayName()
                                .compareTo(tmp2.getAttribute().getDisplayName());
                    }

                });
                request.setAttribute("jobAttributesList", jobAttributesList);
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to get job attributes.", e);
        }
        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    private void setLable(HttpServletRequest request, ResourceBundle bundle)
    {
        setLableToJsp(request, bundle, "lb_job_attributes");
        setLableToJsp(request, bundle, "helper_text_set_attribute");
        setLableToJsp(request, bundle, "lb_name");
        setLableToJsp(request, bundle, "lb_type");
        setLableToJsp(request, bundle, "lb_required");
        setLableToJsp(request, bundle, "lb_values");
        setLableToJsp(request, bundle, "lb_attribute_type_text");
        setLableToJsp(request, bundle, "lb_attribute_type_integer");
        setLableToJsp(request, bundle, "lb_attribute_type_float");
        setLableToJsp(request, bundle, "lb_attribute_type_choiceList");
        setLableToJsp(request, bundle, "lb_attribute_type_file");
        setLableToJsp(request, bundle, "lb_attribute_type_date");
        setLableToJsp(request, bundle, "lb_yes");
        setLableToJsp(request, bundle, "lb_no");
        setLableToJsp(request, bundle, "lb_update_job_attributes");
        setLableToJsp(request, bundle, "lb_close");
        setLableToJsp(request, bundle, "lb_save");
        setLableToJsp(request, bundle, "lb_all_files");
        setLableToJsp(request, bundle, "lb_delete");
        setLableToJsp(request, bundle, "lb_file");
        setLableToJsp(request, bundle, "lb_upload");
    }

    /**
     * Set languages on the page according to locales
     * 
     * @param request
     * @param bundle
     */
    private void setLableToJsp(HttpServletRequest request,
            ResourceBundle bundle, String msg)
    {
        String label = bundle.getString(msg);
        request.setAttribute(msg, label);
    }
}
