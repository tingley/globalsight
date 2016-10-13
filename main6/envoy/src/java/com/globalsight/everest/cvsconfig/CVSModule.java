package com.globalsight.everest.cvsconfig;

import com.globalsight.everest.persistence.PersistentObject;

public class CVSModule extends PersistentObject {
    private static final long serialVersionUID = -8905970137576301722L;
    public static final long PROTOCOL_PSERVER = 0;
    public static final long PROTOCOL_EXT = 1;
    
    private String name = "";
    private String modulename = "";
    private String branch = "";
    private CVSServer server = new CVSServer();
    private String realPath = "";
    private String lastCheckout = "";

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getModulename() {
        return modulename;
    }
    public void setModulename(String modulename) {
        this.modulename = modulename;
    }
	public CVSServer getServer() {
		return server;
	}
	public void setServer(CVSServer server) {
		this.server = server;
	}
	public String getRealPath() {
		return realPath;
	}
	public void setRealPath(String realPath) {
		this.realPath = realPath;
	}
	public String getLastCheckout() {
		return lastCheckout;
	}
	public void setLastCheckout(String lastCheckout) {
		this.lastCheckout = lastCheckout;
	}
	public String getBranch() {
		return branch;
	}
	public void setBranch(String branch) {
		this.branch = branch;
	}
    
}
