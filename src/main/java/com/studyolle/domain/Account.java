package com.studyolle.domain;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

import javax.persistence.ManyToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {
	@Id
	@GeneratedValue
	private Long id;

	@Column(unique = true)
	private String email;

	@Column(unique = true)
	private String nickname;

	private String password;

	private boolean emailVerified;

	private String emailCheckToken;

	private LocalDateTime emailCheckTokenCreatedAt;

	private LocalDateTime joinedAt;

	private String bio;

	private String url;

	private String occupation;

	private String location;

	@Lob
	@Basic(fetch = FetchType.EAGER)
	private String profileImage;

	private boolean studyCreatedByEmail;

	private boolean studyCreatedByWeb;

	private boolean studyEnrollmentResultByEmail;

	private boolean studyEnrollmentResultByWeb;

	private boolean studyUpdatedByEmail;

	private boolean studyUpdatedByWeb;

	@ManyToMany
	private Set<Tag> tags;

	public void generateEmailCheckToken() {
		this.emailCheckToken = UUID.randomUUID().toString();
		this.emailCheckTokenCreatedAt = LocalDateTime.now();
	}

	public void completeSignUp() {
		emailVerified = true;
		joinedAt = LocalDateTime.now();
	}

	public boolean isValidToken(String token) {
		return emailCheckToken.equals(token);
	}

	public boolean canResendEmail() {
		return emailCheckTokenCreatedAt.isBefore(LocalDateTime.now().minusHours(1L));
	}
}
