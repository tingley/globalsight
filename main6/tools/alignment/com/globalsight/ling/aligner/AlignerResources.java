/*
Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
    
THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF 
GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.

THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
BY LAW.
*/

package com.globalsight.ling.aligner;

import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;

import com.globalsight.ling.aligner.AlignerException;
import com.globalsight.ling.aligner.AlignerExceptionConstants;

public class AlignerResources
{
    static private final String PROPERTY 
        = "com.globalsight.ling.aligner.Resources";
    static private Locale locale = null;
    static private ResourceBundle resources = null ;
    
    public static void setLocale(Locale newLocale)
        throws AlignerException
    {
        if(locale == null || !locale.equals(newLocale))
        {
            locale = newLocale;
            try
            {
                resources = PropertyResourceBundle.getBundle(PROPERTY, locale);
            }
            catch(MissingResourceException e)
            {
                throw new AlignerException
                    (AlignerExceptionConstants.PROPERTY_NOT_FOUND,
                     e.toString());
            }
        }
        
    }
    
    public static String getResource(String key)
        throws AlignerException
    {
        String value;
        
        if(resources == null)
        {
            setLocale(new Locale("en", "US"));
        }
        
        try
        {
            value = resources.getString(key);
        }
        catch(MissingResourceException e)
        {
            throw new AlignerException
                (AlignerExceptionConstants.PROPERTY_NOT_FOUND,
                 e.toString());
        }
        return value;
        
    }

}
