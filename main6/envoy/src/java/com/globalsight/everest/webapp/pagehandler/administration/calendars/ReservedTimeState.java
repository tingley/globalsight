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
package com.globalsight.everest.webapp.pagehandler.administration.calendars;

import com.globalsight.calendar.ReservedTime;
import com.globalsight.util.GeneralException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 *  This class represents the state of a ReservedTime object.
 */
public class ReservedTimeState
{
    public static final int UNCHANGED = 0;
    public static final int REMOVED = 1;
    public static final int EDITED = 2;
    public static final int NEW = 3;

    private ReservedTime reservedTime;  // The reserved time
    private long id;  // The id of the reserved time.  This is needed because
                      // new reserved times are created for existing ones but
                      // the new one won't have the correct id, so store it here
    private int state;  // The state of the reserved time (see constants above)
    
    public ReservedTimeState(ReservedTime rt)
    {
        reservedTime = rt;
        state = UNCHANGED;
        id = rt.getId();
    }

    public void setState(int state)
    {
        this.state = state;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public int getState()
    {
        return state;
    }

    public long getId()
    {
        return id;
    }

    public ReservedTime getReservedTime()
    {
        return reservedTime;
    }
}
