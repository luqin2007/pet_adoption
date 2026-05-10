package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.dto.HealthAssessmentResponse;
import com.example.backend.dto.MedicalDetailQueryParams;
import com.example.backend.service.MedicalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Delete;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医疗护理管理模块<br>
 * - 初诊登记 ( √ × )<br>
 * ---- 初诊登记 addFirstRegistration ( √ × )<br>
 * ---- 获取初诊登记 getFirstRegistration ( √ × )<br>
 * ---- 获取所有初诊登记 getFirstRegistrations ( √ × )<br>
 * - 诊疗管理 ( √ × )<br>
 * ---- 创建就诊记录 addMedicalRecord ( √ × )<br>
 * ---- 更新就诊记录 updateMedicalRecord ( √ × )<br>
 * ---- 获取就诊记录 getMedicalRecord ( √ × )<br>
 * ---- 查询就诊记录 getMedicalRecords ( √ × )<br>
 * - 病历创建 ( √ × )<br>
 * ---- 创建病历 addMedicalDetail ( √ × )<br>
 * ---- 获取病历 getMedicalDetail ( √ × )<br>
 * ---- 获取病历 getMedicalDetails ( √ × )<br>
 * ---- 修改病历 updateMedicalDetail ( √ × )<br>
 * ---- 完成病历 completeMedicalDetail ( √ × )<br>
 * ---- 添加检查诊断 addDiagnosis ( √ × )<br>
 * ---- 废弃检查诊断 discardDiagnosis ( √ × )<br>
 * ---- 添加治疗计划 addTreatmentPlan ( √ × )<br>
 * ---- 废弃治疗计划 discardTreatmentPlan ( √ × )<br>
 * - 医疗记录 ( √ × )<br>
 * ---- 开始上传记录 beginExamination ( √ × )<br>
 * ---- 上传记录文件 uploadExamination ( √ × )<br>
 * ---- 删除记录文件（上传时） deleteExamination ( √ × )<br>
 * ---- 新建记录信息 addExamination ( √ × )<br>
 * ---- 获取检查记录 getExamination ( √ × )<br>
 * - 疫苗接种 ( √ × )<br>
 * ---- 接种疫苗 addVaccine ( √ × )<br>
 * ---- 接种记录 getVaccines ( √ × )<br>
 * ---- 最新接种记录 getLatestVaccines ( √ × )<br>
 * - 驱虫管理 ( √ × )<br>
 * ---- 驱虫 addDeworm ( √ × )<br>
 * ---- 驱虫记录 getDeworms ( √ × )<br>
 * - 康复护理 ( √ × )<br>
 * ---- 添加康复计划 addRehabPlan ( √ × )<br>
 * ---- 获取康复计划 getRehabPlan ( √ × )<br>
 * ---- 获取康复计划 getRehabPlans ( √ × )<br>
 * ---- 更新康复计划状态 updateRehabPlanStatus ( √ × )<br>
 * ---- 添加执行记录 addRehabRecord ( √ × )<br>
 * ---- 获取执行记录 getRehabRecords ( √ × )<br>
 * - 健康评估 ( √ × )<br>
 * ---- 创建健康评估 addHealthAssessment ( √ × )<br>
 * ---- 获取健康评估 getHealthAssessment ( √ × )<br>
 * ---- 获取健康评估 getHealthAssessments ( √ × )
 */
@Validated
@RestController
@RequestMapping("/api/v1/medical")
@RequiredArgsConstructor
public class MedicalCareController {

    private final MedicalService medicalService;

    @PostMapping("/first")
    public Result<FirstRegistrationResponse> addFirstVisitRegistration(@Valid @RequestBody FirstRegistrationAddRequest request) {
        FirstRegistrationResponse response = medicalService.addFirstVisitRegistration(request);
        return Result.success(response);
    }

    @GetMapping("/first")
    public Result<Page<FirstRegistrationItemResponse>> getFirstVisitRegistrations(@Valid FirstRegistrationQueryParams query,
                                                                                  PageParams page) {
        Page<FirstRegistrationItemResponse> response = medicalService.getFirstVisitRegistrations(query, page);
        return Result.success(response);
    }

    @GetMapping("/first/{id}")
    public Result<FirstRegistrationResponse> getFirstVisitRegistration(@PathVariable("id") Long registrationId) {
        FirstRegistrationResponse response = medicalService.getFirstVisitRegistration(registrationId);
        return Result.success(response);
    }

    @PostMapping("/record/pet/{id}")
    public Result<MedicalRecordResponse> addMedicalRecord(@PathVariable("id") Long petId,
                                                          @Valid @RequestBody MedicalRecordAddRequest request) {
        MedicalRecordResponse response = medicalService.addMedicalRecord(petId, request);
        return Result.success(response);
    }

    @PutMapping("/record/{id}")
    public Result<MedicalRecordResponse> updateMedicalRecord(@PathVariable("id") Long recordId,
                                                             @Valid @RequestBody MedicalRecordUpdateRequest request) {
        MedicalRecordResponse response = medicalService.updateMedicalRecord(recordId, request);
        return Result.success(response);
    }

    @GetMapping("/record")
    public Result<Page<MedicalRecordResponse>> getMedicalRecords(@Valid MedicalRecordQueryParams query, PageParams page) {
        Page<MedicalRecordResponse> response = medicalService.getMedicalRecords(query, page);
        return Result.success(response);
    }

    @GetMapping("/record/owner-exists")
    public Result<Boolean> existsMedicalRecordByOwnerId(@RequestParam("ownerId") Long ownerId) {
        return Result.success(medicalService.existsMedicalRecordByOwnerId(ownerId));
    }

    @GetMapping("/record/{id}")
    public Result<MedicalRecordResponse> getMedicalRecord(@PathVariable("id") Long recordId) {
        MedicalRecordResponse response = medicalService.getMedicalRecord(recordId);
        return Result.success(response);
    }

    @PostMapping("/detail")
    public Result<MedicalDetailResponse> addMedicalDetail(@Valid @RequestBody MedicalDetailAddRequest request) {
        MedicalDetailResponse response = medicalService.addMedicalDetail(request);
        return Result.success(response);
    }

    @GetMapping("/detail/{id}")
    public Result<MedicalDetailResponse> getMedicalDetail(@PathVariable("id") Long detailId) {
        MedicalDetailResponse response = medicalService.getMedicalDetail(detailId);
        return Result.success(response);
    }

    @GetMapping("/detail")
    public Result<Page<MedicalDetailResponse>> getMedicalDetails(@Valid MedicalDetailQueryParams query, PageParams page) {
        Page<MedicalDetailResponse> response = medicalService.getMedicalDetails(query, page);
        return Result.success(response);
    }

    @PutMapping("/detail/{id}")
    public Result<MedicalDetailResponse> updateMedicalDetail(@PathVariable("id") Long detailId,
                                                             @Valid @RequestBody MedicalDetailUpdateRequest request) {
        MedicalDetailResponse response = medicalService.updateMedicalDetail(detailId, request);
        return Result.success(response);
    }

    @PatchMapping("/detail/{id}")
    public Result<MedicalDetailResponse> completeMedicalDetail(@PathVariable("id") Long detailId) {
        MedicalDetailResponse response = medicalService.completeMedicalDetail(detailId);
        return Result.success(response);
    }

    @DeleteMapping("/detail/{id}")
    public Result<MedicalDetailResponse> discardMedicalDetail(@PathVariable("id") Long detailId) {
        MedicalDetailResponse response = medicalService.discardMedicalDetail(detailId);
        return Result.success(response);
    }

    @PostMapping("/detail/{id}/diagnosis")
    public Result<DiagnosisResponse> addDiagnosis(@PathVariable("id") Long detailId,
                                                  @Valid @RequestBody DiagnosisAddRequest request) {
        DiagnosisResponse response = medicalService.addDiagnosis(detailId, request);
        return Result.success(response);
    }

    @DeleteMapping("/diagnosis/{dId}")
    public Result<Void> discardDiagnosis(@PathVariable("dId") Long diagnosisId) {
        medicalService.discardDiagnosis(diagnosisId);
        return Result.success();
    }

    @PostMapping("/detail/{id}/plan")
    public Result<TreatmentPlanResponse> addTreatmentPlan(@PathVariable("id") Long detailId,
                                                          @Valid @RequestBody TreatmentPlanAddRequest request) {
        TreatmentPlanResponse response = medicalService.addTreatmentPlan(detailId, request);
        return Result.success(response);
    }

    @DeleteMapping("/plan")
    public Result<Void> discardTreatmentPlan(@Valid @RequestBody IdsRequest ids) {
        medicalService.discardTreatmentPlan(ids);
        return Result.success();
    }

    @GetMapping("/details/{id}/exam")
    public Result<List<ExaminationResponse>> getExaminations(@PathVariable("id") Long detailId) {
        List<ExaminationResponse> response = medicalService.getExaminations(detailId);
        return Result.success(response);
    }

    @PutMapping("/details/{id}/exam")
    public Result<String> beginExamination(@PathVariable("id") Long detailId) {
        String uuid = medicalService.beginExamination(detailId);
        return Result.success(uuid);
    }

    @PutMapping("/exam/{_id}/doc")
    public Result<String> uploadExamination(@PathVariable("_id") String uuid,
                                            @ModelAttribute ExaminationFileUploadTable request) {
        String filename = medicalService.uploadExamination(uuid, request);
        return Result.success(filename);
    }

    @DeleteMapping("/exam/{_id}/doc/{name}")
    public Result<Void> deleteExamination(@PathVariable("_id") String uuid, @PathVariable("name") String filename) {
        medicalService.deleteExamination(uuid, filename);
        return Result.success();
    }

    @PostMapping("/exam/{_id}")
    public Result<ExaminationResponse> addExamination(@PathVariable("_id") String uuid,
                                                      @Valid @RequestBody ExaminationAddRequest request) {
        ExaminationResponse response = medicalService.addExamination(uuid, request);
        return Result.success(response);
    }

    @GetMapping("/exam/{id}")
    public Result<ExaminationResponse> getExamination(@PathVariable("id") Long examId) {
        ExaminationResponse response = medicalService.getExamination(examId);
        return Result.success(response);
    }

    @PostMapping("/vaccine/pet/{id}")
    public Result<VaccineResponse> addVaccine(@PathVariable("id") Long petId,
                                              @Valid @RequestBody VaccineAddRequest request) {
        VaccineResponse response = medicalService.addVaccine(petId, request);
        return Result.success(response);
    }

    @GetMapping("/vaccine/pet/{id}")
    public Result<List<VaccineResponse>> getVaccines(@PathVariable("id") Long petId) {
        List<VaccineResponse> response = medicalService.getVaccines(petId);
        return Result.success(response);
    }

    @GetMapping("/vaccine/pet/{id}/new")
    public Result<List<VaccineResponse>> getLatestVaccines(@PathVariable("id") Long petId) {
        List<VaccineResponse> response = medicalService.getLatestVaccines(petId);
        return Result.success(response);
    }

    @PostMapping("/deworm/pet/{id}")
    public Result<DewormResponse> addDeworm(@PathVariable("id") Long petId,
                                            @Valid @RequestBody DewormAddRequest request) {
        DewormResponse response = medicalService.addDeworm(petId, request);
        return Result.success(response);
    }

    @GetMapping("/deworm/pet/{id}")
    public Result<List<DewormResponse>> getDeworms(@PathVariable("id") Long petId) {
        List<DewormResponse> response = medicalService.getDeworms(petId);
        return Result.success(response);
    }

    @PostMapping("/rehab/pet/{id}")
    public Result<RehabPlanResponse> addRehabPlan(@PathVariable("id") Long petId,
                                                  @Valid @RequestBody RehabPlanAddRequest request) {
        RehabPlanResponse response = medicalService.addRehabPlan(petId, request);
        return Result.success(response);
    }

    @GetMapping("/rehab/{id}")
    public Result<RehabPlanResponse> getRehabPlan(@PathVariable("id") Long planId) {
        RehabPlanResponse response = medicalService.getRehabPlan(planId);
        return Result.success(response);
    }

    @GetMapping("/rehab")
    public Result<Page<RehabPlanResponse>> getRehabPlans(@Valid RehabPlanQueryParams query, PageParams page) {
        Page<RehabPlanResponse> response = medicalService.getRehabPlans(query, page);
        return Result.success(response);
    }

    @PutMapping("/rehab/{id}/status")
    public Result<RehabPlanResponse> updateRehabPlanStatus(@PathVariable("id") Long planId,
                                                           @Valid @RequestBody RehabPlanStatusUpdateRequest request) {
        RehabPlanResponse response = medicalService.updateRehabPlanStatus(planId, request);
        return Result.success(response);
    }

    @PostMapping("/rehab/{id}/record")
    public Result<RehabRecordResponse> addRehabRecord(@PathVariable("id") Long planId,
                                                      @Valid @ModelAttribute RehabRecordAddTable request) {
        RehabRecordResponse response = medicalService.addRehabRecord(planId, request);
        return Result.success(response);
    }

    @GetMapping("/rehab/{id}/record")
    public Result<List<RehabRecordResponse>> getRehabRecords(@PathVariable("id") Long planId) {
        List<RehabRecordResponse> response = medicalService.getRehabRecords(planId);
        return Result.success(response);
    }

    @PostMapping("/health/pet/{id}")
    public Result<HealthAssessmentResponse> addHealthAssessment(@PathVariable("id") Long petId,
                                                                @Valid @RequestBody HealthAssessmentAddRequest request) {
        HealthAssessmentResponse response = medicalService.addHealthAssessment(petId, request);
        return Result.success(response);
    }

    @GetMapping("/health/{id}")
    public Result<HealthAssessmentResponse> getHealthAssessment(@PathVariable("id") Long assessmentId) {
        HealthAssessmentResponse response = medicalService.getHealthAssessment(assessmentId);
        return Result.success(response);
    }

    @GetMapping("/health")
    public Result<Page<HealthAssessmentResponse>> getHealthAssessments(PageParams page) {
        Page<HealthAssessmentResponse> response = medicalService.getHealthAssessments(null, page);
        return Result.success(response);
    }

    @GetMapping("/health/pet/{id}")
    public Result<Page<HealthAssessmentResponse>> getHealthAssessments(@PathVariable("id") Long petId, PageParams page) {
        Page<HealthAssessmentResponse> response = medicalService.getHealthAssessments(petId, page);
        return Result.success(response);
    }
}
