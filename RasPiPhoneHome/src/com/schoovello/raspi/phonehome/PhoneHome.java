package com.schoovello.raspi.phonehome;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONObject;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class PhoneHome {

	private static final String PI_DEVICE_ID = "mary-xmas";

	public static void main(String[] args) throws Throwable {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));

		phoneHome();
	}

	private static void phoneHome() throws Exception {
		System.out.print("[" + new Date().toString() + "] ");
		System.out.print("Uploading state to http://www.schoovello.com/raspi.php --> ");
		if (postSelfUpdate()) {
			System.out.println("success");
		} else {
			System.out.println("failure");
		}
	}

	private static boolean postSelfUpdate() throws IOException {
		String ip = getInternalIp();
		if (ip == null || ip.length() == 0) {
			return false;
		}

		JSONObject json = new JSONObject();
		json.put("id", PI_DEVICE_ID);
		json.put("internalIp", ip);
		json.put("lastReport", new Date().toString());
		RequestBody body = RequestBody.create(MediaType.parse("application/json"), json.toString());

		Builder builder = new Builder();
		builder.url("http://www.schoovello.com/raspi.php").post(body);
		Request request = builder.build();

		Response response = new OkHttpClient().newCall(request).execute();
		return response.code() == 200;
	}

	private static String getInternalIp() throws SocketException {
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface networkInterface = interfaces.nextElement();
			if (networkInterface.isUp() && !networkInterface.isLoopback()) {
				List<InterfaceAddress> addresses = networkInterface.getInterfaceAddresses();
				for (InterfaceAddress interfaceAddress : addresses) {
					InetAddress inetAddress = interfaceAddress.getAddress();
					if (inetAddress instanceof Inet4Address) {
						return inetAddress.getHostAddress();
					}
				}
			}
		}

		return null;
	}
}
