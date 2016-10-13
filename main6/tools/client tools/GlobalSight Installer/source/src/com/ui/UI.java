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
package com.ui;

/**
 * Provides some methods let user can communicate with the application.
 */
public interface UI
{
    public static final int MAX_PROCESS = 1000000;
    
    /**
     * Lets user specify the path of GlobalSight server.
     * 
     * @return The path of GlobalSight server.
     */
    public String specifyServer();

    /**
     * Show a message to user.
     * 
     * @param msg
     *            The message to show, can't be null.
     */
    public void showMessage(String msg);
    
    public void showWelcomePage();
    public void addProgress(int addRate, String msg);
    public void finish();
    public void error(String msg);
    public void infoError(String msg);
    public boolean confirmRewrite(String path);
    public void confirmUpgradeAgain();
    public void confirmContinue(String msg);
    public void tryAgain(String msg);
    public void upgradeJdk();
}
