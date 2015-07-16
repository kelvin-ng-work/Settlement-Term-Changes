package security.settlement;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import security.settlement.LocalDBView.SecurityTableModel;

import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;


public class LoginPanel extends JPanel {
	private Font labelFont = new Font("Tahoma", Font.BOLD, 16);
	private Font fieldFont = new Font("Tahoma", Font.PLAIN, 16);
	private JTabbedPane tabbedPanel = new JTabbedPane();
	private JPanel firstTab, secondTab;
	private JPanel statusPanel = new JPanel();
	private JLabel statusLabel = new JLabel("", JLabel.CENTER);
	private JLabel symbol = new JLabel("Symbol", JLabel.LEFT);
	private JLabel trade_utc_time = new JLabel("Trade_UTC_Time", JLabel.LEFT);
	private JLabel broker = new JLabel("Broker", JLabel.LEFT);
	private JLabel trader_Id = new JLabel("Trader Id", JLabel.LEFT);
	private JLabel trade_qty = new JLabel("Trade Quantity", JLabel.LEFT);
	private JLabel trade_price = new JLabel("Trade Price", JLabel.LEFT);
	private JTextField symbol_field = new JTextField();
	private JTextField trade_utc_time_field = new JTextField();
	private JTextField broker_field = new JTextField();
	private JTextField trader_Id_field = new JTextField();
	private JTextField trade_qty_field = new JTextField();
	private JTextField trade_price_field = new JTextField();
	private JButton searchButton = new JButton("Search");
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://host:port/database";
    static final String USER = "user";
    static final String PASS = "password";
	private Connection conn = null;
	private Statement stmt = null;
	private ResultSet rs;
	Object[][] listOfSecurities;
    int totalSecurities = 0;
	
	private JFrame mainFrame;
	private JPanel contentPane;
	private JPanel controlPanel;
	private JTextField userText;
	private JPasswordField passwordText;
	
	// Creates the main display panel and the first tab
	public LoginPanel() {
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
		firstTab = createLoginPanel();
		tabbedPanel.addTab("Login Panel", firstTab);
		tabbedPanel.setSelectedIndex(0);
		secondTab = createDisplayPanel();
		tabbedPanel.addTab("Special Settlement Securities", secondTab);
		int mainPanelWidth = 400;
		int mainPanelHeight = 400;
		tabbedPanel.setPreferredSize(new Dimension(mainPanelWidth, mainPanelHeight));
		setLayout(new GridLayout(1, 1));
		add(tabbedPanel);
	}
	
	// Creates the search panel
	protected JPanel createLoginPanel() {
		JPanel contentPane = new JPanel();
		contentPane.setOpaque(true);
		contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		contentPane.setLayout(new BorderLayout());
		controlPanel = new JPanel();
		controlPanel.setOpaque(true);
		controlPanel.setBorder(BorderFactory.createEmptyBorder(70, 30, 30, 30));
		int controlPanelWidth = 400;
		int controlPanelHeight = 200;
		controlPanel.setPreferredSize(new Dimension(controlPanelWidth, controlPanelHeight));
		JPanel credentialPanel = new JPanel();
		GridLayout credentialLayout = new GridLayout(0,2);
		credentialLayout.setVgap(10);
		credentialLayout.setHgap(10);
		credentialPanel.setLayout(credentialLayout);
		//credentialPanel.setBorder(BorderFactory.createTitledBorder("Login Panel"));
		JLabel nameLabel= new JLabel("User Name", JLabel.LEFT);
		nameLabel.setFont(fieldFont);
		JLabel passwordLabel = new JLabel("Password", JLabel.LEFT);
		passwordLabel.setFont(fieldFont);
		userText = new JTextField(20);
		userText.setFont(fieldFont);
		passwordText = new JPasswordField(20);
		passwordText.setFont(fieldFont);
		JButton loginButton = new JButton("Login");
		loginButton.setFont(fieldFont);
		loginButton.addActionListener(new ActionListener() {
			 public void actionPerformed(ActionEvent e) {     
				processLogin(userText.getText(), new String(passwordText.getPassword()));
			 }
		}); 
		credentialPanel.add(nameLabel);
		credentialPanel.add(userText);
		credentialPanel.add(passwordLabel);       
		credentialPanel.add(passwordText);
		credentialPanel.add(new JPanel());
		credentialPanel.add(loginButton);
		credentialPanel.add(new JPanel());
		int credentialPanelWidth = 400;
		int credentialPanelHeight = 200;
		credentialPanel.setPreferredSize(new Dimension(credentialPanelWidth, credentialPanelHeight));
		controlPanel.add(credentialPanel);
		statusLabel = new JLabel("",JLabel.CENTER);
		contentPane.add(controlPanel, BorderLayout.CENTER);
		contentPane.add(statusLabel, BorderLayout.SOUTH);
		return contentPane;
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
			String sql = "SELECT Username, Password FROM login WHERE Username='" + user + "'";
			// Executes the database query
			ResultSet rs = stmt.executeQuery(sql);
			// Handles returned query result
			if(rs.next()) {
			// Retrieves by column name
				userName  = rs.getString("Username");
				password = rs.getString("Password");
			} else {
				JOptionPane.showMessageDialog(new JFrame(), "User does not exist", "Login Error", JOptionPane.ERROR_MESSAGE);
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
				JOptionPane.showMessageDialog(new JFrame(), "The user name or password is incorrect", "Login Error", JOptionPane.ERROR_MESSAGE);
				return;
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
	
	// Sets up column sizes
    public void initColumnSizes(JTable table) {
    	// Creates table model
        SecurityTableModel model = (SecurityTableModel)table.getModel();
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;
        Object[] longValues = model.longValues;
        // Retrieves table handler
        TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
        // Configures column properties
        for (int i = 0; i < 4; i++) {
            column = table.getColumnModel().getColumn(i);
            comp = headerRenderer.getTableCellRendererComponent(
                                 null, column.getHeaderValue(),
                                 false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;
            comp = table.getDefaultRenderer(model.getColumnClass(i)).
                             getTableCellRendererComponent(
                                 table, longValues[i],
                                 false, false, 0, i);
            cellWidth = comp.getPreferredSize().width;
            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }
	
	// Table model class
    public class SecurityTableModel extends AbstractTableModel {
	   int rowPos = 0;
	   int colPos = 0;
       public SecurityTableModel() {
    	   try{
 	          // Registers the JDBC driver
 	          Class.forName("com.mysql.jdbc.Driver");
 	          // Opens connection
 	          conn = DriverManager.getConnection(DB_URL,USER,PASS);
 	          // Creates SQL statement
 	          stmt = conn.createStatement();
 	          // SQL select statement to retrieve target securities
 	          String sql = "SELECT SECURITY_ID, SYMBOL, SECURITY_DESC, SETTLEMENT_TERM FROM STAGE_SECURITY WHERE PERMANENTLY_DELISTED='N' AND MARKED='Y'";
 	          // Executes the database query
 			  rs = stmt.executeQuery(sql);
 			  // Handles returned query result
 			  if(!rs.first()) {
 				  listOfSecurities = new Object[1][5];
 				  listOfSecurities[0][0] = new Integer(0);
 				  listOfSecurities[0][1] = "N/A";
 				  listOfSecurities[0][2] = "N/A";
 				  listOfSecurities[0][3] = new Integer(0);
 			  } else {
 				  // Extracts data from result set
 				  rs.last();
				  totalSecurities = rs.getRow();
				  listOfSecurities = new Object[totalSecurities][4];
				  rs.first();
	 	          do {
	 	             // Retrieves column data
	 	        	 Integer security_id  = new Integer(rs.getInt("security_id"));
	 	             String symbol = rs.getString("symbol");
	 	             String security_desc = rs.getString("security_desc");
	 	             Integer settlement_term = new Integer(rs.getInt("settlement_term"));
	 	             // Adds the security record to the 2D storing array
	 	             listOfSecurities[rowPos][colPos++] = security_id;
	 	             listOfSecurities[rowPos][colPos++] = symbol;
	 	             listOfSecurities[rowPos][colPos++] = security_desc;
	 	             listOfSecurities[rowPos][colPos++] = settlement_term;
	 	             // Iterates to next row
	 	             rowPos += 1;
	 	             colPos = 0;
	 	           } while(rs.next() && rowPos <= totalSecurities);
 			  }
 	       }catch(SQLException se){
 	          // Handles JDBC errors
 	          se.printStackTrace();
 	       }catch(Exception e){
 	          // Handle other errors
 	          e.printStackTrace();
 	       }
       }
       	
        private String[] columnNames = {"Security_ID",
                                        "Symbol",
                                        "Security_Desc",
                                        "Settlement_Term"
                                        };
        public final Object[] longValues = { new Integer(20), "SECURITY_SYMBOL",
                                            "SECURITY_DESC", new Integer(20) };

        // Gets the number of columns
        public int getColumnCount() {
            return columnNames.length;
        }

        // Gets the number of rows
        public int getRowCount() {
        	return totalSecurities;
        }

    	// Gets the column name
        public String getColumnName(int col) {
            return columnNames[col];
        }

    	// Gets a specific security record's data value
        public Object getValueAt(int row, int col) {
    		return listOfSecurities[row][col];
        }

        // Gets the class of a particular column
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
    }
	
    // Creates the display tab
	protected JPanel createDisplayPanel() {
	    JPanel displayPanel = new JPanel();
	    displayPanel.setLayout(new GridLayout(0, 1));
	    int displayPanelWidth = 300;
		int displayPanelHeight = 300;
		displayPanel.setPreferredSize(new Dimension(displayPanelWidth, displayPanelHeight));
		LocalDBView dbViewModel = new LocalDBView();
        // Creates the security records table
	    JTable table = new JTable(new SecurityTableModel());
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        // Creates the scroll pane
        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        // Sets up column sizes
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(500);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);
        Font font = new Font("Tahoma", Font.PLAIN, 16);
        table.setFont(font);
        table.getTableHeader().setFont(font);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        // Adds the scroll pane to this panel.
        displayPanel.add(scrollPane);
        return displayPanel;
	}
	
	// Main program
	public static void main(String[] args) {
		JFrame frame = new JFrame("Trade and Settlement Change");
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		int framePaneWidth = 1033;
		int framePaneHeight = 450;
		frame.setPreferredSize(new Dimension(framePaneWidth, framePaneHeight));
		frame.getContentPane().add(new LoginPanel(), BorderLayout.CENTER);
		frame.pack();
		int halfWidth = frame.getWidth()/2;
	    int halfHeight = frame.getHeight()/2;
	    int x = (Toolkit.getDefaultToolkit().getScreenSize().width/2)-halfWidth;
	    int y = (Toolkit.getDefaultToolkit().getScreenSize().height/2)-halfHeight;
	    frame.setLocation(x, y);
		frame.setVisible(true);
	}
}
