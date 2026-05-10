package com.example.backend.controller;

import com.example.backend.dto.InfoPetTypeResponse;
import com.example.backend.dto.Result;
import com.example.backend.service.InformationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/info")
@RequiredArgsConstructor
public class InformationController {

    private final InformationService informationService;

    @GetMapping("/provinces")
    public Result<List<String>> getProvinces() {
        return Result.success(informationService.getProvinces());
    }

    @GetMapping("/cities")
    public Result<List<String>> getCities(@RequestParam String province) {
        return Result.success(informationService.getCities(province));
    }

    @GetMapping("/districts")
    public Result<List<String>> getDistricts(@RequestParam String province, @RequestParam String city) {
        return Result.success(informationService.getDistricts(province, city));
    }

    @GetMapping("/pet-types")
    public Result<List<InfoPetTypeResponse>> getPetTypes() {
        return Result.success(informationService.getPetTypes());
    }
}
