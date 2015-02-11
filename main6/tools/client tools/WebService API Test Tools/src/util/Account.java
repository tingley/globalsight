package util;

public class Account
{
    private String host;
    private String port;
    private String userName;
    private String password;
    private String https;

    public Account() {
    }

    public Account(String host, String port, String userName, String password, boolean https) {
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.https = (https == true ? "true" : "false");
    }

    public String toString() {
        return
                "host=" + host + " port=" + port + " userName=" + userName + " password=" + password + " htttps" + https;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Account)) return false;
        Account temp = (Account) o;
        return temp.host.equals(host)
                && temp.port.equals(port)
                && temp.userName.equals(userName)
                && temp.https.equals(https);

    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + host.hashCode();
        result = 37 * result + port.hashCode();
        result = 37 * result + userName.hashCode();
        result = 37 * result + password.hashCode();
        result = 37 * result + https.hashCode();
        return result;
    }


    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @return the port
     */
    public String getPort() {
        return port;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @param port the port to set
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public String getHttps() {
        return https;
    }

    public void setHttps(String https) {
        this.https = https;
    }


}
