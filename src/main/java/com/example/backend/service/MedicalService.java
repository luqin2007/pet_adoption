package com.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.mapper.*;
import com.example.backend.util.ServiceException;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.backend.entity.property.ParentType.*;

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
    private final DiagnosisMapper diagnosisMapper;
    private final ExaminationMapper examinationMapper;
    private final ExaminationFileMapper examinationFileMapper;
    private final ExaminationDiagnosisMapper examinationDiagnosisMapper;
    private final TreatmentPlanMapper treatmentPlanMapper;
    private final OrderMapper orderMapper;
    private final ItemMapper itemMapper;
    private final VaccineMapper vaccineMapper;
    private final VaccineRecordMapper vaccineRecordMapper;
    private final DewormerMapper dewormerMapper;
    private final DewormRecordMapper dewormRecordMapper;
    private final RehabPlanMapper rehabPlanMapper;
    private final RehabPlanStatusMapper rehabPlanStatusMapper;
    private final RehabRecordMapper rehabRecordMapper;
    private final HealthAssessmentMapper healthAssessmentMapper;

    private PetService petService;
    private UserService userService;
    private FileService fileService;

    private final long KEY_TIMEOUT_MINUTES = 10;

    @Value("${key.examination.uuid}")
    private String uuidKeyTemplate;
    @Value("${key.examination.file}")
    private String fileKeyTemplate;

    /**
     * 初诊登记
     */
    @Transactional
    public FirstRegistrationResponse addFirstVisitRegistration(FirstRegistrationAddRequest request) {
        // 校验
        petService.requireExist(request.getPetId());
        User login = requireLoginUser();
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
        String cover = fileService.getCoverUrl(registration.getPetId(), PET);
        return FirstRegistrationResponse.create(registration, immunityHistories, allergyHistories, List.of(), pet, cover, login);
    }

    /**
     * 获取初诊登记
     */
    public Page<FirstRegistrationItemResponse> getFirstVisitRegistrations(FirstRegistrationQueryParams params, PageParams request) {
        Page<FirstRegistration> response = firstRegistrationMapper.selectByRequest(params).page(request);
        Map<Long, Pet> pets = petService.groupById(
                response.getRecords().stream().map(FirstRegistration::getPetId),
                Pet::getId, Pet::getSex, Pet::getType, Pet::getBreed);
        Map<Long, User> users = userService.groupById(
                response.getRecords().stream().map(FirstRegistration::getRegistrarId),
                User::getId, User::getUsername, User::getAvatar);
        return convertDto(response,
                registration -> FirstRegistrationItemResponse.createBatch(registration, pets, users));
    }

    /**
     * 获取初诊登记详细信息
     */
    public FirstRegistrationResponse getFirstVisitRegistration(Long registrationId) {
        FirstRegistration registration = firstRegistrationMapper.requireById(registrationId);
        List<ImmunityHistory> immunityHistories = immunityHistoryMapper.queryByRegistration(registrationId).list();
        List<AllergyHistory> allergyHistories = allergyHistoryMapper.queryByRegistration(registrationId).list();
        Pet pet = petService.selectById(registration.getPetId(),
                Pet::getId, Pet::getSex, Pet::getType, Pet::getBreed);
        String cover = fileService.getCoverUrl(registration.getPetId(), PET);
        User user = userService.selectById(registration.getRegistrarId(),
                User::getId, User::getUsername, User::getAvatar);

        // 病历
        Set<Long> detailIds = medicalRecordMapper.selectByPet(registration.getPetId())
                .list(MedicalRecord::getId)
                .collect(Collectors.toSet());
        List<MedicalDetail> details = listById(detailIds,
                MedicalDetail::getId,
                MedicalDetail::getRecordId,
                MedicalDetail::getDoctorId,
                MedicalDetail::getCreateTime,
                MedicalDetail::getIsCompleted,
                MedicalDetail::getSummary);
        Map<Long, User> users = userService.groupById(
                details.stream().map(MedicalDetail::getDoctorId),
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
    public MedicalRecordResponse addMedicalRecord(Long petId, MedicalRecordAddRequest request) {
        // 权限/环境校验
        User login = requireLoginUser();
        requirePermission(login.isDoctor());
        firstRegistrationMapper.selectByPet(petId).requireExist();

        // 存储数据
        MedicalRecord record = request.createEntity(petId, login.getId());
        medicalRecordMapper.insert(record);
        return buildMedicalRecordResponse(record);
    }

    /**
     * 更新就诊记录
     */
    @Transactional
    public MedicalRecordResponse updateMedicalRecord(Long recordId, MedicalRecordUpdateRequest request) {
        // 权限/环境校验
        User login = requireLoginUser();
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
        String cover = fileService.getCoverUrl(record.getPetId(), PET);
        User user = userService.selectById(record.getDoctorId(),
                User::getId, User::getUsername, User::getAvatar);
        User owner = record.getOwnerId() == null ? null
                : userService.selectById(record.getOwnerId(), User::getId, User::getUsername, User::getAvatar);
        return MedicalRecordResponse.create(record, pet, cover, user, owner);
    }

    /**
     * 获取就诊记录列表
     */
    public Page<MedicalRecordResponse> getMedicalRecords(MedicalRecordQueryParams queryRequest, PageParams pageParams) {
        Page<MedicalRecord> result = medicalRecordMapper.queryByRequest(queryRequest).page(pageParams);
        Set<Long> petIds = result.getRecords().stream()
                .map(MedicalRecord::getPetId)
                .collect(Collectors.toSet());
        Map<Long, Pet> pets = petService.groupById(petIds,
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed);
        Map<Long, String> covers = fileService.getCoverUrls(PET, petIds);
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().flatMap(record -> Stream.of(record.getDoctorId(), record.getOwnerId())),
                User::getId, User::getUsername, User::getAvatar);
        return convertDto(result, record -> MedicalRecordResponse.createBatch(record, pets, covers, users));
    }

    /**
     * 创建病历
     */
    @Transactional
    public MedicalDetailResponse addMedicalDetail(MedicalDetailAddRequest request) {
        // 权限校验
        User login = requireLoginUser();
        requirePermission(login.isDoctor());
        MedicalRecord record = medicalRecordMapper.selectById(request.getRecordId(), MedicalRecord::getPetAge);

        // 创建病历
        MedicalDetail detail = request.createEntity(login.getId());
        save(detail);

        // 返回
        Pet pet = petService.selectById(record.getPetId(),
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed);
        String cover = fileService.getCoverUrl(record.getPetId(), PET);
        List<PetTagResponse> tags = petService.getTags(pet.getId());
        return MedicalDetailResponse.create(detail, record, login, List.of(), List.of(), pet, cover, tags);
    }

    /**
     * 获取病历
     */
    public MedicalDetailResponse getMedicalDetail(Long detailId) {
        MedicalDetail detail = requireById(detailId);
        return buildMedicalDetailResponse(detail);
    }

    /**
     * 获取病历
     */
    public Page<MedicalDetailResponse> getMedicalDetails(MedicalDetailQueryParams params, PageParams request) {
        Page<MedicalDetail> result = baseMapper.queryByParams(params).page(request);
        Map<Long, MedicalRecord> records = medicalRecordMapper.groupById(
                result.getRecords().stream().map(MedicalDetail::getRecordId),
                MedicalRecord::getId, MedicalRecord::getPetId, MedicalRecord::getPetAge);
        Set<Long> detailIds = result.getRecords().stream()
                .map(MedicalDetail::getId)
                .collect(Collectors.toSet());
        Map<Long, TreatmentPlan> plans = treatmentPlanMapper.selectByDetails(detailIds).groupById();
        List<Order> orderList = orderMapper.queryByTreatmentPlans(plans.keySet()).list();
        Map<Long, User> users = userService.groupById(
                Stream.of(
                        result.getRecords().stream().map(MedicalDetail::getDoctorId),
                        plans.values().stream().map(TreatmentPlan::getDoctorId),
                        orderList.stream().map(Order::getAllowerId)).flatMap(Function.identity()),
                User::getId, User::getUsername, User::getAvatar);
        Map<Long, Diagnosis> diagnosisMap = diagnosisMapper.queryByDetails(detailIds).groupById();
        Map<Long, Examination> examinationMap = examinationMapper.queryByDetails(detailIds).groupById();
        Map<Long, List<ExaminationFileResponse>> examinationFiles = examinationFileMapper
                .queryByExaminations(examinationMap.keySet())
                .groupList(ExaminationFile::getExaminationId, ExaminationFileResponse::create);
        Map<Long, List<ExaminationResponse>> examinations = examinationMap.values().stream()
                .map(exam -> ExaminationResponse.createBatch(exam, examinationFiles))
                .collect(Collectors.groupingBy(ExaminationResponse::getDetailId));
        Map<Long, List<DiagnosisResponse>> diagnoses = diagnosisMap.values().stream()
                .map(diagnosis -> DiagnosisResponse.createBatch(diagnosis, examinations))
                .collect(Collectors.groupingBy(DiagnosisResponse::getDetailId));
        Map<Long, Item> items = itemMapper.groupById(
                orderList.stream().map(Order::getItemId),
                Item::getId, Item::getName);
        Map<Long, List<OrderResponse>> orders = orderList.stream()
                .map(order -> OrderResponse.createBatch(order, users, items))
                .collect(Collectors.groupingBy(OrderResponse::getParentId));
        Map<Long, List<TreatmentPlanResponse>> treatments = plans.values().stream()
                .map(plan -> TreatmentPlanResponse.createBatch(plan, users, orders))
                .collect(Collectors.groupingBy(TreatmentPlanResponse::getDetailId));
        Set<Long> petIds = records.values().stream().map(MedicalRecord::getPetId)
                .collect(Collectors.toSet());
        Map<Long, Pet> pets = petService.groupById(petIds,
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed);
        Map<Long, String> covers = fileService.getCoverUrls(PET, petIds);
        Map<Long, List<PetTagResponse>> tags = petService.getTags(petIds);
        return convertDto(result, detail ->
                MedicalDetailResponse.createBatch(detail, records, users, diagnoses, treatments, pets, covers, tags));
    }

    /**
     * 修改病历
     */
    @Transactional
    public MedicalDetailResponse updateMedicalDetail(Long detailId, MedicalDetailUpdateRequest request) {
        // 权限校验
        User login = requireLoginUser();
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
        User login = requireLoginUser();
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
        Map<Long, Examination> examinationMap = examinationMapper.queryByDetail(detailId).groupById();
        Map<Long, List<ExaminationFileResponse>> files = examinationFileMapper
                .queryByExaminations(examinationMap.keySet())
                .groupList(ExaminationFile::getExaminationId, ExaminationFileResponse::create);

        Map<Long, List<ExaminationResponse>> examinations = examinationDiagnosisMapper
                .queryByExaminations(examinationMap.keySet())
                .groupList(ExaminationDiagnosis::getDiagnosisId,
                        exam -> ExaminationResponse.createBatch(exam.getExaminationId(), examinationMap, files));

        // 构造返回值
        List<DiagnosisResponse> examinationDiagnoses = diagnosisMapper.queryByDetail(detailId).list().stream()
                .map(diagnosis -> DiagnosisResponse.createBatch(diagnosis, examinations))
                .toList();
        List<TreatmentPlan> plans = treatmentPlanMapper.selectByDetail(detailId).list();
        Set<Long> planIds = plans.stream().map(TreatmentPlan::getId).collect(Collectors.toSet());
        List<Order> orderList = orderMapper.queryByTreatmentPlans(planIds).list();
        Map<Long, User> users = userService.groupById(
                plans.stream().map(TreatmentPlan::getDoctorId),
                orderList.stream().map(Order::getAllowerId),
                User::getId, User::getUsername, User::getAvatar);
        Map<Long, Item> items = itemMapper.groupById(orderList.stream().map(Order::getItemId));
        Map<Long, List<OrderResponse>> orders = orderList.stream()
                .map(order -> OrderResponse.createBatch(order, users, items))
                .collect(Collectors.groupingBy(OrderResponse::getParentId));
        List<TreatmentPlanResponse> treatments = plans.stream()
                .map(plan -> TreatmentPlanResponse.createBatch(plan, users, orders))
                .toList();
        Pet pet = petService.selectById(record.getPetId(),
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed);
        String cover = fileService.getCoverUrl(record.getPetId(), PET);
        List<PetTagResponse> tags = petService.getTags(pet.getId());
        return MedicalDetailResponse.create(detail, record, doctor, examinationDiagnoses, treatments, pet, cover, tags);
    }

    /**
     * 添加检查结果
     */
    @Transactional
    public DiagnosisResponse addDiagnosis(Long detailId, DiagnosisAddRequest request) {
        // 检查权限
        User login = requireLoginUser();
        requirePermission(login.isDoctor());
        requireDetailOpen(detailId, MedicalDetail::getIsCompleted, MedicalDetail::getIsDiscard);

        Diagnosis diagnosis = request.create(detailId);
        diagnosisMapper.insert(diagnosis);
        List<ExaminationDiagnosis> entries = request.createEntries(diagnosis.getId());
        examinationDiagnosisMapper.insert(entries);

        Set<Long> examinationIds = entries.stream()
                .map(ExaminationDiagnosis::getExaminationId)
                .collect(Collectors.toSet());
        Map<Long, List<ExaminationFileResponse>> files = examinationFileMapper
                .queryByExaminations(examinationIds)
                .groupList(ExaminationFile::getExaminationId, ExaminationFileResponse::create);
        List<ExaminationResponse> examinations = examinationMapper.selectByIds(examinationIds).stream()
                .map(examination -> ExaminationResponse.createBatch(examination, files))
                .toList();
        return DiagnosisResponse.create(diagnosis, examinations);
    }

    /**
     * 废弃检查结果
     */
    @Transactional
    public void discardDiagnosis(Long diagnosisId) {
        User login = requireLoginUser();
        requirePermission(login.isDoctor());
        Long detailId = diagnosisMapper
                .requireById(diagnosisId, Diagnosis::getDetailId)
                .getDetailId();
        requireDetailOpen(detailId, MedicalDetail::getIsCompleted, MedicalDetail::getIsDiscard);
        diagnosisMapper.discardById(diagnosisId).update();
    }

    /**
     * 创建治疗计划
     */
    @Transactional
    public TreatmentPlanResponse addTreatmentPlan(Long detailId, TreatmentPlanAddRequest request) {
        // 权限校验
        User login = requireLoginUser();
        requirePermission(login.isDoctor());
        requireDetailOpen(detailId, MedicalDetail::getIsCompleted, MedicalDetail::getIsDiscard);

        // 保存
        TreatmentPlan plan = request.create(login.getId(), detailId);
        treatmentPlanMapper.insert(plan);
        List<Order> orders = request.createOrders(login.getId(), plan.getId());
        orderMapper.insert(orders);

        Map<Long, User> users = userService.groupById(
                orders.stream().map(Order::getAllowerId),
                User::getId, User::getUsername, User::getAvatar);
        Map<Long, Item> items = itemMapper.groupById(
                orders.stream().map(Order::getItemId),
                Item::getId, Item::getName);
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
        User login = requireLoginUser();
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

        treatmentPlanMapper.discardByIds(planIds).update();
    }

    /**
     * 准备上传检查结果
     */
    public String beginExamination(Long detailId) {
        // 权限校验
        User login = requireLoginUser();
        requirePermission(login.isDoctor());
        requireDetailOpen(detailId, MedicalDetail::getIsCompleted, MedicalDetail::getIsDiscard);

        // 生成随机 uuid
        return beginRedisUuid(uuidKeyTemplate, String.valueOf(detailId));
    }

    /**
     * 上传文件
     */
    public String uploadExamination(String uuid, ExaminationFileUploadTable request) {
        // 权限校验
        User login = requireLoginUser();
        requirePermission(login.isDoctor());
        String redisKey = requireRedisUuid(uuidKeyTemplate, uuid);
        Long detailId = Long.valueOf(redisHelper.getString(redisKey));
        requireDetailOpen(detailId, MedicalDetail::getIsCompleted, MedicalDetail::getIsDiscard);

        // 上传文件
        return fileService
                .uploadFileToTemp(request.getFile(), request.getName(), uuid, fileKeyTemplate, EXAMINATION)
                .getFilename();
    }

    /**
     * 删除文件（上传时）
     */
    public void deleteExamination(String uuid, String filename) {
        // 权限校验
        User login = requireLoginUser();
        requirePermission(login.isDoctor());
        String redisKey = requireRedisUuid(uuidKeyTemplate, uuid);
        Long detailId = Long.valueOf(redisHelper.getString(redisKey));
        requireDetailOpen(detailId, MedicalDetail::getIsCompleted, MedicalDetail::getIsDiscard);

        // 删除文件及记录
        fileService.deleteTempFile(fileKeyTemplate, uuid, filename, EXAMINATION);
    }

    /**
     * 添加检查
     */
    public ExaminationResponse addExamination(String uuid, ExaminationAddRequest request) {
        // 权限校验
        User login = requireLoginUser();
        requirePermission(login.isDoctor());
        String redisKey = requireRedisUuid(uuidKeyTemplate, uuid);
        Long detailId = Long.valueOf(redisHelper.getString(redisKey));
        requireDetailOpen(detailId, MedicalDetail::getIsCompleted, MedicalDetail::getIsDiscard);

        // 创建检查结果
        Examination examination = request.create(login.getId(), detailId);
        examinationMapper.insert(examination);

        // 创建检查结果文件
        List<ExaminationFile> files = fileService.saveTempExaminations(fileKeyTemplate, uuid, examination);
        return ExaminationResponse.create(examination, files.stream()
                .map(ExaminationFileResponse::create)
                .toList());
    }

    /**
     * 获取检查结果
     */
    public ExaminationResponse getExamination(Long examId) {
        Examination examination = examinationMapper.requireById(examId);
        List<ExaminationFileResponse> files = examinationFileMapper.queryByExamination(examId).list().stream()
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
        User login = requireLoginUser();
        requirePermission(login.isDoctor());
        Pet pet = petService.requireById(petId,
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed, Pet::getAge);
        Vaccine vaccine = vaccineMapper.requireById(request.getVaccineId());

        // 记录疫苗
        VaccineRecord record = request.create(petId, login.getId());
        vaccineRecordMapper.insert(record);

        // 返回值
        Item item = itemMapper.requireById(vaccine.getItemId(), Item::getId, Item::getName);
        String cover = fileService.getCoverUrl(petId, PET);
        return VaccineResponse.create(record, vaccine, item, pet, cover, login);
    }

    /**
     * 获取疫苗接种记录
     */
    public List<VaccineResponse> getVaccines(Long petId) {
        Pet pet = petService.requireById(petId,
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed, Pet::getAge);
        String cover = fileService.getCoverUrl(petId, PET);
        List<VaccineRecord> vaccines = vaccineRecordMapper.selectList(vaccineRecordMapper.queryByPet(petId));
        Map<Long, Vaccine> vaccineMap = vaccineMapper.groupById(vaccines.stream().map(VaccineRecord::getVaccineId));
        Map<Long, Item> itemMap = itemMapper.groupById(
                vaccineMap.values().stream().map(Vaccine::getItemId),
                Item::getId, Item::getName);
        Map<Long, User> doctorMap = userService.groupById(
                vaccines.stream().map(VaccineRecord::getDoctorId),
                User::getId, User::getUsername, User::getAvatar);
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
        String cover = fileService.getCoverUrl(petId, PET);
        Map<Long, Optional<VaccineRecord>> vaccines = vaccineRecordMapper
                // 获取疫苗记录（倒序）
                .selectList(vaccineRecordMapper.queryByPet(petId)).stream()
                // 取分组第一项
                .collect(Collectors.groupingBy(VaccineRecord::getVaccineId, LinkedHashMap::new, Collectors.reducing((a, b) -> a)));
        Map<Long, Vaccine> vaccineMap = vaccineMapper.groupById(vaccines.keySet());
        Map<Long, Item> itemMap = itemMapper.groupById(
                vaccineMap.values().stream().map(Vaccine::getItemId),
                Item::getId, Item::getName);
        Map<Long, User> doctorMap = userService.groupById(
                vaccines.values().stream().map(Optional::orElseThrow).map(VaccineRecord::getDoctorId),
                User::getId, User::getUsername, User::getAvatar);
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
        User login = requireLoginUser();
        requirePermission(login.isDoctor() || login.isWorker());
        Dewormer dewormer = dewormerMapper.requireById(request.getDewormerId());
        Item dewormerItem = itemMapper.requireById(dewormer.getItemId(),
                Item::getId, Item::getName);
        Pet pet = petService.requireById(petId,
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed, Pet::getAge);

        DewormRecord record = request.create(petId, login.getId());
        dewormRecordMapper.insert(record);

        String cover = fileService.getCoverUrl(petId, PET);
        return DewormResponse.create(record, dewormer, dewormerItem, pet, cover, login);
    }

    /**
     * 获取驱虫记录
     */
    public List<DewormResponse> getDeworms(Long petId) {
        requireLoginUser();
        Pet pet = petService.requireById(petId,
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed, Pet::getAge);

        String cover = fileService.getCoverUrl(petId, PET);
        List<DewormRecord> deworms = dewormRecordMapper.selectList(dewormRecordMapper.queryByPet(petId));
        Map<Long, Dewormer> dewormerMap = dewormerMapper.groupById(deworms.stream().map(DewormRecord::getDewormerId));
        Map<Long, Item> itemMap = itemMapper.groupById(
                dewormerMap.values().stream().map(Dewormer::getItemId),
                Item::getId, Item::getName);
        Map<Long, User> doctorMap = userService.groupById(
                deworms.stream().map(DewormRecord::getDoctorId),
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
        Map<Long, String> covers = fileService.getCoverUrls(PET, petIds);
        List<VaccineRecord> vaccines = vaccineRecordMapper.selectList(vaccineRecordMapper.queryByPets(petIds));
        Map<Long, Vaccine> vaccineMap = vaccineMapper.groupById(vaccines.stream().map(VaccineRecord::getVaccineId));
        Map<Long, Item> itemMap = itemMapper.groupById(
                vaccineMap.values().stream().map(Vaccine::getItemId),
                Item::getId, Item::getName);
        Map<Long, User> doctorMap = userService.groupById(
                vaccines.stream().map(VaccineRecord::getDoctorId),
                User::getId, User::getUsername, User::getAvatar);
        return vaccines.stream()
                .map(record -> VaccineResponse.createBatch(record, pets, covers, vaccineMap, itemMap, doctorMap))
                .collect(Collectors.groupingBy(VaccineResponse::getPetId));
    }

    /**
     * 获取驱虫记录
     */
    public Map<Long, List<DewormResponse>> getDewormsByPetIds(Set<Long> petIds) {
        requireLoginUser();
        Map<Long, Pet> pets = petService.groupById(petIds,
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed, Pet::getAge);
        Map<Long, String> covers = fileService.getCoverUrls(PET, petIds);
        List<DewormRecord> deworms = dewormRecordMapper.selectList(dewormRecordMapper.queryByPets(petIds));
        Map<Long, Dewormer> dewormerMap = dewormerMapper.groupById(deworms.stream().map(DewormRecord::getDewormerId));
        Map<Long, Item> itemMap = itemMapper.groupById(
                dewormerMap.values().stream().map(Dewormer::getItemId),
                Item::getId, Item::getName);
        Map<Long, User> doctorMap = userService.groupById(
                deworms.stream().map(DewormRecord::getDoctorId),
                User::getId, User::getUsername, User::getAvatar);
        return deworms.stream()
                .map(record -> DewormResponse.createBatch(record, pets, covers, dewormerMap, itemMap, doctorMap))
                .collect(Collectors.groupingBy(DewormResponse::getPetId));
    }

    /**
     * 添加康复计划
     */
    @Transactional
    public RehabPlanResponse addRehabPlan(Long petId, RehabPlanAddRequest request) {
        // 权限校验
        User login = requireLoginUser();
        requirePermission(login.isDoctor());
        Pet pet = petService.requireById(petId,
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed);

        // 保存
        RehabPlan plan = request.create(login.getId(), petId);
        rehabPlanMapper.insert(plan);
        List<Order> orders = request.createOrders(login.getId(), plan.getId());
        orderMapper.insert(orders);
        RehabPlanStatus status = request.createStatus(plan);
        rehabPlanStatusMapper.insert(status);

        // 返回
        String cover = fileService.getCoverUrl(petId, PET);
        return RehabPlanResponse.create(plan, login, pet, cover,
                List.of(RehabPlanStatusResponse.create(status, login)));
    }

    /**
     * 获取康复计划
     */
    public RehabPlanResponse getRehabPlan(Long planId) {
        RehabPlan plan = rehabPlanMapper.requireById(planId);
        User doctor = userService.requireById(plan.getDoctorId(),
                User::getId, User::getUsername, User::getAvatar);
        Pet pet = petService.requireById(plan.getPetId(),
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed);
        String cover = fileService.getCoverUrl(plan.getPetId(), PET);
        List<RehabPlanStatus> plans = rehabPlanStatusMapper.selectList(rehabPlanStatusMapper.queryByPlan(planId));
        Map<Long, User> users = userService.groupById(
                plans.stream().map(RehabPlanStatus::getUserId),
                User::getId, User::getUsername, User::getAvatar);
        return RehabPlanResponse.create(plan, doctor, pet, cover, plans.stream()
                .map(record -> RehabPlanStatusResponse.createBatch(record, users))
                .toList());
    }

    /**
     * 查询康复计划
     */
    public Page<RehabPlanResponse> getRehabPlans(RehabPlanQueryParams queryRequest, PageParams pageParams) {
        Page<RehabPlan> plans = rehabPlanMapper.queryByRequest(queryRequest).page(pageParams);
        Set<Long> planIds = plans.getRecords().stream()
                .map(RehabPlan::getId)
                .collect(Collectors.toSet());
        List<RehabPlanStatus> statusRecordMap = rehabPlanStatusMapper.selectList(rehabPlanStatusMapper.queryByPlans(planIds));
        Map<Long, User> users = userService.groupById(
                plans.getRecords().stream().map(RehabPlan::getDoctorId),
                statusRecordMap.stream().map(RehabPlanStatus::getUserId),
                User::getId, User::getUsername, User::getAvatar);
        Set<Long> petIds = plans.getRecords().stream().map(RehabPlan::getPetId)
                .collect(Collectors.toSet());
        Map<Long, Pet> pets = petService.groupById(petIds,
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed);
        Map<Long, String> covers = fileService.getCoverUrls(PET, petIds);
        Map<Long, List<RehabPlanStatusResponse>> statusRecords = statusRecordMap.stream()
                .map(record -> RehabPlanStatusResponse.createBatch(record, users))
                .collect(Collectors.groupingBy(RehabPlanStatusResponse::getPlanId));
        return convertDto(plans,
                plan -> RehabPlanResponse.createBatch(plan, users, pets, covers, statusRecords));
    }

    /**
     * 更新康复计划结果
     */
    @Transactional
    public RehabPlanResponse updateRehabPlanStatus(Long planId, RehabPlanStatusUpdateRequest request) {
        // 权限校验
        User login = requireLoginUser();
        requirePermission(login.isDoctor());
        RehabPlan plan = rehabPlanMapper.requireById(planId);

        // 更新
        RehabPlanStatus status = request.create(planId, login.getId());
        rehabPlanMapper.updateStatusById(planId, status.getStatus()).update();
        rehabPlanStatusMapper.insert(status);

        // 返回
        plan.setStatus(status.getStatus());
        User user = userService.requireById(status.getUserId(),
                User::getId, User::getUsername, User::getAvatar);
        Pet pet = petService.requireById(plan.getPetId(),
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed);
        String cover = fileService.getCoverUrl(plan.getPetId(), PET);
        List<RehabPlanStatus> statusRecords = rehabPlanStatusMapper.selectList(rehabPlanStatusMapper.queryByPlan(planId));
        Map<Long, User> users = userService.groupById(
                statusRecords.stream().map(RehabPlanStatus::getUserId),
                User::getId, User::getUsername, User::getAvatar);
        return RehabPlanResponse.create(plan, user, pet, cover, statusRecords.stream()
                .map(record -> RehabPlanStatusResponse.createBatch(record, users))
                .toList());
    }

    /**
     * 添加康复记录
     */
    @Transactional
    public RehabRecordResponse addRehabRecord(Long planId, RehabRecordAddTable request) {
        // 权限校验
        User login = requireLoginUser();
        requirePermission(login.isDoctor() || login.isVolunteer());
        rehabPlanMapper.requireExist(planId);

        // 添加数据
        RehabRecord record = request.create(planId, login.getId());
        rehabRecordMapper.insert(record);
        List<MediaFile> files = fileService.uploadMediaFiles(request.getFiles(), record.getId(), REHAB_PLAN);
        return RehabRecordResponse.create(record, login, files);
    }

    /**
     * 根据康复计划获取康复记录
     */
    public List<RehabRecordResponse> getRehabRecords(Long planId) {
        List<RehabRecord> records = rehabRecordMapper.selectList(rehabRecordMapper.queryByPlan(planId));
        Map<Long, User> users = userService.groupById(
                records.stream().map(RehabRecord::getUserId),
                User::getId, User::getUsername, User::getAvatar);
        Set<Long> recordIds = records.stream().map(RehabRecord::getId).collect(Collectors.toSet());
        Map<Long, List<MediaFile>> files = fileService.getBaseMapper().queryByParents(REHAB_PLAN, recordIds)
                .select(MediaFile::getFilename)
                .groupList(MediaFile::getParentId);
        return records.stream()
                .map(record -> RehabRecordResponse.createBatch(record, users, files))
                .toList();
    }

    /**
     * 添加健康评估
     */
    public HealthAssessmentResponse addHealthAssessment(Long petId, HealthAssessmentAddRequest request) {
        // 权限校验
        User login = requireLoginUser();
        requirePermission(login.isDoctor());
        Pet pet = petService.requireById(petId,
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed);

        // 添加数据
        HealthAssessment assessment = request.create(petId, login.getId());
        healthAssessmentMapper.insert(assessment);

        String cover = fileService.getCoverUrl(petId, PET);
        return HealthAssessmentResponse.create(assessment, pet, cover, login);
    }

    /**
     * 获取健康评估
     */
    public HealthAssessmentResponse getHealthAssessment(Long assessmentId) {
        HealthAssessment assessment = healthAssessmentMapper.requireById(assessmentId);
        Pet pet = petService.requireById(assessment.getPetId(),
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed);
        String cover = fileService.getCoverUrl(pet.getId(), PET);
        User assessor = userService.requireById(assessment.getAssessorId(),
                User::getId, User::getUsername, User::getAvatar);
        return HealthAssessmentResponse.create(assessment, pet, cover, assessor);
    }

    /**
     * 获取健康评估
     */
    public Page<HealthAssessmentResponse> getHealthAssessments(@Nullable Long petId, PageParams pageParams) {
        Page<HealthAssessment> assessments = petId == null
                ? healthAssessmentMapper.selectPage(pageParams.createPage(), null)
                : healthAssessmentMapper.queryByPet(petId).page(pageParams);
        Set<Long> petIds = assessments.getRecords().stream()
                .map(HealthAssessment::getPetId)
                .collect(Collectors.toSet());
        Map<Long, Pet> pets = petService.groupById(petIds,
                Pet::getId, Pet::getName, Pet::getSex, Pet::getType, Pet::getBreed);
        Map<Long, String> covers = fileService.getCoverUrls(PET, petIds);
        Map<Long, User> users = userService.groupById(
                assessments.getRecords().stream().map(HealthAssessment::getAssessorId),
                User::getId, User::getUsername, User::getAvatar);
        return convertDto(assessments,
                assessment -> HealthAssessmentResponse.createBatch(assessment, pets, covers, users));
    }

    @Autowired
    public void setServices(PetService petService, UserService userService, FileService fileService) {
        this.petService = petService;
        this.userService = userService;
        this.fileService = fileService;
    }
}
