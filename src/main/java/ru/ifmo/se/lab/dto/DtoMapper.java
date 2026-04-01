package ru.ifmo.se.lab.dto;

import ru.ifmo.se.lab.dto.payment.PaymentDataDto;
import ru.ifmo.se.lab.dto.payment.YookassaPaymentDataDto;
import ru.ifmo.se.lab.model.Accommodation;
import ru.ifmo.se.lab.model.Booking;
import ru.ifmo.se.lab.model.BookingRequest;
import ru.ifmo.se.lab.model.PaymentData;
import ru.ifmo.se.lab.model.User;
import ru.ifmo.se.lab.model.YookassaPaymentData;

public class DtoMapper {
    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .login(user.getLogin())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User toEntity(UserDto userDto) {
        return User.builder()
                .login(userDto.getLogin())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public static AccommodationDto toDto(Accommodation accommodation) {
        return AccommodationDto.builder()
                .id(accommodation.getId())
                .hostId(accommodation.getHost().getId())
                .name(accommodation.getName())
                .description(accommodation.getDescription())
                .maxGuestsNumber(accommodation.getMaxGuestsNumber())
                .bedsCount(accommodation.getBedsCount())
                .address(accommodation.getAddress())
                .rating(accommodation.getRating())
                .pricePerNight(accommodation.getPricePerNight())
                .published(accommodation.isPublished())
                .build();
    }

    public static BookingDto toDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .accommodationId(booking.getAccommodation().getId())
                .userId(booking.getUser().getId())
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .price(booking.getPrice())
                .build();
    }

    public static BookingRequestDto toDto(BookingRequest bookingRequest) {
        return BookingRequestDto.builder()
                .id(bookingRequest.getId())
                .accommodationId(bookingRequest.getAccommodation().getId())
                .clientId(bookingRequest.getClient().getId())
                .hostId(bookingRequest.getHost().getId())
                .paymentDataId(bookingRequest.getPaymentData().getId())
                .checkIn(bookingRequest.getCheckIn())
                .checkOut(bookingRequest.getCheckOut())
                .messageToHost(bookingRequest.getMessageToHost())
                .build();
    }

    public static PaymentDataDto toDto(PaymentData paymentData) {
        if (paymentData instanceof YookassaPaymentData yookassaPaymentData) {
            return YookassaPaymentDataDto.builder()
                    .id(yookassaPaymentData.getId())
                    .userId(yookassaPaymentData.getUser().getId())
                    .paymentMethodName(yookassaPaymentData.getPaymentMethodName())
                    .yookassaPaymentId(yookassaPaymentData.getYookassaPaymentId())
                    .build();
        }

        return PaymentDataDto.builder()
                .id(paymentData.getId())
                .userId(paymentData.getUser().getId())
                .paymentMethodName(paymentData.getPaymentMethodName())
                .build();
    }

    public static PaymentDataDto toDto(YookassaPaymentData yookassaPaymentData) {
        return YookassaPaymentDataDto.builder()
                .id(yookassaPaymentData.getId())
                .userId(yookassaPaymentData.getUser().getId())
                .paymentMethodName(yookassaPaymentData.getPaymentMethodName())
                .yookassaPaymentId(yookassaPaymentData.getYookassaPaymentId())
                .build();
    }
}