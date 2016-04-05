package jp.co.jiec.fit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.fitness.Fitness;
import com.google.api.services.fitness.FitnessScopes;
import com.google.api.services.fitness.model.ListSessionsResponse;

import jp.co.jiec.deltaspike.DeltaSpikeInvoker;

/**
 * Hello world!
 *
 */
public class App implements Runnable{
	/** 秘密鍵ファイル（拡張子が*.p12のやつ） */
	//private static final java.io.File PRIVATE_KEY = new java.io.File("client_secret_947203636832.apps.googleusercontent.com.json");
	private static final java.io.File PRIVATE_KEY = new java.io.File("slim3taira-761c6e7d045b.p12");
	/** サービスアカウントのメールアドレス */
	private static final String SERVICE_ACCOUUNT_EMAIL = "947203636832@developer.gserviceaccount.com";


	public static void main( String[] args ){
		DeltaSpikeInvoker.run(App.class);
	}

	@Override
	public void run() throws RuntimeException{
		try{
			NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
			JsonFactory factory = JacksonFactory.getDefaultInstance();
			GoogleCredential.Builder builder = new GoogleCredential.Builder();
			builder.setTransport(transport);
			builder.setJsonFactory(factory);
			builder.setServiceAccountPrivateKeyFromP12File(PRIVATE_KEY);
			// ↓Googleドライブに対する全てのアクセス権限を要求
			builder.setServiceAccountScopes(Collections.singleton(FitnessScopes.FITNESS_LOCATION_READ));
			// ↓メソッド名は～AccountId()だが、実際にセットするのはメールアドレスなので注意
			builder.setServiceAccountId(SERVICE_ACCOUUNT_EMAIL);
			//GoogleCredential credential = GoogleCredential.fromStream(App.class.getResourceAsStream("/client_secret_947203636832.apps.googleusercontent.com.json")).createScoped(Collections.singleton(FitnessScopes.FITNESS_LOCATION_READ));
			GoogleCredential credential = builder.build();

			Fitness fit = new Fitness.Builder(transport, factory, credential).setApplicationName("FitnessSample").build();
			Fitness.Users users = fit.users();
			System.out.println(users.toString());

			Fitness.Users.Sessions sessions = users.sessions();
			ListSessionsResponse res = sessions.list("hagegalka@gmail.com").execute();
			System.out.println(res.toPrettyString());

			Fitness.Users.Dataset set = users.dataset();
			System.out.println(set.toString());
			Fitness.Users.DataSources source = users.dataSources();
			System.out.println(source.toString());
			Fitness.Users.DataSources.List list = source.list("hagegalka@gmail.com");
			System.out.println(list.toString());
			System.out.println(list.execute().toPrettyString());
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
}