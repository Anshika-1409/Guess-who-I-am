package guesswho;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GameServer {
    private static final Pattern ANSWER_ROUTE = Pattern.compile("^/api/games/([^/]+)/answer$");
    private static final Pattern GUESS_ROUTE = Pattern.compile("^/api/games/([^/]+)/guess$");
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "4173"));
    private static final String HOST = System.getenv().getOrDefault("HOST", "0.0.0.0");
    private static final Path PUBLIC_DIR = Path.of("").toAbsolutePath().normalize();

    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();
    private final GameEngine engine = new GameEngine();

    public static void main(String[] args) throws IOException {
        new GameServer().start();
    }

    private void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(HOST, PORT), 0);
        server.createContext("/", this::handle);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();
        System.out.printf("Guess Who I Am is running at http://%s:%d/%n", HOST, PORT);
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            addCorsHeaders(exchange);

            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();

            if (path.startsWith("/api/")) {
                handleApi(exchange, path);
            } else {
                serveStatic(exchange, path);
            }
        } catch (IllegalArgumentException error) {
            sendJson(exchange, 400, Map.of("error", error.getMessage()));
        } catch (Exception error) {
            error.printStackTrace();
            sendJson(exchange, 500, Map.of("error", "The game server encountered an error."));
        } finally {
            exchange.close();
        }
    }

    private void handleApi(HttpExchange exchange, String path) throws IOException {
        if (exchange.getRequestMethod().equals("GET") && path.equals("/api/health")) {
            sendJson(exchange, 200, Map.of(
                "status", "ok",
                "engine", "probabilistic",
                "knowledgeSize", GameData.PEOPLE.size()
            ));
            return;
        }

        if (exchange.getRequestMethod().equals("POST") && path.equals("/api/games")) {
            GameSession session = new GameSession();
            sessions.put(session.id, session);
            sendJson(exchange, 201, engine.startRound(session));
            return;
        }

        Matcher answerMatcher = ANSWER_ROUTE.matcher(path);
        if (exchange.getRequestMethod().equals("POST") && answerMatcher.matches()) {
            GameSession session = requireSession(answerMatcher.group(1));
            Map<String, Object> body = readJsonBody(exchange);
            sendJson(exchange, 200, engine.answerQuestion(session, stringField(body, "answer")));
            return;
        }

        Matcher guessMatcher = GUESS_ROUTE.matcher(path);
        if (exchange.getRequestMethod().equals("POST") && guessMatcher.matches()) {
            GameSession session = requireSession(guessMatcher.group(1));
            Map<String, Object> body = readJsonBody(exchange);
            sendJson(exchange, 200, engine.resolveGuess(session, stringField(body, "result")));
            return;
        }

        sendJson(exchange, 404, Map.of("error", "API route not found."));
    }

    private GameSession requireSession(String sessionId) {
        GameSession session = sessions.get(sessionId);

        if (session == null) {
            throw new IllegalArgumentException("Game session not found.");
        }

        return session;
    }

    private Map<String, Object> readJsonBody(HttpExchange exchange) throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            String body = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            return Json.parseObject(body.isBlank() ? "{}" : body);
        }
    }

    private String stringField(Map<String, Object> body, String name) {
        Object value = body.get(name);

        if (!(value instanceof String text) || text.isBlank()) {
            throw new IllegalArgumentException("Field '" + name + "' is required.");
        }

        return text;
    }

    private void serveStatic(HttpExchange exchange, String requestPath) throws IOException {
        if (!exchange.getRequestMethod().equals("GET") && !exchange.getRequestMethod().equals("HEAD")) {
            sendText(exchange, 405, "Method not allowed", "text/plain; charset=utf-8");
            return;
        }

        String relativePath = requestPath.equals("/") ? "index.html" : requestPath.substring(1);
        Path file = PUBLIC_DIR.resolve(relativePath).normalize();

        if (!file.startsWith(PUBLIC_DIR) || !Files.isRegularFile(file)) {
            sendText(exchange, 404, "Not found", "text/plain; charset=utf-8");
            return;
        }

        byte[] data = Files.readAllBytes(file);
        exchange.getResponseHeaders().set("Content-Type", contentType(file));
        exchange.sendResponseHeaders(200, exchange.getRequestMethod().equals("HEAD") ? -1 : data.length);

        if (!exchange.getRequestMethod().equals("HEAD")) {
            exchange.getResponseBody().write(data);
        }
    }

    private void sendJson(HttpExchange exchange, int status, Object payload) throws IOException {
        sendText(exchange, status, Json.stringify(payload), "application/json; charset=utf-8");
    }

    private void sendText(HttpExchange exchange, int status, String body, String contentType) throws IOException {
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, data.length);
        exchange.getResponseBody().write(data);
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private String contentType(Path file) {
        String name = file.getFileName().toString().toLowerCase();

        if (name.endsWith(".html")) return "text/html; charset=utf-8";
        if (name.endsWith(".css")) return "text/css; charset=utf-8";
        if (name.endsWith(".js")) return "text/javascript; charset=utf-8";
        if (name.endsWith(".svg")) return "image/svg+xml; charset=utf-8";
        return "application/octet-stream";
    }
}
