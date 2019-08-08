package service;// Copyright (C) 2019 Meituan
// All rights reserved

import vo.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author liming
 * @version 1.0
 * @created 2019/8/5 下午12:57
 **/
public class ReadSqlTask implements Callable<List<String>> {
    private int fileNum;

    private final String fileSuffix = "./src/main/webapp/sql_text_";

    public ReadSqlTask(int num) {
        fileNum = num;
    }

    public List<String> call() throws Exception {
        String fileName = fileSuffix + fileNum + ".txt";
        List<String> sqlList = new ArrayList<String>();
        FileReader reader = null;
        BufferedReader buffer = null;
        try {
            reader = new FileReader(fileName);
            buffer = new BufferedReader(reader);
            String sqlStr = null;
            while ((sqlStr = buffer.readLine()) != null) {
                sqlList.add(sqlStr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (buffer != null) {
                    buffer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sqlList;
    }

}
