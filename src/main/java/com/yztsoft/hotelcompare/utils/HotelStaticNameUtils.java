/** 
 *@Copyright (c) 宝库技术团队 www.baoku.com
 *@Package com.baoku.hotel.common.utils
 *@Project：baoku-hotel-manager
 *@authur：miwang miwang@baoku.com
 *@date：2016年8月26日 下午1:47:44   
 *@version 1.0
 */
package com.yztsoft.hotelcompare.utils;

import com.google.common.collect.Sets;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *酒店静态信息（地区、名称处理）
 */
public class HotelStaticNameUtils {
	/**
	 * 长度>2 过滤其中名称最后 是区、县、市、盟、自治州的部分
	 * @param name
	 * @return
	 */
	public static String replaceStr(String name) {
		if (StringUtils.isNotBlank(name) && name.length() > 2) {
			name = name.replaceAll("自治州", "");
			name=trimSpc(name);
			String lastName = name.substring(name.length() - 1);
			if (REGIONS.contains(lastName)) {
				name = name.substring(0, name.length() - 1);
			}
		}
		return name;
	}
	
	public static String trimSpc(String txt) {
		if (StringUtils.isNotBlank(txt)) {
			String regex = "（[^）]*）";
			txt = txt.replaceAll(regex, "");
		}
		return txt;
	}
	
	public static final Set<String> REGIONS = Sets.newHashSet("区", "县", "市", "盟");

	
	/**
	 * 过滤特殊字符
	 * 
	 * @param text
	 * @return String
	 */
	public static String stringFilter(String text) {
		// 清除掉所有特殊字符
		if (StringUtils.isNotBlank(text)) {
			String regEx = "[`~!@#$%^&*（）()+=|{}':;',\\-\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、.？_]";
			Pattern p = Pattern.compile(regEx);
			Matcher m = p.matcher(text);
			text = m.replaceAll("").trim();
		}
		return text;
	}

	/**
	 * 抽取数字
	 * @param text
	 * @return
	 */
	public static String digitalNumber(String text) {
		String rs = "";
		try {
			// 只要数字
			if (StringUtils.isNotBlank(text)) {
				String regEx = "\\d";
				Pattern p = Pattern.compile(regEx);
				Matcher m = p.matcher(text);
				while (m.find()) {
					if (!"".equals(m.group())){
						rs += m.group();}
				}
			}
		} catch (Exception e) {
		}
		return rs;
	}
	
	/**
	 * 酒店名称处理部门关键字 用户名称对比
	 * @param hotelName
	 * @return
	 */
	public static String replcae(String hotelName){
		if(StringUtils.isNotBlank(hotelName)){
			hotelName = hotelName.replace("门店", "").replace("酒店", "").replace("店", "");
		}
		return hotelName;
	}
	
	
	/**
	 * 过滤房型名称
	 * 
	 * @param roomName
	 *            房型名称
	 * @return String 过滤后房型名称
	 */
	public static String roomfilterName(String roomName) {
		String result = roomName;
		String[] filterArr = new String[] { "房", "间", "床", "人", "准" };
		//替换中文括号为英文括号
		result = result.replace("（", "(");
		
		// 截取 第一个括号前面的内容
		if (result.indexOf("(") != -1) {
			result = result.substring(0, result.indexOf("("));
		}
		result = stringFilter(result);
		// 过滤关键字
		for (int i = 0; i < filterArr.length; i++) {
			result = result.replace(filterArr[i], "");
		}
		return result;
	}
}
