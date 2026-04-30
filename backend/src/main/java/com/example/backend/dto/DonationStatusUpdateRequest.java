package com.example.backend.dto;

import com.example.backend.entity.Donation;
import com.example.backend.entity.DonationStatusUpdateRecord;
import com.example.backend.entity.property.DonationStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.Errors;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class DonationStatusUpdateRequest extends StatusUpdateRequest {

    /**
     * Donation: id, status
     */
    public DonationStatusUpdateRecord create(Donation donation, Long userId) {
        return new DonationStatusUpdateRecord(null,
                donation.getId(),
                userId,
                donation.getStatus(),
                DonationStatus.get(status),
                reason,
                new Date());
    }

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, DonationStatusUpdateRequest::getStatus, DonationStatus.class, "request.item_donation.donation.status");
    }
}
