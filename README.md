# Blockade
My final project for CIS 1200 is an implementation of the game blockade in Swing, complete with a primitive AI opponent.
The mushroom and tictactoe folders are sample games for me to base my game off of provided by CIS 1200 course, while the blockade folder is my project. 

Run game to play blockade. The game involves a toolbar and a grid of states. The AI will try to approach the player while avoiding its own death, but it is set to only see 7 moves in advance to save system resources. You can change this at searchDepth in the GameState class. 

To do: 

1. Improve readibility, add comments, separate ai decisions into its own class.

2. Implement save/load functionality.

3. Try out playing with multiple opponents.

4. Try other algorithms of play, such as perhaps a graph search algorithm.
