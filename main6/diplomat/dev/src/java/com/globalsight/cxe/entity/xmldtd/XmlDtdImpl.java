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
package com.globalsight.cxe.entity.xmldtd;

import java.util.HashSet;
import java.util.Set;

import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.DtdFileManager;

/** Implements an XmlDtdFile */
public class XmlDtdImpl extends PersistentObject implements XmlDtd
{
    private static final long serialVersionUID = -873521866679410418L;
    private long companyId;
    private String description;
    private String dtdText;
    private boolean addComment;
    private boolean sendEmail;

    private Set<FileProfileImpl> fileProfiles = new HashSet<FileProfileImpl>();

    public XmlDtdImpl()
    {
        description = null;
        dtdText = null;
    }

    /** Constructs an XmlDtdFileImpl with id, name, description, and dtdText* */
    public XmlDtdImpl(String p_name, String p_description, String p_dtdText)
    {
        this.setName(p_name);
        description = p_description;
        dtdText = p_dtdText;
    }

    /**
     * Get name of the company this activity belong to.
     * 
     * @return The company name.
     */
    public long getCompanyId()
    {
        return this.companyId;
    }

    /**
     * Get name of the company this activity belong to.
     * 
     * @return The company name.
     */
    public void setCompanyId(long p_companyId)
    {
        this.companyId = p_companyId;
    }

    /**
     * * Return the description of the XML Dtd File *
     * 
     * @return XML Dtd File description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * * Return the body of the XML Dtd File *
     * 
     * @return XML Dtd File
     */
    public String getDtdText()
    {
        return dtdText;
    }

    /**
     * * Sets the description of the XML Dtd File
     */
    public void setDescription(String p_description)
    {
        description = p_description;
    }

    /**
     * * Sets the body of the XML Dtd File
     */
    public void setDtdText(String p_dtdText)
    {
        dtdText = p_dtdText;
    }

    public int getFileNumber()
    {
        return DtdFileManager.getAllFiles(getId()).size();
    }

    public Set<FileProfileImpl> getFileProfiles()
    {
        return fileProfiles;
    }

    public void setFileProfiles(Set<FileProfileImpl> fileProfiles)
    {
        this.fileProfiles = fileProfiles;
    }

    public boolean isAddComment()
    {
        return addComment;
    }

    public void setAddComment(boolean addComment)
    {
        this.addComment = addComment;
    }

    public boolean isSendEmail()
    {
        return sendEmail;
    }

    public void setSendEmail(boolean sendEmail)
    {
        this.sendEmail = sendEmail;
    }

    @Override
    public boolean referenced()
    {
        return fileProfiles.size() > 0;
    }
}
