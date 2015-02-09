package com.helwigdev.a.dogecoinutilities;

public class PoolWorker {
	String name;
	double hashrate;
	public PoolWorker(String name, double hashrate) {
		this.name = name;
		this.hashrate = hashrate;
	}
	public double getHashrate() {
		return hashrate;
	}
	public void setHashrate(double hashrate) {
		this.hashrate = hashrate;
	}
	public String getName() {
		return name;
	}


}
