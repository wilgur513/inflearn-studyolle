package com.studyolle.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.studyolle.account.AccountRepository;
import com.studyolle.account.UserAccount;
import com.studyolle.domain.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SettingsControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	AccountRepository accountRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	Account account;

	@BeforeEach
	void setUp() {
		account = Account.builder()
			.nickname("nickname")
			.email("email@email.com")
			.password("password")
			.build();
		accountRepository.save(account);
	}

	@Test
	@DisplayName("프로필 업데이트 폼 페이지")
	void profileUpdateForm() throws Exception {
		mockMvc.perform(get("/settings/profile")
			.with(user(new UserAccount(account)))
		)
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("account"))
			.andExpect(model().attributeExists("profile"))
			.andExpect(view().name("settings/profile"));
	}

	@Test
	@DisplayName("익명 사용자 프로필 업데이트 폼 접근 제한")
	void anonymousProfileUpdateForm() throws Exception {
		mockMvc.perform(get("/settings/profile"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrlPattern("**/login"));
	}

	@Test
	@DisplayName("프로필 정상적인 업데이트")
	void profileUpdate() throws Exception {
		mockMvc.perform(post("/settings/profile")
			.with(csrf())
			.with(user(new UserAccount(account)))
			.param("bio", "new bio")
			.param("location", "new location")
			.param("occupation", "new occupation")
			.param("url", "new url")
			.param("profileImage", "new profileImage")
		)
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/settings/profile"))
			.andExpect(flash().attributeExists("message"));

		Account updatedAccount = accountRepository.findByNickname("nickname");
		assertThat(updatedAccount.getBio()).isEqualTo("new bio");
		assertThat(updatedAccount.getUrl()).isEqualTo("new url");
		assertThat(updatedAccount.getLocation()).isEqualTo("new location");
		assertThat(updatedAccount.getOccupation()).isEqualTo("new occupation");
		assertThat(updatedAccount.getProfileImage()).isEqualTo("new profileImage");
	}

	@Test
	@DisplayName("프로필 업데이트 실패")
	void profileUpdateFail() throws Exception {
		String bio = "36자 이상으로 길게 작성된 소개글 36자 이상으로 길게 작성된 소개글 36자 이상으로 길게 작성된 소개글 36자 이상으로 길게 작성된 소개글";
		mockMvc.perform(post("/settings/profile")
			.with(csrf())
			.with(user(new UserAccount(account)))
			.param("bio", bio)
		)
			.andExpect(status().isOk())
			.andExpect(view().name("settings/profile"))
			.andExpect(model().attributeExists("account"))
			.andExpect(model().attributeExists("profile"))
			.andExpect(model().hasErrors());

		Account notUpdatedAccount = accountRepository.findByNickname("nickname");
		assertThat(notUpdatedAccount.getBio()).isBlank();
	}

	@Test
	@DisplayName("패스워드 변경 폼 페이지")
	void updatePasswordForm() throws Exception {
	    mockMvc.perform(get("/settings/password")
	        .with(user(new UserAccount(account)))
	    )
		    .andExpect(status().isOk())
		    .andExpect(model().attributeExists("account"))
		    .andExpect(model().attributeExists("passwordForm"))
		    .andExpect(view().name("settings/password"));
	}

	@Test
	@DisplayName("정상적인 패스워드 변경")
	void updatePassword() throws Exception {
	    mockMvc.perform(post("/settings/password")
	        .with(user(new UserAccount(account)))
		    .with(csrf())
		    .param("newPassword", "newPassword")
		    .param("newPasswordConfirm", "newPassword")
	    )
		    .andExpect(status().is3xxRedirection())
		    .andExpect(redirectedUrl("/settings/password"))
		    .andExpect(flash().attributeExists("message"));

	    Account updatedAccount = accountRepository.findByNickname("nickname");
	    assertThat(passwordEncoder.matches("newPassword", updatedAccount.getPassword())).isTrue();
	}

	@Test
	@DisplayName("잘못된 패스워드 변경")
	void updatePasswordFail() throws Exception {
	    mockMvc.perform(post("/settings/password")
	        .with(user(new UserAccount(account)))
		    .with(csrf())
		    .param("newPassword", "12345678")
		    .param("newPasswordConfirm", "87654321")
	    )
		    .andExpect(status().isOk())
		    .andExpect(model().hasErrors())
		    .andExpect(model().attributeExists("account"))
		    .andExpect(model().attributeExists("passwordForm"))
		    .andExpect(view().name("settings/password"));

	    Account notUpdatedAccount = accountRepository.findByNickname("nickname");
	    assertThat(notUpdatedAccount.getPassword()).isEqualTo("password");
	}

	@Test
	@DisplayName("알림 설정 페이지")
	void notificationUpdateForm() throws Exception {
	    mockMvc.perform(get("/settings/notifications")
	        .with(user(new UserAccount(account)))
	    )
		    .andExpect(status().isOk())
		    .andExpect(model().attributeExists("account"))
		    .andExpect(model().attributeExists("notifications"))
		    .andExpect(view().name("settings/notifications"));
	}

	@Test
	@DisplayName("정상적인 알림 설정 수정")
	void updateNotifications() throws Exception {
		mockMvc.perform(post("/settings/notifications")
			.with(user(new UserAccount(account)))
			.with(csrf())
			.param("studyCreatedByEmail", "true")
			.param("studyCreatedByWeb", "true")
			.param("studyEnrollmentResultByEmail", "true")
			.param("studyEnrollmentResultByWeb", "true")
			.param("studyUpdatedByEmail", "true")
			.param("studyUpdatedByWeb", "true")
		)
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/settings/notifications"))
			.andExpect(flash().attributeExists("message"));

		Account updatedAccount = accountRepository.findByNickname("nickname");

		assertThat(updatedAccount.isStudyCreatedByEmail()).isTrue();
		assertThat(updatedAccount.isStudyCreatedByWeb()).isTrue();
		assertThat(updatedAccount.isStudyUpdatedByEmail()).isTrue();
		assertThat(updatedAccount.isStudyUpdatedByWeb()).isTrue();
		assertThat(updatedAccount.isStudyEnrollmentResultByEmail()).isTrue();
		assertThat(updatedAccount.isStudyEnrollmentResultByWeb()).isTrue();
	}

	@Test
	@DisplayName("잘못된 알림 설정 수정 요청 처리")
	void updateNotificationsFail() throws Exception {
	    mockMvc.perform(post("/settings/notifications")
	        .with(user(new UserAccount(account)))
		    .with(csrf())
		    .param("studyCreatedByEmail", "invalid")
	    )
		    .andExpect(status().isOk())
		    .andExpect(model().attributeExists("account"))
		    .andExpect(model().attributeExists("notifications"))
		    .andExpect(view().name("settings/notifications"));

	    Account notUpdatedAccount = accountRepository.findByNickname("nickname");
	    assertThat(notUpdatedAccount.isStudyUpdatedByEmail()).isFalse();
	}

	@Test
	@DisplayName("닉네임 수정 폼 페이지")
	void updateNicknameForm() throws Exception {
	    mockMvc.perform(get("/settings/account")
	        .with(user(new UserAccount(account)))
	    )
		    .andExpect(status().isOk())
		    .andExpect(model().attributeExists("account"))
		    .andExpect(model().attributeExists("nicknameForm"))
		    .andExpect(view().name("settings/account"));
	}

	@Test
	@DisplayName("정상적인 닉네임 수정")
	void updateNickname() throws Exception {
		mockMvc.perform(post("/settings/account")
			.with(user(new UserAccount(account)))
			.with(csrf())
			.param("nickname", "new_nickname")
		)
			.andExpect(status().is3xxRedirection())
			.andExpect(flash().attributeExists("message"))
			.andExpect(redirectedUrl("/settings/account"));

		assertThat(accountRepository.findByNickname("new_nickname")).isNotNull();
		assertThat(accountRepository.findByNickname("nickname")).isNull();
	}

	@Test
	@DisplayName("중복된 닉네임 수정")
	void updateDuplicateNickname() throws Exception {
	    Account other = Account.builder()
		    .nickname("other")
		    .email("other@email.com")
		    .password("password")
		    .build();
	    accountRepository.save(other);

        mockMvc.perform(post("/settings/account")
	        .with(user(new UserAccount(account)))
	        .with(csrf())
	        .param("nickname", "other")
	    )
	        .andExpect(status().isOk())
	        .andExpect(model().hasErrors())
	        .andExpect(model().attributeExists("account"))
	        .andExpect(model().attributeExists("nicknameForm"))
	        .andExpect(view().name("settings/account"));

        assertThat(accountRepository.findByNickname("nickname")).isNotNull();
	}

	@Test
	@DisplayName("비정상적인 닉네임 수정")
	void updateNicknameFail() throws Exception {
	    mockMvc.perform(post("/settings/account")
	        .with(user(new UserAccount(account)))
		    .with(csrf())
		    .param("nickname", "has space")
	    )
		    .andExpect(status().isOk())
		    .andExpect(model().hasErrors())
		    .andExpect(model().attributeExists("account"))
		    .andExpect(model().attributeExists("nicknameForm"));

	    assertThat(accountRepository.findByNickname("nickname")).isNotNull();
	    assertThat(accountRepository.findByNickname("has space")).isNull();
	}
}