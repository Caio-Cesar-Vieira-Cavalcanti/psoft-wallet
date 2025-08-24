package com.ufcg.psoft.commerce.service.admin;

import com.ufcg.psoft.commerce.model.user.AdminModel;

public interface AdminService {
    void validateAdmin(String email, String accessCode);
    AdminModel getAdmin();
}
