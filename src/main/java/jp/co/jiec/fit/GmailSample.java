package jp.co.jiec.fit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;

import com.google.api.client.auth.oauth2.Credential;
//import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
//import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

public class GmailSample {
	private static String userId = "hagegalka@gmail.com";
	private static final String CLIENT_SECRET_PATH = "client_secret_967384916577-1oj3o2llkngk55f8a3ke4pnpk7uu0m4k.apps.googleusercontent.com.json";

	public static void main(String[] args) throws Exception {
		NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		/*
		FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(new File(System.getProperty("user.home"), ".credentials/gmail.json"));
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory,  new FileReader(CLIENT_SECRET_PATH));
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				  httpTransport, jsonFactory, clientSecrets, Arrays.asList(GmailScopes.GMAIL_READONLY))
		          .setDataStoreFactory(dataStoreFactory)
		          .setAccessType("offline")
		          .build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");*/

		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory,  new FileReader(CLIENT_SECRET_PATH));
		// Allow user to authorize via url.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, Arrays.asList(GmailScopes.GMAIL_READONLY)).setAccessType("online").setApprovalPrompt("auto").build();

		String url = flow.newAuthorizationUrl().setRedirectUri(GoogleOAuthConstants.OOB_REDIRECT_URI).build();
		System.out.println("Please open the following URL in your browser then type the authorization code:\n" + url);

		// Read code entered by user.
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String code = br.readLine();
		//String code = "4/sWyTx12tmZHiz1z1Pkc8j_64jmBm_3p4UZ5f1FnBhmg.ctIROJ1LZHEcgrKXntQAax3YmGYrlgI";

		// Generate Credential using retrieved code.
		GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(GoogleOAuthConstants.OOB_REDIRECT_URI).execute();
		GoogleCredential credential = new GoogleCredential().setFromTokenResponse(response);

	    /*
		GoogleCredential.Builder builder = new GoogleCredential.Builder();
		builder.setTransport(httpTransport);
		builder.setJsonFactory(jsonFactory);
		builder.setServiceAccountPrivateKeyFromP12File(new File("gaeredirect-b304d451590b.p12"));
		builder.setServiceAccountScopes(Collections.singleton(GmailScopes.GMAIL_READONLY));
		builder.setServiceAccountId("967384916577-fsrfsks08h4o7dthfe8mf6vcn72hmf4q@developer.gserviceaccount.com");
		//builder.setServiceAccountId("hagegalka@gmail.com");
		GoogleCredential credential = builder.build();
		*/

		JsonBatchCallback<Message> callback = new JsonBatchCallback<Message>() {
			@Override
			public void onSuccess(Message t, HttpHeaders responseHeaders) throws IOException {
				System.out.println(t.getSnippet());
				System.out.println(t.getLabelIds());
				System.out.println(t.getPayload().getFilename());
				System.out.println(t.getPayload().getMimeType());
				String s = t.getPayload().getBody().getData();
				if(s == null){
					System.out.println("null");
				}else{
					System.out.println(t.getPayload().getPartId());
					//System.out.println(new String(Base64.decodeBase64(s)));
				}
			}
			@Override
			public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
				System.out.println("Error Message: " + e.getMessage());
			}
		};

		Gmail gmail = new Gmail.Builder(httpTransport, jsonFactory, credential).setApplicationName("GmailSample").build();
		BatchRequest batchRequest = gmail.batch();
		ListMessagesResponse list = gmail.users().messages().list(userId).execute();
		for(Message message : list.getMessages()){
			System.out.println(message.toPrettyString());
			gmail.users().messages().get(userId, message.getId()).queue(batchRequest, callback);
		}
		batchRequest.execute();
	}
}