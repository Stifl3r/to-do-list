package za.co.learnings.todolist.api.job;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TransactionRequiredException;

@Service
public class QuartzPersistence {

    final
    EntityManagerFactory emf;

    public QuartzPersistence(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public <T> T persist(T entity) {
        var em = emf.createEntityManager();
        try {
            em.joinTransaction();
            em.persist(entity);
        } catch (TransactionRequiredException tre) {
            EntityTransaction tx = null;
            try {
                tx = em.getTransaction();
                tx.begin();
                em.persist(entity);
                tx.commit();
            } catch (Exception e) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            }
        }
        em.close();
        return entity;
    }
}
