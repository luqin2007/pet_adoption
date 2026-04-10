package com.example.backend.event;

import com.example.backend.entity.Donation;
import com.example.backend.entity.DonationItem;

import java.util.List;

/**
 * 捐赠
 */
public record DonationAddEvent(Donation donation, List<DonationItem> items) {
}
