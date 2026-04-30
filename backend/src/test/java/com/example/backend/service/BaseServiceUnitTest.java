package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.IId;
import com.example.backend.mapper.IBaseMapper;
import com.example.backend.util.RedisHelper;
import com.example.backend.util.ServiceException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BaseServiceUnitTest {

    private final RedisHelper redisHelper = mock(RedisHelper.class);
    private final TestService service = new TestService();

    @Test
    void beginRedisUuidStoresContentWithGeneratedKeyAndConfiguredTimeout() {
        service.redisHelper = redisHelper;
        service.keyTimeout = 15;
        when(redisHelper.hasString(eq("unit:%s"), anyString())).thenReturn(false);

        String uuid = service.beginRedisUuid("unit:%s", "payload");

        assertNotNull(uuid);
        assertFalse(uuid.isBlank());
        verify(redisHelper).putString("unit:" + uuid, "payload", 15);
    }

    @Test
    void requireRedisUuidRefreshesValidUuidAndReturnsResolvedKey() {
        service.redisHelper = redisHelper;
        service.keyTimeout = 20;
        when(redisHelper.hasString("unit:%s", "abc")).thenReturn(true);

        String key = service.requireRedisUuid("unit:%s", "abc");

        assertEquals("unit:abc", key);
        verify(redisHelper).expireString("unit:abc", 20);
    }

    @Test
    void requireRedisUuidRejectsExpiredUuid() {
        service.redisHelper = redisHelper;
        when(redisHelper.hasString("unit:%s", "expired")).thenReturn(false);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.requireRedisUuid("unit:%s", "expired"));

        assertEquals(ServiceException.E_INVALIDATE, ex.getCode());
        assertEquals("exception.invalidate.request_timeout", ex.getMessage());
        verify(redisHelper, never()).expireString(anyString(), anyLong());
    }

    @Test
    void convertDtoKeepsPaginationMetadataAndMapsRecords() {
        Page<DummyEntity> source = Page.of(2, 3, 8);
        source.setRecords(List.of(
                new DummyEntity(1L, "alpha"),
                new DummyEntity(2L, "beta")
        ));

        Page<String> converted = service.convertDto(source, item -> item.id + ":" + item.name);

        assertEquals(2, converted.getCurrent());
        assertEquals(3, converted.getSize());
        assertEquals(8, converted.getTotal());
        assertEquals(List.of("1:alpha", "2:beta"), converted.getRecords());
    }

    private static class TestService extends BaseService<IBaseMapper<DummyEntity>, DummyEntity> {
    }

    private record DummyEntity(Long id, String name) implements IId {
        @Override
        public Long getId() {
            return id;
        }
    }
}
