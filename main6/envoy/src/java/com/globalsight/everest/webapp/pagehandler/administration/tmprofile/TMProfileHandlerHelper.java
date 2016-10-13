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
package com.globalsight.everest.webapp.pagehandler.administration.tmprofile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.util.Assert;

public class TMProfileHandlerHelper implements TMProfileConstants
{
    /**
     * Get the SessionManager object for this session.
     */
    static SessionManager getSessionManager(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(false);
        return (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
    }

    /**
     * Get a translation memory profile by the given id.
     */
    public static TranslationMemoryProfile getTMProfileById(long p_tmProfileId)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getProjectHandler().getTMProfileById(
                    p_tmProfileId, true);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }
    
    // first save the iflow template and then the workflow template info...
    static void saveTMProfile(TranslationMemoryProfile p_tmProfile)
            throws EnvoyServletException
    {
        try
        {
            if (p_tmProfile.getId() == -1)
            {
                ServerProxy.getProjectHandler().createTranslationMemoryProfile(
                        p_tmProfile);
            }
            else
            {
                ServerProxy.getProjectHandler().modifyTranslationMemoryProfile(
                        p_tmProfile);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get all translation memory profiles
     */
    public static List getAllTMProfiles() throws EnvoyServletException
    {
        try
        {
            return new ArrayList(ServerProxy.getProjectHandler()
                    .getAllTMProfiles());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    public static TranslationMemoryProfile getTMProfileByName(String name)
    {
        Assert.assertNotNull(name, "TM Profile name");

        TranslationMemoryProfile result = null;
        List allProfiles = getAllTMProfiles();

        for (int i = 0; i < allProfiles.size(); i++)
        {
            TranslationMemoryProfile profile = (TranslationMemoryProfile) allProfiles
                    .get(i);
            if (name.equals(profile.getName()))
            {
                result = profile;
                break;
            }
        }

        return result;
    }

    /**
     * Get all translation memory profiles
     */
    public static Collection getProjectTMs() throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getProjectHandler().getAllProjectTMs();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }
    
    /**
     * Checks the input password
     */
    public static boolean checkPassword(String p_pass)
    {
        if(p_pass == null)
        {
            return false;
        }
        
        Pattern pattern = Pattern.compile("\\*+");
        Matcher matcher = pattern.matcher(p_pass);
        return !matcher.matches();
    }

}
