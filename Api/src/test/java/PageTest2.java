import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageTest2 {
    List<Comment> content;
    int totalPages;
    int totalElements;
    int size;

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Comment {
        String text;
    }
}
