package com.globalsight.everest.cvsconfig;

import java.util.Set;

import com.globalsight.everest.persistence.PersistentObject;

public class CVSRepository extends PersistentObject {   
    private static final long serialVersionUID = 102042100694034953L; 
    public static final long PROTOCOL_PSERVER = 0;
	public static final long PROTOCOL_EXT = 1;
	
	private String name = "";
	private String repository = "";
	private String folderName = "";
	private String loginUser = "";
	private String loginPwd = "";
	private String CVSRootEnv = "";
	private CVSServer m_server;
    private Set cvsModule;

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

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRepository() {
		return repository;
	}
	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getFolderName() {
		return folderName;
	}
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}	
	
    public CVSServer getServer()
    {
        return m_server;
    }
    
    public void setServer(CVSServer p_server)
    {
        m_server = p_server;
    }
    
    public Set<CVSModule> getModuleSet()
    {
        return cvsModule;
    }

    public void setModuleSet(Set<CVSModule> cvsModule)
    {
        this.cvsModule = cvsModule;
    }
}
