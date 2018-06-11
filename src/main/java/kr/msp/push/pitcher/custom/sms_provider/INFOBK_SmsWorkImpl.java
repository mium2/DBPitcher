package kr.msp.push.pitcher.custom.sms_provider;

import kr.msp.push.pitcher.common.LoadConfig;
import kr.msp.push.pitcher.worker.sms.SmsWork;
import kr.msp.push.pitcher.worker.sms.SmsWorkBean;
import kr.msp.push.pitcher.worker.sms.SmsWorkerManager;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Y.B.H(mium2) on 18. 4. 3..
 */

public class INFOBK_SmsWorkImpl extends SmsWork {
    @Override
    public boolean sendSmsProcess(SmsWorkBean smsWorkBean) {
        try {
            int msgTotalSize = 0;
            byte[] titleBytes = smsWorkBean.getTITLE().getBytes("euc-kr");
            msgTotalSize = titleBytes.length;
            if (!smsWorkBean.getSUB_TITLE().equals("")) {
                byte[] subTitleBytes = smsWorkBean.getSUB_TITLE().getBytes("euc-kr");
                msgTotalSize += subTitleBytes.length;
            }
            Map<String, String> paramMap = new HashMap<String, String>();
            // SMS : TARGET,CALLBACK,MSG,SENDER   MMS : TARGET,CALLBACK,MSG,TITLE
            paramMap.put("TARGET", SmsWorkerManager.getInstance().getLegacyDbService().getPhoneNum(smsWorkBean.getCUID())); //[SMS/MMS 공통] 핸드폰 번호
            paramMap.put("CALLBACK", LoadConfig.getProperty(LoadConfig.SMS_CALLBACKNUM)); //[SMS/MMS 공통] 발송자 번호

            if (msgTotalSize > SmsWorkerManager.SMS_MAX_LENGTH) {
                String lgu_etc = LoadConfig.getProperty(LoadConfig.SMS_INFOBKSMS_ETC);
                String[] etcArr = lgu_etc.split("\\|");
                for(int i=0; i<etcArr.length; i++){
                    String[] keyValueArr = etcArr[i].split("=");
                    paramMap.put(keyValueArr[0],keyValueArr[1]);
                }

                paramMap.put("TITLE", smsWorkBean.getTITLE());
                if (!smsWorkBean.getSUB_TITLE().equals("")) {
                    paramMap.put("MSG", smsWorkBean.getSUB_TITLE());
                }
                // 장문문자 : MMS 발송
                SmsWorkerManager.getInstance().getSmsDbService().infobankSendSms(paramMap);
            } else {
                String lgu_etc = LoadConfig.getProperty(LoadConfig.SMS_INFOBKSMS_ETC);
                String[] etcArr = lgu_etc.split("\\|");
                for(int i=0; i<etcArr.length; i++){
                    String[] keyValueArr = etcArr[i].split("=");
                    paramMap.put(keyValueArr[0],keyValueArr[1]);
                }

                paramMap.put("SENDER", LoadConfig.getProperty(LoadConfig.SMS_SENDER));
                StringBuilder sb = new StringBuilder(smsWorkBean.getTITLE());
                if (!smsWorkBean.getSUB_TITLE().equals("")) {
                    sb.append(smsWorkBean.getSUB_TITLE());
                }
                paramMap.put("MSG", sb.toString());
                // 단문문자 : SMS 발송
                SmsWorkerManager.getInstance().getSmsDbService().infobankSendMms(paramMap);
            }

            return true;
        }catch (Exception e){
            smsWorkBean.setRESULT_CODE("500");
            smsWorkBean.setRESULT_MSG(e.toString());
            e.printStackTrace();
            return false;
        }

    }
}
