package com.globalsight.cxe.entity.gitconnector;

import com.globalsight.everest.persistence.PersistentObject;

public class GitConnectorJob extends PersistentObject {
	
	private static final long serialVersionUID = -6550586794788689231L;
	
    private long gitConnectorId;
    private long jobId;
    
	public void setGitConnectorId(long gitConnectorId) {
		this.gitConnectorId = gitConnectorId;
	}
	
	public long getGitConnectorId() {
		return gitConnectorId;
	}
	
	public void setJobId(long jobId) {
		this.jobId = jobId;
	}
	
	public long getJobId() {
		return jobId;
	}
}
