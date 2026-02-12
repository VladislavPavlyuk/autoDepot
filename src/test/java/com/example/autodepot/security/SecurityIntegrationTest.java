package com.example.autodepot.security;

import com.example.autodepot.AbstractPostgresTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class SecurityIntegrationTest extends AbstractPostgresTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fleetDashboard_WhenUnauthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/fleet/dashboard"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("/login*"));
    }

    @Test
    void apiDashboard_WhenUnauthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("/login*"));
    }

    @Test
    void login_WhenValidCredentials_RedirectsToDashboard() throws Exception {
        mockMvc.perform(formLogin()
                .user("admin")
                .password("admin"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/fleet/dashboard"))
            .andExpect(authenticated().withUsername("admin"));
    }

    @Test
    void login_WhenInvalidPassword_RedirectsToLoginWithError() throws Exception {
        mockMvc.perform(formLogin()
                .user("admin")
                .password("wrong"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?error"))
            .andExpect(unauthenticated());
    }

    @Test
    void fleetDashboard_WhenAuthenticated_Returns200() throws Exception {
        mockMvc.perform(get("/fleet/dashboard").with(user("admin")))
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard"));
    }

    @Test
    void apiDashboard_WhenAuthenticated_Returns200() throws Exception {
        mockMvc.perform(get("/api/dashboard").with(user("admin")))
            .andExpect(status().isOk());
    }

    @Test
    void logout_WhenAuthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(post("/logout").with(user("admin")).with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?logout"));
    }
}
