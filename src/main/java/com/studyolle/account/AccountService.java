package com.studyolle.account;

import com.studyolle.settings.Profile;
import java.util.Set;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studyolle.domain.Account;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {
	private final AccountRepository accountRepository;
	private final JavaMailSender javaMailSender;
	private final PasswordEncoder passwordEncoder;


	public Account processNewAccount(SignUpForm signUpForm) {
		Account newAccount = saveNewAccount(signUpForm);
		newAccount.generateEmailCheckToken();
		sendSignUpConfirmEmail(newAccount);
		return newAccount;
	}

	public void completeSignUp(Account account) {
		account.completeSignUp();
		login(account);
	}

	private Account saveNewAccount(SignUpForm signUpForm) {
		Account account = Account.builder()
			.email(signUpForm.getEmail())
			.nickname(signUpForm.getNickname())
			.password(passwordEncoder.encode(signUpForm.getPassword()))
			.studyCreatedByWeb(true)
			.studyEnrollmentResultByWeb(true)
			.studyUpdatedByWeb(true)
			.build();
		return accountRepository.save(account);
	}

	public void sendSignUpConfirmEmail(Account account) {
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setSubject("스터디올래, 회원가입 인증!");
		mailMessage
			.setText("/check-email-token?token=" + account.getEmailCheckToken() + "&email=" + account.getEmail());
		javaMailSender.send(mailMessage);
	}

	public void login(Account account) {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
			new UserAccount(account), account.getPassword(), Set.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		SecurityContextHolder.getContext().setAuthentication(token);
	}

	@Transactional(readOnly = true)
	@Override
	public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
		Account account = accountRepository.findByEmail(emailOrNickname);

		if (account == null) {
			account = accountRepository.findByNickname(emailOrNickname);
		}

		if (account == null) {
			throw new UsernameNotFoundException(emailOrNickname);
		}

		return new UserAccount(account);
	}

	public void updateProfile(Account account, Profile profile) {
		account.setUrl(profile.getUrl());
		account.setBio(profile.getBio());
		account.setLocation(profile.getLocation());
		account.setOccupation(profile.getOccupation());
		account.setProfileImage(profile.getProfileImage());
		accountRepository.save(account);
	}
}
