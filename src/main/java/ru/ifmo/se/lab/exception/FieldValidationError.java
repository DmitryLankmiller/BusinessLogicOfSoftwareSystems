package ru.ifmo.se.lab.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FieldValidationError {
    private String field;
    private Object rejectedValue;
    private String message;
}