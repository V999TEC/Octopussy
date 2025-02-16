package uk.co.myzen.a_z.json;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Varying {

	@JsonProperty("code")
	private String code;

	@JsonProperty("standing_charge_exc_vat")
	private Float standingChargeExcVat;

	@JsonProperty("standing_charge_inc_vat")
	private Float standingChargeIncVat;

	@JsonProperty("online_discount_exc_vat")
	private Float onlineDiscountExcVat;

	@JsonProperty("online_discount_inc_vat")
	private Float onlineDiscountIncVat;

	@JsonProperty("dual_fuel_discount_exc_vat")
	private Float dualFuelDiscountExcVat;

	@JsonProperty("dual_fuel_discount_inc_vat")
	private Float dualFuelDiscountIncVat;

	@JsonProperty("exit_fees_exc_vat")
	private Float exitFeesExcVat;

	@JsonProperty("exit_fees_inc_vat")
	private Float exitFeesIncVat;

	@JsonProperty("exit_fees_type")
	private String exitFeesType;

	@JsonProperty("links")
	private ArrayList<Links> links;

	@JsonProperty("standard_unit_rate_exc_vat")
	private Float standardUnitRateExcVat;

	@JsonProperty("standard_unit_rate_inc_vat")
	private Float standardUnitRateIncVat;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Float getStandingChargeExcVat() {
		return standingChargeExcVat;
	}

	public void setStandingChargeExcVat(Float standingChargeExcVat) {
		this.standingChargeExcVat = standingChargeExcVat;
	}

	public Float getStandingChargeIncVat() {
		return standingChargeIncVat;
	}

	public void setStandingChargeIncVat(Float standingChargeIncVat) {
		this.standingChargeIncVat = standingChargeIncVat;
	}

	public Float getOnlineDiscountExcVat() {
		return onlineDiscountExcVat;
	}

	public void setOnlineDiscountExcVat(Float onlineDiscountExcVat) {
		this.onlineDiscountExcVat = onlineDiscountExcVat;
	}

	public Float getOnlineDiscountIncVat() {
		return onlineDiscountIncVat;
	}

	public void setOnlineDiscountIncVat(Float onlineDiscountIncVat) {
		this.onlineDiscountIncVat = onlineDiscountIncVat;
	}

	public Float getDualFuelDiscountExcVat() {
		return dualFuelDiscountExcVat;
	}

	public void setDualFuelDiscountExcVat(Float dualFuelDiscountExcVat) {
		this.dualFuelDiscountExcVat = dualFuelDiscountExcVat;
	}

	public Float getDualFuelDiscountIncVat() {
		return dualFuelDiscountIncVat;
	}

	public void setDualFuelDiscountIncVat(Float dualFuelDiscountIncVat) {
		this.dualFuelDiscountIncVat = dualFuelDiscountIncVat;
	}

	public Float getExitFeesExcVat() {
		return exitFeesExcVat;
	}

	public void setExitFeesExcVat(Float exitFeesExcVat) {
		this.exitFeesExcVat = exitFeesExcVat;
	}

	public Float getExitFeesIncVat() {
		return exitFeesIncVat;
	}

	public void setExitFeesIncVat(Float exitFeesIncVat) {
		this.exitFeesIncVat = exitFeesIncVat;
	}

	public String getExitFeesType() {
		return exitFeesType;
	}

	public void setExitFeesType(String exitFeesType) {
		this.exitFeesType = exitFeesType;
	}

	public Float getStandardUnitRateExcVat() {
		return standardUnitRateExcVat;
	}

	public void setStandardUnitRateExcVat(Float standardUnitRateExcVat) {
		this.standardUnitRateExcVat = standardUnitRateExcVat;
	}

	public Float getStandardUnitRateIncVat() {
		return standardUnitRateIncVat;
	}

	public void setStandardUnitRateIncVat(Float standardUnitRateIncVat) {
		this.standardUnitRateIncVat = standardUnitRateIncVat;
	}

	public ArrayList<Links> getLinks() {
		return links;
	}

	public void setLinks(ArrayList<Links> links) {
		this.links = links;
	}

}
