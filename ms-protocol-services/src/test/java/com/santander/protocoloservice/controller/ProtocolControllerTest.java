package com.santander.protocoloservice.controller;

import com.santander.protocoloservice.services.ProtocolSequencialServicos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProtocolController.class)
class ProtocolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProtocolSequencialServicos protocolSequencialServicos;

    @Test
    void shouldCreateProtocol() throws Exception {
        // Arrange
        when(protocolSequencialServicos.generateProtocol("AUTO"))
                .thenReturn("AUTO-2026-00001");

        // Act + Assert
        mockMvc.perform(post("/")
                        .param("code", "AUTO"))
                .andExpect(status().isCreated())
                .andExpect(content().string("AUTO-2026-00001"));
    }

    @Test
    void shouldReturnBadRequestWhenCodeIsMissing() throws Exception {
        mockMvc.perform(post("/"))
                .andExpect(status().isBadRequest());
    }
}
