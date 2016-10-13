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
package com.plug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.config.properties.EnvoyConfig;
import com.config.properties.Resource;
import com.plug.Version_7_1_6_0.DbServer;
import com.plug.Version_7_1_6_2.InsertJspFilterForFilterConfiguration;
import com.ui.UI;
import com.ui.UIFactory;
import com.util.FileUtil;
import com.util.PropertyUtil;
import com.util.ServerUtil;

public class Plug_7_1_6_2 implements Plug
{
    private static Logger log = Logger.getLogger(Plug_7_1_6_2.class);

    private UI ui = UIFactory.getUI();
    private DbServer dbServer = new DbServer();

    @Override
    public void run()
    {
        updateJSPFilter();
        updateMsOfficeFilter();
        updateStyleFile();
    }

    private void updateJSPFilter()
    {
        List<Long> allCompanyIds = new ArrayList<Long>();
        try
        {
            allCompanyIds = dbServer.getAllCompanyIds();
        }
        catch (SQLException e)
        {
            log.error("Can not get all company ids.", e);
        }
        for(int i = 0; i < allCompanyIds.size(); i++)
        {
            long companyId = allCompanyIds.get(i);
            log.info("Initial JSP Filter for company with id: " + companyId + "...");
            try
            {
                InsertJspFilterForFilterConfiguration.insertFilterConfigur("JSP Filter", "|13|", "jsp_filter", "The filter for JSP files.", companyId);
            }
            catch (SQLException e)
            {
              log.info("Failed");
              log.debug(e);
              ui.confirmContinue(MessageFormat.format(Resource
                      .get("msg.updateDatabase"), e.getMessage()));
            }
        }
        log.info("Success to update JSP Filters.");
    }
    
    private void updateMsOfficeFilter()
    {
    	String fileName = "WordExtractor.properties";
    	String paraKey = "unextractableWordParagraphStyles";
    	String charKey = "unextractableWordCharacterStyles";
    	String rootPath = ServerUtil.getPath() + EnvoyConfig.RESOURCE_PARENT;
    	
    	String defaultParaStyles = "";
    	String defaultCharStyles = "";
    	
        File defaultFile = new File(rootPath + "/" + fileName);
        if (defaultFile.exists())
        {
            defaultParaStyles = PropertyUtil.get(defaultFile, paraKey);
            defaultCharStyles = PropertyUtil.get(defaultFile, charKey);
            
            if (defaultParaStyles == null)
            	defaultParaStyles = "";
            if (defaultCharStyles == null)
            	defaultCharStyles = "";
        }
        
        try 
        {
			List<String> companyNames = dbServer.getAllCompanyNames();
			for (String name : companyNames)
			{
				String paraStyles = defaultParaStyles;
				String charStyles = defaultCharStyles;
				
				File file = new File(rootPath + "/" + name + "/" + fileName);
				if (file.exists())
				{
					 paraStyles = PropertyUtil.get(file, paraKey);
					 charStyles = PropertyUtil.get(file, charKey);
					 if (paraStyles == null)
						 paraStyles = defaultParaStyles;
					 if (charStyles == null)
						 charStyles = defaultCharStyles;
				}
				
				updateStyles(name, paraStyles, charStyles);
			}
		} 
        catch (SQLException e) 
		{
			log.error("Can not get all company names.", e);
		}       
    }
    
    private void updateStyles(String companyName, String paraStyles,
			String charStyles) 
    {
    	long companyId = dbServer.getCompanyIdByName(companyName);
    	
    	String sql = "update ms_office_doc_filter set "
				+ "UNEXTRACTABLE_WORD_PARAGRAPH_STYLES = '" + paraStyles
				+ "', UNEXTRACTABLE_WORD_CHARACTER_STYLES = '" + charStyles
				+ "' where COMPANY_ID = " + companyId;

    	log.info("Updating MSOfficeDocFilter for company with id: " + companyId + "...");
		try 
		{
			dbServer.update(sql);
		} 
		catch (SQLException e) 
		{
			log.info("Failed");
            log.debug(e);
            ui.confirmContinue(MessageFormat.format(Resource
                    .get("msg.updateDatabase"), e.getMessage()));
		}
		
		log.info("Success to update MSOfficeDocFilter.");
	}
    
    private void updateStyleFile()
    {
        File root = new File(ServerUtil.getPath() + EnvoyConfig.RESOURCE_PARENT);
        List<File> tagProperties = FileUtil.getAllFiles(root, new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return "Styles.properties".equalsIgnoreCase(pathname
                        .getName());
            }
        });
        
        for (File f : tagProperties)
        {
            try
            {
                BufferedReader in = new BufferedReader(new FileReader(f));
                String s = in.readLine();
                StringBuilder content = new StringBuilder();

                while (s != null)
                {
                    content.append(s).append(FileUtil.lineSeparator);
                    s = in.readLine();
                }
                in.close();
                
                String fileContent = content.toString();
                fileContent = fileContent.replace("true", "false");


                FileWriter out = new FileWriter(f, false);
                out.write(fileContent);
                out.write(FileUtil.lineSeparator);
                out.flush();
                out.close();
            }
            catch (FileNotFoundException e)
            {
                log.error(e.getMessage(), e);
                ui.error(e.getMessage());
                System.out.println();
            }
            catch (IOException e)
            {
                log.error(e.getMessage(), e);
                ui.error(e.getMessage());
                System.out.println();
            }
           
        }
    }
}
