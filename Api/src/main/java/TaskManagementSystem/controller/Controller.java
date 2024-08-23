package TaskManagementSystem.controller;

import TaskManagementSystem.dto.*;
import TaskManagementSystem.model.SortByDate;
import TaskManagementSystem.service.TaskService;
import TaskManagementSystem.service.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
public class Controller {
  private static final Logger log = LoggerFactory.getLogger(Controller.class);

  private final UserService userService;
  private final TaskService taskService;

  public Controller(UserService userService, TaskService taskService) {
    this.userService = userService;
    this.taskService = taskService;
  }

  //авторизация
  @PostMapping("/auth")
  public ResponseEntity<?> createAuthenticationToken(@RequestBody UserDto userDto) {
    log.info("Post /auth. User: {}", userDto.getEmail());
    return userService.authenticateUser(userDto);
  }

  //регистрация
  @PostMapping("/registration")
  @ResponseStatus(HttpStatus.CREATED)
  public String createNewUser(@RequestBody UserDto userDto) {
    log.info("Post /registration. User: {}", userDto.getEmail());
    userService.createNewUser(userDto);
    return "Пользователь " + userDto.getEmail() + " создан";
  }

  //удаление пользователя
  @DeleteMapping("/deleteUser")
  @Transactional
  public String deleteUser(Principal principal) {
    String userName = principal.getName();
    log.info("Delete /deleteUser. User: {}", userName);
    taskService.deleteAuthor(userName);
    taskService.deleteExecutor(userName);
    userService.deleteUser(userName);
    return "Пользователь " + userName + " удален";
  }

  //создание задачи
  @PostMapping("/createTask")
  @ResponseStatus(HttpStatus.CREATED)
  @Transactional
  public String createTask(Principal principal, @RequestBody TaskDto taskDto) {
    log.info("Post /createTask. Task: {}", taskDto.getName());
    taskService.createTask(principal.getName(), taskDto);
    return "Задача " + taskDto.getName() + " создана";
  }

  //получение созданных вами задач
  @GetMapping("/getMyCreateTasks/{page},{size}/{dataUpOnDown}")
  @Transactional
  public Page<ResultTaskDto> getMyCreateTasks(Principal principal, @PathVariable("page") int page, @PathVariable("size") int size, @PathVariable("dataUpOnDown") String dataUpOnDown) {
    log.info("Get /getMyCreateTasks. User: {}", principal.getName());
    return taskService.getAuthorTasks(principal.getName(), new PageableDto(page, size), new DataUpOnDownDTO(SortByDate.getSortByDate(dataUpOnDown)));
  }

  //получение задачу по названию
  @GetMapping("/getTaskByName/{nameTask}")
  @Transactional
  public ResultTaskDto getTaskByName(@PathVariable("nameTask") String nameTask) {
    log.info("Get /getTaskByName. Task: {}", nameTask);
    return taskService.getTask(nameTask);
  }

  //получение созданных задач пользователя
  @GetMapping("/getAuthorTasks/{author}/{page},{size}/{dataUpOnDown}")
  @Transactional
  public Page<ResultTaskDto> getAuthorTasks(@PathVariable("author") String author, @PathVariable("page") int page, @PathVariable("size") int size, @PathVariable("dataUpOnDown") String dataUpOnDown) {
    log.info("Get /getAuthorTasks. User: {}", author);
    return taskService.getAuthorTasks(author, new PageableDto(page, size), new DataUpOnDownDTO(SortByDate.getSortByDate(dataUpOnDown)));
  }

  //получение всех задач
  @GetMapping("/getAllTasks/{page},{size}/{dataUpOnDown}")
  @Transactional
  public Page<ResultTaskDto> getAllTasks(@PathVariable("page") int page, @PathVariable("size") int size, @PathVariable("dataUpOnDown") String dataUpOnDown) {
    log.info("Get /getAllTasks");
    return taskService.getAllTasks(new PageableDto(page, size), new DataUpOnDownDTO(SortByDate.getSortByDate(dataUpOnDown)));
  }

  //изменение задачи
  @PatchMapping("/updateTask/{nameTask}")
  @Transactional
  public ResultTaskDto updateTask(Principal principal, @PathVariable("nameTask") String nameTask, @RequestBody TaskDto taskDto) {
    log.info("Patch /updateTask. Task: {}", taskDto.getName());
    return taskService.updateTask(principal.getName(), nameTask, taskDto);
  }

  //повышение приоритета задачи
  @PatchMapping("/updatePriorityUp/{nameTask}")
  @Transactional
  public ResultTaskDto updatePriorityUp(Principal principal, @PathVariable("nameTask") String nameTask) {
    log.info("Patch /updatePriorityUp. Task: {}", nameTask);
    return taskService.updatePriorityUp(principal.getName(), nameTask);
  }

  //понижение приоритета задачи
  @PatchMapping("/updatePriorityDown/{nameTask}")
  @Transactional
  public ResultTaskDto updatePriorityDown(Principal principal, @PathVariable("nameTask") String nameTask) {
    log.info("Patch /updatePriorityDown. Task: {}", nameTask);
    return taskService.updatePriorityDown(principal.getName(), nameTask);
  }

  //завершение задачи
  @PatchMapping("/updateStatusComplete/{nameTask}")
  @Transactional
  public ResultTaskDto updateStatusDone(Principal principal, @PathVariable("nameTask") String nameTask) {
    log.info("Patch /updateStatusComplete. Task: {}", nameTask);
    return taskService.updateStatusComplete(principal.getName(), nameTask);
  }

  //изменение исполнителя
  @PatchMapping("/setExecutor/{nameTask}/{executor}")
  @Transactional
  public ResultTaskDto setExecutor(Principal principal, @PathVariable("nameTask") String nameTask, @PathVariable("executor") String executor) {
    log.info("Patch /setExecutor. Task: {}", nameTask);
    if (executor.isEmpty() || executor.equals("null") || executor.equals("delete") || executor.equals("")) {
      executor = null;
    }
    return taskService.setExecutor(principal.getName(), nameTask, executor);
  }

  //получение задачи для выполнения
  @PatchMapping("/I_will_work_on_this_task/{nameTask}")
  @Transactional
  public ResultTaskDto I_will_work_on_this_task(Principal principal, @PathVariable("nameTask") String nameTask) {
    log.info("Patch /I_will_work_on_this_task. Task: {}. User: {}", nameTask, principal.getName());
    return taskService.I_will_work_on_this_task(principal.getName(), nameTask);
  }

  //получение задач над которыми вы работаете
  @GetMapping("/getMyWorkTasks/{page},{size}/{dataUpOnDown}")
  @Transactional
  public Page<ResultTaskDto> getMyWorkTasks(Principal principal, @PathVariable("page") int page, @PathVariable("size") int size, @PathVariable("dataUpOnDown") String dataUpOnDown) {
    log.info("Get /getMyWorkTasks. User: {}", principal.getName());
    return taskService.getMyWorkTasks(principal.getName(), new PageableDto(page, size), new DataUpOnDownDTO(SortByDate.getSortByDate(dataUpOnDown)));
  }

  //удаление задачи
  @DeleteMapping("/deleteTask/{nameTask}")
  @Transactional
  public String deleteTask(Principal principal, @PathVariable("nameTask") String nameTask) {
    log.info("Delete /deleteTask. Task: {}", nameTask);
    taskService.deleteTask(principal.getName(), nameTask);
    return "Задача " + nameTask + " удалена";
  }

  //создание комментария
  @PostMapping("/createComment/{nameTask}")
  @ResponseStatus(HttpStatus.CREATED)
  @Transactional
  public String createComment(Principal principal, @PathVariable("nameTask") String nameTask, @RequestBody CommentDto commentDto) {
    log.info("Post /createComment. Task: {}", nameTask);
    taskService.createComment(principal.getName(), nameTask, commentDto.getText());
    return "Комментарий добавлен";
  }

  //изменение комментария
  @PatchMapping("/updateComment/{nameTask}")
  @Transactional
  public ResultCommentDto updateComment(Principal principal, @PathVariable("nameTask") String nameTask, @RequestBody CommentDto commentDto) {
    log.info("Patch /updateComment. Task: {}", nameTask);
    return taskService.updateComment(principal.getName(), nameTask, commentDto.getText());
  }

  //получение комментариев задачи
  @GetMapping("/getComments/{nameTask}/{page},{size}/{dataUpOnDown}")
  @Transactional
  public Page<ResultCommentDto> getComments(@PathVariable("nameTask") String nameTask, Principal principal, @PathVariable("page") int page, @PathVariable("size") int size, @PathVariable("dataUpOnDown") String dataUpOnDown) {
    log.info("Get /getComments. Task: {}", nameTask);
    return taskService.getComments(nameTask, new PageableDto(page, size), new DataUpOnDownDTO(SortByDate.getSortByDate(dataUpOnDown)));
  }

  //удаление комментария
  @DeleteMapping("/deleteComment/{nameTask}")
  @Transactional
  public String deleteComment(Principal principal, @PathVariable("nameTask") String nameTask) {
    log.info("Delete /deleteComment. Task: {}", nameTask);
    taskService.deleteComment(principal.getName(), nameTask);
    return "Комментарий удален";
  }
}
