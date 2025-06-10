package com.ipaam.ai.transfer.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class KeycloakUserDetails implements UserDetails {

    private String username;
    private String nationalCode;
    private String birthdate;
    private Collection<? extends GrantedAuthority> authorities;

    public KeycloakUserDetails(String username, String nationalCode, String birthdate, Collection<GrantedAuthority> authorities) {
    }

    // Constructor, getters, and other UserDetails methods

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getNationalCode() {
        return nationalCode;
    }

    public String getBirthdate() {
        return birthdate;
    }


}
