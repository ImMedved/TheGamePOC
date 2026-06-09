The application is a multiplayer game with both clients running locally. Docker is used to launch the game. Three peer nodes are launched, one of which acts as a validator/judge, monitoring for desynchronization and verifying the validity of moves. The moves themselves are implemented using the mirco-step architecture, where each step represents one tick of the game core.

# Launch

To launch, clone the repository locally on Windows 11, start the Docker daemon, and run the following command from the admin PowerShell:
```powershell -ExecutionPolicy Bypass -File .\run-docker.ps1```

# File Structure

The project is divided into four modules, including logging and an entry point.

## Input
This module receives data locally from the keyboard and mouse. It processes click events and receives the raw mouse position to convert into intents. The intents for one tick are then generated as a snapshot, which is read from the kernel and subsequently updated. The Input module is unaware of the existence of other modules; their network input operates similarly and transmits the same data structure.

## Core
The loop that processes the world state. It generates a WorldState snapshot for the next frame based on the intent of each player. At the end of each core tick, the staggeng and actual snapshots are swapped, allowing the other modules to receive the new data. Upon receiving an intent from one of the players, the main loop triggers the command processor, which calls the appropriate command and processes the input. It also handles long-running timed events, such as bullet inflight, cooldowns, and so on.

## Render
This renders the world state to the screen by interpreting data from the WorldState snapshot across several layers (background, level, characters, hud). It has its own independent clock and can run faster than the core, resulting in a smoother image (it is recommended to use the same clock frequency). All textures are transferred as vertex objects and stored in memory by keys. Animations are implemented using repeating sequences. Each layer has its own independent renderer, and objects like levels, characters, abilities, and so on are modular and can be easily modified and expanded. The layer isn't completely isolated from the core; logic leaks through several hooks that disrupt the architecture.

## Network
The network module implements the game's peer-to-peer network subsystem, enabling data exchange between nodes without a central server. It includes components for the transport layer, packet serialization, cryptographic message signing, game state synchronization using the lockstep model, as well as mechanisms for generating VDF entropy and validating player actions. Each network packet is signed with the sender's private key and verified by the recipient, preventing identity spoofing and data modification during transmission.

Data transmission begins with the formation of a player's local input, which is encoded, signed, and sent to all connected nodes via P2P connections. The recipient verifies the digital signature, saves the input in the synchronization system, and, if a validator, additionally verifies the correctness of the action relative to the game logic. After processing, both clients perform the same simulation step and, if necessary, send a state snapshot (STATE_FRAME) for additional verification. When an invalid signature, game rule violation, or desynchronization is detected, the validator distributes a VOID packet notifying all participants that the current game session has been cancelled.

