package cc.noharry.bleexample.model;

import java.util.List;

public class HealthBean {


    /**
     * dataFrom : device
     * dataType : staticData
     * deviceType : watch
     * userID : 3
     * healthData : [{"deadmanStat":1,"wearStat":1,"sleepTotalTime":678,"sleepValidTime":234,"step":1234,"distance":1234,"calorie":123,"speed":12,"posture":2,"mood":3,"protein":25,"water":12,"glucose":12,"reality":12,"sham":12,"bodyTemp":32,"pulseRate":60,"respiration":30,"SPO2":90,"PI":20,"heartRate":60,"height":168,"healthIndex":8,"lifeIndex":8,"chargestat":"1","batCap":"50","batstate":"1","Shuzhang":"12","Shousuo":"50","UnixTime":1522587325}]
     * UnixTime : 1522587325
     */

    private String dataFrom;
    private String dataType;
    private String deviceType;
    private String userID;
    private int UnixTime;
    private List<HealthDataBean> healthData;

    public String getDataFrom() {
        return dataFrom;
    }

    public void setDataFrom(String dataFrom) {
        this.dataFrom = dataFrom;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public int getUnixTime() {
        return UnixTime;
    }

    public void setUnixTime(int UnixTime) {
        this.UnixTime = UnixTime;
    }

    public List<HealthDataBean> getHealthData() {
        return healthData;
    }

    public void setHealthData(List<HealthDataBean> healthData) {
        this.healthData = healthData;
    }

    public static class HealthDataBean {
        /**
         * deadmanStat : 1
         * wearStat : 1
         * sleepTotalTime : 678
         * sleepValidTime : 234
         * step : 1234
         * distance : 1234
         * calorie : 123
         * speed : 12
         * posture : 2
         * mood : 3
         * protein : 25
         * water : 12
         * glucose : 12
         * reality : 12
         * sham : 12
         * bodyTemp : 32
         * pulseRate : 60
         * respiration : 30
         * SPO2 : 90
         * PI : 20
         * heartRate : 60
         * height : 168
         * healthIndex : 8
         * lifeIndex : 8
         * chargestat : 1
         * batCap : 50
         * batstate : 1
         * Shuzhang : 12
         * Shousuo : 50
         * UnixTime : 1522587325
         */

        private int deadmanStat;
        private int wearStat;
        private int sleepTotalTime;
        private int sleepValidTime;
        private int step;
        private int distance;
        private int calorie;
        private int speed;
        private int posture;
        private int mood;
        private int protein;
        private int water;
        private int glucose;
        private int reality;
        private int sham;
        private int bodyTemp;
        private int pulseRate;
        private int respiration;
        private int SPO2;
        private int PI;
        private int heartRate;
        private int height;
        private int healthIndex;
        private int lifeIndex;
        private String chargestat;
        private String batCap;
        private String batstate;
        private String Shuzhang;
        private String Shousuo;
        private int UnixTime;

        public int getDeadmanStat() {
            return deadmanStat;
        }

        public void setDeadmanStat(int deadmanStat) {
            this.deadmanStat = deadmanStat;
        }

        public int getWearStat() {
            return wearStat;
        }

        public void setWearStat(int wearStat) {
            this.wearStat = wearStat;
        }

        public int getSleepTotalTime() {
            return sleepTotalTime;
        }

        public void setSleepTotalTime(int sleepTotalTime) {
            this.sleepTotalTime = sleepTotalTime;
        }

        public int getSleepValidTime() {
            return sleepValidTime;
        }

        public void setSleepValidTime(int sleepValidTime) {
            this.sleepValidTime = sleepValidTime;
        }

        public int getStep() {
            return step;
        }

        public void setStep(int step) {
            this.step = step;
        }

        public int getDistance() {
            return distance;
        }

        public void setDistance(int distance) {
            this.distance = distance;
        }

        public int getCalorie() {
            return calorie;
        }

        public void setCalorie(int calorie) {
            this.calorie = calorie;
        }

        public int getSpeed() {
            return speed;
        }

        public void setSpeed(int speed) {
            this.speed = speed;
        }

        public int getPosture() {
            return posture;
        }

        public void setPosture(int posture) {
            this.posture = posture;
        }

        public int getMood() {
            return mood;
        }

        public void setMood(int mood) {
            this.mood = mood;
        }

        public int getProtein() {
            return protein;
        }

        public void setProtein(int protein) {
            this.protein = protein;
        }

        public int getWater() {
            return water;
        }

        public void setWater(int water) {
            this.water = water;
        }

        public int getGlucose() {
            return glucose;
        }

        public void setGlucose(int glucose) {
            this.glucose = glucose;
        }

        public int getReality() {
            return reality;
        }

        public void setReality(int reality) {
            this.reality = reality;
        }

        public int getSham() {
            return sham;
        }

        public void setSham(int sham) {
            this.sham = sham;
        }

        public int getBodyTemp() {
            return bodyTemp;
        }

        public void setBodyTemp(int bodyTemp) {
            this.bodyTemp = bodyTemp;
        }

        public int getPulseRate() {
            return pulseRate;
        }

        public void setPulseRate(int pulseRate) {
            this.pulseRate = pulseRate;
        }

        public int getRespiration() {
            return respiration;
        }

        public void setRespiration(int respiration) {
            this.respiration = respiration;
        }

        public int getSPO2() {
            return SPO2;
        }

        public void setSPO2(int SPO2) {
            this.SPO2 = SPO2;
        }

        public int getPI() {
            return PI;
        }

        public void setPI(int PI) {
            this.PI = PI;
        }

        public int getHeartRate() {
            return heartRate;
        }

        public void setHeartRate(int heartRate) {
            this.heartRate = heartRate;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getHealthIndex() {
            return healthIndex;
        }

        public void setHealthIndex(int healthIndex) {
            this.healthIndex = healthIndex;
        }

        public int getLifeIndex() {
            return lifeIndex;
        }

        public void setLifeIndex(int lifeIndex) {
            this.lifeIndex = lifeIndex;
        }

        public String getChargestat() {
            return chargestat;
        }

        public void setChargestat(String chargestat) {
            this.chargestat = chargestat;
        }

        public String getBatCap() {
            return batCap;
        }

        public void setBatCap(String batCap) {
            this.batCap = batCap;
        }

        public String getBatstate() {
            return batstate;
        }

        public void setBatstate(String batstate) {
            this.batstate = batstate;
        }

        public String getShuzhang() {
            return Shuzhang;
        }

        public void setShuzhang(String Shuzhang) {
            this.Shuzhang = Shuzhang;
        }

        public String getShousuo() {
            return Shousuo;
        }

        public void setShousuo(String Shousuo) {
            this.Shousuo = Shousuo;
        }

        public int getUnixTime() {
            return UnixTime;
        }

        public void setUnixTime(int UnixTime) {
            this.UnixTime = UnixTime;
        }
    }
}
