package codes.recursive.dexcomproxy.model;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class User {
    private String accountName;
    private String password;
    private String applicationId;

    public User(String accountName, String password) {
        this.accountName = accountName;
        this.password = password;
        this.applicationId = "d8665ade-9673-4e27-9ff6-92db4ce13d13";
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
}

