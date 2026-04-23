package com.example.backend.service;

import com.example.backend.dto.TempFileInfo;
import com.example.backend.entity.DeleteJob;
import com.example.backend.entity.User;
import com.example.backend.entity.property.MediaType;
import com.example.backend.entity.property.ParentType;
import com.example.backend.entity.property.UserRole;
import com.example.backend.mapper.DeleteJobMapper;
import com.example.backend.util.CustomUserDetails;
import com.example.backend.util.RedisHelper;
import com.example.backend.util.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileServiceUnitTest {

    @TempDir
    Path tempDir;

    @TempDir
    Path uploadDir;

    private DeleteJobMapper deleteJobMapper;
    private RedisHelper redisHelper;
    private FileService service;

    @BeforeEach
    void setUp() {
        deleteJobMapper = mock(DeleteJobMapper.class);
        redisHelper = mock(RedisHelper.class);
        service = new FileService(deleteJobMapper);
        service.setObjects(redisHelper, mock(), mock(), mock(), mock());
        ReflectionTestUtils.setField(service, "keyTimeout", 30L);
        ReflectionTestUtils.setField(service, "temp", tempDir.toString());
        ReflectionTestUtils.setField(service, "upload", uploadDir.toString());

        User user = new User();
        user.setId(7L);
        user.setUsername("tester");
        user.setPassword("{noop}pwd");
        user.setRole(UserRole.ADMIN.getMask());
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken(new CustomUserDetails(user), null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void uploadTempFileWritesFileAndStoresMetadataInRedis() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "pet-note.txt", "text/plain", "hello".getBytes());

        TempFileInfo info = service.uploadTempFile(file, "领养资料", "uuid-1", "tmp:%s", ParentType.ADOPT);

        assertEquals("领养资料", info.getName());
        assertEquals(7L, info.getUserId());
        assertEquals(MediaType.IMAGE, info.getType());
        assertTrue(info.getFilename().endsWith("_pet-note.txt"));
        assertTrue(Files.exists(tempDir.resolve(Path.of("adopt", "uuid-1", info.getFilename()))));
        verify(redisHelper).putObjectToHash("tmp:uuid-1", info.getFilename(), info);
        verify(redisHelper).expireObject("tmp:uuid-1", 30);
    }

    @Test
    void uploadTempImageRejectsNonImageContentBeforeRedisWrite() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "notes.txt", "text/plain", "not an image".getBytes());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.uploadTempImage(file, null, "uuid-2", "tmp:%s", ParentType.PET));

        assertEquals(ServiceException.E_INVALIDATE, ex.getCode());
        assertEquals("exception.invalidate.file.unsupported_media", ex.getMessage());
        verify(redisHelper, never()).putObjectToHash(anyString(), anyString(), any());
    }

    @Test
    void deleteFileCreatesDeleteJobForExistingFilename() {
        service.deleteFile("avatar.png", 9L, ParentType.USER);

        ArgumentCaptor<DeleteJob> captor = ArgumentCaptor.forClass(DeleteJob.class);
        verify(deleteJobMapper).insert(captor.capture());
        assertTrue(captor.getValue().getPath().endsWith(Path.of("user", "9", "avatar.png").toString()));
    }

    @Test
    void deleteFileIgnoresBlankFilename() {
        service.deleteFile(" ", 9L, ParentType.USER);

        verifyNoInteractions(deleteJobMapper);
    }
}
