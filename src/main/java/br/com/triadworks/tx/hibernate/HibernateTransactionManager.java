package br.com.triadworks.tx.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import br.com.triadworks.tx.spi.DataAccessException;
import br.com.triadworks.tx.spi.TransactionCallback;
import br.com.triadworks.tx.spi.TransactionManager;
import br.com.triadworks.tx.spi.TransactionVoidCallback;

public class HibernateTransactionManager implements TransactionManager<Session> {

	private SessionFactory factory;
	
	public HibernateTransactionManager(SessionFactory factory) {
		this.factory = factory;
	}

	@Override
	public <R> R doInTransactionWithReturn(TransactionCallback<Session, R> callback) throws DataAccessException {
		
		Session session = null;
		Transaction tx = null;
		try {

			session = factory.openSession();
			
			tx = session.beginTransaction(); // inicia transação
			R result = callback.execute(session);
			tx.commit(); // comita transação

			return result;

		} catch (Exception e) {
			if (tx != null && tx.isActive()) {
				tx.rollback(); // rollback da transação
			}
			throw new DataAccessException(e);
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

	@Override
	public void doInTransaction(TransactionVoidCallback<Session> callback) throws DataAccessException {
		doInTransactionWithReturn(new TransactionCallback<Session, Void>() {
			@Override
			public Void execute(Session session) {
				callback.execute(session);
				return null;
			}
		});
	}

}
