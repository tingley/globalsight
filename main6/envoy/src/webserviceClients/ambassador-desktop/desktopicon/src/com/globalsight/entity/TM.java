package com.globalsight.entity;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class is used for Project TM
 */

@XmlRootElement(name = "ProjectTM")
public class TM {

	private int id;
	private String name;
	private String domain;
	private String organization;
	private String description;
	
	public TM(){}
	
	public TM(int id, String name, String domain, String organization, String description) {
		this.id = id;
		this.name = name;
		this.domain = domain;
		this.organization = organization;
		this.description = description;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
