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

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.globalsight.ling.aligner.AlignmentResult;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.util.SortUtil;

/**
 * GamReader reads GlobalSight Alignment Map (GAM) file and set the alignment
 * info in AlignmentResult object.
 */

public class GamReader implements GamConstants
{
    // Key: Tuv id (Long)
    // Value: BaseTmTuv
    private Map m_sourceTuvs = new HashMap();
    private Map m_targetTuvs = new HashMap();

    private String m_fileName;

    /**
     * Reads a GAM file and returns an AlignmentResult object.
     * 
     * @param p_reader
     *            Reader object from which the GAM file is read
     * @param p_sourceTuvs
     *            List of source BaseTmTuv objects
     * @param p_targetTuvs
     *            List of target BaseTmTuv objects
     * @param p_fileName
     *            GAM file name (for error message)
     * @return AlignmentResult object
     */
    public AlignmentResult read(Reader p_reader, List p_sourceTuvs,
            List p_targetTuvs, String p_fileName) throws Exception
    {
        m_fileName = p_fileName;

        setTuvs(p_sourceTuvs, p_targetTuvs);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);

        SAXParser parser = factory.newSAXParser();
        GamHandler handler = new GamHandler();
        parser.parse(new InputSource(p_reader), handler);

        return handler.getAlignmentResult();
    }

    private void setTuvs(List p_sourceTuvs, List p_targetTuvs)
    {
        Iterator it = p_sourceTuvs.iterator();
        while (it.hasNext())
        {
            BaseTmTuv tuv = (BaseTmTuv) it.next();
            m_sourceTuvs.put(new Long(tuv.getId()), tuv);
        }

        it = p_targetTuvs.iterator();
        while (it.hasNext())
        {
            BaseTmTuv tuv = (BaseTmTuv) it.next();
            m_targetTuvs.put(new Long(tuv.getId()), tuv);
        }
    }

    private class GamHandler extends DefaultHandler
    {
        private IdAssembler m_idAssembler = new IdAssembler();
        private String m_srcTmxFileName = null;
        private String m_trgTmxFileName = null;

        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException
        {
            try
            {
                if (qName.equals(ELEM_GAM))
                {
                    processGam(attributes);
                }
                else if (qName.equals(ELEM_ALIGN))
                {
                    processAlign(attributes);
                }
            }
            catch (Exception e)
            {
                SAXException se;
                if (e instanceof SAXException)
                {
                    se = (SAXException) e;
                }
                else
                {
                    se = new SAXException(e);
                }

                throw se;
            }
        }

        public InputSource resolveEntity(String publicId, String systemId)
        {
            if (systemId.indexOf("gam.dtd") > -1)
            {
                return new InputSource(new StringReader(GAM_DTD));
            }
            else
            {
                // use the default behaviour
                return null;
            }
        }

        // ErrorHandler interface methods

        public void error(SAXParseException e) throws SAXException
        {
            throw new SAXException("GAM file parse error at\n  line "
                    + e.getLineNumber() + "\n  column " + e.getColumnNumber()
                    + "\n  Message:" + e.getMessage());
        }

        public void fatalError(SAXParseException e) throws SAXException
        {
            error(e);
        }

        public void warning(SAXParseException e)
        {
            System.err.println("GAM file parse warning at\n  line "
                    + e.getLineNumber() + "\n  column " + e.getColumnNumber()
                    + "\n  Message:" + e.getMessage());
        }

        public AlignmentResult getAlignmentResult()
        {
            AlignmentResult alignmentResult = new AlignmentResult();
            alignmentResult.setSourceTmxFileName(m_srcTmxFileName);
            alignmentResult.setTargetTmxFileName(m_trgTmxFileName);

            Iterator it = m_idAssembler.createIdListPairs().iterator();
            while (it.hasNext())
            {
                IdAssembler.IdListPair idListPair = (IdAssembler.IdListPair) it
                        .next();

                List sourceTuvs = getSourceTuvsByIds(idListPair.getSourceList());
                List targetTuvs = getTargetTuvsByIds(idListPair.getTargetList());

                alignmentResult.addAlignedSegments(sourceTuvs, targetTuvs);
            }

            return alignmentResult;
        }

        private void processGam(Attributes p_attributes) throws Exception
        {
            String soruceTmxFile = p_attributes.getValue(ATT_SOURCE_TMX);
            String targetTmxFile = p_attributes.getValue(ATT_TARGET_TMX);

            m_srcTmxFileName = soruceTmxFile;
            m_trgTmxFileName = targetTmxFile;
        }

        private void processAlign(Attributes p_attributes) throws SAXException
        {
            String approved = p_attributes.getValue(ATT_APPROVED);
            if (approved.equals(VALUE_Y))
            {
                String sourceIdsStr = p_attributes.getValue(ATT_SOURCE);
                String targetIdsStr = p_attributes.getValue(ATT_TARGET);

                List sourceIds = parseIdsString(sourceIdsStr);
                List targetIds = parseIdsString(targetIdsStr);

                m_idAssembler.populateIdMap(sourceIds, targetIds);
            }
        }

        private List parseIdsString(String p_idsString)
        {
            List idList = new ArrayList();

            StringTokenizer st = new StringTokenizer(p_idsString, ",");
            while (st.hasMoreTokens())
            {
                String token = st.nextToken();
                token = token.trim();
                Long id = new Long(token);
                idList.add(id);
            }

            return idList;
        }

        private List getSourceTuvsByIds(List p_ids)
        {
            return getTuvsByIds(p_ids, m_sourceTuvs);
        }

        private List getTargetTuvsByIds(List p_ids)
        {
            return getTuvsByIds(p_ids, m_targetTuvs);
        }

        private List getTuvsByIds(List p_ids, Map p_idTuvMap)
        {
            List tuvList = new ArrayList();
            Iterator it = p_ids.iterator();
            while (it.hasNext())
            {
                Long id = (Long) it.next();
                tuvList.add(p_idTuvMap.get(id));
            }

            return tuvList;
        }

    }

    /*
     * This class is created in order to support the following <align> elements.
     * 
     * <align source="1" target="1" approved="Y" /> <align source="1" target="2"
     * approved="Y" /> <align source="2" target="3" approved="Y" />
     * 
     * A correct way to express the alignment above is:
     * 
     * <align source="1,2" target="1,2,3" approved="Y" />
     * 
     * The QA client can't do it right now. Thus, the class.
     */
    private class IdAssembler
    {
        Map m_sourceToTarget = new HashMap();
        Map m_targetToSource = new HashMap();

        public void populateIdMap(List p_sourceIds, List p_targetIds)
        {
            Iterator itSource = p_sourceIds.iterator();
            while (itSource.hasNext())
            {
                Long sourceId = (Long) itSource.next();

                Iterator itTarget = p_targetIds.iterator();
                while (itTarget.hasNext())
                {
                    Long targetId = (Long) itTarget.next();

                    Set targetList = (Set) m_sourceToTarget.get(sourceId);
                    if (targetList == null)
                    {
                        targetList = new HashSet();
                        m_sourceToTarget.put(sourceId, targetList);
                    }
                    targetList.add(targetId);

                    Set sourceList = (Set) m_targetToSource.get(targetId);
                    if (sourceList == null)
                    {
                        sourceList = new HashSet();
                        m_targetToSource.put(targetId, sourceList);
                    }
                    sourceList.add(sourceId);
                }
            }
        }

        public List createIdListPairs()
        {
            List idListPairList = new ArrayList();

            for (Iterator itSourceKey = m_sourceToTarget.keySet().iterator(); itSourceKey
                    .hasNext(); itSourceKey = m_sourceToTarget.keySet()
                    .iterator())
            {
                IdListPair idListPair = new IdListPair();
                idListPairList.add(idListPair);

                Long sourceId = (Long) itSourceKey.next();
                followIdChains(sourceId, idListPair);
                idListPair.addSourceId(sourceId);
            }

            return idListPairList;
        }

        private void followIdChains(Long p_sourceId, IdListPair p_idListPair)
        {
            Set targetIds = (Set) m_sourceToTarget.get(p_sourceId);
            m_sourceToTarget.remove(p_sourceId);

            if (targetIds != null)
            {
                Iterator itTargetKey = targetIds.iterator();
                while (itTargetKey.hasNext())
                {
                    Long targetId = (Long) itTargetKey.next();
                    Set sourceIds = (Set) m_targetToSource.get(targetId);
                    Iterator itSourceKey = sourceIds.iterator();
                    while (itSourceKey.hasNext())
                    {
                        Long sourceId = (Long) itSourceKey.next();
                        if (!sourceId.equals(p_sourceId))
                        {
                            followIdChains(sourceId, p_idListPair);
                            p_idListPair.addSourceId(sourceId);
                        }
                    }

                    p_idListPair.addTargetId(targetId);
                }
            }

        }

        public class IdListPair
        {
            Set m_sourceList = new HashSet();
            Set m_targetList = new HashSet();

            public void addSourceId(Long p_id)
            {
                m_sourceList.add(p_id);
            }

            public void addTargetId(Long p_id)
            {
                m_targetList.add(p_id);
            }

            public List getSourceList()
            {
                List list = new ArrayList(m_sourceList);
                SortUtil.sort(list);
                return list;
            }

            public List getTargetList()
            {
                List list = new ArrayList(m_targetList);
                SortUtil.sort(list);
                return list;
            }

            public String toString()
            {
                return "soruce list: " + m_sourceList + " target list: "
                        + m_targetList;
            }

        }

    }

}
