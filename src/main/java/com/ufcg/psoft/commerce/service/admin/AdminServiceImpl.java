package com.ufcg.psoft.commerce.service.admin;

import com.ufcg.psoft.commerce.model.user.AccessCodeModel;
import com.ufcg.psoft.commerce.model.user.AdminModel;
import com.ufcg.psoft.commerce.model.user.EmailModel;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminServiceImpl implements AdminService {
    private AdminModel admin;

    public AdminServiceImpl() {
        this.admin = new AdminModel(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "admin",
                new EmailModel("admin@example.com"),
                new AccessCodeModel("123456")
        );
    }

    public void validateAdmin(String email, String accessCode) {
        this.admin.validateAccess(email, accessCode);
    }
}
