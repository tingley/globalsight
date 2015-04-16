package com.adobe.cq.sling;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrUtil;

@Component
@Service
public class CartServiceImpl implements CartService {
	/** Default log. */
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Reference
	private ResourceResolverFactory resolverFactory;
	private Session session;
	private final String cart = "globalsight-cart";

	@Override
	public void removeCart(String key) {
		ResourceResolver resourceResolver = null;
		try {
			resourceResolver = resolverFactory
					.getAdministrativeResourceResolver(null);
			session = resourceResolver.adaptTo(Session.class);
			Node root = session.getRootNode();
			Node content = root.getNode("content");
			Node cartRoot = null;
			int cartRec = this.doesNodeExist(content, cart);

			if (cartRec == -1) {
				cartRoot = content.addNode(cart, "sling:OrderedFolder");
			} else {
				cartRoot = content.getNode(cart);
			}

			Node userCartNode = null;
			String userCart = cart + "-" + this.getCurrentUserId(session);
			int userRec = this.doesNodeExist(cartRoot, userCart);

			if (userRec == -1) {
				userCartNode = cartRoot.addNode(userCart, "nt:unstructured");
			} else {
				userCartNode = cartRoot.getNode(userCart);
			}

			PropertyIterator pi = userCartNode.getProperties();

			while (pi.hasNext()) {
				Property prop = pi.nextProperty();

				if (prop.getType() == PropertyType.STRING
						&& (prop.getValue().getString()).equals(key)) {
					prop.setValue((String[]) null);
				}
			}

			session.save();
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (session != null && session.isLive()) {
				session.logout();
			}
		}
	}

	@Override
	public void removeCart() {
		ResourceResolver resourceResolver = null;
		try {
			resourceResolver = resolverFactory
					.getAdministrativeResourceResolver(null);
			session = resourceResolver.adaptTo(Session.class);
			Node root = session.getRootNode();
			Node content = root.getNode("content");
			Node cartRoot = null;
			int cartRec = this.doesNodeExist(content, cart);

			if (cartRec == -1) {
				cartRoot = content.addNode(cart, "sling:OrderedFolder");
			} else {
				cartRoot = content.getNode(cart);
			}

			Node userCartNode = null;
			String userCart = cart + "-" + this.getCurrentUserId(session);
			int userRec = this.doesNodeExist(cartRoot, userCart);

			if (userRec == -1) {
				userCartNode = cartRoot.addNode(userCart, "nt:unstructured");
			} else {
				userCartNode = cartRoot.getNode(userCart);
			}

			PropertyIterator pi = userCartNode.getProperties();

			while (pi.hasNext()) {
				Property prop = pi.nextProperty();

				if (prop.getType() == PropertyType.STRING) {
					prop.setValue((String[]) null);
				}
			}

			session.save();
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (session != null && session.isLive()) {
				session.logout();
			}
		}

	}

	public int add2Cart(String pagePath) {
		log.info("Node \'" + pagePath + "\' will add to cart.");
		
		final int OK = 1;
		final int EXISTED = -1;
		final int ERROR = -2;
		int result = 0;

		ResourceResolver resourceResolver = null;

		try {
			resourceResolver = resolverFactory
					.getAdministrativeResourceResolver(null);
			session = resourceResolver.adaptTo(Session.class);

			Node root = session.getRootNode();
			Node content = root.getNode("content");
			Node cartRoot = null;
			int cartRec = this.doesNodeExist(content, cart);
			

			if (cartRec == -1) {
				cartRoot = content.addNode(cart, "sling:OrderedFolder");
			} else {
				cartRoot = content.getNode(cart);
			}

			Node userCartNode = null;
			String userCart = cart + "-" + this.getCurrentUserId(session);
			int userRec = this.doesNodeExist(cartRoot, userCart);

			if (userRec == -1) {
				userCartNode = cartRoot.addNode(userCart, "nt:unstructured");
			} else {
				userCartNode = cartRoot.getNode(userCart);
			}

			// Get Locale Object
			//2015.3.17 mark it
//			Resource resource = resourceResolver.resolve(pagePath);
//			Page page = resource.adaptTo(Page.class);
//			Locale local = page.getLanguage(false);
//			Node currentNode = session.getNode(pagePath);
//			String nodeIdentifier = local.getLanguage() + "-"
//					+ currentNode.getName();

			PropertyIterator pi = userCartNode.getProperties();
			boolean flag = false;

			while (pi.hasNext()) {
				Property prop = pi.nextProperty();
//				String propName = prop.getName();
				String value = prop.getValue().getString();
				
				if (value.equals(pagePath)) {
					result = EXISTED;
					flag = true;
					break;
				}
			}

			if (!flag) {
//				userCartNode.setProperty(nodeIdentifier, pagePath);03.17
				
				String propName = pagePath.substring(1).replaceAll("/", "-");
				userCartNode.setProperty(propName, pagePath);
				log.info("UserId \'" + this.getCurrentUserId(session)
						+ "\' add node \' " + pagePath + "\' to cart.");
				result = OK;
			}

			session.save();

		} catch (Exception e) {
			log.error(e.getMessage());
			result = ERROR;
		} finally {
			if (session != null && session.isLive()) {
				session.logout();
			}
		}
		return result;
	}

	/*
	 * no use
	 * (non-Javadoc)
	 * @see com.adobe.cq.sling.CartService#exportFileFromCart()
	 */
	@Override
	public void exportFileFromCart() {
		ResourceResolver resourceResolver = null;
		Map<Node, String> pagePathMap = new HashMap<Node, String>();
		try {
			resourceResolver = resolverFactory
					.getAdministrativeResourceResolver(null);
			session = resourceResolver.adaptTo(Session.class);
			Node root = session.getRootNode();
			Node content = root.getNode("content");
			Node cartRoot = content.getNode(cart);
			String userCart = cart + "-" + this.getCurrentUserId(session);
			Node userCartNode = cartRoot.getNode(userCart);
			PropertyIterator pi = userCartNode.getProperties();
			Set<Node> cNodes = new HashSet<Node>();

			while (pi.hasNext()) {
				Property prop = pi.nextProperty();

				if (prop.getType() == PropertyType.STRING) {
					String value = prop.getValue().toString();
					Node node = session.getNodeByIdentifier(value
							+ "/jcr:content");
					pagePathMap.put(node, value);
					cNodes.add(node);
				}
			}

			Document document = DocumentHelper.createDocument();
			Element gsTranslationFile = document
					.addElement(XmlConstants.TranslationFile);
			gsTranslationFile.addAttribute(XmlConstants.GSSourcelanguage, "en");// TODO
			gsTranslationFile.addAttribute(XmlConstants.GSTargetLanguage, "fr");// TODO
			gsTranslationFile.addAttribute(XmlConstants.GSDescription, " ");

			Element gsPageTranslations = gsTranslationFile
					.addElement(XmlConstants.GSPageTranslations);
			// OutputFormat format = OutputFormat.createPrettyPrint();
			OutputFormat format = OutputFormat.createCompactFormat();
			/*
			 * // 缩减格式 OutputFormat format = OutputFormat.createCompactFormat();
			 */
			/*
			 * // 指定XML编码 format.setEncoding("GBK");
			 */
			format.setEncoding("UTF-8");
			XMLWriter writer = new XMLWriter(new FileWriter(new File(
					"GlobalSightSourceFile//source_temp.xml")), format);// TODO: file name

			for (Node cNode : cNodes) {
				Element gsPageTranslation = gsPageTranslations
						.addElement(XmlConstants.GSPageTranslation);
				gsPageTranslation.addAttribute(XmlConstants.PagePath,
						pagePathMap.get(cNode));
				Element propertyContents = gsPageTranslation
						.addElement(XmlConstants.PropertyContents);
				nodeVisitor(propertyContents, cNode);
			}
			writer.write(document);
			writer.close();
			session.save();
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (session != null && session.isLive()) {
				session.logout();
			}
		}
	}
	/*
	 * no use
	 * (non-Javadoc)
	 * @see com.adobe.cq.sling.CartService#exportFileFromCart(java.lang.String[])
	 */

	@Override
	public void exportFileFromCart(String[] keyArray) {
		if (keyArray == null || keyArray.length == 0) {
			return;
		}

		Set<String> keySet = new HashSet<String>();

		for (String key : keyArray) {
			keySet.add(key);
		}

		ResourceResolver resourceResolver = null;
		Map<Node, String> pagePathMap = new HashMap<Node, String>();
		try {
			resourceResolver = resolverFactory
					.getAdministrativeResourceResolver(null);
			session = resourceResolver.adaptTo(Session.class);
			Node root = session.getRootNode();
			Node content = root.getNode("content");
			Node cartRoot = content.getNode(cart);
			String userCart = cart + "-" + this.getCurrentUserId(session);
			Node userCartNode = cartRoot.getNode(userCart);
			PropertyIterator pi = userCartNode.getProperties();
			Set<Node> cNodes = new HashSet<Node>();

			while (pi.hasNext()) {
				Property prop = pi.nextProperty();

				if (prop.getType() == PropertyType.STRING) {
					String value = prop.getValue().toString();

					if (keySet.contains(value)) { // !!!!
						Node node = session.getNodeByIdentifier(value
								+ "/jcr:content");
						pagePathMap.put(node, value);
						cNodes.add(node);
					}
				}
			}

			Document document = DocumentHelper.createDocument();
			Element gsTranslationFile = document
					.addElement(XmlConstants.TranslationFile);
			gsTranslationFile.addAttribute(XmlConstants.GSSourcelanguage, "en");// TODO
			gsTranslationFile.addAttribute(XmlConstants.GSTargetLanguage, "");// TODO
			gsTranslationFile.addAttribute(XmlConstants.GSDescription, "");

			Element gsPageTranslations = gsTranslationFile
					.addElement(XmlConstants.GSPageTranslations);
			OutputFormat format = OutputFormat.createCompactFormat();
			format.setEncoding("UTF-8");

			for (Node cNode : cNodes) {
				String tempFileName = cNode.getIdentifier();
				int begin = tempFileName.indexOf("/", 1);
				tempFileName = tempFileName.substring(begin).replace('/', '.');
				String suffix = ".xml";
				XMLWriter writer = new XMLWriter(new FileWriter(new File(
						"sourceFile//source_temp" + tempFileName + suffix)),
						format);// TODO: file name
				Element gsPageTranslation = gsPageTranslations
						.addElement(XmlConstants.GSPageTranslation);
				gsPageTranslation.addAttribute(XmlConstants.PagePath,
						pagePathMap.get(cNode));
				Element propertyContents = gsPageTranslation
						.addElement(XmlConstants.PropertyContents);
				nodeVisitor(propertyContents, cNode);
				writer.write(document);
				writer.close();
			}

			session.save();
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (session != null && session.isLive()) {
				session.logout();
			}
		}
	}

	@Override
	public void importTargetFile(File targetFile) {
		try {
			ResourceResolver resourceResolver = resolverFactory
					.getAdministrativeResourceResolver(null);
			session = resourceResolver.adaptTo(Session.class);

			log.info("begin to parse target file :" + targetFile.getAbsolutePath());
			
			XmlHelper helper = new XmlHelper(targetFile);
			TranslationFile translationFile = helper.traversalDocument();
			String sourceCode = translationFile.getGSSourceLanguage();
			String targetCode = translationFile.getGSTargetLanguage();
			log.info("Target file sourceCode: " + sourceCode);
			log.info("Target file targetCode: " + targetCode);

			// 1. create target language node
			// 2. live copy source page
			// 3. update target page
			List<GSPageTranslation> pageTranslations = translationFile
					.getGSPageTranslationList();

			if (pageTranslations != null && pageTranslations.size() != 0) {
				log.info("pageTranslations size: " + pageTranslations.size());

				for (GSPageTranslation pageTran : pageTranslations) {

					String sourcePagePath = pageTran.getPagePath().substring(1);
					String targetPagePath = sourcePagePath.replace("/"
							+ sourceCode, "/" + targetCode);

					log.info("targetPagePath: " + targetPagePath);

					Node root = session.getRootNode();
					String[] folds = targetPagePath.split("/");

					for (String fold : folds) {
						try {
							root = root.getNode(fold);
						} catch (PathNotFoundException ex) {
							root = root.addNode(fold, "cq:Page");
							// jcr:content
							Node resNode = root.addNode("jcr:content",
									"cq:PageContent");
							Calendar lastModified = Calendar.getInstance();
							resNode.setProperty("jcr:lastModified",
									lastModified);
							resNode.setProperty("jcr:data", "");
						}
					}

					// copy
					Node src = session.getRootNode().getNode(
							sourcePagePath + "/jcr:content");
					Node dstParent = session.getRootNode().getNode(
							targetPagePath);
					Node newNode = JcrUtil.copy(src, dstParent, "jcr:content", true);
					newNode.getSession().save();

					List<PropertyContent> propConts = pageTran
							.getPropertyContentList();

					for (PropertyContent propCont : propConts) {
						String nodePath = propCont.getNodePath();
						String property = propCont.getPropertyName();
						String targetText = propCont.getText();
						String refPath = propCont.getRefPath();
						StringBuffer absPath = new StringBuffer("");

						if ("path".equals(property) && refPath != null) {
							/*************** reference ********/
							// 1. copy reference and create new reference
							// 2. update text
							// 3. change path to new reference
							Node referNode = session.getRootNode().getNode(
									refPath);
							Node desParent = referNode.getParent();
							Node newReferNode = JcrUtil
									.copy(referNode, desParent,
											referNode.getName() + targetCode);
							newReferNode.setProperty("text", targetText);// TODO
							newReferNode.getSession().save();

							String newReferPath = newReferNode.getIdentifier();
							absPath.append(targetPagePath);
							absPath.append("/");
							absPath.append(nodePath);
							Node jcrNode = session.getRootNode().getNode(
									absPath.toString());
							jcrNode.setProperty(property, newReferPath);
						} else {
							// change JCR node path
							absPath.append(targetPagePath);
							absPath.append("/");
							absPath.append(nodePath);

							Node jcrNode = session.getRootNode().getNode(
									absPath.toString());
							jcrNode.setProperty(property, targetText);
						}
					}

				}
			}
			session.save();

		} catch (Exception ex) {
			log.error("Error", ex);
		} finally {
			if (session != null && session.isLive()) {
				session.logout();
			}
		}

	}

	@Override
	public String createTreeData() {
		final StringBuffer sb = new StringBuffer("");
		ResourceResolver resourceResolver = null;
		try {
			resourceResolver = resolverFactory
					.getAdministrativeResourceResolver(null);
			session = resourceResolver.adaptTo(Session.class);

			Node root = session.getRootNode();
			Node content = root.getNode("content");
			Node cartRoot = content.getNode(cart);
			String userCart = cart + "-" + this.getCurrentUserId(session);
			Node userCartNode = cartRoot.getNode(userCart);
			PropertyIterator pi = userCartNode.getProperties();
			Set<Node> cNodes = new HashSet<Node>();

			while (pi.hasNext()) {
				Property prop = pi.nextProperty();

				if (prop.getType() == PropertyType.STRING) {
					String value = prop.getValue().toString();
					Node node = session.getNodeByIdentifier(value);
					cNodes.add(node);
				}
			}

			Set<Node> dNodes = new HashSet<Node>();
			sb.append("[");
			// filter parent-child relationship node
			filterNodes(cNodes, dNodes);
			cNodes.removeAll(dNodes);
			StringBuffer rootNode = new StringBuffer(
					"{\"title\": \"Cart Content\", \"key\": \"root\", \"isFolder\": \"false\", \"children\": [");
			sb.append(rootNode.toString());

			for (Node node : cNodes) {
				iterateNode(node, sb);
			}
			sb.append("]}");
			sb.append("]");
		} catch (Exception ex) {
		} finally {
			if (session != null && session.isLive()) {
				session.logout();
			}
		}

		log.info("##########################");
		log.info(sb.toString());
		
		return sb.toString();
	}

	private void filterNodes(Set<Node> cNodes, Set<Node> dNodes)
			throws Exception {
		if (cNodes == null || cNodes.isEmpty()) {
			return;
		}

		for (Node node : cNodes) {
			this.iterateNode(node, cNodes, dNodes);
		}

	}

	private void iterateNode(Node node, Set<Node> cNodes, Set<Node> dNodes)
			throws Exception {
		if ("jcr:content".equals(node.getName())) {
			return;
		}

		if (node != null && node.hasNodes()) {
			NodeIterator ni = node.getNodes();

			while (ni.hasNext()) {
				Node child = ni.nextNode();

				if ("jcr:content".equals(node.getName())) {
					continue;
				}

				for (Node nod : cNodes) {
					if (nod.getIdentifier().equals(child.getIdentifier())) {
						dNodes.add(nod);
						break;
					}
				}

				iterateNode(child, cNodes, dNodes);
			}
		}
	}

	private void iterateNode(Node node, StringBuffer sb) throws Exception {
		if ("jcr:content".equals(node.getName())) {
			return;
		}
		sb.append("{");

		try {
			sb.append("\"title\":");
			String title = node.getIdentifier();
			title = title.substring(title.indexOf("/", 1) + 1).replace('/', '.');
			sb.append("\"").append(title).append("\",");
			sb.append("\"key\":");
			sb.append("\"").append(node.getIdentifier()).append("\",");
			sb.append("\"isFolder\":").append(false);

			// Whether this node has child nodes.
			if (node != null && node.hasNodes()) {
				NodeIterator ni = node.getNodes();

				if (ni.getSize() != 1) {
					sb.append(",");
					sb.append("\"children\":[");

					while (ni.hasNext()) {
						Node child = ni.nextNode();
						iterateNode(child, sb);
					}
					sb.append("]");
				}
			}
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		sb.append("},");
	}

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
						&& ("jcr:title".equals(name) || "text".equals(name))) {
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

	private String getCurrentUserId(Session session) {
		String userId = session.getUserID();
		return userId;
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
			log.error("Error:" + e.getMessage());
		}
		return 0;
	}

//	 public static void main(String[] args) {
//	 String s1 = "/content/geometrixx/en/products/square" ;
//	 System.out.println(s1.indexOf("/", 1));
//	 System.out.println(s1.substring(s1.indexOf("/", 1) +1).replace('/', '.'));
//	 // String s2 = s1.replaceAll("/en", "/fr");
//	 // System.out.println("s2 = " +s2);
//	
//	 // CartServiceImpl cart = new CartServiceImpl();
//	 // cart.createTreeData();
//	 }
}