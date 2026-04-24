package dk.easv.easvexam.bll;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.InputStream;

public class ApiService {
    private final String BASE_URL = "https://studentiffapi-production.up.railway.app";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public InputStream fetchRandomTiff() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/getRandomFile"))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new RuntimeException("Failed to fetch file. HTTP Code: " + response.statusCode());
        }
    }
}
