//package com.jippy.foodandmart.entity;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "merchant_kyc", schema = "jippy_fm")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class MerchantKyc {
//
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	@Column(name = "kyc_id")
//	private Integer kycId;
//
//	@JsonIgnore
//	@OneToOne(fetch = FetchType.LAZY)
////	@JoinColumn(name = "merchant_id", insertable = false, updatable = false)
//	@JoinColumn(name = "merchant_id", nullable = false)
//	private Merchant merchant;
//
//	@Column(name = "pan_number", length = 20)
//	private String panNumber;
//	@Column(name = "aadhaar_number", length = 20)
//	private String aadhaarNumber;
//	@Column(name = "fssai_number", length = 30)
//	private String fssaiNumber;
//	@Column(name = "gst_number", length = 30)
//	private String gstNumber;
//
//	@Column(name = "verified", nullable = false)
//	@Builder.Default
//	private Boolean verified = false;
//
//	@Column(name = "created_at")
//	private LocalDateTime createdAt;
//	@Column(name = "created_by")
//	private Integer createdBy;
//	@Column(name = "updated_at")
//	private LocalDateTime updatedAt;
//	@Column(name = "updated_by")
//	private Integer updatedBy;
//
//	 @PrePersist
//	    public void onCreate() {
//	        this.createdAt = LocalDateTime.now();
//	    }
//
//	    @PreUpdate
//	    public void onUpdate() {
//	        this.updatedAt = LocalDateTime.now();
//	    }
//}


package com.jippy.foodandmart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_kyc", schema = "jippy_fm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FmMerchantKyc {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "kyc_id")
	private Integer kycId;

	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "merchant_id", nullable = false)
	private FmMerchant merchant;

	@Column(name = "pan_number", length = 20)
	private String panNumber;

	@Column(name = "aadhaar_number", length = 20)
	private String aadhaarNumber;

	@Column(name = "fssai_number", length = 30)
	private String fssaiNumber;

	@Column(name = "gst_number", length = 30)
	private String gstNumber;

	@Column(name = "verified", nullable = false)
	@Builder.Default
	private Boolean verified = false;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "created_by")
	private Integer createdBy;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "updated_by")
	private Integer updatedBy;

	@PrePersist
	public void onCreate() {
		this.createdAt = LocalDateTime.now();
		if (this.verified == null) {
			this.verified = false;
		}
	}

	@PreUpdate
	public void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}