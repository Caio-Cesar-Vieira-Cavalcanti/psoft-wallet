package com.ufcg.psoft.commerce.model.asset;


import jakarta.persistence.*;

@Entity(name = "TB_ASSET_TYPE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class AssetType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public abstract double taxCalculate(double profit);
}
