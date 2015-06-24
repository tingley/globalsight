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
package com.ui.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.Main;
import com.config.properties.InstallValues;
import com.config.properties.Resource;
import com.ui.UI;
import com.util.Assert;
import com.util.CmdUtil;
import com.util.ServerUtil;
import com.util.UpgradeUtil;

/**
 * A implement of <code>com.ui.UI</code>, Provide a method of communication
 * with the text.
 * 
 * @see com.ui.UI
 */
public class TextUI implements UI
{
    private Logger log = Logger.getLogger(TextUI.class);
    private int processValue = 0;
    private String processMsg = "";
    private final String WELCOLM_TITLE = "-- Welcome --------------------------------------------------";
    private final String PROCESS_TITLE = "-- Update ---------------------------------------------------";
    private final String PATH_TITLE = "-- Specify GlobalSight server -------------------------------";
    private final String CONFIRM_TITLE = "-- GlobalSight Upgrade Installer ----------------------------";
    private final String ROOT = "-------------------------------------------------------------";
    private final String YN = " [Y/N]:";

    private static final String EXIT = "exit";

    private void printSpecifyServerPage(String input)
    {
        String key = Main.isPatch() ? "msg.patch.specifyServerText"
                : "msg.build.specifyServerText";

        clear();
        System.out.println();
        System.out.println(PATH_TITLE);
        System.out.println();
        System.out.println(Resource.get(key));
        System.out.println();
        System.out.println(Resource.get("input.server") + input);
        System.out.println();
        System.out.println(ROOT);
    }

    private void printProcessPage()
    {
        clear();

        System.out.println();
        System.out.println(PROCESS_TITLE);
        System.out.println();

        System.out.print(Resource.get("msg.serverPath"));
        System.out.println(ServerUtil.getPath());
        System.out.println();

        System.out.println(processMsg);

        int process = processValue * 100 / MAX_PROCESS;
        System.out.print("[");
        for (int i = 0; i < process / 2; i++)
        {
            System.out.print("|");
        }

        for (int i = process / 2; i < 50; i++)
        {
            System.out.print(" ");
        }
        System.out.print("] ");
        System.out.println(process + "%");

        System.out.println();
        System.out.println(ROOT);
    }

    /**
     * @see com.ui.UI#specifyServer()
     */
    @Override
    public String specifyServer()
    {
        printSpecifyServerPage("");

        String path = null;
        while (path == null)
        {
            System.out.println();
            System.out.println(Resource.get("input.exit"));
            System.out.println();

            String inputValue = get(Resource.get("input.server"), ".*");

            if (EXIT.equalsIgnoreCase(inputValue))
            {
                System.exit(0);
            }

            if (ServerUtil.isServerPath(inputValue))
            {
                if (!Main.isPatch())
                {
                    try
                    {
                        String oldPath = new File(inputValue)
                                .getCanonicalPath();
                        String newPath = UpgradeUtil.newInstance().getPath();
                        oldPath = oldPath.replace("\\", "/");
                        newPath = newPath.replace("\\", "/");
                        if (oldPath.equalsIgnoreCase(newPath))
                        {
                            printSpecifyServerPage(oldPath);
                            System.out.println();
                            System.out.println(Resource
                                    .get("path.upgrade.same"));
                            continue;
                        }
                    }
                    catch (IOException e1)
                    {
                        log.error(e1.getMessage(), e1);
                    }
                }

                path = inputValue;
            }
            else
            {
                printSpecifyServerPage(inputValue);
                System.out.println();
                System.out.println(Resource.get("msg.wrongServerPath"));
            }
        }

        return path;
    }

    /**
     * @see com.ui.UI#inputServerPath(String msg)
     */
    @Override
    public void showMessage(String msg)
    {
        System.out.println(msg);
        pressEntryKey();
    }

    /**
     * Just let user press "Entry" key to continue process. Typically, it is
     * used to remind user something.
     */
    private void pressEntryKey()
    {
        System.out.print(Resource.get("msg.continue"));
        try
        {
            System.in.read();
        }
        catch (IOException e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    public String readLine()
    {
    	BufferedReader input = new BufferedReader(new InputStreamReader(
                System.in));
        String inputValue = null;
        try
        {
            inputValue = input.readLine();
        }
        catch (IOException e)
        {
            log.error(e.getMessage(), e);
            StackTraceElement[] trace = e.getStackTrace();
            for (StackTraceElement msg : trace)
            {
                log.error("\tat " + msg);
            }

            pressEntryKey();
            System.exit(0);
        }
        
        return inputValue;
    }

    public String get(String label, String regex, String accept)
    {
        Assert.assertNotNull(regex, "Regex");
        Pattern pattern = Pattern.compile(regex);

        String value = null;
        while (value == null)
        {
            System.out.print(label);
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    System.in));
            String inputValue = null;
            try
            {
                inputValue = input.readLine();
            }
            catch (IOException e)
            {
                log.error(e.getMessage(), e);
                StackTraceElement[] trace = e.getStackTrace();
                for (StackTraceElement msg : trace)
                {
                    log.error("\tat " + msg);
                }

                pressEntryKey();
                System.exit(0);
            }
            Matcher matcher = pattern.matcher(inputValue);
            if (matcher.matches())
            {
                value = inputValue;
            }
            else
            {
                String msg;
                if (accept == null)
                {
                    msg = Resource.get("msg.inputInvalid");
                }
                else
                {
                    msg = MessageFormat.format(Resource.get("msg.errorInput"),
                            accept);
                }
                System.out.println(msg);
            }
        }

        return value;
    }

    /**
     * Let user input some thing.<br>
     * If the value user inputed is not match the regular expression
     * <code>regex</code>, application will ask user input again.
     * 
     * @param label
     *            Label to show before input.
     * @param regex
     *            The regular expression to check user's input.
     * @return User's input.
     */
    public String get(String label, String regex)
    {
        return get(label, regex, null);
    }

    @Override
    public void addProgress(int addRate, String msg)
    {
        processValue += addRate;
        processMsg = msg;

        if (addRate == 0 && msg != null && msg.trim().length() > 0)
        {
            printProcessPage();
        }
    }

    @Override
    public void error(String msg)
    {
        System.out.println();
        System.out.println(msg);
        exit();
    }

    public void infoError(String msg)
    {
        error(msg);
    }

    private void exit()
    {
        System.out.println();
        System.out.println(Resource.get("msg.exit"));
        try
        {
            System.in.read();
        }
        catch (IOException e)
        {
            log.error(e.getMessage(), e);
        }
        System.exit(0);
    }

    public void clear()
    {
        if (ServerUtil.isInLinux())
        {
            String[] cmd =
            { "sh", "./script/linux/clearScreen.sh" };
            try
            {
                CmdUtil.run(cmd);
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void showWelcomePage()
    {
        clear();

        System.out.println();
        System.out.println(WELCOLM_TITLE);
        System.out.println();
        System.out.println(Resource.get("msg.warnText"));
        System.out.println();
        System.out.println(ROOT);
        System.out.println();

        try
        {
            System.in.read();
        }
        catch (IOException e)
        {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void finish()
    {
        processValue = MAX_PROCESS;
        processMsg = Resource.get("process.finishText");
        printProcessPage();

        exit();
    }

    @Override
    public boolean confirmRewrite(String path)
    {
        clear();
        System.out.println();
        System.out.println(CONFIRM_TITLE);
        System.out.println();
        System.out.println(MessageFormat.format(
                Resource.get("confirm.rewrite"), path));
        System.out.println();
        System.out.println(ROOT);
        System.out.println();
        String input = get(Resource.get("confirm.rewrite2") + YN, "[YyNn]",
                "Y, y, N, n");
        return input.equalsIgnoreCase("y");
    }
    
    @Override
    public void confirmUpgradeAgain()
    {
        clear();
        System.out.println();
        System.out.println(CONFIRM_TITLE);
        System.out.println();
        System.out.println(Resource.get("version.upgrade.same"));
        System.out.println();
        System.out.println(ROOT);
        System.out.println();
        String input = get(Resource.get("confirm.upgradeAgain") + YN, "[YyNn]",
                "Y, y, N, n");
        if (!input.equalsIgnoreCase("y"))
        {
            log.info("Not upgrade again");
            System.exit(0);
        }
    }

    public static void main(String[] args)
    {

    }

    @Override
    public void confirmContinue(String msg)
    {
        clear();
        System.out.println();
        System.out.println(CONFIRM_TITLE);
        System.out.println();
        System.out.println(msg);
        System.out.println();
        System.out.println(ROOT);
        System.out.println();
        String input = get(Resource.get("confirm.continue")+ YN, "[YyNn]",
                "Y, y, N, n");
        if (!input.equalsIgnoreCase("y"))
        {
            log.info("Exit");
            System.exit(0);
        }
    }

    @Override
    public void tryAgain(String msg)
    {
        clear();
        System.out.println();
        System.out.println(CONFIRM_TITLE);
        System.out.println();
        System.out.println(msg);
        System.out.println();
        System.out.println(ROOT);
        System.out.println();
        String input = get(Resource.get("confirm.retrye")+ YN, "[YyNn]",
                "Y, y, N, n");
        if (!input.equalsIgnoreCase("y"))
        {
            log.info("Exit");
            System.exit(0);
        }
        
    }

	@Override
	public void upgradeJdk() {
		
		clear();
        System.out.println();
        System.out.println(CONFIRM_TITLE);
        System.out.println();
        System.out.println(Resource.get("msg.jdk.text"));
        System.out.println();
        System.out.println(ROOT);
        System.out.println();
        
        while (true)
        {
            System.out.println(Resource.get("input.exit"));

            String inputValue = get(Resource.get("lb.jdk.home") + ": ", ".*");
            
            if (EXIT.equalsIgnoreCase(inputValue))
            {
                System.exit(0);
            }
            
            if (new File(inputValue + "/bin").exists())
            {
            	InstallValues.setJavaHome(inputValue);
            	break;
            }
            else
            {
            	System.out.println(Resource
                        .get("msg.jdk.home.wrong"));
            }
        }
        
        
//        return input.equalsIgnoreCase("y");
	}
}
