package com.singhand.bloomFilter.vo;

/**
 *
 */
public enum Code {
	
	parameter_error(401, "Request parameter error " +
			"('topic' should end with '$l' or '$m' or '$s', and 'url' should not be empty)."),
	no_such_topic(402, "The bloom filter of the requested topic does not exist. " +
			"Please call the interface /create to create the bloom filter first."),
	such_topic_already_exist(403,  "The bloom filter of requested topic already exists."),
	success(1, "Request success.");

	private int code;
	private String message;
	
	Code(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

}
