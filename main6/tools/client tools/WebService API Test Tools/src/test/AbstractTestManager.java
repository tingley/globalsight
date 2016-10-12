package test;

import com.globalsight.www.webservices.Ambassador4FalconProxy;
import com.globalsight.www.webservices.AmbassadorProxy;
import jodd.props.Props;
import jodd.util.StringUtil;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import util.*;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public abstract class AbstractTestManager implements TestManager
{
    private static Logger logger = Logger.getLogger(AbstractTestManager.class);

    private String realToken = null;
    private Account account = null;
    private boolean isFalconTest = false;
    private Ambassador4FalconProxy falconProxy = null;
    private AmbassadorProxy proxy = null;
    private ArrayList<Operation> operationList = null;
    Props props = new Props();
    String defaultWSDLUrl = "/globalsight/services/AmbassadorWebService?wsdl";
    String defaultWSDLClass = "com.globalsight.www.webservice.Ambassador";
    private String currentPath = FileUtil.getCurrentPath();

    public AbstractTestManager() {
    }

    public AbstractTestManager(Account account) {
        this.account = account;
        String currentPath = FileUtil.getCurrentPath();
        File testPropertyFile = new File(currentPath, WebServiceConstants.TEST_PROPERTIES);
        if (testPropertyFile.exists() && testPropertyFile.isFile()) {
            try {
                props.load(testPropertyFile);
                defaultWSDLUrl = props.getValue("webservice.url");
                defaultWSDLClass = props.getValue("webservice.class");
            }
            catch (IOException ioe) {
                logger.error("Failed to load test.properties file.", ioe);
            }
        }
        logger.info("defaultWSDLUrl == " + defaultWSDLUrl);

        String endpoint;
        if ("false".equalsIgnoreCase(account.getHttps())) {
            endpoint = "http://" + account.getHost() + ":" + account.getPort() + defaultWSDLUrl;
        } else {
            endpoint = "https://" + account.getHost() + ":" + account.getPort() + defaultWSDLUrl;
        }

        logger.info("User " + account.getUserName() + " [" + account.getPassword() + "] is accessing WSDL " + endpoint);
        String mainClassName = defaultWSDLClass.substring(defaultWSDLClass.lastIndexOf(".") + 1);
        if ("Ambassador".equalsIgnoreCase(mainClassName)) {
            isFalconTest = false;
            proxy = new AmbassadorProxy(endpoint);
        } else {
            isFalconTest = true;
            falconProxy = new Ambassador4FalconProxy(endpoint);
        }

        //get realToken
        try {
            String fullAccessToken = "";
            String username = account.getUserName();
            String password = account.getPassword();
            fullAccessToken = isFalconTest ? falconProxy.login(username, password) : proxy.login(username, password);
            logger.info("Access token ==== " + fullAccessToken);
            realToken = getRealAccessToken(fullAccessToken);
        }
        catch (Exception e) {
            logger.error("Failed to login to " + endpoint, e);
        }

        //Abassador Webservice WSDL to operations(XML) file
        parseWSDL(endpoint);

        // read operation XML files to operationList
        operationList = readOpeationXML();
    }

    // transform "fullAccessToken" into "realToken"
    private static String getRealAccessToken(String fullAccessToken) {
        String realToken = fullAccessToken;
        if (StringUtil.isBlank(realToken)) {
            int index = fullAccessToken.indexOf("+_+");
            if (index > 0) {
                realToken = fullAccessToken.substring(0, index);
            }
        }

        return realToken;
    }

    //parsing Abassador Webservice WSDL file into operations(XML) file
    private void parseWSDL(String endpoint) {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer tr;
        try {
            logger.info("Start parsing WSDL into local operation XML file ...");
            tr = tFactory.newTransformer(new StreamSource(currentPath + "/" + WebServiceConstants.WSDL2OPERATION_FILE));
            tr.transform(new StreamSource(endpoint), new StreamResult(currentPath + "/" + WebServiceConstants.OPERATION_FILE));
            logger.info("Start parsing WSDL into local operation XML file successfully.");
        }
        catch (TransformerException e) {
            logger.error("Failed to parse WSDL file.", e);
        }
    }

    private ArrayList<Operation> readOpeationXML() {
        String filePath = currentPath + "/" + WebServiceConstants.OPERATION_FILE;
        File operationXML = new File(filePath);
        if (!operationXML.exists() || !operationXML.isFile()) {
            logger.error("operations_" + account.getHost() + "_" + account.getPort() + ".xml does not exist.");
            return null;
        }

        Document doc = null;
        try {
            doc = new SAXReader().read(operationXML);
        }
        catch (Exception e) {
            logger.error("Failed to read operations.xml file.", e);
        }

        Element root = doc.getRootElement();
        ArrayList<Operation> operationList = new ArrayList<Operation>();
        List<Element> operations = root.elements("operation");
        for (Element operation : operations) {
            String name = operation.attributeValue("name");
            String returnType = operation.element("return").attributeValue("type");
            ArrayList<Parameter> paramList = new ArrayList<Parameter>();

            List<Element> params = operation.elements("param");
            for (Element param : params) {
                String paramType = param.attributeValue("type");
                String paramName = param.getStringValue();
                paramList.add(new Parameter(paramType, paramName));
            }
            operationList.add(new Operation(name, returnType, paramList));

        }
        Collections.sort(operationList,
                new Comparator()
                {
                    public int compare(Object a, Object b) {
                        Operation o1 = (Operation) a;
                        Operation o2 = (Operation) b;
                        return o1.getName().compareTo(o2.getName());
                    }
                });

        return operationList;
    }

    private static void createAccountXML(Account account, String fileName) {
        if (account == null || StringUtil.isBlank(fileName))
            return;

        Document doc = DocumentHelper.createDocument();
        Element accountsElement = doc.addElement("accounts");

        Element accountElement = accountsElement.addElement("account");

        Element hostElement = accountElement.addElement("host");
        hostElement.setText(account.getHost());

        Element portElement = accountElement.addElement("port");
        portElement.setText(account.getPort());

        Element userNameElement = accountElement.addElement("userName");
        userNameElement.setText(account.getUserName());

        Element passwordElement = accountElement.addElement("password");
        passwordElement.setText(account.getPassword());

        Element httpsElement = accountElement.addElement("https");
        httpsElement.setText(account.getHttps());

        // write XML file
        File accountsXML = new File(fileName);
        try {
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");
            XMLWriter out = new XMLWriter(new FileWriter(accountsXML), format);
            out.write(doc);
            out.close();
        }
        catch (Exception e) {
            logger.error("Failed to create account XML file.", e);
        }
    }

    private static void addAccountXML(Account newAccount, String fileName) {
        File accountsXML = new File(fileName);
        if (!accountsXML.exists() || !accountsXML.isFile()) {
            logger.error("Failed to read account XML file " + fileName);
            createAccountXML(newAccount, fileName);
        }

        Document doc = null;
        try {
            doc = new SAXReader().read(accountsXML);
        }
        catch (Exception e) {
            logger.error("Failed to add new account to XML file.", e);
        }
        Element root = doc.getRootElement();
        Element newAccountElement = root.addElement("account");

        Element hostElement = newAccountElement.addElement("host");
        hostElement.setText(newAccount.getHost());

        Element portElement = newAccountElement.addElement("port");
        portElement.setText(newAccount.getPort());

        Element userNameElement = newAccountElement.addElement("userName");
        userNameElement.setText(newAccount.getUserName());

        Element passwordElement = newAccountElement.addElement("password");
        passwordElement.setText(newAccount.getPassword());

        Element httpsElement = newAccountElement.addElement("https");
        httpsElement.setText(newAccount.getHttps());

        try {
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");
            XMLWriter out = new XMLWriter(new FileWriter(accountsXML), format);
            out.write(doc);
            out.close();
        }
        catch (Exception e) {
            logger.error("Failed to write account XML file.", e);
        }
    }


    /**
     * @return the realToken
     */
    public String getRealToken() {
        return realToken;
    }

    /**
     * @return the account
     */
    public Account getAccount() {
        return account;
    }

    /**
     * @return the proxy
     */
    public Object getProxy() {
        return isFalconTest ? falconProxy : proxy;
    }

    /**
     * @return the operationList
     */
    public ArrayList<Operation> getOperationList() {
        return operationList;
    }

    /**
     * @param realToken the realToken to set
     */
    public void setRealToken(String realToken) {
        this.realToken = realToken;
    }

    /**
     * @param account the account to set
     */
    public void setAccount(Account account) {
        this.account = account;
    }

    /**
     * @param operationList the operationList to set
     */
    public void setOperationList(ArrayList<Operation> operationList) {
        this.operationList = operationList;
    }

    public boolean isFalconTest() {
        return isFalconTest;
    }
}