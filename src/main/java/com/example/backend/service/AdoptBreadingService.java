package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.entity.property.*;
import com.example.backend.event.*;
import com.example.backend.mapper.*;
import com.example.backend.util.FileUtils;
import com.example.backend.util.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.backend.entity.property.AdoptBreadingStatus.AGREEMENT_DRAFT;
import static com.example.backend.entity.property.AdoptBreadingStatus.AGREEMENT_SIGNED;
import static com.example.backend.entity.property.ParentType.*;

@SuppressWarnings("unchecked")
@Service
@RequiredArgsConstructor
public class AdoptBreadingService extends BaseService<AdoptMapper, Adopt> {

    private final BreadingMapper breadingMapper;
    private final AgreementMapper agreementMapper;
    private final AgreementFileMapper agreementFileMapper;
    private final AgreementUpdateRecordMapper agreementUpdateRecordMapper;
    private final FollowTaskMapper followTaskMapper;
    private final FollowRecordMapper followRecordMapper;

    private PetService petService;
    private UserService userService;
    private FileService fileService;

    @Value("${file.upload}")
    private String upload;

    /**
     * 申请领养
     */
    @Transactional
    public AdoptResponse addAdopt(AdoptAddRequest request) {
        // 校验
        User login = requireLoginUser();
        petService.requireExist(request.getPetId());

        // 保存
        Adopt adopt = request.create(login.getId());
        save(adopt);
        eventPublisher.publishEvent(new AdoptAddEvent(adopt));
        return buildAdoptResponse(adopt);
    }

    /**
     * 获取领养申请
     */
    public AdoptResponse getAdopt(Long adoptId) {
        Adopt adopt = requireById(adoptId);
        return buildAdoptResponse(adopt);
    }

    /**
     * 查询领养申请
     */
    public Page<AdoptResponse> getAdopts(AdoptQueryParams paramRequest, PageParams pageRequest) {
        Page<Adopt> result = getBaseMapper().queryByRequest(paramRequest).page(pageRequest);
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
        Map<Long, FollowRecord> records = followRecordMapper.queryByTasks(taskIds).group(FollowRecord::getTaskId,
                FollowRecord::getId, FollowRecord::getTaskId, FollowRecord::getSummary, FollowRecord::getVisitTime);
        Map<Long, List<FollowTaskResponse>> followTasks = tasks.stream()
                .map(task -> FollowTaskResponse.createBatch(task, users, records))
                .collect(Collectors.groupingBy(FollowTaskResponse::getAdoptId));
        return convertDto(result, adopt -> AdoptResponse.createBatch(adopt, pets, covers, users, followTasks));
    }

    /**
     * 修改领养记录状态
     */
    @Transactional
    public AdoptResponse updateAdoptStatus(Long adoptId, String status) {
        // 校验
        User login = requireLoginUser();
        requirePermission(login.isWorker());
        Adopt adopt = requireById(adoptId);
        AdoptBreadingStatus target = AdoptBreadingStatus.get(status);
        AdoptBreadingStatus oldStatus = adopt.getStatus();
        require(oldStatus.isChangeable(), "领养状态异常");
        // 签订状态仅能通过 signAgreement 方法实现
        require(target != AdoptBreadingStatus.AGREEMENT_SIGNED, "领养状态异常");

        // 更新记录
        Date now = new Date();
        adopt.setStatus(target);
        adopt.setUpdateTime(now);
        if (target == AdoptBreadingStatus.PASS || target == AdoptBreadingStatus.REJECT)
            adopt.setReviewTime(now);
        updateById(adopt);
        eventPublisher.publishEvent(new AdoptStatusEvent(adopt, oldStatus));
        return buildAdoptResponse(adopt);
    }

    /**
     * 申请寄养
     */
    @Transactional
    public BreadingResponse addBreading(BreadingAddRequest request) {
        User login = requireLoginUser();
        Breading breading = request.create(login.getId());
        breadingMapper.insert(breading);
        eventPublisher.publishEvent(new BreadingAddEvent(breading));
        return buildBreadingResponse(breading);
    }

    /**
     * 获取寄养申请
     */
    public BreadingResponse getBreading(Long breadingId) {
        Breading breading = breadingMapper.requireById(breadingId);
        return buildBreadingResponse(breading);
    }

    /**
     * 查询寄养申请
     */
    public Page<BreadingResponse> getBreadingPets(BreadingQueryParams queryRequest, PageParams pageRequest) {
        Page<Breading> result = breadingMapper.queryByRequest(queryRequest).page(pageRequest);
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().flatMap(info -> Stream.of(info.getApplicantId(), info.getReviewerId())),
                User::getId, User::getUsername, User::getAvatar);
        return convertDto(result, breading -> BreadingResponse.createBatch(breading, users));
    }

    /**
     * 修改寄养状态
     */
    @Transactional
    public BreadingResponse updateBreadingStatus(Long breadingId, String status) {
        User login = requireLoginUser();
        requirePermission(login.isWorker());
        Breading breading = breadingMapper.requireById(breadingId);
        AdoptBreadingStatus target = AdoptBreadingStatus.get(status);
        AdoptBreadingStatus oldStatus = breading.getStatus();
        require(oldStatus.isChangeable(), "寄养状态异常");
        require(target != AdoptBreadingStatus.AGREEMENT_SIGNED, "寄养状态异常");

        Date now = new Date();
        breading.setStatus(target);
        breading.setUpdateTime(now);
        if (target == AdoptBreadingStatus.PASS || target == AdoptBreadingStatus.REJECT)
            breading.setReviewTime(now);
        breadingMapper.updateById(breading);
        eventPublisher.publishEvent(new BreadingStatusEvent(breading, oldStatus));
        return buildBreadingResponse(breading);
    }

    /**
     * 起草协议
     */
    public AgreementResponse addAgreement(AgreementAddRequest request) {
        User login = requireLoginUser();
        requirePermission(login.isWorker());

        // 保存协议
        Agreement agreement = request.create();
        agreementMapper.insert(agreement);
        AgreementUpdateRecord updateRecord = new AgreementUpdateRecord(agreement, AgreementUpdateType.CREATE);
        agreementUpdateRecordMapper.insert(updateRecord);

        // 上传文件
        List<AgreementFile> files;
        if (agreement.getType() == AgreementType.PAPER) {
            AgreementUpdateRecord uploadRecord = new AgreementUpdateRecord(agreement, AgreementUpdateType.UPLOAD);
            agreementUpdateRecordMapper.insert(uploadRecord);
            files = uploadAgreementFiles(agreement.getId(), request.getFiles());
        } else {
            files = List.of();
        }

        // 更新记录状态
        transactionTemplate.executeWithoutResult(status -> {
            ParentType parentType = agreement.getParentType();
            switch (parentType) {
                case ADOPT -> baseMapper.updateStatus(agreement.getParentId(), AGREEMENT_DRAFT).update();
                case BREADING -> breadingMapper.updateStatus(agreement.getParentId(), AGREEMENT_DRAFT).update();
                default -> throw ServiceException.invalidate("无效协议 " + parentType);
            }
            agreementUpdateRecordMapper.updateStatus(updateRecord.getId(), AgreementUpdateStatus.SUCCESS);
        });

        eventPublisher.publishEvent(new AgreementAddEvent(agreement, files, login));
        return buildAgreementResponse(agreement);
    }

    /**
     * 修改协议（电子协议）
     */
    public AgreementResponse updateAgreement(Long agreementId, AgreementUpdateRequest request) {
        User login = requireLoginUser();
        requirePermission(login.isWorker());
        Agreement agreement = agreementMapper.requireById(agreementId);

        // 记录旧协议内容
        AgreementUpdateRecord record = recordAgreementUpdate(agreement);
        request.applyTo(agreement);
        agreementMapper.updateById(agreement);
        agreementUpdateRecordMapper.updateStatus(record.getId(), AgreementUpdateStatus.SUCCESS);
        eventPublisher.publishEvent(new AgreementUpdateEvent(agreement, login));
        return buildAgreementResponse(agreement);
    }

    /**
     * 修改协议（上传纸质扫描件）
     */
    public List<AgreementFileResponse> uploadAgreement(Long agreementId, AgreementFilesUploadTable request) {
        User login = requireLoginUser();
        requirePermission(login.isWorker());

        Agreement agreement = agreementMapper.requireById(agreementId);
        AgreementUpdateRecord record = recordAgreementUpdate(agreement);

        // 上传文件
        List<Pair<String, Date>> files = fileService.uploadImages(request.getFiles(), agreementId, AGREEMENT);
        List<AgreementFile> agreementFiles = new ArrayList<>(files.size());
        for (int i = 0; i < files.size(); ++i) {
            agreementFiles.add(new AgreementFile(agreementId, files.get(i), i + 1));
        }
        agreementFileMapper.insert(agreementFiles);
        agreementUpdateRecordMapper.updateStatus(record.getId(), AgreementUpdateStatus.SUCCESS);

        // 更新协议
        agreement.setUpdateTime(new Date());
        agreementMapper.setUpdateTime(agreementId).update();
        eventPublisher.publishEvent(new AgreementUpdateEvent(agreement, login));
        return agreementFiles.stream()
                .map(AgreementFileResponse::create)
                .toList();
    }

    /*
    上传协议图片
     */
    private List<AgreementFile> uploadAgreementFiles(Long agreementId, List<MultipartFile> files) {
        List<Pair<String, Date>> result = fileService.uploadImages(files, agreementId, AGREEMENT);
        List<AgreementFile> agreementFiles = new ArrayList<>(files.size());
        for (int i = 0; i < result.size(); i++) {
            agreementFiles.add(new AgreementFile(agreementId, result.get(i), i + 1));
        }
        agreementFileMapper.insert(agreementFiles);
        return agreementFiles;
    }

    /*
    记录旧协议内容
     */
    private AgreementUpdateRecord recordAgreementUpdate(Agreement agreement) {
        AgreementUpdateRecord record = new AgreementUpdateRecord(agreement, AgreementUpdateType.UPDATE);
        if (agreement.getType() == AgreementType.PAPER) {
            // 纸质协议
            // 1. 记录协议文件集
            List<AgreementFile> agreementFiles = agreementFileMapper.queryByAgreement(agreement.getId()).list();
            record.setContent(objectMapper.writeValueAsString(agreementFiles));
            agreementUpdateRecordMapper.insert(record);
            // 2. 备份协议文件
            Path path = FileUtils.generatePath(upload, AGREEMENT, agreement.getId());
            Path newPath = FileUtils.generatePath(upload, AGREEMENT_RECORD, record.getId());
            FileUtils.copyDirectory(path, newPath);
            FileUtils.deleteDirectory(path);
            if (Files.isDirectory(path))
                throw ServiceException.system("文件转移异常，请联系管理员手动处理");
        } else {
            agreementUpdateRecordMapper.insert(record);
        }
        return record;
    }

    /**
     * 签署协议
     */
    public AgreementResponse signAgreement(Long agreementId, MultipartFile sign) {
        User login = requireLoginUser();
        Agreement agreement = agreementMapper.requireById(agreementId);
        requirePermission(login.isWorker());
        require(null == agreement.getSign(), "协议已签署");
        // 状态检查
        Long parentId = agreement.getParentId();
        if (agreement.getParentType() == ParentType.ADOPT)
            requireEqual(AGREEMENT_DRAFT, selectById(parentId, Adopt::getStatus).getStatus(), "领养状态异常");
        else if (agreement.getParentType() == ParentType.BREADING)
            requireEqual(AGREEMENT_DRAFT, breadingMapper.selectById(parentId, Breading::getStatus).getStatus(), "寄养状态异常");
        else
            throw ServiceException.invalidate("服务类型异常");

        AgreementUpdateRecord record = new AgreementUpdateRecord(agreement, AgreementUpdateType.SIGN);
        agreementUpdateRecordMapper.insert(record);

        // 上传文件
        String filename = fileService.uploadImage(sign, agreementId, AGREEMENT);
        AgreementFile file = new AgreementFile(agreementId, filename, 0);
        agreementFileMapper.insert(file);

        // 更新数据
        transactionTemplate.executeWithoutResult(status -> {
            agreementMapper.sign(agreementId, filename, new Date()).update();
            switch (agreement.getParentType()) {
                case ADOPT -> baseMapper.updateStatus(parentId, AGREEMENT_SIGNED).update();
                case BREADING -> breadingMapper.updateStatus(parentId, AGREEMENT_SIGNED).update();
                default -> throw ServiceException.invalidate("无效类型 " + agreement.getParentType());
            }
            agreementUpdateRecordMapper.updateStatus(record.getId(), AgreementUpdateStatus.SUCCESS);
        });
        eventPublisher.publishEvent(new AgreementUpdateEvent(agreement, login));
        return buildAgreementResponse(agreement);
    }

    /**
     * 获取协议
     */
    public AgreementResponse getAgreement(Long agreementId) {
        Agreement agreement = agreementMapper.requireById(agreementId);
        return buildAgreementResponse(agreement);
    }

    /**
     * 查询协议列表
     */
    public Page<AgreementResponse> getAgreements(AgreementQueryParams queryRequest, PageParams pageRequest) {
        Page<Agreement> result = agreementMapper.queryByRequest(queryRequest).page(pageRequest);
        Set<Long> agreementIds = result.getRecords().stream()
                .map(Agreement::getId)
                .collect(Collectors.toSet());
        Map<Long, List<AgreementFileResponse>> files = agreementFileMapper
                .queryByAgreements(agreementIds)
                .groupList(AgreementFile::getAgreementId, AgreementFileResponse::create);
        return convertDto(result, agreement -> AgreementResponse.createBatch(agreement, files));
    }

    /**
     * 创建跟踪任务
     */
    @Transactional
    public FollowTaskResponse addFollowTask(Long adoptId, FollowTaskAddRequest request) {
        // 权限校验
        User login = requireLoginUser();
        requirePermission(login.isWorker());
        Long volunteerId = request.getVolunteerId();
        User volunteer = userService.requireById(volunteerId, User::getRole);
        requirePermission(volunteer.isVolunteer());
        Adopt adopt = requireById(adoptId,
                Adopt::getStatus);
        requireEqual(AdoptBreadingStatus.TRACKING, adopt.getStatus(), "领养状态错误");

        // 记录
        FollowTask task = request.create(adoptId, login.getId());
        followTaskMapper.insert(task);
        eventPublisher.publishEvent(new FollowTaskAddEvent(task, login));
        return buildFollowTaskResponse(task);
    }

    /**
     * 更新跟踪任务
     */
    @Transactional
    public FollowTaskResponse updateFollowTask(Long taskId, FollowTaskUpdateRequest request) {
        User login = requireLoginUser();
        FollowTask task = followTaskMapper.requireById(taskId);
        requirePermission(login.isWorker() || login.is(task.getVolunteerId()));
        if (!login.isWorker()) {
            // 以下内容必须由工作人员修改
            requirePermission(
                    request.getVolunteerId() == null || Objects.equals(request.getVolunteerId(), task.getVolunteerId()));
            requirePermission(
                    request.getPlanTime() == null || Objects.equals(request.getPlanTime(), task.getPlanTime()));
        }

        request.applyTo(task);
        followTaskMapper.updateById(task);
        eventPublisher.publishEvent(new FollowTaskUpdateEvent(task, login));
        return buildFollowTaskResponse(task);
    }

    /**
     * 获取跟踪任务
     */
    public FollowTaskResponse getFollowTask(Long taskId) {
        FollowTask task = followTaskMapper.requireById(taskId);
        return buildFollowTaskResponse(task);
    }

    /**
     * 查询跟踪任务列表
     */
    public Page<FollowTaskResponse> getFollowTasks(FollowTaskQueryParams query, PageParams page) {
        Page<FollowTask> result = followTaskMapper.queryByRequest(query).page(page);
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().flatMap(task -> Stream.of(task.getWorkerId(), task.getVolunteerId())),
                User::getId, User::getUsername);
        Set<Long> followIds = result.getRecords().stream()
                .map(FollowTask::getId)
                .collect(Collectors.toSet());
        Map<Long, FollowRecord> records = followRecordMapper.queryByTasks(followIds).groupById(
                FollowRecord::getId, FollowRecord::getSummary, FollowRecord::getVisitTime);
        return convertDto(result, task -> FollowTaskResponse.createBatch(task, users, records));
    }

    /**
     * 创建跟踪记录
     */
    @Transactional
    public FollowRecordResponse addFollowRecord(Long taskId, FollowRecordAddRequest request) {
        // 权限校验
        User login = requireLoginUser();
        FollowTask task = followTaskMapper.requireById(taskId,
                FollowTask::getStatus);
        requireEqual(FollowTaskStatus.IN_PROGRESS, task.getStatus(), "跟踪任务状态错误");
        boolean allowed = login.isWorker() || Objects.equals(task.getVolunteerId(), login.getId());
        requirePermission(allowed);
        require(!followRecordMapper.queryByTask(taskId).exists(), "跟踪记录已存在");

        FollowRecord record = request.create(taskId, login.getId());
        followRecordMapper.insert(record);
        eventPublisher.publishEvent(new FollowRecordEvent(record));
        User volunteer = userService.selectById(record.getVolunteerId(),
                User::getId, User::getUsername, User::getAvatar);
        return FollowRecordResponse.create(record, volunteer);
    }

    /**
     * 获取跟踪记录
     */
    public Page<FollowRecordResponse> getFollowRecords(Long taskId, PageParams request) {
        Page<FollowRecord> records = followRecordMapper.queryByTask(taskId).page(request);
        Map<Long, User> users = userService.groupById(
                records.getRecords().stream().map(FollowRecord::getVolunteerId),
                User::getId, User::getUsername, User::getAvatar);
        return convertDto(records,
                record -> FollowRecordResponse.createBatch(record, users));
    }

    /**
     * 查询跟踪记录
     */
    public Page<FollowRecordResponse> getFollowRecords(FollowRecordQueryParams queryRequest, PageParams pageRequest) {
        Page<FollowRecord> result = followRecordMapper.queryByRequest(queryRequest).page(pageRequest);
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().map(FollowRecord::getVolunteerId),
                User::getId, User::getUsername, User::getAvatar);
        return convertDto(result, record -> FollowRecordResponse.createBatch(record, users));
    }

    private AdoptResponse buildAdoptResponse(Adopt adopt) {
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
        Map<Long, FollowRecord> records = followRecordMapper.queryByTasks(taskIds).group(FollowRecord::getTaskId,
                FollowRecord::getId, FollowRecord::getTaskId, FollowRecord::getSummary, FollowRecord::getVisitTime);
        return AdoptResponse.create(adopt,
                pet, cover,
                users.get(adopt.getApplicantId()),
                adopt.getReviewerId() == null ? null : users.get(adopt.getReviewerId()),
                tasks.stream().map(task -> FollowTaskResponse.createBatch(task, users, records)).toList());
    }

    private BreadingResponse buildBreadingResponse(Breading breading) {
        User login = requireLoginUser();
        User applicant = login.is(breading.getApplicantId()) ? login : userService.requireById(breading.getApplicantId(),
                User::getId, User::getUsername, User::getAvatar);
        User reviewer = login.is(breading.getReviewerId()) ? login : userService.selectById(breading.getReviewerId(),
                User::getId, User::getUsername, User::getAvatar);
        return BreadingResponse.create(breading, applicant, reviewer);
    }

    private AgreementResponse buildAgreementResponse(Agreement agreement) {
        List<AgreementFileResponse> files = agreementFileMapper.queryByAgreement(agreement.getId()).list().stream()
                .filter(file -> file.getPage() != null && file.getPage() > 0)
                .map(AgreementFileResponse::create)
                .toList();
        return AgreementResponse.create(agreement, files);
    }

    private FollowTaskResponse buildFollowTaskResponse(FollowTask task) {
        User worker = userService.selectById(task.getWorkerId(),
                User::getId, User::getUsername, User::getAvatar);
        User volunteer = userService.selectById(task.getVolunteerId(),
                User::getId, User::getUsername, User::getAvatar);
        FollowRecord record = followRecordMapper.queryByTask(task.getId()).one(
                FollowRecord::getId, FollowRecord::getSummary, FollowRecord::getVisitTime);
        return FollowTaskResponse.create(task, worker, volunteer, record);
    }

    @Autowired
    public void setServices(PetService petService, UserService userService, FileService fileService) {
        this.petService = petService;
        this.userService = userService;
        this.fileService = fileService;
    }
}
