package org.cis1200.blockade;

/**
 * This file holds an enumeration called Direction, which is used in
 * GameObj.java to indicate the direction an object should move after it
 * collides with another object.
 *
 * One can make a method take in or return a Direction (thus limiting the
 * possible cases of the input to the enum cases)
 */
public enum Direction {
    UP, DOWN, LEFT, RIGHT;

    public Direction opposite() {
        return switch (this) {
            case UP -> Direction.DOWN;
            case DOWN -> Direction.UP;
            case LEFT -> Direction.RIGHT;
            case RIGHT -> Direction.LEFT;
        };
    }

    public Direction[] nonOpposite() {
        return switch (this) {
            case UP -> new Direction[] {
                Direction.LEFT, Direction.UP, Direction.RIGHT
            };
            case DOWN -> new Direction[] {
                Direction.LEFT, Direction.DOWN, Direction.RIGHT
            };
            case LEFT -> new Direction[] {
                Direction.LEFT, Direction.UP, Direction.DOWN
            };
            case RIGHT -> new Direction[] {
                Direction.DOWN, Direction.UP, Direction.RIGHT
            };
        };
    }

}
