package com.globalsight.dispatcher.dao;

public class MockMTPLanguagesDAO extends MTPLanguagesDAO
{
    public MockMTPLanguagesDAO(String p_folderPath)
    {
        if (!p_folderPath.endsWith("/") && !p_folderPath.endsWith("\\"))
        {
            p_folderPath += "/";
        }
        this.filePath = p_folderPath + fileName;
    }
}
