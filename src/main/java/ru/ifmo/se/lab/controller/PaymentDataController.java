package ru.ifmo.se.lab.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import ru.ifmo.se.lab.dto.PageResponse;
import ru.ifmo.se.lab.dto.payment.PaymentDataDto;
import ru.ifmo.se.lab.security.SecurityUtils;
import ru.ifmo.se.lab.service.PaymentDataCrudService;

@RestController
@RequestMapping("/payment-data")
@RequiredArgsConstructor
@Validated
public class PaymentDataController {

    private final PaymentDataCrudService paymentDataCrudService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public PageResponse<PaymentDataDto> getPaymentData(
            @RequestParam(value = "page", defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(value = "size", defaultValue = "10") @Positive int size,
            @RequestParam(value = "sort_by", defaultValue = "id") String sortBy,
            @RequestParam(value = "sort_dir", defaultValue = "asc") String sortDir) {
        return paymentDataCrudService.findPaymentData(SecurityUtils.getCurrentPrincipal(), page, size, sortBy, sortDir);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public PaymentDataDto getPaymentDataById(@PathVariable @Positive int id) {
        return paymentDataCrudService.findPaymentDataById(SecurityUtils.getCurrentPrincipal(), id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public PaymentDataDto createPaymentData(@RequestBody @Valid PaymentDataDto paymentDataDto) {
        return paymentDataCrudService.addPaymentData(SecurityUtils.getCurrentPrincipal(), paymentDataDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public PaymentDataDto updatePaymentData(
            @PathVariable @Positive int id,
            @RequestBody @Valid PaymentDataDto paymentDataDto) {
        return paymentDataCrudService.updatePaymentData(SecurityUtils.getCurrentPrincipal(), id, paymentDataDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deletePaymentData(@PathVariable @Positive int id) {
        paymentDataCrudService.deletePaymentData(SecurityUtils.getCurrentPrincipal(), id);
    }
}