package br.com.http.queue;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/queue")
public class QueueService {

	@Context
	private ServletContext context;

	@GET
	public Response createMessage() {
		ServiceManager manager = (ServiceManager) context.getAttribute(ServiceManager.class.getName());
		manager.queue().send("http://localhost:8080/test");
		return Response.ok().build();
	}
}
