package kr.msp.push.pitcher.custom;

import com.jolbox.bonecp.BoneCPDataSource;
import kr.msp.push.pitcher.common.LoadConfig;
import kr.msp.push.pitcher.config.ApplicationConfig;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * Created by Y.B.H(mium2) on 18. 5. 11..
 */
@ComponentScan("kr.msp.push.pitcher.custom")
public class CustomApplicationConfig extends ApplicationConfig{

    // Push Fetch legacy DB 시작
    @Bean(destroyMethod = "close")
    public DataSource legacyPushDataSource() {
        logger.info("###[Push Fetch LEGCY DB] dataSource load start");
        String JDBC_URL = LoadConfig.getProperty(LoadConfig.LEGACY_PUSH_JDBC_URL);
        String JDBC_USERNAME = LoadConfig.getProperty(LoadConfig.LEGACY_PUSH_JDBC_USERNAME);
        String JDBC_PASSWORD = LoadConfig.getProperty(LoadConfig.LEGACY_PUSH_JDBC_PASSWORD);
        if(LoadConfig.getProperty(LoadConfig.LEGACY_PUSH_DBCP_ENC).equals("Y")){
            JDBC_URL = getDbcpDecode(JDBC_URL);
            JDBC_USERNAME = getDbcpDecode(JDBC_USERNAME);
            JDBC_PASSWORD = getDbcpDecode(JDBC_PASSWORD);
        }

        BoneCPDataSource ds = new BoneCPDataSource();
        ds.setDriverClass(LoadConfig.getProperty(LoadConfig.LEGACY_PUSH_JDBC_DRIVERCLASSNAME));
        ds.setJdbcUrl(JDBC_URL);
        ds.setUsername(JDBC_USERNAME);
        ds.setPassword(JDBC_PASSWORD);
        ds.setMinConnectionsPerPartition(LoadConfig.getIntProperty(LoadConfig.LEGACY_PUSH_MINCONNECTIONCNT));
        ds.setMaxConnectionsPerPartition(LoadConfig.getIntProperty(LoadConfig.LEGACY_PUSH_MAXCONNECTIONCNT));
        ds.setIdleMaxAgeInSeconds(LoadConfig.getIntProperty(LoadConfig.LEGACY_PUSH_IDLEMAXSECONDS));
        //Mysql : SELECT 1, oracle SELECT 1 from dual
        ds.setConnectionTestStatement(LoadConfig.getProperty(LoadConfig.LEGACY_PUSH_CONNECTIONTESTSTATEMENT));
        ds.setIdleConnectionTestPeriodInMinutes(LoadConfig.getIntProperty(LoadConfig.LEGACY_PUSH_IDLECONNECTIONTESTPERIODMIN));
        ds.setDisableConnectionTracking(true);
        logger.info("###[Push Fetch LEGCY DB] dataSource completed");
        return ds;
    }

    @Bean
    public SqlSessionFactory legacyPushSqlSessionFactory(DataSource legacyPushDataSource, ApplicationContext applicationContext) throws Exception {
        logger.info("###[Push Fetch LEGCY DB] sqlSessionFactory start");
        String mapperSrc= "classpath:sqlMap/"+ LoadConfig.getProperty(LoadConfig.LEGACY_PUSH_DBTYPE)+"/*.xml";
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setConfigLocation(applicationContext.getResource("classpath:sqlMap/configuration.xml"));
        bean.setDataSource(legacyPushDataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(mapperSrc));
        logger.info("###[Push Fetch LEGCY DB] sqlSessionFactory completed");
        return bean.getObject();
    }

    @Bean
    @Qualifier("legacyPushSessionTemplate")
    public SqlSessionTemplate legacyPushSqlSession(SqlSessionFactory legacyPushSqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(legacyPushSqlSessionFactory);
    }
    // legacy DB마침
}
