package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.util.BasicLogger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

public class TransferService {
    private final String API_BASE_URL = "http://localhost:8080/transfers";
    private final RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;

    public void setAuthToken(String authToken) {this.authToken = authToken;}

    //potentially need to add authToken as param
    public TransferService(String API_BASE_URL) {
    }

    public Transfer createTransfer(Transfer transfer) {
        try {
            ResponseEntity<Transfer> response = restTemplate.exchange (
                    API_BASE_URL,
                    HttpMethod.POST,
                    makeTransferEntity(transfer),
                    Transfer.class
            );
            if (response.getStatusCode() == HttpStatus.CREATED)
            {
                return response.getBody();
            }
            else
            {
                System.out.println("Failed to create transfer. Status code: " + response.getStatusCode());
            }
        } catch (RestClientResponseException e)
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
//TODO FINISH TRANSFERSERVICE: GET ALL TRANSFERS FOR USER, AND UPDATE TRANSFER
    public Transfer getTransferById(int transferId) {
        try
        {
            ResponseEntity<Transfer> response = restTemplate.exchange(
                    API_BASE_URL + transferId,
                    HttpMethod.GET,
                    makeAuthEntity(),
                    Transfer.class
            );

            if (response.getStatusCode() == HttpStatus.OK)
            {
                return response.getBody();
            }
            else
            {
                System.out.println("Failed to get transfer details, Status code: " + response.getStatusCode());
            }
        }
        catch (RestClientResponseException e)
        {
            handleRestClientResponseException(e);
        }
        catch (ResourceAccessException e)
        {
            BasicLogger.log(e.getMessage());
            System.out.println("Cannot access the resource " + e.getMessage());
        }
        return null;
    }



    public List<Transfer> getAllTransfersForUser(int userId) {
        try
        {
            ResponseEntity<List<Transfer>> response = restTemplate.exchange(
                    API_BASE_URL + "/users/" + userId,
                    HttpMethod.GET,
                    makeAuthEntity(),
                    new ParameterizedTypeReference<List<Transfer>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK)
            {
                return response.getBody();
            }
            else
            {
                System.out.println("Failed to get transfer list. Status code: " + response.getStatusCode());
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
        return Collections.emptyList();
    }


    public Transfer updateTransfer(Transfer transfer) {
        try
        {
            ResponseEntity<Transfer> response = restTemplate.exchange(
                    API_BASE_URL + transfer.getTransferId(),
                    HttpMethod.PUT,
                    makeTransferEntity(transfer),
                    Transfer.class
            );

            if (response.getStatusCode() == HttpStatus.OK)
            {
                return response.getBody();
            }
            else
            {
                System.out.println("Failed to update the transfer. Status code: " + response.getStatusCode());
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



    private HttpEntity<Transfer> makeTransferEntity(Transfer transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(transfer, headers);
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
