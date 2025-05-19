package view.frontend.LoginFrame;

import java.io.Serializable;

/*
* 这个类是用来记录用户名及密码，还有哈希后的密码
* 我设置了用户名一旦设置不可更改的逻辑
* */
public class User implements Serializable {
    private static final long serialVersionUID = 1L; //序列化
    private String username;
    private String hashedPassword;

    public User(String username, String hashedPassword) {
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    @Override
    public String toString() {
        return "User{" +
               "username='" + username + '\'' +
               '}';
    }
}