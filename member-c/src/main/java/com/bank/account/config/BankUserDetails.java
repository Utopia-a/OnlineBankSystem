package com.bank.account.config;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * 自定义 UserDetails，携带 userId，供 Controller 使用
 */
@Getter
public class BankUserDetails extends User {

    private final Long userId;

    public BankUserDetails(Long userId, String username,
                            Collection<? extends GrantedAuthority> authorities) {
        super(username, "", authorities);
        this.userId = userId;
    }
}
