package br.com.http.persistence;

import java.io.Serializable;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.SequenceGenerator;

public class UseExistingOrGenerateIdGenerator extends SequenceGenerator {

	@Override
	public Serializable generate(SessionImplementor session, Object object) {
		Serializable id = session.getEntityPersister(null, object).getClassMetadata().getIdentifier(object, session);
		return id != null ? id : super.generate(session, object);
	}
}
