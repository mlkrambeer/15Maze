# 15Maze

This is a phone app I made for my mobile computing class.  It is a 15 puzzle.  When I initially created this repository, I accidentally called it 15 maze for some reason.

This app as it stands now has several features which are controlled by the buttons and switches at the bottom of the screen.

The radio buttons allow you to select either a numbers game or an image game.  A numbers game will show numbered tiles to be arranged in order of their number.
The tiles turn red when they are in the correct position.  An image game will instead scramble an image into 16 pieces, remove 1 piece, and rearrange the pieces.

Currently how it works is that I have several images loaded into a folder within the project, and I select one of those.  The app will persistently save the current image
selection.  This can currently be changed in one of two ways.  First, there is a "random image" switch.  If this is toggled on, then whenever an image game begins,
it will select a random image from among the images in my internal folder.  The second way to change the image is to click the select image button.  This will bring up 
a file explorer which allows the user to select an image from among the files in their phone.  After selecting a file, the app will return to the main menu.  From there, simply
press new game while "image" is selected from the radio buttons, and an image game will begin with whatever image the user selected from their phoen.

There are a few more switches.  The one which is labeled "fast move" toggles between two movement options which I programmed for the tiles.  Fast move allows the user either
to click and drag the tiles with their finger, or they can simply tap and release a tile and it will be moved to the other open position.

If fast move is not on, then the ability to tap a tile is removed.  The user must drag the tile to the new position.  And if the tile is not dragged far enough (more than
half way to the new position) it will actually snap back to where it was instead of snapping to the new position.

Finally, there is a switch to toggle on and off the counters (which are the timer and the move counters, above).  If at any point during a puzzle the counters are toggled off,
then the timer will be set to zero and stop counting, even if resumed.  A new game will have to be started in order to get the timer running again.  The move counter, however,
will continue to count whether or not it is displayed.  However, if the counters are not toggled on, then the high scores will not be updated even if the user sets a new high
score in either category.

The high scores are saved persistently.  There is currently no way for the user to delete or reset their scores.  They can only ever be improved upon.

When the user solves a puzzle, a victory message is displayed and the high scores are updated if necessary.

When the app is paused, the timer pauses as well, and resumes when the app resumes.  If the app is paused and the timer has not been disabled, then the tiles are made invisible
while the app is paused.  This is to prevent cheating the timer, as one might be tempted to pause and look at the tiles to plan out their moves, then resume.

