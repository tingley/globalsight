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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * A util class, provide some methods to convert java object and xml file.
 * <p>
 * For example, you can save the value of a jave object to a xml file, or load
 * the saved java object from a xml file.
 * <p>
 * Make sure that the xml file is created used the util, others may be some
 * error will throw out.
 */
public class XmlUtil
{
    /**
     * Loads saved java object from <code>file</code>.
     * 
     * @param <T>
     *            The class of object. Can't be null.
     * @param clazz
     *            The class of object. Can't be null.
     * @param file
     *            The xml file to load. Can't be null and must exist.
     * @return The loaded object.
     */
    @SuppressWarnings("unchecked")
    public static <T> T load(Class<T> clazz, String file)
    {
        FileReader fr = null;
        T ob = null;
        try
        {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller um = context.createUnmarshaller();
            fr = new FileReader(file);
            ob = (T) um.unmarshal(fr);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (fr != null)
            {
                try
                {
                    fr.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return ob;
    }

    /**
     * Translates object to string.
     * 
     * @param ob
     * @param format
     * @return
     */
    public static String object2String(Object ob, boolean format)
    {
        JAXBContext context;
        try
        {
            context = JAXBContext.newInstance(ob.getClass());
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, format);
            StringWriter s = new StringWriter();
            m.marshal(ob, s);
            return s.toString();
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }

        return "";
    }
    
    /**
     * Translates object to string.
     * @param ob
     * @return
     */
    public static String object2String(Object ob)
    {
        return object2String(ob, false);
    }
    
    /**
     * Translates String to Object.
     * @param ob
     * @return 
     * @return
     */
    public static <T> T string2Object(Class<T> clazz, String xml)
    {
        T ob = null;
        try
        {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller um = context.createUnmarshaller();
            StringReader s = new StringReader(xml);
            ob = (T) um.unmarshal(s);
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }

        return ob;
    }
    
    /**
     * Saves a java object to a xml file. If the file has been exist, it will be
     * covered.
     * 
     * @param ob
     *            The java object to save. Can't be null.
     * @param filePath
     *            The path of xml file.
     */
    public static void save(Object ob, String filePath)
    {
        FileWriter fw = null;
        try
        {
            JAXBContext context = JAXBContext.newInstance(ob.getClass());
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            fw = new FileWriter(filePath);
            m.marshal(ob, fw);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (fw != null)
            {
                try
                {
                    fw.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
