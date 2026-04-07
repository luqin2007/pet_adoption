package com.example.backend.dto;

import com.example.backend.entity.Donation;
import com.example.backend.entity.DonationStatusUpdateRecord;
import com.example.backend.entity.property.DonationStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;

@Data
public class DonationStatusUpdateRequest implements IRequest, IRequestValidate {

    @NotBlank(message = "request.item_donation.donation.status")
    private String status;

    @NotBlank(message = "request.item_donation.donation.status_reason")
    private String reason;

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
