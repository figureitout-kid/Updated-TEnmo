package com.techelevator.tenmo.services;


import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;
@Service
public class ConsoleService {
    private static final String API_BASE_URL = "http://localhost:8080/";
    private AuthenticatedUser currentUser;
    private UserService userService = new UserService(API_BASE_URL);
    private final Scanner scanner = new Scanner(System.in);

    @Autowired
    public ConsoleService() {
        this.userService = userService;
    }

    public int promptForMenuSelection(String prompt) {
        int menuSelection;
        System.out.print(prompt);
        try {
            menuSelection = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            menuSelection = -1;
        }
        return menuSelection;
    }

    public void printGreeting() {
        System.out.println("*********************");
        System.out.println("* Welcome to TEnmo! *");
        System.out.println("*********************");
    }

    public void printLoginMenu() {
        System.out.println();
        System.out.println("1: Register");
        System.out.println("2: Login");
        System.out.println("0: Exit");
        System.out.println();
    }

    public void printMainMenu() {
        System.out.println();
        System.out.println("1: View your current balance");
        System.out.println("2: View your past transfers");
        System.out.println("3: View your pending requests");
        System.out.println("4: Send TE bucks");
        System.out.println("5: Request TE bucks");
        System.out.println("0: Exit");
        System.out.println();
    }



    public UserCredentials promptForCredentials() {
        String username = promptForString("Username: ");
        String password = promptForString("Password: ");
        return new UserCredentials(username, password);
    }

    public String promptForString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public int promptForInt(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    public BigDecimal promptForBigDecimal(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                return new BigDecimal(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a decimal number.");
            }
        }
    }

    public void pause() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public void printBalance(BigDecimal balance) { System.out.println("Your current balance is: $" + balance);}
    public void printErrorMessage(String message) { System.out.println(message);}
    public void printSuccessMessage(String message) { System.out.println(message); }

    //TODO : make the printing of user names and ids a little more fun/creative
    public void printUsers(List<User> users) {
        System.out.println("USERS");
        System.out.println(String.format("%-10s %-10s", "ID", "NAME"));
        System.out.println("----------------------------------");

        for (User user : users)
        {
            System.out.println(String.format("%-10d %-10s", user.getId(), user.getUsername()));
        }

        System.out.println("----------------------------------");
    }

    public int promptForUserId() {
        System.out.print("\nEnter the ID of the user you want to send TE bucks to - ");
        return promptForInt("User ID: ");
    }

    public void printTransfers(List<Transfer> transfers, int userId) {

        System.out.println("TRANSFERS:");
        System.out.println(String.format("%-10s %-25s %-10s", "ID" , "FROM/TO", "AMOUNT"));
        System.out.println("--------------------------------------------");

        int userAccountId = userService.getAccountIdByUserId(userId);
        for (Transfer transfer : transfers)
        {
            String fromTo = "Unknown";
            String positiveNegative = "";
            if (transfer.getAccountFrom() == userAccountId)
            {
                //outgoing transfer, show who it is "to"
                String username = userService.getUsernameByAccountId(transfer.getAccountTo());
                fromTo = "To: " + username;
                positiveNegative = "-";
            }
            else  if (transfer.getAccountTo() == userAccountId)
            {
                //incoming transfer, show who it "from"
                String username = userService.getUsernameByAccountId(transfer.getAccountFrom());
                fromTo = "From: " + username;
                positiveNegative = "+";
            }

            System.out.println(String.format("%-10d %-25s $%s%-10.2f",
                                             transfer.getTransferId(),
                                             fromTo,
                                             positiveNegative, transfer.getAmount()));
        }
        System.out.println("--------------------------------------------");
    }

    public BigDecimal promptForAmount() {
        return promptForBigDecimal("Enter the amount of TE bucks to send: ");
    }

}


