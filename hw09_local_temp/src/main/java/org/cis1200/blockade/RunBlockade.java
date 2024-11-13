package org.cis1200.blockade;

// imports necessary libraries for Java swing

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.IOException;

/**
 * Game Main class that specifies the frame and widgets of the GUI
 */
public class RunBlockade implements Runnable {
    public void run() {
        // NOTE : recall that the 'final' keyword notes immutability even for
        // local variables.

        // Top-level frame in which game components live.
        // Be sure to change "TOP LEVEL FRAME" to the name of your game
        final JFrame frame = new JFrame("Blockade");
        frame.setLocation(300, 300);

        // Status panel
        final JPanel status_panel = new JPanel();
        frame.add(status_panel, BorderLayout.SOUTH);
        final JLabel status = new JLabel("Running...");
        status_panel.add(status);

        // Main playing area
        final GameCourt court = new GameCourt(status, 20, 20, "Player", 250);
        frame.add(court, BorderLayout.CENTER);

        // Reset button
        final JPanel control_panel = new JPanel();
        frame.add(control_panel, BorderLayout.NORTH);

        // Note here that when we add an action listener to the reset button, we
        // define it as an anonymous inner class that is an instance of
        // ActionListener with its actionPerformed() method overridden. When the
        // button is pressed, actionPerformed() will be called.
        final JButton reset = new JButton("Reset");
        reset.addActionListener(e -> court.reset());
        control_panel.add(reset);

        final JButton pause = new JButton("Pause");
        pause.addActionListener(e -> court.pause());
        control_panel.add(pause);

        final JButton resume = new JButton("Resume");
        resume.addActionListener(e -> court.resume());
        control_panel.add(resume);

        final JButton save = new JButton("Save");
        save.addActionListener(e -> {
            try {
                court.save();
            } catch (IOException exc) {
                JOptionPane.showMessageDialog(frame, exc.getMessage());
            }
        });
        control_panel.add(save);

        final JButton info = new JButton("Info");
        info.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                    frame, "INSTRUCTIONS: \nMove your arrow keys to " +
                            "leave behind a trail of blocks! You will die if you crash " +
                            "into a block or the wall. Try to beat the bot!"
            );
        });
        control_panel.add(info);

        JOptionPane optionPane = new JOptionPane();
        JSlider slider = new JSlider();
        slider.setMajorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setSnapToTicks(true);
        slider.setValue(5);
        slider.setMaximum(10);
        ChangeListener changeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                JSlider theSlider = (JSlider) changeEvent.getSource();
                if (!theSlider.getValueIsAdjusting()) {
                    optionPane.setInputValue(Integer.valueOf(theSlider.getValue()));
                }
            }
        };
        slider.addChangeListener(changeListener);
        optionPane.setMessage(new Object[] { "Select a speed: ", slider });
        optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
        optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);

        final JButton speed = new JButton("Settings");
        speed.addActionListener(e -> {
            JDialog dialog = optionPane.createDialog(frame, "Settings");
            dialog.setVisible(true);
            if (optionPane.getInputValue().getClass() != "".getClass()) {
                court.setTimer(2000 / ((int) optionPane.getInputValue() + 1));
            }
        });
        control_panel.add(speed);

        // final JButton load = new JButton("Load");
        // load.addActionListener(e -> court = new GameCourt());
        // control_panel.add(load);

        // Put the frame on the screen
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Start game
        court.reset();
    }
}