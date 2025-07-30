package com.ufcg.psoft.commerce.model.user;

import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import com.ufcg.psoft.commerce.exception.user.UnauthorizedUserAccessException;
import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("C")
public class ClientModel extends UserModel {

    @Builder
    public ClientModel(UUID id, String fullName, EmailModel email, AccessCodeModel accessCode, AddressModel address, PlanTypeEnum planType, double budget, WalletModel wallet) {
        super(id, fullName, email, accessCode);
        this.address = address;
        this.planType = planType;
        this.budget = budget;
        this.wallet = wallet;
        this.interestedPriceVariationAssets = new HashMap<>();
        this.interestedAvailabilityAssets = new HashMap<>();
    }

    @Embedded
    private AddressModel address;

    @Column(nullable = false)
    private PlanTypeEnum planType;

    @Column(nullable = false)
    private double budget;

    @OneToOne(cascade = CascadeType.ALL)
    private WalletModel wallet;

    @ManyToMany
    @MapKey(name = "id")
    @JoinTable(
            name = "client_interest_price_variation_assets",
            joinColumns = @JoinColumn(name = "client_id"),
            inverseJoinColumns = @JoinColumn(name = "asset_id")
    )
    private Map<UUID, AssetModel> interestedPriceVariationAssets;

    @ManyToMany
    @MapKey(name = "id")
    @JoinTable(
            name = "client_interest_availability_assets",
            joinColumns = @JoinColumn(name = "client_id"),
            inverseJoinColumns = @JoinColumn(name = "asset_id")
    )
    private Map<UUID, AssetModel> interestedAvailabilityAssets;

    @Override
    public void validateAccess(String accessCode) {
        if (!this.getAccessCode().matches(accessCode)) {
            throw new UnauthorizedUserAccessException("Unauthorized client access: access code is incorrect");
        }
    }
}