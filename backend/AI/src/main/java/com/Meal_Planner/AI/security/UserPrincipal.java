package com.Meal_Planner.AI.security;

import com.Meal_Planner.AI.entity.User;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UserPrincipal implements UserDetails {

    @Getter
    private final UUID id;
    @Getter
    private final String name;
    private final String email;
    private final String password;


    public UserPrincipal(User user){
        this.id=user.getId();
        this.email=user.getEmail();
        this.password=user.getPassword();
        this.name=user.getName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
