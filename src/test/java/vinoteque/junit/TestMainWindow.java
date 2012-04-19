package vinoteque.junit;

import java.util.Properties;
import org.apache.log4j.Logger;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import viniteque.config.AppConfig;


public class TestMainWindow {
    private static final Logger logger = Logger.getLogger(TestMainWindow.class);
    
    @Test
    public void getAppVersion() throws Exception {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        //Utils.writeProperties((Properties)ctx.getBean("appProps"));
        Properties props = (Properties) ctx.getBean("appProps");
        assertNotNull(props);
    }

}
