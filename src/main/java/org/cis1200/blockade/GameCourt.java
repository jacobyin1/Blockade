package org.cis1200.blockade;



import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * GameCourt
 *
 * This class holds the primary game logic for how different objects interact
 * with one another. Take time to understand how the timer interacts with the
 * different methods and how it repaints the GUI on every tick().
 *
 */
// want functionality for pause, resume, reset, save, load, settings, show
// elapsed time
public class GameCourt extends JPanel {

    // Game constants
    private final int boardHeight;
    private final int boardWidth;
    private String username;

    // Update interval for timer, in milliseconds
    private int interval;

    // the state of the game logic
    private GameState gs;
    private Timer timer;
    private Direction userDirection = null;
    private boolean paused = false;

    private double time;

    private final JLabel status; // Current status text, i.e. "Running..."

    final private int blockWidth;
    final private int separator;

    private boolean useBotNet;

    public GameCourt(
            JLabel status, int boardHeight, int boardWidth,
            String username, int interval, boolean useBotNet
    ) {
        time = 0;
        blockWidth = 20;
        separator = 6;
        this.boardHeight = boardHeight;
        this.boardWidth = boardWidth;
        this.username = username;
        this.interval = interval;
        this.useBotNet = useBotNet;

        gs = new GameState(boardHeight, boardWidth, username);
        // creates border around the court area, JComponent method
        setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // The timer is an object which triggers an action periodically with the
        // given INTERVAL. We register an ActionListener with this timer, whose
        // actionPerformed() method is called each time the timer triggers. We
        // define a helper method called tick() that actually does everything
        // that should be done in a single time step.
        timer = new Timer(interval, e -> tick());
        timer.start(); // MAKE SURE TO START THE TIMER!

        // Enable keyboard focus on the court area. When this component has the
        // keyboard focus, key events are handled by its key listener.
        setFocusable(true);

        // This key listener allows the square to move as long as an arrow key
        // is pressed, by changing the square's velocity accordingly. (The tick
        // method below actually moves the square.)
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    userDirection = Direction.LEFT;
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    userDirection = Direction.RIGHT;
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    userDirection = Direction.DOWN;
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    userDirection = Direction.UP;
                }
            }
        });

        this.status = status;
        status.setText("Press an arrow key to start.");
        requestFocusInWindow();
    }

    /**
     * (Re-)set the game to its initial state.
     */
    public void reset() {
        time = 0;
        gs = new GameState(boardHeight, boardWidth, username);
        userDirection = null;
        timer.stop();
        timer = new Timer(interval, e -> tick());
        timer.start();
        status.setText("Press an arrow key to start.");

        // Make sure that this component has the keyboard focus
        requestFocusInWindow();
    }

    public void setTimer(int i) {
        interval = i;
        reset();
    }

    public void setUsername(String s) {
        username = s;
    }

    public void save() {
        String filePath = "files/saved_game.csv";
        File file = Paths.get(filePath).toFile();
        try {
            FileWriter output = new FileWriter(file);
            CSVWriter outputcsv = new CSVWriter(output);
            String[] line1 = {username, String.valueOf(time)};
            outputcsv.writeNext(line1);
            outputcsv.writeAll(Arrays.asList(gs.saveBoard()));
            outputcsv.writeNext(gs.saveCharacters());
            outputcsv.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        paused = true;
        requestFocusInWindow();
    }

    public void load() throws NullPointerException, IOException, CsvValidationException {
        String filePath = "files/saved_game.csv";
        File file = Paths.get(filePath).toFile();
        FileReader filereader = new FileReader(file);
        CSVReader csvread = new CSVReader(filereader);

        String[] line1 = csvread.readNext();
        String[][] gsString = new String[boardHeight][boardWidth];
        for (int h = 0; h < boardHeight; h++) {
            gsString[h] = csvread.readNext();
        }
        String[] lineLast = csvread.readNext();

        username = line1[0];
        time = Double.parseDouble(line1[1]);
        int[][] gsInt = new int[gsString.length][gsString[0].length];
        for (int i = 0; i < gsString.length; i++) {
            for (int j = 0; j < gsString[i].length; j++) {
                gsInt[i][j] = Integer.parseInt(gsString[i][j]);
            }
        }

        int x1 = Integer.parseInt(lineLast[0]);
        int y1 = Integer.parseInt(lineLast[1]);
        int x2 = Integer.parseInt(lineLast[2]);
        int y2 = Integer.parseInt(lineLast[3]);

        gs = new GameState(boardHeight, boardWidth, username, gsInt,
                new Coordinate(x1, y1), new Coordinate(x2, y2));
        userDirection = null;
        timer.stop();
        timer = new Timer(interval, e -> tick());
        timer.start();
        status.setText("Game reloaded!");
        requestFocusInWindow();

    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    /**
     * This method is called every time the timer defined in the constructor
     * triggers.
     */
    void tick() {
        if (paused) {
            status.setText("Paused");
            return;
        }
        GameState.State state = gs.getState();
        if (state.equals(GameState.State.EndPlayerLost) ||
                state.equals(GameState.State.EndPlayerWon)
                || state.equals(GameState.State.EndTie)) {
            return;
        }
        if (userDirection != null) {
            gs.move(userDirection, useBotNet);
            time = (double) Math.round(10 * (time + (double) interval / 1000)) / 10;
        }
        GameState.State state1 = gs.getState();
        String label = switch (state1) {
            case Start -> "Press an arrow key to start.";
            case Playing -> "Time: " + time + "s";
            case EndTie -> "It's a tie!";
            case EndPlayerLost -> username + " loses in " + time + "s!";
            case EndPlayerWon -> username + " wins in " + time + "s!";
        };

        status.setText(label);

        // update the display
        repaint();

    }

    @Override
    public void paintComponent(Graphics g) {
        Coordinate cursor = new Coordinate(0, 0);
        int[][] board = gs.getBoard();
        for (int i = 0; i < board.length - 1; i++) {
            for (int j = 0; j < board[i].length - 1; j++) {
                cursor = paintRect(board[i][j], g, cursor.x(), cursor.y(), blockWidth, blockWidth);
                cursor = paintRect(0, g, cursor.x(), cursor.y(), separator, blockWidth);
            }
            int endJ = board[i].length - 1;
            cursor = paintRect(board[i][endJ], g, cursor.x(), cursor.y(), blockWidth, blockWidth);
            paintRect(0, g, 0, cursor.y() + blockWidth, cursor.x(), separator);
            cursor = new Coordinate(0, cursor.y() + blockWidth + separator);
        }

        int i = board.length - 1;
        for (int j = 0; j < board[i].length - 1; j++) {
            cursor = paintRect(board[i][j], g, cursor.x(), cursor.y(), blockWidth, blockWidth);
            cursor = paintRect(0, g, cursor.x(), cursor.y(), separator, blockWidth);
        }
        int endJ = board[i].length - 1;
        paintRect(board[i][endJ], g, cursor.x(), cursor.y(), blockWidth, blockWidth);

    }

    /*
    This method is called when repainting the GameCourt component. It is used to make a rectangle
    at coordinate (x, y) according to some width and height, then returns a coordinate (x + width, y)
    to facilitate the next paint Rect iteration.
    Color int:
    0 is Gray for nothing, 1 is Green for player, 2 is Red for opponent
     */
    private Coordinate paintRect(int color, Graphics g, int x, int y, int width, int height) {
        if (color == 0) {
            g.setColor(Color.LIGHT_GRAY);
        } else if (color == 1) {
            g.setColor(Color.GREEN);
        } else if (color == 2) {
            g.setColor(Color.RED);
        }
        g.fillRect(x, y, width, height);
        return new Coordinate(x + width, y);
    }

    @Override
    public Dimension getPreferredSize() {
        int x = blockWidth * boardWidth + (boardWidth - 1) * separator;
        int y = blockWidth * boardHeight + (boardHeight - 1) * separator;
        return new Dimension(x, y);
    }
}