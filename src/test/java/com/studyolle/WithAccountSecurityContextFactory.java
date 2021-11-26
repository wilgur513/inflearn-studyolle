package com.studyolle;

import com.studyolle.account.AccountRepository;
import com.studyolle.account.AccountService;
import com.studyolle.account.UserAccount;
import com.studyolle.domain.Account;
import java.lang.annotation.Annotation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

@RequiredArgsConstructor
public class WithAccountSecurityContextFactory implements WithSecurityContextFactory<WithAccount> {
	private final AccountRepository accountRepository;

	@Override
	public SecurityContext createSecurityContext(WithAccount annotation) {
		String nickname = annotation.value();
		Account account = Account.builder()
			.nickname(nickname)
			.email(nickname + "@email.com")
			.password("password")
			.build();
		accountRepository.save(account);

		UserDetails principle = new UserAccount(account);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(new UsernamePasswordAuthenticationToken(principle, principle.getPassword(),
			principle.getAuthorities()));
		return context;
	}
}
