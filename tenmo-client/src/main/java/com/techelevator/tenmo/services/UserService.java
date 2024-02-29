package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserService {
    private final String API_BASE_URL = "http://localhost:8080/users";
    private final RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;

    public UserService(String API_BASE_URL) {
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public List<User> getAllUsers() {
        try
        {
            ResponseEntity<List<User>> response = restTemplate.exchange(
                    API_BASE_URL,
                    HttpMethod.GET,
                    makeAuthEntity(),
                    new ParameterizedTypeReference<List<User>>() {}
            );
            if (response.getStatusCode() == HttpStatus.OK)
            {
                return response.getBody();
            }
            else
            {
                System.out.println("Failed to get users. Status code: " + response.getStatusCode());
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

    public User getUserById(int userId) {
        try
        {
            ResponseEntity<User> response = restTemplate.exchange(
              API_BASE_URL + "/" + userId,
              HttpMethod.GET,
              makeAuthEntity(),
              User.class
            );
            if (response.getStatusCode() == HttpStatus.OK)
            {
                return response.getBody();
            }
            else
            {
                System.out.println("Failed to get the user. Status code: " + response.getStatusCode());
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

    public String getUsernameByAccountId(int accountId) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    API_BASE_URL + "/" + accountId + "/username",
                    HttpMethod.GET,
                    makeAuthEntity(),
                    String.class
            );
            if (response.getStatusCode() == HttpStatus.OK)
            {
                return response.getBody();
            }
            else
            {
                System.out.println("Failed to get the username. Status code: " + response.getStatusCode());
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


    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }

    //helper method to filter out the current user when printing all users
    public List<User> getAllUsersExcludingCurrent(int currentUserId) {
        List<User> allUsers = getAllUsers();
        List<User> filteredUsers = new ArrayList<>();

        if (allUsers == null)
        {
            return Collections.emptyList();
        }

        for (User user : allUsers)
        {
            if (user.getId() != currentUserId)
            {
                filteredUsers.add(user);
            }
        }
        return filteredUsers;
    }

    public String getUsernameByUserId(int userId) {
        User user = getUserById(userId);
        if (user != null)
        {
            return user.getUsername();
        }
        else
        {
            System.out.println("User not found for ID: " + userId);
            return null;
        }
    }
}
