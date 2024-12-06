package com.raymondweng.types;

public record Position(int x, int y) {

    public String toString() {
        return String.valueOf(x) + y;
    }
}
