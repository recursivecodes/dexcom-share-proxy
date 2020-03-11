package codes.recursive.dexcomproxy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;

import java.util.Date;

@Introspected
public class GlucoseReading {
    @JsonIgnore
    private String DT;
    @JsonIgnore
    private String ST;
    private int trend;
    private int value;

    private Date displayTime;
    private Date systemTime;

    public GlucoseReading(String DT, String ST, int trend, int value) {
        this.DT = DT;
        this.ST = ST;
        this.trend = trend;
        this.value = value;
    }

    @JsonIgnore
    public String getDT() {
        return DT;
    }

    public void setDT(String DT) {
        this.DT = DT;
    }

    @JsonIgnore
    public String getST() {
        return ST;
    }

    public void setST(String ST) {
        this.ST = ST;
    }

    @JsonProperty("trend")
    public int getTrend() {
        return trend;
    }
    @JsonProperty("Trend")
    public void setTrend(int trend) {
        this.trend = trend;
    }

    @JsonProperty("value")
    public int getValue() {
        return value;
    }
    @JsonProperty("Value")
    public void setValue(int value) {
        this.value = value;
    }

    @JsonProperty("displayTime")
    public Date getDisplayTime() {
        return getDateFromString(this.getDT());
    }

    @JsonProperty("systemTime")
    public Date getSystemTime() {
        return getDateFromString(this.getST());
    }

    private Date getDateFromString(String dString) {
        dString = dString.replace("/", "");
        dString = dString.replace("Date(", "");
        dString = dString.replace(")", "");
        String[] ds = dString.split("\\+");
        dString = ds[0];
        long ts = Long.parseLong(dString);
        return new Date( ts );
    }
}
