package br.com.triadworks.tx.jpa;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.triadworks.sample.model.Produto;
import br.com.triadworks.tx.spi.DataAccessException;
import br.com.triadworks.tx.spi.TransactionCallback;
import br.com.triadworks.tx.spi.TransactionVoidCallback;

public class TransactionManagerTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManagerTest.class);

	private JpaTransactionManager txManager;
	
	@Before
	public void setUp() {
		txManager = new JpaTransactionManager();
	}
	
	@After
	public void cleanUp() {
		txManager.doInTransaction(new TransactionVoidCallback() {
			@Override
			public void execute(EntityManager entityManager) {
				entityManager
					.createQuery("delete from Produto")
					.executeUpdate();
			}
		});
	}
	
	@Test
	public void deveInserirNovoProdutoNoBancoDeDados() {
		
		Produto ipad = new Produto("iPad Retina Display");
		
		txManager.doInTransaction(new TransactionVoidCallback() {
			@Override
			public void execute(EntityManager entityManager) {
				entityManager.persist(ipad);
			}
		});
		
		Produto produto = txManager.doInTransactionWithReturn(new TransactionCallback<Produto>() {
			@Override
			public Produto execute(EntityManager entityManager) {
				return entityManager.find(Produto.class, ipad.getId());
			}
		});
		
		assertEquals("produto id", ipad.getId(), produto.getId());
		assertEquals("produto nome", ipad.getNome(), produto.getNome());
	}
	
	@Test
	public void naoDeveInserirProdutosEmCasoDeErroDentroDaTransacao() {
		
		try {
			txManager.doInTransaction(new TransactionVoidCallback() {
				@Override
				public void execute(EntityManager entityManager) {
					entityManager.persist(new Produto("produto #1"));
					entityManager.persist(new Produto("produto #2"));
					entityManager.persist(new Produto("produto #3"));
					entityManager.persist(new Produto("produto #4"));
					
					throw new IllegalStateException("Erro qualquer");
				}
			});
		} catch(DataAccessException e) {
			LOGGER.error("Erro ao tentar gravar varios produtos na mesma transação: ", e);
		}
		
		Long total = txManager.doInTransactionWithReturn(new TransactionCallback<Long>() {
			@Override
			public Long execute(EntityManager entityManager) {
				Long count = entityManager
					.createQuery("select count(p) from Produto p", Long.class)
					.getSingleResult();
				return count;
			}
		});
		
		assertEquals("total de produtos gravados", 0, total.longValue());
	}

}
