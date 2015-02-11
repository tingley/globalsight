package com.globalsight.dispatcher.dao;

public class MockMTProfilesDAO extends MTProfilesDAO
{
    public MockMTProfilesDAO(String p_folderPath)
    {
        if (!p_folderPath.endsWith("/") && !p_folderPath.endsWith("\\"))
        {
            p_folderPath += "/";
        }
        this.filePath = p_folderPath + fileName;
    }
}
