package br.com.triadworks.tx.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class TransactionManager {

	private JpaUtils jpaUtils;

	public TransactionManager() {
		this(new JpaUtils());
	}

	public TransactionManager(JpaUtils jpaUtils) {
		this.jpaUtils = jpaUtils;
	}

	/**
	 * Executa uma Unidade de Trabalho dentro de um contexto transacional e
	 * retorna o resultado da operação.
	 */
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

	/**
	 * Executa uma Unidade de Trabalho dentro de um contexto transacional.
	 */
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
