package cc.noharry.bleexample.ContentValue;

public class BFrameConst {

    public static byte FRAME_HEAD = (byte)0xFF;
    public static int  MESSAGE_ID_LENGTH = 4;
    public static byte FRAME_END = (byte)0x00;

    /**
     * 开始发送消息的数据包起始 msgType
     */
    public static final int START_MSG_ID_START = 1;

    /***********************发送主设备唯一标识码的数据包消息类型***********************/
    /**
     * 主设备给 Server 发送 Token
     */
    public static final int START_MSG_ID_TOKEN = 2;
    /**
     * 主设备给 Server 发送内容包
     */
    public static final int START_MSG_ID_CENTRAL = 3;
    /**
     * Server 给主设备发送内容包
     */
    public static final int START_MSG_ID_SERVER = 5;
    /**
     * Server 给主设备发送内容包完成
     */
    public static final int START_MSG_ID_SERVER_COMPLETE = 6;

    /**
     * 之后将这里改为本机唯一标识码
     */
    public static final String TOKEN = "token1";
//    public static final String TOKEN = "一二三四五六七八九十壹贰叁肆伍陆柒捌玖拾000000000000000000001234567890";

}