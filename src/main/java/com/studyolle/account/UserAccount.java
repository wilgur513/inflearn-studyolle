package com.studyolle.account;

import com.studyolle.domain.Account;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Set;

public class UserAccount extends User {
    private Account account;

    public UserAccount(Account account) {
        super(account.getNickname(), account.getPassword(), Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }
}
