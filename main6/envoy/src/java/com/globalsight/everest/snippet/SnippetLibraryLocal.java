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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.persistence.snippet.SnippetDescriptorModifier;
import com.globalsight.everest.persistence.snippet.SnippetUnnamedQueries;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.snippet.importer.SnippetImporter;
import com.globalsight.importer.IImportManager;
import com.globalsight.importer.ImporterException;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.Output;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SessionInfo;

/**
 * Implementation of the SnippetLibrary interface. Snippet names are
 * case-insensitive and normalized to uppercase.
 * 
 * @see SnippetLibrary
 */
public class SnippetLibraryLocal implements SnippetLibrary
{
    private static final Logger c_logger = Logger
            .getLogger("Snippets" /* SnippetLibraryLocal.class */);

    /**
     * @see SnippetLibrary.addSnippet(Snippet, boolean)
     */
    public Snippet addSnippet(String p_user, Snippet p_snippet,
            boolean p_validate) throws SnippetException, RemoteException
    {
        c_logger.info("User " + p_user + " is creating snippet "
                + p_snippet.getName());

        try
        {
            // set up the snippet that it was copied from
            Snippet snippetCopiedFrom = null;
            if (!p_snippet.isGeneric())
            {
                // get the generic snippet it was copied from to
                // validate it against
                GlobalSightLocale nullLocale = null;
                ArrayList a = getSnippets(p_snippet.getName(), nullLocale);
                if (a != null && a.size() > 0)
                {
                    snippetCopiedFrom = (Snippet) a.get(0);
                }
            }

            if (p_validate)
            {
                // validate the snippet against its copy
                // this could be NULL - so it is just extracted and verified
                validateSnippet(p_snippet, snippetCopiedFrom);
            }

            // if this is a generic snippet then the name should be unique
            if (p_snippet.isGeneric())
            {
                // must create and pass the locale - otherwise call clashes
                // with another method (ambiguous)
                GlobalSightLocale locale = null;
                ArrayList snippets = getSnippets(p_snippet.getName(), locale);
                if (snippets != null && snippets.size() > 0)
                {
                    String[] args =
                    { p_snippet.getName() };
                    throw new SnippetException(
                            SnippetException.DUPLICATE_SNIPPET_NAME, args, null);
                }
            }

            if (p_snippet.getId() < 1)
            {
                HibernateUtil.save(p_snippet);
            }
        }
        catch (Exception pe)
        {
            c_logger.error("A persistence exception occured when "
                    + "adding new snippet " + p_snippet.toString(), pe);
            String args[] =
            { p_snippet.toString() };
            throw new SnippetException(SnippetException.ADD_SNIPPET, args, pe);
        }

        return p_snippet;
    }

    public Snippet addSnippet(String p_user, String p_name, String p_desc,
            String p_locale, String p_version, String p_value,
            boolean p_validate) throws RemoteException, SnippetException
    {
        Snippet snippy = null;

        GlobalSightLocale locale = null;
        if (p_locale != null && p_locale.length() > 0)
        {
            locale = getGlobalSightLocale(p_locale);
        }

        snippy = new SnippetImpl(p_name, p_desc, locale, p_value);
        return addSnippet(p_user, snippy, p_validate);
    }

    /**
     * @see SnippetLibrary.cloneSnippet(p_snippet)
     */
    public Snippet cloneSnippet(String p_user, Snippet p_snippet)
            throws RemoteException, SnippetException
    {
        return cloneSnippet(p_user, p_snippet, p_snippet.getLocale());
    }

    /**
     * @see SnippetLibrary.cloneSnippet(p_snippet, p_locale)
     */
    public Snippet cloneSnippet(String p_user, Snippet p_snippet,
            GlobalSightLocale p_locale) throws SnippetException,
            RemoteException
    {
        Snippet clonedSnippet = null;

        try
        {
            clonedSnippet = new SnippetImpl(p_snippet.getName(), p_snippet
                    .getDescription(), p_locale, p_snippet.getContent());

            clonedSnippet = addSnippet(p_user, clonedSnippet, false);
        }
        catch (SnippetException pe)
        {
            String localeName = p_locale == null ? "null" : p_locale
                    .getDisplayName();

            c_logger.error(
                    "Persistence exception occured when adding a cloned snippet "
                            + p_snippet.toString() + " with locale "
                            + localeName, pe);

            String[] args =
            { p_snippet.toString(), localeName };
            throw new SnippetException(SnippetException.CLONE_SNIPPET, args, pe);
        }

        return clonedSnippet;
    }

    /**
     * @see SnippetLibrary.cloneSnippet(Snippet, String)
     */
    public Snippet cloneSnippet(String p_user, Snippet p_snippet,
            String p_locale) throws RemoteException, SnippetException
    {
        GlobalSightLocale locale = null;

        if (p_locale != null && p_locale.length() > 0)
        {
            locale = getGlobalSightLocale(p_locale);
        }

        return cloneSnippet(p_user, p_snippet, locale);
    }

    /**
     * @see SnippetLibrary.modifySnippet(Snippet, boolean)
     */
    public Snippet modifySnippet(String p_user, Snippet p_snippet,
            boolean p_validate) throws SnippetException, RemoteException
    {
        c_logger.info("User " + p_user + " is modifying snippet "
                + p_snippet.getName() + " id (" + p_snippet.getId() + ") "
                + "in locale " + p_snippet.getLocale());

        // retrieve the snippet before modifcation
        Snippet oldSnippet = getSnippet(p_snippet.getName(), p_snippet
                .getLocale(), p_snippet.getId());

        // if one was returned modify it.
        if (oldSnippet != null)
        {
            if (p_validate)
            {
                // first validate that the snippet is valid
                // if not will throw an exception
                validateSnippet(p_snippet, oldSnippet);
            }

            Session session = HibernateUtil.getSession();
            Transaction transaction = session.beginTransaction();
            SnippetImpl sClone = (SnippetImpl) session.get(SnippetImpl.class,
                    oldSnippet.getIdAsLong());

            // update the modifiable attributes
            sClone.setDescription(p_snippet.getDescription());
            sClone.setContent(p_snippet.getContent());
            session.update(sClone);
            transaction.commit();
            //session.close();

            // Thu Dec 05 23:42:10 2002 CvdL: removing snippets
            // causes trouble at HP because users add generic
            // snippets by mistake, then the admin tries to modify
            // them, and then all copies are gone. This is
            // impossible to clean up manually so we simply don't
            // delete dependent snippets. An accompanying fix will
            // prohibit generic snippets from being added in first
            // place.

            // TODO: unclear whether this is the right behavior.
            // if this is a generic snippet that has been modified
            // it should invalidate/delete the ones that reference
            // the older copy.
            if (false /* p_snippet.isGeneric() */)
            {
                ArrayList dependantSnippets = getSnippets(p_snippet.getName());
                // if there are more snippets than just the one
                // being modified...
                if (dependantSnippets.size() > 1)
                {
                    int indexOfGeneric = dependantSnippets.indexOf(p_snippet);
                    // remove it from the list and delete the other ones
                    dependantSnippets.remove(indexOfGeneric);
                    removeSnippets(dependantSnippets);
                }
            }

            // TODO: which object must be returned here?
            return sClone;
        }
        else
        {
            // not found so do an add.
            return addSnippet(p_user, p_snippet, p_validate);
        }
    }

    public Snippet modifySnippet(String p_user, String p_name, String p_desc,
            String p_locale, String p_version, String p_value,
            boolean p_validate) throws RemoteException, SnippetException
    {
        GlobalSightLocale locale = null;

        if (p_locale != null && p_locale.length() > 0)
        {
            locale = getGlobalSightLocale(p_locale);
        }

        // create a new snippet with the modified information
        Snippet snippy = new SnippetImpl(Long.parseLong(p_version), p_name,
                p_desc, locale, p_value);

        // call to modify the current snippet with this new one.
        return modifySnippet(p_user, snippy, p_validate);
    }

    /**
     * @see SnippetLibrary.removeSnippet(Snippet)
     */
    public void removeSnippet(String p_user, Snippet p_snippet)
            throws SnippetException, RemoteException
    {
        c_logger.info("User " + p_user + " is deleting snippet "
                + p_snippet.getName() + " id (" + p_snippet.getId() + ")");

        try
        {
            // if it is the generic snippet - should invalidate/delete
            // the others also that have dependancies on it.
            if (p_snippet.isGeneric())
            {
                removeSnippets(p_user, p_snippet.getName());
            }
            else
            {
                HibernateUtil.delete((SnippetImpl) p_snippet);
            }
        }
        catch (Exception pe)
        {
            c_logger.error("An error occurred when removing snippet "
                    + p_snippet.toString(), pe);
            String[] args =
            { p_snippet.toString() };
            throw new SnippetException(SnippetException.REMOVE_SNIPPET, args,
                    pe);
        }
    }

    /**
     * @see SnippetLibrary.removeSnippet(String, GlobalSightLocale, long)
     */
    public void removeSnippet(String p_user, String p_name,
            GlobalSightLocale p_locale, long p_version)
            throws SnippetException, RemoteException
    {
        Snippet s = getSnippet(p_name, p_locale, p_version);
        if (s != null)
        {
            removeSnippet(p_user, s);
        }
        // else it doesn't exist - so already deleted
    }

    /**
     * @see SnippetLibrary.removeSnippet(String, String, String)
     */
    public void removeSnippet(String p_user, String p_name, String p_locale,
            String p_version) throws SnippetException, RemoteException
    {
        GlobalSightLocale locale = null;

        if (p_locale != null && p_locale.length() > 0)
        {
            locale = getGlobalSightLocale(p_locale);
        }

        Snippet s = getSnippet(p_name, locale, Long.parseLong(p_version));
        if (s != null)
        {
            removeSnippet(p_user, s);
        }
        // else it doesn't exist - so already deleted
    }

    /**
     * @see SnippetLibrary.removeSnippets(String)
     */
    public void removeSnippets(String p_user, String p_name)
            throws SnippetException, RemoteException
    {
        try
        {
            ArrayList snippets = getSnippets(p_name);

            if (snippets.size() > 0)
            {
                HibernateUtil.delete(snippets);
            }
        }
        catch (Exception pe)
        {
            c_logger.error("Error when removing snippets with name " + p_name,
                    pe);
            String[] args =
            { p_name };
            throw new SnippetException(
                    SnippetException.REMOVE_SNIPPETS_WITH_NAME, args, pe);
        }
    }

    /**
     * @see SnippetLibrary.getGenericSnippetNames()
     */
    public ArrayList getGenericSnippetNames() throws RemoteException,
            SnippetException
    {
        ArrayList result = new ArrayList();

        try
        {
            String hql = "select s.name from SnippetImpl s where s.locale is null order by s.name";
            List names = HibernateUtil.search(hql);
            result.addAll(names);
        }
        catch (PersistenceException pe)
        {
            c_logger.error("Can't retrieve snippet names", pe);

            throw new SnippetException(SnippetException.GENERIC_SNIPPET_NAMES,
                    null, pe);
        }

        return result;
    }

    /**
     * @see SnippetLibrary.getSnippetsByLocale(GlobalSightLocale)
     */
    public ArrayList getSnippetsByLocale(GlobalSightLocale p_locale)
            throws RemoteException, SnippetException
    {
        ArrayList result = new ArrayList();

        try
        {
            String sql = SnippetDescriptorModifier.SNIPPETS_BY_LOCALE_SQL;
            Map map = new HashMap();
            map.put("snippetLocaleIdArg", p_locale.getIdAsLong());
            result.addAll(HibernateUtil.searchWithSql(sql, map,
                    SnippetImpl.class));
        }
        catch (PersistenceException pe)
        {
            c_logger.error("Can't retrieve snippets by locale", pe);

            String[] args =
            { p_locale.toString() };

            throw new SnippetException(SnippetException.SNIPPETS_BY_LOCALE,
                    args, pe);
        }

        return result;
    }

    /**
     * @see SnippetLibrary.getSnippetsByLocale(String)
     */
    public ArrayList getSnippetsByLocale(String p_locale)
            throws RemoteException, SnippetException
    {
        GlobalSightLocale locale = null;

        if (p_locale != null && p_locale.length() > 0)
        {
            locale = getGlobalSightLocale(p_locale);
        }

        return getSnippetsByLocale(locale);
    }

    /**
     * @see SnippetLibrary.getSnippets()
     */
    public ArrayList getSnippets() throws SnippetException, RemoteException
    {
        ArrayList snippets = null;

        try
        {
            String hql = "from SnippetImpl s order by s.id";
            snippets = new ArrayList(HibernateUtil.search(hql));
        }
        catch (PersistenceException pe)
        {
            c_logger.error("Failed to retrieve all the snippets.", pe);
            throw new SnippetException(SnippetException.ALL_SNIPPETS, null, pe);
        }

        return snippets == null ? new ArrayList(0) : snippets;
    }

    /**
     * @see SnippetLibrary.getSnippets(String)
     */
    public ArrayList getSnippets(String p_name) throws SnippetException,
            RemoteException
    {
        ArrayList snippets = null;

        try
        {
            p_name = PersistentObject.truncateString(p_name,
                    SnippetImpl.MAX_NAME_LEN).toUpperCase();
            String hql = "from SnippetImpl s where s.name = :name order by s.id";
            Map map = new HashMap();
            map.put("name", p_name);
            snippets = new ArrayList(HibernateUtil.search(hql, map));
        }
        catch (Exception pe)
        {
            c_logger.error("Failed to retrieve all the snippets with name "
                    + p_name, pe);
            String[] args =
            { p_name };
            throw new SnippetException(SnippetException.SNIPPETS_BY_NAME, args,
                    pe);
        }

        return snippets == null ? new ArrayList(0) : snippets;
    }

    /**
     * @see SnippetLibrary.getSnippets(String, GlobalSightLocale)
     */
    public ArrayList getSnippets(String p_name, GlobalSightLocale p_locale)
            throws SnippetException, RemoteException
    {
        ArrayList snippets = null;

        try
        {
            p_name = PersistentObject.truncateString(p_name,
                    SnippetImpl.MAX_NAME_LEN).toUpperCase();

            Vector args = new Vector(2);
            args.add(p_name);
            if (p_locale != null)
            {
                String sql = SnippetDescriptorModifier.SNIPPETS_BY_NAME_AND_LOCALE_SQL;
                Map map = new HashMap();
                map.put("snippetName", p_name);
                map.put("snippetLocaleIdArg", p_locale.getIdAsLong());

                args.add(p_locale.getIdAsLong());
                snippets = new ArrayList(HibernateUtil.searchWithSql(sql, map,
                        SnippetImpl.class));
            }
            else
            {
                String hql = "from SnippetImpl s where s.name = :name and s.locale is null order by s.id";
                Map map = new HashMap();
                map.put("snippetName", p_name);
                snippets = new ArrayList(HibernateUtil.search(hql, map));
            }
        }
        catch (Exception pe)
        {
            String localeName = p_locale == null ? "null" : p_locale
                    .getDisplayName();

            c_logger.error("Failed to retrieve all the snippets with name: "
                    + p_name + " and locale: " + localeName, pe);

            String[] args =
            { p_name, localeName };
            throw new SnippetException(
                    SnippetException.SNIPPETS_BY_NAME_AND_LOCALE, args, pe);
        }

        return snippets == null ? new ArrayList(0) : snippets;
    }

    /**
     * @see SnippetLibrary.getSnippets(String, String)
     */
    public ArrayList getSnippets(String p_name, String p_locale)
            throws RemoteException, SnippetException
    {
        GlobalSightLocale locale = null;

        if (p_locale != null && p_locale.length() > 0)
        {
            locale = getGlobalSightLocale(p_locale);
        }

        // get the locale object
        return getSnippets(p_name, locale);
    }

    /**
     * @see SnippetLibrary.getSnippets(Collection, GlobalSightLocale)
     */
    public ArrayList getSnippets(Collection p_names, GlobalSightLocale p_locale)
            throws SnippetException, RemoteException
    {
        ArrayList snippets = null;

        ArrayList names = new ArrayList(p_names.size());

        for (Iterator it = p_names.iterator(); it.hasNext();)
        {
            String name = (String) it.next();

            names.add(PersistentObject.truncateString(name,
                    SnippetImpl.MAX_NAME_LEN).toUpperCase());
        }

        try
        {
            // if not a generic snippet
            if (p_locale != null)
            {
                snippets = new ArrayList(SnippetUnnamedQueries
                        .getSnippetsByNamesAndLocaleQuery(names, p_locale
                                .getIdAsLong()));
            }
            else
            {
                snippets = new ArrayList(SnippetUnnamedQueries
                        .getGenericSnippetsByNames(names));
            }
        }
        catch (PersistenceException pe)
        {
            String localeName = p_locale == null ? "null" : p_locale
                    .getDisplayName();

            c_logger.error("Failed to retrieve all the snippets with name: "
                    + names + " and locale: " + localeName, pe);

            String[] args =
            { names.toString(), localeName };
            throw new SnippetException(
                    SnippetException.SNIPPETS_BY_NAMES_AND_LOCALE, args, pe);
        }

        return snippets;
    }

    /**
     * @see SnippetLibrary.getSnippets(Collection, String)
     */
    public ArrayList getSnippets(Collection p_names, String p_locale)
            throws RemoteException, SnippetException
    {
        GlobalSightLocale locale = null;

        if (p_locale != null && p_locale.length() > 0)
        {
            locale = getGlobalSightLocale(p_locale);
        }

        return getSnippets(p_names, locale);
    }

    /**
     * @see SnippetLibrary.getSnippet(String, GlobalSightLocale, long)
     */
    public Snippet getSnippet(String p_name, GlobalSightLocale p_locale,
            long p_version) throws SnippetException, RemoteException
    {
        Snippet s = null;

        try
        {
            p_name = PersistentObject.truncateString(p_name,
                    SnippetImpl.MAX_NAME_LEN).toUpperCase();

            Collection snippets = null;
            Vector args = new Vector(3);
            args.add(p_name);

            if (p_locale != null)
            {
                String hql = "from SnippetImpl s where s.id = :sId and s.name = :name and s.locale.id = :lId";
                Map map = new HashMap();
                map.put("sId", new Long(p_version));
                map.put("name", p_name);
                map.put("lId", p_locale.getIdAsLong());
                snippets = HibernateUtil.search(hql, map);
                ;
            }
            else
            {
                // Generic snippet: just search by name
                String hql = "from SnippetImpl s where s.name = :name and s.locale is null order by s.id";
                Map map = new HashMap();
                map.put("name", p_name);
                snippets = HibernateUtil.search(hql, map);
                ;
            }

            if (snippets.size() > 0)
            {
                Iterator si = snippets.iterator();
                s = (Snippet) si.next();
            }
        }
        catch (Exception pe)
        {
            String localeName = p_locale == null ? "null" : p_locale
                    .getDisplayName();

            c_logger.error("Failed to retrieve the snippet with name: "
                    + p_name + ", locale: " + localeName + ", and version:  "
                    + p_version, pe);

            String[] args =
            { p_name, localeName, Long.toString(p_version) };
            throw new SnippetException(SnippetException.SNIPPET_BY_KEY, args,
                    pe);
        }

        return s;
    }

    /**
     * @see SnippetLibrary.getSnippet(String, String, long)
     */
    public Snippet getSnippet(String p_name, String p_locale, long p_id)
            throws RemoteException, SnippetException
    {
        GlobalSightLocale locale = null;

        if (p_locale != null && p_locale.length() > 0)
        {
            locale = getGlobalSightLocale(p_locale);
        }

        return getSnippet(p_name, locale, p_id);
    }

    /**
     * Returns an object that allows to import snippet files.
     * 
     * @return an object that implements the general IImportManager interface.
     */
    public IImportManager getImporter(String p_user) throws RemoteException,
            ImporterException
    {
        return new SnippetImporter(new SessionInfo(p_user, ""));
    }

    //
    // Private Methods
    //

    /**
     * This method does NOT check if any of the snippets are generic snippets.
     * If it does contain one or more it should also contain all the snippets
     * with the same name.
     */
    private void removeSnippets(Collection p_snippets) throws SnippetException
    {
        try
        {
            HibernateUtil.delete(p_snippets);
        }
        catch (Exception pe)
        {
            c_logger.error("An error occurred when removing snippets "
                    + p_snippets.toString(), pe);

            String[] args =
            { p_snippets.toString() };
            throw new SnippetException(
                    SnippetException.REMOVE_SNIPPETS_WITH_NAME, args, pe);
        }
    }

    /**
     * Wraps the code for getting the locale manager and any exceptions.
     */
    private GlobalSightLocale getGlobalSightLocale(String p_locale)
            throws SnippetException, RemoteException
    {
        try
        {
            return ServerProxy.getLocaleManager().getLocaleByString(p_locale);
        }
        catch (GeneralException ge)
        {
            c_logger.error("Invalid locale " + p_locale, ge);
            String[] args =
            { p_locale };
            throw new SnippetException(SnippetException.INVALID_LOCALE, args,
                    ge);
        }
    }

    /**
     * Verifies that a copy of a snippet is not too different from its previous
     * version. If the snippets are inacceptably different, a SnippetException
     * is thrown.
     */
    private void validateSnippet(Snippet p_new, Snippet p_old)
            throws SnippetException
    {
        // Generic snippets can be updated at will.
        if (p_old == null || p_new.isGeneric()) { return; }

        try
        {
            // Wed Dec 11 14:36:47 2002 CvdL: weaken verification
            // to check only GS tags (see GSDEF 8278).

            // validateSnippetStructure(p_newSnippet, p_oldSnippet);
            validateSnippetGSTags(p_new, p_old);
        }
        catch (SnippetException se)
        {
            c_logger.warn("Invalid snippet", se);
            throw se;
        }
        catch (Exception e)
        {
            c_logger.warn("Unexpected exception when validating snippet "
                    + p_new + " content = `" + p_new.getContent() + "'", e);
            invalidSnippet(p_new, e, SnippetException.INVALID_SNIPPET);
        }
    }

    /**
     * Walks the list of GXM DocumentElements in two snippets and verifies that
     * all GS tags are present and the same. Changes in skeleton and segment
     * elements are ignored.
     */
    private void validateSnippetGSTags(Snippet p_new, Snippet p_old)
            throws SnippetException
    {
        Output oldOutput = extractSnippet(p_old);
        Output newOutput = extractSnippet(p_new);

        Iterator oldElements = oldOutput.documentElementIterator();
        Iterator newElements = newOutput.documentElementIterator();

        DocumentElement newElement = null;
        DocumentElement oldElement = null;

        int newType = DocumentElement.NONE;
        int oldType = DocumentElement.NONE;

        while (newElements.hasNext())
        {
            newElement = (DocumentElement) newElements.next();

            newType = newElement.type();

            if (newType == DocumentElement.GSA_START
                    || newType == DocumentElement.GSA_END)
            {
                oldElement = null;
                oldType = DocumentElement.NONE;

                // Search for this GS tag in the old snippet.
                while (oldType != newType && oldElements.hasNext())
                {
                    oldElement = (DocumentElement) oldElements.next();
                    oldType = oldElement.type();
                }

                // GS tag not found.
                if (oldType != newType)
                {
                    invalidSnippet(p_new, null,
                            SnippetException.INVALID_GS_TAG_ADDED);
                }

                // Ensure the content of the GS tags is unchanged.
                validateContent(p_new, newElement, oldElement);
            }
        }

        // Ensure old snippet does not contain more GS tags.
        while (oldElements.hasNext())
        {
            oldElement = (DocumentElement) oldElements.next();
            oldType = oldElement.type();

            if (oldType == DocumentElement.GSA_START
                    || oldType == DocumentElement.GSA_END)
            {
                invalidSnippet(p_new, null,
                        SnippetException.INVALID_GS_TAG_REMOVED);
            }
        }
    }

    /**
     * Verifies that GXML DocumentElement content is not too different from its
     * previous version.
     * 
     * Note: currently this method works for skeleton elements and GS tags (and
     * localizables). It is not prepared to correctly compare translatables and
     * TMX markup they may contain.
     */
    private void validateContent(Snippet p_snippet, DocumentElement p_new,
            DocumentElement p_old) throws SnippetException
    {
        // initial and trailing whitespace doesn't count
        String oldText = p_old.getText().trim();
        String newText = p_new.getText().trim();

        // quick exit: strings are identical
        if (oldText.equalsIgnoreCase(newText)) { return; }

        oldText = Text.normalizeWhiteSpaces(oldText);
        newText = Text.normalizeWhiteSpaces(newText);

        // whitespace-normalized strings are acceptable.
        if (oldText.equalsIgnoreCase(newText)) { return; }

        String[] args =
        { p_snippet.getName() + " " + p_snippet.getId(), p_new.getText(),
                p_old.getText() };

        throw new SnippetException(SnippetException.INVALID_DIFFERENT_CONTENT,
                args, null);
    }

    /**
     * Extracts a snippet as HTML using paragraph segmentation.
     */
    private Output extractSnippet(Snippet p_snippet) throws SnippetException
    {
        Output result = null;

        try
        {
            DiplomatAPI extractor = new DiplomatAPI();

            extractor.setSourceString(p_snippet.getContent());
            extractor.setInputFormat(DiplomatAPI.FORMAT_HTML);
            extractor.setSentenceSegmentation(false);
            extractor.extract();

            result = extractor.getOutput();
        }
        catch (Exception e)
        {
            invalidSnippet(p_snippet, e,
                    SnippetException.INVALID_EXTRACTION_ERROR);
        }

        return result;
    }

    /**
     * Helper method to throw an INVALID_SNIPPET exception.
     */
    private void invalidSnippet(Snippet p_snippet, Exception e, String p_key)
            throws SnippetException
    {
        String[] args =
        { p_snippet.getName() + " " + p_snippet.getId() };
        throw new SnippetException(p_key, args, e);
    }
}
