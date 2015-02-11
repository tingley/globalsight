/**
 *  Copyright 2013 Welocalize, Inc. 
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
package com.globalsight.dispatcher.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.globalsight.dispatcher.bo.GlobalSightLocale;
import com.globalsight.dispatcher.dao.CommonDAO;
import com.globalsight.dispatcher.dao.DispatcherDAOFactory;

/**
 * Dispatcher Controller for 'Test' Pages
 * 
 * @author Joey
 *
 */
@Controller
@RequestMapping("/onlineTest")
public class OnlineTestController {
    
    @RequestMapping(value = "/main", method = RequestMethod.GET)
    public String listAllMTProfiles(HttpServletRequest p_request, ModelMap p_model)
    {
        StringBuffer basicURl = p_request.getRequestURL();
        Map<GlobalSightLocale, Set<GlobalSightLocale>> pairs = 
                DispatcherDAOFactory.getMTPLanguagesDAO().getSupportedLocalePairs();
        List<GlobalSightLocale> srcLocales = new ArrayList<GlobalSightLocale>(pairs.keySet());
        Collections.sort(srcLocales);
        
        p_model.put("basicURL", basicURl.substring(0, basicURl.indexOf("/onlineTest")));
        p_model.put("allGlobalSightLocale", CommonDAO.getAllGlobalSightLocale());
        p_model.put("srcLocales", srcLocales);
        
        
        return "onlineTestMain";
    }

    @RequestMapping(value = "/getTrgLocales", method = RequestMethod.GET)
    public String getTargetLocales(HttpServletRequest p_request, ModelMap p_model)
    {
        String srcLocaleShortName = (String) p_request.getParameter("srcLocale");
        GlobalSightLocale srcGL = CommonDAO.getGlobalSightLocaleByShortName(srcLocaleShortName);
        Map<GlobalSightLocale, Set<GlobalSightLocale>> pairs = 
                DispatcherDAOFactory.getMTPLanguagesDAO().getSupportedLocalePairs();
        List<GlobalSightLocale> trgGLS = new ArrayList<GlobalSightLocale>(pairs.get(srcGL));
        Collections.sort(trgGLS);
        
        p_model.put("trgLocales", trgGLS);        
        
        return "onlineTestMain";
    }
    
}