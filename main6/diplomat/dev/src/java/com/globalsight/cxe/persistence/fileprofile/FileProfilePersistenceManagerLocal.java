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
package com.globalsight.cxe.persistence.fileprofile;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.globalsight.cxe.adapter.copyflow.CopyFlowAdapter;
import com.globalsight.cxe.adapter.msoffice.MsOfficeAdapter;
import com.globalsight.cxe.adapter.pdf.PdfAdapter;
import com.globalsight.cxe.adapter.quarkframe.QuarkFrameAdapter;
import com.globalsight.cxe.entity.fileextension.FileExtension;
import com.globalsight.cxe.entity.fileextension.FileExtensionImpl;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import com.globalsight.cxe.entity.knownformattype.KnownFormatTypeImpl;
import com.globalsight.cxe.persistence.knownformattype.KnownFormatTypeDescriptorModifier;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.webapp.pagehandler.administration.fileprofile.FileProfileConstants;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.system.LogManager;
import com.globalsight.util.system.LogType;

/**
 * Implements the service interface for performing CRUD operations for
 * FileProfiles.
 */
public class FileProfilePersistenceManagerLocal implements
        FileProfilePersistenceManager
{
    /**
     * Default constructor TODO: remove throws clause.
     */
    public FileProfilePersistenceManagerLocal()
    // throws FileProfileEntityException, RemoteException
    {
        super();
    }

    //
    // IMPLEMENTATION OF FileProfiles FUNCTIONALITY
    //

    /**
     * Creates a new FileProfile object in the data store
     * 
     * @return the newly created object
     */
    public FileProfile createFileProfile(FileProfile p_profile)
            throws FileProfileEntityException, RemoteException
    {
        try
        {
            // if this is not a duplicate name - add it
            if (!isFileProfileNameDuplicate(p_profile))
            {
                HibernateUtil.save((FileProfileImpl) p_profile);
                return p_profile;
            }
            else
            {
                // it is a duplicate
                String errorArgs[] =
                { p_profile.getName() };
                throw new FileProfileEntityException(
                        FileProfileEntityException.MSG_FILE_PROFILE_ALREADY_EXISTS,
                        errorArgs, null);
            }
        }
        catch (Exception e)
        {
            throw new FileProfileEntityException(e);
        }
    }

    /**
     * Return the file profile that has the given id.
     * 
     * @param p_name
     *            - The id of the file profile.
     * @param p_editable
     *            -- whether the object should be editable
     * @return the file profile for the given name.
     */
    public FileProfile getFileProfileById(long p_id, boolean p_editable)
            throws FileProfileEntityException, RemoteException
    {
        return readFileProfile(p_id);
    }

    /**
     * Return the file profile with the given id from the database The returned
     * object is in a state that allows editing.
     * 
     * @return the file profile with the given id
     */
    public FileProfile readFileProfile(long p_id)
            throws FileProfileEntityException, RemoteException
    {
        try
        {
            return (FileProfileImpl) HibernateUtil.get(FileProfileImpl.class,
                    p_id, false);
        }
        catch (Exception e)
        {
            throw new FileProfileEntityException(e);
        }
    }

    /**
     * Delete the given file profile from the database.
     */
    public void deleteFileProfile(FileProfile p_fileProfile)
            throws FileProfileEntityException, RemoteException
    {
        try
        {
            HibernateUtil.delete(p_fileProfile);
            LogManager.log(LogType.FILEPROFILE, LogManager.EVENT_TYPE_REMOVE, p_fileProfile.getId(),
                    "Delete File Profile [" + p_fileProfile.getName() + "]", p_fileProfile.getCompanyId());
        }
        catch (Exception pe)
        {
            throw new FileProfileEntityException(pe);
        }
    }

    /**
     * <p>
     * Update the given file profile in the database. It is assumed that the
     * given object is a valid clone (i.e. it was obtained from the database in
     * an EDITABLE state).
     * 
     * <p>
     * This method must be changed to be a void method.
     */
    public FileProfile updateFileProfile(FileProfile p_fileProfile)
            throws FileProfileEntityException, RemoteException
    {
        try
        {
            // verify that the name isn't a duplicate
            // in case the name was modified.
            if (!isFileProfileNameDuplicate(p_fileProfile))
            {
                HibernateUtil.update((FileProfileImpl) p_fileProfile);
            }
            else
            {
                // it is a duplicate
                String errorArgs[] =
                { p_fileProfile.getName() };
                throw new FileProfileEntityException(
                        FileProfileEntityException.MSG_FILE_PROFILE_ALREADY_EXISTS,
                        errorArgs, null);
            }
        }
        catch (Exception e)
        {
            throw new FileProfileEntityException(e);
        }
        return p_fileProfile;
    }

    /**
     * Return all available file profiles from the database. These are returned
     * as EDITABLE objects.
     * 
     * @return a collection of file profiles
     */
    public Collection getAllFileProfiles() throws FileProfileEntityException,
            RemoteException
    {
        try
        {
            Vector ids = KnownFormatTypeDescriptorModifier
                    .getExcludedFormatIds();
            StringBuffer hql = new StringBuffer();
            HashMap map = null;
            hql.append("from FileProfileImpl f where f.isActive = 'Y' ");

            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql.append("and f.companyId = :companyId ");
                map = new HashMap();
                map.put("companyId", Long.parseLong(currentId));
            }

            if (ids != null && ids.size() > 0)
            {
                hql.append("and f.knownFormatTypeId != ").append(ids.get(0));
                for (int i = 1; i < ids.size(); i++)
                {
                    hql.append(" and f.knownFormatTypeId != ").append(
                            ids.get(i));
                }
            }

            hql.append(" order by f.name");

            return filterFileProfiles(HibernateUtil.search(hql.toString(), map));
        }
        catch (FileProfileEntityException fpee)
        {
            throw fpee;
        }
        catch (Exception e)
        {
            throw new FileProfileEntityException(e);
        }
    }

    public Collection getAllFileProfilesByCondition(String condition)
            throws FileProfileEntityException, RemoteException
    {
        try
        {
            Vector ids = KnownFormatTypeDescriptorModifier
                    .getExcludedFormatIds();

            StringBuilder hql = new StringBuilder();
            Session session = HibernateUtil.getSession();
            hql.append("select new com.globalsight.cxe.entity.fileprofile.FileprofileVo(f,kft.name,lp.name,c.name) ");
            hql.append(" from FileProfileImpl f, Company c ,BasicL10nProfile lp, KnownFormatTypeImpl kft ");
            hql.append(" where  kft.id=f.knownFormatTypeId and f.l10nProfileId=lp.id and c.id=f.companyId");

            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql.append(" and f.companyId = ").append(
                        Long.parseLong(currentId));
            }

            if (ids != null && ids.size() > 0)
            {
                hql.append(" and f.knownFormatTypeId != ").append(ids.get(0));
                for (int i = 1; i < ids.size(); i++)
                {
                    hql.append(" and f.knownFormatTypeId != " + ids.get(i));
                }
            }

            hql.append(" ").append(condition)
                    .append(" and f.isActive = 'Y' order by f.name");
            Query q = session.createQuery(hql.toString());

            return q.list();
        }
        catch (FileProfileEntityException fpee)
        {
            throw fpee;
        }
        catch (Exception e)
        {
            throw new FileProfileEntityException(e);
        }
    }

    /**
     * @see FileProfilePersistenceManager.getFileProfilesByExtension
     */
    public Collection getFileProfilesByExtension(List p_extensionNames)
            throws FileProfileEntityException, RemoteException
    {
        try
        {
            StringBuffer sql = new StringBuffer();
            sql.append(" select fp.* from FILE_PROFILE fp, ");
            sql.append(" FILE_PROFILE_EXTENSION fpe, EXTENSION ext ");
            sql.append(" where fp.is_active = 'Y' and fp.id = ");
            sql.append(" fpe.file_profile_id and fpe.extension_id = ext.id ");
            sql.append(" and ext.name in ( ");

            int size = p_extensionNames.size();
            for (int i = 0; i < p_extensionNames.size(); i++)
            {
                sql.append("'");
                String ext = (String) p_extensionNames.get(i);
                sql.append(ext);
                if (i < size - 1)
                {
                    sql.append("',");
                }
            }

            sql.append("') union select * from FILE_PROFILE where ");
            sql.append(" is_active = 'Y' and id not in ( select distinct (");
            sql.append(" file_profile_id ) from FILE_PROFILE_EXTENSION ) ");

            return HibernateUtil.searchWithSql(FileProfileImpl.class,
                    sql.toString());
        }
        catch (Exception e)
        {
            throw new FileProfileEntityException(e);
        }
    }

    /**
     * @see FileProfilePersistenceManager.getFileProfilesByExtension
     */
    public Collection getFileProfilesByExtension(List p_extensionNames,
            long p_companyId) throws FileProfileEntityException,
            RemoteException
    {
        try
        {
            StringBuffer sql = new StringBuffer();
            sql.append(" select fp.* from FILE_PROFILE fp, ");
            sql.append(" FILE_PROFILE_EXTENSION fpe, EXTENSION ext ");
            sql.append(" where fp.is_active = 'Y' and fp.id = ");
            sql.append(" fpe.file_profile_id and fpe.extension_id = ext.id ");
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(String
                    .valueOf(p_companyId)))
            {
                sql.append(" and fp.companyId='");
                sql.append(p_companyId);
                sql.append("'");
            }

            sql.append(" and ext.name in ( ");

            int size = p_extensionNames.size();
            for (int i = 0; i < p_extensionNames.size(); i++)
            {
                sql.append("'");
                String ext = (String) p_extensionNames.get(i);
                sql.append(ext);
                if (i < size - 1)
                {
                    sql.append("',");
                }
            }

            sql.append("') union select * from FILE_PROFILE where ");
            sql.append(" is_active = 'Y'");
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(String
                    .valueOf(p_companyId)))
            {
                sql.append(" and companyId='");
                sql.append(p_companyId);
                sql.append("'");
            }
            sql.append(" and id not in (select distinct (");
            sql.append("file_profile_id) from FILE_PROFILE_EXTENSION)");

            return HibernateUtil.searchWithSql(FileProfileImpl.class,
                    sql.toString());
        }
        catch (Exception e)
        {
            throw new FileProfileEntityException(e);
        }
    }

    /**
     * Return the file profile id for the given name from the database The
     * returned object is in a state that does not allow editing.
     * 
     * @param p_name
     *            - The name of the file profile.
     * @return the file profile id for the given name.
     */
    public long getFileProfileIdByName(String p_name)
            throws FileProfileEntityException, RemoteException
    {
        try
        {
            FileProfile fileProFile = getFileProfileByName(p_name);
            long id = -1;
            if (fileProFile != null)
            {
                id = fileProFile.getId();
            }

            return id;
        }
        catch (Exception e)
        {
            throw new FileProfileEntityException(e);
        }
    }

    /**
     * Return the file profile with the given name from the database The
     * returned object is in a state that does not allow editing.
     * 
     * @return the file profile with the given name
     */
    public FileProfile getFileProfileByName(String p_name)
            throws FileProfileEntityException, RemoteException
    {
        try
        {
            String hql = "from FileProfileImpl f where f.isActive = 'Y' and f.name = :name";
            HashMap map = new HashMap();
            map.put("name", p_name);

            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and f.companyId = :companyId";
                map.put("companyId", Long.parseLong(currentId));
            }
            hql += " order by f.id desc";

            List result = HibernateUtil.search(hql, map);

            FileProfile fileProfile = null;
            if (result != null && result.size() > 0)
            {
                fileProfile = (FileProfileImpl) result.get(0);
            }

            return fileProfile;
        }
        catch (Exception e)
        {
            throw new FileProfileEntityException(e);
        }
    }

    /**
     * Return the file profile with the given name from the database The
     * returned object is in a state that does not allow editing.
     * 
     * @return the file profile with the given name
     */
    public FileProfile getFileProfileByName(String p_name, boolean p_isActive)
            throws FileProfileEntityException, RemoteException
    {
        try
        {
            String hql = "from FileProfileImpl f where f.isActive = '";
            hql += p_isActive ? "Y" : "N";
            hql += "' and f.name = :name";
            HashMap map = new HashMap();
            map.put("name", p_name);

            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and f.companyId = :companyId";
                map.put("companyId", Long.parseLong(currentId));
            }
            hql += " order by f.id desc";

            List result = HibernateUtil.search(hql, map);

            FileProfile fileProfile = null;
            if (result != null && result.size() > 0)
            {
                fileProfile = (FileProfileImpl) result.get(0);
            }

            return fileProfile;
        }
        catch (Exception e)
        {
            throw new FileProfileEntityException(e);
        }
    }

    //
    // IMPLEMENTATION OF FileExtensions FUNCTIONALITY
    //

    /**
     * Creates a new FileExtension object in the data store.
     * 
     * @throws FileProfileEntityException
     *             if the extension is not valid or already exists.
     * @return the newly created object
     */
    public FileExtension createFileExtension(FileExtension p_extension)
            throws FileProfileEntityException, RemoteException
    {
        // Validation throws an exception that can be shown in the UI.
        validateFileExtension(p_extension);
        return newFileExtension(p_extension);
    }

    private FileExtension newFileExtension(FileExtension p_extension)
            throws FileProfileEntityException
    {
        try
        {
            HibernateUtil.save((FileExtensionImpl) p_extension);

            return readFileExtension(p_extension.getId());
        }
        catch (Exception e)
        {
            throw new FileProfileEntityException(e);
        }
    }

    /**
     * Creates default file extensions for each company.
     * 
     * @param p_companyId
     *            Id of the company with to be created.
     */
    public void createDefaultFileExtension(String p_companyId)
            throws FileProfileEntityException, RemoteException
    {
        String[] extensions = FileProfileConstants.extensions;

        for (int i = 0; i < extensions.length; i++)
        {
            newFileExtension(new FileExtensionImpl(extensions[i], p_companyId));
        }
    }

    /**
     * Returns the file profile with the given id from the database The returned
     * object is in a state that allows editing.
     * 
     * @return the file profile with the given id
     */
    public FileExtension readFileExtension(long p_id)
            throws FileProfileEntityException, RemoteException
    {
        try
        {
            return (FileExtensionImpl) HibernateUtil.get(
                    FileExtensionImpl.class, p_id);
        }
        catch (Exception e)
        {
            throw new FileProfileEntityException(e);
        }
    }

    /**
     * Deletes a File Extension from the datastore
     */
    public void deleteFileExtension(FileExtension p_fileExtension)
            throws FileProfileEntityException, RemoteException
    {
        try
        {
            HibernateUtil.delete((FileExtensionImpl) p_fileExtension);
        }
        catch (Exception e)
        {
            throw new FileProfileEntityException(e);
        }
    }

    /**
     * <p>
     * Update the given file extension in the database. It is assumed that the
     * given object is a valid clone (i.e. it was obtained from the database in
     * an EDITABLE state).
     * 
     * <p>
     * This method must be changed to be a void method.
     */
    public FileExtension updateFileExtension(FileExtension p_fileExtension)
            throws FileProfileEntityException, RemoteException
    {
        try
        {
            HibernateUtil.update((FileExtensionImpl) p_fileExtension);
        }
        catch (Exception e)
        {
            throw new FileProfileEntityException(e);
        }

        return p_fileExtension;
    }

    /**
     * Return all available file extensions from the database. These are
     * returned as EDITABLE objects.
     * 
     * @return a collection of file extensions
     */
    public Collection getAllFileExtensions() throws FileProfileEntityException,
            RemoteException
    {
        try
        {
            String hql = "from FileExtensionImpl f where f.isActive='Y' ";

            HashMap map = null;
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and f.companyId = :companyId";
                map = new HashMap();
                map.put("companyId", Long.parseLong(currentId));
            }

            return HibernateUtil.search(hql, map);
        }
        catch (Exception e)
        {
            throw new FileProfileEntityException(e);
        }
    }

    public FileExtensionImpl getFileExtension(long id)
    {
        try
        {
            return HibernateUtil.get(FileExtensionImpl.class, id);
        }
        catch (Exception e)
        {
            throw new FileProfileEntityException(e);
        }
    }

    public HashMap<Long, String> getIdViewFileExtensions()
            throws FileProfileEntityException, RemoteException
    {
        String sql = "SELECT fp.ID fid,e.NAME ename FROM file_profile fp, extension e, file_profile_extension fpe "
                + "WHERE fp.ID=fpe.FILE_PROFILE_ID AND e.ID=fpe.EXTENSION_ID AND fp.IS_ACTIVE='Y' AND e.IS_ACTIVE='Y'";

        Session session = HibernateUtil.getSession();
        SQLQuery query = session.createSQLQuery(sql);
        Collection<Object[]> listo = query.list();
        HashMap<Long, String> result = new HashMap<Long, String>();
        try
        {
            for (Iterator<Object[]> a = listo.iterator(); a.hasNext();)
            {
                Object[] test = a.next();
                long key = ((BigInteger) test[0]).longValue();
                String value = result.get(key);
                if (value != null)
                {
                    result.put(key, value + "<br>" + (String) test[1]);

                }
                else
                {
                    result.put(key, (String) test[1]);

                }

            }
        }

        catch (Exception e)
        {
            throw new FileProfileEntityException(e);
        }
        return result;
    }

    /**
     * Return all EXTENSIONs associated with the given file profile. These are
     * returned as IMMUTABLE (i.e. non-editable) objects.
     * 
     * @param p_fileProfile
     *            the file profile object containing the extension ids to search
     *            for
     * 
     * @return a collection of file extension objects.
     */
    public Collection getFileExtensionsByFileProfile(FileProfile p_fileProfile)
            throws FileProfileEntityException, RemoteException
    {
        Collection results = null;

        // if there are file profile extensions
        if (p_fileProfile.getFileExtensionIds().size() > 0)
        {
            Vector args = new Vector();
            args.add(p_fileProfile.getFileExtensionIds());

            try
            {
                String sql = fileExtensionIds(p_fileProfile
                        .getFileExtensionIds());
                results = HibernateUtil.searchWithSql(sql, null,
                        FileExtensionImpl.class);
            }
            catch (Exception e)
            {
                throw new FileProfileEntityException(e);
            }
        }
        else
        {
            results = new Vector();
        }

        return results;
    }

    private String fileExtensionIds(Vector p_fileExtensionIds)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT * FROM EXTENSION WHERE ");
        addExtensionIds(sb, p_fileExtensionIds);
        return sb.toString();
    }

    private void addExtensionIds(StringBuffer p_sb, Vector p_ids)
    {
        p_sb.append("ID in (");
        for (int i = 0; i < p_ids.size(); i++)
        {
            p_sb.append(p_ids.elementAt(i));
            if (i < p_ids.size() - 1)
            {
                p_sb.append(", ");
            }
        }
        p_sb.append(")");
    }

    /**
     * Checks if the file profile name associated with the specified profile
     * already exists as an active profile. If it does, makes sure that it isn't
     * the same file profile being passed in (this can happeen on a modify if
     * the name isn't changed). This is used when creating a new profile or
     * modifying an existing one.
     * 
     * @return 'true' if it already exists, 'false' if it doesn't.
     */
    private boolean isFileProfileNameDuplicate(FileProfile p_profile)
            throws Exception
    {
        boolean isDuplicate = false;

        // check if an active file profile already exists with this name
        long fileProfileId = getFileProfileIdByName(p_profile.getName());

        // If one was found verify that it isn't the same file profile
        // as passed in...
        if (fileProfileId > 0)
        {
            if (fileProfileId != p_profile.getId())
            {
                isDuplicate = true;
            }
        }

        return isDuplicate;
    }

    /**
     * Check if the file extension entered by user is duplicate, i.e. if that
     * file extension already exist.
     * 
     * @return 'true' if that file extension already exist, or 'false' if that
     *         file extension is not a duplicate.
     */
    private boolean isFileExtensionNameDuplicate(FileExtension p_extension)
            throws FileProfileEntityException, RemoteException
    {
        boolean result = false;
        FileExtensionImpl fileExtension = new FileExtensionImpl(p_extension);
        Collection c = getAllFileExtensions();

        for (Iterator it = c.iterator(); it.hasNext();)
        {
            FileExtensionImpl ext = (FileExtensionImpl) it.next();

            if ((fileExtension.getName()).equals(ext.getName()))
            {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * Check if the file extension entered by user is valid, i.e. check if it is
     * more than 40 characters long, if it contains certain invalid characters
     * or if the file extension already exists.
     * 
     * @return 'true' if the FileExtension is valid, 'false' if it doesn't
     *         satisfy these 3 conditions
     */
    private boolean validateFileExtension(FileExtension p_fileExtension)
            throws FileProfileEntityException, RemoteException
    {
        if (EditUtil.getUTF8Len(p_fileExtension.getName()) > 40)
        {
            String errorArgs[] =
            { p_fileExtension.getName() };
            throw new FileProfileEntityException(
                    FileProfileEntityException.MSG_FILE_EXTENSION_TOO_LONG,
                    errorArgs, null,
                    FileProfileEntityException.PROP_FILEEXTENSION_MESSAGES);
        }
        else if (!EditUtil.validateFileExtension(p_fileExtension.getName()))
        {
            String errorArgs[] =
            { p_fileExtension.getName() };
            throw new FileProfileEntityException(
                    FileProfileEntityException.MSG_FILE_EXTENSION_INVALID,
                    errorArgs, null,
                    FileProfileEntityException.PROP_FILEEXTENSION_MESSAGES);
        }
        else if (isFileExtensionNameDuplicate(p_fileExtension))
        {
            String errorArgs[] =
            { p_fileExtension.getName() };
            throw new FileProfileEntityException(
                    FileProfileEntityException.MSG_FILE_EXTENSION_ALREADY_EXISTS,
                    errorArgs, null,
                    FileProfileEntityException.PROP_FILEEXTENSION_MESSAGES);
        }

        return true;
    }

    //
    // IMPLEMENTATION OF KnownFormatType FUNCTIONALITY
    //

    /**
     * Return all available known format types from the database. These are
     * returned as IMMUTABLE objects.
     * 
     * @return a collection of known format types
     */
    public Collection getAllKnownFormatTypes()
            throws FileProfileEntityException, RemoteException
    {
        try
        {
            Vector ids = KnownFormatTypeDescriptorModifier
                    .getExcludedFormatIds();
            StringBuffer hql = new StringBuffer();
            hql.append("from KnownFormatTypeImpl t ");
            if (ids != null && ids.size() > 0)
            {
                hql.append("where t.id != ").append(ids.get(0));
                for (int i = 1; i < ids.size(); i++)
                {
                    hql.append(" and t.id != ").append(ids.get(i));
                }
            }
            hql.append(" order by t.name");

            List types = HibernateUtil.search(hql.toString(), null);
            return filterFormats(types);
        }
        catch (Exception e)
        {
            throw new FileProfileEntityException(e);
        }
    }

    /**
     * Reads the KnownFormatType object from the datastore (not editable)
     */
    public KnownFormatType queryKnownFormatType(long p_id)
            throws FileProfileEntityException, RemoteException
    {
        try
        {
            return (KnownFormatTypeImpl) HibernateUtil.get(
                    KnownFormatTypeImpl.class, p_id);
        }
        catch (Exception e)
        {
            throw new FileProfileEntityException(e);
        }
    }

    /**
     * Gets the KnownFormatType object from the datastore
     */
    public KnownFormatType getKnownFormatTypeById(long p_id, boolean p_editable)
            throws FileProfileEntityException, RemoteException
    {
        try
        {
            return (KnownFormatTypeImpl) HibernateUtil.get(
                    KnownFormatTypeImpl.class, p_id);
        }
        catch (Exception e)
        {
            throw new FileProfileEntityException(e);
        }
    }

    /**
     * Removes formats which relate to adapters that are not installed.
     */
    private ArrayList filterFormats(Collection c)
    {
        ArrayList a = new ArrayList(c);
        Iterator iter = a.iterator();
        while (iter.hasNext())
        {
            KnownFormatType format = (KnownFormatType) iter.next();
            if (formatNotInstalled(format))
                iter.remove();
        }
        return a;
    }

    /**
     * Removes file profiles which have formats that relate to adapters that are
     * not installed.
     */
    private ArrayList filterFileProfiles(Collection c)
            throws FileProfileEntityException, RemoteException
    {
        ArrayList a = new ArrayList(c);
        Iterator iter = a.iterator();
        while (iter.hasNext())
        {
            FileProfile fp = (FileProfile) iter.next();
            long formatId = fp.getKnownFormatTypeId();
            KnownFormatType format = queryKnownFormatType(formatId);
            if (formatNotInstalled(format))
                iter.remove();
        }
        return a;
    }

    /**
     * Returns true if the given format is NOT installed Otherwise returns false
     */
    private boolean formatNotInstalled(KnownFormatType p_format)
    {
        String formatName = p_format.getName();

        if (formatName.startsWith(KnownFormatType.FRAME)
                && !QuarkFrameAdapter.isFrameInstalled())
        {
            return true;
        }
        if (formatName.equals(KnownFormatType.QUARK)
                && !QuarkFrameAdapter.isQuarkInstalled())
        {
            return true;
        }
        if (formatName.equals(KnownFormatType.QUARKMAC)
                && !CopyFlowAdapter.isCopyFlowInstalled())
        {
            return true;
        }
        if ((formatName.equals(KnownFormatType.WORD) || formatName
                .equals(KnownFormatType.RTF))
                && !MsOfficeAdapter.isWordInstalled())
        {
            return true;
        }
        if (formatName.equals(KnownFormatType.EXCEL)
                && !MsOfficeAdapter.isExcelInstalled())
        {
            return true;
        }
        if (formatName.equals(KnownFormatType.POWERPOINT)
                && !MsOfficeAdapter.isPowerPointInstalled())
        {
            return true;
        }
        if (formatName.equals(KnownFormatType.PDF) && !PdfAdapter.isInstalled())
        {
            return true;
        }

        return false; // format is installed
    }

    /**
     * Validate if specified file profile is a XLZ reference file profile
     * 
     * @param p_fileProfileName
     * @return if specified file profile name is a xlz reference file profile,
     *         return true
     */
    public boolean isXlzReferenceXlfFileProfile(String p_fileProfileName)
    {
        try
        {
            if (p_fileProfileName != null && p_fileProfileName.endsWith("_RFP"))
            {
                String tmpFPName = p_fileProfileName.substring(0,
                        p_fileProfileName.length() - 4);
                FileProfile xlzFP = getFileProfileByName(tmpFPName);
                if (xlzFP == null)
                    xlzFP = getFileProfileByName(tmpFPName, false);
                FileProfile xlfFP = getFileProfileByName(p_fileProfileName,
                        false);

                if (xlzFP != null && xlfFP != null
                        && xlzFP.getReferenceFP() == xlfFP.getId())
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
