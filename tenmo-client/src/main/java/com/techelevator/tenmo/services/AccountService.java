package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

public class AccountService {
    private final String API_BASE_URL = "http://localhost:8080/accounts";
    private final String BALANCE_URL = "/balance";
    private final RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;
    private BigDecimal currentBalance;

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public AccountService(String API_BASE_URL) {
    }

    public BigDecimal getCurrentBalance(int userId) {
        try
        {
            ResponseEntity<BigDecimal> response = restTemplate.exchange (
                API_BASE_URL + BALANCE_URL + "/" + userId,
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

    public boolean updateBalance(int userId, BigDecimal newBalance) {
        String updateBalanceUrl = API_BASE_URL + "/" + userId + BALANCE_URL ;

        HttpEntity<BigDecimal> entity = new HttpEntity<>(newBalance, makeAuthEntity().getHeaders());

        try
        {
            restTemplate.put(updateBalanceUrl, entity);
            return true;
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
        return false;
    }

    public Account getAccountByUserId(int userId) {
        String url = API_BASE_URL + "/user/" + userId + "/account";
        HttpEntity<Void> entity = makeAuthEntity();
        try
        {
            ResponseEntity<Account> response = restTemplate.exchange(url, HttpMethod.GET, entity, Account.class);
            return response.getBody();
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

    public Integer getUserIdByAccountId(int accountId) {
        String url = API_BASE_URL + "/" + accountId + "/account-to-userId";
        HttpEntity<Void> entity = makeAuthEntity();

        try
        {
            ResponseEntity<Integer> response = restTemplate.exchange(url, HttpMethod.GET, entity, Integer.class);
            return response.getBody();
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
