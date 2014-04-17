package br.com.http.timer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.SQLException;

import javax.ejb.TimerHandle;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.sql.rowset.serial.SerialBlob;

import br.com.http.queue.HttpRequestMessage;

import com.google.common.base.Joiner;

@Entity
@Table(name = "esb_job")
public class Job {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(length = 10, nullable = false)
	private String method;

	@Column(length = 2048, nullable = false)
	private String url;

	@Column(length = 4000, nullable = true, name = "cookie_content")
	private String cookieContent;

	@Column(length = 255, nullable = true, name = "cookie_name")
	private String cookieName;

	@Column(length = 255, nullable = true, name = "basic_auth_username")
	private String basicAuthUsername;

	@Column(length = 255, nullable = true, name = "basic_auth_password")
	private String basicAuthPassword;

	@Column(length = 255, nullable = false)
	private String second = "*";
	@Column(length = 255, nullable = false)
	private String minute = "*";
	@Column(length = 255, nullable = false)
	private String hour = "*";
	@Column(length = 255, nullable = false, name = "day_of_month")
	private String dayOfMonth = "*";
	@Column(length = 255, nullable = false, name = "day_of_week")
	private String dayOfWeek = "*";
	@Column(length = 255, nullable = false)
	private String month = "*";
	@Column(length = 255, nullable = false)
	private String year = "*";

	@Column(nullable = false)
	private boolean active = true;

	@Column(name = "timer_handle")
	@Lob
	private Blob timerHandle;

	public Job(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCookieContent() {
		return cookieContent;
	}

	public void setCookieContent(String cookieContent) {
		this.cookieContent = cookieContent;
	}

	public String getCookieName() {
		return cookieName;
	}

	public void setCookieName(String cookieName) {
		this.cookieName = cookieName;
	}

	public String getBasicAuthUsername() {
		return basicAuthUsername;
	}

	public void setBasicAuthUsername(String basicAuthUsername) {
		this.basicAuthUsername = basicAuthUsername;
	}

	public String getBasicAuthPassword() {
		return basicAuthPassword;
	}

	public void setBasicAuthPassword(String basicAuthPassword) {
		this.basicAuthPassword = basicAuthPassword;
	}

	public String getSecond() {
		return second;
	}

	public void setSecond(String second) {
		this.second = second;
	}

	public String getMinute() {
		return minute;
	}

	public void setMinute(String minute) {
		this.minute = minute;
	}

	public String getHour() {
		return hour;
	}

	public void setHour(String hour) {
		this.hour = hour;
	}

	public String getDayOfMonth() {
		return dayOfMonth;
	}

	public void setDayOfMonth(String dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public void serialize(TimerHandle timerHandle) {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			ObjectOutputStream stream = new ObjectOutputStream(bytes);

			stream.writeObject(timerHandle);

			stream.flush();

			this.timerHandle = new SerialBlob(bytes.toByteArray());
		} catch (SQLException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public TimerHandle geTimerHandle() {
		try {
			ObjectInputStream input = new ObjectInputStream(timerHandle.getBinaryStream());
			return (TimerHandle) input.readObject();
		} catch (ClassNotFoundException | SQLException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public HttpRequestMessage createHttpRequestMessage() {
		HttpRequestMessage message = new HttpRequestMessage(method, url);

		if (this.basicAuthUsername != null) {
			message.withBasicAuth(basicAuthUsername, basicAuthPassword);
		}

		if (this.cookieName != null) {
			message.withCookie(cookieName, cookieContent);
		}

		return message;
	}

	public void configureCronExpression(String cron) {
		String[] parts = cron.split(" ");
		this.second = parts[0];
		this.minute = parts[1];
		this.hour = parts[2];
		this.dayOfWeek = parts[3];
		this.dayOfMonth = parts[4];
		this.month = parts[5];
		this.year = parts[6];
	}

	public String getCronExpression() {
		return Joiner.on(' ').join(this.second, minute, hour, dayOfWeek, dayOfMonth, month, year);
	}

	public void inactivate() {
		this.active = false;
	}

	public boolean isActivate() {
		return this.active;
	}
}
