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
package com.globalsight.everest.util.comparator;

import java.util.Locale;

import com.globalsight.everest.glossaries.GlossaryFile;
import com.globalsight.everest.glossaries.GlossaryUpload;

/**
 * This class can be used to compare GlossaryFile objects
 */
public class GlossaryFileComparator extends StringComparator
{
    private static final long serialVersionUID = -7196747104191119958L;

    // types of comparison
    public static final int M_SOURCE_LOCALE = 1;
    public static final int M_TARGET_LOCALE = 2;
    public static final int M_CATEGORY = 3;
    public static final int M_FILENAME = 4;

    // Used for order by DESC or ASC
    private static boolean asc_source_locale = true;
    private static boolean asc_target_locale = true;
    private static boolean asc_fileName = true;

    /**
     * Creates a GlossaryFileComparator with the given type and locale. If the
     * type is not a valid type, then the default comparison is done by source
     * locale
     */
    public GlossaryFileComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    public GlossaryFileComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two GlossaryFile objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        GlossaryFile a = (GlossaryFile) p_A;
        GlossaryFile b = (GlossaryFile) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
            default:
            case M_SOURCE_LOCALE:
                aValue = a.isForAnySourceLocale() ? GlossaryUpload.KEY_ANY_SOURCE_LOCALE : a
                        .getSourceLocale().toString();
                bValue = b.isForAnySourceLocale() ? GlossaryUpload.KEY_ANY_SOURCE_LOCALE : b
                        .getSourceLocale().toString();

                if (asc_source_locale)
                {
                    rv = this.compareStrings(aValue, bValue);
                }
                else
                {
                    rv = this.compareStrings(bValue, aValue);
                }

                break;
            case M_TARGET_LOCALE:
                aValue = a.isForAnyTargetLocale() ? GlossaryUpload.KEY_ANY_TARGET_LOCALE : a
                        .getTargetLocale().toString();
                bValue = b.isForAnyTargetLocale() ? GlossaryUpload.KEY_ANY_TARGET_LOCALE : b
                        .getTargetLocale().toString();
                if (asc_target_locale)
                {
                    rv = this.compareStrings(aValue, bValue);
                }
                else
                {
                    rv = this.compareStrings(bValue, aValue);
                }
                break;
            case M_CATEGORY:
                aValue = a.getCategory();
                bValue = b.getCategory();
                rv = this.compareStrings(aValue, bValue);
                break;
            case M_FILENAME:
                aValue = a.getFilename();
                bValue = b.getFilename();
                if (asc_fileName)
                {
                    rv = this.compareStrings(aValue, bValue);
                }
                else
                {
                    rv = this.compareStrings(bValue, aValue);
                }
                break;
        }
        return rv;
    }

    public static boolean isAsc_source_locale()
    {
        return asc_source_locale;
    }

    public static void setAsc_source_locale(boolean ascSourceLocale)
    {
        asc_source_locale = ascSourceLocale;
    }

    public static boolean isAsc_target_locale()
    {
        return asc_target_locale;
    }

    public static void setAsc_target_locale(boolean ascTargetLocale)
    {
        asc_target_locale = ascTargetLocale;
    }

    public static boolean isAsc_fileName()
    {
        return asc_fileName;
    }

    public static void setAsc_fileName(boolean ascFileName)
    {
        asc_fileName = ascFileName;
    }

}
