package examples.jmx;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class ProcessMonitor {

    private static String HELP_MESSAGE = "java examples.jmx.ProcessMonitor [option]";

    private static String CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("?", "help", false, "ヘルプメッセージを表示します。");
        options.addOption("h", "host", true, "ホスト名を指定してください。この値デフォルト値は\"localhost\"です。");
        options.addOption("p", "port", true, "ポート番号を指定してください。この値デフォルト値は\"5000\"です。");
        options.addOption("i", "pid", true, "プロセスIDを指定してください。この値を指定するとローカルプロセスに接続を行います。");
        options.addOption("d", "dispName", true, "表示名を指定してください。この値を指定するとローカルプロセスに接続を行います。");

        // オプションの解析
        CommandLine cl = null;
        try {
            cl = new DefaultParser().parse(options, args);
        } catch (UnrecognizedOptionException e) {
            System.err.println("不明なオプションが指定されています。[" + e.getMessage() + "]");
        } catch (MissingArgumentException e) {
            System.err.println("オプション引数が入力されていません。[" + e.getMessage() + "]");
        } catch (ParseException e) {
            System.err.println("オプション解析に失敗しました。[" + e.getMessage() + "]");
        }
        if (cl == null || cl.hasOption("?")) {
            new HelpFormatter().printHelp(HELP_MESSAGE, options);
            return;
        }

        ProcessMonitor pm = new ProcessMonitor();
        String connectAddress;
        if (cl.hasOption("i")) {
            // プロセスIDでローカルプロセスに接続を行う。
            try {
                connectAddress = pm.getLocalAddress(cl);
            } catch (Exception e) {
                e.printStackTrace();
                ProcessMonitor.printLocalProcess(System.err);
                return;
            }
        } else if (cl.hasOption("d")) {
            String pid;
            try {
                pid = pm.searchPid(cl);
            } catch (NoSuchElementException e) {
                System.err.println("指定されたDisplayNameのローカルプロセスは存在しませんでした。");
                System.err.println();
                ProcessMonitor.printLocalProcess(System.err);
                return;
            }
            try {
                connectAddress = pm.getLocalAddress(pid);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println();
                ProcessMonitor.printLocalProcess(System.err);
                return;
            }
        } else {
            try {
                connectAddress = pm.getServerAddress(cl);
            } catch (NumberFormatException e) {
                System.err.println("ポート番号に数値以外の値が指定されています。");
                new HelpFormatter().printHelp(HELP_MESSAGE, options);
                return;
            }
        }

        pm.printProcessInfo(connectAddress);
    }

    public static void printLocalProcess(PrintStream ps) {
        List<VirtualMachineDescriptor> vmDescriptors = VirtualMachine.list();
        vmDescriptors.forEach(vmd -> {
            ps.println("++++　ローカルJVMプロセス　+++++++++++++");
            ps.println("id　　　　　：" + vmd.id());
            ps.println("displayName ：" + vmd.displayName());
            ps.println("name　　　　：" + vmd.provider().name());
            ps.println("type　　　　：" + vmd.provider().type());
            ps.println();
        });
    }

    public String searchPid(CommandLine cl) throws NoSuchElementException {
        String displayName = cl.getOptionValue("d");
        RuntimeMXBean runtimeMx = ManagementFactory.getRuntimeMXBean();
        List<VirtualMachineDescriptor> vmDescriptors = VirtualMachine.list();
        return vmDescriptors.stream().filter(vmd -> !Long.toString(runtimeMx.getPid()).equals(vmd.id()) &&
                vmd.displayName().contains(displayName)).map(VirtualMachineDescriptor::id).findFirst().orElseThrow();
    }

    public String getLocalAddress(CommandLine cl)
            throws AttachNotSupportedException, AgentLoadException, AgentInitializationException, IOException {
        String pid = cl.getOptionValue("i");
        return getLocalAddress(pid);
    }

    public String getLocalAddress(String pid)
            throws AttachNotSupportedException, AgentLoadException, AgentInitializationException, IOException {
        VirtualMachine vm = VirtualMachine.attach(pid);
        String connectorAddress;
        try {
            connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
            if (connectorAddress == null) {
                vm.startLocalManagementAgent();
                connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
            }
        } finally {
            vm.detach();
        }
        return connectorAddress;
    }

    public String getServerAddress(CommandLine cl) throws NumberFormatException {
        String hostName = cl.getOptionValue("h", "localhost");
        String portStr = cl.getOptionValue("p", "5000");
        int portNum = Integer.parseInt(portStr);

        // デフォルトのURL：service:jmx:rmi:///jndi/rmi://localhost:5000/jmxrmi
        StringBuilder builder = new StringBuilder("service:jmx:rmi:///jndi/rmi://");
        builder.append(hostName).append(":").append(portNum).append("/jmxrmi");

        return builder.toString();
    }

    public void printProcessInfo(String connectAddress) {
        System.out.println("connect address = " + connectAddress);
        System.out.println();

        // プロセスに接続する
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
        NumberFormat nf = NumberFormat.getNumberInstance();
        try (JMXConnector jmxc = JMXConnectorFactory.connect(new JMXServiceURL(connectAddress))) {
            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

            // Java仮想マシンの実行時情報を表示する
            RuntimeMXBean rmxbean = ManagementFactory.getPlatformMXBean(mbsc, RuntimeMXBean.class);
            System.out.println("++++　VM情報　++++++++++++++++++++++++++");
            System.out.println("仮想マシン名　　　　　　：" + rmxbean.getName());
            System.out.println("プロセスID　　　　　　　：" + rmxbean.getPid());
            System.out.println("起動時間　　　　　　　　：" + dtf.format(
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(rmxbean.getStartTime()), ZoneId.systemDefault())));
            System.out.println("稼働時間　　　　　　　　：" + BigDecimal.valueOf(
                    rmxbean.getUptime()).divide(BigDecimal.valueOf(1000)) + " s");
            System.out.println("クラスパス　　　　　　　：" + rmxbean.getClassPath());
            System.out.println("入力パラメータ　　　　　：" + rmxbean.getInputArguments().stream().collect(
                    Collectors.joining(", ")));
            System.out.println("ライブラリパス　　　　　：" + rmxbean.getLibraryPath());

            // メモリ情報を表示する
            MemoryMXBean mmxbean = ManagementFactory.getPlatformMXBean(mbsc, MemoryMXBean.class);
            MemoryUsage memoryUsage = mmxbean.getHeapMemoryUsage();
            System.out.println();
            System.out.println("++++　メモリ情報　++++++++++++++++++++++");
            System.out.println("最大ヒープサイズ　　　　："
                    + nf.format(BigDecimal.valueOf(memoryUsage.getMax()).divide(BigDecimal.valueOf(1024), 0,
                            RoundingMode.DOWN))
                    + " KB");
            System.out.println("ヒープ使用量　　　　　　："
                    + nf.format(BigDecimal.valueOf(memoryUsage.getUsed()).divide(BigDecimal.valueOf(1024), 0,
                            RoundingMode.DOWN))
                    + " KB");

            // OSの情報を表示する
            OperatingSystemMXBean omxbean = ManagementFactory.getPlatformMXBean(mbsc, OperatingSystemMXBean.class);
            System.out.println();
            System.out.println("++++　OS情報　++++++++++++++++++++++++++");
            System.out.println("OS名　　　　　　　　　　：" + omxbean.getName());
            System.out.println("OSバージョン　　　　　　：" + omxbean.getVersion());
            System.out.println("OSアーキテクチャ　　　　：" + omxbean.getArch());
            com.sun.management.OperatingSystemMXBean somxbean = ManagementFactory.getPlatformMXBean(mbsc,
                    com.sun.management.OperatingSystemMXBean.class);
            System.out.println("物理メモリ合計　　　　　："
                    + nf.format(BigDecimal.valueOf(somxbean.getTotalPhysicalMemorySize()).divide(
                            BigDecimal.valueOf(1024), 0,
                            RoundingMode.DOWN))
                    + " KB");
            System.out.println("空きメモリ合計　　　　　："
                    + nf.format(BigDecimal.valueOf(somxbean.getFreePhysicalMemorySize()).divide(
                            BigDecimal.valueOf(1024), 0,
                            RoundingMode.DOWN))
                    + " KB");
            System.out.println("VMプロセスのCPU使用時間 ："
                    + BigDecimal.valueOf(somxbean.getProcessCpuTime()).divide(BigDecimal.valueOf(1000000000)) + " s");

            // ログイン情報を表示する
            LoginMonitorMBean lmbean = JMX.newMBeanProxy(mbsc, LoginMonitorMBean.createObjectName(),
                    LoginMonitorMBean.class);
            System.out.println();
            System.out.println("++++　ログイン情報　++++++++++++++++++++");
            System.out.println("ログイン数　　　　　　　：" + lmbean.getLoginCount());
            System.out.println("ログインユーザID　　　　：" + Arrays.stream(lmbean.getLoginInfos()).map(
                    data -> ((Integer) data.get(LoginMonitorMBean.LUI_ITEM_ID)).toString())
                    .collect(Collectors.joining(", ")));
            System.out.println("ロックID　　　　　　　　：" + Arrays.stream(lmbean.getLoginLockIds()).mapToObj(
                    id -> Integer.valueOf(id).toString()).collect(Collectors.joining(", ")));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
