import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Description:TODO
 * Create Time:2019/12/18 0018 下午 4:03
 * Author:KingJA
 * Email:kingjavip@gmail.com
 */
public class MessageCreator {
    private static final String SN_HEADER = "收到暗号，我是(SN):";
    private static final String PORT_HEADER = "这是暗号，请回电端口(Port):";

    public static String buildWithPort(int port) {
        return PORT_HEADER + port;
    }

    public static int parsePort(String data) {
        if (data.startsWith(PORT_HEADER)) {
            return Integer.parseInt(data.substring(PORT_HEADER.length()));
        }
        return -1;
    }

    public static String buildWithSn(String sn) {
        return SN_HEADER + sn;
    }

    public static String parseSn(String data) {
        if (data.startsWith(SN_HEADER)) {
            return data.substring(SN_HEADER.length());
        }
        return null;
    }





}
