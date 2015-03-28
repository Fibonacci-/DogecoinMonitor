package com.helwigdev.a.dogecoinutilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.Callable;

public class WebDataGet implements Callable<String> {
	//Legacy network code used by Utilities class
	//TODO remove or rewrite as opaque redirection to normalized network call

	public WebDataGet(String url) {
		this.url = url;
	}

	String url;

	@Override
	public String call() throws Exception {
		URL oUrl = new URL(url);
		BufferedReader in = new BufferedReader(new InputStreamReader(oUrl.openStream()));
		String content = "";
		String inputLine;
		while ((inputLine = in.readLine()) != null){
			content += inputLine;
		}
		in.close();
		return content;
	}

}