package com.techelevator.tenmo.services;

import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

public class AccountService {
    private final String BASE_ACCOUNT_URL = "http://localhost:8080/accounts";
    private final String BALANCE_URL = "/balance";
    private final RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;
    private BigDecimal currentBalance;

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public BigDecimal getCurrentBalance(int userId) {
        try
        {
            ResponseEntity<BigDecimal> response = restTemplate.exchange (
                userId + BASE_ACCOUNT_URL + BALANCE_URL,
                    HttpMethod.GET,
                    makeAuthEntity(),
                    BigDecimal.class
            );
            if (response.getStatusCode() == HttpStatus.OK)
            {
                return response.getBody();
            }
            else
            {
                System.out.println("Failed to get balance. Status code: " + response.getStatusCode());
            }
        }
        catch (RestClientResponseException e)
        {
            handleRestClientResponseException(e);
        }
        catch (ResourceAccessException e)
        {
            BasicLogger.log(e.getMessage());
            System.out.println("Cannot access the resource: " + e.getMessage());
        }
        return null;
    }

// TODO add getAccountByAccountId and getAccountByUserId --------------------------------------------------------


    //helper methods

    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }

    private void handleRestClientResponseException(RestClientResponseException e) {
        BasicLogger.log(e.getMessage());
        if (e.getRawStatusCode() == HttpStatus.UNAUTHORIZED.value()) {
            System.out.println("Not authorized to perform this operation. Please log in.");
        } else if (e.getRawStatusCode() == HttpStatus.BAD_REQUEST.value()) {
            System.out.println("An error occurred while processing the request: " + e.getStatusText());
        } else {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

}
