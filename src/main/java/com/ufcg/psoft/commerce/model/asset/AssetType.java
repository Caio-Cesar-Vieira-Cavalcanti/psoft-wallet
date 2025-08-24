package com.ufcg.psoft.commerce.model.asset;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "asset_type")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type_discriminator", discriminatorType = DiscriminatorType.STRING)
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class AssetType {

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("name")
    @Column(nullable = false, unique = true)
    private String name;
    
    protected AssetType(String name) {
        this.name = name;
    }

    public abstract double taxCalculate(double profit);
}
