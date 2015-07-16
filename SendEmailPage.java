package security.settlement;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

// Email UI and operations
public class SendEmailPage extends JFrame {
	private JPanel controlPanel;
	private JLabel statusLabel;
	private JSpinner timeSpinner;
	private DateFormat dateFormat;
	private Calendar c;
	private int businessDaysUntilSettlement;
	private Date settlementDate;
	private Date tradeDate;
	private Font font;
	private String[] securitySymbolsArray;
	
    public SendEmailPage(long dateOfTrade, long dateToSettlement, int businessDays, String[] securitySymbols) {
    	securitySymbolsArray = new String[100];
    	securitySymbolsArray = securitySymbols;
    	settlementDate = new Date(dateToSettlement);
    	tradeDate = new Date(dateOfTrade);
    	businessDaysUntilSettlement = businessDays;
        initUI();
    }

    // UI Initialization
    public final void initUI() {
    	// Sets up UI theme
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
    	controlPanel = new JPanel();
    	GridLayout layout = new GridLayout(4,2);
    	layout.setVgap(10);
    	layout.setHgap(10);
    	controlPanel.setLayout(layout);
    	controlPanel.setBorder(new EmptyBorder(new Insets(40, 60, 40, 60)));
    	JLabel settlementLabel = new JLabel("Settlement Date", JLabel.CENTER);
    	settlementLabel.setFont(font);
    	controlPanel.add(settlementLabel);
        JTextField settlementDateDisplay = new JTextField();
        settlementDateDisplay.setFont(font);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        settlementDateDisplay.setText(dateFormat.format(settlementDate));
        controlPanel.add(settlementDateDisplay);
        JLabel deliveryDateTime = new JLabel("Email Delivery", JLabel.CENTER);
        deliveryDateTime.setFont(font);
        controlPanel.add(deliveryDateTime);
        timeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "MM/dd/yyyy hh:mm");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setValue(new Date());
        timeSpinner.setFont(font);
        controlPanel.add(timeSpinner);
        controlPanel.add(new JPanel());
        JButton sendEmailButton = new JButton("OK");
        sendEmailButton.setFont(font);
        sendEmailButton.addActionListener(new sendEmailActionListener());
        controlPanel.add(sendEmailButton);
        controlPanel.add(new JPanel());
        statusLabel = new JLabel("",JLabel.CENTER);
        statusLabel.setFont(font);
        controlPanel.add(statusLabel);
        add(controlPanel);
        pack();
        setTitle("Notification Email");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //setLocationRelativeTo(null);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - getHeight()) / 2);
        setLocation(x, y);
    }
    
    // Button handler to send email
    class sendEmailActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
        	//System.out.println((Date)timeSpinner.getValue());
        	//EmailDemo launchEmail = new EmailDemo(businessDaysUntilSettlement, tradeDate, settlementDate, (Date)timeSpinner.getValue(), securitySymbolsArray);
        	//launchEmail.sendEmail();
        	statusLabel.setText("Email is set to be sent.");
        	//System.out.println((Date)timeSpinner.getValue());
        }
     }
    
    // Main function to run this program alone
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	SendEmailPage emailWindow = new SendEmailPage(100, 100, 3, new String[]{"AAA", "BBB", "CCC"});
            	emailWindow.setVisible(true);
            }
        });
    }
}