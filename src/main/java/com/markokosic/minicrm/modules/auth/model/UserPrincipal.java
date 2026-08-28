package com.markokosic.minicrm.modules.auth.model;

import com.markokosic.minicrm.modules.role.dto.Roles;
import com.markokosic.minicrm.modules.user.User;
import com.markokosic.minicrm.modules.user.model.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRoles().name()));
    }


    public Long getId(){return user.getId();}
    public String getEmail(){return user.getEmail();}

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    public Long getTenantId(){return user.getTenantId();}

    @Override
    public boolean isAccountNonExpired() {
//        return UserDetails.super.isAccountNonExpired();
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
//        return UserDetails.super.isAccountNonLocked();
        return true;
    }


    @Override
    public boolean isCredentialsNonExpired() {
//        return UserDetails.super.isCredentialsNonExpired();
        return true;

    }

    @Override
    public boolean isEnabled() {
        return  user.getStatus().equals(UserStatus.ACTIVE);
    }
}
