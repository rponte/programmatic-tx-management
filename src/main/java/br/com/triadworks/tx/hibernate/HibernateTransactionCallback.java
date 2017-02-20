package br.com.triadworks.tx.hibernate;

import org.hibernate.Session;

import br.com.triadworks.tx.support.TransactionCallback;

@FunctionalInterface
public interface HibernateTransactionCallback<R> extends TransactionCallback<Session, R> {

}
