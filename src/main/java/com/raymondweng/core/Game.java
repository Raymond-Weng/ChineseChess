package com.raymondweng.core;

import com.raymondweng.Main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class Game {
    private static HashMap<Integer, Game> games = new HashMap<Integer, Game>();

    private Game(int id){
        synchronized (Main.main.connection){
            try {
                Statement stmt = Main.main.connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT ID FROM GAME WHERE ID = " + id);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Game getGame(int id){
        if(games.containsKey(id)){
            return games.get(id);
        }
        return new Game(id);
    }

    public void startGame(int black, int red){
        synchronized (Main.main.connection){
            try {
                Statement stmt = Main.main.connection.createStatement();
                stmt.executeUpdate("INSERT INTO GAME (RED_PLAYER, BLACK_PLAYER, LAST_MOVE)  VALUES (" + red + "," + black + ", STRFTIME('%s', 'now'))");
                ResultSet rs = stmt.executeQuery("SELECT ID FROM GAME WHERE BLACK_PLAYER = " + black);
                rs.next();
                String id = rs.getString("ID");
                rs.close();
                stmt.executeUpdate("UPDATE PLAYER SET GAME_PLAYING = " + id + " WHERE DISCORD_ID = " + red + " OR DISCORD_ID = " + black);
                stmt.executeUpdate("UPDATE PLAYER SET PLAYING_RED = TRUE WHERE DISCORD_ID = " + red);
                stmt.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
