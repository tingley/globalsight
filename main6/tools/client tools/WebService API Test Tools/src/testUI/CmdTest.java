/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package testUI;
import util.*;

import java.io.IOException;
import java.io.File;
import java.io.PrintStream;

/**
 *
 * @author likun
 */
public class CmdTest {

    public static void main(String args[]){
        runNotepad();

    }

    public static void runNotepad(){
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
	String filePath = path+ "/" +"output/a.pdf";
        File af = new File(filePath);

        Runtime runtime = Runtime.getRuntime();
        try{
             System.out.println(runtime.exec("rundll32 url.dll FileProtocolHandler "+af.getAbsolutePath()));
        }
        catch(IOException e){
        	PrintStream ps = WriteLog.getPrintStreamForLog(WebServiceConstants.LOG_FILE);
        	e.printStackTrace(ps);
        }


    }

}
