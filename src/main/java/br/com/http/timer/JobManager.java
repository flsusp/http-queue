package br.com.http.timer;

import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerHandle;
import javax.ejb.TimerService;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@Stateless
public class JobManager {

	@Resource
	private TimerService timerService;

	@Inject
	private EntityManager em;

	@Inject
	private JobExecutor executor;

	public Job createJob(Job job) {
		em.persist(job);

		ScheduleExpression schedule = new ScheduleExpression();
		schedule.second(job.getSecond());
		schedule.minute(job.getMinute());
		schedule.hour(job.getHour());
		schedule.dayOfMonth(job.getDayOfMonth());
		schedule.dayOfWeek(job.getDayOfWeek());
		schedule.month(job.getMonth());
		schedule.year(job.getYear());

		TimerConfig timerConfig = new TimerConfig(job.getId(), true);
		Timer timer = timerService.createCalendarTimer(schedule, timerConfig);
		TimerHandle timerHandle = timer.getHandle();
		job.serialize(timerHandle);

		return job;
	}

	public void removeJob(long jobId) {
		Job job = em.find(Job.class, jobId);
		TimerHandle timerHandle = job.geTimerHandle();
		timerHandle.getTimer().cancel();
		job.inactivate();
	}

	@Timeout
	public void execute(Timer timer) {
		Job job = em.find(Job.class, timer.getInfo());
		JobExecution execution = executor.createExecution(job);
		executor.execute(execution);
	}
}
