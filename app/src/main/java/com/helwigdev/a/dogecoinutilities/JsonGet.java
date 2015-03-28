package com.helwigdev.a.dogecoinutilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;

public class JsonGet implements Callable<Object> {

	//TODO rewrite this - legacy, should be deprecated, I think it's only used in Utilities

	public JsonGet(String url, int JSONType){
		this.url = url;
		this.type = JSONType;
	}

	String url;
	int type;
	public static final int JSON_TYPE_OBJECT = 0;
	public static final int JSON_TYPE_ARRAY = 1;

	@Override
	public Object call() throws Exception {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			if(type == JSON_TYPE_OBJECT) {
				return new JSONObject(jsonText);
			} else{
				return new JSONArray(jsonText);
			}
		} finally {
			is.close();
		}
	}

	private String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}



}
