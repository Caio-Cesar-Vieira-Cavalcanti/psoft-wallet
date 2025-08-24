package com.ufcg.psoft.model;

import com.ufcg.psoft.commerce.model.asset.AssetModel;
import com.ufcg.psoft.commerce.model.wallet.HoldingModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Holding Model Unit Tests")
class HoldingModelUnitTests {

    private HoldingModel holding;
    private AssetModel asset;

    @BeforeEach
    void setup() {
        asset = AssetModel.builder()
                .id(UUID.randomUUID())
                .name("Test Asset")
                .build();

        holding = HoldingModel.builder()
                .id(UUID.randomUUID())
                .asset(asset)
                .quantity(10.0)
                .accumulatedPrice(1000.0)
                .build();
    }

    @Test
    @DisplayName("Should create HoldingModel with correct values")
    void testHoldingModelCreation() {
        assertNotNull(holding.getId());
        assertEquals(asset, holding.getAsset());
        assertEquals(10.0, holding.getQuantity());
        assertEquals(1000.0, holding.getAccumulatedPrice());
    }

    @Test
    @DisplayName("Should increase quantity correctly")
    void testIncreaseQuantityAfterPurchase() {
        holding.increaseQuantityAfterPurchase(5.0);
        assertEquals(15.0, holding.getQuantity());

        holding.increaseQuantityAfterPurchase(0);
        assertEquals(15.0, holding.getQuantity());
    }

    @Test
    @DisplayName("Should increase accumulated price correctly")
    void testIncreaseAccumulatedPrice() {
        holding.increaseAccumulatedPrice(500.0);
        assertEquals(1500.0, holding.getAccumulatedPrice());

        holding.increaseAccumulatedPrice(0);
        assertEquals(1500.0, holding.getAccumulatedPrice());
    }

    @Test
    @DisplayName("Should set quantity and accumulated price correctly using setters")
    void testSetters() {
        holding.setQuantity(20.0);
        holding.setAccumulatedPrice(2000.0);

        assertEquals(20.0, holding.getQuantity());
        assertEquals(2000.0, holding.getAccumulatedPrice());
    }
}
