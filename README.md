# RoboSneks
A competitive(?) game of snake/tron between two robots!

# Approach
- Re-define goals in own words
- Create State objects
- Create the board game with Compose!
- Game loop in a coroutine

#  Goals
## Rules + Gameplay
- The game starts by placing SnekFood™ on a random tile on the game board.
- RoboSneks start at opposite corners of the board & take turns making *legal* moves (1/2 second intervals)
    - When a RoboSnek moves, it's body fills (occupies) in the space the head previously moved through.
    - A RoboSnek cannot move through a tile that is already occupied!
    - A RoboSnek can only move up, down, left or right into an unoccupied space. Diagonals are not allowed
- If a RoboSnek gets stuck (i.e. can't move), it stays there until the end of the round. The opponent RoboSnek continues until it eats the SnekFood™
- First RoboSnek to eat the SnekFood™ gets one point. The board resets an the next round begins with everything reset and the SnekFood™ is placed in a new random location on the board

## Requirements
-[x] Must keep track of total score during session.
-[x] Sessions should run continuously.
-[x] Maintain turn order of RoboSneks using coroutines!

## Optional
-[] Alternate robot search algorithms,
-[x] Count stalemate rounds
-[] Count game time
-[x] Additional RoboSneks
-[x] Moving the goal during the round

# How to Run
### Requirements
- The latest version of Android Studio. This app was developed using `Android Studio Electric Eel | 2022.1.1 Beta 2` and has also been tested on `Android Studio Dolphin | 2021.3.1 Patch 1` so one of those should work just fine to build the app! Others may work, but try at your own risk!
- A physical android device or an emulated device using android studio
- JDK 18

### Running the App
1. Download & extract the project. Open in Android studio.
2. Build the app - `./gradlew :app:assemble`
3. Run the app - `./gradlew :app:installDebug` (Make sure you have a device connected or an emulator running!)

# Architectural Approach + Developer thoughts
The UI of this App was built using Jetpack Compose! Compose was chosen for two reasons:
1. A lot of things in UI need to change frequently. Using Compose over the more static XML layouts means that changing the screen is a lot simpler! 
   - We *can* programmatically build layouts with Compose, but it's a lot more writing, is less intuitive/clear and harder to debug
2. I've never used compose before, and this seemed like a great opportunity to play around with it!
   - I'm not an expert in it *yet* but I'm happy to say that it was straightforward enough to pick up the basics within a few hours :) 
   - Please don't ask me to do anything complicated with it yet. 🙈 If you'd like an example of a layout I've built with xml, feel free to check out my other repos - [maybe this one?](https://github.com/vhenri/stock-list-app)

The "Game Engine" is one big class with all of the game logic in it. Ideally in a larger/more complex app we'd want to break down a file this large into smaller/ more reusable lines of code, but this is a pretty un-complicated app :)

The Game State object contains everything you'd need to know about the currently running game. This includes: The location of the food, the location and details about the sneks and useful data about the game such as the current Snek turn, the number of stalemate rounds and how many Sneks are stuck in the current round.

State Flows are used in to pass this information between the game engine and the activity

# App Features
The app contains all of the required features as well as some fun bonus features! 
1. **Dynamic Board Size**: a simple 7 x 7 board sounding kinda boring? Why not expand the board to 15 x 15 and beyond! 
   - *Note:* Boards larger than 50x50 are kinda hard to see and I haven't really tested beyond this, so try at your own risk! It does look kinda pretty tho imo.
2. **Up to 4 Sneks!**: Either have one lonely snek looking for food or give it up to 3 friends to battle against! 
   - if you try 5 Sneks or more, the game will crash. We *could* make this a loop and generate any number of sneks, but then we'd have to randomly generate snek body + head colors and that felt like a lot and I'm a bit picky when it comes to colors sometimes 😛 (Not a designer, but I like to pretend sometimes)
3. **Moving Food Mode! (Beta)**: Set this flag to true and watch the Sneks wildly chase the randomly moving food. 
  - This feature works best with a larger board size, but will often result in a *stalemate* since the Sneks easily tangle themselves up.
  - Feature is still in Beta - smaller board sizes *may* cause a crash. This is something that is a WIP, but due to time constraints has not been fully debugged. 

# Demos
### Multiple Board Sizes

### Multiple Sneks

### Moving Food Mode (Beta)

# *Not* Included
Due to time constraints , a few features were **not** added but may be considered in future iterations:
1. Total Game Time / Play Duration / Average Game length
2. Game Features UI
   - Right now, if you want to change the board size, the number of sneks or enable/disable moving food mode, you'll need to change the code. This can be done in `GameEngine.kt -> companion object`.
4. Alternate / Improved Search Algorithms
   - I did spend some time researching better search algorithms:
     - [Steering Behaviors For Autonomous Characters](http://www.red3d.com/cwr/steer/)
     - [Automated Snake Game Solvers via AI Search Algorithms](https://cpb-us-e2.wpmucdn.com/sites.uci.edu/dist/5/1894/files/2016/12/AutomatedSnakeGameSolvers.pdf)
   - AND I also consulted a [Game Dev](https://7ports.ca/) friend of mine around where to start if one wanted to implement a ✨dynamic pathfinding algorithm✨ (don't worry, he doesn't do kotlin and I kept the details vague). His answer? Use a library. 🙄 Otherwise look into [A* search algorithm](https://en.wikipedia.org/wiki/A*_search_algorithm)
     - Turns out not a lot of people use Android for building games! 🤷🏼‍♀️ Unfortunately, I couldn't easily find anything that would be useful at that point in the build.
     - Implementing these seemed a *little bit* out of scope for this project, so I prioritized other features!  