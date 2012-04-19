package viniteque.config;

import java.io.IOException;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import vinoteque.db.HsqldbDao;

/**
 * Spring configuration file.
 * @author gushakov
 */
@Configuration
@EnableTransactionManagement
public class AppConfig {
    @Value("${jdbc.driverClassName}")
    private String jdbcDriverClassName;
    @Value("${jdbc.url}")
    private String jdbcUrl;
    @Value("${jdbc.username}")
    private String jdbcUsername;
    @Value("${jdbc.password}")
    private String jdbcPassword;

    @Bean(name="appProps")
    public static Properties appProps(){
        try {
            Resource file = new ClassPathResource("app.properties");
            Properties props = new Properties();
            props.load(file.getInputStream());
            return props;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertiesConfigurer(){
        PropertySourcesPlaceholderConfigurer propertiesConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertiesConfigurer.setProperties(appProps());
        return propertiesConfigurer;
    }
    
    @Bean(destroyMethod="close")
    public DataSource dataSource(){
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(jdbcDriverClassName);
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(jdbcUsername);
        dataSource.setPassword(jdbcPassword);
        return dataSource;
    }
    
    @Bean
    public HsqldbDao hsqldbDao(){
        return new HsqldbDao(dataSource());
    }
    
    @Bean
    public DataSourceTransactionManager transactionManager(){
        return new DataSourceTransactionManager(dataSource());
    }
}
