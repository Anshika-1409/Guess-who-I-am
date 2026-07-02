package guesswho;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class GameEngineTest {

    public static void main(String[] args) {

        peopleExist();
        questionsExist();

        startsWithQuestion();
        recordsAnswer();
        rejectsInvalidAnswer();
        wrongGuessForcesQuestion();

        noDuplicatePeople();
        everyQuestionUsed();
        probabilitiesValid();
        everyPersonHasEnoughTraits();

        guessesKnownPeople();
        solvesFocusedGroupsQuickly();

        System.out.println("All Java backend tests passed.");
    }

    private static void startsWithQuestion() {
        GameEngine engine = new GameEngine();
        GameSession session = new GameSession();

        Map<String, Object> response = engine.startRound(session);

        require(response.get("mode").equals("answer"),
                "Round should start in answer mode.");

        require(session.currentQuestion != null,
                "Round should have an active question.");

        require(session.asked.size() == 1,
                "Round should count the first question.");
    }

    private static void recordsAnswer() {
        GameEngine engine = new GameEngine();
        GameSession session = new GameSession();

        engine.startRound(session);
        engine.answerQuestion(session, "yes");

        require(session.answers.size() == 1,
                "Answer should be stored.");

        require(session.asked.size() == 2,
                "Engine should ask another question.");
    }

    private static void rejectsInvalidAnswer() {
        GameEngine engine = new GameEngine();
        GameSession session = new GameSession();

        engine.startRound(session);

        try {
            engine.answerQuestion(session, "sometimes");
            throw new AssertionError("Invalid answer should be rejected.");
        } catch (IllegalArgumentException expected) {

            require(
                expected.getMessage().contains("Invalid answer"),
                "Unexpected validation error."
            );
        }
    }

    private static void wrongGuessForcesQuestion() {
        GameEngine engine = new GameEngine();
        GameSession session = new GameSession();

        Map<String, Object> response = engine.startRound(session);

        GameData.Person target = person("Taylor Swift");

        while (!response.get("mode").equals("guess")) {
            response = engine.answerQuestion(
                    session,
                    answerFor(target, session.currentQuestion)
            );
        }

        response = engine.resolveGuess(session, "wrong");

        require(
                response.get("mode").equals("answer"),
                "Wrong guess should return to answer mode."
        );

        require(
                session.currentQuestion != null,
                "Wrong guess should produce another question."
        );

        require(
                session.wrongGuesses.size() == 1,
                "Wrong guess should be remembered."
        );
    }

    /**
     * Test every person in the database.
     */
    private static void guessesKnownPeople() {

        for (GameData.Person person : GameData.PEOPLE) {

            require(
                    playUntilSolved(person.name()),
                    "Engine failed to identify " + person.name()
            );
        }
    }

    private static boolean playUntilSolved(String targetName) {

        GameEngine engine = new GameEngine();
        GameSession session = new GameSession();

        GameData.Person target = person(targetName);

        Map<String, Object> response = engine.startRound(session);

        // Increased limit for larger databases
        for (int turn = 0; turn < 80; turn++) {

            if (response.get("mode").equals("guess")) {

                if (targetName.equals(session.pendingGuess)) {

                    response = engine.resolveGuess(session, "correct");

                    return response.get("mode").equals("start");
                }

                response = engine.resolveGuess(session, "wrong");
                continue;
            }

            response = engine.answerQuestion(
                    session,
                    answerFor(target, session.currentQuestion)
            );
        }

        return false;
    }

    private static void solvesFocusedGroupsQuickly() {
        assertSolvedWithin("CarryMinati", 15);
        assertSolvedWithin("Triggered Insaan", 15);
        assertSolvedWithin("Bhuvan Bam", 15);
        assertSolvedWithin("Ashish Chanchlani", 15);
        assertSolvedWithin("Tech Burner", 15);
        assertSolvedWithin("Mrwhosetheboss", 15);
        assertSolvedWithin("Shah Rukh Khan", 16);
        assertSolvedWithin("Ranbir Kapoor", 16);
        assertSolvedWithin("Alia Bhatt", 16);
        assertSolvedWithin("Allu Arjun", 16);
        assertSolvedWithin("Prabhas", 16);
        assertSolvedWithin("Yash", 16);
    }

    private static void assertSolvedWithin(String targetName, int maxQuestions) {
        int questionCount = questionsNeededToSolve(targetName);
        require(
            questionCount <= maxQuestions,
            targetName + " needed " + questionCount
                + " questions; expected at most " + maxQuestions
        );
    }

    private static int questionsNeededToSolve(String targetName) {
        GameEngine engine = new GameEngine();
        GameSession session = new GameSession();
        GameData.Person target = person(targetName);

        Map<String, Object> response = engine.startRound(session);

        for (int turn = 0; turn < 80; turn++) {
            if (response.get("mode").equals("guess")) {
                if (targetName.equals(session.pendingGuess)) {
                    return session.asked.size();
                }

                response = engine.resolveGuess(session, "wrong");
                continue;
            }

            response = engine.answerQuestion(
                session,
                answerFor(target, session.currentQuestion)
            );
        }

        throw new AssertionError("Engine failed to identify " + targetName);
    }

    private static String answerFor(
            GameData.Person target,
            GameData.Question question) {

        double probability = target.probabilityFor(question.id());

        if (probability >= 0.75) {
            return "yes";
        }

        if (probability <= 0.25) {
            return "no";
        }

        return "maybe";
    }

    /**
     * Ensure people exist.
     */
    private static void peopleExist() {

        require(
                !GameData.PEOPLE.isEmpty(),
                "People list is empty."
        );
    }

    /**
     * Ensure questions exist.
     */
    private static void questionsExist() {

        require(
                !GameData.QUESTIONS.isEmpty(),
                "Question list is empty."
        );
    }

    /**
     * Ensure no duplicate names.
     */
    private static void noDuplicatePeople() {

        Set<String> names = new HashSet<>();

        for (GameData.Person person : GameData.PEOPLE) {

            require(
                    names.add(person.name()),
                    "Duplicate person found: " + person.name()
            );
        }
    }

    /**
     * Every question should be used by at least one person.
     */
    private static void everyQuestionUsed() {

        for (GameData.Question question : GameData.QUESTIONS) {

            boolean used = GameData.PEOPLE.stream()
                    .anyMatch(person ->
                            person.probabilityFor(question.id()) > 0);

            require(
                    used,
                    "Unused question: " + question.id()
            );
        }
    }

    /**
     * Validate probability values.
     */
    private static void probabilitiesValid() {

        for (GameData.Person person : GameData.PEOPLE) {

            person.traits().values().forEach(probability ->

                    require(
                            probability >= 0.0 && probability <= 1.0,
                            person.name()
                                    + " has invalid probability: "
                                    + probability
                    )
            );
        }
    }

    /**
     * Every person should have enough traits for the current seed data.
     */
    private static void everyPersonHasEnoughTraits() {

        for (GameData.Person person : GameData.PEOPLE) {

            require(
                    person.traits().size() >= 4,
                    person.name() + " has too few traits."
            );
        }
    }

    private static GameData.Person person(String name) {

        return GameData.PEOPLE.stream()
                .filter(person -> person.name().equals(name))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("Person not found: " + name));
    }

    private static void require(boolean condition, String message) {

        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
