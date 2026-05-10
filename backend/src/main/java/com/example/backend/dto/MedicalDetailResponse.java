package com.example.backend.dto;

import com.example.backend.entity.MedicalDetail;
import com.example.backend.entity.MedicalRecord;
import com.example.backend.entity.Pet;
import com.example.backend.entity.User;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class MedicalDetailResponse implements IResponse {

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
    private BigDecimal weight;
    private BigDecimal temperature;
    private Integer heartRate;
    private Integer respiratoryRate;
    private String physicalExam;
    private List<DiagnosisResponse> objectiveDiagnoses;

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
     * MedicalRecord: petId, petAge<br>
     * User: id, username, avatar
     */
    public static MedicalDetailResponse create(MedicalDetail detail, MedicalRecord record, User doctor,
                                               List<DiagnosisResponse> diagnoses,
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
                diagnoses,
                detail.getDiagnosis(),
                detail.getDifferential(),
                detail.getExam(),
                detail.getTreatment(),
                detail.getAdvice(),
                treatments,
                doctor.getId(),
                doctor.getUsername(),
                FileUtils.generateAssetUrl(ParentType.USER, doctor.getId(), doctor.getAvatar()),
                record.getPetAge(),
                pet.getId(),
                pet.getName(),
                pet.getSex(),
                pet.getType(),
                pet.getBreed(),
                tags,
                cover);
    }

    /**
     * Pet: id, name, sex, type, breed<br>
     * MedicalRecord: petId, petAge<br>
     * User: id, username, avatar<br>
     * <br>
     * records: MedicalDetail.id<br>
     * doctors: MedicalDetail.doctorId<br>
     * diagnoses: MedicalDetail.id<br>
     * treatments: MedicalDetail.id<br>
     * pets: MedicalRecord.petId<br>
     * covers: MedicalRecord.petId<br>
     * tags: MedicalRecord.petId
     */
    public static MedicalDetailResponse createBatch(MedicalDetail detail,
                                                    Map<Long, MedicalRecord> records,
                                                    Map<Long, User> doctors,
                                                    Map<Long, List<DiagnosisResponse>> diagnoses,
                                                    Map<Long, List<TreatmentPlanResponse>> treatments,
                                                    Map<Long, Pet> pets, Map<Long, String> covers,
                                                    Map<Long, List<PetTagResponse>> tags) {
        MedicalRecord record = records.get(detail.getRecordId());
        return create(detail, record,
                doctors.get(detail.getDoctorId()),
                diagnoses.get(detail.getId()),
                treatments.get(detail.getId()),
                pets.get(record.getPetId()),
                covers.get(record.getPetId()),
                tags.get(record.getPetId()));
    }
}
