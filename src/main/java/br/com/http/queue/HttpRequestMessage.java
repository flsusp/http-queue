package br.com.http.queue;

import java.io.Serializable;

public class HttpRequestMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String url;

	public HttpRequestMessage(String url) {
		super();
		this.url = url;
	}

	public String getUrl() {
		return url;
	}
}
