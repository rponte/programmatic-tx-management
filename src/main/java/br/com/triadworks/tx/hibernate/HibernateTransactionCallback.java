package br.com.triadworks.tx.hibernate;

import org.hibernate.Session;

import br.com.triadworks.tx.spi.TransactionCallback;

public interface HibernateTransactionCallback<R> extends TransactionCallback<Session, R> {

}
