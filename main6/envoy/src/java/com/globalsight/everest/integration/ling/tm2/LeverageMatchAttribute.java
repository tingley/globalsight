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
package com.globalsight.everest.integration.ling.tm2;

import java.util.Date;

/**
 * Representation of "leverage_match_attr_[companyId]" table.
 * 
 * @author YorkJin
 * @since 2015-03-06
 * @version 8.6.1
 */
public class LeverageMatchAttribute
{
	private long id = 0;
    private Long sourcePageId;
    private long originalSourceTuvId;
    private String subid;
    private short orderNum = 0;
    private long targetLocaleId;
    private String name = null;
	// store string type attribute less than 512
	private String varcharValue = null;
	// store string type attribute longer than 512
	private String textValue = null;
	// store int or long type attribute
	private long longValue = 0;
	// store date type attribute
	private Date dateValue = null;

	/**
	 * Available values for "NAME" column
	 */
	public static final String SID = "SID";

	public LeverageMatchAttribute(long sourcePageId, long originalSourceTuvId,
			String subId, short orderNum, long targetLocaleId)
	{
		this.sourcePageId = sourcePageId;
		this.originalSourceTuvId = originalSourceTuvId;
		this.subid = subId;
		this.orderNum = orderNum;
		this.targetLocaleId = targetLocaleId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Long getSourcePageId() {
		return sourcePageId;
	}

	public void setSourcePageId(Long sourcePageId) {
		this.sourcePageId = sourcePageId;
	}

	public long getOriginalSourceTuvId() {
		return originalSourceTuvId;
	}

	public void setOriginalSourceTuvId(long originalSourceTuvId) {
		this.originalSourceTuvId = originalSourceTuvId;
	}

	public String getSubid() {
		return subid;
	}

	public void setSubid(String subid) {
		this.subid = subid;
	}

	public short getOrderNum() {
		return orderNum;
	}

	public void setOrderNum(short orderNum) {
		this.orderNum = orderNum;
	}

	public long getTargetLocaleId() {
		return targetLocaleId;
	}

	public void setTargetLocaleId(long targetLocaleId) {
		this.targetLocaleId = targetLocaleId;
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

	// Utility
	public String getUniqueKey()
	{
		return this.getOriginalSourceTuvId() + "_" + this.getSubid() + "_"
				+ this.getOrderNum() + "_" + this.getTargetLocaleId();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof LeverageMatchAttribute)
		{
			LeverageMatchAttribute other = (LeverageMatchAttribute) obj;
			return this.getOriginalSourceTuvId() == other
					.getOriginalSourceTuvId()
					&& this.getTargetLocaleId() == other.getTargetLocaleId()
					&& this.getSubid().equals(other.getSubid())
					&& this.getOrderNum() == other.getOrderNum();
		}

		return false;
	}
}
