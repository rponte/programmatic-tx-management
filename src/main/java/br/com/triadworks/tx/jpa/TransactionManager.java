package br.com.triadworks.tx.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import br.com.triadworks.tx.spi.DataAccessException;
import br.com.triadworks.tx.spi.TransactionCallback;
import br.com.triadworks.tx.spi.TransactionManagerI;
import br.com.triadworks.tx.spi.TransactionVoidCallback;

public class TransactionManager implements TransactionManagerI {

	private JpaUtils jpaUtils;

	public TransactionManager() {
		this(new JpaUtils());
	}

	public TransactionManager(JpaUtils jpaUtils) {
		this.jpaUtils = jpaUtils;
	}

	/* (non-Javadoc)
	 * @see br.com.triadworks.tx.jpa.TransactionManagerI#doInTransactionWithReturn(br.com.triadworks.tx.jpa.TransactionCallback)
	 */
	@Override
	public <T> T doInTransactionWithReturn(final TransactionCallback<T> callback) throws DataAccessException {

		EntityManager entityManager = null;
		EntityTransaction tx = null;
		try {

			entityManager = jpaUtils.getEntityManager();
			tx = entityManager.getTransaction();

			tx.begin(); // inicia transação
			T result = callback.execute(entityManager);
			tx.commit(); // comita transação

			return result;

		} catch (Exception e) {
			if (tx != null && tx.isActive()) {
				tx.rollback(); // rollback da transação
			}
			throw new DataAccessException(e);
		} finally {
			jpaUtils.close(entityManager);
		}
	}

	/* (non-Javadoc)
	 * @see br.com.triadworks.tx.jpa.TransactionManagerI#doInTransaction(br.com.triadworks.tx.jpa.TransactionVoidCallback)
	 */
	@Override
	public void doInTransaction(final TransactionVoidCallback callback) throws DataAccessException {
		doInTransactionWithReturn(new TransactionCallback<Void>() {
			@Override
			public Void execute(EntityManager entityManager) {
				callback.execute(entityManager);
				return null;
			}
		});
	}
}
