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

@Path("/queue")
@RequestScoped
public class QueueService {

	@Inject
	private QueueSender queue;

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createMessage(@FormParam("url") String url) {
		queue.send(new HttpRequestMessage(url));
		return Response.ok().build();
	}
}
