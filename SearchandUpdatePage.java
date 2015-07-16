package security.settlement;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
	private JCheckBox updateAll;
	private JCheckBox emailOption;
	private JTextField securityID;
	private JTextField symbol;
	private JTextField holidays;
	private JSpinner timeSpinner;
	private JSpinner refTimeSpinner;
	private Font labelFont;
	private Font fieldFont;
	private String[] securitySymbolsArray;
	private long tradeDateLong;
	private long settlementDateLong;
	private boolean selectAll;
	private Calendar calendar;
	

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
			if(updateAll.isSelected()) {
				selectAll = true;
			} else {
				selectAll = false;
			}
			if(!(securityID.getText().length() == 0)) {
				newContentPane = new LocalDBControl(Integer.parseInt(securityID.getText()), selectAll);
			} else if (!(symbol.getText().length() == 0)) {
				newContentPane = new LocalDBControl(symbol.getText(), selectAll);
			} else {
				//JOptionPane.showMessageDialog(new JFrame(), "Input either a security Id or symbol.", "Input Error", JOptionPane.ERROR_MESSAGE);
				newContentPane = new LocalDBControl(selectAll);
			}
			newContentPane.setOpaque(true);
			frame.setContentPane(newContentPane);
			frame.setPreferredSize(new Dimension(800, 800));
			frame.pack();
			frame.setVisible(true);
			Calendar calendar = Calendar.getInstance();
			int today = calendar.get(Calendar.DAY_OF_WEEK);
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
			//System.out.println(calendar.getTime());
		}
	}

	// Updates settlement dates of securities
	class updateActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int holidaysCount, daysToSettlement, businessDays;
			// Trade date
			tradeDateLong = ((Date)refTimeSpinner.getValue()).getTime();
			// Settlement date
			settlementDateLong = ((Date)timeSpinner.getValue()).getTime();
			// holidays in between trade date and settlement date
			holidaysCount = Integer.parseInt(holidays.getText());
			// Trade date is today's date
			if(tradeDateLong % (24 * 60 * 60 * 1000) != 14400000)
			{
				long timeAfterDayStart = tradeDateLong % (24 * 60 * 60 * 1000);
				tradeDateLong -= timeAfterDayStart;
			}
			// Total days between trade date and settlement date
			long days = (settlementDateLong - tradeDateLong) / (24 * 60 * 60 * 1000);
			daysToSettlement = (int)days;
			if (daysToSettlement == 0) {
				businessDays = 0;
			} else {
				// Business days between trade date and settlement date
				businessDays = daysToSettlement;
				// Sets calendar to the coming Friday
				calendar = Calendar.getInstance();
				calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
				long Friday = (calendar.getTime()).getTime();
				// Retrieves the Friday in the week of the trade date
				while(Friday < tradeDateLong) {
					Friday += 1000 * 60 * 60 * 24 * 7;
				}
				//System.out.println("Trade date: " + refTimeSpinner.getValue());
				//System.out.println("Friday in the week of trade date: " + new Date(Friday));
				if (daysToSettlement < 7) {	// Settlement date is within one week after trade date
					long extendsOverWeekend = settlementDateLong - Friday;
					if (extendsOverWeekend > 0 && extendsOverWeekend <= 150000000) {	// Settlement date is set to a weekend date
						JOptionPane.showMessageDialog(new JFrame(), "Settlement date is incorrectly set to a weekend date.", "Update Error", JOptionPane.ERROR_MESSAGE);
						return;
					} else if (extendsOverWeekend > 0) {	// Settlement date spans over a weekend
						businessDays = daysToSettlement - 2;
					}
				} else if (daysToSettlement >= 7 && daysToSettlement < 14) { // Settlement date is longer than one week after trade date
					long followingFriday = Friday + (1000 * 60 * 60 * 24 * 7);
					long extendsOverTwoWeekends = settlementDateLong - followingFriday;
					//System.out.println(new Date(followingFriday));
					//System.out.println("Settlement date is after the following week's Friday: " + extendsOverTwoWeekends);
					if (extendsOverTwoWeekends > 0 && extendsOverTwoWeekends <= 150000000) {	// Settlement date is set to a weekend date
						JOptionPane.showMessageDialog(new JFrame(), "Settlement date is incorrectly set to a weekend date.", "Update Error", JOptionPane.ERROR_MESSAGE);
						return;
					} else if (extendsOverTwoWeekends > 0) {	// Settlement date spans over two weekends
						businessDays = daysToSettlement - 4;
					} else {	// // Settlement date spans over a weekend
						businessDays = daysToSettlement - 2;
					}
				} else if (daysToSettlement >= 14) { // Settlement date is longer than two weeks after trade date
					long nextFollowingFriday = Friday + (1000 * 60 * 60 * 24 * 14);
					long extendsOverThreeWeekends = settlementDateLong - nextFollowingFriday;
					//System.out.println(new Date(nextFollowingFriday));
					//System.out.println("Settlement date is after the second following week's Friday: " + extendsOverThreeWeekends);
					if (extendsOverThreeWeekends > 0 && extendsOverThreeWeekends <= 150000000) {	// Settlement date is set to a weekend date
						JOptionPane.showMessageDialog(new JFrame(), "Settlement date is incorrectly set to a weekend date.", "Update Error", JOptionPane.ERROR_MESSAGE);
						return;
					} else if (extendsOverThreeWeekends > 0) {	// Settlement date spans over three weekends
						businessDays = daysToSettlement - 6;
					} else {	// Settlement date spans over two weekends
						businessDays = daysToSettlement - 4;
					}
				}
				businessDays -= holidaysCount;
			}
			
			securitySymbolsArray = new String[100];
			if(!(securityID.getText().length() == 0)) {
				if(!(newContentPane.updateSettlementDate(businessDays, tradeDateLong))) {
					JOptionPane.showMessageDialog(new JFrame(), "None of the securities are selected.", "Update Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				securitySymbolsArray = newContentPane.getSecurities();
				newContentPane = new LocalDBControl(Integer.parseInt(securityID.getText()), false);
			} else if (!(symbol.getText().length() == 0)) {
				if(!(newContentPane.updateSettlementDate(businessDays, tradeDateLong))) {
					JOptionPane.showMessageDialog(new JFrame(), "None of the securities are selected.", "Update Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				securitySymbolsArray = newContentPane.getSecurities();
				newContentPane = new LocalDBControl(symbol.getText(), false);
			} else {
				JOptionPane.showMessageDialog(new JFrame(), "User did not specify securities to be updated.", "Update Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date date = new Date();
			newContentPane.setOpaque(true);
			frame.setContentPane(newContentPane);
			frame.pack();
			frame.setVisible(true);
			if(emailOption.isSelected()) {
				EmailDemo launchEmail = new EmailDemo(businessDays, new Date(tradeDateLong), new Date(settlementDateLong), securitySymbolsArray);
				launchEmail.sendEmail();
				//SendEmailPage emailWindow = new SendEmailPage(tradeDateLong, settlementDateLong, businessDays, securitySymbolsArray);
				//emailWindow.setVisible(true);
			}
			// Invokes PowerShell script to update OmegaDB database (replaced by scheduled PowerShell script on the remote server)
			/*
			String command = "powershell.exe -NoProfile -ExecutionPolicy Bypass -File \"C:\\Users\\kelvin.ng\\Desktop\\Operations Applications\\SettlementTermChanges\\initSettlementTermChanges.ps1\"";
			try {
				Process powerShellProcess = Runtime.getRuntime().exec(command);
			} catch (IOException err) {
				err.printStackTrace();
			}
			*/
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
		labelFont = new Font("Tahoma", Font.BOLD, 16);
		fieldFont = new Font("Tahoma", Font.PLAIN, 16);

		mainFrame = new JFrame("Settlement Term Adjustment");

		mainFrame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent windowEvent){
				System.exit(0);
			}
		});
		contentPane = new JPanel();
		contentPane.setOpaque(true);
		contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		int contentPaneWidth = 500;
		int contentPaneHeight = 750;
		contentPane.setPreferredSize(new Dimension(contentPaneWidth, contentPaneHeight));
		controlPanel = new JPanel();
		controlPanel.setOpaque(true);
		controlPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
		GridLayout contorlLayout = new GridLayout(0,2);
		contorlLayout.setVgap(10);
		contorlLayout.setHgap(10);
		controlPanel.setLayout(contorlLayout);
		int controlPanelWidth = 500;
		int controlPanelHeight = 700;
		controlPanel.setPreferredSize(new Dimension(controlPanelWidth, controlPanelHeight));
		//controlPanel.setBorder(BorderFactory.createTitledBorder(" "));
		contentPane.add(controlPanel);
		// Search Options
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new GridLayout(1, 2));
		firstOption = new JRadioButton("ID");
		firstOption.setFont(fieldFont);
		secondOption = new JRadioButton("Symbol");
		secondOption.setFont(fieldFont);
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
		securityIdLabel.setFont(labelFont);
		securityID = new JTextField();
		securityID.setFont(fieldFont);
		JLabel symbolLabel = new JLabel("Security Symbol", JLabel.LEFT);
		symbolLabel.setFont(labelFont);
		symbol = new JTextField();
		symbol.setFont(fieldFont);
		updateAll = new JCheckBox("Update All");
		updateAll.setHorizontalTextPosition(SwingConstants.RIGHT);
		updateAll.setFont(new Font("Tahoma", Font.PLAIN, 12));
		JPanel searchButtonPanel = new JPanel();
		BorderLayout searchPanelLayout = new BorderLayout();
		searchButtonPanel.setLayout(searchPanelLayout);
		searchButton = new JButton("Search");
		searchButtonPanel.add(searchButton, BorderLayout.SOUTH);
		searchButtonPanel.add(updateAll, BorderLayout.EAST);
		searchButton.setFont(fieldFont);
		securityID.setEnabled(true);
		symbol.setEnabled(false);
		controlPanel.add(securityIdLabel);
		controlPanel.add(securityID);
		controlPanel.add(symbolLabel);
		controlPanel.add(symbol);
		controlPanel.add(new JPanel());
		controlPanel.add(searchButtonPanel);
		// Data Fields
		JLabel tradeDateLabel = new JLabel("Trade Date", JLabel.LEFT);
		tradeDateLabel.setFont(labelFont);
		JLabel settlementDateLabel = new JLabel("Settlement Date", JLabel.LEFT);
		settlementDateLabel.setFont(labelFont);
		JLabel holidaysLabel = new JLabel("Interim Holidays", JLabel.LEFT);
		holidaysLabel.setFont(labelFont);
		timeSpinner = new JSpinner(new SpinnerDateModel());
		refTimeSpinner = new JSpinner(new SpinnerDateModel());
		JSpinner.DateEditor refTimeEditor = new JSpinner.DateEditor(refTimeSpinner, "MM/dd/yyyy");
		refTimeSpinner.setEditor(refTimeEditor);
		refTimeSpinner.setValue(new Date());
		refTimeSpinner.setFont(fieldFont);
		JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "MM/dd/yyyy");
		timeSpinner.setEditor(timeEditor);
		timeSpinner.setValue(new Date());
		timeSpinner.setFont(fieldFont);
		holidays = new JTextField("0");
		holidays.setFont(fieldFont);
		emailOption = new JCheckBox("Require Email Confirmation");
		emailOption.setHorizontalTextPosition(SwingConstants.RIGHT);
		emailOption.setFont(new Font("Tahoma", Font.PLAIN, 12));
		JPanel updateButtonPanel = new JPanel();
		BorderLayout updatePanelLayout = new BorderLayout();
		updateButtonPanel.setLayout(updatePanelLayout);
		updateButton = new JButton("Update");
		updateButtonPanel.add(updateButton, BorderLayout.SOUTH);
		updateButtonPanel.add(emailOption, BorderLayout.EAST);
		updateButton.setFont(fieldFont);
		controlPanel.add(tradeDateLabel);
		controlPanel.add(refTimeSpinner);
		controlPanel.add(settlementDateLabel);
		controlPanel.add(timeSpinner);
		controlPanel.add(holidaysLabel);
		controlPanel.add(holidays);
		controlPanel.add(new JPanel());
		controlPanel.add(updateButtonPanel);
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