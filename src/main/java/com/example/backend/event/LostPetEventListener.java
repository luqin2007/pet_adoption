package com.example.backend.event;

import com.example.backend.entity.Location;
import com.example.backend.entity.LostPet;
import com.example.backend.entity.LostPetClaim;
import com.example.backend.entity.Pet;
import com.example.backend.entity.property.LostPetStatus;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.mapper.LostPetLocationMapper;
import com.example.backend.mapper.LostPetMapper;
import com.example.backend.mapper.PetLocationMapper;
import com.example.backend.mapper.PetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Component
@RequiredArgsConstructor
public class LostPetEventListener extends BaseEventListener {

    private final LostPetMapper lostPetMapper;
    private final PetMapper petMapper;
    private final PetLocationMapper petLocationMapper;
    private final LostPetLocationMapper lostPetLocationMapper;

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

    @Async
    @TransactionalEventListener
    public void onLostPetAdd(LostPetAddEvent event) {
        compareLostPet(event.data(), event.location());
    }

    @Async
    @TransactionalEventListener
    public void onLostPetUpdate(LostPetUpdateEvent event) {
        compareLostPet(event.data(), event.location());
    }

    private void compareLostPet(LostPet lostPet, Location location) {
        if (lostPet.getStatus() == LostPetStatus.SEARCHING) {
            Set<Long> petIds = petLocationMapper.queryByLocation(location, lostPet.getLostTime())
                    .list(Location::getParentId)
                    .collect(Collectors.toSet());
            List<Pet> pets = petMapper.selectList(petIds).stream()
                    .filter(pet -> !Boolean.TRUE.equals(pet.getIsDiscard()) && pet.getStatus().isAdoptable())
                    .filter(pet -> pet.matchPet(lostPet))
                    .toList();
            if (!pets.isEmpty()) {
                String title = langHelper.get("notification.lost_pet_match.title");
                String content = langHelper.get("notification.lost_pet_match.content", pets.size(), lostPet.getName());
                notify(null, List.of(lostPet.getOwnerId()), NoticeSource.PET_RECORD, title, content);
            }
        }
    }
}
