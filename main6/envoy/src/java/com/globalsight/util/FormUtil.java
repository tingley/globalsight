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

package com.globalsight.util;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;

public class FormUtil
{
    public static void addSubmitToken(HttpServletRequest request,
            String formName)
    {
        String tokenName = getTokenName(formName);
        String submitToken = generateToken();
        request.getSession().setAttribute(tokenName, submitToken);
        request.setAttribute(tokenName, submitToken);
    }

    /**
     * Verifies if the request is a duplicate submission.
     * Use <code>addSubmitToken</code> in the last request.
     * And use <code>removeSubmitToken</code> after this method returns true.
     * @param request
     * @param formName
     * @return true if the submit is not a duplicate one.
     */
    public static boolean isDuplicateSubmisson(HttpServletRequest request,
            String formName)
    {
        return !isNotDuplicateSubmisson(request, formName);
    }

    /**
     * Verifies if the request is not a duplicate submission.
     * To use this method:<br>
     * Use <code>addSubmitToken</code> in the last request.
     * And use <code>removeSubmitToken</code> after this method returns true.
     * <br><br>
     * Note: You can only use this method for one time for one submission.
     * @param request
     * @param formName
     * @return true if the submit is not a duplicate one.
     */
    public static boolean isNotDuplicateSubmisson(HttpServletRequest request,
            String formName)
    {
        String tokenName = getTokenName(formName);
        Object token1 = request.getSession().getAttribute(tokenName);
        Object token2 = request.getParameter(tokenName);
        boolean notDuplicate = token1 != null && token1.equals(token2);
        if (notDuplicate)
        {
            removeSubmitToken(request, formName);
        }
        return notDuplicate;
    }

    public static void removeSubmitToken(HttpServletRequest request,
            String formName)
    {
        request.getSession().removeAttribute(getTokenName(formName));
    }

    private static String generateToken()
    {
        String token = "";
        token += System.nanoTime();
        token += Math.abs(new Random(System.currentTimeMillis()).nextLong());
        return token;
    }

    public static String getTokenName(String formName)
    {
        return "token.form." + formName;
    }

    public static final class Forms
    {
        public static final String NEW_ACTIVITY_TYPE = "newActivityType";
        public static final String NEW_RATE = "newRate";
        public static final String NEW_PERMISSION_GROUP = "newPermissionGroup";
        public static final String NEW_TRANSLATION_MEMORY = "newTranslationMemory";
        public static final String NEW_JOB_GROUP = "newJobGroup";
        public static final String NEW_PROJECT = "newProject";
        public static final String NEW_LOCALIZATION_PROFILE = "newLocalizationProfile";
        public static final String EDIT_LOCALIZATION_PROFILE = "editLocalizationProfile";
        public static final String NEW_FILE_PROFILE = "newFileProfile";
        public static final String NEW_FILE_EXTENSION = "newFileExtension";
        public static final String NEW_XML_RULE = "newXmlRule";
        public static final String EDIT_COMMENT = "editComment";
        public static final String NEW_ATTRIBUTE = "newAttribute";
        public static final String NEW_ATTRIBUTE_GROUP = "newAttributeGroup";
        public static final String SKIP_ACTIVITIES = "skipActivities";
        public static final String LOGIN = "login";
    }
}
