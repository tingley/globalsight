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

package com.globalsight.diplomat.util;

import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.Locale;
import java.util.StringTokenizer;

import com.globalsight.ling.common.Transcoder;
import com.globalsight.ling.common.TranscoderException;

public class StringBundle
{
    private static StringBundle m_instance = null;
    
    // m_bundles represents a hash of hashes of hashes
    // i.e. hash of bundle names which contains hashes of locales
    private HashMap m_bundles = new HashMap();
    
    /////////////////////////////////////////////////
    private StringBundle()
    {       
    }
    
    /////////////////////////////////////////////////
    private String checkLocale(String p_locale)
    {
        // defaults to english
        if ((p_locale == null) || p_locale.equalsIgnoreCase("null") || p_locale.equalsIgnoreCase(""))
            p_locale = "en_US";
            
        return p_locale;
    }
    
    /////////////////////////////////////////////////
    public void setBundle(String p_bundleName, String p_locale)
    {
        p_locale = checkLocale(p_locale);
            
        if (! ( (m_bundles.containsKey(p_bundleName)) &&
            ((HashMap)m_bundles.get(p_bundleName)).containsKey(p_locale) ) ) 
        {
            HashMap bundle = new HashMap();
            
            StringTokenizer tokens = new StringTokenizer (p_locale, "_");
            Locale locale = new Locale(tokens.nextToken(), tokens.nextToken());
                        
            try
            {
                // retrieve the resource bundle
                ResourceBundle rscBundle = ResourceBundle.getBundle(p_bundleName, locale);
		        Enumeration keys = rscBundle.getKeys();
    		    
		        while ( keys.hasMoreElements() ) 
		        {
		            // retrieve a key
		            String k = (String) keys.nextElement();
                            String v = rscBundle.getString(k);

                            //we know the properties file is in UTF8, so convert it from ISOLatin1 to UTF8
                            try {
                               Transcoder t = new Transcoder();
                               k = t.toUnicode (k.getBytes("ISO8859_1"),"UTF8");
                               v = t.toUnicode (v.getBytes("ISO8859_1"),"UTF8");
                            }
                            catch (Exception te) {}

                            bundle.put(k, v);
		        }
		        
		        // add our hash to the main resource bundle hash
		        if (!m_bundles.containsKey(p_bundleName))
		            m_bundles.put(p_bundleName, new HashMap());
		        ((HashMap)m_bundles.get(p_bundleName)).put(p_locale, bundle);		        
		    }
		    catch (MissingResourceException e) {
		        e.printStackTrace();
		    }
        }
    }
    
    /////////////////////////////////////////////////
    public HashMap getBundle(String p_bundleName, String p_locale)
    {
        HashMap bundle = null;
        
        p_locale = checkLocale(p_locale);
        
        if ( m_bundles.containsKey(p_bundleName) )
            bundle = (HashMap)((HashMap)m_bundles.get(p_bundleName)).get(p_locale);    
        
        return bundle;
    }
    
    /////////////////////////////////////////////////
    public String getString(String p_bundleName, String p_locale, String p_key)
    {
        p_locale = checkLocale(p_locale);
        
        if ( (m_bundles.containsKey(p_bundleName)) &&
            ((HashMap)m_bundles.get(p_bundleName)).containsKey(p_locale) )
        {
            return ( (String)((HashMap)((HashMap)((HashMap)m_bundles.get(p_bundleName)).get(p_locale))).get(p_key) );        
        }
        else
            return null;
    }
    
    /////////////////////////////////////////////////
    public static StringBundle getInstance()
    {
        if (null == m_instance)
            m_instance = new StringBundle();
        return m_instance;
    }    
}

    
    
 
