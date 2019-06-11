package com.yztsoft.hotelcompare.task;

import com.yztsoft.hotelcompare.utils.ExcelUtil;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @classname: test
 * @description:
 * @author: Shi Shijie
 * @create: 2019-06-03 09:42
 **/
@Service
public class vert implements ApplicationRunner {
    private static final org.apache.logging.log4j.Logger log= LogManager.getLogger(vert.class);

    final ExecutorService threadPool = Executors.newFixedThreadPool(1);
    ConcurrentHashMap<String, Vector<String>> mapt = null;

    public synchronized void cityName() {
        File file = new File("d:\\22.xlsx");
        for (List<ListOrderedMap> list : ExcelUtil.parseExcel(file)) {
            try {
                List<ListOrderedMap> listresult = new ArrayList<ListOrderedMap>();
                long count = list.stream().distinct().count();
                log.info("--筛选过滤重复数据数量为：[{"+(list.size()-count)+"}]--");
                List<String> citylist = list.stream().map(e -> e.getValue(1).toString()).distinct().filter(m -> !m.equals("City_GUID")).collect(Collectors.toList());

                List<ListOrderedMap> listnoCompare = list.stream().distinct().filter(t ->
                        (t.size()!=5))
                        .collect(Collectors.toList());
                testWrite(listnoCompare,"没有参与比对的数据");
                citylist.forEach(u -> {
                    List<ListOrderedMap> listfilter = list.stream().distinct().filter(t ->
                            (t.size()==5 && u.equals(t.getValue(1))))
                            .collect(Collectors.toList());
                    try {
                        listresult.addAll(removeDuplicate(listfilter,u));
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                if(listresult.size()>0){
                    List<ListOrderedMap> listresult1 = listresult.stream().distinct().filter(s ->
                            (s==null||s.getValue(5)==null||"".equals(s.getValue(5).toString().trim())))
                            .collect(Collectors.toList());
                    testWrite(listresult1,"没有匹配成功的数据");
                    List<ListOrderedMap> listresult2 = listresult.stream().distinct().filter(r ->
                            (r!=null&&r.getValue(5)!=null&&!"".equals(r.getValue(5).toString().trim())))
                            .collect(Collectors.toList());
                    testWrite(listresult2,"过滤筛选重复数据");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public synchronized List<ListOrderedMap> removeDuplicate(List<ListOrderedMap> roomTypes,String q) throws ExecutionException, InterruptedException {
        long now = System.currentTimeMillis();
        List<ListOrderedMap> result = new ArrayList<>();
        //使用分片，多线程处理任务
//        List<List<ListOrderedMap>> listAll = partList(roomTypes, 100);
        //不适用多线程，（需要逐条匹配）
        List<List<ListOrderedMap>> listAll = new ArrayList<>();
        listAll.add(roomTypes);

        mapt = new  ConcurrentHashMap();
        roomTypes.forEach(t->
            mapt.put(t.getValue(0).toString().trim(), participle(t.getValue(2).toString().trim()))
        );
        ArrayList<Future<List<ListOrderedMap>>> results = new ArrayList<Future<List<ListOrderedMap>>>();
        List<vertTask> taskList = new ArrayList<vertTask>();
        for (int task = 0; task < listAll.size(); task++) {
            taskList.add(new vertTask(task,listAll,roomTypes,mapt,result));
        }
        results.addAll(threadPool.invokeAll(taskList));
        log.info((now - System.currentTimeMillis()) + " ms。 ------ "+ q);
        List<ListOrderedMap> listresult = new ArrayList<>();
        if(result.size()>0){
            result = result.stream().distinct().collect(Collectors.toList());
            for(ListOrderedMap t : result){
                if(t!=null&& t.size()>5){
                    if(!t.getValue(0).toString().trim().equals(t.getValue(5).toString().trim())){
                        listresult.add(t);
                    }
                }
            }
        }
        return result;
    }




    public void testWrite(List<ListOrderedMap> list,String filename) throws Exception {
        if(list.size()>0){
            FileOutputStream fos = new FileOutputStream("d:\\wj\\"+filename+".csv");
            OutputStreamWriter osw = new OutputStreamWriter(fos, "GBK");
            CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader("City_Guid","Hotel_Name", "Hotel_Address", "Hotel_Tel", "Hotel_Guid","Target_Hotel_Gud2");
            CSVPrinter csvPrinter = new CSVPrinter(osw, csvFormat);
            for (ListOrderedMap map : list) {
                if(map!=null&&map.size()>0&&map.size()==1){
                    csvPrinter.printRecord("",
                            "",
                            "",
                           "",
                            map.getValue(0)==null?"":map.getValue(0).toString().trim(),
                            "");
                }if(map!=null&&map.size()>0&&map.size()==2){
                    csvPrinter.printRecord(map.getValue(1)==null?"":map.getValue(1).toString().trim(),
                            "",
                            "",
                            "",
                            map.getValue(0)==null?"":map.getValue(0).toString().trim(),
                            "");
                }if(map!=null&&map.size()>0&&map.size()==3){
                    csvPrinter.printRecord(map.getValue(1)==null?"":map.getValue(1).toString().trim(),
                            map.getValue(2)==null?"":map.getValue(2).toString().trim(),
                            "",
                            "",
                            map.getValue(0)==null?"":map.getValue(0).toString().trim(),
                            "");
                }if(map!=null&&map.size()>0&&map.size()==4){
                    csvPrinter.printRecord(map.getValue(1)==null?"":map.getValue(1).toString().trim(),
                            map.getValue(2)==null?"":map.getValue(2).toString().trim(),
                            map.getValue(3)==null?"":map.getValue(3).toString().trim(),
                            "",
                            map.getValue(0)==null?"":map.getValue(0).toString().trim(),
                            "");
                }if(map!=null&&map.size()>0&&map.size()==5){
                    csvPrinter.printRecord(map.getValue(1)==null?"":map.getValue(1).toString().trim(),
                            map.getValue(2)==null?"":map.getValue(2).toString().trim(),
                            map.getValue(3)==null?"":map.getValue(3).toString().trim(),
                            map.getValue(4)==null?"":map.getValue(4).toString().trim(),
                            map.getValue(0)==null?"":map.getValue(0).toString().trim(),
                            "");
                }if(map!=null&&map.size()>0&&map.size()==6){
                    csvPrinter.printRecord(map.getValue(1)==null?"":map.getValue(1).toString().trim(),
                            map.getValue(2)==null?"":map.getValue(2).toString().trim(),
                            map.getValue(3)==null?"":map.getValue(3).toString().trim(),
                            map.getValue(4)==null?"":map.getValue(4).toString().trim(),
                            map.getValue(0)==null?"":map.getValue(0).toString().trim(),
                            map.getValue(5)==null?"":map.getValue(5).toString().trim());
                }
            }
            csvPrinter.flush();
            csvPrinter.close();
        }
    }


    /**
     * @param source
     * @param n      每次分割的个数
     * @return java.util.List<java.util.List       <       T>>
     * @Title: 将list按照指定元素个数(n)分割
     * @methodName: partList
     * @Description: 如果指定元素个数(n)>list.size(),则返回list;这时候商:0；余数:list.size()
     * @author: 王延飞
     * @date: 2018-07-18 21:13
     */
    public static <T> List<List<T>> partList(List<T> source, int n) {

        if (source == null) {
            return null;
        }

        if (n == 0) {
            return null;
        }
        List<List<T>> result = new ArrayList<List<T>>();
        // 集合长度
        int size = source.size();
        // 余数
        int remaider = size % n;
        System.out.println("余数:" + remaider);
        // 商
        int number = size / n;
        System.out.println("商:" + number);

        for (int i = 0; i < number; i++) {
            List<T> value = source.subList(i * n, (i + 1) * n);
            result.add(value);
        }

        if (remaider > 0) {
            List<T> subList = source.subList(size - remaider, size);
            result.add(subList);
        }
        return result;
    }

    /**
     * 分词
     * @param str
     * @return
     * Vector<String>
     * @throws
     * @author miwang
     * @date 2016年12月2日 下午5:56:10
     */
    public static Vector<String> participle(String str) {
        Vector<String> str1 = new Vector<String>() ;//对输入进行分词
        try {
            StringReader reader = new StringReader(str);
            IKSegmenter ik = new IKSegmenter(reader,false);//当为true时，分词器进行智能切分
            Lexeme lexeme = null ;
            while( ( lexeme = ik.next() ) != null ) {
                str1.add( lexeme.getLexemeText() );
            }
            if( str1.size() == 0 ) {
                return null ;
            }
        } catch (IOException e1) {
            log.error("participle fail",e1);
        }
        return str1;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        log.info("----------------酒店重复筛选开始------------------");
        this.cityName();
        log.info("----------------酒店重复筛选结束------------------");
    }
}
