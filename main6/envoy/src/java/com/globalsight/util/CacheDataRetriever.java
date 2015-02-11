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

/**
 * CacheDataRetriever provides a universal interface to get data to be
 * stored in a cache. Cache classes (MfuCache, MruCache) keep a
 * reference of the interface and call getData() method when the data
 * is requested but the data is not in the cache.
 */
public interface CacheDataRetriever
{
    Object getData(Object p_key)
        throws Exception;
}
