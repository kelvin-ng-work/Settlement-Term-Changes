package security.settlement;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.*;
import java.util.*;

// Database operations
public class LocalDBView extends JPanel {
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
    
    public LocalDBView() {
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
        // Creates the security records table
        JTable table = new JTable(new SecurityTableModel());
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
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        // Sets up column's cell editors
        //setUpSettlementTermColumn(table, table.getColumnModel().getColumn(4));
        // Adds the scroll pane to this panel.
        add(scrollPane);
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
 	          String sql = "SELECT SECURITY_ID, SYMBOL, SECURITY_DESC, SETTLEMENT_TERM FROM STAGE_SECURITY";
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

    // Creates and displays the UI
    private static void createAndShowGUI() {
        // Creates and configures the UI Frame and the content pane
        JFrame frame = new JFrame("All Outstanding Securities");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        LocalDBView newContentPane = new LocalDBView();
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

    // Main function to run this program alone
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });     
    }
}