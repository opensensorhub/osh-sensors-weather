package org.sensorhub.impl.sensor.fltaware;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.sensorhub.impl.sensor.fltaware.InFlightInfo.Waypoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class FltawareApi
{  
	private final static String BASE_URL = "http://flightxml.flightaware.com/json/FlightXML2/";
	private final static String METAR_URL = BASE_URL + "MetarEx?airport=KAUS&startTime=0&howMany=1&offset=0";
	private final static String InFlightInfo_URL = BASE_URL + "InFlightInfo?";
	//	private final static String API_URL = "http://flightxml.flightaware.com/json/FlightXML2/FlightInfoEx?ident=SKW3300";
	// Not working with faFlightId or ident
	private final static String DecodeFlightRoute_URL = BASE_URL + "DecodeFlightRoute?faFlightID=SKW3300-1506267900-schedule-0000";
	//	private final static String API_URL = "http://flightxml.flightaware.com/json/FlightXML2/DecodeFlightRoute?ident=SWA1878&departureTime=1506101700";
	private final static String GetFlightID_URL = BASE_URL + "GetFlightID?ident=SWA1878&departureTime=1506101700";
	private final static String Enroute_URL = BASE_URL + "Enroute?airport=KAUS&howMany=10&filter=airline";
	private final static String Scheduled_URL = BASE_URL + "Scheduled?airport=KAUS&howMany=10&filter=airline";
	private final static String AirlineInfo_URL = BASE_URL + "AirlineInfo?airlineCode=DAL";
	String user = "drgregswilson";
	//	String euser = "earthcast";
	String passwd = "2809b6196a2cfafeb89db0a00b117ac67e876220";
	static Log log = LogFactory.getLog(Class.class);

	public FltawareApi() {
	}

	public static String toJson(Object object) {
		Gson gson = new  GsonBuilder().setPrettyPrinting().create();
		String json =  gson.toJson(object);
		System.out.println(json);
		return json;
	}

	public String invokeNew(String url, String ... args) throws ClientProtocolException, IOException {
		for(String arg: args) {
			url = url + arg + ",";
		}
		if(url.endsWith(","))
			url = url.substring(0, url.length() - 1);
		HttpGet request = new HttpGet(url);
		String auth = user + ":" + passwd;
		byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("ISO-8859-1")));
		String authHeader = "Basic " + new String(encodedAuth);
		request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

		HttpClient client = HttpClientBuilder.create().build();
		HttpResponse response = client.execute(request);

		int statusCode = response.getStatusLine().getStatusCode();
		String resp = EntityUtils.toString(response.getEntity());
		System.err.println(statusCode);
		System.err.println(resp);
		return resp;
	}

	public void printJson(String json) {
		JsonParser parser = new JsonParser();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		JsonElement el = parser.parse(json);
		json = gson.toJson(el); // done
		System.err.println(json);
	}

	public FlightAwareResult fromJson(String json, Class<? extends FlightAwareResult> clazz) {
		Gson gson = new Gson();
		FlightAwareResult info = gson.fromJson(json, clazz);
		return info;
	}

	public static void main(String[] args) throws Exception {
		log.warn("Logging Works");
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");

		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");

		FltawareApi api = new FltawareApi();
		String json = api.invokeNew(DecodeFlightRoute_URL); 
		DecodeFlightResult decodedInfo = (DecodeFlightResult)api.fromJson(json, DecodeFlightResult.class);
		System.err.println(decodedInfo);

		json = api.invokeNew(InFlightInfo_URL, "ident=SWA5437");
		InFlightInfo info = (InFlightInfo) api.fromJson(json, InFlightInfo.class);
		Instant depTime = Instant.ofEpochSecond(info.InFlightInfoResult.departureTime);
		System.err.println(info.InFlightInfoResult.ident + " departed from: " + info.InFlightInfoResult.destination + " at " + depTime);
		api.printJson(json);
//				List<Waypoint> waypoints = info.createWaypoints();
//				for(Waypoint p: waypoints) 
//					System.err.println(p);
	}

}