package com.utils.analyzer.utils;

import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

@Getter
public enum Connections {
    ORACLE {
        @Override
        public Connection DB() {
            return ORACLE.connection("test", "test", "jdbc:oracle:thin:@testbd:1521:tablename");
        }
    },

    POSTGRE {
        @Override
        public Connection DB() {
            return POSTGRE.connection("test", "test", "jdbc:postgresql://0.0.0.0:5432/tablename");
        }
    };


    @SneakyThrows
    public Connection connection(String user, String passord, String jdbc) {
        Properties p = new Properties();
        p.setProperty("user", user);
        p.setProperty("password", passord);
        return DriverManager.getConnection(jdbc, p);
    }

    public abstract Connection DB();
}
