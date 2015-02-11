package com.globalsight.selenium.functions.ephemeraldata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.globalsight.selenium.properties.ConfigUtil;

/**
 * Read Ephemeral Data
 * 
 * New a file and add save and read method
 * 
 * @author leon
 * 
 */
public class EphemeralDataRead
{
    /**
     * This method is used for read job name from ephemeral data during running
     * test cases
     * 
     * job name format: jobName_random digits. We do not know the random digit,
     * we only know the job name
     * 
     * The file path is \\SELENIUM Server IP\GlobalSight Automation
     * Files\TestFiles\EphemeralData\jobNames.txt
     * 
     * Write method is in EphemeralDataSave.java
     * 
     * @param jobNames
     */
    public String readJobNamesAsEphemeralData(String jobNameWithoutRandomDigits)
    {
        String jobNameWithRandomDigits = null;
        String jobNameFilePath = ConfigUtil.getConfigData("Base_Path")
                + "EphemeralData\\jobNames.txt";
        File file = new File(jobNameFilePath);
        
        FileReader reader;
        try
        {
            reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            String jobNames = br.readLine();
            String[] jobName = jobNames.split(",");
            for (int i = 0; i < jobName.length; i++)
            {
                if (!(jobName[i].indexOf(jobNameWithoutRandomDigits) < 0))
                {
                    jobNameWithRandomDigits = jobName[i];
                    break;
                }
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jobNameWithRandomDigits;
    }
}
