package com.raymondweng.types;

public class Move {
    public Move block = null;
    public final int x;
    public final int y;
    public boolean pass = false;

    public Move(int x, int y) {
        this(x, y, false);
    }

    public Move(int x, int y, boolean pass) {
        this.x = x;
        this.y = y;
        this.pass = pass;
    }

    public Move addBlock(Move move){
        this.block = move;
        return this;
    }

    @Override
    public boolean equals(Object move){
        if(move instanceof Move m){
            return x == m.x && y == m.y;
        }else{
            return false;
        }
    }
}
