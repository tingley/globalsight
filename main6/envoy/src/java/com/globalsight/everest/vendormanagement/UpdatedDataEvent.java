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
 
 package com.globalsight.everest.vendormanagement;

 // java
 import java.util.Date;

 // globalsight
 import com.globalsight.everest.foundation.User;


 public class UpdatedDataEvent
 {
     public static final int CREATE_EVENT = 1;
     public static final int UPDATE_EVENT = 2;
     public static final int DELETE_EVENT = 3;

     private int m_type;
     // the user who initiated the event.
     // Could be NULL if this was a system initiated rather than
     // a user initiated event.
     private User m_initiator;
     private Date m_eventDate;
     

     public UpdatedDataEvent(int p_type)
     {
         m_type = p_type;
         m_eventDate = new Date();
     }

     public UpdatedDataEvent(int p_type, User p_initiator)
     {
         m_type = p_type;
         m_initiator = p_initiator;
         m_eventDate = new Date();
     }

     public UpdatedDataEvent(int p_type, User p_initiator,
                             Date p_eventDate)
     {
         m_type = p_type;
         m_initiator = p_initiator;
         m_eventDate = p_eventDate;
     }
     
     public int getEventType()
     {
         return m_type;
     }

     public User getInitiator()
     {
         return m_initiator;
     }

     public String getInitiatorId()
     {
         if (m_initiator != null)
         {
             return m_initiator.getUserId();
         }
         else
         {
             return "";
         }
     }

     public Date getEventDate()
     {
         return m_eventDate;
     }
 }
