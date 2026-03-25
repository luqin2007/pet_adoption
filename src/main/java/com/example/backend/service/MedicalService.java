package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.mapper.*;
import com.example.backend.util.DbUtils;
import com.example.backend.util.FileUtils;
import com.example.backend.util.ServiceException;
import com.example.backend.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.backend.util.C.*;

/**
 * 诊疗、病历等
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class MedicalService extends BaseService<MedicalDetailMapper, MedicalDetail> {

    private final FirstRegistrationMapper firstRegistrationMapper;
    private final AllergyHistoryMapper allergyHistoryMapper;
    private final ImmunityHistoryMapper immunityHistoryMapper;
    private final MedicalRecordMapper medicalRecordMapper;
    private final ExaminationDiagnosisMapper examinationDiagnosisMapper;
    private final ExaminationMapper examinationMapper;
    private final ExaminationFileMapper examinationFileMapper;
    private final ExaminationDiagnosisEntryMapper examinationDiagnosisEntryMapper;
    private final TreatmentPlanMapper treatmentPlanMapper;
    private final OrderMapper orderMapper;
    private final ItemMapper itemMapper;
    private final VaccineMapper vaccineMapper;
    private final VaccineRecordMapper vaccineRecordMapper;
    private final DewormerMapper dewormerMapper;
    private final DewormRecordMapper dewormRecordMapper;

    private PetService petService;
    private UserService userService;

    private final long KEY_TIMEOUT_MINUTES = 10;

    /**
     * 初诊登记
     */
    @Transactional
    public FirstRegistrationResponse addFirstVisitRegistration(FirstRegistrationAddRequest request) {
        // 校验
        petService.requireExist(request.getPetId());
        User login = getLoginUser();
        requirePermission(login.isDoctor() || login.isVolunteer());

        // 添加
        FirstRegistration registration = request.createEntity(login.getId());
        firstRegistrationMapper.insert(registration); // 初诊登记
        List<ImmunityHistory> immunityHistories = request.createImmunityHistories(registration.getId());
        immunityHistoryMapper.insert(immunityHistories); // 免疫史
        List<AllergyHistory> allergyHistories = request.createAllergyHistories(registration.getId());
        allergyHistoryMapper.insert(allergyHistories); // 过敏史

        // 返回
        Pet pet = petService.selectById(registration.getPetId(), Pet::getId, Pet::getSex, Pet::getType, Pet::getBreed);
        String cover = petService.getCoverById(registration.getPetId());
        return FirstRegistrationResponse.create(registration, immunityHistories, allergyHistories, List.of(), pet, cover, login);
    }

    /**
     * 获取初诊登记
     */
    public Page<FirstRegistrationItemResponse> getFirstVisitRegistrations(FirstRegistrationQueryRequest queryRequest,
                                                                          PageRequest pageRequest) {
        Page<FirstRegistration> page = pageRequest.createPage();
        Page<FirstRegistration> response = firstRegistrationMapper.selectPage(page, firstRegistrationMapper.selectByRequest(queryRequest));

        // pets
        Set<Long> petIds = response.getRecords().stream()
                .map(FirstRegistration::getPetId)
                .collect(Collectors.toSet());
        Map<Long, Pet> pets = petService.groupById(petIds,
                Pet::getId, Pet::getSex, Pet::getType, Pet::getBreed);

        // users
        Set<Long> userIds = response.getRecords().stream()
                .map(FirstRegistration::getRegistrarId)
                .collect(Collectors.toSet());
        Map<Long, User> users = userService.groupById(userIds,
                User::getId, User::getUsername, User::getAvatar);
        return DbUtils.convertDto(response, registration ->
                FirstRegistrationItemResponse.createBatch(registration, pets, users));
    }

    /**
     * 获取初诊登记详细信息
     */
    public FirstRegistrationResponse getFirstVisitRegistration(Long registrationId) {
        FirstRegistration registration = firstRegistrationMapper.requireById(registrationId);
        List<ImmunityHistory> immunityHistories = immunityHistoryMapper.selectList(immunityHistoryMapper.queryByRegistration(registrationId));
        List<AllergyHistory> allergyHistories = allergyHistoryMapper.selectList(allergyHistoryMapper.queryByRegistration(registrationId));
        Pet pet = petService.selectById(registration.getPetId(),
                Pet::getId, Pet::getSex, Pet::getType, Pet::getBreed);
        String cover = petService.getCoverById(registration.getPetId());
        User user = userService.selectById(registration.getRegistrarId(),
                User::getId, User::getUsername, User::getAvatar);

        // 病历
        Set<Long> detailIds = medicalRecordMapper.selectList(medicalRecordMapper.selectByPet(registration.getPetId()), MedicalRecord::getId)
                .stream().map(MedicalRecord::getId).collect(Collectors.toSet());
        List<MedicalDetail> details = listById(detailIds,
                MedicalDetail::getId,
                MedicalDetail::getRecordId,
                MedicalDetail::getDoctorId,
                MedicalDetail::getCreateTime,
                MedicalDetail::getIsCompleted,
                MedicalDetail::getSummary);
        Set<Long> userIds = details.stream().map(MedicalDetail::getDoctorId).collect(Collectors.toSet());
        Map<Long, User> users = userService.groupById(userIds,
                User::getId, User::getUsername, User::getAvatar);
        List<MedicalDetailItemResponse> detailResponses = details
                .stream()
                .map(detail -> MedicalDetailItemResponse.createBatch(detail, users))
                .toList();
        return FirstRegistrationResponse.create(registration, immunityHistories, allergyHistories, detailResponses, pet, cover, user);
    }

    /**
     * 创建就诊记录
     */
    @Transactional
    public MedicalRecordResponse addMedicalVisitRecord(MedicalRecordRequest request) {
        // 权限/环境校验
        User login = getLoginUser();
        requirePermission(login.isDoctor());
        firstRegistrationMapper.requireExist(firstRegistrationMapper.selectByPet(request.getPetId()));

        // 存储数据
        MedicalRecord record = request.createEntity();
        medicalRecordMapper.insert(record);
        return buildMedicalRecordResponse(record);
    }

    /**
     * 更新就诊记录
     */
    @Transactional
    public MedicalRecordResponse updateMedicalRecord(Long recordId, MedicalRecordUpdateRequest request) {
        // 权限/环境校验
        User login = getLoginUser();
        requirePermission(login.isWorker() || login.isDoctor());
        MedicalRecord record = medicalRecordMapper.requireById(recordId);

        // 保存新数据
        request.applyTo(record);
        medicalRecordMapper.updateById(record);
        return buildMedicalRecordResponse(record);
    }

    /**
     * 获取就诊记录
     */
    public MedicalRecordResponse getMedicalRecord(Long recordId) {
        MedicalRecord record = medicalRecordMapper.requireById(recordId);
        return buildMedicalRecordResponse(record);
    }

    private MedicalRecordResponse buildMedicalRecordResponse(MedicalRecord record) {
        Pet pet = petService.selectById(record.getPetId(),
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed);
        String cover = petService.getCoverById(record.getPetId());
        User user = userService.selectById(record.getDoctorId(),
                User::getId, User::getUsername, User::getAvatar);
        User owner = record.getOwnerId() == null ? null
                : userService.selectById(record.getOwnerId(), User::getId, User::getUsername, User::getAvatar);
        return MedicalRecordResponse.create(record, pet, cover, user, owner);
    }

    /**
     * 获取就诊记录列表
     */
    public Page<MedicalRecordResponse> getMedicalRecords(MedicalRecordQueryRequest queryRequest, PageRequest pageRequest) {
        LambdaQueryWrapper<MedicalRecord> query = medicalRecordMapper.queryByRequest(queryRequest);
        Page<MedicalRecord> page = pageRequest.createPage();
        Page<MedicalRecord> result = medicalRecordMapper.selectPage(page, query);

        Set<Long> petIds = result.getRecords().stream()
                .map(MedicalRecord::getPetId)
                .collect(Collectors.toSet());
        Set<Long> userIds = result.getRecords().stream()
                .flatMap(record -> Stream.of(record.getDoctorId(), record.getOwnerId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Pet> pets = petService.groupById(petIds,
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed);
        Map<Long, String> covers = petService.getCoversByPetIds(petIds);
        Map<Long, User> users = userService.groupById(userIds);
        return DbUtils.convertDto(result,
                record -> MedicalRecordResponse.createBatch(record, pets, covers, users));
    }

    /**
     * 创建病历
     */
    @Transactional
    public MedicalDetailResponse addMedicalDetail(MedicalDetailAddRequest request) {
        // 权限校验
        User login = getLoginUser();
        requirePermission(login.isDoctor());
        MedicalRecord record = medicalRecordMapper.selectById(request.getRecordId(), MedicalRecord::getPetAge);

        // 创建病历
        MedicalDetail detail = request.createEntity(login.getId());
        save(detail);

        // 返回
        Pet pet = petService.selectById(record.getPetId(),
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed);
        String cover = petService.getCoverById(record.getPetId());
        List<PetTagResponse> tags = petService.getTags(pet.getId());
        return MedicalDetailResponse.create(detail, record, login, List.of(), List.of(), pet, cover, tags);
    }

    /**
     * 修改病历
     */
    @Transactional
    public MedicalDetailResponse updateMedicalDetail(Long detailId, MedicalDetailUpdateRequest request) {
        // 权限校验
        User login = getLoginUser();
        requirePermission(login.isDoctor());
        MedicalDetail detail = requireDetailOpen(detailId);

        // 更新
        request.applyTo(detail);
        save(detail);
        return buildMedicalDetailResponse(detail);
    }

    /**
     * 完成病历
     */
    @Transactional
    public MedicalDetailResponse completeMedicalDetail(Long detailId) {
        // 权限校验
        User login = getLoginUser();
        requirePermission(login.isDoctor());
        MedicalDetail detail = requireDetailOpen(detailId);

        detail.setIsCompleted(true);
        save(detail);
        return buildMedicalDetailResponse(detail);
    }

    private MedicalDetailResponse buildMedicalDetailResponse(MedicalDetail detail) {
        Long detailId = detail.getId();
        MedicalRecord record = medicalRecordMapper.requireById(detail.getRecordId(), MedicalRecord::getPetAge);
        User doctor = userService.selectById(detail.getDoctorId(), User::getId, User::getUsername, User::getAvatar);
        Map<Long, Examination> examinationMap = examinationMapper.group(examinationMapper.queryByDetail(detailId));
        Map<Long, List<ExaminationFileResponse>> files = examinationFileMapper.groupList(
                examinationFileMapper.queryByExaminations(examinationMap.keySet()),
                ExaminationFile::getExaminationId,
                ExaminationFileResponse::create);
        Map<Long, List<ExaminationResponse>> examinations = examinationDiagnosisEntryMapper.groupList(
                examinationDiagnosisEntryMapper.queryByExaminations(examinationMap.keySet()),
                ExaminationDiagnosisEntry::getDiagnosisId,
                examination -> ExaminationResponse.createBatch(examination.getExaminationId(), examinationMap, files));

        // 构造返回值
        List<ExaminationDiagnosisResponse> examinationDiagnoses = examinationDiagnosisMapper
                .selectList(examinationDiagnosisMapper.queryByDetail(detailId)).stream()
                .map(diagnosis -> ExaminationDiagnosisResponse.createBatch(diagnosis, examinations))
                .toList();
        List<TreatmentPlan> plans = treatmentPlanMapper.selectList(treatmentPlanMapper.selectByDetail(detailId));
        Set<Long> planIds = plans.stream().map(TreatmentPlan::getId).collect(Collectors.toSet());
        List<Order> orderList = orderMapper.selectList(orderMapper.queryByTreatmentPlans(planIds));
        Set<Long> userIds = Stream.concat(
                plans.stream().map(TreatmentPlan::getDoctorId),
                orderList.stream().map(Order::getAllowerId)
        ).collect(Collectors.toSet());
        Map<Long, User> users = userService.groupById(userIds, User::getId, User::getUsername, User::getAvatar);
        Set<Long> itemIds = orderList.stream().map(Order::getItemId).collect(Collectors.toSet());
        Map<Long, Item> items = itemMapper.groupById(itemIds);
        Map<Long, List<OrderResponse>> orders = orderList.stream()
                .map(order -> OrderResponse.createBatch(order, users, items))
                .collect(Collectors.groupingBy(OrderResponse::getPlanId));
        List<TreatmentPlanResponse> treatments = plans.stream()
                .map(plan -> TreatmentPlanResponse.createBatch(plan, users, orders))
                .toList();
        Pet pet = petService.selectById(record.getPetId(),
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed);
        String cover = petService.getCoverById(record.getPetId());
        List<PetTagResponse> tags = petService.getTags(pet.getId());
        return MedicalDetailResponse.create(detail, record, doctor, examinationDiagnoses, treatments, pet, cover, tags);
    }

    /**
     * 添加检查结果
     */
    @Transactional
    public ExaminationDiagnosisResponse addDiagnosis(Long detailId, ExaminationDiagnosisAddRequest request) {
        // 检查权限
        User login = getLoginUser();
        requirePermission(login.isDoctor());
        requireDetailOpen(detailId, MedicalDetail::getIsCompleted, MedicalDetail::getIsDiscard);

        ExaminationDiagnosis diagnosis = request.create(detailId);
        examinationDiagnosisMapper.insert(diagnosis);
        List<ExaminationDiagnosisEntry> entries = request.createEntries(diagnosis.getId());
        examinationDiagnosisEntryMapper.insert(entries);

        Set<Long> examinationIds = entries.stream()
                .map(ExaminationDiagnosisEntry::getExaminationId)
                .collect(Collectors.toSet());
        Map<Long, List<ExaminationFileResponse>> files = examinationFileMapper.groupList(
                examinationFileMapper.queryByExaminations(examinationIds),
                ExaminationFile::getExaminationId,
                ExaminationFileResponse::create);
        List<ExaminationResponse> examinations = examinationMapper.selectByIds(examinationIds).stream()
                .map(examination -> ExaminationResponse.createBatch(examination, files))
                .toList();
        return ExaminationDiagnosisResponse.create(diagnosis, examinations);
    }

    /**
     * 废弃检查结果
     */
    @Transactional
    public void discardDiagnosis(Long diagnosisId) {
        User login = getLoginUser();
        requirePermission(login.isDoctor());
        Long detailId = examinationDiagnosisMapper
                .requireById(diagnosisId, ExaminationDiagnosis::getDetailId)
                .getDetailId();
        requireDetailOpen(detailId, MedicalDetail::getIsCompleted, MedicalDetail::getIsDiscard);
        examinationDiagnosisMapper.update(examinationDiagnosisMapper.discardById(diagnosisId));
    }

    /**
     * 创建治疗计划
     */
    @Transactional
    public TreatmentPlanResponse addTreatmentPlan(Long detailId, TreatmentPlanAddRequest request) {
        // 权限校验
        User login = getLoginUser();
        requirePermission(login.isDoctor());
        requireDetailOpen(detailId, MedicalDetail::getIsCompleted, MedicalDetail::getIsDiscard);

        // 保存
        TreatmentPlan plan = request.create(login.getId(), detailId);
        treatmentPlanMapper.insert(plan);
        List<Order> orders = request.createOrders(login.getId(), plan.getId());
        orderMapper.insert(orders);

        Set<Long> userIds = orders.stream().map(Order::getAllowerId).collect(Collectors.toSet());
        Map<Long, User> users = userService.groupById(userIds, User::getId, User::getUsername, User::getAvatar);
        Set<Long> itemIds = orders.stream().map(Order::getItemId).collect(Collectors.toSet());
        Map<Long, Item> items = itemMapper.groupById(itemIds, Item::getId, Item::getName);
        return TreatmentPlanResponse.create(plan, login, orders.stream()
                .map(order -> OrderResponse.createBatch(order, users, items))
                .toList());
    }

    /**
     * 废弃治疗计划
     */
    @Transactional
    public void discardTreatmentPlan(IdsRequest request) {
        // 权限校验
        User login = getLoginUser();
        requirePermission(login.isDoctor());
        HashSet<Long> planIds = new HashSet<>(request.getIds());
        List<TreatmentPlan> plans = treatmentPlanMapper.selectList(planIds,
                TreatmentPlan::getDetailId, TreatmentPlan::getIsDiscard);
        boolean hasDiscardPlan = plans.stream()
                .anyMatch(plan -> !Boolean.TRUE.equals(plan.getIsDiscard()));
        if (hasDiscardPlan)
            throw ServiceException.invalidate("治疗计划已废弃");
        Set<Long> detailIds = plans.stream().map(TreatmentPlan::getDetailId).collect(Collectors.toSet());
        boolean hasReadonlyDetail = listById(detailIds, MedicalDetail::getIsCompleted, MedicalDetail::getIsDiscard)
                .stream()
                .anyMatch(detail -> Boolean.TRUE.equals(detail.getIsCompleted()) || Boolean.TRUE.equals(detail.getIsDiscard()));
        if (hasReadonlyDetail)
            throw ServiceException.invalidate("病历已完成或已废弃");

        treatmentPlanMapper.update(treatmentPlanMapper.discardByIds(planIds));
    }

    /**
     * 准备上传检查结果
     */
    public String beginExamination(Long detailId) {
        // 权限校验
        User login = getLoginUser();
        requirePermission(login.isDoctor());
        requireDetailOpen(detailId, MedicalDetail::getIsCompleted, MedicalDetail::getIsDiscard);

        // 生成随机 uuid
        // 30 分钟有效期
        String uuid = StringUtils.randomUUID(KEY_EXAMINATION, redisHelper, 10);
        String redisKey = String.format(KEY_EXAMINATION, uuid);
        redisHelper.putString(redisKey, String.valueOf(detailId), KEY_TIMEOUT_MINUTES);
        return uuid;
    }

    /**
     * 上传文件
     */
    public String uploadExamination(String examId, ExaminationFileUploadRequest request) {
        // 权限校验
        User login = getLoginUser();
        requirePermission(login.isDoctor());
        Long detailId = Long.valueOf(redisHelper.getString(String.format(KEY_EXAMINATION, examId)));
        requireDetailOpen(detailId, MedicalDetail::getIsCompleted, MedicalDetail::getIsDiscard);

        // 接收文件
        Date now = new Date();
        Pair<String, String> nameAndExt = FileUtils.getNameAndExtension(request.getFile().getOriginalFilename());
        String filename = FileUtils.generateFilename(request.getFile().getOriginalFilename(), now, nameAndExt.getSecond());
        Path path = FileUtils.generateTempPath(PARENT_EXAMINATION, examId);
        FileUtils.upload(request.getFile(), filename, path);

        // 生成临时文件信息
        TempFileInfo fileInfo = new TempFileInfo(
                filename,
                request.getName(),
                login.getId(),
                0,
                now);
        String fileKey = String.format(KEY_EXAMINATION_FILE, examId);
        redisHelper.putObjectToHash(fileKey, filename, fileInfo);

        // 刷新 Redis 键
        String redisKey = String.format(KEY_EXAMINATION, examId);
        redisHelper.expireString(redisKey, KEY_TIMEOUT_MINUTES);
        redisHelper.expireObject(fileKey, KEY_TIMEOUT_MINUTES);
        return filename;
    }

    /**
     * 删除文件（上传时）
     */
    public void deleteExamination(String examId, String filename) {
        // 权限校验
        User login = getLoginUser();
        requirePermission(login.isDoctor());
        Long detailId = Long.valueOf(redisHelper.getString(String.format(KEY_EXAMINATION, examId)));
        requireDetailOpen(detailId, MedicalDetail::getIsCompleted, MedicalDetail::getIsDiscard);

        // 删除文件及记录
        String fileKey = String.format(KEY_EXAMINATION_FILE, examId);
        List<TempFileInfo> files = redisHelper.getAndDeleteObjectsFromHash(fileKey, filename);
        Path path = FileUtils.generateTempPath(PARENT_EXAMINATION, examId);
        for (TempFileInfo file : files)
            FileUtils.tryDeleteFile(path.resolve(file.getFilename()));
        FileUtils.tryDeleteDirectory(path, true);

        // 刷新 Redis 键
        String redisKey = String.format(KEY_EXAMINATION, examId);
        redisHelper.expireString(redisKey, KEY_TIMEOUT_MINUTES);
        redisHelper.expireObject(fileKey, KEY_TIMEOUT_MINUTES);
    }

    /**
     * 添加检查
     */
    public ExaminationResponse addExamination(String examId, ExaminationAddRequest request) {
        // 权限校验
        User login = getLoginUser();
        requirePermission(login.isDoctor());
        Long detailId = Long.valueOf(redisHelper.getString(String.format(KEY_EXAMINATION, examId)));
        requireDetailOpen(detailId, MedicalDetail::getIsCompleted, MedicalDetail::getIsDiscard);

        // 创建检查结果
        Examination examination = request.create(login.getId(), detailId);
        examinationMapper.insert(examination);

        // 创建检查结果文件
        String fileKey = String.format(KEY_EXAMINATION_FILE, examId);
        List<ExaminationFile> files = redisHelper.getObjectsFromHash(fileKey, TempFileInfo.class)
                .map(info -> info.createExamFile(examination.getId()))
                .filter(file -> FileUtils.transferTempFile(file, examId, examination.getId(), PARENT_EXAMINATION))
                .toList();
        examinationFileMapper.insert(files);
        return ExaminationResponse.create(examination, files.stream()
                .map(ExaminationFileResponse::create)
                .toList());
    }

    /**
     * 获取检查结果
     */
    public ExaminationResponse getExamination(Long examId) {
        Examination examination = examinationMapper.requireById(examId);
        List<ExaminationFileResponse> files = examinationFileMapper
                .selectList(examinationFileMapper.queryByExamination(examId)).stream()
                .map(ExaminationFileResponse::create)
                .toList();
        return ExaminationResponse.create(examination, files);
    }

    /*
     * 校验病历可编辑（未完成，未废弃）
     * 至少需要 isCompleted 和 isDiscard 两个字段
     */
    private MedicalDetail requireDetailOpen(Long detailId, SFunction<MedicalDetail, ?>... columns) {
        MedicalDetail detail = columns.length == 0 ? requireById(detailId) : requireById(detailId, columns);
        requireEqual(Boolean.FALSE, detail.getIsCompleted(), "病历已完成");
        requireEqual(Boolean.FALSE, detail.getIsDiscard(), "病历已删除");
        return detail;
    }

    /**
     * 添加疫苗记录
     */
    public VaccineResponse addVaccine(Long petId, VaccineAddRequest request) {
        // 权限校验
        User login = getLoginUser();
        requirePermission(login.isDoctor());
        Pet pet = petService.requireById(petId,
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed, Pet::getAge);
        Vaccine vaccine = vaccineMapper.requireById(request.getVaccineId());

        // 记录疫苗
        VaccineRecord record = request.create(petId, login.getId());
        vaccineRecordMapper.insert(record);

        // 返回值
        Item item = itemMapper.requireById(vaccine.getItemId(), Item::getId, Item::getName);
        String cover = petService.getCoverById(petId);
        return VaccineResponse.create(record, vaccine, item, pet, cover, login);
    }

    /**
     * 获取疫苗接种记录
     */
    public List<VaccineResponse> getVaccines(Long petId) {
        Pet pet = petService.requireById(petId,
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed, Pet::getAge);
        String cover = petService.getCoverById(petId);
        List<VaccineRecord> vaccines = vaccineRecordMapper.selectList(vaccineRecordMapper.queryByPet(petId));
        Set<Long> vaccineIds = vaccines.stream().map(VaccineRecord::getVaccineId).collect(Collectors.toSet());
        Map<Long, Vaccine> vaccineMap = vaccineMapper.groupById(vaccineIds);
        Set<Long> itemIds = vaccineMap.values().stream().map(Vaccine::getItemId).collect(Collectors.toSet());
        Map<Long, Item> itemMap = itemMapper.groupById(itemIds, Item::getId, Item::getName);
        Set<Long> doctorIds = vaccines.stream().map(VaccineRecord::getDoctorId).collect(Collectors.toSet());
        Map<Long, User> doctorMap = userService.groupById(doctorIds, User::getId, User::getUsername, User::getAvatar);
        return vaccines.stream()
                .map(record -> VaccineResponse.createBatch(record, pet, cover, vaccineMap, itemMap, doctorMap))
                .toList();
    }

    /**
     * 获取每种疫苗的最新接种记录
     */
    public List<VaccineResponse> getLatestVaccines(Long petId) {
        Pet pet = petService.requireById(petId,
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed, Pet::getAge);
        String cover = petService.getCoverById(petId);
        Map<Long, Optional<VaccineRecord>> vaccines = vaccineRecordMapper
                // 获取疫苗记录（倒序）
                .selectList(vaccineRecordMapper.queryByPet(petId)).stream()
                // 取分组第一项
                .collect(Collectors.groupingBy(VaccineRecord::getVaccineId, LinkedHashMap::new, Collectors.reducing((a, b) -> a)));
        Map<Long, Vaccine> vaccineMap = vaccineMapper.groupById(vaccines.keySet());
        Set<Long> itemIds = vaccineMap.values().stream().map(Vaccine::getItemId).collect(Collectors.toSet());
        Map<Long, Item> itemMap = itemMapper.groupById(itemIds, Item::getId, Item::getName);
        Set<Long> doctorIds = vaccines.values().stream()
                .map(Optional::orElseThrow)
                .map(VaccineRecord::getDoctorId)
                .collect(Collectors.toSet());
        Map<Long, User> doctorMap = userService.groupById(doctorIds, User::getId, User::getUsername, User::getAvatar);
        return vaccines.values().stream()
                .map(Optional::orElseThrow)
                .map(record -> VaccineResponse.createBatch(record, pet, cover, vaccineMap, itemMap, doctorMap))
                .sorted()
                .toList();
    }

    /**
     * 添加驱虫记录
     */
    @Transactional
    public DewormResponse addDeworm(Long petId, DewormAddRequest request) {
        // 权限校验
        User login = getLoginUser();
        requirePermission(login.isDoctor() || login.isWorker());
        Dewormer dewormer = dewormerMapper.requireById(request.getDewormerId());
        Item dewormerItem = itemMapper.requireById(dewormer.getItemId(),
                Item::getId, Item::getName);
        Pet pet = petService.requireById(petId,
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed, Pet::getAge);

        DewormRecord record = request.create(petId, login.getId());
        dewormRecordMapper.insert(record);

        String cover = petService.getCoverById(petId);
        return DewormResponse.create(record, dewormer, dewormerItem, pet, cover, login);
    }

    /**
     * 获取驱虫记录
     */
    public List<DewormResponse> getDeworms(Long petId) {
        getLoginUser();
        Pet pet = petService.requireById(petId,
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed, Pet::getAge);

        String cover = petService.getCoverById(petId);
        List<DewormRecord> deworms = dewormRecordMapper.selectList(dewormRecordMapper.queryByPet(petId));
        Set<Long> dewormerIds = deworms.stream().map(DewormRecord::getDewormerId).collect(Collectors.toSet());
        Map<Long, Dewormer> dewormerMap = dewormerMapper.groupById(dewormerIds);
        Set<Long> itemIds = dewormerMap.values().stream().map(Dewormer::getItemId).collect(Collectors.toSet());
        Map<Long, Item> itemMap = itemMapper.groupById(itemIds,
                Item::getId, Item::getName);
        Set<Long> doctorIds = deworms.stream().map(DewormRecord::getDoctorId).collect(Collectors.toSet());
        Map<Long, User> doctorMap = userService.groupById(doctorIds,
                User::getId, User::getUsername, User::getAvatar);
        return deworms.stream()
                .map(record -> DewormResponse.createBatch(record, pet, cover, dewormerMap, itemMap, doctorMap))
                .toList();
    }

    /**
     * 获取疫苗接种记录
     */
    public Map<Long, List<VaccineResponse>> getVaccinesByPetIds(Set<Long> petIds) {
        Map<Long, Pet> pets = petService.groupById(petIds,
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed, Pet::getAge);
        Map<Long, String> covers = petService.getCoversByPetIds(petIds);
        List<VaccineRecord> vaccines = vaccineRecordMapper.selectList(vaccineRecordMapper.queryByPets(petIds));
        Set<Long> vaccineIds = vaccines.stream().map(VaccineRecord::getVaccineId).collect(Collectors.toSet());
        Map<Long, Vaccine> vaccineMap = vaccineMapper.groupById(vaccineIds);
        Set<Long> itemIds = vaccineMap.values().stream().map(Vaccine::getItemId).collect(Collectors.toSet());
        Map<Long, Item> itemMap = itemMapper.groupById(itemIds, Item::getId, Item::getName);
        Set<Long> doctorIds = vaccines.stream().map(VaccineRecord::getDoctorId).collect(Collectors.toSet());
        Map<Long, User> doctorMap = userService.groupById(doctorIds, User::getId, User::getUsername, User::getAvatar);
        return vaccines.stream()
                .map(record -> VaccineResponse.createBatch(record, pets, covers, vaccineMap, itemMap, doctorMap))
                .collect(Collectors.groupingBy(VaccineResponse::getPetId));
    }

    /**
     * 获取驱虫记录
     */
    public Map<Long, List<DewormResponse>> getDewormsByPetIds(Set<Long> petIds) {
        getLoginUser();
        Map<Long, Pet> pets = petService.groupById(petIds,
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed, Pet::getAge);
        Map<Long, String> covers = petService.getCoversByPetIds(petIds);
        List<DewormRecord> deworms = dewormRecordMapper.selectList(dewormRecordMapper.queryByPets(petIds));
        Set<Long> dewormerIds = deworms.stream().map(DewormRecord::getDewormerId).collect(Collectors.toSet());
        Map<Long, Dewormer> dewormerMap = dewormerMapper.groupById(dewormerIds);
        Set<Long> itemIds = dewormerMap.values().stream().map(Dewormer::getItemId).collect(Collectors.toSet());
        Map<Long, Item> itemMap = itemMapper.groupById(itemIds,
                Item::getId, Item::getName);
        Set<Long> doctorIds = deworms.stream().map(DewormRecord::getDoctorId).collect(Collectors.toSet());
        Map<Long, User> doctorMap = userService.groupById(doctorIds,
                User::getId, User::getUsername, User::getAvatar);
        return deworms.stream()
                .map(record -> DewormResponse.createBatch(record, pets, covers, dewormerMap, itemMap, doctorMap))
                .collect(Collectors.groupingBy(DewormResponse::getPetId));
    }

    @Autowired
    public void setServices(PetService petService, UserService userService) {
        this.petService = petService;
        this.userService = userService;
    }
}
