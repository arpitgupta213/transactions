package com.tabcorp.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tabcorp.transaction.common.InvalidTransactionException;
import com.tabcorp.transaction.controller.advice.TransactionControllerAdvice;
import com.tabcorp.transaction.model.ro.CustomerTransactionRO;
import com.tabcorp.transaction.model.ro.ProductTransactionRO;
import com.tabcorp.transaction.model.ro.TransactionRO;
import com.tabcorp.transaction.service.TransactionService;
import com.tabcorp.transaction.setup.TransactionDataSetup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
public class TransactionControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private TransactionController transactionController;

    @Mock
    private TransactionService transactionService;

    private JacksonTester<TransactionRO> jsonTransactionConverter;

    private JacksonTester<List<TransactionRO>> jsonTransactionListConverter;

    private JacksonTester<List<CustomerTransactionRO>> jsonCustomerTransactionConverter;

    private JacksonTester<List<ProductTransactionRO>> jsonProductTransactionConverter;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        JacksonTester.initFields(this, objectMapper);
        TransactionControllerAdvice transactionControllerAdvice = new TransactionControllerAdvice();
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController)
                .setControllerAdvice(transactionControllerAdvice)
                .build();
    }

    @Test
    public void testSaveTransactionOk() throws Exception {
        when(transactionService.saveTransaction(any()))
                .thenReturn(TransactionDataSetup.getTransactionRO());
        final MvcResult response = this.mockMvc
                .perform(
                        post("/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        TransactionDataSetup.getTransactionRO())))
                .andExpect(status().isCreated())
                .andReturn();
        Assertions.assertEquals(jsonTransactionConverter.write(TransactionDataSetup.getTransactionRO()).getJson(),
                response.getResponse().getContentAsString());
    }

    @Test
    public void testAddTransactionInvalidCustomer() throws Exception {
        TransactionRO transactionRO = TransactionDataSetup.getTransactionRO();
        transactionRO.setCustomerId(null);
        final MvcResult response = this.mockMvc
                .perform(
                        post("/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        transactionRO)))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assertions.assertEquals("{\"type\":\"about:blank\",\"title\":\"Request data is not correct\",\"status\":400,\"detail\":\"Customer Id is required\",\"instance\":\"/transactions\"}",
                response.getResponse().getContentAsString());
    }

    @Test
    public void testAddTransactionInvalidDate() throws Exception {
        TransactionRO transactionRO = TransactionDataSetup.getTransactionRO();
        transactionRO.setTransactionTime(null);
        final MvcResult response = this.mockMvc
                .perform(
                        post("/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        transactionRO)))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assertions.assertEquals("{\"type\":\"about:blank\",\"title\":\"Request data is not correct\",\"status\":400,\"detail\":\"transaction time is required\",\"instance\":\"/transactions\"}",
                response.getResponse().getContentAsString());
    }

    @Test
    public void testAddTransactionInvalidProduct() throws Exception {
        TransactionRO transactionRO = TransactionDataSetup.getTransactionRO();
        transactionRO.setProductCode(null);
        final MvcResult response = this.mockMvc
                .perform(
                        post("/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        transactionRO)))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assertions.assertEquals("{\"type\":\"about:blank\",\"title\":\"Request data is not correct\",\"status\":400,\"detail\":\"Product code is required\",\"instance\":\"/transactions\"}",
                response.getResponse().getContentAsString());
    }

    @Test
    public void testAddTransactionWithInvalidTransaction() throws Exception {
        when(transactionService.saveTransaction(any()))
                .thenThrow(InvalidTransactionException.class);
        TransactionRO transactionRO = TransactionDataSetup.getTransactionRO();
        final MvcResult response = this.mockMvc
                .perform(
                        post("/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        transactionRO)))
                .andExpect(status().is4xxClientError())
                .andReturn();
        Assertions.assertEquals("{\"type\":\"about:blank\",\"title\":\"Transaction is invalid\",\"status\":400,\"instance\":\"/transactions\"}",
                response.getResponse().getContentAsString());
    }

    @Test
    public void testAddTransactionWithError() throws Exception {
        when(transactionService.saveTransaction(any()))
                .thenThrow(RuntimeException.class);
        TransactionRO transactionRO = TransactionDataSetup.getTransactionRO();
        final MvcResult response = this.mockMvc
                .perform(
                        post("/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        transactionRO)))
                .andExpect(status().isInternalServerError())
                .andReturn();
        Assertions.assertEquals("{\"type\":\"about:blank\",\"title\":\"Request failed\",\"status\":500,\"detail\":\"Server error. Check logs for details\",\"instance\":\"/transactions\"}",
                response.getResponse().getContentAsString());
    }

    @Test
    public void testSaveBulkTransactionOk() throws Exception {
        MultipartFile file = new MockMultipartFile("transactions.json", "[{\"customer_id\":10001,\"transaction_time\":\"2023-12-12 10:08:02\",\"quantity\":2,\"product_code\":\"PROD_005\"},{\"customer_id\":10001,\"transaction_time\":\"2022-12-12 10:08:02\",\"quantity\":7,\"product_code\":\"PRODUCT_003\"}]".getBytes(StandardCharsets.UTF_8));
        when(transactionService.saveBulkTransactions(any()))
                .thenReturn(Collections.singletonList(TransactionDataSetup.getTransactionRO()));
        final MvcResult response = this.mockMvc
                .perform(MockMvcRequestBuilders.multipart("/transactions/bulk-upload")
                                .file("file", file.getBytes())
                                        .characterEncoding("UTF-8"))
                .andExpect(status().isCreated())
                .andReturn();
        Assertions.assertEquals(jsonTransactionListConverter.write(Collections.singletonList(TransactionDataSetup.getTransactionRO())).getJson(),
                response.getResponse().getContentAsString());
    }

    @Test
    public void testSaveBulkTransactionWithNoFile() throws Exception {
        MultipartFile file = new MockMultipartFile("transactions.json", "[{\"customer_id\":10001,\"transaction_time\":\"2023-12-12 10:08:02\",\"quantity\":2,\"product_code\":\"PROD_005\"},{\"customer_id\":10001,\"transaction_time\":\"2022-12-12 10:08:02\",\"quantity\":7,\"product_code\":\"PRODUCT_003\"}]".getBytes(StandardCharsets.UTF_8));
        when(transactionService.saveBulkTransactions(any()))
                .thenReturn(Collections.singletonList(TransactionDataSetup.getTransactionRO()));
        final MvcResult response = this.mockMvc
                .perform(MockMvcRequestBuilders.multipart("/transactions/bulk-upload")
                        .characterEncoding("UTF-8"))
                .andExpect(status().isInternalServerError())
                .andReturn();
        Assertions.assertEquals("{\"type\":\"about:blank\",\"title\":\"Request failed\",\"status\":500,\"detail\":\"Server error. Check logs for details\",\"instance\":\"/transactions/bulk-upload\"}",
                response.getResponse().getContentAsString());
    }

    @Test
    public void testGetTransactionsCostByCustomersOk() throws Exception {
        when(transactionService.getTransactionsCostByCustomers(any()))
                .thenReturn(TransactionDataSetup.getCustomerTransactions());

        final MvcResult response = this.mockMvc
                .perform(
                        get("/transactions/customers-cost")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Assertions.assertEquals(jsonCustomerTransactionConverter.write(TransactionDataSetup.getCustomerTransactions()).getJson(),
                response.getResponse().getContentAsString());
    }

    @Test
    public void testGetTransactionsCostForCustomerOk() throws Exception {
        when(transactionService.getTransactionsCostByCustomers(any()))
                .thenReturn(TransactionDataSetup.getCustomerTransactions());

        final MvcResult response = this.mockMvc
                .perform(
                        get("/transactions/customers-cost")
                                .param("customer_id", "10001")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Assertions.assertEquals(jsonCustomerTransactionConverter.write(TransactionDataSetup.getCustomerTransactions()).getJson(),
                response.getResponse().getContentAsString());
    }

    @Test
    public void testGetTransactionsCostForCustomerNotOk() throws Exception {
        when(transactionService.getTransactionsCostByCustomers(any()))
                .thenThrow(RuntimeException.class);

        final MvcResult response = this.mockMvc
                .perform(
                        get("/transactions/customers-cost")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andReturn();
        Assertions.assertEquals("{\"type\":\"about:blank\",\"title\":\"Request failed\",\"status\":500,\"detail\":\"Server error. Check logs for details\",\"instance\":\"/transactions/customers-cost\"}",
                response.getResponse().getContentAsString());
    }

    @Test
    public void testGetTransactionsCostByProductsOk() throws Exception {
        when(transactionService.getTransactionsCostByProducts(any()))
                .thenReturn(TransactionDataSetup.getProductTransactions());

        final MvcResult response = this.mockMvc
                .perform(
                        get("/transactions/products-cost")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Assertions.assertEquals(jsonProductTransactionConverter.write(TransactionDataSetup.getProductTransactions()).getJson(),
                response.getResponse().getContentAsString());
    }

    @Test
    public void testGetTransactionsCostForProductOk() throws Exception {
        when(transactionService.getTransactionsCostByProducts(any()))
                .thenReturn(TransactionDataSetup.getProductTransactions());

        final MvcResult response = this.mockMvc
                .perform(
                        get("/transactions/products-cost")
                                .param("product_code", "PROD_005")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Assertions.assertEquals(jsonProductTransactionConverter.write(TransactionDataSetup.getProductTransactions()).getJson(),
                response.getResponse().getContentAsString());
    }

    @Test
    public void testGetTransactionsCostForProductNotOk() throws Exception {
        when(transactionService.getTransactionsCostByProducts(any()))
                .thenThrow(RuntimeException.class);

        final MvcResult response = this.mockMvc
                .perform(
                        get("/transactions/products-cost")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andReturn();
        Assertions.assertEquals("{\"type\":\"about:blank\",\"title\":\"Request failed\",\"status\":500,\"detail\":\"Server error. Check logs for details\",\"instance\":\"/transactions/products-cost\"}",
                response.getResponse().getContentAsString());
    }

    @Test
    public void testGetTransactionCountForLocationOk() throws Exception {
        Map<String, Long> transactionCount = new HashMap<>();
        transactionCount.put("transactionCount", 6L);
        when(transactionService.getTransactionCountForLocation(any()))
                .thenReturn(transactionCount);

        final MvcResult response = this.mockMvc
                .perform(
                        get("/transactions/count/Australia")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Assertions.assertEquals("{\"transactionCount\":6}",
                response.getResponse().getContentAsString());
    }

    @Test
    public void testGetTransactionCountForLocationNotOk() throws Exception {
        when(transactionService.getTransactionCountForLocation(any()))
                .thenThrow(RuntimeException.class);

        final MvcResult response = this.mockMvc
                .perform(
                        get("/transactions/count/Australia")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andReturn();
        Assertions.assertEquals("{\"type\":\"about:blank\",\"title\":\"Request failed\",\"status\":500,\"detail\":\"Server error. Check logs for details\",\"instance\":\"/transactions/count/Australia\"}",
                response.getResponse().getContentAsString());
    }
}
