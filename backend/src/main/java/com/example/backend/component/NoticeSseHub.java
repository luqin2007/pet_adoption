package com.example.backend.component;

import com.example.backend.dto.NoticeResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class NoticeSseHub {

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> {
            remove(userId, emitter);
            emitter.complete();
        });
        emitter.onError(ex -> remove(userId, emitter));
        send(emitter, SseEmitter.event()
                .name("connected")
                .data(Map.of("userId", userId)));
        return emitter;
    }

    public void push(NoticeResponse notice) {
        Long userId = notice.getReceiverId();
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) {
            return;
        }
        userEmitters.removeIf(emitter -> !send(emitter, SseEmitter.event()
                .name("notice")
                .data(notice)));
        if (userEmitters.isEmpty()) {
            emitters.remove(userId);
        }
    }

    private void remove(Long userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null) {
            return;
        }
        userEmitters.remove(emitter);
        if (userEmitters.isEmpty()) {
            emitters.remove(userId);
        }
    }

    private boolean send(SseEmitter emitter, SseEmitter.SseEventBuilder event) {
        try {
            emitter.send(event);
            return true;
        } catch (IOException | IllegalStateException e) {
            emitter.completeWithError(e);
            return false;
        }
    }
}
