package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.*;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AccountService accountService = new AccountService(API_BASE_URL);
    private final TransferService transferService = new TransferService(API_BASE_URL);
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final UserService userService = new UserService(API_BASE_URL);

    private AuthenticatedUser currentUser;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }
    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage("Error registering new user.");
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage("Log in has failed.");
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

	private void viewCurrentBalance() {
		try
        {
            int userId = currentUser.getUser().getId();
            accountService.setAuthToken(currentUser.getToken());

            BigDecimal balance = accountService.getCurrentBalance(userId);
            consoleService.printBalance(balance);

        }
        catch (Exception e)
        {
            consoleService.printErrorMessage("Could not retrieve balance.");
        }
		
	}

	private void viewTransferHistory() {
        int userId = currentUser.getUser().getId();
        transferService.setAuthToken(currentUser.getToken());

        List<Transfer> transferHistory = transferService.getAllTransfersForUser(userId);

        if (transferHistory.isEmpty())
        {
            consoleService.printErrorMessage("No transfers found for user: " + currentUser.getUser().getUsername());
        }
        else
        {
            consoleService.printTransfers(transferHistory, userId);
        }
        //prompt for transferid to get transfer details
        int transferId = consoleService.promptForTransferId();
        int userAccountId = userService.getAccountIdByUserId(userId);

        if (transferId != 0) {
            Transfer transfer = transferService.getTransferById(transferId);
            if (transfer != null && (transfer.getAccountFrom() == userAccountId || transfer.getAccountTo() == userAccountId)) {
                consoleService.printTransferDetails(transfer);
            } else {
                consoleService.printErrorMessage("Invalid transfer ID or transfer does not belong to the user.");
            }
        }
    }

	private void viewPendingRequests() {
		// TODO Auto-generated method stub
		
	}


/* TODO with sendBucks : 1> add more informed print messages, 2> chop up sendBucks into
   TODO smaller pieces-- split checking for valid transfers/transfer/etc. 3> Also needs to be atomic- if one update fails, it
   TODO all fails. 4> Double check security measures. */

	private void sendBucks() {
		//display eligible users and prompt for recipient and amount
        List<User> users = userService.getAllUsersExcludingCurrent(currentUser.getUser().getId());
        consoleService.printUsers(users);
        int recipientUserId = consoleService.promptForUserId();
        BigDecimal amount = consoleService.promptForAmount();

        //validate input
        if (recipientUserId == currentUser.getUser().getId() || amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            consoleService.printErrorMessage("Invalid recipient or amount.");
            return;
        }

        //balance check
        BigDecimal balance = accountService.getCurrentBalance(currentUser.getUser().getId());
        if(balance.compareTo(amount) < 0)
        {
            consoleService.printErrorMessage("Insufficient balance.");
            return;
        }

        //retrieve account ids
        Account senderAccount = accountService.getAccountByUserId(currentUser.getUser().getId());
        Account recipientAccount = accountService.getAccountByUserId(recipientUserId);

        //check if both accounts are retrieved
        if (senderAccount == null || recipientAccount == null)
        {
            consoleService.printErrorMessage("One of the accounts could not be found.");
            return;
        }

        //create a pending transfer
        Transfer transfer = new Transfer();
        transfer.setTransferType(TransferType.SEND);
        transfer.setTransferStatus(TransferStatus.PENDING);
        transfer.setAccountFrom(senderAccount.getAccountId());
        transfer.setAccountTo(recipientAccount.getAccountId());
        transfer.setAmount(amount);

        //execute transfer and update balances within transactional context
        Transfer createdTransfer = transferService.createTransfer(transfer);
        if (createdTransfer == null)
        {
            consoleService.printErrorMessage("Failed to initiate transfer.");
            return;
        }
//        BigDecimal balance = accountService.getCurrentBalance(currentUser.getUser().getId());
        //update balances
        boolean senderBalanceUpdated = accountService.updateBalance(currentUser.getUser().getId(), balance.subtract(amount));
		boolean recipientBalanceUpdated = accountService.updateBalance(recipientUserId, accountService.getCurrentBalance(recipientUserId).add(amount));

        //finalize transfer
        if (senderBalanceUpdated && recipientBalanceUpdated)
        {
            createdTransfer.setTransferStatus(TransferStatus.APPROVED);
            consoleService.printSuccessMessage("Transfer successful.");
        }
        else
        {
            createdTransfer.setTransferStatus(TransferStatus.REJECTED);
            consoleService.printErrorMessage("Transfer failed during balance update.");
        }
        transferService.updateTransfer(createdTransfer);
	}

	private void requestBucks() {
        //display eligible users and prompt for recipient and amount
        List<User> users = userService.getAllUsersExcludingCurrent(currentUser.getUser().getId());
        consoleService.printUsers(users);
        int requestedUserId = consoleService.promptForUserId();
        BigDecimal amount = consoleService.promptForAmount();

        //validate input
        if (requestedUserId == currentUser.getUser().getId() || amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            consoleService.printErrorMessage("Invalid recipient or amount.");
            return;
        }

        //balance check
        BigDecimal balance = accountService.getCurrentBalance(currentUser.getUser().getId());
        if(balance.compareTo(amount) < 0)
        {
            consoleService.printErrorMessage("Insufficient balance.");
            return;
        }

        //retrieve account ids
        Account accountSendingRequest = accountService.getAccountByUserId(currentUser.getUser().getId());
        Account accountReceivingRequest = accountService.getAccountByUserId(requestedUserId);

        //check if both accounts are retrieved
        if (accountSendingRequest == null || accountReceivingRequest == null)
        {
            consoleService.printErrorMessage("One of the accounts could not be found.");
            return;
        }

        //create a pending transfer
        Transfer transfer = new Transfer();
        transfer.setTransferType(TransferType.REQUEST);
        transfer.setTransferStatus(TransferStatus.PENDING);
        transfer.setAccountFrom(accountReceivingRequest.getAccountId());
        transfer.setAccountTo(accountSendingRequest.getAccountId());
        transfer.setAmount(amount);

        //execute transfer and update balances within transactional context
        Transfer createdTransfer = transferService.createTransfer(transfer);
        if (createdTransfer == null)
        {
            consoleService.printErrorMessage("Failed to initiate transfer.");
            return;
        }

//        //update balances
//        boolean senderBalanceUpdated = accountService.updateBalance(currentUser.getUser().getId(), balance.subtract(amount));
//        boolean recipientBalanceUpdated = accountService.updateBalance(recipientUserId, accountService.getCurrentBalance(recipientUserId).add(amount));
//
//        //finalize transfer
//        if (senderBalanceUpdated && recipientBalanceUpdated)
//        {
//            createdTransfer.setTransferStatus(TransferStatus.APPROVED);
//            consoleService.printSuccessMessage("Request transfer successful.");
//        }
//        else
//        {
//            createdTransfer.setTransferStatus(TransferStatus.REJECTED);
//            consoleService.printErrorMessage("Transfer failed during balance update.");
//        }
//        transferService.updateTransfer(createdTransfer);
    }
}
