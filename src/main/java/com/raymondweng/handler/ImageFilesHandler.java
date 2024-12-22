package com.raymondweng.handler;

import com.raymondweng.core.BoardImage;
import com.raymondweng.types.Position;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageFilesHandler implements Runnable {
    volatile boolean running = true;

    public final Map<String, BoardImage> boards = new HashMap<String, BoardImage>();

    public BoardImage getBoard(Position[][][] positions, String id) throws IOException {
        synchronized (this) {
            if (boards.containsKey(id)) {
                boards.get(id).extendLife();
            } else {
                boards.put(id, new BoardImage(positions, id));
            }
        }

        return boards.get(id);
    }

    @Override
    public void run() {
        while (running) {
            synchronized (this) {
                for (BoardImage boardImage : boards.values()) {
                    if (System.currentTimeMillis() > boardImage.getDeadTime()) {
                        boardImage.remove();
                        boards.remove(boardImage.id);
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
        for (BoardImage boardImage : boards.values()) {
            boardImage.remove();
        }
    }
}
