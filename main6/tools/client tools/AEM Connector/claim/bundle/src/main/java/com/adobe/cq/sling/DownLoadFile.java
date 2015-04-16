package com.adobe.cq.sling;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.ServerException;

import javax.servlet.ServletContext;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SlingServlet(paths = "/bin/downloadServlet", methods = "GET", metatype = true)
public class DownLoadFile extends SlingAllMethodsServlet {
	private static final long serialVersionUID = -8030544001592533460L;

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	private final static String filePath = "GlobalSightSourceFile\\source_temp.xml";

	protected void doGet(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServerException,
			IOException {
		try {
			File downloadFile = new File(filePath);
			FileInputStream inStream = new FileInputStream(downloadFile);

			// if you want to use a relative path to context root:
			String relativePath = getServletContext().getRealPath("");
			log.info("relativePath = " + relativePath);

			// obtains ServletContext
			ServletContext context = getServletContext();

			// gets MIME type of the file
			String mimeType = context.getMimeType(filePath);
			if (mimeType == null) {
				// set to binary type if MIME mapping not found
				mimeType = "application/octet-stream";
			}

			log.info("MIME type: " + mimeType);

			// modifies response
			response.setContentType(mimeType);
			response.setContentLength((int) downloadFile.length());

			// forces download
			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"",
					downloadFile.getName());
			response.setHeader(headerKey, headerValue);

			// obtains response's output stream
			OutputStream outStream = response.getOutputStream();

			byte[] buffer = new byte[4096];
			int bytesRead = -1;

			while ((bytesRead = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, bytesRead);
			}

			inStream.close();
			outStream.close();

		} catch (Exception ex) {
			log.error("Error:", ex);
		}
	}

	// protected void doGet(SlingHttpServletRequest request,
	// SlingHttpServletResponse response) throws ServerException,
	// IOException {
	//
	// try {
	// // Invoke the adaptTo method to create a Session
	// ResourceResolver resourceResolver = resolverFactory
	// .getAdministrativeResourceResolver(null);
	// session = resourceResolver.adaptTo(Session.class);
	//
	// // create query description as hash map (simplest way, same as form
	// // post)
	// Map<String, String> map = new HashMap<String, String>();
	//
	// // set QueryBuilder search criteria
	// // map.put("type", "dam:Asset");
	// map.put("type", "nt:file");
	// map.put("path", "/content/dam/cart");
	// // map.put("property.value", "image/png");
	//
	// builder = resourceResolver.adaptTo(QueryBuilder.class);
	//
	// // INvoke the Search query
	// Query query = builder.createQuery(PredicateGroup.create(map),
	// session);
	//
	// SearchResult sr = query.getResult();
	//
	// // write out to the AEM Log file
	// log.info("Search Results: " + sr.getTotalMatches());
	//
	// List<String> dataList = new ArrayList<String>();
	//
	// // iterating over the results
	// for (Hit hit : sr.getHits()) {
	//
	// // Convert the HIT to an asset - each asset will be placed into
	// // a ZIP for downloading
	// String path = hit.getPath();
	// log.info("Path : " + path);
	// dataList.add(path);
	// }
	//
	// // ZIP up the AEM DAM Assets
	// byte[] zip = zipFiles(dataList);
	//
	// log.info("zip length: " + zip.length);
	//
	// ServletOutputStream sos = response.getOutputStream();
	// response.setContentType("application/zip");
	// response.setHeader("Content-Disposition",
	// "attachment;filename=dam.zip");
	//
	// // Write bytes to tmp file.
	// sos.write(zip);
	// sos.flush();
	// log.info("The ZIP is sent");
	// } catch (Exception e) {
	// log.info("OH NO-- AN EXCEPTION: " + e.getMessage());
	// log.info("Exception details:", e);
	// } finally {
	// if (session != null) {
	// session.logout();
	// }
	// }
	// }
	//
	// private byte[] zipFiles(List<String> dataList) throws IOException {
	// if (dataList == null || dataList.size() == 0){
	// return null;
	// }
	//
	// ByteArrayOutputStream baos = new ByteArrayOutputStream();
	// ZipOutputStream zos = new ZipOutputStream(baos);
	// byte bytes[] = new byte[2048];
	//
	// try {
	// for (String absPath : dataList) {
	// Node ntFileNode = this.session.getRootNode().getNode(absPath);
	// String fileName = ntFileNode.getName();
	// Node ntResourceNode = ntFileNode.getNode("jcr:content");
	//
	// InputStream is = ntResourceNode.getProperty("jcr:data").getBinary()
	// .getStream();
	// BufferedInputStream bin = new BufferedInputStream(is);
	//
	// zos.putNextEntry(new ZipEntry(fileName));
	//
	// int bytesRead;
	// while ((bytesRead = bin.read(bytes)) != -1) {
	// zos.write(bytes, 0, bytesRead);
	// }
	//
	// zos.closeEntry();
	// bin.close();
	// is.close();
	// }
	// } catch (Exception ex) {
	// }
	//
	// zos.flush();
	// baos.flush();
	// zos.close();
	// baos.close();
	//
	// return baos.toByteArray();
	// }
}
