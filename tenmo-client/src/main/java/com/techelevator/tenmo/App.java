package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AccountService;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TransferService;

import java.util.ArrayList;
import java.util.List;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final AccountService accountService = new AccountService(API_BASE_URL);
    private final TransferService transferService = new TransferService(API_BASE_URL);

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
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
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
        System.out.println(accountService.getBalance(currentUser.getUser().getId()));
	}

	private void viewTransferHistory() {
        int id = currentUser.getUser().getId();
        Transfer[] transfers = transferService.listTransfers(id);
        Account userAccount = accountService.getAccountByUserId(id);
        System.out.println("ID: Transfers to/from: Amount: ");
        System.out.println("--------------------------");
        for(Transfer transfer:transfers){
            Account otherAccount = null;
            User otherUser = null;
            if(userAccount.getAccountId() == transfer.getAccountFrom() ){

                otherAccount = accountService.getAccountByAccountId(transfer.getAccountTo());
                otherUser = accountService.getUserById(otherAccount.getUserId());
                System.out.println(transfer.getTransferId() + "      " + "To : " + otherUser.getUsername() + "       $" + transfer.getAmount() );

            }
            else{

                otherAccount = accountService.getAccountByAccountId(transfer.getAccountFrom());
                otherUser = accountService.getUserById(otherAccount.getUserId());
                System.out.println(transfer.getTransferId() + "      " + "From : " + otherUser.getUsername() + "       $" + transfer.getAmount() );
            }

        }
	}

	private void viewPendingRequests() {
        int userId = currentUser.getUser().getId();
        Account userAccount = accountService.getAccountByUserId(userId);
        int accountId = userAccount.getAccountId();
        Transfer[] transfers = transferService.listPendingTransfers(accountId);
        System.out.println("Pending Transfers:");
        System.out.println("--------------------------");
        System.out.println("ID: To: Amount: ");
        System.out.println("--------------------------");
        System.out.println(transfers);
        for(Transfer transfer:transfers){
            Account otherAccount = null;
            User otherUser = null;
            otherAccount = accountService.getAccountByAccountId(transfer.getAccountTo());
            otherUser = accountService.getUserById(otherAccount.getUserId());
            System.out.println(transfer.getTransferId() + "      " + " To : " + otherUser.getUsername() + "       $" + transfer.getAmount() );

        }

		
	}

	private void sendBucks() {
        User[] users = accountService.listUsers();
        int idSelection = -1;
        System.out.println("Select someone to send TE bucks to: ");
        for(User user: users){
            System.out.println(user.getId() + " " + user.getUsername());

        }
        idSelection= consoleService.promptForMenuSelection("Enter ID of user you are sending to (0 to cancel):");
        if(idSelection < 1){
            mainMenu();
        }
        else{


        }

	}

	private void requestBucks() {
		// TODO Auto-generated method stub
		
	}

}
