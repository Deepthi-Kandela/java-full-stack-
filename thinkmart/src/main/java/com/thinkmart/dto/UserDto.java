package com.thinkmart.dto;

import com.thinkmart.model.User;

public record UserDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String state,
        String address,
        String role,
        String profilePic) {

    public static UserDto from(User u) {
        return new UserDto(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getPhone(),
                u.getState(), u.getAddress(), u.getRole().name(), u.getProfilePic());
    }
}
