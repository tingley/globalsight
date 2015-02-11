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

package com.globalsight.everest.tm;

import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.TmManager;
import com.globalsight.everest.tm.importer.ImportOptions;
import com.globalsight.everest.tm.importer.ImportUtil;

import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.ling.tm2.TmCoreManagerWLRMIImpl;
import com.globalsight.everest.localemgr.LocaleManagerWLRMIImpl;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.projecthandler.ProjectHandlerWLRMIImpl;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.jobhandler.JobHandlerWLRMIImpl;

import com.globalsight.importer.IImportManager;
import com.globalsight.util.progress.InterruptMonitor;
import com.globalsight.util.progress.IProcessStatusListener;
import com.globalsight.util.progress.ProgressReporter;
import com.globalsight.importer.ImporterException;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.server.RegistryLocator;
import com.globalsight.everest.util.server.ServerRegistry;
import com.globalsight.everest.util.system.Envoy;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.util.GeneralException;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

import org.apache.commons.cli.*;

import java.io.*;
import java.util.*;

/**
 * Batch utility to upload TMX files to the server and import them
 * into a specific TM.
 */
public class BatchLoad {

    static final Options opts = new Options()
        .addOption(new Option(null, "savemode", true, null))
        .addOption(new Option(null, "filetype", true, null))
        .addOption(new Option(null, "create", false, null))
        .addOption(new Option(null, "company", true, null))
        .addOption(new Option(null, "temp", false, null))
        .addOption(new Option(null, "no-validate", false, null));

    private static void showUsage(String message) {
        System.err.println(message + "\n" +
"BatchLoad [option]... <tmname> <tmfile>...\n" +
"  --filetype     tmx2 (default) | xml | <ImportOptions constant>\n" +
"  --savemode     merge (default) | overwrite | discard\n" +
"  --company      company id of the TM (default supercompany)\n" +
"  --create       create the TM first\n" +
"  --temp         create the TM difrst, delete it after\n" +
"  --no-validate  don't run the usual validation step\n");
        System.exit(1);
    }

    static public void main(String[] args) throws Exception {
        try {
            CommandLine line = new GnuParser().parse(opts, args);
            if (line.getArgs().length < 2) {
                showUsage("tmname and tmfile arguments are mandatory");
            }
            if (line.getArgs().length > 2) {
                showUsage("too many arguments");
            }

            String tmxType  = line.getOptionValue("filetype");
            if (tmxType == null) {
                tmxType = ImportOptions.TYPE_TMX2;
            } else {
                if (!(tmxType.equals(ImportOptions.TYPE_XML) ||
                      tmxType.equals(ImportOptions.TYPE_TMX1) ||
                      tmxType.equals(ImportOptions.TYPE_TMX2) ||
                      tmxType.equals(ImportOptions.TYPE_TTMX_RTF) ||
                      tmxType.equals(ImportOptions.TYPE_TTMX_HTML) ||
                      tmxType.equals(ImportOptions.TYPE_TTMX_FM) ||
                      tmxType.equals(ImportOptions.TYPE_TTMX_FM_SGML) ||
                      tmxType.equals(ImportOptions.TYPE_TTMX_IL) ||
                      tmxType.equals(ImportOptions.TYPE_TTMX_XPTAG))) {
                    showUsage("Unknown filetype: " + tmxType);
                }
            }

            String syncMode = line.getOptionValue("savemode");
            if (syncMode == null) {
                syncMode = ImportOptions.SYNC_OVERWRITE;
            } else {
                if (!(syncMode.equals(ImportOptions.SYNC_OVERWRITE) ||
                      syncMode.equals(ImportOptions.SYNC_MERGE) ||
                      syncMode.equals(ImportOptions.SYNC_DISCARD))) {
                    showUsage("Unknown savemode: " + syncMode);
                }
            }

            String companyId = line.getOptionValue("company");
            if (companyId == null) {
                companyId = "0";
            } else {
                Integer.parseInt(companyId);
            }
            CompanyThreadLocal.getInstance().setIdValue(companyId);

            String tmName = line.getArgs()[0];

            String tmxName = line.getArgs()[1];
            if (!(new File(tmxName).exists())) {
                showUsage("File does not exist: " + tmxName);
            }

            init();

            if (line.hasOption("create") || line.hasOption("temp")) {
                ProjectTM tm = new ProjectTM();
                tm.setCompanyId(companyId);
                tm.setName(tmName);
                ServerProxy.getProjectHandler().createProjectTM(tm);
            } else {
                ProjectTM tm = ServerProxy.getProjectHandler()
                    .getProjectTMByName(tmxName, false);
                if (tm == null) {
                    showUsage("no such TM: " + tmxName);
                }
            }

            if (! line.hasOption("no-validate")) {
                // This adds the DOCTYPE required for the file to validate,
                // among other things
                File validatedFile = File.createTempFile("batchimport", ".tmx");
                validatedFile.deleteOnExit();
                ImportUtil.createInstance().saveTmFileWithValidation(
                    new File(tmxName), validatedFile);
                tmxName = validatedFile.getPath();
            }

            runImport(tmName, tmxName, tmxType, syncMode);

            if (line.hasOption("temp")) {
                ProjectTM tm = ServerProxy.getProjectHandler()
                    .getProjectTMByName(tmName, false);
                LingServerProxy.getTmCoreManager().removeTmData(tm,
                    // stub this out in favor of using log4j
                    new ProgressReporter() {
                        public void setMessageKey(String key, String msg) {
                            //System.out.println(msg);
                        }
                        public void setPercentage(int percentage) {
                            //System.out.println(percentage + "%");
                        }
                    },
                    new InterruptMonitor());
            }

        } catch (ParseException e) {
            showUsage(e.getMessage());
        } catch (Exception e) {
            System.err.println("\nError: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void init() throws Exception {
        // This would start everything, but we can be a bit more frugal
        // Envoy.main(new String[] { "startup" });
        ServerRegistry reg = RegistryLocator.getRegistry();
        reg.bind("TmManager", new TmManagerLocal());
        reg.bind("TmCoreManager", new TmCoreManagerWLRMIImpl());
        reg.bind("ProjectHandler", new ProjectHandlerWLRMIImpl());
        reg.bind("LocaleManager", new LocaleManagerWLRMIImpl());
        reg.bind("JobHandlerServer", new JobHandlerWLRMIImpl());
    }

    public static void runImport(
            String tmName, String tmxName, String tmxType, String syncMode)
            throws Exception {
        ImportOptions options = new ImportOptions();
        options.setFileName(tmxName);
        options.setFileType(tmxType);
        options.setSyncMode(syncMode);
        options.setSelectedSource("all");
        options.setSelectedTargets(Collections.singleton("all"));

        IImportManager importer =
            ServerProxy.getTmManager().getImporter(tmName);
        importer.setImportOptions(options.getXml());
        importer.setImportFile(tmxName);

        // Convert and validate input file.
        String temp = importer.analyzeFile();
        options.init(temp);

        String error = options.getErrorMessage();
        if (error != null && error.length() > 0) {
            System.err.println("error: " + error);
            System.exit(1);
        }

        long start = System.currentTimeMillis();
        int countTotal = options.getExpectedEntryCount();

        final Object monitor = new Object();

        importer.attachListener(
            new IProcessStatusListener()
            {
                public void listen(int entryCount, int percentage,
                    String message)
                {
                    System.out.print("\rEntry " + entryCount + " - " +
                        percentage + "% " + message);
                    if (percentage == 100)
                    {
                        System.out.println();
                    }
                    if (message.startsWith("import finished"))
                    {
                        synchronized (monitor)
                        {
                            monitor.notify();
                        }
                    }
                }
            });

        synchronized (monitor)
        {
            // runs in a background thread; we'll wake up when the final
            // listen message is sent
            importer.doImport();
            monitor.wait();
        }

        long stop = System.currentTimeMillis();
        long duration = stop - start;
        long durationPerTU = duration / countTotal;

        System.out.println("Imported " + countTotal + " entries in " +
            getTimeString(duration) + ", " +
            getTimeString(durationPerTU) + " per TU.");
    }

    public static String getTimeString(long duration) {
        if (duration < 1000L) {
            // millisecond range
            return String.valueOf(duration) + "ms";
        }
        else if (duration < 60L*1000L) {
            // second range less than a minute
            return String.valueOf(duration/1000L) + "s " +
                String.valueOf(duration % 1000L) + "ms";
        }
        else {
            // minute range, don't show ms.
            long seconds, minutes, hours;

            seconds = duration / 1000;
            hours = seconds / 3600;
            seconds = seconds % 3600;
            minutes = seconds / 60;
            seconds = seconds % 60;

            return String.valueOf(hours) + "h " +
                String.valueOf(minutes) + "m " +
                String.valueOf(seconds) + "s";
        }
    }
}
