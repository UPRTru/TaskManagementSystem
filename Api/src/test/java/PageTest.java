import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageTest {
    List<Task> content;
    int totalPages;
    int totalElements;
    int size;

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Task {
        String name;
        String description;
        String status;
        String priority;
    }
}
