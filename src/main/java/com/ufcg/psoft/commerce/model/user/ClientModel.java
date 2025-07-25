package com.ufcg.psoft.commerce.model.user;

import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("C")
public class ClientModel extends UserModel {

    public ClientModel(UUID id, String fullName, EmailModel email, AccessCodeModel accessCode, AddressModel address, PlanTypeEnum planType, double budget, WalletModel wallet) {
        super(id, fullName, email, accessCode);
        this.address = address;
        this.planType = planType;
        this.budget = budget;
        this.wallet = wallet;
    }

    @Embedded
    private AddressModel address;

    @Column(nullable = false)
    private PlanTypeEnum planType;

    @Column(nullable = false)
    private double budget;

    @OneToOne
    private WalletModel wallet;

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
}