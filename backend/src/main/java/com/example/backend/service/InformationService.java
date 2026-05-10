package com.example.backend.service;

import com.example.backend.dto.InfoPetTypeResponse;
import com.example.backend.dto.LocationRequest;
import com.example.backend.entity.InfoPetType;
import com.example.backend.entity.Location;
import com.example.backend.entity.property.ParentType;
import com.example.backend.mapper.InfoPetTypeMapper;
import com.example.backend.util.CityHelper;
import com.example.backend.util.ServiceException;
import com.example.backend.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.example.backend.util.StringUtils.normalize;

@Service
@RequiredArgsConstructor
public class InformationService extends BaseService<InfoPetTypeMapper, InfoPetType> {

    private final CityHelper cityHelper;

    public List<String> getProvinces() {
        return cityHelper.getProvinces();
    }

    public List<String> getCities(String province) {
        return cityHelper.getCities(province);
    }

    public List<String> getDistricts(String province, String city) {
        if (province == null) return List.of();
        if (city == null) return List.of();
        return cityHelper.getAreas(province, city);
    }

    public void validateLocation(LocationRequest request) {
        if (!cityHelper.isValid(request.getProvince(), request.getCity(), request.getDistrict())) {
            throw ServiceException.invalidate("exception.invalidate.location");
        }
    }

    public Location createValidatedLocation(LocationRequest request, ParentType parentType, Long parentId, Long userId) {
        validateLocation(request);
        return request.createLocation(parentType, parentId, userId);
    }

    public void applyValidatedLocation(LocationRequest request, Location location) {
        validateLocation(request);
        request.applyTo(location);
    }

    public List<InfoPetTypeResponse> getPetTypes() {
        Map<String, List<String>> grouped = new LinkedHashMap<>();
        for (InfoPetType row : baseMapper.queryAll().list()) {
            grouped.computeIfAbsent(row.getType(), _key -> new ArrayList<>());
            if (row.getBreed() != null && !row.getBreed().isBlank() && !grouped.get(row.getType()).contains(row.getBreed())) {
                grouped.get(row.getType()).add(row.getBreed());
            }
        }

        return grouped.entrySet().stream()
                .map(entry -> new InfoPetTypeResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    public boolean addPetType(String type, String breed) {
        if (!StringUtils.hasText(type))
            return false;

        breed = normalize(breed, true);
        if (baseMapper.queryByTypeAndBreed(type, breed).exists())
            return false;

        baseMapper.insert(new InfoPetType(null, type, breed, new Date()));
        return true;
    }
}
