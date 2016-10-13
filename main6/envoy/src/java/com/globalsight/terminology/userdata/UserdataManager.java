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

package com.globalsight.terminology.userdata;

import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.permission.Permission;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.IUserdataManager;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.TermbaseExceptionMessages;
import com.globalsight.terminology.java.InputModel;
import com.globalsight.terminology.util.PermissionHelper;
import com.globalsight.terminology.util.SqlUtil;
import com.globalsight.util.SessionInfo;

/**
 * <p>
 * The RMI interface implementation for the User Data Manager.
 * </p>
 * 
 * <p>
 * Throughout this class, the parameter p_user denotes the username for which
 * data is requested. The name of the user currently calling this code is stored
 * in m_session.
 * </p>
 */
public class UserdataManager implements IUserdataManager,
        TermbaseExceptionMessages
{
    static private final Logger CATEGORY = Logger
            .getLogger(UserdataManager.class);

    static final public String ALL_USERS = "*";
    static final public int MAX_USERNAME_LEN = 80;
    static final public int MAX_NAME_LEN = 200;

    //
    // Private Members
    //
    private SessionInfo m_session = null;
    private Termbase m_database = null;

    //
    // Constructor
    //
    public UserdataManager(Termbase p_database, SessionInfo p_session)
    {
        m_session = p_session;
        m_database = p_database;
    }

    //
    // Interface Implementation
    //

    public String getTermbaseName() throws TermbaseException
    {
        return m_database.getName();
    }

    public InputModel getObject(long id) throws TermbaseException
    {
        InputModel model = HibernateUtil.get(InputModel.class, id);

        return model;
    }

    /**
     * <p>
     * Retrieves a system-wide default object of the given type.
     * </p>
     */
    public String getDefaultObject(String companyId) throws TermbaseException
    {
        StringBuffer hql = new StringBuffer();
        hql.append("from InputModel model where model.termbase.id=");
        hql.append(m_database.getId());
        hql.append(" and isDefault='Y'");
        List list = HibernateUtil.search(hql.toString());

        if (list != null && list.size() > 0)
        {
            InputModel model = (InputModel) list.get(0);
            return model.getValue();
        }
        else
        {
            return "<noresult></noresult>";
        }
    }

    /**
     * Creates an object with name p_name and value p_value for the given user.
     * If the user name is null, a system object is created for all users.
     */
    public void createObject(int p_type, String p_user, String p_name,
            String p_value) throws TermbaseException
    {
        checkWriteAccess(p_user);

        if (p_name == null || p_name.length() == 0)
        {
            String[] args =
            { "object name cannot be empty" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        InputModel model = new InputModel();
        model.setType(p_type);
        model.setName(p_name);
        model.setUserName(p_user);
        model.setValue(p_value);
        model.setIsDefault("N");
        com.globalsight.terminology.java.Termbase tb = HibernateUtil.get(
                com.globalsight.terminology.java.Termbase.class,
                m_database.getId());
        model.setTermbase(tb);
        try
        {
            HibernateUtil.save(model);
        }
        catch (Exception e)
        {
            throw new TermbaseException(e);
        }
        // doCreateObject(p_type, p_user, p_name, p_value);
    }

    public void modifyObject(long id, int p_type, String p_user, String p_name,
            String p_value) throws TermbaseException
    {
        InputModel model = HibernateUtil.get(InputModel.class, id);
        model.setType(p_type);
        model.setName(p_name);
        model.setUserName(p_user);
        model.setValue(p_value);
        try
        {
            HibernateUtil.update(model);
        }
        catch (Exception e)
        {
            throw new TermbaseException(e);
        }
    }

    public boolean isSetDefault(long id)
    {
        StringBuffer hqlBuffer = new StringBuffer();
        hqlBuffer.append("from InputModel model where model.termbase.id='");
        hqlBuffer.append(m_database.getId());
        hqlBuffer.append("' and model.isDefault='Y'");
        List list = HibernateUtil.search(hqlBuffer.toString());

        if (list != null && list.size() > 0)
        {
            return true;
        }

        return false;
    }

    /**
     * Makes a system-wide object the default object.
     */
    public void makeDefaultObject(long id) throws TermbaseException
    {
        InputModel model = HibernateUtil.get(InputModel.class, id);

        try
        {
            model.setIsDefault("Y");
            HibernateUtil.update(model);
        }
        catch (Exception e)
        {
            throw new TermbaseException(e);
        }
    }

    /**
     * Unsets a system-wide default object.
     */
    public void unsetDefaultObject(long p_id) throws TermbaseException
    {
        InputModel model = HibernateUtil.get(InputModel.class, p_id);

        try
        {
            model.setIsDefault("N");
            HibernateUtil.update(model);
        }
        catch (Exception e)
        {
            throw new TermbaseException(e);
        }
    }

    /**
     * Deletes all objects for the given user. If the caller is Administrator
     * and p_user is null, all system objects are deleted.
     */
    public String deleteObject(long id) throws TermbaseException
    {
        InputModel model = HibernateUtil.get(InputModel.class, id);
        String info = new String();

        if (model.getIsDefault().equals("N"))
        {
            try
            {
                HibernateUtil.delete(model);
                info = "success";
            }
            catch (Exception e)
            {
                throw new TermbaseException(e);
            }
        }
        else
        {
            info = "isSetDefault";
        }

        return info;
    }

    //
    // Worker Methods
    //

    public List doGetInputModelList(int p_type, String p_user)
            throws TermbaseException
    {
        StringBuffer hql = new StringBuffer();
        hql.append("select im from InputModel im where im.termbase.id=");
        hql.append(m_database.getId());
        hql.append(" and im.type='").append(p_type);
        hql.append("' and im.userName='");
        if (p_user == null || p_user.length() == 0)
        {
            hql.append(ALL_USERS).append("'");
        }
        else
        {
            hql.append(SqlUtil.quote(p_user)).append("'");
        }

        return HibernateUtil.search(hql.toString());
    }

    //
    // Helper Methods
    //

    /** Checks if the current user is accessing its own data. */
    private boolean isCurrentUser(String p_user)
    {
        return m_session.getUserName().equals(p_user);
    }

    /**
     * Checks read access. A user can read system data and his own data.
     */
    private void checkReadAccess(String p_user) throws TermbaseException
    {
        // If the data belongs to somebody else, it cannot be read.
        if (p_user != null && p_user.length() > 0 && !p_user.equals(ALL_USERS)
                && !isCurrentUser(p_user))
        {
            String[] args =
            { "caller is not allowed to view data" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }
    }

    /**
     * Checks write access. A user can modify his own data. Only administrators
     * can modify other users' and system data.
     * 
     * @param p_user
     *            this is the user that owns the object to be modified, not the
     *            current user. The current user name is stored in m_session.
     */
    private void checkWriteAccess(String p_user) throws TermbaseException
    {
        // Users with the permission to manage input models can change
        // all data (Administrator and Project Managers by default).
        if (PermissionHelper.hasPermission(m_session.getUserName(),
                Permission.TERMINOLOGY_INPUT_MODELS))
        {
            return;
        }

        // Other users cannot modify system data (which belongs to user "")
        if (p_user == null || p_user.length() == 0)
        {
            String[] args =
            { "caller is not allowed to create or modify system data" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        // Other users cannot modify somebody else's data, only their own.
        if (p_user != null && p_user.length() > 0 && !isCurrentUser(p_user))
        {
            String[] args =
            { "caller is not allowed to create or modify other users' data" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }
    }
}
