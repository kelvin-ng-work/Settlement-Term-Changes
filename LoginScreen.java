package security.settlement;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

// Creates the Login UI and operations
public class LoginScreen {
	   private JFrame mainFrame;
	   private JPanel contentPane;
	   private JPanel controlPanel;
	   private JLabel statusLabel;
	   private JTextField userText;
	   private JPasswordField passwordText;
	   // JDBC driver name and database URL
	   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	   static final String DB_URL = "jdbc:mysql://localhost/SECURITY";
	   // Database user credential
	   static final String USER = "root";
	   static final String PASS = "root";
	   private Connection conn = null;
	   private Statement stmt = null;

	   // Main function to run this program alone
	   public static void main(String[] args){
		  LoginScreen loginControl = new LoginScreen();
		  loginControl.showLogin();
	   }
	   
	   // Initialization function
	   public LoginScreen(){
	      prepareGUI();
	   }

	   // Sets up Login window UI frame
	   private void prepareGUI(){
		   // Sets the UI theme
		   try {
		        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		    } catch (ClassNotFoundException e) {
		        e.printStackTrace();
		    } catch (InstantiationException e) {
		        e.printStackTrace();
		    } catch (IllegalAccessException e) {
		        e.printStackTrace();
		    } catch (UnsupportedLookAndFeelException e) {
		        e.printStackTrace();
		    }
		    mainFrame = new JFrame("Login");
		    mainFrame.addWindowListener(new WindowAdapter() {
				 public void windowClosing(WindowEvent windowEvent){
				    System.exit(0);
				 }
	        });
		    //Sets up the control panel
	        JPanel contentPane = new JPanel();
			contentPane.setOpaque(true);
			contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			contentPane.setLayout(new BorderLayout(5, 5));		  
	        controlPanel = new JPanel(); 
	        controlPanel.setOpaque(true);
	        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	        int controlPanelWidth = 500;
	        int controlPanelHeight = 250;
	        controlPanel.setPreferredSize(new Dimension(controlPanelWidth, controlPanelHeight));
	        statusLabel = new JLabel("",JLabel.CENTER);
	        contentPane.add(controlPanel, BorderLayout.PAGE_START);
	        contentPane.add(statusLabel, BorderLayout.CENTER);
	        int frameWidth = 600;
	        int frameHeight = 350;
	        mainFrame.setPreferredSize(new Dimension(frameWidth, frameHeight));
	        mainFrame.setContentPane(contentPane);
	        mainFrame.setLocationRelativeTo(null);
	        //mainFrame.setLocationByPlatform(true);
	   }

	   // Sets up Login window UI components
	   private void showLogin(){
		  JPanel credentialPanel = new JPanel();
	      GridLayout credentialLayout = new GridLayout(4,2);
	      credentialLayout.setVgap(10);
	      credentialLayout.setHgap(10);
	      credentialPanel.setLayout(credentialLayout);
	      credentialPanel.setBorder(BorderFactory.createTitledBorder("Login Panel"));
	      JLabel namelabel= new JLabel("Username: ", JLabel.CENTER);
	      JLabel passwordLabel = new JLabel("Password: ", JLabel.CENTER);
	      userText = new JTextField(20);
	      passwordText = new JPasswordField(20);      
	      JButton loginButton = new JButton("Login");
	      loginButton.addActionListener(new ActionListener() {
	         public void actionPerformed(ActionEvent e) {     
	            processLogin(userText.getText(), new String(passwordText.getPassword()));
	         }
	      }); 
	      JButton viewSecuritiesButton = new JButton("View Records");
	      viewSecuritiesButton.addActionListener(new ActionListener() {
	    	  public void actionPerformed(ActionEvent e) {     
	    		    JFrame frame = new JFrame("Settlement Date Table");
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					LocalDBControl newContentPane = new LocalDBControl();
					newContentPane.setOpaque(true);
					frame.setContentPane(newContentPane);
					frame.setPreferredSize(new Dimension(700,700));
					//Display the window.
					frame.pack();
					frame.setVisible(true);
		         }
	      });
	      credentialPanel.add(namelabel);
	      credentialPanel.add(userText);
	      credentialPanel.add(passwordLabel);       
	      credentialPanel.add(passwordText);
	      credentialPanel.add(new JPanel());
	      credentialPanel.add(loginButton);
	      credentialPanel.add(new JPanel());
	      credentialPanel.add(viewSecuritiesButton);
	      int credentialPanelWidth = 400;
		  int credentialPanelHeight = 200;
		  credentialPanel.setPreferredSize(new Dimension(credentialPanelWidth, credentialPanelHeight));
	      controlPanel.add(credentialPanel);
	      mainFrame.pack();
	      mainFrame.setVisible(true);
	   }
	   
	   // Sets up Login operations
	   private void processLogin(String user, String passphrase) {
		   String userName;
		   String password;
		   try{
			  // Registers the JDBC driver
		      Class.forName("com.mysql.jdbc.Driver");
		      // Opens connection
		      conn = DriverManager.getConnection(DB_URL,USER,PASS);
		      // Creates SQL statement
		      stmt = conn.createStatement();
		      // SQL select statement to retrieve login user credential
		      String sql = "SELECT Username, Password FROM login_tbl WHERE Username='" + user + "'";
		      // Executes the database query
		      ResultSet rs = stmt.executeQuery(sql);
		      // Handles returned query result
			  if(rs.next()) {
					  // Retrieves by column name
					  userName  = rs.getString("Username");
					  password = rs.getString("Password");
				} else {
					System.out.println("User does not exist.");
					return;
				}
		      // Cleans up JDBC connection
		      rs.close();
		      stmt.close();
		      conn.close();
		      // Checks login credential and either displays the search and update window or shows an error message
		      if(userName.equals(user) && password.equals(passphrase)) {
		    	  SearchandUpdatePage newPage = new SearchandUpdatePage();
		      } else {
		    	  String data = "User Credential is incorrect. Please re-enter username and passowrd."; 
	              statusLabel.setText(data);
		      }
		   }catch(SQLException se){
		      // Handles JDBC errors
		      se.printStackTrace();
		   }catch(Exception e){
		      // Handles other errors
		      e.printStackTrace();
		   }finally{
		      // Cleans up database recourses
			  try {
	             stmt.close();
	             conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }
		   }
	   }
}