package cn.weathermcp.weathermcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WeatherService {
    //简单城市名到城市代码映射
    private static final Map<String, String> CITY_CODE_MAP = new HashMap<>();
    private static final Map<String, String> PROVINCE_CODE_MAP = new HashMap<>();
    private static final String CITY_CODE_PATH = "static/weather/city_code.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        try {
            //城市编码
            List<Map<String, String>> cities = objectMapper.readValue(
                Files.readAllBytes(Paths.get(CITY_CODE_PATH)),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            for (Map<String, String> c : cities) {
                CITY_CODE_MAP.put(c.get("name"), c.get("code"));
            }
        } catch (Exception e) {
            System.err.println("加载城市代码失败: " + e.getMessage());
        }
    }

    @Tool(description = "通过城市和天数查询未来的天气，最多7天，城市名用汉字，如合肥、广州")
    public List<Map<String, String>> getWeatherByCityAndDays(String cityName, int days) {
        days = days + 1;
        List<Map<String, String>> result = new ArrayList<>();
        String code = CITY_CODE_MAP.get(cityName);
        String url = "http://www.weather.com.cn/weather/" + code + ".shtml";
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();//爬
            Elements daysList = doc.select("ul.t.clearfix li");
            int count = 0;
            for (Element day : daysList) {
                if (count >= days) break;
                Map<String, String> info = new HashMap<>();
                info.put("date", day.selectFirst("h1") != null ? day.selectFirst("h1").text() : "");
                info.put("weather", day.selectFirst("p.wea") != null ? day.selectFirst("p.wea").text() : "");
                info.put("temp", day.selectFirst("p.tem") != null ? day.selectFirst("p.tem").text().replace("\n", " ").trim() : "");
                info.put("wind", day.selectFirst("p.win span") != null ? day.selectFirst("p.win span").attr("title") : "");
                result.add(info);
                count++;
            }
        } catch (IOException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "爬取失败: " + e.getMessage());
            result.add(err);
        }
        return result;
    }

    @Tool(description = "查询省份的所有城市，请传入省份的拼音，如anhui")
    public List<String> getProvinceCities(String provincePinyin) {
        List<String> cities = new ArrayList<>();
        String url = "http://www.weather.com.cn/textFC/" + provincePinyin + ".shtml";
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();
            Elements cityLinks = doc.select("div.lQCity ul li a");
            for (Element link : cityLinks) {
                cities.add(link.text());
            }
        } catch (IOException e) {
            System.err.println("获取城市列表失败: " + e.getMessage());
        }
        return cities;
    }

    @Tool(description = "查询省份所有城市的天气，请传入省份的拼音，如anhui")
    public Map<String, List<Map<String, String>>> getWeatherForAllCitiesInProvince(String provincePinyin) {
        Map<String, List<Map<String, String>>> weatherData = new HashMap<>();
        List<String> cities = getProvinceCities(provincePinyin);//回调
        
        for (String city : cities) {
            List<Map<String, String>> weatherInfo = getWeatherByCityAndDays(city, 7);
            weatherData.put(city, weatherInfo);
        }
        return weatherData;
    }
}
