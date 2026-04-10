package com.example.backend.event;

import com.example.backend.entity.Donation;
import com.example.backend.entity.DonationItem;

import java.util.List;

/**
 * 捐赠更新通知
 */
public record DonationUpdateEvent(Donation donation, List<DonationItem> items) {
}
