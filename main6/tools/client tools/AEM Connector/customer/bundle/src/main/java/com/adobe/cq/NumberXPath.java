package com.adobe.cq;

import org.dom4j.xpath.DefaultXPath;

public class NumberXPath extends DefaultXPath {

	 public NumberXPath(String text){  
	        super(text);  
	    }  
	    protected Object getCompareNumberValue(org.dom4j.Node node) {  
	        return numberValueOf(node);  
	    }  
}
