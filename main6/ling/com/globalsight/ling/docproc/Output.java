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
package com.globalsight.ling.docproc;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.globalsight.ling.common.XmlEntities;

/**
 * Object for output results from extractor.
 */
public class Output
{
    // the document elements
    private Vector m_documentElements = new Vector();
    private DiplomatAttribute m_diplomatAttributes = null;
    private XmlEntities m_encoder = null;

    // List of non-fatal errors
    private List m_errors = null;

    private String m_currentDataFormat = null;
    private boolean m_currentAttributesSet = false;

    /**
     * Output constructor comment.
     */
    public Output()
    {
        super();

        m_documentElements = new Vector();
        m_errors = new Vector();
        m_encoder = new XmlEntities();
        m_diplomatAttributes = new DiplomatAttribute();
    }

    public void addDocumentElement(DocumentElement documentElement,
            boolean append)
    {
        if (!append)
        {
            m_documentElements.add(documentElement);
        }
        else
        {
            boolean isBothTranlatable = (documentElement.type() == DocumentElement.TRANSLATABLE && isSameElementType(DocumentElement.TRANSLATABLE));
            boolean appendAble = false;

            if (isBothTranlatable)
            {
                TranslatableElement elem = (TranslatableElement) m_documentElements
                        .lastElement();
                TranslatableElement addedElem = (TranslatableElement) documentElement;

                boolean isSameDataType = (elem.getDataType() == addedElem
                        .getDataType() || (elem.getDataType() != null && elem
                        .getDataType()
                        .equalsIgnoreCase(addedElem.getDataType())));
                boolean isSameType = (elem.getType() == addedElem.getType() || (elem
                        .getType() != null && elem.getType().equalsIgnoreCase(
                        addedElem.getType())));
                boolean isEmptySid = (elem.getSid() == null && addedElem
                        .getSid() == null);

                appendAble = (isSameDataType && isSameType && isEmptySid);

                if (appendAble)
                {
                    elem.appendChunk(addedElem.getChunk());
                }
            }

            if (!appendAble)
            {
                m_documentElements.add(documentElement);
            }
        }

        m_currentAttributesSet = false;
    }

    public Vector getDocumentElements()
    {
        return m_documentElements;
    }

    public void setDocumentElements(Vector documentElements)
    {
        this.m_documentElements = documentElements;
    }

    public void addDocumentElement(DocumentElement documentElement)
    {
        m_documentElements.add(documentElement);
        m_currentAttributesSet = false;
    }

    public void addExtractorMessage(ExtractorMessage p_message)
    {
        m_errors.add(p_message);
    }

    public void addEmptyGsa(String extract, String desc, String locale,
            String add, boolean delete, String added, String deleted,
            String snippetName, String snippetId)
            throws DocumentElementException
    {
        GsaStartElement elem = new GsaStartElement(true);

        elem.setExtract(extract);
        elem.setDescription(desc);
        elem.setLocale(locale);
        elem.setAdd(add);
        elem.setAdded(added);
        elem.setDeleted(deleted);
        elem.setSnippetName(snippetName);
        elem.setSnippetId(snippetId);

        if (delete)
        {
            elem.setDeletable();
        }

        if (!elem.validate())
        {
            throw new DocumentElementException("Invalid GS tag "
                    + elem.toString());
        }

        m_documentElements.add(elem);
        m_currentAttributesSet = true;
    }

    public void addGsaStart(String extract, String desc, String locale,
            String add, boolean delete, String added, String deleted,
            String snippetName, String snippetId)
            throws DocumentElementException
    {
        GsaStartElement elem = new GsaStartElement(false);

        elem.setExtract(extract);
        elem.setDescription(desc);
        elem.setLocale(locale);
        elem.setAdd(add);
        elem.setAdded(added);
        elem.setDeleted(deleted);
        elem.setSnippetName(snippetName);
        elem.setSnippetId(snippetId);

        if (delete)
        {
            elem.setDeletable();
        }

        if (!elem.validate())
        {
            throw new DocumentElementException("Invalid GS tag "
                    + elem.toString());
        }

        m_documentElements.add(elem);
        m_currentAttributesSet = true;
    }

    public void addGsaEnd()
    {
        // We want GSA tags to be empty. SAX doesn't have an event for
        // empty elements so we'll change a start element to an empty
        // element if there's no other content in between.

        if (isSameElementType(DocumentElement.GSA_START))
        {
            GsaStartElement elem = (GsaStartElement) m_documentElements
                    .lastElement();

            elem.setEmpty();
        }
        else
        {
            m_documentElements.add(new GsaEndElement());
        }

        m_currentAttributesSet = false;
    }

    private void addLoc(String p_strChunk, boolean p_bTmx)
    {
        String chunk;

        if (p_bTmx)
        {
            chunk = p_strChunk;
        }
        else
        {
            chunk = m_encoder.encodeStringBasic(p_strChunk);
        }

        // already existing node, add to it.
        if (isSameElementType(DocumentElement.LOCALIZABLE))
        {
            LocalizableElement elem = (LocalizableElement) m_documentElements
                    .lastElement();

            elem.appendChunk(chunk);
        }
        else
        // create the node than add to it.
        {
            LocalizableElement elem = new LocalizableElement();
            elem.setChunk(chunk);
            m_documentElements.add(elem);
            m_currentAttributesSet = false;
        }
    }

    public void addLocalizable(String p_strChunk)
    {
        addLoc(p_strChunk, false);
    }

    public void addLocalizableTmx(String p_Tmx)
    {
        addLoc(p_Tmx, true);
    }

    public void addSegment(SegmentNode p_segment)
            throws DocumentElementException
    {
        // if not a translatable node than throw an exception
        if (!isSameElementType(DocumentElement.TRANSLATABLE))
        {
            throw new DocumentElementException(
                    "cannot add segment to a non-translatable tag");
        }

        TranslatableElement elem = (TranslatableElement) m_documentElements
                .lastElement();
        elem.addSegment(p_segment);
    }

    private void addSkel(String p_skel, boolean p_tmx)
    {
        String chunk;

        if (p_tmx)
        {
            chunk = p_skel;
        }
        else
        {
            chunk = m_encoder.encodeStringBasic(p_skel);
        }

        if (isSameElementType(DocumentElement.SKELETON))
        {
            // already existing node, add to it.

            SkeletonElement elem = (SkeletonElement) m_documentElements
                    .lastElement();
            elem.appendSkeleton(chunk);
        }
        else
        {
            // create the node than add to it.

            SkeletonElement elem = new SkeletonElement();
            elem.setSkeleton(chunk);
            m_documentElements.add(elem);
            m_currentAttributesSet = false;
        }
    }

    public void addSkeleton(String p_strSkeleton)
    {
        addSkel(p_strSkeleton, false);
    }

    public void addSkeletonTmx(String p_strSkeleton)
    {
        addSkel(p_strSkeleton, true);
    }

    public void addToTotalWordCount(int p_iWordCount)
    {
        m_diplomatAttributes.addWordCount(p_iWordCount);
    }

    private void addTrans(String p_strChunk, boolean p_bTmx, String sid,
            Map xliffTransPart, boolean isPreserveWS, String dataType)
    {
        String chunk;

        if (p_bTmx)
        {
            chunk = p_strChunk;
        }
        else
        {
            chunk = m_encoder.encodeStringBasic(p_strChunk);
        }

        // already existing node, add to it.
        boolean appended = false;
        if (isSameElementType(DocumentElement.TRANSLATABLE))
        {
            TranslatableElement elem = (TranslatableElement) m_documentElements
                    .lastElement();

            if (dataType == null
                    || elem.getDataType() == null
                    || (dataType != null && dataType.equals(elem.getDataType())))
            {
                elem.appendChunk(chunk);

                if (isPreserveWS)
                {
                    elem.setPreserveWhiteSpace(isPreserveWS);
                }
                if (sid != null)
                {
                    elem.setSid(sid);
                }

                appended = true;
            }
        }

        // create the node then add to it.
        if (!appended)
        {
            TranslatableElement elem = new TranslatableElement();
            elem.setChunk(chunk);
            elem.setSid(sid);
            elem.setPreserveWhiteSpace(isPreserveWS);
            elem.setDataType(dataType);

            if (xliffTransPart != null)
            {
                elem.setXliffPart(xliffTransPart);
            }

            m_documentElements.add(elem);
            m_currentAttributesSet = false;
        }
    }

    private void addTrans(String p_strChunk, boolean p_bTmx, String sid)
    {
        addTrans(p_strChunk, p_bTmx, sid, null, false, null);
    }

    private void addTrans(String p_strChunk, boolean p_bTmx)
    {
        addTrans(p_strChunk, p_bTmx, null);
    }

    /**
     * Add a transaltion node to the Output object Creation date: (7/28/2000
     * 11:05:18 AM)
     * 
     * @param p_strChunk
     *            java.lang.String
     */
    public void addTranslatable(String p_strChunk)
    {
        addTrans(p_strChunk, false);
    }
    
    public void addTranslatable(String p_strChunk, String sid)
    {
        addTrans(p_strChunk, false, sid);
    }

    public void addTranslatableTmx(String p_Tmx)
    {
        addTrans(p_Tmx, true);
    }

    public void addTranslatableTmx(String p_Tmx, String sid)
    {
        addTrans(p_Tmx, true, sid, null, false, null);
    }

    public void addTranslatableTmx(String p_Tmx, String sid,
            boolean isPreserveWS, String dataType)
    {
        addTrans(p_Tmx, true, sid, null, isPreserveWS, dataType);
    }

    public void addTranslatableTmx(String p_Tmx, String sid,
            boolean isPreserveWS)
    {
        addTrans(p_Tmx, true, sid, null, isPreserveWS, null);
    }

    public void addTranslatableTmx(String p_Tmx, String sid, Map XliffAttributes)
    {
        addTrans(p_Tmx, true, sid, XliffAttributes, false, null);
    }

    public Iterator documentElementIterator()
    {
        return m_documentElements.iterator();
    }

    public Iterator errorIterator()
    {
        return m_errors.iterator();
    }

    public String getDataFormat()
    {
        return m_diplomatAttributes.getDataType();
    }

    public String getLocale()
    {
        return m_diplomatAttributes.getLocale();
    }

    public String getTargetEncoding()
    {
        return m_diplomatAttributes.getTargetEncoding();
    }

    public int getWordCount()
    {
        return m_diplomatAttributes.getWordCount();
    }

    private boolean isSameElementType(int p_iDocumentType)
    {
        if (m_documentElements.isEmpty())
        {
            return false;
        }

        return (((DocumentElement) m_documentElements.lastElement()).type() == p_iDocumentType);
    }

    public void setDataFormat(String p_DataFormat)
    {
        m_diplomatAttributes.setDataType(p_DataFormat);
    }

    /*
     * private void setGsaAttrs(String extract, String add, boolean delete,
     * String added, String deleted, String snippetName, String snippetId)
     * throws DocumentElementException { // if not a localizable node than throw
     * an exception if (!isSameElementType(DocumentElement.GSA_START)) { throw
     * new DocumentElementException (
     * "cannot add GSA attributes to non-GSA tag"); }
     * 
     * GsaStartElement elem = (GsaStartElement)m_documentElements.lastElement();
     * 
     * elem.setExtract(extract); elem.setAdd(add); elem.setAdded(added);
     * elem.setDeleted(deleted); elem.setSnippetName(snippetName);
     * elem.setSnippetId(snippetId);
     * 
     * if (delete) { elem.setDeletable(); }
     * 
     * m_currentAttributesSet = true; }
     */

    public void setLocale(String p_Locale)
    {
        m_diplomatAttributes.setLocale(p_Locale);
    }

    public void setLocalizableAttrs(String p_strDataType, String p_strType)
            throws DocumentElementException
    {
        // if not a localizable node, throw an exception
        if (!isSameElementType(DocumentElement.LOCALIZABLE))
        {
            throw new DocumentElementException(
                    "cannot set localizable attributes on non-localizable tag");
        }

        LocalizableElement elem = (LocalizableElement) m_documentElements
                .lastElement();

        elem.setDataType(p_strDataType);
        elem.setType(p_strType);
        m_currentAttributesSet = true;
    }

    public void setTargetEncoding(String p_strTargetEncoding)
    {
        m_diplomatAttributes.setTargetEncoding(p_strTargetEncoding);
    }

    /**
     * Caution: m_diplomatAttributes.WordCount may not be zero!
     * 
     * @author Jim Hargrave
     * @param p_iTotalWordCount
     *            int
     */
    public void setTotalWordCount(int p_iTotalWordCount)
    {
        m_diplomatAttributes.addWordCount(p_iTotalWordCount);
    }

    public void setTranslatableAttrs(String p_strDataType, String p_strType,
            String isLocalized) throws DocumentElementException
    {
        // if not a translatable node, throw an exception
        if (!isSameElementType(DocumentElement.TRANSLATABLE))
        {
            throw new DocumentElementException(
                    "cannot set translatable attributes on non-translatable tag");
        }

        TranslatableElement elem = (TranslatableElement) m_documentElements
                .lastElement();

        elem.setDataType(p_strDataType);
        elem.setType(p_strType);

        if (isLocalized != null)
        {
            elem.setIsLocalized(isLocalized);
        }

        m_currentAttributesSet = true;
    }

    public void setTranslatableAttrs(String p_strDataType, String p_strType)
            throws DocumentElementException
    {
        setTranslatableAttrs(p_strDataType, p_strType, null);
    }

    public DiplomatAttribute getDiplomatAttribute()
    {
        return m_diplomatAttributes;
    }

    public DocumentElement getCurrentElement()
    {
        if (m_documentElements.isEmpty())
        {
            return null;
        }

        return (DocumentElement) m_documentElements.lastElement();
    }

    public String getCurrentDataFormat()
    {
        return m_currentDataFormat;
    }

    public void setCurrentDataFormat(String p_DataFormat)
    {
        m_currentDataFormat = p_DataFormat;
    }

    // /**
    // * Test to see if we have set the current nodes attributes
    // *
    // * @return boolean
    // */
    // public boolean isSetCurrentAttributes()
    // {
    // return m_currentAttributesSet;
    // }

    public int getSize()
    {
        return m_documentElements.size();
    }

    /**
     * Clear DocumentElements, seldom use.
     */
    public void clearDocumentElements()
    {
        // can't be like this: ... = null;
        this.m_documentElements = new Vector();
    }

    public String getTargetLanguage()
    {
        return m_diplomatAttributes.getTargetLanguage();
    }

    public void setTargetLanguage(String targetLanguage)
    {
        if (targetLanguage != null)
            m_diplomatAttributes.setTargetLanguage(targetLanguage);
    }

}
