package com.globalsight.restful.version1_0.fileProfile;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "fileProfileInfo")
public class GetFileProfileResponse implements Serializable
{
    private static final long serialVersionUID = 1L;

    long id;
    String name;
    long l10nprofileId;
    long sourceFileFormat;
    String description;
    List<String> fileExtension;
    LocaleInfo localeInfo;

    @XmlElement(name = "id")
    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    @XmlElement(name = "name")
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @XmlElement(name = "description")
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @XmlElement(name = "l10nprofileId")
    public long getL10nprofileId()
    {
        return l10nprofileId;
    }

    public void setL10nprofileId(long l10nprofileId)
    {
        this.l10nprofileId = l10nprofileId;
    }

    @XmlElement(name = "sourceFileFormat")
    public long getSourceFileFormat()
    {
        return sourceFileFormat;
    }

    public void setSourceFileFormat(long sourceFileFormat)
    {
        this.sourceFileFormat = sourceFileFormat;
    }

    @XmlList
    public List<String> getFileExtension()
    {
        return fileExtension;
    }

    public void setFileExtension(List<String> fileExtension)
    {
        this.fileExtension = fileExtension;
    }

    @XmlElement(name = "localeInfo")
    public LocaleInfo getLocaleInfo()
    {
        return localeInfo;
    }

    public void setLocaleInfo(LocaleInfo localeInfo)
    {
        this.localeInfo = localeInfo;
    }

    public void addLocaleInfo(String sourceLocale, List<String> targetLocales)
    {
        LocaleInfo locale = new LocaleInfo();
        locale.setSourceLocale(sourceLocale);
        locale.setTargetLocale(targetLocales);
        localeInfo = locale;
    }

    private class LocaleInfo implements Serializable
    {
        private static final long serialVersionUID = 1L;
        String sourceLocale;
        List<String> targetLocales;

        @XmlElement(name = "sourceLocale")
        public String getSourceLocale()
        {
            return sourceLocale;
        }

        public void setSourceLocale(String sourceLocale)
        {
            this.sourceLocale = sourceLocale;
        }

        @XmlList
        public List<String> getTargetLocales()
        {
            return targetLocales;
        }

        public void setTargetLocale(List<String> targetLocales)
        {
            this.targetLocales = targetLocales;
        }
    }

}
