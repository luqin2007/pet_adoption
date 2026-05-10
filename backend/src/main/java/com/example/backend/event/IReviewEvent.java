package com.example.backend.event;

public interface IReviewEvent<T> extends INotifyEvent<T> {

    Long applicantId();

    Long reviewerId();
}
