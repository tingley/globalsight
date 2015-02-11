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
package com.globalsight.cxe.adapter.mediasurface;
import com.mediasurface.client.*;
import com.mediasurface.datatypes.*;
import com.mediasurface.general.*;
import java.util.Hashtable;
import java.util.Locale;

/**
 * Utility class representing Mediasurface
 * connections.
 */
public class MediasurfaceConnection
{
    /** A Hashtable to keep initialized content server connections*/
    private static Hashtable s_contentServerCache = new Hashtable();

    /** A Hashtable to keep mediasurface locale mappings */
    private static Hashtable s_locales = new Hashtable();

    /**
     * Gets an initialized Mediasurface connection
     * object for the given ContentServer
     * 
     * @param p_contentServer
     *               contains the content server info needed
     * @return Mediasurface
     * @exception ConnectionException
     * @exception InitException
     */
    public static Mediasurface getConnection(ContentServer p_contentServer)
    throws ConnectionException,InitException
    {
        String contentServerId = p_contentServer.toString();
        Mediasurface m = (Mediasurface) s_contentServerCache.get(contentServerId);
        if (m == null)
        {
            m = new Mediasurface();
            m.init(p_contentServer.url,
                   p_contentServer.name,
                   p_contentServer.port);
            s_contentServerCache.put(contentServerId, m);
        }
        return m;
    }

    /**
     * Takes a Java locale and returns the appropriate
     * Mediasurface locale. Can return null if the locale
     * does not exist in Mediasurface.
     * 
     * @param p_locale locale
     * @return ILocale
     */
    public static ILocale getMediasurfaceLocale(Locale p_locale,
                                                Mediasurface p_mediasurface,
                                                SecurityContextHandle p_sch)
    throws com.mediasurface.general.ResourceException,
        com.mediasurface.general.AuthorizationException
    {
        //first check the hashtable
        ILocale loc = (ILocale) s_locales.get(p_locale);
        if (loc == null)
        {
            ILocale locales[] = p_mediasurface.getLocales(p_sch);
            for (int i=0; i < locales.length; i++)
            {
                loc = (ILocale) locales[i];
                String countryCode = p_locale.getCountry();
                String langCode = p_locale.getLanguage();
                if (loc.getIso3166CountryCode().equals(countryCode) &&
                    loc.getIso639LanguageCode().equals(langCode))
                {
                    s_locales.put(p_locale,loc);
                    break;
                }
            }
        }
        return loc;
    }
}

