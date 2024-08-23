package TaskManagementSystem.mapper;

import TaskManagementSystem.dto.ResultCommentDto;
import TaskManagementSystem.model.Comment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommentMapper {
    public static ResultCommentDto commentToDto(Comment comment) {
        return ResultCommentDto.builder()
                .text(comment.getText())
                .author(UserMapper.userToDto(comment.getAuthor()))
                .date(comment.getDate())
                .build();
    }

    public static List<ResultCommentDto> commentsToDto(Collection<Comment> comments) {
        List<ResultCommentDto> result = new ArrayList<>();
        for (Comment comment : comments) {
            result.add(commentToDto(comment));
        }
        return result;
    }
}
