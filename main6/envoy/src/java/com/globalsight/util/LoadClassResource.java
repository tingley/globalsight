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

import org.apache.log4j.Logger;

import com.globalsight.util.GeneralException;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.WhereIsClassInPath;


import java.net.URL;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;



/**
 * Standalone utility for loading a class a printing out
 * what it consists of, and finding a URL
 * to a resource associated with the class.
 * Requires the fully qualified class name (e.g. foo.bar.Howdy).
 */
public class LoadClassResource 
{
     private static final Logger CATEGORY =
            Logger.getLogger(
            LoadClassResource.class.getName());

    public static void main(String[] args) throws Throwable
	{
        if (args.length != 1 && args.length != 2) 
		{
            System.out.println("Loads a class and displays what it consists of, or does the same and also displays a URL to a resource associated with the class");
            System.out.println("USAGE:   java " +
                     LoadClassResource.class.getName() + " <fully qualified class name> [<resource name>]");
            return;
        }
        if (args.length == 2)
            System.out.println(loadClassResource(args[0], args[1]));
        else
            System.out.println(loadClassResource(args[0],null));
     }

	/**
	 * Load a class and return
     * what it consists of, and find a URL
     * to a resource associated with the class.
	 * @param p_fullClassName fully qualified class name to find.
     * @param p_resource Resource associated with the class to find.
     * May be null.
	 * @returns String containing what he class consists of, and
     * the URL to the resource, if any.
	 */
    public static String loadClassResource(String p_fullClassName,
            String p_resource) throws Throwable
	{
        StringBuffer buffer = new StringBuffer(2000);
        try
        {
            Class theClass = Class.forName(p_fullClassName);
            ClassLoader loader = theClass.getClassLoader();
            theClass = loader.loadClass(p_fullClassName);
            if (p_resource != null)
            {
                URL url = loader.getResource(p_resource);
                if (url == null)
                {
                    throw new RuntimeException();
                }
                buffer.append(url.toString()); 
                buffer.append(GlobalSightCategory.getLineContinuation());
            }
            buffer.append(p_fullClassName);
            buffer.append(" loaded");
            buffer.append(GlobalSightCategory.getLineContinuation());
            buffer.append(dumpClass(theClass));
            return buffer.toString();	  
	  	}
        catch (Throwable t)
        {
            String classpath = WhereIsClassInPath.getImplicitClassPath();
            buffer.append(p_fullClassName);
            buffer.append(" not loaded or resource ");
            buffer.append(p_resource);
            buffer.append(" not found from implicit classpath.");
            buffer.append(GlobalSightCategory.getLineContinuation());
            buffer.append(classpath);
            buffer.append(GlobalSightCategory.getLineContinuation());
            buffer.append("java home = ");
            buffer.append(System.getProperty("java.home"));
            buffer.append(GlobalSightCategory.getLineContinuation());
            buffer.append(t.getMessage());
            return buffer.toString(); 
        }
    }

    
    public static URL getResource(String p_fullClassName,
            String p_resource) throws Exception
    {
        Class theClass = Class.forName(p_fullClassName);
        ClassLoader loader = theClass.getClassLoader();
        theClass = loader.loadClass(p_fullClassName);
        if (p_resource != null)
        {
                return loader.getResource(p_resource);
        }
        return null;
    }
 

    /**
     * Returns the reflected parts of the class.
     */
    public static String dumpClass(Class p_class)
    {
        StringBuffer buffer = new StringBuffer(2000);
        buffer.append(p_class.getName());  
        buffer.append(GlobalSightCategory.getLineContinuation());
        Class[] classes = p_class.getClasses();
        buffer.append ("Classes ");
        for (int i = 0; i < classes.length; i++)
        {
             buffer.append(GlobalSightCategory.getLineContinuation());
             buffer.append("\t" + classes[i].getName());
        }
        buffer.append(GlobalSightCategory.getLineContinuation());
        Class[] interfaces = p_class.getInterfaces();
        buffer.append ("Interfaces ");
        for (int i = 0; i < interfaces.length; i++)
        {
            buffer.append(GlobalSightCategory.getLineContinuation());
            buffer.append("\t" + interfaces[i].getName());
        }
        buffer.append(GlobalSightCategory.getLineContinuation());
        Class superClass = p_class.getSuperclass();
        buffer.append ("Super class " + (superClass!=null?
                superClass.getName():"null"));
        buffer.append(GlobalSightCategory.getLineContinuation());
        buffer.append ("Class loader " + p_class.getClassLoader().toString());
        buffer.append(GlobalSightCategory.getLineContinuation());
        Constructor[] constructors = p_class.getConstructors();
        buffer.append ("Constructors ");
        for (int i = 0; i < constructors.length; i++)
        {
            buffer.append(GlobalSightCategory.getLineContinuation());
            buffer.append("\t" + constructors[i].toString());
        }
        buffer.append(GlobalSightCategory.getLineContinuation());
        Constructor[] declaredConstructors = p_class.getDeclaredConstructors();
        buffer.append ("Declared Constructors ");
        for (int i = 0; i < declaredConstructors.length; i++)
        {
            buffer.append(GlobalSightCategory.getLineContinuation());
            buffer.append("\t" + declaredConstructors[i].toString());
        }
        buffer.append(GlobalSightCategory.getLineContinuation());
        Field[] fields = p_class.getFields();
        buffer.append ("Fields ");
        for (int i = 0; i < fields.length; i++)
        {
            buffer.append(GlobalSightCategory.getLineContinuation());
            buffer.append("\t" + fields[i].toString());
        }
        buffer.append(GlobalSightCategory.getLineContinuation());
        Field[] declaredFields = p_class.getDeclaredFields();
        buffer.append ("Declared Fields ");
        for (int i = 0; i < declaredFields.length; i++)
        {
            buffer.append(GlobalSightCategory.getLineContinuation());
            buffer.append("\t" + declaredFields[i].toString());
        }
        buffer.append(GlobalSightCategory.getLineContinuation());
        Method[] methods = p_class.getMethods();
        buffer.append ("Methods ");
        for (int i = 0; i < methods.length; i++)
        {
            buffer.append(GlobalSightCategory.getLineContinuation());
            buffer.append("\t" + methods[i].toString());
        }
        return buffer.toString();
    }
}
