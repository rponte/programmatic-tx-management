package br.com.triadworks.tx.support;

@FunctionalInterface
public interface TransactionVoidCallback<T> {

	public void execute(T t);
	
}
