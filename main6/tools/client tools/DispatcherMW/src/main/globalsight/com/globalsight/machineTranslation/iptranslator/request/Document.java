package com.globalsight.machineTranslation.iptranslator.request;

public class Document
{
    private String fileName;
    private String contentType;
    private byte[] document;
    private Long wordCount;

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getContentType()
    {
        return contentType;
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    public byte[] getDocument()
    {
        return document;
    }

    public void setDocument(byte[] document)
    {
        this.document = document;
    }

    @Override
    public String toString()
    {
        return "[fileName=" + fileName + " , contentType=" + contentType
                + " ,wc=" + wordCount + "]";
    }

    public Long getWordCount()
    {
        return wordCount;
    }

    public void setWordCount(Long wordCount)
    {
        this.wordCount = wordCount;
    }
}
