package TaskManagementSystem.repository;

import jakarta.persistence.LockModeType;
import TaskManagementSystem.model.Comment;
import TaskManagementSystem.model.Task;
import TaskManagementSystem.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<Comment> findByTaskAndAuthor(Task task, User author);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  List<Comment> findAllByAuthor(User author);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Page<Comment> findAllByTask(Task task, Pageable pageable);
}
