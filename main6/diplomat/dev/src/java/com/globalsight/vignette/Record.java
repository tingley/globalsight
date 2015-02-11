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
package com.globalsight.vignette;


public class Record
{

    public Record()
    {
    }

    public Record(String s, String s1, String s2, String s3, String s4, String s5, String s6, 
            String s7)
    {
        server = s;
        database = s1;
        table = s2;
        key = s3;
        keyid = s4;
        name = s5;
        mid = s6;
        status = s7;
    }

    public String getServer()
    {
        return server;
    }

    public String getDatabase()
    {
        return database;
    }

    public String getTable()
    {
        return table;
    }

    public String getKey()
    {
        return key;
    }

    public String getKeyid()
    {
        return keyid;
    }

    public String getName()
    {
        return name;
    }

    public String getMid()
    {
        return mid;
    }

    public String getStatus()
    {
        return status;
    }

    String server;
    String database;
    String table;
    String key;
    String keyid;
    String name;
    String mid;
    String status;
}
