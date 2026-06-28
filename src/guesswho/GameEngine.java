package guesswho;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class GameEngine {
    private static final Set<String> VALID_ANSWERS = Set.of(
        "yes", "probably", "maybe", "probably_not", "no", "unknown"
    );
    private static final int MIN_QUESTIONS_BEFORE_GUESS = 4;
    private static final int SOFT_QUESTION_LIMIT = 10;
    private static final int HARD_QUESTION_LIMIT = 15;
    private static final int MAX_INFORMATION_CANDIDATES = 160;
    private static final double MIN_ACTIVE_PROBABILITY = 0.00015;
    private static final double MIN_INFORMATION_GAIN = 0.015;
    private static final double MIN_SPLIT_PROBABILITY = 0.08;
    private static final double IMPOSSIBLE_LOG_SCORE = -1_000_000.0;

    record Ranking(String name, double probability, int confidence) {}

    Map<String, Object> startRound(GameSession session) {
        initializeSession(session);
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(message(
            "machine",
            "Ready",
            "Think of a famous person or character. I will narrow it down."
        ));
        messages.addAll(askNext(session));
        session.updatedAt = Instant.now();
        return response(session, messages);
    }

    Map<String, Object> answerQuestion(GameSession session, String answer) {
        initializeSession(session);
        if (!VALID_ANSWERS.contains(answer)) {
            throw new IllegalArgumentException("Invalid answer.");
        }
        if (session.currentQuestion == null) {
            throw new IllegalArgumentException("There is no active question to answer.");
        }

        String questionId = session.currentQuestion.id();
        session.answers.put(questionId, answer);
        updateProbabilities(session, questionId, answer);
        pruneCandidates(session);
        session.currentQuestion = null;

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(message("player", "You", answerLabel(answer)));
        messages.addAll(askNext(session));
        session.updatedAt = Instant.now();
        return response(session, messages);
    }

    Map<String, Object> resolveGuess(GameSession session, String result) {
        initializeSession(session);
        if (!Set.of("correct", "wrong").contains(result)) {
            throw new IllegalArgumentException("Invalid guess result.");
        }
        if (session.pendingGuess == null) {
            throw new IllegalArgumentException("There is no active guess to resolve.");
        }

        String guessedName = session.pendingGuess;
        List<Map<String, Object>> messages = new ArrayList<>();
        if (result.equals("correct")) {
            session.solved = true;
            session.pendingGuess = null;
            messages.add(message("result", "Solved", guessedName + " was the answer."));
        } else {
            session.wrongGuesses.add(guessedName);
            session.logScores.put(guessedName, IMPOSSIBLE_LOG_SCORE);
            session.activeCandidates.remove(guessedName);
            session.pendingGuess = null;
            session.forceQuestionAfterWrong = true;
            messages.add(message("player", "You", "Not them"));
            messages.add(message(
                "machine",
                "Recalculating",
                "That rules out one strong possibility. Let me narrow it further."
            ));
            messages.addAll(askNext(session));
        }

        session.updatedAt = Instant.now();
        return response(session, messages);
    }

    private void initializeSession(GameSession session) {
        if (session.initialized) {
            return;
        }
        for (GameData.Person person : GameData.PEOPLE) {
            session.logScores.put(person.name(), 0.0);
            session.activeCandidates.add(person.name());
        }
        session.initialized = true;
    }

    private List<Map<String, Object>> askNext(GameSession session) {
        pruneCandidates(session);
        List<Ranking> rankings = rankings(session);
        if (rankings.isEmpty()) {
            resetCandidates(session);
            rankings = rankings(session);
        }

        Ranking leader = rankings.getFirst();
        Ranking runnerUp = rankings.size() > 1 ? rankings.get(1) : null;
        GameData.Question nextQuestion = selectBestQuestion(session, rankings);

        if (!session.forceQuestionAfterWrong && makeGuess(session, leader, runnerUp, nextQuestion)) {
            session.pendingGuess = leader.name();
            session.currentQuestion = null;
            return List.of(message(
                "guess",
                "My guess",
                "I think you are thinking of " + leader.name() + "."
            ));
        }

        if (nextQuestion == null) {
            session.pendingGuess = leader.name();
            session.currentQuestion = null;
            return List.of(message(
                "guess",
                "Best remaining guess",
                "My strongest remaining answer is " + leader.name() + "."
            ));
        }

        session.forceQuestionAfterWrong = false;
        session.currentQuestion = nextQuestion;
        session.asked.add(nextQuestion.id());
        return List.of(message("machine", "Question", nextQuestion.text()));
    }

    private boolean makeGuess(
        GameSession session,
        Ranking leader,
        Ranking runnerUp,
        GameData.Question nextQuestion
    ) {
        if (session.wrongGuesses.contains(leader.name())) {
            return false;
        }
        int asked = session.asked.size();
        double gap = runnerUp == null ? 1.0 : leader.probability() - runnerUp.probability();
        boolean veryLikely = asked >= MIN_QUESTIONS_BEFORE_GUESS && leader.probability() >= 0.72;
        boolean largeGap = asked >= MIN_QUESTIONS_BEFORE_GUESS
            && leader.probability() >= 0.48
            && gap >= 0.28;
        boolean lateEnough = asked >= SOFT_QUESTION_LIMIT && leader.probability() >= 0.28;
        boolean noUsefulQuestion = nextQuestion == null || asked >= HARD_QUESTION_LIMIT;
        return veryLikely || largeGap || lateEnough || noUsefulQuestion;
    }

    private void updateProbabilities(GameSession session, String questionId, String answer) {
        if (answer.equals("unknown")) {
            return;
        }
        for (String personName : new ArrayList<>(session.activeCandidates)) {
            GameData.Person person = GameData.PERSON_BY_NAME.get(personName);
            double likelihood = answerLikelihood(answer, person.probabilityFor(questionId));
            double currentScore = session.logScores.getOrDefault(personName, IMPOSSIBLE_LOG_SCORE);
            session.logScores.put(personName, currentScore + Math.log(Math.max(0.001, likelihood)));
        }
    }

    private void pruneCandidates(GameSession session) {
        List<Ranking> rankings = normalizeScores(session, new ArrayList<>(session.activeCandidates));
        if (rankings.size() <= 8) {
            return;
        }
        LinkedHashSet<String> kept = new LinkedHashSet<>();
        rankings.stream()
            .filter(ranking -> ranking.probability() >= MIN_ACTIVE_PROBABILITY)
            .limit(MAX_INFORMATION_CANDIDATES)
            .forEach(ranking -> kept.add(ranking.name()));
        rankings.stream().limit(8).forEach(ranking -> kept.add(ranking.name()));
        session.activeCandidates.clear();
        session.activeCandidates.addAll(kept);
    }

    private GameData.Question selectBestQuestion(GameSession session, List<Ranking> rankings) {
        List<Ranking> viableRankings = rankings.stream()
            .filter(ranking -> ranking.probability() >= MIN_ACTIVE_PROBABILITY)
            .limit(MAX_INFORMATION_CANDIDATES)
            .toList();
        if (viableRankings.size() < 2) {
            return null;
        }
        return GameData.QUESTIONS.stream()
            .filter(question -> !session.asked.contains(question.id()))
            .filter(question -> !isNearDuplicateTopic(session, question))
            .map(question -> Map.entry(question, calculateInformationGain(question, viableRankings)))
            .filter(entry -> entry.getValue() >= MIN_INFORMATION_GAIN)
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElseGet(() -> fallbackQuestion(session, viableRankings));
    }

    private GameData.Question fallbackQuestion(GameSession session, List<Ranking> rankings) {
        return GameData.QUESTIONS.stream()
            .filter(question -> !session.asked.contains(question.id()))
            .map(question -> Map.entry(question, splitQuality(question, rankings)))
            .filter(entry -> entry.getValue() >= MIN_SPLIT_PROBABILITY)
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    private boolean isNearDuplicateTopic(GameSession session, GameData.Question question) {
        long askedFromSameTopic = session.asked.stream()
            .map(GameData.QUESTION_BY_ID::get)
            .filter(askedQuestion -> askedQuestion != null)
            .filter(askedQuestion -> askedQuestion.topic().equals(question.topic()))
            .count();
        return askedFromSameTopic >= 2;
    }

    private double calculateInformationGain(GameData.Question question, List<Ranking> rankings) {
        double currentEntropy = calculateEntropy(rankings.stream().map(Ranking::probability).toList());
        Map<String, List<Double>> posteriorByAnswer = new LinkedHashMap<>();
        Map<String, Double> answerProbabilities = new LinkedHashMap<>();
        for (String answer : List.of("yes", "probably", "maybe", "probably_not", "no")) {
            double answerProbability = 0.0;
            List<Double> unnormalizedPosterior = new ArrayList<>();
            for (Ranking ranking : rankings) {
                GameData.Person person = GameData.PERSON_BY_NAME.get(ranking.name());
                double likelihood = answerLikelihood(answer, person.probabilityFor(question.id()));
                double weighted = ranking.probability() * likelihood;
                unnormalizedPosterior.add(weighted);
                answerProbability += weighted;
            }
            if (answerProbability > 0.0) {
                double total = answerProbability;
                posteriorByAnswer.put(
                    answer,
                    unnormalizedPosterior.stream().map(value -> value / total).toList()
                );
                answerProbabilities.put(answer, answerProbability);
            }
        }
        double yesSide = answerProbabilities.getOrDefault("yes", 0.0)
            + answerProbabilities.getOrDefault("probably", 0.0);
        double noSide = answerProbabilities.getOrDefault("no", 0.0)
            + answerProbabilities.getOrDefault("probably_not", 0.0);
        if (yesSide < MIN_SPLIT_PROBABILITY || noSide < MIN_SPLIT_PROBABILITY) {
            return 0.0;
        }
        double expectedEntropy = 0.0;
        for (Map.Entry<String, Double> entry : answerProbabilities.entrySet()) {
            expectedEntropy += entry.getValue() * calculateEntropy(posteriorByAnswer.get(entry.getKey()));
        }
        return currentEntropy - expectedEntropy;
    }

    private double splitQuality(GameData.Question question, List<Ranking> rankings) {
        double yesSide = rankings.stream()
            .mapToDouble(ranking -> {
                GameData.Person person = GameData.PERSON_BY_NAME.get(ranking.name());
                return ranking.probability() * person.probabilityFor(question.id());
            })
            .sum();
        return 0.5 - Math.abs(0.5 - yesSide);
    }

    private double calculateEntropy(List<Double> probabilities) {
        return probabilities.stream()
            .filter(probability -> probability > 0)
            .mapToDouble(probability -> -probability * (Math.log(probability) / Math.log(2)))
            .sum();
    }

    private List<Ranking> rankings(GameSession session) {
        return normalizeScores(session, new ArrayList<>(session.activeCandidates));
    }

    private List<Ranking> normalizeScores(GameSession session, List<String> candidateNames) {
        if (candidateNames.isEmpty()) {
            return List.of();
        }
        double maxLogScore = candidateNames.stream()
            .mapToDouble(name -> session.logScores.getOrDefault(name, IMPOSSIBLE_LOG_SCORE))
            .max()
            .orElse(0);
        Map<String, Double> weights = new LinkedHashMap<>();
        double totalWeight = 0.0;
        for (String name : candidateNames) {
            if (session.wrongGuesses.contains(name)) {
                continue;
            }
            double weight = Math.exp(session.logScores.getOrDefault(name, IMPOSSIBLE_LOG_SCORE) - maxLogScore);
            weights.put(name, weight);
            totalWeight += weight;
        }
        if (totalWeight <= 0.0) {
            return List.of();
        }
        double total = totalWeight;
        return weights.entrySet().stream()
            .map(entry -> {
                double probability = entry.getValue() / total;
                return new Ranking(entry.getKey(), probability, (int) Math.round(probability * 100));
            })
            .sorted(Comparator.comparingDouble(Ranking::probability).reversed())
            .toList();
    }

    private void resetCandidates(GameSession session) {
        session.activeCandidates.clear();
        for (GameData.Person person : GameData.PEOPLE) {
            if (!session.wrongGuesses.contains(person.name())) {
                session.activeCandidates.add(person.name());
            }
        }
    }

    private double answerLikelihood(String answer, double yesProbability) {
        double p = Math.max(0.02, Math.min(0.98, yesProbability));
        return switch (answer) {
            case "yes" -> p;
            case "probably" -> (0.75 * p) + (0.25 * (1.0 - p));
            case "maybe" -> 0.22 + (0.56 * (1.0 - Math.abs(p - 0.5) * 2.0));
            case "probably_not" -> (0.25 * p) + (0.75 * (1.0 - p));
            case "no" -> 1.0 - p;
            case "unknown" -> 1.0;
            default -> throw new IllegalStateException("Unexpected answer.");
        };
    }

    private String answerLabel(String answer) {
        return switch (answer) {
            case "yes" -> "Yes";
            case "probably" -> "Probably";
            case "maybe" -> "Maybe";
            case "probably_not" -> "Probably not";
            case "no" -> "No";
            case "unknown" -> "Don't know";
            default -> throw new IllegalStateException("Unexpected answer.");
        };
    }

    private Map<String, Object> response(
        GameSession session,
        List<Map<String, Object>> messages
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sessionId", session.id);
        payload.put("mode", mode(session));
        payload.put("currentQuestion", questionPayload(session.currentQuestion));
        payload.put("pendingGuess", session.pendingGuess);
        payload.put("stats", stats(session));
        payload.put("messages", messages);
        return payload;
    }

    private Map<String, Object> stats(GameSession session) {
        List<Ranking> rankings = rankings(session);
        Ranking leader = rankings.getFirst();
        List<Map<String, Object>> candidates = rankings.stream()
            .limit(4)
            .map(person -> Map.<String, Object>of(
                "name", person.name(),
                "confidence", Math.max(1, person.confidence())
            ))
            .toList();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("confidence", Math.min(99, Math.max(8, leader.confidence())));
        stats.put("questionCount", session.asked.size());
        stats.put(
            "guessCount",
            session.wrongGuesses.size() + (session.pendingGuess == null ? 0 : 1)
        );
        stats.put("knowledgeSize", GameData.PEOPLE.size());
        stats.put("candidates", candidates);
        return stats;
    }

    private String mode(GameSession session) {
        if (session.solved) {
            return "start";
        }

        if (session.pendingGuess != null) {
            return "guess";
        }

        return "answer";
    }

    private Map<String, Object> questionPayload(GameData.Question question) {
        if (question == null) {
            return null;
        }

        return Map.of(
            "id", question.id(),
            "text", question.text()
        );
    }

    private Map<String, Object> message(String type, String label, String text) {
        return Map.of(
            "type", type,
            "label", label,
            "text", text
        );
    }
}
