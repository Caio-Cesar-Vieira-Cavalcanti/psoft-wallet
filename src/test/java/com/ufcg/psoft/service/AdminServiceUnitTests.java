package com.ufcg.psoft.service;

import com.ufcg.psoft.commerce.exception.user.UnauthorizedUserAccessException;
import com.ufcg.psoft.commerce.model.user.AdminModel;
import com.ufcg.psoft.commerce.service.admin.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Admin Service Unit Tests")
class AdminServiceUnitTests {

    private AdminServiceImpl adminService;
    private AdminModel adminMock;

    @BeforeEach
    void setUp() {
        adminMock = mock(AdminModel.class);

        adminService = new AdminServiceImpl();

        org.springframework.test.util.ReflectionTestUtils.setField(adminService, "admin", adminMock);
    }

    @Test
    @DisplayName("Should call validateAccess with correct parameters")
    void shouldCallValidateAccessWithCorrectParameters() {
        String email = "admin@example.com";
        String accessCode = "123456";

        adminService.validateAdmin(email, accessCode);

        verify(adminMock, times(1)).validateAccess(email, accessCode);
    }

    @Test
    @DisplayName("Should throw exception when validateAccess fails")
    void shouldThrowExceptionWhenValidateAccessFails() {
        String email = "wrong@example.com";
        String accessCode = "wrongcode";

        doThrow(new UnauthorizedUserAccessException("Unauthorized admin access: email or access code is incorrect"))
                .when(adminMock).validateAccess(email, accessCode);

        UnauthorizedUserAccessException ex = assertThrows(UnauthorizedUserAccessException.class, () -> {
            adminService.validateAdmin(email, accessCode);
        });

        assertEquals("Unauthorized admin access: email or access code is incorrect", ex.getMessage());

        verify(adminMock).validateAccess(email, accessCode);
    }
}
