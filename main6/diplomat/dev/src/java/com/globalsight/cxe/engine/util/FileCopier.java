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
package com.globalsight.cxe.engine.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author shaucle
 */
/**
 * This class mimic the behavior of "cp sourceDir/. destDir" in UNIX or "xcopy
 * /e /i sourceDir destDir" in Windows.
 */
public class FileCopier {

    static private final int BUFFER_SIZE = 16384;

    public static void copy(String p_sourceDir, String p_destDir) {
        File sourceDir = new File(p_sourceDir);
        copy(sourceDir, p_destDir);
    }
    public static void copy(File p_sourceFile, String p_destDir){
        File destDir = new File(p_destDir);
        copyFile(p_sourceFile, destDir);
    }
    public static void copyDir(File p_sourceDir, String p_destDir){
        File destDir = new File(p_destDir);
        File[] fileEntries = p_sourceDir.listFiles();
        doCopy(fileEntries, destDir);
    }

    private static void doCopy(File[] p_fileEntries, File p_destDir){
        int size = p_fileEntries.length;
        for (int i = 0; i < size; i++) {
            if (p_fileEntries[i].isDirectory()) {
                File destDir = new File(p_destDir, p_fileEntries[i].getName());
                destDir.mkdir();
                File[] fileEntries = p_fileEntries[i].listFiles();
                doCopy(fileEntries, destDir);

            } else {
                copyFile(p_fileEntries[i], p_destDir);
            }
        }
    }

    public static void copyFile(File p_sourceFile, File p_destDir){
        check(p_destDir);
        File destFile = new File(p_destDir, p_sourceFile.getName());
        byte[] buffer = new byte[BUFFER_SIZE];
        int size;
        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            input = new FileInputStream(p_sourceFile);
            output = new FileOutputStream(destFile);

            while ((size = input.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
        } catch (IOException e) {
            //TODO we should use a common Exception here
            throw new RuntimeException("file copy error : " + e.getMessage(), e);
        } finally {
            try {
                if (input != null)
                    input.close();
            } catch (IOException e) {
                //ignore
            }
            try {
                if (output != null)
                    output.close();
            } catch (IOException e) {
                //ignore
            }
        }
    }

    /**
     * Copys file to target dir with given target file name
     * @param p_sourceFile
     * @param p_destDir
     * @param p_targetFileName
     */
    public static void copyFile(File p_sourceFile, 
    		                    File p_destDir, 
    		                    String p_targetFileName){
        check(p_destDir);
        File destFile = new File(p_destDir, p_targetFileName);
        byte[] buffer = new byte[BUFFER_SIZE];
        int size;
        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            input = new FileInputStream(p_sourceFile);
            output = new FileOutputStream(destFile);

            while ((size = input.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
        } catch (IOException e) {
            //TODO we should use a common Exception here
            throw new RuntimeException("file copy error : " + e.getMessage(), e);
        } finally {
            try {
                if (input != null)
                    input.close();
            } catch (IOException e) {
                //ignore
            }
            try {
                if (output != null)
                    output.close();
            } catch (IOException e) {
                //ignore
            }
        }
    }

    
    /**
     * @param dir
     */
    private static void check(File dir) {
        if(!dir.isDirectory()){
            throw new RuntimeException(dir+"is not a dir");
        }
        
    }
    static public void main(String[] args) throws Exception {
        copy(args[0], args[1]);
    }
}