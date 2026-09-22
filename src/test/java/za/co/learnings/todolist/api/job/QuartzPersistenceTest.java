package za.co.learnings.todolist.api.job;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TransactionRequiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.learnings.todolist.api.repository.entity.QuartzJobHistory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class QuartzPersistenceTest {

    private EntityManager entityManager;
    private EntityTransaction transaction;
    private QuartzPersistence quartzPersistence;

    @BeforeEach
    public void setUp() {
        var entityManagerFactory = mock(EntityManagerFactory.class);
        entityManager = mock(EntityManager.class);
        transaction = mock(EntityTransaction.class);
        given(entityManagerFactory.createEntityManager()).willReturn(entityManager);
        given(entityManager.getTransaction()).willReturn(transaction);
        quartzPersistence = new QuartzPersistence(entityManagerFactory);
    }

    @Test
    public void persistWhenTransactionIsActiveShouldJoinItAndPersist() {
        //Given
        var entity = new QuartzJobHistory();

        //When
        var actual = quartzPersistence.persist(entity);

        //Then
        assertThat(actual).isSameAs(entity);
        var order = inOrder(entityManager);
        order.verify(entityManager).joinTransaction();
        order.verify(entityManager).persist(entity);
        order.verify(entityManager).close();
        verify(entityManager, never()).getTransaction();
    }

    @Test
    public void persistWhenNoTransactionIsActiveShouldUseOwnTransaction() {
        //Given
        var entity = new QuartzJobHistory();
        willThrow(new TransactionRequiredException()).given(entityManager).joinTransaction();

        //When
        quartzPersistence.persist(entity);

        //Then
        var order = inOrder(transaction, entityManager);
        order.verify(transaction).begin();
        order.verify(entityManager).persist(entity);
        order.verify(transaction).commit();
        order.verify(entityManager).close();
    }

    // Regression test: the entity manager used to be left open when persisting failed.
    @Test
    public void persistWhenOwnTransactionFailsShouldRollbackAndCloseEntityManager() {
        //Given
        var entity = new QuartzJobHistory();
        willThrow(new TransactionRequiredException()).given(entityManager).joinTransaction();
        willThrow(new PersistenceException("db down")).given(entityManager).persist(entity);
        given(transaction.isActive()).willReturn(true);

        //When
        var thrown = catchThrowable(() -> quartzPersistence.persist(entity));

        //Then
        assertThat(thrown).isInstanceOf(PersistenceException.class).hasMessage("db down");
        verify(transaction).rollback();
        verify(transaction, never()).commit();
        verify(entityManager).close();
    }

    // Regression test: same leak, when persisting inside the joined transaction fails.
    @Test
    public void persistWhenJoinedTransactionFailsShouldCloseEntityManager() {
        //Given
        var entity = new QuartzJobHistory();
        willThrow(new PersistenceException("db down")).given(entityManager).persist(entity);

        //When
        var thrown = catchThrowable(() -> quartzPersistence.persist(entity));

        //Then
        assertThat(thrown).isInstanceOf(PersistenceException.class);
        verify(entityManager).close();
    }
}
