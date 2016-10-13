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
package com.globalsight.ling.docproc;

/**
 * class created by Jim Hargrave
 * copyright (C) 1999 - Global Sight.
 *                                                                         
 * Javadoc description goes here...
 *                                                                    
 * ========================================================= 
 *  This material is an unpublished work and trade secret which is the property
 *  of Global Sight, San Jose, California. It may not be disclosed, reproduced,
 *  adapted, merged, translated or used in any manner whatsoever without the
 *  prior consent of Global Sight Corporation.
 * =========================================================
 */
 
public class ExtractorMessage
{
	public String m_strMessage 	= null;
	public int m_iMessType 			= NOTE;

	// static message types
	public static final int WARNING 				= 0;
	public static final int ERROR		 				= 1;
	public static final int NOTE		 				= 2;
/**
 * ExtractorError constructor comment.
 */
public ExtractorMessage() {
	super();
}
}
