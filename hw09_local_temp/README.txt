=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=
CIS 1200 Game Project README
PennKey: _______
=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=

===================
=: Core Concepts :=
===================

- List the four core concepts, the features they implement, and why each feature
  is an appropriate use of the concept. Incorporate the feedback you got after
  submitting your proposal.

  1. 2d arrays - The board array is a good example of this because the board has two dimensions.
  The array of ints helps to establish characteristics of each square of the board, although
  it likely should have been an array of enum instances instead.

  2. Recursion - The bot algorithm is an appropriate use of recursion because it traverses a
  tree comparing ints that build on the layers. The instructor feedback about looking
  into pathfinding was helpful.

  3. File I/O - saving and loading - incomplete
  This is appropriate because it saves game states to a file and can load them hypothetically.

  4. JUnit testing - very incomplete
  This is appropriate because there are encapsulated states within Character and the board
  that should be preserved and tested independently.

===============================
=: File Structure Screenshot :=
===============================
- Include a screenshot of your project's file structure. This should include
  all of the files in your project, and the folders they are in. You can
  upload this screenshot in your homework submission to gradescope, named 
  "file_structure.png".

=========================
=: Your Implementation :=
=========================

- Provide an overview of each of the classes in your code, and what their
  function is in the overall game.

  Coordinate class represents a record of x and y and has a distance function which is just
  the sum of the x and y differences.

  Character class represents the single square that is your character or the bot. It helps to
  restrict movement to non opposite arrows and helps to define what movements are possible.

  Direction represents a direction and corresponds to user input that gets passed to the game state.

  GameState class defines what happens to the 2D array representing the board every time
  move is called. It will register the user move and create a bot move then determine what the
  state of the game is - draw/win/loss.

  GameCourt manages the GameState by displaying a representation of the 2d board and looking
  for user input in the arrow keys to call move at regular intervals. It handles time and
  functionality for the buttons in the control.

  GameLoader is an incomplete class meant to create a game court from a saved csv file.

  RunBlockade creates the GUI display along with the GameCourt. It creates the control buttons
  and is the starter for the game.



- Were there any significant stumbling blocks while you were implementing your
  game (related to your design, or otherwise)?
  The recursive algorithm was giving me troubles, especially because I did not want to repetitively
  clone the 2D array board as I thought it would be too slow. It was a pain because I had to
  keep resetting a position after analyzing it.


- Evaluate your design. Is there a good separation of functionality? How well is
  private state encapsulated? What would you refactor, if given the chance?
  The encapsulation is okay but it could be better. There is a lot of overlap
  between GameState and GameCourt particularly regarding the state of the game.
  I would refactor by trying to move the logic regarding the state to the GameState
  object.



========================
=: External Resources :=
========================

- Cite any external resources (images, tutorials, etc.) that you may have used 
  while implementing your game.

  I read the wikipedia article on minimax and tried to implement it as my bot.
