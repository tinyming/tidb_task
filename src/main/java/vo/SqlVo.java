package vo;// Copyright (C) 2019 Meituan
// All rights reserved

/**
 * @author liming
 * @version 1.0
 * @created 2019/8/5 下午4:52
 **/
public class SqlVo {

    private String sqlStr;
    private int index; //同一个文件里的顺序标号
    private int fileId;//文件编号
    public SqlVo(String str,int fileNum, int num) {
        sqlStr = str;
        fileId = fileNum;
        index = num;
    }

    public String getSqlStr() {
        return sqlStr;
    }

    public void setSqlStr(String sqlStr) {
        this.sqlStr = sqlStr;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    @Override
    public String toString() {
        return "SqlVo{" + "sqlStr='" + sqlStr + '\'' + ", index=" + index + ", fileId=" + fileId + '}';
    }
}
