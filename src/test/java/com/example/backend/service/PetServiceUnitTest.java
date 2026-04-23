package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.entity.property.UserRole;
import com.example.backend.mapper.LocationMapper;
import com.example.backend.mapper.PetMapper;
import com.example.backend.mapper.PetStatusRecordMapper;
import com.example.backend.mapper.PetTagMapper;
import com.example.backend.util.CustomUserDetails;
import com.example.backend.util.MPLambdaUpdate;
import com.example.backend.util.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PetServiceUnitTest {

    private PetMapper petMapper;
    private PetService service;

    @BeforeEach
    void setUp() {
        petMapper = mock(PetMapper.class);
        service = new PetService(mock(PetStatusRecordMapper.class), mock(LocationMapper.class), mock(PetTagMapper.class));
        ReflectionTestUtils.setField(service, "baseMapper", petMapper);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void workerCanDiscardPet() {
        login(user(1L, "worker", UserRole.WORKER.getMask()));
        @SuppressWarnings("unchecked")
        MPLambdaUpdate<?> update = mock(MPLambdaUpdate.class);
        when(petMapper.discardPetById(33L)).thenReturn((MPLambdaUpdate) update);

        service.deletePet(33L);

        verify(petMapper).discardPetById(33L);
        verify(update).update();
    }

    @Test
    void normalUserCannotDiscardPet() {
        login(user(2L, "normal", UserRole.NORMAL.getMask()));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.deletePet(33L));

        assertEquals(ServiceException.E_AUTH, ex.getCode());
        assertEquals("exception.auth.denied", ex.getMessage());
        verify(petMapper, never()).discardPetById(anyLong());
    }

    private static void login(User user) {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken(new CustomUserDetails(user), null));
    }

    private static User user(Long id, String username, int role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword("{noop}pwd");
        user.setEmail(username + "@example.com");
        user.setRole(role);
        return user;
    }
}
