package com.raymondweng;

import com.raymondweng.listeners.EventListener;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static Main main;

    public final Connection connection;

    public static void main(String[] args) throws IOException {
        System.out.print("Input the token: ");
        main = new Main(new BufferedReader(new InputStreamReader(System.in)).readLine());
    }

    public Main(String token) {
        // create or connect to database
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        boolean databaseExists = new File("./database/data.db").exists();
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
            if (!databaseExists) {
                Statement stmt = connection.createStatement();
                stmt.executeUpdate("CREATE TABLE PLAYER" +
                        "(DISCORD_ID INTEGER PRIMARY KEY NOT NULL ," +
                        "POINT INTEGER NOT NULL DEFAULT 1000," +
                        "DATE_CREATED DATE NOT NULL," +
                        "GAME_PLAYING INTEGER DEFAULT NULL," +
                        "PLAYING_RED BOOLEAN DEFAULT NULL)");
                stmt.executeUpdate("CREATE TABLE GAME" +
                        "(ID INTEGER PRIMARY KEY AUTOINCREMENT ," +
                        "RED_PLAYER INTEGER NOT NULL," +
                        "BLACK_PLAYER INTEGER NOT NULL," +
                        "TURN_RED BOOLEAN NOT NULL DEFAULT TRUE," +
                        "PROCESS TEXT NOT NULL DEFAULT ''," +
                        "PLAYING BOOLEAN NOT NULL DEFAULT TRUE, " +
                        "RED_TIMELEFT INTEGER NOT NULL DEFAULT 600, " +
                        "BLACK_TIMELEFT INTEGER NOT NULL DEFAULT 600, " +
                        "LAST_MOVE INTEGER NOT NULL)");
                stmt.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // connect to discord
        JDABuilder jdaBuilder = JDABuilder.createDefault(token);
        jdaBuilder
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES)
                .addEventListeners(new EventListener());
        jdaBuilder.build();
    }
}