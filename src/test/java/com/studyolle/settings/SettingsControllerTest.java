package com.studyolle.settings;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
		    .with(user(new UserAccount(account))))
		    .andExpect(status().isOk())
		    .andExpect(model().attribute("account", account))
		    .andExpect(model().attribute("profile", new Profile(account)))
		    .andExpect(view().name("settings/profile"));
	}

}