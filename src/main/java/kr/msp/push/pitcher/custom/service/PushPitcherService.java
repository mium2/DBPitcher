package kr.msp.push.pitcher.custom.service;

import kr.msp.push.pitcher.service.MspDB.IPushPitcherService;
import kr.msp.push.pitcher.util.mybatis.MyBatisSupport;
import kr.msp.push.pitcher.worker.push.PushWorkBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
/**
 * Created by Y.B.H(mium2) on 2018. 4. 5..
 */
@Service
public class PushPitcherService extends MyBatisSupport implements IPushPitcherService {
    @Autowired(required = true)
    @Qualifier("legacyPushSessionTemplate")
    private SqlSessionTemplate sqlSessionTemplate;
    @Autowired(required = true)
    private Properties myProperties;
    private Logger logger = LoggerFactory.getLogger(PushPitcherService.class);
    private @Value("${DBTYPE:ORACLE}")
    String dbType;

    /**
     * DB에 저장된 푸시발송요청 원장 데이타 발송완료 처리후 삭제처리.
     * @param pushWorkBean
     * @return
     * @throws Exception
     */
    public int delPitcherMsg(final PushWorkBean pushWorkBean) throws Exception{
        // TODO : 이곳은 UPMC까지 발송이 완료(성공,실패 둘다) 된 후 처리해야 할 일을 구현한다.  성공/실패 여부는 RESULT_CODE "0000" 일경우 성공 나머지는 실패 임.
        int applyRow = sqlSessionTemplate.delete("pushPitchermsg.delPitcherMsg", pushWorkBean);
        return applyRow;
    }

    /**
     * 푸시발송 할 데이타 가져오기
     * @param pushPitcherSeqno
     * @return
     * @throws Exception
     */
    public List<Object> selPitcherMsg(final long pushPitcherSeqno) throws Exception{
        List<Object>  pitcherWorkBeans = sqlSessionTemplate.selectList("pushPitchermsg.selPitcherMsg2000", pushPitcherSeqno);
        return pitcherWorkBeans;
    }

    /**
     * 푸시 발송 완료된 데이타 완료 테이블에 저장시 사용.
     * @param pushWorkBean
     * @return
     * @throws Exception
     */
    public int inPitcherCompletedMsg(final PushWorkBean pushWorkBean) throws Exception{
        int applyRow = sqlSessionTemplate.update("pushPitchermsg.inPitcherCompletedMsg", pushWorkBean);
        return applyRow;
    }

    /**
     * 처음 구동시 오래전에 보내지 못한 데이타 보내지 않기 위해 지워내기.
     * @param expireMin
     * @return
     */
    public int oldDontSendMsgDel(int expireMin){
        Map<String,Object> paramMap = new HashMap<String, Object>();
        paramMap.put("expireMin",expireMin);
        int applyRow = sqlSessionTemplate.delete("pushPitchermsg.oldDelPitcherMsg", paramMap);
        return applyRow;
    }

    @Deprecated
    public List<Object> selAllPitcherMsg(int expireMin) throws Exception{
        Map<String,Object> paramMap = new HashMap<String, Object>();
        paramMap.put("expireMin",expireMin);
        List<Object>  pitcherWorkBeans = sqlSessionTemplate.selectList("pushPitchermsg.selAllPitcherMsg", paramMap);
        return pitcherWorkBeans;
    }
}
