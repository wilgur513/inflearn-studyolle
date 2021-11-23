package com.studyolle.main;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.studyolle.account.AccountService;
import com.studyolle.account.SignUpForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MainControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    AccountService accountService;

    @BeforeEach
    void setUp() throws Exception {
        SignUpForm form = new SignUpForm();
        form.setNickname("username");
        form.setEmail("email@email.com");
        form.setPassword("password");
        accountService.processNewAccount(form);
    }

    @ParameterizedTest(name="아이디 : {0}")
    @ValueSource(strings={"email@email.com", "username"})
    @DisplayName("이메일 또는 닉네임 로그인 테스트")
    void loginWithEmail(String emailOrNickname) throws Exception {
        mockMvc.perform(post("/login")
            .param("username", emailOrNickname)
            .param("password", "password")
            .with(csrf())
        )
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(authenticated().withUsername("username"));
    }

    @Test
    @DisplayName("로그인 실패 테스트")
    void loginFail() throws Exception {
        mockMvc.perform(post("/login")
            .param("username", "invalid")
            .param("password", "invalid")
            .with(csrf())
        )
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?error"))
            .andExpect(unauthenticated());
    }
}