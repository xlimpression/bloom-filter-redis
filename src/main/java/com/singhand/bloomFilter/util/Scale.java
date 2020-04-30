package com.singhand.bloomFilter.util;

/**
 * 
 *
 */
public enum Scale {
	
	SMALL(10_000_000L, 0.0001D, "$s"),
	MIDDLE(50_000_000L, 0.00001D, "$m"),
	LARGE(100_000_000L, 0.00001D, "$l");
	
	private long expectedInsertions;
	private double fpp;
	
	private String suffix;
	
	Scale(long expectedInsertions, double fpp, String suffix) {
		this.expectedInsertions = expectedInsertions;
		this.fpp = fpp;
		this.suffix = suffix;
	}

	public long getExpectedInsertions() {
		return expectedInsertions;
	}

	public double getFpp() {
		return fpp;
	}

	public String getSuffix() {
		return suffix;
	}
	
}
