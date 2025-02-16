package uk.co.myzen.a_z.json;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class V1ProductSpecific {

	@JsonProperty("code")
	private String code;

	@JsonProperty("description")
	private String description;

	@JsonProperty("display_name")
	private String displayName;

	@JsonProperty("full_name")
	private String fullName;

	@JsonProperty("is_business")
	private Boolean isBusiness;

	@JsonProperty("is_green")
	private Boolean isGreen;

	@JsonProperty("is_prepay")
	private Boolean isPrepay;

	@JsonProperty("is_restricted")
	private Boolean isRestricted;

	@JsonProperty("is_tracker")
	private Boolean isTracker;

	@JsonProperty("is_variable")
	private Boolean isVariable;

	@JsonProperty("term")
	private String term;

	@JsonProperty("available_from")
	private String availableFrom;

	@JsonProperty("available_to")
	private String availableTo;

	@JsonProperty("tariffs_active_at")
	private String tariffsActiveAt;

	@JsonProperty("links")
	private ArrayList<Links> links;

	@JsonProperty("single_register_electricity_tariffs")
	private SingleRegisterElectricityTariffs singleRegisterElectricityTariffs;

	@JsonProperty("dual_register_electricity_tariffs")
	private DualRegisterElectricityTariffs dualRegisterElectricityTariffs;

	@JsonProperty("single_register_gas_tariffs")
	private SingleRegisterGasTariffs singleRegisterGasTariffs;

	@JsonProperty("sample_quotes")
	private SampleQuotes sampleQuotes;

	@JsonProperty("brand")
	private String brand;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getAvailableFrom() {
		return availableFrom;
	}

	public void setAvailableFrom(String availableFrom) {
		this.availableFrom = availableFrom;
	}

	public String getAvailableTo() {
		return availableTo;
	}

	public void setAvailableTo(String availableTo) {
		this.availableTo = availableTo;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public Boolean getIsBusiness() {
		return isBusiness;
	}

	public void setIsBusiness(Boolean isBusiness) {
		this.isBusiness = isBusiness;
	}

	public Boolean getIsGreen() {
		return isGreen;
	}

	public void setIsGreen(Boolean isGreen) {
		this.isGreen = isGreen;
	}

	public Boolean getIsPrepay() {
		return isPrepay;
	}

	public void setIsPrepay(Boolean isPrepay) {
		this.isPrepay = isPrepay;
	}

	public Boolean getIsRestricted() {
		return isRestricted;
	}

	public void setIsRestricted(Boolean isRestricted) {
		this.isRestricted = isRestricted;
	}

	public Boolean getIsTracker() {
		return isTracker;
	}

	public void setIsTracker(Boolean isTracker) {
		this.isTracker = isTracker;
	}

	public Boolean getIsVariable() {
		return isVariable;
	}

	public void setIsVariable(Boolean isVariable) {
		this.isVariable = isVariable;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public ArrayList<Links> getLinks() {
		return links;
	}

	public void setLinks(ArrayList<Links> links) {
		this.links = links;
	}

	public String getTariffsActiveAt() {
		return tariffsActiveAt;
	}

	public void setTariffsActiveAt(String tariffsActiveAt) {
		this.tariffsActiveAt = tariffsActiveAt;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public SingleRegisterElectricityTariffs getSingleRegisterElectricityTariffs() {
		return singleRegisterElectricityTariffs;
	}

	public void setSingleRegisterElectricityTariffs(SingleRegisterElectricityTariffs singleRegisterElectricityTariffs) {
		this.singleRegisterElectricityTariffs = singleRegisterElectricityTariffs;
	}

	public DualRegisterElectricityTariffs getDualRegisterElectricityTariffs() {
		return dualRegisterElectricityTariffs;
	}

	public void setDualRegisterElectricityTariffs(DualRegisterElectricityTariffs dualRegisterElectricityTariffs) {
		this.dualRegisterElectricityTariffs = dualRegisterElectricityTariffs;
	}

	public SingleRegisterGasTariffs getSingleRegisterGasTariffs() {
		return singleRegisterGasTariffs;
	}

	public void setSingleRegisterGasTariffs(SingleRegisterGasTariffs singleRegisterGasTariffs) {
		this.singleRegisterGasTariffs = singleRegisterGasTariffs;
	}

	public SampleQuotes getSampleQuotes() {
		return sampleQuotes;
	}

	public void setSampleQuotes(SampleQuotes sampleQuotes) {
		this.sampleQuotes = sampleQuotes;
	}
}
