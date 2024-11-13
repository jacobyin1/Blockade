package org.cis1200.blockade;

public record Coordinate(int x, int y) {
    public int dist(Coordinate c) {
        return Math.abs(c.x - x) + Math.abs(c.y - y) +
                Math.max(Math.abs(c.x - x), Math.abs(c.y - y));
    }

    @Override
    public String toString() {
        return x + "," + y;
    }
}
