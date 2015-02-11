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
package com.globalsight.everest.persistence.snippet;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * SnippetDescriptorModifier extends DescriptorModifier by providing amendment
 * methods unique to the Snippet object's DescriptorModifier.
 */
public class SnippetDescriptorModifier
{
    private static final String SNIPPET_NAME_ARG = "snippetName";
    private static final String SNIPPET_LOCALE_ID_ARG = "snippetLocaleIdArg";

    protected static final String ID_ARG = "id";
    protected static final String ID_ATTR = PersistentObject.M_ID;
    protected static final String NAME_ARG = "name";
    protected static final String NAME_ATTR = PersistentObject.M_NAME;
    protected static final String LOCALE_ARG = "locale";
    protected static final String LOCALE_ATTR = "m_locale";

    // query strings

    // retrieves all the snippets with a specific name and a specific locale
    // will also retrieve the generic snippet (same name, but no locale)
    public static final String SNIPPETS_BY_NAME_AND_LOCALE_SQL = "select * " +
    		"from snippet where name = :"
            + SNIPPET_NAME_ARG
            + " and (locale_id = :"
            + SNIPPET_LOCALE_ID_ARG
            + " or locale_id is null) order by id";

    // retrieve generic + locale-specific snippet names
    public static final String SNIPPETS_BY_LOCALE_SQL = "select * from snippet "
            + "where (locale_id = :"
            + SNIPPET_LOCALE_ID_ARG
            + " or locale_id is null) order by name, id";
}
