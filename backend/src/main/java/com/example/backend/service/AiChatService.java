package com.example.backend.service;

import com.example.backend.dto.ai.ChatRequest;
import com.example.backend.dto.ai.ChatResponse;
import com.example.backend.dto.ai.Usage;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
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
        ChatRequest contents = new ChatRequest("""
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
                }""");
        contents.setText("宠物信息：\n类型：%s\n品种：%s\n年龄：%s\n性别：%s\n特征：%s\n描述：%s",
                type != null ? type : "未知",
                breed != null ? breed : "未知",
                age != null ? age + " 月" : "未知",
                sex != null ? sex : "未知",
                features != null ? features : "无",
                description != null ? description : "无");

        if (systemConfigService.isMultimodalEnabled() && petId != null) {
            loadPetImagePaths(petId, parentType).forEach(contents::addImage);
        }
        return callChatApi(contents, "extract_features");
    }

    /**
     * 判定两个宠物特征是否匹配
     */
    public String judgeMatch(String lostPetFeatures, String petFeatures) {
        requireAi();
        ChatRequest contents = new ChatRequest("""
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
                }""");
        contents.setText("走失宠物特征：\n%s\n\n流浪宠物特征：\n%s", lostPetFeatures, petFeatures);
        return callChatApi(contents, "match");
    }

    private String callChatApi(ChatRequest contents, String operation) {
        String apiKey = systemConfigService.getApiKey();
        String model = systemConfigService.getModel();
        String endpoint = systemConfigService.getApiEndpoint();
        String url = endpoint.endsWith("/chat/completions") ? endpoint : endpoint + "/chat/completions";
        var request = contents.buildRequest(apiKey, model);

        try {
            ResponseEntity<ChatResponse> response = restTemplate.postForEntity(url, request, ChatResponse.class);
            ChatResponse body = response.getBody();
            if (body == null) {
                throw new RuntimeException("AI API returned empty response");
            }

            // 记录 token 用量
            Usage usage = body.getUsage();
            if (usage != null) {
                Integer prompt = usage.getPromptTokens();
                Integer completion = usage.getCompletionTokens();
                tokenUsageService.recordUsage(operation, model, prompt, completion);
            }

            return body.getFirstMessage();
        } catch (Exception e) {
            LOGGER.error("AI API call failed: {}", e.getMessage());
            throw new RuntimeException("AI 服务调用失败: " + e.getMessage());
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
            if (i >= 4 && paths[0] != null) { // 尽可能包含封面
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
