package com.example.obdreader.trips;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TripRecord {
    // 记录ID供数据库使用（主键）
    private Integer id;
    // 旅行开始的日期
    private Date startDate;
    //旅行结束的日期
    private Date endDate;
    private Integer engineRpmMax = 0;
    private Integer speed = 0;
    private String engineRuntime;

    public TripRecord() {
        startDate = new Date();
    }

    public Integer getSpeedMax() {
        return speed;
    }

    public void setSpeedMax(int value) {
        if (this.speed < value)
            speed = value;
    }

    public void setSpeedMax(String value) {
        setSpeedMax(Integer.parseInt(value));
    }

    /**
     * 描述：
     * id属性的Getter方法。
     *
     * @return Integer - the id value.
     */
    public Integer getID() {
        return id;
    }

    /**
     * 描述：
     *   id属性的设置方法。
     *
     * @param id - the Integer id value.
     */
    public void setID(Integer id) {
        this.id = id;
    }

    /**
     * 描述：
     * date属性的Getter方法。
     *
     * @return Date - the start date value
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * 描述：
     * date属性的设置方法。
     *
     * @param date - the Date value.
     */
    public void setStartDate(Date date) {
        this.startDate = date;
    }

    /**
     * 描述：
     * date属性的Getter方法。
     *
     * @return Date - the end date value
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * 描述：
     * date属性的设置方法。
     *
     * @param date - the Date value.
     */
    public void setEndDate(Date date) {
        this.endDate = date;
    }

    public Integer getEngineRpmMax() {
        return this.engineRpmMax;
    }

    public void setEngineRpmMax(Integer value) {
        if (this.engineRpmMax < value) {
            this.engineRpmMax = value;
        }
    }

    public void setEngineRpmMax(String value) {
        setEngineRpmMax(Integer.parseInt(value));
    }

    /**
     * 描述：
     * date属性的Getter方法为String值。
     *
     * @return String - the date value (MM/dd/yyyy).
     */
    public String getStartDateString() {
        //todo
        //return dateFormatter.format(this.startDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US);
        return sdf.format(this.startDate);
    }

    public String getEngineRuntime() {
        return engineRuntime;
    }

    public void setEngineRuntime(String value) {
        if (!value.equals("00:00:00")) {
            this.engineRuntime = value;
        }
    }
}
