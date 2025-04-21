# Minesweeper Android App

A modern, animated Minesweeper game for Android with user profiles, authentication, and high‑score tracking across multiple difficulties.

## Table of Contents
- [Demo](#demo)
- [Features](#features)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Building & Running](#building--running)
- [Usage](#usage)
- [Architecture & Tech Stack](#architecture--tech-stack)
- [Contributing](#contributing)
- [License](#license)

## Demo
<img src="https://github.com/user-attachments/assets/5cc0efd3-0de1-432c-8d6d-6540000f0f0d" height="500"/> <img src="https://github.com/user-attachments/assets/28643fc0-4dd1-4435-aadd-00de6ca1d216" height="500"/> <img src="https://github.com/user-attachments/assets/e9dd8300-9492-49b5-b57e-b6947ddbb100" height="500"/> <img src="https://github.com/user-attachments/assets/ad034fe0-0e6d-48d7-85e7-d2359a855ba1" height="500"/>


## Features
- **User Profiles & Authentication**: Register and log in multiple players; select a profile to play.
- **Dynamic Profile Management**: Add and remove profiles on the home screen.
- **Multiple Difficulties**: Easy (5×5, 3 mines), Medium (8×8, 10 mines), Hard (10×10, 20 mines).
- **High‑Score Tracking**: Stores best times per difficulty and shows “No high score yet” if none.
- **Hints & Restart**: Reveal a safe tile as a hint; restart the current game at any time.
- **Animated UI**: Title and grid cells animate into view; touch feedback on buttons.
- **Responsive Layout**: Adapts to screen and keyboard, with scrollable profile list.
- **Exit Confirmation**: Intercepts back button to confirm going home, quitting, or dismissing.

## Getting Started

### Prerequisites
- Android Studio (Arctic Fox or later)
- Android SDK Platform 29+
- Java SE Development Kit (JDK) 8 or higher

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/Ayush-D2004/Minesweeper.git
   cd Minesweeper
   ```
2. Open the project in Android Studio.
3. Let Gradle sync and download dependencies.

### Building & Running
1. Connect an Android device or start an emulator.
2. Click **Run** (▶️) in Android Studio or:
   ```bash
   ./gradlew installDebug
   ```
3. The app will launch automatically.

## Usage
1. **Add New Profile**: On the home screen, tap **Add New Profile**, enter a username and password, then **Register** or **Login**.
2. **Select Profile**: Tap a profile card to start a game with that user’s data.
3. **Play**: Tap to reveal cells, long‑press to flag mines.
4. **Hint**: Tap the Hint button to reveal a safe cell (once per game).
5. **Restart**: Tap Restart to reset the current board.
6. **Difficulty**: Switch between Easy, Medium, and Hard at any time; high scores update accordingly.
7. **Back Button**: During play, back‑press opens a dialog to _Go Home_, _Quit_, or _Cancel_.

## Architecture & Tech Stack
- **Language**: Java
- **UI**: Android XML layouts, ConstraintLayout, RecyclerView, CardView
- **Persistence**: SQLite via `UserDatabaseHelper`
- **Animations**: `AnimationUtils` (fade, scale, grid entry) & touch feedback
- **Project Structure**:
  - `MainActivity.java`: Core UI & game flow
  - `GameBoard.java` & `Tile.java`: Game logic
  - `User.java` & `UserDatabaseHelper.java`: User data model & storage
  - `UserProfileAdapter.java`: Profile list adapter
  - `GameOverDialogFragment.java` & `ExitGameDialogFragment.java`: Dialog fragments

## Contributing
Contributions are welcome! Please fork the repo, create a feature branch, commit your changes, and open a pull request.

1. Fork it
2. Create your feature branch (`git checkout -b feature/YourFeature`)
3. Commit your changes (`git commit -am 'Add YourFeature'`)
4. Push to the branch (`git push origin feature/YourFeature`)
5. Open a Pull Request

## License
This project is licensed under the [MIT License](LICENSE).
