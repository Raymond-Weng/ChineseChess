package com.raymondweng.handler;

import com.raymondweng.core.Board;
import com.raymondweng.types.Position;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageFilesHandler implements Runnable {
    volatile boolean running = true;

    public final Map<String, Board> boards = new HashMap<String, Board>();

    public Board getBoard(Position[][][] positions, String id) throws IOException {
        synchronized (this) {
            if (boards.containsKey(id)) {
                boards.get(id).extendLife();
            } else {
                boards.put(id, new Board(positions, id));
            }
        }

        return boards.get(id);
    }

    @Override
    public void run() {
        while (running) {
            synchronized (this) {
                for (Board board : boards.values()) {
                    if (System.currentTimeMillis() > board.getDeadTime()) {
                        board.remove();
                        boards.remove(board.id);
                    }
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stop() {
        System.out.println("Stop command received, server stopping.");
        running = false;
        for (Board board : boards.values()) {
            board.remove();
        }
    }
}
