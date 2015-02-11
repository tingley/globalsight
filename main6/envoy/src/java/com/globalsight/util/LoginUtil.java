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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;

import javax.servlet.http.HttpServletRequest;

public class LoginUtil 
{
	public static List<String> TOKENS = new ArrayList<String>();
	private static String NAME = "token.login";
	
	
	 public static boolean isFromLoginPage(HttpServletRequest request)
	    {
	        Object token = request.getParameter(NAME);
	        if (TOKENS.contains(token))
	        {
	        	TOKENS.remove(token);
	        	return true;
	        }
	        
	        return false;
	    }
	
	public static void addSubmitToken(HttpServletRequest request)
    {
        String submitToken = generateToken();
        TOKENS.add(submitToken);
        request.setAttribute(NAME, submitToken);
        
        Timer timer = new Timer(); 
        timer.schedule(new Task(submitToken), 30 * 60 * 1000);
    }
	
	private static String generateToken()
    {
        String token = "";
        token += System.nanoTime();
        token += Math.abs(new Random(System.currentTimeMillis()).nextLong());
        return token;
    }
}
