/* Copyright (c) 2004-2005, GlobalSight Corporation.  All rights reserved.
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

import java.io.FileReader;
import java.net.URL;
import com.globalsight.www.webservices.VendorManagementServiceLocator;
import com.globalsight.www.webservices.VendorManagementService;
import com.globalsight.www.webservices.VendorManagement;

/**
 * The VendorManagementWebServiceClient is an example
 * program used to test out the VM Web Service. It should not be
 * used in any production environment.
 * 
 * It serves as a code example for others to make their own
 * web service clients in Java.
 */
public class VendorManagementWebServiceClient
{
    private String m_wsdlUrl = null;
    private String m_accessToken = null;
    private VendorManagement m_vm = null;

    /**
     * Creates a VendorManagementServiceClient object
     * 
     * @param p_wsdlUrl  WSDL URL to access Vendor Mgmt web services
     */
    public VendorManagementWebServiceClient(String p_wsdlUrl)
    throws Exception
    {
        m_wsdlUrl = p_wsdlUrl;
        VendorManagementServiceLocator loc = new VendorManagementServiceLocator();
        m_vm = loc.getVendorManagementWebService(new URL(m_wsdlUrl));
    }

    /**
     * Main VendorManagementServiceClient program
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        if(args.length < 5)
        {
            printUsage();
            return;
        }
        String hostName = args[0];
        String port = args[1];
        String wsdlUrl = "http://" + hostName + ":" + port +
            "/globalsight/services/VendorManagementWebService?wsdl";            
        String username = args[2];
        String password = args[3];
        try
        {
            VendorManagementWebServiceClient client =
                new VendorManagementWebServiceClient(wsdlUrl);
            String command = args[4];
            if(command.equals("hello"))
            {
                client.helloWorld();
                return;
            }

            //everything else needs to login first
            client.login(username,password);
            if(command.equals("addVendor"))
            {
                client.executeSimpleGetWebServiceCall(args,"addVendor",true);
            }
            else if(command.equals("modifyVendor"))
            {
                client.executeSimpleGetWebServiceCall(args,"modifyVendor",true);
            }
            else if(command.equals("removeVendor"))
            {
                client.executeSimpleGetWebServiceCall(args,"removeVendor",false);
            } 
            else if(command.equals("getVendorInfo"))
            {
                client.executeSimpleGetWebServiceCall(args,"queryVendorBasicInfo");
            } 
            else if(command.equals("getVendorDetail"))
            {
                client.executeSimpleGetWebServiceCall(args,"queryVendorDetails",false);
            } 
            else
            {
                printUsage();
            }

        } catch(Exception e)
        {
            System.out.println("Failed to run web service client with exception " + e);
        }
    }


    /**
     * Gets the content of an XML file
     * 
     * @param p_fileName
     * @return 
     * @exception Exception
     */
    private String getXmlFileContent(String p_fileName)
        throws Exception
    {
        FileReader fr = new FileReader(p_fileName);
        StringBuffer vendorXml = new StringBuffer();
        char[] newLine = new char[80];
        int numCharsRead = 0;
        while((numCharsRead = fr.read(newLine, 0, 80)) != -1)
        {
            vendorXml.append(newLine, 0, numCharsRead);
            newLine = new char[80];
        }
        fr.close();
        String vendorXmlString = vendorXml.toString();
        return vendorXmlString;
    }


    /**
     * Prints out command line usage
     */
    private static void printUsage()
    {
        System.out.println("USAGE: VendorManagementWebServiceClient <hostname> <port> <username> <password> <command>");
        System.out.println("\tWhere command is one of the following:");
        System.out.println("\thello -- Causes the web service to say hello. Simple connectivity test.");
        System.out.println("\taddVendor <vendorXml> -- Add a vendor.\r\n\t\tMust pass the vendor data in a specific XML format.");
        System.out.println("\tmodifyVendor <vendorXml> -- Moidfy a vendor.\r\n\t\tMust pass the vendor data in a specific XML format.");
        System.out.println("\tremoveVendor <customVendorId> -- Remove the vendor with the specific custom vendor id.");
        System.out.println("\tgetVendorInfo -- Returns XML describing all the vendors in the system.");
        System.out.println("\tgetVendorDetail < customVendorId> -- Returns XML describing the specific vendor in more detail.");
        System.out.println("Each command except hello first automatically\r\n\t\tinvokes Login() on the webservice.");
    }

    /**
     * Executes a simple web service call that expects
     * the basic 5 command line args. The only value
     * sent to the web service is the access token.
     * Output is sent to stdout.
     * 
     * @param p_args command line args
     * @param p_methodName
     *               name of web service method to call
     */
    private void executeSimpleGetWebServiceCall(String[] p_args, String p_methodName)
    throws Exception
    {
        if (p_args.length !=5)
        {
            printUsage();
            return;
        }
        String rv = null;
        if (p_methodName.equals("queryVendorBasicInfo"))
            rv = m_vm.queryVendorBasicInfo(m_accessToken);
        System.out.println(rv);
    }


    /**
     * Executes a simple web service call that expects
     * the basic 6 command line args. Output is sent
     * to stdout.
     * 
     * @param p_args command line args
     * @param p_methodName
     *               name of web service method to call
     */
    private void executeSimpleGetWebServiceCall(String[] p_args, String p_methodName,
                                                boolean p_fifthArgIsXml)
    throws Exception
    {
        if (p_args.length !=6)
        {
            printUsage();
            return;
        }
        String fifthArg = p_args[5];
        if (p_fifthArgIsXml)
            fifthArg = getXmlFileContent(fifthArg);

        String rv = null;
        if (p_methodName.equals("addVendor"))
            rv = m_vm.addVendor(m_accessToken,fifthArg);
        else if (p_methodName.equals("modifyVendor"))
            rv = m_vm.modifyVendor(m_accessToken,fifthArg);
        else if (p_methodName.equals("removeVendor"))
            rv = m_vm.removeVendor(m_accessToken,fifthArg);
        else if (p_methodName.equals("queryVendorDetails"))
            rv = m_vm.queryVendorDetails(m_accessToken,fifthArg);

        else
        {
            throw new Exception("Error in VendorManagementServiceClient code. Unhandled web service call.");
        }

        System.out.println(rv);
    }

    /**
     * Calls the helloWorld web service
     * 
     * @param p_args
     * @exception Exception
     */
    private void helloWorld() throws Exception
    {
        String rv= m_vm.helloWorld();
        System.out.println(rv);
    }

    /**
     * Logs into the web service
     * 
     * @param p_username
     * @param p_password
     * @return 
     * @exception Exception
     */
    private void login (String p_username, String p_password) throws Exception
    {
        m_accessToken = m_vm.login(p_username,p_password);
    }
}

