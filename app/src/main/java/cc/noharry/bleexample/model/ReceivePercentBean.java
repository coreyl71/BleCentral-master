package cc.noharry.bleexample.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ReceivePercentBean implements Parcelable {

    private int receiveCount;
    private int totalCount;
    private String percent;

    public ReceivePercentBean() {
    }

    protected ReceivePercentBean(Parcel in) {
        receiveCount = in.readInt();
        totalCount = in.readInt();
        percent = in.readString();
    }

    public static final Creator<ReceivePercentBean> CREATOR = new Creator<ReceivePercentBean>() {
        @Override
        public ReceivePercentBean createFromParcel(Parcel in) {
            return new ReceivePercentBean(in);
        }

        @Override
        public ReceivePercentBean[] newArray(int size) {
            return new ReceivePercentBean[size];
        }
    };

    public int getReceiveCount() {
        return receiveCount;
    }

    public void setReceiveCount(int receiveCount) {
        this.receiveCount = receiveCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(receiveCount);
        dest.writeInt(totalCount);
        dest.writeString(percent);
    }
}
