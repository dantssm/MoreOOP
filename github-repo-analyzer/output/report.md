# Repository Analysis: MoreOOP

## Summary
A text-based fantasy fight simulator based on Lord of the Rings characters, developed in Java with Maven. The project models different character types (Hobbits, Elves, Kings, Knights) with unique attributes and behaviors, allowing them to engage in simulated combat managed by a game coordinator.

## Technologies
- Java
- Maven
- JUnit 5
- Reflections Library
- GitHub Actions

## Strengths
- Clean usage of inheritance with a common Character base class and specialized character sub-types.
- Decoupled game loop logic isolated within a dedicated GameManager class.
- Dynamic character discovery using reflections, enabling extension without modifying the CharacterFactory.
- Configured automated testing framework and continuous integration workflow using GitHub Actions.

## Issues
- **Vulnerability to infinite loops**: In GameManager, if two Hobbits fight, neither deals damage and they just cry endlessly, causing the program to hang.
- **Significant code duplication**: The King and Knight classes duplicate identical random value generation and damage calculation logic.
- **Poor encapsulation**: The Character class exposes power and hp as public mutable fields despite providing getters and setters.
- **Functional mismatch**: The Elf.kick method decreases target HP by 1 instead of decreasing target power by 1 as specified in requirements.
- **Heavy external dependency**: The org.reflections library is introduced solely to dynamically instantiate a small, static set of subclasses, adding startup overhead and dependency footprint.
- **Lack of test coverage**: No tests exist for GameManager, CharacterFactory, or the randomized combat mechanics of King and Knight.

## Recommendations
- **Prevent Infinite Loops**: Introduce a maximum turn limit or round threshold in GameManager.fight to gracefully resolve non-lethal fights.
- **Refactor King and Knight**: Extract the shared randomized damage and instantiation logic into a shared intermediate base class (e.g., Noble) to reduce duplication.
- **Enforce Encapsulation**: Change the visibility of fields in the Character class from public to private or protected.
- **Align Elf Implementation**: Correct Elf.kick to reduce the opponent's power by 1 instead of HP when the opponent is stronger.
- **Optimize Character Factory**: Replace the Reflections library with a simple registry pattern or Java Service Provider Interface (SPI).
- **Expand Test Coverage**: Add unit tests for GameManager, CharacterFactory, and the randomized damage range of King and Knight.
