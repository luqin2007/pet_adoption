package com.example.backend.event;

import com.example.backend.entity.LostPet;
import com.example.backend.entity.LostPetClaim;
import com.example.backend.mapper.LostPetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@SuppressWarnings("unchecked")
@Component
@RequiredArgsConstructor
public class LostPetEventListener extends BaseEventListener {

    private final LostPetMapper lostPetMapper;

    @TransactionalEventListener
    public void onClaimAdd(LostPetClaimAddEvent event) {
        LostPetClaim claim = event.data();
        LostPet lostPet = lostPetMapper.requireById(claim.getLostPetId(),
                LostPet::getName, LostPet::getOwnerId);
        notifyWorkers(claim.getApplicantId(), event, lostPet.getName());
    }

    @TransactionalEventListener
    public void onClaimApprove(LostPetClaimApproveEvent event) {
        LostPetClaim claim = event.data();
        LostPet lostPet = lostPetMapper.requireById(claim.getLostPetId(), LostPet::getName);
        notify(claim.getApplicantId(), event, lostPet.getName());
        sendEmail(claim.getApplicantId(), event, lostPet.getName());
    }
}
