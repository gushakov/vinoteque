package vinoteque.junit;

import java.util.Properties;
import viniteque.config.AppConfig;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import vinoteque.utils.Utils;

import static org.junit.Assert.*;

public class TestMainWindow {
    private static final Logger logger = Logger.getLogger(TestMainWindow.class);
    
    @Test
    public void getAppVersion() throws Exception {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        Utils.writeProperties((Properties)ctx.getBean("appProps"));
    }

}
