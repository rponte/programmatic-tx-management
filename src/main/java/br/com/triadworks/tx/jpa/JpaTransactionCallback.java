package br.com.triadworks.tx.jpa;

import javax.persistence.EntityManager;

import br.com.triadworks.tx.spi.TransactionCallback;

@FunctionalInterface
public interface JpaTransactionCallback<R> extends TransactionCallback<EntityManager, R> {

}
