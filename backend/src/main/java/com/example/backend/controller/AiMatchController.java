package com.example.backend.controller;

import com.example.backend.dto.AIMatchResult;
import com.example.backend.dto.Result;
import com.example.backend.service.AiMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiMatchController {

    private final AiMatchService aiMatchService;

    /**
     * 为走失宠物匹配流浪宠物
     */
    @GetMapping("/match/lost/{id}")
    public Result<List<AIMatchResult>> matchLostPet(@PathVariable("id") Long lostPetId,
                                                    @RequestParam(value = "limit", defaultValue = "10") int limit) {
        List<AIMatchResult> results = aiMatchService.matchLostPet(lostPetId, limit);
        return Result.success(results);
    }

    /**
     * 为流浪宠物匹配走失宠物
     */
    @GetMapping("/match/pet/{id}")
    public Result<List<AIMatchResult>> matchPetToLost(@PathVariable("id") Long petId,
                                                      @RequestParam(value = "limit", defaultValue = "10") int limit) {
        List<AIMatchResult> results = aiMatchService.matchPetToLost(petId, limit);
        return Result.success(results);
    }
}
