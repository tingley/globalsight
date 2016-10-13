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

package com.globalsight.persistence;

public class SequenceStore
{
    private Long m_sequenceNumberValue;
    private Long m_numberOfObjects;

    public SequenceStore(long p_sequenceNumber, 
                         long p_numberOfObjects)
    {
        m_sequenceNumberValue = new Long(p_sequenceNumber);
        m_numberOfObjects = new Long(p_numberOfObjects);
    }

    public  long getSequenceNumberValue()
    {
        return m_sequenceNumberValue.longValue();
    }
    public long getNumberOfObjects()
    {
        return m_numberOfObjects.longValue();
    }
}
