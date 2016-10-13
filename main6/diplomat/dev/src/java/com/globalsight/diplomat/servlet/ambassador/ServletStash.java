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
package com.globalsight.diplomat.servlet.ambassador;

import java.util.HashMap;
import java.io.IOException;

import com.globalsight.diplomat.util.Logger;

/* Singleton class used to store "stuff" based on a string key. This allows
* the servlets working together for the GlobalSight interface (preview, save, etc.)
* to share information that cannot be stored in the Session object.	*/
public class ServletStash
{
    private static ServletStash theServletStash = null;
    private static Logger theLogger = Logger.getLogger();
    private HashMap m_hashmap = null;

   private ServletStash() {

       m_hashmap = new HashMap();
    }

    public static ServletStash getServletStash() {
	if (theServletStash == null)
	    theServletStash = new ServletStash();
	return theServletStash;
    }

   //only allow the item to be "removed" count times before really removing
   public Object remove (Object p_key)
   {
      synchronized(m_hashmap) {
         Object o = m_hashmap.get(p_key);
         if (o != null) {
            String countKey = p_key + "CT";
            Integer count = (Integer) m_hashmap.get(countKey);
            int countval = count.intValue() - 1;
            if (countval == 0)
               {
                  theLogger.println(Logger.DEBUG_D, "ServletStash: removing object " +
                                    p_key);
                  m_hashmap.remove(p_key);
                  m_hashmap.remove(countKey);
               }
            else
            {
               theLogger.println(Logger.DEBUG_D, "ServletStash: changing count for object " +
                                 p_key + " to " + countval);
               m_hashmap.put(countKey, new Integer(countval)); //replace the count
            }
         }
         return o;
      }
   }

   //just get the item from the Hashmap (ignore the associated count)
   public Object get (Object p_key)
   {
      synchronized(m_hashmap) {
         return m_hashmap.get(p_key);
      }
   }

   //put the item in the Hashmap and associate a count with it.
   //the count is the number of times the item can be "removed" from the stash
   //until it actually gets removed
   public Object put (Object p_key, Object p_val, int p_count)
   {
      synchronized(m_hashmap) {
         Integer count = new Integer(p_count);
         String countKey = p_key + "CT";
         m_hashmap.put(countKey,count);
         Object o = m_hashmap.put(p_key,p_val);
         theLogger.println(Logger.DEBUG_D, "ServletStash: putting object " +
                           p_key + " with count " + count);
         return o;
      }
   }
}
