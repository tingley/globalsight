package com.globalsight.everest.webapp.pagehandler.administration.category;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jodd.util.StringUtil;

import org.apache.log4j.Logger;

import com.globalsight.everest.category.CommonCategory;
import com.globalsight.everest.category.CategoryHelper;
import com.globalsight.everest.category.CategoryType;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServletUtil;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

public class CategoryMainHandler extends PageHandler
{
    private Logger logger = Logger.getLogger(CategoryMainHandler.class);

    public void invokePageHandler(WebPageDescriptor pageDescriptor, HttpServletRequest request,
            HttpServletResponse response, ServletContext context) throws ServletException,
            IOException, EnvoyServletException
    {
        HttpSession session = request.getSession();
        String action = ServletUtil.get(request, "action");
        if (ACTION_NEW.equals(action))
        {
            createCategory(request, session);
        }
        else if (ACTION_EDIT.equals(action))
        {
            updateCategory(request, session);
        }
        else if (ACTION_REMOVE.equals(action))
        {
            removeCategory(request, session);
        }
        dataForTable(request, session);
        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    private void dataForTable(HttpServletRequest request, HttpSession session)
    {
        List<CommonCategory> categories = CategoryHelper.getCategories(null, 2);
        setTableNavigation(request, session, categories, null, 20, "categories", "category");
    }

    private void removeCategory(HttpServletRequest request, HttpSession session)
    {
        try
        {
            String[] tmpids = request.getParameterValues("ckb");
            if (tmpids == null || tmpids.length == 0)
                return;
            ArrayList<String> ids = new ArrayList<String>();
            for (String id : ids)
            {
                if (StringUtil.isBlank(id))
                    continue;
                ids.add(id.trim());
            }
            CategoryHelper.removeCategory(ids);
        }
        catch (Exception e)
        {
            logger.error("Error find when removing categories.", e);
        }
    }

    private void updateCategory(HttpServletRequest request, HttpSession session)
    {
        CommonCategory category = getModel(request);
        category.setId(ServletUtil.getIntValue(request, "id", 0));
        CategoryHelper.updateCategory(category);
    }

    private void createCategory(HttpServletRequest request, HttpSession session)
    {
        CommonCategory category = getModel(request);
        CategoryHelper.addCategory(category);
    }

    private CommonCategory getModel(HttpServletRequest request)
    {
        CommonCategory category = new CommonCategory();
        category.setCompanyId(CompanyWrapper.getCurrentCompanyIdAsLong());
        category.setName(ServletUtil.get(request, "nameField"));
        category.setMemo(ServletUtil.get(request, "memoField", "", true));
        category.setType(ServletUtil.getIntValue(request, "typeField",
                CategoryType.SegmentComment.getValue()));
        category.setIsActive(true);
        return category;
    }
}
