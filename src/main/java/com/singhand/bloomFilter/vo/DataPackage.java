package com.singhand.bloomFilter.vo;


import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Data
@Builder
public class DataPackage<T> {

	private String message;
	private int status;
	private String timestamp;
	private T data; 
	private Long total;
	private String info;


	public static <T> DataPackage<T> buildSuccessfulDataPackage(T data, String info, Long total){
		DataPackage.DataPackageBuilder<T> packageBuilder = DataPackage.<T>builder().status(Code.success.getCode()).
				message(Code.success.getMessage());
		if(!Objects.isNull(data)){
			packageBuilder.data(data);
		}
		if(StringUtils.isNotBlank(info)){
			packageBuilder.info(info);
		}
		if(Objects.nonNull(total)){
			packageBuilder.total(total);
		}
		return packageBuilder.timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)).build();
	}

	public static <T> DataPackage<T> buildSuccessfulDataPackage(T data, String info){
		return buildSuccessfulDataPackage(data, info, null);
	}

	public static <T> DataPackage<T> buildSuccessfulDataPackage(T data){
		return buildSuccessfulDataPackage(data, StringUtils.EMPTY, null);
	}

	public static <T> DataPackage<T> buildParameterErrorDataPackage(){
		return DataPackage.<T>builder().status(Code.parameter_error.getCode()).
				message(Code.parameter_error.getMessage()).
				timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)).build();
	}


	public static <T> DataPackage<T> buildSuchTopicAlreadyExistDataPackage(){
		return DataPackage.<T>builder().status(Code.such_topic_already_exist.getCode()).
				message(Code.such_topic_already_exist.getMessage()).
				timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)).build();
	}

	public static <T> DataPackage<T> buildNoSuchTopicDataPackage(){
		return DataPackage.<T>builder().status(Code.no_such_topic.getCode()).
				message(Code.no_such_topic.getMessage()).
				timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)).build();
	}

}