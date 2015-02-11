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
package com.globalsight.util.j2ee.jboss;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Enumeration;

/**
 * The UncacheableFileServlet can be use to view files using the
 * Jboss(??) file servlet, however it doesn't allow the files to be cached.
 * So when viewing them they read from the directory they are stored in.
 */
public class UncacheableFileServlet extends HttpServlet
{
    /**
    * Sets the response header to expire in 0 seconds and then lets the FileServlet
    * handle the request.
    * @param p_request -- the request
    * @param p_response -- the response
    * @throws ServletException, IOException
    */
    public void service(HttpServletRequest p_request, HttpServletResponse p_response)
        throws ServletException, IOException
    {
        throw new ServletException("Not implemented yet!!");
    }
}

