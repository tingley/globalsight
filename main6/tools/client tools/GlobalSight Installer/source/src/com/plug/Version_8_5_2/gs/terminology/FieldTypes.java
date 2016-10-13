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

package com.plug.Version_8_5_2.gs.terminology;

/**
 * <p>This class defines the well-known field types like Domain and
 * Definition.</p>
 */
public interface FieldTypes
{
    static int FIELD_UNKNOWN = 0;

    // concept-level
    static int FIELD_CONCEPT_STATUS = 1;
    static int FIELD_DOMAIN = 2;
    static int FIELD_PROJECT = 3;

    // term-level
    static int FIELD_TERM_STATUS = 4;
    static int FIELD_TERM_TYPE = 5;
    static int FIELD_DEFINITION = 6;
    static int FIELD_CONTEXT = 7;
    static int FIELD_POS = 8;
}
