package com.adobe.cq.sling;

import java.util.ArrayList;
import java.util.List;

public class GSPageTranslation {
	
	private String PagePath;
	private List<PropertyContent> PropertyContentList = new ArrayList<PropertyContent>();
	
	public String getPagePath() {
		return PagePath;
	}
	
	public void setPagePath(String pagePath) {
		PagePath = pagePath;
	}
	
	public List<PropertyContent> getPropertyContentList() {
		return PropertyContentList;
	}
	
	public void setPropertyContentList(List<PropertyContent> propertyContentList) {
		PropertyContentList = propertyContentList;
	}
	
}
