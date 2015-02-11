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

import com.globalsight.BuildVersion;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.tm.importer.ImportOptions;
import com.globalsight.ling.tm2.TmVersion;
import com.globalsight.ling.tm3.integration.Tm3Enabler;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.progress.ProgressReporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;

public class Benchmark {

    private static void showUsage(String message) {
        System.err.println(message + "\n" +
"Usage:\n" +
"Benchmark <tmfile>...\n" +
"The first tmfile should be small (~100 TUs) and will be imported until the \n"+
"times stabilize.  The rest are the benchmark TMs to import." +
"");
        System.exit(1);
    }

    static public void main(String[] args) throws Exception {
        BatchLoad.init();
        CompanyThreadLocal.getInstance().setIdValue(CompanyWrapper.SUPER_COMPANY_ID);
        List<File> files = new ArrayList<File>();
        for (String fileName : args) {
            files.add(new File(fileName));
        }
        benchmark(System.out, files);
    }

    public static void benchmark(PrintStream out, List<File> tmxFiles)
            throws Exception {
        if (tmxFiles.size() == 0) {
            showUsage("must pass at least one file");
        }
        for (File file : tmxFiles) {
            if (! file.exists()) {
                showUsage(file + " does not exist");
            }
        }

        Runtime runtime = Runtime.getRuntime();
        out.println(
            BuildVersion.PRODUCT + " " +
            BuildVersion.VERSION + " (" +
            BuildVersion.BUILD_DATE + ")");
        out.println(getHostname());
        out.println(
            System.getProperty("os.name") + " " +
            System.getProperty("os.version") + " (" +
            System.getProperty("os.arch") + ")");
        out.println(
            System.getProperty("java.vm.vendor") + " " +
            System.getProperty("java.vm.name") + " " +
            System.getProperty("java.vm.version"));
        out.println(
            runtime.availableProcessors() + " CPUs, " +
            runtime.maxMemory() / 1024 + " kB memory");
        // TODO: database stats

        BatchLoad.removeTm("Benchmark TM");
        out.println();
        out.println(getTm2TuCount() + " TM2 TUVs");

        String companyId = CompanyThreadLocal.getInstance().getValue();
        Company company = CompanyWrapper.getCompanyById(companyId);
        TmVersion origTmVersion = company.getTmVersion();

        new Tm3Enabler(HibernateUtil.getSession().connection(), company).enable(
            new ProgressReporter() {
                public void setMessageKey(String msg, String def) {}
                public void setPercentage(int pct) {}
            });

        for (TmVersion tmVersion :
                new TmVersion[] { TmVersion.TM2, TmVersion.TM3 }) {

            out.println();
            out.println("Benchmarking " + tmVersion);

            company.setTmVersion(tmVersion);
            BatchLoad.createTm("Benchmark TM");

            importUntilStable(out, tmxFiles.get(0).toString());
            for (File f : tmxFiles.subList(1, tmxFiles.size())) {
                BatchLoad.ImportStats r =
                    BatchLoad.runImport(System.err,
                        "Benchmark TM", f.toString(),
                        ImportOptions.TYPE_TMX2, ImportOptions.SYNC_OVERWRITE);
                out.println(
                    f + " (" + r.getTuCount() + " TUs): " +
                    BatchLoad.getTimeString(r.getDt()) + ", " +
                    BatchLoad.getTimeString(r.getDtPerDu()) + " per TU");
            }

            BatchLoad.removeTm("Benchmark TM");
        }
    }

    public static BatchLoad.ImportStats importUntilStable(
            PrintStream out, String tmxFile) throws Exception{
        Long lastDt = null;
        while (true) {
            BatchLoad.ImportStats r =
                BatchLoad.runImport(System.err,
                    "Benchmark TM", tmxFile,
                    ImportOptions.TYPE_TMX2, ImportOptions.SYNC_OVERWRITE);
                out.println(
                    tmxFile + " (" + r.getTuCount() + " TUs): " +
                    BatchLoad.getTimeString(r.getDt()) + ", " +
                    BatchLoad.getTimeString(r.getDtPerDu()) + " per TU");
            if (lastDt != null &&
                (lastDt - r.getDt()) / (float) r.getDt() < .05) {
                return r;
            }
            lastDt = Long.valueOf(r.getDt());
        }
    }

    public static String getHostname() throws Exception {
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec("hostname");
        BufferedReader in =
            new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String r = in.readLine();
        proc.waitFor();
        return r;
    }

    public static long getTm2TuCount() throws Exception {
        Connection c = HibernateUtil.getSession().connection();
        String q = "select count(*) from project_tm_tuv_t";
        ResultSet r = c.prepareStatement(q).executeQuery();
        r.next();
        return r.getLong(1);
    }
}
