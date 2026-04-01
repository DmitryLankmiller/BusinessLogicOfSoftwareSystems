package ru.ifmo.se.lab.dto.payment;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", defaultImpl = YookassaInputInfo.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = YookassaInputInfo.class, name = "yookassa")
})
public interface PaymentMethodInputInfo {
}