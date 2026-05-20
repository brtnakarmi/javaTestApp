package com.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PracticeApp {
    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", PracticeApp::handleHome);
        server.createContext("/login", PracticeApp::handleLogin);
        server.createContext("/dashboard", PracticeApp::handleDashboard);
        server.createContext("/contact", PracticeApp::handleContact);
        server.createContext("/submitted", PracticeApp::handleSubmitted);
        server.createContext("/iframe", PracticeApp::handleIframePage);
        server.createContext("/iframe-content", PracticeApp::handleIframeContent);
        server.createContext("/popup", PracticeApp::handlePopup);
        server.createContext("/upload", PracticeApp::handleUpload);
        server.createContext("/style.css", PracticeApp::handleCss);
        server.setExecutor(null);
        System.out.println("Practice app running at http://localhost:" + PORT);
        server.start();
    }

    private static void handleHome(HttpExchange exchange) throws IOException {
        String html = layout("Selenium Practice App", """
            <h1>Selenium Practice App</h1>
            <p>This tiny local app is designed for Selenium practice.</p>
            <nav class='cards'>
              <a data-testid='login-link' href='/login'>Login practice</a>
              <a data-testid='contact-link' href='/contact'>Form practice</a>
              <a data-testid='iframe-link' href='/iframe'>Iframe practice</a>
              <a data-testid='popup-link' href='/popup' target='_blank'>New window practice</a>
              <a data-testid='upload-link' href='/upload'>File upload practice</a>
            </nav>
        """);
        send(exchange, 200, html, "text/html");
    }

    private static void handleLogin(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> form = readForm(exchange);
            if ("qa@example.com".equals(form.get("email")) && "password123".equals(form.get("password"))) {
                redirect(exchange, "/dashboard?name=QA%20Student");
            } else {
                send(exchange, 401, loginPage("Invalid email or password"), "text/html");
            }
            return;
        }
        send(exchange, 200, loginPage(""), "text/html");
    }

    private static String loginPage(String error) {
        return layout("Login", """
            <h1>Login</h1>
            <p>Use <strong>qa@example.com</strong> / <strong>password123</strong></p>
            <div id='login-error' class='error'>%s</div>
            <form method='post' action='/login' data-testid='login-form'>
              <label>Email <input id='email' name='email' type='email' /></label>
              <label>Password <input id='password' name='password' type='password' /></label>
              <button id='login-button' type='submit'>Sign in</button>
            </form>
        """.formatted(error));
    }

    private static void handleDashboard(HttpExchange exchange) throws IOException {
        Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
        String name = query.getOrDefault("name", "QA Student");
        String html = layout("Dashboard", """
            <h1 id='welcome-message'>Welcome, %s</h1>
            <p class='success' data-testid='login-success'>You logged in successfully.</p>
            <input id='table-search' placeholder='Filter users by role' onkeyup='filterTable()' />
            <table id='users-table'>
              <thead><tr><th>Name</th><th>Role</th><th>Status</th></tr></thead>
              <tbody>
                <tr><td>Asha</td><td>QA Engineer</td><td>Active</td></tr>
                <tr><td>Ben</td><td>Developer</td><td>Active</td></tr>
                <tr><td>Mina</td><td>QA Lead</td><td>Inactive</td></tr>
              </tbody>
            </table>
            <button id='alert-button' onclick='alert("This is a browser alert")'>Show alert</button>
            <script>
              function filterTable() {
                const filter = document.getElementById('table-search').value.toLowerCase();
                document.querySelectorAll('#users-table tbody tr').forEach(row => {
                  row.style.display = row.innerText.toLowerCase().includes(filter) ? '' : 'none';
                });
              }
            </script>
        """.formatted(escape(name)));
        send(exchange, 200, html, "text/html");
    }

    private static void handleContact(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> form = readForm(exchange);
            redirect(exchange, "/submitted?fullName=" + encode(form.getOrDefault("fullName", "")) + "&topic=" + encode(form.getOrDefault("topic", "")));
            return;
        }
        String html = layout("Contact Form", """
            <h1>Contact Form</h1>
            <form method='post' action='/contact' data-testid='contact-form'>
              <label>Full name <input id='fullName' name='fullName' /></label>
              <label>Email <input id='contactEmail' name='email' type='email' /></label>
              <label>Topic
                <select id='topic' name='topic'>
                  <option value=''>Choose one</option>
                  <option value='support'>Support</option>
                  <option value='sales'>Sales</option>
                  <option value='feedback'>Feedback</option>
                </select>
              </label>
              <label><input id='newsletter' name='newsletter' type='checkbox' value='yes' /> Subscribe to newsletter</label>
              <fieldset>
                <legend>Priority</legend>
                <label><input name='priority' type='radio' value='low' /> Low</label>
                <label><input name='priority' type='radio' value='high' /> High</label>
              </fieldset>
              <label>Message <textarea id='message' name='message'></textarea></label>
              <button id='submit-contact' type='submit'>Submit</button>
            </form>
        """);
        send(exchange, 200, html, "text/html");
    }

    private static void handleSubmitted(HttpExchange exchange) throws IOException {
        Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
        String html = layout("Submitted", """
            <h1 id='submitted-heading'>Thanks, %s</h1>
            <p id='submitted-topic'>Topic: %s</p>
        """.formatted(escape(query.getOrDefault("fullName", "")), escape(query.getOrDefault("topic", ""))));
        send(exchange, 200, html, "text/html");
    }

    private static void handleIframePage(HttpExchange exchange) throws IOException {
        String html = layout("Iframe", """
            <h1>Iframe Practice</h1>
            <iframe id='practice-frame' src='/iframe-content' title='Practice iframe'></iframe>
        """);
        send(exchange, 200, html, "text/html");
    }

    private static void handleIframeContent(HttpExchange exchange) throws IOException {
        String html = """
            <!doctype html><html><body>
              <h2 id='iframe-title'>Inside the iframe</h2>
              <button id='iframe-button' onclick='document.getElementById("iframe-result").innerText="Clicked inside iframe"'>Iframe button</button>
              <p id='iframe-result'></p>
            </body></html>
        """;
        send(exchange, 200, html, "text/html");
    }

    private static void handlePopup(HttpExchange exchange) throws IOException {
        String html = layout("Popup Window", """
            <h1 id='popup-title'>New Window Page</h1>
            <p>This page opens in a new browser tab/window.</p>
        """);
        send(exchange, 200, html, "text/html");
    }

    private static void handleUpload(HttpExchange exchange) throws IOException {
        String html = layout("Upload", """
            <h1>Upload Practice</h1>
            <input id='file-upload' type='file' />
            <p id='file-name'></p>
            <script>
              document.getElementById('file-upload').addEventListener('change', e => {
                document.getElementById('file-name').innerText = e.target.files[0]?.name || '';
              });
            </script>
        """);
        send(exchange, 200, html, "text/html");
    }

    private static void handleCss(HttpExchange exchange) throws IOException {
        String css = """
            body { font-family: system-ui, Arial, sans-serif; margin: 40px; max-width: 850px; }
            label { display: block; margin: 14px 0; }
            input, select, textarea, button { padding: 8px; margin-top: 4px; }
            table { border-collapse: collapse; width: 100%; margin-top: 20px; }
            th, td { border: 1px solid #ccc; padding: 10px; }
            .cards a { display: inline-block; margin: 8px; padding: 12px; border: 1px solid #ccc; text-decoration: none; }
            .error { color: #b00020; min-height: 22px; }
            .success { color: green; }
            iframe { width: 100%; height: 180px; border: 1px solid #999; }
        """;
        send(exchange, 200, css, "text/css");
    }

    private static String layout(String title, String body) {
        return """
            <!doctype html>
            <html lang='en'>
            <head><meta charset='utf-8'><title>%s</title><link rel='stylesheet' href='/style.css'></head>
            <body><a href='/'>Home</a>%s</body>
            </html>
        """.formatted(title, body);
    }

    private static Map<String, String> readForm(HttpExchange exchange) throws IOException {
        String raw = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return parseQuery(raw);
    }

    private static Map<String, String> parseQuery(String raw) {
        Map<String, String> map = new HashMap<>();
        if (raw == null || raw.isBlank()) return map;
        for (String pair : raw.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = decode(parts[0]);
            String value = parts.length > 1 ? decode(parts[1]) : "";
            map.put(key, value);
        }
        return map;
    }

    private static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().add("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    private static void send(HttpExchange exchange, int status, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", contentType + "; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
