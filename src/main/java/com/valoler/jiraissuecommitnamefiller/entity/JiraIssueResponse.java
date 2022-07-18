package com.valoler.jiraissuecommitnamefiller.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class JiraIssueResponse {

	@JsonProperty("expand")
	private String expand;

	@JsonProperty("self")
	private String self;

	@JsonProperty("id")
	private String id;

	@JsonProperty("fields")
	private JiraIssueFields jiraIssueFields;

	@JsonProperty("key")
	private String key;

	@Data
	public static class JiraIssueFields {
		@JsonProperty("summary")
		private String summary;
	}

}
