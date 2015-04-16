package com.adobe.cq.sling;

import java.io.IOException;
import java.rmi.ServerException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SlingServlet(paths = "/bin/mySearchServlet", methods = "POST", metatype = true)
public class HandleClaim extends SlingAllMethodsServlet {

	private static final long serialVersionUID = 2598426539166789515L;
	
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Reference
	private SlingRepository repository;

	public void bindRepository(SlingRepository repository) {
		this.repository = repository;
	}

	@Override
	protected void doPost(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServerException,
			IOException {

		try {
			String nodePath = request.getParameter("nodePath");
			this.addtoCart(nodePath);
			
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
	
	private void addtoCart(String page) throws Exception {
		this.getCartService().add2Cart(page);
	}
	
	private CartService getCartService(){
		 BundleContext ctx = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
		 ServiceReference serviceReference = ctx.getServiceReference(CartService.class.getName());
		 return CartService.class.cast(ctx.getService(serviceReference));
	}
}
