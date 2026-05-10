package com.example.backend.controller;

import com.example.backend.util.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AssetControllerUnitTest {

    @TempDir
    Path tempDir;

    @Test
    void getAssetReturnsReadableUploadedFile() throws Exception {
        Path dir = tempDir.resolve("pet").resolve("12");
        Files.createDirectories(dir);
        Files.write(dir.resolve("cover.png"), new byte[]{1, 2, 3});
        AssetController controller = new AssetController();
        ReflectionTestUtils.setField(controller, "upload", tempDir.toString());

        ResponseEntity<Resource> response = controller.getAsset("pet", 12L, "cover.png");

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().contentLength());
        assertTrue(response.getHeaders().getContentDisposition().isInline());
    }

    @Test
    void getAssetRejectsUnknownFolder() {
        AssetController controller = new AssetController();
        ReflectionTestUtils.setField(controller, "upload", tempDir.toString());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> controller.getAsset("unknown", 12L, "cover.png"));

        assertEquals(ServiceException.E_NOT_FOUND, ex.getCode());
    }

    @Test
    void getAssetRejectsUnsafeFilename() {
        AssetController controller = new AssetController();
        ReflectionTestUtils.setField(controller, "upload", tempDir.toString());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> controller.getAsset("pet", 12L, "..%2fsecret.png"));

        assertEquals(ServiceException.E_INVALIDATE, ex.getCode());
    }
}
