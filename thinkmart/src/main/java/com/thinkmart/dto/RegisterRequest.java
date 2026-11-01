package com.thinkmart.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Bound from a multipart/form-data request (so a profile picture can be uploaded). */
@Getter
@Setter
public class RegisterRequest {

    @NotBlank @Size(min = 2, max = 30)
    private String firstName;

    @NotBlank @Size(min = 2, max = 30)
    private String lastName;

    @NotBlank @Email @Size(max = 120)
    private String email;

    @NotBlank
    @Pattern(regexp = "[6-9][0-9]{9}", message = "must be 10 digits starting with 6, 7, 8 or 9")
    private String phone;

    @NotBlank @Size(min = 6, max = 50)
    private String password;

    @NotNull @Past
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dob;

    @NotBlank
    private String gender;

    @NotBlank
    private String state;

    @NotBlank @Size(max = 500)
    private String address;

    /** CUSTOMER or VENDOR (case-insensitive). */
    @NotBlank
    private String role;

    /** Optional profile picture (.png / .jpg / .jpeg, max 2 MB). */
    private MultipartFile pic;
}
