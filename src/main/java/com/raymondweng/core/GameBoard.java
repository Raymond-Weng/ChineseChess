package com.raymondweng.core;

import com.raymondweng.types.Move;
import com.raymondweng.types.Position;

import java.util.ArrayList;

class Piece {
    private volatile static boolean setup = false;
    private static final Piece[][] pieces = new Piece[2][8];

    public static Piece getPiece(boolean isRed, int type) {
        if (!setup) {
            setup = true;
            synchronized (pieces) {
                for (int i = 0; i < 8; i++) {
                    pieces[0][i] = new Piece(false, type);
                    pieces[1][i] = new Piece(true, type);
                }
            }
        }
        return pieces[isRed ? 1 : 0][type];
    }

    boolean isRed;
    public final int type;
    public final ArrayList<Move> moves = new ArrayList<>();

    private Piece(boolean isRed, int type) {
        this.isRed = isRed;
        this.type = type;
        switch (type) {
            case 0:
                moves.add(new Move(-1, 0));
                moves.add(new Move(1, 0));
                moves.add(new Move(0, -1));
                moves.add(new Move(0, 1));
                break;
            case 1:
                moves.add(new Move(-1, 1));
                moves.add(new Move(1, -1));
                moves.add(new Move(1, 1));
                moves.add(new Move(-1, -1));
                break;
            case 2:
                moves.add(new Move(2, 2).addBlock(new Move(1, 1)));
                moves.add(new Move(2, -2).addBlock(new Move(1, -1)));
                moves.add(new Move(-2, 2).addBlock(new Move(-1, 1)));
                moves.add(new Move(-2, -2).addBlock(new Move(-1, -1)));
                break;
            case 3:
                moves.add(new Move(1, 2).addBlock(new Move(0, 1)));
                moves.add(new Move(2, 1).addBlock(new Move(1, 0)));
                moves.add(new Move(-1, 2).addBlock(new Move(0, 1)));
                moves.add(new Move(-2, 1).addBlock(new Move(-1, 0)));
                moves.add(new Move(1, -2).addBlock(new Move(0, -1)));
                moves.add(new Move(2, -1).addBlock(new Move(1, 0)));
                moves.add(new Move(-1, -2).addBlock(new Move(0, -1)));
                moves.add(new Move(-2, -1).addBlock(new Move(-1, 0)));
                break;
            case 4:
            case 5:
                for (int x = -8; x <= 8; x++) {
                    moves.add(new Move(x, 0, (type == 5)));
                }
                for (int y = -9; y <= 8; y++) {
                    moves.add(new Move(0, y, (type == 5)));
                }
                break;
            case 7:
                // crossed river
                moves.add(new Move(-1, 0));
                moves.add(new Move(1, 0));
            case 6:
                moves.add(new Move(0, (isRed ? -1 : 1)).addBlock(new Move(0, 1)));
                break;
        }
    }
}

public class GameBoard {
    private volatile Position positions[][][] = new Position[2][7][5];
    private Piece[][] board = new Piece[9][10];

    public GameBoard() {
        // positions[][][] setup
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

        // board[][] setup
        for (int i = 0; i < 9; i++) {
            board[i][0] = Piece.getPiece(true, Math.abs(4 - i));
        }
        for (int i = 0; i < 9; i++) {
            board[i][1] = null;
        }
        for (int i = 0; i < 9; i++) {
            if (i == 1 || i == 7) {
                board[i][2] = Piece.getPiece(true, 5);
            } else {
                board[i][2] = null;
            }
        }
        for (int i = 0; i < 9; i++) {
            if (i % 2 == 0) {
                board[i][3] = Piece.getPiece(true, 6);
            } else {
                board[i][3] = null;
            }
        }
        for (int i = 0; i < 9; i++) {
            board[i][4] = null;
        }
        for (int i = 0; i < 9; i++) {
            board[i][5] = null;
        }
        for (int i = 0; i < 9; i++) {
            if (i % 2 == 0) {
                board[i][6] = Piece.getPiece(false, 6);
            } else {
                board[i][6] = null;
            }
        }
        for (int i = 0; i < 9; i++) {
            if (i == 1 || i == 7) {
                board[i][7] = Piece.getPiece(true, 5);
            } else {
                board[i][7] = null;
            }
        }
        for (int i = 0; i < 9; i++) {
            board[i][8] = null;
        }
        for (int i = 0; i < 9; i++) {
            board[i][9] = Piece.getPiece(true, Math.abs(4 - i));
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

    public Position[][][] getPositions() {
        return positions;
    }

    public GameBoard move(Position pos, Move move) {
        //TODO
        return null;
    }

    public boolean checked() {
        //TODO
        return false;
    }
}
