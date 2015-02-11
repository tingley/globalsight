package com.globalsight.dispatcher.dao;

import java.io.FileNotFoundException;
import java.util.Calendar;

import javax.xml.bind.JAXBException;

import com.globalsight.dispatcher.bo.MachineTranslationProfile;
import com.globalsight.dispatcher.dao.MTProfilesDAO;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileConstants;

public class MTProfileDAOTest
{
    static MTProfilesDAO dao = new MTProfilesDAO("E:/Data/temp/000/");
    /**
     * @param args
     * @throws JAXBException 
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws JAXBException, FileNotFoundException
    {
        remove(1);
        add("H05");
        remove(3);
        add("H06");
    }
    
    public static void add(String p_name) throws JAXBException
    {
        MachineTranslationProfile mtProfile1 = new MachineTranslationProfile();
        if (p_name != null)
            mtProfile1.setMtProfileName(p_name);
        mtProfile1.setMtEngine(MTProfileConstants.MT_ENGINE);
        mtProfile1.setDescription("Time:" + Calendar.getInstance().getTime().toString());
        dao.saveMTProfile(mtProfile1);
    }
    
    public static void remove(long p_mtProfileID) throws JAXBException
    {
        MachineTranslationProfile mtProfile = dao.getMTProfile(p_mtProfileID);
        if(mtProfile != null)
        dao.deleteMTProfile(p_mtProfileID);
    }

}
