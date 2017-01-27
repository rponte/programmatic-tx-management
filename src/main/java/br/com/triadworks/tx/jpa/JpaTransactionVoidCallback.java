package br.com.triadworks.tx.jpa;

import javax.persistence.EntityManager;

import br.com.triadworks.tx.spi.TransactionVoidCallback;

@FunctionalInterface
public interface JpaTransactionVoidCallback extends TransactionVoidCallback<EntityManager> {

}
