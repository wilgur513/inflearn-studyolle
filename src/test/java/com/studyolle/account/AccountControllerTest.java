package com.studyolle.account;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.studyolle.domain.Account;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    private JavaMailSender javaMailSender;

    Account account;

    @BeforeEach
    void setUp() throws Exception {
        account = Account.builder()
            .email("email@email.com")
            .nickname("nickname")
            .password("password")
            .build();
        account.generateEmailCheckToken();
        accountRepository.save(account);
    }

    @DisplayName("???????????? ?????? ???????????? ?????????")
    @Test
    public void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
            .andExpect(status().isOk())
            .andExpect(view().name("account/sign-up"))
            .andExpect(model().attributeExists("signUpForm"))
            .andExpect(unauthenticated())
        ;
    }

    @DisplayName("???????????? ?????? - ????????? ??????")
    @Test
    public void signUpFormWithWrongInput() throws Exception {
        mockMvc.perform(post("/sign-up")
            .param("nickname", "jinhyeok")
            .param("email", "email....")
            .param("password", "12345")
            .with(csrf())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("account/sign-up"))
            .andExpect(unauthenticated())
        ;
    }

    @DisplayName("???????????? ?????? - ????????? ??????")
    @Test
    public void signUpFormWithCorrectInput() throws Exception {
        mockMvc.perform(post("/sign-up")
            .param("nickname", "jinhyeok")
            .param("email", "jinhyeok@email.com")
            .param("password", "12345678")
            .with(csrf())
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/"))
            .andExpect(authenticated())
        ;

        Account account = accountRepository.findByEmail("jinhyeok@email.com");
        assertNotNull(account.getEmailCheckToken());
        assertNotNull(account);
        assertNotEquals(account.getPassword(), "12345678");
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @DisplayName("?????? ?????? ?????? - ????????? ??????")
    @Test
    public void checkEmailTokenWithWrongInput() throws Exception {
        mockMvc.perform(get("/check-email-token")
            .param("token", "invalid-token")
            .param("email", "email@email.com")
        )
            .andExpect(status().isOk())
            .andExpect(view().name("account/checked-email"))
            .andExpect(model().attributeExists("error"))
            .andExpect(unauthenticated())
        ;
    }

    @DisplayName("?????? ?????? ?????? - ????????? ??????")
    @Test
    public void checkEmailTokenWithCorrectInput() throws Exception {
        mockMvc.perform(get("/check-email-token")
            .param("token", account.getEmailCheckToken())
            .param("email", account.getEmail())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("account/checked-email"))
            .andExpect(model().attributeDoesNotExist("error"))
            .andExpect(model().attributeExists("nickname"))
            .andExpect(model().attributeExists("numberOfUser"))
            .andExpect(authenticated())
        ;
    }

    @Test
    @DisplayName("?????? ???????????? ????????? ?????????")
    void checkEmailWithAnonymous() throws Exception {
        mockMvc.perform(get("/check-email"))
            .andDo(print())
            .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("???????????? ???????????? ????????? ?????????")
    void checkEmailWithAccount() throws Exception {
        mockMvc.perform(get("/check-email")
            .with(user(new UserAccount(account)))
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(view().name("account/check-email"))
            .andExpect(model().attribute("nickname", "nickname"))
            .andExpect(model().attribute("email", "email@email.com"));
    }

    @Test
    @DisplayName("?????? ????????? ????????? ????????? ??????")
    void resendEmailWithAnonymous() throws Exception {
        mockMvc.perform(get("/resend-confirm-email"))
            .andDo(print())
            .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("?????? ???????????? ????????? ????????? ?????? ??????")
    void resendEmailWithAccount() throws Exception {
        account.setEmailCheckTokenCreatedAt(LocalDateTime.now().minusHours(2L));
        accountRepository.save(account);

        mockMvc.perform(get("/resend-confirm-email")
            .with(user(new UserAccount(account)))
        )
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/"))
        ;

        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("????????? ????????? ??? 1?????? ?????? ?????? ?????????")
    void resendEmailWithAccountBefore1Hour() throws Exception {
        account.setEmailCheckTokenCreatedAt(LocalDateTime.now().minusMinutes(30L));
        accountRepository.save(account);

        mockMvc.perform(get("/resend-confirm-email")
            .with(user(new UserAccount(account)))
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(view().name("account/check-email"))
            .andExpect(model().attributeExists("error"))
        ;

        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("?????? ????????? ?????? ?????????")
    void profileWithOwner() throws Exception {
        mockMvc.perform(get("/profile/nickname").with(user(new UserAccount(account))))
            .andExpect(status().isOk())
            .andExpect(view().name("account/profile"))
            .andExpect(model().attribute("isOwner", true))
            .andExpect(model().attribute("account", account));
    }

    @Test
    @DisplayName("?????? ????????? ????????? ??????")
    void profileWithOther() throws Exception {
        Account other = Account.builder()
            .email("other@other.com")
            .nickname("other")
            .password("password")
            .build();
        accountRepository.save(other);

        mockMvc.perform(get("/profile/nickname").with(user(new UserAccount(other))))
            .andExpect(status().isOk())
            .andExpect(view().name("account/profile"))
            .andExpect(model().attribute("isOwner", false))
            .andExpect(model().attribute("account", account));
    }
}