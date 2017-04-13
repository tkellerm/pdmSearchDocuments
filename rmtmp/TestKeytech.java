package de.abasgmh.infosystem.pdmDocuments;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.springframework.boot.json.JacksonJsonParser;


public class TestKeytech {

	
		private static Client client;		
		
		private static String name;
		private static String password;
		
		 private static String REST_SERVICE_SEARCHPRODUCT_URL = "https://{1}/Search?classtypes=DEFAULT_MI&fields=as_mi__name=";
		 private static final String PASS = "pass";
		 private static final String FAIL = "fail";
		 private static String server = "demo.keytech.de"; 
		 
//		@Override
//		public int runFop(FOPSessionContext arg0, String[] arg1)
//				throws FOPException {
//			
//			name ="jgrant";
//			password ="";
//			JSONParser parser = new JSONParser();
//			try {
//				ScreenBuffer screen = BufferFactory.newInstance().getScreenBuffer();
//				ReadableBuffer product = screen.getRefFieldBuffer("yartikel");
//			//	String elementKey = product.getStringValue("nummer");
//				String elementKey = "MISC_FILE:2006513";
//				Object obj = parser.parse(getHTML("https://demo.keytech.de/elements/"+elementKey+"/files"));
//			//	Object obj2 = parser.parse(getHTML("https://demo.keytech.de/elements/"+elementKey,"jgrant",""));
//				JSONObject jsonObj = 	(JSONObject) obj;
//		//		JSONObject jsonObj2 = 	(JSONObject) obj2;
//		//		System.out.println(jsonObj2.toJSONString());
//				JSONArray files = (JSONArray)jsonObj.get("FileInfos");
//				System.out.println(files.toJSONString());
//				
//				checkFiles(files, elementKey);
//		
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}	
//			
//			
//			return 0;
//		}
		public static void main(String[] args) {
		
			name ="jgrant";
			password ="";
			JacksonJsonParser parser = new JacksonJsonParser();
			TestKeytech.init();
			String productnumber = "11";
			TestKeytech.searchProduct(productnumber);
			
			
//			try {
//				ScreenBuffer screen = BufferFactory.newInstance().getScreenBuffer();
//				ReadableBuffer product = screen.getRefFieldBuffer("yartikel");
//			//	String elementKey = product.getStringValue("nummer");
//				String elementKey = "MISC_FILE:2006513";
//				String html = getHTML("https://demo.keytech.de/elements/"+elementKey+"/files");
//				Object obj = parser.parse(getHTML("https://demo.keytech.de/elements/"+elementKey+"/files"));
//			//	
//				JSONObject jsonObj = 	(JSONObject) obj;
//		//		JSONObject jsonObj2 = 	(JSONObject) obj2;
//		//		System.out.println(jsonObj2.toJSONString());
//				JSONArray files = (JSONArray)jsonObj.get("FileInfos");
//				System.out.println(files.toJSONString());
//				
//				checkFiles(files, elementKey);
//		
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
		}
		
		 private static void searchProduct(String productnumber) {
			 
			 String url1 = REST_SERVICE_SEARCHPRODUCT_URL + productnumber;
			 String url2 = url1.replace("{1}", TestKeytech.server);
			 GenericType<List<String>> list = new GenericType<List<String>>() {};
		      List<String> users = client
		         .target(url2)
		         .request()
		         .get(list);
		      
		      String result = PASS;
		      if(users.isEmpty()){
		         result = FAIL;
		         }
		 }

		private static  void init() {
			
			HttpAuthenticationFeature feature = HttpAuthenticationFeature.basicBuilder()
				    .nonPreemptive()
				    .credentials(TestKeytech.name, TestKeytech.password)
				    .build();

				ClientConfig clientConfig = new ClientConfig();
				clientConfig.register(feature) ;

			
				 TestKeytech.client = ClientBuilder.newClient(clientConfig);
			
	
}

//		
	
	
	
}
