package com.studyolle.account;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.studyolle.domain.Account;

import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AccountController {
	private final SignUpFormValidator signUpFormValidator;
	private final AccountService accountService;
	private final AccountRepository accountRepository;

	@InitBinder("signUpForm")
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(signUpFormValidator);
	}

	@GetMapping("/sign-up")
	public String signUpForm(Model model) {
		model.addAttribute(new SignUpForm());
		return "account/sign-up";
	}

	@PostMapping("/sign-up")
	public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
		if (errors.hasErrors()) {
			return "account/sign-up";
		}

		Account account = accountService.processNewAccount(signUpForm);
		accountService.login(account);
		return "redirect:/";
	}

	@GetMapping("/check-email")
	public String checkEmail(@CurrentUser Account account, Model model) {
		model.addAttribute("nickname", account.getNickname());
		model.addAttribute("email", account.getEmail());
		return "account/check-email";
	}

	@GetMapping("/resend-confirm-email")
	public String resendConfirmEmail(@CurrentUser Account account, Model model) {
		if (account.canResendEmail()) {
			accountService.sendSignUpConfirmEmail(account);
			return "redirect:/";
		}
		model.addAttribute("error", "wrong");
		return "account/check-email";
	}

	@GetMapping("/check-email-token")
	public String checkEmailToken(String token, String email, Model model) {
		String view = "account/checked-email";
		Account account = accountRepository.findByEmail(email);

		if (account == null || !account.isValidToken(token)) {
			model.addAttribute("error", "wrong");
			return view;
		}

		accountService.completeSignUp(account);
		model.addAttribute("numberOfUser", accountRepository.count());
		model.addAttribute("nickname", account.getNickname());
		return view;
	}

	@GetMapping("/profile/{nickname}")
	public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser Account account) {
		Account byNickname = accountRepository.findByNickname(nickname);
		if (byNickname == null) {
			throw new IllegalArgumentException(nickname + "??? ???????????? ???????????? ????????????.");
		}

		model.addAttribute("account", byNickname);
		model.addAttribute("isOwner", account.equals(byNickname));
		return "account/profile";
	}

	@GetMapping("/email-login")
	public String emailLoginForm(Model model) {
		return "account/email-login";
	}

	@PostMapping("/email-login")
	public String emailLogin(String email, RedirectAttributes redirectAttributes, Model model) {
		Account account = accountRepository.findByEmail(email);

		if (account == null) {
			redirectAttributes.addFlashAttribute("error", "????????? ????????? ????????? ????????????.");
			return "redirect:/email-login";
		}

		if (!account.canResendEmail()) {
			redirectAttributes.addFlashAttribute("error", "???????????? 1?????? ?????? ?????? ??? ????????????.");
			return "redirect:/email-login";
		}

		accountService.sendLoginLink(account);
		redirectAttributes.addFlashAttribute("message", "????????? ?????? ????????? ??????????????????.");
		return "redirect:/email-login";
	}

	@GetMapping("/login-by-email")
	public String loginByEmail(String token, String email, Model model) {
		Account account = accountRepository.findByEmail(email);

		if (account == null || !account.isValidToken(token)) {
			model.addAttribute("error", "???????????? ??? ????????????.");
			return "account/logged-in-by-email";
		}

		accountService.login(account);
		return "account/logged-in-by-email";
	}
}
