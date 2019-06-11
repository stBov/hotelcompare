package com.yztsoft.hotelcompare.task;

import com.yztsoft.hotelcompare.utils.HotelStaticNameUtils;
import com.yztsoft.hotelcompare.utils.SimilarityStringText;
import com.yztsoft.hotelcompare.utils.StringUtils;
import org.apache.commons.collections.map.ListOrderedMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @classname: taskdubbl
 * @description:
 * @author: Shi Shijie
 * @create: 2019-06-04 10:29
 **/
public class vertTask implements Callable<List<ListOrderedMap>> {

    private int index;
    private List<List<ListOrderedMap>> listAll;
    private List<ListOrderedMap> roomTypes;
    private ConcurrentHashMap<String, Vector<String>> ik;
    private List<ListOrderedMap> result;

    public vertTask(int index, List<List<ListOrderedMap>> listAll, List<ListOrderedMap> roomTypes, ConcurrentHashMap<String, Vector<String>> ik, List<ListOrderedMap> result){
        this.index = index;
        this.listAll = listAll;
        this.roomTypes = roomTypes;
        this.ik = ik;
        this.result = result;
    }

    @Override
    public List<ListOrderedMap> call() throws Exception {
        List<ListOrderedMap> temp = listAll.get(index);
        int t = temp.size();
        int m = roomTypes.size();
        for (int i=0; i<t; i++) {
            List<String> stre = new ArrayList<>();
            ListOrderedMap temp2 = new ListOrderedMap();
            temp2 = temp.get(i);
            Vector<String> hotelName1 = ik.get(temp2.getValue(0).toString().trim());
            String hotel = temp2.getValue(2).toString().trim();
            String hotelAddress1 = temp2.getValue(3).toString().trim();
            String hotelTel1 = temp2.getValue(4).toString().trim();
            String hotelUid = temp2.getValue(0).toString().trim();
//            for (int j=0; j<m; j++) {
            for (int j=i; j<m; j++) {
                ListOrderedMap temp1 = new ListOrderedMap();
                temp1 = roomTypes.get(j);
                if (temp1.size() >= 5 && temp2.size() >= 5 &&
                        !hotelUid.equals(temp1.getValue(0).toString().trim()) &&
                        similarityRule(temp1.getValue(2).toString().trim(),
                                hotel,
                                ik.get(temp1.getValue(0).toString().trim()),
                                temp1.getValue(3).toString().trim(),
                                temp1.getValue(4).toString().trim(),
                                hotelName1,
                                hotelAddress1,
                                hotelTel1)) {
                    stre.add(temp1.getValue(0).toString().trim());
                }
            }
            temp2.put("5",stre.stream().collect(Collectors.joining(",")));
            result.add(temp2);
        }
        return result;
    }

    private boolean similarityRule(String trim, String address, Vector<String> hotelName, String hotelAddress, String hotelTel, Vector<String> hotelName1, String hotelAddress1, String hotelTel1) throws Exception {
        boolean nameSame = false;
        float nameScore = 0;
        if (trim.equals(address)) {
            nameSame = true;
        } else {
            nameScore = getSimilarity(hotelName, hotelName1);
            if (nameScore > 0.8) {
                nameSame = true;
            }
        }
        boolean phoneSame = false;
        // 电话相同被认为是同一家酒店
        String dbPhone = hotelTel;
        String phone = hotelTel1;
        if (StringUtils.isNotBlank(phone)) {
            if (phone.startsWith("+86")) {
                phone = phone.replace("+86", "");
            }
            if (phone.length() > 11 && phone.startsWith("86")) {
                phone = phone.replace("86", "");
            }
        }
        if (StringUtils.isNotBlank(phone) && StringUtils.isNotBlank(dbPhone)) {
            phone = HotelStaticNameUtils.digitalNumber(phone);
            dbPhone = HotelStaticNameUtils.digitalNumber(dbPhone);
            if (phone.indexOf(dbPhone) > -1 || dbPhone.indexOf(phone) > -1) {
                phoneSame = true;
            }
        }
        boolean addrSame = false;
        // 地址
        String dbAddr = hotelAddress;
        String addr = hotelAddress1;
        Float score = 0f;
        if (StringUtils.isNotBlank(dbAddr) && StringUtils.isNotBlank(addr)) {
            score = SimilarityStringText.similarity(dbAddr, addr);
            if (score >= 0.75f) {
                addrSame = true;
            }
        } else {
            addrSame = true;
        }
        // 电话和地址是并且的关系
        if ((nameSame && addrSame) || (nameSame && phoneSame) || (nameSame && phoneSame && addrSame)) {
            return true;
        }
        return false;
    }



    // 阈值
    public double YUZHI = 0.2;

    /**
     * 返回百分比
     *
     * @author: Administrator
     * @Date: 2015年1月22日
     * @param T1
     * @param T2
     * @return
     */
    public float getSimilarity(Vector<String> T1, Vector<String> T2) throws Exception {
        int size = 0, size2 = 0;
        if (T1 != null && (size = T1.size()) > 0 && T2 != null && (size2 = T2.size()) > 0) {
            ConcurrentHashMap<String, double[]> T = new ConcurrentHashMap<String, double[]>();
            // T1和T2的并集T
            String index = null;
            for (int i = 0; i < size; i++) {
                index = T1.get(i);
                if (index != null) {
                    double[] c = T.get(index);
                    c = new double[2];
                    c[0] = 1; // T1的语义分数Ci
                    c[1] = YUZHI;// T2的语义分数Ci
                    T.put(index, c);
                }
            }

            for (int i = 0; i < size2; i++) {
                index = T2.get(i);
                if (index != null) {
                    double[] c = T.get(index);
                    if (c != null && c.length == 2) {
                        c[1] = 1; // T2中也存在，T2的语义分数=1
                    } else {
                        c = new double[2];
                        c[0] = YUZHI; // T1的语义分数Ci
                        c[1] = 1; // T2的语义分数Ci
                        T.put(index, c);
                    }
                }
            }
            // 开始计算，百分比
            Iterator<String> it = T.keySet().iterator();
            double s1 = 0, s2 = 0, Ssum = 0; // S1、S2
            while (it.hasNext()) {
                double[] c = T.get(it.next());
                Ssum += c[0] * c[1];
                s1 += c[0] * c[0];
                s2 += c[1] * c[1];
            }
            // 百分比
            return (float) (Ssum / Math.sqrt(s1 * s2));
        } else {
            throw new Exception("传入参数有问题！");
        }
    }

}
