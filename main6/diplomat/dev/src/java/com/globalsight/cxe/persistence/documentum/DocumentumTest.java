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
package com.globalsight.cxe.persistence.documentum;

import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.GeneralException;

//junit
import junit.framework.TestSuite;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.textui.TestRunner;

import java.util.*;

public class DocumentumTest extends TestCase
{

    private static final Logger s_logger = Logger
            .getLogger(DocumentumTest.class);

    DocumentumPersistenceManager mgr = null;

    DocumentumUserInfo userInfo = null;

    String[] ids = null;

    String[] pwds = null;

    String[] aids = null;

    static int testNumber = 10;

    final static int BASE_ID = 1000;

    final static int BASE_PWD = 1050;

    /**
     * @param args
     */
    public static void main(String[] args)
    {

        try
        {
            if (args.length >= 1)
            {
                testNumber = Integer.valueOf(args[1]).intValue();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        TestRunner.run(DocumentumTest.class);
    }

    public void setUp()
    {
        try
        {
            mgr = ServerProxy.getDocumentumPersistenceManager();
        } catch (RemoteException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (GeneralException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NamingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        

        ids = new String[testNumber];
        pwds = new String[testNumber];
        aids = new String[testNumber];
        for (int i = 0; i < testNumber; i++)
        {
            ids[i] = String.valueOf(i + BASE_ID);
            pwds[i] = String.valueOf(i + BASE_PWD);
        }

    }

    public void tearDown()
    {
        s_logger.info("test over");
    }

    public void testAll() 
    {
        s_logger.info("Before testAll ---->");
        Collection c = getAll();
        Iterator it = c.iterator();
        while(it.hasNext())
        {
            DocumentumUserInfo test = (DocumentumUserInfo)it.next();
            printUser(test);
        }
        s_logger.info("After testAll ----");
    }
    
    public void testPath()
    {
        s_logger.info("Before testPath -------->");
        String file = "D:\\Ambassador_6.7.2_20060621\\Ambassador_6.7.2\\deployment\\ambassador.ear\\lib\\classes";
        String classpath = System.getProperty("java.class.path");
        if (classpath.indexOf(file + ";") == -1)
        {
            try
            {
                System.setProperty("java.class.path", classpath + ";" + file
                        + ";");
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        s_logger.info("After testPath -------->");

        System.out.println(System.getProperty("java.class.path"));
    }
    
    public void testModify()
    {
        s_logger.info("Before Modify ------->");
        Collection c = getAll();
        Iterator it = c.iterator();
        int i = 1;
        while(it.hasNext())
        {
            DocumentumUserInfo test = (DocumentumUserInfo)it.next();
            s_logger.info("Before modify ===");
            printUser(test);
            String id = test.getDocumentumUserId() + i;
            String pwd = test.getDocumentumPassword() + i;
            String docbase = "docBase" + i;
            long uid = test.getId();
            test.setDocumentumUserId(id);
            test.setDocumentumPassword(pwd);
            test.setDocumentumDocbase(docbase);
            DocumentumUserInfo testNew = modify(test);
            assertEquals(testNew.getDocumentumUserId(), id);
            assertEquals(testNew.getDocumentumPassword(), pwd);
            assertEquals(testNew.getDocumentumDocbase(), docbase);
            assertEquals(testNew.getId(), uid);
            s_logger.info("After modify ===");
            printUser(testNew);
        }
        s_logger.info("After Modfiy ----->");
    }
  
    public void testDelete()
    {
        s_logger.info("Before Delete ==="); 
        Collection c = getAll();
        Iterator it = c.iterator();
        int i = 1;        
        testAll();
        while(it.hasNext())
        {
            
            i ++;
            if (i%2 ==1) {
                it.next();
                continue;                
            }
            DocumentumUserInfo test = (DocumentumUserInfo)it.next();                      
            long uid = test.getId();
            remove(test);
            assertNull(find(uid));            
        }        
        testAll();
        s_logger.info("After Delete ===");
    }
    
    public void testCreate()
    {
        s_logger.info("Before Create ===");
        for (int i = 0; i < testNumber; i++)
        {
            DocumentumUserInfo test = null;
            userInfo = new DocumentumUserInfo();
            userInfo.setDocumentumUserId(ids[i]);
            userInfo.setDocumentumPassword(pwds[i]);
            userInfo.setDocumentumDocbase(ids[i] + pwds[i]);
            create(userInfo);
            test = find(userInfo.getId());
            assertEquals(test.getDocumentumUserId(), ids[i]);
            assertEquals(test.getDocumentumPassword(), pwds[i]);
            assertEquals(test.getDocumentumDocbase(), ids[i] + pwds[i]);
            s_logger.info("test[" + i + "] is id:"
                    + test.getDocumentumUserId() + " pwd:"
                    + test.getDocumentumPassword());
            aids[i] = String.valueOf(test.getId());
        }
        s_logger.info("After Create ===");
    }

    private DocumentumUserInfo create(DocumentumUserInfo p_userInfo)
    {
        try
        {
            mgr.createDocumentumUserInfo(p_userInfo);
        } catch (RemoteException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DocumentumPersistenceManagerException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return userInfo;
    }

    private DocumentumUserInfo modify(DocumentumUserInfo p_userInfo)
    {
        DocumentumUserInfo test = null;
        try
        {
            test = mgr.modifyDocumentumUserInfo(p_userInfo);
        } catch (RemoteException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DocumentumPersistenceManagerException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return test;
    }

    private DocumentumUserInfo find(long id)
    {
        DocumentumUserInfo test = null;
        try
        {
            test = mgr.findDocumentumUserInfo(String.valueOf(id));
        } catch (RemoteException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DocumentumPersistenceManagerException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return test;
    }

    private void remove(DocumentumUserInfo p_userInfo)
    {
        boolean test = false;
        try
        {
            test = mgr.removeDocumentumUserInfo(p_userInfo);
        } catch (RemoteException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DocumentumPersistenceManagerException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertTrue(test);
    }

    private Collection getAll()
    {
        try
        {
            return mgr.getAllDocumentumUserInfos();
        } catch (RemoteException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DocumentumPersistenceManagerException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    private void printUser(DocumentumUserInfo userInfo)
    {
        s_logger.info("userInfo is id:"
                + userInfo.getId() + " name:"
                + userInfo.getDocumentumUserId() + " pwd:"
                + userInfo.getDocumentumPassword() + " docbase:"
                + userInfo.getDocumentumDocbase()
                );
    }
}
