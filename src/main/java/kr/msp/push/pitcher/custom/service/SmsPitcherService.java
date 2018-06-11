package kr.msp.push.pitcher.custom.service;

import kr.msp.push.pitcher.service.MspDB.ISmsPitcherService;
import kr.msp.push.pitcher.util.mybatis.MyBatisSupport;
import kr.msp.push.pitcher.worker.sms.SmsWorkBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Y.B.H(mium2) on 18. 4. 5..
 * 유라클에서 푸시발송 실패 또는 미수신 푸시메세지를  T_PUSH_SMS테이블에 등록하는 데 해당테이블에서 SMS 발송 메세지를
 */
@Service
public class SmsPitcherService extends MyBatisSupport implements ISmsPitcherService{
    @Autowired(required = true)
    @Qualifier("mspSessionTemplate")
    private SqlSessionTemplate sqlSessionTemplate;

    @Autowired(required = true)
    private Properties myProperties;
    private Logger logger = LoggerFactory.getLogger(SmsPitcherService.class);
    private
    @Value("${DBTYPE:ORACLE}")
    String dbType;


    public List<SmsWorkBean> selPitcherMsg(Map<String,String> paramMap) throws Exception {
        List<SmsWorkBean> smsWorkBeans = sqlSessionTemplate.selectList("smsPitchermsg.selSmsPitcherMsg", paramMap);
        return smsWorkBeans;
    }

    public int delPitcherMsg(Map<String,String> paramMap) throws Exception {
        int applyRow = sqlSessionTemplate.delete("smsPitchermsg.delSmsPitcherMsg", paramMap);
        return applyRow;
    }

    public int upSuccTableUpSms(SmsWorkBean smsWorkBean) throws Exception {
        int applyRow = sqlSessionTemplate.update("smsPitchermsg.upSuccTableUpSms", smsWorkBean);
        return applyRow;
    }

    public int upFailTableUpSms(SmsWorkBean smsWorkBean) throws Exception {
        int applyRow = sqlSessionTemplate.update("smsPitchermsg.upFailTableUpSms", smsWorkBean);
        return applyRow;
    }


}