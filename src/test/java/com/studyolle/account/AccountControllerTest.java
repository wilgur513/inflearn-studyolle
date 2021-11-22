package com.studyolle.account;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
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

    @DisplayName("회원가입 화면 보이는지 테스트")
    @Test
    public void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated())
        ;
    }

    @DisplayName("회원가입 처리 - 입력값 오류")
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

    @DisplayName("회원가입 처리 - 입력값 정상")
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

    @DisplayName("인증 메일 확인 - 입력값 오류")
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

    @DisplayName("인증 메일 확인 - 입력값 정상")
    @Test
    public void checkEmailTokenWithCorrectInput() throws Exception {
        Account account = Account.builder()
                .email("email@email.com")
                .nickname("nickname")
                .password("password")
                .build();
        accountRepository.save(account);
        account.generateEmailCheckToken();

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
    @DisplayName("익명 사용자가 이메일 재확인")
    void checkEmailWithAnonymous() throws Exception {
        mockMvc.perform(get("/check-email"))
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/"));
    }

    @Test
    @DisplayName("로그인한 사용자가 이메일 재확인")
    void checkEmailWithAccount() throws Exception {
        Account account = Account.builder()
            .email("email@email.com")
            .nickname("nickname")
            .password("password")
            .build();
        accountRepository.save(account);
        account.generateEmailCheckToken();

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
    @DisplayName("익명 사용자 이메일 재전송 요청")
    void resendEmailWithAnonymous() throws Exception {
        mockMvc.perform(get("/resend-confirm-email"))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("일반 사용자가 이메일 재전송 요청 성공")
    void resendEmailWithAccount() throws Exception {
        Account account = Account.builder()
            .nickname("nickname")
            .email("email@email.com")
            .password("password")
            .build();
        account.generateEmailCheckToken();
        account.setEmailCheckTokenCreatedAt(LocalDateTime.now().minusHours(2L));
        accountRepository.save(account);

        mockMvc.perform(get("/resend-confirm-email")
            .with(user(new UserAccount(account)))
        )
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/"))
        ;
    }

    @Test
    @DisplayName("이메일 재전송 후 1시간 안에 다시 재전송")
    void resendEmailWithAccountBefore1Hour() throws Exception {
        Account account = Account.builder()
            .nickname("nickname")
            .email("email@email.com")
            .password("password")
            .build();
        account.generateEmailCheckToken();
        account.setEmailCheckTokenCreatedAt(LocalDateTime.now().minusMinutes(30L));
        accountRepository.save(account);

        mockMvc.perform(get("/resend-confirm-email")
            .with(user(new UserAccount(account)))
        )
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:account/check-email"))
            .andExpect(model().attributeExists("error"))
        ;
    }
}