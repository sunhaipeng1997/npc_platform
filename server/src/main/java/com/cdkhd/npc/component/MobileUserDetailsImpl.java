package com.cdkhd.npc.component;

import com.cdkhd.npc.entity.Area;
import com.cdkhd.npc.entity.Town;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class MobileUserDetailsImpl implements UserDetails {
    private final String uid;
    private final Set<String> roles;
    private final Set<GrantedAuthority> authorities;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final boolean enabled;
    private final Area area;
    private final Town town;

    public MobileUserDetailsImpl(String uid, Set<String> roles, Area area, Town town) {
        this(uid, roles, Collections.emptySet(), area, town);
    }

    public MobileUserDetailsImpl(String uid, Set<String> roles, Set<? extends GrantedAuthority> authorities, Area area, Town town) {
        this(uid, roles, authorities, true, true, true, true, area, town);
    }

    public MobileUserDetailsImpl(String uid, Set<String> roles, Set<? extends GrantedAuthority> authorities, boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired, boolean enabled, Area area, Town town) {
        this.uid = uid;
        this.roles = roles;
        this.authorities = (Set<GrantedAuthority>) authorities;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.enabled = enabled;
        this.area = area;
        this.town = town;
    }

    public String getUid() {
        return uid;
    }

    public Collection<String> getRoles() {
        return roles;
    }

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
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public Area getArea() {
        return area;
    }

    public Town getTown() {
        return town;
    }
}
