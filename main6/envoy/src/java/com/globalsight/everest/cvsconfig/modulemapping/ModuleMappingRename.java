package com.globalsight.everest.cvsconfig.modulemapping;

import com.globalsight.everest.persistence.PersistentObject;

public class ModuleMappingRename extends PersistentObject{
	private static final long serialVersionUID = 3650531295651077329L;
	
	private String sourceName = "";
	private String targetName = "";
	private String moduleMappingId = "";
	private ModuleMapping moduleMapping;
	public String getSourceName() {
		return sourceName;
	}
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}
	public String getModuleMappingId() {
		return moduleMappingId;
	}
	public void setModuleMappingId(String moduleMappingId) {
		this.moduleMappingId = moduleMappingId;
	}
	public String getTargetName() {
		return targetName;
	}
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
	public ModuleMapping getModuleMapping() {
		return moduleMapping;
	}
	public void setModuleMapping(ModuleMapping moduleMapping) {
		this.moduleMapping = moduleMapping;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ModuleMappingRename::").append(sourceName).append(",").append(targetName).append(", ModuleMappingId==").append(moduleMappingId);
		return sb.toString();
	}
}
