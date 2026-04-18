package com.example.backend.event;

import com.example.backend.entity.Pet;
import com.example.backend.entity.PetStatusRecord;
import com.example.backend.mapper.PetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@SuppressWarnings("unchecked")
@Component
@RequiredArgsConstructor
public class PetEventListener extends BaseEventListener {

    private final PetMapper petMapper;

    @TransactionalEventListener
    public void onPetAdd(PetAddEvent event) {
        notifyWorkers(event.user().getId(), event);
    }

    @TransactionalEventListener
    public void onPetLocation(PetLocationEvent event) {
        // TODO 走失认领
    }

    @TransactionalEventListener
    public void onPetStatus(PetStatusChangeEvent event) {
        PetStatusRecord record = event.data();
        String petName = petMapper.requireById(record.getPetId(), Pet::getName).getName("");
        notifyWorkers(null, event, petName);
    }

    @TransactionalEventListener
    public void onPetUpdate(PetUpdateEvent event) {
        // TODO 走失认领
    }
}
