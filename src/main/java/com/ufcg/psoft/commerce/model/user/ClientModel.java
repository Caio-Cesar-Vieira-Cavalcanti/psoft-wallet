package com.ufcg.psoft.commerce.model.user;

import com.ufcg.psoft.commerce.enums.PlanTypeEnum;
import com.ufcg.psoft.commerce.exception.user.UnauthorizedUserAccessException;
import com.ufcg.psoft.commerce.model.observer.ISubscriber;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import jakarta.persistence.*;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("C")
public class ClientModel extends UserModel implements ISubscriber {

    public static final String ANSI_MAGENTA = "\u001B[35m";
    public static final String ANSI_RESET = "\u001B[0m";
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientModel.class);

    @Builder
    public ClientModel(UUID id, String fullName, EmailModel email, AccessCodeModel accessCode, AddressModel address, PlanTypeEnum planType, WalletModel wallet) {
        super(id, fullName, email, accessCode);
        this.address = address;
        this.planType = planType;
        this.wallet = wallet;
    }

    @Embedded
    private AddressModel address;

    @Column(nullable = false)
    private PlanTypeEnum planType;

    @OneToOne(cascade = CascadeType.ALL)
    private WalletModel wallet;

    @Override
    public void validateAccess(String accessCode) {
        if (this.getAccessCode().matches(accessCode)) {
            throw new UnauthorizedUserAccessException("Unauthorized client access: access code is incorrect");
        }
    }

    @Override
    public void notify(String context) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{}{} (Notified client: {}){}", ANSI_MAGENTA, context, this.getFullName(), ANSI_RESET);
        }
    }

    @Override
    public boolean isAdmin() {
        return false;
    }
}