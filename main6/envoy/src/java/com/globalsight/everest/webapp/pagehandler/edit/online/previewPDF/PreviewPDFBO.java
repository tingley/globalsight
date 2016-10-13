/**
 *  Copyright 2013 Welocalize, Inc. 
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
package com.globalsight.everest.webapp.pagehandler.edit.online.previewPDF;

/**
 * The Business Object for Preview PDF.
 * 
 * @field versionType
 *            File version type, such as ADOBE_CS2/ADOBE_CS3/ADOBE_CS4/ADOBE_CS5/ADOBE_CS5_5/ADOBE_FM9/ADOBE_TYPE_IDML
 * @field fileType
 *            File type, such as ADOBE_INDD/ADOBE_INX/ADOBE_FM/ADOBE_IDML
 * @field fileSuffix
 *            File Suffix
 * @field isTranslateMaster
 *            Tags for whether translate master
 * @field isTranslateHiddenLayer
 *            Tags for whether translate hidden layer 
 */
public class PreviewPDFBO implements PreviewPDFConstants
{
    private int versionType = ADOBE_CS2;
    private String fileType = ADOBE_INDD;
    private String fileSuffix = INDD_SUFFIX;
    private boolean isTranslateMaster = true;
    private boolean isTranslateHiddenLayer = false;
    
    private String m_relSafeName = null;
    private String m_safeBaseFileName = null;
    
    public PreviewPDFBO() {}
    
    public PreviewPDFBO(int versionType, String fileType, String fileSuffix, 
            boolean isTranslateMaster, boolean isTranslateHiddenLayer)
    {
        this.versionType = versionType;
        this.fileType = fileType;
        this.fileSuffix = fileSuffix;
        this.isTranslateMaster = isTranslateMaster;
        this.isTranslateHiddenLayer = isTranslateHiddenLayer;
    }
    
    public PreviewPDFBO(int versionType, String fileType, String fileSuffix, 
            boolean isTranslateMaster, boolean isTranslateHiddenLayer, 
            String m_relSafeName, String m_safeBaseFileName)
    {
        this.versionType = versionType;
        this.fileType = fileType;
        this.fileSuffix = fileSuffix;
        this.isTranslateMaster = isTranslateMaster;
        this.isTranslateHiddenLayer = isTranslateHiddenLayer;
        this.m_relSafeName = m_relSafeName;
        this.m_safeBaseFileName = m_safeBaseFileName;
    }

    
    public int getVersionType()
    {
        return versionType;
    }

    public void setVersionType(int versionType)
    {
        this.versionType = versionType;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

    public String getFileSuffix()
    {
        return fileSuffix;
    }

    public void setFileSuffix(String fileSuffix)
    {
        this.fileSuffix = fileSuffix;
    }

    public boolean isTranslateMaster()
    {
        return isTranslateMaster;
    }

    public void setTranslateMaster(boolean isTranslateMaster)
    {
        this.isTranslateMaster = isTranslateMaster;
    }

    public boolean isTranslateHiddenLayer()
    {
        return isTranslateHiddenLayer;
    }

    public void setTranslateHiddenLayer(boolean isTranslateHiddenLayer)
    {
        this.isTranslateHiddenLayer = isTranslateHiddenLayer;
    }

    public String getRelSafeName()
    {
        return m_relSafeName;
    }

    public void setRelSafeName(String m_relSafeName)
    {
        this.m_relSafeName = m_relSafeName;
    }

    public String getSafeBaseFileName()
    {
        return m_safeBaseFileName;
    }

    public void setSafeBaseFileName(String m_safeBaseFileName)
    {
        this.m_safeBaseFileName = m_safeBaseFileName;
    }

}
