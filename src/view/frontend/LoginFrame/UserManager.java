package view.frontend.LoginFrame;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
/*
* 这个类用来
*
* */
public class UserManager {
    private static final String USER_DATA_FILE = "users.dat"; // Store in project root
    private Map<String, User> users;

    public UserManager() {
        this.users = loadUsers();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(
                    password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            // This should ideally be handled more gracefully, e.g., logging
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public boolean registerUser(String username, String password) {
        if (users.containsKey(username)) {
            return false; // User already exists
        }
        String hashedPassword = hashPassword(password);
        User newUser = new User(username, hashedPassword);
        users.put(username, newUser);
        saveUsers();
        return true;
    }

    public User loginUser(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getHashedPassword().equals(hashPassword(password))) {
            return user; // Login successful
        }
        return null; // Login failed
    }

    @SuppressWarnings("unchecked")
    private Map<String, User> loadUsers() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USER_DATA_FILE))) {
            Object data = ois.readObject();
            if (data instanceof HashMap) {
                return (HashMap<String, User>) data;
            }
        } catch (FileNotFoundException e) {
            // File not found, which is okay on first run
            return new HashMap<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading user data: " + e.getMessage());
            // In a real app, might want to backup old file and start fresh, or halt
        }
        return new HashMap<>(); // Return empty map if loading fails or file doesn't exist
    }

    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_DATA_FILE))) {
            oos.writeObject(this.users);
        } catch (IOException e) {
            System.err.println("Error saving user data: " + e.getMessage());
            // Handle error appropriately
        }
    }

    // Method to check if a user exists, could be useful for UI validation
    public boolean userExists(String username) {
        return users.containsKey(username);
    }
}