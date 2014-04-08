package br.com.http.timer;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.http.queue.HttpRequestMessage;

@Entity
@Table(name = "esb_job_execution")
public class JobExecution {

	private static final Logger logger = LoggerFactory.getLogger(JobExecution.class);

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Job job;

	@Column(nullable = false, name = "ts_start")
	@Temporal(TemporalType.TIMESTAMP)
	private Date start;

	@Column(nullable = true, name = "ts_finish")
	@Temporal(TemporalType.TIMESTAMP)
	private Date finish;

	@Column(nullable = true, name = "http_response_status")
	private Integer httpResponseStatus;

	@Column(nullable = true, length = 1024, name = "client_error")
	private String clientError;

	@Column(nullable = false, length = 50)
	@Enumerated(EnumType.STRING)
	private JobExecutionStatus status;

	public JobExecution(Job job) {
		this.job = job;
		this.start = new Date();
		this.status = JobExecutionStatus.Running;
	}

	protected JobExecution() {
		super();
	}

	public void execute() {
		HttpRequestMessage http = job.createHttpRequestMessage();

		try {
			http.send();

			this.httpResponseStatus = http.getResponseStatus();

			this.status = JobExecutionStatus.Success;
		} catch (Exception e) {
			this.status = JobExecutionStatus.Failed;
			logger.error("Error executing job http request.", e);

			this.clientError = e.getMessage();
			if (this.clientError != null && this.clientError.length() > 1024) {
				this.clientError = this.clientError.substring(0, 1023);
			}
		} finally {
			this.finish = new Date();
		}
	}
}
