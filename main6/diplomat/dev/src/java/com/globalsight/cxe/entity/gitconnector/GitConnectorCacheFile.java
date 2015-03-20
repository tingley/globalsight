package com.globalsight.cxe.entity.gitconnector;

import com.globalsight.everest.persistence.PersistentObject;

public class GitConnectorCacheFile extends PersistentObject {

	private static final long serialVersionUID = -7041199332999519873L;
	
	private String filePath;
	private long gitConnectorId;
    
	public void setGitConnectorId(long gitConnectorId) {
		this.gitConnectorId = gitConnectorId;
	}
	
	public long getGitConnectorId() {
		return gitConnectorId;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFilePath() {
		return filePath;
	}
}
