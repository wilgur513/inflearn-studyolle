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
			.andExpect(model().attribute("account", account))
			.andExpect(model().attribute("profile", new Profile(account)))
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

}