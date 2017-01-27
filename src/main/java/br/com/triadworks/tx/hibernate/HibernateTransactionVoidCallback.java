package br.com.triadworks.tx.hibernate;

import org.hibernate.Session;

import br.com.triadworks.tx.spi.TransactionVoidCallback;

@FunctionalInterface
public interface HibernateTransactionVoidCallback extends TransactionVoidCallback<Session> {

}
