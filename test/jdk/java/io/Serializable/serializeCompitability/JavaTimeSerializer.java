
import java.io.Serializable;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.YearMonth;
import java.time.chrono.HijrahDate;

public class JavaTimeSerializer implements Serializable {
    private YearMonth yearMonth;
    private MonthDay monthDay;
    private LocalDate localDate;
    private HijrahDate hijrahDate;
}
