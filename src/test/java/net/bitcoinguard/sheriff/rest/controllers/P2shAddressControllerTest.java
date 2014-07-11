package net.bitcoinguard.sheriff.rest.controllers;

import net.bitcoinguard.sheriff.core.entities.Key;
import net.bitcoinguard.sheriff.core.entities.P2shAddress;
import net.bitcoinguard.sheriff.core.entities.Transaction;
import net.bitcoinguard.sheriff.core.services.KeysRepositoryCustom;
import net.bitcoinguard.sheriff.core.services.P2shAddressesRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class P2shAddressControllerTest {
    @InjectMocks
    private P2shAddressController p2shAddressController;
    private MockMvc mockMvc;

    @Mock
    P2shAddressesRepository p2shAddressesRepository;
    @Mock
    KeysRepositoryCustom keysRepository;

    @Captor
    ArgumentCaptor<List<String>> captor;

    P2shAddress testAddress;
    Key testKey;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(p2shAddressController).build();
    }

    @Before
    public void initTestEntities(){
        testAddress = new P2shAddress();
        testAddress.setId(1L);
        testAddress.setAddress("testAddress");
        testAddress.setRedeemScript("redeemScript");
        List<Key> returnKeys = new ArrayList<>();
        testAddress.setKeys(returnKeys);
        testKey = new Key();
        testKey.setPublicKey("testKey");
        returnKeys.add(testKey);

    }



    @Test
    public void testFindExistingAddress() throws Exception {

        when(p2shAddressesRepository.findByAddress("testAddress")).thenReturn(testAddress);

        mockMvc.perform(get("/rest/addresses/testAddress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address", is(testAddress.getAddress())))
                .andExpect(jsonPath("$.links[*].href", hasItem(endsWith("/addresses/testAddress"))));

    }

    @Test
    public void testFindNonExistingAddress() throws Exception {

        when(p2shAddressesRepository.findByAddress("notExistingAddress")).thenReturn(null);

        mockMvc.perform(get("/rest/addresses/1"))
                .andExpect(status().isNotFound());

    }


    @Test
    public void testCreateNewAddress() throws Exception {

        when(keysRepository.generateNewKey()).thenReturn(testKey);
        when(p2shAddressesRepository.createNew(anyList(), any(Integer.class))).thenReturn(testAddress);

        mockMvc.perform(post("/rest/addresses")
                        .content("{\"keys\":[\"key1\",\"key2\"],\"requiredKeys\":2, \"totalKeys\":3}")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.address", is("testAddress")))
                .andExpect(jsonPath("$.keys[*]", hasItem("testKey")))
                .andExpect(jsonPath("$.links[*].href", hasItem(endsWith("/addresses/testAddress"))));

        verify(p2shAddressesRepository).createNew(captor.capture(), anyInt());

        List<String> keys = captor.getValue();
        assertThat(keys, contains("key1", "key2", testKey.getPublicKey()));

    }

    @Test
    public void testToManyKeys() throws Exception {
        mockMvc.perform(post("/rest/addresses")
                        .content("{\"keys\":[\"key1\",\"key2\"],\"requiredKeys\":2, \"totalKeys\":2}")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    public void testCreateTransaction() throws Exception{
        Transaction transaction = new Transaction();
        transaction.setTargetAddress("targetAddress");
        transaction.setAmount(10000L);
        transaction.setId(1L);
        transaction.setRawTransaction("rawTransaction");

        when(p2shAddressesRepository.createNewTransaction(transaction.getId(),transaction.getTargetAddress(),transaction.getAmount())).thenReturn(transaction);

        mockMvc.perform(post("/rest/addresses/1")
                        .content("{\"amount\":10000,\"targetAddress\":\"targetAddress\"}")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.links[*].href", hasItem(endsWith("/transactions/1"))));
    }

    @Test
    public void testNothingInRequest() throws Exception {
        mockMvc
                .perform(post("/rest/addresses")
                                .content("{}")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

}