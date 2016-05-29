package com.ugame.prophecy.db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 测试用
 */
public class TestMysql {
    private Connection conn;
    
    public TestMysql(String db_file_name_prefix) throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        //@see D:/ugame/src/c/mysql/mysql-5.1.46
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ugame", "root", "");
    }

    public void shutdown() throws SQLException {
        Statement st = conn.createStatement();
        st.execute("drop table sample_table;");
        conn.close();
    }
    
    public synchronized void query(String expression) throws SQLException {
        Statement st = null;
        ResultSet rs = null;
        st = conn.createStatement();
        rs = st.executeQuery(expression);
        dump(rs);
        st.close();
    }

    public synchronized void update(String expression) throws SQLException {
        Statement st = null;
        st = conn.createStatement();
        int i = st.executeUpdate(expression);
        if (i == -1) {
            System.out.println("db error : " + expression);
        }
        st.close();
    }
    
    public static void dump(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colmax = meta.getColumnCount();
        int i;
        Object o = null;
        for (; rs.next(); ) {
            for (i = 0; i < colmax; ++i) {
                o = rs.getObject(i + 1);
                System.out.print(o.toString() + " ");
            }
            System.out.println(" ");
        }
    }

    public static void main(String[] args) {
        TestMysql db = null;
        try {
            //db = new Testdb("db_file");
            //内存数据库，测试用，数据易失
            db = new TestMysql("mem:mymemdb");  
        } catch (Exception ex1) {
            ex1.printStackTrace();
            return;
        }
        try {
            db.update("CREATE TABLE sample_table ( id INTEGER auto_increment not null, str_col VARCHAR(256), num_col INTEGER, primary key(id))");
        } catch (SQLException ex2) {
            ex2.printStackTrace();
        }
        try {
            db.update("INSERT INTO sample_table(str_col,num_col) VALUES('Ford', 100)");
            db.update("INSERT INTO sample_table(str_col,num_col) VALUES('Toyota', 200)");
            db.update("INSERT INTO sample_table(str_col,num_col) VALUES('Honda', 300)");
            db.update("INSERT INTO sample_table(str_col,num_col) VALUES('GM', 400)");
            db.query("SELECT * FROM sample_table WHERE num_col < 250");
            db.shutdown();
        } catch (SQLException ex3) {
            ex3.printStackTrace();
        }
    }
}

