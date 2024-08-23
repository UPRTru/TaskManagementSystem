package TaskManagementSystem.model;

public enum SortByDate {
    UP,
    DOWN,
    NONE;

    public static SortByDate getSortByDate(String sortBy) {
        if (sortBy.equals("UP") || sortBy.equals("up")) {
            return SortByDate.UP;
        } else if (sortBy.equals("DOWN") || sortBy.equals("down")) {
            return SortByDate.DOWN;
        } else {
            return SortByDate.NONE;
        }
    }
}
