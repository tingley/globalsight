package com.config.properties;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.util.PropertyUtil;
import com.util.ServerUtil;

public class EnvoyConfig
{
    public final static String RESOURCE_PARENT = "/jboss/server/standalone/deployments/globalsight.ear/lib/classes/properties";
    private final static String FILE_NAME = RESOURCE_PARENT
            + "/envoy.properties";
    private static List<String> COMPANY_FILES = null;
    private static List<String> COMPANY_NAMES = null;
    private static List<String> IGNORE_COMPANY_NAMES = new ArrayList<String>();
    static
    {
        IGNORE_COMPANY_NAMES.add("aligner");
        IGNORE_COMPANY_NAMES.add("tm");
    }

    public static boolean needCopyToAllCompany(String name)
    {
        return getCompanyFiles().contains(name.trim());
    }

    public static List<String> getCompanyFiles()
    {
        if (COMPANY_FILES == null)
        {
            COMPANY_FILES = new ArrayList<String>();
            String value = PropertyUtil.get(ServerUtil.getPath() + FILE_NAME,
                    "profile.level.company");
            String[] names = value.split(",");
            for (String name : names)
            {
                COMPANY_FILES.add(name.trim());
            }
        }

        return COMPANY_FILES;
    }

    public static List<String> getCompanyNames()
    {
        if (COMPANY_NAMES == null)
        {
            COMPANY_NAMES = new ArrayList<String>();
            File root = new File(ServerUtil.getPath() + RESOURCE_PARENT);
            File[] files = root.listFiles();
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    boolean ignore = false;
                    for (String ignoreName : IGNORE_COMPANY_NAMES)
                    {
                        if (ignoreName.equalsIgnoreCase(file.getName()))
                        {
                            ignore = true;
                            break;
                        }
                    }
                    
                    if (!ignore)
                    {
                        COMPANY_NAMES.add(file.getName());
                    }
                }
            }
        }

        return COMPANY_NAMES;
    }
}
