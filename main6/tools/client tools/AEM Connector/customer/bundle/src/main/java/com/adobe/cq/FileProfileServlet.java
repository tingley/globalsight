package com.adobe.cq;

import java.io.IOException;
import java.rmi.ServerException;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SlingServlet(paths = "/bin/fileProfile", methods = "GET", metatype = true)
public class FileProfileServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = 50629514175832001L;

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	protected void doGet(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServerException,
			IOException {

		String fp = this.getCustomerService().getXmlFileProfileExt();

		try {
			JSONObject obj = new JSONObject();
			obj.put("result", fp);
			String jsonData = obj.toString();
			response.getWriter().write(jsonData);
		} catch (Exception e) {
			log.error("Exception:", e);
		}
	}

	private CustomerService getCustomerService() {
		BundleContext ctx = FrameworkUtil.getBundle(this.getClass())
				.getBundleContext();
		ServiceReference serviceReference = ctx
				.getServiceReference(CustomerService.class.getName());
		return CustomerService.class.cast(ctx.getService(serviceReference));
	}

}
