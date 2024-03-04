package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.*;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
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
        //reminder: in this context the sender is the current user who is viewing their pending requests for money
        int userId = currentUser.getUser().getId();
        transferService.setAuthToken(currentUser.getToken());

        List<Transfer> allTransfers = transferService.getAllTransfersForUser(userId);
        List<Transfer> pendingTransfers = new ArrayList<>();

        for (Transfer transfer : allTransfers) {
            if (transfer.getTransferStatus() == TransferStatus.PENDING) {
                pendingTransfers.add(transfer);
            }
        }

        if (pendingTransfers.isEmpty()) {
            consoleService.printErrorMessage("No pending transfers found.");
        } else {
            consoleService.printTransfers(pendingTransfers, userId);
        }

        //TODO-- the following code is recycled to print transfer information, can just create a separate helper method to use here and in view transferHistory, as well as a createTransferObject method

        //prompt for transferid to get transfer details
        int transferId = consoleService.promptForTransferIdForPending();
        if (transferId == 0) return;

        Transfer transfer = transferService.getTransferById(transferId);

        //check transferId exists, and ensure the account from is actually the user
        if (transferId != 0) {
            if (transfer != null && transfer.getAccountFrom() == userService.getAccountIdByUserId(userId)) {
                consoleService.printTransferDetails(transfer);
            } else {
                consoleService.printErrorMessage("Invalid transfer ID or transfer does not belong to the user.");
                return;
            }
        }

        //prompt for approval or rejection
        int approveOrReject = consoleService.promptForApproveReject();

        //store accountIds for each account
        int receivingAccountId = transfer.getAccountTo();
        int sendingAccountId = transfer.getAccountFrom();

        //retrieve userIds from accountIds to updateBalances accordingly
        int receivingUserId = accountService.getUserIdByAccountId(receivingAccountId);
        int sendingUserId = accountService.getUserIdByAccountId(sendingAccountId);

        switch (approveOrReject) {
            case 1: //approve
                BigDecimal balance = accountService.getCurrentBalance(userId);
                if (balance.compareTo(transfer.getAmount()) >= 0) {
                    boolean senderBalanceUpdated = accountService.updateBalance(userId, balance.subtract(transfer.getAmount()));
                    boolean receiverBalanceUpdated = accountService.updateBalance(receivingUserId, accountService.getCurrentBalance(receivingUserId).add(transfer.getAmount()));
                    if (senderBalanceUpdated && receiverBalanceUpdated)
                    {
                        transferService.updateTransferStatus(transferId, TransferStatus.APPROVED);
                        consoleService.printSuccessMessage("Transfer approved.");
                    }
                }
                else
                {
                    consoleService.printErrorMessage("Insufficient balance to approve transfer.");
                }
                break;
            case 2: //reject
                if (transfer != null && transfer.getTransferStatus() == TransferStatus.PENDING)
                {
                    transferService.updateTransferStatus(transferId, TransferStatus.REJECTED);
                    consoleService.printSuccessMessage("Transfer rejected.");
                } else {
                    consoleService.printErrorMessage("Could not reject transfer.");
                }
                break;
            default:
                break;

        }
    }



/* TODO with sendBucks : 1> add more informed print messages, 2> chop up sendBucks into
   TODO smaller pieces-- split checking for valid transfers/transfer/etc. 3> Also needs to be atomic- if one update fails, it
   TODO all fails. 4> Double check security measures. */

	private void sendBucks() {
		//display eligible users and prompt for recipient and amount
        List<User> users = userService.getAllUsersExcludingCurrent(currentUser.getUser().getId());
        consoleService.printUsers(users);
        int recipientUserId = consoleService.promptForUserIdToSend();
        BigDecimal amount = consoleService.promptForAmountSend();

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
        int requestedUserId = consoleService.promptForUserIdToRequest();
        BigDecimal amount = consoleService.promptForAmountRequest();

        //validate input
        if (requestedUserId == currentUser.getUser().getId() || amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            consoleService.printErrorMessage("Invalid recipient or amount.");
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
            consoleService.printErrorMessage("Failed to initiate request.");
            return;
        }
        else { consoleService.printSuccessMessage("Request successfully sent to " + userService.getUsernameByUserId(requestedUserId));}
    }
}


