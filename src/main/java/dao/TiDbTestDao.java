// Copyright (C) 2019 Meituan
// All rights reserved
package dao;


import org.apache.ibatis.io.*;
import org.apache.ibatis.session.*;
import org.springframework.stereotype.*;

import java.io.*;

/**
 * @author liming
 * @version 1.0
 * @created 2019/8/6 下午5:43
 **/
@Component
public class TiDbTestDao {
    private final String resource = "mybatis-config.xml";
    private SqlSessionFactory sqlSessionFactory;

    public TiDbTestDao() {
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    public int updateTable(String statement) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        int count = sqlSession.update(statement);
        sqlSession.close();
        return count;
    }


}
