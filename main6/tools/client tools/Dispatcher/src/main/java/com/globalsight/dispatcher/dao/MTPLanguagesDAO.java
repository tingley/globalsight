/**
 *  Copyright 2013 Welocalize, Inc. 
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
package com.globalsight.dispatcher.dao;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.globalsight.dispatcher.bo.GlobalSightLocale;
import com.globalsight.dispatcher.bo.MTPLanguage;
import com.globalsight.dispatcher.bo.MTPLanguages;

/**
 * Machine Translation Profile Language DAO.
 * 
 * @author Joey
 * 
 */
public class MTPLanguagesDAO
{
    private static final Logger logger = Logger.getLogger(MTPLanguagesDAO.class);
    private final String fileName = "Languages.xml";
    private String filePath;
    private MTPLanguages mtpLanguages;    
    
    public MTPLanguagesDAO()
    {
    }

    public MTPLanguagesDAO(String p_folderPath)
    {
        if(!p_folderPath.endsWith("/") && !p_folderPath.endsWith("\\"))
        {
            p_folderPath += "/";
        }
        this.filePath = p_folderPath + fileName;
    }
    
    // Get the File Path in Server.
    public String getFilePath()
    {
        if (filePath == null)
        {
            String serverPath = MTPLanguagesDAO.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            serverPath = serverPath.substring(0, serverPath.indexOf("WEB-INF"));
            filePath = serverPath + "data/" + fileName;
        }
        
        return filePath;
    }
    
    protected void saveMTPLanguages(MTPLanguages p_langs) throws JAXBException
    {
        // create JAXB context and instantiate marshaller
        JAXBContext context = JAXBContext.newInstance(MTPLanguages.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        // Write to File
        m.marshal(p_langs, new File(getFilePath()));
    }
    
    public void saveMTPLanguage(MTPLanguage p_lang) throws JAXBException
    {
        if(p_lang.getId() < 0)
        {
            if (mtpLanguages == null)
                getMTPLanguages();
            p_lang.setId(mtpLanguages.getAndIncrement());
        } 
        
        getMTPLanguages().add(p_lang);        
        saveMTPLanguages(mtpLanguages);
    }
    
    public void updateMTPLanguage(MTPLanguage p_lang) throws JAXBException
    {
        for(MTPLanguage lang : getMTPLanguages())
        {
            if(lang.getId() == p_lang.getId())
            {
                mtpLanguages.getLanguageSet().remove(lang);
                mtpLanguages.getLanguageSet().add(p_lang);
                saveMTPLanguages(mtpLanguages);
                break;
            }
        }  
    }
    
    public void saveOrUpdateMTPLanguage(MTPLanguage p_lang)
    {
        try
        {
            if (p_lang.getId() < 0)
            {
                saveMTPLanguage(p_lang);
            }
            else
            {
                updateMTPLanguage(p_lang);
            }
        }
        catch (JAXBException e)
        {
            String message = "saveOrUpdateMTPLanguage error";
            message += "[ID:" + p_lang.getId() + ", mtProfileID:" + p_lang.getMtProfile().getId() + "]";
            logger.info(message, e);
        }
    }
    
    public void deleteMTPLanguage(long p_id) throws JAXBException 
    {
        for(MTPLanguage lang : getMTPLanguages())
        {
            if(lang.getId() == p_id)
            {
                mtpLanguages.getLanguageSet().remove(lang);
                saveMTPLanguages(mtpLanguages);
                break;
            }
        }  
    }
    
    public void deleteMTPLanguage(String[] p_removeIDS) throws JAXBException 
    {
        if(p_removeIDS == null || p_removeIDS.length == 0)
        {
            return;
        }
        
        Set<Long> remoceIDSet = new HashSet<Long>();
        for(String id : p_removeIDS)
        {
            remoceIDSet.add(Long.valueOf(id));
        }
        
        for(MTPLanguage mtProfile : getMTPLanguages())
        {
            if(remoceIDSet.contains(mtProfile.getId()))
            {
                mtpLanguages.getLanguageSet().remove(mtProfile);
            }
        }  
        
        saveMTPLanguages(mtpLanguages);
    }
    
    // Get MTP Language Business Object
    public MTPLanguage getMTPLanguage(long p_id)
    {        
        for(MTPLanguage lang : getMTPLanguages())
        {
            if(lang.getId() == p_id)
            {
                return lang;
            }
        }  
        
        return null;
    }
    
    // Get MTP Language Business Object
    public MTPLanguage getMTPLanguage(String p_mtpLangName)
    {        
        for(MTPLanguage lang : getMTPLanguages())
        {
            if(lang.getName().equals(p_mtpLangName))
            {
                return lang;
            }
        }  
        
        return null;
    }
    
    public MTPLanguage getMTPLanguage(GlobalSightLocale p_srcLocale, GlobalSightLocale p_trgLocale)
    {        
        for(MTPLanguage lang : getMTPLanguages())
        {
            if(lang.getSrcLocale().equals(p_srcLocale)
                    && lang.getTrgLocale().equals(p_trgLocale))
            {
                return lang;
            }
        }  
        
        return null;
    }
    
    // Get first matched MTPLanguage.
    public MTPLanguage getFirstMTPLanguage(long p_mtProfileID)
    {        
        for(MTPLanguage lang : getMTPLanguages())
        {
            if(lang.getMtProfileId() == p_mtProfileID)
            {
                return lang;
            }
        }  
        
        return null;
    }    
    
    public Set<MTPLanguage> getMTPLanguages()
    {
        if (mtpLanguages == null)
        {
            try
            {
                File file = new File(getFilePath());
                if (!file.exists())
                {
                    file.createNewFile();
                    mtpLanguages = new MTPLanguages();
                    return mtpLanguages.getLanguageSet();
                }
                else if (file.length() == 0)
                {
                    mtpLanguages = new MTPLanguages();
                    return mtpLanguages.getLanguageSet();
                }

                JAXBContext context = JAXBContext.newInstance(MTPLanguages.class);
                Unmarshaller um = context.createUnmarshaller();
                mtpLanguages = (MTPLanguages) um.unmarshal(new FileReader(getFilePath()));
            }
            catch (JAXBException jaxbEx)
            {
                String message = "getMTPLanguages --> JAXBException:" + getFilePath();
                logger.error(message, jaxbEx);
                return null;
            }
            catch (IOException ioEx)
            {
                String message = "getMTPLanguages --> IOException:" + getFilePath();
                logger.error(message, ioEx);
            }
        }
        
        return mtpLanguages.getLanguageSet();
    }
    
    public Map<GlobalSightLocale, Set<GlobalSightLocale>> getSupportedLocalePairs(){
        Map<GlobalSightLocale, Set<GlobalSightLocale>> pairs = new HashMap<GlobalSightLocale, Set<GlobalSightLocale>>();
        for(MTPLanguage lang : getMTPLanguages())
        {
            GlobalSightLocale srcLocale = lang.getSrcLocale();
            Set<GlobalSightLocale> trgLocaleSet = pairs.get(srcLocale);
            if(trgLocaleSet == null){
                trgLocaleSet = new HashSet<GlobalSightLocale>();
            }
            trgLocaleSet.add(lang.getTrgLocale());
            pairs.put(srcLocale, trgLocaleSet);
        } 
        return pairs;
    }
    
    
}
