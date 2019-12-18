import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.UUID;

/**
 * Description:TODO
 * Create Time:2019/12/18 0018 上午 11:31
 * Author:KingJA
 * Email:kingjavip@gmail.com
 */
public class UDPProvider {
    public static void main(String[] args) throws IOException {
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn);
        provider.start();
        //读取任意键盘信息后退出
        System.in.read();
        provider.exit();


    }
    private static class Provider extends Thread {
        private final String sn;
        private boolean done = false;
        private DatagramSocket ds = null;

        public Provider(String sn) {
            super();
            this.sn = sn;
        }

        @Override
        public void run() {
            super.run();
            //作为接受者，指定一个端口用于数据接收
            try {
                //监听20000端口
                ds = new DatagramSocket(20000);
                while (!done) {
                    System.out.println("UDPProvider 开始");
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
                    System.out.println("UDPProvider 接收到从IP ：" + ip + " 端口：" + port + "获取的数据：" + data);
                    //解析端口号
                    int responsePort = MessageCreator.parsePort(data);
                    if (responsePort != -1) {
                        //构建回送数据
                        String responseData = MessageCreator.buildWithSn(sn);
                        byte[] responseDataBytes = responseData.getBytes();
                        //直接根据发送者构建一份回送信息
                        DatagramPacket responsePack = new DatagramPacket(responseDataBytes, responseDataBytes.length,
                                receivePack.getAddress(), responsePort);
                        ds.send(responsePack);
                    }
                }
            } catch (IOException ignored) {
            } finally {
                close();
            }

            System.out.println("UDPProvider 结束");
        }

        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }

        void exit() {
            done = true;
            close();
        }
    }
}
