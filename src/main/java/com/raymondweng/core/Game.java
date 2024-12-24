package com.raymondweng.core;

import com.raymondweng.Main;
import com.raymondweng.types.Move;
import com.raymondweng.types.Pair;
import com.raymondweng.types.Position;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;

public class Game {
    private static final HashMap<String, Game> games = new HashMap<>();

    private final String id;
    private final String red;
    private final String black;
    private final boolean playing;
    private volatile int blackTime = 600;
    private volatile int redTime = 600;
    private volatile int lastMove = -1;
    private volatile boolean redPlaying = true;
    private volatile GameBoard gameBoard;

    private Game(String id, String red, String black, int time, boolean playing) {
        this.id = id;
        this.red = red;
        this.black = black;
        this.playing = playing;
        this.lastMove = time;
        this.gameBoard = new GameBoard();
    }

    public static int playingGamesCount() {
        return games.size();
    }

    public static Game getGame(int id) {
        if (games.containsKey(id)) {
            return games.get(id);
        }
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM GAME WHERE ID = " + id);
            if (rs.next()) {
                Game g = new Game(
                        rs.getString("ID"),
                        rs.getString("RED_PLAYER"),
                        rs.getString("BLACK_PLAYER"),
                        -1,
                        rs.getBoolean("PLAYING"));
                rs.close();
                stmt.close();
                connection.close();
                return g;
            } else {
                rs.close();
                stmt.close();
                connection.close();
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Game startGame(String red, String black) {
        String id;
        int time = -1;
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("INSERT INTO GAME (RED_PLAYER, BLACK_PLAYER)  VALUES (" + red + "," + black + ")");
            ResultSet rs = stmt.executeQuery("SELECT ID FROM GAME WHERE BLACK_PLAYER = " + black);
            rs.next();
            id = rs.getString("ID");
            rs.close();
            stmt.executeUpdate("UPDATE PLAYER SET GAME_PLAYING = " + id + " WHERE DISCORD_ID = " + red + " OR DISCORD_ID = " + black);
            stmt.executeUpdate("UPDATE PLAYER SET PLAYING_RED = TRUE WHERE DISCORD_ID = " + red);
            stmt.executeUpdate("UPDATE PLAYER SET PLAYING_RED = FALSE WHERE DISCORD_ID = " + black);
            rs = stmt.executeQuery("SELECT STRFTIME('%s', 'now') AS T");
            rs.next();
            time = rs.getInt("T");
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        games.put(id, new Game(id, red, black, time, true));
        return games.get(id);
    }

    public static void update() {
        synchronized (games) {
            try {
                Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT STRFTIME('%s', 'now') AS T");
                rs.next();
                int time = rs.getInt("T");
                rs.close();
                stmt.close();
                for (Game game : games.values()) {
                    if (time - game.lastMove > ((game.redPlaying ? game.redTime : game.blackTime) >= 0 ? 180 : 60)) {
                        game.endGame(!game.redPlaying, game.redPlaying, "用盡步時");
                    }
                }
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static boolean isLegalPosition(String s) {
        return s.matches("[A-I][0-9][A-I][0-9]");
    }

    public static Pair<Position, Position> stringToPosition(String pos) {
        if (!isLegalPosition(pos)) {
            return null;
        }
        return new Pair<>(
                new Position((pos.charAt(0) - 'A'), (pos.charAt(1) - '0')),
                new Position((pos.charAt(2) - 'A'), (pos.charAt(3) - '0'))
        );
    }

    public void endGame(boolean redWin, boolean blackWin, String reason) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT BLACK_PLAYER, RED_PLAYER FROM GAME WHERE ID = '" + id + "'");
            String b = rs.getString("BLACK_PLAYER");
            String r = rs.getString("RED_PLAYER");
            rs.close();
            rs = stmt.executeQuery("SELECT POINT FROM PLAYER WHERE DISCORD_ID = '" + b + "'");
            rs.next();
            int bp = rs.getInt("POINT");
            rs.close();
            rs = stmt.executeQuery("SELECT POINT FROM PLAYER WHERE DISCORD_ID = '" + r + "'");
            rs.next();
            int rp = rs.getInt("POINT");
            int db = (int) (16 * ((redWin ? 0 : (blackWin ? 1 : 0.5)) - (1 / (1 + Math.pow(10, (rp - bp) / 400d)))));
            int dr = (int) (16 * ((redWin ? 1 : (blackWin ? 0 : 0.5)) - (1 / (1 + Math.pow(10, (bp - rp) / 400d)))));
            rs.close();
            stmt.executeUpdate("UPDATE PLAYER SET GAME_PLAYING = NULL, PLAYING_RED = NULL WHERE DISCORD_ID = " + red + " OR DISCORD_ID = " + black);
            stmt.executeUpdate("UPDATE PLAYER SET POINT = POINT + " + db + " WHERE DISCORD_ID = " + black);
            stmt.executeUpdate("UPDATE PLAYER SET POINT = POINT + " + dr + " WHERE DISCORD_ID = " + red);
            stmt.executeUpdate("UPDATE GAME SET PLAYING = FALSE, END_REASON = '" + reason + "' WHERE ID = " + id);
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        games.remove(this.id);
    }

    public String move(String action){
        Pair<Position, Position> positionPair = stringToPosition(action);
        return gameBoard.move(positionPair.first(), new Move(positionPair.first(), positionPair.second()), redPlaying);
    }

    public String getMessage() {
        //TODO message
        return "The message haven't been written yet";
    }

    public File toImage() throws IOException {
        return Main.main.getImageFilesHandler().getBoard(gameBoard.getPositions(), toString()).getImage();
    }

    public String toString() {
        return gameBoard.toString();
    }

    public boolean isOnesTurn(String id){
        return id.equals(redPlaying ? red : black);
    }
}
