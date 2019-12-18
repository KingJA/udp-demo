import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Description:UDP提供者，用于提供服务
 * Create Time:2019/12/18 0018 上午 11:31
 * Author:KingJA
 * Email:kingjavip@gmail.com
 */
public class UDPSearcher {

    private static final int LISTEN_PORT = 30000;


    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("UDPSearcher 开始");
        Listener listener = listen();
        sendBroadcast();
        //读取任意键盘信息后退出
        System.in.read();
        List<Device> devices = listener.getDevicesAndClose();
        for (Device device : devices) {
            System.out.println("Device：" + device.toString());
        }
        System.out.println("UDPProvider 结束");
    }

    private static Listener listen() throws InterruptedException {
        System.out.println("UDPProvider listen 开始");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, countDownLatch);
        listener.start();
        countDownLatch.await();
        return listener;

    }

    private static void sendBroadcast() throws IOException {
        System.out.println("UDPSearcher sendBroadcast 开始");
        //作为搜索放，无需指定端口，让系统自动分配
        DatagramSocket ds = new DatagramSocket();
        //构建一份请求数据
        String requestData = MessageCreator.buildWithPort(LISTEN_PORT);
        byte[] requestDataBytes = requestData.getBytes();
        //直接构建packet
        DatagramPacket requestPack = new DatagramPacket(requestDataBytes, requestDataBytes.length);
        //端口20000，广播地址
        requestPack.setAddress(InetAddress.getByName("255.255.255.255"));
        requestPack.setPort(20000);
        ds.send(requestPack);
        ds.close();
        System.out.println("UDPProvider sendBroadcast 结束");
    }

    private static class Device {
        int port;
        String ip;
        String sn;

        public Device(int port, String ip, String sn) {
            this.port = port;
            this.ip = ip;
            this.sn = sn;
        }

        @Override
        public String toString() {
            return "Device{" +
                    "port=" + port +
                    ", ip='" + ip + '\'' +
                    ", sn='" + sn + '\'' +
                    '}';
        }
    }

    private static class Listener extends Thread {
        private final int listenPort;
        private final CountDownLatch countDownLatch;
        private final List<Device> devices = new ArrayList<>();
        private boolean done = false;
        private DatagramSocket ds = null;

        public Listener(int listenPort, CountDownLatch countDownLatch) {
            super();
            this.listenPort = listenPort;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            super.run();
            //通知已启动
            countDownLatch.countDown();
            try {
                //监听回送端口
                ds = new DatagramSocket(listenPort);
                while (!done) {
                    //创建接收实体
                    final byte[] buf = new byte[512];
                    DatagramPacket receivePack = new DatagramPacket(buf, buf.length);
                    //接收
                    ds.receive(receivePack);
                    //打印接收到的信息与发送者的信息
                    //发送者的ip地址
                    String ip = receivePack.getAddress().getHostAddress();
                    int port = receivePack.getPort();
                    int dataLen = receivePack.getLength();
                    String data = new String(receivePack.getData(), 0, dataLen);
                    System.out.println("UDPSearcher 接收到从IP ：" + ip + " 端口：" + port + "获取的数据：" + data);

                    String sn = MessageCreator.parseSn(data);
                    if (sn != null) {
                        Device device = new Device(port, ip, sn);
                        devices.add(device);
                    }

                }

            } catch (Exception ignored) {

            } finally {
                close();
            }
            System.out.println("UDPProvider listener 结束");
        }


        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }


        List<Device> getDevicesAndClose() {
            done = true;
            close();
            return devices;
        }
    }

}
