package com.ufcg.psoft.commerce.model;

import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Builder
@DiscriminatorValue("C")
public class ClientModel extends UserModel {

    @OneToOne
    private AddressModel address;

    @Column(nullable = false)
    private PlanTypeEnum planType;

    @Column(nullable = false)
    private double budget;

    @OneToOne
    private WalletModel wallet;

//    @OneToMany
//    @MapKey(name = "id")
//    @JoinTable(
//            name = "client_waiting_assets",
//            joinColumns = @JoinColumn(name = "client_id"),
//            inverseJoinColumns = @JoinColumn(name = "asset_id")
//    )
//    private Map<UUID, Asset> interestedAssets;
//
//    @OneToMany
//    @MapKey(name = "id")
//    @JoinTable(
//            name = "client_waiting_assets",
//            joinColumns = @JoinColumn(name = "client_id"),
//            inverseJoinColumns = @JoinColumn(name = "asset_id")
//    )
//    private Map<UUID, Asset> waitingAssetAvailable;

}
