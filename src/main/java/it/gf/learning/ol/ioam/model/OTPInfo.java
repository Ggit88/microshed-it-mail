package it.gf.learning.ol.ioam.model;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name="OTPInfo", description="POJO representing OTP data")
public class OTPInfo {
	
	@Schema(required = true)
	private String otp;
	
	public OTPInfo () {
	}
	
	@JsonbCreator
	public OTPInfo ( @JsonbProperty("otp") String otp) {
		this.otp = otp;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	@Override
	public String toString() {
		return "OTPInfo [otp=" + otp + "]";
	}
	
}
