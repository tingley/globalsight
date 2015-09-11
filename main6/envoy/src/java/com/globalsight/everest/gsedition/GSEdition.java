package com.globalsight.everest.gsedition;

import com.globalsight.everest.persistence.PersistentObject;

public class GSEdition extends PersistentObject{

    /**
     * 
     */
    private static final long serialVersionUID = 8874587622069814431L;
    
    private String name = "";
    private String host_name = "";
    private String host_port = "";
    private String userName = "";
    private String password = "";
    private long company_id;
    private String description = "";
    private boolean enableHttps = false;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getHostName() {
        return host_name;
    }
    public void setHostName(String host_name) {
        this.host_name = host_name;
    }
    
    public String getHostPort() {
        return host_port;
    }
    
    public void setHostPort(String host_port) {
        this.host_port = host_port;
    }
    
    public String getUserName() {
        
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getPassword() {
        
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public long getCompanyID() {
        
        return company_id;
    }
    
    public void setCompanyID(long company_id) {
        this.company_id = company_id;
    }
    
    public String getDescription() {
        
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setEnableHttps(boolean enableHttps)
    {
    	this.enableHttps = enableHttps;
    }
    
    public boolean getEnableHttps()
    {
    	return this.enableHttps;
    }
}
