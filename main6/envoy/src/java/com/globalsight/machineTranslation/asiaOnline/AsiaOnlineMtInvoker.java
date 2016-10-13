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
package com.globalsight.machineTranslation.asiaOnline;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.log4j.Logger;

import AOAPI.AOConfig;
import AOAPI.DataLookup;
import AOAPI.JobAction;
import AOAPI.JobData;
import AOAPI.Translate;
import AOAPI.Utilities;

public class AsiaOnlineMtInvoker
{
    // Constants for language pair information in sequence
    public static final String LP_CODE = "Code";
    public static final String LP_SOURCE_LANGUAGE_CODE = "SourceLanguageCode";
    public static final String LP_TARGET_LANGUAGE_CODE = "TargetLanguageCode";
    public static final String LP_INPUT_ABBREVIATION = "InputAbbreviation";
    public static final String LP_OUTPUT_ABBREVIATION = "OutputAbbreviation";
    public static final String LP_LANGUAGE_PAIR_NAME = "LanguagePairName";
    public static final String LP_LANGUAGE_PAIR_CODE_NAME = "LanguagePairCodeName";
    public static final String LP_INPUT_LANGUAGE = "InputLanguage";
    public static final String LP_OUTPUT_LANGUAGE = "OutputLanguage";
    public static final String LP_LANGUAGE_PAIR = "LanguagePair";
    public static final String LP_DISABLED = "Disabled";
    public static final String LP_ROW_NUMBER = "row_number";
    public static final String LP_DUMMY = "dummy";
    
    // Constants for domain combinations information in sequence.
    public static final String DC_CODE = "Code";
    public static final String DC_LANGUAGE_PAIR_CODE = "LanguagePairCode";
    public static final String DC_SOURCE_LANGUAGE_CODE = "SourceLanguageCode";
    public static final String DC_SOURCE_LANGUAGE = "SourceLanguage";
    public static final String DC_SOURCE_ABBREVIATION = "SourceAbbreviation";
    public static final String DC_TARGET_LANGUAGE_CODE = "TargetLanguageCode";
    public static final String DC_TARGET_LANGUAGE = "TargetLanguage";
    public static final String DC_TARGET_ABBREVIATION = "TargetAbbreviation";
    public static final String DC_LANGUAGE_PAIR = "LanguagePair";
    public static final String DC_DOMAIN_COUNT = "DomainCount";
    public static final String DC_DOMAIN_COMBINATION_NAME = "DomainCombinationName";
    public static final String DC_DOMAIN_COMBINATION_DESC = "DomainCombinationDesc";
    public static final String DC_DOMAIN_COMBINATION = "DomainCombination";
    public static final String DC_DOMAIN_COM_NAME = "DomainComName";
    public static final String DC_PRIVATE = "Private";
    public static final String DC_DISABLED = "Disabled";
    public static final String DC_DELETED = "Deleted";
    public static final String DC_CAPITALIZE = "Capitalize";
    public static final String DC_DEFAULT = "Default";
    public static final String DC_TRUE_CASE = "TrueCase";
    
    private AOConfig oAOConfig = null;
    private Translate oAOTranslation = null;
    private String srcData = null;
    private String sError = null;
    private boolean bExtractOutput = false;
    
    private static DataLookup oAODataLookup = null;
    
    private static final Logger s_logger = Logger
            .getLogger(AsiaOnlineMtInvoker.class);

    /**
     * Constructor.
     * 
     * @param p_aoConfig
     *            - AOConfig object.
     */
    public AsiaOnlineMtInvoker(AOConfig p_aoConfig)
    {
        oAOConfig = p_aoConfig;
    }
    
    /**
     * Constructor.
     * 
     * @param p_aoMtUrl
     *            - the URL of Asia Online(AO) machine translation engine.
     * @param p_aoMtPort
     *            - the port of API URL is using, such as 80.
     * @param p_aoMtUserName
     *            - the user name to logon to AO machine translation server.
     * @param p_aoMtPassword
     *            - the password to logon to AO machine translation server.
     * @param p_aoMtAccountNumber
     *            - every customer in AO has his own account name and account
     *            number. The account number will be used during integration
     *            with AO MT engine.
     */
    public AsiaOnlineMtInvoker(String p_aoMtUrl, int p_aoMtPort,
            String p_aoMtUserName, String p_aoMtPassword,
            int p_aoMtAccountNumber)
    {
        oAOConfig = new AOConfig();
        
        oAOConfig.setAPIURL(p_aoMtUrl);
        oAOConfig.setAPIPort(p_aoMtPort);
        oAOConfig.setUsername(p_aoMtUserName);
        oAOConfig.setPassword(p_aoMtPassword);
        oAOConfig.setAccountNo(p_aoMtAccountNumber);
    }

    public Translate getAOTranslation()
    {
        return oAOTranslation;
    }

    public void setAOTranslation(Translate oAOTranslation)
    {
        this.oAOTranslation = oAOTranslation;
    }

    /**
     * Get all language pairs Asia Online supports.
     * 
     * @return HashMap : All information for one language pair is stored in a
     *         map,and all the maps are stored in a wrapper map with "Code" as
     *         key.
     */
    public Map getAllSupportedLanguagePairs() throws Exception
    {
        Map lpMap = new TreeMap();

        oAODataLookup = new DataLookup();
        oAODataLookup.setConfig(oAOConfig);

        // Get all language pairs
        ArrayList<String> oLanguagePairs = oAODataLookup.GetLanguagePairs();
        if (oLanguagePairs != null && oLanguagePairs.size() > 0)
        {
            for (int i = 0; i < oLanguagePairs.size(); i++)
            {
                Object oData = oLanguagePairs.get(i);
                String[][] asData = (String[][]) oData;
                // Put all key-values to a map.
                Map dataOfOneLP = new HashMap();
                for (int j = 0; j < asData.length; j++)
                {
                    String key = asData[j][0];
                    String value = asData[j][1] == null ? "" : asData[j][1];
                    dataOfOneLP.put(key, value);
                }
                String lpCode = (String) dataOfOneLP.get(LP_CODE);
                lpMap.put(lpCode, dataOfOneLP);
            }            
        }
        // If no LP info is returned, probably certain settings wrong or the
        // server is not connected.
        else
        {
            throw new Exception(
                    "Error : Can not retrieve data from Asia Online MT engine, please check the required settings.");
        }

        return lpMap;
    }
    
    public List getDomainCombinationByLPCode(long p_languagePairCode)
    {
        List dcListForSpecifiedLPCode = new ArrayList();
        oAODataLookup = new DataLookup();
        oAODataLookup.setConfig(oAOConfig);

        ArrayList oDomainCode = oAODataLookup.GetDomainCombinations(p_languagePairCode);
        for (int i = 0; i < oDomainCode.size(); i++)
        {
            DomainCombination dc = new DomainCombination();

            Object oData = oDomainCode.get(i);
            String[][] asData = (String[][]) oData;

            for (int j = 0; j < asData.length; j++)
            {
                String key = asData[j][0];
                String value = asData[j][1];
                if (key != null && value != null)
                {
                    if (DC_CODE.equalsIgnoreCase(key))
                    {
                        dc.setCode(value);
                    }
                    else if (DC_LANGUAGE_PAIR_CODE.equalsIgnoreCase(key))
                    {
                        dc.setLanguagePairCode(value);
                    }
                    else if (DC_SOURCE_LANGUAGE_CODE.equalsIgnoreCase(key))
                    {
                        dc.setSourceLanguageCode(value);
                    }
                    else if (DC_SOURCE_LANGUAGE.equalsIgnoreCase(key))
                    {
                        dc.setSourceLanguage(value);
                    }
                    else if (DC_SOURCE_ABBREVIATION.equalsIgnoreCase(key))
                    {
                        dc.setSourceAbbreviation(value);
                    }
                    else if (DC_TARGET_LANGUAGE_CODE.equalsIgnoreCase(key))
                    {
                        dc.setTargetLanguageCode(value);
                    }
                    else if (DC_TARGET_LANGUAGE.equalsIgnoreCase(key))
                    {
                        dc.setTargetLanguage(value);
                    }
                    else if (DC_TARGET_ABBREVIATION.equalsIgnoreCase(key))
                    {
                        dc.setTargetAbbreviation(value);
                    }
                    else if (DC_LANGUAGE_PAIR.equalsIgnoreCase(key))
                    {
                        dc.setLanguagePair(value);
                    }
                    else if (DC_DOMAIN_COUNT.equalsIgnoreCase(key))
                    {
                        dc.setDomainCount(value);
                    }
                    else if (DC_DOMAIN_COMBINATION_NAME.equalsIgnoreCase(key))
                    {
                        dc.setDomainCombinationName(value);
                    }
                    else if (DC_DOMAIN_COMBINATION_DESC.equalsIgnoreCase(key)) 
                    {
                        dc.setDomainCombinationDesc(value);
                    }
                    else if (DC_DOMAIN_COMBINATION.equalsIgnoreCase(key))
                    {
                        dc.setDomainCombination(value);
                    }
                    else if (DC_DOMAIN_COM_NAME.equalsIgnoreCase(key))
                    {
                        dc.setDomainComName(value);
                    }
                    else if (DC_PRIVATE.equalsIgnoreCase(key))
                    {
                        dc.setPrivate(value);
                    }
                    else if (DC_DISABLED.equalsIgnoreCase(key))
                    {
                        dc.setDisabled(value);
                    }
                    else if (DC_DELETED.equalsIgnoreCase(key))
                    {
                        dc.setDeleted(value);
                    }
                    else if (DC_CAPITALIZE.equalsIgnoreCase(key))
                    {
                        dc.setCapitalize(value);
                    }
                    else if (DC_DEFAULT.equalsIgnoreCase(key))
                    {
                        dc.setDefault(value);
                    }
                    else if (DC_TRUE_CASE.equalsIgnoreCase(key))
                    {
                        dc.setTrueCase(value);
                    }
                }
            }
            
            dcListForSpecifiedLPCode.add(dc);
        }
        
        return dcListForSpecifiedLPCode;
    }
    
    public void translationAndGetResult(String inputData,
            String outputData)
    {
        // Start job and return jobID
        String strJobID = jobStart(inputData);

        if (strJobID.equals(""))
        {
            s_logger.error(sError);
            s_logger.error("CLIENT > ERROR : Cannot get the Job iD from server");
        }
        else
        {
            boolean FLAG_COMPLETE = checkStatusUntilJobComplete(UUID.fromString(strJobID));

            if (!FLAG_COMPLETE)
            {
                s_logger.error(sError);
            }
            else
            {
                String strLink = getJobResultLink(UUID.fromString(strJobID));

                if (strLink.equals(""))
                {
                    s_logger.error(sError);
                    s_logger.error("CLIENT > ERROR : Cannot get the result path of translaton");
                }
                else
                {
                    boolean FLAG_DL = downloadResult(strLink, outputData);

                    if (!FLAG_DL)
                    {
                        s_logger.error(sError);
                        s_logger.error("CLIENT > ERROR : Downloading incomplete!");
                    }
                }
            }
        }
    }



    /**
     * Check job status.
     * 
     * @param jobUUID
     *            - jobID
     * 
     * @return - Job status
     */
    public String checkStatus(UUID jobUUID)
    {
        String ret = "";

        try
        {
            JobAction oJobAction = new JobAction();
            oJobAction.setConfig(oAOConfig);
            oJobAction.setJobId(jobUUID);
            AOAPI.JobStatus oJobStatus = oJobAction.GetJobStatus(true);
            ret = oJobStatus.getStatus();
        }
        catch (Exception ex)
        {
            sError = ex.getMessage();
        }

        if (ret.length() == 0)
        {
            sError = "CLIENT > ERROR : Cannot get the status from server.";
        }
        else
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.info("SERVER > Translation status : " + ret);                
            }
        }

        return ret;
    }

    /**
     * Control GlobalSight waiting time for checking job status.
     * 
     * @param jobUUID
     *            - job ID.
     * 
     * @return
     */
    private boolean checkStatusUntilJobComplete(UUID jobUUID)
    {
        Long timePerRound = 6000L; // 6 seconds
        int STATUS_CHECKING_ROUND_LIMIT = 33; // waiting no more than 2 minutes
        if (oAOTranslation.getDocumentType().equals("PHRASE"))
        {
            timePerRound = 3000L; // 3 seconds
            STATUS_CHECKING_ROUND_LIMIT = 3; // waiting at most 3*3 seconds
        }
        s_logger.info("The time per round to check status is "
                + Math.round(timePerRound / 1000) + " seconds.");
        int iter = 0;
        boolean isCompleteFlag = false;

        String retStatus = "";

        while (isCompleteFlag == false && iter < STATUS_CHECKING_ROUND_LIMIT)
        {
            iter++;

            // Checking status of translation
            retStatus = checkStatus(jobUUID);

            if (retStatus.equals("Complete"))
            {
                // Set complete flag
                isCompleteFlag = true;
            }
            else
            {
                // Return status
                if (!retStatus.equals("Processing")
                        && !retStatus.equals("Queued"))
                {
                    sError = "SERVER > ERROR : Translation Incompleted.";
                    s_logger.error(sError);
                    break;
                }

                // Sleep thread
                try
                {
                    Thread.sleep(timePerRound);
                }
                catch (InterruptedException ex)
                {
                    sError = ex.getMessage();
                }
            }

            if (iter == STATUS_CHECKING_ROUND_LIMIT
                    && isCompleteFlag == false)
            {
                sError = "CLIENT > ERROR : Operation incomplete because check statusround limited exceed.";
                if (s_logger.isDebugEnabled())
                {
                    s_logger.info(sError);                    
                }
            }
        }
        
        s_logger.info("All the waiting time is : "
                + (iter * timePerRound / 1000) + " seconds.");

        return isCompleteFlag;
    }

    /**
     * Get the result link by job UUID.
     * 
     * @param jobUUID
     * @return
     */
    public String getJobResultLink(UUID jobUUID)
    {
        String retLink = "";

        try
        {
            JobAction oJobAction = new JobAction();
            oJobAction.setConfig(oAOConfig);
            oJobAction.setJobId(jobUUID);
            JobData oJobData = oJobAction.DownloadJobData("tran", bExtractOutput);

            retLink = oJobData.getPath();

            if (retLink.length() > 0 && !retLink.trim().startsWith("http://"))
            {
                retLink = getFullLink(retLink);
            }
            if (s_logger.isDebugEnabled())
            {
                s_logger.info("SERVER > Get link : " + retLink);
            }
        }
        catch (Exception ex)
        {
            sError = ex.getMessage();
            retLink = "";
        }

        return retLink;
    }

    public String getFullLink(String link)
    {
        String strLink = link;
        String url = oAOConfig.getAPIURL();

        if (url.lastIndexOf("/") > -1)
        {
            url = url.substring(0, url.lastIndexOf("/"));
        }
        strLink = url + "/DataDownload.aspx" + strLink;

        return strLink;
    }

    public boolean downloadResult(String strLink, String outputPath)
    {
        boolean FLAG_CAN_DOWNLOAD = false;

        try
        {
            URL url = new URL(strLink);

            // Add extension when down-load the ZIP file type
            if (bExtractOutput)
            {
                if (!outputPath.endsWith(".zip"))
                {
                    outputPath += ".zip";
                }
            }

            BufferedInputStream in = new BufferedInputStream(url.openStream());
            FileOutputStream fos = new FileOutputStream(outputPath);
            BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
            byte[] data = new byte[1024];
            int x = 0;

            while ((x = in.read(data, 0, 1024)) >= 0)
            {
                bout.write(data, 0, x);
            }

            bout.close();
            in.close();

            // Set flag
            FLAG_CAN_DOWNLOAD = true;
            
            s_logger.info("CLIENT > Download completed.");
            s_logger.info("CLIENT > See the output file : " + outputPath);
        }
        catch (IOException e)
        {
            sError = e.getMessage();
        }

        return FLAG_CAN_DOWNLOAD;
    }

    /**
     * Start job and return jobID.
     * 
     * @param inputData
     *            - A string to represent the file, Url or segment to be
     *            translated.
     * 
     * @return - jobID
     */
    public String jobStart(String inputData)
    {
        String retUUID = "";
        try
        {
            oAOTranslation.setBase64Encode(false);
            srcData = inputData;
            if (oAOTranslation.getDocumentType().equals("PHRASE"))
            {
                // do nothing.
            }
            else if (oAOTranslation.getDocumentType().equals("URL"))
            {
                if (!srcData.startsWith("http://"))
                {
                    srcData = "http://" + srcData;
                }
            }
            else
            {
                oAOTranslation.setFileName(new File(srcData).getName());
                byte[] retBuffer = Utilities.readFileAsByte(srcData);

                try
                {
                    srcData = Utilities.encodeBase64(retBuffer);
                }
                catch (Exception ex)
                {
                    sError = ex.getMessage();
                    return "";
                }

                oAOTranslation.setBase64Encode(true);
            }

            oAOTranslation.setSourceData(srcData);
            oAOTranslation.setSourcePath("");

            oAOTranslation.setConfig(oAOConfig);

            // Start translation job
            oAOTranslation.JobStart();

            if (oAOTranslation.getLastError() != null)
            {
                if (oAOTranslation.getLastError().contains(
                        "Unexpected end tag.")
                        || oAOTranslation.getLastError().contains(
                                "unexpected end tag."))
                {
                    sError = "CLIENT > ERROR : Sorry, this document cannot be submitted. CData must not be defined in HTML translation processing.";
                }
                else
                {
                    sError = oAOTranslation.getLastError();
                }
            }
            else
            {
                retUUID = oAOTranslation.getJobID().toString();
            }
        }
        catch (Exception ex)
        {
            sError = ex.getMessage();
            sError += oAOTranslation.getLastError();
        }

        return retUUID;
    }    
    
    /**
     * Cancel job by job UUID.
     * 
     * @param uuidJob
     */
    public void jobCancel(UUID uuidJob)
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.info("CLIENT > Sending job cancelling requestion to server.");
            s_logger.info("CLIENT > Waiting for response...");            
        }
        String ret = "";

        try
        {
            JobAction oJobAction = new JobAction();
            oJobAction.setConfig(oAOConfig);
            oJobAction.setJobId(uuidJob);
            oJobAction.JobCancel();
            ret = "CANCEL";
        }
        catch (Exception ex)
        {
            sError = ex.getMessage();
        }

        if (ret.equals(""))
        {
            sError = "CLIENT > ERROR : Cannot get the status from server.";
        }
        else
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.info("SERVER > Translation status : " + ret);                
            }
        }
    }

    /**
     * Re-run job by job UUID.
     * 
     * @param uuidJob
     */
    public void jobRerun(UUID uuidJob)
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.info("CLIENT > Sending job rerun requestion to server.");
            s_logger.info("CLIENT > Waiting for response...");            
        }
        String ret = "";

        try
        {
            JobAction oJobAction = new JobAction();
            oJobAction.setConfig(oAOConfig);
            oJobAction.setJobId(uuidJob);
            oJobAction.JobRerun();
            ret = "RERUN";
        }
        catch (Exception ex)
        {
            sError = ex.getMessage();
        }

        if (ret.equals(""))
        {
            sError = "CLIENT > ERROR : Cannot get the status from server.";
        }
        else
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.info("SERVER > Translation status : " + ret);                
            }
        }
    }

    /**
     * Delete job by job UUID.
     * 
     * @param uuidJob
     */
    public void jobDelete(UUID uuidJob)
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.info("CLIENT > Sending job delete requestion to server.");
            s_logger.info("CLIENT > Waiting for response...");            
        }
        String ret = "";

        try
        {
            JobAction oJobAction = new JobAction();
            oJobAction.setConfig(oAOConfig);
            oJobAction.setJobId(uuidJob);
            oJobAction.JobDelete();
            ret = "DELETE";
        }
        catch (Exception ex)
        {
            sError = ex.getMessage();
        }

        if (ret.equals(""))
        {
            sError = "CLIENT > ERROR : Cannot get the status from server.";
        }
        else
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.info("SERVER > Translation status : " + ret);                
            }
        }

    }

    /**
     * Extract config file to socket parameter
     * 
     * @param AOConfig
     *            - AOConfig object
     * @param Translate
     *            - Translate object
     * @param filename
     *            - the configuration file name.
     */
    private void extractConfigFile(AOConfig oAOConfig,
            Translate oAOTrans, String filename)
    {
        File aFile = new File(filename);

        if (!(aFile.exists()))
        {
            System.err.println("CLIENT > ERROR: Config file \"" + filename
                    + "\" doesn\'t exist.");
            System.exit(1);
        }

        try
        {
            BufferedReader input = new BufferedReader(new FileReader(aFile));

            try
            {
                String line = null;
                while ((line = input.readLine()) != null)
                {
                    line = input.readLine();
                    oAOConfig.setAPIURL(line);
                    line = input.readLine();
                    line = input.readLine();
                    line = input.readLine();
                    oAOConfig.setAPIPort(Integer.parseInt(line));
                    line = input.readLine();
                    line = input.readLine();
                    line = input.readLine();
                    oAOConfig.setUsername(line);
                    line = input.readLine();
                    line = input.readLine();
                    line = input.readLine();
                    oAOConfig.setPassword(line);
                    line = input.readLine();
                    line = input.readLine();
                    line = input.readLine();
                    oAOConfig.setAccountNo(Integer.parseInt(line));
                    line = input.readLine();
                    line = input.readLine();
                    line = input.readLine();
                    oAOTrans.setLanguagePair(Integer.parseInt(line));
                    line = input.readLine();
                    line = input.readLine();
                    line = input.readLine();
                    oAOTrans.setDomainCombination(Integer.parseInt(line));
                    line = input.readLine();
                    line = input.readLine();
                    line = input.readLine();
                    oAOTrans.setProjectNo(Integer.parseInt(line));
                    line = input.readLine();
                    line = input.readLine();
                    line = input.readLine();

                    // Document type setting
                    /*
                     * "HTML", "PHRASE", "TMX", "TXT", "URL", "XML", "XLIFF"
                     * "DOC" "DOCX"
                     */
                    if (line.toLowerCase().equals("html"))
                    {
                        oAOTrans.setDocumentType("HTML");
                    }
                    else if (line.toLowerCase().equals("phrase"))
                    {
                        oAOTrans.setDocumentType("PHRASE");
                    }
                    else if (line.toLowerCase().equals("tmx"))
                    {
                        oAOTrans.setDocumentType("TMX");
                    }
                    else if (line.toLowerCase().equals("txt"))
                    {
                        oAOTrans.setDocumentType("TXT");
                    }
                    else if (line.toLowerCase().equals("url"))
                    {
                        oAOTrans.setDocumentType("URL");
                    }
                    else if (line.toLowerCase().equals("xml"))
                    {
                        oAOTrans.setDocumentType("XML");
                    }
                    else if (line.toLowerCase().equals("xliff"))
                    {
                        oAOTrans.setDocumentType("XLIFF");
                    }
                    else if (line.toLowerCase().equals("doc"))
                    {
                        oAOTrans.setDocumentType("DOC");
                    }
                    else if (line.toLowerCase().equals("docx"))
                    {
                        oAOTrans.setDocumentType("DOCX");
                    }
                    else
                    {
                        System.err
                                .println("ERROR: This version does not support document type: "
                                        + line);
                        System.exit(1);
                    }

                    oAOTrans.setDocumentType(oAOTrans.getDocumentType());

                    // Set for get Debug Log for loging each step of translation.
                    oAOTrans.setDebugLog(true);

                    // Extract file option (zip/unzip)
                    boolean FOUND_SET_EXTRACT_OUTPUT = false;
                    boolean FOUND_SET_PRIORITY = false;
                    while ((line = input.readLine()) != null)
                    {
                        if (line.equals("[zipped output]"))
                        {
                            FOUND_SET_EXTRACT_OUTPUT = true;
                            FOUND_SET_PRIORITY = false;
                            continue;
                        }
                        else if (line.equals("[priority]"))
                        {
                            FOUND_SET_PRIORITY = true;
                            FOUND_SET_EXTRACT_OUTPUT = false;
                            continue;
                        }

                        if (FOUND_SET_EXTRACT_OUTPUT == true)
                        {
                            if (line.equalsIgnoreCase("true"))
                            {
                                bExtractOutput = true;
                            }
                            FOUND_SET_EXTRACT_OUTPUT = false;
                            continue;
                        }
                        else if (FOUND_SET_PRIORITY == true)
                        {
                            try
                            {
                                int ipriority = line.length() > 0 ? Integer
                                        .parseInt(line) : 99;
                                oAOTrans.setPriority(String.valueOf(ipriority));
                                break;
                            }
                            catch (NumberFormatException nfe)
                            {
                                System.err
                                        .println("ERROR: Configuration error {priority is not a valid number.}");
                                System.exit(1);
                            }
                            FOUND_SET_PRIORITY = false;
                            continue;
                        }
                    }
                }
            }
            finally
            {
                input.close();
            }
        }
        catch (IOException e)
        {
            sError = e.getMessage();
            System.exit(1);
        }

    }
    
    public static void main(String[] args) throws Exception
    {
        AsiaOnlineMtInvoker aoInvoker = new AsiaOnlineMtInvoker(
                "http://api.languagestudio.com/DatasetReceiver.asmx", 
                80, "YorkJin", "welocalizetest", 68);
        Map map = aoInvoker.getAllSupportedLanguagePairs();
        System.out.println(map);
    }

}
