import com.google.gson.*;
import java.net.http.*;
import java.net.URI;
import java.util.*;
import java.io.*;

public class TranslationHelper {
    private static final String API_URL = "https://alfarooj.pythonanywhere.com/api/";
    private static String currentLanguage = "en";
    private static Map<String, String> translationCache = new HashMap<>();
    
    public static void setLanguage(String langCode) {
        currentLanguage = langCode;
        saveLanguage(langCode);
    }
    
    public static String getCurrentLanguage() {
        return currentLanguage;
    }
    
    public static void saveLanguage(String langCode) {
        currentLanguage = langCode;
        // Save to file for persistence
        try (FileWriter writer = new FileWriter("language_config.txt")) {
            writer.write(langCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void loadLanguage() {
        try (BufferedReader reader = new BufferedReader(new FileReader("language_config.txt"))) {
            currentLanguage = reader.readLine();
            if (currentLanguage == null || currentLanguage.isEmpty()) {
                currentLanguage = "en";
            }
        } catch (IOException e) {
            currentLanguage = "en";
        }
    }
    
    public static String translateText(String text) {
        if (text == null || text.isEmpty()) return text;
        if (currentLanguage.equals("en")) return text;
        
        String cacheKey = text + "_" + currentLanguage;
        if (translationCache.containsKey(cacheKey)) {
            return translationCache.get(cacheKey);
        }
        
        try {
            String json = String.format("{\"text\":\"%s\",\"target_lang\":\"%s\"}", 
                text.replace("\"", "\\\""), currentLanguage);
            String response = sendPostRequest(API_URL + "translate", json);
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            if (jsonResponse.get("success").getAsBoolean()) {
                String translated = jsonResponse.get("translated").getAsString();
                translationCache.put(cacheKey, translated);
                return translated;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }
    
    public static void translateButton(JButton button, String originalText) {
        String translated = translateText(originalText);
        button.setText(translated);
    }
    
    public static void translateLabel(JLabel label, String originalText) {
        String translated = translateText(originalText);
        label.setText(translated);
    }
    
    public static void translateFrame(JFrame frame, String originalTitle) {
        String translated = translateText(originalTitle);
        frame.setTitle(translated);
    }
    
    private static String sendPostRequest(String urlString, String json) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(urlString))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
