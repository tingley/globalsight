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

// globalsight
import java.rmi.RemoteException;

/**
 * This class manages a page's templates and template parts and implements
 * atomic additions and deletions of snippets and content in case of
 * concurrent modifications.
 *
 * Positions are positive integers (1..n) that indicate the occurence
 * of a GS tag in the template for a specific locale.
 */
public interface TemplateManager
{
    // the name used when binding a TemplateManager for remote lookup
    public static final String SERVICE_NAME = "TemplateManager";

    /**
     * Adds a Snippet to a page in the given Locale.  This is
     * implemented by writing the new template(part)s to the database,
     * then updating the source page's template collection atomically
     * (synchronized), and returning the modified source page.
     *
     * @param p_user: username for this operation for logging.
     * @param p_page: the (source or target) page to add the snippet to.
     * @param p_locale: the locale for which to add the snippet.
     * @param p_position: specifies at which position of multiple
     * positions for the same snippet this snippet should be inserted.
     * @param p_snippet: the snippet to add.
     *
     * @return The (source) Page's whose templates have been updated
     * with the newly added snippet.
     */
    public Page addSnippet(String p_user, long p_srcPageId,
        String p_locale, int p_position, String p_snippetName,
        String p_snippetLocale, long p_snippetId)
        throws TemplateException, RemoteException;

    /**
     * Deletes a Snippet from a page in the given target locale.
     *
     * @param p_user: username for this operation for logging.
     * @param p_page: the (source or target) page to add the snippet to.
     * @param p_locale: the locale for which to delete the snippet.
     * @param p_position: specifies at which position of multiple
     * positions for the same snippet this snippet should be inserted.
     *
     * @return The (source) Page's whose templates have been updated
     * with the deleted snippet.
     */
    public Page deleteSnippet(String p_user, long p_srcPageId, String
        p_locale, int p_position)
        throws TemplateException, RemoteException;

    /**
     * Deletes content (translatable/localizable) from a page
     * in the given target locale.
     *
     * @param p_user: username for this operation for logging.
     * @param p_page: the (source or target) page to add content to.
     * @param p_locale: the locale for which to delete content.
     * @param p_position: specifies at which position of multiple
     * positions the content should be deleted.
     *
     * @return The (source) Page's whose templates have been updated
     * with the deleted content.
     */
    public Page deleteContent(String p_user, long p_srcPageId,
        String p_locale, int p_position)
        throws TemplateException, RemoteException;

    /**
     * Un-deletes content (translatable/localizable) from a page
     * in the given target locale.
     *
     * @param p_user: username for this operation for logging.
     * @param p_page: the (source or target) page to work on.
     * @param p_locale: the locale for which to un-delete content.
     * @param p_position: specifies at which position of multiple
     * positions the content should be un-deleted.
     *
     * @return The (source) Page's whose templates have been updated
     * with the retstored/un-deleted content.
     */
    public Page undeleteContent(String p_user, long p_srcPageId,
        String p_locale, int p_position)
        throws TemplateException, RemoteException;
}

