# Robo-Snek
A competitive(?) game of snake/tron between two robots!

# Approach
- Re-define goals in own words
- Create State objects
- Create the board game with Compose!
- Game loop in a coroutine

#  Goals
## Rules + Gameplay
- The game starts by placing SnekFood™️ on a random tile on the game board.
- RoboSneks start at opposite corners of the board & take turns making *legal* moves (1/2 second intervals)
    - When a RoboSnek moves, it's body fills (occupies) in the space the head previously moved through.
    - A RoboSnek cannot move through a tile that already has a body tile in it!
    - A RoboSnek can only move up, down, left or right into an unoccupied space. Diagonals are not allowed
- If a RoboSnek gets stuck (i.e. can't move), it stays there until the end of the round. The opponent RoboSnek continues until it eats the SnekFood™️
- First RoboSnek to eat the SnekFood™ gets one point. The board resets an the next round begins with everything reset and the SnekFood™ is placed in a new random location on the board

## Requirements
- Must keep track of total score during session.
- Sessions should run continuously.
- Maintain turn order of RoboSneks using coroutines!

## Optional
- Alternate robot search algorithms,
- Handle (or count?) Stalemate rounds, game time
- Additional RoboSneks
- Moving the goal during the round
