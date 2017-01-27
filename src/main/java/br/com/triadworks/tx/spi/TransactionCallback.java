package br.com.triadworks.tx.spi;

@FunctionalInterface
public interface TransactionCallback<T, R> {

	public R execute(T t);
	
}
