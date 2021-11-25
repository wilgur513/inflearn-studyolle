package com.studyolle.account;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {
	private final AccountRepository accountRepository;

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.isAssignableFrom(SignUpForm.class);
	}

	@Override
	public void validate(Object target, Errors errors) {
		SignUpForm form = (SignUpForm) target;

		if (accountRepository.existsByEmail(form.getEmail())) {
			errors.rejectValue("email", "invalid.email", "중복된 이메일이 존재합니다.");
		}

		if (accountRepository.existsByNickname(form.getNickname())) {
			errors.rejectValue("nickname", "invalid.nickname", "중복된 닉네임이 존재합니다.");
		}
	}
}
