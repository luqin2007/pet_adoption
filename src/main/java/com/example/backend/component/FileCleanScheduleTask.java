package com.example.backend.component;

import com.example.backend.entity.DeleteJob;
import com.example.backend.entity.property.ActionStatus;
import com.example.backend.entity.property.ParentType;
import com.example.backend.mapper.DeleteJobMapper;
import com.example.backend.util.RedisHelper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class FileCleanScheduleTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileCleanScheduleTask.class);

    private final DeleteJobMapper deleteJobMapper;
    private final RedisHelper redisHelper;

    private final AtomicBoolean isCleanRunning = new AtomicBoolean(false);
    private final AtomicBoolean isDeleteJobRunning = new AtomicBoolean(false);

    @Value("${file.temp}")
    private String tempPath;
    @Value("${key.examination.file}")
    private String examinationFile;
    @Value("${key.donation.file}")
    private String donationFile;
    @Value("${key.rescue_task.file}")
    private String rescueTaskFile;
    @Value("${key.lost_pet.file}")
    private String lostPetFile;

    @Async
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void cleanFile() {
        beginJob(isDeleteJobRunning, () -> {
            deleteTempFiles(ParentType.EXAMINATION, examinationFile);
            deleteTempFiles(ParentType.DONATION, donationFile);
            deleteTempFiles(ParentType.RESCUE_TASK, rescueTaskFile);
            deleteTempFiles(ParentType.LOST_PET, lostPetFile);
        });
    }

    @Async
    @Scheduled(cron = "0 0/5 0 * * ?")
    public void doDeleteJob() {
        beginJob(isCleanRunning, () -> {
            executeDeleteJob(10, ActionStatus.WAITING);
            executeDeleteJob(5, ActionStatus.FAILED);
        });
    }

    private void beginJob(AtomicBoolean sign, Runnable job) {
        if (!sign.get()) {
            sign.set(true);
            try {
                job.run();
            } finally {
                sign.set(false);
            }
        }
    }

    private void deleteTempFiles(ParentType parent, String redisKeyTemplate) {
        Path path = Paths.get(tempPath, parent.getFolder());
        if (!Files.isDirectory(path))
            return;

        try (Stream<Path> files = Files.list(path)) {
            files.forEach(file -> {
                try {
                    if (Files.isDirectory(file)) {
                        String uuid = file.getFileName().toString();
                        if (!redisHelper.hasString(redisKeyTemplate, uuid)) {
                            FileSystemUtils.deleteRecursively(file);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("删除目录失败 {}", path, e);
                }
            });
        } catch (IOException e) {
            LOGGER.error("获取文件列表失败 {}", path, e);
        }
    }

    private void executeDeleteJob(int count, ActionStatus status) {
        List<DeleteJob> files = deleteJobMapper.query(count, status)
                .list(DeleteJob::getId, DeleteJob::getPath);
        for (DeleteJob file : files) {
            Path path = Paths.get(file.getPath());
            if (!Files.exists(path)) {
                deleteJobMapper.success(file).update();
                continue;
            }

            // 删除
            deleteJobMapper.start(file).update();
            try {
                if (Files.isRegularFile(path))
                    Files.delete(path);
                else
                    FileSystemUtils.deleteRecursively(path);
            } catch (IOException e) {
                LOGGER.error("删除文件失败 {}", path, e);
            } finally {
                if (Files.exists(path))
                    deleteJobMapper.fail(file).update();
                else
                    deleteJobMapper.success(file).update();
            }
        }
    }
}
