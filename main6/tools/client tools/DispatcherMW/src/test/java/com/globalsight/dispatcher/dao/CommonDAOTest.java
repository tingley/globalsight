package com.globalsight.dispatcher.dao;

import java.util.List;

import com.globalsight.dispatcher.bo.GlobalSightLocale;

public class CommonDAOTest extends CommonDAO
{
    public static void main(String[] args)
    {
        print(getAllGlobalSightLocale(), "getAllGlobalSightLocale");
        print(allLocalesById, "\nallLocalesById");

        System.out.println("\n****************************************");
        print(1);
        print(98);

        System.out.println("\n****************************************");
        print(getGlobalSightLocaleByShortName("en_US"));
        print(getGlobalSightLocaleByShortName("en-US"));
    }

    public static void print(List<GlobalSightLocale> p_list, String p_msg)
    {
        System.out.println(p_msg + "\n*************************************");
        for (int i = 0; i < p_list.size(); i++)
        {
            GlobalSightLocale gsl = p_list.get(i);
            System.out.print("Index:" + (i < 10 ? "0" + i : i) + "\t");
            print(gsl);
        }
    }

    public static void print(int p_gsLocaleID)
    {
        print(getGlobalSightLocaleById(p_gsLocaleID));
    }

    public static void print(GlobalSightLocale p_gsLocale)
    {
        if (p_gsLocale == null)
        {
            System.out.println("The GlobalSight is NULL. ");
        }
        else
        {
            System.out.println("ID:" + p_gsLocale.getId() + "\t\tDisplayName:" + p_gsLocale.getDisplayName());
        }
    }
}
