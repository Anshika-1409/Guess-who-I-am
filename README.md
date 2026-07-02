# Guess Who I Am

An Akinator-style guessing game with a Java backend and browser frontend. It does
not require an LLM or any external API.

## How it works

- Every known person or character has a probabilistic trait profile.
- Player answers update the posterior probability of every active candidate.
- The engine measures expected information gain for every unused question.
- It asks the question expected to reduce uncertainty the most.
- It guesses when one candidate has enough probability or top-two separation.
- Incorrect guesses are eliminated and the questioning continues.
- Very unlikely candidates are pruned so question selection stays fast.

The starter knowledge base includes 100 personalities and a 219-question catalog
covering music, film, sports, business, politics, historical figures, internet
creators, anime, gaming, K-pop, franchises, awards, appearance traits, and
fictional characters.

## Requirements

- Java 25 or newer

## Run

```powershell
.\run-java.ps1
```

Open [http://127.0.0.1:4173/](http://127.0.0.1:4173/).

If you open `index.html` with VS Code Live Server on port `5500`, keep the Java
server running too. The frontend will automatically send API calls to
`http://127.0.0.1:4173`.

If the terminal says the address is already in use, the game server is probably
already running. Open [http://127.0.0.1:4173/](http://127.0.0.1:4173/) instead
of starting another copy.

## Deploy

This project can be deployed as one Java web service because the backend serves
both the API and the static frontend.

Recommended path:

1. Push this folder to a GitHub repository.
2. Create a new Render Web Service from that repository.
3. Choose Docker as the runtime.
4. Use `/api/health` as the health check path.
5. Deploy.

The included `Dockerfile` compiles the Java source and serves the app. The
included `render.yaml` provides the same settings as infrastructure config.

## Test

```powershell
.\test-java.ps1
```

The tests simulate complete rounds for several different targets.

## API

- `POST /api/games` starts a new game session.
- `POST /api/games/{sessionId}/answer` submits `yes`, `no`, `maybe`, or `unknown`.
- `POST /api/games/{sessionId}/guess` submits `correct` or `wrong`.
- `GET /api/health` reports the engine type and knowledge-base size.

Game sessions currently live in server memory. Add people and questions in
`src/guesswho/GameData.java`.

Supported answers are `yes`, `probably`, `maybe`, `probably_not`, `no`, and
`unknown`.
