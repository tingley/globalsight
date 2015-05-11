package com.globalsight.cxe.entity.gitconnector;

import com.globalsight.everest.persistence.PersistentObject;

public class GitConnectorCacheFile extends PersistentObject {

	private static final long serialVersionUID = -7041199332999519873L;
	
	private String filePath;
	private long gitConnectorId;
	private String srcFilePath;
	private String dstFilePath;
    
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

	public String getSrcFilePath() {
		return srcFilePath;
	}

	public void setSrcFilePath(String srcFilePath) {
		this.srcFilePath = srcFilePath;
	}

	public String getDstFilePath() {
		return dstFilePath;
	}

	public void setDstFilePath(String dstFilePath) {
		this.dstFilePath = dstFilePath;
	}
}
