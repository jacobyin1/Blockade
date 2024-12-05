package org.cis1200.blockade;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

public class GameState {

    private BotNet botNet;
    private int[][] board;
    private Character bot;
    private Character human;

    private State state;

    final private int searchDepth;

    public enum State {
        Start,
        Playing,
        EndPlayerWon,
        EndTie,
        EndPlayerLost
    }

    public GameState(int dimX, int dimY, String username) {
        board = new int[dimY][dimX];
        Coordinate start1 = new Coordinate(dimX * 3 / 4, dimY * 3 / 4);
        Coordinate start2 = new Coordinate(dimX / 4, dimY / 4);
        bot = new Character(start1, Color.RED, "Bot 1");
        human = new Character(start2, Color.GREEN, username);
        fill(start1, 2);
        fill(start2, 1);
        state = State.Start;
        searchDepth = 7;
        botNet = new BotNet();
    }

    public GameState(int dimX, int dimY, String username, int[][] board, Coordinate start1, Coordinate start2) {
        this.board = board;
        bot = new Character(start1, Color.RED, "Bot 1");
        human = new Character(start2, Color.GREEN, username);
        fill(start1, 2);
        fill(start2, 1);
        state = State.Start;
        searchDepth = 7;
    }

    public int[][] getBoard() {
        int cols = board[0].length;
        int[][] boardClone = new int[board.length][cols];
        for (int i = 0; i < board.length; i++) {
            boardClone[i] = Arrays.copyOf(board[i], cols);
        }
        return boardClone;
    }


    // Recursive algorithm to determine best bot move
    private Direction botSearch() {
        int[][] boardClone = getBoard();
        Direction cur = bot.getCurrentDirection();
        if (cur == null) {
            return Direction.UP;
        }
        Direction[] ds = bot.getCurrentDirection().nonOpposite();
        //System.out.println("loop");
        int dir1Val = botSearchRecWrap(1, true, ds[0], boardClone);
        int dir2Val = botSearchRecWrap(1, true, ds[1], boardClone);
        int dir3Val = botSearchRecWrap(1, true, ds[2], boardClone);
        //System.out.println(ds[0] + ": " + dir1Val + ", " + ds[1] + ": " + dir2Val + ", " + ds[2] + ": " + dir3Val);
        if (dir1Val >= dir2Val && dir1Val >= dir3Val) {
            return ds[0];
        }
        if (dir2Val >= dir3Val) {
            return ds[1];
        }
        return ds[2];

    }
    /*
    This method is a wrapper for botSearchRec. It calls botSearchRec, then reverts the boardClone
    and character back to their original states, allowing for the recursive calculation to
    continue using the same array.
     */
    private int botSearchRecWrap(int level, boolean botTurn, Direction d, int[][] boardClone) {
        Character target;
        if (botTurn) {
            target = bot;
        } else {
            target = human;
        }
        Direction initial = target.getCurrentDirection();
        target.move(d);


        int i = botSearchRec(level, botTurn, boardClone);

        Coordinate c = target.getCurrent();

        try {
            boardClone[c.y()][c.x()] = board[c.y()][c.x()];
        } catch (ArrayIndexOutOfBoundsException e) {

        }

        target.undoMove();
        target.setCurrentDirection(initial);
        return i;
    }
    /*
    Base cases:
        If the heads run into each other, that will be a high reward.
        If the search depth is reached, the distance to the human should be minimized.
    Bot turn:
        If bot died, return a very negative score
        Recursive case:
            Consider each way the human can move recursively. Assuming the human calculates up to
            searchDepth, the worst he can do to the bot is the minimum direction value of the
            three states.
    Human turn:
        If the human died, return a very positive score.
        Recursive case:
            Consider each way that the bot can now move. The bot will choose the optimal up to
            searchDepth, and thus the maximum direction value.
     */
    private int botSearchRec(int level, boolean botTurn, int[][] boardClone) {
        boolean hAlive = human.isAlive(boardClone);
        boolean bAlive = bot.isAlive(boardClone);
        Coordinate h = human.getCurrent();
        Coordinate b = bot.getCurrent();
        if (h.equals(b)) {
            return 998;
        }

        if (level == searchDepth) {
            return 100 - h.dist(b);
        }


        if (botTurn) {
            if (!bAlive) {
                return -999 + level;
            }
            Coordinate c = bot.getCurrent();
            boardClone[c.y()][c.x()] = 2;
            Direction[] ds = human.getCurrentDirection().nonOpposite();
            int dv1 = botSearchRecWrap(level + 1, !botTurn, ds[0], boardClone);
            int dv2 = botSearchRecWrap(level + 1, !botTurn, ds[1], boardClone);
            int dv3 = botSearchRecWrap(level + 1, !botTurn, ds[2], boardClone);
            return Math.min(Math.min(dv1, dv2), dv3);
        } else {
            if (!hAlive) {
                return 999 + level;
            }
            Coordinate c = human.getCurrent();
            boardClone[c.y()][c.x()] = 1;
            Direction[] ds = bot.getCurrentDirection().nonOpposite();
            int dv1 = botSearchRecWrap(level + 1, !botTurn, ds[0], boardClone);
            int dv2 = botSearchRecWrap(level + 1, !botTurn, ds[1], boardClone);
            int dv3 = botSearchRecWrap(level + 1, !botTurn, ds[2], boardClone);
            return Math.max(Math.max(dv1, dv2), dv3);
        }

    }

    public void move(Direction d, boolean useBotNet) {
        if (useBotNet) {
            move(d, botNet.getMove(getBoard(), bot.getCurrent(), human.getCurrent()));
        } else {
            move(d, botSearch());
        }
    }

    private void move(Direction dHuman, Direction dBot) {
        if (state == State.Start) {
            state = State.Playing;
        }
        if (state == State.EndPlayerWon || state == State.EndTie || state == State.EndPlayerLost) {
            return;
        }
        human.move(dHuman);
        bot.move(dBot);
        boolean hIsAlive = human.isAlive(board);
        boolean bIsAlive = bot.isAlive(board);
        Coordinate a = human.getCurrent();
        Coordinate b = bot.getCurrent();
        if ((!hIsAlive && !bIsAlive) || a.equals(b)) {
            state = State.EndTie;
        } else if (!hIsAlive) {
            state = State.EndPlayerLost;
        } else if (!bIsAlive) {
            state = State.EndPlayerWon;
        } else {
            fill(a, 1);
            fill(b, 2);
        }
    }

    // fills in the 2D array with a 1 representing human or 2 representing bot
    private void fill(Coordinate c, int i) {
        board[c.y()][c.x()] = i;
    }

    public State getState() {
        return state;
    }

    @Override
    public String toString() {
        return Arrays.deepToString(board) +
                "\n" + bot +
                "\n" + human +
                "\n" + state +
                "," + searchDepth;
    }

    public String[][] saveBoard() {
        String[][] s = new String[board.length][board[0].length];
        for (int i = 0; i < s.length; i++) {
            for (int j = 0; j < s[i].length; j++) {
                s[i][j] = String.valueOf(board[i][j]);
            }
        }
        return s;
    }
    public String[] saveCharacters() {
        Coordinate botC = bot.getCurrent();
        Coordinate humanC = human.getCurrent();
        int[] is = {botC.x(), botC.y(), humanC.x(), humanC.y()};
        String[] s = new String[is.length];
        for (int i = 0; i < is.length; i++) {
            s[i] = String.valueOf(is[i]);
        }
        return s;
    }
}
