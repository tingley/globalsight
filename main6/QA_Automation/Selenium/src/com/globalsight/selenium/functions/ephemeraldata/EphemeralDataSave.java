package com.globalsight.selenium.functions.ephemeraldata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.globalsight.selenium.testcases.ConfigUtil;

/**
 * Save Ephemeral Data
 * 
 * New a file and add save and read method
 * 
 * @author leon
 * 
 */
public class EphemeralDataSave
{
    /**
     * This method is used for saving job names during running the test cases
     * 
     * The file path is \\SELENIUM Server IP\GlobalSight Automation
     * Files\TestFiles\EphemeralData\jobNames.txt
     * 
     * Read method is in EphemeralDataRead.java
     * 
     * @param jobNames
     */
    public void saveJobNamesAsEphemeralData(String jobNames)
    {
        String jobNameFilePath = ConfigUtil.getConfigData("Base_Path")+"EphemeralData\\jobNames.txt";
        File file = new File(jobNameFilePath);
        FileWriter writer;
        try
        {
            writer = new FileWriter(file);
            writer.write(jobNames);
            writer.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
