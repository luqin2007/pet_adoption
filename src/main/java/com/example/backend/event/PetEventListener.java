package com.example.backend.event;

import com.example.backend.entity.Location;
import com.example.backend.entity.LostPet;
import com.example.backend.entity.Pet;
import com.example.backend.entity.PetStatusRecord;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.mapper.PetLocationMapper;
import com.example.backend.mapper.PetMapper;
import com.example.backend.service.LostPetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Component
@RequiredArgsConstructor
public class PetEventListener extends BaseEventListener {

    private final PetMapper petMapper;
    private final PetLocationMapper petLocationMapper;
    private final LostPetService lostPetService;

    @TransactionalEventListener
    public void onPetAdd(PetAddEvent event) {
        notifyWorkers(event.user().getId(), event);
        comparePet(event.data(), event.location());
    }

    @TransactionalEventListener
    public void onPetStatus(PetStatusChangeEvent event) {
        PetStatusRecord record = event.data();
        String petName = petMapper.requireById(record.getPetId(), Pet::getName).getName("");
        notifyWorkers(null, event, petName);
    }

    @TransactionalEventListener
    public void onPetUpdate(PetUpdateEvent event) {
        Location location = petLocationMapper.queryByPet(event.data().getId())
                .desc(Location::getCreateTime)
                .one();
        comparePet(event.data(), location);
    }

    @TransactionalEventListener
    public void onPetLocation(PetLocationEvent event) {
        Pet pet = petMapper.requireById(event.data().getParentId());
        comparePet(pet, event.data());
    }

    private void comparePet(Pet pet, Location location) {
        List<LostPet> pets = lostPetService.listMatchedLostPets(pet, location);
        if (!pets.isEmpty()) {
            Set<Long> ownerIds = pets.stream()
                    .map(LostPet::getOwnerId)
                    .collect(Collectors.toSet());
            String title = langHelper.get("notification.lost_pet_match.title");
            String content = langHelper.get("notification.lost_pet_match.content", pets.size());
            notify(null, ownerIds, NoticeSource.PET_RECORD, title, content);
        }
    }
}
