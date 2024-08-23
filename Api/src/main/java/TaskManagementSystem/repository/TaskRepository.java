package TaskManagementSystem.repository;

import jakarta.persistence.LockModeType;
import TaskManagementSystem.model.Task;
import TaskManagementSystem.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<Task> findByName(String name);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  List<Task> findAllByAuthor(User author);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  List<Task> findAllByExecutor(User executor);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Page<Task> findAllByAuthor(User author, Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Page<Task> findAllByExecutor(User executor, Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @NonNull
  Page<Task> findAll(@NonNull Pageable pageable);
}
