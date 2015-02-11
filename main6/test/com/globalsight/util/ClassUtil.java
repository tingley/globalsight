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

package com.globalsight.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import junit.framework.Assert;

/**
 * This class provide some method to simplify operation on private fields or
 * methods.
 * 
 * There is a example show how to use it.
 * 
 * <pre>
 * public class Demo 
 * { 
 * private String name = "demo";
 * 
 * private String demoMthod() { return "method 1"; }
 * 
 * private String demoMthod(int n) { return "method 2"; }
 * 
 * public String getName() { return name; } 
 * }
 * </pre>
 * <pre>
 * public class DemoTest 
 * { 
 * 
 * private Demo demo = new Demo();
 * 
 * @Test 
 * public void testDemoMthod() throws Exception 
 * {     
 *       String s1 = (String)ClassUtil.testMethod(demo, "demoMthod");
 *       // The first method has run
 *       Assert.assertEquals("method 1", s1);
 * 
 *       String s2 = (String) ClassUtil.testMethod(demo, "demoMthod", 1);
 *       // The second method has run
 *       Assert.assertEquals("method 2", s2);
 * 
 *       ClassUtil.updateField(demo, "name", "new name");
 *       // The private field has been updated
 *       Assert.assertEquals("new name", demo.getName()); 
 * } 
 * }
 * </pre>
 */
public class ClassUtil
{
    /**
     * Update a private field if the class not provide a set method.
     * 
     * @param Object
     *            The object the need to update field
     * @param name
     *            The field name
     * @param value
     *            The value that will set to the object
     * @throws Exception
     */
    public static void updateField(Object object, String name, Object value)
            throws Exception
    {
        Field f = object.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(object, value);
    }

    /**
     * Test a private method according to the method name and method parameters.
     * 
     * Note that the method will only check method name and method parameters
     * size. So there is some problem if there is another method that has same
     * name and same size of parameters as the method that you want to test
     * 
     * @param object
     *            The object that need to test
     * @param name
     *            The name of the method will be test
     * @param objects
     *            All parameters
     * @return
     * @throws Exception
     */
    public static Object testMethodWithException(Object object, String name,
            Object... objects) throws Exception
    {
        Method[] ms = object.getClass().getDeclaredMethods();
        Method rMethod = null;
        for (Method m : ms)
        {
            String mName = m.getName();
            if (mName.equals(name))
            {
                if (objects == null)
                {
                    if (m.getParameterTypes().length != 0)
                    {
                        continue;
                    }
                }
                else
                {
                    if (objects.length != m.getParameterTypes().length)
                    {
                        continue;
                    }
                }
                
                rMethod = m;
                break;
            }
        }

        Assert.assertNotNull("Can not find the method " + name, rMethod);

        rMethod.setAccessible(true);
        
        return rMethod.invoke(object, objects);
    }

    /**
     * Test a private method according to the method name and method parameters.
     * 
     * Note that the method will only check method name and method parameters
     * size. So there is some problem if there is another method that has same
     * name and same size of parameters as the method that you want to test
     * 
     * Note that if you want to handle the exception yourself, you can use
     * <code>testMethodWithException</code>
     * 
     * @param object
     * @param name
     * @param objects
     * @return
     * @see testMethodWithException
     */
    public static Object testMethod(Object object, String name,
            Object... objects)
    {
        try
        {
            return testMethodWithException(object, name, objects);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.assertFalse(true);
        }

        return null;
    }
}
