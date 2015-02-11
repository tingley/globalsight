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

package com.globalsight.cxe.adapter.vignette;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.BaseAdapter;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.GeneralException;
import com.globalsight.vignette.VignetteConnection;
import com.vignette.cms.client.beans.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;

/**
 * Helper class used by the VignetteAdapter
 */
public class VignetteExporter
{
    // Private Members
    private CxeMessage m_cxeMessage = null;
    private String m_eventFlowXml = null;
    private String[] m_errorArgs = null;
    private VignetteConnection m_conn = null;
    private Logger m_logger = null;

    //data from the event flow xml
    private String m_objectId = null;
    private String m_path = null;
    private String m_returnPath = null;
    private String m_sourceLocale = null;
    private String m_targetLocale = null;
    private String m_targetProjectMid = null;
    private String m_sourceProjectMid = null;
    private String m_returnStatus = null;
    private String m_versionFlag = null;
    private String m_defaultWorkflow = null;

    /**
     * Creates a VignetteExporter helper object using
     * the EventFlowXml and Content. This helper object
     * will actually write the content back to Vignette
     * 
     * @param p_errorArgs -- error args to use in case of an export error
     */
    public VignetteExporter(CxeMessage p_cxeMessage, Logger p_logger, VignetteConnection p_conn)
    {
        m_cxeMessage = p_cxeMessage;
        m_eventFlowXml = m_cxeMessage.getEventFlowXml();
        m_errorArgs = new String[2];
        m_errorArgs[0] = p_logger.getName();
        m_conn = p_conn;
        m_logger = p_logger;
    }


    /**
     * Actually does the exporting to Vignette by using
     * Vignette's API
     * 
     * @exception VignetteAdapterException
     */
    public void export() throws VignetteAdapterException
    {
        parseEventFlowXml();
        m_errorArgs[1] = m_path; //add the filename of the Vignette content to the error arg

        try
        {
            //create a static file
            m_logger.debug("Writing to target project " + m_targetProjectMid);
            Project targetProj = (Project) m_conn.cms.findByManagementId(m_targetProjectMid);
	    CMSObject srcObj = m_conn.cms.findByManagementId(m_objectId); 
            String name= srcObj.getName();
	    StaticFile srcFile = (StaticFile) srcObj;
	    Project srcProj = srcFile.getProject();

	    //BMC RFE to merely prepend the locale specific directory to the path
	    StringBuffer sbt = new StringBuffer();
	    sbt.append("/").append(m_targetLocale).append(m_path);
	    String targetPath = sbt.toString();

            m_logger.info("Writing file (" + name + ") to " + targetPath);

            BaseAdapter.preserveOriginalFileContent(m_cxeMessage.getMessageData(),
                                                    m_cxeMessage.getParameters());

            StaticFile lastVersion = (StaticFile) m_conn.cms.findByDocrootPath(targetPath);
            if (lastVersion == null)
            {
		m_logger.info("The doc does not already exist, creating it in Vignette.");
		//the file does not exist, make sure that all the sub projects exist before
		//creating the file
		Project targetSubProject = createVignetteProjectStructure(srcProj,targetProj);

		//now write out the file
                StaticFile sfile = targetSubProject.createFile();
                sfile.setPath(targetPath);
                sfile.setName(name);
                sfile.commit();
                sfile.setAutoCommit(true);
                sfile.setContentsFromBytes(readContentAsBytes());
            }
            else
            {
		m_logger.info("The doc already exists in Vignette, overwriting.");
                lastVersion.setContentsFromBytes(readContentAsBytes());
                lastVersion.commit();
            }
        }
        catch (Exception e)
        {
            m_logger.error("Unable to export content to Vignette.",e);
            throw new VignetteAdapterException("InputOutputEx",m_errorArgs,e);
        }
    }


    /**
     * Parses the EventFlowXML to get whatever data
     * the Vignette Exporter needs from it.
     * 
     * @exception VignetteAdapterException
     */
    private void parseEventFlowXml() throws VignetteAdapterException
    {
        try
        {
            StringReader sr = new StringReader(m_eventFlowXml);
            InputSource is = new InputSource(sr);
            DOMParser parser = new DOMParser();
            parser.setFeature("http://xml.org/sax/features/validation", false); //don't validate
            parser.parse(is);
            Element rootElem = parser.getDocument().getDocumentElement();
            parseTargetInformation(rootElem);
            parseSourceInformation(rootElem);
            parseVignetteSpecificData(rootElem);
        }
        catch (Exception e)
        {
            m_logger.error("Unable to parse EventFlowXml. Cannot determine information needed to export to Vignette.",e);
            throw new VignetteAdapterException("CxeInternalEx",m_errorArgs,e);
        }
    }


    /**
     * Parses out the Vignette Specific information
     * like ObectID, ReturnPath, and Path
     * 
     * @param p_rootElement
     *               -- the root of the event flow xml
     * @exception Exception
     */
    private void parseVignetteSpecificData(Element p_rootElement)
    throws Exception
    {
        NodeList nl = p_rootElement.getElementsByTagName("category");
        Element categoryElement = null;
        boolean foundVignetteCategory = false;
        for (int i=0; i < nl.getLength() & !foundVignetteCategory; i++)
        {
            categoryElement = (Element) nl.item(i);
            if (categoryElement.getAttribute("name").equals("Vignette"))
                foundVignetteCategory = true;
        }

        if (!foundVignetteCategory)
            throw new Exception("No Vignette Category in EventFlowXML");

        NodeList attributeList = categoryElement.getElementsByTagName("da");
        for (int k=0; k < attributeList.getLength();k++)
        {
            Element attrElement = (Element)attributeList.item(k);
            String name = attrElement.getAttribute("name");
            NodeList values = attrElement.getElementsByTagName("dv");
            Element valElement = (Element) values.item(0);
            String val = "";
            if (valElement.getFirstChild() != null)
                val = valElement.getFirstChild().getNodeValue();
            m_logger.debug( name + " = " + val);
            if (name.equals("ObjectId"))
                m_objectId = val;
            else if (name.equals("Path"))
                m_path = val;
            else if (name.equals("ReturnPath"))
                m_returnPath = val;
            else if (name.equals("TargetProjectMid"))
                m_targetProjectMid = val;
            else if (name.equals("SourceProjectMid"))
                m_sourceProjectMid = val;
            else if (name.equals("ReturnStatus"))
                m_returnStatus = val;
            else if (name.equals("VersionFlag"))
                m_versionFlag = val;
            else if (name.equals("DefaultWorkflow"))
                m_defaultWorkflow = val;
        }
    }

    /**
     * Parses out some target information (locale)
     * 
     * @param p_rootElement
     *               -- the root of the event flow xml
     * @exception Exception
     */
    private void parseTargetInformation(Element p_rootElement)
    throws Exception
    {
        NodeList nl = p_rootElement.getElementsByTagName("target");
        Element targetElement = (Element) nl.item(0);
        nl = targetElement.getElementsByTagName("locale");
        Element localeElement = (Element) nl.item(0);
        m_targetLocale = localeElement.getFirstChild().getNodeValue();
        m_logger.debug( "targetLocale = " + m_targetLocale);
    }

    /**
     * Parses out some source information (locale)
     * 
     * @param p_rootElement -- the root of the event flow xml
     * @exception Exception
     */
    private void parseSourceInformation(Element p_rootElement)
    throws Exception
    {
        NodeList nl = p_rootElement.getElementsByTagName("source");
        Element sourceElement = (Element) nl.item(0);
        nl = sourceElement.getElementsByTagName("locale");
        Element localeElement = (Element) nl.item(0);
        m_sourceLocale = localeElement.getFirstChild().getNodeValue();
        m_logger.debug( "sourceLocale = " + m_sourceLocale);
    }



    /**
     * Replaces one substring with another within a main string.
     *  
     * @param	s		Main string.
     * @param	find	The substring to find.
     * @param	rep		Replaces the find substring.
     * @return	The new string.
     */
    public static String replaceString(String s,
                                       String find,
                                       String rep) {

        return replaceString(new StringBuffer(s),
                             find, rep).toString();
    }

    /**
     * Replaces one substring with another within a main string.
     *
     * @param	sb		Main string. (StringBuffer)
     * @param	find	The substring to find.
     * @param	rep		Replaces the find substring.
     * @return	The new string. (StringBuffer)
     */
    public static StringBuffer replaceString(StringBuffer sb,
                                             String find,
                                             String rep) {
        StringBuffer buf = new StringBuffer(sb.toString());
        int startIndex = 0;
        int stringLocation = 0;

        boolean done = false;

        // Halts if no replacements
        if (!find.equals(rep))
        {
            String s = buf.toString();

            // Continues while more substring(s) (find) exist
            while (!done)
            {

                // Grab the position of the substring (find)
                if ((stringLocation = s.indexOf(find,startIndex)) >= 0)
                {

                    // Replace "find" with "rep"
                    buf.delete(stringLocation, stringLocation + find.length());
                    buf.insert(stringLocation, rep);
                    startIndex = stringLocation + rep.length();
                    s = buf.toString();

                }
                else
                {
                    done = true;
                }
            }
        }

        return buf;
    }



    /**
     * Creates the mirror directory(project) structure
     * of the source project in the target project minus
     * the top level dir
     * 
     * @param p_srcProj
     * @param p_trgProj
     * @return Returns the appropriate sub-project in the target project
     *         where the file can be created
     */
    private Project createVignetteProjectStructure(Project p_srcProj, Project p_trgProj)
    throws Exception
    {
	CMSObject srcBaseProject = m_conn.cms.findByManagementId(m_sourceProjectMid); 
	m_logger.debug( " src base project is: " + srcBaseProject.toString());

	//figure out all the child projects relative to the src base project
	ArrayList projects = getRelativeProjects(p_srcProj, (Project)srcBaseProject);

	//now create the projects in the target project
	Project[] existingProjects = p_trgProj.getProjects();
	boolean projectExists = false;
	Project n = null;
	for (int j=0; j < existingProjects.length; j++ )
	{
	    if (existingProjects[j].getName().equals(m_targetLocale))
		{
		m_logger.debug( "Project " + m_targetLocale + " already exists.");
		projectExists = true;
		n = existingProjects[j];
		break;
	    }
	}
	//create the top level locale specific project
	if (projectExists == false)
	{
		m_logger.debug( "Creating " + m_targetLocale + " at top level");
		n = p_trgProj.createProject();
		n.setName(m_targetLocale);
		n.commit();
	}

	//create the other sub projects
	for (int i=projects.size()-1; i > -1; i--)
	{
	    Project p = (Project) projects.get(i);
	    existingProjects = n.getProjects();
	    projectExists = false;
	    for (int j=0; j < existingProjects.length; j++ )
		{
		if (existingProjects[j].getName().equals(p.getName()))
		    {
		    m_logger.debug( "Project " + p.getName() + " already exists.");
		    projectExists = true;
		    n = existingProjects[j];
		    break;
		}
	    }

	    if (!projectExists)
		{
		n = n.createProject();
		n.setName(p.getName());
		n.commit();
		m_logger.debug( "created project: " + n.getName() + " with id " + n.getManagementId());
	    }
	}

	return n;
    }

    //returns the relative path of the sub project to the project
    //each project name is an item in the list
    private ArrayList getRelativeProjects(Project p_subProject, Project p_baseProject)
    throws Exception
    {
	ArrayList projects = new ArrayList();
	boolean hitBaseProject = false;
	Project p = p_subProject;
	String mid = p_baseProject.getManagementId();
	while (hitBaseProject != true)
	{
	    if (p.getManagementId().equals(mid))
	    {
		hitBaseProject = true;
	    }
	    else
	    {
		m_logger.debug("Rel project: " + p.getName());
		projects.add(p);
		p = p.getProject();
	    }
	}
	m_logger.debug("Size of rel projects array: " + projects.size());
	return projects;
    }


    /**
     * Reads the message data from the cxemessage and returns all the data
     * 
     * @return byte[]
     */
    private byte[] readContentAsBytes() throws Exception
    {
        MessageData md = m_cxeMessage.getMessageData();
        int size = (int) md.getSize();
        byte[] buffer = new byte[size];
        BufferedInputStream bis = new BufferedInputStream(md.getInputStream());
        bis.read(buffer,0,size);
        bis.close();
        m_cxeMessage.setDeleteMessageData(true);
        return buffer;
    }
}

