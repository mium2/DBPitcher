import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import kr.msp.push.pitcher.common.LoadConfig;
import kr.msp.push.pitcher.config.ApplicationConfig;
import kr.msp.push.pitcher.monitoring.zookeeper.ZookeeperMetrics;
import kr.msp.push.pitcher.monitoring.zookeeper.ZookeeperMonitor;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Y.B.H(mium2) on 18. 2. 1..
 */
public class ZookeeperTestClient {
    private static String SERVER_CONF_FILE = "./conf/config.xml";
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    public static final String CONFIG_ARG = "config-file";
    private Gson gson = new Gson();

    public ZookeeperTestClient(){
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure("./conf/logback.xml");
        } catch (JoranException je) {
            logger.error(je.getMessage());
            return;
        }

        try {
            LoadConfig.Load(SERVER_CONF_FILE);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        ApplicationContext ctx = new AnnotationConfigApplicationContext(ApplicationConfig.class);
    }
    public static void main(String[] args){

        ZookeeperTestClient zookeeperTestClient = new ZookeeperTestClient();
        try {
            zookeeperTestClient.getZookeeperInfo();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getZookeeperInfo() {
        List<String> zookeeperList = new ArrayList<String>();
        String zookeeperStr = LoadConfig.getProperty(LoadConfig.ZOOKEEPER_HOSTPORT);
        if(zookeeperStr.indexOf(",")>0){
            String[] zookeeperStrArr = zookeeperStr.split(",");
            System.getProperty("ZOOKEEPER_CNT",""+zookeeperStr.length());
            for(int i=0; i<zookeeperStrArr.length; i++){
                zookeeperList.add(zookeeperStrArr[i].trim());
            }
        }else{
            zookeeperList.add(zookeeperStr.trim());
            System.getProperty("ZOOKEEPER_CNT","1");
        }

        ZookeeperMonitor zookeeper = ZookeeperMonitor.getInstance();
        Map<String,String> taskArgs = Maps.newHashMap();
        List<ZookeeperMetrics> zookeeperMetricses = zookeeper.execute(zookeeperList);

        List<Map<String,String>> zookeeperStatusList = new ArrayList<Map<String, String>>();
        for(ZookeeperMetrics zookeeperMetric : zookeeperMetricses){
            Map<String,String> zookeeperInfoMap = zookeeperMetric.getMetrics();
            zookeeperInfoMap.put("display_name", zookeeperMetric.getDisplayName());
            zookeeperStatusList.add(zookeeperInfoMap);
        }

        System.out.println(gson.toJson(zookeeperStatusList));

    }

}
