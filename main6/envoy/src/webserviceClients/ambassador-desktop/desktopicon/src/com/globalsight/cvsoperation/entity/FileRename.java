package com.globalsight.cvsoperation.entity;

public class FileRename {
	private String ID = "";
	private String sourceFilename = "";
	private String targetFilename = "";
	public String getID() {
		return ID;
	}
	public void setID(String id) { 
		ID = id;
	}
	public String getSourceFilename() {
		return sourceFilename;
	}
	public void setSourceFilename(String sourceFilename) {
		this.sourceFilename = sourceFilename;
	}
	public String getTargetFilename() {
		return targetFilename;
	}
	public void setTargetFilename(String targetFilename) {
		this.targetFilename = targetFilename;
	}
}
