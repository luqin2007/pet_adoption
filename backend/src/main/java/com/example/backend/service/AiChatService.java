package com.example.backend.service;

import com.example.backend.entity.LostPet;
import com.example.backend.entity.MediaFile;
import com.example.backend.entity.Pet;
import com.example.backend.entity.User;
import com.example.backend.entity.property.ParentType;
import com.example.backend.mapper.MediaFileMapper;
import com.example.backend.util.FileUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AiChatService extends BaseServiceWithoutMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiChatService.class);

    private final MediaFileMapper mediaFileMapper;
    private final SystemConfigService systemConfigService;
    private final AiTokenUsageService tokenUsageService;
    private final UserSettingService userSettingService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${file.upload}")
    private String upload;

    /**
     * 提取宠物特征（文本 + 可选图片）
     */
    public String extractPetFeatures(Pet pet) {
        return extractPetFeatures(pet.getType(), pet.getBreed(), pet.getAge(), pet.getSex(),
                pet.getHealth(), pet.getDescription(), pet.getId(), ParentType.PET);
    }

    public String extractPetFeatures(LostPet pet) {
        return extractPetFeatures(pet.getType(), pet.getBreed(), pet.getAge(), pet.getSex(),
                pet.getFeatures(), pet.getDescription(), pet.getId(), ParentType.LOST_PET);
    }

    private String extractPetFeatures(String type, String breed, Integer age, String sex,
                                     String features, String description,
                                     Long petId, ParentType parentType) {
        requireAi();
        String systemPrompt = """
                你是一个宠物特征分析专家。请根据以下宠物信息分析并提取结构化特征。
                以 JSON 格式返回，仅返回 JSON 不附带任何其他内容：
                {
                  "age_group": "幼年/少年/成年/老年",
                  "size": "小型/中型/大型",
                  "color_pattern": "详细毛色花纹描述",
                  "coat_type": "短毛/中长毛/长毛/卷毛/无毛",
                  "special_marks": ["特殊标记数组，无则[]"],
                  "temperament": ["性情关键词数组"],
                  "description_keywords": ["3-5个最关键匹配词"],
                  "breed_traits": "品种典型特征描述或外观特点",
                  "visual_features": "综合所有信息后的整体外貌描述，用于文本匹配"
                }""";

        List<Map<String, Object>> contents = new ArrayList<>();
        contents.add(Map.of("type", "text", "text",
                String.format("宠物信息：\n类型：%s\n品种：%s\n年龄：%s\n性别：%s\n特征：%s\n描述：%s",
                        type != null ? type : "未知",
                        breed != null ? breed : "未知",
                        age != null ? age + " 月" : "未知",
                        sex != null ? sex : "未知",
                        features != null ? features : "无",
                        description != null ? description : "无")));

        if (systemConfigService.isMultimodalEnabled() && petId != null) {
            List<Path> imagePaths = loadPetImagePaths(petId, parentType);
            for (Path imagePath : imagePaths) {
                try {
                    byte[] imageBytes = Files.readAllBytes(imagePath);
                    String base64 = Base64.getEncoder().encodeToString(imageBytes);
                    contents.add(Map.of(
                            "type", "image_url",
                            "image_url", Map.of("url", "data:image/jpeg;base64," + base64)
                    ));
                } catch (Exception e) {
                    LOGGER.warn("Failed to load image for pet {}: {}", petId, e.getMessage());
                }
            }
        }

        Map<String, Object> response = callChatApi(systemPrompt, contents, "extract_features");
        return extractContent(response);
    }

    /**
     * 判定两个宠物特征是否匹配
     */
    public String judgeMatch(String lostPetFeatures, String petFeatures) {
        requireAi();
        String systemPrompt = """
                请根据两个宠物的特征判断它们是否为同一只动物。
                第一个是走失宠物（主人报失），第二个是流浪宠物（救助站收容）。
                
                分析要点：
                1. 毛色花纹是否高度一致
                2. 特殊标记是否匹配
                3. 品种/体型是否一致
                
                以 JSON 格式返回，仅返回 JSON：
                {
                  "is_match": true/false,
                  "confidence": 0-1的小数,
                  "reasons": ["匹配原因简述"]
                }""";

        String userContent = "走失宠物特征：\n" + lostPetFeatures + "\n\n流浪宠物特征：\n" + petFeatures;
        List<Map<String, Object>> contents = List.of(Map.of("type", "text", "text", userContent));
        Map<String, Object> response = callChatApi(systemPrompt, contents, "match");
        return extractContent(response);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map<String, Object> callChatApi(String systemPrompt, List<Map<String, Object>> contents, String operation) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(systemConfigService.getApiKey());

        String model = systemConfigService.getModel();
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", contents)
        ));
        requestBody.put("temperature", 0.1);
        requestBody.put("max_tokens", 2000);

        String endpoint = systemConfigService.getApiEndpoint();
        String url = endpoint.endsWith("/chat/completions") ? endpoint : endpoint + "/chat/completions";

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(requestBody, headers),
                    Map.class);

            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new RuntimeException("AI API returned empty response");
            }

            // 记录 token 用量
            Map<String, Object> usage = (Map<String, Object>) body.get("usage");
            if (usage != null) {
                int prompt = ((Number) usage.getOrDefault("prompt_tokens", 0)).intValue();
                int completion = ((Number) usage.getOrDefault("completion_tokens", 0)).intValue();
                tokenUsageService.recordUsage(operation, model, prompt, completion);
            }

            return body;
        } catch (Exception e) {
            LOGGER.error("AI API call failed: {}", e.getMessage());
            throw new RuntimeException("AI 服务调用失败: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> response) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("AI API returned no choices");
            }
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");
            return content != null ? content.trim() : "";
        } catch (Exception e) {
            LOGGER.error("Failed to extract AI response content: {}", e.getMessage());
            throw new RuntimeException("解析 AI 响应失败: " + e.getMessage());
        }
    }

    /**
     * 加载宠物图片路径
     */
    private List<Path> loadPetImagePaths(Long petId, ParentType parentType) {
        Path[] paths = new Path[]{ null, // 封面
                null, null, null };      // 其他图片
        int i = 1;
        for (MediaFile file : mediaFileMapper.queryByParent(parentType, petId).list()) {
            if (Boolean.TRUE.equals(file.getIsCover())) { // 封面
                paths[0] = FileUtils.generatePath(upload, parentType, petId, file.getFilename());
            } else if (i < 4) { // 三张即可
                paths[i++] = FileUtils.generatePath(upload, parentType, petId, file.getFilename());
            }
            if (i >= 4 && paths[0] != null) { // 必须包含封面
                break;
            }
        }
        return Stream.of(paths).filter(Objects::nonNull).toList();
    }

    private void requireAi() {
        User login = requireLoginUser();
        boolean isAiEnable = systemConfigService.isAiEnabled()
                && Boolean.TRUE.equals(userSettingService.getOrCreate(login.getId()).getEnableAi());
        require(isAiEnable, "AI 匹配功能未启用");
    }
}
