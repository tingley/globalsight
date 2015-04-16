package com.adobe.cq;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
//Sling Imports
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.globalsight.www.webservices.WebService4AEM;
import com.globalsight.www.webservices.WebService4AEM_Service;

//This is a component so it can provide or consume services
@Component
// This component provides the service defined through the interface
@Service
public class CustomerServiceImpl implements CustomerService {
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Reference
	private ResourceResolverFactory resolverFactory;
	private Session session;
	private static String wsdlUrl = null;
	private static final int OK = 1;
	private static final int FAIL_VERIFY = 0;
	private static final int ERROR = -1;
	private static final int EXISTED = -2;
	private static final int HTTPS_NOT_SUPPORT = -3;
	private static final String conNode = "/content/globalsight/connector";
	private static final String globalSightNode = "/content/globalsight";
	private static final String jobsNode = "/content/globalsight/job";
	private static final QName SERVICE_NAME = new QName(
			"http://webservices.globalsight.com/", "WebService4AEM");

	// private static final QName HTTPS_SERVICE_NAME = new QName(
	// "http://webservices.globalsight.com/", "WebService4AEM");

	private final String cart = "globalsight-cart";

	public int insertGlobalsightData(String hostName, String hostPort,
			String username, String password, String desc, String enableHttps,
			String checkInterval) {

		try {
			StringBuffer strURL = new StringBuffer("");
			boolean eHttps = "YES".equalsIgnoreCase(enableHttps) ? true : false;

			if (eHttps) {
				strURL.append("https://");
			} else {
				strURL.append("http://");
			}
			strURL.append(hostName);
			strURL.append(":");
			strURL.append(hostPort);
			strURL.append("/globalsight/aemServices/WebService4AEM?wsdl");

			log.info("globalsight configure url: " + strURL.toString());

			URL wsdlURL = new URL(strURL.toString());
			WebService4AEM_Service ss = null;

			if (eHttps == true) {
				setTrustStore();
				ss = new WebService4AEM_Service(wsdlURL, SERVICE_NAME);
			} else {
				ss = new WebService4AEM_Service(wsdlURL, SERVICE_NAME);
			}

			WebService4AEM port = ss.getWebService4AEMPort();
			String fullAccessToken = null;

			log.info("Invoking login...");
			java.lang.String _login_arg0 = username;
			java.lang.String _login_arg1 = password;
			java.lang.String _login__return = port.login(_login_arg0,
					_login_arg1);
			log.info("Login FullAccessToken = " + _login__return);
			fullAccessToken = _login__return;

			if (fullAccessToken == null) {
				return FAIL_VERIFY;
			}

			session = this.getSession();
			Node root = session.getRootNode();
			Node content = root.getNode("content");
			Node gsNode = null;
			Node connectorNode = null;

			java.lang.Iterable<Node> custNode = JcrUtils.getChildNodes(content,
					"globalsight");
			Iterator it = custNode.iterator();

			if (it.hasNext()) {
				gsNode = content.getNode("globalsight");
				Iterable itCust = JcrUtils.getChildNodes(gsNode);
				Iterator childNodeIt = itCust.iterator();

				if (childNodeIt.hasNext()) {
					try {
						connectorNode = gsNode.getNode("connector");
					} catch (PathNotFoundException ex) {
						connectorNode = gsNode.addNode("connector",
								"nt:unstructured");
					}
				}
			} else {
				gsNode = content.addNode("globalsight", "sling:OrderedFolder");
			}

			if (connectorNode == null) {
				connectorNode = gsNode.addNode("connector", "nt:unstructured");
			}

			connectorNode.setProperty("hostName", hostName);
			connectorNode.setProperty("hostPort", hostPort);
			connectorNode.setProperty("username", username);
			connectorNode.setProperty("password", password);
			connectorNode.setProperty("https", enableHttps);
			connectorNode.setProperty("desc", desc);
			connectorNode.setProperty("fullAccessToken", fullAccessToken);
			connectorNode.setProperty("checkInterval", checkInterval);

			session.save();
			return OK;

		} catch (WebServiceException ex) {
			log.error("WebServiceException: " + ex);
			log.error("Return https not support value: " + HTTPS_NOT_SUPPORT);
			return HTTPS_NOT_SUPPORT;
		} catch (Exception ex) {
			log.error("Exception:", ex);
			return ERROR;
		} finally {
			logout();
		}
	}

	private void setTrustStore() {
		StringBuffer certsPath = new StringBuffer();

		String javaHome = System.getProperty("java.home");

		if (javaHome == null || "".equals(javaHome)) {
			log.error("JAVA_HOME is null, please set it.");
			return;
		}

		certsPath.append(javaHome == null ? "" : javaHome)
				.append(File.separator).append("lib").append(File.separator)
				.append("security");

		File file = new File(certsPath.toString(), "cacerts");
		if (!file.exists() || !file.isFile()) {
			file = new File(certsPath.toString(), "jssecacerts");
			log.info("cacerts file created.");
		}

		if (file.exists()) {
			String path = file.getAbsolutePath();
			System.setProperty("javax.net.ssl.trustStore", path);
			log.info("cacerts path: " + path);

			// password is not required
			// System.setProperty("javax.net.ssl.trustStorePassword","changeit");
		}

	}

	public String getXmlFileProfileExt() {
		String token = this.getToken();
		log.info("AccessToken: " + token);

		if (token == null) {
			return null;
		}

		StringBuffer fileProfile = new StringBuffer();;

		try {
			String wsdlUrl = this.getGlobalSightUrl();
			if (wsdlUrl == null) {
				log.error("wsdlUrl is null, please go to configure GlobalSight Connector!");
				return null;
			}
			URL url = new URL(wsdlUrl);
			log.info("Get globalsight wsdl url: " + url.toString());
			WebService4AEM_Service service = new WebService4AEM_Service(url);
			String fileProfileInfo = service.getWebService4AEMPort().getFileProfileInfoEx(
					token);
			Document doc = DocumentHelper.parseText(fileProfileInfo);
			
			fileProfile.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			fileProfile.append("<").append(doc.getRootElement().getName()).append(">");

			List<org.dom4j.Node> list = doc
					.selectNodes("fileProfileInfo/fileProfile");
			org.dom4j.XPath path = new NumberXPath("localeInfo/targetLocale");
			path.sort(list);

			Iterator it = list.iterator();

			while (it.hasNext()) {
				Element ele = (Element) it.next();
				fileProfile.append((ele.asXML()));
			}
			fileProfile.append("</").append(doc.getRootElement().getName()).append(">");
		} catch (Exception e) {
			log.error("Error: ", e);
		}
		
		//log.info("sorted fileProfile info: " + fileProfile.toString());

		return fileProfile.toString();
	}

	public String getToken() {
		session = this.getSession();
		try {
			Node connectorNode = session.getNode(conNode);

			if (connectorNode == null) {
				return null;
			}

			Property name = connectorNode.getProperty("username");
			Property pwd = connectorNode.getProperty("password");

			if (name != null && pwd != null) {
				String userName = name.getValue().getString();
				String password = pwd.getValue().getString();
				String wsdlUrl = this.getGlobalSightUrl();

				if (wsdlUrl == null) {
					log.error("wsdlUrl is null, please go to configure GlobalSight Connector!");
					return null;
				}

				URL url = new URL(wsdlUrl);
				log.info("Get globalsight wsdl url: " + url.toString());
				WebService4AEM_Service service = new WebService4AEM_Service(url);
				WebService4AEM port = service.getWebService4AEMPort();

				return port.login(userName, password);
			}
		} catch (Exception e) {
			log.error("Error: ", e);
		} finally {
			logout();
		}
		return null;
	}

	public int hasAccessToken() {
		session = this.getSession();
		try {
			Node connectorNode = session.getNode(conNode);

			if (connectorNode == null) {
				return 0;
			}
			Property prop = connectorNode.getProperty("fullAccessToken");

			if (prop == null) {
				return 0;
			}
			return 1;

		} catch (Exception e) {
			return 0;
		} finally {
			logout();
		}
	}

	public String getCurrentUserId(Session session) {
		String userId = session.getUserID();
		return userId;
	}

	public String getCurrentNodeTitle(Node node) throws Exception {
		return node.getProperty("jcr:title").getString();

	}

	private Session getSession() {
		session = null;
		ResourceResolver resourceResolver;
		try {
			resourceResolver = resolverFactory
					.getAdministrativeResourceResolver(null);
			session = resourceResolver.adaptTo(Session.class);
		} catch (LoginException e) {
			log.error("GetCurrentUserIdException: " + e);
		}

		return session;
	}

	private void logout() {
		if (session != null && session.isLive()) {
			session.logout();
		}
	}

	public String getJobStatus(String jobName) {
		session = this.getSession();

		try {
			Node connectorNode = session.getNode(conNode);

			if (connectorNode == null) {
				return null;
			}

			Property name = connectorNode.getProperty("username");
			Property pwd = connectorNode.getProperty("password");

			if (name != null && pwd != null) {
				String wsdlUrl = this.getGlobalSightUrl();

				if (wsdlUrl == null) {
					log.error("wsdlUrl is null, please go to configure GlobalSight Connector!");
					return null;
				}

				URL url = new URL(wsdlUrl);
				log.info("Get globalsight wsdl url: " + url.toString());
				WebService4AEM_Service service = new WebService4AEM_Service(url);
				WebService4AEM port = service.getWebService4AEMPort();
				String accessFullToken = port
						.login(name.getValue().getString(), pwd.getValue()
								.getString());

				return port.getJobStatus(accessFullToken, jobName);
			}

		} catch (Exception e) {
			log.error("Error: ", e);
		} finally {
			logout();
		}

		return null;
	}

	public String getJobExportFile(String jobName) {
		session = this.getSession();

		try {
			Node connectorNode = session.getNode(conNode);

			if (connectorNode == null) {
				return null;
			}

			Property name = connectorNode.getProperty("username");
			Property pwd = connectorNode.getProperty("password");

			if (name != null && pwd != null) {
				String wsdlUrl = this.getGlobalSightUrl();

				if (wsdlUrl == null) {
					log.error("wsdlUrl is null, please go to configure GlobalSight Connector!");
					return null;
				}

				URL url = new URL(wsdlUrl);
				log.info("Get globalsight wsdl url: " + url.toString());
				WebService4AEM_Service service = new WebService4AEM_Service(url);
				WebService4AEM port = service.getWebService4AEMPort();
				String accessFullToken = port
						.login(name.getValue().getString(), pwd.getValue()
								.getString());

				return port.getJobExportFiles(accessFullToken, jobName);
			}

		} catch (Exception e) {
			log.error("Error: ", e);
		} finally {
			logout();
		}

		return null;
	}

	public Map<String, String> getGlobalsightData() {
		session = this.getSession();

		try {
			Node connectorNode = session.getNode(conNode);

			if (connectorNode == null) {
				return null;
			}

			Property propHost = connectorNode.getProperty("hostName");
			Property propPort = connectorNode.getProperty("hostPort");
			Property proDesc = connectorNode.getProperty("desc");
			Property propName = connectorNode.getProperty("username");
			Property propPwd = connectorNode.getProperty("password");
			Property propHttps = connectorNode.getProperty("https");
			Property propInterval = connectorNode.getProperty("checkInterval");

			String checkInterval = "";
			if (propInterval != null) {
				checkInterval = propInterval.getValue().getString();
			}

			String hostName = "";
			if (propHost != null) {
				hostName = propHost.getValue().getString();
			}

			String hostPort = "";
			if (propPort != null) {
				hostPort = propPort.getValue().getString();
			}

			String name = "";
			if (propName != null) {
				name = propName.getValue().getString();
			}

			String pwd = "";
			if (propPwd != null) {
				pwd = propPwd.getValue().getString();
			}

			String enableHttps = "";
			if (propHttps != null) {
				enableHttps = propHttps.getValue().getString();
			}

			String desc = "";
			if (proDesc != null) {
				desc = proDesc.getValue().getString();
			}

			Map<String, String> map = new HashMap<String, String>();
			map.put(AEMConstants.HostName, hostName);
			map.put(AEMConstants.HostPort, hostPort);
			map.put(AEMConstants.UserName, name);
			map.put(AEMConstants.Password, pwd);
			map.put(AEMConstants.Desc, desc);
			map.put(AEMConstants.EnableHttps, enableHttps);
			map.put(AEMConstants.CheckInterval, checkInterval);
			return map;

		} catch (Exception e) {
			log.error("Error: ", e);
		} finally {
			logout();
		}

		return null;
	}

	public String getGlobalSightUrl() {
		StringBuffer url = new StringBuffer();
		session = this.getSession();

		try {
			Node connectorNode = session.getNode(conNode);

			if (connectorNode == null) {
				return null;
			}

			Property propHost = connectorNode.getProperty("hostName");
			Property propPort = connectorNode.getProperty("hostPort");
			Property propHttps = connectorNode.getProperty("https");

			String enableHttps = "";
			if (propHttps != null) {
				enableHttps = propHttps.getValue().getString();
				if ("No".equalsIgnoreCase(enableHttps)) {
					url.append("http://");
				} else {
					url.append("https://");
				}
			}

			String hostName = "";

			if (propHost != null && propHost.getValue() != null
					&& !propHost.getValue().getString().trim().equals("")) {
				hostName = propHost.getValue().getString();
				url.append(hostName);
			} else {
				throw new Need2ConfigureGlobalSightConnectorException(
						"wsdlUrl is null, please go to configure GlobalSight Connector!");
			}

			String hostPort = "";
			if (propPort != null) {
				hostPort = propPort.getValue().getString();
				url.append(":");
				url.append(hostPort);
			}

			url.append("/globalsight/aemServices/WebService4AEM?wsdl");
			wsdlUrl = url.toString();
		} catch (Exception e) {
			log.error("Error: ", e);
		} finally {
			logout();
		}
		return wsdlUrl;
	}

	public String getCheckInterval() {
		String interval = null;
		session = this.getSession();

		try {
			Node connectorNode = session.getNode(conNode);

			if (connectorNode == null) {
				return null;
			}

			Property checkInterval = connectorNode.getProperty("checkInterval");

			if (checkInterval != null) {
				interval = checkInterval.getValue().getString();
				return interval;
			}
		} catch (Exception e) {
			log.error("Error: ", e);
		} finally {
			logout();
		}
		return null;
	}

	public File createPageTempFile(File folder, String pageKey) {
		log.info("Enter mothod 'createPageTempFile()'");
		// Set<String> keySet = new HashSet<String>();
		// keySet.add(pageKey);
		ResourceResolver resourceResolver = null;
		Map<Node, String> pagePathMap = new HashMap<Node, String>();
		File tempFile = null;
		try {
			resourceResolver = resolverFactory
					.getAdministrativeResourceResolver(null);
			session = resourceResolver.adaptTo(Session.class);
			Node pageNode = null;
			// Node root = session.getRootNode();//03.16
			// Node content = root.getNode("content");
			// Node cartRoot = content.getNode(cart);
			// String userCart = cart + "-" + this.getCurrentUserId(session);
			// Node userCartNode = cartRoot.getNode(userCart);
			// PropertyIterator pi = userCartNode.getProperties();

			// Modify 2015.03.16
			// while (pi.hasNext()) {
			// Property prop = pi.nextProperty();
			//
			// if (prop.getType() == PropertyType.STRING) {
			// String value = prop.getValue().toString();
			//
			// if (keySet.contains(value)) { // !!!!
			// Node node = session.getNodeByIdentifier(value
			// + "/jcr:content");
			// pagePathMap.put(node, value);
			// pageNode = node;
			// log.info("Fine, page key matched in the cart property list!");
			// break;
			// }
			// }
			// }

			Node node = session.getNodeByIdentifier(pageKey + "/jcr:content");
			pagePathMap.put(node, pageKey);
			pageNode = node;
			// end 03.16

			Document document = DocumentHelper.createDocument();
			Element gsTranslationFile = document
					.addElement(XmlConstants.TranslationFile);
			gsTranslationFile.addAttribute(XmlConstants.GSSourcelanguage, "en");// TODO
			gsTranslationFile.addAttribute(XmlConstants.GSTargetLanguage, "");// TODO
			gsTranslationFile.addAttribute(XmlConstants.GSDescription, "");

			Element gsPageTranslations = gsTranslationFile
					.addElement(XmlConstants.GSPageTranslations);
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("UTF-8");

			if (pageNode != null) {
				String tempFileName = pageKey;
				int begin = tempFileName.indexOf("/", 1);
				tempFileName = tempFileName.substring(begin + 1).replace('/',
						'.');
				String suffix = ".xml";
				log.info("Page key \'" + pageKey + "\' tempFileName is "
						+ tempFileName + suffix);

				tempFile = new File(folder, tempFileName + suffix);

				XMLWriter writer = new XMLWriter(new FileWriter(tempFile),
						format);// TODO: file name
				Element gsPageTranslation = gsPageTranslations
						.addElement(XmlConstants.GSPageTranslation);
				gsPageTranslation.addAttribute(XmlConstants.PagePath,
						pagePathMap.get(pageNode));
				Element propertyContents = gsPageTranslation
						.addElement(XmlConstants.PropertyContents);
				nodeVisitor(propertyContents, pageNode);
				writer.write(document);
				writer.close();
			} else {
				log.error("Find page key in the cart list, but it's pageNode is null!");
			}

			session.save();
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (session != null && session.isLive()) {
				session.logout();
			}
		}
		return tempFile;
	}

	/**
	 * 
	 * @param folder
	 *            temple folder
	 * @param pageKeys
	 *            translate pages
	 * @param target
	 *            translate language
	 * @return
	 */
	public List<File> createPageTempFile(File tempFolder, String[] pageKeys,
			String pTarget) {
		Set<String> keySet = new HashSet<String>();

		for (String pageKey : pageKeys) {
			keySet.add(pageKey);
		}

		List<File> tempFiles = new ArrayList<File>(keySet.size());
		ResourceResolver resourceResolver = null;
		Map<Node, String> pagePathMap = new HashMap<Node, String>();
		try {
			resourceResolver = resolverFactory
					.getAdministrativeResourceResolver(null);
			session = resourceResolver.adaptTo(Session.class);
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("UTF-8");

			if (pTarget != null && pTarget != "") {
				File targetFolder = null;

				for (String pageKey : pageKeys) {

					Resource resource = resourceResolver.resolve(pageKey);
					Page page = resource.adaptTo(Page.class);
					Locale locale = page.getLanguage(false);

					StringBuffer localeInfo = new StringBuffer(
							locale.getLanguage());

					if (!"".equals(locale.getCountry())) {
						localeInfo.append("_").append(locale.getCountry());
					}

					log.info("############### get page [" + pageKey
							+ "] locale information: \n "
							+ localeInfo.toString());

					Node node = session.getNodeByIdentifier(pageKey
							+ "/jcr:content");
					pagePathMap.put(node, pageKey);
					Node pageNode = node;

					if (pageNode != null) {
						String suffix = ".xml";
						String tempFileName = pageKey;
						int begin = tempFileName.indexOf("/", 1);
						tempFileName = tempFileName.substring(begin + 1)
								.replace('/', '.');

						if (targetFolder == null) {
							targetFolder = new File(
									AEMConstants.GlobalSightSourceFile
											+ File.separator
											+ tempFolder.getName()
											+ File.separator + pTarget);
							targetFolder.mkdirs();
							log.info("OK! Success in creating target  temp folder:	"
									+ targetFolder.getName());
						}

						File tempFile = new File(
								AEMConstants.GlobalSightSourceFile
										+ File.separator + tempFolder.getName()
										+ File.separator
										+ targetFolder.getName()
										+ File.separator + tempFileName
										+ suffix);
						log.info("Page key [" + pageKey
								+ "] temp file name is " + tempFile.getName());

						XMLWriter writer = new XMLWriter(new FileOutputStream(
								tempFile), format);

						// --
						Document document = DocumentHelper.createDocument();
						Element gsTranslationFile = document
								.addElement(XmlConstants.TranslationFile);
						gsTranslationFile.addAttribute(
								XmlConstants.GSSourcelanguage,
								locale.toString());
						gsTranslationFile.addAttribute(
								XmlConstants.GSTargetLanguage, "");
						gsTranslationFile.addAttribute(
								XmlConstants.GSDescription, "");
						Element gsPageTranslations = gsTranslationFile
								.addElement(XmlConstants.GSPageTranslations);
						// --

						Element gsPageTranslation = gsPageTranslations
								.addElement(XmlConstants.GSPageTranslation);
						gsPageTranslation.addAttribute(XmlConstants.PagePath,
								pagePathMap.get(pageNode));
						Element propertyContents = gsPageTranslation
								.addElement(XmlConstants.PropertyContents);
						nodeVisitor(propertyContents, pageNode);
						writer.write(document);
						writer.close();
						tempFiles.add(tempFile);
					} else {
						log.error("Found page key in the cart list, but it's pageNode is null!");
					}
				}

			}
			session.save();
		} catch (Exception e) {
			log.error("Error", e);
		} finally {
			if (session != null && session.isLive()) {
				session.logout();
			}
		}
		return tempFiles;
	}

	// public File createPageTempFile(File folder, String[] pageKeys) {
	// log.info("Enter mothod 'createPageTempFile()' with array parameters...");
	//
	// Set<String> keySet = new HashSet<String>();
	//
	// for (String pageKey : pageKeys) {
	// keySet.add(pageKey);
	// }
	// ResourceResolver resourceResolver = null;
	// Map<Node, String> pagePathMap = new HashMap<Node, String>();
	// File tempFile = null;
	// try {
	// Node pageNode = null;
	// resourceResolver = resolverFactory
	// .getAdministrativeResourceResolver(null);
	// session = resourceResolver.adaptTo(Session.class);
	// Document document = DocumentHelper.createDocument();
	// Element gsTranslationFile = document
	// .addElement(XmlConstants.TranslationFile);
	// gsTranslationFile.addAttribute(XmlConstants.GSSourcelanguage, "en");//
	// TODO
	// gsTranslationFile.addAttribute(XmlConstants.GSTargetLanguage, "");// TODO
	// gsTranslationFile.addAttribute(XmlConstants.GSDescription, "");
	// Element gsPageTranslations = gsTranslationFile
	// .addElement(XmlConstants.GSPageTranslations);
	// OutputFormat format = OutputFormat.createPrettyPrint();
	// format.setEncoding("GBK");
	//
	// String tempFileName = "source_en_temple";
	// String suffix = ".xml";
	// tempFile = new File(folder, tempFileName + suffix);
	//
	// for (String pageKey : pageKeys) {
	// Node node = session.getNodeByIdentifier(pageKey
	// + "/jcr:content");
	// pagePathMap.put(node, pageKey);
	// pageNode = node;
	//
	// if (pageNode != null) {
	// log.info("Page key \'" + pageKey
	// + "\' content would record in file " + tempFileName
	// + suffix);
	// XMLWriter writer = new XMLWriter(new FileWriter(tempFile),
	// format);
	// Element gsPageTranslation = gsPageTranslations
	// .addElement(XmlConstants.GSPageTranslation);
	// gsPageTranslation.addAttribute(XmlConstants.PagePath,
	// pagePathMap.get(pageNode));
	// Element propertyContents = gsPageTranslation
	// .addElement(XmlConstants.PropertyContents);
	// nodeVisitor(propertyContents, pageNode);
	// writer.write(document);
	// writer.close();
	// } else {
	// log.error("Find page key in the cart list, but it's pageNode is null!");
	// }
	// }
	// session.save();
	// } catch (Exception e) {
	// log.error(e.getMessage());
	// } finally {
	// if (session != null && session.isLive()) {
	// session.logout();
	// }
	// }
	// return tempFile;
	// }

	private void nodeVisitor(Element propertyContents, Node node)
			throws Exception {
		if (node == null)
			return;

		setPropertyContent(propertyContents, node);

		NodeIterator nodeIte = node.getNodes();

		while (nodeIte != null && nodeIte.hasNext()) {
			Node childNode = nodeIte.nextNode();
			nodeVisitor(propertyContents, childNode);
		}

	}

	private void setPropertyContent(Element propertyContents, Node node) {
		if (node == null)
			return;

		try {
			PropertyIterator proIt = node.getProperties();
			while (proIt.hasNext()) {
				Property pro = proIt.nextProperty();

				if (pro.isMultiple())
					continue;

				Value value = pro.getValue();
				String name = pro.getName();
				int type = value.getType();

				if (type == PropertyType.STRING
						&& ("jcr:title".equals(name) 
								|| "text".equals(name)
								|| "tableData".equals(name)
								|| "jcr:description".equals(name)
								//||  name.endsWith("Label")
								//|| "options".equals(name)
								)) {
					Element propertyContent = propertyContents
							.addElement(XmlConstants.PropertyContent);
					int index = node.getPath().lastIndexOf("jcr:content");
					String path = node.getPath().substring(index);
					String text = value.getString();
					propertyContent.addAttribute(XmlConstants.NodePath, path);
					propertyContent.addAttribute(XmlConstants.PropertyName,
							name);
					propertyContent.setText(text);
				}
			}

			// --reference--
			String nodeName = node.getName();
			if ("reference".equals(nodeName)) {
				PropertyIterator propReference = node.getProperties();

				while (propReference.hasNext()) {
					Property pro = propReference.nextProperty();
					int type = pro.getType();
					String name = pro.getName();

					if ("path".equals(name) && type == PropertyType.STRING) {
						String referPath = pro.getValue().getString()
								.substring(1);
						Node referNode = node.getSession().getRootNode()
								.getNode(referPath);

						PropertyIterator referNodeProp = referNode
								.getProperties();

						while (referNodeProp.hasNext()) {
							Property prop = referNodeProp.nextProperty();

							if (prop.getType() == PropertyType.STRING
									&& "text".equals(prop.getName())) {
								Element propertyContent = propertyContents
										.addElement(XmlConstants.PropertyContent);
								int index = node.getPath().lastIndexOf(
										"jcr:content");
								String path = node.getPath().substring(index);
								String text = prop.getValue().getString();
								propertyContent.addAttribute(
										XmlConstants.PropertyName, name);
								propertyContent.addAttribute(
										XmlConstants.NodePath, path);
								propertyContent.addAttribute(
										XmlConstants.RefPath, referPath);
								propertyContent.setText(text);
								break;
							}
						}
						break;
					}

				}
			}
			// --end--
		} catch (Exception ex) {
			log.error("Error setPropertyContent(): ", ex);
		}
	}

	public void persisJob(Set<JobBean> jobs) {
		if (jobs == null || jobs.size() == 0) {
			log.warn("No jobs for persisting before JVM down.");
			return;
		}

		log.info("Persisting jobs size:	" + jobs.size());
		session = this.getSession();

		try {
			Node root = session.getRootNode();
			Node content = root.getNode("content");
			Node jobNode = null;
			Node gsNode = null;

			java.lang.Iterable<Node> custNode = JcrUtils.getChildNodes(content,
					"globalsight");
			Iterator it = custNode.iterator();

			if (it.hasNext()) {
				try {
					gsNode = content.getNode("globalsight");
				} catch (PathNotFoundException ex) {
					gsNode = content.addNode("globalsight",
							"sling:OrderedFolder");
				}

				Iterable itCust = JcrUtils.getChildNodes(gsNode);
				Iterator childNodeIt = itCust.iterator();

				if (childNodeIt.hasNext()) {
					try {
						jobNode = gsNode.getNode("job");
					} catch (PathNotFoundException ex) {
						jobNode = gsNode.addNode("job", "nt:unstructured");
					}
				}
			} else {
				gsNode = content.addNode("globalsight", "sling:OrderedFolder");
			}

			if (jobNode == null) {
				jobNode = gsNode.addNode("job", "nt:unstructured");
			}

			// else {
			// PropertyIterator jobProperties = jobNode.getProperties();
			//
			// while (jobProperties.hasNext()) {
			// Property jobProp = jobProperties.nextProperty();
			//
			// if (jobProp.getType() == PropertyType.STRING) {
			// jobProp.remove();
			// }
			// }
			// }

			for (JobBean job : jobs) {
				String jobName = job.getJobName();
				String jobStatus = job.getStatus();
				jobNode.setProperty(jobName, jobStatus);
				log.info("Persisting job \'" + jobName
						+ "\' into AEM before JVM down.");
				session.save();
			}

		} catch (Exception ex) {
			log.error("Persisting job error: ", ex);
		} finally {
			this.logout();
		}
	}

	public void persisJob(JobBean job) {
		if (job == null) {
			log.warn("No job for persisting");
			return;
		}

		session = this.getSession();

		try {
			Node root = session.getRootNode();
			Node content = root.getNode("content");
			Node jobNode = null;
			Node gsNode = null;

			java.lang.Iterable<Node> custNode = JcrUtils.getChildNodes(content,
					"globalsight");
			Iterator it = custNode.iterator();

			if (it.hasNext()) {
				try {
					gsNode = content.getNode("globalsight");
				} catch (PathNotFoundException ex) {
					gsNode = content.addNode("globalsight",
							"sling:OrderedFolder");
				}

				Iterable itCust = JcrUtils.getChildNodes(gsNode);
				Iterator childNodeIt = itCust.iterator();

				if (childNodeIt.hasNext()) {
					try {
						jobNode = gsNode.getNode("job");
					} catch (PathNotFoundException ex) {
						jobNode = gsNode.addNode("job", "nt:unstructured");
					}
				}
			} else {
				gsNode = content.addNode("globalsight", "sling:OrderedFolder");
			}

			if (jobNode == null) {
				jobNode = gsNode.addNode("job", "nt:unstructured");
			}

			// name:value = jobName : status
			String jobName = job.getJobName();
			String jobStatus = job.getStatus();
			jobNode.setProperty(jobName, jobStatus);
			log.info("Persisting job [" + jobName
					+ "] into AEM before JVM down.");
			session.save();

		} catch (Exception ex) {
			log.error("Persisting job error: ", ex);
		} finally {
			this.logout();
		}
	}

	public void updateJobStatus(JobBean job) {
		if (job == null) {
			return;
		}

		log.info("Begin updating job [" + job.getJobName() + "] status");
		session = this.getSession();

		try {
			Node jobnode = session.getNode(jobsNode);

			if (jobnode == null) {
				log.warn("No find job node [" + jobsNode
						+ "] when updating job status.");
				return;
			}

			PropertyIterator jobProperties = jobnode.getProperties();

			while (jobProperties.hasNext()) {
				Property jobProp = jobProperties.nextProperty();
				String aemName = jobProp.getName();

				if (jobProp.getType() == PropertyType.STRING
						&& job.getJobName().equals(aemName)) {
					jobProp.setValue(job.getStatus());
					log.info("Finished updating job status [" + aemName
							+ "] in AEM to " + job.getStatus());
					session.save();
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			this.logout();
		}
	}

	public Set<JobBean> getInProgressJob() {
		log.info("Begin loading unfinished jobs from AEM.");
		session = this.getSession();
		Set<JobBean> jobs = new HashSet<JobBean>();

		try {
			Node jobnode = session.getNode(jobsNode);

			if (jobnode == null) {
				log.warn("No find job node [" + jobsNode + "] in AEM.");
				return null;
			}

			PropertyIterator jobProperties = jobnode.getProperties();

			while (jobProperties.hasNext()) {
				Property jobProp = jobProperties.nextProperty();
				String status = jobProp.getValue().getString();

				if (jobProp.getType() == PropertyType.STRING
						&& AEMConstants.InProgress.equals(status)) {
					JobBean job = new JobBean();
					String name = jobProp.getName();
					job.setJobName(name);
					job.setStatus(status);
					jobs.add(job);
					log.info("Loading InProgress job [" + name + "] from AEM.");
				}
			}
			return jobs;

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			this.logout();
		}
		return null;

	}

	public void removeCartItems(String[] pKeys) {
		if (pKeys == null || pKeys.length == 0) {
			log.warn("items that user wants to update cart are empty!");
			return;
		}

		log.info("Begin to remove cart item after create job work.");

		try {
			session = this.getSession();
			Node root = session.getRootNode();
			Node content = root.getNode("content");
			Node cartRoot = null;
			int cartRec = this.doesNodeExist(content, cart);

			if (cartRec == -1) {
				return;
			} else {
				cartRoot = content.getNode(cart);
			}

			Node userCartNode = null;
			String userCart = cart + "-" + this.getCurrentUserId(session);
			int userRec = this.doesNodeExist(cartRoot, userCart);

			if (userRec == -1) {
				return;
			} else {
				userCartNode = cartRoot.getNode(userCart);
			}
			PropertyIterator pi = userCartNode.getProperties();

			while (pi.hasNext()) {
				Property prop = (Property) pi.next();
				log.info("Current query property name in *AEM* cart is:	"
						+ prop.getName());
				log.info("Current query property value in *AEM* cart is:	"
						+ prop.getValue().getString());

				for (String pkey : pKeys) {
					String aemPKey = prop.getValue().getString();

					if (prop.getType() == PropertyType.STRING
							&& (aemPKey.trim()).equals(pkey.trim())) {
						log.info("Cart item [" + pkey
								+ "] is removed due to creating job work.");
						prop.setValue((String[]) null);
						session.save();
						break;
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (session != null && session.isLive()) {
				session.logout();
			}
		}
	}

	private int doesNodeExist(Node parent, String childName) {
		try {
			java.lang.Iterable<Node> childNodes = JcrUtils.getChildNodes(
					parent, childName);
			Iterator<Node> it = childNodes.iterator();

			if (it.hasNext()) {
				return 1;
			} else {
				return -1;

			}
		} catch (Exception e) {
			log.error("Error doesNodeExist(): " + e.getMessage());
		}
		return 0;
	}

	public static void main(String[] args) {

		// String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		// + "<students><student sn=\"01\">"
		// + "<name>bam</name><age>18</age></student>"
		// + "<student sn=\"04\">"
		// + "<name>lin</name><age>20</age></student>"
		// + "<student sn=\"02\">"
		// + "<name>amy</name><age>30</age></student>"
		// + "</students>";

		String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+

				"<fileProfileInfo>"
				+ "<fileProfile>"
				+ "<id>1000</id>"
				+ "<name>Web Site - HTML</name>"
				+ "<l10nprofile>1010</l10nprofile>"
				+ "<sourceFileFormat>1</sourceFileFormat>"
				+ "<description></description>"
				+ "<fileExtensionInfo>"
				+ "<fileExtension>html</fileExtension>"
				+ "<fileExtension>htm</fileExtension>"
				+ "</fileExtensionInfo>"
				+ "<localeInfo>"
				+ "<sourceLocale>en_US</sourceLocale>"
				+ "<targetLocale>zh_CN</targetLocale>"
				+ "</localeInfo>"
				+ "</fileProfile>"

				+ "<fileProfile>"
				+ "<id>1001</id>"
				+ "<name>Web Site - HTML - MS</name>"
				+ "<l10nprofile>1009</l10nprofile>"
				+ "<sourceFileFormat>1</sourceFileFormat>"
				+ "<description></description>"
				+ "<fileExtensionInfo>"
				+ "<fileExtension>html</fileExtension><fileExtension>htm</fileExtension>"
				+ "</fileExtensionInfo>"
				+ "<localeInfo><sourceLocale>en_US</sourceLocale><targetLocale>jp_JA</targetLocale></localeInfo>"
				+ "</fileProfile>"

				+ "<fileProfile>"
				+ "<id>1002</id>"
				+ "<name>Web Site - XML</name>"
				+ "<l10nprofile>1009</l10nprofile>"
				+ "<sourceFileFormat>7</sourceFileFormat>	"
				+ "<description></description>"
				+ "<fileExtensionInfo><fileExtension>xml</fileExtension></fileExtensionInfo>"
				+ "<localeInfo><sourceLocale>en_US</sourceLocale><targetLocale>fr_Fr</targetLocale></localeInfo>"
				+ "</fileProfile>"

				+ "</fileProfileInfo>";

		try {
			Document doc = DocumentHelper.parseText(text);

			System.err.println(doc.getRootElement().getName());

			List<org.dom4j.Node> list = doc
					.selectNodes("fileProfileInfo/fileProfile");
			org.dom4j.XPath path = new NumberXPath("localeInfo/targetLocale");
			path.sort(list);

			Iterator it = list.iterator();

			while (it.hasNext()) {
				Element ele = (Element) it.next();
				System.out.println(ele.asXML());
			}

		} catch (Exception ex) {

		}
	}
}