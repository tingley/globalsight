package test;

import java.io.*;
import java.util.*;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import util.*;


public class AccountM {
	
	
	
	/**
	 * in UI
	 * TestManager t = null;
	 * 
	 * Login:
	 * if(t==null) t = new TestManagerImpl(account)
	 * else t.switch(account)
	 * 
	 * Account Management
	 * if(t.getAllAccount!=null) display all accounts
	 * else no Account yet
	 * 
	 * */
	
	public static void createXML(Account account){
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
		String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		File accountsXML = new File(path + "/" +WebServiceConstants.ACCOUNTS_FILE);
		try{
			XMLWriter out = new XMLWriter(new FileWriter(accountsXML));
			out.write(doc);
			out.close();
		}
		catch(Exception e){
			WriteLog.info(WebServiceConstants.IS_LOG, "Create 'accounts.xml' Exception in AbstractTestManager.writeAccount()");
			PrintStream ps = WriteLog.getPrintStreamForLog(WebServiceConstants.LOG_FILE);
        	e.printStackTrace(ps);
        	e.printStackTrace();
		}
		
		
		
	}
	
	private static void addAccount(Account newAccount){
		String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		File accountsXML = new File(path + "/" + WebServiceConstants.ACCOUNTS_FILE);
		if(!accountsXML.exists()) 
			WriteLog.info(WebServiceConstants.IS_LOG, "'accounts.xml' file does not exists");
		
		Document doc = null;
		try{
			 doc = new SAXReader().read(accountsXML);
		}
		catch(Exception e){
			WriteLog.info(WebServiceConstants.IS_LOG, "Add new Account Exception in AbstractTestManager.writeAccount()");
			PrintStream ps = WriteLog.getPrintStreamForLog(WebServiceConstants.LOG_FILE);
        	e.printStackTrace(ps);
        	e.printStackTrace();
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
		
		// duplicated wirte file codes
		try{
			XMLWriter out = new XMLWriter(new FileWriter(accountsXML));
			out.write(doc);
			out.close();
		}
		catch(Exception e){
			WriteLog.info(WebServiceConstants.IS_LOG, " Add and write 'accounts.xml' Exception in AbstractTestManager.writeAccount()");
			PrintStream ps = WriteLog.getPrintStreamForLog(WebServiceConstants.LOG_FILE);
        	e.printStackTrace(ps);
        	e.printStackTrace();
		}	
	}
}
