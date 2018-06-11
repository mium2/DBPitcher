package kr.msp.push.pitcher.custom.worker;

import kr.msp.push.pitcher.common.LoadConfig;
import kr.msp.push.pitcher.custom.service.PushPitcherService;
import kr.msp.push.pitcher.worker.push.IPushDBFetcherJob;
import kr.msp.push.pitcher.worker.push.PushWorkBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Y.B.H(mium2) on 18. 5. 11..
 * 설명 : 해당 클래스는 UPMC로 푸시발송 해야 할 데이타를 주기적으로 DB로 부터 가져와 리스트에 담는 일을 처리 하는 클래스 입니다.
 */
@Service
public class CustomPushDBFetcher implements IPushDBFetcherJob {
    @Autowired(required = true)
    PushPitcherService pushPitcherService;

    private Logger logger = LoggerFactory.getLogger(CustomPushDBFetcher.class);

    public static long LAST_GET_PITCHER_SEQNO = 0;

    /**
     * 해당 메소드는 구동시 한번 만 실행됨
     * @throws Exception
     */
    @Override
    public void beforeFetchDBJob() throws Exception {
        /**
         * 어플리케이션을 종료 후 오랜 시간이 지난 뒤에 올렸을 때 너무 오래된 메세지는 보내지 않고 삭제 처리 한다.
         */
        int delRowCnt = pushPitcherService.oldDontSendMsgDel(LoadConfig.getIntProperty(LoadConfig.PUSH_EXPIREPUSHMIN));

        if (delRowCnt > 0) {
            logger.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            logger.warn("!! [DEL PUSH OLD MSG] Deletion processing of expired messages : {}", delRowCnt);
            logger.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    /**
     * 해당 메소드는 config에서 설정한 시간 주기로 계속 반복 호출됨
     * 설명 : 푸시 발송할 데이타를 일정 수 만큼 DB에서 가져와 리스트에 담는 로직 구현.
     * @return
     * @throws Exception
     */
    @Override
    public List<PushWorkBean> fetchDBJob() throws Exception {

        List<PushWorkBean> pushWorkBeans = new ArrayList<PushWorkBean>();
        logger.trace("[PushDBFetcherThread] PushDBFetcherThread sleep MiliTime:" + LoadConfig.getIntProperty(LoadConfig.PUSH_DB_WATCHMILITIME));
        List<Object> pitcherWorkBeanList = pushPitcherService.selPitcherMsg(LAST_GET_PITCHER_SEQNO);
        int loopcnt = pitcherWorkBeanList.size();
        for(int i=0; i<loopcnt; i++){
            PushWorkBean dbData = (PushWorkBean)pitcherWorkBeanList.get(i);
            PushWorkBean pushWorkBean = new PushWorkBean();
            pushWorkBean.setPITCHER_SEQNO(dbData.getPITCHER_SEQNO());  //필수. 중요: 해당 Primary Key를 이용하여 삭제됨.
            pushWorkBean.setAPP_ID(dbData.getAPP_ID()); //필수
            pushWorkBean.setCUID(dbData.getCUID());  //필수
            pushWorkBean.setMESSAGE(dbData.getMESSAGE()); // 필수
            pushWorkBean.setSERVICECODE(dbData.getSERVICECODE()); //필수 (ALL2, PRIVATE, PUBLIC)
            pushWorkBean.setTYPE(dbData.getTYPE());  //필수 E(개별),M(회원),N(비회원),S(시스템발송-메세지원장테이블 저장안됨),C(CSV),A(전체),G(그룹) 중 발송타입 택일
            pushWorkBean.setEXT(dbData.getEXT()); //선택
            pushWorkBean.setDB_IN("N"); //선택
            pushWorkBean.setPUSH_FAIL_SMS_SEND("Y");    //선택
            pushWorkBean.setSMS_READ_WAIT_MINUTE("0");  //선택
            pushWorkBean.setSUB_TITLE("");       //선택
            pushWorkBean.setSPLIT_MSG_CNT("0");  //선택
            pushWorkBean.setDELAY_SECOND("0");   //선택
            pushWorkBean.setDOZ_GCM_SEND("Y");   //선택
            pushWorkBean.setSEND_TIME_LIMIT(""); //선택

            pushWorkBeans.add(pushWorkBean);
            if(i==(loopcnt-1)){
                //조회된 마지막 데이타 부터 다시 조회해야 되므로 인덱스키인 PITCHER_SEQNO값을 저장한다.
                LAST_GET_PITCHER_SEQNO = dbData.getPITCHER_SEQNO();
            }
        }
        return pushWorkBeans;
    }

    /**
     * fetchDBJob()과 마찬가지로 계속 반복 호출됨
     * 가져온 정보를 발송큐에 저장 완료 후 할일이 있으면 구현하면됨.
     * @throws Exception
     */
    @Override
    public void afterfetchDBJob() throws Exception {

    }

    /**
     * fetchDBJob()과 마찬가지로 계속 반복 호출됨.
     * 발송할 데이타가 DB에 없을 경우 호출 됨.
     * @return
     * @throws Exception
     */
    @Override
    public List<PushWorkBean> fetchNoneDBIdleJob() throws Exception {
        return null;
    }



}
