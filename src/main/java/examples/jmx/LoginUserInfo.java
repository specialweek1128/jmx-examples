package examples.jmx;

import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

public class LoginUserInfo {

    private static final String ID = LoginMonitorMBean.LUI_ITEM_ID;

    private static final String NAME = LoginMonitorMBean.LUI_ITEM_NAME;

    private final int id;

    private final String name;

    public LoginUserInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void addMBean(LoginMonitorMBean mbean) throws OpenDataException {
        CompositeType compositeType = new CompositeType(
                "LoginUserInfo",
                "ログインユーザ情報を保持するデータ型",
                new String[] { ID, NAME },
                new String[] { "ログインユーザID", "ログインユーザ名" },
                new OpenType[] { SimpleType.INTEGER, SimpleType.STRING });
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(ID, id);
        dataMap.put(NAME, name);
        mbean.addLoginInfo(new CompositeDataSupport(compositeType, dataMap));
    }

    public static int getMBeanId(CompositeData data) {
        return (Integer) data.get(ID);
    }

}
