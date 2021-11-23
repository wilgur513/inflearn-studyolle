package com.studyolle.main;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.studyolle.account.AccountRepository;
import com.studyolle.account.AccountService;
import com.studyolle.account.SignUpForm;
import com.studyolle.account.UserAccount;
import com.studyolle.domain.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
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
    @Autowired
    AccountRepository accountRepository;

    @BeforeEach
    void setUp() throws Exception {
        SignUpForm form = new SignUpForm();
        form.setNickname("username");
        form.setEmail("email@email.com");
        form.setPassword("password");
        accountService.processNewAccount(form);
    }

    @Test
    @DisplayName("일반 사용자 루트 페이지 접근 테스트")
    void indexWithAccount() throws Exception {
        Account account = accountRepository.findByNickname("username");
        mockMvc.perform(get("/").with(user(new UserAccount(account))))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("account"))
            .andExpect(view().name("index"))
            .andExpect(authenticated().withUsername("username"));
    }

    @Test
    @DisplayName("익명 사용자 루트 페이지 접근 테스트")
    void indexWithAnonymous() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(model().attributeDoesNotExist("account"))
            .andExpect(view().name("index"));
    }

    @Test
    @DisplayName("로그인 페이지")
    void loginPage() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(view().name("login"));
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

    @Test
    @DisplayName("로그아웃 테스트")
    void logout() throws Exception {
        mockMvc.perform(post("/logout")
            .with(csrf())
            .with(user("username"))
        )
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("로그인 시 Remember Me 사용 테스트")
    void rememberMe() throws Exception {
        mockMvc.perform(post("/login")
            .param("username", "username")
            .param("password", "password")
            .param("remember-me", "true")
            .with(csrf())
        )
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(cookie().exists("remember-me"));
    }

}