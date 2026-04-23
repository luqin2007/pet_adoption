package com.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.example.backend.entity.MedicalDetail;
import com.example.backend.entity.User;
import com.example.backend.entity.property.UserRole;
import com.example.backend.facade.AdoptBreadingFacade;
import com.example.backend.facade.ItemDonationFacade;
import com.example.backend.mapper.*;
import com.example.backend.util.CustomUserDetails;
import com.example.backend.util.RedisHelper;
import com.example.backend.util.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WorkflowBeginServiceUnitTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void workerCanBeginAgreementDraft() {
        login(user(1L, "worker", UserRole.WORKER.getMask()));
        RedisHelper redisHelper = mockRedisHelper();
        AdoptBreadingService service = new AdoptBreadingService(
                mock(BreadingMapper.class),
                mock(AgreementMapper.class),
                mock(AgreementFileMapper.class),
                mock(AgreementUpdateRecordMapper.class),
                mock(FollowTaskMapper.class),
                mock(FollowRecordMapper.class),
                mock(AdoptBreadingFacade.class),
                mock(LocationMapper.class));
        wireBase(service, redisHelper);
        ReflectionTestUtils.setField(service, "agreementTemplate", "agreement:%s");

        String uuid = service.beginAgreement();

        assertNotNull(uuid);
        verify(redisHelper).putString("agreement:" + uuid, "", 30L);
    }

    @Test
    void normalUserCannotBeginAgreementDraft() {
        login(user(2L, "normal", UserRole.NORMAL.getMask()));
        AdoptBreadingService service = new AdoptBreadingService(
                mock(BreadingMapper.class),
                mock(AgreementMapper.class),
                mock(AgreementFileMapper.class),
                mock(AgreementUpdateRecordMapper.class),
                mock(FollowTaskMapper.class),
                mock(FollowRecordMapper.class),
                mock(AdoptBreadingFacade.class),
                mock(LocationMapper.class));
        wireBase(service, mockRedisHelper());

        ServiceException ex = assertThrows(ServiceException.class, service::beginAgreement);

        assertEquals(ServiceException.E_AUTH, ex.getCode());
    }

    @Test
    void loggedInUserCanBeginDonationWithUserIdPayload() {
        login(user(12L, "donor", UserRole.DONOR.getMask()));
        RedisHelper redisHelper = mockRedisHelper();
        ItemDonationService service = new ItemDonationService(
                mock(DonationMapper.class),
                mock(DonationItemMapper.class),
                mock(DonationFileMapper.class),
                mock(DonationStatusUpdateMapper.class),
                mock(CategoryMapper.class),
                mock(StockMapper.class),
                mock(StockRecordMapper.class),
                mock(SubscribeMapper.class),
                mock(ItemDonationFacade.class));
        wireBase(service, redisHelper);
        ReflectionTestUtils.setField(service, "donationTemplate", "donation:%s");

        String uuid = service.beginDonation();

        assertNotNull(uuid);
        verify(redisHelper).putString("donation:" + uuid, "12", 30L);
    }

    @Test
    void loggedInUserCanBeginLostPetReport() {
        login(user(13L, "owner", UserRole.NORMAL.getMask()));
        RedisHelper redisHelper = mockRedisHelper();
        LostPetService service = new LostPetService(
                mock(LostPetClaimMapper.class),
                mock(LostPetMismatchMapper.class),
                mock(PetMapper.class),
                mock(LocationMapper.class));
        wireBase(service, redisHelper);
        ReflectionTestUtils.setField(service, "lostPetKey", "lost:%s");

        String uuid = service.beginLostPet();

        assertNotNull(uuid);
        verify(redisHelper).putString("lost:" + uuid, "", 30L);
    }

    @Test
    void doctorCanBeginExaminationForOpenMedicalDetail() {
        login(user(14L, "doctor", UserRole.DOCTOR.getMask()));
        RedisHelper redisHelper = mockRedisHelper();
        MedicalDetailMapper medicalDetailMapper = mock(MedicalDetailMapper.class);
        MedicalDetail detail = new MedicalDetail();
        detail.setId(88L);
        detail.setIsCompleted(false);
        detail.setIsDiscard(false);
        when(medicalDetailMapper.requireById(eq(88L), anyVararg())).thenReturn(detail);
        MedicalService service = medicalService();
        wireBase(service, redisHelper);
        ReflectionTestUtils.setField(service, "baseMapper", medicalDetailMapper);
        ReflectionTestUtils.setField(service, "uuidKeyTemplate", "exam:%s");

        String uuid = service.beginExamination(88L);

        assertNotNull(uuid);
        verify(redisHelper).putString("exam:" + uuid, "88", 30L);
    }

    @Test
    void nonDoctorCannotBeginExamination() {
        login(user(15L, "normal", UserRole.NORMAL.getMask()));
        MedicalService service = medicalService();
        wireBase(service, mockRedisHelper());

        ServiceException ex = assertThrows(ServiceException.class, () -> service.beginExamination(88L));

        assertEquals(ServiceException.E_AUTH, ex.getCode());
    }

    @Test
    void doctorCannotBeginExaminationForCompletedMedicalDetail() {
        login(user(16L, "doctor", UserRole.DOCTOR.getMask()));
        MedicalDetailMapper medicalDetailMapper = mock(MedicalDetailMapper.class);
        MedicalDetail detail = new MedicalDetail();
        detail.setId(88L);
        detail.setIsCompleted(true);
        detail.setIsDiscard(false);
        when(medicalDetailMapper.requireById(eq(88L), anyVararg())).thenReturn(detail);
        MedicalService service = medicalService();
        wireBase(service, mockRedisHelper());
        ReflectionTestUtils.setField(service, "baseMapper", medicalDetailMapper);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.beginExamination(88L));

        assertEquals(ServiceException.E_INVALIDATE, ex.getCode());
        assertEquals("exception.invalidate.medical_detail.completed", ex.getMessage());
    }

    @Test
    void doctorCannotBeginExaminationForDiscardedMedicalDetail() {
        login(user(17L, "doctor", UserRole.DOCTOR.getMask()));
        MedicalDetailMapper medicalDetailMapper = mock(MedicalDetailMapper.class);
        MedicalDetail detail = new MedicalDetail();
        detail.setId(88L);
        detail.setIsCompleted(false);
        detail.setIsDiscard(true);
        when(medicalDetailMapper.requireById(eq(88L), anyVararg())).thenReturn(detail);
        MedicalService service = medicalService();
        wireBase(service, mockRedisHelper());
        ReflectionTestUtils.setField(service, "baseMapper", medicalDetailMapper);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.beginExamination(88L));

        assertEquals(ServiceException.E_INVALIDATE, ex.getCode());
        assertEquals("exception.invalidate.medical_detail.discarded", ex.getMessage());
    }

    private static RedisHelper mockRedisHelper() {
        RedisHelper redisHelper = mock(RedisHelper.class);
        when(redisHelper.hasString(anyString(), anyString())).thenReturn(false);
        return redisHelper;
    }

    private static void wireBase(BaseService<?, ?> service, RedisHelper redisHelper) {
        service.setObjects(redisHelper, mock(), mock(), mock(), mock());
        ReflectionTestUtils.setField(service, "keyTimeout", 30L);
    }

    @SuppressWarnings("unchecked")
    private static SFunction<MedicalDetail, ?>[] anyVararg() {
        return any(SFunction[].class);
    }

    private static MedicalService medicalService() {
        return new MedicalService(
                mock(FirstRegistrationMapper.class),
                mock(AllergyHistoryMapper.class),
                mock(ImmunityHistoryMapper.class),
                mock(MedicalRecordMapper.class),
                mock(DiagnosisMapper.class),
                mock(ExaminationMapper.class),
                mock(ExaminationFileMapper.class),
                mock(ExaminationDiagnosisMapper.class),
                mock(TreatmentPlanMapper.class),
                mock(OrderMapper.class),
                mock(ItemMapper.class),
                mock(VaccineMapper.class),
                mock(VaccineRecordMapper.class),
                mock(DewormerMapper.class),
                mock(DewormRecordMapper.class),
                mock(RehabPlanMapper.class),
                mock(RehabPlanStatusMapper.class),
                mock(RehabRecordMapper.class),
                mock(HealthAssessmentMapper.class));
    }

    private static void login(User user) {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken(new CustomUserDetails(user), null));
    }

    private static User user(Long id, String username, int role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword("{noop}pwd");
        user.setEmail(username + "@example.com");
        user.setRole(role);
        return user;
    }
}
