package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "terms_and_conditions",
        schema = "jippy_fm"
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermsAndConditions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "terms_and_conditions_id")
    private Long termsAndConditionsId;

    @Column(name = "app_type")
    private String appType;

    @Column(name = "privacy_and_policy")
    private String privacyAndPolicy;

    @Column(name = "terms_and_conditions")
    private String termsAndConditions;
}