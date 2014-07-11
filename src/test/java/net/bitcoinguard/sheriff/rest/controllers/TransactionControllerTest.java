package net.bitcoinguard.sheriff.rest.controllers;

import net.bitcoinguard.sheriff.core.entities.Transaction;
import net.bitcoinguard.sheriff.core.services.TransactionsService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TransactionControllerTest {
    @InjectMocks
    private TransactionController transactionController;
    private MockMvc mockMvc;

    @Mock
    TransactionsService transactionsService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
    }


    @Test
    public void testFindExistingTransaction() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(100000L);


        when(transactionsService.findOne(1L)).thenReturn(transaction);

        mockMvc.perform(get("/rest/transactions/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(100000)))
                .andExpect(jsonPath("$.links[*].href", hasItem(endsWith("/transactions/1"))));
    }

    @Test
    public void testFindNonExistingAddress() throws Exception {

        when(transactionsService.findOne(1L)).thenReturn(null);

        mockMvc.perform(get("/rest/transactions/1"))
                .andExpect(status().isNotFound());

    }
}