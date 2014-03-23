package br.com.http.timer;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.JsonObject;

@Path("/job")
@RequestScoped
public class JobService {

	@Inject
	private JobManager jobManager;

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createJob(@FormParam("method") String method, @FormParam("url") String url,
			@FormParam("cookie-content") String cookieContent, @FormParam("cookie-name") String cookieName,
			@FormParam("username") String basicAuthUsername, @FormParam("password") String basicAuthPassword,
			@FormParam("cron") String cron) {
		Job job = new Job();
		job.setMethod(method);
		job.setUrl(url);
		job.setCookieContent(cookieContent);
		job.setCookieName(cookieName);
		job.setBasicAuthUsername(basicAuthUsername);
		job.setBasicAuthPassword(basicAuthPassword);
		job.configureCronExpression(cron);

		job = jobManager.createJob(job);

		JsonObject json = new JsonObject();
		json.addProperty("id", job.getId());

		return Response.ok(json.toString()).build();
	}

	@Path("/{id}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public Response createJob(@QueryParam("id") Long id) {
		jobManager.removeJob(id);
		return Response.ok().build();
	}

}
