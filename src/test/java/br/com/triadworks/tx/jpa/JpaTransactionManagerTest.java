package br.com.triadworks.tx.jpa;

import static org.junit.Assert.assertEquals;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.triadworks.sample.model.Produto;
import br.com.triadworks.tx.spi.DataAccessException;
import br.com.triadworks.tx.spi.TransactionManager;

public class JpaTransactionManagerTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JpaTransactionManagerTest.class);

	private TransactionManager<EntityManager> txManager;
	private EntityManagerFactory entityManagerFactory;
	
	@Before
	public void setUp() {
		entityManagerFactory = Persistence.createEntityManagerFactory("sample-pu");
		txManager = new JpaTransactionManager(entityManagerFactory);
	}
	
	@After
	public void cleanUp() {
		entityManagerFactory.close();
	}
	
	@Test
	public void deveInserirNovoProdutoNoBancoDeDados() {
		
		Produto ipad = new Produto("iPad Retina Display");
		
		txManager.doInTransaction(new JpaTransactionVoidCallback() {
			@Override
			public void execute(EntityManager entityManager) {
				entityManager.persist(ipad);
			}
		});
		
		Produto produto = txManager.doInTransactionWithReturn(new JpaTransactionCallback<Produto>() {
			@Override
			public Produto execute(EntityManager entityManager) {
				return entityManager.find(Produto.class, ipad.getId());
			}
		});
		
		assertEquals("produto id", ipad.getId(), produto.getId());
		assertEquals("produto nome", ipad.getNome(), produto.getNome());
	}
	
	@Test
	public void deveInserirNovoProdutoNoBancoDeDados_comJava8() {
		
		Produto ipad = new Produto("iPad Retina Display");
		
		txManager.doInTransaction((em) -> em.persist(ipad));
		
		Produto produto = txManager
				.doInTransactionWithReturn((em) -> em.find(Produto.class, ipad.getId()));
		
		assertEquals("produto id", ipad.getId(), produto.getId());
		assertEquals("produto nome", ipad.getNome(), produto.getNome());
	}
	
	@Test
	public void naoDeveInserirProdutosEmCasoDeErroDentroDaTransacao() {
		
		try {
			txManager.doInTransaction(new JpaTransactionVoidCallback() {
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
		
		Long total = txManager.doInTransactionWithReturn(new JpaTransactionCallback<Long>() {
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
	
	@Test
	public void naoDeveInserirProdutosEmCasoDeErroDentroDaTransacao_comJava8() {
		
		try {
			txManager.doInTransaction((em) ->  {
				em.persist(new Produto("produto #1"));
				em.persist(new Produto("produto #2"));
				em.persist(new Produto("produto #3"));
				em.persist(new Produto("produto #4"));
				
				throw new IllegalStateException("Erro qualquer");
			});
		} catch(DataAccessException e) {
			LOGGER.error("Erro ao tentar gravar varios produtos na mesma transação: ", e);
		}
		
		Long total = txManager.doInTransactionWithReturn((em) -> {
				Long count = em.createQuery("select count(p) from Produto p", Long.class)
					.getSingleResult();
				return count;
		});
		
		assertEquals("total de produtos gravados", 0, total.longValue());
	}

}
