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

package com.globalsight.everest.snippet;

// globalsight
import com.globalsight.util.GlobalSightLocale;

/**
 * A snippet is considered a named fragment of source text (HTML or XML).
 * There is only one single global namespace.
 * Initially, a snippet is considered language-independent ("generic snippet"). 
 * When a snippet is added to a page in a target locale, a copy is made for 
 * that target locale; the segment content of this copy can be modified to fit the
 * actual context.  Since the same"named" snippet can be added several times to 
 * the same page, the snippet also receives an id number.  Thus the primary key
 * of the snippet is [name, locale, id number] - with [name, null/"all", 0] 
 * identifying the generic snippet.
 */
public interface Snippet
{
    /**
     * @return The id of this snippet.
     */
    long getId();

    /**
     * @return The id of this snippet as a Long object.
     */
    Long getIdAsLong();

    /**
     * @return The name that has been given to the snippet.
     */
    String getName();

    /**
     * @return The description that has been given to this snippet
     *         or an empty string if one wasn't specified.
     */
    String getDescription();

    /**
     * Set the description for this particular snippet.
     * A locale specific snippet will initially inherit its description
     * from the generic snippet it was copied from.
     */
    void setDescription(String p_desc);

    /**
     * @return The locale that this snippet is associated with or NULL
     *         if it is a generic snippet that can be associated with any locale.
     */
    GlobalSightLocale getLocale();

    /**
     * @return The content of this snippet.
     */
    String getContent();

    /**
     * @return Set the content of the snippet or override what ever it used to be.
     */
    void setContent(String p_content);

    /**
     * @return 'true' - if the snippet is one that can be associated with any locale.
     *         'false' - if the snippet is associated with a particular locale.
     */
    boolean isGeneric();
}
