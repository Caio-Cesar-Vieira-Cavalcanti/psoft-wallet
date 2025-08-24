package com.ufcg.psoft.model;

import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import com.ufcg.psoft.commerce.model.wallet.WalletModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Wallet Model Unit Tests")
class WalletModelUnitTests {

    private WalletModel wallet;
    private UUID holdingId;

    @BeforeEach
    void setup() {
        holdingId = UUID.randomUUID();
        wallet = WalletModel.builder()
                .id(UUID.randomUUID())
                .budget(10000.0)
                .holdings(new HashMap<>())
                .build();
    }

    @Test
    @DisplayName("Should create WalletModel with correct values")
    void testWalletModelCreation() {
        assertNotNull(wallet.getId());
        assertEquals(10000.0, wallet.getBudget());
        assertNotNull(wallet.getHoldings());
        assertTrue(wallet.getHoldings().isEmpty());
    }

    @Test
    @DisplayName("Should decrease budget correctly")
    void testDecreaseBudgetAfterPurchase() {
        wallet.decreaseBudgetAfterPurchase(500.0);
        assertEquals(9500.0, wallet.getBudget());

        wallet.decreaseBudgetAfterPurchase(0);
        assertEquals(9500.0, wallet.getBudget());
    }

    @Test
    @DisplayName("Should set budget correctly using setter")
    void testSetBudget() {
        wallet.setBudget(20000.0);
        assertEquals(20000.0, wallet.getBudget());
    }

    @Test
    @DisplayName("Should add and retrieve holdings correctly")
    void testHoldingsMap() {
        HoldingModel holding = HoldingModel.builder()
                .id(holdingId)
                .build();

        wallet.getHoldings().put(holdingId, holding);

        assertEquals(1, wallet.getHoldings().size());
        assertSame(holding, wallet.getHoldings().get(holdingId));
    }

    @Test
    @DisplayName("Should remove holding correctly")
    void testRemoveHolding() {
        HoldingModel holding = HoldingModel.builder()
                .id(holdingId)
                .build();

        wallet.getHoldings().put(holdingId, holding);
        wallet.getHoldings().remove(holdingId);

        assertTrue(wallet.getHoldings().isEmpty());
    }
}