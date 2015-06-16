import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.sql.*;
import java.util.*;

// Database operations
public class LocalDBControl extends JPanel {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://host/database";
	// Database user credential
    static final String USER = "user";
    static final String PASS = "password";
    Connection conn = null;
    Statement stmt = null;
    ResultSet rs;
    private boolean DEBUG = false;
    private int securityId = 0;
    private String securitySymbol = "";    
    Object[][] listOfSecurities;
    int totalSecurities = 0;
    
    public LocalDBControl() {
        this(0, "");
    }

    public LocalDBControl(int Id) {
        this(Id, "");
    }
    
    public LocalDBControl(String symbol) {
    	this(0, symbol);
    }
    
    public LocalDBControl(int Id, String symbol) {
		super(new GridLayout(1,0));
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
        securityId = Id;
        securitySymbol = symbol;
        // Creates the security records table
        JTable table = new JTable(new MyTableModel(securityId, securitySymbol));
        table.setAutoCreateRowSorter(true);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        // Creates the scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        // Sets up column sizes
        initColumnSizes(table);
        // Sets up settlement term column's cell editors
        //setUpSettlementTermColumn(table, table.getColumnModel().getColumn(4));
        // Adds the scroll pane to this panel.
        add(scrollPane);
    }

    // Sets up column sizes
    private void initColumnSizes(JTable table) {
    	// Creates table model
        MyTableModel model = (MyTableModel)table.getModel();
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;
        Object[] longValues = model.longValues;
        // Retrieves table handler
        TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
        // Configures column properties
        for (int i = 0; i < 5; i++) {
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
            if (DEBUG) {
                System.out.println("Initializing width of column "
                                   + i + ". "
                                   + "headerWidth = " + headerWidth
                                   + "; cellWidth = " + cellWidth);
            }
            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }

    // Configures drop-down box for Settlement Term column
    public void setUpSettlementTermColumn(JTable table, TableColumn settlementTermColumn) {
        JComboBox<Integer> comboBox = new JComboBox<Integer>();
        comboBox.addItem(new Integer(0));
        comboBox.addItem(new Integer(1));
        comboBox.addItem(new Integer(2));
        comboBox.addItem(new Integer(3));
        settlementTermColumn.setCellEditor(new DefaultCellEditor(comboBox));
        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer();
        renderer.setToolTipText("Click for drop-down options");
        settlementTermColumn.setCellRenderer(renderer);
    }

    // Table model class
    class MyTableModel extends AbstractTableModel {
	   int securityId;
	   String securitySymbol;
	   int rowPos = 0;
	   int colPos = 0;
       public MyTableModel(int securityId, String securitySymbol) {
    	   securityId = securityId;
    	   securitySymbol = securitySymbol;
    	   try{
    		  // 2D string array that holds security records
 			  //listOfSecurities = new Object[100][100];
 	          // Registers the JDBC driver
 	          Class.forName("com.mysql.jdbc.Driver");
 	          // Opens connection
 	          conn = DriverManager.getConnection(DB_URL,USER,PASS);
 	          // Creates SQL statement
 	          stmt = conn.createStatement();
 	          // SQL select statement to retrieve target securities
 	          String sql = "SELECT SECURITY_ID, SYMBOL, SECURITY_DESC, SETTLEMENT_TERM FROM SECURITY WHERE PERMANENTLY_DELISTED='N'";
 	          // Adds selection criteria
 	          if(securityId != 0 && securitySymbol != "") {
 	        	  String searchCriteria = " AND SECURITY_ID=" + securityId + " AND SYMBOL LIKE '" + securitySymbol + "'";
 	        	  sql += searchCriteria;
 	          } else if (securityId != 0) {
 	        	  String securityIdSearchCriterion = " AND SECURITY_ID=" + securityId;
 	        	  sql += securityIdSearchCriterion;
 	          } else if (securitySymbol != "") {
 	        	  String securitySymbolSearchCriterion = " AND (SYMBOL LIKE '" + securitySymbol + "' OR SYMBOL='" + securitySymbol +"')";
	        	  sql += securitySymbolSearchCriterion;
 	          }
 	          // Executes the database query
 			  rs = stmt.executeQuery(sql);
 			  // Handles returned query result
 			  if(!rs.first()) {
 				  listOfSecurities = new Object[2][5];
 				  //System.out.println("No record found.");
 				  listOfSecurities[0][0] = Boolean.FALSE;
 				  listOfSecurities[0][1] = new Integer(0);
 				  listOfSecurities[0][2] = "N/A";
 				  listOfSecurities[0][3] = "N/A";
 				  listOfSecurities[0][4] = new Integer(0);
 			  } else {
 				  // Extracts data from result set
 				  rs.last();
 				  totalSecurities = rs.getRow();
 				  listOfSecurities = new Object[totalSecurities][5];
 				  rs.first();
 	 	          do {
 	 	             // Retrieves column data
 	 	        	 Integer security_id  = new Integer(rs.getInt("security_id"));
 	 	             String symbol = rs.getString("symbol");
 	 	             String security_desc = rs.getString("security_desc");
 	 	             Integer settlement_term = new Integer(rs.getInt("settlement_term"));
 	 	             // Adds the security record to the 2D storing array
 	 	             listOfSecurities[rowPos][colPos++] = Boolean.FALSE;
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
       
       public MyTableModel(int securityId) {
    	   this(securityId, "");
       }
       
       public MyTableModel(String securitySymbol) {
    	   this(0, securitySymbol);
       }
       
       public MyTableModel() {
    	   this(0, "");
       }
       	
        private String[] columnNames = {"Update",
        								"Security_ID",
                                        "Symbol",
                                        "Security_Desc",
                                        "Settlement_Term"
                                        };
        public final Object[] longValues = {Boolean.FALSE, new Integer(20), "SECURITY_SYMBOL",
                                            "SECURITY_DESC", new Integer(20)};

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
        
        // Sets the value of a data field
        public void setValueAt(Object value, int row, int col) {
        	listOfSecurities[row][col] = value;
        	fireTableDataChanged();
        }
        
        // Makes table cells editable
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return true;
        }
    }

    // Creates and displays the UI
    private static void createAndShowGUI() {
        // Creates and configures the UI Frame and the content pane
        JFrame frame = new JFrame("Security Table");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        LocalDBControl newContentPane = new LocalDBControl();
        int contentPaneWidth = 700;
  	    int contentPaneHeight = 700;
        newContentPane.setPreferredSize(new Dimension(contentPaneWidth, contentPaneHeight));
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationByPlatform(true);
        frame.pack();
        frame.setVisible(true);
    }
    
    // Changes the settlement date of selected securities using user input security Id or symbol
    public void changeSettlementDate(int numOfDays, int securityId, String symbol) {
    	   // Sets up and executes SQL UPDATE statement
     	   try{
  	          String sql = "UPDATE SECURITY SET SETTLEMENT_TERM=" + numOfDays + ", MARKED='Y'";
  	          String searchTerm;
  	          if(securityId != 0) {
  	        	  searchTerm =  " WHERE SECURITY_ID=" + securityId;
  	        	  sql += searchTerm;
  	          } else if (!symbol.equals("")) {
  	        	  searchTerm = " WHERE SYMBOL LIKE '" + symbol + "'";
  	        	  sql += searchTerm;
  	          }
  			  stmt.executeUpdate(sql);
  	       }catch(SQLException se){
  	          // Handles JDBC errors
  	          se.printStackTrace();
  	       }catch(Exception e){
  	          // Handle other errors
  	          e.printStackTrace();
  	       }
    }
    
    // Changes the settlement date of selected securities for selected securities in the table
    public void updateSettlementDate(int numOfDays) {
    	   // Sets up and executes SQL UPDATE statement
     	   try{
     		   String sql = "UPDATE SECURITY SET SETTLEMENT_TERM=" + numOfDays + ", MARKED='Y'";
  	           String searchTerm = "";
  	           int updateCheck = 0;
     		   for (int i = 0; i < listOfSecurities.length; i++) {
     			  if (listOfSecurities[i][0] == Boolean.TRUE) {
     				  if(updateCheck == 0) {
     					 searchTerm +=  " WHERE SECURITY_ID=" + ((Integer)listOfSecurities[i][1]).intValue();
     					 updateCheck = 1;
     				  } else {
     					 searchTerm +=  " OR SECURITY_ID=" + ((Integer)listOfSecurities[i][1]).intValue();
     				  }
     			  }
     		   }
     		   sql += searchTerm;
  			   stmt.executeUpdate(sql);
  	       }catch(SQLException se){
  	          // Handles JDBC errors
  	          se.printStackTrace();
  	       }catch(Exception e){
  	          // Handle other errors
  	          e.printStackTrace();
  	       }
    }
    
    // Retrieves selected securities
    public String[] getSecurities(int securityId, String symbol) {
    	String[] securitiesArray = new String[100];
		int pos = 0;
		// Sets up and executes SQL statement
    	try {
    		String sql = "SELECT SYMBOL FROM SECURITY WHERE PERMANENTLY_DELISTED='N' AND MARKED='Y'";
    		String searchTerm;
    		if(securityId != 0) {
    			searchTerm = " AND SECURITY_ID=" + securityId;
    			sql += searchTerm;
    		} else if(!symbol.equals("")) {
    			searchTerm = " AND SYMBOL LIKE '" + symbol + "'";
    			sql += searchTerm;
    		}
    		rs = stmt.executeQuery(sql);
    	  // Handles returned query result
		  if(!rs.first()) {
			securitiesArray[0] = "N/A";
		  } else {
			  do {
				 String securitySymbol = rs.getString("symbol");
				 securitiesArray[pos++] = securitySymbol;        
			  } while(rs.next() && pos < 100);
		  }
    	}catch(SQLException se){
          //Handle errors for JDBC
          se.printStackTrace();
        }catch(Exception e){
           //Handle errors for Class.forName
           e.printStackTrace();
        }
    	return securitiesArray;
    }

    // Main function to run this program alone
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
        
        
        
    }
}