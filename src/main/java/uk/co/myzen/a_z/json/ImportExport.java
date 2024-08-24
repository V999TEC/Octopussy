package uk.co.myzen.a_z.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ImportExport {

	@JsonProperty("import")
	private Float gridImport;

	@JsonProperty("export")
	private Float gridExport;

	public Float getGridImport() {
		return gridImport;
	}

	public void setGridImport(Float gridImport) {
		this.gridImport = gridImport;
	}

	public Float getGridExport() {
		return gridExport;
	}

	public void setGridExport(Float gridExport) {
		this.gridExport = gridExport;
	}
}
