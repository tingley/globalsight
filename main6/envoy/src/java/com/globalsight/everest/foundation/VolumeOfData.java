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
package com.globalsight.everest.foundation;

import java.io.Serializable;

/**
 * Represents an amount of data to be translated.  This amount is used
 * for criteria for automatically dispatching a job.
 */
public class VolumeOfData
    implements Serializable
{
    private static final long serialVersionUID = 892439266989575577L;
    private int m_volume;

    public VolumeOfData()
    {
        super();
    }

    /**
     * Set the context of the data's volume.
     * @param p_volume - The volume of the data.
     */
    public void setVolumeContext(int p_volume)
    {
        m_volume = p_volume;
    }

    public void setVolume(int p_volume)
    {
        m_volume = p_volume;
    }
    
    public int getVolume()
    {
        return m_volume;
    }

    /**
     * Return a string representation of the object.
     */
    public String toString()
    {
        return super.toString() +
            " m_volume=" + Integer.toString(m_volume);
    }
}



