package br.com.triadworks.tx.jpa;

import javax.persistence.EntityManager;

@FunctionalInterface
public interface TransactionCallback<T> {

	public T execute(EntityManager entityManager);
	
}
