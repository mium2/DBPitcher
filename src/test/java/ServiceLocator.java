import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2015-05-15
 * Time: 오전 9:36
 * To change this template use File | Settings | File Templates.
 */
public class ServiceLocator {

    private Map cache;
    private static ServiceLocator me;
    static{
        me = new ServiceLocator();
    }

    private ServiceLocator(){
        cache = Collections.synchronizedMap(new HashMap());
    }

    public static ServiceLocator getInstance(){
        return me;
    }

    public Object getData(String key){
        if(cache.containsKey(key)){
            return cache.get(key);
        }else{
            //TODO: 캐시에 없으므로 찾아서 보낸 후 캐시에 집어넣는 로직 구현
            return null;
        }
    }
}
