package org.cis1200.blockade;

import java.awt.*;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class GameLoader {
    static String extractColumn(String csvLine, int csvColumn) {
        if (csvLine == null) {
            return null;
        }
        String[] data = csvLine.split("\\,");
        if (csvColumn >= data.length || csvColumn < 0) {
            return null;
        } else {
            return data[csvColumn];
        }
    }

    public GameCourt load(String filePath) throws IOException {
        if (filePath == null) {
            throw new IllegalArgumentException("No Filepath!");
        }
        Reader r;
        try {
            r = new FileReader(filePath);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File Not Found");
        }
        BufferedReader br = new BufferedReader(r);
        List<String> lines = new LinkedList<>();
        try {
            for (int i = 0; i < 7; i++) {
                lines.add(br.readLine());
            }
        } catch (IOException e) {
            throw new IOException("Format is incorrect");
        }
        String s = lines.get(2);
        int red = Integer.parseInt(extractColumn(s, 0));
        int g = Integer.parseInt(extractColumn(s, 1));
        int b = Integer.parseInt(extractColumn(s, 2));
        String name = extractColumn(s, 3);
        int x = Integer.parseInt(extractColumn(s, 4));
        int y = Integer.parseInt(extractColumn(s, 5));
        Direction d = Direction.valueOf(extractColumn(s, 6));
        Character c1 = new Character(new Coordinate(x, y), new Color(red, g, b), name, d);

        s = lines.get(3);
        red = Integer.parseInt(extractColumn(s, 0));
        g = Integer.parseInt(extractColumn(s, 1));
        b = Integer.parseInt(extractColumn(s, 2));
        name = extractColumn(s, 4);
        x = Integer.parseInt(extractColumn(s, 4));
        y = Integer.parseInt(extractColumn(s, 5));
        d = Direction.valueOf(extractColumn(s, 6));
        Character c2 = new Character(new Coordinate(x, y), new Color(red, g, b), name, d);

        s = lines.get(4);
        GameState.State st = GameState.State.valueOf(extractColumn(s, 0));
        int depth = Integer.parseInt(extractColumn(s, 1));

        return null;

    }
}
