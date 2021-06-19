package za.co.learnings.todolist.api.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static java.time.LocalDateTime.ofInstant;

public class DateUtil {

    private DateUtil() {
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return date == null ?
                null :
                ofInstant(date.toInstant(),
                ZoneId.systemDefault());
    }
}
