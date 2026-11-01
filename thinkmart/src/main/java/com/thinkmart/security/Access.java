package com.thinkmart.security;

import org.springframework.http.HttpStatus;

import com.thinkmart.exception.ApiException;
import com.thinkmart.model.Role;
import com.thinkmart.model.User;

/** Tiny helper for role checks inside controllers / services. */
public final class Access {

    private Access() {
    }

    public static void requireRole(User user, Role... allowed) {
        for (Role r : allowed) {
            if (user.getRole() == r) {
                return;
            }
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to do that");
    }
}
