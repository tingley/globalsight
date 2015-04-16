package com.adobe.cq.sling;

import java.io.File;
import java.io.IOException;
import java.rmi.ServerException;

import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//This is a component so it can provide or consume services
@SlingServlet(paths = "/bin/upfile", methods = "POST", metatype = true)
@Property(name = "sling.auth.requirements", value = "-/bin/upfile")
public class HandleFile extends
		org.apache.sling.api.servlets.SlingAllMethodsServlet {
	private static final long serialVersionUID = 2598426539166789515L;


	/** Default log. */
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	// Inject a Sling ResourceResolverFactory
	@Reference
	private ResourceResolverFactory resolverFactory;

	@Override
	protected void doGet(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServerException,
			IOException {
		this.doPost(request, response);
	}

	@Override
	protected void doPost(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServerException,
			IOException {
		try {
			synchronized (this) {
				String filePathName = request.getParameter("filePathName");
				log.info("Http client post parameter filePathName: " + filePathName);
				
				if (filePathName == null) {
					log.error("Sorry, file path name is null!");
					return;
				}
				File targetFile = new File(filePathName);
	
				if (targetFile.exists()) {
					log.info("Import operation finds the target file: " + targetFile.getAbsolutePath());
					this.getCartService().importTargetFile(targetFile);
				} else {
					log.error("Sorry, target file not existed as a result import job file exited! Please check the httpclient post parameter");
				}
			}
		} catch (Exception e) {
			log.error("Exception: ", e);
		}

	}
	
	private CartService getCartService(){
		BundleContext ctx = FrameworkUtil.getBundle(this.getClass())
				.getBundleContext();
		ServiceReference serviceReference = ctx
				.getServiceReference(CartService.class.getName());
		return CartService.class.cast(ctx.getService(serviceReference));
	}

}