package com.example.backend.facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.entity.property.ParentType;
import com.example.backend.mapper.AdoptMapper;
import com.example.backend.mapper.AgreementFileMapper;
import com.example.backend.mapper.BreadingMapper;
import com.example.backend.mapper.FollowRecordMapper;
import com.example.backend.mapper.FollowTaskMapper;
import com.example.backend.service.FileService;
import com.example.backend.service.PetService;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.backend.entity.property.ParentType.PET;

@Component
@RequiredArgsConstructor
public class AdoptBreadingFacade {

    private final AdoptMapper adoptMapper;
    private final BreadingMapper breadingMapper;
    private final FollowTaskMapper followTaskMapper;
    private final FollowRecordMapper followRecordMapper;
    private final AgreementFileMapper agreementFileMapper;
    private final PetService petService;
    private final UserService userService;
    private final FileService fileService;

    public AdoptResponse buildAdoptResponse(Adopt adopt) {
        Pet pet = petService.selectById(adopt.getPetId(),
                Pet::getId, Pet::getName);
        String cover = fileService.getCoverUrl(adopt.getPetId(), PET);
        List<FollowTask> tasks = followTaskMapper.queryByAdopt(adopt.getId()).list();
        Map<Long, User> users = userService.groupById(
                Stream.of(adopt.getApplicantId(), adopt.getReviewerId()),
                tasks.stream().flatMap(task -> Stream.of(task.getWorkerId(), task.getVolunteerId())),
                User::getId, User::getUsername, User::getAvatar);
        Set<Long> taskIds = tasks.stream()
                .map(FollowTask::getId)
                .collect(Collectors.toSet());
        Map<Long, FollowRecord> records = taskIds.isEmpty() ? Map.of()
                : followRecordMapper.queryByTasks(taskIds).group(FollowRecord::getTaskId,
                FollowRecord::getId, FollowRecord::getTaskId, FollowRecord::getSummary, FollowRecord::getVisitTime);
        return AdoptResponse.create(adopt,
                pet, cover,
                users.get(adopt.getApplicantId()),
                adopt.getReviewerId() == null ? null : users.get(adopt.getReviewerId()),
                tasks.stream().map(task -> FollowTaskResponse.createBatch(task, users, records)).toList());
    }

    public Page<AdoptResponse> buildAdoptPage(Page<Adopt> result) {
        Set<Long> adoptIds = result.getRecords().stream()
                .map(Adopt::getId)
                .collect(Collectors.toSet());
        List<FollowTask> tasks = followTaskMapper.queryByAdopts(adoptIds).list();
        Set<Long> petIds = result.getRecords().stream()
                .map(Adopt::getPetId)
                .collect(Collectors.toSet());
        Map<Long, Pet> pets = petService.groupById(petIds,
                Pet::getId, Pet::getName);
        Map<Long, String> covers = fileService.getCoverUrls(PET, petIds);
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().flatMap(adopt -> Stream.of(adopt.getApplicantId(), adopt.getReviewerId())),
                tasks.stream().flatMap(task -> Stream.of(task.getWorkerId(), task.getVolunteerId())),
                User::getId, User::getUsername, User::getAvatar);
        Set<Long> taskIds = tasks.stream()
                .map(FollowTask::getId)
                .collect(Collectors.toSet());
        Map<Long, FollowRecord> records = taskIds.isEmpty() ? Map.of()
                : followRecordMapper.queryByTasks(taskIds).group(FollowRecord::getTaskId,
                FollowRecord::getId, FollowRecord::getTaskId, FollowRecord::getSummary, FollowRecord::getVisitTime);
        Map<Long, List<FollowTaskResponse>> followTasks = tasks.stream()
                .map(task -> FollowTaskResponse.createBatch(task, users, records))
                .collect(Collectors.groupingBy(FollowTaskResponse::getAdoptId));
        return convert(result, adopt -> AdoptResponse.createBatch(adopt, pets, covers, users, followTasks));
    }

    public BreadingResponse buildBreadingResponse(Breading breading, User login) {
        User applicant = login != null && login.is(breading.getApplicantId()) ? login
                : userService.requireById(breading.getApplicantId(),
                User::getId, User::getUsername, User::getAvatar);
        User reviewer = login != null && login.is(breading.getReviewerId()) ? login
                : userService.selectById(breading.getReviewerId(),
                User::getId, User::getUsername, User::getAvatar);
        return BreadingResponse.create(breading, applicant, reviewer);
    }

    public Page<BreadingResponse> buildBreadingPage(Page<Breading> result) {
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().flatMap(info -> Stream.of(info.getApplicantId(), info.getReviewerId())),
                User::getId, User::getUsername, User::getAvatar);
        return convert(result, breading -> BreadingResponse.createBatch(breading, users));
    }

    public AgreementResponse buildAgreementResponse(Agreement agreement) {
        List<AgreementFileResponse> files = agreementFileMapper.queryByAgreement(agreement.getId()).list().stream()
                .filter(file -> file.getPage() != null && file.getPage() > 0)
                .sorted(java.util.Comparator.comparing(AgreementFile::getPage))
                .map(AgreementFileResponse::create)
                .toList();
        return AgreementResponse.create(agreement, resolveAgreementPetName(agreement), files);
    }

    public Page<AgreementResponse> buildAgreementPage(Page<Agreement> result) {
        Set<Long> agreementIds = result.getRecords().stream()
                .map(Agreement::getId)
                .collect(Collectors.toSet());
        Map<Long, String> petNames = buildAgreementPetNames(result.getRecords());
        Map<Long, List<AgreementFileResponse>> files = agreementFileMapper
                .queryByAgreements(agreementIds)
                .list().stream()
                .filter(file -> file.getPage() != null && file.getPage() > 0)
                .sorted(java.util.Comparator.comparing(AgreementFile::getAgreementId).thenComparing(AgreementFile::getPage))
                .map(AgreementFileResponse::create)
                .collect(Collectors.groupingBy(AgreementFileResponse::getAgreementId));
        return convert(result, agreement -> AgreementResponse.createBatch(agreement, petNames.get(agreement.getId()), files));
    }

    public FollowTaskResponse buildFollowTaskResponse(FollowTask task) {
        User worker = userService.selectById(task.getWorkerId(),
                User::getId, User::getUsername, User::getAvatar);
        User volunteer = userService.selectById(task.getVolunteerId(),
                User::getId, User::getUsername, User::getAvatar);
        FollowRecord record = followRecordMapper.queryByTask(task.getId()).one(
                FollowRecord::getId, FollowRecord::getSummary, FollowRecord::getVisitTime);
        return FollowTaskResponse.create(task, worker, volunteer, record);
    }

    public Page<FollowTaskResponse> buildFollowTaskPage(Page<FollowTask> result) {
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().flatMap(task -> Stream.of(task.getWorkerId(), task.getVolunteerId())),
                User::getId, User::getUsername, User::getAvatar);
        Set<Long> followIds = result.getRecords().stream()
                .map(FollowTask::getId)
                .collect(Collectors.toSet());
        Map<Long, FollowRecord> records = followIds.isEmpty() ? Map.of()
                : followRecordMapper.queryByTasks(followIds).group(FollowRecord::getTaskId,
                FollowRecord::getId, FollowRecord::getTaskId, FollowRecord::getSummary, FollowRecord::getVisitTime);
        return convert(result, task -> FollowTaskResponse.createBatch(task, users, records));
    }

    public Page<FollowRecordResponse> buildFollowRecordPage(Page<FollowRecord> result) {
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().map(FollowRecord::getVolunteerId),
                User::getId, User::getUsername, User::getAvatar);
        return convert(result, record -> FollowRecordResponse.createBatch(record, users));
    }

    private <T, R> Page<R> convert(Page<T> source, Function<T, R> mapper) {
        Page<R> page = PageDTO.of(source.getCurrent(), source.getSize(), source.getTotal());
        page.setRecords(source.getRecords().stream().map(mapper).toList());
        return page;
    }

    private String resolveAgreementPetName(Agreement agreement) {
        return switch (agreement.getParentType()) {
            case ADOPT -> {
                Adopt adopt = adoptMapper.requireById(agreement.getParentId(), Adopt::getPetId);
                Pet pet = petService.selectById(adopt.getPetId(), Pet::getName);
                yield pet == null ? null : pet.getName();
            }
            case BREADING -> breadingMapper.requireById(agreement.getParentId(), Breading::getPetName).getPetName();
            default -> null;
        };
    }

    private Map<Long, String> buildAgreementPetNames(List<Agreement> agreements) {
        Map<Long, String> petNames = new java.util.HashMap<>();
        Set<Long> adoptIds = agreements.stream()
                .filter(agreement -> agreement.getParentType() == ParentType.ADOPT)
                .map(Agreement::getParentId)
                .collect(Collectors.toSet());
        Set<Long> breadingIds = agreements.stream()
                .filter(agreement -> agreement.getParentType() == ParentType.BREADING)
                .map(Agreement::getParentId)
                .collect(Collectors.toSet());

        Map<Long, Long> adoptPetIds = adoptMapper.selectList(adoptIds, Adopt::getId, Adopt::getPetId).stream()
                .collect(Collectors.toMap(Adopt::getId, Adopt::getPetId));
        Map<Long, String> petNamesByPetId = petService.groupById(new java.util.HashSet<>(adoptPetIds.values()),
                Pet::getId, Pet::getName)
                .values()
                .stream()
                .collect(Collectors.toMap(Pet::getId, Pet::getName));
        for (Map.Entry<Long, Long> entry : adoptPetIds.entrySet()) {
            petNames.put(entry.getKey(), petNamesByPetId.get(entry.getValue()));
        }

        Map<Long, String> breadingNames = breadingMapper.selectList(breadingIds, Breading::getId, Breading::getPetName).stream()
                .collect(Collectors.toMap(Breading::getId, Breading::getPetName));
        petNames.putAll(breadingNames);
        return petNames;
    }
}
