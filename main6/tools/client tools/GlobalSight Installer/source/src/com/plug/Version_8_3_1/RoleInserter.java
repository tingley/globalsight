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
import java.util.Vector;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;

import com.config.properties.Resource;
import com.plug.Version_8_3_0.UserLdapUtil;
import com.ui.UI;
import com.ui.UIFactory;
import com.util.db.DbUtil;
import com.util.db.DbUtilFactory;
import com.util.ldap.LdapUtil;

public class RoleInserter {

	public static final String LDAP_ROLE_NAME_DELIMITER = " ";
	public static final String LDAP_ATTR_ACTIVITY = "activityType";
	public static final String LDAP_ATTR_SOURCE_LOCALE = "sourceLocale";
	public static final String LDAP_ATTR_TARGET_LOCALE = "targetLocale";
	public static final String LDAP_ATTR_COST = "cost";
	public static final String LDAP_ATTR_ROLE_TYPE = "roleType";
	public static final String LDAP_ATTR_ROLE_NAME = "cn";
	public static final String LDAP_ATTR_RATES = "rateId";
	public static final String LDAP_ATTR_MEMBERSHIP = "uniqueMember";
	public static final String LDAP_ATTR_STATUS = "status";
	private static final String LDAP_ROLE_TYPE_CONTAINER = "C";
	private static final String LDAP_ROLE_TYPE_USER = "U";

	public static final String LDAP_CREATED_STATUS = "CREATED";
	public static final String LDAP_ACTIVE_STATUS = "ACTIVE";
	public static final String LDAP_DELETED_STATUS = "DELETED";
	public static final String LDAP_DEACTIVE_STATUS = "DEACTIVE";

	static final int CREATED = 1;
	static final int ACTIVE = 2;
	static final int DEACTIVE = 3;
	static final int DELETED = 4;
	
	private static Logger log = Logger.getLogger(RoleInserter.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void insert() throws NamingException, SQLException {

		DbUtil util = DbUtilFactory.getDbUtil();
		try {
			List count = util.query("select count(id) from CONTAINER_ROLE");
			List c = (List) count.get(0);
			Long n = (Long) (c.get(0));
			if (n > 0) {
				log.info("Not insert roles");
				return;
			}
		} catch (SQLException e1) {
			log.error(e1);
		}

		String filter = UserLdapUtil.getSearchFilterForRole();
		DirContext dirContext = LdapUtil.checkOutConnection();
		SearchControls constraints = new SearchControls();
		constraints.setReturningObjFlag(true);
		constraints.setCountLimit(0);
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		constraints.setReturningAttributes(getSearchAttributeNames());
		NamingEnumeration res = dirContext.search(LdapUtil.ROLE_BASE_DN,
				filter, constraints);

		List<List> userRoles = new ArrayList<List>();
		List<List> containerRoleUserIds = new ArrayList<List>();
		List<List> containerRoleRates = new ArrayList<List>();
		
		UI ui = UIFactory.getUI();
		
		String msg = Resource.get("process.insertRole");
		ui.addProgress(0, msg);
		log.info(msg);

		int n = 0;
		while (res.hasMoreElements()) {

			Object searchResultObj = res.nextElement();
			if (searchResultObj instanceof SearchResult) {
				SearchResult tempSearchResult = (SearchResult) searchResultObj;
				Attributes entry = tempSearchResult.getAttributes();

				Attribute attr = entry.get(LDAP_ATTR_ROLE_TYPE);
				String roleType = getSingleAttributeValue(attr);

				attr = entry.get(LDAP_ATTR_ACTIVITY);
				String activityName = getSingleAttributeValue(attr);
				activityName = getSingleAttributeValue(attr);

				List ids = util
						.queryForSingleColumn("select id from activity a where a.name = '"
								+ activityName + "'");

				Long activityId = null;
				if (ids.size() > 0) {
					activityId = (Long) ids.get(0);
				}

				attr = entry.get(LDAP_ATTR_SOURCE_LOCALE);
				String sourceLocale = getSingleAttributeValue(attr);

				attr = entry.get(LDAP_ATTR_TARGET_LOCALE);
				String targetLocale = getSingleAttributeValue(attr);

				attr = entry.get(LDAP_ATTR_ROLE_NAME);
				String name = getSingleAttributeValue(attr);

				attr = entry.get(LDAP_ATTR_STATUS);
				String status = getSingleAttributeValue(attr);
				int state = getStateAsInt(status);

				if (roleType.equals(LDAP_ROLE_TYPE_USER)) {
					attr = entry.get(LDAP_ATTR_COST);
					String cost = getSingleAttributeValue(attr);

					String rate = getSingleAttributeValue(attr);

					attr = entry.get(LDAP_ATTR_MEMBERSHIP);
					String userDn = getSingleAttributeValue(attr);
					String userId = UserLdapUtil.parseUserIdFromDn(userDn);

					List userRole = new ArrayList();
					userRole.add(name);
					userRole.add(state);
					userRole.add(activityId);
					userRole.add(sourceLocale);
					userRole.add(targetLocale);
					userRole.add(userId);
					userRole.add(rate);
					userRole.add(cost);
					userRoles.add(userRole);

				} else if (roleType.equals(LDAP_ROLE_TYPE_CONTAINER)) {

					n++;

					String sql = "insert into CONTAINER_ROLE"
							+ "(NAME,STATE,ACTIVITY_ID,SOURCE_LOCALE,TARGET_LOCALE) values(?,?,?,?,?)";

					Long id = util.executeWithIds(
							sql,
							new ContainerRoleExecute(name, state, activityId,
									sourceLocale, targetLocale)).get(0);

					attr = entry.get(LDAP_ATTR_MEMBERSHIP);
					if (getMultiAttributeValue(attr) != null) {
						Vector userDns = getMultiAttributeValue(attr);

						for (int i = 0; i < userDns.size(); i++) {
							String userDn = (String) userDns.get(i);
							String userId = UserLdapUtil
									.parseUserIdFromDn(userDn);
							userId = userId.trim();

							if (userId.length() > 0) {
								List containerRoleUserId = new ArrayList();
								containerRoleUserId.add(id);
								containerRoleUserId.add(userId);
								containerRoleUserIds.add(containerRoleUserId);
							}
						}
					}

					attr = entry.get(LDAP_ATTR_RATES);
					if (getMultiAttributeValue(attr) != null) {

						Vector rates = getMultiAttributeValue(attr);
						for (int i = 0; i < rates.size(); i++) {
							Long rateId = Long.parseLong((String) rates.get(i));

							List containerRoleRate = new ArrayList();
							containerRoleRate.add(id);
							containerRoleRate.add(rateId);
							containerRoleRates.add(containerRoleRate);
						}
					}
				}
			}
		}

		res.close();

		String userRoleSql = "insert into USER_ROLE"
				+ "(NAME,STATE,ACTIVITY_ID,SOURCE_LOCALE,TARGET_LOCALE,USER,RATE,COST) values(?,?,?,?,?,?,?,?)";
		util.executeBatch(userRoleSql, userRoles);

		String containerRoleUserIdSql = "insert into CONTAINER_ROLE_USER_IDS"
				+ "(ROLE_ID,USER_ID) values(?,?)";
		util.executeBatch(containerRoleUserIdSql, containerRoleUserIds);

		String containerRoleRateSql = "insert into CONTAINER_ROLE_RATE"
				+ "(ROLE_ID,RATE_ID) values(?,?)";
		util.executeBatch(containerRoleRateSql, containerRoleRates);

		log.info("Inserted " + userRoles.size() + " records into USER_ROLE");
		log.info("Inserted " + n + " records into CONTAINER_ROLE");
		log.info("Inserted " + containerRoleUserIds.size() + " records into CONTAINER_ROLE_USER_IDS");
		log.info("Inserted " + containerRoleRates.size() + " records into CONTAINER_ROLE_RATE");
	}

	/**
	 * Conversion for changing the state to an int that is used by the UserImpl
	 * java class.
	 */
	private int getStateAsInt(String p_userState) {
		int state = 0;
		if (p_userState == null) {
			state = DELETED;
		} else if (p_userState.equals(LDAP_ACTIVE_STATUS)) {
			state = ACTIVE;
		} else if (p_userState.equals(LDAP_CREATED_STATUS)) {
			state = CREATED;
		} else if (p_userState.equals(LDAP_DEACTIVE_STATUS)) {
			state = DEACTIVE;
		} else {
			state = DELETED;
		}
		return state;
	}

	/**
	 * Get a Vector of String for an Attribute values.
	 * 
	 * @param p_attribute
	 *            - Attribute to be read
	 * @return A Vector of String
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Vector getMultiAttributeValue(Attribute p_attribute)
			throws NamingException {
		if (p_attribute == null) {
			return null;
		}

		NamingEnumeration attrValues = p_attribute.getAll();
		if (attrValues != null && attrValues.hasMoreElements()) {
			Vector v = new Vector();
			while (attrValues.hasMoreElements()) {
				Object strObj = attrValues.nextElement();
				v.addElement(strObj.toString());
			}

			attrValues.close();
			return v;
		} else {
			attrValues.close();
			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	private  String getSingleAttributeValue(Attribute p_attribute)
			throws NamingException {
		if (p_attribute == null) {
			return null;
		}
		NamingEnumeration attrValues = p_attribute.getAll();
		if (attrValues != null && attrValues.hasMoreElements()) {
			Object strObj = attrValues.nextElement();
			attrValues.close();
			return strObj.toString();
		} else {
			attrValues.close();
			return null;
		}
	}

	private String[] getSearchAttributeNames() {
		return new String[] { LDAP_ATTR_ACTIVITY, LDAP_ATTR_SOURCE_LOCALE,
				LDAP_ATTR_TARGET_LOCALE, LDAP_ATTR_COST, LDAP_ATTR_RATES,
				LDAP_ATTR_ROLE_TYPE, LDAP_ATTR_ROLE_NAME, LDAP_ATTR_MEMBERSHIP,
				LDAP_ATTR_STATUS };
	}

}
