const labels = {
  yes: "Yes",
  probably: "Probably",
  no: "No",
  probably_not: "Probably not",
  maybe: "Maybe",
  unknown: "Don't know",
};

let sessionId = null;
let busy = false;
let knowledgeSize = null;
const API_BASE_URL = getApiBaseUrl();

const conversation = document.querySelector("#conversation");
const answerDock = document.querySelector("#answerDock");
const guessDock = document.querySelector("#guessDock");
const startButton = document.querySelector("#startButton");
const resetButton = document.querySelector("#resetButton");
const correctButton = document.querySelector("#correctButton");
const wrongButton = document.querySelector("#wrongButton");
const candidateList = document.querySelector("#candidateList");
const confidenceText = document.querySelector("#confidenceText");
const confidenceBar = document.querySelector("#confidenceBar");
const turnCount = document.querySelector("#turnCount");
const guessCount = document.querySelector("#guessCount");
const knowledgeCount = document.querySelector("#knowledgeCount");

function setBusy(isBusy) {
  busy = isBusy;

  document.querySelectorAll("button").forEach((button) => {
    button.disabled = isBusy;
  });
}

function setMode(mode) {
  answerDock.classList.toggle("hidden", mode !== "answer");
  guessDock.classList.toggle("hidden", mode !== "guess");
  startButton.classList.toggle("hidden", mode !== "start");
}

function addMessage(type, label, text) {
  const message = document.createElement("article");
  const messageLabel = document.createElement("small");
  const messageText = document.createElement("p");

  message.className = `message ${type}`;
  messageLabel.textContent = label;
  messageText.textContent = text;
  message.append(messageLabel, messageText);
  conversation.appendChild(message);
  conversation.scrollTop = conversation.scrollHeight;
}

function showError(error) {
  addMessage("result", "Connection", error.message || "The game server did not respond.");
}

function getApiBaseUrl() {
  const isJavaServer =
    window.location.hostname === "127.0.0.1" && window.location.port === "4173";

  if (isJavaServer) {
    return "";
  }

  return "http://127.0.0.1:4173";
}

function updateStats(stats) {
  confidenceText.textContent = `${stats.confidence}%`;
  confidenceBar.style.width = `${stats.confidence}%`;
  turnCount.textContent = stats.questionCount;
  guessCount.textContent = stats.guessCount;
  if (Number.isInteger(stats.knowledgeSize)) {
    knowledgeSize = stats.knowledgeSize;
  }
  knowledgeCount.textContent = knowledgeSize ?? "--";

  candidateList.replaceChildren(
    ...stats.candidates.map((person, index) => {
      const item = document.createElement("div");
      const name = document.createElement("strong");
      const confidence = document.createElement("span");

      item.className = "candidate";
      name.textContent = `Possibility ${index + 1}`;
      confidence.textContent = `${person.confidence}%`;
      item.append(name, confidence);

      return item;
    })
  );
}

async function callApi(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
    },
    ...options,
  });

  const text = await response.text();
  const payload = text ? JSON.parse(text) : {};

  if (!response.ok) {
    throw new Error(payload.error || "The game server returned an error.");
  }

  return payload;
}

async function loadHealth() {
  try {
    const payload = await callApi("/api/health");
    if (Number.isInteger(payload.knowledgeSize)) {
      knowledgeSize = payload.knowledgeSize;
      knowledgeCount.textContent = knowledgeSize;
    }
  } catch (error) {
    knowledgeCount.textContent = "--";
  }
}

function applyGameUpdate(payload) {
  sessionId = payload.sessionId;

  payload.messages.forEach((message) => {
    addMessage(message.type, message.label, message.text);
  });

  updateStats(payload.stats);
  setMode(payload.mode);

  if (payload.mode === "start") {
    startButton.textContent = "Play again";
  }
}

async function startGame() {
  if (busy) {
    return;
  }

  setBusy(true);
  conversation.innerHTML = "";
  startButton.textContent = "Starting...";

  try {
    const payload = await callApi("/api/games", {
      method: "POST",
      body: JSON.stringify({}),
    });
    applyGameUpdate(payload);
  } catch (error) {
    setMode("start");
    showError(error);
    startButton.textContent = "Try again";
  } finally {
    setBusy(false);
  }
}

async function answerQuestion(answer) {
  if (busy || !sessionId) {
    return;
  }

  setBusy(true);
  addMessage("player", "You", labels[answer]);

  try {
    const payload = await callApi(`/api/games/${sessionId}/answer`, {
      method: "POST",
      body: JSON.stringify({ answer }),
    });

    // The server owns the full transcript, including player answers.
    conversation.lastElementChild?.remove();
    applyGameUpdate(payload);
  } catch (error) {
    showError(error);
  } finally {
    setBusy(false);
  }
}

async function resolveGuess(result) {
  if (busy || !sessionId) {
    return;
  }

  setBusy(true);

  try {
    const payload = await callApi(`/api/games/${sessionId}/guess`, {
      method: "POST",
      body: JSON.stringify({ result }),
    });
    applyGameUpdate(payload);
  } catch (error) {
    showError(error);
  } finally {
    setBusy(false);
  }
}

document.querySelectorAll(".answer-button").forEach((button) => {
  button.addEventListener("click", () => answerQuestion(button.dataset.answer));
});

startButton.addEventListener("click", startGame);
resetButton.addEventListener("click", startGame);
correctButton.addEventListener("click", () => resolveGuess("correct"));
wrongButton.addEventListener("click", () => resolveGuess("wrong"));

setMode("start");
updateStats({
  confidence: 12,
  questionCount: 0,
  guessCount: 0,
  knowledgeSize: null,
  candidates: [
    { name: "", confidence: 3 },
    { name: "", confidence: 3 },
    { name: "", confidence: 3 },
    { name: "", confidence: 3 },
  ],
});
addMessage("machine", "Table open", "Pick any celebrity, athlete, creator, actor, or icon.");
loadHealth();
