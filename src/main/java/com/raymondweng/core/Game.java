package com.raymondweng.core;

import java.sql.*;
import java.util.HashMap;

public class Game {
    private static HashMap<Integer, Game> games = new HashMap<Integer, Game>();

    private final int id;
    private final int red;
    private final int black;
    private final boolean playing;
    private int blackTime = 600;
    private int redTime = 600;
    private int lastMove = -1;
    private boolean redPlaying = true;


    private Game(int id, int red, int black, boolean playing) {
        this.id = id;
        this.red = red;
        this.black = black;
        this.playing = playing;
    }

    public static Game getGame(int id) {
        if (games.containsKey(id)) {
            return games.get(id);
        }
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/player.db");
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM games WHERE ID = " + id);
            rs.next();
            return new Game(id, rs.getInt("RED_PLAYER"), rs.getInt("BLACK_PLAYER"), rs.getBoolean("PLAYING"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Game startGame(int black, int red) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("INSERT INTO GAME (RED_PLAYER, BLACK_PLAYER, LAST_MOVE)  VALUES (" + red + "," + black + ", STRFTIME('%s', 'now'))");
            ResultSet rs = stmt.executeQuery("SELECT ID FROM GAME WHERE BLACK_PLAYER = " + black);
            rs.next();
            String id = rs.getString("ID");
            rs.close();
            stmt.executeUpdate("UPDATE PLAYER SET GAME_PLAYING = " + id + " WHERE DISCORD_ID = " + red + " OR DISCORD_ID = " + black);
            stmt.executeUpdate("UPDATE PLAYER SET PLAYING_RED = TRUE WHERE DISCORD_ID = " + red);
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static void update(){
        Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
        for (Game game : games.values()) {
            if(game.lastMove != -1 && game.){

            }
        }
    }
}
