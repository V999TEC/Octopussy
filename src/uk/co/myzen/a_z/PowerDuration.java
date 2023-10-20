package uk.co.myzen.a_z;

public class PowerDuration {

	private Long epochSecond;

	private Float power;

	private Integer secsDuration;

	public Float getPower() {
		return power;
	}

	public void setPower(Float power) {
		this.power = power;
	}

	public Integer getSecsDuration() {
		return secsDuration;
	}

	public void setSecsDuration(Integer secsDuration) {
		this.secsDuration = secsDuration;
	}

	public Long getEpochSecond() {
		return epochSecond;
	}

	public void setEpochSecond(Long epochSecond) {
		this.epochSecond = epochSecond;
	}
}
