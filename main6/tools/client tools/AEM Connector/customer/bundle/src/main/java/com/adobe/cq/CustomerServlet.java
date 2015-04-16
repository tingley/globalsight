package com.adobe.cq;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.ServerException;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SlingServlet(paths = "/bin/customer", methods = "GET", metatype = true)
public class CustomerServlet extends SlingAllMethodsServlet {
	
	private static final long serialVersionUID = 2598426539166789515L;

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	protected void doGet(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServerException,
			IOException {

		response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
		int result = this.getCustomerService().hasAccessToken();
		String ip = getServerIpAddr();

		JSONWriter jw = new JSONWriter(response.getWriter());
		try {
			jw.array();
			
			jw.object();
			jw.key("result");
			jw.value(result);
			jw.endObject();
			
			//ip address 2015.03.18
			jw.object();
			jw.key("ipAddress");
			jw.value(ip);
			jw.endObject();
			
			jw.endArray();
		} catch (JSONException e) {
			log.error("JSON Exception:", e);
		}
	}

	private String getServerIpAddr() {
		InetAddress ip;
		try {
			ip = InetAddress.getLocalHost();
			log.info("The server IP address : " + ip.getHostAddress());
			return ip.getHostAddress();
		} catch (UnknownHostException e) {
			log.error("get server ipAddress error: ", e);
		}
		return "";
	}
	
	private CustomerService getCustomerService() {
		BundleContext ctx = FrameworkUtil.getBundle(this.getClass())
				.getBundleContext();
		ServiceReference serviceReference = ctx
				.getServiceReference(CustomerService.class.getName());
		return CustomerService.class.cast(ctx.getService(serviceReference));
	}

}
