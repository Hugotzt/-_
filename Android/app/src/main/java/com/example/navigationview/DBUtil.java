package com.example.navigationview;


import android.util.Log;
import android.widget.Toast;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import com.mysql.jdbc.PreparedStatement;

public class DBUtil {
	
    public static Connection getConnection()
	{
		Connection conn=null;
		try
		{           
           	Class.forName("com.mysql.jdbc.Driver");
           	String url = "jdbc:mysql://192.168.44.1:3306/kunchong?useSSL=false&serverTimezone=UTC";
            conn=DriverManager.getConnection(
                    url,
                    "root",
                    "root");
		}
		catch(Exception e)
		{
			Log.d("",e.toString());
		}
		return conn;
	}
    
    public static void delete(String table, String whereClause, Object[] whereArgs)
    {
    	try
		{
			Connection conn=getConnection();			
			PreparedStatement ps = null;			
	        ps = (PreparedStatement) conn.prepareStatement("DELETE FROM " + table
                    + (!(boolean)(whereClause.equals(""))
                    ? " WHERE " + whereClause : ""));
	        if (whereArgs != null) 
	        {
                int numArgs = whereArgs.length;
                for (int i = 0; i < numArgs; i++) 
                {
                    DBUtil.bindObjectToProgram(ps, i + 1, whereArgs[i]);
                }
	        }      
            ps.executeUpdate();
			ps.close();
			conn.close();

		}
		catch(Exception e)
		{
            Log.d("",e.toString());
		}
    	
    }

    public static void insert(String table,HashMap<String, Object> mValues)
    {
    	try
		{
			Connection conn=getConnection();		
			StringBuilder sql = new StringBuilder();
	        sql.append("INSERT");	       
	        sql.append(" INTO ");
	        sql.append(table);
	       
	        StringBuilder values = new StringBuilder();
	       
	        if (mValues != null && mValues.size() > 0)
	        {
	            
	            Iterator<Entry<String, Object>> entriesIter = mValues.entrySet().iterator();
	            sql.append('(');
	            boolean needSeparator = false;
	            while (entriesIter.hasNext())
	            {
	            	if (needSeparator) 
	            	{
	                    sql.append(", ");
	                    values.append(", ");
	                }
	                needSeparator = true;
	                Entry<String, Object> entry = entriesIter.next();
	                sql.append(entry.getKey());
	                values.append('?');         
	                
	            }
	            sql.append(')');  

	        sql.append(" VALUES(");
	        sql.append(values);
	        sql.append(");");
	        
	        PreparedStatement ps = null;	       
	        ps = (PreparedStatement) conn.prepareStatement(sql.toString());

	            // Bind the values
	            if (mValues != null) 
	            {
	                int size = mValues.size();
	                Iterator<Entry<String, Object>> entriesIter2 = mValues.entrySet().iterator();
	                for (int i = 0; i < size; i++)
	                {
	                    Entry<String, Object> entry = entriesIter2.next();
	                    DBUtil.bindObjectToProgram(ps, i + 1, entry.getValue());
	                }         

	      
		         }
	            
	            ps.executeUpdate();
				ps.close();
				conn.close(); 
	        }
		}
		catch(Exception e)
		{
            Log.d("",e.toString());
		}
		
    }
    
    public static void update(String table, HashMap<String, Object> mValues,String whereClause, Object[] whereArgs) 
    {
       
    	Connection conn=getConnection();

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ");        
        sql.append(table);
        sql.append(" SET ");

        if (mValues != null && mValues.size() > 0)
        {            
           Iterator<Entry<String, Object>> entriesIter = mValues.entrySet().iterator();
           while (entriesIter.hasNext()) 
           {
            Entry<String, Object> entry = entriesIter.next();
            sql.append(entry.getKey());
            sql.append("=?");
            if (entriesIter.hasNext()) 
            {
                sql.append(", ");
            }
           }
        }

        if (!(boolean)(whereClause.equals(""))) {
            sql.append(" WHERE ");
            sql.append(whereClause);
        }

        
        PreparedStatement ps = null;	       
       
        try {
        	 ps = (PreparedStatement) conn.prepareStatement(sql.toString());

            // Bind the values
            int msize = mValues.size();
            Iterator<Entry<String, Object>> entriesIter = mValues.entrySet().iterator();
            int bindArg = 1;
            for (int i = 0; i < msize; i++) {
                Entry<String, Object> entry = entriesIter.next();
                DBUtil.bindObjectToProgram(ps, bindArg, entry.getValue());
                bindArg++;
            }

            if (whereArgs != null) {
                int wsize = whereArgs.length;
                for (int i = 0; i < wsize; i++) {
                	DBUtil.bindObjectToProgram(ps, bindArg,whereArgs[i]);
                    bindArg++;
                }
            }

            // Run the program and then cleanup
            ps.executeUpdate();
			ps.close();
			conn.close();
            
           }
			catch(Exception e)
			{
                Log.d("",e.toString());
			}
    }

    public boolean login(String name,String password) throws SQLException {

        String sql = "select * from users where name = ? and password = ?";

        Connection conn= getConnection();

        try {

            PreparedStatement pst= (PreparedStatement) conn.prepareStatement(sql);
            pst.setString(1,name);
            pst.setString(2,password);

            if(pst.executeQuery().next()){
                return true;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            conn.close();
        }

        return false;
    }

    public boolean register(User user) throws SQLException {

        String sql = "insert into users(name,username,password,age,phone,img) values (?,?,?,?,?,?)";

        Connection conn= getConnection();

        try {
            PreparedStatement pst = (PreparedStatement) conn.prepareStatement(sql);

            pst.setString(1,user.getName());
            pst.setString(2,user.getUsername());
            pst.setString(3,user.getPassword());
            pst.setInt(4,user.getAge());
            pst.setString(5,user.getPhone());
            pst.setBytes(6,user.getImg());

            int value = pst.executeUpdate();

            if(value>0){
                return true;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            conn.close();
        }
        return false;
    }

    public User findUser(String name) throws SQLException {
        // sql 语句
        String sql = "select * from users where name = ?";
        // 创建连接对象
        Connection coon= getConnection();
        // 创建用户对象用于接收返回值
        User user = null;

        try {
            // 创建数据库对象
            PreparedStatement pst= (PreparedStatement) coon.prepareStatement(sql);
            // 传入参数到 "?"
            pst.setString(1,name);
            // 查询语句
            ResultSet rs = pst.executeQuery();

            while (rs.next()){
                int id = rs.getInt(1);
                String namedb = rs.getString(2);
                String username = rs.getString(3);
                String passworddb  = rs.getString(4);
                int age = rs.getInt(5);
                String phone = rs.getString(6);
                byte[] img = rs.getBytes(7);
                user = new User(id,namedb,username,passworddb,age,phone,img);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            coon.close();
        }
        return user;
    }


    public static ResultSet rawQuery(String sql, Object[] selectionArgs) throws SQLException 
    {    
    	Connection conn=getConnection();
    	PreparedStatement ps = null;	       
	    ps = (PreparedStatement) conn.prepareStatement(sql);    	
    	ResultSet rs=null;
    	if (selectionArgs != null) 
        {
            int numArgs = selectionArgs.length;
            for (int i = 0; i < numArgs; i++) 
            {
                DBUtil.bindObjectToProgram(ps, i + 1, selectionArgs[i]);
            }
        }      
        rs=ps.executeQuery();
		
     return rs;
    }

    public static void execSQL(String sql, Object[] selectionArgs) throws SQLException 
    {    
    	 Connection conn=getConnection();
    	 PreparedStatement ps = null;	       
	     ps = (PreparedStatement) conn.prepareStatement(sql);    	
    	
    	if (selectionArgs != null) 
        {
            int numArgs = selectionArgs.length;
            for (int i = 0; i < numArgs; i++) 
            {
                DBUtil.bindObjectToProgram(ps, i + 1, selectionArgs[i]);
            }
        }      
        ps.executeUpdate();
		ps.close();
		conn.close();
     
    }
    

    public static void bindObjectToProgram(PreparedStatement prog, int index,
            Object value) throws SQLException {
        if (value == null) {
        	prog.setString(index, value.toString());
        } else if (value instanceof Double || value instanceof Float) {
            prog.setDouble(index, ((Number)value).doubleValue());
        } else if (value instanceof Number) {
            prog.setLong(index, ((Number)value).longValue());
        } else if (value instanceof Boolean) {
            Boolean bool = (Boolean)value;
            if (bool) {
                prog.setLong(index, 1);
            } else {
                prog.setLong(index, 0);
            }
        } else if (value instanceof byte[]){
            prog.setBytes(index, (byte[]) value);
        } else {
            prog.setString(index, value.toString());
        }
    }

}
