package com.raymondweng.types;

public record Position(int x, int y) {

    public String toString() {
        return String.valueOf(x) + y;
    }

    public Position move(Move move) {
        return new Position(x + move.x, y + move.y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Position p) {
            return p.x == x && p.y == y;
        }
        return false;
    }

    public boolean inBoard() {
        return x >= 0 && x < 8 && y >= 0 && y < 9;
    }
}
