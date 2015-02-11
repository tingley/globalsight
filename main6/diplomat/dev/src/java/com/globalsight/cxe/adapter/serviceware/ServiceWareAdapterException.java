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
package com.globalsight.cxe.adapter.serviceware;

import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

/**
 * Exception class used by the ServiceWareAdapter
 */
public class ServiceWareAdapterException extends GeneralException
{
    //////////////////////////////////////
    // Constants                        //
    //////////////////////////////////////
    private static final String PROPERTYFILE = "ServiceWareAdapterMsg";

    //////////////////////////////////////
    // Constructors                     //
    //////////////////////////////////////

    /**
     * Creates a ServiceWareAdapterException
     * 
     * @param p_key  message key
     * @param p_args message args
     * @param p_exception
     *               actual exception
     */
    public ServiceWareAdapterException(String p_key, String[] p_args, Exception p_exception)
    {
	super(p_key,p_args,p_exception,PROPERTYFILE);
    }


    /**
     * Creates a ServiceWareAdapterException based on
     * a key, a filename, a logging category, and
     * an existing Exception
     * 
     * @param p_key      key in property file
     * @param p_filename displayname of a file
     * @param p_logger   a logging category
     * @param e          an Exception
     */
    public ServiceWareAdapterException(String p_key,
                                      String p_adapterName,
                                      String p_fileName,
                                      Exception e)
    {
        super(p_key,makeErrorArgs(p_adapterName,p_fileName),e,PROPERTYFILE);
    }


    /**
     * Creates a ServiceWareAdapterException based on
     * a key, a filename, an adapter name, and a
     * problem description
     * 
     * @param p_key      key in property file
     * @param p_adapterName
     *                   adapter name
     * @param p_filename display name of a file
     * @param p_problem  problem description
     */
    public ServiceWareAdapterException(String p_key, 
                                      String p_adapterName,
                                      String p_fileName,
                                      String p_problem)
    {
        super(p_key,makeErrorArgs(p_adapterName,p_fileName),
              new Exception(p_problem),PROPERTYFILE);
    }

    /**
     * Creates a ServiceWareAdapterException based on
     * a key, an adapter name, and an exception
     * 
     * @param p_key      key in property file
     * @param p_adapterName
     *                   adapter name
     * @param p_exception some exception
     */
    public ServiceWareAdapterException(String p_key, 
                                      String p_adapterName,
                                      Exception p_exception)
    {
        super(p_key, makeErrorArgs(p_adapterName), p_exception, PROPERTYFILE);
    }

    //////////////////////////////////////
    // Private Methods                  //
    //////////////////////////////////////

    /**
     * Creates an array of error args based on the adaptername
     * and the filename
     * 
     * @param p_adapterName
     *                   adapter name
     * @param p_fileName filename
     * @return String[]
     */
    private static String[] makeErrorArgs(String p_adapterName, String p_fileName)
    {
        String[] errorArgs = new String[2];
        errorArgs[0] = p_adapterName;
        errorArgs[1] = p_fileName;
        return errorArgs;
    }

    /**
     * Creates an array of error args based on the adaptername
     * 
     * @param p_adapterName
     *                   adapter name
     * @return String[]
     */
    private static String[] makeErrorArgs(String p_adapterName)
    {
        String[] errorArgs = new String[1];
        errorArgs[0] = p_adapterName;
        return errorArgs;
    }
}

