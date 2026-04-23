package com.example.backend.module;

import com.example.backend.dto.Result;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.FileUtils;
import com.example.backend.util.ServiceException;
import com.example.backend.util.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class ResultAndUtilityContractTest {

    @Test
    void successResultUsesUnifiedEnvelope() {
        Result<String> result = Result.success("ok");

        assertEquals(200, result.code());
        assertEquals("ok", result.data());
        assertEquals("success", result.message());
    }

    @Test
    void errorResultCanBeWrappedWithHttpStatusMatchingBusinessCode() {
        Result<Void> result = Result.error(ServiceException.E_NOT_FOUND, "not found");

        var response = Result.wrap(result);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertSame(result, response.getBody());
    }

    @Test
    void fileNameHelpersHandleExtensionAndEmptyValues() {
        assertEquals("avatar", FileUtils.getNameWithoutExtension("avatar.png"));
        assertEquals("README", FileUtils.getNameWithoutExtension("README"));
        assertEquals("", FileUtils.getNameWithoutExtension(""));

        Pair<String, String> nameAndExtension = FileUtils.getNameAndExtension("pet.photo.jpg");
        assertEquals("pet.photo", nameAndExtension.getFirst());
        assertEquals("jpg", nameAndExtension.getSecond());
    }

    @Test
    void generatedFileNameAndAssetUrlAreStable() {
        Date createTime = Date.from(Instant.parse("2026-04-23T07:30:00Z"));

        assertEquals("20260423153000_pet.jpg", FileUtils.generateFilename("pet", createTime, "jpg"));
        assertEquals("/assets/user/9/avatar.png", FileUtils.generateAssetUrl(ParentType.USER, 9L, "avatar.png"));
        assertNull(FileUtils.generateAssetUrl(ParentType.USER, 9L, null));
    }

    @Test
    void randomStringLengthMatchesRequest() {
        String value = StringUtils.generateRandomString(32);

        assertEquals(32, value.length());
        assertTrue(value.chars().allMatch(ch -> Character.isLetterOrDigit((char) ch)));
    }
}
