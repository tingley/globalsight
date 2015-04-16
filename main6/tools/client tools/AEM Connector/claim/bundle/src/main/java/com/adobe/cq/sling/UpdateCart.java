package com.adobe.cq.sling;

import java.io.IOException;
import java.rmi.ServerException;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SlingServlet(paths = "/bin/updateCartServlet", methods = "POST", metatype = true)
public class UpdateCart extends SlingAllMethodsServlet {
	
	private static final long serialVersionUID = 1L;
	
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	protected void doPost(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServerException,
			IOException {
		try {
			String requestType = request.getParameter("requestType");
			String keys = request.getParameter("keys");
			log.info("requestType = " + requestType);
			
			if (requestType != null && "updateCart".equals(requestType)) {
				if (keys != null) {
					String[] keyArray = keys.split(",");
					updateCart(keyArray);
				}	
			} else if (requestType != null && "removeCart".equals(requestType)){
				if (keys != null) {
					String[] keyArray = keys.split(",");
					removeCart(keyArray);
				}	
			}
			
		} catch (Exception ex){
			log.error("Error:", ex);
		}
	}

	private void removeCart(String[] keyArray) {
		if (keyArray == null)
			return ;
		CartService cs = this.getCartService();

		for (String key : keyArray) {
			cs.removeCart(key);
		}
	}
	
	private void updateCart(String[] keyArray) {
		if (keyArray == null)
			return ;
		
		CartService cs = this.getCartService();
		cs.exportFileFromCart(keyArray);
	}
	
	private CartService getCartService(){
		BundleContext ctx = FrameworkUtil.getBundle(this.getClass())
				.getBundleContext();
		ServiceReference serviceReference = ctx
				.getServiceReference(CartService.class.getName());
		return CartService.class.cast(ctx.getService(serviceReference));
	}
}