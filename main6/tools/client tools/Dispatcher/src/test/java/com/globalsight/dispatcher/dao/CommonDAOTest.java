package com.globalsight.dispatcher.dao;

import java.util.List;

import com.globalsight.dispatcher.bo.GlobalSightLocale;

public class CommonDAOTest extends CommonDAO
{
    public static void main(String[] args){
//        print(getAllGlobalSightLocale(), "getAllGlobalSightLocale");
//        print(allLocalesById, "allLocalesById");
        
        System.out.println("****************************************");
        GlobalSightLocale gsl = getGlobalSightLocaleById(1);
        System.out.println(gsl.getId() + "\t" + gsl.getDisplayName());
        gsl = getGlobalSightLocaleById(98);
        System.out.println(gsl.getId() + "\t" + gsl.getDisplayName());
        print(gsl);
    }
    
    public static void print(List<GlobalSightLocale> p_list, String p_msg){
        System.out.println(p_msg + "\t*************************************");
        for(int i=0; i<p_list.size();i++){
            GlobalSightLocale gsl = p_list.get(i);
            System.out.println("Index:" + (i<10? "0" + i : i) + "\t" + gsl.getId() + "\t" + gsl.getDisplayName());
        }
    }
    
    public static void print(GlobalSightLocale p_gsLocale){
        System.out.println("ID:\t" + p_gsLocale.getId());
        System.out.println("toString:\t" + p_gsLocale);
        System.out.println("getDisplayName:\t" + p_gsLocale.getDisplayName());
        System.out.println("getLocale:\t" + p_gsLocale.getLocale());
    }
}
