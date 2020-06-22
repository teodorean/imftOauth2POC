package imftOauth2POC.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Security;
import java.security.SecurityPermission;
import java.util.Base64;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.ws.spi.Provider;

//import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.sun.mail.imap.IMAPStore;
import com.sun.mail.smtp.SMTPTransport;

//import com.sun.mail.smtp.SMTPTransport;

public class MainClass {


	//Add these parameters to run the program
	private static final String recipients = "";
	
	public static final String POST_URL = "https://login.microsoftonline.com/axw987.onmicrosoft.com/oauth2/v2.0/token";
	
	public final static String yourUserName = "";
	
	public final static String password="";
	
	public final static String POST_PARAMS = "client_id=5007fd03-0a8c-43b6-9aa3-199a354f3948&"
			+ "scope=offline_access https://outlook.office365.com/SMTP.Send https://outlook.office365.com/Mail.Read"
			+ " https://outlook.office365.com/Mail.Send https://outlook.office365.com/IMAP.AccessAsUser.All&"
			+ "client_secret=GqR2ZhY_PC_l6VEq0C-rxXOZ8.HUHl_GrM&"
			+ "username=" + yourUserName+ "&"
			+ "password=" + password + "&"
			+ "grant_type=password";
	
	
	public static void main(String[] args) throws MessagingException {
		//Security.addProvider(new BouncyCastleProvider());
		try {
			String accessToken = sendPOST();
			readMail(recipients , accessToken, yourUserName);
			sendTestMail(recipients, accessToken, yourUserName);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private static String sendPOST() throws IOException {
		URL obj = new URL(POST_URL);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		//con.setRequestProperty("Host", "login.microsoftonline.com");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		// For POST only - START
		con.setDoOutput(true);
		OutputStream os = con.getOutputStream();
		os.write(POST_PARAMS.getBytes());
		os.flush();
		os.close();
		// For POST only - END

		int responseCode = con.getResponseCode();
		System.out.println("POST Response Code :: " + responseCode);
		StringBuffer response = new StringBuffer();
		if (responseCode == HttpURLConnection.HTTP_OK) { //success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			System.out.println(response.toString());
		} else {
			System.out.println("POST request not worked");
		}
		String [] output = response.toString().split("\"");
		//for (int i=0;i<output.length;i++) {
		//	System.out.println(i +". "+ output[i]);
		//}
		String accessToken = output[15];
		System.out.println(accessToken);
		//accessToken="213";
		return accessToken;
		/*
		try {
			readMail(recipients, accessToken, yourUserName);
			sendTestMail(recipients, accessToken, yourUserName);
			
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	private static void sendTestMail(String tos, String accessToken,String username) throws MessagingException {
        Properties props = new Properties();
       // props.put("mail.imap.ssl.enable", "true"); // required for Gmail
        props.put("mail.smtp.auth.xoauth2.disable","false");
        props.put("mail.smtp.sasl.enable", "true");
        props.put("mail.imap.auth.mechanisms", "XOAUTH2");
        props.put("mail.smtp.auth.mechanisms","XOAUTH2");
        //props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.transport.protocol","smtp");
        //props.put("mail.smtp.host","smtp.office365.com");
        String token = tokenforsmtp(username,accessToken);
        props.put("mail.debug",true);

        Session session = Session.getInstance(props);
        session.setDebug(true);
        try {
            Message m1 = testMessage(username,session,tos);
            SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
            //transport.connect("smtp.office365.com",username,null);
            //transport.issueCommand("AUTH XOAUTH2 " + token, 235);
            transport.connect("smtp.office365.com", username, accessToken);
            transport.sendMessage(m1, m1.getAllRecipients());
            //Store store = session.getStore();
           // store.connect("smtp.office365.com", username, token);

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        //
    }
	
	public static void readMail(String tos, String accessToken, String username) {
		Properties props = new Properties();
		//props.put("mail.imap.ssl.enable", "true"); // required for Gmail
		props.put("mail.imap.auth.xoauth2.disable", "false");
		//props.put("mail.imap.sasl.enable", "true");
		//props.put("mail.imap.sasl.mechanisms", "XOAUTH2");
		props.put("mail.imap.auth.mechanisms", "XOAUTH2");
		//props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
		//props.put("mail.imap.port", "143");
		//props.put("mail.imap.auth.login.disable", "true");
		props.put("mail.imap.auth.plain.disable", "true");
		props.put("mail.imap.starttls.enable", "true");
		//props.put("mail.transport.protocol", "smtp");
		//props.put("mail.smtp.host", "smtp.office365.com");
		String token = tokenforsmtp(username,accessToken);
        props.put("mail.debug",true);
		
        props.put("mail.debug.auth", true);
		Session session = Session.getInstance(props);
		session.setDebug(true);
		try {
			//Store store = session.getStore("imap");
			Store store = session.getStore("imap");
			store.connect("outlook.office365.com",username, accessToken);
			Folder folder = store.getFolder("INBOX");
			if (folder == null || !folder.exists()) {
				System.out.println("BAD FOLDER");
			}
			folder.open(Folder.READ_WRITE);
			int messageCount = folder.getMessageCount();
			System.out.println(messageCount);
			Message msg = folder.getMessage(1);
			try {
				MimeMultipart mes = (MimeMultipart) msg.getContent();
				System.out.println(mes.getBodyPart(1).getInputStream().toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    public static Message testMessage(String from,Session session,String tos) {
        try {
            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            InternetAddress[] recipients = InternetAddress.parse(tos);
            message.setRecipients(Message.RecipientType.TO,
                    recipients);

            // Set Subject: header field
            message.setSubject("O365 email test");

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();

            // Now set the actual message
            messageBodyPart.setText("This is message body");

            // Create a multipar message
            Multipart multipart = new MimeMultipart();

            // Set text message part
            multipart.addBodyPart(messageBodyPart);

            // Part two is attachment
            messageBodyPart = new MimeBodyPart();
            String filename = "C:\\Users\\tdimache\\Pictures\\Screenshots\\Screenshot (1).png";
            DataSource source = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filename);
            multipart.addBodyPart(messageBodyPart);

            // Send the complete message parts
            message.setContent(multipart);
            return message;
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static String tokenforsmtp(String userName, String accessToken) {
        final String ctrlA=Character.toString((char) 1);

        final String coded= "user=" + userName + ctrlA+"auth=Bearer " + accessToken + ctrlA+ctrlA;
        return Base64.getEncoder().encodeToString(coded.getBytes());
        //base64("user=" + userName + "^Aauth=Bearer " + accessToken + "^A^A")
    }

}
