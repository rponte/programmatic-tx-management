package br.com.triadworks.tx.support;

@FunctionalInterface
public interface TransactionCallback<T, R> {

	public R execute(T t);
	
}
