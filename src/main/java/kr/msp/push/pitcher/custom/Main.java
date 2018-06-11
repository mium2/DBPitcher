package kr.msp.push.pitcher.custom;

import kr.msp.push.pitcher.DBPitcherMain;

/**
 * Created by Y.B.H(mium2) on 18. 5. 11..
 */
public class Main {
    public static void main(String[] args) throws Exception {
        // 어플리케이션 구동시 초기화 작업(config.xml, logback.xml, Spring application...)
        DBPitcherMain.getInstance().init(args, CustomApplicationConfig.class);

        // 실질적인 Daemon 서비스 구동
        DBPitcherMain.getInstance().startDaemon();

    }
}
