package TaskManagementSystem.mapper;

import TaskManagementSystem.dto.ResultTaskDto;
import TaskManagementSystem.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskMapper {
    public static ResultTaskDto taskToDto(Task task) {
        return ResultTaskDto.builder()
                .name(task.getName())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .author(UserMapper.userToDto(task.getAuthor()))
                .executor(UserMapper.userToDto(task.getExecutor()))
                .build();
    }

    public static List<ResultTaskDto> taskToDto(List<Task> tasks) {
        List<ResultTaskDto> result = new ArrayList<>();
        for (Task task : tasks) {
            result.add(taskToDto(task));
        }
        return result;
    }
}
