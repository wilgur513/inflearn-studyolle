package com.studyolle.settings;

import com.studyolle.account.AccountService;
import com.studyolle.account.CurrentUser;
import com.studyolle.domain.Account;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class SettingsController {

	private final String SETTINGS_PROFILE_VIEW_NAME = "settings/profile";
	private final String SETTINGS_PROFILE_URL = "/settings/profile";

	private final String SETTINGS_PASSWORD_VIEW_NAME = "settings/password";
	private final String SETTINGS_PASSWORD_VIEW_URL = "/settings/password";

	private final String SETTINGS_NOTIFICATIONS_VIEW_NAME = "settings/notifications";
	private final String SETTINGS_NOTIFICATIONS_VIEW_URL = "/settings/notifications";

	private final AccountService accountService;
	private final ModelMapper modelMapper;

	@InitBinder("passwordForm")
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(new PasswordFormValidator());
	}

	@GetMapping(SETTINGS_PROFILE_URL)
	public String profileUpdateForm(@CurrentUser Account account, Model model) {
		model.addAttribute(account);
		model.addAttribute(modelMapper.map(account, Profile.class));
		return SETTINGS_PROFILE_VIEW_NAME;
	}

	@PostMapping(SETTINGS_PROFILE_URL)
	public String profileUpdate(@CurrentUser Account account, @Valid Profile profile, Errors errors, Model model,
	                            RedirectAttributes attributes) {
		if (errors.hasErrors()) {
			model.addAttribute(account);
			return SETTINGS_PROFILE_VIEW_NAME;
		}

		accountService.updateProfile(account, profile);
		attributes.addFlashAttribute("message", "프로필 수정이 완료됐습니다.");
		return "redirect:" + SETTINGS_PROFILE_URL;
	}

	@GetMapping(SETTINGS_PASSWORD_VIEW_URL)
	public String passwordUpdateForm(@CurrentUser Account account, Model model) {
		model.addAttribute(account);
		model.addAttribute(new PasswordForm());
		return SETTINGS_PASSWORD_VIEW_NAME;
	}

	@PostMapping(SETTINGS_PASSWORD_VIEW_URL)
	public String passwordUpdate(@CurrentUser Account account, @Valid PasswordForm passwordForm, Errors errors,
	                             Model model, RedirectAttributes attributes) {
		if (errors.hasErrors()) {
			model.addAttribute(account);
			return SETTINGS_PASSWORD_VIEW_NAME;
		}
		accountService.updatePassword(account, passwordForm.getNewPassword());
		attributes.addFlashAttribute("message", "비밀번호 변경이 완료됐습니다.");
		return "redirect:" + SETTINGS_PASSWORD_VIEW_URL;
	}

	@GetMapping(SETTINGS_NOTIFICATIONS_VIEW_URL)
	public String notificationUpdateForm(@CurrentUser Account account, Model model) {
		model.addAttribute(account);
		model.addAttribute(modelMapper.map(account, Notifications.class));
		return SETTINGS_NOTIFICATIONS_VIEW_NAME;
	}

	@PostMapping(SETTINGS_NOTIFICATIONS_VIEW_URL)
	public String notificationUpdate(@CurrentUser Account account, @Valid Notifications notifications,
	                                 Errors errors, Model model, RedirectAttributes attributes) {
		if (errors.hasErrors()) {
			model.addAttribute(account);
			return SETTINGS_NOTIFICATIONS_VIEW_NAME;
		}

		accountService.updateNotifications(account, notifications);
		attributes.addFlashAttribute("message", "알림 설정 변경이 완료됐습니다.");
		return "redirect:" + SETTINGS_NOTIFICATIONS_VIEW_URL;
	}
}
