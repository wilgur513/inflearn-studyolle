package com.studyolle.settings;

import com.studyolle.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;


@Data
public class Profile {
	@Length(max = 35)
	private String bio;

	private String url;

	private String occupation;

	private String location;

	private String profileImage;
}
