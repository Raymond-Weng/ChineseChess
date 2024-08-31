package com.raymondweng;

import com.raymondweng.core.Game;
import com.raymondweng.listeners.EventListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;

public class Main {
    public static Main main;

    public final JDA jda;

    public static void main(String[] args) {
        System.out.print("Input the token: ");
        try {
            main = new Main(new BufferedReader(new InputStreamReader(System.in)).readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int cnt = 0;
        while(true){
            Game.update();
            if(cnt == 60){
                try {
                    Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM PLAYER");
                    Main.main.jda.getVoiceChannelById("1279333408010539052").getManager().setName("已註冊人數：" + resultSet.getString(1)).queue();
                    Main.main.jda.getVoiceChannelById("1279333695265833001").getManager().setName("對戰中對局數：" + Game.playingGamesCount()).queue();
                    cnt = 0;
                    resultSet.close();
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }else{
                cnt++;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
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
            if (databaseExists) {
                Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
                Statement stmt = connection.createStatement();
                stmt.executeUpdate("DELETE FROM GAME WHERE PLAYING = TRUE");
                stmt.close();
                connection.close();
            } else {
                Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
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
                        "LAST_MOVE INTEGER NOT NULL," +
                        "END_REASON TEXT DEFAULT NULL)");
                stmt.close();
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // connect to discord
        JDABuilder jdaBuilder = JDABuilder.createDefault(token);
        jdaBuilder
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES)
                .addEventListeners(new EventListener());
        jda = jdaBuilder.build();
    }
}