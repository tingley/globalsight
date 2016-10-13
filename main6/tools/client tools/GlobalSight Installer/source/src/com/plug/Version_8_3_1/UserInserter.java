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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.plug.Version_8_3_0.User;
import com.plug.Version_8_3_0.UserUtil;
import com.util.db.DbUtil;
import com.util.db.DbUtilFactory;

public class UserInserter {
	
	private static Logger log = Logger.getLogger(UserInserter.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void inserter() throws SQLException {

		DbUtil util = DbUtilFactory.getDbUtil();
		try {
			List count = util.query("select count(id) from user");
			List c = (List) count.get(0);
			Long n = (Long) (c.get(0));
			if ( n > 0) {
				log.info("Not insert users");
				return;
			}
		} catch (SQLException e1) {
			log.error(e1);
		}

		List<User> users = UserUtil.getAllUsers();
		List<List> allUsers = new ArrayList<List>();

		for (User u : users) {

			List user = new ArrayList();

			String isInAllProjects = u.isInAllProjects() ? "Y" : "N";

			user.add(u.getUserId());
			user.add(u.getState());
			user.add(u.getUserName());
			user.add(u.getFirstName());
			user.add(u.getLastName());
			user.add(u.getTitle());
			user.add(u.getCompanyName());
			user.add(u.getPassword());
			user.add(getString(u.getEmail(), 900));
			user.add(getString(u.getCcEmail(), 900));
			user.add(getString(u.getBccEmail(), 900));
			user.add(u.getAddress());
			user.add(u.getDefaultLocale());
			user.add(u.getType());
			user.add(u.getPhoneNumber(User.PhoneType.OFFICE));
			user.add(u.getPhoneNumber(User.PhoneType.HOME));
			user.add(u.getPhoneNumber(User.PhoneType.CELL));
			user.add(u.getPhoneNumber(User.PhoneType.FAX));
			user.add(isInAllProjects);

			allUsers.add(user);
		}

		String sql = "insert into user"
				+ "(USER_ID, STATE, USER_NAME, FIRST_NAME, LAST_NAME, "
				+ "TITLE, COMPANY_NAME, PASSWORD, EMAIL, CC_EMAIL, "
				+ "BCC_EMAIL, ADDRESS, DEFAULT_LOCALE, TYPE, "
				+ "OFFICE_PHONE_NUMBER, HOME_PHONE_NUMBER, "
				+ "CELL_PHONE_NUMBER, FAX_PHONE_NUMBER, "
				+ "IN_ALL_PROJECTS) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		util.executeBatch(sql, allUsers);
		log.info("Inserted " + allUsers.size() + " records into USER");
	}
	
	private String getString(String s, int length)
	{
		if (s != null && s.length() > length)
		{
			s = s.substring(0, length);
		}
		
		return s;
	}
}
