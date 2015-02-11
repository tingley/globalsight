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
package com.globalsight.everest.page;

import java.rmi.RemoteException;

import com.globalsight.everest.util.system.RemoteServer;


/**
 * This class represents the remote implementation of the TemplateManager that
 * manages page templates and their modifications when content or snippets are
 * added or deleted.
 * Note that all of the methods of this class throw the following exceptions:
 * 1. TemplateException - For template related errors.
 * 2. RemoteException - For network related exception.
 */
public class TemplateManagerWLRMIImpl
    extends RemoteServer
    implements TemplateManagerWLRemote
{

    // passes all calls off to the local instance (serves as a proxy)
    TemplateManager m_localInstance = null;

    /**
     * Construct a remote Template Manager.
     *
     * @exception java.rmi.RemoteException Network related exception.
     */
    public TemplateManagerWLRMIImpl()
        throws RemoteException, TemplateException
    {
        super(TemplateManager.SERVICE_NAME);
        m_localInstance = new TemplateManagerLocal();
    }

    /**
     * @see TemplateManager.addSnippet(String, SourcePage,
     * GlobalSightLocale, int, Snippet)
     */
    public Page addSnippet(String p_user, long p_srcPageId,
        String p_locale, int p_position, String p_snippetName,
        String p_snippetLocale, long p_snippetId)
        throws TemplateException, RemoteException
    {
        return m_localInstance.addSnippet(p_user, p_srcPageId, p_locale,
            p_position, p_snippetName, p_snippetLocale, p_snippetId);
    }

    /**
     * @see TemplateManager.deleteSnippet(String, SourcePage,
     * GlobalSightLocale, int)
     */
    public Page deleteSnippet(String p_user, long p_srcPageId,
        String p_locale, int p_position)
        throws TemplateException, RemoteException
    {
        return m_localInstance.deleteSnippet(
            p_user, p_srcPageId, p_locale, p_position);
    }

    /**
     * @see TemplateManager.deleteContent(String, SourcePage,
     * GlobalSightLocale, int)
     */
    public Page deleteContent(String p_user, long p_srcPageId,
        String p_locale, int p_position)
        throws TemplateException, RemoteException
    {
        return m_localInstance.deleteContent(
            p_user, p_srcPageId, p_locale, p_position);
    }

    /**
     * @see TemplateManager.undeleteContent(String, SourcePage,
     * GlobalSightLocale, int)
     */
    public Page undeleteContent(String p_user, long p_srcPageId,
        String p_locale, int p_position)
        throws TemplateException, RemoteException
    {
        return m_localInstance.undeleteContent(p_user, p_srcPageId,
            p_locale, p_position);
    }
}

