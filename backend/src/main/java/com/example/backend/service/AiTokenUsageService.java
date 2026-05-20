package com.example.backend.service;

import com.example.backend.entity.AiTokenUsage;
import com.example.backend.mapper.AiTokenUsageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiTokenUsageService extends BaseService<AiTokenUsageMapper, AiTokenUsage> {

    public void recordUsage(String operation, String model, int promptTokens, int completionTokens) {
        AiTokenUsage usage = new AiTokenUsage(null, new Date(), operation, model,
                promptTokens, completionTokens, promptTokens + completionTokens, new Date());
        baseMapper.insert(usage);
    }

    public long getTodayTotalTokens() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date start = cal.getTime();
        List<AiTokenUsage> list = baseMapper.lambdaQuery()
                .ge(AiTokenUsage::getCreatedAt, start)
                .list();
        return list.stream().mapToLong(AiTokenUsage::getTotalTokens).sum();
    }

    public List<AiTokenUsage> getRecentUsage(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -days);
        Date since = cal.getTime();
        return baseMapper.lambdaQuery()
                .ge(AiTokenUsage::getCreatedAt, since)
                .desc(AiTokenUsage::getCreatedAt)
                .list();
    }
}
