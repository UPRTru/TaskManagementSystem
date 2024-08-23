package TaskManagementSystem.service;

import TaskManagementSystem.controller.Controller;
import TaskManagementSystem.dto.*;
import TaskManagementSystem.exception.AlreadyExistsException;
import TaskManagementSystem.exception.BelongException;
import TaskManagementSystem.exception.NotFoundException;
import TaskManagementSystem.mapper.CommentMapper;
import TaskManagementSystem.mapper.TaskMapper;
import TaskManagementSystem.model.*;
import TaskManagementSystem.repository.CommentRepository;
import TaskManagementSystem.repository.TaskRepository;
import TaskManagementSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
  private static final Logger log = LoggerFactory.getLogger(Controller.class);
  private TaskRepository taskRepository;
  private UserRepository userRepository;
  private CommentRepository commentRepository;

  @Autowired
  public TaskService(TaskRepository taskRepository, UserRepository userRepository, CommentRepository commentRepository) {
    this.taskRepository = taskRepository;
    this.userRepository = userRepository;
    this.commentRepository = commentRepository;
  }

  public void createTask(String author, TaskDto taskDto) {
    try {
      if (taskRepository.findByName(taskDto.getName()).isPresent()) {
        throw new AlreadyExistsException("");
      } else {
        throw new NotFoundException("");
      }
    } catch (Exception e) {
      if (e instanceof AlreadyExistsException) {
        log.info(e.getMessage());
        throw new AlreadyExistsException("Задача " + taskDto.getName() + " уже существует");
      } else {
        Task task = new Task();
        task.setAuthor(findUser(author));
        task.setName(taskDto.getName());
        task.setDescription(taskDto.getDescription());
        task.setStatus(Status.PENDING);
        task.setPriority(Priority.MEDIUM);
        task.setComments(List.of());
        task.setDate(new Date());
        taskRepository.save(task);
      }
    }
  }

  public ResultTaskDto getTask(String nameTask) {
      return TaskMapper.taskToDto(findTask(nameTask));
  }

  public Page<ResultTaskDto> getAuthorTasks(String author, PageableDto pageableDto, DataUpOnDownDTO dataUpOnDownDTO) {
    Map<User, Page<Task>> map = checkTaskOnAuthorOrExecutor(findUser(author), true, pageableDto, dataUpOnDownDTO);;
    return taskToDtoInPageMapValue(map);
  }

  public Page<ResultTaskDto> getMyWorkTasks(String executor, PageableDto pageableDto, DataUpOnDownDTO dataUpOnDownDTO) {
    Map<User, Page<Task>> map = checkTaskOnAuthorOrExecutor(findUser(executor), false, pageableDto, dataUpOnDownDTO);
    return taskToDtoInPageMapValue(map);
  }

  private Sort sort (DataUpOnDownDTO dataUpOnDownDTO) {
    if (dataUpOnDownDTO.getSortByDate() == SortByDate.DOWN) {
      return Sort.by(Sort.Direction.DESC, "date");
    } else {
      return Sort.by(Sort.Direction.ASC, "date");
    }
  }

  private Page<ResultTaskDto> taskToDtoInPageMapValue(Map<User, Page<Task>> map) {
      return new PageImpl<>(map.entrySet().stream().findFirst().get().getValue().stream()
              .map(TaskMapper::taskToDto).collect(Collectors.toList()));
  }

  public Page<ResultTaskDto> getAllTasks(PageableDto pageableDto, DataUpOnDownDTO dataUpOnDownDTO) {
    try {
      Page<Task> tasks = taskRepository.findAll(PageRequest.of(pageableDto.getPage(), pageableDto.getSize(), sort(dataUpOnDownDTO)));
      return new PageImpl<>(tasks.stream().map(TaskMapper::taskToDto).collect(Collectors.toList()));
    } catch (Exception e) {
      throw new NotFoundException("Задач еще нет");
    }
  }

  public ResultTaskDto updateTask(String author, String nameTask, TaskDto taskDto) {
    Task task = checkTask(author, nameTask);
    if (taskDto.getName() != null && !taskDto.getName().isEmpty() && !task.getName().equals("")) {
      task.setName(taskDto.getName());
    }
    if (taskDto.getDescription() != null && !taskDto.getDescription().isEmpty() && !task.getName().equals("")) {
      task.setDescription(taskDto.getDescription());
    }
    return TaskMapper.taskToDto(taskRepository.save(task));
  }

  public ResultTaskDto updatePriorityUp(String author, String nameTask) {
    Task task = checkTask(author, nameTask);
    task.setPriority(Priority.up(task.getPriority()));
    return TaskMapper.taskToDto(taskRepository.save(task));
  }

  public ResultTaskDto updatePriorityDown(String author, String nameTask) {
    Task task = checkTask(author, nameTask);
    task.setPriority(Priority.down(task.getPriority()));
    return TaskMapper.taskToDto(taskRepository.save(task));
  }

  public ResultTaskDto updateStatusComplete(String user, String nameTask) {
    try {
      Task task = findTask(nameTask);
      if (task.getAuthor().getEmail().equals(user) || (task.getExecutor() != null && task.getExecutor().getEmail().equals(user))) {
        task.setStatus(Status.COMPLETED);
        return TaskMapper.taskToDto(taskRepository.save(task));
      } else {
        throw new BelongException("Задача " + nameTask + " не принадлежит " + user);
      }
    } catch (Exception e) {
      log.info(e.getMessage());
      throw e;
    }
  }

  public ResultTaskDto setExecutor(String author, String nameTask, String executor) {
    Task task = checkTask(author, nameTask);
    if (executor == null) {
      task.setExecutor(null);
      task.setStatus(Status.PENDING);
    } else {
      task.setExecutor(findUser(executor));
      task.setStatus(Status.IN_PROGRESS);
    }
    return TaskMapper.taskToDto(taskRepository.save(task));
  }

  public ResultTaskDto I_will_work_on_this_task(String executor, String nameTask) {
    Task task = findTask(nameTask);
    if (task.getStatus().equals(Status.PENDING)) {
      task.setExecutor(findUser(executor));
      task.setStatus(Status.IN_PROGRESS);
      return TaskMapper.taskToDto(taskRepository.save(task));
    } else {
      throw new AlreadyExistsException("У задачи " + nameTask + " уже есть исполнитель");
    }
  }

  public void deleteAuthor(String author) {
    User user = findUser(author);
    List<Comment> comments = commentRepository.findAllByAuthor(user);
    if (comments != null && !comments.isEmpty()) {
      commentRepository.deleteAll(comments);
    }
    List<Task> tasks = taskRepository.findAllByAuthor(user);
    if (tasks != null && !tasks.isEmpty()) {
      for (Task task : tasks) {
          if (task.getComments() != null && !task.getComments().isEmpty()) {
            commentRepository.deleteAll(task.getComments());
          }
          taskRepository.delete(task);
      }
    }
  }
  public void deleteExecutor(String executor) {
    try {
      User user = findUser(executor);
      List<Task> tasks = taskRepository.findAllByExecutor(user);
      for (Task task : tasks) {
        task.setExecutor(null);
        task.setStatus(Status.PENDING);
        taskRepository.save(task);
      }
    } catch (Exception e) {
      return;
    }
  }

  public void deleteTask(String author, String nameTask) {
    Task task = checkTask(author, nameTask);
    taskRepository.delete(task);
  }

  public void createComment(String author, String nameTask, String text) {
    try {
      findComment(nameTask, author);
      throw new AlreadyExistsException("Комментарий у задачи " + nameTask + " от пользователя " + author + " уже существует");
    } catch (NotFoundException e) {
      Comment comment = new Comment();
      comment.setAuthor(findUser(author));
      comment.setTask(findTask(nameTask));
      comment.setText(text);
      comment.setDate(new Date());
      commentRepository.save(comment);
    } catch (Exception e) {
      log.info(e.getMessage());
      throw e;
    }
  }

  public Page<ResultCommentDto> getComments(String nameTask, PageableDto pageableDto, DataUpOnDownDTO dataUpOnDownDTO) {
    try {
      return commentRepository.findAllByTask(findTask(nameTask), PageRequest.of(pageableDto.getPage(), pageableDto.getSize(), sort(dataUpOnDownDTO))).map(CommentMapper::commentToDto);
    } catch (Exception e) {
      throw new NotFoundException("Комментарии к задаче " + nameTask + " не найдены");
    }
  }

  public ResultCommentDto updateComment(String author, String nameTask, String text) {
    try {
      Comment comment = checkComment(nameTask, author);
      comment.setText(text);
      return CommentMapper.commentToDto(commentRepository.save(comment));
    } catch (Exception e) {
      log.info(e.getMessage());
      throw e;
    }
  }

  public void deleteComment(String author, String nameTask) {
    try {
      Comment comment = checkComment(nameTask, author);
      commentRepository.delete(comment);
    } catch (Exception e) {
      log.info(e.getMessage());
      throw e;
    }
  }

  private User findUser(String email) {
    return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Пользователь " + email + " не найден"));
  }

  private Task findTask(String name) {
    return taskRepository.findByName(name).orElseThrow(() -> new NotFoundException("Задача " + name + " не найдена"));
  }

  private Comment findComment(String task, String author) {
    return commentRepository.findByTaskAndAuthor(findTask(task), findUser(author)).orElseThrow(() -> new NotFoundException("Комментарий " + author + " не найден"));
  }

  private Task checkTask(String author, String name) {
    Task task = findTask(name);
    if (!task.getAuthor().getEmail().equals(author)) {
      throw new BelongException("Задача " + name + " не принадлежит " + author);
    }
    return task;
  }

  private Map<User, Page<Task>> checkTaskOnAuthorOrExecutor(User user, boolean author, PageableDto pageableDto, DataUpOnDownDTO dataUpOnDownDTO) {
    try {
      Map<User, Page<Task>> map = new HashMap<>();
      Page<Task> tasks;
      if (author) {
        tasks = taskRepository.findAllByAuthor(user, PageRequest.of(pageableDto.getPage(), pageableDto.getSize(), sort(dataUpOnDownDTO)));
      } else {
        tasks = taskRepository.findAllByExecutor(user, PageRequest.of(pageableDto.getPage(), pageableDto.getSize(), sort(dataUpOnDownDTO)));
      }
      map.put(user, tasks);
      if (tasks.isEmpty()) {
        throw new NotFoundException("У пользователя " + user.getEmail() + " нет задач");
      }
      return map;
    } catch (Exception e) {
      log.info(e.getMessage());
      throw e;
    }
  }

  private Comment checkComment(String nameTask, String author) {
    Comment comment = findComment(nameTask, author);
    if (!comment.getAuthor().getEmail().equals(author)) {
      throw new BelongException("Комментарий " + author + " не принадлежит " + author);
    }
    return comment;
  }
}
