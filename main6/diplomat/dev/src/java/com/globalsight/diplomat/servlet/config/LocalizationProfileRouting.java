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

package com.globalsight.diplomat.servlet.config;

public class LocalizationProfileRouting
{
	private int m_stage = 0;
	private int m_sequence = 0;
	private long m_user = 0;
	private int m_duration = 0;
	
	public LocalizationProfileRouting (int p_stage, int p_sequence, long p_user, int p_duration)
	{
		m_stage = p_stage;
		m_sequence = p_sequence;
		m_user = p_user;
		m_duration = p_duration;	
	}
	
	public int getStage() { return m_stage; }
	public int getSequence() { return m_sequence; }
	public long getUser() { return m_user; }
	public int getDuration() { return m_duration; }
	
	public void setStage(int p_stage) { m_stage = p_stage; }
	public void setSequence(int p_sequence) { m_sequence = p_sequence; }
	public void setUser(long p_user) { m_user = p_user; }
	public void setDuration(int p_duration) { m_duration = p_duration; }
}