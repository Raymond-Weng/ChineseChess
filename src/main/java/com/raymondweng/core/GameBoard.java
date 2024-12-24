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

    public String move(Position pos, Move move, boolean redPlaying) {
        // move one's piece?
        if (board[pos.x()][pos.y()] == null) {
            return "不能移動空白";
        } else if (board[pos.x()][pos.y()].isRed != redPlaying) {
            return "不能移動對方的棋子";
        }

        // legal move?
        Position dist = pos.move(move);
        if (board[dist.x()][dist.y()] != null && board[dist.x()][dist.y()].isRed == redPlaying) {
            return "不能吃自己的棋子";
        }
        if (illegalMove(board, pos, move)) {
            return "這個棋子不能這樣移動";
        }

        // checked?
        Piece[][] nb = moveStep(board, pos, move);
        if (check(nb, !redPlaying)) {
            return "你被將軍了";
        }

        // checkmate?
        if (check(nb, redPlaying)) checkmate:{
            for (int i = 0; i <= 8; i++) {
                for (int r = 0; r <= 9; r++) {
                    Position p = new Position(i, r);
                    if (board[i][r] != null && board[i][r].isRed != redPlaying) {
                        for (Move m : board[i][r].moves) {
                            if (!check(moveStep(nb, p, m), redPlaying)) {
                                break checkmate;
                            }
                        }
                    }
                }
            }
            return "checkmate"; // THIS STRING IS RELATED TO onEvent() case %move in EventListener.java
        }

        // TODO update board

        return null;
    }

    private boolean illegalMove(Piece[][] b, Position pos, Move move) {
        boolean badMove = true;
        for (Move m : b[pos.x()][pos.y()].moves) {
            if (move == m) {
                if (m.pass) {
                    if (move.x == 0) {
                        int passed = 0;
                        for (int i = 1; i <= move.y; i++) {
                            if (b[pos.x()][pos.y() + i] != null) {
                                passed++;
                            }
                        }
                        badMove = (passed == 0 || passed == 2);
                    } else if (move.y == 0) {
                        int passed = 0;
                        for (int i = 1; i <= move.x; i++) {
                            if (b[pos.x() + i][pos.y()] != null) {
                                passed++;
                            }
                        }
                        badMove = (passed == 0 || passed == 2);
                    }
                } else if (m.block == null) {
                    badMove = false;
                } else {
                    badMove = b[pos.move(m.block).x()][pos.move(m.block).y()] != null;
                }
                break;
            }
        }
        return badMove;
    }

    private boolean check(Piece[][] nb, boolean red) {
        Position king = null;
        for (int i = 3; i <= 5; i++) {
            for (int r = (red ? 7 : 0); r <= (red ? 9 : 2); r++) {
                if (nb[i][r].type == 0) {
                    king = new Position(i, r);
                    break;
                }
            }
            if (king != null) {
                break;
            }
        }

        for (int i = 0; i <= 8; i++) {
            for (int r = 0; r <= 9; r++) {
                if (nb[i][r] != null && nb[i][r].isRed == red) {
                    Position pos1 = new Position(i, r);
                    for (Move m : nb[i][r].moves) {
                        Position pos2 = pos1.move(m);
                        if (pos2.equals(king) && pos2.inBoard() && !illegalMove(nb, pos1, m)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    // move WITHOUT checking
    private Piece[][] moveStep(Piece[][] board, Position pos, Move move) {
        Piece[][] nb = board.clone();
        Position dist = pos.move(move);
        nb[dist.x()][dist.y()] = nb[pos.x()][pos.y()];
        nb[pos.x()][pos.y()] = null;
        return nb;
    }
}
