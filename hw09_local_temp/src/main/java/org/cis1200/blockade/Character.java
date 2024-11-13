package org.cis1200.blockade;

import java.awt.*;

public class Character {
    final private Color color;
    final private String name;
    private Coordinate current;
    private Direction currentDirection;

    public Character(Coordinate start, Color color, String name) {
        this(start, color, name, null);
    }

    public Character(Coordinate current, Color color, String name, Direction direction) {
        this.current = current;
        this.color = color;
        this.name = name;
        this.currentDirection = direction;
    }

    public Coordinate getCurrent() {
        return current;
    }

    public void setCurrentDirection(Direction currentDirection) {
        this.currentDirection = currentDirection;
    }

    public void undoMove() {
        forceMove(currentDirection.opposite());
    }

    private void forceMove(Direction d) {
        current = switch (d) {
            case UP -> new Coordinate(current.x(), current.y() - 1);
            case DOWN -> new Coordinate(current.x(), current.y() + 1);
            case LEFT -> new Coordinate(current.x() - 1, current.y());
            case RIGHT -> new Coordinate(current.x() + 1, current.y());
        };
    }

    public Coordinate move(Direction d) {
        if (currentDirection != null && currentDirection == d.opposite()) {
            d = currentDirection;
        }
        forceMove(d);
        currentDirection = d;
        return current;
    }

    // private Direction opposite(Direction d) {
    // return switch (d) {
    // case UP -> Direction.DOWN;
    // case DOWN -> Direction.UP;
    // case LEFT -> Direction.RIGHT;
    // case RIGHT -> Direction.LEFT;
    // };
    // }

    public boolean isAlive(int[][] board) {
        if (current.y() < board.length && current.y() >= 0
                && current.x() < board[0].length && current.x() >= 0) {
            return (board[current.y()][current.x()] == 0);
        } else {
            return false;
        }
    }

    public Color getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    @Override
    public String toString() {
        return color.getRed() + "," + color.getGreen() + "," + color.getBlue() +
                "," + name +
                "," + current +
                "," + currentDirection;
    }
}
