package com.singhand.bloomFilter.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.ContextValueFilter;
import com.singhand.bloomFilter.util.Scale;
import com.singhand.bloomFilter.util.ByteUnitFormat;
import com.singhand.bloomFilter.vo.DataPackage;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
public class BloomFilterController {
	
	private static final Logger LOGGER = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

	public static final ContextValueFilter VALUE_FILTER = (context, object, name, value) -> {
		if (name.equalsIgnoreCase("codec")) {
			return null;
		}
		if(name.equalsIgnoreCase("size") && value instanceof Long){
			return ByteUnitFormat.B.humanReadable(((long)value)/8);
		}
		return value;
	};

	private RedissonClient redissonClient;

	public BloomFilterController(org.redisson.api.RedissonClient redissonClient) {
		this.redissonClient = redissonClient;
	}


	@ResponseBody
	@RequestMapping(value ="/create", method= RequestMethod.POST)
	@PreAuthorize("hasAuthority('ROLE_NORMAL')")
	public DataPackage<Boolean> create(@RequestParam String topic){
		Scale scale = findScale(topic);
		if(Objects.isNull(scale)){
			return DataPackage.buildParameterErrorDataPackage();
		}
		RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(topic);
		boolean ret = bloomFilter.tryInit(scale.getExpectedInsertions(), scale.getFpp());
		if(ret){
			bloomFilter.add(StringUtils.EMPTY);
		}
		return ret ? DataPackage.buildSuccessfulDataPackage(Boolean.TRUE, bloomFilterToJsonStr(bloomFilter)) :
				DataPackage.buildSuchTopicAlreadyExistDataPackage();
	}

	@ResponseBody
	@RequestMapping(value ="/del", method= RequestMethod.POST)
	@PreAuthorize("hasAuthority('ROLE_NORMAL')")
	public DataPackage<Boolean> del(@RequestParam String topic){
		Scale scale = findScale(topic);
		if(Objects.isNull(scale)){
			return DataPackage.buildParameterErrorDataPackage();
		}

		RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(topic);
		if(!bloomFilter.isExists()){
			return DataPackage.buildNoSuchTopicDataPackage();
		}
		bloomFilter.delete();
		return DataPackage.buildSuccessfulDataPackage(!bloomFilter.isExists());
	}


	@ResponseBody
	@RequestMapping(value ="/put", method= RequestMethod.POST)
	@PreAuthorize("hasAuthority('ROLE_NORMAL')")
	public  DataPackage<Boolean> add(@RequestParam String topic,@RequestParam String url){
		Scale scale = findScale(topic);
		if(Objects.isNull(scale) || StringUtils.isBlank(url)){
			return DataPackage.buildParameterErrorDataPackage();
		}

		RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(topic);
		try{
			boolean ret = bloomFilter.add(url);
			return DataPackage.buildSuccessfulDataPackage(ret);
		}catch (IllegalStateException ex){
			if("Bloom filter is not initialized!".equals(ex.getMessage())){
				return DataPackage.buildNoSuchTopicDataPackage();
			}else{
				throw ex;
			}
		}
	}

	@ResponseBody
	@RequestMapping(value ="/contains", method= RequestMethod.POST)
	@PreAuthorize("hasAuthority('ROLE_NORMAL')")
	public synchronized DataPackage<Boolean> contains(@RequestParam String topic, @RequestParam String url) {
		Scale scale = findScale(topic);
		if(Objects.isNull(scale) || StringUtils.isBlank(url)){
			return DataPackage.buildParameterErrorDataPackage();
		}

		RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(topic);
		try{
			boolean ret = bloomFilter.contains(url);
			return DataPackage.buildSuccessfulDataPackage(ret);
		}catch (IllegalStateException ex){
			if("Bloom filter is not initialized!".equals(ex.getMessage())){
				return DataPackage.buildNoSuchTopicDataPackage();
			}else{
				throw ex;
			}
		}
	}

	@ResponseBody
	@RequestMapping(value ="/info", method= RequestMethod.POST)
	@PreAuthorize("hasAuthority('ROLE_NORMAL')")
	public DataPackage<String> info(@RequestParam String topic){
		Scale scale = findScale(topic);
		if(Objects.isNull(scale)){
			return DataPackage.buildParameterErrorDataPackage();
		}

		RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(topic);
		if(!bloomFilter.isExists()){
			return DataPackage.buildNoSuchTopicDataPackage();
		}
		return DataPackage.buildSuccessfulDataPackage(bloomFilterToJsonStr(bloomFilter));
	}

	@ResponseBody
	@RequestMapping(value ="/infoAll", method= RequestMethod.POST)
	@PreAuthorize("hasAuthority('ROLE_NORMAL')")
	public DataPackage<List<String>> infoAll(){
		List<String> collect = redissonClient.getKeys().getKeysStream().
				filter(StringUtils::isNotBlank).
				filter(key -> Objects.nonNull(findScale(key))).
				map(redissonClient::getBloomFilter).
				filter(RBloomFilter::isExists).
				map(this::bloomFilterToJsonStr).
				collect(Collectors.toList());
		return DataPackage.buildSuccessfulDataPackage(collect, StringUtils.EMPTY, (long) collect.size());
	}


	private <T> String bloomFilterToJsonStr(RBloomFilter<T> bloomFilter){
		return Optional.ofNullable(bloomFilter).
				map(bf -> JSON.toJSONString(bf, VALUE_FILTER)).
				map(JSON::parseObject).map(jsonObject -> {
					jsonObject.put("count", bloomFilter.count()-1);
					return jsonObject;
				}).map(jsonObject -> JSON.toJSONString(jsonObject)).orElse(StringUtils.EMPTY);
	}


	private Scale findScale(String topic){
		if(StringUtils.isNotBlank(topic)){
			return Arrays.stream(Scale.values()).
					filter(scale -> topic.trim().endsWith(scale.getSuffix())).
					findFirst().orElse(null);
		}
		return null;
	}

}
