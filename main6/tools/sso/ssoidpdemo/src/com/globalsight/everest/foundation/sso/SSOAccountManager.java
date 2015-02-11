package com.globalsight.everest.foundation.sso;

public interface SSOAccountManager
{
    /**
     * Login one user
     * @param userId The user ID
     * @param password The password
     * @return 1 login successful, 0 login failed
     */
    public int loginUser(String userId, String password);
    
    /**
     * Get the message for login result
     * @param loginResult
     * @return the mssage for login result, can be empty
     */
    public String getLoginResultMessage(int loginResult);
    
    /**
     * Check one user
     * @param userId The user ID
     * @return true if this user exits
     */
    public boolean checkUserExists(String userId);
}
