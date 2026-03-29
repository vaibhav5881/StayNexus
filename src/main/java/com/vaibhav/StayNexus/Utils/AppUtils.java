package com.vaibhav.StayNexus.Utils;

import com.vaibhav.StayNexus.Entities.UserEntity;
import org.springframework.security.core.context.SecurityContextHolder;

public class AppUtils {

    public static UserEntity getCurrentUser() {
        return (UserEntity) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
