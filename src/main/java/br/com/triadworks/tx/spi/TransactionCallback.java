package br.com.triadworks.tx.spi;

import javax.persistence.EntityManager;

@FunctionalInterface
public interface TransactionCallback<T> {

	public T execute(EntityManager entityManager);
	
}
