package br.com.http.timer;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.com.http.queue.HttpRequestMessage;

@Entity
@Table(name = "esb_job_execution")
public class JobExecution {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Job job;

	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date start;

	@Column(nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date finish;

	@Column(nullable = true)
	private Integer httpResponseStatus;

	@Column(nullable = true, length = 10240)
	private String httpResponseContent;

	public JobExecution(Job job) {
		this.job = job;
		this.start = new Date();
	}

	protected JobExecution() {
		super();
	}

	public void execute() {
		HttpRequestMessage http = job.createHttpRequestMessage();

		http.send();

		this.httpResponseStatus = http.getResponseStatus();
		this.httpResponseContent = http.getResponseContent();
	}
}
