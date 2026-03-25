package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.service.MedicalService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医疗护理管理模块
 * - 初诊登记 ( √ × )
 * ---- 初诊登记 addFirstRegistration ( √ × )
 * ---- 获取初诊登记 getFirstRegistration ( √ × )
 * ---- 获取所有初诊登记 getFirstRegistrations ( √ × )
 * - 诊疗管理 ( √ × )
 * ---- 创建就诊记录 addMedicalRecord ( √ × )
 * ---- 更新就诊记录 updateMedicalRecord ( √ × )
 * ---- 获取就诊记录 getMedicalRecord ( √ × )
 * ---- 查询就诊记录 getMedicalRecords ( √ × )
 * - 病历创建 ( √ × )
 * ---- 创建病历 addMedicalDetail ( √ × )
 * ---- 修改病历 updateMedicalDetail ( √ × )
 * ---- 完成病历 completeMedicalDetail ( √ × )
 * ---- 添加检查诊断 addDiagnosis ( √ × )
 * ---- 废弃检查诊断 discardDiagnosis ( √ × )
 * ---- 添加治疗计划 addTreatmentPlan ( √ × )
 * ---- 废弃治疗计划 discardTreatmentPlan ( √ × )
 * - 医疗记录 ( √ × )
 * ---- 开始上传记录 beginExamination ( √ × )
 * ---- 上传记录文件 uploadExamination ( √ × )
 * ---- 删除记录文件（上传时） deleteExamination ( √ × )
 * ---- 新建记录信息 addExamination ( √ × )
 * ---- 获取检查记录 getExamination ( √ × )
 * - 疫苗接种 ( √ × )
 * ---- 接种疫苗 addVaccine ( √ × )
 * ---- 接种记录 getVaccines ( √ × )
 * ---- 最新接种记录 getLatestVaccines ( √ × )
 * - 驱虫管理 ( √ × )
 * ---- 驱虫 addDeworm ( √ × )
 * ---- 驱虫记录 getDeworms ( √ × )
 * - 康复护理 ( × × )
 * ---- 添加康复计划 addRehabPlan ( × × )
 * ---- 获取康复计划 getRehabPlans ( × × )
 * ---- 设置康复计划状态 setRehabPlanStatus ( × × )
 * ---- 添加执行记录 addRehabRecord ( × × )
 * ---- 获取执行记录 getRehabRecords ( × × )
 * - 健康评估 ( × × )
 * ---- 创建健康评估 addHealthAssessment ( × × )
 * ---- 获取健康评估 getHealthAssessments ( × × )
 */
@Validated
@RestController
@RequestMapping("/medical")
@RequiredArgsConstructor
public class MedicalCareController {

    private final MedicalService medicalService;

    @PostMapping("/first")
    public Result<FirstRegistrationResponse> addFirstVisitRegistration(@RequestBody FirstRegistrationAddRequest request) {
        FirstRegistrationResponse response = medicalService.addFirstVisitRegistration(request);
        return Result.success(response);
    }

    @GetMapping("/first")
    public Result<Page<FirstRegistrationItemResponse>> getFirstVisitRegistrations(FirstRegistrationQueryRequest query, PageRequest page) {
        Page<FirstRegistrationItemResponse> response = medicalService.getFirstVisitRegistrations(query, page);
        return Result.success(response);
    }

    @GetMapping("/first/{id}")
    public Result<FirstRegistrationResponse> getFirstVisitRegistration(@PathVariable("id") Long registrationId) {
        FirstRegistrationResponse response = medicalService.getFirstVisitRegistration(registrationId);
        return Result.success(response);
    }

    @PostMapping("/record")
    public Result<MedicalRecordResponse> addMedicalRecord(@RequestBody MedicalRecordRequest request) {
        MedicalRecordResponse response = medicalService.addMedicalVisitRecord(request);
        return Result.success(response);
    }

    @PostMapping("/record/{id}")
    public Result<MedicalRecordResponse> updateMedicalRecord(@PathVariable("id") Long recordId,
                                                             @RequestBody MedicalRecordUpdateRequest request) {
        MedicalRecordResponse response = medicalService.updateMedicalRecord(recordId, request);
        return Result.success(response);
    }

    @GetMapping("/record/{id}")
    public Result<MedicalRecordResponse> getMedicalRecord(@PathVariable("id") Long recordId) {
        MedicalRecordResponse response = medicalService.getMedicalRecord(recordId);
        return Result.success(response);
    }

    @GetMapping("/record")
    public Result<Page<MedicalRecordResponse>> getMedicalRecords(MedicalRecordQueryRequest query, PageRequest page) {
        Page<MedicalRecordResponse> response = medicalService.getMedicalRecords(query, page);
        return Result.success(response);
    }

    @PostMapping("/detail")
    public Result<MedicalDetailResponse> addMedicalDetail(MedicalDetailAddRequest request) {
        MedicalDetailResponse response = medicalService.addMedicalDetail(request);
        return Result.success(response);
    }

    @PostMapping("/detail/{id}")
    public Result<MedicalDetailResponse> updateMedicalDetail(@PathVariable("id") Long detailId, MedicalDetailUpdateRequest request) {
        MedicalDetailResponse response = medicalService.updateMedicalDetail(detailId, request);
        return Result.success(response);
    }

    @PutMapping("/detail/{id}")
    public Result<MedicalDetailResponse> completeMedicalDetail(@PathVariable("id") Long detailId) {
        MedicalDetailResponse response = medicalService.completeMedicalDetail(detailId);
        return Result.success(response);
    }

    @PostMapping("/detail/{id}/diagnosis")
    public Result<ExaminationDiagnosisResponse> addDiagnosis(@PathVariable("id") Long detailId,
                                                             @RequestBody ExaminationDiagnosisAddRequest request) {
        ExaminationDiagnosisResponse response = medicalService.addDiagnosis(detailId, request);
        return Result.success(response);
    }

    @DeleteMapping("/diagnosis/{dId}")
    public Result<Void> discardDiagnosis(@PathVariable("dId") Long diagnosisId) {
        medicalService.discardDiagnosis(diagnosisId);
        return Result.success();
    }

    @PostMapping("/detail/{id}/plan")
    public Result<TreatmentPlanResponse> addTreatmentPlan(@PathVariable("id") Long detailId, TreatmentPlanAddRequest request) {
        TreatmentPlanResponse response = medicalService.addTreatmentPlan(detailId, request);
        return Result.success(response);
    }

    @DeleteMapping("/plan")
    public Result<Void> discardTreatmentPlan(@RequestBody IdsRequest ids) {
        medicalService.discardTreatmentPlan(ids);
        return Result.success();
    }

    @PutMapping("/details/{id}/exam")
    public Result<String> beginExamination(@PathVariable("id") Long detailId) {
        String uuid = medicalService.beginExamination(detailId);
        return Result.success(uuid);
    }

    @PostMapping("/exam/{_id}/doc")
    public Result<String> uploadExamination(@PathVariable("_id") String examId, ExaminationFileUploadRequest request) {
        String filename = medicalService.uploadExamination(examId, request);
        return Result.success(filename);
    }

    @DeleteMapping("/exam/{_id}/doc/{name}")
    public Result<Void> deleteExamination(@PathVariable("_id") String examId, @PathVariable("name") String filename) {
        medicalService.deleteExamination(examId, filename);
        return Result.success();
    }

    @PostMapping("/exam/{_id}")
    public Result<ExaminationResponse> addExamination(@PathVariable("_id") String examId, ExaminationAddRequest request) {
        ExaminationResponse response = medicalService.addExamination(examId, request);
        return Result.success(response);
    }

    @GetMapping("/exam/{id}")
    public Result<ExaminationResponse> getExamination(@PathVariable("id") Long examId) {
        ExaminationResponse response = medicalService.getExamination(examId);
        return Result.success(response);
    }

    @PostMapping("/vaccine/pet/{id}")
    public Result<VaccineResponse> addVaccine(@PathVariable("id") Long petId, @RequestBody VaccineAddRequest request) {
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
    public Result<DewormResponse> addDeworm(@PathVariable("id") Long petId, @RequestBody DewormAddRequest request) {
        DewormResponse response = medicalService.addDeworm(petId, request);
        return Result.success(response);
    }

    @GetMapping("/deworm/pet/{id}")
    public Result<List<DewormResponse>> getDeworms(@PathVariable("id") Long petId) {
        List<DewormResponse> response = medicalService.getDeworms(petId);
        return Result.success(response);
    }
}
