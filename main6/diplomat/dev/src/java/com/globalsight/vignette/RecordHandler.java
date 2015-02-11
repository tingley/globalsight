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

import java.util.Enumeration;
import java.util.Vector;

public class RecordHandler
{

    public RecordHandler()
    {
        records = new Vector(10);
    }

    public void addRecord(Record record)
    {
        records.addElement(record);
    }

    public void deleteRecord(String s)
    {
        for(Enumeration enumeration = getRecords(); enumeration.hasMoreElements();)
        {
            Record record = (Record)enumeration.nextElement();
            if(record.getMid().equals(s))
            {
                records.removeElement(record);
                break;
            }
        }

    }

    public void emptyRecordHandler()
    {
        records = new Vector(10);
    }

    public Enumeration getRecords()
    {
        return records.elements();
    }

    Vector records;
}
