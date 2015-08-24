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

package com.globalsight.everest.tuv;

import java.util.Date;

import org.apache.log4j.Logger;

/**
 * Representation of "translation_tu_tuv_attr_[companyId]" table.
 * 
 * @author YorkJin
 * @since 2015-03-06
 * @version 8.6.1
 */
public class TuTuvAttributeImpl
{
	private static Logger logger = Logger.getLogger(TuTuvAttributeImpl.class);

	/**
	 * Available values for "NAME" column
	 */
	public static final String SID = "SID";
	public static final String STATE = "STATE";

	/**
	 * Available values for "OBJECT_TYPE" column
	 */
	public static final String OBJECT_TYPE_TU = "TU";
	public static final String OBJECT_TYPE_TUV = "TUV";

	private long id = 0;
	private long objectId = 0;
	private String objectType = null;
	private String name = null;
	// store string type attribute less than 512
	private String varcharValue = null;
	// store string type attribute longer than 512
	private String textValue = null;
	// store int or long type attribute
	private long longValue = 0;
	// store date type attribute
	private Date dateValue = null;

	public TuTuvAttributeImpl()
	{
		
	}

	public TuTuvAttributeImpl(long objectId, String objectType,
			String name)
	{
		this.objectId = objectId;
		this.objectType = objectType;
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getObjectId() {
		return objectId;
	}

	public void setObjectId(long objectId) {
		this.objectId = objectId;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVarcharValue() {
		return varcharValue;
	}

	public void setVarcharValue(String varcharValue) {
		this.varcharValue = varcharValue;
	}

	public String getTextValue() {
		return textValue;
	}

	public void setTextValue(String textValue) {
		this.textValue = textValue;
	}

	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}

	public Date getDateValue() {
		return dateValue;
	}

	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}
}
