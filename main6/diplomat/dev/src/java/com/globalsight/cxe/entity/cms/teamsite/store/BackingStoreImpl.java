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
package com.globalsight.cxe.entity.cms.teamsite.store;
/*
 * Copyright (c) 2001 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */
import com.globalsight.everest.persistence.PersistentObject;

/** Implements an BackingStore */
public class BackingStoreImpl extends PersistentObject implements BackingStore
{
    private static final long serialVersionUID = 7333177870462671350L;

    //	PRIVATE MEMBERS

	/** Default constructor for TOPLink*/
    public BackingStoreImpl()
    {
        m_name = null;
    }

    /** Constructs an BackingStoreImpl with id, name**/
    //public BackingStoreImpl(long p_id, String p_name)
    public BackingStoreImpl(String p_name)
    {
	    m_name = p_name;
    }

    /** Constructs an BackingStoreImpl from an BackingStore **/
    public BackingStoreImpl(BackingStore o)
    {
	    this (o.getName());
    }

    /**
     ** Return the name of the Backing Store
     ** @return Backing Store name
     **/
    public String getName()
    {
    	return m_name;
    }

    /**
     ** Sets the name of the Backing Store
     **/
    public void setName(String p_name)
    {
	    m_name = p_name;
    }

    /** Returns a string representation of the object*/
    public String toString()
    {
	    return m_name;
    }

    /**
     * Return a string representation of the object for debugging purposes.
     * @return a string representation of the object for debugging purposes.
     */
    public String toDebugString()
    {
        return super.toString()  
                + " m_name=" 
                + (m_name == null ? "null" 
                : m_name) 
                ;
    }

    
}

