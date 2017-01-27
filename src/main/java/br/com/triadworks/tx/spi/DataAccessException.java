package br.com.triadworks.tx.spi;

public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(Throwable throwable) {
        super(throwable);
    }
    
}
