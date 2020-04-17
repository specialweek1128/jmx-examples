package examples.jmx;

import java.lang.management.ManagementFactory;

import javax.annotation.PostConstruct;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartupBean {

    private static Logger log = LoggerFactory.getLogger(StartupBean.class);

    @PostConstruct
    public void initAfterStartup() {
        try {
            log.info("MBean登録処理");
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.registerMBean(new LoginMonitor(), LoginMonitorMBean.createObjectName());
        } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
            throw new IllegalStateException(e);
        }
    }

}
