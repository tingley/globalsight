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
package com.plug.Version_8_3_1;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.util.db.Execute;

public class ContainerRoleExecute implements Execute {
	private String name;
	private int state;
	private Long activityId;
	private String sourceLocale;
	private String targetLocale;

	public ContainerRoleExecute(String name, int state, Long activityId,
			String sourceLocale, String targetLocale) {
		super();
		this.name = name;
		this.state = state;
		this.activityId = activityId;
		this.sourceLocale = sourceLocale;
		this.targetLocale = targetLocale;
	}

	@Override
	public void setValue(PreparedStatement st) {
		try {
			st.setString(1, name);
			st.setInt(2, state);
			st.setObject(3, activityId);
			st.setString(4, sourceLocale);
			st.setString(5, targetLocale);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}