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
package com.globalsight.everest.workflow;

public class ScorecardScore {
	private long id;
	private String scorecardCategory;
	private int score;
	private long workflowId;
	private long jobId;
	private long companyId;
	private String modifyUserId;
	private boolean isActive = true;
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public void setScorecardCategory(String scorecardCategory) {
		this.scorecardCategory = scorecardCategory;
	}
	
	public String getScorecardCategory() {
		return scorecardCategory;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	public int getScore() {
		return score;
	}
	
	public void setWorkflowId(long workflowId) {
		this.workflowId = workflowId;
	}
	
	public long getWorkflowId() {
		return workflowId;
	}
	
	public void setCompanyId(long companyId) {
		this.companyId = companyId;
	}
	
	public long getCompanyId() {
		return companyId;
	}
	
	public void setModifyUserId(String modifyUserId) {
		this.modifyUserId = modifyUserId;
	}
	
	public String getModifyUserId() {
		return modifyUserId;
	}
	
	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	public boolean getIsActive() {
		return isActive;
	}

	public void setJobId(long jobId) {
		this.jobId = jobId;
	}

	public long getJobId() {
		return jobId;
	}	
}
