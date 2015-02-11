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


/**
 * Specifies the names of all the named queries for Snippets.
 */
public interface SnippetQueryNames
{

    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //

    /**
     * A named query to return all snippets with a particular name.
     * <p>
     * Arguments: 1 - The name of the snippet(s).
     */
    public static String SNIPPETS_BY_NAME = "getSnippetsByName";

    /**
     * A named query to return a generic snippet by its name.
     * <p>
     * Arguments: 1 - The name of the generic snippet.
     */
    public static String GENERIC_SNIPPET_BY_NAME = "getGenericSnippetByName";

    /**
     * A named query to return all snippets with a particular name and locale.
     * <p>
     * Arguments: 1 - The name of the snippet(s).
     *            2 - The id of the GlobalSightLocale the snippet is specified in.
     */
    public static String SNIPPETS_BY_NAME_AND_LOCALE = "getSnippetsByNameAndLocale";

    /**
     * A named query to return a particular snippet.
     * <p>
     * Arguments: 1 - The name of the snippet.
     *            2 - The id of the GlobalSightLocale the snippet is specified in.
     *            3 - The id of the snippet.
     */
    public static String SNIPPET_BY_KEY = "getSnippet";

    /**
     * A named query to return all snippets.
     */
    public static String ALL_SNIPPETS = "getAllSnippets";

    /**
     * A named query to return all generic snippet names.
     */
    public static String GENERIC_SNIPPET_NAMES = "getGenericSnippetNames";

    /**
     * A named query to return all snippet names that can be used in a
     * locale (so includes generic snippet names).
     */
    public static String SNIPPETS_BY_LOCALE = "getSnippetsByLocale";
}
