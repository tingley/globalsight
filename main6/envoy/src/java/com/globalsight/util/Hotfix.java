package com.globalsight.util;

import java.util.Comparator;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Hotfix {


	private String name;
	private String version;
	private String date;
	private String dependency;
	private String description;
	private String sequence;
	private boolean installed;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDependency() {
		return dependency;
	}

	public void setDependency(String dependency) {
		this.dependency = dependency;
	}

	public boolean getInstalled() {
		return installed;
	}

	public void setInstalled(boolean installed) {
		this.installed = installed;
	}
	
	public static Comparator<Hotfix> getComparator() {
		return new Comparator<Hotfix>() {

			@Override
			public int compare(Hotfix o1, Hotfix o2) {

				String v1 = o1.getSequence();
				String v2 = o2.getSequence();

				if (v1 == null || v2 == null)
					return -1;
				
				String[] vv1 = v1.split("\\.");
				String[] vv2 = v2.split("\\.");

				int n1 = Integer.parseInt(vv1[vv1.length - 1]);
				int n2 = Integer.parseInt(vv2[vv2.length - 1]);

				return n1 - n2;
			}
		};
	}
}
