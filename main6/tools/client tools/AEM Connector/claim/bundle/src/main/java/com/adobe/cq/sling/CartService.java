package com.adobe.cq.sling;

import java.io.File;


public interface CartService {
	
	public void removeCart();
	
	public void removeCart(String key);
	
	public int add2Cart(String pagePath);
	
	public void exportFileFromCart();
	
	public void exportFileFromCart(String[] keyArray);
	
	public void importTargetFile(File targetFile);
	
	public String createTreeData();
}
