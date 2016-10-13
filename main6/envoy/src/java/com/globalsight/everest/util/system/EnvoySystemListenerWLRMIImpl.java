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
package com.globalsight.everest.util.system;

/* Copyright (c) 2000, GlobalSight Corporation. All Rights Preserved. */
/*
 * This file was automatically generated.
 * Generation date : October 10, 2000
 * Modified: Feb. 11, 2003
 */

import com.globalsight.everest.util.system.RemoteServer;

public class EnvoySystemListenerWLRMIImpl extends RemoteServer implements EnvoySystemListenerWLRemote
{
	EnvoySystemListener m_localReference;

	EnvoySystemListenerWLRMIImpl
            (EnvoySystemListener p_localReference) throws java.rmi.RemoteException
	{
		super();
		m_localReference = p_localReference;
	}
	public Object getLocalReference()
	{
		return m_localReference;
	}
	public  void shutdownSystem() throws java.rmi.RemoteException,com.globalsight.everest.util.system.SystemShutdownException
	{
		m_localReference.shutdownSystem();
	}
}
