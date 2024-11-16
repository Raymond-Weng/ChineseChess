package com.raymondweng.handler;

import com.raymondweng.core.Board;
import com.raymondweng.core.Position;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageFilesHandler implements Runnable {
    volatile boolean running = true;

    public final Map<String, Board> boards = new HashMap<String, Board>();

    public Board getBoard(Position[][][] positions, String id) throws IOException {
        synchronized (this) {
            if(boards.containsKey(id)){
                boards.get(id).extendLife();
            }else{
                boards.put(id, new Board(positions, id));
            }
        }

        return boards.get(id);
    }

    @Override
    public void run() {
        long nextUpdate = System.currentTimeMillis() + 1000;
        while (running) {
            if(System.currentTimeMillis() > nextUpdate){
                nextUpdate += 1000;
                synchronized (this){
                    for (Board board : boards.values()) {
                        if(System.currentTimeMillis() > board.getDeadTime()){
                            board.remove();
                            boards.remove(board.id);
                        }
                    }
                }
            }
        }
    }

    public void stop() {
        running = false;
        for (Board board : boards.values()) {
            board.remove();
        }
    }
}
