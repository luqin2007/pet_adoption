package com.example.backend.dto;

import com.example.backend.entity.MedicalDetail;
import com.example.backend.entity.MedicalRecord;
import com.example.backend.entity.Pet;
import com.example.backend.entity.User;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;

import static com.example.backend.util.C.PARENT_USER;

@Data
@AllArgsConstructor
public class MedicalDetailResponse {

    private Long id;
    private Long recordId;
    private Date createTime;
    private Boolean isCompleted;

    // ---- S 主观信息
    private String description;
    private String history;
    private String pastHistory;
    private String lifeHabit;

    // ---- O 客观检查
    private Double weight;
    private Double temperature;
    private Double heartRate;
    private Double respiratoryRate;
    private String physicalExam;
    private List<ExaminationDiagnosisResponse> objectiveDiagnoses;

    // ---- A 评估诊断
    private String diagnosis;
    private String differential;

    // ---- P 治疗方案
    private String exam;
    private String treatment;
    private String advice;
    private List<TreatmentPlanResponse> treatments;

    // user
    private Long doctorId;
    private String username;
    private String avatar;

    // record
    private Integer age;

    // pet
    private Long petId;
    private String name;
    private String sex;
    private String type;
    private String breed;
    private List<PetTagResponse> tags;
    private String cover;

    /**
     * Pet: id, name, sex, type, breed<br>
     * MedicalRecord: petAge<br>
     * User: id, username, avatar<br>
     * <br>
     * treatments: MedicalDetail.id
     */
    public static MedicalDetailResponse create(MedicalDetail detail,
                                               MedicalRecord record,
                                               User doctor,
                                               List<ExaminationDiagnosisResponse> examinationDiagnoses,
                                               List<TreatmentPlanResponse> treatments,
                                               Pet pet, String cover, List<PetTagResponse> tags) {
        return new MedicalDetailResponse(
                detail.getId(),
                detail.getRecordId(),
                detail.getCreateTime(),
                detail.getIsCompleted(),
                detail.getDescription(),
                detail.getHistory(),
                detail.getPastHistory(),
                detail.getLifeHabit(),
                detail.getWeight(),
                detail.getTemperature(),
                detail.getHeartRate(),
                detail.getRespiratoryRate(),
                detail.getPhysicalExam(),
                examinationDiagnoses,
                detail.getDiagnosis(),
                detail.getDifferential(),
                detail.getExam(),
                detail.getTreatment(),
                detail.getAdvice(),
                treatments,
                doctor.getId(),
                doctor.getUsername(),
                FileUtils.generateAssetUrl(PARENT_USER, doctor.getId(), doctor.getAvatar()),
                record.getPetAge(),
                pet.getId(),
                pet.getName(),
                pet.getSex(),
                pet.getType(),
                pet.getBreed(),
                tags,
                cover);
    }
}
