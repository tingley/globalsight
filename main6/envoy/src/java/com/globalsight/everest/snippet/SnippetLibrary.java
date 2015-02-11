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

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.importer.IImportManager;
import com.globalsight.importer.ImporterException;

import java.util.ArrayList;
import java.util.Collection;

import java.rmi.RemoteException;


/**
 * This class holds the collection of Snippets.  It provides the API
 * for adding, modifying, removing and retrieving Snippets.  The
 * methods all persist the changes in the database or query from the
 * cache/database.
 */
public interface SnippetLibrary
{
    public static final String SERVICE_NAME = "SnippetLibrary";

    /**
     * Add the specified snippet to the library (and database).  If
     * the snippet is a generic snippet then the name must be unique.
     *
     * @param p_snippet: The snippet to add.
     * @param p_validate: Specifiy 'true' if the format of the snippet
     * should be validated and only the translatable text changed or
     * 'false' if no validation should be done.
     * @return The added snippet with its id set.
     *
     * @exception SnippetException - an error occured, possibly the
     * generice snippet with the specific name already exist.
     */
    Snippet addSnippet(String p_user, Snippet p_snippet, boolean p_validate)
        throws RemoteException, SnippetException;

    /**
     * Add the specified snippet to the library (and database).  If
     * the snippet is a generic snippet then the name must be unique.
     *
     * @param p_name: The name of the snippet to add.
     * @param p_desc: The description of the snippet to add.
     * @param p_locale: The locale of the snippet to add.  If 'null'
     * then a generic snippet.
     * @param p_version: The version of the snippet (unique id).
     * @param p_value: The actual content of the snippet
     * @param p_validate: Specifiy 'true' if the format of the snippet
     * should be validated and only the translatable text changed or
     * 'false' if no validation should be done.
     * @return The added snippet with its id set.
     *
     * @exception SnippetException - an error occured, possibly the
     * generice snippet with the specific name already exist.
     */
    Snippet addSnippet(String p_user, String p_name, String p_desc,
        String p_locale, String p_version, String p_value, boolean p_validate)
        throws RemoteException, SnippetException;

    /**
     * This creates a clone of the snippet passed in with
     * a new id and persists it in the database.
     *
     * @param p_snippet The snippet to make a clone of.
     * @return The new snippet that is a clone of the one passed in.
     */
    Snippet cloneSnippet(String p_user, Snippet p_snippet)
        throws RemoteException, SnippetException;

    /**
     * This creates a clone of the snippet with the specified locale
     * and persists it in the database.  It will have a new id.
     *
     * @param p_snippet The snippet to make a clone of.
     * @param p_locale  The locale to set in the clone.
     * @return The new snippet that is a clone of the one passed in
     * (name, description and content) with the specified locale.
     */
    Snippet cloneSnippet(String p_user, Snippet p_snippet,
        GlobalSightLocale p_locale)
        throws RemoteException, SnippetException;

    /**
     * This creates a clone of the snippet with the specified locale
     * and persists in in the database.  It will have a new id.
     *
     * @param p_snippet The snippet to make a clone of.
     * @param p_localeAsString The locale to set in the clone.  The
     * locale is specified as a string ie. en_US, fr_FR
     * @return The new snippet that is a clone of the one passed in.
     */
    Snippet cloneSnippet(String p_user, Snippet p_snippet,
        String p_localeAsString)
        throws RemoteException, SnippetException;

    /**
     * <p>Modify the snippet with the new snippet.  If this is the
     * generic snippet then this modification causes all locale
     * specific snippets associated with this generic one (same name)
     * to become invalid, essentially logically deleted.  Since the
     * generic snippet has changed, the ones derived from it need to
     * be changed by the user.</p>
     *
     * <p>If the snippet doesn't exist yet, it will be added.</p>
     *
     * @param p_snippet: The snippet with its modified information.
     * @param p_validate: Specifiy 'true' if the format of the snippet
     * should be validated and only the translatable text changed or
     * 'false' if no validation needs to be done.  Uses the snippet's
     * name, locale and id to locate the older version of the snippet.
     * These attributes can not be modified.
     * @return the modified snippet
     */
    Snippet modifySnippet(String p_user, Snippet p_snippet, boolean p_validate)
        throws RemoteException, SnippetException;

    Snippet modifySnippet(String p_user, String p_name, String p_desc,
        String p_locale, String p_version, String p_value, boolean p_validate)
        throws RemoteException, SnippetException;

    /**
     * Remove the specified snippet.  If this is the generic snippet
     * then this remoal causes locale specific snippets associated
     * with this generic one (same name) to be invalidated (logically
     * deleted).
     *
     * @param p_snippet: The snippet to remove.
     */
    void removeSnippet(String p_user, Snippet p_snippet)
        throws RemoteException, SnippetException;

    /**
     * Removes the specified snippet.
     * @see removeSnippet(p_snippet)
     */
    void removeSnippet(String p_user, String p_name,
        GlobalSightLocale p_locale, long p_id)
        throws RemoteException, SnippetException;

    /**
     * Removes the specified snippet.
     * @see removeSnippet(p_snippet)
     */
    void removeSnippet(String p_user, String p_name, String p_locale,
        String p_version)
        throws SnippetException, RemoteException;

    /**
     * Removes all snippets with the particular name.
     * @param p_name: The name of the snippets to remove.
     */
    void removeSnippets(String p_user, String p_name)
        throws RemoteException, SnippetException;

    /**
     * Returns a list of all generic snippet names (for creating positions).
     * @return array of String, sorted alphabetically.
     */
    ArrayList getGenericSnippetNames()
        throws RemoteException, SnippetException;

    /**
     * Returns a list of all snippet names that can be inserted in a
     * given locale (so this includes generic snippet names).
     * @return array of String, sorted alphabetically.
     */
    ArrayList getSnippetsByLocale(GlobalSightLocale p_locale)
        throws RemoteException, SnippetException;

    ArrayList getSnippetsByLocale(String p_localeAsString)
        throws RemoteException, SnippetException;

    /**
     * @return An array of all Snippets.
     */
    ArrayList getSnippets()
        throws RemoteException, SnippetException;

    /**
     * @return An array of all snippets with the specified name.  This
     * will return the generic snippet and all locale specific
     * snippets that were copied from the generic one.
     */
    ArrayList getSnippets(String p_name)
        throws RemoteException, SnippetException;

    /**
     * Return an array of all snippets that have the specified name
     * and locale.  This is all snippets that have the same format but
     * different content for a particular locale.
     *
     * @param p_name The name of the snippet to be returned.
     * @param p_locale The locale of the snippet to return or NULL if
     * the generic snippet should be returned.
     * @return An array of all snippets with the specified name and
     * locale.
     */
    ArrayList getSnippets(String p_name, GlobalSightLocale p_locale)
        throws RemoteException, SnippetException;

    /**
     * Return an array of all snippets that have the specified name
     * and locale.  This is all snippets that have the same format but
     * different content for a particular locale.
     *
     * @param p_name The name of the snippet to be returned.
     * @param p_locale The name of the locale as a string ie. "en_US".
     *
     * @return An array of all snippets with the specified name and
     * locale.
     */
    ArrayList getSnippets(String p_name, String p_localeAsString)
        throws RemoteException, SnippetException;

    /**
     * Return an array of all snippets that match one of the names
     * passed in and the specific locale.  This could be used to
     * return all the snippets that can be added to a page.  The names
     * of the snippets are known and the locale.  This method returns
     * the snippets themselves.
     *
     * @param p_names: A collection of Strings which are the names of
     * snippets to query for.
     * @param p_locale: The locale of the snippets or NULL if looking
     * for the generic snippets.
     * @return An array of all snippets that match the specified parameters.
     */
    ArrayList getSnippets(Collection p_names, GlobalSightLocale p_locale)
        throws RemoteException, SnippetException;

    /**
     * Return an array of all snippets that match one of the names
     * passed in and the specific locale.  This could be used to
     * return all the snippets that can be added to a page.  The names
     * of the snippets are known and the locale.  This method returns
     * the snippets themselves.
     *
     * @param p_names: A collection of Strings which are the names of
     * snippets to query for.
     * @param p_locale: The locale of the snippets or NULL if looking
     * for the generic snippets.
     * @return An array of all snippets that match the specified parameters.
     */
    ArrayList getSnippets(Collection p_names, String p_localeAsString)
        throws RemoteException, SnippetException;

    /**
     * Return the snippet identified by the specified attributes which
     * are a key to a snippet.
     *
     * @param p_name: The name of the snippet.
     * @param p_locale: The locale the snippet is in or NULL if
     * looking for the generic snippet.
     * @param p_id: The id of the snippet.
     * @return The snippet.
     */
    Snippet getSnippet(String p_name, GlobalSightLocale p_locale, long p_id)
        throws RemoteException, SnippetException;

    /**
     * Return the snippet identified by the specified attributes which
     * are a key to a snippet.
     *
     * @param p_name: The name of the snippet.
     * @param p_locale: The locale the snippet as a string ie. en_US,
     * fr_FR Or null if a generic snippet.
     * @param p_id: The id of the snippet.
     * @return The snippet.
     */
    Snippet getSnippet(String p_name, String p_localeAsString, long p_id)
        throws RemoteException, SnippetException;

    /**
     * Returns an object that allows to import snippet files.
     *
     * @return an object that implements the general IImportManager
     * interface.
     */
    IImportManager getImporter(String p_user)
        throws RemoteException, ImporterException;
}
