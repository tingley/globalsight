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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;

/**
 * A util class, let operate object more easy.
 * 
 */
public class ObjectUtil
{
    static private final Logger logger = Logger.getLogger(ObjectUtil.class);
    
    /**
     * Depth clone the specified object.
     * 
     * @param ob
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepClone(T ob)
    {
        ByteArrayOutputStream  byteOut = new ByteArrayOutputStream();             
        ObjectOutputStream out;
        try
        {
            out = new ObjectOutputStream(byteOut);
            out.writeObject(ob);                    
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());             
            ObjectInputStream in = new ObjectInputStream(byteIn);  
            
            return (T) in.readObject();
        } 
        catch (Exception e)
        {
            logger.error(e);
        }             
        
        return null;
    }
}
