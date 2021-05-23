/*basic banking system.
log in to your account with account number and password
or create new checking or saving account
functions: deposit, withdraw, check balance, get account info, get statement, change pin
*/
package bank;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Scanner;

public class Bank {
    
    static DateTimeFormatter format = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    private static final String CONN_STRING = "jdbc:mysql://localhost:3306/bank";
    
    static Connection conn = null;
    
    public static void accStatement(int accNumber) throws SQLException{
        Statement stmt = conn.createStatement();
        String sql = "SELECT * FROM transactions WHERE accNumber = '"+accNumber+"'";
        ResultSet rs = stmt.executeQuery(sql);
        System.out.println("**Account Statement**");
        while(rs.next()){
            
            System.out.print(rs.getString("txDate") + "   ");
            System.out.print("- " + rs.getString("txType") + " -");
            System.out.print("\t");
            System.out.print("$" + rs.getInt("txAmount"));
            System.out.print("\t");
            System.out.println("Balance: $" + rs.getInt("balance"));
            
        }
       
        System.out.println();
        menu(accNumber);
    }
    public static void changePin(int accNumber) throws SQLException{
        System.out.print("Enter current PIN: ");
        Scanner input = new Scanner(System.in);
        int oldPin = input.nextInt();
        Statement stmt = conn.createStatement();
        String sql = "SELECT pin FROM accounts WHERE accNumber = '"+accNumber+"'";
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        int pin = rs.getInt("pin");
        
        //checking if the pin matches
        if (oldPin == pin){
            System.out.print("Enter new PIN: ");
            int newPin = input.nextInt();
            sql = "UPDATE accounts SET pin = '"+newPin+"' WHERE accNumber = '"+accNumber+"'";
            stmt.executeUpdate(sql);
            System.out.println("PIN successfully changed.");
        }
        else{
            System.out.println("Wrong PIN. Please try again.");
            changePin(accNumber);
        }
        
    }
    
    public static int checkBalance(int accNumber) throws SQLException{
        
        Statement stmt = conn.createStatement();
        String sql = "SELECT amount FROM accounts WHERE accNumber = '"+accNumber+"'";
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        int amount = rs.getInt("amount");
        return amount;
    } 
    
    public static void getInfo(int accNumber) throws SQLException{
        Statement stmt = conn.createStatement();
        String sql = "SELECT * FROM accounts WHERE accNumber = '"+accNumber+"'";
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        System.out.println("*Account Info*");
        System.out.println("Account Number: " + rs.getInt("accNumber"));
        System.out.println("Account Holder Name: " + rs.getString("accName"));
        System.out.println("Account Balace: $" + rs.getInt("amount"));
        System.out.println("Account Type: " + rs.getString("type"));
        System.out.println("Account Creation Date: " + rs.getString("date"));
        System.out.println();
        menu(accNumber);
       
    }
    
    public static void withdraw(int accNumber) throws SQLException{
        int amount = checkBalance(accNumber);
        System.out.println("Account Balance: $" + amount);
        System.out.println("How much would you like to withdraw?");
        System.out.print("Amount: $");
        Scanner input = new Scanner(System.in);
        int money = input.nextInt();
        
        //checking if the account has enough money for this withdrawal
        if (money <= amount){
           amount = amount - money; 
        }
        else{
            System.out.println("Insufficent account balance for this withdrawal.");
            menu(accNumber);
        }
        Statement stmt = conn.createStatement();
        String sql = "UPDATE accounts SET amount = '"+amount+"' WHERE accNumber = '"+accNumber+"'";
        stmt.executeUpdate(sql);
        System.out.println("Withdrawal successful.");
        
        //displaying account balance after withdrawal
        amount = checkBalance(accNumber);
        System.out.println("Account Balance: $" + amount);
        
        //adding this transaction to the transaction history table
        LocalDateTime ldt = LocalDateTime.now();
        String date = ldt.format(format);
        sql = "INSERT INTO transactions VALUES ('"+accNumber+"', '"+money+"', 'withdrawal', '"+date+"', '"+amount+"')";
        stmt.executeUpdate(sql);
        System.out.println();
        
        menu(accNumber);
        
    }
    
    public static void deposit(int accNumber) throws SQLException{
        int amount = checkBalance(accNumber);
        System.out.println("Account Balance: $" + amount);
        
        System.out.println("How much would you like to deposit?");
        System.out.print("Amount: $");
        Scanner input = new Scanner(System.in);
        int deposit = input.nextInt();
       
        //updating database with the new account balance
        amount = amount + deposit;
        Statement stmt = conn.createStatement();
        String sql = "UPDATE accounts SET amount = '"+amount+"' WHERE accNumber = '"+accNumber+"'";
        stmt.executeUpdate(sql);
        System.out.println("Deposit successful.");
        
        //displaying account balance after deposit
        amount = checkBalance(accNumber);
        System.out.println("Account Balance: $" + amount);
        
        //adding this transaction to the transaction history table
        LocalDateTime ldt = LocalDateTime.now();
        String date = ldt.format(format);
        sql = "INSERT INTO transactions VALUES ('"+accNumber+"', '"+deposit+"', 'deposit', '"+date+"', '"+amount+"')";
        stmt.executeUpdate(sql);
        System.out.println();
        
        menu(accNumber);
        
    } 
    
    public static void menu(int accNumber) throws SQLException{
        System.out.println();
        System.out.println("-------MENU-------");
        System.out.println("Option 1: Make a deposit");
        System.out.println("Option 2: Withdraw money");
        System.out.println("Option 3: Check account balance");
        System.out.println("Option 4: Get your account info");
        System.out.println("Option 5: Get account statement");
        System.out.println("Option 6: Change your PIN");
        System.out.println("Option 7: Log out");
        System.out.print("Select option: ");
        Scanner input = new Scanner(System.in);
        int selection = input.nextInt();
        System.out.println();
        
        switch (selection) {
        case 1:
            deposit(accNumber);
            break;
        case 2:
            withdraw(accNumber);
            break;
        case 3:
            System.out.println("Account Balance: $" +checkBalance(accNumber));
            menu(accNumber);
            break;
        case 4:
            getInfo(accNumber);
            break;
        case 5:
            accStatement(accNumber);
            break;
        case 6:
            changePin(accNumber);
            break;
        case 7:
            System.out.println("Have a great day!");
            break;
        default:
            System.out.println("Please select a valid option.");
        }
        
    }
    
    public static void newAccount() throws SQLException{
        Scanner input = new Scanner(System.in);
        int amount = 0;
        
        Random rand = new Random();
        int account = 10000000 + rand.nextInt(99999999);
        
        //validating that account number does not already exist
        Statement stmt = conn.createStatement();
        String sql = "SELECT accNumber FROM accounts";
        ResultSet rs = stmt.executeQuery(sql);
        while(rs.next()){
            if(account == rs.getInt("accNumber")){
                newAccount();
            }
        }
        
        System.out.println("*NEW ACCOUNT SETUP*");
        
        System.out.print("Select account type - checking OR savings: ");
        String type = input.nextLine();
        System.out.print("Account holder name: ");
        String name = input.nextLine();
        System.out.print("Enter your PIN: ");
        int pin = input.nextInt();
        LocalDateTime ldt = LocalDateTime.now();
        String date = ldt.format(format);
        
        //Saving new account info into database
        
        sql = "INSERT INTO accounts VALUES ('"+account+"', '"+pin+"', '"+name+"', '"+amount+"', '"+date+"', '"+type+"')";
        stmt.executeUpdate(sql);
        System.out.println("You have successfully created a new account.");
        
        menu(account);
      
    }
    
    
    public static void login() throws SQLException{
        Scanner input = new Scanner(System.in);
        int pin = 0, enteredPin;
        int accNumber;
         
        System.out.print("Please enter Account Number: ");
        accNumber = input.nextInt();
        System.out.print("PIN: ");
        enteredPin = input.nextInt();
         
        //retrieving info from the database
        try{
            Statement stmt = conn.createStatement();
            String sql = "SELECT pin FROM accounts WHERE accNumber = '"+accNumber+"'";
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            pin = rs.getInt("pin");
        }
        catch(SQLException e){
            System.out.println("Account does not exist.");
            optionsMenu();
        }
        
        //checking if the pin matches
        if (enteredPin == pin){
            menu(accNumber);
        }
        else{
            System.out.println("Wrong PIN.");
            optionsMenu();
        }
         
    }
     
        
    public static void optionsMenu() throws SQLException{
        int option;
        System.out.println();
        System.out.println("What would you like to do today?");
        System.out.println("Option 1: Create new account");
        System.out.println("Option 2: Log into your account");
        System.out.print("Select option: ");
        Scanner input = new Scanner(System.in);
        option = input.nextInt();
        if(option == 1){
            newAccount();
        }
        else if(option == 2){
            login();
        }
        else{
            System.out.println("Please select valid option.");
            optionsMenu();
        }
    
    }
    
    
        
    public static void main(String[] args) throws SQLException {
        
        //Welcome message
        try{
            conn = DriverManager.getConnection(CONN_STRING, USERNAME, PASSWORD);
            System.out.println("Hello! Welcome to ABC Bank Ltd. mobile banking.");
            
        }
        catch(SQLException e){
            System.out.println(e);
        }
        optionsMenu();
        
         
    }

}
