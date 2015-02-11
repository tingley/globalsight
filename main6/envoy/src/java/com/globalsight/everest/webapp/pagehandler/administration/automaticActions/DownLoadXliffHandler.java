package com.globalsight.everest.webapp.pagehandler.administration.automaticActions;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.util.system.SystemConfiguration;

public class DownLoadXliffHandler extends PageHandler{
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context)
            throws ServletException, IOException, EnvoyServletException
        {
        String DOCROOT = "/";
        SystemConfiguration sc = SystemConfiguration.getInstance();

        String root = sc.getStringParameter(
            SystemConfiguration.WEB_SERVER_DOC_ROOT);

        if (!(root.endsWith("/") || root.endsWith("\\"))) {
            root = root + "/";
        }

        DOCROOT = root + "_Exports_";
        
        BufferedInputStream br = null;   
        OutputStream ut = null;  
        String fileName = p_request.getParameter("fileName");
        String companyName = p_request.getParameter("companyName");
        String filePath = DOCROOT + "/" + companyName + "/" + fileName;   

        File file = new File(filePath);   
        byte[] buf = new byte[1024];   
        int len = 0;   

        p_response.reset();
        p_response.setContentType("application/x-msdownload");   
        p_response.setHeader("Content-Disposition","attachment; filename=" + fileName);   

        if (p_request.isSecure())
        {
            setHeaderForHTTPSDownload(p_response);
        }
        
        br = new BufferedInputStream(new FileInputStream(file));   
        ut = p_response.getOutputStream();  
        
        try {
            while((len = br.read(buf))!=-1){
                ut.write(buf, 0, len);   
            }
            
        } catch (IOException e) {
            ut.close();
            e.printStackTrace();   
        }  
            
        ut.close();
    }
}

