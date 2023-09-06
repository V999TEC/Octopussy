package uk.co.myzen.a_z.json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Detail {

	@JsonProperty("id")
	private Integer id;

	@JsonProperty("moved_in_at")
	private String movedInAt;

	@JsonProperty("moved_out_at")
	private String movedOutAt;

	@JsonProperty("address_line_1")
	private String addressLine1;

	@JsonProperty("address_line_2")
	private String addressLine2;

	@JsonProperty("address_line_3")
	private String addressLine3;

	@JsonProperty("town")
	private String town;

	@JsonProperty("county")
	private String county;

	@JsonProperty("postcode")
	private String postcode;

	@JsonProperty("electricity_meter_points")
	private List<ElectricityMeterPoint> electrictyMeterPoints;

	@JsonProperty("gas_meter_points")
	private List<GasMeterPoint> gasMeterPoints;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getMovedInAt() {
		return movedInAt;
	}

	public void setMovedInAt(String movedInAt) {
		this.movedInAt = movedInAt;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getMovedOutAt() {
		return movedOutAt;
	}

	public void setMovedOutAt(String movedOutAt) {
		this.movedOutAt = movedOutAt;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getAddressLine3() {
		return addressLine3;
	}

	public void setAddressLine3(String addressLine3) {
		this.addressLine3 = addressLine3;
	}

	public String getTown() {
		return town;
	}

	public void setTown(String town) {
		this.town = town;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public List<ElectricityMeterPoint> getElectrictyMeterPoints() {
		return electrictyMeterPoints;
	}

	public void setElectrictyMeterPoints(List<ElectricityMeterPoint> electrictyMeterPoints) {
		this.electrictyMeterPoints = electrictyMeterPoints;
	}

	public List<GasMeterPoint> getGasMeterPoints() {
		return gasMeterPoints;
	}

	public void setGasMeterPoints(List<GasMeterPoint> gasMeterPoints) {
		this.gasMeterPoints = gasMeterPoints;
	}

}
