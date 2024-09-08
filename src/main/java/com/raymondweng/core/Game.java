package com.raymondweng.core;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;

public class Game {
    private static final HashMap<Integer, Game> games = new HashMap<Integer, Game>();

    private final int id;
    private final String red;
    private final String black;
    private final boolean playing;
    private int blackTime = 600;
    private int redTime = 600;
    private int lastMove = -1;
    private boolean redPlaying = true;

    private Position positions[][][] = new Position[2][7][5];
    // [color][type][number]
    // color: 0->red, 1->black
    // types:
    // 0->帥
    // 1->士
    // 2->象
    // 3->馬
    // 4->車
    // 5->炮
    // 6->兵
    // number:
    // type0: 0
    // type1~5: 0~1 (0 is the left one in the beginning
    // type6: 0~4 (from left to right in the beginning)


    private Game(int id, String red, String black, boolean playing) {
        this.id = id;
        this.red = red;
        this.black = black;
        this.playing = playing;

        for (int i = 0; i < 2; i++) {
            positions[i][0][0] = new Position(4, i * 9);
            for (int r = 0; r < 2; r++) {
                positions[i][1][r] = new Position(3 + (r * 2), i * 9);
                positions[i][2][r] = new Position(2 + (r * 4), i * 9);
                positions[i][3][r] = new Position(1 + (r * 6), i * 9);
                positions[i][4][r] = new Position(r * 8, i * 9);
                positions[i][5][r] = new Position(1 + (r * 6), 2 + (i * 5));
            }
            for (int r = 0; r < 5; r++) {
                positions[i][6][r] = new Position(r * 2, 3 + (i * 3));
            }
        }
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
                        rs.getInt("ID"),
                        rs.getString("RED_PLAYER"),
                        rs.getString("BLACK_PLAYER"),
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
        int id;
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("INSERT INTO GAME (RED_PLAYER, BLACK_PLAYER, LAST_MOVE)  VALUES (" + red + "," + black + ", STRFTIME('%s', 'now'))");
            ResultSet rs = stmt.executeQuery("SELECT ID FROM GAME WHERE BLACK_PLAYER = " + black);
            rs.next();
            id = rs.getInt("ID");
            rs.close();
            stmt.executeUpdate("UPDATE PLAYER SET GAME_PLAYING = " + id + " WHERE DISCORD_ID = " + red + " OR DISCORD_ID = " + black);
            stmt.executeUpdate("UPDATE PLAYER SET PLAYING_RED = TRUE WHERE DISCORD_ID = " + red);
            stmt.executeUpdate("UPDATE PLAYER SET PLAYING_RED = FALSE WHERE DISCORD_ID = " + black);
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new Game(id, red, black, true);
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getMessage(){
        //TODO message
        return "The message haven't been written yet";
    }

    public File toImage() throws IOException {
        File file = new File("./maps/" + this + ".png");
        if (file.exists()) {
            return file;
        } else {
            BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            g2d.drawImage(ImageIO.read(new File("./maps/board.png")), 0, 0, null);
            //TODO draw pieces
            g2d.dispose();
            ImageIO.write(image, "png", file);
            return file;
        }
    }

    public String toString() {
        StringBuilder res = new StringBuilder();

        for (int i = 0; i < 2; i++) {
            res.append(posToString(positions[i][0][0]));
            for (int r = 0; r < 2; r++) {
                res.append(posToString(positions[i][1][r]));
                res.append(posToString(positions[i][2][r]));
                res.append(posToString(positions[i][3][r]));
                res.append(posToString(positions[i][4][r]));
                res.append(posToString(positions[i][5][r]));
            }
            for (int r = 0; r < 5; r++) {
                res.append(posToString(positions[i][6][r]));
            }
        }

        return res.toString();
    }

    private String posToString(Position pos) {
        return pos == null ? "n" : pos.toString();
    }
}
