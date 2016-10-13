/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.everest.persistence;


public class DescriptorNamedQueriesTest
{
    //
    // PRIVATE CONSTANTS
    //
    // private static final String[] STRING_TYPES =
    // { "name", "state", "type", "language", "country", "externalpageid",
    // "managerid", "batchid", "userid" };
    //
    // //
    // // PRIVATE MEMBER VARIABLES
    // //
    //    private transient TopLinkPersistence m_tlp;

    //
    // CONSTRUCTORS
    //
    /* Default constructor; initialize the persistence service */
    // private DescriptorNamedQueriesTest()
    // throws PersistenceException
    // {
    // super();
    // m_tlp = PersistenceService.getInstance();
    // }
    //
    // PRIVATE SUPPORT METHODS
    //
    /* Write out the descriptor and query details for each descriptor in */
    /* the project */
//    private void displayDescriptorsAndQueries()
//    {
//        Enumeration e = getDescriptors();
//        int count = 1;
//        while (e.hasMoreElements())
//        {
//            Descriptor d = (Descriptor) e.nextElement();
//            displayDescriptor(count++, d);
//            displayQueries(d.getQueryManager().getQueries());
//            System.out.println("");
//        }
//    }
//
//    /* Write out detail for the given descriptor */
//    private void displayDescriptor(int p_index, Descriptor p_descriptor)
//    {
//        System.out.println("=============== DESCRIPTOR " + p_index
//                + " ===============");
//        System.out.println(p_descriptor.toString());
//    }

    /* Write out the details for all the queries in the given hashtable */
//    private void displayQueries(Hashtable p_queries)
//    {
//        int queryCount = 1;
//        Enumeration e = p_queries.keys();
//        while (e.hasMoreElements())
//        {
//            String key = (String) e.nextElement();
//            DatabaseQuery q = (DatabaseQuery) p_queries.get(key);
//            System.out.println("");
//            System.out.println("--- QUERY " + (queryCount++) + " ---");
//            displayQueryDetails(q);
//        }
//        if (queryCount == 1)
//        {
//            System.out.println("--- No Named Queries ---");
//        }
//    }
//
//    /* Write out details for the given query. */
//    private void displayQueryDetails(DatabaseQuery p_query)
//    {
//        System.out.println("   Type      = " + p_query.getClass().getName());
//        System.out.println("   Name      = " + p_query.getName());
//        System.out.println("   Ref Class = "
//                + p_query.getReferenceClass().getName());
//        Vector v = p_query.getArguments();
//        System.out.println("   Parms     = " + v);
//        Vector args = new Vector();
//        for (int i = 0; i < v.size(); i++)
//        {
//            String s = ((String) v.elementAt(i)).toLowerCase();
//            if (isStringType(s))
//            {
//                args.addElement("" + (char) (i + 65));
//            }
//            else
//            {
//                args.addElement(new Long(i + 1));
//            }
//        }
//        System.out.println("...attempting execution with args " + args + "...");
//        executeQuery(p_query.getName(), args);
//    }
//
//    /* Determine whether the given string represents a "string-type" in */
//    /* the context of an SQL statement */
//    private boolean isStringType(String p_string)
//    {
//        boolean isStringType = false;
//
//        for (int i = 0; !isStringType && i < STRING_TYPES.length; i++)
//        {
//            isStringType = p_string.indexOf(STRING_TYPES[i]) > -1;
//        }
//        return isStringType;
//    }
//
//    /* Call on the persistence service to execute the given query with the */
//    /* given collection of arguments */
//    private void executeQuery(String p_name, Vector p_args)
//    {
//        try
//        {
//            if (p_args.size() > 0)
//            {
//                m_tlp.executeNamedQuery(p_name, p_args, false);
//            }
//            else
//            {
//                m_tlp.executeNamedQuery(p_name, false);
//            }
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//    }
//
//    /* Get all descriptors that have been loaded by the current project */
//    private Enumeration getDescriptors()
//    {
//        Hashtable ht = new Hashtable();
//        try
//        {
//            ht = m_tlp.acquireClientSession().getDescriptors();
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//        return ht.elements();
//    }

    /* Execute the test methods */
    // private void run() throws Exception
    // {
    // displayDescriptorsAndQueries();
    //    }

    //
    // PUBLIC STATIC METHOD
    //
    /**
     * Create a new instance of the class and execute its run() method.
     */
    public static void main(String[] args)
    {
        // try
        // {
        // DescriptorNamedQueriesTest test = new DescriptorNamedQueriesTest();
        // test.run();
        // }
        // catch (Exception e)
        // {
        // System.out.println("DescriptorNamedQueriesTest startup failure...");
        // e.printStackTrace();
        // }
        // System.exit(0);
    }
}
