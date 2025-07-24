package com.ufcg.psoft.commerce.model;

import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@DiscriminatorValue("C")
public class ClientModel extends UserModel {

    public ClientModel(UUID id, String fullName, EmailModel email, AccessCodeModel accessCode, AddressModel address, PlanTypeEnum planType, double budget, WalletModel wallet) {
        super(id, fullName, email, accessCode);
        this.address = address;
        this.planType = planType;
        this.budget = budget;
//        this.wallet = wallet;
    }

    @OneToOne
    private AddressModel address;

    @Column(nullable = false)
    private PlanTypeEnum planType;

    @Column(nullable = false)
    private double budget;

//    @OneToOne
//    private WalletModel wallet;

//    @ManyToMany
//    @MapKey(name = "id")
//    @JoinTable(
//            name = "client_waiting_assets",
//            joinColumns = @JoinColumn(name = "client_id"),
//            inverseJoinColumns = @JoinColumn(name = "asset_id")
//    )
//    private Map<UUID, Asset> interestedAssets;
//
//    @ManyToMany
//    @MapKey(name = "id")
//    @JoinTable(
//            name = "client_waiting_assets",
//            joinColumns = @JoinColumn(name = "client_id"),
//            inverseJoinColumns = @JoinColumn(name = "asset_id")
//    )
//    private Map<UUID, Asset> waitingAssetAvailable;


    public AddressModel getAddress() {
        return address;
    }

    public PlanTypeEnum getPlanType() {
        return planType;
    }

    public double getBudget() {
        return budget;
    }
}
