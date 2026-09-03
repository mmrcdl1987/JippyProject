
package com.jippy.foodandmart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_kyc", schema = "jippy_fm")
public class FmUserKyc {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "kyc_id")
	private Integer kycId;
	@Column(name = "entity_id", nullable = false)
	private Integer entityId;

	@Column(name = "entity_type", nullable = false)
	private String entityType;

	@Column(name = "pan_number", length = 20)
	private String panNumber;

	@Column(name = "aadhaar_number", length = 20)
	private String aadhaarNumber;

	@Column(name = "fssai_number", length = 30)
	private String fssaiNumber;

	@Column(name = "gst_number", length = 30)
	private String gstNumber;

	@Column(name = "pan_number_url", length = 500)
	private String panNumberUrl;

	@Column(name = "aadhaar_number_url", length = 500)
	private String aadhaarNumberUrl;

	@Column(name = "fssai_number_url", length = 500)
	private String fssaiNumberUrl;

	@Column(name = "gst_number_url", length = 500)
	private String gstNumberUrl;

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

//	@JsonIgnore
//	@OneToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "merchant_id", nullable = false)
//	private FmMerchant merchant;
}