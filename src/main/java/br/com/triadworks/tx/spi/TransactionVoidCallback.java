package br.com.triadworks.tx.spi;

import javax.persistence.EntityManager;

@FunctionalInterface
public interface TransactionVoidCallback {

	public void execute(EntityManager entityManager);
	
}
