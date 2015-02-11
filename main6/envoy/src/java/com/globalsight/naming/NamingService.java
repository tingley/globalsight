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

package com.globalsight.naming;

import java.util.Properties;
import java.util.Hashtable;
import java.rmi.*;
import javax.naming.*;
import org.apache.log4j.Logger;
import com.globalsight.util.GeneralException;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.util.j2ee.AppServerWrapperFactory;
import com.globalsight.util.j2ee.AppServerWrapper;

/**
 * Local front-end for the naming service.
 */
public final class NamingService 
{
    private static final Logger CATEGORY =
            Logger.getLogger(
            NamingService.class.getName());  

    private static Context s_context =
        AppServerWrapperFactory.getAppServerWrapper().getNamingContext();

    public static void main (String[] args) throws NamingException 
    {
        if (args.length != 1) {
            System.out.println("USAGE:   java " +
                    NamingService.class.getName() + " <context name>");
            return;
        }
        System.out.println(NamingService.getDump(args[0]));
     }


    public static void rebind(String p_name, Remote p_obj)
            throws NamingException 
    {
        s_context.rebind(p_name, p_obj);
    }


    public static Object lookup(String p_name)
	        throws NamingException 
    {
	    return(s_context.lookup(p_name));
    }


    public static String getDump(String p_namedContext) 
            throws NamingException 
    {
        return getDump(s_context, p_namedContext);
    }


    public static String getDump(Context p_context, String p_namedContext)
            throws NamingException 
    {
        StringBuffer dump = new StringBuffer(100);
        try 
        {
            NamingEnumeration bindings = p_context.listBindings(
                    p_namedContext);
            while (bindings.hasMore()) 
            {
                Binding binding = (Binding)bindings.next();
                dump.append("\"" + binding.getName()
                        + "\" --> " + binding.getClassName());
		        if (binding.getObject() instanceof Context) 
                {
                    dump.append("  {  " + 
                            getDump((Context)binding.getObject())
                            + "  }  ");
		        }
            }
            bindings.close();
        }
        catch (NamingException ne) 
        {
            CATEGORY.error(ne.getMessage(), ne);
            throw ne;
        }
        return dump.toString();
    }


    public static String getDump(Context p_context) 
            throws NamingException 
    {
        StringBuffer dump = new StringBuffer(100);
        try 
        {
            NamingEnumeration bindings = p_context.listBindings("");
            while (bindings.hasMore()) 
            { 
                Binding binding = (Binding)bindings.next();
                dump.append("\"" + binding.getName()
                        + "\" --> " + binding.getClassName());
		        if (binding.getObject() instanceof Context) 
                {
                    dump.append("  {  " + 
                            getDump((Context)binding.getObject())
                            + "  }  ");
		        }
            }
            bindings.close();
        }
        catch (NamingException ne) 
        {
            CATEGORY.error(ne.getMessage(), ne);
            throw ne;
        }
        return dump.toString();
    }
}



