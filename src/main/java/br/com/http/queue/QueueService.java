package br.com.http.queue;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/message")
@RequestScoped
public class QueueService {

	@Inject
	private QueueSender queue;

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createMessage(@FormParam("method") String method, @FormParam("url") String url, @FormParam("cookie-content") String cookieContent,
			@FormParam("cookie-name") String cookieName, @FormParam("username") String basicAuthUsername,
			@FormParam("password") String basicAuthPassword) {
		HttpRequestMessage message = new HttpRequestMessage(method, url);

		if (cookieName != null) {
			message.withCookie(cookieName, cookieContent);
		}
		if (basicAuthUsername != null) {
			message.withBasicAuth(basicAuthUsername, basicAuthPassword);
		}

		queue.send(message);

		return Response.ok().build();
	}
}
