package com.pata3d.mira;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final String TAG = "Mira";
    private static final int PORT = 8765;

    private WebView webView;
    private LocalAssetServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        server = new LocalAssetServer();
        server.start();

        webView = new WebView(this);
        webView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setDatabasePath(getDir("webview-db", MODE_PRIVATE).getPath());
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage message) {
                Log.d(TAG, message.message());
                return true;
            }
        });
        webView.loadUrl("http://127.0.0.1:" + PORT + "/?android=1");
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        if (server != null) server.stop();
        super.onDestroy();
    }

    private class LocalAssetServer {
        private final ExecutorService executor = Executors.newCachedThreadPool();
        private volatile boolean running;
        private ServerSocket serverSocket;

        void start() {
            running = true;
            executor.execute(() -> {
                try {
                    serverSocket = new ServerSocket(PORT, 8);
                    while (running) {
                        Socket socket = serverSocket.accept();
                        executor.execute(() -> handle(socket));
                    }
                } catch (IOException e) {
                    if (running) Log.e(TAG, "Servidor local falhou", e);
                }
            });
        }

        void stop() {
            running = false;
            try {
                if (serverSocket != null) serverSocket.close();
            } catch (IOException ignored) {
            }
            executor.shutdownNow();
        }

        private void handle(Socket socket) {
            try (Socket s = socket) {
                InputStream in = s.getInputStream();
                OutputStream out = s.getOutputStream();
                String requestLine = readRequestLine(in);
                if (requestLine == null || !requestLine.startsWith("GET ")) {
                    writeText(out, 405, "Method Not Allowed", "text/plain", "Metodo nao permitido");
                    return;
                }
                drainRequestHeaders(in);

                String rawPath = requestLine.split(" ")[1];
                String path = URLDecoder.decode(rawPath.split("\\?")[0], "UTF-8");
                if (path.equals("/")) path = "/index.html";
                if (path.contains("..")) {
                    writeText(out, 403, "Forbidden", "text/plain", "Caminho bloqueado");
                    return;
                }

                byte[] body = readAsset("web" + path);
                if (body == null) {
                    Log.w(TAG, "Asset nao encontrado: " + path);
                    writeText(out, 404, "Not Found", "text/plain", "Arquivo nao encontrado");
                    return;
                }
                Log.d(TAG, "Servindo asset: " + path + " (" + body.length + " bytes)");
                writeBytes(out, 200, "OK", mimeType(path), body);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao servir asset", e);
            }
        }

        private String readRequestLine(InputStream in) throws IOException {
            ByteArrayOutputStream line = new ByteArrayOutputStream();
            int prev = -1;
            int cur;
            while ((cur = in.read()) != -1) {
                if (prev == '\r' && cur == '\n') break;
                if (cur != '\r') line.write(cur);
                prev = cur;
                if (line.size() > 4096) break;
            }
            if (line.size() == 0) return null;
            return line.toString("UTF-8");
        }

        private void drainRequestHeaders(InputStream in) throws IOException {
            while (readRequestLine(in) != null) {
                // Apenas consome os headers. O servidor local nao precisa deles.
            }
        }

        private byte[] readAsset(String assetPath) {
            byte[] normal = readAssetExact(assetPath);
            if (normal != null) return normal;
            return readAssetExact(assetPath.replace("/", "\\"));
        }

        private byte[] readAssetExact(String assetPath) {
            try (InputStream asset = getAssets().open(assetPath)) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] chunk = new byte[8192];
                int read;
                while ((read = asset.read(chunk)) != -1) {
                    buffer.write(chunk, 0, read);
                }
                return buffer.toByteArray();
            } catch (IOException e) {
                return null;
            }
        }

        private void writeText(OutputStream out, int code, String status, String type, String text)
                throws IOException {
            writeBytes(out, code, status, type, text.getBytes(StandardCharsets.UTF_8));
        }

        private void writeBytes(OutputStream out, int code, String status, String type, byte[] body)
                throws IOException {
            String headers = "HTTP/1.1 " + code + " " + status + "\r\n"
                    + "Content-Type: " + type + "\r\n"
                    + "Content-Length: " + body.length + "\r\n"
                    + "Access-Control-Allow-Origin: *\r\n"
                    + "X-Content-Type-Options: nosniff\r\n"
                    + "Cache-Control: no-cache\r\n"
                    + "Connection: close\r\n\r\n";
            out.write(headers.getBytes(StandardCharsets.UTF_8));
            out.write(body);
            out.flush();
        }

        private String mimeType(String path) {
            String lower = path.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".html")) return "text/html; charset=utf-8";
            if (lower.endsWith(".js") || lower.endsWith(".mjs")) return "text/javascript; charset=utf-8";
            if (lower.endsWith(".css")) return "text/css; charset=utf-8";
            if (lower.endsWith(".json") || lower.endsWith(".webmanifest")) return "application/json; charset=utf-8";
            if (lower.endsWith(".png")) return "image/png";
            if (lower.endsWith(".svg")) return "image/svg+xml";
            if (lower.endsWith(".md")) return "text/markdown; charset=utf-8";
            return "application/octet-stream";
        }
    }
}
