import java.text.SimpleDateFormat;
import java.util.Date;

public class Constants {
    public static String VERSION = "pre-alpha-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    public static String NAME = "Render Test";
    public static long TIME_STEP = 1000/60;

}
