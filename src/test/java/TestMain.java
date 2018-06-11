import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import kr.msp.push.pitcher.common.LoadConfig;
import kr.msp.push.pitcher.config.ApplicationConfig;
import kr.msp.push.pitcher.custom.service.PushPitcherService;
import kr.msp.push.pitcher.util.tcpchecker.TcpAliveConManager;
import kr.msp.push.pitcher.worker.push.PushWorkerManager;
import kr.msp.push.pitcher.worker.sms.SmsWorkerManager;
import kr.msp.push.pitcher.worker.zookeeper.productor.SmsProductorManager;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2018-3-20
 * Time: 오전 10:55
 * To change this template use File | Settings | File Templates.
 */
public class TestMain implements Watcher, Closeable {
    private PushPitcherService pushPitcherService;
    private static String SERVER_CONF_FILE = "./conf/config.xml";
    Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public TestMain(){
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
        pushPitcherService = ctx.getBean(PushPitcherService.class);
    }
    public static void main(String[] args){
        try {

            String aaa = "20180423160319";

            String bbb = "20180327121248";

            if(Long.parseLong(aaa)>= Long.parseLong(bbb)){
                System.out.println("aaa가 더 커");
            }else{
                System.out.println("bbb가 더 커");
            }

//            new TestMain().pushPitcherService.inTestPushMsg(10);
//            new TestMain().lastCharConfirm();
//            new TestMain().timeCheck();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void timeCheck() throws Exception{
        String chkTime = "20180329121300";
        SimpleDateFormat dt = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = dt.parse(chkTime);

        System.out.println(date.toString());

        Date curdate = new Date(System.currentTimeMillis());
        System.out.println(curdate.toString());

        System.out.println((curdate.getTime()-date.getTime())/1000);

    }

    private void timeCheck2(){
        String chkTime = "20029311";
    }

    private void lastCharConfirm(){
        String url = "http://211.241.199.243:8080/";

        String url2 = url.substring(url.length()-1);

        System.out.println("#### url2:"+url2);
    }

    private void getZookeeperInfo(){
        try {
            ZooKeeper zk = new ZooKeeper(LoadConfig.getProperty(LoadConfig.ZOOKEEPER_HOSTPORT), 15000, this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void stringChk(){
        SmsProductorManager.getInstance().getQueueCount();


    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if(watchedEvent.getType() == Event.EventType.None){
            switch (watchedEvent.getState()) {
                case SyncConnected:
                    logger.info("######################################################################################");
                    logger.info("###[MASTER NODE] SERVICE OK >>> zookeeper network status changed] : SyncConnected.");
                    logger.info("######################################################################################");
                    // 주키퍼가 정상적으로 작동한다는 이벤트를 받았으므로 있으므로 DB에서 주기적으로 정보를 가져오는 쓰레드 구동 시킴. 주의: 반드시 해당 master NODE가 리더인지 확인 후 구동 해야함.
                    PushWorkerManager.getInstance().changeUseZookeeper();
                    break;
                case Disconnected:
                    logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    logger.error("!!![MASTER NODE]SERVICE ERROR >>> zookeeper network status changed] : Disconnected.");
                    logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    // 주키퍼와 연결이 끊어졌다는 이벤트를 받았을 때 어떻게 처리 해야 할까? 네트웍 문제는 배제하고 어플리케이션 셧다운 개념으로 만 접근 하는게 좋을 듯 하다.
                    // 즉, 통신이 끊긴 이유를 주키퍼가 죽었다고 처리할 경우 : 주키퍼 없이 보내는 로직으로 변경
                    //TODO : 주키퍼와 연결이 끊겼을 경우 자신의 UPMC말고 다른 UPMC와 통신이 되는지 확인 후 된다 면 주키퍼가 죽은 것으로 판단하고 마스터일 경우 마스터 roll 수행하고 주키퍼를 사용하지 않는 worker 구동.
                    //TODO : 경우의 수 : 1.주키퍼가 문제인 경우. 2.DB Pitcher 네트웍이 문제인 경우.
                    //TODO : 이 두가지의 경우 중 1일 경우는 마스터일 경우 주키퍼 사용하지 않는걸로 바꾸면 될 것 같음.
                    //TODO : 주키퍼 미사용으로 처리방법 : 마스터 roll일 경우는 LoadConfig.ZOOKEEPER_USEYN을 N롤 셋팅후 PushWorkerManager에 있는 PuDBFetcherThread를 살리고 ZooKeeperTaskProductor stop PushWorkerThread start 하면 될것 같음.

                    try {
                        logger.debug("### TcpAliveConManager.getInstance().getAliveHostNameList().size():"+ TcpAliveConManager.getInstance().getAliveHostNameList().size());
                        if (TcpAliveConManager.getInstance().getAliveHostNameList().size() > 0) {
                            // 한번 더 확인
                            long chkMiliTime = TcpAliveConManager.getInstance().getConnetionRetryTime();
                            long sleepMiliTime = (TcpAliveConManager.getInstance().getAliveHostNameList().size() * 3000) + chkMiliTime;
                            Thread.sleep(sleepMiliTime + 5000);
                            logger.debug("### dead zookeeper or my network check~!");
                            if (TcpAliveConManager.getInstance().getAliveHostNameList().size() > 0) {
                                // TODO : 로컬에 설치된 UPMC말고 다른 UPMC와 연결이 되어있으므로 주키퍼가 죽은것으로 판단 가능함.
                                logger.debug("### change not use zookeeper call~~!");
                                PushWorkerManager.getInstance().changeNotUseZookeeper();
                                SmsWorkerManager.getInstance().changeNotUseZookeeper();
                            }
                        }
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }
                    break;
                case Expired:
                    logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    logger.error("!!![MASTER NODE]SERVICE ERROR >>> zookeeper network status changed] : Expired.");
                    logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                default:
                    break;
            }
        }
    }





}
