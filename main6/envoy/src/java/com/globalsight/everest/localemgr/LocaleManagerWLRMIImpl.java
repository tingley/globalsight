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
package com.globalsight.everest.localemgr;

import java.rmi.RemoteException;
import java.util.Vector;

import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.util.GlobalSightLocale;

public class LocaleManagerWLRMIImpl extends RemoteServer implements
        LocaleManagerWLRemote
{
    LocaleManager m_localReference;

    public LocaleManagerWLRMIImpl() throws java.rmi.RemoteException
    {
        super(LocaleManager.SERVICE_NAME);
        m_localReference = new LocaleManagerLocal();
    }

    public GlobalSightLocale addLocale(GlobalSightLocale p_locale)
            throws LocaleManagerException, java.rmi.RemoteException
    {
        return m_localReference.addLocale(p_locale);
    }

    public LocalePair addSourceTargetLocalePair(GlobalSightLocale param1,
            GlobalSightLocale param2, long companyId)
            throws LocaleManagerException, java.rmi.RemoteException
    {
		return m_localReference.addSourceTargetLocalePair(param1, param2,
				companyId);
    }

    public java.util.Vector getAllSourceLocalesByCompanyId(String p_companyId)
            throws LocaleManagerException, java.rmi.RemoteException
    {
        return m_localReference.getAllSourceLocalesByCompanyId(p_companyId);
    }

    public java.util.Vector getAllSourceLocales()
            throws LocaleManagerException, java.rmi.RemoteException
    {
        return m_localReference.getAllSourceLocales();
    }

    public java.util.Vector getAllTargetLocales()
            throws LocaleManagerException, java.rmi.RemoteException
    {
        return m_localReference.getAllTargetLocales();
    }

    public java.util.Vector getAvailableLocales()
            throws LocaleManagerException, java.rmi.RemoteException
    {
        return m_localReference.getAvailableLocales();
    }

    public java.util.Vector getSupportedLocalesForUi()
            throws LocaleManagerException, java.rmi.RemoteException
    {
        return m_localReference.getSupportedLocalesForUi();
    }

    public GlobalSightLocale getLocaleById(long p_id)
            throws LocaleManagerException, java.rmi.RemoteException
    {
        return m_localReference.getLocaleById(p_id);
    }

    public GlobalSightLocale getLocaleByString(String p_localeString)
            throws LocaleManagerException, java.rmi.RemoteException
    {
        return m_localReference.getLocaleByString(p_localeString);
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    public Vector getSourceTargetLocalePairs() throws LocaleManagerException,
            java.rmi.RemoteException
    {
        return m_localReference.getSourceTargetLocalePairs();
    }

    /**
     * @see LocaleManager.getLocalePairBySourceTargetStrings(String, String)
     */
    public LocalePair getLocalePairBySourceTargetStrings(
            String p_sourceLocaleString, String p_targetLocaleString)
            throws LocaleManagerException, java.rmi.RemoteException
    {
        return m_localReference.getLocalePairBySourceTargetStrings(
                p_sourceLocaleString, p_targetLocaleString);
    }

    /**
     * Get a source/target locales pair based on the given id.
     * 
     * @return A LocalePair object (the source/target locale pair)
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public LocalePair getLocalePairById(long p_localePairId)
            throws LocaleManagerException, java.rmi.RemoteException
    {
        return m_localReference.getLocalePairById(p_localePairId);
    }

    /**
     * Get a locale pair object based on the given source and target ids.
     * 
     * @return A LocalePair object (the source/target locale pair)
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public LocalePair getLocalePairBySourceTargetIds(long p_sourceLocaleId,
            long p_targetLocaleId) throws LocaleManagerException,
            java.rmi.RemoteException
    {
        return m_localReference.getLocalePairBySourceTargetIds(
                p_sourceLocaleId, p_targetLocaleId);
    }

    public Vector getTargetLocalesByCompanyId(GlobalSightLocale p_sourceLocale,
            String p_companyId) throws LocaleManagerException, RemoteException
    {
        return m_localReference.getTargetLocalesByCompanyId(p_sourceLocale,
                p_companyId);
    }

    public java.util.Vector getTargetLocales(
    /* java.util.Locale */GlobalSightLocale param1)
            throws LocaleManagerException, java.rmi.RemoteException
    {
        return m_localReference.getTargetLocales(param1);
    }

    public void removeSourceTargetLocalePair(
            com.globalsight.everest.foundation.LocalePair param1)
            throws LocaleManagerException, java.rmi.RemoteException
    {
        m_localReference.removeSourceTargetLocalePair(param1);
    }

    public java.util.Vector getAllCodeSets() throws LocaleManagerException,
            java.rmi.RemoteException
    {
        return m_localReference.getAllCodeSets();
    }

    public java.util.Vector getAllCodeSets(long p_localeId)
            throws LocaleManagerException, java.rmi.RemoteException
    {
        return m_localReference.getAllCodeSets(p_localeId);
    }

    @Override
    public Vector getTargetLocalesByProject(GlobalSightLocale locale,
            String p_project) throws LocaleManagerException, RemoteException
    {
        return m_localReference.getTargetLocalesByProject(locale, p_project);
    }

    public LocalePair getLocalePairBySourceTargetAndCompanyStrings(
            String p_sourceLocaleString, String p_targetLocaleString,
            long companyId) throws LocaleManagerException, RemoteException
    {
        return m_localReference.getLocalePairBySourceTargetAndCompanyStrings(
                p_sourceLocaleString, p_targetLocaleString, companyId);
    }
}
