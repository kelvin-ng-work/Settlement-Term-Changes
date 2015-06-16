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
    private Date sentDate;
    private int daysUntilNotification;
    
    public EmailDemo(int days, Date date, String[] securitySymbols) {
    	securitySymbolsArray = new String[100];
    	securitySymbolsArray = securitySymbols;
    	sentDate = date;
    	daysUntilNotification = days;
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
        	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        	String messageBody = "Good morning/afternoon,<br><br>";
        	messageBody += "We updated the settlement dates of the following securities to " + daysUntilNotification + " days.";
        	messageBody += "Please be notified that these securities will be settled on " + dateFormat.format(sentDate) + ".<br><br>";
        	messageBody += "<b>Updated Securities:</b><br><br>";
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
        	messageBody += "<br><br>Best Regards,<br>";
        	messageBody += "OmegaATS";
        	// Configures email settings
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("user@domain"));
            message.setRecipients(Message.RecipientType.TO,
            InternetAddress.parse("user@domain"));
            message.setSubject("Settlement Change Notice");
            message.setContent(messageBody, "text/html");
            message.setSentDate(sentDate);
            Transport.send(message);
            System.out.println("Done");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
    // Main function to run this program alone
	public static void main(String[]args) throws IOException {
			EmailDemo emailLauncher = new EmailDemo(0, new Date(), new String[]{"AAA", "BBB", "CCC"});
			emailLauncher.sendEmail();
	}
}