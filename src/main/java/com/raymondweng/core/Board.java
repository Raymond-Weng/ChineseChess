package com.raymondweng.core;

import com.raymondweng.types.Position;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Board {
    public final String id;
    private final Position[][][] positions;
    private final String[][] name = {
            {"帥", "仕", "相", "馬", "車", "炮", "兵"},
            {"將", "士", "象", "馬", "車", "炮", "卒"}
    };
    // [color][type][number]
    // color: 0->red, 1->black
    // number:
    // type0: 0
    // type1~5: 0~1 (0 is the left one in the beginning
    // type6: 0~4 (from left to right in the beginning)

    private File file;
    private long deadTime;

    public Board(Position[][][] positions, String id) throws IOException {
        // setup
        this.id = id;
        this.positions = positions;

        // generate image
        file = new File("./maps/" + id + ".png");
        BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.drawImage(ImageIO.read(new File("./maps/board.png")), 0, 0, null);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 7; j++) {
                if (j == 0) {
                    drawPiece(g2d, i, j, 0);
                } else {
                    for (int k = 0; k < (j == 6 ? 5 : 2); k++) {
                        drawPiece(g2d, i, j, k);
                    }
                }
            }
        }
        g2d.dispose();
        ImageIO.write(image, "png", file);

        // give 10 sec to upload the image
        extendLife();
    }

    public File getImage() throws IOException {
        return file;
    }

    public void drawPiece(Graphics2D graphics2D, int color, int type, int number) {
        graphics2D.setColor(color == 0 ? Color.red : Color.black);
        graphics2D.setStroke(new BasicStroke(10));
        graphics2D.drawOval(55 + positions[color][type][number].x() * 100,
                5 + positions[color][type][number].y() * 100,
                90,
                90);
        graphics2D.setFont(new Font("ITALIC", Font.BOLD, 70));
        graphics2D.drawString(name[color][type],
                55 + positions[color][type][number].x() * 100 + 10,
                5 + positions[color][type][number].y() * 100 + 70);
    }

    public void extendLife() {
        this.deadTime = System.currentTimeMillis() + 10000;
    }

    public long getDeadTime() {
        return deadTime;
    }

    public void remove() {
        file.delete();
    }
}
