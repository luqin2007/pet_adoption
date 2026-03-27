package com.example.backend.component;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.backend.entity.Pet;
import com.example.backend.entity.property.ParentType;
import com.example.backend.mapper.PetMapper;
import com.example.backend.util.FileUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class FileCleanScheduleTask {

    private static final Logger logger = LoggerFactory.getLogger(FileCleanScheduleTask.class);

    private final PetMapper petMapper;
    private final FileUtils fileUtils;

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanFile() {
        cleanPetMediaFiles();
    }

    private void cleanPetMediaFiles() {
        Set<Long> petIds = new HashSet<>(petMapper.selectObjs(Wrappers.<Pet>lambdaQuery()
                .select(Pet::getId)));
        Path pets = FileUtils.generateTempPath(ParentType.PET, "");
        if (!Files.isDirectory(pets)) return;

        cleanDirectory("cleanPetMediaFiles", pets, path -> {
            try {
                String s = path.getFileName().toString();
                Long id = Long.parseLong(s);
                return !petIds.contains(id);
            } catch (NumberFormatException e) {
                logger.warn("cleanPetMediaFiles: 非标准命名方式 {}", path);
                return false;
            }
        });
    }

    private void cleanDirectory(String loggerName, Path root, Predicate<Path> predicate) {
        try (Stream<Path> files = Files.list(root)) {
            files.filter(Files::isDirectory)
                    .filter(predicate)
                    .forEach(path -> {
                        try {
                            FileSystemUtils.deleteRecursively(path);
                            logger.info("{}: {} 已删除", loggerName, path);
                        } catch (Exception e) {
                            String log = String.format("%s: %s 删除失败", loggerName, path);
                            logger.warn(log, e);
                        }
                    });
        } catch (IOException e) {
            logger.warn(loggerName, e);
        }
    }
}
