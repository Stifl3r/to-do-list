package za.co.learnings.todolist.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.co.learnings.todolist.api.repository.entity.QuartzJobHistory;

import javax.transaction.Transactional;

@Transactional
public interface QrtzJobHistoryRepository extends JpaRepository<QuartzJobHistory, Integer> {

    @Query("SELECT q from QuartzJobHistory q " +
            "WHERE q.jobGroup = :jobGroup")
    Page<QuartzJobHistory> findByFilter(Pageable pageable, @Param("jobGroup") String jobGroup);
}
