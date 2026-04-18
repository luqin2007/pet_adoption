package com.example.backend.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class VolunteerEventListener extends BaseEventListener {

    @TransactionalEventListener
    public void onShiftAdd(VolunteerShiftAddEvent event) {
        notify(event.data().getVolunteerId(), event);
        sendEmail(event.data().getVolunteerId(), event);
    }

    @TransactionalEventListener
    public void onShiftUpdate(VolunteerShiftUpdateEvent event) {
        notify(event.data().getVolunteerId(), event);
        sendEmail(event.data().getVolunteerId(), event);
    }

    @TransactionalEventListener
    public void onShiftStatus(VolunteerShiftStatusEvent event) {
        boolean reply = notifyReview(event);
        if (reply) {
            sendEmail(event.data().getVolunteerId(), event);
        }
    }

    @TransactionalEventListener
    public void onRecordAdd(VolunteerRecordAddEvent event) {
        notifyWorkers(event.data().getVolunteerId(), event);
    }

    @TransactionalEventListener
    public void onRecordStatus(VolunteerRecordStatusEvent event) {
        boolean reply = notifyReview(event);
        if (reply) {
            sendEmail(event.data().getVolunteerId(), event);
        }
    }
}
