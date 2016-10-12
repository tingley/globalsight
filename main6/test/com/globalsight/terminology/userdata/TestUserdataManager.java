package com.globalsight.terminology.userdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.permission.PermissionGroupImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.Definition;
import com.globalsight.terminology.TermbaseTestConstants;
import com.globalsight.terminology.TermbaseTestHelper;
import com.globalsight.terminology.java.InputModel;
import com.globalsight.terminology.java.Termbase;
import com.globalsight.util.SessionInfo;

public class TestUserdataManager implements TermbaseTestConstants
{
    static UserdataManager m_instance;
    static Definition m_definition;
    static SessionInfo m_session;
    static long m_tbid = -1;
    static long m_pgid = -1;
    
    long m_modelIDDefa = -1;
    long m_modelID = -1;
    String m_modelNameDefa = "JunitModelNameForDefault";
    String m_modelName = "JUnitModelName";
    String m_modelValue = "<conceptGrp><concept></concept>" 
            + "<languageGrp>" 
            + "<language name=\"English\" locale=\"en\"/>"
            + "<termGrp><term termId=\"-1000\">required</term></termGrp>" 
            + "</languageGrp>" 
            + "<languageGrp>"
            + "<language name=\"Chinese (China)\" locale=\"zh_CN\"/>"
            + "<termGrp><term termId=\"-1000\">optional</term></termGrp>" 
            + "</languageGrp>" 
            + "<languageGrp>"
            + "<language name=\"French\" locale=\"fr\"/>" + "<termGrp><term termId=\"-1000\">optional</term></termGrp>"
            + "<termGrp><term termId=\"-1000\">optional, multiple</term></termGrp>" 
            + "</languageGrp>"
            + "</conceptGrp>";
    
    @BeforeClass
    public static void init()
    {
        // Insert Termbase
        String hql = "from Termbase tb where tb.name = :jTBName and tb.description = :jTBDesc";
        Map<String, String> params = new HashMap<String, String>();
        params.put("jTBName", m_TBName);
        params.put("jTBDesc", m_TBDesc + " For TestUserdataManager.");
        Termbase tb = (Termbase) HibernateUtil.getFirst(hql, params);
        
        if (tb == null)
        {
            tb = new Termbase();
            tb.setName(m_TBName);
            tb.setDescription(m_TBDesc);
            Company company = HibernateUtil.get(Company.class, 1);
            tb.setCompany(company);
            tb.setDefination(m_TBDefi);

            try
            {
                HibernateUtil.saveOrUpdate(tb);
                m_tbid = tb.getId();
                System.out.println("SAVE Termbase: " + m_tbid + "/" + tb.getName() + "/" + tb.getDescription() + "/");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        // Insert permissiongroup & permissiongroup_user
        if (tb != null)
        {
            m_tbid = tb.getId();
            m_definition = new Definition(m_TBDefi);
            PermissionGroupImpl pg = new PermissionGroupImpl("JUnit PG", "For JUnit Test", m_permissionSetString, "1");
            try
            {
                HibernateUtil.saveOrUpdate(pg);
                m_pgid = pg.getId();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            hql = "insert into permissiongroup_user values(" + m_pgid + ", " + m_user + ")";
            try
            {
                HibernateUtil.executeSql(hql);
            }
            catch (HibernateException e)
            {
                e.printStackTrace();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
            m_session = new SessionInfo(m_user, "Admin");
        }
        
        com.globalsight.terminology.Termbase termbase = TermbaseTestHelper.getTermbase(tb);
        m_instance = new UserdataManager(termbase, m_session);
    }

    @AfterClass
    public static void clear()
    {
        String hql;
        PermissionGroupImpl pg;
        // Delete permissiongroup
        if (m_pgid == -1)
        {
            String sql = "delete from permissiongroup where company_ID = '1' and name = 'JUnit PG'";
            try
            {
                HibernateUtil.executeSql(sql);
            }
            catch (HibernateException e)
            {
                e.printStackTrace();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }

        }
        else
        {
            pg = HibernateUtil.get(PermissionGroupImpl.class, m_pgid);
            if (pg != null)
            {
                try
                {
                    HibernateUtil.delete(pg);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        
        // Delete permissiongroup_user
        hql = "DELETE from permissiongroup_user where user_id = " + m_user + " and PERMISSIONGROUP_ID="+m_pgid;
        try
        {
            HibernateUtil.executeSql(hql);
        }
        catch (HibernateException e1)
        {
            e1.printStackTrace();
        }
        catch (SQLException e1)
        {
            e1.printStackTrace();
        }
        
        // Delete InputModel
        hql = "from InputModel m where m.termbase.id = :tbid and m.userName = :user";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("tbid", m_tbid);
        map.put("user", m_user);
        List<?> list = HibernateUtil.search(hql, map);
        if (list != null && list.size() > 0)
        {
            System.out.println("DELETE " + list.size() + " input model, which is created by " + m_user);
            try
            {
                HibernateUtil.delete(list);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        if (m_tbid != -1)
        {
            Termbase tb = (Termbase) HibernateUtil.get(Termbase.class, m_tbid);
            try
            {
                HibernateUtil.delete(tb);
                System.out.println("DELETE Termbase: " + m_tbid + "/" + tb.getName() + "/" + tb.getDescription() + "/");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        m_instance = null;
    }
    
    @Test
    public void testCreateObject()
    {
        m_instance.createObject(1, m_user, m_modelName, m_modelValue);
        String hql = "from InputModel m where m.name = :name";
        Map<String, Object> paras = new HashMap<String, Object>();
        paras.put("name", m_modelName);
        InputModel model = (InputModel) HibernateUtil.getFirst(hql, paras);
        assertNotNull(model);
    }
    
    @Test
    public void testGetObject()
    {
        m_instance.createObject(1, m_user, m_modelName, m_modelValue);
        String hql = "from InputModel m where m.name = :name";
        Map<String, Object> paras = new HashMap<String, Object>();
        paras.put("name", m_modelName);
        InputModel model = (InputModel) HibernateUtil.getFirst(hql, paras);
        model = m_instance.getObject(model.getId());
        assertNotNull(model);
    }

    @Test
    public void testGetDefaultObject()
    {
        m_instance.createObject(1, m_user, m_modelNameDefa, m_modelValue);
        String hql = "from InputModel m where m.name = :name";
        Map<String, Object> paras = new HashMap<String, Object>();
        paras.put("name", m_modelNameDefa);
        InputModel model = (InputModel) HibernateUtil.getFirst(hql, paras);
        m_instance.makeDefaultObject(model.getId());
        String str = m_instance.getDefaultObject("1");
        assertTrue(str != null && !str.matches("<noresult></noresult>"));
    }

    @Test
    public void testModifyObject()
    {
        long id = getModelID();
        int expected = 1000;
        m_instance.modifyObject(id, expected, m_user, m_modelName, m_modelValue);
        InputModel model = HibernateUtil.get(InputModel.class, id);
        assertEquals(expected, model.getType());
    }

    @Test
    public void testIsSetDefault()
    {
        testGetDefaultObject();
        assertTrue(m_instance.isSetDefault(-100));
    }

    @Test
    public void testUnsetDefaultObject()
    {
        long id = getDefaultModelID();
        String expected = "N";
        m_instance.unsetDefaultObject(id);
        InputModel model = HibernateUtil.get(InputModel.class, id);
        assertSame(expected, model.getIsDefault());
    }
    
    @Test
    public void testMakeDefaultObject()
    {
        long id = getDefaultModelID();
        String expected = "Y";
        m_instance.makeDefaultObject(id);
        InputModel model = HibernateUtil.get(InputModel.class, id);
        assertSame(expected, model.getIsDefault());
    }

    @Test
    public void testDeleteObject()
    {
        long id = getModelID();
        m_instance.deleteObject(id);
        m_modelID = -1;
        InputModel model = HibernateUtil.get(InputModel.class, id);
        assertNull(model);
    }

    @Test
    public void testDoGetInputModelList()
    {
        List<?> list = m_instance.doGetInputModelList(1, m_user);
        assertTrue(list != null && list.size() > 0);
    }

    
    private long getModelID()
    {
        if (m_modelID != -1)
        {
            return m_modelID;
        }
        else
        {
            String hql = "from InputModel m where m.name = :name";
            Map<String, Object> paras = new HashMap<String, Object>();
            paras.put("name", m_modelName);
            InputModel model = (InputModel) HibernateUtil.getFirst(hql, paras);
            m_modelID = model.getId();
            return m_modelID;
        }
    }
    
    private long getDefaultModelID()
    {
        if (m_modelIDDefa != -1)
        {
            return m_modelIDDefa;
        }
        else
        {
            String hql = "from InputModel m where m.name = :name";
            Map<String, Object> paras = new HashMap<String, Object>();
            paras.put("name", m_modelNameDefa);
            InputModel model = (InputModel) HibernateUtil.getFirst(hql, paras);
            m_modelIDDefa = model.getId();
            return m_modelIDDefa;
        }
    }
}
