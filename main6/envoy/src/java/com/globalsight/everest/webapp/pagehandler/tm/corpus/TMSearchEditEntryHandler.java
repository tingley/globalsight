package com.globalsight.everest.webapp.pagehandler.tm.corpus;

import java.io.PrintWriter;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;

public class TMSearchEditEntryHandler extends PageActionHandler
{
    @ActionHandler(action = TM_ACTION_EDIT_ENTRY, formClass = "")
    public void editEntry(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        HttpSession session = request.getSession(false);

        ResourceBundle bundle = PageHandler.getBundle(session);
        String userId = getUser(session).getUserId();

        // set label
        Locale uiLocale = (Locale) request.getSession().getAttribute(
                WebAppConstants.UILOCALE);
        request.setAttribute("userId", userId);
        request.setAttribute("uiLocale", uiLocale);
        TMSearchEditEntryHandlerHelper.setLable(request, bundle);
        // set entry info
        TMSearchEditEntryHandlerHelper.editEntries(request);
    }

    @ActionHandler(action = TM_ACTION_SAVE_ENTRY, formClass = "")
    public void saveEntry(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        HttpSession session = request.getSession(false);
        String userId = getUser(session).getUserId();
        String message = TMSearchEditEntryHandlerHelper.saveEntries(request,
                userId);

        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.write(message);
        if (out != null)
        {
            out.close();
        }
        pageReturn();
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        // TODO Auto-generated method stub

    }
}
