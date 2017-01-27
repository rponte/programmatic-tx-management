package br.com.triadworks.tx.jpa;

import javax.persistence.EntityManager;

@FunctionalInterface
public interface TransactionVoidCallback {

	public void execute(EntityManager entityManager);
	
}
