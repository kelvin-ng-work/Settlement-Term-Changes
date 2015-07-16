package security.settlement;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

// Sends email using Outlook account
public class EmailDemo {
	// Account credential
	private final String username = "user@domain";
    private final String password = "password";
    // String array that holds the updated security symbols
    private String[] securitySymbolsArray;
    // Date and time to send the notification email
    private Date tradeDate;
    private Date settlementDate;
    private int settlementTerm;
    
    public EmailDemo(int days, Date dateOfTrade, Date dateOfsettlement, String[] securitySymbols) {
    	securitySymbolsArray = new String[100];
    	securitySymbolsArray = securitySymbols;
    	tradeDate = dateOfTrade;
    	settlementDate = dateOfsettlement;
    	settlementTerm = days;
    }
    
    // Configurations the email and sets the date and time to send the email
    public void sendEmail() {
    	// Email server properties
    	Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "outlook.office365.com");
        props.put("mail.smtp.port", "587");
        // Email session
		Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				});
        try {
        	// Email message body
        	DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        	String messageBody = "Good morning/afternoon,<br><br>";
        	if (settlementTerm == 0) {
        		messageBody += "The following symbol(s), with the trade date of " + dateFormat.format(tradeDate) + ", will be for same day settlement:<br><br>";
        	} else {
        		messageBody += "The following symbol(s), with the trade date of " + dateFormat.format(tradeDate) + ", will be for " + settlementTerm + "-day settlement on " + dateFormat.format(settlementDate) + ":<br><br>";
        	}
        	
        	int count = 0;
        	int newLineCount = 0;
        	// Appends the security symbols to the email message body
        	do {
        		messageBody += securitySymbolsArray[count];
    			if(newLineCount < 10 && (count != securitySymbolsArray.length - 1) && !(securitySymbolsArray[count+1] == null)) {
            		messageBody += ", ";
        			newLineCount++;
        		} else {
        			messageBody += "<br>";
        			newLineCount = 0;
        		}
        		count++;
        	} while (count < securitySymbolsArray.length && !(securitySymbolsArray[count] == null));
        	messageBody += "<br>Best Regards,<br>";
        	messageBody += "OmegaATS";
        	// Configures email settings
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("user@domain"));
            message.setRecipients(Message.RecipientType.TO,
            InternetAddress.parse("user@domain"));
            message.setSubject("Trade and Settlement Date Change(s)");
            message.setContent(messageBody, "text/html");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
    // Main function to run this program alone
	public static void main(String[]args) throws IOException {
			EmailDemo emailLauncher = new EmailDemo(0, new Date(), new Date(), new String[]{"AAA", "BBB", "CCC"});
			emailLauncher.sendEmail();
	}
}