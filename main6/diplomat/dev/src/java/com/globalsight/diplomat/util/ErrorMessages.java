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
package com.globalsight.diplomat.util;

/**The ErrorMessages class contains the error message codes for the localized error
**messages that can be sent by CXE to CAP. The actual messages for these codes are
**in as-SSL/lib/nls/<locale>/*.txt. This class has messages for cxe_errors.txt
*/
public class ErrorMessages
{
    public static String DATABASE_ERROR = "cxe001";
    public static String EFXML_ERROR = "cxe002";
    public static String IO_ERROR = "cxe003";
    public static String EXTRACTION_ERROR = "cxe004";
}
