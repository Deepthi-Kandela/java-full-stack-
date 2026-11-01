package com.thinkmart.dto;

import jakarta.validation.constraints.Size;

public record CheckoutRequest(@Size(max = 500) String shippingAddress) {
}
