package com.globalsight.cxe.entity.fileprofile;

import com.globalsight.cxe.entity.xmldtd.XmlDtdImpl;

public class FileprofileVo  
{
    private FileProfile fileProfile;
    private String formatName;
    private String locName;
    private String companyName;

    public FileprofileVo(FileProfile fileProfile, String formatName, String locName,
            String companyNmae)
    {
        super();
        this.fileProfile = fileProfile;
        this.formatName = formatName;
        this.locName = locName;
        this.companyName = companyNmae;
    }

    public FileProfile getFileProfile()
    {
        return fileProfile;
    }

    public void setFileProfile(FileProfile fileProfile)
    {
        this.fileProfile = fileProfile;
    }

    public String getFormatName()
    {
        return formatName;
    }

    public void setFormatName(String formatName)
    {
        this.formatName = formatName;
    }

    public String getLocName()
    {
        return locName;
    }

    public void setLocName(String locName)
    {
        this.locName = locName;
    }


    public String getCompanyName()
    {
        return companyName;
    }

    public void setCompanyName(String companyName)
    {
        this.companyName = companyName;
    }

    public XmlDtdImpl getXmlDtd()
    {
        return fileProfile.getXmlDtd();
    }

    public String getCodeSet()
    {
        return fileProfile.getCodeSet();
    }

    public String getFilterName()
    {
        return fileProfile.getFilterName();
    }

    public String getDescription()
    {
        return fileProfile.getDescription();
    }

    public long getId()
    {
        return fileProfile.getId();
    }

    public String getName()
    {
        return fileProfile.getName();
    }

    public Long getL10nprofileId()
    {
        return fileProfile.getL10nProfileId();
    }

    public Long getSpecialFilterId()
    {
        return fileProfile.getSpecialFilterId();
    }

    public String getFilterTabName()
    {
        return fileProfile.getFilterTableName();
    }
}
