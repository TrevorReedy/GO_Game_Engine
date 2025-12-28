# Go Board Game (Java)

A desktop implementation of the board game **Go**, built in Java with a Swing-based graphical interface and a rule-accurate board engine.  
This project emphasizes **correct capture mechanics, territory evaluation, and Japanese scoring**, backed by extensive unit tests and realistic endgame scenarios.

---

## 🎯 Project Goals

- Accurately implement the core rules of Go
- Model complex board state transitions safely and deterministically
- Compute **Japanese scoring** correctly (territory + prisoners)
- Build a testable, extensible game engine suitable for future features (AI, SGF, online play)
- Demonstrate strong algorithmic reasoning and defensive programming

---

## 🧠 Architecture Overview

### Board-Centric Design
The `Board` class is the single source of truth for game state:

- Maintains a 2D `Piece[][]` board
- Tracks turn order and prisoner counts
- Validates all moves
- Resolves captures and suicide prevention
- Computes final scores

This strict ownership prevents UI logic from mutating game state directly and enables deterministic unit testing.

---

## ⚙️ Core Game Mechanics

### Stone Placement & Turn Management
- Moves are validated before execution:
  - Bounds check
  - Occupancy check
  - Suicide prevention
- Turns alternate automatically after a successful move
- Pass and resign actions update game flow without modifying board state

---

### Liberty & Capture Resolution

- Liberties are determined using **flood-fill traversal** of connected stone groups
- When a stone is placed:
  1. Adjacent enemy groups are checked for zero liberties
  2. Entire groups are removed atomically
  3. Prisoner counts are updated accordingly
- Suicide moves are rejected unless the move results in a capture

This approach avoids partial group removal and ensures rule-correct captures.

---

## ⚖️ Japanese Scoring Algorithm (Technical Breakdown)

Scoring follows **Japanese rules**:

finalScore = territory + prisoners

markdown
Copy code

### 1️⃣ Territory Identification
- The board is scanned for **empty intersections (`.`)** that have not yet been visited
- Each empty region is explored using a **flood-fill / BFS traversal**
- During traversal:
  - Region size is counted
  - All bordering stone colors are recorded

### 2️⃣ Territory Ownership Resolution
- If an empty region is bordered **exclusively by Black stones**, it is Black territory
- If bordered **exclusively by White stones**, it is White territory
- If bordered by **both colors**, the region is neutral and scores **0**
- Regions touching the board edge are still valid territory if fully enclosed

### 3️⃣ Region-Based Scoring (Critical Detail)
- Territory is scored by **region size**, not per region
- Example:
  - A 2×2 enclosed area = **4 points**, not 1
- This is enforced by:
  - Completing the entire flood-fill before scoring
  - Marking all visited points to prevent double-counting

### 4️⃣ Prisoner Integration
- Prisoners are tracked at capture time, not inferred from the final board
- Final scoring simply adds:
blackScore = blackTerritory + capturedByBlack
whiteScore = whiteTerritory + capturedByWhite

yaml
Copy code

This separation ensures scoring remains deterministic and avoids ambiguous “dead stone” inference.

---

## 🧪 Testing Strategy

### Unit Test Coverage Includes:
- Single-stone and multi-stone captures
- Suicide move rejection
- Turn alternation correctness
- Territory recognition for:
- Single-point eyes
- Multi-point regions
- Multiple independent regions
- Endgame scoring on:
- Near-full 9×9 boards
- Realistic 19×19 endgame positions
- Explicit prisoner + territory validation

### Mock Board Testing
- Endgame tests load full board states directly (no move spam)
- Prisoner counts are injected explicitly for deterministic validation
- Prevents scoring tests from being dependent on move history

This approach mirrors real production testing of complex rule engines.

---

## 🖥️ Graphical Interface (Swing)

- Interactive board rendering
- Mouse-based stone placement
- Pass and resign controls
- Screen management system for future extensibility

UI is intentionally thin and delegates all rule logic to the board engine.

---

## 📁 Project Structure

```text
src/
├── Board.java          // Core rules, captures, scoring
├── Piece.java          // Stone abstraction
├── Move.java           // Move validation & execution
├── GameScreen.java     // Main gameplay UI
├── ScreenManager.java  // Screen transitions
├── Input.java          // Mouse / input handling
└── tests/
   └── BoardTest.java  // Rule & scoring validation
```


## 🧩 Why This Project Is Technically Interesting
- Implements non-trivial graph traversal algorithms

- Requires careful handling of state consistency

- Demonstrates the difficulty of real-world rule systems

- Highlights test-driven debugging of edge cases

- Models a complex board game without relying on external libraries


## 📜 License
- This project is intended for educational and portfolio use.
