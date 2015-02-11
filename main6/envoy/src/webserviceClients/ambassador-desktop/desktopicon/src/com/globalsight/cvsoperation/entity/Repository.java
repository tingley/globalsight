package com.globalsight.cvsoperation.entity;

import com.globalsight.cvsoperation.util.CVSConfigureHelper;
import com.globalsight.util.Constants;

public class Repository {
	
	private String protocol = CVSConfigureHelper.getProperty(Constants.CVS_PROTOCOL);
	
	private String username = CVSConfigureHelper.getProperty(Constants.CVS_USERNAME);
	
	private String password = CVSConfigureHelper.getProperty(Constants.CVS_PASSWORD);

	private int index = 0;
	
	private String cvsServer = null;
	
	private int cvsServerPort;
	
	private String cvsRoot = null;
	
	private String cvsLocalBin = null;
	
	private String cvsSandbox = null;

	//true: all false:not all
	private boolean isAllFlag = true;
	
	public Repository(int index, String cvsServer, String cvsRoot, String cvsLocalBin, String cvsSandbox)
	{
		this.index = index;
		this.cvsServer = cvsServer;
		this.cvsRoot = cvsRoot;
		this.cvsLocalBin = cvsLocalBin;
		this.cvsSandbox = cvsSandbox;
	}
	
	public Repository(int index, String cvsServer, int cvsServerPort, String cvsRoot, String cvsLocalBin, String cvsSandbox)
	{
		this.index = index;
		this.cvsServer = cvsServer;
		this.cvsServerPort = cvsServerPort;
		this.cvsRoot = cvsRoot;
		this.cvsLocalBin = cvsLocalBin;
		this.cvsSandbox = cvsSandbox;
	}
	
	public void setIndex(int index)
	{
		this.index = index;
	}
	
	public int getIndex()
	{
		return this.index;
	}
	
	public void setCvsServer(String cvsServer)
	{
		this.cvsServer = cvsServer;
	}
	
	public String getCvsServer()
	{
		return this.cvsServer;
	}
	
	public void setCvsServerPort(int cvsServerPort)
	{
		this.cvsServerPort = cvsServerPort;
	}
	
	public int getCvsServerPort()
	{
		return this.cvsServerPort;
	}
	
	public void setCvsRoot(String cvsRoot)
	{
		this.cvsRoot = cvsRoot;
	}
	
	public String getCvsRoot()
	{
		return this.cvsRoot;
	}
	
	public void setCvsLocalBin(String cvsLocalBin)
	{
		this.cvsLocalBin = cvsLocalBin;
	}
	
	public String getCvsLocalBin()
	{
		return this.cvsLocalBin;
	}
	
	public void setCvsSandbox(String cvsSandbox)
	{
		this.cvsSandbox = cvsSandbox;
	}
	
	public String getCvsSandbox()
	{
		return this.cvsSandbox;
	}
	
	public void setIsAllFlag(boolean isAllFlag)
	{
		this.isAllFlag = isAllFlag;
	}
	
	public boolean getIsAllFlag()
	{
		return this.isAllFlag;
	}
	
	/**
	 * return the full cvs root
	 * For example: :protocal:username@cvs_server(or cvs_host):cvs_root
	 * @return
	 */
	public String getFullCVSRoot()
	{
		if ("ext".equalsIgnoreCase(this.protocol)) {
			return ":" + this.protocol + ":" + this.username + "@" + this.cvsServer + ":" + this.cvsRoot;
		} else {
			return ":" + this.protocol + ":" + this.username + ":" + this.password + "@" + this.cvsServer + ":" + this.cvsRoot;			
		}
	}
	
	public boolean equals(Object obj)
	{
		if (obj == this) {
			return true;
		}

		if (obj instanceof Repository) {
			Repository rep = (Repository) obj;
			return (rep.cvsServer.equals(this.cvsServer) && 
					rep.cvsRoot.equals(this.cvsRoot) &&
					rep.cvsSandbox.equals(this.cvsSandbox) && 
					rep.cvsLocalBin.equals(this.cvsLocalBin));
		}

		return false;
	}
	
	public String toString()
	{
		String strReturn = "";
		if ( isAllFlag )
		{
			try {
				strReturn = " :" + protocol.toLowerCase() + ":" + username + ":" + password + "@" +
				this.cvsServer + ":" + this.cvsRoot + ";" + this.cvsSandbox +
				";" + this.cvsLocalBin;
			} catch (Exception e) {
				strReturn = " :" + this.cvsServer + ":" + this.cvsRoot + ";" + this.cvsSandbox + ";" + this.cvsLocalBin;
			}			
		}
		else
		{
			strReturn = this.cvsRoot + " on '" + this.cvsServer + "' " + "(in directory:" + this.cvsSandbox + ")";
		}

		return strReturn;
	}
}
