package com.studyolle.settings;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PasswordFormValidator implements Validator {
	@Override
	public boolean supports(Class<?> clazz) {
		return PasswordForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		PasswordForm form = (PasswordForm) target;
		if (!form.getNewPassword().equals(form.getNewPasswordConfirm())) {
			errors.rejectValue("newPassword", "wrong.value", "입력한 새 패스워드가 일치하지 않습니다.");
		}
	}
}
