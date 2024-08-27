package com.raymondweng.core;

import java.sql.*;
import java.util.HashMap;

public class Game {
    private static final HashMap<Integer, Game> games = new HashMap<Integer, Game>();

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
            if(rs.next()) {
                Game g = new Game(
                        rs.getInt("ID"),
                        rs.getInt("RED_PLAYER"),
                        rs.getInt("BLACK_PLAYER"),
                        rs.getBoolean("PLAYING"));
                rs.close();
                stmt.close();
                connection.close();
                return g;
            }else{
                rs.close();
                stmt.close();
                connection.close();
                return null;
            }
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
            stmt.executeUpdate("UPDATE PLAYER SET PLAYING_RED = FALSE WHERE DISCORD_ID = " + black);
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static void update() {
        synchronized (games) {
            try {
                Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT STRFTIME('%s', 'now') AS T");
                int time = rs.getInt("T");
                rs.close();
                stmt.close();
                for (Game game : games.values()) {
                    if (time - game.lastMove > (game.redPlaying ? game.redTime : game.blackTime)) {
                        if ((game.redPlaying ? game.redTime : game.blackTime) < 0) {
                            if (time - game.lastMove > 30) {
                                game.endGame(!game.redPlaying, game.redPlaying);
                            }
                        }
                    }
                }
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void endGame(boolean redWin, boolean blackWin) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT BLACK_PLAYER, RED_PLAYER FROM GAME WHERE ID = '" + id + "'");
            int black = rs.getInt("BLACK_PLAYER");
            int red = rs.getInt("RED_PLAYER");
            int db = (int) (16 * ((redWin ? 0 : (blackWin ? 1 : 0.5)) - (1 / (1 + Math.pow(10, (red - black) / 400d)))));
            int dr = (int) (16 * ((redWin ? 1 : (blackWin ? 0 : 0.5)) - (1 / (1 + Math.pow(10, (black - red) / 400d)))));
            black += db;
            red += dr;
            rs.close();
            stmt.executeUpdate("UPDATE PLAYER SET GAME_PLAYING = NULL, PLAYING_RED = NULL WHERE DISCORD_ID = " + red + " OR DISCORD_ID = " + black);
            stmt.executeUpdate("UPDATE PLAYER SET POINT = POINT + " + db + " WHERE DISCORD_ID = " + black);
            stmt.executeUpdate("UPDATE PLAYER SET POINT = POINT + " + dr + " WHERE DISCORD_ID = " + red);
            stmt.executeUpdate("UPDATE GAME SET GAME_PLAYING = FALSE WHERE ID = " + id);
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
