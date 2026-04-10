package com.example.backend.event;

import com.example.backend.entity.Donation;
import com.example.backend.entity.DonationStatusUpdateRecord;

/**
 * 捐赠更新通知
 */
public record DonationStatusEvent(Donation donation, DonationStatusUpdateRecord record) {
}
