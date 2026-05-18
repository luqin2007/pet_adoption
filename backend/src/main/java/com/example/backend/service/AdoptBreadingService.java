package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.entity.property.*;
import com.example.backend.event.*;
import com.example.backend.facade.AdoptBreadingFacade;
import com.example.backend.mapper.*;
import com.example.backend.util.ServiceException;
import com.example.backend.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.backend.entity.property.AdoptBreadingStatus.*;
import static com.example.backend.entity.property.AgreementType.PAPER;
import static com.example.backend.entity.property.ParentType.AGREEMENT;
import static com.example.backend.entity.property.ParentType.VOLUNTEER_TASK;

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
    private final VolunteerTaskMapper volunteerTaskMapper;
    private final VolunteerShiftMapper volunteerShiftMapper;
    private final VolunteerShiftStatusRecordMapper volunteerShiftStatusRecordMapper;
    private final AdoptBreadingFacade adoptBreadingFacade;
    private final LocationMapper locationMapper;
    private final InformationService informationService;

    private PetService petService;
    private UserService userService;
    private FileService fileService;

    @Value("${key.agreement.uuid}")
    private String agreementTemplate;
    @Value("${key.agreement.file}")
    private String agreementFileTemplate;

    /**
     * 申请领养
     */
    @Transactional
    public AdoptResponse addAdopt(AdoptAddRequest request) {
        // 校验
        User login = requireLoginUser();
        PetStatus petStatus = petService.requireById(request.getPetId(), Pet::getStatus).getStatus();
        requirePermission(petStatus.isAdoptable());

        // 保存
        Adopt adopt = request.create(login.getId());
        save(adopt);
        Location location = informationService.createValidatedLocation(request, ParentType.ADOPT, adopt.getId(), login.getId());
        locationMapper.insert(location);
        eventPublisher.publishEvent(new AdoptAddEvent(adopt, login));
        return adoptBreadingFacade.buildAdoptResponse(adopt);
    }

    /**
     * 获取领养申请
     */
    public AdoptResponse getAdopt(Long adoptId) {
        User login = requireLoginUser();
        Adopt adopt = requireById(adoptId);
        requirePermission(login.isWorker() || login.is(adopt.getApplicantId()));
        return adoptBreadingFacade.buildAdoptResponse(adopt);
    }

    /**
     * 查询领养申请
     */
    public Page<AdoptResponse> getAdopts(AdoptQueryParams paramRequest, PageParams pageRequest) {
        User login = requireLoginUser();
        if (!login.isWorker()) {
            paramRequest.setUser(Set.of(login.getId()));
        }
        Page<Adopt> result = getBaseMapper().queryByRequest(paramRequest).page(pageRequest);
        return adoptBreadingFacade.buildAdoptPage(result);
    }

    /**
     * 修改领养记录状态
     */
    @Transactional
    public AdoptResponse updateAdoptStatus(Long adoptId, String status) {
        // 校验
        User login = requireLoginUser();
        AdoptBreadingStatus target = AdoptBreadingStatus.get(status);
        if (target != CANCEL) {
            requirePermission(login.isWorker());
        }
        Adopt adopt = requireById(adoptId);
        if (target == CANCEL) {
            requirePermission(login.isWorker() || login.is(adopt.getApplicantId()));
        }
        AdoptBreadingStatus oldStatus = adopt.getStatus();
        require(oldStatus.isChangeable(), "exception.invalidate.adopt.status_abnormal");
        // 签订状态仅能通过 signAgreement 方法实现
        require(target != AGREEMENT_DRAFT
                && target != AGREEMENT_PENDING_CONFIRM
                && target != AGREEMENT_SIGNED, "exception.invalidate.adopt.status_abnormal");

        // 更新记录
        Date now = new Date();
        adopt.setStatus(target);
        adopt.setUpdateTime(now);
        if (target == AdoptBreadingStatus.PASS || target == AdoptBreadingStatus.REJECT) {
            adopt.setReviewTime(now);
            adopt.setReviewerId(login.getId());
        }
        updateById(adopt);
        eventPublisher.publishEvent(new AdoptStatusEvent(adopt, login));
        return adoptBreadingFacade.buildAdoptResponse(adopt);
    }

    /**
     * 申请寄养
     */
    @Transactional
    public BreadingResponse addBreading(BreadingAddRequest request) {
        User login = requireLoginUser();
        Breading breading = request.create(login.getId());
        breadingMapper.insert(breading);
        eventPublisher.publishEvent(new BreadingAddEvent(breading, login));
        return adoptBreadingFacade.buildBreadingResponse(breading, login);
    }

    /**
     * 获取寄养申请
     */
    public BreadingResponse getBreading(Long breadingId) {
        Breading breading = breadingMapper.requireById(breadingId);
        return adoptBreadingFacade.buildBreadingResponse(breading, requireLoginUser());
    }

    /**
     * 查询寄养申请
     */
    public Page<BreadingResponse> getBreadingPets(BreadingQueryParams queryRequest, PageParams pageRequest) {
        Page<Breading> result = breadingMapper.queryByRequest(queryRequest).page(pageRequest);
        return adoptBreadingFacade.buildBreadingPage(result);
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
        require(oldStatus.isChangeable(), "exception.invalidate.breading.status_abnormal");
        require(target != AGREEMENT_DRAFT
                && target != AGREEMENT_PENDING_CONFIRM
                && target != AGREEMENT_SIGNED, "exception.invalidate.breading.status_abnormal");

        Date now = new Date();
        breading.setStatus(target);
        breading.setUpdateTime(now);
        if (target == AdoptBreadingStatus.PASS || target == AdoptBreadingStatus.REJECT)
            breading.setReviewTime(now);
        breadingMapper.updateById(breading);
        eventPublisher.publishEvent(new BreadingStatusEvent(breading, login));
        return adoptBreadingFacade.buildBreadingResponse(breading, login);
    }

    /**
     * 准备起草协议
     */
    public String beginAgreement() {
        User login = requireLoginUser();
        requirePermission(login.isWorker());
        return beginRedisUuid(agreementTemplate, String.valueOf(login.getId()));
    }

    /**
     * 起草协议
     */
    @Transactional
    public AgreementResponse addAgreement(AgreementAddRequest request) {
        User login = requireLoginUser();
        requirePermission(login.isWorker());

        // 检查 uuid
        String uuid = request.getUuid();
        if (AgreementType.get(request.getType()) == PAPER) {
            String user = requireRedisUuid(agreementTemplate, uuid);
            requirePermission(login.is(Long.valueOf(user)));
        }

        // 检查申请
        ParentType parentType = ParentType.get(request.getParentType());
        AdoptBreadingStatus status;
        Long applicantId;
        switch (parentType) {
            case ADOPT -> {
                Adopt adopt = requireById(request.getParentId(), Adopt::getStatus, Adopt::getApplicantId);
                status = adopt.getStatus();
                applicantId = adopt.getApplicantId();
            }
            case BREADING -> {
                Breading breading = breadingMapper.requireById(request.getParentId(), Breading::getStatus, Breading::getApplicantId);
                status = breading.getStatus();
                applicantId = breading.getApplicantId();
            }
            default -> throw ServiceException.system("exception.system.agreement.parent_type_invalid");
        }
        Agreement agreement = request.create(applicantId, login.getId());
        requireEqual(AdoptBreadingStatus.PASS, status, "exception.invalidate.adopt_breading_status");

        // 保存协议
        agreementMapper.insert(agreement);
        agreementUpdateRecordMapper.insert(new AgreementUpdateRecord(agreement, AgreementUpdateType.CREATE));

        // 转移文件
        List<String> fileOrders = request.getFileOrder() == null ? List.of() : request.getFileOrder();
        List<AgreementFile> files = new ArrayList<>(fileOrders.size());
        if (agreement.getType() == PAPER) {
            Map<String, TempFileInfo> fileMap = fileService
                    .saveTempFiles(agreementFileTemplate, uuid, agreement, AGREEMENT)
                    .collect(Collectors.toMap(TempFileInfo::getFilename, Function.identity()));
            for (int i = 0; i < fileOrders.size(); i++) { // 处理页码
                TempFileInfo file = fileMap.get(fileOrders.get(i));
                files.add(new AgreementFile(agreement.getId(), file.getFilename(), file.getCreateTime(), i + 1));
            }
            agreementFileMapper.insert(files);
        }

        // 更新记录状态
        switch (parentType) {
            case ADOPT -> baseMapper.updateStatus(agreement.getParentId(), AGREEMENT_DRAFT).update();
            case BREADING -> breadingMapper.updateStatus(agreement.getParentId(), AGREEMENT_DRAFT).update();
            default -> throw ServiceException.system("exception.system.agreement.parent_type_invalid");
        }

        eventPublisher.publishEvent(new AgreementAddEvent(agreement, files, login));
        return adoptBreadingFacade.buildAgreementResponse(agreement);
    }

    /**
     * 修改协议（电子协议）
     */
    @Transactional
    public AgreementResponse updateAgreement(Long agreementId, AgreementUpdateRequest request) {
        User login = requireLoginUser();
        Agreement agreement = agreementMapper.requireById(agreementId);
        requirePermission(login.is(agreement.getReviewerId()) || login.is(agreement.getApplicantId()));
        requireAgreementStatus(agreement, AGREEMENT_DRAFT);

        // 记录旧协议内容
        recordAgreementUpdate(agreement);
        request.applyTo(agreement);
        agreementMapper.updateById(agreement);
        eventPublisher.publishEvent(new AgreementUpdateEvent(agreement, login));
        return adoptBreadingFacade.buildAgreementResponse(agreement);
    }

    /**
     * 修改协议（为纸质协议上传一张扫描件）
     */
    @Transactional
    public List<AgreementFileResponse> uploadAgreement(Long agreementId, AgreementFilesUploadTable request) {
        User login = requireLoginUser();
        Agreement agreement = agreementMapper.requireById(agreementId);
        requirePermission(login.is(agreement.getReviewerId()) || login.is(agreement.getApplicantId()));

        // 备份协议数据
        requireAgreementStatus(agreement, AGREEMENT_DRAFT);
        recordAgreementUpdate(agreement);
        if (agreement.getType() == AgreementType.ELECTRONIC) { // 切换协议类型
            agreement.setType(PAPER);
            agreement.setContent(null);
        }

        // 更新页码
        List<AgreementFile> files = agreementFileMapper.queryByAgreement(agreementId).list();
        int page = request.getPage();
        require(page >= 1 && page <= files.size() + 1, "request.adopt_breading.agreement.page");
        files.stream()
                .filter(file -> file.getPage() >= page)
                .peek(file -> file.setPage(file.getPage() + 1))
                .forEach(agreementFileMapper::updateById);

        // 上传文件
        String filename = fileService.uploadImage(request.getFile(), agreementId, AGREEMENT);
        AgreementFile agreementFile = new AgreementFile(agreementId, filename, page);
        agreementFileMapper.insert(agreementFile);

        // 更新协议
        agreement.setUpdateTime(new Date());
        agreementMapper.updateById(agreement);
        eventPublisher.publishEvent(new AgreementUpdateEvent(agreement, login));
        return listAgreementFileResponses(agreementId);
    }

    /**
     * 准备阶段上传协议扫描件
     */
    public String uploadAgreementFile(String uuid, MultipartFile file) {
        User login = requireLoginUser();
        requirePermission(login.isWorker());
        String user = requireRedisUuid(agreementTemplate, uuid);
        requirePermission(login.is(Long.valueOf(user)));

        // 上传文件
        TempFileInfo fileInfo = fileService.uploadTempImage(file, null, uuid, agreementFileTemplate, AGREEMENT);
        return fileInfo.getFilename();
    }

    /**
     * 准备阶段删除协议扫描件
     */
    public void deleteAgreementFile(String uuid, String filename) {
        User login = requireLoginUser();
        requirePermission(login.isWorker());
        String user = requireRedisUuid(agreementTemplate, uuid);
        requirePermission(login.is(Long.valueOf(user)));
        fileService.deleteTempFile(agreementFileTemplate, uuid, filename, AGREEMENT);
    }

    /**
     * 删除已有协议中的纸质扫描件
     */
    @Transactional
    public List<AgreementFileResponse> deleteAgreementFile(Long agreementId, Long fileId) {
        User login = requireLoginUser();
        Agreement agreement = agreementMapper.requireById(agreementId);
        requirePermission(login.is(agreement.getReviewerId()) || login.is(agreement.getApplicantId()));
        requireAgreementStatus(agreement, AGREEMENT_DRAFT);
        requireEqual(PAPER, agreement.getType(), "exception.invalidate.agreement_type");
        AgreementFile target = agreementFileMapper.requireById(fileId);
        requireEqual(agreementId, target.getAgreementId(), "exception.not_found.agreement_file");

        recordAgreementUpdate(agreement);
        agreementFileMapper.deleteById(fileId);
        // 有备份 不实际删除文件
        // fileService.deleteFile(target.getFilename(), agreementId, AGREEMENT);

        List<AgreementFile> files = agreementFileMapper.queryByAgreement(agreementId).list().stream()
                .filter(file -> !Objects.equals(file.getId(), fileId))
                .toList();
        for (int i = 0; i < files.size(); i++) {
            files.get(i).setPage(i + 1);
        }
        agreementFileMapper.updateById(files);
        agreement.setUpdateTime(new Date());
        agreementMapper.updateById(agreement);
        eventPublisher.publishEvent(new AgreementUpdateEvent(agreement, login));
        return listAgreementFileResponses(agreementId);
    }

    /**
     * 调整已有协议中的纸质扫描件顺序
     */
    @Transactional
    public List<AgreementFileResponse> reorderAgreementFiles(Long agreementId, AgreementFilesOrderRequest request) {
        User login = requireLoginUser();
        Agreement agreement = agreementMapper.requireById(agreementId);
        requirePermission(login.is(agreement.getReviewerId()) || login.is(agreement.getApplicantId()));
        requireAgreementStatus(agreement, AGREEMENT_DRAFT);
        requireEqual(PAPER, agreement.getType(), "exception.invalidate.agreement_type");

        List<AgreementFile> files = agreementFileMapper.queryByAgreement(agreementId).list();
        List<Long> fileOrder = request.getFileOrder();
        require(fileOrder.size() == files.size(), "request.adopt_breading.agreement.file_order");

        Map<Long, AgreementFile> fileMap = files.stream()
                .collect(Collectors.toMap(AgreementFile::getId, Function.identity()));
        require(fileMap.size() == fileOrder.size(), "request.adopt_breading.agreement.file_order");
        require(fileMap.keySet().containsAll(fileOrder), "request.adopt_breading.agreement.file_order");

        // 更新
        recordAgreementUpdate(agreement);
        for (int i = 0; i < fileOrder.size(); i++) {
            AgreementFile file = fileMap.get(fileOrder.get(i));
            file.setPage(i + 1);
        }
        agreementFileMapper.updateById(files);

        agreement.setUpdateTime(new Date());
        agreementMapper.updateById(agreement);
        eventPublisher.publishEvent(new AgreementUpdateEvent(agreement, login));
        return listAgreementFileResponses(agreementId);
    }

    /*
    记录旧协议内容
     */
    private void recordAgreementUpdate(Agreement agreement) {
        AgreementUpdateRecord record = new AgreementUpdateRecord(agreement, AgreementUpdateType.UPDATE);
        if (agreement.getType() == PAPER) {
            // 纸质协议 记录协议文件集
            List<AgreementFile> agreementFiles = agreementFileMapper.queryByAgreement(agreement.getId()).list();
            record.setContent(objectMapper.writeValueAsString(agreementFiles));
            agreementUpdateRecordMapper.insert(record);
        } else {
            agreementUpdateRecordMapper.insert(record);
        }
    }

    /**
     * 签署协议
     */
    @Transactional
    public AgreementResponse signAgreement(Long agreementId, MultipartFile sign) {
        User login = requireLoginUser();
        Agreement agreement = agreementMapper.requireById(agreementId);
        requirePermission(login.is(agreement.getApplicantId()));
        Long parentId = agreement.getParentId();
        if (agreement.getParentType() == ParentType.ADOPT) {
            Adopt adopt = requireById(parentId, Adopt::getStatus);
            requireEqual(AGREEMENT_DRAFT, adopt.getStatus(), "exception.invalidate.adopt.status_abnormal");
        } else if (agreement.getParentType() == ParentType.BREADING) {
            Breading breading = breadingMapper.requireById(parentId, Breading::getStatus);
            requireEqual(AGREEMENT_DRAFT, breading.getStatus(), "exception.invalidate.breading.status_abnormal");
        } else {
            throw ServiceException.system("exception.system.agreement.parent_type_invalid");
        }

        if (agreement.getSignTime() != null) {
            throw ServiceException.conflict("exception.conflict.agreement.signed");
        }
        String oldSign = agreement.getSign();
        Date now = new Date();
        String filename = fileService.uploadImage(sign, agreementId, AGREEMENT);
        agreement.setSign(filename);
        agreement.setSignTime(null);
        agreement.setUpdateTime(now);
        agreementUpdateRecordMapper.insert(new AgreementUpdateRecord(agreement, AgreementUpdateType.SIGN_UPLOAD));

        // 更新数据
        agreementMapper.uploadSign(agreementId, filename, now).update();
        switch (agreement.getParentType()) {
            case ADOPT -> baseMapper.updateStatus(parentId, AGREEMENT_PENDING_CONFIRM).update();
            case BREADING -> breadingMapper.updateStatus(parentId, AGREEMENT_PENDING_CONFIRM).update();
            default -> throw ServiceException.system("exception.system.agreement.parent_type_invalid");
        }
        if (oldSign != null && !oldSign.equals(filename)) {
            fileService.deleteFile(oldSign, agreementId, AGREEMENT);
        }
        eventPublisher.publishEvent(new AgreementUpdateEvent(agreement, login));
        return adoptBreadingFacade.buildAgreementResponse(agreement);
    }

    /**
     * 确认签署协议
     */
    @Transactional
    public AgreementResponse confirmAgreementSign(Long agreementId, ConfirmAgreementSignRequest request) {
        User login = requireLoginUser();
        requirePermission(login.isWorker());
        Agreement agreement = agreementMapper.requireById(agreementId);
        requirePermission(login.is(agreement.getReviewerId()));
        requireAgreementStatus(agreement, AGREEMENT_PENDING_CONFIRM);
        require(StringUtils.hasText(agreement.getSign()), "exception.invalidate.agreement.status_abnormal");

        Long parentId = agreement.getParentId();
        if (Boolean.TRUE.equals(request.getAgree())) { // 同意
            Date now = new Date();
            agreement.setSignTime(now);
            agreement.setUpdateTime(now);
            agreementUpdateRecordMapper.insert(new AgreementUpdateRecord(agreement, AgreementUpdateType.SIGN_CONFIRM));

            agreementMapper.confirmSign(agreementId, now).update();
            switch (agreement.getParentType()) {
                case ADOPT -> baseMapper.updateStatus(parentId, AGREEMENT_SIGNED).update();
                case BREADING -> breadingMapper.updateStatus(parentId, AGREEMENT_SIGNED).update();
                default -> throw ServiceException.system("exception.system.agreement.parent_type_invalid");
            }
        } else { // 拒绝：回退状态
            String oldSign = agreement.getSign();
            agreement.setSign(null);
            agreement.setSignTime(null);
            agreement.setUpdateTime(new Date());
            AgreementUpdateRecord record = new AgreementUpdateRecord(agreement, AgreementUpdateType.SIGN_REFUSE);
            record.setContent(request.getReason());
            agreementUpdateRecordMapper.insert(record);

            // 更新数据
            agreementMapper.uploadSign(agreementId, null, null).update();
            switch (agreement.getParentType()) {
                case ADOPT -> baseMapper.updateStatus(parentId, AGREEMENT_DRAFT).update();
                case BREADING -> breadingMapper.updateStatus(parentId, AGREEMENT_DRAFT).update();
                default -> throw ServiceException.system("exception.system.agreement.parent_type_invalid");
            }
            if (oldSign != null) {
                fileService.deleteFile(oldSign, agreementId, AGREEMENT);
            }
        }

        eventPublisher.publishEvent(new AgreementUpdateEvent(agreement, login));
        return adoptBreadingFacade.buildAgreementResponse(agreement);
    }

    private List<AgreementFileResponse> listAgreementFileResponses(Long agreementId) {
        return agreementFileMapper.queryByAgreement(agreementId).list().stream()
                .map(AgreementFileResponse::create)
                .toList();
    }

    private void requireAgreementStatus(Agreement agreement, AdoptBreadingStatus expected) {
        AdoptBreadingStatus status = switch (agreement.getParentType()) {
            case ADOPT -> requireById(agreement.getParentId(), Adopt::getStatus).getStatus();
            case BREADING -> breadingMapper.requireById(agreement.getParentId(), Breading::getStatus).getStatus();
            default -> throw ServiceException.system("exception.system.agreement.parent_type_invalid");
        };
        requireEqual(expected, status, "exception.invalidate.agreement.status_abnormal");
    }

    /**
     * 获取协议
     */
    public AgreementResponse getAgreement(Long agreementId) {
        User login = requireLoginUser();
        Agreement agreement = agreementMapper.requireById(agreementId);
        requirePermission(login.isWorker() || login.is(agreement.getApplicantId()));
        return adoptBreadingFacade.buildAgreementResponse(agreement);
    }

    /**
     * 查询协议列表
     */
    public Page<AgreementResponse> getAgreements(AgreementQueryParams queryRequest, PageParams pageRequest) {
        User login = requireLoginUser();
        if (!login.isWorker()) {
            queryRequest.setUser(login.getId());
        }
        Page<Agreement> result = agreementMapper.queryByRequest(queryRequest).page(pageRequest);
        return adoptBreadingFacade.buildAgreementPage(result);
    }

    /**
     * 创建回访任务
     */
    @Transactional
    public FollowTaskResponse addFollowTask(Long adoptId, FollowTaskAddRequest request) {
        // 权限校验
        User login = requireLoginUser();
        requirePermission(login.isWorker());
        Long volunteerId = request.getVolunteerId();
        User volunteer = userService.requireById(volunteerId, User::getRole);
        requirePermission(volunteer.isVolunteer());
        Adopt adopt = requireById(adoptId, Adopt::getStatus);
        require(Set.of(AdoptBreadingStatus.AGREEMENT_SIGNED, AdoptBreadingStatus.TRACKING).contains(adopt.getStatus()),
                "exception.invalidate.adopt.status_invalid");

        // 记录
        FollowTask task = request.create(adoptId, adopt.getApplicantId(), login.getId());
        followTaskMapper.insert(task);
        if (adopt.getStatus() == AdoptBreadingStatus.AGREEMENT_SIGNED) {
            baseMapper.updateStatus(adoptId, AdoptBreadingStatus.TRACKING).update();
        }
        eventPublisher.publishEvent(new FollowTaskAddEvent(task, login));
        return adoptBreadingFacade.buildFollowTaskResponse(task);
    }

    /**
     * 更新回访任务
     */
    @Transactional
    public FollowTaskResponse updateFollowTask(Long taskId, FollowTaskUpdateRequest request) {
        User login = requireLoginUser();
        FollowTask task = followTaskMapper.requireById(taskId);
        FollowTaskStatus status = FollowTaskStatus.get(request.getStatus());
        FollowTaskStatus oldStatus = task.getStatus();
        Adopt adopt = requireById(task.getAdoptId(), Adopt::getApplicantId, Adopt::getPetId);

        boolean isTaskWorker = login.isWorker() && login.is(task.getWorkerId());
        boolean isTaskWorkerOrAdmin = isTaskWorker || login.isAdmin();
        requirePermission(login.is(task.getWorkerId()) // 负责工作人员
                || login.is(task.getVolunteerId()) // 负责志愿者
                || login.is(adopt.getApplicantId()) // 领养人：可修改时间
                || login.isAdmin());
        if (status == FollowTaskStatus.FINISH) {
            requirePermission(oldStatus == FollowTaskStatus.IN_PROGRESS && isTaskWorkerOrAdmin);
        }
        if (!isTaskWorkerOrAdmin) {
            /*
            以下内容必须由工作人员修改：
            - 志愿者
            - 负责工作人员
            - 状态
             */
            requirePermission(Objects.equals(request.getVolunteerId(), task.getVolunteerId()));
            requirePermission(Objects.equals(request.getWorkerId(), task.getWorkerId()));
            requirePermission(status == task.getStatus());
        }

        if (status == FollowTaskStatus.IN_PROGRESS && oldStatus != FollowTaskStatus.IN_PROGRESS) {
            require(task.getPlanTime() != null, "exception.invalidate.follow_task.status_invalid");
            VolunteerTask volunteerTask = volunteerTaskMapper.queryFollowTask(task.getId()).one();
            if (volunteerTask == null) { // 新建回访志愿者任务
                Pet pet = petService.requireById(adopt.getPetId(), Pet::getId, Pet::getName);
                Location source = locationMapper.queryByParent(ParentType.PET, adopt.getPetId()).require();
                volunteerTask = new VolunteerTask(null,
                        VolunteerTaskType.FOLLOW_VISIT,
                        task.getId(),
                        null,
                        "回访任务：" + pet.getName("未命名宠物"),
                        "领养回访任务 #" + task.getId(),
                        task.getPlanTime(),
                        task.getPlanTime(), // 结束时间暂且使用开始时间，需要管理员手动设置
                        new Date());
                volunteerTaskMapper.insert(volunteerTask);
                Location location = source.copy(VOLUNTEER_TASK, volunteerTask.getId(), task.getWorkerId());
                locationMapper.insert(location);
                volunteerTask.setLocationId(location.getId());
                volunteerTaskMapper.updateById(volunteerTask);
            }

            if (volunteerShiftMapper.queryByVolunteerTask(task.getVolunteerId(), volunteerTask.getId()).exists()) {
                throw ServiceException.conflict("exception.conflict.volunteer.shift.exists");
            }
            Date start = task.getPlanTime();
            if (volunteerShiftMapper.queryConflictByVolunteer(task.getVolunteerId(), start).exists()) {
                throw ServiceException.conflict("exception.conflict.volunteer.shift.time");
            }
            Date now = new Date();
            VolunteerShift shift = new VolunteerShift(null, // 志愿者排班
                    task.getVolunteerId(),
                    task.getWorkerId(),
                    volunteerTask.getId(),
                    VolunteerShiftStatus.ASSIGNED,
                    "领养回访任务 #" + task.getId(),
                    start,
                    start,
                    now,
                    now);
            volunteerShiftMapper.insert(shift);
        }

        if (status == FollowTaskStatus.FINISH && oldStatus != FollowTaskStatus.FINISH) { // 完成志愿者任务
            VolunteerTask volunteerTask = volunteerTaskMapper.queryFollowTask(task.getId()).require();
            VolunteerShift shift = volunteerShiftMapper.queryByVolunteerTask(task.getVolunteerId(), volunteerTask.getId()).require();
            VolunteerShiftStatusRecord record = new VolunteerShiftStatusRecord(null,
                    shift.getId(),
                    shift.getStatus(),
                    VolunteerShiftStatus.COMPLETED,
                    "回访任务完成",
                    new Date());
            Date now = new Date();
            shift.setStatus(VolunteerShiftStatus.COMPLETED);
            shift.setEndTime(now);
            shift.setUpdateTime(now);
            volunteerShiftMapper.updateById(shift);
            volunteerShiftStatusRecordMapper.insert(record);
            eventPublisher.publishEvent(new VolunteerShiftStatusEvent(shift, volunteerTask, record, login));
        }

        request.applyTo(task);
        followTaskMapper.updateById(task);
        eventPublisher.publishEvent(new FollowTaskUpdateEvent(task, login));
        return adoptBreadingFacade.buildFollowTaskResponse(task);
    }

    /**
     * 获取回访任务
     */
    public FollowTaskResponse getFollowTask(Long taskId) {
        FollowTask task = followTaskMapper.requireById(taskId);
        return adoptBreadingFacade.buildFollowTaskResponse(task);
    }

    /**
     * 查询回访任务列表
     */
    public Page<FollowTaskResponse> getFollowTasks(FollowTaskQueryParams query, PageParams page) {
        User login = requireLoginUser();
        if (!login.isWorker()) {
            query.setRequireAdopter(login.getId());
            if (login.isVolunteer()) {
                query.setRequireVolunteer(login.getId());
            }
        } else { // 工作人员查询不需要限定领养人、志愿者
            query.setRequireAdopter(null);
            query.setRequireVolunteer(null);
        }

        Page<FollowTask> result = followTaskMapper.queryByRequest(query).page(page);
        return adoptBreadingFacade.buildFollowTaskPage(result);
    }

    /**
     * 创建回访记录
     */
    @Transactional
    public FollowRecordResponse addFollowRecord(Long taskId, FollowRecordAddRequest request) {
        // 权限校验
        User login = requireLoginUser();
        FollowTask task = followTaskMapper.requireById(taskId,
                FollowTask::getStatus, FollowTask::getAdopterId, FollowTask::getVolunteerId);
        requireEqual(FollowTaskStatus.IN_PROGRESS, task.getStatus(), "exception.invalidate.follow_task.status_invalid");
        requirePermission(login.isWorker() || login.is(task.getVolunteerId()));
        if (followRecordMapper.queryByTask(taskId).exists())
            throw ServiceException.conflict("exception.conflict.follow_record.exists");

        FollowRecord record = request.create(taskId, task.getAdopterId(), login.getId());
        followRecordMapper.insert(record);
        eventPublisher.publishEvent(new FollowRecordEvent(record, login));
        return adoptBreadingFacade.buildFollowRecordResponse(record);
    }

    /**
     * 获取回访记录
     */
    public Page<FollowRecordResponse> getFollowRecords(Long taskId, PageParams request) {
        Page<FollowRecord> records = followRecordMapper.queryByTask(taskId).page(request);
        return adoptBreadingFacade.buildFollowRecordPage(records);
    }

    /**
     * 查询回访记录
     */
    public Page<FollowRecordResponse> getFollowRecords(FollowRecordQueryParams query, PageParams page) {
        User login = requireLoginUser();
        if (!login.isWorker()) {
            query.setRequireAdopter(login.getId());
            if (login.isVolunteer()) {
                query.setRequireVolunteer(login.getId());
            }
        } else { // 工作人员查询不需要限定领养人、志愿者
            query.setRequireAdopter(null);
            query.setRequireVolunteer(null);
        }
        Page<FollowRecord> result = followRecordMapper.queryByRequest(query).page(page);
        return adoptBreadingFacade.buildFollowRecordPage(result);
    }

    @Autowired
    public void setServices(PetService petService, UserService userService, FileService fileService) {
        this.petService = petService;
        this.userService = userService;
        this.fileService = fileService;
    }
}
