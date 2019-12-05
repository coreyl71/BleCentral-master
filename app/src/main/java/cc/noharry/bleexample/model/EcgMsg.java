package cc.noharry.bleexample.model;

import java.util.Arrays;

/**
 * 定义帧的结构
 * 帧头：4个字节，0xbebebebe
 * 帧计数：1个字节
 * 帧长度：1个字节，128
 * sessionId： 4个字节
 * heart_array： 心电数组  100字节
 * breath_array：呼吸数组  10字节
 * temperature_value：体温  2字节
 * heart_value： 心率  1字节
 * breath_value: 呼吸率 1字节
 * power_level:电池标志位  1字节
 * reserved_array: 保留字节数组  1字节
 * 帧尾:   2字节   0xaaaa
 */
public class EcgMsg {

    public EcgMsg() {
        super();
    }

    // 帧头：4个字节，0xbebebebe
    private byte[] frameHeader;
    // 帧计数：1个字节
    private int frameNum;
    // 帧长度：1个字节，128
    private int frameLength;
    private byte[] sessionID;
    // 心电数组  100字节
    private byte[] heart_array;
    // 呼吸数组  10字节
    private byte[] breath_array;
    // 体温  2字节
    private float temperature_value;
    // 心率  1字节
    private int heart_value;
    // 呼吸率 1字节
    private int breath_value;
    // 电池标志位  1字节
    private int power_level;
    // 保留字节数组  1字节
    private byte[] reserved_array;

    public EcgMsg(byte[] frameHeader, int frameNum, int frameLength, byte[] sessionID, byte[] heart_array, byte[] breath_array, float temperature_value, int heart_value, int breath_value, int power_level, byte[] reserved_array) {
        this.frameHeader = frameHeader;
        this.frameNum = frameNum;
        this.frameLength = frameLength;
        this.sessionID = sessionID;
        this.heart_array = heart_array;
        this.breath_array = breath_array;
        this.temperature_value = temperature_value;
        this.heart_value = heart_value;
        this.breath_value = breath_value;
        this.power_level = power_level;
        this.reserved_array = reserved_array;
    }

    public byte[] getFrameHeader() {
        return frameHeader;
    }

    public void setFrameHeader(byte[] frameHeader) {
        this.frameHeader = frameHeader;
    }

    public int getFrameNum() {
        return frameNum;
    }

    public void setFrameNum(int frameNum) {
        this.frameNum = frameNum;
    }

    public int getFrameLength() {
        return frameLength;
    }

    public void setFrameLength(int frameLength) {
        this.frameLength = frameLength;
    }

    public byte[] getSessionID() {
        return sessionID;
    }

    public void setSessionID(byte[] sessionID) {
        this.sessionID = sessionID;
    }

    public byte[] getHeart_array() {
        return heart_array;
    }

    public void setHeart_array(byte[] heart_array) {
        this.heart_array = heart_array;
    }

    public byte[] getBreath_array() {
        return breath_array;
    }

    public void setBreath_array(byte[] breath_array) {
        this.breath_array = breath_array;
    }

    public float getTemperature_value() {
        return temperature_value;
    }

    public void setTemperature_value(float temperature_value) {
        this.temperature_value = temperature_value;
    }

    public int getHeart_value() {
        return heart_value;
    }

    public void setHeart_value(int heart_value) {
        this.heart_value = heart_value;
    }

    public int getBreath_value() {
        return breath_value;
    }

    public void setBreath_value(int breath_value) {
        this.breath_value = breath_value;
    }

    public int getPower_level() {
        return power_level;
    }

    public void setPower_level(int power_level) {
        this.power_level = power_level;
    }

    public byte[] getReserved_array() {
        return reserved_array;
    }

    public void setReserved_array(byte[] reserved_array) {
        this.reserved_array = reserved_array;
    }

    @Override
    public String toString() {
        return "EcgMsg{" +
                "frameHeader=" + Arrays.toString(frameHeader) +
                ", frameNum=" + frameNum +
                ", frameLength=" + frameLength +
                ", sessionID=" + Arrays.toString(sessionID) +
                ", heart_array=" + Arrays.toString(heart_array) +
                ", breath_array=" + Arrays.toString(breath_array) +
                ", temperature_value=" + temperature_value +
                ", heart_value=" + heart_value +
                ", breath_value=" + breath_value +
                ", power_level=" + power_level +
                ", reserved_array=" + Arrays.toString(reserved_array) +
                '}';
    }

}
