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
	private JCheckBox updateStatus;
	private JTextField securityID;
	private JTextField symbol;
	private JTextField holidays;
	private JSpinner timeSpinner;
	private JSpinner refTimeSpinner;
	private Font font;
	private String[] securitySymbolsArray;
	private long tradeDateLong;
	private long settlementDateLong;
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
			frame.setPreferredSize(new Dimension(800,800));
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
			// Trade date
			tradeDateLong = ((Date)refTimeSpinner.getValue()).getTime();
			// Settlement date
			settlementDateLong = ((Date)timeSpinner.getValue()).getTime();
			// holidays in between trade date and settlement date
			int holidaysCount = Integer.parseInt(holidays.getText());
			// Total days between trade date and settlement date
			long days = (settlementDateLong - tradeDateLong) / (24 * 60 * 60 * 1000);
			int daysToSettlement = (int)days;
			// Trade date is today's date
			if(tradeDateLong % (24 * 60 * 60 * 1000) != 14400000)
			{
				daysToSettlement++;
			}
			// Business days between trade date and settlement date
			int businessDays = daysToSettlement;
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
				//System.out.println("Settlement date is after Friday: " + extendsOverWeekend);
				if (extendsOverWeekend > 0) {
					businessDays = daysToSettlement - 2;
				}
			} else if (daysToSettlement >= 7 && daysToSettlement < 14) { // Settlement date is longer than one week after trade date
				long followingFriday = Friday + (1000 * 60 * 60 * 24 * 7);
				long extendsOverTwoWeekends = settlementDateLong - followingFriday;
				//System.out.println(new Date(followingFriday));
				//System.out.println("Settlement date is after the following week's Friday: " + extendsOverTwoWeekends);
				if (extendsOverTwoWeekends > 0) {
					businessDays = daysToSettlement - 4;
				} else {
					businessDays = daysToSettlement - 2;
				}
			} else if (daysToSettlement >= 14) { // Settlement date is longer than two weeks after trade date
				long nextFollowingFriday = Friday + (1000 * 60 * 60 * 24 * 14);
				long extendsOverThreeWeekends = settlementDateLong - nextFollowingFriday;
				//System.out.println(new Date(nextFollowingFriday));
				//System.out.println("Settlement date is after the second following week's Friday: " + extendsOverThreeWeekends);
				if (extendsOverThreeWeekends > 0) {
					businessDays = daysToSettlement - 6;
				} else {
					businessDays = daysToSettlement - 4;
				}
			}
			businessDays -= holidaysCount;
			securitySymbolsArray = new String[100];
			if(!(securityID.getText().length() == 0)) {
				newContentPane.updateSettlementDate(businessDays, tradeDateLong);
				securitySymbolsArray = newContentPane.getSecurities();
				newContentPane = new LocalDBControl(securityID.getText());
			} else if (!(symbol.getText().length() == 0)) {
				newContentPane.updateSettlementDate(businessDays, tradeDateLong);
				securitySymbolsArray = newContentPane.getSecurities();
				newContentPane = new LocalDBControl(symbol.getText());
			} else {
				JOptionPane.showMessageDialog(new JFrame(), "User did not specify securities to be updated.", "Update Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			updateStatus.setSelected(true);
			DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date date = new Date();
			newContentPane.setOpaque(true);
			frame.setContentPane(newContentPane);
			frame.pack();
			frame.setVisible(true);
			SendEmailPage emailWindow = new SendEmailPage(tradeDateLong, settlementDateLong, businessDays, securitySymbolsArray);
			emailWindow.setVisible(true);
			// Invokes powershell script to update OmegaDB database
			String command = "powershell.exe -NoProfile -ExecutionPolicy Bypass -File \"C:\\Users\\kelvin.ng\\Desktop\\Operations Applications\\SettlementTermChanges\\initSettlementTermChanges.ps1\"";
			try {
				Process powerShellProcess = Runtime.getRuntime().exec(command);
			} catch (IOException err) {
				err.printStackTrace();
			}
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
		font = new Font("Tahoma", Font.PLAIN, 16);
		int frameWidth = 500;
		int frameHeight = 850;
		mainFrame = new JFrame("Trade/Settlement Term Changes");
		mainFrame.setPreferredSize(new Dimension(frameWidth, frameHeight));
		mainFrame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent windowEvent){
				System.exit(0);
			}
		});
		contentPane = new JPanel();
		contentPane.setOpaque(true);
		contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		int contentPaneWidth = 500;
		int contentPaneHeight = 800;
		contentPane.setPreferredSize(new Dimension(contentPaneWidth, contentPaneHeight));
		controlPanel = new JPanel();
		controlPanel.setOpaque(true);
		controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		GridLayout contorlLayout = new GridLayout(9,2);
		contorlLayout.setVgap(10);
		contorlLayout.setHgap(10);
		controlPanel.setLayout(contorlLayout);
		int controlPanelWidth = 450;
		int controlPanelHeight = 750;
		controlPanel.setPreferredSize(new Dimension(controlPanelWidth, controlPanelHeight));
		controlPanel.setBorder(BorderFactory.createTitledBorder(" "));
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
		JLabel tradeDateLabel = new JLabel("Trade Date", JLabel.LEFT);
		tradeDateLabel.setFont(font);
		JLabel settlementDateLabel = new JLabel("Settlement Date", JLabel.LEFT);
		settlementDateLabel.setFont(font);
		JLabel holidaysLabel = new JLabel("Interim Holidays", JLabel.LEFT);
		holidaysLabel.setFont(font);
		timeSpinner = new JSpinner(new SpinnerDateModel());
		refTimeSpinner = new JSpinner(new SpinnerDateModel());
		JSpinner.DateEditor refTimeEditor = new JSpinner.DateEditor(refTimeSpinner, "MM/dd/yyyy");
		refTimeSpinner.setEditor(refTimeEditor);
		refTimeSpinner.setValue(new Date());
		refTimeSpinner.setFont(font);
		JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "MM/dd/yyyy");
		timeSpinner.setEditor(timeEditor);
		timeSpinner.setValue(new Date());
		timeSpinner.setFont(font);
		holidays = new JTextField("0");
		holidays.setFont(font);
		updateButton = new JButton("Update");
		updateButton.setFont(font);
		controlPanel.add(tradeDateLabel);
		controlPanel.add(refTimeSpinner);
		controlPanel.add(settlementDateLabel);
		controlPanel.add(timeSpinner);
		controlPanel.add(holidaysLabel);
		controlPanel.add(holidays);
		controlPanel.add(new JPanel());
		controlPanel.add(updateButton);
		// Status Confirmation
		updateStatus = new JCheckBox("Completed");
		updateStatus.setFont(font);
		updateStatus.setEnabled(false);
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