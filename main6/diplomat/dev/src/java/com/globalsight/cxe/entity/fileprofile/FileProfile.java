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
package com.globalsight.cxe.entity.fileprofile;

import java.util.Vector;

import com.globalsight.cxe.entity.filterconfiguration.QAFilter;
import com.globalsight.cxe.entity.xmldtd.XmlDtdImpl;

/**
 * Represents a CXE File Profile entity object.
 */
public interface FileProfile
{
    /**
     * Determines whether the secondary target files should be exported by
     * default. This is used during an automatic export in order to export
     * target pages or secondary target files.
     * 
     * @return True if the secondary target files should be exported. Otherwise,
     *         returns false.
     */
    boolean byDefaultExportStf();

    /**
     * * Return the id of the FileProfile (cannot be set) *
     * 
     * @return id as a long
     */
    public long getId();

    /**
     * Return the list of file extension Ids for this file profile
     * 
     * @return Vector of file extension Ids as BigDecimal
     */
    public Vector<Long> getFileExtensionIds();

    /**
     * Return the id of the known format type (HTML,XML,CSS,etc.)
     * 
     * @return known format type id
     */
    public long getKnownFormatTypeId();

    /**
     * Return the id of the L10nProfile that is associated with this File
     * Profile
     * 
     * @return L10NProfileId
     */
    public long getL10nProfileId();

    /**
     * Return the name of the file profile
     * 
     * @return file profile name
     */
    public String getName();

    /**
     * Return the description of the file profile
     * 
     * @return file profile description
     */
    public String getDescription();

    /**
     * Return the company id of the file profile
     * 
     * @return file profile company id
     */
    public long getCompanyId();

    /**
     * Return the character set of the file profile
     * 
     * @return file profile codeSet
     */
    public String getCodeSet();

    /**
     * Return the id of the XML Rule File
     * 
     * @return XML Rule File id
     */
    public long getXmlRuleId();

    /**
     * Return the id of the xml DTD
     * 
     * @return
     */
    public long getXmlDtdId();

    /**
     * Return the script on export
     * 
     * @return
     */
    public String getScriptOnExport();

    /**
     * Return the script on import
     * 
     * @return
     */
    public String getScriptOnImport();

    /**
     * Set the script on export
     * 
     * @param p_scriptOnExport
     */
    public void setScriptOnExport(String p_scriptOnExport);

    /**
     * Set the script on import
     * 
     * @param p_scriptOnImport
     */
    public void setScriptOnImport(String p_scriptOnImport);

    /**
     * Sets the flag that determines whether the secondary target files should
     * be exported by default.
     * 
     * @param p_byDefaultExportStf
     *            - The boolean value to be set which will be used during an
     *            auto export.
     */
    void byDefaultExportStf(boolean p_byDefaultExportStf);

    /**
     * Sets the File Profile's associated list of Extension Ids The Vector must
     * contain BigDecimal
     */
    public void setFileExtensionIds(Vector<Long> p_extensionIds);

    /**
     * Set the id of the known format type (HTML,XML,CSS,etc.)
     */
    public void setKnownFormatTypeId(long p_knownFormatType);

    /**
     * Set the id of the localization profile
     */
    public void setL10nProfileId(long p_l10nProfileId);

    /**
     * Set the name of the file profile
     */
    public void setName(String p_name);

    /**
     * Set the description of the file profile
     * 
     * @param p_description
     *            The description of the file profile
     */
    public void setDescription(String p_description);

    /**
     * Set the company id of the file profile
     * 
     * @param p_companyId
     *            The company id of the file profile
     */
    public void setCompanyId(long p_companyId);

    /**
     * Set the character set of the file profile
     * 
     * @param p_codeSet
     *            The character set of the file profile
     */
    public void setCodeSet(String p_codeSet);

    public boolean supportsSid();

    public boolean supportsUnicodeEscape();

    public boolean getEntityEscape();

    boolean translateHeader();

    public String getJavascriptFilterRegex();

    public XmlDtdImpl getXmlDtd();

    public void setXmlDtd(XmlDtdImpl xmlDtd);

    public long getFilterId();

    public void setFilterId(long filterId);

    public void setFilterTableName(String filterTableName);

    public String getFilterName();

    public String getFilterTableName();

    public long getQaFilterId();

    public QAFilter getQaFilter();

    public void setQaFilter(QAFilter qaFilter);

    public boolean getPreserveSpaces();

    public int getTerminologyApproval();

    public void setTerminologyApproval(int flag);

    public int getXlfSourceAsUnTranslatedTarget();

    public void setXlfSourceAsUnTranslatedTarget(int flag);

    public long getReferenceFP();

    public void setReferenceFP(long p_referenceFP);

    public int getBOMType();

    public void setBOMType(int p_BOMType);
}
