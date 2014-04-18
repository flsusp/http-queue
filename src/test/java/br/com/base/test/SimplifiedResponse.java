package br.com.base.test;

public class SimplifiedResponse {
	private int statusCode;
	private String content;

	SimplifiedResponse(int statusCode, String content) {
		this.statusCode = statusCode;
		this.content = content;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getContent() {
		return content;
	}
}