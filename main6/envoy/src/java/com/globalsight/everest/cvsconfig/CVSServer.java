package com.globalsight.everest.cvsconfig;

import java.util.Set;
import com.globalsight.everest.persistence.PersistentObject;

public class CVSServer extends PersistentObject {
	private static final long serialVersionUID = -1130474684481496767L;
	
	public static final long PROTOCOL_PSERVER = 0;
	public static final long PROTOCOL_EXT = 1;
	
	private String name = "";
	private String hostIP = "127.0.0.1";
	private int hostPort = 2401;
	private String sandbox = "";
	private long companyId = 0;
	private int protocol = 0;
	private String repository = "";
	private String loginUser = "";
	private String loginPwd = "";
	private String CVSRootEnv = "";
	private Set modules;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getHostIP() {
		return hostIP;
	}
	public void setHostIP(String hostIP) {
		this.hostIP = hostIP;
	}
	public int getHostPort() {
		return hostPort;
	}
	public void setHostPort(int hostPort) {
		this.hostPort = hostPort;
	}
	public String getSandbox() {
		return sandbox;
	}
	public void setSandbox(String sandbox) {
		this.sandbox = sandbox;
	}
	public long getCompanyId() {
		return companyId;
	}
	public void setCompanyId(long companyID) {
		this.companyId = companyID;
	}
	public int getProtocol() {
		return protocol;
	}
	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}
	
    public Set<CVSModule> getModuleSet()
    {
        return modules;
    }

    public void setModuleSet(Set<CVSModule> modules)
    {
        this.modules = modules;
    }
	public String getRepository() {
		return repository;
	}
	public void setRepository(String repository) {
		this.repository = repository;
	}
	public String getLoginUser() {
		return loginUser;
	}
	public void setLoginUser(String loginUser) {
		this.loginUser = loginUser;
	}
	public String getLoginPwd() {
		return loginPwd;
	}
	public void setLoginPwd(String loginPwd) {
		this.loginPwd = loginPwd;
	}
	public String getCVSRootEnv() {
		return CVSRootEnv;
	}
	public void setCVSRootEnv(String cVSRootEnv) {
		CVSRootEnv = cVSRootEnv;
	}
}
