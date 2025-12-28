# AlphaGo, Go, and Why I Chose the Harder Board

I found myself watching a documentary about AlphaGo one night and was amazed when AlphaGo defeated Lee Sedol in 2016, it felt like a turning point—not just for artificial intelligence, but for how we think about complexity.

I had already played and studied chess, and chess always made sense to me as a programmer. The rules are rigid. The board is constrained. Evaluation—while deep—is conceptually approachable. You can count material. You can identify threats. You can usually explain *why* a position is winning or losing.

Go is different.

Go doesn’t reward you for taking pieces. It rewards you for understanding **space**. It doesn’t end cleanly. The board fills slowly, ambiguously, and the hardest questions are answered only at the end: *Which stones are alive? Which territory is real? What actually belongs to whom?*

That ambiguity is exactly what made Go interesting to me as a software engineer.

If AlphaGo could conquer a game where brute-force evaluation was essentially impossible, I wanted to understand what made that game so difficult—by building it.

---

## The Project: A Go Engine From First Principles

This project is a **Java-based Go board game** with a Swing UI and a rule-accurate board engine underneath. I didn’t want a toy implementation. I wanted something that actually respected the rules players care about:

- Liberties  
- Group captures  
- Suicide prevention  
- Prisoners  
- Territory  
- Japanese scoring  

The UI is intentionally thin. Almost all of the complexity lives in the `Board` class, which acts as the **single source of truth** for game state. Every rule—good or bad—has to survive there.

That decision paid off when things got hard.

---

## Why Go Is Harder Than Chess (In Code)

In chess, pieces have identity and intent. In Go, stones don’t move—and yet the game state changes constantly.

A stone’s value depends entirely on:
- its connected group  
- the group’s liberties  
- what *might* be captured several moves later  

This forced me to think less in terms of objects and more in terms of **connected components on a graph**.

The board is a grid, but the game logic is graph traversal.

---

## Capture Logic: Flood-Fill or You’re Wrong

The first non-trivial challenge was capture logic.

You can’t check a single stone’s liberties in isolation. You must evaluate the **entire connected group**. That led to a flood-fill traversal approach:

1. Starting from a stone, traverse all connected stones of the same color  
2. Track whether *any* stone in the group has an adjacent empty point  
3. If none do, the entire group is captured atomically  

Partial removal leads to illegal states, so capture resolution had to be all-or-nothing.

This same traversal logic later became the backbone of scoring.

---

## Suicide Moves: The Simulate-and-Rollback Problem

Preventing suicide moves sounds simple until you remember this rule:

> A move that appears suicidal is legal **if it captures an opponent group**.

The only correct way to evaluate this is to:
1. Temporarily place the stone  
2. Remove any opponent groups that lose all liberties  
3. Check whether the placed stone’s group has liberties  
4. Roll the board back to its original state  

This forced me to build logic that could *safely mutate state, test a condition, and undo everything*—a pattern that appears constantly in real-world systems.

---

## Japanese Scoring: Where Most Go Engines Break

Captures were hard. Scoring was worse.

Japanese scoring is often summarized as:

```yaml
score = territory + prisoners
```

That’s correct — but it hides almost all of the real complexity. Implementing Japanese scoring correctly requires solving several subtle problems that don’t exist in games like chess.

In my engine, Japanese scoring happens in three layers:
1. **Flood-fill empty regions** to measure territory size  
2. **Determine ownership** of each region based on surrounding colors  
3. **Add prisoners**, tracked historically during captures  

---

## Flood-Fill as the Core Scoring Algorithm

At the end of the game, I compute territory by scanning the board for empty intersections and flood-filling each unvisited region.

Final scoring combines region size with prisoner counts:

```java
public void calculateJapaneseScoring() {
    boolean[][] visited = new boolean[cols][rows];
    int blackTerritory = 0;
    int whiteTerritory = 0;

    this.blackScore = 0;
    this.whiteScore = 0;

    for (int col = 0; col < cols; col++) {
        for (int row = 0; row < rows; row++) {
            TerritoryResult result = determineTerritoryOwner(col, row, visited);
            if (result != null) {
                if (result.owner == Color.BLACK) blackTerritory += result.size;
                else if (result.owner == Color.WHITE) whiteTerritory += result.size;
            }
        }
    }

    this.blackScore = blackTerritory + capturedByBlack;
    this.whiteScore = whiteTerritory + capturedByWhite;
}
```
The critical detail is += result.size. Territory is scored by how many intersections it contains, not by how many regions exist.

Territory Evaluation in Practice
Each empty region is evaluated using a breadth-first search that:

- **Traverses all connected empty points**

- **Counts the region size**

- **Records all bordering stone colors**

```java
private TerritoryResult determineTerritoryOwner(int col, int row, boolean[][] visited) {
    if (pieceArray[col][row] != null || visited[col][row]) return null;

    Set<Color> borderingColors = new HashSet<>();
    Queue<int[]> queue = new LinkedList<>();
    queue.add(new int[]{col, row});
    visited[col][row] = true;

    int regionSize = 0;

    while (!queue.isEmpty()) {
        int[] current = queue.poll();
        regionSize++;

        for (int[] dir : directions) {
            int nc = current[0] + dir[0];
            int nr = current[1] + dir[1];
            if (!isValidPosition(nc, nr)) continue;

            Piece neighbor = pieceArray[nc][nr];
            if (neighbor != null) {
                borderingColors.add(neighbor.color);
            } else if (!visited[nc][nr]) {
                visited[nc][nr] = true;
                queue.add(new int[]{nc, nr});
            }
        }
    }

    if (borderingColors.size() == 1) {
        return new TerritoryResult(borderingColors.iterator().next(), regionSize);
    }
    return null;
}
```
Ownership is decided only after traversal completes. Returning early leads to region fragmentation and incorrect scoring.

Prisoners Are Historical, Not Derivable
A subtle but critical insight:

You cannot infer prisoners from the final board.

Captured stones are removed during play, so the board alone does not encode capture history. Prisoners must be tracked at capture time and added during scoring:

```java
blackScore = blackTerritory + capturedByBlack;
whiteScore = whiteTerritory + capturedByWhite;
```
This separation keeps scoring deterministic and avoids incorrect assumptions about dead stones.

## Testing With Realistic Endgames

To validate correctness, I built tests using **near-full 9×9 and 19×19 boards**, not contrived or minimal examples.

These tests are designed to:

- Load full board states directly  
- Validate **multi-point territory regions**  
- Verify **territory + prisoner integration**  
- Catch **double-counting and undercounting** errors  

More than once, a test returned something like **“7 points”** when the correct answer was **“4”**.  
Every time, the bug traced back to traversal logic or incorrect region accounting.

That’s when I knew the project was doing its job: **forcing correctness through realistic constraints**.

---

## UI Last, Logic First

The Swing UI exists to make the game playable, but it does **not** own the rules.

- The board does not know it is being rendered  
- The UI does not know how scoring works  

This separation made debugging possible and made future extensions realistic instead of fragile.

---

## What This Project Taught Me

Building Go taught me more than building chess ever did:

- How to reason about **connected components**  
- How easy it is to get traversal logic subtly wrong  
- Why **correctness beats cleverness**  
- How tests expose flawed assumptions  
- Why real-world rule systems are inherently messy  

AlphaGo didn’t just beat a human. It proved that Go’s difficulty wasn’t hype—it was structural.

After building even a small slice of that complexity myself, I finally understand why.
