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

package com.globalsight.terminology.termleverager.recognizer;

import java.util.ResourceBundle;
import java.util.Locale;

/**
 * Factory class that constructs TermRecognizerRules depending on the locale.
 */
final class TermRecognizerRulesFactory
{

    private static final String TERM_RECOGNIZER_RULE_FILE_NAME =
        "com.globalsight.terminology.termleverager.recognizer.TermRecognizerRules";

    /**
     * Create rules for p_locale.
     *
     * NOTE: In the future we may want to cache these.
     */
    static public final TermRecognizerRules makeTermRecognizerRules(Locale p_locale)
    {
        ResourceBundle resource;
        TermRecognizerRules rules = null;

        // read in the parameters for this locale
        resource = ResourceBundle.getBundle(TERM_RECOGNIZER_RULE_FILE_NAME, p_locale);

        rules = new TermRecognizerRules(p_locale, resource);

        return rules;
    }
}
