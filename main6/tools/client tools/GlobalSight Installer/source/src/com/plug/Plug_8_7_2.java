package com.plug;

import java.io.File;

import org.apache.log4j.Logger;

import com.util.FileUtil;
import com.util.ServerUtil;

public class Plug_8_7_2 implements Plug
{
    private static Logger log = Logger.getLogger(Plug_8_7_2.class);

    // use
    // "blaise-translation-supplier-api-example-1.4.0-RC1-jar-with-dependencies.jar"
    // to replace previous 1.3.1 jar.
    private static final String BLAISE_OLD_JAR_FILE1 = "/jboss/server/standalone/deployments/globalsight.ear/lib/blaise-translation-supplier-api-example-1.2.1.jar";
    private static final String BLAISE_OLD_JAR_FILE2 = "/jboss/server/standalone/deployments/globalsight.ear/lib/blaise-translation-supplier-api-example-1.3.0-jar-with-dependencies.jar";
    private static final String BLAISE_OLD_JAR_FILE3 = "/jboss/server/standalone/deployments/globalsight.ear/lib/blaise-translation-supplier-api-example-1.3.1-jar-with-dependencies.jar";

    @Override
    public void run()
    {
        // GBS-4555: Delete old Blaise jar file
        deleteFiles(ServerUtil.getPath() + BLAISE_OLD_JAR_FILE1);
        deleteFiles(ServerUtil.getPath() + BLAISE_OLD_JAR_FILE2);
        deleteFiles(ServerUtil.getPath() + BLAISE_OLD_JAR_FILE3);
    }

    private void deleteFiles(String path)
    {
        try
        {
            File f = new File(path);
            if (f.exists())
                FileUtil.deleteFile(f);
        }
        catch (Exception e)
        {
            log.error(e);
        }
    }
}
