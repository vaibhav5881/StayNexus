package com.vaibhav.StayNexus.Service.Interfaces;

import com.vaibhav.StayNexus.Entities.BookingEntity;

public interface CheckoutService {

    String getCheckoutSession(BookingEntity booking,
                              String successUrl,
                              String failureUrl);
}
