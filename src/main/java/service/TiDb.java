// Copyright (C) 2019 Meituan
// All rights reserved
package service;

/**
 * @author liming
 * @version 1.0
 * @created 2019/8/6 下午4:52
 **/

import dao.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.context.support.*;
import org.springframework.stereotype.*;
import org.springframework.test.context.*;
import vo.*;

import java.util.*;
import java.util.concurrent.*;

@Component
public  class TiDb{

    static TiDbTestDao tiDbTestDao;

    public static void main(String[] args) {
//        TiDb tidbTest = new TiDb();
//        System.out.println("hello");
//        Map<Integer, List<String>> testMap = new HashMap<Integer, List<String>>();
//        String[] strA = {"A","B"};
//        String[] strC = {"C"};
//        testMap.put(1,new ArrayList<String>(Arrays.asList(strA)));
//        testMap.put(2,new ArrayList<String>(Arrays.asList(strC)));
//        List<List<String>> allSortList = tidbTest.getAllSqlSort(testMap);
//        System.out.println(allSortList.size());

        ApplicationContext APPLICATION_CONTEXT= new ClassPathXmlApplicationContext("classpath:tidb_task_text.xml");
        tiDbTestDao = APPLICATION_CONTEXT.getBean(TiDbTestDao.class);
        TiDb tidbTest = new TiDb();
        tidbTest.start();
    }

    public void start() {
        Map<Integer, List<String>> sqlMap = getAllSqlMap();
        List<List<String>> allSortList = getAllSqlSort(sqlMap);
        //查看打印结果
        if (allSortList != null) {
            int index = 1;
            for (List<String> sqlList: allSortList) {
                if ( sqlList != null) {
                    System.out.println("case" + index++ + ":");
                    for (String sql : sqlList) {
                        System.out.println(sql);
                    }
                }
            }
        }
        //调用方法执行所有的情况
        runSqlCase(allSortList);
    }


    public Map<Integer, List<String>> getAllSqlMap() {
        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<List<String>>> futureList = new ArrayList<Future<List<String>>>();
        System.out.println("请输入模拟TiDb客户端的个数:");
        Scanner scanner = new Scanner(System.in);
        int tiDbClientNum = scanner.nextInt();
        for (int i = 1; i <= tiDbClientNum; ++i) {
            ReadSqlTask readSqlTask = new ReadSqlTask(i);
            Future<List<String>> future = executor.submit(readSqlTask);
            futureList.add(future);
        }
        int i = 1;
        Map<Integer, List<String>> resultMap = new HashMap<Integer, List<String>>();
        try {
            for (Future<List<String>> future : futureList) {
                resultMap.put(i++, future.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    public List<List<String>> getAllSqlSort(Map<Integer, List<String>> sqlMap) {
        List<SqlVo> sqlVoList = getSqlVoList(sqlMap);
        if (sqlVoList == null || sqlVoList.isEmpty()) {
            return Collections.emptyList();
        }
        List<List<String>> result = new ArrayList<>();
        SqlVo[] sqlVos = sqlVoList.toArray(new SqlVo[sqlVoList.size()]);
        List<List<SqlVo>> allSqlSort = new ArrayList<List<SqlVo>>();
        allSort(0, sqlVos, allSqlSort);
        if (allSqlSort.isEmpty()) {
            return Collections.emptyList();
        }
        for (List<SqlVo> list : allSqlSort) {
            List<String> sqlStrList = new ArrayList<>();
            if (legalSort(list, sqlStrList)) {
                result.add(sqlStrList);
            }
        }
        return result;
    }

    /**
     * 用于判断得出的顺序是否合法
     * @param sqlVoList
     * @return
     */
    public boolean legalSort(List<SqlVo> sqlVoList, List<String> sqlStrList) {
        if (sqlVoList == null || sqlVoList.isEmpty() || sqlStrList == null) {
            return false;
        }
        Map<Integer, List<SqlVo>> listMap = new HashMap<>();
        for (SqlVo sqlVo : sqlVoList) {
            sqlStrList.add(sqlVo.getSqlStr());
            if (listMap.containsKey(sqlVo.getFileId())) {
                listMap.get(sqlVo.getFileId()).add(sqlVo);
            } else {
                List<SqlVo> list = new ArrayList<>();
                list.add(sqlVo);
                listMap.put(sqlVo.getFileId(), list);
            }
        }
        //遍历所有的文件sql，看是否按照最初的顺序排列
        for (Integer fileId : listMap.keySet()) {
            int index = 1;
            List<SqlVo> list = listMap.get(fileId);
            for (SqlVo sqlVo : list) {
                if (sqlVo.getIndex() != index++) {
                    return false;
                }
            }
        }
        return true;
    }

    public void allSort(int index, SqlVo[] sqlVos, List<List<SqlVo>> allSqlSort) {
        if (index == sqlVos.length -1) {
            List<SqlVo> strList = new ArrayList<SqlVo>();
            for (int i = 0; i < sqlVos.length; ++i) {
                strList.add(sqlVos[i]);
            }
            allSqlSort.add(strList);
        } else {
            SqlVo temp = null;
            for (int i = index; i < sqlVos.length; i++) {
                temp = sqlVos[i];
                sqlVos[i] = sqlVos[index];
                sqlVos[index] = temp;
                allSort(index + 1, sqlVos, allSqlSort);
                temp = sqlVos[i];
                sqlVos[i] = sqlVos[index];
                sqlVos[index] = temp;
            }
        }
    }

    public List<SqlVo> getSqlVoList(Map<Integer, List<String>> sqlMap) {
        if (sqlMap == null || sqlMap.isEmpty()) {
            return Collections.emptyList();
        }
        List<SqlVo> result = new ArrayList<SqlVo>();
        for (Map.Entry<Integer, List<String>> entry : sqlMap.entrySet()) {
            int index = entry.getKey();
            List<String> sqlList = entry.getValue();
            if (sqlList == null || sqlList.isEmpty()) {
                continue;
            }
            int sqlNum = 1;
            for (String str : sqlList) {
                SqlVo sqlVo = new SqlVo(str, index, sqlNum++);
                result.add(sqlVo);
            }
        }
        return result;
    }

    /**
     * 执行所有的语句
     * @param allSortList
     */
    public void runSqlCase(List<List<String>> allSortList) {
        if (allSortList == null || allSortList.isEmpty()) {
            return;
        }
        for (List<String> sqlList : allSortList) {
            //每个 sqlList执行的表不同
            for (String sql : sqlList) {
                if (checkSqlStr(sql)) {
                    tiDbTestDao.updateTable(sql);
                }
            }
        }
    }

    /**
     * 检查sql语法的合法性，没有实现
     * @param str
     * @return
     */
    public boolean checkSqlStr(String str){
        return true;
    }
}
