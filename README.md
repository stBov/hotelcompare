# hotelcompare
匹配算法：
根据“酒店名称”“酒店地址”“酒店电话” 三字段比填。
“酒店名称”匹配，根据IK分词器，通过结果集匹配，再除总匹配数，得出一个百分比，如果大于80%，则为同一家。返回true
“酒店电话”匹配，去掉+86使用数字匹配算法。 返回true
“酒店地址”匹配，字符串匹配算法。直接匹配。返回true
上述三个匹配算法两两匹配，如果其中有一个为true，则匹配成功，为重复酒店数据。
