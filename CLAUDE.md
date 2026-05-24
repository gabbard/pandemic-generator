## Project overview

Android app (+ pure-JVM core library) that assists players of the board game *Pandemic* by tracking and simulating the game's deck mechanics — specifically the infection deck and player deck, whose ordering matters but is hidden from players.

## Module structure

- **`pandemic-generator-core`** — pure Kotlin/JVM library containing all game logic. No Android dependencies; can be tested with plain JUnit.
- **`app`** — Android UI layer. Activities delegate to the core library for all state transitions. No game logic lives here.

## Core domain concepts

- **`Deck<T>`** — ordered list of cards. Key operations: `draw(n)` from top, `drawOneFromTheBottom()`, `shuffled(rng)`, `splitAsEvenlyAsPossible(n)`, `placeOnTopOf(other)`. `splitAsEvenlyAsPossible` uses round-robin assignment (card 0 → stack 0, card 1 → stack 1, …) so stacks are interleaved, not contiguous slices.
- **`TrackableState`** — game state reconstructable from public information (infection deck order, player deck composition, infection rate, current player). Drives all transitions.
- **`UntrackableState`** — board cube counts and player hands. Set up at game start but not updated during play; tracking board state is out of scope for this app.
- **`GameState`** — combines both.
- **`RuleSet`** — configures a game variant (number of players, which roles/events/epidemics are in the pool, how many epidemics to use). `setupGame(rng)` is the entry point that builds an initial `GameState`.
- **Transitions** — only two: `DRAW_PLAYER_CARDS` and `INFECT`. They alternate strictly. `executeTransition` returns a sealed `TransitionResult` with the new state and what happened.
- **Epidemic sequence** — when an epidemic card is drawn: infection rate advances one step, one card is drawn from the *bottom* of the infection deck, the discard pile is shuffled and placed on *top* of the remaining infection deck, discard pile is cleared.
- **Initial setup** — 9 infection cards are drawn to seed the board (first 3 get 3 cubes, next 3 get 2, last 3 get 1). Player hands are dealt before epidemics are inserted. Epidemics are distributed by splitting the remaining player deck into N even stacks, inserting one epidemic per stack, shuffling each stack, then concatenating.
- **`NATIONAL_CHAMPIONSHIP_RULES`** — the concrete `RuleSet` used in the app: 2 players (4-card opening hands), 6 Virulent Strain epidemics, 5 events, all competitive roles.

## Build

```
# Requires Android SDK. Set sdk.dir in local.properties or ANDROID_HOME.
./gradlew assembleDebug                       # build app
./gradlew :pandemic-generator-core:test       # run core unit tests (no device needed)
```

## Tests

New tests go in `pandemic-generator-core/src/test/java/org/gabbard/pandemicgenerator/`. The `app` module's placeholder tests (`ExampleUnitTest`, `ExampleInstrumentedTest`) are scaffolding only.
