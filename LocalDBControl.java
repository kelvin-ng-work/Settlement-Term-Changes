package security.settlement;

import javax.swing.AbstractButton;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

// Database operations
public class LocalDBControl extends JPanel {
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
   static final String DB_URL = "jdbc:mysql://host:port/database";
   static final String USER = "user";
   static final String PASS = "password";
    Connection conn = null;
    Statement stmt = null;
    ResultSet rs;
    private int securityId = 0;
    private String securitySymbol = "";    
    Object[][] listOfSecurities;
    int totalSecurities = 0;
    JTable table;
    
    public LocalDBControl(boolean updateStatus) {
        this(0, "", updateStatus);
    }

    public LocalDBControl(int Id, boolean updateStatus) {
        this(Id, "", updateStatus);
    }
    
    public LocalDBControl(String symbol, boolean updateStatus) {
    	this(0, symbol, updateStatus);
    }
    
    public LocalDBControl(int Id, String symbol, boolean updateStatus) {
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
        table = new JTable(new SecurityTableModel(securityId, securitySymbol, updateStatus));
        table.setAutoCreateRowSorter(true);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        // Creates the scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        // Sets up column sizes
        initColumnSizes(table);
        Font font = new Font("Tahoma", Font.PLAIN, 16);
        table.setFont(font);
        table.getTableHeader().setFont(font);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        // Sets up settlement term column's cell editors
        //setUpSettlementTermColumn(table, table.getColumnModel().getColumn(4));
        // Adds the scroll pane to this panel.
        add(scrollPane);
    }

    // Sets up column sizes
    private void initColumnSizes(JTable table) {
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
            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }
    
    // Configures drop-down box for Settlement Term column
    /*
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
	*/
    
    // Table model class
    class SecurityTableModel extends DefaultTableModel {
	   int securityId;
	   String securitySymbol;
	   int rowPos = 0;
	   int colPos = 0;
       public SecurityTableModel(int securityId, String securitySymbol, boolean updateStatus) {
    	   securityId = securityId;
    	   securitySymbol = securitySymbol;
    	   try{
 	          // Registers the JDBC driver
 	          Class.forName("com.mysql.jdbc.Driver");
 	          // Opens connection
 	          conn = DriverManager.getConnection(DB_URL,USER,PASS);
 	          // Creates SQL statement
 	          stmt = conn.createStatement();
 	          // SQL select statement to retrieve target securities
 	          String sql = "SELECT SECURITY_ID, SYMBOL, SECURITY_DESC, SETTLEMENT_TERM FROM STAGE_SECURITY WHERE PERMANENTLY_DELISTED='N'";
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
 				  listOfSecurities = new Object[1][5];
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
 	 	             listOfSecurities[rowPos][colPos++] = updateStatus;
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
       
       public SecurityTableModel(int securityId, boolean updateStatus) {
    	   this(securityId, "", updateStatus);
       }
       
       public SecurityTableModel(String securitySymbol, boolean updateStatus) {
    	   this(0, securitySymbol, updateStatus);
       }
       
       public SecurityTableModel(boolean updateStatus) {
    	   this(0, "", updateStatus);
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
        LocalDBControl newContentPane = new LocalDBControl("%AA", false);
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
    
    // Changes the settlement date of selected securities for selected securities in the table
    public boolean updateSettlementDate(int numOfDays, long tradeDate) {
	   // Sets up and executes SQL UPDATE statement
 	   try{
 		   SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		   String dateOfTrade = dateFormat.format(new Date(tradeDate));
 		   String sql = "UPDATE STAGE_SECURITY SET SETTLEMENT_TERM=" + numOfDays + ", TRADE_DATE='" + dateOfTrade + "', MARKED='Y'";
           String searchTerm = "";
           int updateCheck = 0;
 		   for (int i = 0; i < listOfSecurities.length; i++) {
 			  if (listOfSecurities[i][0] == Boolean.TRUE) {
 				  if(updateCheck == 0) {
 					 searchTerm += " WHERE SECURITY_ID=" + ((Integer)listOfSecurities[i][1]).intValue();
 					 updateCheck = 1;
 				  } else {
 					 searchTerm += " OR SECURITY_ID=" + ((Integer)listOfSecurities[i][1]).intValue();
 				  }
 			  }
 		   }
 		   if (updateCheck == 0) {
				return false;
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
 	   return true;
    }
    
    // Retrieves selected securities
    public String[] getSecurities() {
    	String[] securitiesArray = new String[100];
    	int pos = 0;
		// Sets up and executes SQL statement
    	try {
    		String sql = "SELECT SYMBOL FROM STAGE_SECURITY WHERE PERMANENTLY_DELISTED='N' AND MARKED='Y'";
    		String searchTerm = "";
            int updateCheck = 0;
            for (int i = 0; i < listOfSecurities.length; i++) {
            	if (listOfSecurities[i][0] == Boolean.TRUE) {
            		if(updateCheck == 0) {
            			searchTerm += " AND (SECURITY_ID=" + ((Integer)listOfSecurities[i][1]).intValue();
            			updateCheck = 1;
            		} else {
            			searchTerm += " OR SECURITY_ID=" + ((Integer)listOfSecurities[i][1]).intValue();
            		}
            	}
            }
            if(updateCheck == 1) {
            	searchTerm += ")";
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