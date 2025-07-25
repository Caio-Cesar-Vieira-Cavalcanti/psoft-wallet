package com.ufcg.psoft.commerce.service.admin;

import com.ufcg.psoft.commerce.exception.admin.UnauthorizedAdminAccessException;
import com.ufcg.psoft.commerce.model.AccessCodeModel;
import com.ufcg.psoft.commerce.model.AdminModel;
import com.ufcg.psoft.commerce.model.EmailModel;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminService {
    private AdminModel admin;

    public AdminService() {
        this.admin = new AdminModel(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "admin",
                new EmailModel("admin@example.com"),
                new AccessCodeModel("123456")
        );
    }

    public void validateAdmin(String email, String accessCode) {
        if (!admin.getEmail().matches(email) || !admin.getAccessCode().matches(accessCode)) {
            throw new UnauthorizedAdminAccessException();
        }
    }
}
