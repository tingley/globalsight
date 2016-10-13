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
package com.globalsight.util.database;

/* Copyright (c) 1999, Global Sight Corporation.  All rights reserved. */

/**
 * This exception is thrown when an attempt to load a JDBC driver
 * failed.
 * 
 * @version     1.0, (12/6/99 11:06:35 AM)
 * @author      Marvin Lau, mlau@globalsight.com
 * 
 * MODIFIED     MM/DD/YYYY
 * mlau         12/06/1999   Initial version.
 */
public class FailedToLoadDriverException extends Exception
{
/**
 * FailedToLoadDriverException constructor comment.
 */
public FailedToLoadDriverException() {
	super();
}
/**
 * FailedToLoadDriverException constructor comment.
 * @param s java.lang.String
 */
public FailedToLoadDriverException(String s) {
	super(s);
}
}
