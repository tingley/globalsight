package com.globalsight.machineTranslation.asiaOnline;

import java.util.Calendar;
import java.util.Map;

public class HelloWorld
{
    public static void main(String[] args) throws Exception{
        System.out.println("Time: " + Calendar.getInstance().getTime());
        AsiaOnlineMtInvoker aoInvoker = new AsiaOnlineMtInvoker(
                "http://api.languagestudio.com/DatasetReceiver.asmx", 
                80, "YorkJin", "welocalizetest", 68);
        Map map = aoInvoker.getAllSupportedLanguagePairs();
        System.out.println(map);
    }
}
