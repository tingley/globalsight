package com.globalsight.dispatcher.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.globalsight.dispatcher.bo.Account;
import com.globalsight.dispatcher.bo.AppConstants;
import com.globalsight.dispatcher.bo.TranslateFileBO;
import com.globalsight.dispatcher.dao.AccountDAO;
import com.globalsight.dispatcher.dao.CommonDAO;
import com.globalsight.dispatcher.dao.DispatcherDAOFactory;
import com.globalsight.dispatcher.util.FileUtil;
import com.globalsight.dispatcher.util.ZipIt;

/**
 * The Controller of File Manager, deal with the source file and target file.
 * 
 * @author Joey
 *
 */
@Controller
@RequestMapping("/fileManagement")
public class FileManagementController implements AppConstants
{
    final Logger logger = Logger.getLogger(FileManagementController.class);
    CommonDAO commonDAO = new CommonDAO();
    AccountDAO accountDAO = DispatcherDAOFactory.getAccountDAO();
    
    @RequestMapping(value = "/main")
    public String listAll(@RequestParam Map<String, String> p_reqMap, ModelMap p_model)
    {
        List<TranslateFileBO> fileList = new ArrayList<TranslateFileBO>();
        String accountIDStr = p_reqMap.get("accountId");
        if(accountIDStr != null && accountIDStr.trim().length() > 0)
        {
            long accountID = Long.valueOf(accountIDStr);
            Account account = accountDAO.getAccount(accountID);
            if (account != null)
                fileList = commonDAO.getTranslateFile(account);
        }
        else 
        {
            Set<Account> accounts = accountDAO.getAllAccounts();
            if (accounts != null && accounts.size() > 0)
            {
                Account account = accounts.iterator().next();
                accountIDStr = String.valueOf(account.getId());
                fileList = commonDAO.getTranslateFile(account);
            }
        }
        
        p_model.put("allTranslateFiles", fileList);
        p_model.put("allAccounts", accountDAO.getAllAccounts());
        p_model.put("selectAccount", accountIDStr);
        return "fileManagement";
    }
    
    @RequestMapping("/downloadJobs")
    public void downloadJobs(HttpServletRequest p_request, HttpServletResponse p_response) throws JSONException, IOException
    {  
        Set<File> files = new HashSet<File>();
        long accountID = Long.valueOf(p_request.getParameter(JSONPN_ACCOUNT_ID));
        String[] jobIDS = p_request.getParameter(JSONPN_JOBIDS).split(",");
        Account account = accountDAO.getAccount(accountID);
        File accountFolder = new File(commonDAO.getFileStorage(), account.getAccountName());
        for (String jobID : jobIDS)
        {
            File jobFolder = getJobFolder(account, jobID);
            files.addAll(FileUtil.getAllFiles(jobFolder));
        }
        
        try
        {
            File zipFile = new File("DispatcherMW_Jobs.zip");
            ZipIt.addEntriesToZipFile(zipFile, files, accountFolder.getAbsolutePath(), "");
            FileUtil.sendFile(zipFile, null, p_response, false);
        }
        catch (Exception e)
        {
            logger.error("Download Job files error. ", e);
        }
    }
    
    @RequestMapping("/downloadXLF")
    public void downloadXLF(HttpServletRequest p_request, HttpServletResponse p_response) throws IOException, JSONException
    {
        long accountID = Long.valueOf(p_request.getParameter(JSONPN_ACCOUNT_ID));  
        String jobID = p_request.getParameter(JSONPN_JOBID);  
        String fileType = p_request.getParameter("fileType"); 
        
        try
        {
            Account account = accountDAO.getAccount(accountID);
            File folder = new File(CommonDAO.getFileStorage(), account.getAccountName() + "/" + jobID + "/" + fileType);
            FileUtil.sendFile(FileUtil.getAllFiles(folder).get(0), null, p_response, false);
            logger.info("Download File:" + folder.getAbsolutePath());
        }
        catch (Exception e)
        {
            StringBuffer message = new StringBuffer();
            message.append("Download XLF file error, by Account ID: " + accountID);
            message.append(", Job ID: " + jobID);
            message.append(", File Type: " + fileType);
            logger.error(message, e);
        }
    }
    
    @RequestMapping("/removeJobs")
    public String removeJobs(HttpServletRequest p_request, HttpServletResponse p_response)
    {
        long accountID = Long.valueOf(p_request.getParameter(JSONPN_ACCOUNT_ID));
        String[] jobIDS = p_request.getParameterValues(JSONPN_JOBIDS+"[]");
        Account account = accountDAO.getAccount(accountID);
        for (String jobID : jobIDS)
        {
            File file = getJobFolder(account, jobID);
            logger.info("Delete folder: " + file);
            FileUtil.deleteFile(file);
        }
        
        return "fileManagement";
    }
    
    private File getJobFolder(Account p_account, String p_jobID)
    {
        return new File(CommonDAO.getFileStorage(), p_account.getAccountName() + "/" + p_jobID);
    }
}
