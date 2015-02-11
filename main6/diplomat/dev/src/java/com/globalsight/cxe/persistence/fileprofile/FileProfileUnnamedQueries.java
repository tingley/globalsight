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
package com.globalsight.cxe.persistence.fileprofile;

// globalsight

/**
 * FileProfileUnnamedQueries.  This class contains and builds
 * the queries that can't be be built ahead with a name.
 * @deprecated
 */
public class FileProfileUnnamedQueries
{
    //
    // PRIVATE STATIC VARIABLES
    //
    // private static Class s_entityClass = FileProfileImpl.class;
    //    
    // private static final String FILE_PROFILES_BY_EXT_SQL =
    // "select fp.* from file_profile fp, file_profile_extension fpe, extension
    // ext where " +
    // "fp.is_active = 'Y' and fp.id = fpe.file_profile_id and fpe.extension_id
    // = ext.id and ext.name in (";
    //
    // private static final String UNION_OF_SQL = " UNION ";
    //
    // private static final String FILE_PROFILES_ANY_EXT_SQL =
    // "select * from file_profile where is_active = 'Y' and id not in " +
    //        " (select distinct(file_profile_id) from file_profile_extension)";


    //
    // PUBLIC STATIC METHODS
    //

     /**  
      * Build a query that returns a list of file profiles that have at least
      * one of the extensions mentioned in it listed.
      */
    // public static ReadAllQuery getFileProfilesByExt(List p_extensions)
    // {
    // ReadAllQuery query = new ReadAllQuery(new ExpressionBuilder());
    // query.setReferenceClass(s_entityClass);
    //
    // StringBuffer sb = new StringBuffer(FILE_PROFILES_BY_EXT_SQL);
    //        
    // int size = p_extensions.size();
    // for (int i = 0 ; i < size ; i++)
    // {
    // sb.append("'");
    // String ext = (String)p_extensions.get(i);
    // sb.append(ext);
    // if (i < size - 1)
    // {
    // sb.append("',");
    // }
    // }
    //
    // sb.append("')");
    // sb.append(UNION_OF_SQL);
    // sb.append(FILE_PROFILES_ANY_EXT_SQL);
    //        query.setSQLString(sb.toString());
    //        return query;
    //    }
}
