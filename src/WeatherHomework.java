import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherService {

    // API URL
    private static final String API_URL = "https://api.weather.yandex.ru/v2/forecast";
    // Координаты Вологды
    private static final String LAT = "59.1300";
    private static final String LON = "39.5400";
    // Ключ API
    private static final String API_KEY = "59f8e17c-e20f-45e7-a687-9cbcb42d7175";

    public static void main(String[] args) {
        try {
            // Формируем URL запроса с координатами города Вологды
            String urlStr = API_URL + "?lat=" + LAT + "&lon=" + LON + "&limit=7"; // Параметр limit = 7 для получения прогноза на 7 дней
            URL url = new URL(urlStr);

            // Открываем соединение
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Yandex-API-Key", API_KEY); // Устанавливаем ключ API
            connection.setRequestProperty("Content-Type", "application/json");

            int responseCode = connection.getResponseCode();

            // Проверяем успешность запроса
            if (responseCode == 200) {
                // Читаем ответ от сервера
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Печатаем весь ответ в формате JSON
                System.out.println("Полученные данные: " + response);

                // Парсим JSON-ответ и извлекаем температуру
                JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
                JsonObject fact = jsonObject.getAsJsonObject("fact");
                int temp = fact.get("temp").getAsInt();
                System.out.println("Температура сейчас: " + temp + "°C");

                // Вычисляем среднюю температуру за период (используем поле forecasts)
                double averageTemp = calculateAverageTemperature(jsonObject);
                System.out.println("Средняя температура за указанный период: " + averageTemp + "°C");

            } else {
                System.out.println("Ошибка: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Метод для вычисления средней температуры из прогнозов
    private static double calculateAverageTemperature(JsonObject jsonObject) {
        double totalTemp = 0;
        int count = 0;

        // Достаем массив "forecasts" из ответа
        JsonElement forecastsElement = jsonObject.get("forecasts");
        if (forecastsElement != null && forecastsElement.isJsonArray()) {
            for (JsonElement element : forecastsElement.getAsJsonArray()) {
                JsonObject day = element.getAsJsonObject();
                JsonObject parts = day.getAsJsonObject("parts");
                if (parts != null) {
                    JsonObject dayPart = parts.getAsJsonObject("day");
                    if (dayPart != null) {
                        totalTemp += dayPart.get("temp_avg").getAsDouble();
                        count++;
                    }
                }
            }
        }

        return count > 0 ? totalTemp / count : 0.0;
    }
}