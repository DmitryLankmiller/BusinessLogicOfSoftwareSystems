package ru.ifmo.se.lab.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.ifmo.se.lab.dto.DtoMapper;
import ru.ifmo.se.lab.dto.PageResponse;
import ru.ifmo.se.lab.dto.payment.PaymentDataDto;
import ru.ifmo.se.lab.dto.payment.YookassaPaymentDataDto;
import ru.ifmo.se.lab.exception.ResourceNotFoundException;
import ru.ifmo.se.lab.model.PaymentData;
import ru.ifmo.se.lab.model.User;
import ru.ifmo.se.lab.model.YookassaPaymentData;
import ru.ifmo.se.lab.repository.PaymentDataRepository;
import ru.ifmo.se.lab.repository.UserRepository;
import ru.ifmo.se.lab.security.AppPrincipal;
import ru.ifmo.se.lab.security.AppRole;

@Service
@RequiredArgsConstructor
public class PaymentDataCrudService {

    private final PaymentDataRepository paymentDataRepository;
    private final UserRepository userRepository;

    public PageResponse<PaymentDataDto> findPaymentData(AppPrincipal principal, int page, int size, String sortBy,
            String sortDir) {
        requireAdmin(principal);

        Sort sort = buildSort(sortBy, sortDir);
        Page<PaymentData> paymentData = paymentDataRepository.findAll(PageRequest.of(page, size, sort));
        return buildPaymentDataPageResponse(paymentData);
    }

    public PaymentDataDto findPaymentDataById(AppPrincipal principal, int id) {
        requireAdmin(principal);

        PaymentData paymentData = paymentDataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentData with id=%d not found".formatted(id)));

        return DtoMapper.toDto(paymentData);
    }

    public PaymentDataDto addPaymentData(AppPrincipal principal, PaymentDataDto paymentDataDto) {
        requireAdmin(principal);

        User user = userRepository.findById(paymentDataDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id=%d not found".formatted(paymentDataDto.getUserId())));

        PaymentData paymentData = toEntity(paymentDataDto, user);
        PaymentData savedPaymentData = paymentDataRepository.save(paymentData);
        return DtoMapper.toDto(savedPaymentData);
    }

    public PaymentDataDto updatePaymentData(AppPrincipal principal, int id, PaymentDataDto paymentDataDto) {
        requireAdmin(principal);

        PaymentData paymentData = paymentDataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentData with id=%d not found".formatted(id)));

        User user = userRepository.findById(paymentDataDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id=%d not found".formatted(paymentDataDto.getUserId())));

        paymentData.setUser(user);
        paymentData.setPaymentMethodName(paymentDataDto.getPaymentMethodName());

        if (paymentData instanceof YookassaPaymentData yookassaPaymentData
                && paymentDataDto instanceof YookassaPaymentDataDto yookassaPaymentDataDto) {
            yookassaPaymentData.setYookassaPaymentId(yookassaPaymentDataDto.getYookassaPaymentId());
        }

        PaymentData savedPaymentData = paymentDataRepository.save(paymentData);
        return DtoMapper.toDto(savedPaymentData);
    }

    public void deletePaymentData(AppPrincipal principal, int id) {
        requireAdmin(principal);

        PaymentData paymentData = paymentDataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentData with id=%d not found".formatted(id)));

        paymentDataRepository.delete(paymentData);
    }

    private void requireAdmin(AppPrincipal principal) {
        if (principal.getRole() != AppRole.ADMIN) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private PageResponse<PaymentDataDto> buildPaymentDataPageResponse(Page<PaymentData> paymentData) {
        List<PaymentDataDto> content = paymentData.getContent().stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());

        PageResponse<PaymentDataDto> response = new PageResponse<>();
        response.setContent(content);
        response.setPage(paymentData.getNumber());
        response.setSize(paymentData.getSize());
        response.setTotalElements(paymentData.getTotalElements());
        response.setTotalPages(paymentData.getTotalPages());
        response.setHasNext(paymentData.hasNext());
        return response;
    }

    private Sort buildSort(String sortBy, String sortDir) {
        return sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
    }

    private PaymentData toEntity(PaymentDataDto paymentDataDto, User user) {
        if (paymentDataDto instanceof YookassaPaymentDataDto yookassaPaymentDataDto) {
            return YookassaPaymentData.builder()
                    .user(user)
                    .paymentMethodName(paymentDataDto.getPaymentMethodName())
                    .yookassaPaymentId(yookassaPaymentDataDto.getYookassaPaymentId())
                    .build();
        }

        return PaymentData.builder()
                .user(user)
                .paymentMethodName(paymentDataDto.getPaymentMethodName())
                .build();
    }
}