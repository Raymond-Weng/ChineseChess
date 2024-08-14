package com.raymondweng.core;

import com.raymondweng.Main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class Game {
    private static HashMap<Integer, Game> games = new HashMap<Integer, Game>();

    boolean playing;

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

    }
}
