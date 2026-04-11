package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.entity.property.*;
import com.example.backend.event.*;
import com.example.backend.facade.AdoptBreadingFacade;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.example.backend.entity.property.AdoptBreadingStatus.AGREEMENT_DRAFT;
import static com.example.backend.entity.property.AdoptBreadingStatus.AGREEMENT_SIGNED;
import static com.example.backend.entity.property.ParentType.AGREEMENT;
import static com.example.backend.entity.property.ParentType.AGREEMENT_RECORD;

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
    private final AdoptBreadingFacade adoptBreadingFacade;

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
        return adoptBreadingFacade.buildAdoptResponse(adopt);
    }

    /**
     * 获取领养申请
     */
    public AdoptResponse getAdopt(Long adoptId) {
        Adopt adopt = requireById(adoptId);
        return adoptBreadingFacade.buildAdoptResponse(adopt);
    }

    /**
     * 查询领养申请
     */
    public Page<AdoptResponse> getAdopts(AdoptQueryParams paramRequest, PageParams pageRequest) {
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
        eventPublisher.publishEvent(new BreadingAddEvent(breading));
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
        require(oldStatus.isChangeable(), "寄养状态异常");
        require(target != AdoptBreadingStatus.AGREEMENT_SIGNED, "寄养状态异常");

        Date now = new Date();
        breading.setStatus(target);
        breading.setUpdateTime(now);
        if (target == AdoptBreadingStatus.PASS || target == AdoptBreadingStatus.REJECT)
            breading.setReviewTime(now);
        breadingMapper.updateById(breading);
        eventPublisher.publishEvent(new BreadingStatusEvent(breading, oldStatus));
        return adoptBreadingFacade.buildBreadingResponse(breading, login);
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
        return adoptBreadingFacade.buildAgreementResponse(agreement);
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
        return adoptBreadingFacade.buildAgreementResponse(agreement);
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
        return adoptBreadingFacade.buildAgreementResponse(agreement);
    }

    /**
     * 获取协议
     */
    public AgreementResponse getAgreement(Long agreementId) {
        Agreement agreement = agreementMapper.requireById(agreementId);
        return adoptBreadingFacade.buildAgreementResponse(agreement);
    }

    /**
     * 查询协议列表
     */
    public Page<AgreementResponse> getAgreements(AgreementQueryParams queryRequest, PageParams pageRequest) {
        Page<Agreement> result = agreementMapper.queryByRequest(queryRequest).page(pageRequest);
        return adoptBreadingFacade.buildAgreementPage(result);
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
        return adoptBreadingFacade.buildFollowTaskResponse(task);
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
        return adoptBreadingFacade.buildFollowTaskResponse(task);
    }

    /**
     * 获取跟踪任务
     */
    public FollowTaskResponse getFollowTask(Long taskId) {
        FollowTask task = followTaskMapper.requireById(taskId);
        return adoptBreadingFacade.buildFollowTaskResponse(task);
    }

    /**
     * 查询跟踪任务列表
     */
    public Page<FollowTaskResponse> getFollowTasks(FollowTaskQueryParams query, PageParams page) {
        Page<FollowTask> result = followTaskMapper.queryByRequest(query).page(page);
        return adoptBreadingFacade.buildFollowTaskPage(result);
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
        return adoptBreadingFacade.buildFollowRecordPage(records);
    }

    /**
     * 查询跟踪记录
     */
    public Page<FollowRecordResponse> getFollowRecords(FollowRecordQueryParams queryRequest, PageParams pageRequest) {
        Page<FollowRecord> result = followRecordMapper.queryByRequest(queryRequest).page(pageRequest);
        return adoptBreadingFacade.buildFollowRecordPage(result);
    }

    @Autowired
    public void setServices(PetService petService, UserService userService, FileService fileService) {
        this.petService = petService;
        this.userService = userService;
        this.fileService = fileService;
    }
}
