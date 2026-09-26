package com.fudn.orderservice;

import com.fudn.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureMockMvc
class OrderControllerValidationTests {

    @MockBean
    private OrderService orderService;

    private final MockMvc mockMvc;

    @Autowired
    OrderControllerValidationTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void shouldRejectOrderWithoutSkuCode() throws Exception {
        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"price": 1000, "quantity": 1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                        {"message":"Request validation failed","errors":{"skuCode":"skuCode is required"}}
                        """));
    }

    @Test
    void shouldRejectInvalidQuantityType() throws Exception {
        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skuCode":"galaxy_24","price":1000,"quantity":"abc"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectEmptyBody() throws Exception {
        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/api/order")
                        .content("not-json"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void shouldRejectGetRequest() throws Exception {
        mockMvc.perform(get("/api/order"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void shouldRejectUnknownPath() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skuCode":"iphone_15","price":1000,"quantity":1}
                                """))
                .andExpect(status().isNotFound());
    }
}
