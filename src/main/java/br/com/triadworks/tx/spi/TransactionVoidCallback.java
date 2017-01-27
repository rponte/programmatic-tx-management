package br.com.triadworks.tx.spi;

@FunctionalInterface
public interface TransactionVoidCallback<T> {

	public void execute(T t);
	
}
