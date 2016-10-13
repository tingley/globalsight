package com.globalsight.demo;

import java.util.Hashtable;

import com.globalsight.everest.foundation.sso.SSOAccountManager;


public class AccountManagerDemo implements SSOAccountManager
{

    @Override
    public boolean checkUserExists(String userId)
    {
        if (userId == null)
        {
            return false;
        }
        
        String userIdLow = userId.toLowerCase();
        
        return users.containsKey(userIdLow);
    }

    @Override
    public String getLoginResultMessage(int loginResult)
    {
        String result = "";
        
        if (1 == loginResult)
        {
            result = "Login successfully.";
        }
        else if (0 == loginResult)
        {
            result = "Login failed, password is invalid.";
        }
        else if (-1 == loginResult)
        {
            result = "Login failed, user does not exist.";
        }
        else if (-2 == loginResult)
        {
            result = "Login failed, argument not valid.";
        }
        
        return result;
    }

    @Override
    public int loginUser(String userId, String password)
    {
        // argument is not valid
        if (userId == null || password == null)
        {
            return -2;
        }
        
        String userIdLow = userId.toLowerCase();
        // user does not exist
        if (!users.containsKey(userIdLow))
        {
            return -1;
        }
        
        String mypwd = users.get(userIdLow);
        if (mypwd.equals(password))
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    private static Hashtable<String, String> users = new Hashtable<String, String>();
    
    static
    {
        users.put("user0", "password");
        users.put("user1", "password");
        users.put("user2", "password");
        users.put("user3", "password");
        users.put("user4", "password");
    }
}
