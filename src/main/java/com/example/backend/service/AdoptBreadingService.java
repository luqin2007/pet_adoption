package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.entity.property.*;
import com.example.backend.mapper.*;
import com.example.backend.util.FileUtils;
import com.example.backend.util.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.backend.entity.property.AdoptBreadingStatus.AGREEMENT_DRAFT;
import static com.example.backend.entity.property.AdoptBreadingStatus.AGREEMENT_SIGNED;
import static com.example.backend.entity.property.ParentType.AGREEMENT;
import static com.example.backend.entity.property.ParentType.AGREEMENT_UPDATE;

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

    private final PetService petService;
    private final UserService userService;

    /**
     * 申请领养
     */
    @Transactional
    public AdoptResponse addAdopt(AdoptAddRequest request) {
        // 校验
        User login = getLoginUser();
        petService.requireExist(request.getPetId());

        // 保存
        Adopt adopt = request.create(login.getId());
        save(adopt);
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
        Page<Adopt> page = pageRequest.createPage();
        LambdaQueryWrapper<Adopt> query = getBaseMapper().queryByRequest(paramRequest);
        Page<Adopt> result = page(page, query);
        Set<Long> adoptIds = result.getRecords().stream()
                .map(Adopt::getId)
                .collect(Collectors.toSet());
        List<FollowTask> tasks = followTaskMapper.selectList(followTaskMapper.queryByAdopts(adoptIds));
        Set<Long> petIds = result.getRecords().stream()
                .map(Adopt::getPetId)
                .collect(Collectors.toSet());
        Map<Long, Pet> pets = petService.groupById(petIds,
                Pet::getId, Pet::getName);
        Map<Long, String> covers = petService.getCoversByPetIds(petIds);
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().flatMap(adopt -> Stream.of(adopt.getApplicantId(), adopt.getReviewerId())),
                tasks.stream().flatMap(task -> Stream.of(task.getWorkerId(), task.getVolunteerId())),
                User::getId, User::getUsername, User::getAvatar);
        Set<Long> taskIds = tasks.stream()
                .map(FollowTask::getId)
                .collect(Collectors.toSet());
        Map<Long, FollowRecord> records = followRecordMapper.groupFirst(
                followRecordMapper
                        .queryByTasks(taskIds)
                        .select(FollowRecord::getId, FollowRecord::getTaskId, FollowRecord::getSummary, FollowRecord::getVisitTime),
                FollowRecord::getTaskId,
                Function.identity());
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
        User login = getLoginUser();
        requirePermission(login.isWorker());
        Adopt adopt = requireById(adoptId);
        AdoptBreadingStatus target = AdoptBreadingStatus.get(status);
        require(adopt.getStatus().isChangeable(), "领养状态异常");
        // 签订状态仅能通过 signAgreement 方法实现
        require(target != AdoptBreadingStatus.AGREEMENT_SIGNED, "领养状态异常");

        // 更新记录
        Date now = new Date();
        adopt.setStatus(target);
        adopt.setUpdateTime(now);
        if (target == AdoptBreadingStatus.PASS || target == AdoptBreadingStatus.REJECT)
            adopt.setReviewTime(now);
        return buildAdoptResponse(adopt);
    }

    /**
     * 申请寄养
     */
    @Transactional
    public BreadingResponse addBreading(BreadingAddRequest request) {
        User login = getLoginUser();
        Breading breading = request.create(login.getId());
        breadingMapper.insert(breading);
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
        Page<Breading> page = pageRequest.createPage();
        LambdaQueryWrapper<Breading> query = breadingMapper.queryByRequest(queryRequest);
        Page<Breading> result = breadingMapper.selectPage(page, query);
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
        User login = getLoginUser();
        requirePermission(login.isWorker());
        Breading breading = breadingMapper.requireById(breadingId);
        AdoptBreadingStatus target = AdoptBreadingStatus.get(status);
        require(breading.getStatus().isChangeable(), "寄养状态异常");
        require(target != AdoptBreadingStatus.AGREEMENT_SIGNED, "寄养状态异常");

        Date now = new Date();
        breading.setStatus(target);
        breading.setUpdateTime(now);
        if (target == AdoptBreadingStatus.PASS || target == AdoptBreadingStatus.REJECT)
            breading.setReviewTime(now);
        breadingMapper.updateById(breading);
        return buildBreadingResponse(breading);
    }

    /**
     * 起草协议
     */
    @Transactional
    public AgreementResponse addAgreement(AgreementAddRequest request) {
        User login = getLoginUser();
        requirePermission(login.isWorker());

        // 保存协议
        Agreement agreement = request.create();
        agreementMapper.insert(agreement);
        AgreementUpdateRecord updateRecord = request.createUpdateRecord(agreement);
        agreementUpdateRecordMapper.insert(updateRecord);

        // 更新记录状态
        ParentType parentType = agreement.getParentType();
        switch (parentType) {
            case ADOPT -> update(baseMapper.updateStatus(agreement.getParentId(), AGREEMENT_DRAFT));
            case BREADING ->
                    breadingMapper.update(breadingMapper.updateStatus(agreement.getParentId(), AGREEMENT_DRAFT));
            default -> throw ServiceException.invalidate("无效协议 " + parentType);
        }
        return buildAgreementResponse(agreement);
    }

    /**
     * 修改协议
     */
    @Transactional
    public AgreementResponse updateAgreement(Long agreementId, AgreementUpdateRequest request) {
        User login = getLoginUser();
        requirePermission(login.isWorker());
        Agreement agreement = agreementMapper.requireById(agreementId);
        requireEqual(AgreementType.ELECTRONIC, agreement.getType(), "仅适用于电子协议");

        agreementMapper.updateById(agreement);
        AgreementUpdateRecord updateRecord = request.applyTo(agreement);
        agreementUpdateRecordMapper.insert(updateRecord);
        return buildAgreementResponse(agreement);
    }

    /**
     * 上传纸质扫描件
     */
    @Transactional
    public List<AgreementFileResponse> uploadAgreement(Long agreementId, AgreementFilesUploadTable request) {
        User login = getLoginUser();
        requirePermission(login.isWorker());
        Agreement agreement = agreementMapper.requireById(agreementId,
                Agreement::getType);
        requireEqual(AgreementType.PAPER, agreement.getType(), "仅适用于纸质协议");

        // 检查是否是图片
        List<Pair<String, MediaType>> extAndTypes = request.getFiles().stream()
                .map(FileUtils::getFileExtensionAndType)
                .toList();
        boolean onlyImage = extAndTypes.stream()
                .allMatch(extAndType -> extAndType.getSecond() == MediaType.IMAGE);
        require(onlyImage, "不支持的图片格式");

        // 已上传 - 创建更改记录
        List<AgreementFile> agreementFiles = agreementFileMapper.selectList(agreementFileMapper.queryByAgreement(agreementId));
        Path path = FileUtils.generateFilePath(AGREEMENT, agreementId);
        if (!ObjectUtils.isEmpty(agreementFiles)) {
            AgreementUpdateRecord updateRecord = request.createUpdateRecord(agreementId, agreementFiles, objectMapper);
            agreementUpdateRecordMapper.insert(updateRecord);
            // 文件已存在 - 移动
            Path newPath = FileUtils.generateFilePath(AGREEMENT_UPDATE, updateRecord.getId());
            FileUtils.moveDirectory(path, newPath);
            // 删除文件记录
            agreementFileMapper.deleteByIds(agreementFiles);
        }
        if (Files.isDirectory(path))
            throw ServiceException.system("文件转移异常，请联系管理员手动处理");

        // 上传文件
        List<AgreementFile> files = new ArrayList<>(request.getFiles().size());
        for (int i = 0; i < request.getFiles().size(); ++i) {
            Date now = new Date();
            Pair<String, MediaType> extAndType = extAndTypes.get(i);
            MultipartFile file = request.getFiles().get(i);
            String name = FileUtils.getNameWithoutExtension(file.getOriginalFilename());
            String filename = FileUtils.generateFilename(name, now, extAndType.getFirst());
            FileUtils.upload(file, filename, path);
            AgreementFile agreementFile = new AgreementFile(null,
                    agreementId,
                    filename,
                    i + 1,
                    now);
            files.add(agreementFile);
        }
        agreementFileMapper.insert(files);

        // 更新协议
        agreement.setUpdateTime(new Date());
        agreementMapper.update(agreementMapper.setUpdateTime(agreementId));
        return files.stream()
                .map(AgreementFileResponse::create)
                .toList();
    }

    /**
     * 签署协议
     */
    @Transactional
    public AgreementResponse signAgreement(Long agreementId, MultipartFile sign) {
        User login = getLoginUser();
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

        // 上传文件
        Date now = new Date();
        String original = sign.getOriginalFilename();
        String name = FileUtils.getNameWithoutExtension(original);
        Pair<String, MediaType> extAndType = FileUtils.getFileExtensionAndType(sign);
        requireEqual(MediaType.IMAGE, extAndType.getSecond(), "不支持的图片格式");
        String filename = FileUtils.generateFilename(name, now, extAndType.getFirst());
        Path path = FileUtils.generateFilePath(AGREEMENT, agreementId);
        FileUtils.upload(sign, filename, path);

        // 保存数据
        String content = switch (agreement.getType()) {
            case ELECTRONIC -> agreement.getContent();
            case PAPER -> {
                List<AgreementFile> files = agreementFileMapper.selectList(agreementFileMapper.queryByAgreement(agreementId));
                yield objectMapper.writeValueAsString(files);
            }
        };
        AgreementUpdateRecord updateRecord = new AgreementUpdateRecord(null, agreementId, content, AgreementUpdateType.SIGN, now);
        agreementUpdateRecordMapper.insert(updateRecord);
        agreement.setSign(filename);
        agreement.setSignTime(now);
        agreement.setUpdateTime(now);
        agreementMapper.updateById(agreement);
        AgreementFile file = new AgreementFile(null, agreementId, filename, 0, now);
        agreementFileMapper.insert(file);
        switch (agreement.getParentType()) {
            case ADOPT -> update(baseMapper.updateStatus(parentId, AGREEMENT_SIGNED));
            case BREADING -> breadingMapper.update(breadingMapper.updateStatus(parentId, AGREEMENT_SIGNED));
            default -> throw ServiceException.invalidate("无效类型 " + agreement.getParentType());
        }
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
        Page<Agreement> page = pageRequest.createPage();
        LambdaQueryWrapper<Agreement> query = agreementMapper.queryByRequest(queryRequest);
        Page<Agreement> result = agreementMapper.selectPage(page, query);
        Set<Long> agreementIds = result.getRecords().stream()
                .map(Agreement::getId)
                .collect(Collectors.toSet());
        Map<Long, List<AgreementFileResponse>> files = agreementFileMapper.groupList(
                agreementFileMapper.queryByAgreements(agreementIds),
                AgreementFile::getAgreementId,
                AgreementFileResponse::create);
        return convertDto(result, agreement -> AgreementResponse.createBatch(agreement, files));
    }

    /**
     * 创建跟踪任务
     */
    @Transactional
    public FollowTaskResponse addFollowTask(Long adoptId, FollowTaskAddRequest request) {
        // 权限校验
        User login = getLoginUser();
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
        return buildFollowTaskResponse(task);
    }

    /**
     * 更新跟踪任务
     */
    @Transactional
    public FollowTaskResponse updateFollowTask(Long taskId, FollowTaskUpdateRequest request) {
        User login = getLoginUser();
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
        Page<FollowTask> result = followTaskMapper.selectPage(page.createPage(), followTaskMapper.queryByRequest(query));
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().flatMap(task -> Stream.of(task.getWorkerId(), task.getVolunteerId())),
                User::getId, User::getUsername);
        Set<Long> followIds = result.getRecords().stream()
                .map(FollowTask::getId)
                .collect(Collectors.toSet());
        Map<Long, FollowRecord> records = followRecordMapper.group(
                followRecordMapper.queryByTasks(followIds)
                        .select(FollowRecord::getId, FollowRecord::getSummary, FollowRecord::getVisitTime));
        return convertDto(result, task -> FollowTaskResponse.createBatch(task, users, records));
    }

    /**
     * 创建跟踪记录
     */
    @Transactional
    public FollowRecordResponse addFollowRecord(Long taskId, FollowRecordAddRequest request) {
        // 权限校验
        User login = getLoginUser();
        FollowTask task = followTaskMapper.requireById(taskId,
                FollowTask::getStatus);
        requireEqual(FollowTaskStatus.IN_PROGRESS, task.getStatus(), "跟踪任务状态错误");
        boolean allowed = login.isWorker() || Objects.equals(task.getVolunteerId(), login.getId());
        requirePermission(allowed);
        require(!followRecordMapper.exists(followRecordMapper.queryByTask(taskId)), "跟踪记录已存在");

        FollowRecord record = request.create(taskId, login.getId());
        followRecordMapper.insert(record);
        User volunteer = userService.selectById(record.getVolunteerId(),
                User::getId, User::getUsername, User::getAvatar);
        return FollowRecordResponse.create(record, volunteer);
    }

    /**
     * 获取跟踪记录
     */
    public Page<FollowRecordResponse> getFollowRecords(Long taskId, PageParams request) {
        Page<FollowRecord> page = request.createPage();
        Page<FollowRecord> records = followRecordMapper.selectPage(page, followRecordMapper.queryByTask(taskId));
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
        Page<FollowRecord> page = pageRequest.createPage();
        LambdaQueryWrapper<FollowRecord> query = followRecordMapper.queryByRequest(queryRequest);
        Page<FollowRecord> result = followRecordMapper.selectPage(page, query);
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().map(FollowRecord::getVolunteerId),
                User::getId, User::getUsername, User::getAvatar);
        return convertDto(result, record -> FollowRecordResponse.createBatch(record, users));
    }

    private AdoptResponse buildAdoptResponse(Adopt adopt) {
        Pet pet = petService.selectById(adopt.getPetId(),
                Pet::getId, Pet::getName);
        String cover = petService.getCoverById(adopt.getPetId());
        List<FollowTask> tasks = followTaskMapper.selectList(followTaskMapper.queryByAdopt(adopt.getId()));
        Map<Long, User> users = userService.groupById(
                Stream.of(adopt.getApplicantId(), adopt.getReviewerId()),
                tasks.stream().flatMap(task -> Stream.of(task.getWorkerId(), task.getVolunteerId())),
                User::getId, User::getUsername, User::getAvatar);
        Set<Long> taskIds = tasks.stream()
                .map(FollowTask::getId)
                .collect(Collectors.toSet());
        Map<Long, FollowRecord> records = followRecordMapper.groupFirst(
                followRecordMapper
                        .queryByTasks(taskIds)
                        .select(FollowRecord::getId, FollowRecord::getTaskId, FollowRecord::getSummary, FollowRecord::getVisitTime),
                FollowRecord::getTaskId,
                Function.identity());
        return AdoptResponse.create(adopt,
                pet, cover,
                users.get(adopt.getApplicantId()),
                adopt.getReviewerId() == null ? null : users.get(adopt.getReviewerId()),
                tasks.stream().map(task -> FollowTaskResponse.createBatch(task, users, records)).toList());
    }

    private BreadingResponse buildBreadingResponse(Breading breading) {
        User login = getLoginUser();
        User applicant = login.is(breading.getApplicantId()) ? login : userService.requireById(breading.getApplicantId(),
                User::getId, User::getUsername, User::getAvatar);
        User reviewer = login.is(breading.getReviewerId()) ? login : userService.selectById(breading.getReviewerId(),
                User::getId, User::getUsername, User::getAvatar);
        return BreadingResponse.create(breading, applicant, reviewer);
    }

    private AgreementResponse buildAgreementResponse(Agreement agreement) {
        LambdaQueryWrapper<AgreementFile> query = agreementFileMapper.queryByAgreement(agreement.getId());
        List<AgreementFileResponse> files = agreementFileMapper.selectList(query).stream()
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
        FollowRecord record = followRecordMapper.selectOne(followRecordMapper.queryByTask(task.getId())
                .select(FollowRecord::getId, FollowRecord::getSummary, FollowRecord::getVisitTime));
        return FollowTaskResponse.create(task, worker, volunteer, record);
    }
}
