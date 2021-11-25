package com.studyolle.account;

import java.util.Set;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.studyolle.domain.Account;

public class UserAccount extends User {
	private final Account account;

	public UserAccount(Account account) {
		super(account.getNickname(), account.getPassword(), Set.of(new SimpleGrantedAuthority("ROLE_USER")));
		this.account = account;
	}

	public Account getAccount() {
		return account;
	}
}
