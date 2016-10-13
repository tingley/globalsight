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
package com.globalsight.everest.edit.online.imagereplace;

/**
 * Specifies the names of all the named queries for ImageReplaceFileMap.
 */
public interface ImageReplaceFileMapQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return all image maps.
     * <p>
     * Arguments: 1: Image Map id.
     */
    public static String IMAGE_MAP_BY_ID =
        "getImageMapById";

    /**
     * A named query to return the image maps specified by the
     * given target page id.
     * <p>
     * Arguments: 1: Target Page id.
     */
    public static String IMAGE_MAPS_BY_TARGET_PAGE_ID =
        "getImageMapsByTargetPageId";

    /**
     * A named query to return the image map specified by the
     * given foreign keys
     * <p>
     * Arguments: 1: Target Page Id
     *            2: TUV id
     *            3: Sub id
     */
    public static String IMAGE_MAP_BY_EXTERNAL_IDS =
        "getImageMapByExternalIds";
}
