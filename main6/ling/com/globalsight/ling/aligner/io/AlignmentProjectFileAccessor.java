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
package com.globalsight.ling.aligner.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.globalsight.everest.aligner.AlignmentStatus;
import com.globalsight.ling.aligner.AlignmentProject;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.util.AmbFileStoragePathUtils;


/**
 * AlignmentProjectFileAccessor is responsible to read and write the
 * project file.
 */
public class AlignmentProjectFileAccessor
{
//    private static File c_file = new File(
//        AlignmentProject.ALIGNER_PACKAGE_DIRECTORY, "Aligner.prj");
    
    private static File getAlignerProjectFile()
    {
        return new File(AmbFileStoragePathUtils.getAlignerPackageDir(), "Aligner.prj");
    }

    /**
     * Reads the project file and returns a List of AlignmentStatus objects.
     *
     * @return Set of AlignmentStatus objects
     */
    static public Set read()
        throws Exception
    {
        Set status = null;

        File c_file = getAlignerProjectFile();
        synchronized(c_file)
        {
            if (c_file.exists())
            {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);

                SAXParser parser = factory.newSAXParser();
                ProjectHandler handler = new ProjectHandler();
                Reader reader = new InputStreamReader(
                      new FileInputStream(c_file), "UTF-8");
                parser.parse(new InputSource(reader), handler);
                reader.close();

                status = handler.getAlignmentStatus();
            }
            else
            {
                status = new HashSet();
            }
        }

        return status;
    }


    /**
     * add a project to the project file.
     *
     * @param p_status AlignmentStatus object
     */
    static public void addProject(AlignmentStatus p_status)
        throws Exception
    {
        File c_file = getAlignerProjectFile();
        synchronized(c_file)
        {
            Set projects = null;

            if (c_file.exists())
            {
                projects = read();
            }
            else
            {
                projects = new HashSet();
            }

            // remove old status and add new status
            projects.remove(p_status);
            projects.add(p_status);

            write(projects);
        }
    }


    /**
     * delete a project from the project file.
     *
     * @param p_projectName project name to be deleted
     */
    static public void deleteProject(String p_projectName)
        throws Exception
    {
        File c_file = getAlignerProjectFile();
        synchronized(c_file)
        {
            Set projects = read();

            Iterator it = projects.iterator();
            while (it.hasNext())
            {
                AlignmentStatus status = (AlignmentStatus)it.next();

                if (status.getPackageName().equals(p_projectName))
                {
                    projects.remove(status);
                    break;
                }
            }

            write(projects);
        }
    }

    /**
     * get a project status for a specified project.
     *
     * @param p_projectName project name to be deleted
     * @return AlignmentStatus object. When a specified project cannot
     * be found, null is returned.
     */
    static public AlignmentStatus getProjectStatus(String p_projectName)
        throws Exception
    {
        File c_file = getAlignerProjectFile();
        synchronized(c_file)
        {
            Set projects = read();

            Iterator it = projects.iterator();
            while (it.hasNext())
            {
                AlignmentStatus status = (AlignmentStatus)it.next();

                if (status.getPackageName().equals(p_projectName))
                {
                    return status;
                }
            }
        }

        return null;
    }



    static private void write(Set p_projects)
        throws IOException
    {
        File c_file = getAlignerProjectFile();
        c_file.delete();

        PrintWriter output = new PrintWriter(
            new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(c_file), "UTF-8")));

        writeHeader(output);

        XmlEntities xmlEncoder = new XmlEntities();
        Iterator it = p_projects.iterator();
        while (it.hasNext())
        {
            AlignmentStatus status = (AlignmentStatus)it.next();

            writeStatus(output, status, xmlEncoder);
        }

        writeTrailer(output);

        output.close();
    }



    static private void writeHeader(PrintWriter p_output)
        throws IOException
    {
        // projects root element
        p_output.println("<projects>");
        checkError(p_output);
    }


    static private void writeTrailer(PrintWriter p_output)
        throws IOException
    {
        p_output.println("</projects>");
        checkError(p_output);
    }


    static private void writeStatus(PrintWriter p_output,
        AlignmentStatus p_status, XmlEntities p_xmlEncoder)
        throws IOException
    {
        p_output.println("  <project>");
        p_output.println("    <name>" +
            p_xmlEncoder.encodeStringBasic(p_status.getPackageName()) +
            "</name>");

        p_output.println("    <totalUnitNumber>" +
            p_status.getTotalUnitNumber() + "</totalUnitNumber>");
        p_output.println("    <errorUnitNumber>" +
            p_status.getErrorUnitNumber() + "</errorUnitNumber>");

        List error = p_status.getErrorMessages();
        if (error != null)
        {
            Iterator it = error.iterator();
            while (it.hasNext())
            {
                String errorMessage = (String)it.next();

                p_output.println("    <error>" +
                    p_xmlEncoder.encodeStringBasic(errorMessage) +
                    "</error>");
            }
        }

        p_output.println("  </project>");

        checkError(p_output);
    }


    static private void checkError(PrintWriter p_output)
        throws IOException
    {
        if (p_output.checkError())
        {
            throw new IOException("PrintWriter write error");
        }
    }

    static private class ProjectHandler
        extends DefaultHandler
    {
        private XmlEntities m_xmlEncoder = new XmlEntities();
        private AlignmentStatus m_currentStatus;
        private Set m_statusList = new HashSet();
        private StringBuffer m_sb = new StringBuffer();
        private boolean m_inContents = false;

        public void startElement(String uri, String localName,
            String qName, Attributes attributes)
            throws SAXException
        {
            try
            {
                if (qName.equals("project"))
                {
                    processProjectStart();
                }
                else if (qName.equals("name") ||
                    qName.equals("totalUnitNumber") ||
                    qName.equals("errorUnitNumber") ||
                    qName.equals("error"))
                {
                    processElementStart();
                }
            }
            catch (Exception e)
            {
                SAXException se;

                if (e instanceof SAXException)
                {
                    se = (SAXException)e;
                }
                else
                {
                    se = new SAXException(e);
                }

                throw se;
            }
        }


        public void endElement(String uri, String localName, String qName)
            throws SAXException
        {
            try
            {
                if (qName.equals("project"))
                {
                    processProjectEnd();
                }
                else if (qName.equals("name"))
                {
                    processNameEnd();
                }
                else if (qName.equals("totalUnitNumber"))
                {
                    processTotalUnitNumberEnd();
                }
                else if (qName.equals("errorUnitNumber"))
                {
                    processErrorUnitNumberEnd();
                }
                else if (qName.equals("error"))
                {
                    processErrorEnd();
                }
            }
            catch(Exception e)
            {
                SAXException se;

                if (e instanceof SAXException)
                {
                    se = (SAXException)e;
                }
                else
                {
                    se = new SAXException(e);
                }

                throw se;
            }
        }



        public void characters(char[] ch, int start, int length)
            throws SAXException
        {
            if (m_inContents)
            {
                String content = new String(ch, start, length);
                content = m_xmlEncoder.encodeStringBasic(content);

                m_sb.append(content);
            }
        }


        // ErrorHandler interface methods

        public void error(SAXParseException e)
            throws SAXException
        {
            throw new SAXException("Project file parse error at\n  line " +
                e.getLineNumber() + "\n  column " + e.getColumnNumber() +
                "\n  Message:" + e.getMessage());
        }

        public void fatalError(SAXParseException e)
            throws SAXException
        {
            error(e);
        }

        public void warning(SAXParseException e)
        {
            System.err.println("Project file parse warning at\n  line " +
                e.getLineNumber() + "\n  column " + e.getColumnNumber() +
                "\n  Message:" + e.getMessage());
        }


        public Set getAlignmentStatus()
        {
            return m_statusList;
        }


        private void processProjectStart()
        {
        }


        private void processElementStart()
        {
            m_sb.setLength(0);
            m_inContents = true;
        }

        private void processProjectEnd()
        {
            m_statusList.add(m_currentStatus);
        }


        private void processNameEnd()
        {
            String projectName = m_sb.toString();

            m_currentStatus = new AlignmentStatus(projectName, "",
                projectName + AlignmentProject.ALIGNMENT_PKG_EXT);

            m_inContents = false;
        }

        private void processTotalUnitNumberEnd()
            throws Exception
        {
            m_currentStatus.setTotalUnitNumber(
                Integer.parseInt(m_sb.toString()));
            m_inContents = false;
        }

        private void processErrorUnitNumberEnd()
            throws Exception
        {
            m_currentStatus.setErrorUnitNumber(
                Integer.parseInt(m_sb.toString()));
            m_inContents = false;
        }

        private void processErrorEnd()
        {
            m_currentStatus.addErrorMessage(m_sb.toString());
            m_inContents = false;
        }
    }
}
