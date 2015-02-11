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
package com.globalsight.cxe.persistence.fileextension;

/**
 * Specifies the names of all the named queries for FileExtension.
 */
public interface FileExtensionQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return all file extensions.
     * <p>
     * Arguments: none.
     */
    public static String ALL_FILE_EXTENSIONS = "getAllFileExtensions";

    /**
     * A named query to return the file extension specified by the given id.
     * <p>
     * Arguments: 1: File Extension id.
     */
    public static String FILE_EXTENSION_BY_ID = "getFileExtensionById";

    /**
     * A named query to return the file extensions specified by the given ids.
     * <p>
     * Arguments: 1: List of File Extension ids.
     */
    public static String FILE_EXTENSIONS_BY_ID_LIST = "getFileExtensionsByIdList";
}
