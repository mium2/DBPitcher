package kr.msp.push.pitcher.custom.sms_provider;

import kr.msp.push.pitcher.common.LoadConfig;
import kr.msp.push.pitcher.util.MessageIDGenerator;
import kr.msp.push.pitcher.util.StringUtil;
import kr.msp.push.pitcher.worker.sms.SmsWork;
import kr.msp.push.pitcher.worker.sms.SmsWorkBean;
import kr.msp.push.pitcher.worker.sms.SmsWorkerManager;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Y.B.H(mium2) on 18. 4. 3..
 */
@Component
public class LGU_SmsWorkImpl extends SmsWork {
    @Override
    public boolean sendSmsProcess(SmsWorkBean smsWorkBean) {
        try {
            Map<String, String> paramMap = new HashMap<String, String>();
            String lgu_etc = LoadConfig.getProperty(LoadConfig.SMS_LGU_ETC);
            String[] etcArr = lgu_etc.split("\\|");
            for(int i=0; i<etcArr.length; i++){
                String[] keyValueArr = etcArr[i].split("=");
                paramMap.put(keyValueArr[0],keyValueArr[1]);
            }
            String SND_DTTM = getSysDateTime();
            //CMP_MSG_ID(회사사용하는 메세지아이디),CMP_USR_ID(),RCV_PHN_ID(받는사람 핸드폰 번호),CALLBACK,SND_MSG
            paramMap.put("CMP_MSG_ID",SND_DTTM+ MessageIDGenerator.getInstance().next());
            paramMap.put("CMP_USR_ID", LoadConfig.getProperty(LoadConfig.SMS_SENDER));
            paramMap.put("RCV_PHN_ID", SmsWorkerManager.getInstance().getLegacyDbService().getPhoneNum(smsWorkBean.getCUID())); //[SMS/MMS 공통] 핸드폰 번호
            paramMap.put("CALLBACK", LoadConfig.getProperty(LoadConfig.SMS_CALLBACKNUM)); //[SMS/MMS 공통] 발송자 번호
            paramMap.put("SND_MSG",smsWorkBean.getTITLE());
            paramMap.put("SND_DTTM",SND_DTTM);
            paramMap.put("WRT_DTTM",SND_DTTM);

            SmsWorkerManager.getInstance().getSmsDbService().lguSendSms(paramMap);

            return true;
        }catch (Exception e){
            smsWorkBean.setRESULT_CODE("500");
            smsWorkBean.setRESULT_MSG(e.toString());
            e.printStackTrace();
            return false;
        }
    }

    private String getSysDateTime(){
        // 현재시간을 구할 수 있는 객체 생성
        Calendar calendar = Calendar.getInstance();
        StringBuffer buffer = new StringBuffer();

        String year = StringUtil.toZoroString(calendar.get(Calendar.YEAR), 4);
        String month = StringUtil.toZoroString(calendar.get(Calendar.MONTH) + 1, 2);
        String day = StringUtil.toZoroString(calendar.get(Calendar.DATE), 2);
        String hour = StringUtil.toZoroString(calendar.get(Calendar.HOUR_OF_DAY), 2);
        String minute = StringUtil.toZoroString(calendar.get(Calendar.MINUTE), 2);
        String second = StringUtil.toZoroString(calendar.get(Calendar.SECOND), 2);

        buffer.append(year);
        buffer.append(month);
        buffer.append(day);
        buffer.append(hour);
        buffer.append(minute);
        buffer.append(second);

        return buffer.toString();
    }
}
