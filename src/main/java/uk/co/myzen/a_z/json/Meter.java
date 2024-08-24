package uk.co.myzen.a_z.json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Meter {

	@JsonProperty("serial_number")
	private String serialNumber;

	@JsonProperty("registers")
	private List<Register> registers;

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public List<Register> getRegisters() {
		return registers;
	}

	public void setRegisters(List<Register> registers) {
		this.registers = registers;
	}

}
