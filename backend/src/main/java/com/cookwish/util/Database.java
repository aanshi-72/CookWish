package com.cookwish.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public final class Database {
    private static final Properties PROPERTIES = new Properties();
    static {
        try {
            InputStream input = Database.class.getClassLoader().getResourceAsStream("db.properties");
            if (input != null) {
                PROPERTIES.load(input);
                input.close();
            }

            Class.forName("com.mysql.cj.jdbc.Driver");

        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Database() {}

    public static Connection getConnection() throws Exception {

        String url = System.getenv("DB_URL");
        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");

        if (url == null || url.isEmpty()) {
            url = PROPERTIES.getProperty("db.url");
        }

        if (username == null || username.isEmpty()) {
            username = PROPERTIES.getProperty("db.username");
        }

        if (password == null || password.isEmpty()) {
            password = PROPERTIES.getProperty("db.password");
        }

        if (url == null || username == null || password == null) {
            throw new IllegalStateException(
                    "Database configuration is missing. Set DB_URL, DB_USERNAME and DB_PASSWORD."
            );
        }

        return DriverManager.getConnection(url, username, password);
    }
}
