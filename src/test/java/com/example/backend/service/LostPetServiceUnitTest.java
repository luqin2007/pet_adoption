package com.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.example.backend.entity.*;
import com.example.backend.entity.property.LostPetStatus;
import com.example.backend.entity.property.ParentType;
import com.example.backend.entity.property.PetStatus;
import com.example.backend.mapper.*;
import com.example.backend.util.MPLambdaQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LostPetServiceUnitTest {

    private LostPetMapper lostPetMapper;
    private LostPetMismatchMapper lostPetMismatchMapper;
    private PetMapper petMapper;
    private LocationMapper locationMapper;
    private LostPetService service;

    @BeforeEach
    void setUp() {
        lostPetMapper = mock(LostPetMapper.class);
        lostPetMismatchMapper = mock(LostPetMismatchMapper.class);
        petMapper = mock(PetMapper.class);
        locationMapper = mock(LocationMapper.class);
        service = new LostPetService(lostPetClaimMapper(), lostPetMismatchMapper, petMapper, locationMapper);
        ReflectionTestUtils.setField(service, "baseMapper", lostPetMapper);
    }

    @Test
    void listMatchedPetsReturnsOnlyUnignoredPetsThatMatchProfile() {
        LostPet lostPet = lostPet(1L, LostPetStatus.SEARCHING, "F", "CAT", "橘猫");
        Location location = location(100L);
        MPLambdaQuery<LostPetMismatch> mismatchQuery = mockQuery();
        MPLambdaQuery<Location> locationQuery = mockQuery();
        when(lostPetMismatchMapper.queryByLostPet(1L)).thenReturn(mismatchQuery);
        when(mismatchQuery.list(anySFunction())).thenReturn(Stream.of(99L));
        when(locationMapper.queryPetLocations(location, lostPet.getLostTime())).thenReturn(locationQuery);
        when(locationQuery.list(anySFunction())).thenReturn(Stream.of(88L, 99L));
        when(petMapper.selectList(Set.of(88L))).thenReturn(List.of(
                pet(88L, PetStatus.SHELTERED, false, "F", "CAT", "橘猫"),
                pet(89L, PetStatus.SHELTERED, false, "M", "DOG", "柴犬")));

        List<Pet> matches = service.listMatchedPets(lostPet, location);

        assertEquals(List.of(88L), matches.stream().map(Pet::getId).toList());
    }

    @Test
    void listMatchedPetsReturnsEmptyWhenLostPetAlreadyClosed() {
        List<Pet> matches = service.listMatchedPets(
                lostPet(1L, LostPetStatus.CLOSED, "F", "CAT", "橘猫"),
                location(100L));

        assertEquals(List.of(), matches);
        verifyNoInteractions(lostPetMismatchMapper, locationMapper, petMapper);
    }

    @Test
    void listMatchedLostPetsReturnsEmptyForDiscardedPet() {
        List<LostPet> matches = service.listMatchedLostPets(
                pet(7L, PetStatus.SHELTERED, true, "F", "CAT", "橘猫"),
                location(100L));

        assertEquals(List.of(), matches);
        verifyNoInteractions(lostPetMismatchMapper, locationMapper, lostPetMapper);
    }

    @SuppressWarnings("unchecked")
    private static <T extends IId> MPLambdaQuery<T> mockQuery() {
        return mock(MPLambdaQuery.class);
    }

    @SuppressWarnings("unchecked")
    private static <T, R> SFunction<T, R> anySFunction() {
        return any(SFunction.class);
    }

    private static LostPetClaimMapper lostPetClaimMapper() {
        return mock(LostPetClaimMapper.class);
    }

    private static LostPet lostPet(Long id, LostPetStatus status, String sex, String type, String breed) {
        return new LostPet(id,
                10L,
                "小橘",
                24,
                sex,
                type,
                breed,
                "左耳缺角",
                new Date(System.currentTimeMillis() - 86_400_000L),
                "13800000000",
                "走失",
                null,
                status,
                new Date(),
                new Date());
    }

    private static Pet pet(Long id, PetStatus status, boolean discarded, String sex, String type, String breed) {
        return new Pet(id,
                20L,
                "候选宠物",
                24,
                sex,
                type,
                breed,
                "健康",
                "描述",
                status,
                discarded,
                new Date(),
                new Date());
    }

    private static Location location(Long id) {
        return new Location(id,
                1L,
                ParentType.LOST_PET,
                10L,
                "广东",
                "广州",
                "天河",
                "测试地址",
                new Date());
    }
}
