package br.com.http.timer;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Stateless
public class JobExecutor {

    @PersistenceContext(unitName = "primary")
	private EntityManager em;

	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public JobExecution createExecution(Job job) {
		JobExecution execution = new JobExecution(job);
		em.persist(execution);
		return execution;
	}

	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public void execute(JobExecution execution) {
		execution = em.merge(execution);
		execution.execute();
	}
}
