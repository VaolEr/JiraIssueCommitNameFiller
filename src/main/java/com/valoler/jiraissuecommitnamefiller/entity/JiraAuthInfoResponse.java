package com.valoler.jiraissuecommitnamefiller.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class JiraAuthInfoResponse {

	@JsonProperty("loginInfo")
	private LoginInfo loginInfo;

	@JsonProperty("name")
	private String name;

	@JsonProperty("self")
	private String self;

	@Data
	public static class LoginInfo{

		@JsonProperty("failedLoginCount")
		private Integer failedLoginCount;

		@JsonProperty("lastFailedLoginTime")
		private String lastFailedLoginTime;

		@JsonProperty("loginCount")
		private Integer loginCount;

		@JsonProperty("previousLoginTime")
		private String previousLoginTime;
	}
}