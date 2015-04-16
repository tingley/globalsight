package com.adobe.cq.sling;

public class PropertyContent {

	private String PropertyName;
	
	/** node ±¾ÉíµÄpath*/
	private String NodePath;
	
	private String text;
	
	//reference
	private String RefPath = null;
	
	
	public PropertyContent(){
		super();
	}

	public String getPropertyName() {
		return PropertyName;
	}

	public void setPropertyName(String propertyName) {
		PropertyName = propertyName;
	}

	public String getNodePath() {
		return NodePath;
	}

	public void setNodePath(String nodePath) {
		NodePath = nodePath;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getRefPath() {
		return RefPath;
	}

	public void setRefPath(String refPath) {
		RefPath = refPath;
	}

	
	
}
