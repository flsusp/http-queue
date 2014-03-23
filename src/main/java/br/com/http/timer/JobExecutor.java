package br.com.http.timer;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@Stateless
public class JobExecutor {

	@Inject
	private EntityManager em;

	public JobExecution createExecution(Job job) {
		JobExecution execution = new JobExecution(job);
		em.persist(execution);
		return execution;
	}

	public void execute(JobExecution execution) {
		execution = em.merge(execution);
		execution.execute();
	}
}
