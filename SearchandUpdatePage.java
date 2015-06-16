import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;

// Searches and updates securities
public class SearchandUpdatePage {
   private JFrame mainFrame;
   private JFrame frame;
   private JPanel contentPane;
   private JPanel controlPanel;
   private LocalDBControl newContentPane;
   private JRadioButton firstOption;
   private JRadioButton secondOption;
   private JButton searchButton;
   private JButton updateButton;
   private JComboBox<Integer> settlementTermSelection;
   private JCheckBox updateStatus;
   private JTextField securityID;
   private JTextField symbol;
   private JSpinner timeSpinner;
   private JSpinner refTimeSpinner;
   private JTextField dateOfChange;
   private Font font;
   private String[] securitySymbolsArray;
   
   // Configures search by security ID option
   class securityIdSearchActionListener implements ActionListener {
      public void actionPerformed(ActionEvent e) {
			if(firstOption.isSelected()) {
				securityID.setEnabled(true);
				symbol.setEnabled(false);
			}
      }
   }
   
   // Configures search by wildcard security symbol option
   class securitySymbolSearchActionListener implements ActionListener {
      public void actionPerformed(ActionEvent e) {
          if(secondOption.isSelected()) {
				securityID.setEnabled(false);
				symbol.setEnabled(true);
			}
      }
   }
   
   // Searches and displays securities table
   class searchActionListener implements ActionListener {
	   public void actionPerformed(ActionEvent e) {
				frame = new JFrame("Security Table");
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				if(!(securityID.getText().length() == 0)) {
					newContentPane = new LocalDBControl(Integer.parseInt(securityID.getText()));
				} else if (!(symbol.getText().length() == 0)) {
					newContentPane = new LocalDBControl(symbol.getText());
				} else {
					//JOptionPane.showMessageDialog(new JFrame(), "Input either a security Id or symbol.", "Input Error", JOptionPane.ERROR_MESSAGE);
					newContentPane = new LocalDBControl();
				}
				newContentPane.setOpaque(true);
				frame.setContentPane(newContentPane);
				frame.setPreferredSize(new Dimension(900,900));
				frame.pack();
				frame.setVisible(true);
	   }
   }
   
   // Updates settlement dates of securities
   class updateActionListener implements ActionListener {
	   public void actionPerformed(ActionEvent e) {
		    long days = (((Date)timeSpinner.getValue()).getTime() - ((Date)refTimeSpinner.getValue()).getTime()) / (24 * 60 * 60 * 1000);
		    int daysUntilSettlement = (int)days + 1;
		    securitySymbolsArray = new String[100];
		    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		    Date date = new Date();
		    dateOfChange.setText(dateFormat.format(date));
		    if(!updateStatus.isSelected()) {
			    updateStatus.setSelected(true);
		    }
			if(!(securityID.getText().length() == 0)) {
				newContentPane.updateSettlementDate(daysUntilSettlement);
				newContentPane = new LocalDBControl(Integer.parseInt(securityID.getText()));
				securitySymbolsArray = newContentPane.getSecurities(Integer.parseInt(securityID.getText()), "");
			} else if (!(symbol.getText().length() == 0)) {
				newContentPane.updateSettlementDate(daysUntilSettlement);
				newContentPane = new LocalDBControl(symbol.getText());
				securitySymbolsArray = newContentPane.getSecurities(0, symbol.getText());
			} else {
				newContentPane = new LocalDBControl();
				securitySymbolsArray = newContentPane.getSecurities(0, "");
			}
			newContentPane.setOpaque(true);
			frame.setContentPane(newContentPane);
			frame.pack();
			frame.setVisible(true);
		    SendEmailPage emailWindow = new SendEmailPage(daysUntilSettlement, securitySymbolsArray);
		    emailWindow.setVisible(true);
	   }
   }

   public SearchandUpdatePage(){
	   prepareGUI();
   }

   public static void main(String[] args){
		SearchandUpdatePage SearchandUpdatePage = new SearchandUpdatePage();
   }

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
	    font = new Font("Palatino", Font.PLAIN, 16);
	    int frameWidth = 700;
	    int frameHeight = 700;
        mainFrame = new JFrame("Update Settlement Terms");
	    mainFrame.setPreferredSize(new Dimension(frameWidth, frameHeight));
        mainFrame.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent windowEvent){
			    System.exit(0);
		    }
        });
      
	  contentPane = new JPanel();
	  contentPane.setOpaque(true);
	  contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	  int contentPaneWidth = 600;
	  int contentPaneHeight = 600;
	  contentPane.setPreferredSize(new Dimension(contentPaneWidth, contentPaneHeight));
	  controlPanel = new JPanel();
	  controlPanel.setOpaque(true);
	  controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	  GridLayout contorlLayout = new GridLayout(8,2);
	  contorlLayout.setVgap(10);
	  contorlLayout.setHgap(10);
	  controlPanel.setLayout(contorlLayout);
	  int controlPanelWidth = 600;
	  int controlPanelHeight = 600;
	  controlPanel.setPreferredSize(new Dimension(controlPanelWidth, controlPanelHeight));
	  controlPanel.setBorder(BorderFactory.createTitledBorder("Settlement Term Changes"));
	  controlPanel.setFont(font);
	  contentPane.add(controlPanel);
	  // Search Options
	  JPanel buttonsPanel = new JPanel();
	  buttonsPanel.setLayout(new GridLayout(1, 2));
	  firstOption = new JRadioButton("ID");
	  firstOption.setFont(font);
	  secondOption = new JRadioButton("Symbol");
	  secondOption.setFont(font);
	  ButtonGroup searchOptionGroup = new ButtonGroup();
	  searchOptionGroup.add(firstOption);
	  searchOptionGroup.add(secondOption);
	  firstOption.setSelected(true);
	  buttonsPanel.add(firstOption);
	  buttonsPanel.add(secondOption);
	  buttonsPanel.setBorder(BorderFactory.createTitledBorder("Search Options"));
	  controlPanel.add(new JPanel());
	  controlPanel.add(buttonsPanel);
	  // Search Fields
	  JLabel securityIdLabel = new JLabel("Security ID", JLabel.LEFT);
	  securityIdLabel.setFont(font);
	  securityID = new JTextField();
	  securityID.setFont(font);
	  JLabel symbolLabel = new JLabel("Security Symbol", JLabel.LEFT);
	  symbolLabel.setFont(font);
	  symbol = new JTextField();
	  symbol.setFont(font);
	  searchButton = new JButton("Search");
	  searchButton.setFont(font);
	  securityID.setEnabled(true);
	  symbol.setEnabled(false);
	  controlPanel.add(securityIdLabel);
	  controlPanel.add(securityID);
	  controlPanel.add(symbolLabel);
	  controlPanel.add(symbol);
	  controlPanel.add(new JPanel());
	  controlPanel.add(searchButton);
	  // Data Fields
	  JLabel newSettlementTermLabel = new JLabel("New Settlement Date", JLabel.LEFT);
      newSettlementTermLabel.setFont(font);
      /*
      settlementTermSelection = new JComboBox<Integer>();
      settlementTermSelection.addItem(1);
      settlementTermSelection.addItem(2);
      settlementTermSelection.addItem(3);
      settlementTermSelection.addItem(4);
      settlementTermSelection.addItem(5);
      settlementTermSelection.addItem(6);
      settlementTermSelection.addItem(7);
      settlementTermSelection.setFont(font);
      */
      timeSpinner = new JSpinner(new SpinnerDateModel());
      refTimeSpinner = new JSpinner(new SpinnerDateModel());
      JSpinner.DateEditor refTimeEditor = new JSpinner.DateEditor(refTimeSpinner, "MM/dd/yyyy");
      refTimeSpinner.setValue(new Date());
      JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "MM/dd/yyyy");
      timeSpinner.setEditor(timeEditor);
      timeSpinner.setValue(new Date());
      timeSpinner.setFont(font);
      updateButton = new JButton("Update");
      updateButton.setFont(font);
      controlPanel.add(newSettlementTermLabel);
      controlPanel.add(timeSpinner);
      controlPanel.add(new JPanel());
      controlPanel.add(updateButton);
      // Status Confirmation     
      JLabel dateOfChangeLabel = new JLabel("Date of Change", JLabel.LEFT);
      dateOfChangeLabel.setFont(font);
      dateOfChange = new JTextField();
      dateOfChange.setEditable(false);
      updateStatus = new JCheckBox("Completed");
      updateStatus.setFont(font);
      updateStatus.setEnabled(false);
      controlPanel.add(dateOfChangeLabel);
      controlPanel.add(dateOfChange);
      controlPanel.add(new JPanel());
      controlPanel.add(updateStatus);
      mainFrame.add(contentPane);
      mainFrame.pack();
      Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
      int x = (int) ((dimension.getWidth() - mainFrame.getWidth()) / 2);
      int y = (int) ((dimension.getHeight() - mainFrame.getHeight()) / 2);
      mainFrame.setLocation(x, y);
      //mainFrame.setLocationRelativeTo(null);
      mainFrame.setVisible(true);
      firstOption.addActionListener(new securityIdSearchActionListener());
      secondOption.addActionListener(new securitySymbolSearchActionListener());
      searchButton.addActionListener(new searchActionListener());
      updateButton.addActionListener(new updateActionListener());
   }
}