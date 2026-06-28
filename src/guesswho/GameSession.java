package guesswho;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class GameSession {
    final String id = UUID.randomUUID().toString();
    final Map<String, String> answers = new LinkedHashMap<>();
    final Map<String, Double> logScores = new LinkedHashMap<>();
    final Set<String> activeCandidates = new LinkedHashSet<>();
    final List<String> asked = new ArrayList<>();
    final List<String> wrongGuesses = new ArrayList<>();
    GameData.Question currentQuestion;
    String pendingGuess;
    boolean initialized;
    boolean forceQuestionAfterWrong;
    boolean solved;
    final Instant createdAt = Instant.now();
    Instant updatedAt = createdAt;
}
