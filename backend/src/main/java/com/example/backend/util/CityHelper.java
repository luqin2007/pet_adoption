package com.example.backend.util;

import com.example.backend.dto.pca.Area;
import com.example.backend.dto.pca.City;
import com.example.backend.dto.pca.Province;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CityHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CityHelper.class);

    private final ObjectMapper objectMapper;

    @Getter
    private List<String> provinces;
    private final Map<String, List<String>> cities = new HashMap<>();
    private final Map<String, Map<String, List<String>>> areas = new HashMap<>();

    @PostConstruct
    public void loadCities() throws IOException {
        Resource resource = new ClassPathResource("informations/pca.json");
        if (!resource.exists()) {
            LOGGER.error("CityHelper: pca.json not found");
            return;
        }

        Province[] pca =
                objectMapper.readerForArrayOf(Province.class).readValue(resource.getInputStream());
        provinces = new ArrayList<>(pca.length);
        for (Province province : pca) {
            provinces.add(province.getName());
            List<String> cityNames = new ArrayList<>(province.getChildren().size());
            Map<String, List<String>> areaMap = new HashMap<>(province.getChildren().size());
            for (City city : province.getChildren()) {
                cityNames.add(city.getName());
                List<String> areaNames = new ArrayList<>(city.getChildren().size());
                for (Area area : city.getChildren()) {
                    areaNames.add(area.getName());
                }
                areaMap.put(city.getName(), List.copyOf(areaNames));
            }
            cities.put(province.getName(), List.copyOf(cityNames));
            areas.put(province.getName(), areaMap);
        }
        provinces = List.copyOf(provinces);
    }

    public List<String> getCities(String province) {
        return cities.getOrDefault(province, List.of());
    }

    public List<String> getAreas(String province, String city) {
        return areas.getOrDefault(province, Map.of())
                .getOrDefault(city, List.of());
    }

    public boolean isValid(String province, String city, String area) {
        Map<String, List<String>> cities = areas.get(province);
        if (cities == null) return false;
        List<String> areaList = cities.get(city);
        if (areaList == null) return false;
        return areaList.isEmpty() ? StringUtils.hasText(area) : areaList.contains(area);
    }
}
